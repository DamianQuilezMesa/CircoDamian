package com.damianqm.tarea3adt.config;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.Objects;

import org.slf4j.Logger;

import com.damianqm.tarea3adt.view.FxmlView;

import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Gestiona el cambio de escenas en el Stage principal.
 * Tamaño fijo 900×620 — no redimensionable.
 */
public class StageManager {

    private static final Logger LOG = getLogger(StageManager.class);

    /** Ancho fijo de todas las ventanas */
    private static final double W = 900;
    /** Alto fijo de todas las ventanas */
    private static final double H = 620;

    private final Stage            primaryStage;
    private final SpringFXMLLoader springFXMLLoader;

    public StageManager(SpringFXMLLoader springFXMLLoader, Stage stage) {
        this.springFXMLLoader = springFXMLLoader;
        this.primaryStage     = stage;
    }

    public void switchScene(final FxmlView view) {
        Parent root = loadViewNodeHierarchy(view.getFxmlFile());
        show(root, view.getTitle());
    }

    private void show(final Parent rootnode, String title) {
        Scene scene = primaryStage.getScene();
        if (scene == null) {
            scene = new Scene(rootnode, W, H);
        } else {
            scene.setRoot(rootnode);
        }

        primaryStage.setTitle(title);
        primaryStage.setScene(scene);

        // Tamaño fijo — no redimensionable
        primaryStage.setWidth(W);
        primaryStage.setHeight(H);
        primaryStage.setMinWidth(W);
        primaryStage.setMinHeight(H);
        primaryStage.setMaxWidth(W);
        primaryStage.setMaxHeight(H);
        primaryStage.setResizable(false);

        primaryStage.centerOnScreen();

        try {
            primaryStage.show();
        } catch (Exception e) {
            logAndExit("Unable to show scene: " + title, e);
        }
    }

    private Parent loadViewNodeHierarchy(String fxmlFilePath) {
        Parent rootNode = null;
        try {
            rootNode = springFXMLLoader.load(fxmlFilePath);
            Objects.requireNonNull(rootNode, "Root FXML node must not be null");
        } catch (Exception e) {
            logAndExit("Unable to load FXML: " + fxmlFilePath, e);
        }
        return rootNode;
    }

    private void logAndExit(String errorMsg, Exception exception) {
        LOG.error(errorMsg, exception, exception.getCause());
        Platform.exit();
    }
}
