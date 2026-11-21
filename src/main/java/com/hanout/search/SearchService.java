package com.hanout.search;

import com.hanout.catalog.*;
import com.hanout.common.Money;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional(readOnly = true)
public class SearchService {

    private final CatalogService catalogService;
    private final SearchNormalizer normalizer;

    public SearchService(CatalogService catalogService, SearchNormalizer normalizer) {
        this.catalogService = catalogService;
        this.normalizer = normalizer;
    }

    public record SearchResultItem(
            Long productId,
            String slug,
            Long vendorId,
            String name,
            String description,
            String locale,
            Long skuId,
            String skuCode,
            Money price,
            int stockOnHand,
            int availableStock,
            double matchScore
    ) {}

    public List<SearchResultItem> search(String query, String preferredLocale) {
        if (preferredLocale == null || preferredLocale.isBlank()) {
            preferredLocale = "ar";
        }
        String locale = preferredLocale.toLowerCase();

        List<Product> products = catalogService.findAllProducts();
        if (query == null || query.isBlank()) {
            return products.stream()
                    .map(p -> toItem(p, locale, 1.0))
                    .filter(Objects::nonNull)
                    .toList();
        }

        String rawLower = query.toLowerCase().trim();
        String arabiziNormalized = normalizer.normalizeArabizi(rawLower);
        String fullyNormalized = normalizer.normalizeAll(rawLower);

        List<SearchResultItem> results = new ArrayList<>();

        for (Product product : products) {
            List<ProductI18n> translations = catalogService.findTranslations(product.getId());
            List<ProductAlias> aliases = catalogService.findAliases(product.getId());

            double score = 0.0;

            // 1. Check aliases exact and prefix match
            for (ProductAlias alias : aliases) {
                String aliasNorm = normalizer.normalizeAll(alias.getAlias());
                if (aliasNorm.equalsIgnoreCase(fullyNormalized) || alias.getAlias().equalsIgnoreCase(rawLower)) {
                    score = Math.max(score, 100.0);
                } else if (aliasNorm.contains(fullyNormalized) || fullyNormalized.contains(aliasNorm)) {
                    score = Math.max(score, 70.0);
                }
            }

            // 2. Check localized names
            for (ProductI18n t : translations) {
                String nameNorm = normalizer.normalizeAll(t.getName());
                String descNorm = normalizer.normalizeAll(t.getDescription() == null ? "" : t.getDescription());

                if (nameNorm.contains(fullyNormalized) || nameNorm.contains(arabiziNormalized)) {
                    score = Math.max(score, t.getLocale().equalsIgnoreCase(locale) ? 90.0 : 80.0);
                } else if (descNorm.contains(fullyNormalized)) {
                    score = Math.max(score, 50.0);
                }
            }

            if (score > 0.0) {
                SearchResultItem item = toItem(product, locale, score);
                if (item != null) {
                    results.add(item);
                }
            }
        }

        results.sort(Comparator.comparingDouble(SearchResultItem::matchScore).reversed());
        return results;
    }

    private SearchResultItem toItem(Product product, String locale, double score) {
        List<ProductI18n> translations = catalogService.findTranslations(product.getId());
        ProductI18n selected = translations.stream()
                .filter(t -> t.getLocale().equalsIgnoreCase(locale))
                .findFirst()
                .orElseGet(() -> translations.isEmpty() ? null : translations.get(0));

        if (selected == null) return null;

        List<Sku> skus = catalogService.findSkusByProductId(product.getId());
        Sku primarySku = skus.isEmpty() ? null : skus.get(0);

        return new SearchResultItem(
                product.getId(),
                product.getSlug(),
                product.getVendorId(),
                selected.getName(),
                selected.getDescription(),
                selected.getLocale(),
                primarySku != null ? primarySku.getId() : null,
                primarySku != null ? primarySku.getSkuCode() : null,
                primarySku != null ? primarySku.getPriceMillimes() : Money.ZERO,
                primarySku != null ? primarySku.getStockOnHand() : 0,
                primarySku != null ? primarySku.getAvailableStock() : 0,
                score
        );
    }
}
