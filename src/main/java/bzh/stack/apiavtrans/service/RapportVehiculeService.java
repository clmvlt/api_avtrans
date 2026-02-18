package bzh.stack.apiavtrans.service;

import bzh.stack.apiavtrans.dto.notification.NotificationCreateRequest;
import bzh.stack.apiavtrans.dto.vehicule.RapportVehiculeCreateRequest;
import bzh.stack.apiavtrans.dto.vehicule.RapportVehiculeDTO;
import bzh.stack.apiavtrans.dto.vehicule.RapportVehiculeListResponse;
import bzh.stack.apiavtrans.entity.RapportVehicule;
import bzh.stack.apiavtrans.entity.RapportVehiculePicture;
import bzh.stack.apiavtrans.entity.User;
import bzh.stack.apiavtrans.entity.Vehicule;
import bzh.stack.apiavtrans.mapper.RapportVehiculeMapper;
import bzh.stack.apiavtrans.repository.RapportVehiculePictureRepository;
import bzh.stack.apiavtrans.repository.RapportVehiculeRepository;
import bzh.stack.apiavtrans.repository.VehiculeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RapportVehiculeService {

    private final RapportVehiculeRepository rapportVehiculeRepository;
    private final VehiculeRepository vehiculeRepository;
    private final RapportVehiculePictureRepository rapportVehiculePictureRepository;
    private final RapportVehiculeMapper rapportVehiculeMapper;
    private final FileStorageService fileStorageService;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public RapportVehiculeListResponse getRapportsByVehiculeId(UUID vehiculeId, Pageable pageable, boolean fetchAll) {
        Vehicule vehicule = vehiculeRepository.findById(vehiculeId)
                .orElseThrow(() -> new RuntimeException("Véhicule non trouvé avec l'ID : " + vehiculeId));

        RapportVehiculeListResponse response = new RapportVehiculeListResponse();
        response.setSuccess(true);
        response.setMessage("Rapports récupérés avec succès");

        if (fetchAll) {
            List<RapportVehicule> allRapports = rapportVehiculeRepository.findByVehiculeOrderByCreatedAtDesc(vehicule);
            List<RapportVehiculeDTO> rapports = allRapports.stream()
                    .map(rapportVehiculeMapper::toDTO)
                    .collect(Collectors.toList());
            response.setData(rapports);
            response.setTotalElements((long) rapports.size());
        } else {
            Page<RapportVehicule> page = rapportVehiculeRepository.findByVehiculeOrderByCreatedAtDesc(vehicule, pageable);
            List<RapportVehiculeDTO> rapports = page.getContent().stream()
                    .map(rapportVehiculeMapper::toDTO)
                    .collect(Collectors.toList());
            response.setData(rapports);
            response.setPage(page.getNumber());
            response.setSize(page.getSize());
            response.setTotalElements(page.getTotalElements());
            response.setTotalPages(page.getTotalPages());
        }

        return response;
    }

    @Transactional
    public RapportVehiculeDTO createRapport(RapportVehiculeCreateRequest request, User currentUser) {
        Vehicule vehicule = vehiculeRepository.findById(request.getVehiculeId())
                .orElseThrow(() -> new RuntimeException("Véhicule non trouvé avec l'ID : " + request.getVehiculeId()));

        RapportVehicule rapport = new RapportVehicule();
        rapport.setUser(currentUser);
        rapport.setVehicule(vehicule);
        rapport.setCommentaire(request.getCommentaire());

        RapportVehicule saved = rapportVehiculeRepository.save(rapport);

        if (request.getPicturesB64() != null && !request.getPicturesB64().isEmpty()) {
            for (String pictureB64 : request.getPicturesB64()) {
                String pictureUrl = fileStorageService.saveRapportImage(pictureB64, saved.getId());

                RapportVehiculePicture picture = new RapportVehiculePicture();
                picture.setRapportVehicule(saved);
                picture.setPicturePath(fileStorageService.extractFileNameFromUrl(pictureUrl));
                rapportVehiculePictureRepository.save(picture);
            }
        }

        NotificationCreateRequest notificationRequest = new NotificationCreateRequest();
        notificationRequest.setTitle("Nouveau rapport de véhicule");
        notificationRequest.setDescription(String.format("%s %s a créé un rapport pour le véhicule %s",
            currentUser.getFirstName(), currentUser.getLastName(), vehicule.getImmat()));
        notificationRequest.setRefType("rapport_vehicule");
        notificationRequest.setRefId(saved.getId().toString());

        notificationService.sendNotificationToRoleWithPreference("Administrateur", notificationRequest, "rapport_vehicule");
        notificationService.sendNotificationToRoleWithPreference("Mécanicien", notificationRequest, "rapport_vehicule");

        return rapportVehiculeMapper.toDTO(saved);
    }

    @Transactional(readOnly = true)
    public RapportVehiculeDTO getLatestUserRapport(User currentUser) {
        RapportVehicule rapport = rapportVehiculeRepository.findLatestByUser(currentUser)
                .orElseThrow(() -> new RuntimeException("Aucun rapport trouvé pour cet utilisateur"));

        return rapportVehiculeMapper.toDTO(rapport);
    }
}
