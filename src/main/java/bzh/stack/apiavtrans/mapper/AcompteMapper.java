package bzh.stack.apiavtrans.mapper;

import bzh.stack.apiavtrans.dto.acompte.AcompteDTO;
import bzh.stack.apiavtrans.entity.Acompte;
import org.springframework.stereotype.Component;

@Component
public class AcompteMapper {

    private final UserMapper userMapper;

    public AcompteMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public AcompteDTO toDTO(Acompte acompte) {
        if (acompte == null) {
            return null;
        }

        AcompteDTO dto = new AcompteDTO();
        dto.setUuid(acompte.getUuid());
        dto.setMontant(acompte.getMontant());
        dto.setRaison(acompte.getRaison());
        dto.setStatus(acompte.getStatus() != null ? acompte.getStatus().name() : null);
        dto.setRejectionReason(acompte.getRejectionReason());
        dto.setValidatedAt(acompte.getValidatedAt());
        dto.setIsPaid(acompte.getIsPaid());
        dto.setPaidDate(acompte.getPaidDate());
        dto.setCreatedAt(acompte.getCreatedAt());
        dto.setUpdatedAt(acompte.getUpdatedAt());

        if (acompte.getUser() != null) {
            dto.setUserUuid(acompte.getUser().getUuid());
            dto.setUser(userMapper.toDTO(acompte.getUser()));
        }

        if (acompte.getValidatedBy() != null) {
            dto.setValidatedByUuid(acompte.getValidatedBy().getUuid());
            dto.setValidatedBy(userMapper.toDTO(acompte.getValidatedBy()));
        }

        return dto;
    }
}
