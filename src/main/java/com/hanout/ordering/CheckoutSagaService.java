package com.hanout.ordering;

import com.hanout.cart.CartService;
import com.hanout.catalog.CatalogService;
import com.hanout.catalog.Category;
import com.hanout.catalog.Product;
import com.hanout.catalog.Sku;
import com.hanout.common.Money;
import com.hanout.haggling.HagglingService;
import com.hanout.inventory.InventoryService;
import com.hanout.inventory.ReservationReason;
import com.hanout.payment.Payment;
import com.hanout.payment.PaymentService;
import com.hanout.risk.RiskDecision;
import com.hanout.risk.RiskScoringService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class CheckoutSagaService {

    private final CartService cartService;
    private final CatalogService catalogService;
    private final HagglingService hagglingService;
    private final InventoryService inventoryService;
    private final RiskScoringService riskScoringService;
    private final PaymentService paymentService;
    private final OrderRepository orderRepository;
    private final VendorOrderRepository vendorOrderRepository;
    private final OrderLineRepository orderLineRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${hanout.payout.platform-commission-rate:10.0}")
    private BigDecimal commissionRatePct;

    public CheckoutSagaService(CartService cartService,
                               CatalogService catalogService,
                               HagglingService hagglingService,
                               InventoryService inventoryService,
                               RiskScoringService riskScoringService,
                               PaymentService paymentService,
                               OrderRepository orderRepository,
                               VendorOrderRepository vendorOrderRepository,
                               OrderLineRepository orderLineRepository,
                               ApplicationEventPublisher eventPublisher) {
        this.cartService = cartService;
        this.catalogService = catalogService;
        this.hagglingService = hagglingService;
        this.inventoryService = inventoryService;
        this.riskScoringService = riskScoringService;
        this.paymentService = paymentService;
        this.orderRepository = orderRepository;
        this.vendorOrderRepository = vendorOrderRepository;
        this.orderLineRepository = orderLineRepository;
        this.eventPublisher = eventPublisher;
    }

    public record CheckoutCommand(
            Long buyerId,
            String phone,
            String shippingAddress,
            Long governorateId,
            String paymentGateway, // 'COD', 'KONNECT', 'MOCK'
            String idempotencyKey
    ) {}

    public record CheckoutResponse(
            Long orderId,
            OrderStatus status,
            Money totalAmount,
            List<Long> vendorOrderIds,
            String paymentGateway,
            String paymentRef
    ) {}

    @Transactional
    public CheckoutResponse execute(CheckoutCommand cmd) {
        // 1. Check Idempotency Key
        if (cmd.idempotencyKey() != null && !cmd.idempotencyKey().isBlank()) {
            Optional<Order> existing = orderRepository.findByIdempotencyKey(cmd.idempotencyKey());
            if (existing.isPresent()) {
                Order order = existing.get();
                List<VendorOrder> subOrders = vendorOrderRepository.findByOrderId(order.getId());
                return new CheckoutResponse(
                        order.getId(),
                        order.getStatus(),
                        order.getTotalAmount(),
                        subOrders.stream().map(VendorOrder::getId).toList(),
                        cmd.paymentGateway(),
                        "IDEMPOTENT_RETRY"
                );
            }
        }

        // 2. Validate Cart
        CartService.ResolvedCart cart = cartService.getCart(cmd.buyerId());
        if (cart.lines().isEmpty()) {
            throw new IllegalStateException("Cart is empty");
        }

        // 3. Risk Assessment
        RiskScoringService.RiskSignals signals = new RiskScoringService.RiskSignals(
                cmd.phone(),
                cart.total(),
                cmd.governorateId(),
                false, // can be derived from prior order history
                0,
                0
        );
        RiskScoringService.RiskEvaluationResult riskEval = riskScoringService.evaluate(signals);
        if (riskEval.decision() == RiskDecision.BLOCK) {
            throw new IllegalStateException("Order blocked due to high fraud risk assessment");
        }
        if (riskEval.decision() == RiskDecision.REQUIRE_DEPOSIT && "COD".equalsIgnoreCase(cmd.paymentGateway())) {
            throw new IllegalStateException("COD not permitted for this high risk profile. Online deposit required.");
        }

        // 4. Reserve stock for non-offer lines
        String checkoutRef = "CHECKOUT-" + UUID.randomUUID().toString().substring(0, 8);
        List<String> createdReservations = new ArrayList<>();

        for (var line : cart.lines()) {
            if (line.offerId() != null && line.isOfferLocked()) {
                // Already reserved under offer. Convert offer.
                hagglingService.convertOffer(line.offerId());
            } else {
                String resRef = checkoutRef + "-SKU-" + line.skuId();
                inventoryService.reserveStock(line.skuId(), line.qty(), ReservationReason.ORDER, resRef, LocalDateTime.now().plusHours(2));
                createdReservations.add(resRef);
            }
        }

        // 5. Create Parent Order
        Money codAmount = "COD".equalsIgnoreCase(cmd.paymentGateway()) ? cart.total() : Money.ZERO;
        Order order = new Order(cmd.buyerId(), cart.total(), codAmount, cmd.shippingAddress(), cmd.phone(), cmd.governorateId(), cmd.idempotencyKey());
        order = orderRepository.save(order);

        // 6. Split into Vendor Sub-Orders
        Map<Long, List<CartService.ResolvedCartLine>> vendorGrouped = new HashMap<>();
        for (var line : cart.lines()) {
            vendorGrouped.computeIfAbsent(line.vendorId(), k -> new ArrayList<>()).add(line);
        }

        List<Long> vendorOrderIds = new ArrayList<>();

        for (Map.Entry<Long, List<CartService.ResolvedCartLine>> entry : vendorGrouped.entrySet()) {
            Long vendorId = entry.getKey();
            List<CartService.ResolvedCartLine> lines = entry.getValue();

            Money vendorSubTotal = lines.stream()
                    .map(CartService.ResolvedCartLine::lineTotal)
                    .reduce(Money.ZERO, Money::plus);

            Money commission = vendorSubTotal.percent(commissionRatePct);

            VendorOrder subOrder = new VendorOrder(order.getId(), vendorId, vendorSubTotal, commission);
            subOrder = vendorOrderRepository.save(subOrder);
            vendorOrderIds.add(subOrder.getId());

            // 7. Create Snapshot Order Lines
            for (var line : lines) {
                Sku sku = catalogService.findSkuById(line.skuId()).orElseThrow();
                Product product = catalogService.findProductById(sku.getProductId()).orElseThrow();
                Category category = catalogService.findCategoryById(product.getCategoryId()).orElseThrow();

                OrderLine orderLine = new OrderLine(
                        subOrder.getId(),
                        line.skuId(),
                        line.offerId(),
                        line.qty(),
                        line.unitPrice(), // SNAPSHOT!
                        category.getVatRate(),
                        line.lineTotal()
                );
                orderLineRepository.save(orderLine);
            }
        }

        // 8. Process Payment
        Payment payment = paymentService.processPayment(order.getId(), cmd.paymentGateway(), cart.total(), cmd.idempotencyKey());

        // 9. Commit stock reservations
        for (String resRef : createdReservations) {
            inventoryService.commitReservation(resRef);
        }
        for (var line : cart.lines()) {
            if (line.offerId() != null) {
                inventoryService.commitReservation("OFFER-" + line.offerId());
            }
        }

        // 10. Record Risk Assessment
        riskScoringService.recordAssessment(order.getId(), signals);

        // 11. Clear Cart
        cartService.clearCart(cmd.buyerId());

        // 12. Publish OrderPlaced domain event
        OrderPlacedEvent event = new OrderPlacedEvent(
                order.getId(),
                order.getBuyerId(),
                order.getTotalAmount(),
                vendorOrderIds,
                cmd.paymentGateway(),
                order.getPhone(),
                order.getShippingAddress()
        );
        eventPublisher.publishEvent(event);

        return new CheckoutResponse(
                order.getId(),
                order.getStatus(),
                order.getTotalAmount(),
                vendorOrderIds,
                cmd.paymentGateway(),
                payment.getExternalRef()
        );
    }
}
