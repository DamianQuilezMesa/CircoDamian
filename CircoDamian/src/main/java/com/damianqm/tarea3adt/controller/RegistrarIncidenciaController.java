package com.damianqm.tarea3adt.controller;

import com.damianqm.tarea3adt.config.StageManager;
import com.damianqm.tarea3adt.modelo.Espectaculo;
import com.damianqm.tarea3adt.modelo.Numero;
import com.damianqm.tarea3adt.modelo.objectdb.TipoIncidencia;
import com.damianqm.tarea3adt.services.EspectaculoService;
import com.damianqm.tarea3adt.services.IncidenciaService;
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
import java.util.ResourceBundle;

/**
 * CU8 – Registrar nueva incidencia (cualquier usuario autenticado).
 */
@Controller
public class RegistrarIncidenciaController implements Initializable {

	@FXML
	private ComboBox<TipoIncidencia> cbTipo;
	@FXML
	private TextArea taDescripcion;
	@FXML
	private ComboBox<Espectaculo> cbEspectaculo;
	@FXML
	private ComboBox<Numero> cbNumero;
	@FXML
	private Label lblMensaje;
	@FXML
	private Label lblContador;

	@Autowired
	private IncidenciaService incidenciaService;
	@Autowired
	private EspectaculoService espectaculoService;
	@Lazy
	@Autowired
	private StageManager stageManager;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		cbTipo.setItems(FXCollections.observableArrayList(TipoIncidencia.values()));
		cbTipo.setPromptText("-- Selecciona el tipo --");

		List<Espectaculo> espectaculos = espectaculoService.findAll();
		cbEspectaculo.setItems(FXCollections.observableArrayList(espectaculos));
		cbEspectaculo.setPromptText("-- Ninguno (opcional) --");
		cbEspectaculo.setConverter(new StringConverter<Espectaculo>() {
			@Override
			public String toString(Espectaculo e) {
				return e == null ? "" : "[" + e.getId() + "] " + e.getNombre();
			}

			@Override
			public Espectaculo fromString(String s) {
				return null;
			}
		});

		// Los números se cargan de forma INDEPENDIENTE del espectáculo: una incidencia
		// puede asociarse solo a un número, solo a un espectáculo, a ambos o a ninguno.
		cbNumero.setItems(FXCollections.observableArrayList(espectaculoService.findAllNumeros()));
		cbNumero.setPromptText("-- Ninguno (opcional) --");
		cbNumero.setConverter(new StringConverter<Numero>() {
			@Override
			public String toString(Numero n) {
				return n == null ? "" : "[" + n.getId() + "] " + n.getNombre();
			}

			@Override
			public Numero fromString(String s) {
				return null;
			}
		});

		// Contador de caracteres en descripción
		taDescripcion.textProperty().addListener((obs, viejo, nuevo) -> {
			int len = nuevo == null ? 0 : nuevo.length();
			lblContador.setText(len + " / 1000");
			lblContador.setStyle(len > 1000 ? "-fx-text-fill:#e74c3c;" : "-fx-text-fill:#888;");
		});
	}

	@FXML
	private void guardar(ActionEvent e) {
		lblMensaje.setText("");

		TipoIncidencia tipo = cbTipo.getValue();
		String desc = taDescripcion.getText();
		Espectaculo esp = cbEspectaculo.getValue();
		Numero numero = cbNumero.getValue();

		Long idEsp = esp != null ? esp.getId() : null;
		Long idNum = numero != null ? numero.getId() : null;

		try {
			incidenciaService.registrar(tipo, desc, idEsp, idNum);
			ok("Incidencia registrada correctamente.");
			limpiarFormulario();
		} catch (IllegalArgumentException ex) {
			error(ex.getMessage());
		} catch (Exception ex) {
			error("Error al guardar la incidencia: " + ex.getMessage());
		}
	}

	@FXML
	private void limpiar(ActionEvent e) {
		limpiarFormulario();
		lblMensaje.setText("");
	}

	private void limpiarFormulario() {
		cbTipo.getSelectionModel().clearSelection();
		taDescripcion.clear();
		cbEspectaculo.getSelectionModel().clearSelection();
		// No vaciamos la lista de números (es independiente); solo deseleccionamos.
		cbNumero.getSelectionModel().clearSelection();
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
