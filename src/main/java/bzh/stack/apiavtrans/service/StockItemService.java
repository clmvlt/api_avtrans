package bzh.stack.apiavtrans.service;

import bzh.stack.apiavtrans.dto.stock.StockItemCreateRequest;
import bzh.stack.apiavtrans.dto.stock.StockItemDTO;
import bzh.stack.apiavtrans.dto.stock.StockItemUpdateRequest;
import bzh.stack.apiavtrans.entity.StockCategory;
import bzh.stack.apiavtrans.entity.StockItem;
import bzh.stack.apiavtrans.mapper.StockItemMapper;
import bzh.stack.apiavtrans.repository.StockCategoryRepository;
import bzh.stack.apiavtrans.repository.StockItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockItemService {

    private final StockItemRepository stockItemRepository;
    private final StockCategoryRepository stockCategoryRepository;
    private final StockItemMapper stockItemMapper;

    @Transactional(readOnly = true)
    public List<StockItemDTO> getAllStockItems() {
        return stockItemRepository.findAllByOrderByNomAsc().stream()
                .map(stockItemMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public StockItemDTO getStockItemById(UUID id) {
        StockItem stockItem = stockItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Article non trouvé avec l'ID : " + id));
        return stockItemMapper.toDTO(stockItem);
    }

    @Transactional
    public StockItemDTO createStockItem(StockItemCreateRequest request) {
        if (stockItemRepository.findByReference(request.getReference()).isPresent()) {
            throw new RuntimeException("Un article avec cette référence existe déjà : " + request.getReference());
        }

        StockItem stockItem = new StockItem();
        stockItem.setReference(request.getReference());
        stockItem.setNom(request.getNom());
        stockItem.setDescription(request.getDescription());
        stockItem.setQuantite(request.getQuantite());
        stockItem.setUnite(request.getUnite());

        if (request.getCategoryId() != null) {
            StockCategory category = stockCategoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Catégorie non trouvée avec l'ID : " + request.getCategoryId()));
            stockItem.setCategory(category);
        }

        StockItem saved = stockItemRepository.save(stockItem);
        return stockItemMapper.toDTO(saved);
    }

    @Transactional
    public StockItemDTO updateStockItem(UUID id, StockItemUpdateRequest request) {
        StockItem stockItem = stockItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Article non trouvé avec l'ID : " + id));

        if (request.getReference() != null && !stockItem.getReference().equals(request.getReference())) {
            stockItemRepository.findByReference(request.getReference()).ifPresent(existing -> {
                throw new RuntimeException("Un autre article possède déjà cette référence : " + request.getReference());
            });
            stockItem.setReference(request.getReference());
        }

        if (request.getNom() != null) {
            stockItem.setNom(request.getNom());
        }

        if (request.getDescription() != null) {
            stockItem.setDescription(request.getDescription());
        }

        if (request.getQuantite() != null) {
            stockItem.setQuantite(request.getQuantite());
        }

        if (request.getCategoryId() != null) {
            StockCategory category = stockCategoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Catégorie non trouvée avec l'ID : " + request.getCategoryId()));
            stockItem.setCategory(category);
        }

        if (request.getUnite() != null) {
            stockItem.setUnite(request.getUnite());
        }

        StockItem updated = stockItemRepository.save(stockItem);
        return stockItemMapper.toDTO(updated);
    }

    @Transactional
    public void deleteStockItem(UUID id) {
        StockItem stockItem = stockItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Article non trouvé avec l'ID : " + id));
        stockItemRepository.delete(stockItem);
    }
}
