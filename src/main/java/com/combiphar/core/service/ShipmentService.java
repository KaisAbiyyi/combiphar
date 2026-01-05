package com.combiphar.core.service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.combiphar.core.model.Shipment;
import com.combiphar.core.model.Shipment.Status;
import com.combiphar.core.repository.OrderRepository;
import com.combiphar.core.repository.ShipmentRepository;

/**
 * Service untuk mengelola pengiriman.
 */
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final OrderRepository orderRepository;

    public ShipmentService() {
        this(new ShipmentRepository(), new OrderRepository());
    }

    public ShipmentService(ShipmentRepository shipmentRepo, OrderRepository orderRepo) {
        this.shipmentRepository = Objects.requireNonNull(shipmentRepo);
        this.orderRepository = Objects.requireNonNull(orderRepo);
    }

    public List<Shipment> getAllShipments() {
        return shipmentRepository.findAll();
    }

    public Optional<Shipment> getShipmentById(String id) {
        return id == null || id.isBlank() ? Optional.empty() : shipmentRepository.findById(id);
    }

    public Optional<Shipment> getShipmentByOrderId(String orderId) {
        return orderId == null || orderId.isBlank() ? Optional.empty() : shipmentRepository.findByOrderId(orderId);
    }

    public Shipment createShipment(String orderId, String addressId, String courierName) {
        Objects.requireNonNull(orderId, "Order ID wajib diisi");
        orderRepository.findById(orderId).orElseThrow(() -> new IllegalArgumentException("Order tidak ditemukan"));
        if (shipmentRepository.findByOrderId(orderId).isPresent()) {
            throw new IllegalStateException("Shipment sudah ada untuk order ini");
        }
        Shipment shipment = new Shipment(orderId, addressId, courierName);
        shipmentRepository.save(shipment);
        return shipment;
    }

    public void updateTrackingNumber(String shipmentId, String trackingNumber) {
        Objects.requireNonNull(shipmentId, "Shipment ID wajib diisi");
        if (trackingNumber == null || trackingNumber.isBlank()) {
            throw new IllegalArgumentException("Nomor resi tidak boleh kosong");
        }
        findOrThrow(shipmentId);
        shipmentRepository.updateTrackingNumber(shipmentId, trackingNumber.trim());
    }

    public void updateStatus(String shipmentId, Status status) {
        Objects.requireNonNull(shipmentId, "Shipment ID wajib diisi");
        Objects.requireNonNull(status, "Status wajib diisi");
        Shipment shipment = findOrThrow(shipmentId);
        if (status == Status.DELIVERED) {
            shipmentRepository.markAsDelivered(shipmentId);
        } else {
            shipmentRepository.updateStatus(shipmentId, status);
        }

        // Auto-update order status to COMPLETED when shipment is RECEIVED
        if (status == Status.RECEIVED) {
            orderRepository.updateOrderStatus(shipment.getOrderId(), "COMPLETED");
        }
    }

    public void markAsReceived(String shipmentId) {
        Objects.requireNonNull(shipmentId, "Shipment ID wajib diisi");
        Shipment shipment = findOrThrow(shipmentId);
        shipmentRepository.markAsReceived(shipmentId);

        // Auto-update order status to COMPLETED when shipment is received
        orderRepository.updateOrderStatus(shipment.getOrderId(), "COMPLETED");
    }

    private Shipment findOrThrow(String shipmentId) {
        return shipmentRepository.findById(shipmentId).orElseThrow(() -> new IllegalArgumentException("Shipment tidak ditemukan"));
    }
}
