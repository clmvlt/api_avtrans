package bzh.stack.apiavtrans.mapper;

import bzh.stack.apiavtrans.dto.vehicule.VehiculeEquipementDTO;
import bzh.stack.apiavtrans.entity.VehiculeEquipement;
import org.springframework.stereotype.Component;

@Component
public class VehiculeEquipementMapper {

    public VehiculeEquipementDTO toDTO(VehiculeEquipement equipement) {
        if (equipement == null) {
            return null;
        }

        VehiculeEquipementDTO dto = new VehiculeEquipementDTO();
        dto.setId(equipement.getId());
        dto.setVehiculeId(equipement.getVehicule() != null ? equipement.getVehicule().getId() : null);
        dto.setVehiculeImmat(equipement.getVehicule() != null ? equipement.getVehicule().getImmat() : null);
        dto.setNom(equipement.getNom());
        dto.setQuantite(equipement.getQuantite());
        dto.setCommentaire(equipement.getCommentaire());
        dto.setCreatedAt(equipement.getCreatedAt());

        return dto;
    }

    public VehiculeEquipement toEntity(VehiculeEquipementDTO dto) {
        if (dto == null) {
            return null;
        }

        VehiculeEquipement equipement = new VehiculeEquipement();
        equipement.setId(dto.getId());
        equipement.setNom(dto.getNom());
        equipement.setQuantite(dto.getQuantite());
        equipement.setCommentaire(dto.getCommentaire());
        equipement.setCreatedAt(dto.getCreatedAt());

        return equipement;
    }
}
