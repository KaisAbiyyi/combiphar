
import java.util.Map;

import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.loader.ClasspathLoader;

import io.javalin.Javalin;
import io.javalin.rendering.template.JavalinPebble;

/**
 * Application entry point for Combiphar Used Goods system. Configures Javalin
 * server with Pebble templating and static file serving.
 */
public class Main {

    private static final int PORT = 7070;

    public static void main(String[] args) {
        PebbleEngine engine = createPebbleEngine();
        Javalin app = createApp(engine);
        registerRoutes(app);
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
        return Javalin.create(config -> {
            config.fileRenderer(new JavalinPebble(engine));
            config.staticFiles.add("/static");
        });
    }

    /**
     * Registers all application routes.
     */
    private static void registerRoutes(Javalin app) {
        // Home / Catalog page
        app.get("/", ctx -> {
            Map<String, Object> model = Map.of(
                    "title", "Katalog - Combiphar Used Goods",
                    "activePage", "catalog");
            ctx.render("customer/catalog", model);
        });

        app.get("/catalog", ctx -> {
            Map<String, Object> model = Map.of(
                    "title", "Katalog - Combiphar Used Goods",
                    "activePage", "catalog");
            ctx.render("customer/catalog", model);
        });

        // Product detail page (dummy for now)
        app.get("/product/{id}", ctx -> {
            String productId = ctx.pathParam("id");
            Map<String, Object> model = Map.of(
                    "title", "Set Meja Rapat Glasium - Combiphar Used Goods",
                    "productId", productId);
            ctx.render("customer/product-detail", model);
        });

        // Cart page
        app.get("/cart", ctx -> {
            Map<String, Object> model = Map.of(
                    "title", "Keranjang Saya - Combiphar Used Goods",
                    "activePage", "cart");
            ctx.render("customer/cart", model);
        });

        // Checkout page
        app.get("/checkout", ctx -> {
            Map<String, Object> model = Map.of(
                    "title", "Checkout Pesanan - Combiphar Used Goods",
                    "activePage", "checkout");
            ctx.render("customer/checkout", model);
        });

        // Admin login page
        app.get("/admin/login", ctx -> {
            ctx.render("admin/login");
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

        // Admin category page
        app.get("/admin/kategori", ctx -> {
            Map<String, Object> model = Map.of(
                    "title", "Kategori Produk",
                    "pageTitle", "Kategori Produk",
                    "userName", "Nashya Putri",
                    "userRole", "Admin Warehouse Jakarta",
                    "activePage", "kategori");
            ctx.render("admin/category", model);
        });
    }
}
