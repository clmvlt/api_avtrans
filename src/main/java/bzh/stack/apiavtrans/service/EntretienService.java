package bzh.stack.apiavtrans.service;

import bzh.stack.apiavtrans.dto.entretien.*;
import bzh.stack.apiavtrans.entity.*;
import bzh.stack.apiavtrans.mapper.EntretienMapper;
import bzh.stack.apiavtrans.mapper.EntretienFileMapper;
import bzh.stack.apiavtrans.repository.*;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EntretienService {

    private final EntretienRepository entretienRepository;
    private final EntretienFileRepository entretienFileRepository;
    private final VehiculeRepository vehiculeRepository;
    private final TypeEntretienRepository typeEntretienRepository;
    private final EntretienMapper entretienMapper;
    private final EntretienFileMapper entretienFileMapper;
    private final FileStorageService fileStorageService;

    @Transactional(readOnly = true)
    public List<EntretienDTO> getEntretiensByVehicule(UUID vehiculeId) {
        Vehicule vehicule = vehiculeRepository.findById(vehiculeId)
                .orElseThrow(() -> new RuntimeException("Véhicule non trouvé avec l'ID : " + vehiculeId));

        return entretienRepository.findByVehiculeOrderByDateEntretienDesc(vehicule).stream()
                .map(entretienMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EntretienDTO getEntretienById(UUID id) {
        Entretien entretien = entretienRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Entretien non trouvé avec l'ID : " + id));
        return entretienMapper.toDTO(entretien);
    }

    @Transactional
    public EntretienDTO createEntretien(EntretienCreateRequest request, User mecanicien) {
        Vehicule vehicule = vehiculeRepository.findById(request.getVehiculeId())
                .orElseThrow(() -> new RuntimeException("Véhicule non trouvé avec l'ID : " + request.getVehiculeId()));

        TypeEntretien typeEntretien = typeEntretienRepository.findById(request.getTypeEntretienId())
                .orElseThrow(() -> new RuntimeException("Type d'entretien non trouvé avec l'ID : " + request.getTypeEntretienId()));

        Entretien entretien = new Entretien();
        entretien.setVehicule(vehicule);
        entretien.setTypeEntretien(typeEntretien);
        entretien.setMecanicien(mecanicien);
        entretien.setDateEntretien(request.getDateEntretien());
        entretien.setKilometrage(request.getKilometrage());
        entretien.setCommentaire(request.getCommentaire());
        entretien.setCout(request.getCout());

        Entretien saved = entretienRepository.save(entretien);

        if (request.getFiles() != null && !request.getFiles().isEmpty()) {
            for (EntretienFileUploadRequest fileRequest : request.getFiles()) {
                addFileToEntretien(saved, fileRequest);
            }
        }

        return entretienMapper.toDTO(saved);
    }

    @Transactional
    public EntretienDTO updateEntretien(UUID id, EntretienUpdateRequest request) {
        Entretien entretien = entretienRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Entretien non trouvé avec l'ID : " + id));

        if (request.getTypeEntretienId() != null) {
            TypeEntretien typeEntretien = typeEntretienRepository.findById(request.getTypeEntretienId())
                    .orElseThrow(() -> new RuntimeException("Type d'entretien non trouvé avec l'ID : " + request.getTypeEntretienId()));
            entretien.setTypeEntretien(typeEntretien);
        }

        if (request.getDateEntretien() != null) {
            entretien.setDateEntretien(request.getDateEntretien());
        }
        if (request.getKilometrage() != null) {
            entretien.setKilometrage(request.getKilometrage());
        }
        if (request.getCommentaire() != null) {
            entretien.setCommentaire(request.getCommentaire());
        }
        if (request.getCout() != null) {
            entretien.setCout(request.getCout());
        }

        Entretien updated = entretienRepository.save(entretien);
        return entretienMapper.toDTO(updated);
    }

    @Transactional
    public void deleteEntretien(UUID id) {
        Entretien entretien = entretienRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Entretien non trouvé avec l'ID : " + id));

        List<EntretienFile> files = entretienFileRepository.findByEntretienOrderByCreatedAtDesc(entretien);
        for (EntretienFile file : files) {
            fileStorageService.deleteEntretienFile(file.getFilePath());
            entretienFileRepository.delete(file);
        }

        entretienRepository.delete(entretien);
    }

    @Transactional(readOnly = true)
    public List<EntretienFileDTO> getEntretienFiles(UUID entretienId) {
        Entretien entretien = entretienRepository.findById(entretienId)
                .orElseThrow(() -> new RuntimeException("Entretien non trouvé avec l'ID : " + entretienId));

        return entretienFileRepository.findByEntretienOrderByCreatedAtDesc(entretien).stream()
                .map(entretienFileMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public EntretienFileDTO addEntretienFile(UUID entretienId, EntretienFileUploadRequest request) {
        Entretien entretien = entretienRepository.findById(entretienId)
                .orElseThrow(() -> new RuntimeException("Entretien non trouvé avec l'ID : " + entretienId));

        return addFileToEntretien(entretien, request);
    }

    @Transactional
    public void deleteEntretienFile(UUID fileId) {
        EntretienFile file = entretienFileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("Fichier non trouvé avec l'ID : " + fileId));

        fileStorageService.deleteEntretienFile(file.getFilePath());
        entretienFileRepository.delete(file);
    }

    private EntretienFileDTO addFileToEntretien(Entretien entretien, EntretienFileUploadRequest request) {
        FileStorageService.FileInfo fileInfo = fileStorageService.saveEntretienFile(
                request.getFileB64(),
                request.getOriginalName(),
                request.getMimeType()
        );

        EntretienFile file = new EntretienFile();
        file.setEntretien(entretien);
        file.setFilePath(fileInfo.fileName());
        file.setOriginalName(request.getOriginalName());
        file.setMimeType(request.getMimeType());
        file.setFileSize(fileInfo.fileSize());

        EntretienFile saved = entretienFileRepository.save(file);
        return entretienFileMapper.toDTO(saved);
    }

    @Transactional(readOnly = true)
    public Page<Entretien> searchEntretiens(EntretienHistoryRequest request) {
        int page = request.getPage() != null ? request.getPage() : 0;
        int size = request.getSize() != null ? request.getSize() : 10;
        String sortBy = request.getSortBy() != null ? request.getSortBy() : "dateEntretien";
        String sortDirection = request.getSortDirection() != null ? request.getSortDirection() : "desc";

        Sort sort = sortDirection.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<Entretien> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (request.getVehiculeId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("vehicule").get("id"), request.getVehiculeId()));
            }

            if (request.getTypeEntretienId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("typeEntretien").get("id"), request.getTypeEntretienId()));
            }

            if (request.getDossierId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("typeEntretien").get("dossier").get("id"), request.getDossierId()));
            }

            if (request.getMecanicienId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("mecanicien").get("uuid"), request.getMecanicienId()));
            }

            if (request.getStartDate() != null) {
                ZonedDateTime startDateTime = request.getStartDate().atStartOfDay(ZoneId.of("Europe/Paris"));
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("dateEntretien"), startDateTime));
            }

            if (request.getEndDate() != null) {
                ZonedDateTime endDateTime = request.getEndDate().plusDays(1).atStartOfDay(ZoneId.of("Europe/Paris"));
                predicates.add(criteriaBuilder.lessThan(root.get("dateEntretien"), endDateTime));
            }

            if (request.getKmMin() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("kilometrage"), request.getKmMin()));
            }

            if (request.getKmMax() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("kilometrage"), request.getKmMax()));
            }

            if (request.getCoutMin() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("cout"), request.getCoutMin()));
            }

            if (request.getCoutMax() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("cout"), request.getCoutMax()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return entretienRepository.findAll(spec, pageable);
    }
}
