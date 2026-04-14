package com.damianqm.tarea3adt.controller;

import com.damianqm.tarea3adt.config.StageManager;
import com.damianqm.tarea3adt.view.FxmlView;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Pantalla de bienvenida (primera interfaz que ve el usuario).
 * Permite:
 *  - Ver espectáculos sin autenticarse (CU1 – acceso invitado)
 *  - Ir al Login para autenticarse
 */
@Controller
public class BienvenidaController implements Initializable {

    @Lazy @Autowired
    private StageManager stageManager;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // No se requiere inicialización especial
    }

    /** Navega a la vista de espectáculos sin necesidad de autenticación (CU1). */
    @FXML
    private void verEspectaculos(ActionEvent event) {
        stageManager.switchScene(FxmlView.VER_ESPECTACULOS);
    }

    /** Navega a la pantalla de inicio de sesión. */
    @FXML
    private void irALogin(ActionEvent event) {
        stageManager.switchScene(FxmlView.LOGIN);
    }

    /** Muestra la ayuda contextual de esta pantalla. */
    @FXML
    private void mostrarAyuda(ActionEvent event) {
        Alert ayuda = new Alert(Alert.AlertType.INFORMATION);
        ayuda.setTitle("Ayuda – Pantalla de Bienvenida");
        ayuda.setHeaderText("¿Qué puedo hacer aquí?");
        ayuda.setContentText(
            "Esta es la pantalla de inicio del sistema CIRCO.\n\n" +
            "• 'Ver Espectáculos' → Consulta el listado de todos los espectáculos " +
              "disponibles sin necesidad de identificarte.\n\n" +
            "• 'Iniciar Sesión' → Accede con tus credenciales para ver funcionalidades " +
              "adicionales según tu perfil (Artista, Coordinación o Admin).\n\n" +
            "Si no tienes cuenta, contacta con el Administrador del sistema."
        );
        ayuda.showAndWait();
    }
}
