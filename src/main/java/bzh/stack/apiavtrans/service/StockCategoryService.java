package bzh.stack.apiavtrans.service;

import bzh.stack.apiavtrans.dto.stock.StockCategoryCreateRequest;
import bzh.stack.apiavtrans.dto.stock.StockCategoryDTO;
import bzh.stack.apiavtrans.dto.stock.StockCategoryUpdateRequest;
import bzh.stack.apiavtrans.entity.StockCategory;
import bzh.stack.apiavtrans.mapper.StockCategoryMapper;
import bzh.stack.apiavtrans.repository.StockCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockCategoryService {

    private final StockCategoryRepository stockCategoryRepository;
    private final StockCategoryMapper stockCategoryMapper;

    @Transactional(readOnly = true)
    public List<StockCategoryDTO> getAllCategories() {
        return stockCategoryRepository.findAllByOrderByNomAsc().stream()
                .map(stockCategoryMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public StockCategoryDTO getCategoryById(UUID id) {
        StockCategory category = stockCategoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Catégorie non trouvée avec l'ID : " + id));
        return stockCategoryMapper.toDTO(category);
    }

    @Transactional
    public StockCategoryDTO createCategory(StockCategoryCreateRequest request) {
        if (stockCategoryRepository.findByNom(request.getNom()).isPresent()) {
            throw new RuntimeException("Une catégorie avec ce nom existe déjà : " + request.getNom());
        }

        StockCategory category = new StockCategory();
        category.setNom(request.getNom());
        category.setDescription(request.getDescription());

        StockCategory saved = stockCategoryRepository.save(category);
        return stockCategoryMapper.toDTO(saved);
    }

    @Transactional
    public StockCategoryDTO updateCategory(UUID id, StockCategoryUpdateRequest request) {
        StockCategory category = stockCategoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Catégorie non trouvée avec l'ID : " + id));

        if (request.getNom() != null && !category.getNom().equals(request.getNom())) {
            stockCategoryRepository.findByNom(request.getNom()).ifPresent(existing -> {
                throw new RuntimeException("Une autre catégorie possède déjà ce nom : " + request.getNom());
            });
            category.setNom(request.getNom());
        }

        if (request.getDescription() != null) {
            category.setDescription(request.getDescription());
        }

        StockCategory updated = stockCategoryRepository.save(category);
        return stockCategoryMapper.toDTO(updated);
    }

    @Transactional
    public void deleteCategory(UUID id) {
        StockCategory category = stockCategoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Catégorie non trouvée avec l'ID : " + id));
        stockCategoryRepository.delete(category);
    }
}
