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

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AlerteService {

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

        Integer kilometrageActuel = latestKm != null ? latestKm.getKm() : 0;

        List<VehiculeTypeEntretien> configurations = vehiculeTypeEntretienRepository.findByVehiculeAndActifTrue(vehicule);
        List<AlerteEntretienDTO> alertes = new ArrayList<>();

        for (VehiculeTypeEntretien config : configurations) {
            AlerteEntretienDTO alerte = calculateAlerte(vehicule, latestKm, config, kilometrageActuel);
            if (alerte != null) {
                alertes.add(alerte);
            }
        }

        return alertes;
    }

    @Transactional(readOnly = true)
    public List<AlerteEntretienDTO> getAllAlertes() {
        List<Vehicule> vehicules = vehiculeRepository.findAll();
        List<AlerteEntretienDTO> alertes = new ArrayList<>();

        for (Vehicule vehicule : vehicules) {
            VehiculeKilometrage latestKm = vehiculeKilometrageRepository
                    .findLatestByVehicule(vehicule)
                    .orElse(null);

            Integer kilometrageActuel = latestKm != null ? latestKm.getKm() : 0;

            List<VehiculeTypeEntretien> configurations = vehiculeTypeEntretienRepository.findByVehiculeAndActifTrue(vehicule);

            for (VehiculeTypeEntretien config : configurations) {
                AlerteEntretienDTO alerte = calculateAlerte(vehicule, latestKm, config, kilometrageActuel);
                if (alerte != null) {
                    alertes.add(alerte);
                }
            }
        }

        return alertes;
    }

    private AlerteEntretienDTO calculateAlerte(Vehicule vehicule, VehiculeKilometrage latestKm, VehiculeTypeEntretien config, Integer kilometrageActuel) {
        List<Entretien> entretiens = entretienRepository
                .findByVehiculeAndTypeEntretienOrderByDateEntretienDesc(vehicule, config.getTypeEntretien().getId());

        Entretien dernierEntretien = entretiens.isEmpty() ? null : entretiens.get(0);

        AlerteEntretienDTO alerte = new AlerteEntretienDTO();
        alerte.setVehicule(vehiculeMapper.toDTO(vehicule, latestKm));
        alerte.setTypeEntretien(typeEntretienMapper.toDTO(config.getTypeEntretien()));
        alerte.setDernierEntretien(dernierEntretien != null ? entretienMapper.toDTO(dernierEntretien) : null);

        if (config.getPeriodiciteType() == VehiculeTypeEntretien.PeriodiciteType.KILOMETRAGE) {
            return calculateAlerteKilometrage(alerte, dernierEntretien, config, kilometrageActuel);
        } else if (config.getPeriodiciteType() == VehiculeTypeEntretien.PeriodiciteType.TEMPOREL) {
            return calculateAlerteTemporelle(alerte, dernierEntretien, config);
        }

        return null;
    }

    private AlerteEntretienDTO calculateAlerteKilometrage(AlerteEntretienDTO alerte, Entretien dernierEntretien,
                                                           VehiculeTypeEntretien config, Integer kilometrageActuel) {
        if (dernierEntretien == null) {
            alerte.setProchainKilometrage(config.getPeriodiciteValeur());
            alerte.setKmRestants(config.getPeriodiciteValeur() - kilometrageActuel);
            alerte.setEnRetard(kilometrageActuel >= config.getPeriodiciteValeur());
            alerte.setMessage(buildMessageKilometrage(alerte.getKmRestants(), alerte.getEnRetard()));
            return alerte;
        }

        Integer prochainKm = dernierEntretien.getKilometrage() + config.getPeriodiciteValeur();
        Integer kmRestants = prochainKm - kilometrageActuel;

        alerte.setProchainKilometrage(prochainKm);
        alerte.setKmRestants(kmRestants);
        alerte.setEnRetard(kmRestants <= 0);
        alerte.setMessage(buildMessageKilometrage(kmRestants, alerte.getEnRetard()));

        return alerte;
    }

    private AlerteEntretienDTO calculateAlerteTemporelle(AlerteEntretienDTO alerte, Entretien dernierEntretien,
                                                          VehiculeTypeEntretien config) {
        if (dernierEntretien == null) {
            ZonedDateTime prochaineDateTemporelle = ZonedDateTime.now().plusDays(config.getPeriodiciteValeur());
            long joursRestants = ChronoUnit.DAYS.between(ZonedDateTime.now(), prochaineDateTemporelle);

            alerte.setProchaineDateTemporelle(prochaineDateTemporelle);
            alerte.setJoursRestants(joursRestants);
            alerte.setEnRetard(false);
            alerte.setMessage(buildMessageTemporel(joursRestants, false));
            return alerte;
        }

        ZonedDateTime prochaineDateTemporelle = dernierEntretien.getDateEntretien().plusDays(config.getPeriodiciteValeur());
        long joursRestants = ChronoUnit.DAYS.between(ZonedDateTime.now(), prochaineDateTemporelle);

        alerte.setProchaineDateTemporelle(prochaineDateTemporelle);
        alerte.setJoursRestants(joursRestants);
        alerte.setEnRetard(joursRestants <= 0);
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
            return "URGENT : Entretien en retard de " + Math.abs(joursRestants) + " jours";
        } else if (joursRestants <= 30) {
            return "Entretien à prévoir dans " + joursRestants + " jours";
        }
        return "Prochain entretien dans " + joursRestants + " jours";
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

        VehiculeProchainEntretienDTO dto = new VehiculeProchainEntretienDTO();
        dto.setVehicule(vehiculeMapper.toDTO(vehicule, latestKm));

        List<AlerteEntretienDTO> alertes = getAlertesForVehicule(vehicule.getId());

        if (!alertes.isEmpty()) {
            AlerteEntretienDTO prochainKm = alertes.stream()
                    .filter(a -> a.getKmRestants() != null)
                    .min(Comparator.comparingInt(AlerteEntretienDTO::getKmRestants))
                    .orElse(null);

            AlerteEntretienDTO prochainDate = alertes.stream()
                    .filter(a -> a.getJoursRestants() != null)
                    .min(Comparator.comparingLong(AlerteEntretienDTO::getJoursRestants))
                    .orElse(null);

            dto.setProchainEntretienKm(prochainKm);
            dto.setProchainEntretienDate(prochainDate);
        } else {
            dto.setProchainEntretienKm(null);
            dto.setProchainEntretienDate(null);
        }

        return dto;
    }
}
