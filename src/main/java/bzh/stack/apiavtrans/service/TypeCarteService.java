package bzh.stack.apiavtrans.service;

import bzh.stack.apiavtrans.dto.carte.TypeCarteCreateRequest;
import bzh.stack.apiavtrans.dto.carte.TypeCarteUpdateRequest;
import bzh.stack.apiavtrans.entity.TypeCarte;
import bzh.stack.apiavtrans.repository.TypeCarteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TypeCarteService {

    private final TypeCarteRepository typeCarteRepository;

    public List<TypeCarte> getAllTypeCartes() {
        return typeCarteRepository.findAll();
    }

    public Optional<TypeCarte> getTypeCarte(UUID uuid) {
        return typeCarteRepository.findById(uuid);
    }

    public Optional<TypeCarte> getTypeCarteByNom(String nom) {
        return typeCarteRepository.findByNom(nom);
    }

    @Transactional
    public TypeCarte createTypeCarte(TypeCarteCreateRequest request) {
        TypeCarte typeCarte = new TypeCarte();
        typeCarte.setNom(request.getNom());
        typeCarte.setDescription(request.getDescription());
        return typeCarteRepository.save(typeCarte);
    }

    @Transactional
    public TypeCarte updateTypeCarte(UUID uuid, TypeCarteUpdateRequest request) {
        TypeCarte typeCarte = typeCarteRepository.findById(uuid)
                .orElseThrow(() -> new RuntimeException("Type de carte non trouve avec l'uuid: " + uuid));

        if (request.getNom() != null) {
            typeCarte.setNom(request.getNom());
        }
        if (request.getDescription() != null) {
            typeCarte.setDescription(request.getDescription());
        }

        return typeCarteRepository.save(typeCarte);
    }

    @Transactional
    public void deleteTypeCarte(UUID uuid) {
        TypeCarte typeCarte = typeCarteRepository.findById(uuid)
                .orElseThrow(() -> new RuntimeException("Type de carte non trouve avec l'uuid: " + uuid));
        typeCarteRepository.delete(typeCarte);
    }
}
