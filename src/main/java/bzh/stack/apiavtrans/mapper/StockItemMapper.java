package bzh.stack.apiavtrans.mapper;

import bzh.stack.apiavtrans.dto.stock.StockItemDTO;
import bzh.stack.apiavtrans.entity.StockItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockItemMapper {

    private final StockCategoryMapper stockCategoryMapper;

    public StockItemDTO toDTO(StockItem stockItem) {
        if (stockItem == null) {
            return null;
        }

        StockItemDTO dto = new StockItemDTO();
        dto.setId(stockItem.getId());
        dto.setReference(stockItem.getReference());
        dto.setNom(stockItem.getNom());
        dto.setDescription(stockItem.getDescription());
        dto.setQuantite(stockItem.getQuantite());
        dto.setCategory(stockCategoryMapper.toDTO(stockItem.getCategory()));
        dto.setUnite(stockItem.getUnite());
        dto.setPrixUnitaire(stockItem.getPrixUnitaire());
        dto.setCreatedAt(stockItem.getCreatedAt());
        dto.setUpdatedAt(stockItem.getUpdatedAt());

        return dto;
    }
}
