package com.damianqm.tarea3adt.config;

import com.damianqm.tarea3adt.view.FxmlView;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * Gestiona el Stage principal y el cambio de escenas.
 * Tamaño fijo, ventana no redimensionable.
 * Además conecta la tecla F1 a la ayuda del controlador activo.
 */
public class StageManager {

    private static final Logger LOG = LoggerFactory.getLogger(StageManager.class);

    /** Ancho fijo (incluye márgenes del SO). */
    private static final double W = 1120;
    /** Alto fijo (incluye barra de título del SO). */
    private static final double H = 760;

    private final Stage primaryStage;
    private final SpringFXMLLoader springFXMLLoader;

    public StageManager(SpringFXMLLoader springFXMLLoader, Stage stage) {
        this.springFXMLLoader = springFXMLLoader;
        this.primaryStage = stage;
    }

    /** Cambia la escena mostrando la vista indicada y engancha F1 a su ayuda. */
    public void switchScene(FxmlView view) {
        try {
            FXMLLoader loader = springFXMLLoader.loadWithLoader(view.getFxmlFile());
            mostrar(loader.getRoot(), view.getTitle(), loader.getController());
        } catch (Exception e) {
            LOG.error("Error al cargar la vista: " + view.getFxmlFile(), e);
            Platform.exit();
        }
    }

    private void mostrar(Parent root, String title, Object controller) {
        Scene scene = primaryStage.getScene();
        if (scene == null) {
            scene = new Scene(root, W, H);
        } else {
            scene.setRoot(root);
        }

        // F1: abre la ayuda del controlador actual (si tiene el método)
        scene.setOnKeyPressed((KeyEvent ev) -> {
            if (ev.getCode() == KeyCode.F1) {
                invocarAyuda(controller);
                ev.consume();
            }
        });

        primaryStage.setTitle(title);
        primaryStage.setScene(scene);

        primaryStage.setMaximized(false);
        primaryStage.setFullScreen(false);
        primaryStage.setWidth(W);
        primaryStage.setHeight(H);
        primaryStage.setMinWidth(W);
        primaryStage.setMinHeight(H);
        primaryStage.setMaxWidth(W);
        primaryStage.setMaxHeight(H);
        primaryStage.setResizable(false);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    /**
     * Busca el método 'mostrarAyuda' del controlador y lo invoca.
     * Usamos reflexión para no obligar a que todos implementen una interfaz común.
     */
    private void invocarAyuda(Object controller) {
        if (controller == null) return;
        try {
            Method m = controller.getClass().getDeclaredMethod("mostrarAyuda", ActionEvent.class);
            m.setAccessible(true);
            m.invoke(controller, (ActionEvent) null);
        } catch (NoSuchMethodException e) {
            // La pantalla no tiene ayuda, no hacemos nada
        } catch (Exception e) {
            LOG.warn("No se pudo abrir la ayuda con F1", e);
        }
    }
}
