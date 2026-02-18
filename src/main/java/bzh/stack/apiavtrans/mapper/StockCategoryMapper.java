package bzh.stack.apiavtrans.mapper;

import bzh.stack.apiavtrans.dto.stock.StockCategoryDTO;
import bzh.stack.apiavtrans.entity.StockCategory;
import org.springframework.stereotype.Component;

@Component
public class StockCategoryMapper {

    public StockCategoryDTO toDTO(StockCategory category) {
        if (category == null) {
            return null;
        }

        StockCategoryDTO dto = new StockCategoryDTO();
        dto.setId(category.getId());
        dto.setNom(category.getNom());
        dto.setDescription(category.getDescription());
        dto.setCreatedAt(category.getCreatedAt());
        dto.setUpdatedAt(category.getUpdatedAt());

        return dto;
    }
}
