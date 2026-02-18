package bzh.stack.apiavtrans.mapper;

import bzh.stack.apiavtrans.dto.common.EmailVerificationDTO;
import bzh.stack.apiavtrans.entity.EmailVerification;
import org.springframework.stereotype.Component;

@Component
public class EmailVerificationMapper {


    public EmailVerificationDTO toDTO(EmailVerification emailVerification) {
        if (emailVerification == null) {
            return null;
        }

        EmailVerificationDTO dto = new EmailVerificationDTO();
        dto.setUuid(emailVerification.getUuid());
        dto.setEmail(emailVerification.getEmail());
        dto.setToken(emailVerification.getToken());
        dto.setVerificationDateTime(emailVerification.getVerificationDateTime());
        dto.setUserUuid(emailVerification.getUser() != null ? emailVerification.getUser().getUuid() : null);

        return dto;
    }

    public EmailVerification toEntity(EmailVerificationDTO dto) {
        if (dto == null) {
            return null;
        }

        EmailVerification emailVerification = new EmailVerification();
        emailVerification.setUuid(dto.getUuid());
        emailVerification.setEmail(dto.getEmail());
        emailVerification.setToken(dto.getToken());
        emailVerification.setVerificationDateTime(dto.getVerificationDateTime());

        return emailVerification;
    }
}
