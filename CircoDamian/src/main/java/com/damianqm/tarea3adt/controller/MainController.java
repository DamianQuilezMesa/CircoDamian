package com.damianqm.tarea3adt.controller;

import com.damianqm.tarea3adt.config.StageManager;
import com.damianqm.tarea3adt.services.SesionService;
import com.damianqm.tarea3adt.view.FxmlView;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

import java.net.URL;
import java.util.ResourceBundle;

/** Menú principal. Muestra las opciones según el perfil del usuario. */
@Controller
public class MainController implements Initializable {

	@FXML
	private Label lblBienvenida;
	@FXML
	private Label lblPerfil;

	@FXML
	private Button btnVerEspectaculos;
	@FXML
	private Button btnBuscarEspectaculo;
	@FXML
	private Button btnGestionEspectaculo;
	@FXML
	private Button btnGestionNumero;
	@FXML
	private Button btnRegistrarPersona;
	@FXML
	private Button btnModificarPersona;
	@FXML
	private Button btnFichaArtista;
	@FXML
	private Button btnLogout;
	@FXML
	private Button btnBienvenida;

	@Autowired
	private SesionService sesionService;
	@Lazy
	@Autowired
	private StageManager stageManager;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		configurarVistaPorPerfil();
	}

	/** Ajusta textos y botones visibles según el perfil actual. */
	private void configurarVistaPorPerfil() {
		boolean autenticado = sesionService.isAutenticado();

		if (autenticado) {
			lblBienvenida.setText("Bienvenido/a, " + sesionService.getUsuarioActual().getPersona().getNombre());
			lblPerfil.setText("Perfil: " + sesionService.getPerfilActual());
		} else {
			lblBienvenida.setText("Bienvenido/a al Circo");
			lblPerfil.setText("Modo invitado");
		}

		btnVerEspectaculos.setVisible(true);
		mostrarBoton(btnBuscarEspectaculo, autenticado);
		mostrarBoton(btnGestionEspectaculo, sesionService.isCoordinacion());
		mostrarBoton(btnGestionNumero, sesionService.isCoordinacion());
		mostrarBoton(btnRegistrarPersona, sesionService.isAdmin());
		mostrarBoton(btnModificarPersona, sesionService.isAdmin());
		mostrarBoton(btnFichaArtista, sesionService.isArtista());
		mostrarBoton(btnLogout, autenticado);
		mostrarBoton(btnBienvenida, !autenticado);
	}

	private void mostrarBoton(Button btn, boolean visible) {
		btn.setVisible(visible);
		btn.setManaged(visible);
	}

	@FXML
	private void irVerEspectaculos(ActionEvent e) {
		stageManager.switchScene(FxmlView.VER_ESPECTACULOS);
	}

	@FXML
	private void irBuscarEspectaculo(ActionEvent e) {
		stageManager.switchScene(FxmlView.BUSCAR_ESPECTACULO);
	}

	@FXML
	private void irGestionEspectaculo(ActionEvent e) {
		stageManager.switchScene(FxmlView.GESTION_ESPECTACULO);
	}

	@FXML
	private void irGestionNumero(ActionEvent e) {
		stageManager.switchScene(FxmlView.GESTION_NUMERO);
	}

	@FXML
	private void irRegistrarPersona(ActionEvent e) {
		stageManager.switchScene(FxmlView.REGISTRO_PERSONA);
	}

	@FXML
	private void irModificarPersona(ActionEvent e) {
		stageManager.switchScene(FxmlView.MODIFICAR_PERSONA);
	}

	@FXML
	private void irFichaArtista(ActionEvent e) {
		stageManager.switchScene(FxmlView.FICHA_ARTISTA);
	}

	@FXML
	private void irBienvenida(ActionEvent e) {
		stageManager.switchScene(FxmlView.BIENVENIDA);
	}

	@FXML
	private void logout(ActionEvent e) {
		sesionService.logout();
		stageManager.switchScene(FxmlView.BIENVENIDA);
	}

	@FXML
	private void mostrarAyuda(ActionEvent e) {
		Alert a = new Alert(Alert.AlertType.INFORMATION);
		a.setTitle("Ayuda – Menú Principal");
		a.setHeaderText("Opciones disponibles según tu perfil");
		a.setContentText(
				"• Ver Espectáculos: listado público.\n" + "• Buscar Espectáculo: detalle completo (requiere sesión).\n"
						+ "• Gestionar Espectáculos y Números: Coordinación/Admin.\n"
						+ "• Registrar / Modificar Persona: solo Admin.\n" + "• Ver mi Ficha: solo Artista.\n"
						+ "• Cerrar Sesión: vuelve al modo invitado.\n\n"
						+ "Pulsa F1 para abrir la ayuda en cualquier pantalla.");
		a.showAndWait();
	}
}
