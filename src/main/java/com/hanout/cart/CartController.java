package com.hanout.cart;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    public record AddCartItemRequest(Long skuId, int qty, Long offerId) {}

    @PostMapping("/items")
    public ResponseEntity<CartItem> addItem(@RequestBody AddCartItemRequest req, Authentication auth) {
        Long buyerId = auth != null && auth.getPrincipal() instanceof Long ? (Long) auth.getPrincipal() : 1L;
        CartItem item = cartService.addToCart(buyerId, req.skuId(), req.qty(), req.offerId());
        return ResponseEntity.ok(item);
    }

    @GetMapping
    public ResponseEntity<CartService.ResolvedCart> getCart(Authentication auth) {
        Long buyerId = auth != null && auth.getPrincipal() instanceof Long ? (Long) auth.getPrincipal() : 1L;
        return ResponseEntity.ok(cartService.getCart(buyerId));
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<Void> removeItem(@PathVariable Long id) {
        cartService.removeItem(id);
        return ResponseEntity.noContent().build();
    }
}
