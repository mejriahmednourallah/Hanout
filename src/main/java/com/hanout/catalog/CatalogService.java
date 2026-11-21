package com.hanout.catalog;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class CatalogService {

    private final ProductRepository productRepository;
    private final ProductI18nRepository i18nRepository;
    private final ProductAliasRepository aliasRepository;
    private final SkuRepository skuRepository;
    private final CategoryRepository categoryRepository;

    public CatalogService(ProductRepository productRepository,
                          ProductI18nRepository i18nRepository,
                          ProductAliasRepository aliasRepository,
                          SkuRepository skuRepository,
                          CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.i18nRepository = i18nRepository;
        this.aliasRepository = aliasRepository;
        this.skuRepository = skuRepository;
        this.categoryRepository = categoryRepository;
    }

    public Optional<Product> findProductById(Long id) {
        return productRepository.findById(id);
    }

    public Optional<Product> findProductBySlug(String slug) {
        return productRepository.findBySlug(slug);
    }

    public List<Product> findAllProducts() {
        return productRepository.findAll();
    }

    public List<ProductI18n> findTranslations(Long productId) {
        return i18nRepository.findByProductId(productId);
    }

    public Optional<ProductI18n> findTranslation(Long productId, String locale) {
        return i18nRepository.findByProductIdAndLocale(productId, locale);
    }

    public List<ProductAlias> findAliases(Long productId) {
        return aliasRepository.findByProductId(productId);
    }

    public Optional<Sku> findSkuById(Long skuId) {
        return skuRepository.findById(skuId);
    }

    public List<Sku> findSkusByProductId(Long productId) {
        return skuRepository.findByProductId(productId);
    }

    public Optional<Category> findCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId);
    }

    public List<Category> findAllCategories() {
        return categoryRepository.findAll();
    }
}
