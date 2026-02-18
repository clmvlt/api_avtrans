package bzh.stack.apiavtrans.service;

import bzh.stack.apiavtrans.dto.entretien.TypeEntretienCreateRequest;
import bzh.stack.apiavtrans.dto.entretien.TypeEntretienDTO;
import bzh.stack.apiavtrans.dto.entretien.TypeEntretienUpdateRequest;
import bzh.stack.apiavtrans.entity.DossierTypeEntretien;
import bzh.stack.apiavtrans.entity.TypeEntretien;
import bzh.stack.apiavtrans.mapper.TypeEntretienMapper;
import bzh.stack.apiavtrans.repository.DossierTypeEntretienRepository;
import bzh.stack.apiavtrans.repository.TypeEntretienRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TypeEntretienService {

    private final TypeEntretienRepository typeEntretienRepository;
    private final DossierTypeEntretienRepository dossierTypeEntretienRepository;
    private final TypeEntretienMapper typeEntretienMapper;

    @Transactional(readOnly = true)
    public List<TypeEntretienDTO> getAllTypesEntretien() {
        return typeEntretienRepository.findAll().stream()
                .map(typeEntretienMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TypeEntretienDTO getTypeEntretienById(UUID id) {
        TypeEntretien typeEntretien = typeEntretienRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Type d'entretien non trouvé avec l'ID : " + id));
        return typeEntretienMapper.toDTO(typeEntretien);
    }

    @Transactional
    public TypeEntretienDTO createTypeEntretien(TypeEntretienCreateRequest request) {
        if (typeEntretienRepository.findByNom(request.getNom()).isPresent()) {
            throw new RuntimeException("Un type d'entretien avec ce nom existe déjà : " + request.getNom());
        }

        TypeEntretien typeEntretien = new TypeEntretien();
        typeEntretien.setNom(request.getNom());
        typeEntretien.setDescription(request.getDescription());

        if (request.getDossierId() != null) {
            DossierTypeEntretien dossier = dossierTypeEntretienRepository.findById(request.getDossierId())
                    .orElseThrow(() -> new RuntimeException("Dossier non trouvé avec l'ID : " + request.getDossierId()));
            typeEntretien.setDossier(dossier);
        }

        TypeEntretien saved = typeEntretienRepository.save(typeEntretien);
        return typeEntretienMapper.toDTO(saved);
    }

    @Transactional
    public TypeEntretienDTO updateTypeEntretien(UUID id, TypeEntretienUpdateRequest request) {
        TypeEntretien typeEntretien = typeEntretienRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Type d'entretien non trouvé avec l'ID : " + id));

        if (request.getNom() != null && !typeEntretien.getNom().equals(request.getNom())) {
            typeEntretienRepository.findByNom(request.getNom()).ifPresent(t -> {
                throw new RuntimeException("Un autre type d'entretien possède déjà ce nom : " + request.getNom());
            });
            typeEntretien.setNom(request.getNom());
        }

        if (request.getDescription() != null) {
            typeEntretien.setDescription(request.getDescription());
        }

        if (request.getDossierId() != null) {
            DossierTypeEntretien dossier = dossierTypeEntretienRepository.findById(request.getDossierId())
                    .orElseThrow(() -> new RuntimeException("Dossier non trouvé avec l'ID : " + request.getDossierId()));
            typeEntretien.setDossier(dossier);
        }

        TypeEntretien updated = typeEntretienRepository.save(typeEntretien);
        return typeEntretienMapper.toDTO(updated);
    }

    @Transactional
    public void deleteTypeEntretien(UUID id) {
        TypeEntretien typeEntretien = typeEntretienRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Type d'entretien non trouvé avec l'ID : " + id));
        typeEntretienRepository.delete(typeEntretien);
    }
}
