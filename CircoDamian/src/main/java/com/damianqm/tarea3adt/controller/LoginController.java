package com.damianqm.tarea3adt.controller;

import com.damianqm.tarea3adt.config.StageManager;
import com.damianqm.tarea3adt.services.SesionService;
import com.damianqm.tarea3adt.view.FxmlView;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

/** Pantalla de Login (CU2). */
@Controller
public class LoginController implements Initializable {

    @FXML private TextField txtUsuario;
    @FXML private PasswordField pfPassword;
    @FXML private TextField tfPasswordVisible;
    @FXML private CheckBox chkMostrarPassword;
    @FXML private Label lblMensaje;

    @Autowired private SesionService sesionService;
    @Lazy @Autowired private StageManager stageManager;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        tfPasswordVisible.setVisible(false);
        lblMensaje.setText("");

        // Mantener sincronizados los dos campos de contraseña
        pfPassword.textProperty().addListener((obs, viejo, nuevo) -> {
            if (!tfPasswordVisible.getText().equals(nuevo)) {
                tfPasswordVisible.setText(nuevo);
            }
        });
        tfPasswordVisible.textProperty().addListener((obs, viejo, nuevo) -> {
            if (!pfPassword.getText().equals(nuevo)) {
                pfPassword.setText(nuevo);
            }
        });
    }

    @FXML
    private void login(ActionEvent event) {
        String usuario = txtUsuario.getText().trim();
        String password = pfPassword.getText();

        if (usuario.isEmpty() || password.isEmpty()) {
            lblMensaje.setText("Introduce usuario y contraseña.");
            return;
        }

        if (sesionService.login(usuario, password)) {
            stageManager.switchScene(FxmlView.MAIN);
        } else {
            lblMensaje.setText("Usuario o contraseña incorrectos.");
            pfPassword.clear();
            tfPasswordVisible.clear();
        }
    }

    /** Alterna entre mostrar u ocultar la contraseña. */
    @FXML
    private void toggleMostrarPassword(ActionEvent event) {
        boolean mostrar = chkMostrarPassword.isSelected();
        tfPasswordVisible.setVisible(mostrar);
        pfPassword.setVisible(!mostrar);
    }

    @FXML
    private void recuperarPassword(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Recuperar contraseña");
        dialog.setHeaderText("Introduce tu nombre de usuario");
        dialog.setContentText("Usuario:");

        Optional<String> resultado = dialog.showAndWait();
        if (resultado.isPresent()) {
            Optional<String> pass = sesionService.recuperarPassword(resultado.get());
            Alert alert;
            if (pass.isPresent()) {
                alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setContentText("Tu contraseña es: " + pass.get());
            } else {
                alert = new Alert(Alert.AlertType.WARNING);
                alert.setContentText("No se encontró ningún usuario con ese nombre.");
            }
            alert.setTitle("Recuperar contraseña");
            alert.setHeaderText(null);
            alert.showAndWait();
        }
    }

    @FXML
    private void mostrarAyuda(ActionEvent event) {
        Alert ayuda = new Alert(Alert.AlertType.INFORMATION);
        ayuda.setTitle("Ayuda – Inicio de Sesión");
        ayuda.setHeaderText("¿Cómo iniciar sesión?");
        ayuda.setContentText(
            "Introduce tu nombre de usuario y contraseña para acceder.\n\n" +
            "• Marca 'Mostrar contraseña' para verla mientras la escribes.\n" +
            "• Si la olvidaste, pulsa 'Recuperar contraseña'.\n" +
            "• Credenciales del admin: admin / admin.\n\n" +
            "Pulsa F1 para abrir la ayuda en cualquier pantalla."
        );
        ayuda.showAndWait();
    }

    @FXML
    private void volverBienvenida(ActionEvent event) {
        stageManager.switchScene(FxmlView.BIENVENIDA);
    }
}
