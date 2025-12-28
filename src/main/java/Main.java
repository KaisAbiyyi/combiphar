
import java.util.Map;

import com.combiphar.core.controller.AuthController;
import com.combiphar.core.controller.CategoryController;
import com.combiphar.core.controller.ItemController;
import com.combiphar.core.controller.QualityCheckController;
import com.combiphar.core.middleware.AuthMiddleware;
import com.combiphar.core.repository.UserRepository;
import com.combiphar.core.service.AuthService;
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
        // Initialize dependencies
        UserRepository userRepository = new UserRepository();
        AuthService authService = new AuthService(userRepository);
        AuthController authController = new AuthController(authService);

        // Initialize Phase 2 controllers
        CategoryController categoryController = new CategoryController();
        ItemController itemController = new ItemController();
        QualityCheckController qcController = new QualityCheckController();

        PebbleEngine engine = createPebbleEngine();
        Javalin app = createApp(engine);

        registerRoutes(app, authController, categoryController, itemController, qcController);

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

            // Session Configuration
            config.jetty.modifyServletContextHandler(handler -> {
                handler.getSessionHandler().setSessionCookie("COMBIPHAR_SESSION");
                handler.getSessionHandler().setMaxInactiveInterval(3600); // 1 hour
            });
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
            QualityCheckController qcController) {
        // Home / Catalog page
        app.get("/", ctx -> {
            Map<String, Object> model = buildModel(
                    "Katalog - Combiphar Used Goods",
                    "catalog",
                    ctx.sessionAttribute("currentUser"));
            ctx.render("customer/catalog", model);
        });

        app.get("/catalog", ctx -> {
            Map<String, Object> model = buildModel(
                    "Katalog - Combiphar Used Goods",
                    "catalog",
                    ctx.sessionAttribute("currentUser"));
            ctx.render("customer/catalog", model);
        });

        // Auth Routes
        app.post("/logout", authController::handleLogout);
        app.get("/login", authController::showLogin);
        app.post("/login", authController::handleLogin);
        app.get("/register", authController::showRegister);
        app.post("/register", authController::handleRegister);
        app.get("/logout", authController::handleLogout);

        // Protected Customer Routes
        app.before("/profile", AuthMiddleware.authenticated);
        app.before("/cart", AuthMiddleware.authenticated);
        app.before("/checkout", AuthMiddleware.authenticated);
        app.before("/payment", AuthMiddleware.authenticated);
        app.before("/history", AuthMiddleware.authenticated);
        app.before("/order/*", AuthMiddleware.authenticated);

        app.get("/profile", authController::showProfile);

        // Admin Auth
        app.get("/admin/login", authController::showAdminLogin);
        app.post("/admin/login", authController::handleAdminLogin);

        // Protected Admin Routes
        app.before("/admin/*", AuthMiddleware.adminOnly);
        app.before("/admin", AuthMiddleware.adminOnly);

        // Product detail page
        app.get("/product/{id}", ctx -> {
            String productId = ctx.pathParam("id");
            Map<String, Object> model = buildModel(
                    "Set Meja Rapat Glasium - Combiphar Used Goods",
                    "product",
                    ctx.sessionAttribute("currentUser"));
            model.put("productId", productId);
            ctx.render("customer/product-detail", model);
        });

        // Cart page
        app.get("/cart", ctx -> {
            Map<String, Object> model = buildModel(
                    "Keranjang Saya - Combiphar Used Goods",
                    "cart",
                    ctx.sessionAttribute("currentUser"));
            ctx.render("customer/cart", model);
        });

        // Checkout page
        app.get("/checkout", ctx -> {
            Map<String, Object> model = buildModel(
                    "Checkout Pesanan - Combiphar Used Goods",
                    "checkout",
                    ctx.sessionAttribute("currentUser"));
            ctx.render("customer/checkout", model);
        });

        // Payment page
        app.get("/payment", ctx -> {
            Map<String, Object> model = buildModel(
                    "Pembayaran Pesanan - Combiphar Used Goods",
                    "payment",
                    ctx.sessionAttribute("currentUser"));
            ctx.render("customer/payment", model);
        });

        app.get("/checkout/payment", ctx -> {
            Map<String, Object> model = buildModel(
                    "Pembayaran Pesanan - Combiphar Used Goods",
                    "payment",
                    ctx.sessionAttribute("currentUser"));
            ctx.render("customer/payment", model);
        });

        // History page
        app.get("/history", ctx -> {
            Map<String, Object> model = buildModel(
                    "Riwayat Transaksi - Combiphar Used Goods",
                    "history",
                    ctx.sessionAttribute("currentUser"));
            ctx.render("customer/history", model);
        });

        // Order tracking page
        app.get("/order/{id}", ctx -> {
            String orderId = ctx.pathParam("id");
            Map<String, Object> model = buildModel(
                    "Detail Pesanan - Combiphar Used Goods",
                    "history",
                    ctx.sessionAttribute("currentUser"));
            model.put("orderId", orderId);
            ctx.render("customer/order-tracking", model);
        });

        // Admin dashboard page
        app.get("/admin", ctx -> {
            Map<String, Object> model = Map.of(
                    "title", "Dashboard Admin",
                    "pageTitle", "Dashboard Admin",
                    "userName", "Nashya Putri",
                    "userRole", "Admin Warehouse Jakarta",
                    "activePage", "dashboard");
            ctx.render("admin/dashboard", model);
        });

        app.get("/admin/dashboard", ctx -> {
            Map<String, Object> model = Map.of(
                    "title", "Dashboard Admin",
                    "pageTitle", "Dashboard Admin",
                    "userName", "Nashya Putri",
                    "userRole", "Admin Warehouse Jakarta",
                    "activePage", "dashboard");
            ctx.render("admin/dashboard", model);
        });

        // Admin category page (English route)
        app.get("/admin/category", categoryController::showCategoryPage);

        // Admin product page (English route)
        app.get("/admin/products", itemController::showProductPage);

        // ====== PHASE 2: Category Management API ======
        app.get("/api/admin/categories", categoryController::getAllCategories);
        app.get("/api/admin/categories/{id}", categoryController::getCategoryById);
        app.post("/api/admin/categories", categoryController::createCategory);
        app.put("/api/admin/categories/{id}", categoryController::updateCategory);
        app.delete("/api/admin/categories/{id}", categoryController::deleteCategory);

        // ====== PHASE 2: Item/Product Management API ======
        app.get("/api/admin/items", itemController::getAllItems);
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

        // Admin order page (English route)
        app.get("/admin/orders", ctx -> {
            Map<String, Object> model = Map.of(
                    "title", "Monitoring Pesanan",
                    "pageTitle", "Monitoring Pesanan",
                    "userName", "Nashya Putri",
                    "userRole", "Admin Warehouse Jakarta",
                    "activePage", "orders");
            ctx.render("admin/order", model);
        });

        // Admin transaction page (English route)
        app.get("/admin/transactions", ctx -> {
            Map<String, Object> model = Map.of(
                    "title", "Daftar Transaksi",
                    "pageTitle", "Daftar Transaksi",
                    "userName", "Nashya Putri",
                    "userRole", "Admin Warehouse Jakarta",
                    "activePage", "transactions");
            ctx.render("admin/transaction", model);
        });

        // Admin payment page (English route)
        app.get("/admin/payments", ctx -> {
            Map<String, Object> model = Map.of(
                    "title", "Verifikasi Pembayaran",
                    "pageTitle", "Verifikasi Pembayaran",
                    "userName", "Nashya Putri",
                    "userRole", "Admin Keuangan",
                    "activePage", "payments");
            ctx.render("admin/payment", model);
        });

        // Admin shipping page (English route)
        app.get("/admin/shipping", ctx -> {
            Map<String, Object> model = Map.of(
                    "title", "Monitoring Pengiriman",
                    "pageTitle", "Monitoring Pengiriman",
                    "userName", "Nashya Putri",
                    "userRole", "Admin Warehouse Jakarta",
                    "activePage", "shipping");
            ctx.render("admin/shipping", model);
        });

        // Admin user page (English route)
        app.get("/admin/users", ctx -> {
            Map<String, Object> model = Map.of(
                    "title", "Manajemen Pengguna",
                    "pageTitle", "Manajemen Pengguna",
                    "userName", "Nashya Putri",
                    "userRole", "Admin Warehouse Jakarta",
                    "activePage", "users",
                    "totalUsers", 128,
                    "customerCount", 102,
                    "adminCount", 6,
                    "activeToday", 5);
            ctx.render("admin/user", model);
        });

        // Admin reports page (English route)
        app.get("/admin/reports", ctx -> {
            Map<String, Object> model = Map.of(
                    "title", "Laporan Kinerja",
                    "pageTitle", "Laporan Kinerja",
                    "userName", "Nashya Putri",
                    "userRole", "Admin Warehouse Jakarta",
                    "activePage", "reports");
            ctx.render("admin/laporan", model);
        });
    }
}
