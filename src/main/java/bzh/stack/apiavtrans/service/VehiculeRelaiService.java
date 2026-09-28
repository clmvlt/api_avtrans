package bzh.stack.apiavtrans.service;

import bzh.stack.apiavtrans.dto.vehicule.VehiculeRelaiCreateRequest;
import bzh.stack.apiavtrans.dto.vehicule.VehiculeRelaiDTO;
import bzh.stack.apiavtrans.dto.vehicule.VehiculeRelaiUpdateRequest;
import bzh.stack.apiavtrans.entity.Vehicule;
import bzh.stack.apiavtrans.entity.VehiculeKilometrage;
import bzh.stack.apiavtrans.entity.VehiculeRelai;
import bzh.stack.apiavtrans.mapper.VehiculeRelaiMapper;
import bzh.stack.apiavtrans.repository.VehiculeKilometrageRepository;
import bzh.stack.apiavtrans.repository.VehiculeRelaiRepository;
import bzh.stack.apiavtrans.repository.VehiculeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Véhicules relais : un relais remplace temporairement un véhicule de la flotte. Pendant sa
 * période (dates incluses), les relevés kilométriques saisis sur le véhicule lui sont rattachés,
 * pour ne fausser ni le kilométrage du véhicule ni ses alertes d'entretien.
 */
@Service
@RequiredArgsConstructor
public class VehiculeRelaiService {

    private static final ZoneId PARIS = ZoneId.of("Europe/Paris");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    /** Fin des périodes sans date de fin. */
    private static final LocalDate SANS_FIN = LocalDate.of(9999, 12, 31);
    private static final UUID AUCUN_ID = new UUID(0L, 0L);
    private static final int IMMAT_MAX = 20;

    private final VehiculeRelaiRepository vehiculeRelaiRepository;
    private final VehiculeRepository vehiculeRepository;
    private final VehiculeKilometrageRepository vehiculeKilometrageRepository;
    private final VehiculeRelaiMapper vehiculeRelaiMapper;

    @Transactional(readOnly = true)
    public List<VehiculeRelaiDTO> getRelaisByVehicule(UUID vehiculeId) {
        Vehicule vehicule = findVehicule(vehiculeId);
        return vehiculeRelaiRepository.findByVehiculeOrderByDateDebutDesc(vehicule).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public VehiculeRelaiDTO getRelaiById(UUID id) {
        return toDTO(findRelai(id));
    }

    @Transactional
    public VehiculeRelaiDTO createRelai(VehiculeRelaiCreateRequest request) {
        if (request.getVehiculeId() == null) {
            throw new RuntimeException("Le véhicule est obligatoire");
        }
        Vehicule vehicule = findVehicule(request.getVehiculeId());

        VehiculeRelai relai = new VehiculeRelai();
        relai.setVehicule(vehicule);
        appliquer(relai, request.getImmat(), request.getMarque(), request.getModele(),
                request.getDateDebut(), request.getDateFin(), request.getKmDebut(), request.getKmFin(),
                request.getMotif(), request.getCommentaire());
        verifierChevauchement(relai);

        // L'ancienne plaque relais saisie à la main est remplacée par le suivi des relais
        if (vehicule.getRelaiImmat() != null) {
            vehicule.setRelaiImmat(null);
            vehiculeRepository.save(vehicule);
        }

        VehiculeRelai saved = vehiculeRelaiRepository.save(relai);
        rattacherReleves(saved);
        return toDTO(saved);
    }

    @Transactional
    public VehiculeRelaiDTO updateRelai(UUID id, VehiculeRelaiUpdateRequest request) {
        VehiculeRelai relai = findRelai(id);
        appliquer(relai, request.getImmat(), request.getMarque(), request.getModele(),
                request.getDateDebut(), request.getDateFin(), request.getKmDebut(), request.getKmFin(),
                request.getMotif(), request.getCommentaire());
        verifierChevauchement(relai);

        VehiculeRelai saved = vehiculeRelaiRepository.save(relai);
        vehiculeKilometrageRepository.detacherDuRelai(saved);
        rattacherReleves(saved);
        return toDTO(saved);
    }

    /** Supprime le relais ; ses relevés redeviennent des relevés du véhicule. */
    @Transactional
    public void deleteRelai(UUID id) {
        VehiculeRelai relai = findRelai(id);
        vehiculeKilometrageRepository.detacherDuRelai(relai);
        relai.getVehicule().getRelais().remove(relai);
        vehiculeRelaiRepository.delete(relai);
    }

    /** Relais du véhicule en cours à cette date, ou null. */
    @Transactional(readOnly = true)
    public VehiculeRelai relaiActifLe(Vehicule vehicule, LocalDate date) {
        return vehiculeRelaiRepository.findActifLe(vehicule, date).orElse(null);
    }

    /** Relais en cours aujourd'hui, par identifiant de véhicule. */
    @Transactional(readOnly = true)
    public Map<UUID, VehiculeRelai> relaisEnCoursParVehicule() {
        return vehiculeRelaiRepository.findAllActifsLe(aujourdhui()).stream()
                .collect(Collectors.toMap(r -> r.getVehicule().getId(), Function.identity(), (a, b) -> a));
    }

    /** Le véhicule a-t-il déjà eu un relais déclaré (l'ancienne plaque relais est alors ignorée) ? */
    @Transactional(readOnly = true)
    public boolean aDesRelais(Vehicule vehicule) {
        return vehiculeRelaiRepository.existsByVehicule(vehicule);
    }

    /** DTO avec le dernier relevé, le nombre de relevés et le statut du jour. */
    @Transactional(readOnly = true)
    public VehiculeRelaiDTO toDTO(VehiculeRelai relai) {
        VehiculeKilometrage dernier = vehiculeKilometrageRepository.findLatestByRelai(relai).orElse(null);
        long nbReleves = vehiculeKilometrageRepository.countByRelai(relai);
        return vehiculeRelaiMapper.toDTO(relai, dernier, nbReleves, aujourdhui());
    }

    // ── Règles ──

    private void appliquer(VehiculeRelai relai, String immat, String marque, String modele,
                           LocalDate dateDebut, LocalDate dateFin, Integer kmDebut, Integer kmFin,
                           String motif, String commentaire) {
        String plaque = immat != null ? immat.trim().toUpperCase() : "";
        if (plaque.isEmpty()) {
            throw new RuntimeException("L'immatriculation du véhicule relais est obligatoire");
        }
        if (plaque.length() > IMMAT_MAX) {
            throw new RuntimeException("L'immatriculation du véhicule relais ne doit pas dépasser " + IMMAT_MAX + " caractères");
        }
        if (dateDebut == null) {
            throw new RuntimeException("La date de début du relais est obligatoire");
        }
        if (dateFin != null && dateFin.isBefore(dateDebut)) {
            throw new RuntimeException("La date de fin du relais doit être postérieure ou égale à la date de début");
        }
        if ((kmDebut != null && kmDebut < 0) || (kmFin != null && kmFin < 0)) {
            throw new RuntimeException("Le kilométrage ne peut pas être négatif");
        }
        if (kmDebut != null && kmFin != null && kmFin < kmDebut) {
            throw new RuntimeException("Le kilométrage au retour doit être supérieur ou égal au kilométrage au départ");
        }

        relai.setImmat(plaque);
        relai.setMarque(texte(marque));
        relai.setModele(texte(modele));
        relai.setDateDebut(dateDebut);
        relai.setDateFin(dateFin);
        relai.setKmDebut(kmDebut);
        relai.setKmFin(kmFin);
        relai.setMotif(texte(motif));
        relai.setCommentaire(texte(commentaire));
    }

    private void verifierChevauchement(VehiculeRelai relai) {
        List<VehiculeRelai> conflits = vehiculeRelaiRepository.findChevauchements(
                relai.getVehicule(),
                relai.getId() != null ? relai.getId() : AUCUN_ID,
                relai.getDateDebut(),
                relai.getDateFin() != null ? relai.getDateFin() : SANS_FIN);
        if (!conflits.isEmpty()) {
            VehiculeRelai conflit = conflits.get(0);
            String periode = conflit.getDateFin() != null
                    ? "du " + conflit.getDateDebut().format(DATE_FORMAT) + " au " + conflit.getDateFin().format(DATE_FORMAT)
                    : "depuis le " + conflit.getDateDebut().format(DATE_FORMAT) + ", sans date de fin";
            throw new RuntimeException("Ce véhicule a déjà un relais sur cette période : "
                    + conflit.getImmat() + " " + periode);
        }
    }

    /** Rattache au relais les relevés du véhicule saisis pendant sa période (dates incluses). */
    private void rattacherReleves(VehiculeRelai relai) {
        ZonedDateTime debut = relai.getDateDebut().atStartOfDay(PARIS);
        ZonedDateTime fin = (relai.getDateFin() != null ? relai.getDateFin().plusDays(1) : SANS_FIN).atStartOfDay(PARIS);
        vehiculeKilometrageRepository.rattacherAuRelai(relai, relai.getVehicule(), debut, fin);
    }

    private Vehicule findVehicule(UUID vehiculeId) {
        return vehiculeRepository.findById(vehiculeId)
                .orElseThrow(() -> new RuntimeException("Véhicule non trouvé avec l'ID : " + vehiculeId));
    }

    private VehiculeRelai findRelai(UUID id) {
        return vehiculeRelaiRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Relais non trouvé avec l'ID : " + id));
    }

    private static String texte(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static LocalDate aujourdhui() {
        return LocalDate.now(PARIS);
    }
}
