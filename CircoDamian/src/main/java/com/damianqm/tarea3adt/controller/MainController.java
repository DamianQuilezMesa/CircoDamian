package com.damianqm.tarea3adt.controller;

import com.damianqm.tarea3adt.config.StageManager;
import com.damianqm.tarea3adt.modelo.Perfil;
import com.damianqm.tarea3adt.services.SesionService;
import com.damianqm.tarea3adt.view.FxmlView;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Pantalla principal post-login.
 * Muestra las opciones disponibles según el perfil del usuario.
 * - Invitado/Todos: Ver espectáculos, Buscar espectáculo
 * - Autenticado: Ficha artista (si es Artista)
 * - Coordinación/Admin: Gestionar espectáculos
 * - Admin: Registrar persona, Modificar persona
 */
@Controller
public class MainController implements Initializable {

    @FXML private Label lblBienvenida;
    @FXML private Label lblPerfil;

    // Botones cuya visibilidad depende del perfil
    @FXML private Button btnVerEspectaculos;
    @FXML private Button btnBuscarEspectaculo;
    @FXML private Button btnGestionEspectaculo;
    @FXML private Button btnGestionNumero;
    @FXML private Button btnRegistrarPersona;
    @FXML private Button btnModificarPersona;
    @FXML private Button btnFichaArtista;
    @FXML private Button btnLogout;
    @FXML private Button btnBienvenida;

    @Autowired
    private SesionService sesionService;

    @Lazy
    @Autowired
    private StageManager stageManager;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurarVistaPorPerfil();
    }

    private void configurarVistaPorPerfil() {
        Perfil perfil = sesionService.getPerfilActual();
        boolean autenticado = sesionService.isAutenticado();

        if (autenticado && sesionService.getUsuarioActual() != null) {
            lblBienvenida.setText("Bienvenido/a, " +
                    sesionService.getUsuarioActual().getPersona().getNombre());
            lblPerfil.setText("Perfil: " + perfil);
        } else {
            lblBienvenida.setText("Bienvenido/a al Circo");
            lblPerfil.setText("Modo invitado");
        }

        // Opciones siempre visibles
        btnVerEspectaculos.setVisible(true);
        setBtn(btnBuscarEspectaculo,  autenticado);

        // Solo coordinación y admin
        setBtn(btnGestionEspectaculo, sesionService.isCoordinacion());
        setBtn(btnGestionNumero,      sesionService.isCoordinacion());

        // Solo admin
        setBtn(btnRegistrarPersona,   sesionService.isAdmin());
        setBtn(btnModificarPersona,   sesionService.isAdmin());

        // Solo artista
        setBtn(btnFichaArtista,       sesionService.isArtista());

        // Logout / Bienvenida
        setBtn(btnLogout,    autenticado);
        setBtn(btnBienvenida, !autenticado);
    }


    /** Ayuda contextual del menú principal. */
    @FXML
    private void mostrarAyuda(ActionEvent e) {
        javafx.scene.control.Alert a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        a.setTitle("Ayuda – Menú Principal");
        a.setHeaderText("Opciones disponibles");
        a.setContentText(
            "Desde aquí accedes a todas las funciones según tu perfil:\n\n" +
            "• Ver Espectáculos: listado público de espectáculos.\n" +
            "• Buscar Espectáculo: busca por nombre (requiere sesión).\n" +
            "• Gestionar Espectáculos: alta/modificación (Coordinación/Admin).\n" +
            "• Registrar / Modificar Persona: gestión de personas (Admin).\n" +
            "• Ver mi Ficha: tus datos y trayectoria (Artista).\n" +
            "• Cerrar Sesión: vuelve al modo invitado."
        );
        a.showAndWait();
    }

    /** Muestra u oculta un botón y actualiza managed para no dejar espacio vacío */
    private void setBtn(Button btn, boolean visible) {
        btn.setVisible(visible);
        btn.setManaged(visible);
    }

    @FXML private void irVerEspectaculos(ActionEvent e) {
        stageManager.switchScene(FxmlView.VER_ESPECTACULOS);
    }

    @FXML private void irBuscarEspectaculo(ActionEvent e) {
        stageManager.switchScene(FxmlView.BUSCAR_ESPECTACULO);
    }

    @FXML private void irGestionEspectaculo(ActionEvent e) {
        stageManager.switchScene(FxmlView.GESTION_ESPECTACULO);
    }

    @FXML private void irGestionNumero(ActionEvent e) {
        stageManager.switchScene(FxmlView.GESTION_NUMERO);
    }

    @FXML private void irRegistrarPersona(ActionEvent e) {
        stageManager.switchScene(FxmlView.REGISTRO_PERSONA);
    }

    @FXML private void irModificarPersona(ActionEvent e) {
        stageManager.switchScene(FxmlView.MODIFICAR_PERSONA);
    }

    @FXML private void irFichaArtista(ActionEvent e) {
        stageManager.switchScene(FxmlView.FICHA_ARTISTA);
    }

    @FXML private void logout(ActionEvent e) {
        sesionService.logout();
        stageManager.switchScene(FxmlView.BIENVENIDA);
    }

    @FXML private void irBienvenida(ActionEvent e) {
        stageManager.switchScene(FxmlView.BIENVENIDA);
    }
}
