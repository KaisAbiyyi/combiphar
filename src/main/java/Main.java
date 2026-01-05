
import java.util.Map;

import com.combiphar.core.controller.AddressController;
import com.combiphar.core.controller.AdminOrderController;
import com.combiphar.core.controller.AdminPaymentController;
import com.combiphar.core.controller.AdminShipmentController;
import com.combiphar.core.controller.AdminUserController;
import com.combiphar.core.controller.AuthController;
import com.combiphar.core.controller.CartController;
import com.combiphar.core.controller.CatalogController;
import com.combiphar.core.controller.CategoryController;
import com.combiphar.core.controller.CheckoutController;
import com.combiphar.core.controller.DashboardController;
import com.combiphar.core.controller.ItemController;
import com.combiphar.core.controller.PaymentController;
import com.combiphar.core.controller.PaymentUploadController;
import com.combiphar.core.controller.QualityCheckController;
import com.combiphar.core.controller.ReportController;
import com.combiphar.core.middleware.AuthMiddleware;
import com.combiphar.core.repository.AddressRepository;
import com.combiphar.core.repository.CartRepository;
import com.combiphar.core.repository.ItemRepository;
import com.combiphar.core.repository.UserRepository;
import com.combiphar.core.service.AuthService;
import com.combiphar.core.service.CartService;
import com.combiphar.core.service.FileUploadService;
import com.combiphar.core.service.OrderService;
import com.combiphar.core.service.PaymentService;
import com.combiphar.core.service.ShipmentService;
import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.loader.ClasspathLoader;

import io.javalin.Javalin;
import io.javalin.http.ForbiddenResponse;
import io.javalin.http.RedirectResponse;
import io.javalin.rendering.template.JavalinPebble;

/**
 * Application entry point for Combiphar Used Goods system. Configures Javalin
 * server with Pebble templating and static file serving.
 */
public class Main {

    private static final int PORT = 7070;

    public static void main(String[] args) {
        // Initialize repositories
        UserRepository userRepository = new UserRepository();
        ItemRepository itemRepository = new ItemRepository();
        CartRepository cartRepository = new CartRepository();
        AddressRepository addressRepository = new AddressRepository();

        // Initialize services
        AuthService authService = new AuthService(userRepository);
        CartService cartService = new CartService(itemRepository);
        OrderService orderService = new OrderService();
        FileUploadService fileUploadService = new FileUploadService();
        PaymentService paymentService = new PaymentService(fileUploadService);

        // Initialize controllers - Phase 1: Auth
        AuthController authController = new AuthController(authService, addressRepository);

        // Initialize Phase 2 controllers
        CategoryController categoryController = new CategoryController();
        ItemController itemController = new ItemController();
        QualityCheckController qcController = new QualityCheckController();

        // Initialize Phase 3 controllers (Customer Catalog)
        CatalogController catalogController = new CatalogController();

        // Initialize Phase 4 controllers (Transaction Flow)
        CartController cartController = new CartController(cartService, cartRepository);

        CheckoutController checkoutController = new CheckoutController(orderService, addressRepository);

        // Initialize Phase 5 controllers (Payment & Shipment)
        PaymentController paymentController = new PaymentController(paymentService, orderService);
        PaymentUploadController paymentUploadController = new PaymentUploadController(fileUploadService, orderService,
                cartRepository);
        ShipmentService shipmentService = new ShipmentService();
        AdminShipmentController adminShipmentController = new AdminShipmentController(shipmentService);
        AdminPaymentController adminPaymentController = new AdminPaymentController();
        AdminOrderController adminOrderController = new AdminOrderController();
        AdminUserController adminUserController = new AdminUserController(userRepository);
        ReportController reportController = new ReportController();
        DashboardController dashboardController = new DashboardController();

        // Initialize Address controller
        AddressController addressController = new AddressController(addressRepository);

        PebbleEngine engine = createPebbleEngine();
        Javalin app = createApp(engine);

        registerRoutes(app, authController, categoryController,
                itemController, qcController, catalogController, cartController,
                checkoutController, paymentController, paymentUploadController,
                adminShipmentController, adminPaymentController, adminOrderController, adminUserController,
                shipmentService, cartRepository, orderService, addressController, reportController, 
                dashboardController);

        // Run DB migrations (best-effort). This will create carts/cart_items if
        // missing.
        com.combiphar.core.migration.MigrationRunner.runMigrations();

        app.start(PORT);
    }

    /**
     * Creates configured Pebble template engine.
     */
    private static PebbleEngine createPebbleEngine() {
        ClasspathLoader loader = new ClasspathLoader();
        loader.setPrefix("templates");
        loader.setSuffix(".pebble");

        return new PebbleEngine.Builder()
                .loader(loader)
                .build();
    }

    /**
     * Creates Javalin app with static files and template renderer.
     */
    private static Javalin createApp(PebbleEngine engine) {
        Javalin app = Javalin.create(config -> {
            config.fileRenderer(new JavalinPebble(engine));
            config.staticFiles.add("/static");
            config.staticFiles.add(staticConfig -> {
                staticConfig.hostedPath = "/uploads";
                staticConfig.directory = "uploads";
                staticConfig.location = io.javalin.http.staticfiles.Location.EXTERNAL;
            });

            // Session Configuration
            config.jetty.modifyServletContextHandler(handler -> {
                handler.getSessionHandler().setSessionCookie("COMBIPHAR_SESSION");
                handler.getSessionHandler().setMaxInactiveInterval(3600); // 1 hour
            });

            // Multipart Configuration - untuk mengurangi warning pada Windows
            config.jetty.multipartConfig.cacheDirectory("uploads/temp");
            config.http.maxRequestSize = 5_242_880L; // 5MB max file size
        });

        // Global Exception Handlers for cleaner redirects
        app.exception(RedirectResponse.class, (e, ctx) -> {
            // RedirectResponse message is the URL
            ctx.redirect(e.getMessage());
        });

        app.exception(ForbiddenResponse.class, (e, ctx) -> {
            // Redirect to home on forbidden access
            ctx.redirect("/");
        });

        return app;
    }

    /**
     * Builds template model with common attributes including current user.
     * Defensive programming: uses HashMap to handle null values gracefully.
     */
    private static Map<String, Object> buildModel(String title, String activePage, Object currentUser) {
        Map<String, Object> model = new java.util.HashMap<>();
        model.put("title", title != null ? title : "Combiphar Used Goods");
        model.put("activePage", activePage != null ? activePage : "");
        model.put("currentUser", currentUser); // Can be null, that's fine for templates
        return model;
    }

    /**
     * Registers all application routes.
     */
    private static void registerRoutes(Javalin app, AuthController authController,
            CategoryController categoryController,
            ItemController itemController,
            QualityCheckController qcController,
            CatalogController catalogController,
            CartController cartController,
            CheckoutController checkoutController,
            PaymentController paymentController,
            PaymentUploadController paymentUploadController,
            AdminShipmentController adminShipmentController,
            AdminPaymentController adminPaymentController,
            AdminOrderController adminOrderController,
            AdminUserController adminUserController,
            ShipmentService shipmentService,
            CartRepository cartRepository,
            OrderService orderService,
            AddressController addressController,
            ReportController reportController,
            DashboardController dashboardController) {
        // ====== PHASE 3: Customer Catalog Routes ======
        // Home / Catalog page - delegated to CatalogController
        app.get("/", catalogController::showCatalogPage);
        app.get("/catalog", catalogController::showCatalogPage);

        // Product detail page - delegated to CatalogController
        app.get("/product/{id}", catalogController::showProductDetail);

        // Catalog search API endpoint
        app.get("/api/catalog/search", catalogController::searchProducts);

        // Auth Routes
        app.post("/logout", authController::handleLogout);
        app.get("/login", authController::showLogin);
        app.post("/login", authController::handleLogin);
        // After successful login, sync session cart with persisted cart (non-blocking)
        app.after("/login", ctx -> {
            com.combiphar.core.model.User user = ctx.sessionAttribute("currentUser");
            if (user != null) {
                try {
                    com.combiphar.core.model.Cart cart = ctx.sessionAttribute("cart");
                    if (cart != null && !cart.isEmpty()) {
                        cartRepository.saveCartForUser(user.getId(), cart);
                    } else {
                        cartRepository.findByUserId(user.getId()).ifPresent(loaded -> {
                            ctx.sessionAttribute("cart", loaded);
                        });
                    }
                } catch (Exception e) {
                    System.err.println("[Main] failed to load persisted cart after login: " + e.getMessage());
                }
            }
        });
        app.get("/register", authController::showRegister);
        app.post("/register", authController::handleRegister);
        app.get("/logout", authController::handleLogout);

        // Protected Customer Routes
        app.before("/profile", AuthMiddleware.authenticated);
        app.before("/addresses", AuthMiddleware.authenticated);
        app.before("/addresses/*", AuthMiddleware.authenticated);
        app.before("/cart", AuthMiddleware.authenticated);
        app.before("/checkout", AuthMiddleware.authenticated);
        app.before("/payment", AuthMiddleware.authenticated);
        app.before("/payment/*", AuthMiddleware.authenticated);
        app.before("/history", AuthMiddleware.authenticated);
        app.before("/order/*", AuthMiddleware.authenticated);

        app.get("/profile", authController::showProfile);

        // ====== Address Management Routes ======
        app.get("/addresses", addressController::showAddressList);
        app.get("/addresses/add", addressController::showAddressForm);
        app.post("/addresses/add", addressController::handleAddAddress);
        app.post("/addresses/set-primary", addressController::setPrimaryAddress);
        app.post("/addresses/delete", addressController::deleteAddress);

        // ====== PHASE 4: Cart & Checkout Routes ======
        // Cart management
        app.get("/cart", cartController::showCart);
        // API cart endpoints require authentication
        app.before("/api/cart/*", AuthMiddleware.authenticatedApi);
        app.post("/api/cart/add", cartController::addToCart);
        app.post("/api/cart/update", cartController::updateCartItem);
        app.post("/api/cart/remove", cartController::removeFromCart);
        app.post("/api/cart/clear", cartController::clearCart);
        app.put("/api/cart/update", cartController::updateCartItem);
        app.delete("/api/cart/remove", cartController::removeFromCart);
        // address-settings save endpoint removed

        // Checkout flow
        app.get("/checkout", checkoutController::showCheckout);
        app.post("/api/checkout/calculate", checkoutController::calculateOrder);
        app.post("/checkout/validate-address", checkoutController::validateAddress);

        // Admin Auth
        app.get("/admin/login", authController::showAdminLogin);
        app.post("/admin/login", authController::handleAdminLogin);

        // Protected Admin Routes
        app.before("/admin/*", AuthMiddleware.adminOnly);
        app.before("/admin", AuthMiddleware.adminOnly);

        // ====== PHASE 5: Payment Routes ======
        // Payment transfer page - shows bank account info
        app.get("/payment", paymentController::showPaymentPage);

        // Payment upload page - upload bukti pembayaran
        app.get("/payment/upload", paymentController::showUploadPage);

        // Payment upload API
        app.post("/api/payment/upload", paymentUploadController::uploadPaymentProof);

        // Order complete API - user marks order as received
        app.post("/api/order/complete", ctx -> {
            String orderId = ctx.formParam("orderId");
            if (orderId == null || orderId.isBlank()) {
                ctx.status(400).json(Map.of("success", false, "message", "Order ID required"));
                return;
            }
            try {
                var shipment = shipmentService.getShipmentByOrderId(orderId);
                if (shipment.isPresent()) {
                    shipmentService.markAsReceived(shipment.get().getId());
                }
                ctx.redirect("/order/" + orderId);
            } catch (Exception e) {
                ctx.status(500).json(Map.of("success", false, "message", e.getMessage()));
            }
        });

        // Legacy payment route (redirect to new payment flow)
        app.get("/checkout/payment", ctx -> ctx.redirect("/payment"));

        // History page
        app.get("/history", ctx -> {
            var user = ctx.sessionAttribute("currentUser");
            Map<String, Object> model = buildModel(
                    "Riwayat Transaksi - Combiphar Used Goods",
                    "history",
                    user);

            if (user != null) {
                String userId = ((com.combiphar.core.model.User) user).getId();
                java.util.List<com.combiphar.core.model.OrderHistory> orderHistory = orderService
                        .getOrderHistory(userId, shipmentService);
                model.put("orderHistory", orderHistory);
            }

            ctx.render("customer/history", model);
        });

        // Order tracking page
        app.get("/order/{id}", ctx -> {
            String orderId = ctx.pathParam("id");
            Map<String, Object> model = buildModel(
                    "Detail Pesanan - Combiphar Used Goods",
                    "history",
                    ctx.sessionAttribute("currentUser"));

            // Add user display name and initials for avatar
            Object currUserObj = ctx.sessionAttribute("currentUser");
            String userName = null;
            String userInitials = "U";
            if (currUserObj != null && currUserObj instanceof com.combiphar.core.model.User) {
                com.combiphar.core.model.User cu = (com.combiphar.core.model.User) currUserObj;
                userName = cu.getName();
                if (userName != null && !userName.isBlank()) {
                    String[] parts = userName.trim().split("\\s+");
                    if (parts.length == 1) {
                        userInitials = parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
                    } else {
                        String first = parts[0].substring(0, 1);
                        String last = parts[parts.length - 1].substring(0, 1);
                        userInitials = (first + last).toUpperCase();
                    }
                }
            }
            model.put("userName", userName);
            model.put("userInitials", userInitials);

            // Get order details
            com.combiphar.core.repository.OrderRepository orderRepo = new com.combiphar.core.repository.OrderRepository();
            orderRepo.findById(orderId).ifPresentOrElse(order -> {
                model.put("order", order);

                // Get payment info
                com.combiphar.core.repository.PaymentRepository paymentRepo = new com.combiphar.core.repository.PaymentRepository();
                paymentRepo.findByOrderId(orderId).ifPresent(payment -> {
                    model.put("payment", payment);
                });

                // Get order items
                com.combiphar.core.repository.OrderItemRepository itemRepo = new com.combiphar.core.repository.OrderItemRepository();
                model.put("items", itemRepo.findByOrderId(orderId));

                // Get shipment info for tracking
                shipmentService.getShipmentByOrderId(orderId).ifPresent(shipment -> {
                    model.put("shipment", shipment);
                });
            }, () -> {
                model.put("error", "Order tidak ditemukan");
            });

            ctx.render("customer/order-tracking", model);
        });

        // Admin dashboard page
        app.get("/admin", dashboardController::showDashboard);
        app.get("/admin/dashboard", dashboardController::showDashboard);

        // Admin category page (English route)
        app.get("/admin/category", categoryController::showCategoryPage);

        // Admin product page (English route)
        app.get("/admin/products", itemController::showProductPage);

        // ====== PHASE 2: Category Management API ======
        app.get("/api/admin/categories", categoryController::getAllCategories);
        app.get("/api/admin/categories/export-csv", categoryController::exportCategoriesCsv);
        app.post("/api/admin/categories/import-csv", categoryController::importCategoriesCsv);
        app.get("/api/admin/categories/{id}", categoryController::getCategoryById);
        app.get("/api/admin/categories/{id}/item-count", categoryController::getCategoryItemCount);
        app.post("/api/admin/categories", categoryController::createCategory);
        app.put("/api/admin/categories/{id}", categoryController::updateCategory);
        app.delete("/api/admin/categories/{id}", categoryController::deleteCategory);

        // ====== PHASE 2: Item/Product Management API ======
        app.get("/api/admin/items", itemController::getAllItems);
        app.get("/api/admin/items/export-csv", itemController::exportItemsCsv);
        app.post("/api/admin/items/import-csv", itemController::importItemsCsv);
        app.get("/api/admin/items/{id}", itemController::getItemById);
        app.post("/api/admin/items", itemController::createItem);
        app.put("/api/admin/items/{id}", itemController::updateItem);
        app.patch("/api/admin/items/{id}/status", itemController::updateItemStatus);
        app.patch("/api/admin/items/{id}/stock", itemController::updateItemStock);
        app.delete("/api/admin/items/{id}", itemController::deleteItem);

        // New endpoints for updated UI features
        app.post("/api/admin/items/{id}/update-stock", itemController::quickUpdateStock);
        app.post("/api/admin/items/{id}/cancel-qc", itemController::cancelQC);

        // ====== PHASE 2: Quality Control API ======
        app.get("/admin/qc", qcController::showQCDashboard);
        app.get("/api/admin/qc/pipeline", qcController::getQCPipeline);
        app.get("/api/admin/qc/needs-repair", qcController::getItemsNeedingRepair);
        app.get("/api/admin/qc/eligible", qcController::getEligibleItems);
        app.get("/api/admin/qc/statistics", qcController::getQCStatistics);
        app.get("/api/admin/qc/summary", qcController::getDailyQCSummary);
        app.post("/api/admin/qc/check", qcController::performQualityCheck);
        app.post("/api/admin/qc/batch-approve", qcController::batchApprove);
        app.post("/api/admin/qc/batch-reject", qcController::batchReject);

        // ====== Customer API - Published Items ======
        app.get("/api/items/published", itemController::getPublishedItems);

        // Admin order page - delegated to controller
        app.get("/admin/orders", adminOrderController::showOrders);
        app.get("/api/admin/orders/{id}/items", adminOrderController::getOrderItems);

        // Admin payment page (English route) - delegated to controller
        app.get("/admin/payments", adminPaymentController::showPaymentPage);
        app.get("/admin/payments/proof/{id}", adminPaymentController::showPaymentProof);
        app.post("/api/admin/payments/{id}/verify", adminPaymentController::verifyPayment);

        // Admin shipment page (English route) - delegated to controller
        app.get("/admin/shipment", adminShipmentController::showShipmentPage);

        // Admin shipment API routes
        app.post("/api/admin/shipment/{id}/tracking", adminShipmentController::updateTrackingNumber);
        app.post("/api/admin/shipment/{id}/status", adminShipmentController::updateStatus);
        app.post("/api/admin/shipment/create", adminShipmentController::createShipment);

        // Admin user page (English route) - delegated to controller
        app.get("/admin/users", adminUserController::showUsers);
        app.post("/admin/users/status", adminUserController::updateStatus);

        // Admin reports page (English route) - delegated to controller
        app.get("/admin/reports", reportController::showReports);
    }
}
