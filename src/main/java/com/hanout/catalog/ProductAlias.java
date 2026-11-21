package com.hanout.catalog;

import jakarta.persistence.*;

@Entity
@Table(name = "product_aliases")
public class ProductAlias {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private String alias;

    @Column(nullable = false, length = 32)
    private String kind; // 'arabizi', 'synonym', 'typo'

    public ProductAlias() {}

    public ProductAlias(Long id, Long productId, String alias, String kind) {
        this.id = id;
        this.productId = productId;
        this.alias = alias;
        this.kind = kind;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getAlias() { return alias; }
    public void setAlias(String alias) { this.alias = alias; }

    public String getKind() { return kind; }
    public void setKind(String kind) { this.kind = kind; }
}
