package com.hanout.catalog;

import jakarta.persistence.*;

@Entity
@Table(name = "product_i18n")
public class ProductI18n {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false, length = 10)
    private String locale; // 'ar', 'fr', 'en'

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    public ProductI18n() {}

    public ProductI18n(Long id, Long productId, String locale, String name, String description) {
        this.id = id;
        this.productId = productId;
        this.locale = locale;
        this.name = name;
        this.description = description;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getLocale() { return locale; }
    public void setLocale(String locale) { this.locale = locale; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
