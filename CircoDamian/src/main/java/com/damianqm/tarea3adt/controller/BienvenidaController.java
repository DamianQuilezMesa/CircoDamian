package com.damianqm.tarea3adt.controller;

import com.damianqm.tarea3adt.config.StageManager;
import com.damianqm.tarea3adt.view.FxmlView;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

/** Pantalla de Bienvenida (inicio). */
@Controller
public class BienvenidaController {

    @Lazy @Autowired
    private StageManager stageManager;

    @FXML
    private void verEspectaculos(ActionEvent event) {
        stageManager.switchScene(FxmlView.VER_ESPECTACULOS);
    }

    @FXML
    private void irALogin(ActionEvent event) {
        stageManager.switchScene(FxmlView.LOGIN);
    }

    @FXML
    private void mostrarAyuda(ActionEvent event) {
        Alert ayuda = new Alert(Alert.AlertType.INFORMATION);
        ayuda.setTitle("Ayuda – Pantalla de Bienvenida");
        ayuda.setHeaderText("¿Qué puedo hacer aquí?");
        ayuda.setContentText(
            "Pantalla de inicio del sistema CIRCO.\n\n" +
            "• Ver Espectáculos: listado público sin identificarse.\n" +
            "• Iniciar Sesión: acceder con tus credenciales.\n\n" +
            "Pulsa F1 en cualquier pantalla para abrir la ayuda."
        );
        ayuda.showAndWait();
    }
}
