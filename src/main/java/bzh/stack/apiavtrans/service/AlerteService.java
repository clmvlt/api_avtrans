package bzh.stack.apiavtrans.service;

import bzh.stack.apiavtrans.dto.entretien.AlerteEntretienDTO;
import bzh.stack.apiavtrans.dto.entretien.VehiculeProchainEntretienDTO;
import bzh.stack.apiavtrans.entity.*;
import bzh.stack.apiavtrans.mapper.EntretienMapper;
import bzh.stack.apiavtrans.mapper.TypeEntretienMapper;
import bzh.stack.apiavtrans.mapper.VehiculeMapper;
import bzh.stack.apiavtrans.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AlerteService {

    private static final ZoneId PARIS = ZoneId.of("Europe/Paris");

    /** Échelles qui rendent comparables une échéance en km et une échéance en jours (seuils « à prévoir » des clients). */
    private static final double ECHELLE_KM = 10000d;
    private static final double ECHELLE_JOURS = 90d;

    /** La plus urgente d'abord : retards, puis échéances les plus proches. */
    private static final Comparator<AlerteEntretienDTO> PAR_URGENCE =
            Comparator.comparingDouble(AlerteService::urgence);

    private final VehiculeRepository vehiculeRepository;
    private final VehiculeKilometrageRepository vehiculeKilometrageRepository;
    private final VehiculeTypeEntretienRepository vehiculeTypeEntretienRepository;
    private final EntretienRepository entretienRepository;
    private final TypeEntretienMapper typeEntretienMapper;
    private final EntretienMapper entretienMapper;
    private final VehiculeMapper vehiculeMapper;

    @Transactional(readOnly = true)
    public List<AlerteEntretienDTO> getAlertesForVehicule(UUID vehiculeId) {
        Vehicule vehicule = vehiculeRepository.findById(vehiculeId)
                .orElseThrow(() -> new RuntimeException("Véhicule non trouvé avec l'ID : " + vehiculeId));

        VehiculeKilometrage latestKm = vehiculeKilometrageRepository
                .findLatestByVehicule(vehicule)
                .orElse(null);

        return buildAlertes(vehicule, latestKm);
    }

    @Transactional(readOnly = true)
    public List<AlerteEntretienDTO> getAllAlertes() {
        List<Vehicule> vehicules = vehiculeRepository.findAll();
        List<AlerteEntretienDTO> alertes = new ArrayList<>();

        for (Vehicule vehicule : vehicules) {
            VehiculeKilometrage latestKm = vehiculeKilometrageRepository
                    .findLatestByVehicule(vehicule)
                    .orElse(null);

            alertes.addAll(buildAlertes(vehicule, latestKm));
        }

        return alertes;
    }

    /** Une échéance par récurrence active du véhicule, la plus urgente d'abord. */
    private List<AlerteEntretienDTO> buildAlertes(Vehicule vehicule, VehiculeKilometrage latestKm) {
        Integer kilometrageActuel = latestKm != null ? latestKm.getKm() : 0;

        List<VehiculeTypeEntretien> configurations = vehiculeTypeEntretienRepository.findByVehiculeAndActifTrue(vehicule);
        List<AlerteEntretienDTO> alertes = new ArrayList<>();

        for (VehiculeTypeEntretien config : configurations) {
            AlerteEntretienDTO alerte = calculateAlerte(vehicule, latestKm, config, kilometrageActuel);
            if (alerte != null) {
                alertes.add(alerte);
            }
        }

        alertes.sort(PAR_URGENCE);
        return alertes;
    }

    private static double urgence(AlerteEntretienDTO alerte) {
        return alerte.getKmRestants() != null
                ? alerte.getKmRestants() / ECHELLE_KM
                : alerte.getJoursRestants() / ECHELLE_JOURS;
    }

    private AlerteEntretienDTO calculateAlerte(Vehicule vehicule, VehiculeKilometrage latestKm, VehiculeTypeEntretien config, Integer kilometrageActuel) {
        List<Entretien> entretiens = entretienRepository
                .findByVehiculeAndTypeEntretienOrderByDateEntretienDesc(vehicule, config.getTypeEntretien().getId());

        Entretien dernierEntretien = entretiens.isEmpty() ? null : entretiens.get(0);

        AlerteEntretienDTO alerte = new AlerteEntretienDTO();
        alerte.setVehicule(vehiculeMapper.toDTO(vehicule, latestKm));
        alerte.setTypeEntretien(typeEntretienMapper.toDTO(config.getTypeEntretien()));
        alerte.setDernierEntretien(dernierEntretien != null ? entretienMapper.toDTO(dernierEntretien) : null);
        alerte.setPeriodiciteType(config.getPeriodiciteType());
        alerte.setPeriodiciteValeur(config.getPeriodiciteValeur());

        if (config.getPeriodiciteType() == VehiculeTypeEntretien.PeriodiciteType.KILOMETRAGE) {
            return calculateAlerteKilometrage(alerte, vehicule, dernierEntretien, config, kilometrageActuel);
        } else if (config.getPeriodiciteType() == VehiculeTypeEntretien.PeriodiciteType.TEMPOREL) {
            return calculateAlerteTemporelle(alerte, dernierEntretien, config);
        }

        return null;
    }

    private AlerteEntretienDTO calculateAlerteKilometrage(AlerteEntretienDTO alerte, Vehicule vehicule, Entretien dernierEntretien,
                                                           VehiculeTypeEntretien config, Integer kilometrageActuel) {
        int reference = dernierEntretien != null
                ? dernierEntretien.getKilometrage()
                : kilometrageALaCreation(vehicule, config);

        Integer prochainKm = reference + config.getPeriodiciteValeur();
        Integer kmRestants = prochainKm - kilometrageActuel;

        alerte.setProchainKilometrage(prochainKm);
        alerte.setKmRestants(kmRestants);
        alerte.setEnRetard(kmRestants <= 0);
        alerte.setMessage(buildMessageKilometrage(kmRestants, alerte.getEnRetard()));

        return alerte;
    }

    /**
     * Sans entretien réalisé, la récurrence part du kilométrage connu à la création de la configuration
     * (dernier relevé d'avant, sinon le premier relevé d'après, sinon 0).
     */
    private int kilometrageALaCreation(Vehicule vehicule, VehiculeTypeEntretien config) {
        Optional<VehiculeKilometrage> releve = vehiculeKilometrageRepository
                .findLatestByVehiculeAtOrBefore(vehicule, config.getCreatedAt());
        if (releve.isEmpty()) {
            releve = vehiculeKilometrageRepository.findEarliestByVehicule(vehicule);
        }
        return releve.map(VehiculeKilometrage::getKm).orElse(0);
    }

    private AlerteEntretienDTO calculateAlerteTemporelle(AlerteEntretienDTO alerte, Entretien dernierEntretien,
                                                          VehiculeTypeEntretien config) {
        // Sans entretien réalisé, la récurrence part de la création de la configuration
        ZonedDateTime reference = dernierEntretien != null ? dernierEntretien.getDateEntretien() : config.getCreatedAt();
        ZonedDateTime prochaineDateTemporelle = reference.plusDays(config.getPeriodiciteValeur());

        LocalDate aujourdhui = LocalDate.now(PARIS);
        LocalDate echeance = prochaineDateTemporelle.withZoneSameInstant(PARIS).toLocalDate();
        long joursRestants = ChronoUnit.DAYS.between(aujourdhui, echeance);

        alerte.setProchaineDateTemporelle(prochaineDateTemporelle);
        alerte.setJoursRestants(joursRestants);
        alerte.setEnRetard(joursRestants < 0);
        alerte.setMessage(buildMessageTemporel(joursRestants, alerte.getEnRetard()));

        return alerte;
    }

    private String buildMessageKilometrage(Integer kmRestants, Boolean enRetard) {
        if (enRetard) {
            return "URGENT : Entretien en retard de " + Math.abs(kmRestants) + " km";
        } else if (kmRestants <= 5000) {
            return "Entretien à prévoir dans " + kmRestants + " km";
        }
        return "Prochain entretien dans " + kmRestants + " km";
    }

    private String buildMessageTemporel(Long joursRestants, Boolean enRetard) {
        if (enRetard) {
            return "URGENT : Entretien en retard de " + libelleJours(Math.abs(joursRestants));
        } else if (joursRestants == 0) {
            return "Entretien à prévoir aujourd'hui";
        } else if (joursRestants <= 30) {
            return "Entretien à prévoir dans " + libelleJours(joursRestants);
        }
        return "Prochain entretien dans " + libelleJours(joursRestants);
    }

    private String libelleJours(long jours) {
        return jours + (jours > 1 ? " jours" : " jour");
    }

    @Transactional(readOnly = true)
    public List<VehiculeProchainEntretienDTO> getVehiculesAvecProchainsEntretiens() {
        List<Vehicule> vehicules = vehiculeRepository.findAll();
        List<VehiculeProchainEntretienDTO> resultat = new ArrayList<>();

        for (Vehicule vehicule : vehicules) {
            resultat.add(buildProchainEntretienDTO(vehicule));
        }

        return resultat;
    }

    @Transactional(readOnly = true)
    public VehiculeProchainEntretienDTO getProchainEntretienForVehicule(UUID vehiculeId) {
        Vehicule vehicule = vehiculeRepository.findById(vehiculeId)
                .orElseThrow(() -> new RuntimeException("Véhicule non trouvé avec l'ID : " + vehiculeId));

        return buildProchainEntretienDTO(vehicule);
    }

    private VehiculeProchainEntretienDTO buildProchainEntretienDTO(Vehicule vehicule) {
        VehiculeKilometrage latestKm = vehiculeKilometrageRepository
                .findLatestByVehicule(vehicule)
                .orElse(null);

        List<AlerteEntretienDTO> alertes = buildAlertes(vehicule, latestKm);

        VehiculeProchainEntretienDTO dto = new VehiculeProchainEntretienDTO();
        dto.setVehicule(vehiculeMapper.toDTO(vehicule, latestKm));
        dto.setAlertes(alertes);

        // Champs historiques, conservés pour les versions de l'application mobile déjà installées
        dto.setProchainEntretienKm(alertes.stream()
                .filter(a -> a.getKmRestants() != null)
                .min(Comparator.comparingInt(AlerteEntretienDTO::getKmRestants))
                .orElse(null));
        dto.setProchainEntretienDate(alertes.stream()
                .filter(a -> a.getJoursRestants() != null)
                .min(Comparator.comparingLong(AlerteEntretienDTO::getJoursRestants))
                .orElse(null));

        return dto;
    }
}
