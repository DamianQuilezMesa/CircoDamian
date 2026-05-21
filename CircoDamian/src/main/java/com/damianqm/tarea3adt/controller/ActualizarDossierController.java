package com.damianqm.tarea3adt.controller;

import com.damianqm.tarea3adt.config.StageManager;
import com.damianqm.tarea3adt.modelo.Artista;
import com.damianqm.tarea3adt.modelo.mongodb.Dossier;
import com.damianqm.tarea3adt.modelo.mongodb.EntradaTrayectoria;
import com.damianqm.tarea3adt.modelo.mongodb.Evaluacion;
import com.damianqm.tarea3adt.services.DossierService;
import com.damianqm.tarea3adt.services.EspectaculoService;
import com.damianqm.tarea3adt.view.FxmlView;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.StringConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * CU12 – Consultar y actualizar el dossier MongoDB de un artista. Permite a
 * coordinación y admin añadir evaluaciones (con nivel y comentario) o
 * actualizar las observaciones generales del artista.
 */
@Controller
public class ActualizarDossierController implements Initializable {

	@FXML
	private ComboBox<Artista> cbArtista;
	@FXML
	private Label lblNombre;
	@FXML
	private Label lblEmail;
	@FXML
	private Label lblNac;
	@FXML
	private Label lblApodo;
	@FXML
	private Label lblEspecialidades;
	@FXML
	private Label lblTrayectoria;
	@FXML
	private Label lblObservaciones;
	@FXML
	private ComboBox<String> cbNivel;
	@FXML
	private TextArea taComentario;
	@FXML
	private TextArea taObservaciones;
	@FXML
	private Label lblMensaje;

	@Autowired
	private DossierService dossierService;
	@Autowired
	private EspectaculoService espectaculoService;
	@Lazy
	@Autowired
	private StageManager stageManager;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		cbNivel.setItems(FXCollections.observableArrayList("BAJO", "MEDIO", "ALTO"));
		cbNivel.setPromptText("-- Selecciona --");

		List<Artista> artistas = espectaculoService.findAllArtistas();
		cbArtista.setItems(FXCollections.observableArrayList(artistas));
		cbArtista.setConverter(new StringConverter<Artista>() {
			@Override
			public String toString(Artista a) {
				return a == null ? "" : "[" + a.getId() + "] " + a.getNombre();
			}

			@Override
			public Artista fromString(String s) {
				return null;
			}
		});

		cbArtista.getSelectionModel().selectedItemProperty().addListener((obs, viejo, nuevo) -> {
			limpiarDatos();
			if (nuevo != null) {
				mostrarDossier(nuevo.getId());
			}
		});
	}

	private void mostrarDossier(Long idArtista) {
		Optional<Dossier> opt = dossierService.buscarPorArtista(idArtista);
		if (opt.isEmpty()) {
			limpiarDatos();
			error("No se encontró dossier para este artista.");
			return;
		}
		Dossier d = opt.get();
		lblNombre.setText(d.getNombre() != null ? d.getNombre() : "—");
		lblEmail.setText(d.getEmail() != null ? d.getEmail() : "—");
		lblNac.setText(d.getNacionalidad() != null ? d.getNacionalidad() : "—");
		lblApodo.setText(d.getApodo() != null ? d.getApodo() : "—");
		lblEspecialidades.setText(d.getEspecialidades().isEmpty() ? "—" : String.join(", ", d.getEspecialidades()));
		lblTrayectoria.setText(formatearTrayectoria(d));
		lblObservaciones
				.setText(d.getObservaciones() != null && !d.getObservaciones().isBlank() ? d.getObservaciones() : "—");
		lblMensaje.setText("");
	}

	private String formatearTrayectoria(Dossier d) {
		if (d.getTrayectoria().isEmpty())
			return "Sin números asignados aún";
		StringBuilder sb = new StringBuilder();
		for (EntradaTrayectoria e : d.getTrayectoria()) {
			sb.append("• [Esp.").append(e.getIdEspectaculo()).append("] ").append(e.getNombreEspectaculo())
					.append(" → Nº").append(e.getIdNumero()).append(" ").append(e.getNombreNumero()).append(" (orden ")
					.append(e.getOrden()).append(", ").append(e.getDuracion()).append(" min)\n");
		}
		// También mostrar evaluaciones existentes
		if (!d.getEvaluaciones().isEmpty()) {
			sb.append("\nEvaluaciones (").append(d.getEvaluaciones().size()).append("):\n");
			for (Evaluacion ev : d.getEvaluaciones()) {
				sb.append("  [").append(ev.getFecha()).append("] ").append(ev.getNivel()).append(" – ")
						.append(ev.getComentario()).append("\n");
			}
		}
		return sb.toString().trim();
	}

	@FXML
	private void guardarEvaluacion(ActionEvent e) {
		lblMensaje.setText("");
		Artista artista = cbArtista.getValue();
		if (artista == null) {
			error("Selecciona un artista primero.");
			return;
		}

		String nivel = cbNivel.getValue();
		String comentario = taComentario.getText();
		try {
			dossierService.agregarEvaluacion(artista.getId(), comentario, nivel);
			ok("Evaluación registrada correctamente.");
			taComentario.clear();
			cbNivel.getSelectionModel().clearSelection();
			mostrarDossier(artista.getId());
		} catch (IllegalArgumentException ex) {
			error(ex.getMessage());
		} catch (Exception ex) {
			error("Error al guardar la evaluación: " + ex.getMessage());
		}
	}

	@FXML
	private void guardarObservaciones(ActionEvent e) {
		lblMensaje.setText("");
		Artista artista = cbArtista.getValue();
		if (artista == null) {
			error("Selecciona un artista primero.");
			return;
		}

		try {
			dossierService.actualizarObservaciones(artista.getId(), taObservaciones.getText());
			ok("Observaciones actualizadas correctamente.");
			taObservaciones.clear();
			mostrarDossier(artista.getId());
		} catch (Exception ex) {
			error("Error al guardar las observaciones: " + ex.getMessage());
		}
	}

	private void limpiarDatos() {
		lblNombre.setText("—");
		lblEmail.setText("—");
		lblNac.setText("—");
		lblApodo.setText("—");
		lblEspecialidades.setText("—");
		lblTrayectoria.setText("—");
		lblObservaciones.setText("—");
		lblMensaje.setText("");
	}

	private void error(String m) {
		lblMensaje.setStyle("-fx-text-fill:#c0392b; -fx-font-weight:bold;");
		lblMensaje.setText(m);
	}

	private void ok(String m) {
		lblMensaje.setStyle("-fx-text-fill:#27ae60; -fx-font-weight:bold;");
		lblMensaje.setText(m);
	}

	@FXML
	private void volver(ActionEvent e) {
		stageManager.switchScene(FxmlView.MAIN);
	}
}
