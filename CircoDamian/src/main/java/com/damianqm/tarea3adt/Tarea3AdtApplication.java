package com.damianqm.tarea3adt;

import com.damianqm.tarea3adt.config.StageManager;
import com.damianqm.tarea3adt.view.FxmlView;
import javafx.application.Application;
import javafx.stage.Stage;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Clase principal. Une Spring Boot y JavaFX.
 * 1. main() llama a Application.launch().
 * 2. init() arranca Spring Boot (beans, conexión a BD, ejecuta data.sql).
 * 3. start() obtiene el StageManager y muestra la pantalla de Bienvenida.
 */
@SpringBootApplication
public class Tarea3AdtApplication extends Application {

    private ConfigurableApplicationContext springContext;
    private StageManager stageManager;

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void init() {
        String[] args = getParameters().getRaw().toArray(new String[0]);
        springContext = new SpringApplicationBuilder(Tarea3AdtApplication.class).run(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // Pedimos el StageManager al contexto pasándole el Stage para que lo gestione
        stageManager = springContext.getBean(StageManager.class, primaryStage);
        stageManager.switchScene(FxmlView.BIENVENIDA);
    }

    @Override
    public void stop() {
        springContext.close();
    }
}
