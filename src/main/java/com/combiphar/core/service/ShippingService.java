package com.combiphar.core.service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.combiphar.core.model.Shipment;
import com.combiphar.core.model.Shipment.Status;
import com.combiphar.core.repository.OrderRepository;
import com.combiphar.core.repository.ShipmentRepository;

/**
 * Service untuk mengelola pengiriman (shipping). Single Responsibility:
 * business logic untuk shipment.
 */
public class ShippingService {

    private final ShipmentRepository shipmentRepository;
    private final OrderRepository orderRepository;

    public ShippingService() {
        this.shipmentRepository = new ShipmentRepository();
        this.orderRepository = new OrderRepository();
    }

    /**
     * Constructor dengan dependency injection untuk testing.
     */
    public ShippingService(ShipmentRepository shipmentRepository, OrderRepository orderRepository) {
        this.shipmentRepository = Objects.requireNonNull(shipmentRepository);
        this.orderRepository = Objects.requireNonNull(orderRepository);
    }

    /**
     * Mendapatkan semua shipment.
     *
     * @return list semua shipment
     */
    public List<Shipment> getAllShipments() {
        return shipmentRepository.findAll();
    }

    /**
     * Mendapatkan shipment berdasarkan ID.
     *
     * @param shipmentId ID shipment
     * @return Optional berisi shipment
     */
    public Optional<Shipment> getShipmentById(String shipmentId) {
        if (shipmentId == null || shipmentId.isBlank()) {
            return Optional.empty();
        }
        return shipmentRepository.findById(shipmentId);
    }

    /**
     * Mendapatkan shipment berdasarkan order ID.
     *
     * @param orderId ID order
     * @return Optional berisi shipment
     */
    public Optional<Shipment> getShipmentByOrderId(String orderId) {
        if (orderId == null || orderId.isBlank()) {
            return Optional.empty();
        }
        return shipmentRepository.findByOrderId(orderId);
    }

    /**
     * Membuat shipment baru untuk order.
     *
     * @param orderId ID order
     * @param courierName nama kurir
     * @return shipment yang dibuat
     */
    public Shipment createShipment(String orderId, String courierName) {
        Objects.requireNonNull(orderId, "Order ID wajib diisi");

        // Cek apakah order exists
        orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order tidak ditemukan"));

        // Cek apakah sudah ada shipment untuk order ini
        if (shipmentRepository.findByOrderId(orderId).isPresent()) {
            throw new IllegalStateException("Shipment sudah ada untuk order ini");
        }

        Shipment shipment = new Shipment(orderId, courierName);
        shipmentRepository.save(shipment);
        return shipment;
    }

    /**
     * Update nomor resi pengiriman. Validasi: nomor resi tidak boleh kosong.
     *
     * @param shipmentId ID shipment
     * @param trackingNumber nomor resi
     */
    public void updateTrackingNumber(String shipmentId, String trackingNumber) {
        Objects.requireNonNull(shipmentId, "Shipment ID wajib diisi");

        if (trackingNumber == null || trackingNumber.isBlank()) {
            throw new IllegalArgumentException("Nomor resi tidak boleh kosong");
        }

        // Validasi shipment exists
        shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new IllegalArgumentException("Shipment tidak ditemukan"));

        shipmentRepository.updateTrackingNumber(shipmentId, trackingNumber.trim());
    }

    /**
     * Update status shipment.
     *
     * @param shipmentId ID shipment
     * @param status status baru
     */
    public void updateStatus(String shipmentId, Status status) {
        Objects.requireNonNull(shipmentId, "Shipment ID wajib diisi");
        Objects.requireNonNull(status, "Status wajib diisi");

        // Validasi shipment exists
        shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new IllegalArgumentException("Shipment tidak ditemukan"));

        if (status == Status.DELIVERED) {
            shipmentRepository.markAsDelivered(shipmentId);
        } else {
            shipmentRepository.updateStatus(shipmentId, status);
        }
    }

    /**
     * Tandai shipment sebagai delivered.
     *
     * @param shipmentId ID shipment
     */
    public void markAsDelivered(String shipmentId) {
        updateStatus(shipmentId, Status.DELIVERED);
    }

    /**
     * Tandai shipment sebagai received (diterima user).
     *
     * @param shipmentId ID shipment
     */
    public void markAsReceived(String shipmentId) {
        Objects.requireNonNull(shipmentId, "Shipment ID wajib diisi");
        shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new IllegalArgumentException("Shipment tidak ditemukan"));
        shipmentRepository.markAsReceived(shipmentId);
    }
}
