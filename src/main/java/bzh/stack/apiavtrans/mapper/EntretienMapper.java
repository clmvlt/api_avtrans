package bzh.stack.apiavtrans.mapper;

import bzh.stack.apiavtrans.dto.entretien.EntretienDTO;
import bzh.stack.apiavtrans.entity.Entretien;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class EntretienMapper {

    private final TypeEntretienMapper typeEntretienMapper;
    private final UserMapper userMapper;
    private final EntretienFileMapper entretienFileMapper;

    public EntretienDTO toDTO(Entretien entretien) {
        if (entretien == null) {
            return null;
        }

        EntretienDTO dto = new EntretienDTO();
        dto.setId(entretien.getId());
        dto.setVehiculeId(entretien.getVehicule() != null ? entretien.getVehicule().getId() : null);
        dto.setVehiculeImmat(entretien.getVehicule() != null ? entretien.getVehicule().getImmat() : null);
        dto.setTypeEntretien(typeEntretienMapper.toDTO(entretien.getTypeEntretien()));
        dto.setMecanicien(userMapper.toDTO(entretien.getMecanicien()));
        dto.setDateEntretien(entretien.getDateEntretien());
        dto.setKilometrage(entretien.getKilometrage());
        dto.setCommentaire(entretien.getCommentaire());
        dto.setCout(entretien.getCout());
        dto.setCreatedAt(entretien.getCreatedAt());

        if (entretien.getFiles() != null) {
            dto.setFiles(entretien.getFiles().stream()
                    .map(entretienFileMapper::toDTOWithUrl)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    public Entretien toEntity(EntretienDTO dto) {
        if (dto == null) {
            return null;
        }

        Entretien entretien = new Entretien();
        entretien.setId(dto.getId());
        entretien.setDateEntretien(dto.getDateEntretien());
        entretien.setKilometrage(dto.getKilometrage());
        entretien.setCommentaire(dto.getCommentaire());
        entretien.setCout(dto.getCout());
        entretien.setCreatedAt(dto.getCreatedAt());

        return entretien;
    }
}
