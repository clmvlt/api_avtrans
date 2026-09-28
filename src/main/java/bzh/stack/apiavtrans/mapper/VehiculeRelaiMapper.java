package bzh.stack.apiavtrans.mapper;

import bzh.stack.apiavtrans.dto.vehicule.VehiculeRelaiDTO;
import bzh.stack.apiavtrans.entity.VehiculeKilometrage;
import bzh.stack.apiavtrans.entity.VehiculeRelai;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class VehiculeRelaiMapper {

    /**
     * @param dernierReleve dernier relevé kilométrique rattaché au relais, ou null
     * @param nbReleves     nombre de relevés rattachés au relais
     * @param aujourdhui    date du jour (Europe/Paris), pour le statut
     */
    public VehiculeRelaiDTO toDTO(VehiculeRelai relai, VehiculeKilometrage dernierReleve, long nbReleves,
                                  LocalDate aujourdhui) {
        if (relai == null) {
            return null;
        }

        VehiculeRelaiDTO dto = new VehiculeRelaiDTO();
        dto.setId(relai.getId());
        dto.setVehiculeId(relai.getVehicule() != null ? relai.getVehicule().getId() : null);
        dto.setVehiculeImmat(relai.getVehicule() != null ? relai.getVehicule().getImmat() : null);
        dto.setImmat(relai.getImmat());
        dto.setMarque(relai.getMarque());
        dto.setModele(relai.getModele());
        dto.setDateDebut(relai.getDateDebut());
        dto.setDateFin(relai.getDateFin());
        dto.setKmDebut(relai.getKmDebut());
        dto.setKmFin(relai.getKmFin());
        dto.setMotif(relai.getMotif());
        dto.setCommentaire(relai.getCommentaire());
        dto.setStatut(statutDe(relai, aujourdhui));
        dto.setNbReleves(nbReleves);
        dto.setCreatedAt(relai.getCreatedAt());
        dto.setUpdatedAt(relai.getUpdatedAt());

        if (dernierReleve != null) {
            dto.setLatestKm(dernierReleve.getKm());
            dto.setLatestKmDate(dernierReleve.getCreatedAt());
        }

        Integer kmArrivee = relai.getKmFin() != null ? relai.getKmFin() : dto.getLatestKm();
        if (relai.getKmDebut() != null && kmArrivee != null && kmArrivee >= relai.getKmDebut()) {
            dto.setKmParcourus(kmArrivee - relai.getKmDebut());
        }

        return dto;
    }

    public static VehiculeRelaiDTO.Statut statutDe(VehiculeRelai relai, LocalDate aujourdhui) {
        if (relai.getDateDebut() != null && relai.getDateDebut().isAfter(aujourdhui)) {
            return VehiculeRelaiDTO.Statut.A_VENIR;
        }
        if (relai.getDateFin() != null && relai.getDateFin().isBefore(aujourdhui)) {
            return VehiculeRelaiDTO.Statut.TERMINE;
        }
        return VehiculeRelaiDTO.Statut.EN_COURS;
    }
}
