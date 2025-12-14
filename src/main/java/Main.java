import java.util.Map;

import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.loader.ClasspathLoader;

import io.javalin.Javalin;
import io.javalin.rendering.template.JavalinPebble;

public class Main {
    public static void main(String[] args) {

        // Pastikan loader nyari ke: /templates/...
        ClasspathLoader loader = new ClasspathLoader();
        loader.setPrefix("templates");     // <= folder di resources
        loader.setSuffix(".pebble");       // <= ekstensi

        PebbleEngine engine = new PebbleEngine.Builder()
                .loader(loader)
                .build();

        Javalin app = Javalin.create(config -> {
            config.fileRenderer(new JavalinPebble(engine));
        });

        app.get("/", ctx -> {
            Map<String, Object> model = Map.of(
                    "title", "Hello from Javalin + Pebble",
                    "name", "Your Majesty"
            );

            // Karena suffix sudah ".pebble", cukup pakai nama tanpa ekstensi:
            ctx.render("home", model);
        });

        app.start(7070);
    }
}
