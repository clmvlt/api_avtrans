package bzh.stack.apiavtrans.mapper;

import bzh.stack.apiavtrans.dto.signature.SignatureDTO;
import bzh.stack.apiavtrans.entity.Signature;
import org.springframework.stereotype.Component;

@Component
public class SignatureMapper {

    private final UserMapper userMapper;

    public SignatureMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public SignatureDTO toDTO(Signature signature) {
        if (signature == null) {
            return null;
        }

        SignatureDTO dto = new SignatureDTO();
        dto.setUuid(signature.getUuid());
        dto.setSignatureBase64(signature.getSignatureBase64());
        dto.setDate(signature.getDate());
        dto.setHeuresSignees(signature.getHeuresSignees());
        dto.setUser(userMapper.toDTO(signature.getUser()));
        dto.setCreatedAt(signature.getCreatedAt());

        return dto;
    }

    public Signature toEntity(SignatureDTO dto) {
        if (dto == null) {
            return null;
        }

        Signature signature = new Signature();
        signature.setUuid(dto.getUuid());
        signature.setSignatureBase64(dto.getSignatureBase64());
        signature.setDate(dto.getDate());
        signature.setHeuresSignees(dto.getHeuresSignees());
        signature.setCreatedAt(dto.getCreatedAt());

        return signature;
    }
}
