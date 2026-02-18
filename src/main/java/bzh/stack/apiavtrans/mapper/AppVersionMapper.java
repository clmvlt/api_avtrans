package bzh.stack.apiavtrans.mapper;

import bzh.stack.apiavtrans.dto.appversion.AppVersionDTO;
import bzh.stack.apiavtrans.entity.AppVersion;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AppVersionMapper {

    @Value("${app.base-api-url:http://192.168.1.120:8081}")
    private String baseUrl;

    public AppVersionDTO toDTO(AppVersion appVersion) {
        if (appVersion == null) {
            return null;
        }

        AppVersionDTO dto = new AppVersionDTO();
        dto.setId(appVersion.getId());
        dto.setVersionCode(appVersion.getVersionCode());
        dto.setVersionName(appVersion.getVersionName());
        dto.setOriginalFileName(appVersion.getOriginalFileName());
        dto.setFileSize(appVersion.getFileSize());
        dto.setChangelog(appVersion.getChangelog());
        dto.setIsActive(appVersion.getIsActive());
        dto.setDownloadCount(appVersion.getDownloadCount());
        dto.setDownloadUrl(baseUrl + "/app-versions/" + appVersion.getId() + "/download");
        dto.setCreatedAt(appVersion.getCreatedAt());

        if (appVersion.getCreatedBy() != null) {
            dto.setCreatedByUuid(appVersion.getCreatedBy().getUuid());
            dto.setCreatedByName(appVersion.getCreatedBy().getFirstName() + " " + appVersion.getCreatedBy().getLastName());
        }

        return dto;
    }
}
