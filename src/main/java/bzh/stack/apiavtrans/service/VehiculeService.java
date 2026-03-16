package bzh.stack.apiavtrans.service;

import bzh.stack.apiavtrans.dto.vehicule.*;
import bzh.stack.apiavtrans.entity.*;
import bzh.stack.apiavtrans.mapper.*;
import bzh.stack.apiavtrans.repository.*;
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
public class VehiculeService {

    private final VehiculeRepository vehiculeRepository;
    private final VehiculeFileRepository vehiculeFileRepository;
    private final VehiculeKilometrageRepository vehiculeKilometrageRepository;
    private final VehiculeAdjustInfoRepository vehiculeAdjustInfoRepository;
    private final VehiculeAdjustInfoPictureRepository vehiculeAdjustInfoPictureRepository;
    private final UserRepository userRepository;

    private final VehiculeMapper vehiculeMapper;
    private final VehiculeFileMapper vehiculeFileMapper;
    private final VehiculeKilometrageMapper vehiculeKilometrageMapper;
    private final VehiculeAdjustInfoMapper vehiculeAdjustInfoMapper;
    private final VehiculeAdjustInfoPictureMapper vehiculeAdjustInfoPictureMapper;

    private final FileStorageService fileStorageService;

    @Transactional(readOnly = true)
    public List<VehiculeDTO> getAllVehicules() {
        return vehiculeRepository.findAllByOrderByImmatAsc().stream()
                .map(vehicule -> {
                    VehiculeKilometrage latestKm = vehiculeKilometrageRepository
                            .findLatestByVehicule(vehicule)
                            .orElse(null);
                    return vehiculeMapper.toDTO(vehicule, latestKm);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public VehiculeDTO getVehiculeById(UUID id) {
        Vehicule vehicule = vehiculeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Véhicule non trouvé avec l'ID : " + id));

        VehiculeKilometrage latestKm = vehiculeKilometrageRepository
                .findLatestByVehicule(vehicule)
                .orElse(null);

        return vehiculeMapper.toDTO(vehicule, latestKm);
    }

    @Transactional
    public VehiculeDTO createVehicule(VehiculeCreateRequest request) {
        if (vehiculeRepository.findByImmat(request.getImmat()).isPresent()) {
            throw new RuntimeException("Un véhicule avec cette immatriculation existe déjà : " + request.getImmat());
        }

        Vehicule vehicule = new Vehicule();
        vehicule.setImmat(request.getImmat());
        vehicule.setModel(request.getModel());
        vehicule.setBrand(request.getBrand());
        vehicule.setComment(request.getComment());
        vehicule.setVin(request.getVin());
        vehicule.setNumeroCarteGrise(request.getNumeroCarteGrise());
        vehicule.setDateMiseEnCirculation(request.getDateMiseEnCirculation());
        vehicule.setTypeCarburant(request.getTypeCarburant());
        vehicule.setPtac(request.getPtac());
        vehicule.setNumeroContratAssurance(request.getNumeroContratAssurance());
        vehicule.setAssureur(request.getAssureur());
        vehicule.setDateExpirationAssurance(request.getDateExpirationAssurance());
        vehicule.setDateProchainControleTechnique(request.getDateProchainControleTechnique());

        Vehicule saved = vehiculeRepository.save(vehicule);

        if (request.getPictureBase64() != null && !request.getPictureBase64().isEmpty()) {
            String pictureUrl = fileStorageService.saveVehiculeProfileImage(request.getPictureBase64(), saved.getId());
            saved.setPicturePath(fileStorageService.extractFileNameFromUrl(pictureUrl));
            saved = vehiculeRepository.save(saved);
        }

        VehiculeKilometrage latestKm = vehiculeKilometrageRepository
                .findLatestByVehicule(saved)
                .orElse(null);

        return vehiculeMapper.toDTO(saved, latestKm);
    }

    @Transactional
    public VehiculeDTO updateVehicule(UUID id, VehiculeUpdateRequest request) {
        Vehicule vehicule = vehiculeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Véhicule non trouvé avec l'ID : " + id));

        if (!vehicule.getImmat().equals(request.getImmat())) {
            vehiculeRepository.findByImmat(request.getImmat()).ifPresent(v -> {
                throw new RuntimeException("Un autre véhicule possède déjà cette immatriculation : " + request.getImmat());
            });
        }

        vehicule.setImmat(request.getImmat());
        vehicule.setModel(request.getModel());
        vehicule.setBrand(request.getBrand());
        vehicule.setComment(request.getComment());
        vehicule.setVin(request.getVin());
        vehicule.setNumeroCarteGrise(request.getNumeroCarteGrise());
        vehicule.setDateMiseEnCirculation(request.getDateMiseEnCirculation());
        vehicule.setTypeCarburant(request.getTypeCarburant());
        vehicule.setPtac(request.getPtac());
        vehicule.setNumeroContratAssurance(request.getNumeroContratAssurance());
        vehicule.setAssureur(request.getAssureur());
        vehicule.setDateExpirationAssurance(request.getDateExpirationAssurance());
        vehicule.setDateProchainControleTechnique(request.getDateProchainControleTechnique());

        if (request.getPictureBase64() != null && !request.getPictureBase64().isEmpty()) {
            if (vehicule.getPicturePath() != null) {
                fileStorageService.deleteVehiculeProfileImage(vehicule.getPicturePath());
            }
            String pictureUrl = fileStorageService.saveVehiculeProfileImage(request.getPictureBase64(), vehicule.getId());
            vehicule.setPicturePath(fileStorageService.extractFileNameFromUrl(pictureUrl));
        }

        Vehicule updated = vehiculeRepository.save(vehicule);

        VehiculeKilometrage latestKm = vehiculeKilometrageRepository
                .findLatestByVehicule(updated)
                .orElse(null);

        return vehiculeMapper.toDTO(updated, latestKm);
    }

    @Transactional
    public void deleteVehicule(UUID id) {
        Vehicule vehicule = vehiculeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Véhicule non trouvé avec l'ID : " + id));

        vehiculeRepository.delete(vehicule);
    }

    @Transactional(readOnly = true)
    public List<VehiculeFileDTO> getVehiculeFiles(UUID vehiculeId) {
        Vehicule vehicule = vehiculeRepository.findById(vehiculeId)
                .orElseThrow(() -> new RuntimeException("Véhicule non trouvé avec l'ID : " + vehiculeId));

        return vehiculeFileRepository.findByVehiculeOrderByCreatedAtDesc(vehicule).stream()
                .map(vehiculeFileMapper::toDTOWithUrl)
                .collect(Collectors.toList());
    }

    @Transactional
    public VehiculeFileDTO addVehiculeFile(UUID vehiculeId, VehiculeFileUploadRequest request) {
        Vehicule vehicule = vehiculeRepository.findById(vehiculeId)
                .orElseThrow(() -> new RuntimeException("Véhicule non trouvé avec l'ID : " + vehiculeId));

        FileStorageService.FileInfo fileInfo = fileStorageService.saveVehiculeFile(
                request.getFileB64(),
                request.getOriginalName(),
                request.getMimeType()
        );

        VehiculeFile file = new VehiculeFile();
        file.setVehicule(vehicule);
        file.setFilePath(fileInfo.fileName());
        file.setOriginalName(request.getOriginalName());
        file.setMimeType(request.getMimeType());
        file.setFileSize(fileInfo.fileSize());

        VehiculeFile saved = vehiculeFileRepository.save(file);
        return vehiculeFileMapper.toDTOWithUrl(saved);
    }

    @Transactional
    public void deleteVehiculeFile(UUID fileId) {
        VehiculeFile file = vehiculeFileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("Fichier non trouvé avec l'ID : " + fileId));

        fileStorageService.deleteVehiculeFile(file.getFilePath());
        vehiculeFileRepository.delete(file);
    }

    @Transactional(readOnly = true)
    public VehiculeKilometrageListResponse getVehiculeKilometrages(UUID vehiculeId, Pageable pageable, boolean fetchAll) {
        Vehicule vehicule = vehiculeRepository.findById(vehiculeId)
                .orElseThrow(() -> new RuntimeException("Véhicule non trouvé avec l'ID : " + vehiculeId));

        VehiculeKilometrageListResponse response = new VehiculeKilometrageListResponse();
        response.setSuccess(true);

        if (fetchAll) {
            List<VehiculeKilometrage> allKilometrages = vehiculeKilometrageRepository.findByVehiculeOrderByCreatedAtDesc(vehicule);
            List<VehiculeKilometrageDTO> kilometrages = allKilometrages.stream()
                    .map(vehiculeKilometrageMapper::toDTO)
                    .collect(Collectors.toList());
            response.setKilometrages(kilometrages);
            response.setTotalElements((long) kilometrages.size());
        } else {
            Page<VehiculeKilometrage> page = vehiculeKilometrageRepository.findByVehiculeOrderByCreatedAtDesc(vehicule, pageable);
            List<VehiculeKilometrageDTO> kilometrages = page.getContent().stream()
                    .map(vehiculeKilometrageMapper::toDTO)
                    .collect(Collectors.toList());
            response.setKilometrages(kilometrages);
            response.setPage(page.getNumber());
            response.setSize(page.getSize());
            response.setTotalElements(page.getTotalElements());
            response.setTotalPages(page.getTotalPages());
        }

        return response;
    }

    @Transactional
    public VehiculeKilometrageDTO addVehiculeKilometrage(VehiculeKilometrageCreateRequest request, User currentUser) {
        Vehicule vehicule = vehiculeRepository.findById(request.getVehiculeId())
                .orElseThrow(() -> new RuntimeException("Véhicule non trouvé avec l'ID : " + request.getVehiculeId()));

        VehiculeKilometrage kilometrage = new VehiculeKilometrage();
        kilometrage.setVehicule(vehicule);
        kilometrage.setKm(request.getKm());
        kilometrage.setUser(currentUser);

        VehiculeKilometrage saved = vehiculeKilometrageRepository.save(kilometrage);
        return vehiculeKilometrageMapper.toDTO(saved);
    }

    @Transactional
    public VehiculeKilometrageDTO addVehiculeKilometrageAsAdmin(VehiculeKilometrageAdminCreateRequest request) {
        Vehicule vehicule = vehiculeRepository.findById(request.getVehiculeId())
                .orElseThrow(() -> new RuntimeException("Véhicule non trouvé avec l'ID : " + request.getVehiculeId()));

        User user = null;
        if (request.getUserUuid() != null) {
            user = userRepository.findById(request.getUserUuid())
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'ID : " + request.getUserUuid()));
        }

        VehiculeKilometrage kilometrage = new VehiculeKilometrage();
        kilometrage.setVehicule(vehicule);
        kilometrage.setKm(request.getKm());
        kilometrage.setUser(user);
        if (request.getCreatedAt() != null) {
            kilometrage.setCreatedAt(request.getCreatedAt());
        }

        VehiculeKilometrage saved = vehiculeKilometrageRepository.save(kilometrage);
        return vehiculeKilometrageMapper.toDTO(saved);
    }

    @Transactional
    public VehiculeKilometrageDTO updateVehiculeKilometrage(UUID id, VehiculeKilometrageUpdateRequest request) {
        VehiculeKilometrage kilometrage = vehiculeKilometrageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Kilométrage non trouvé avec l'ID : " + id));

        if (request.getKm() != null) {
            kilometrage.setKm(request.getKm());
        }

        if (request.getUserUuid() != null) {
            User user = userRepository.findById(request.getUserUuid())
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'ID : " + request.getUserUuid()));
            kilometrage.setUser(user);
        }

        if (request.getCreatedAt() != null) {
            kilometrage.setCreatedAt(request.getCreatedAt());
        }

        VehiculeKilometrage saved = vehiculeKilometrageRepository.save(kilometrage);
        return vehiculeKilometrageMapper.toDTO(saved);
    }

    @Transactional(readOnly = true)
    public VehiculeAdjustInfoListResponse getVehiculeAdjustInfos(UUID vehiculeId, Pageable pageable) {
        Vehicule vehicule = vehiculeRepository.findById(vehiculeId)
                .orElseThrow(() -> new RuntimeException("Véhicule non trouvé avec l'ID : " + vehiculeId));

        Page<VehiculeAdjustInfo> page = vehiculeAdjustInfoRepository.findByVehiculeOrderByCreatedAtDesc(vehicule, pageable);

        List<VehiculeAdjustInfoDTO> adjustInfos = page.getContent().stream()
                .map(vehiculeAdjustInfoMapper::toDTO)
                .collect(Collectors.toList());

        VehiculeAdjustInfoListResponse response = new VehiculeAdjustInfoListResponse();
        response.setSuccess(true);
        response.setAdjustInfos(adjustInfos);
        response.setPage(page.getNumber());
        response.setSize(page.getSize());
        response.setTotalElements(page.getTotalElements());
        response.setTotalPages(page.getTotalPages());

        return response;
    }

    @Transactional
    public VehiculeAdjustInfoDTO createVehiculeAdjustInfo(VehiculeAdjustInfoCreateRequest request, User currentUser) {
        Vehicule vehicule = vehiculeRepository.findById(request.getVehiculeId())
                .orElseThrow(() -> new RuntimeException("Véhicule non trouvé avec l'ID : " + request.getVehiculeId()));

        VehiculeAdjustInfo adjustInfo = new VehiculeAdjustInfo();
        adjustInfo.setVehicule(vehicule);
        adjustInfo.setUser(currentUser);
        adjustInfo.setComment(request.getComment());

        VehiculeAdjustInfo saved = vehiculeAdjustInfoRepository.save(adjustInfo);

        if (request.getPicturesB64() != null && !request.getPicturesB64().isEmpty()) {
            for (String pictureB64 : request.getPicturesB64()) {
                String pictureUrl = fileStorageService.saveVehiculeAdjustImage(pictureB64, saved.getId());

                VehiculeAdjustInfoPicture picture = new VehiculeAdjustInfoPicture();
                picture.setAdjustInfo(saved);
                picture.setPicturePath(fileStorageService.extractFileNameFromUrl(pictureUrl));
                vehiculeAdjustInfoPictureRepository.save(picture);
            }
        }

        return vehiculeAdjustInfoMapper.toDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<VehiculeAdjustInfoPictureDTO> getAdjustInfoPictures(UUID adjustInfoId) {
        VehiculeAdjustInfo adjustInfo = vehiculeAdjustInfoRepository.findById(adjustInfoId)
                .orElseThrow(() -> new RuntimeException("Adjust Info non trouvé avec l'ID : " + adjustInfoId));

        return vehiculeAdjustInfoPictureRepository.findByAdjustInfo(adjustInfo).stream()
                .map(vehiculeAdjustInfoPictureMapper::toDTO)
                .collect(Collectors.toList());
    }
}
