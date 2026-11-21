package com.hanout.ordering;

import com.hanout.delivery.Courier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VendorOrderService {

    private final VendorOrderRepository vendorOrderRepository;
    private final OrderRepository orderRepository;
    private final Courier courier;
    private final ApplicationEventPublisher eventPublisher;

    public VendorOrderService(VendorOrderRepository vendorOrderRepository,
                              OrderRepository orderRepository,
                              Courier courier,
                              ApplicationEventPublisher eventPublisher) {
        this.vendorOrderRepository = vendorOrderRepository;
        this.orderRepository = orderRepository;
        this.courier = courier;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public VendorOrder acceptVendorOrder(Long vendorOrderId, Long vendorId) {
        VendorOrder vo = getVendorOrder(vendorOrderId, vendorId);
        vo.setStatus(VendorOrderStatus.VENDOR_ACCEPTED);
        return vendorOrderRepository.save(vo);
    }

    @Transactional
    public VendorOrder shipVendorOrder(Long vendorOrderId, Long vendorId) {
        VendorOrder vo = getVendorOrder(vendorOrderId, vendorId);
        Order order = orderRepository.findById(vo.getOrderId()).orElseThrow();

        // Book courier
        Courier.ShipmentResult shipment = courier.book(new Courier.ShipmentRequest(
                vo.getId(),
                "Client Order #" + order.getId(),
                order.getPhone(),
                order.getShippingAddress(),
                vo.getSubTotal()
        ));

        vo.setStatus(VendorOrderStatus.SHIPPED);
        vo.setCourierId(shipment.courierCode());
        vo.setTrackingNo(shipment.trackingNumber());
        vo = vendorOrderRepository.save(vo);

        // Update parent order status
        updateParentOrderStatus(vo.getOrderId());

        eventPublisher.publishEvent(new VendorOrderShippedEvent(vo.getId(), vo.getVendorId(), vo.getTrackingNo()));
        return vo;
    }

    @Transactional
    public VendorOrder deliverVendorOrder(Long vendorOrderId) {
        VendorOrder vo = vendorOrderRepository.findById(vendorOrderId)
                .orElseThrow(() -> new IllegalArgumentException("Vendor order not found: " + vendorOrderId));
        vo.setStatus(VendorOrderStatus.DELIVERED);
        vo = vendorOrderRepository.save(vo);

        updateParentOrderStatus(vo.getOrderId());

        eventPublisher.publishEvent(new VendorOrderDeliveredEvent(vo.getId(), vo.getVendorId(), vo.getSubTotal(), vo.getCommissionAmount()));
        return vo;
    }

    private void updateParentOrderStatus(Long parentOrderId) {
        var subOrders = vendorOrderRepository.findByOrderId(parentOrderId);
        boolean allDelivered = subOrders.stream().allMatch(s -> s.getStatus() == VendorOrderStatus.DELIVERED);
        boolean anyShipped = subOrders.stream().anyMatch(s -> s.getStatus() == VendorOrderStatus.SHIPPED || s.getStatus() == VendorOrderStatus.DELIVERED);

        Order parent = orderRepository.findById(parentOrderId).orElseThrow();
        if (allDelivered) {
            parent.setStatus(OrderStatus.DELIVERED);
        } else if (anyShipped) {
            parent.setStatus(OrderStatus.PARTIALLY_SHIPPED);
        }
        orderRepository.save(parent);
    }

    private VendorOrder getVendorOrder(Long vendorOrderId, Long vendorId) {
        VendorOrder vo = vendorOrderRepository.findById(vendorOrderId)
                .orElseThrow(() -> new IllegalArgumentException("Vendor order not found: " + vendorOrderId));
        if (!vo.getVendorId().equals(vendorId)) {
            throw new IllegalArgumentException("Order does not belong to vendor: " + vendorId);
        }
        return vo;
    }
}
