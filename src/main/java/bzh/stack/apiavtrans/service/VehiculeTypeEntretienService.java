package bzh.stack.apiavtrans.service;

import bzh.stack.apiavtrans.dto.entretien.VehiculeTypeEntretienCreateRequest;
import bzh.stack.apiavtrans.dto.entretien.VehiculeTypeEntretienDTO;
import bzh.stack.apiavtrans.dto.entretien.VehiculeTypeEntretienUpdateRequest;
import bzh.stack.apiavtrans.entity.TypeEntretien;
import bzh.stack.apiavtrans.entity.Vehicule;
import bzh.stack.apiavtrans.entity.VehiculeTypeEntretien;
import bzh.stack.apiavtrans.mapper.VehiculeTypeEntretienMapper;
import bzh.stack.apiavtrans.repository.TypeEntretienRepository;
import bzh.stack.apiavtrans.repository.VehiculeRepository;
import bzh.stack.apiavtrans.repository.VehiculeTypeEntretienRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VehiculeTypeEntretienService {

    private final VehiculeTypeEntretienRepository vehiculeTypeEntretienRepository;
    private final VehiculeRepository vehiculeRepository;
    private final TypeEntretienRepository typeEntretienRepository;
    private final VehiculeTypeEntretienMapper vehiculeTypeEntretienMapper;

    @Transactional(readOnly = true)
    public List<VehiculeTypeEntretienDTO> getConfigurationsByVehicule(UUID vehiculeId) {
        Vehicule vehicule = vehiculeRepository.findById(vehiculeId)
                .orElseThrow(() -> new RuntimeException("Véhicule non trouvé avec l'ID : " + vehiculeId));

        return vehiculeTypeEntretienRepository.findByVehicule(vehicule).stream()
                .map(vehiculeTypeEntretienMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VehiculeTypeEntretienDTO> getActiveConfigurationsByVehicule(UUID vehiculeId) {
        Vehicule vehicule = vehiculeRepository.findById(vehiculeId)
                .orElseThrow(() -> new RuntimeException("Véhicule non trouvé avec l'ID : " + vehiculeId));

        return vehiculeTypeEntretienRepository.findByVehiculeAndActifTrue(vehicule).stream()
                .map(vehiculeTypeEntretienMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public VehiculeTypeEntretienDTO getConfigurationById(UUID id) {
        VehiculeTypeEntretien config = vehiculeTypeEntretienRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Configuration non trouvée avec l'ID : " + id));
        return vehiculeTypeEntretienMapper.toDTO(config);
    }

    @Transactional
    public VehiculeTypeEntretienDTO createConfiguration(VehiculeTypeEntretienCreateRequest request) {
        Vehicule vehicule = vehiculeRepository.findById(request.getVehiculeId())
                .orElseThrow(() -> new RuntimeException("Véhicule non trouvé avec l'ID : " + request.getVehiculeId()));

        TypeEntretien typeEntretien = typeEntretienRepository.findById(request.getTypeEntretienId())
                .orElseThrow(() -> new RuntimeException("Type d'entretien non trouvé avec l'ID : " + request.getTypeEntretienId()));

        vehiculeTypeEntretienRepository.findByVehiculeAndTypeEntretien(vehicule, typeEntretien).ifPresent(c -> {
            throw new RuntimeException("Une configuration existe déjà pour ce véhicule et ce type d'entretien");
        });

        VehiculeTypeEntretien config = new VehiculeTypeEntretien();
        config.setVehicule(vehicule);
        config.setTypeEntretien(typeEntretien);
        config.setPeriodiciteType(request.getPeriodiciteType());
        config.setPeriodiciteValeur(request.getPeriodiciteValeur());
        config.setActif(true);

        VehiculeTypeEntretien saved = vehiculeTypeEntretienRepository.save(config);
        return vehiculeTypeEntretienMapper.toDTO(saved);
    }

    @Transactional
    public VehiculeTypeEntretienDTO updateConfiguration(UUID id, VehiculeTypeEntretienUpdateRequest request) {
        VehiculeTypeEntretien config = vehiculeTypeEntretienRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Configuration non trouvée avec l'ID : " + id));

        if (request.getPeriodiciteType() != null) {
            config.setPeriodiciteType(request.getPeriodiciteType());
        }
        if (request.getPeriodiciteValeur() != null) {
            config.setPeriodiciteValeur(request.getPeriodiciteValeur());
        }
        if (request.getActif() != null) {
            config.setActif(request.getActif());
        }

        VehiculeTypeEntretien updated = vehiculeTypeEntretienRepository.save(config);
        return vehiculeTypeEntretienMapper.toDTO(updated);
    }

    @Transactional
    public void deleteConfiguration(UUID id) {
        VehiculeTypeEntretien config = vehiculeTypeEntretienRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Configuration non trouvée avec l'ID : " + id));
        vehiculeTypeEntretienRepository.delete(config);
    }
}
