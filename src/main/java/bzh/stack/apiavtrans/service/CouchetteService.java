package bzh.stack.apiavtrans.service;

import bzh.stack.apiavtrans.dto.couchette.CouchetteSearchRequest;
import bzh.stack.apiavtrans.entity.Couchette;
import bzh.stack.apiavtrans.entity.User;
import bzh.stack.apiavtrans.repository.CouchetteRepository;
import bzh.stack.apiavtrans.repository.CouchetteSpecifications;
import bzh.stack.apiavtrans.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CouchetteService {

    private final CouchetteRepository couchetteRepository;
    private final UserRepository userRepository;

    private static final ZoneId ZONE_PARIS = ZoneId.of("Europe/Paris");

    @Transactional
    public Couchette createCouchette(User user, LocalDate date) {
        if (!Boolean.TRUE.equals(user.getIsCouchette())) {
            throw new RuntimeException("L'utilisateur n'a pas la permission couchette");
        }

        LocalDate couchetteDate = date != null ? date : LocalDate.now(ZONE_PARIS);

        if (couchetteRepository.existsByUserAndDate(user, couchetteDate)) {
            throw new RuntimeException("Une couchette existe déjà pour cette date");
        }

        Couchette couchette = new Couchette();
        couchette.setUser(user);
        couchette.setDate(couchetteDate);

        return couchetteRepository.save(couchette);
    }

    @Transactional
    public Couchette createCouchetteByAdmin(UUID userUuid, LocalDate date) {
        User user = userRepository.findById(userUuid)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (date == null) {
            throw new RuntimeException("La date est obligatoire");
        }

        if (couchetteRepository.existsByUserAndDate(user, date)) {
            throw new RuntimeException("Une couchette existe déjà pour cet utilisateur à cette date");
        }

        Couchette couchette = new Couchette();
        couchette.setUser(user);
        couchette.setDate(date);

        return couchetteRepository.save(couchette);
    }

    @Transactional(readOnly = true)
    public Page<Couchette> getAllCouchettes(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return couchetteRepository.findAllByOrderByDateDescCreatedAtDesc(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Couchette> getCouchettesForUser(User user, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return couchetteRepository.findByUserOrderByDateDescCreatedAtDesc(user, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Couchette> getCouchettesForUserByUuid(UUID userUuid, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return couchetteRepository.findByUserUuidOrderByDateDesc(userUuid, pageable);
    }

    @Transactional(readOnly = true)
    public Couchette getCouchetteById(UUID uuid) {
        return couchetteRepository.findById(uuid)
                .orElseThrow(() -> new RuntimeException("Couchette non trouvée"));
    }

    @Transactional
    public void deleteCouchette(UUID couchetteUuid, User user) {
        Couchette couchette = couchetteRepository.findById(couchetteUuid)
                .orElseThrow(() -> new RuntimeException("Couchette non trouvée"));

        if (!couchette.getUser().getUuid().equals(user.getUuid())) {
            throw new RuntimeException("Vous ne pouvez supprimer que vos propres couchettes");
        }

        LocalDate today = LocalDate.now(ZONE_PARIS);
        if (!couchette.getDate().equals(today)) {
            throw new RuntimeException("Vous ne pouvez supprimer que les couchettes du jour actuel");
        }

        couchetteRepository.delete(couchette);
    }

    @Transactional
    public void deleteCouchetteByAdmin(UUID couchetteUuid) {
        Couchette couchette = couchetteRepository.findById(couchetteUuid)
                .orElseThrow(() -> new RuntimeException("Couchette non trouvée"));

        couchetteRepository.delete(couchette);
    }

    @Transactional(readOnly = true)
    public Page<Couchette> searchCouchettes(CouchetteSearchRequest request) {
        int page = request.getPage() != null ? request.getPage() : 0;
        int size = request.getSize() != null ? request.getSize() : 20;
        String sortBy = request.getSortBy() != null ? request.getSortBy() : "date";
        String sortDirection = request.getSortDirection() != null ? request.getSortDirection() : "desc";

        Sort sort = sortDirection.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return couchetteRepository.findAll(
                CouchetteSpecifications.buildSearchSpec(
                        request.getUserUuid(),
                        request.getStartDate(),
                        request.getEndDate()
                ),
                pageable
        );
    }
}
