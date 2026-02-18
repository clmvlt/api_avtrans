package bzh.stack.apiavtrans.service;

import bzh.stack.apiavtrans.dto.appversion.AppVersionCheckResponse;
import bzh.stack.apiavtrans.dto.appversion.AppVersionDTO;
import bzh.stack.apiavtrans.dto.appversion.AppVersionUpdateRequest;
import bzh.stack.apiavtrans.dto.appversion.AppVersionUploadRequest;
import bzh.stack.apiavtrans.entity.AppVersion;
import bzh.stack.apiavtrans.entity.User;
import bzh.stack.apiavtrans.mapper.AppVersionMapper;
import bzh.stack.apiavtrans.repository.AppVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppVersionService {

    private final AppVersionRepository appVersionRepository;
    private final FileStorageService fileStorageService;
    private final AppVersionMapper appVersionMapper;

    @Transactional
    public AppVersionDTO uploadVersion(AppVersionUploadRequest request, User admin) {
        if (appVersionRepository.existsByVersionCode(request.getVersionCode())) {
            throw new RuntimeException("Version code " + request.getVersionCode() + " already exists");
        }

        FileStorageService.FileInfo fileInfo = fileStorageService.saveApkFile(
                request.getApkB64(),
                request.getVersionCode()
        );

        AppVersion appVersion = new AppVersion();
        appVersion.setVersionCode(request.getVersionCode());
        appVersion.setVersionName(request.getVersionName());
        appVersion.setFileName(fileInfo.fileName());
        appVersion.setOriginalFileName(request.getOriginalFileName() != null ?
                request.getOriginalFileName() : "app-v" + request.getVersionName() + ".apk");
        appVersion.setFileSize(fileInfo.fileSize());
        appVersion.setChangelog(request.getChangelog());
        appVersion.setIsActive(true);
        appVersion.setDownloadCount(0L);
        appVersion.setCreatedBy(admin);

        AppVersion saved = appVersionRepository.save(appVersion);
        return appVersionMapper.toDTO(saved);
    }

    @Transactional
    public AppVersionDTO updateVersion(UUID id, AppVersionUpdateRequest request) {
        AppVersion appVersion = appVersionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Version not found"));

        if (request.getChangelog() != null) {
            appVersion.setChangelog(request.getChangelog());
        }
        if (request.getIsActive() != null) {
            appVersion.setIsActive(request.getIsActive());
        }

        AppVersion saved = appVersionRepository.save(appVersion);
        return appVersionMapper.toDTO(saved);
    }

    @Transactional
    public void deleteVersion(UUID id) {
        AppVersion appVersion = appVersionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Version not found"));

        fileStorageService.deleteApkFile(appVersion.getFileName());
        appVersionRepository.delete(appVersion);
    }

    public List<AppVersionDTO> getAllVersions() {
        return appVersionRepository.findAllByOrderByVersionCodeDesc()
                .stream()
                .map(appVersionMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<AppVersionDTO> getActiveVersions() {
        return appVersionRepository.findByIsActiveTrueOrderByVersionCodeDesc()
                .stream()
                .map(appVersionMapper::toDTO)
                .collect(Collectors.toList());
    }

    public AppVersionDTO getVersionById(UUID id) {
        AppVersion appVersion = appVersionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Version not found"));
        return appVersionMapper.toDTO(appVersion);
    }

    public AppVersionDTO getLatestVersion() {
        AppVersion appVersion = appVersionRepository.findTopByIsActiveTrueOrderByVersionCodeDesc()
                .orElseThrow(() -> new RuntimeException("No active version available"));
        return appVersionMapper.toDTO(appVersion);
    }

    public AppVersionCheckResponse checkForUpdate(Integer currentVersionCode) {
        AppVersionCheckResponse response = new AppVersionCheckResponse();
        response.setCurrentVersionCode(currentVersionCode);

        AppVersion latest = appVersionRepository.findTopByIsActiveTrueOrderByVersionCodeDesc()
                .orElse(null);

        if (latest == null) {
            response.setUpdateAvailable(false);
            response.setLatestVersionCode(null);
            response.setLatestVersion(null);
        } else {
            response.setLatestVersionCode(latest.getVersionCode());
            boolean updateAvailable = latest.getVersionCode() > currentVersionCode;
            response.setUpdateAvailable(updateAvailable);
            response.setLatestVersion(updateAvailable ? appVersionMapper.toDTO(latest) : null);
        }

        return response;
    }

    @Transactional
    public Resource getApkFileForDownload(UUID id) {
        AppVersion appVersion = appVersionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Version not found"));

        if (!appVersion.getIsActive()) {
            throw new RuntimeException("This version is not available for download");
        }

        appVersionRepository.incrementDownloadCount(id);

        Path filePath = fileStorageService.getApkFilePath(appVersion.getFileName());
        try {
            return new InputStreamResource(new FileInputStream(filePath.toFile()));
        } catch (FileNotFoundException e) {
            throw new RuntimeException("APK file not found on server");
        }
    }

    @Transactional
    public Resource getLatestApkFileForDownload() {
        AppVersion appVersion = appVersionRepository.findTopByIsActiveTrueOrderByVersionCodeDesc()
                .orElseThrow(() -> new RuntimeException("No active version available"));

        appVersionRepository.incrementDownloadCount(appVersion.getId());

        Path filePath = fileStorageService.getApkFilePath(appVersion.getFileName());
        try {
            return new InputStreamResource(new FileInputStream(filePath.toFile()));
        } catch (FileNotFoundException e) {
            throw new RuntimeException("APK file not found on server");
        }
    }

    public AppVersion getAppVersionById(UUID id) {
        return appVersionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Version not found"));
    }

    public AppVersion getLatestAppVersion() {
        return appVersionRepository.findTopByIsActiveTrueOrderByVersionCodeDesc()
                .orElseThrow(() -> new RuntimeException("No active version available"));
    }
}
