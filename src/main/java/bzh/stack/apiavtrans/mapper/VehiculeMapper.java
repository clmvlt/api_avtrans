package bzh.stack.apiavtrans.mapper;

import bzh.stack.apiavtrans.dto.vehicule.VehiculeDTO;
import bzh.stack.apiavtrans.dto.vehicule.VehiculeRelaiDTO;
import bzh.stack.apiavtrans.entity.Vehicule;
import bzh.stack.apiavtrans.entity.VehiculeKilometrage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.ZoneId;

@Component
public class VehiculeMapper {

    @Value("${app.base-api-url:http://192.168.1.120:8081}")
    private String baseApiUrl;

    public VehiculeDTO toDTO(Vehicule vehicule, VehiculeKilometrage latestKm) {
        if (vehicule == null) {
            return null;
        }

        VehiculeDTO dto = new VehiculeDTO();
        dto.setId(vehicule.getId());
        dto.setImmat(vehicule.getImmat());
        dto.setRelaiImmat(vehicule.getRelaiImmat());
        dto.setCreatedAt(vehicule.getCreatedAt());
        dto.setModel(vehicule.getModel());
        dto.setBrand(vehicule.getBrand());
        dto.setComment(vehicule.getComment());
        dto.setVin(vehicule.getVin());
        dto.setNumeroCarteGrise(vehicule.getNumeroCarteGrise());
        dto.setDateMiseEnCirculation(vehicule.getDateMiseEnCirculation());
        dto.setTypeCarburant(vehicule.getTypeCarburant());
        dto.setPtac(vehicule.getPtac());
        dto.setNumeroContratAssurance(vehicule.getNumeroContratAssurance());
        dto.setAssureur(vehicule.getAssureur());
        dto.setDateExpirationAssurance(vehicule.getDateExpirationAssurance());
        dto.setDateProchainControleTechnique(vehicule.getDateProchainControleTechnique());

        if (latestKm != null) {
            dto.setLatestKm(latestKm.getKm());
            dto.setLatestKmDate(latestKm.getCreatedAt());
            dto.setVehiculeLatestKm(latestKm.getKm());
            dto.setVehiculeLatestKmDate(latestKm.getCreatedAt());
        }

        if (vehicule.getPicturePath() != null) {
            dto.setPictureUrl(baseApiUrl + "/uploads/vehicules/profile/" + vehicule.getPicturePath());
        }

        return dto;
    }

    /**
     * Véhicule avec son relais en cours : le relais remplace le véhicule, son kilométrage devient le
     * kilométrage courant ({@code latestKm}), celui que les applications comparent à la saisie du
     * chauffeur. Le kilométrage du véhicule reste dans {@code vehiculeLatestKm}.
     *
     * @param latestKm     dernier relevé du véhicule lui-même (relevés des relais exclus)
     * @param relaiEnCours relais en cours aujourd'hui, ou null
     */
    public VehiculeDTO toDTO(Vehicule vehicule, VehiculeKilometrage latestKm, VehiculeRelaiDTO relaiEnCours) {
        VehiculeDTO dto = toDTO(vehicule, latestKm);
        if (dto == null || relaiEnCours == null) {
            return dto;
        }

        dto.setRelaiEnCours(relaiEnCours);
        dto.setRelaiImmat(relaiEnCours.getImmat());
        if (relaiEnCours.getLatestKm() != null) {
            dto.setLatestKm(relaiEnCours.getLatestKm());
            dto.setLatestKmDate(relaiEnCours.getLatestKmDate());
        } else {
            dto.setLatestKm(relaiEnCours.getKmDebut());
            dto.setLatestKmDate(relaiEnCours.getKmDebut() != null
                    ? relaiEnCours.getDateDebut().atStartOfDay(ZoneId.of("Europe/Paris"))
                    : null);
        }
        return dto;
    }

    public Vehicule toEntity(VehiculeDTO dto) {
        if (dto == null) {
            return null;
        }

        Vehicule vehicule = new Vehicule();
        vehicule.setId(dto.getId());
        vehicule.setImmat(dto.getImmat());
        vehicule.setRelaiImmat(dto.getRelaiImmat());
        vehicule.setCreatedAt(dto.getCreatedAt());
        vehicule.setModel(dto.getModel());
        vehicule.setBrand(dto.getBrand());
        vehicule.setComment(dto.getComment());
        vehicule.setVin(dto.getVin());
        vehicule.setNumeroCarteGrise(dto.getNumeroCarteGrise());
        vehicule.setDateMiseEnCirculation(dto.getDateMiseEnCirculation());
        vehicule.setTypeCarburant(dto.getTypeCarburant());
        vehicule.setPtac(dto.getPtac());
        vehicule.setNumeroContratAssurance(dto.getNumeroContratAssurance());
        vehicule.setAssureur(dto.getAssureur());
        vehicule.setDateExpirationAssurance(dto.getDateExpirationAssurance());
        vehicule.setDateProchainControleTechnique(dto.getDateProchainControleTechnique());

        return vehicule;
    }
}
