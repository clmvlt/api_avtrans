package bzh.stack.apiavtrans.controller;

import bzh.stack.apiavtrans.annotation.RequireRole;
import bzh.stack.apiavtrans.dto.appversion.AppVersionCheckResponse;
import bzh.stack.apiavtrans.dto.appversion.AppVersionDTO;
import bzh.stack.apiavtrans.dto.appversion.AppVersionListResponse;
import bzh.stack.apiavtrans.dto.appversion.AppVersionResponse;
import bzh.stack.apiavtrans.dto.appversion.AppVersionUpdateRequest;
import bzh.stack.apiavtrans.dto.appversion.AppVersionUploadRequest;
import bzh.stack.apiavtrans.dto.common.ErrorResponse;
import bzh.stack.apiavtrans.dto.common.SuccessMessageResponse;
import bzh.stack.apiavtrans.entity.AppVersion;
import bzh.stack.apiavtrans.entity.User;
import bzh.stack.apiavtrans.service.AppVersionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/app-versions")
@RequiredArgsConstructor
@Tag(name = "App Versions", description = "Mobile app version management and updates")
public class AppVersionController {

    private final AppVersionService appVersionService;

    // ==================== ADMIN ROUTES ====================

    @RequireRole("Administrateur")
    @Operation(summary = "[ADMINISTRATEUR] Upload a new app version")
    @ApiResponse(responseCode = "200", description = "Version uploaded successfully",
            content = @Content(schema = @Schema(implementation = AppVersionResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid request or version code already exists",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping
    public ResponseEntity<?> uploadVersion(
            @RequestBody AppVersionUploadRequest request,
            HttpServletRequest httpRequest) {
        try {
            User admin = (User) httpRequest.getAttribute("user");
            AppVersionDTO dto = appVersionService.uploadVersion(request, admin);
            return ResponseEntity.ok(new AppVersionResponse(true, "Version uploaded successfully", dto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @RequireRole("Administrateur")
    @Operation(summary = "[ADMINISTRATEUR] Update version metadata")
    @ApiResponse(responseCode = "200", description = "Version updated successfully",
            content = @Content(schema = @Schema(implementation = AppVersionResponse.class)))
    @ApiResponse(responseCode = "404", description = "Version not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PutMapping("/{id}")
    public ResponseEntity<?> updateVersion(
            @Parameter(description = "Version UUID") @PathVariable UUID id,
            @RequestBody AppVersionUpdateRequest request) {
        try {
            AppVersionDTO dto = appVersionService.updateVersion(id, request);
            return ResponseEntity.ok(new AppVersionResponse(true, "Version updated successfully", dto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @RequireRole("Administrateur")
    @Operation(summary = "[ADMINISTRATEUR] Delete a version")
    @ApiResponse(responseCode = "200", description = "Version deleted successfully",
            content = @Content(schema = @Schema(implementation = SuccessMessageResponse.class)))
    @ApiResponse(responseCode = "404", description = "Version not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteVersion(
            @Parameter(description = "Version UUID") @PathVariable UUID id) {
        try {
            appVersionService.deleteVersion(id);
            return ResponseEntity.ok(new SuccessMessageResponse(true, "Version deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @RequireRole("Administrateur")
    @Operation(summary = "[ADMINISTRATEUR] Get all versions including inactive")
    @ApiResponse(responseCode = "200", description = "List of all versions",
            content = @Content(schema = @Schema(implementation = AppVersionListResponse.class)))
    @GetMapping("/admin")
    public ResponseEntity<AppVersionListResponse> getAllVersionsAdmin() {
        List<AppVersionDTO> versions = appVersionService.getAllVersions();
        return ResponseEntity.ok(new AppVersionListResponse(true, versions));
    }

    // ==================== PUBLIC ROUTES ====================

    @Operation(summary = "Get all active versions")
    @ApiResponse(responseCode = "200", description = "List of active versions",
            content = @Content(schema = @Schema(implementation = AppVersionListResponse.class)))
    @GetMapping
    public ResponseEntity<AppVersionListResponse> getActiveVersions() {
        List<AppVersionDTO> versions = appVersionService.getActiveVersions();
        return ResponseEntity.ok(new AppVersionListResponse(true, versions));
    }

    @Operation(summary = "Get latest active version info")
    @ApiResponse(responseCode = "200", description = "Latest version info",
            content = @Content(schema = @Schema(implementation = AppVersionResponse.class)))
    @ApiResponse(responseCode = "404", description = "No active version available",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/latest")
    public ResponseEntity<?> getLatestVersion() {
        try {
            AppVersionDTO dto = appVersionService.getLatestVersion();
            return ResponseEntity.ok(new AppVersionResponse(true, null, dto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "Get version info by ID")
    @ApiResponse(responseCode = "200", description = "Version info",
            content = @Content(schema = @Schema(implementation = AppVersionResponse.class)))
    @ApiResponse(responseCode = "404", description = "Version not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/{id}")
    public ResponseEntity<?> getVersionById(
            @Parameter(description = "Version UUID") @PathVariable UUID id) {
        try {
            AppVersionDTO dto = appVersionService.getVersionById(id);
            return ResponseEntity.ok(new AppVersionResponse(true, null, dto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "Check if an update is available")
    @ApiResponse(responseCode = "200", description = "Update check result",
            content = @Content(schema = @Schema(implementation = AppVersionCheckResponse.class)))
    @GetMapping("/check")
    public ResponseEntity<AppVersionCheckResponse> checkForUpdate(
            @Parameter(description = "Current version code installed on device")
            @RequestParam Integer currentVersion) {
        AppVersionCheckResponse checkResponse = appVersionService.checkForUpdate(currentVersion);
        checkResponse.setSuccess(true);
        return ResponseEntity.ok(checkResponse);
    }

    @Operation(summary = "Download APK by version ID")
    @ApiResponse(responseCode = "200", description = "APK file download")
    @ApiResponse(responseCode = "404", description = "Version not found or inactive")
    @GetMapping("/{id}/download")
    public ResponseEntity<?> downloadVersion(
            @Parameter(description = "Version UUID") @PathVariable UUID id) {
        try {
            AppVersion appVersion = appVersionService.getAppVersionById(id);
            Resource resource = appVersionService.getApkFileForDownload(id);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/vnd.android.package-archive"))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + appVersion.getOriginalFileName() + "\"")
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(appVersion.getFileSize()))
                    .body(resource);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(new ErrorResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "Download latest APK version")
    @ApiResponse(responseCode = "200", description = "Latest APK file download")
    @ApiResponse(responseCode = "404", description = "No active version available")
    @GetMapping("/latest/download")
    public ResponseEntity<?> downloadLatestVersion() {
        try {
            AppVersion appVersion = appVersionService.getLatestAppVersion();
            Resource resource = appVersionService.getLatestApkFileForDownload();

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/vnd.android.package-archive"))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + appVersion.getOriginalFileName() + "\"")
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(appVersion.getFileSize()))
                    .body(resource);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(new ErrorResponse(false, e.getMessage()));
        }
    }
}
