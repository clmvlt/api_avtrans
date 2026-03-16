package bzh.stack.apiavtrans.service;

import bzh.stack.apiavtrans.dto.vehicule.VehiculeEquipementCreateRequest;
import bzh.stack.apiavtrans.dto.vehicule.VehiculeEquipementDTO;
import bzh.stack.apiavtrans.dto.vehicule.VehiculeEquipementUpdateRequest;
import bzh.stack.apiavtrans.entity.Vehicule;
import bzh.stack.apiavtrans.entity.VehiculeEquipement;
import bzh.stack.apiavtrans.mapper.VehiculeEquipementMapper;
import bzh.stack.apiavtrans.repository.VehiculeEquipementRepository;
import bzh.stack.apiavtrans.repository.VehiculeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VehiculeEquipementService {

    private final VehiculeEquipementRepository vehiculeEquipementRepository;
    private final VehiculeRepository vehiculeRepository;
    private final VehiculeEquipementMapper vehiculeEquipementMapper;

    @Transactional(readOnly = true)
    public List<VehiculeEquipementDTO> getEquipementsByVehicule(UUID vehiculeId) {
        Vehicule vehicule = vehiculeRepository.findById(vehiculeId)
                .orElseThrow(() -> new RuntimeException("Véhicule non trouvé avec l'ID : " + vehiculeId));

        return vehiculeEquipementRepository.findByVehiculeOrderByNomAsc(vehicule).stream()
                .map(vehiculeEquipementMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public VehiculeEquipementDTO getEquipementById(UUID id) {
        VehiculeEquipement equipement = vehiculeEquipementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Équipement non trouvé avec l'ID : " + id));
        return vehiculeEquipementMapper.toDTO(equipement);
    }

    @Transactional
    public VehiculeEquipementDTO createEquipement(VehiculeEquipementCreateRequest request) {
        Vehicule vehicule = vehiculeRepository.findById(request.getVehiculeId())
                .orElseThrow(() -> new RuntimeException("Véhicule non trouvé avec l'ID : " + request.getVehiculeId()));

        if (request.getNom() == null || request.getNom().trim().isEmpty()) {
            throw new RuntimeException("Le nom de l'équipement est obligatoire");
        }

        VehiculeEquipement equipement = new VehiculeEquipement();
        equipement.setVehicule(vehicule);
        equipement.setNom(request.getNom().trim());
        equipement.setQuantite(request.getQuantite() != null ? request.getQuantite() : 1);
        equipement.setCommentaire(request.getCommentaire());

        VehiculeEquipement saved = vehiculeEquipementRepository.save(equipement);
        return vehiculeEquipementMapper.toDTO(saved);
    }

    @Transactional
    public VehiculeEquipementDTO updateEquipement(UUID id, VehiculeEquipementUpdateRequest request) {
        VehiculeEquipement equipement = vehiculeEquipementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Équipement non trouvé avec l'ID : " + id));

        if (request.getNom() != null) {
            if (request.getNom().trim().isEmpty()) {
                throw new RuntimeException("Le nom de l'équipement ne peut pas être vide");
            }
            equipement.setNom(request.getNom().trim());
        }
        if (request.getQuantite() != null) {
            equipement.setQuantite(request.getQuantite());
        }
        if (request.getCommentaire() != null) {
            equipement.setCommentaire(request.getCommentaire());
        }

        VehiculeEquipement updated = vehiculeEquipementRepository.save(equipement);
        return vehiculeEquipementMapper.toDTO(updated);
    }

    @Transactional
    public void deleteEquipement(UUID id) {
        VehiculeEquipement equipement = vehiculeEquipementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Équipement non trouvé avec l'ID : " + id));
        vehiculeEquipementRepository.delete(equipement);
    }
}
