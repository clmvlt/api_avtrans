package bzh.stack.apiavtrans.service;

import bzh.stack.apiavtrans.dto.entretien.DossierTypeEntretienCreateRequest;
import bzh.stack.apiavtrans.dto.entretien.DossierTypeEntretienDTO;
import bzh.stack.apiavtrans.dto.entretien.DossierTypeEntretienUpdateRequest;
import bzh.stack.apiavtrans.entity.DossierTypeEntretien;
import bzh.stack.apiavtrans.mapper.DossierTypeEntretienMapper;
import bzh.stack.apiavtrans.repository.DossierTypeEntretienRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DossierTypeEntretienService {

    private final DossierTypeEntretienRepository dossierTypeEntretienRepository;
    private final DossierTypeEntretienMapper dossierTypeEntretienMapper;

    @Transactional(readOnly = true)
    public List<DossierTypeEntretienDTO> getAllDossiers() {
        return dossierTypeEntretienRepository.findAll().stream()
                .map(dossierTypeEntretienMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DossierTypeEntretienDTO getDossierById(UUID id) {
        DossierTypeEntretien dossier = dossierTypeEntretienRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dossier de types d'entretien non trouvé avec l'ID : " + id));
        return dossierTypeEntretienMapper.toDTO(dossier);
    }

    @Transactional
    public DossierTypeEntretienDTO createDossier(DossierTypeEntretienCreateRequest request) {
        if (dossierTypeEntretienRepository.findByNom(request.getNom()).isPresent()) {
            throw new RuntimeException("Un dossier avec ce nom existe déjà : " + request.getNom());
        }

        DossierTypeEntretien dossier = new DossierTypeEntretien();
        dossier.setNom(request.getNom());
        dossier.setDescription(request.getDescription());

        DossierTypeEntretien saved = dossierTypeEntretienRepository.save(dossier);
        return dossierTypeEntretienMapper.toDTO(saved);
    }

    @Transactional
    public DossierTypeEntretienDTO updateDossier(UUID id, DossierTypeEntretienUpdateRequest request) {
        DossierTypeEntretien dossier = dossierTypeEntretienRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dossier de types d'entretien non trouvé avec l'ID : " + id));

        if (request.getNom() != null && !dossier.getNom().equals(request.getNom())) {
            dossierTypeEntretienRepository.findByNom(request.getNom()).ifPresent(d -> {
                throw new RuntimeException("Un autre dossier possède déjà ce nom : " + request.getNom());
            });
            dossier.setNom(request.getNom());
        }

        if (request.getDescription() != null) {
            dossier.setDescription(request.getDescription());
        }

        DossierTypeEntretien updated = dossierTypeEntretienRepository.save(dossier);
        return dossierTypeEntretienMapper.toDTO(updated);
    }

    @Transactional
    public void deleteDossier(UUID id) {
        DossierTypeEntretien dossier = dossierTypeEntretienRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dossier de types d'entretien non trouvé avec l'ID : " + id));
        dossierTypeEntretienRepository.delete(dossier);
    }
}
