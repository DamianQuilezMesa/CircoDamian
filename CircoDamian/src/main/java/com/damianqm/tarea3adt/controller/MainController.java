package com.damianqm.tarea3adt.controller;

import com.damianqm.tarea3adt.config.StageManager;
import com.damianqm.tarea3adt.services.SesionService;
import com.damianqm.tarea3adt.view.FxmlView;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

import java.net.URL;
import java.util.ResourceBundle;

@Controller
public class MainController implements Initializable {

	@FXML
	private Label lblBienvenida;
	@FXML
	private Label lblPerfil;

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
	private Button btnHistorial;
	@FXML
	private Button btnLogout;
	@FXML
	private Button btnBienvenida;

	// Incidencias (Tarea 4 - Parte 2)
	@FXML
	private Separator sepIncidencias;
	@FXML
	private Button btnRegistrarIncidencia;
	@FXML
	private Button btnConsultarIncidencias;
	@FXML
	private Button btnResolverIncidencia;

	// Dossiers MongoDB (Tarea 6)
	@FXML
	private Button btnActualizarDossier;

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
		boolean autenticado = sesionService.isAutenticado();

		if (autenticado) {
			lblBienvenida.setText("Bienvenido/a, " + sesionService.getNombreUsuarioActual());
			lblPerfil.setText("Perfil: " + sesionService.getPerfilActual());
		} else {
			lblBienvenida.setText("Bienvenido/a al Circo");
			lblPerfil.setText("Modo invitado");
		}

		mostrarBoton(btnBuscarEspectaculo, autenticado);
		mostrarBoton(btnGestionEspectaculo, sesionService.isCoordinacion());
		mostrarBoton(btnGestionNumero, sesionService.isCoordinacion());
		mostrarBoton(btnRegistrarPersona, sesionService.isAdmin());
		mostrarBoton(btnModificarPersona, sesionService.isAdmin());
		mostrarBoton(btnHistorial, sesionService.isAdmin());
		mostrarBoton(btnFichaArtista, sesionService.isArtista());

		// Incidencias: registrar y consultar para cualquier autenticado
		mostrarBoton(btnRegistrarIncidencia, autenticado);
		mostrarBoton(btnConsultarIncidencias, autenticado);
		// Resolver: solo coordinación y admin
		mostrarBoton(btnResolverIncidencia, sesionService.isCoordinacion());

		// Dossiers: solo coordinación y admin
		mostrarBoton(btnActualizarDossier, sesionService.isCoordinacion());

		// Separador visible si el usuario puede ver incidencias
		sepIncidencias.setVisible(autenticado);
		sepIncidencias.setManaged(autenticado);

		mostrarBoton(btnLogout, autenticado);
		mostrarBoton(btnBienvenida, !autenticado);
	}

	private void mostrarBoton(Button btn, boolean visible) {
		btn.setVisible(visible);
		btn.setManaged(visible);
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
	private void irHistorial(ActionEvent e) {
		stageManager.switchScene(FxmlView.HISTORIAL);
	}

	@FXML
	private void irRegistrarIncidencia(ActionEvent e) {
		stageManager.switchScene(FxmlView.REGISTRAR_INCIDENCIA);
	}

	@FXML
	private void irConsultarIncidencias(ActionEvent e) {
		stageManager.switchScene(FxmlView.CONSULTAR_INCIDENCIAS);
	}

	@FXML
	private void irResolverIncidencia(ActionEvent e) {
		stageManager.switchScene(FxmlView.CONSULTAR_INCIDENCIAS);
	}

	@FXML
	private void irActualizarDossier(ActionEvent e) {
		stageManager.switchScene(FxmlView.ACTUALIZAR_DOSSIER);
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
}
