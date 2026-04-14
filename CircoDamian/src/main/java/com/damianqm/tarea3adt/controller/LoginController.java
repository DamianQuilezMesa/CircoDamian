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

/**
 * CU2: Login / Logout / Recuperar contraseña.
 */
@Controller
public class LoginController implements Initializable {

    @FXML private TextField txtUsuario;
    @FXML private PasswordField pfPassword;
    @FXML private TextField tfPasswordVisible;
    @FXML private CheckBox chkMostrarPassword;
    @FXML private Label lblMensaje;

    @Autowired
    private SesionService sesionService;

    @Lazy
    @Autowired
    private StageManager stageManager;

    @FXML
    private void login(ActionEvent event) {
        String usuario = txtUsuario.getText().trim();
        String password = chkMostrarPassword.isSelected()
                ? tfPasswordVisible.getText() : pfPassword.getText();
        if (usuario.isEmpty() || password.isEmpty()) {
            lblMensaje.setText("Introduce usuario y contraseña.");
            return;
        }
        if (sesionService.login(usuario, password)) {
            stageManager.switchScene(FxmlView.MAIN);
        } else {
            lblMensaje.setText("Usuario o contraseña incorrectos.");
            pfPassword.clear(); tfPasswordVisible.clear();
        }
    }

    @FXML
    private void toggleMostrarPassword(ActionEvent event) {
        if (chkMostrarPassword.isSelected()) {
            tfPasswordVisible.setText(pfPassword.getText());
            tfPasswordVisible.setVisible(true);
            pfPassword.setVisible(false);
        } else {
            pfPassword.setText(tfPasswordVisible.getText());
            pfPassword.setVisible(true);
            tfPasswordVisible.setVisible(false);
        }
    }

    /** Ayuda contextual de la pantalla de Login. */
    @FXML
    private void mostrarAyuda(ActionEvent event) {
        Alert ayuda = new Alert(Alert.AlertType.INFORMATION);
        ayuda.setTitle("Ayuda – Inicio de Sesión");
        ayuda.setHeaderText("¿Cómo iniciar sesión?");
        ayuda.setContentText(
            "Introduce tu nombre de usuario y contraseña para acceder al sistema.\n\n" +
            "• Marca 'Mostrar contraseña' si quieres ver lo que escribes.\n" +
            "• Si olvidaste tu contraseña, pulsa 'Recuperar contraseña' e introduce tu usuario.\n" +
            "• Las credenciales del administrador son: admin / admin.\n\n" +
            "Si no tienes cuenta, contacta con el Administrador."
        );
        ayuda.showAndWait();
    }

    @FXML
    private void recuperarPassword(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Recuperar contraseña");
        dialog.setHeaderText("Introduce tu nombre de usuario");
        dialog.setContentText("Usuario:");
        Optional<String> resultado = dialog.showAndWait();
        resultado.ifPresent(usuario -> {
            Optional<String> pass = sesionService.recuperarPassword(usuario);
            Alert alert = new Alert(pass.isPresent() ? Alert.AlertType.INFORMATION : Alert.AlertType.WARNING);
            alert.setTitle("Recuperar contraseña");
            alert.setHeaderText(null);
            alert.setContentText(pass.isPresent()
                    ? "Tu contraseña es: " + pass.get()
                    : "No se encontró ningún usuario con ese nombre.");
            alert.showAndWait();
        });
    }


    @FXML
    private void volverBienvenida(javafx.event.ActionEvent e) {
        stageManager.switchScene(com.damianqm.tarea3adt.view.FxmlView.BIENVENIDA);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        tfPasswordVisible.setVisible(false);
        lblMensaje.setText("");
    }
}
