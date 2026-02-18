package bzh.stack.apiavtrans.service;

import bzh.stack.apiavtrans.dto.carte.CarteCreateRequest;
import bzh.stack.apiavtrans.dto.carte.CarteUpdateRequest;
import bzh.stack.apiavtrans.entity.Carte;
import bzh.stack.apiavtrans.entity.TypeCarte;
import bzh.stack.apiavtrans.entity.User;
import bzh.stack.apiavtrans.repository.CarteRepository;
import bzh.stack.apiavtrans.repository.TypeCarteRepository;
import bzh.stack.apiavtrans.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CarteService {

    private final CarteRepository carteRepository;
    private final UserRepository userRepository;
    private final TypeCarteRepository typeCarteRepository;

    public List<Carte> getAllCartes() {
        return carteRepository.findAllByOrderByCreatedAtDesc();
    }

    public Optional<Carte> getCarte(UUID uuid) {
        return carteRepository.findById(uuid);
    }

    public List<Carte> getCartesByUser(UUID userUuid) {
        User user = userRepository.findById(userUuid)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouve avec l'uuid: " + userUuid));
        return carteRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public List<Carte> getCartesByTypeCarte(UUID typeCarteUuid) {
        TypeCarte typeCarte = typeCarteRepository.findById(typeCarteUuid)
                .orElseThrow(() -> new RuntimeException("Type de carte non trouve avec l'uuid: " + typeCarteUuid));
        return carteRepository.findByTypeCarteOrderByCreatedAtDesc(typeCarte);
    }

    @Transactional
    public Carte createCarte(CarteCreateRequest request) {
        User user = null;
        if (request.getUserUuid() != null) {
            user = userRepository.findById(request.getUserUuid())
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouve avec l'uuid: " + request.getUserUuid()));
        }

        TypeCarte typeCarte = typeCarteRepository.findById(request.getTypeCarteUuid())
                .orElseThrow(() -> new RuntimeException("Type de carte non trouve avec l'uuid: " + request.getTypeCarteUuid()));

        Carte carte = new Carte();
        carte.setNom(request.getNom());
        carte.setDescription(request.getDescription());
        carte.setCode(request.getCode());
        carte.setNumero(request.getNumero());
        carte.setUser(user);
        carte.setTypeCarte(typeCarte);

        return carteRepository.save(carte);
    }

    @Transactional
    public Carte updateCarte(UUID uuid, CarteUpdateRequest request) {
        Carte carte = carteRepository.findById(uuid)
                .orElseThrow(() -> new RuntimeException("Carte non trouvee avec l'uuid: " + uuid));

        if (request.getNom() != null) {
            carte.setNom(request.getNom());
        }
        if (request.getDescription() != null) {
            carte.setDescription(request.getDescription());
        }
        if (request.getCode() != null) {
            carte.setCode(request.getCode());
        }
        if (request.getNumero() != null) {
            carte.setNumero(request.getNumero());
        }
        if (Boolean.TRUE.equals(request.getClearUser())) {
            carte.setUser(null);
        } else if (request.getUserUuid() != null) {
            User user = userRepository.findById(request.getUserUuid())
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouve avec l'uuid: " + request.getUserUuid()));
            carte.setUser(user);
        }
        if (request.getTypeCarteUuid() != null) {
            TypeCarte typeCarte = typeCarteRepository.findById(request.getTypeCarteUuid())
                    .orElseThrow(() -> new RuntimeException("Type de carte non trouve avec l'uuid: " + request.getTypeCarteUuid()));
            carte.setTypeCarte(typeCarte);
        }

        return carteRepository.save(carte);
    }

    @Transactional
    public void deleteCarte(UUID uuid) {
        Carte carte = carteRepository.findById(uuid)
                .orElseThrow(() -> new RuntimeException("Carte non trouvee avec l'uuid: " + uuid));
        carteRepository.delete(carte);
    }
}
