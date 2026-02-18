package bzh.stack.apiavtrans.mapper;

import bzh.stack.apiavtrans.dto.vehicule.RapportVehiculeDTO;
import bzh.stack.apiavtrans.entity.RapportVehicule;
import bzh.stack.apiavtrans.entity.RapportVehiculePicture;
import bzh.stack.apiavtrans.entity.VehiculeKilometrage;
import bzh.stack.apiavtrans.repository.RapportVehiculePictureRepository;
import bzh.stack.apiavtrans.repository.VehiculeKilometrageRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class RapportVehiculeMapper {

    private final UserMapper userMapper;
    private final VehiculeMapper vehiculeMapper;
    private final RapportVehiculePictureMapper rapportVehiculePictureMapper;
    private final RapportVehiculePictureRepository rapportVehiculePictureRepository;
    private final VehiculeKilometrageRepository vehiculeKilometrageRepository;

    public RapportVehiculeMapper(UserMapper userMapper,
                                  VehiculeMapper vehiculeMapper,
                                  RapportVehiculePictureMapper rapportVehiculePictureMapper,
                                  RapportVehiculePictureRepository rapportVehiculePictureRepository,
                                  VehiculeKilometrageRepository vehiculeKilometrageRepository) {
        this.userMapper = userMapper;
        this.vehiculeMapper = vehiculeMapper;
        this.rapportVehiculePictureMapper = rapportVehiculePictureMapper;
        this.rapportVehiculePictureRepository = rapportVehiculePictureRepository;
        this.vehiculeKilometrageRepository = vehiculeKilometrageRepository;
    }

    public RapportVehiculeDTO toDTO(RapportVehicule rapport) {
        if (rapport == null) {
            return null;
        }

        RapportVehiculeDTO dto = new RapportVehiculeDTO();
        dto.setId(rapport.getId());
        dto.setUser(userMapper.toDTO(rapport.getUser()));

        VehiculeKilometrage latestKm = null;
        if (rapport.getVehicule() != null) {
            latestKm = vehiculeKilometrageRepository.findLatestByVehicule(rapport.getVehicule()).orElse(null);
        }
        dto.setVehicule(vehiculeMapper.toDTO(rapport.getVehicule(), latestKm));

        dto.setCommentaire(rapport.getCommentaire());
        dto.setCreatedAt(rapport.getCreatedAt());

        List<RapportVehiculePicture> pictures = rapportVehiculePictureRepository.findByRapportVehicule(rapport);
        dto.setPictures(pictures.stream()
                .map(rapportVehiculePictureMapper::toDTO)
                .collect(Collectors.toList()));

        return dto;
    }

    public RapportVehicule toEntity(RapportVehiculeDTO dto) {
        if (dto == null) {
            return null;
        }

        RapportVehicule rapport = new RapportVehicule();
        rapport.setId(dto.getId());
        rapport.setUser(userMapper.toEntity(dto.getUser()));
        rapport.setVehicule(vehiculeMapper.toEntity(dto.getVehicule()));
        rapport.setCommentaire(dto.getCommentaire());
        rapport.setCreatedAt(dto.getCreatedAt());

        return rapport;
    }
}
