package com.damianqm.tarea3adt.controller;

import com.damianqm.tarea3adt.config.StageManager;
import com.damianqm.tarea3adt.modelo.objectdb.Incidencia;
import com.damianqm.tarea3adt.services.IncidenciaService;
import com.damianqm.tarea3adt.view.FxmlView;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

/**
 * CU9 – Resolver incidencia (Coordinador y Administrador).
 */
@Controller
public class ResolverIncidenciaController implements Initializable {

	@FXML
	private TableView<Incidencia> tablaIncidencias;
	@FXML
	private TableColumn<Incidencia, String> colId;
	@FXML
	private TableColumn<Incidencia, String> colFecha;
	@FXML
	private TableColumn<Incidencia, String> colTipo;
	@FXML
	private TableColumn<Incidencia, String> colEstado;
	@FXML
	private TableColumn<Incidencia, String> colDesc;
	@FXML
	private TextArea taAcciones;
	@FXML
	private Label lblMensaje;
	@FXML
	private Label lblSeleccionada;

	private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

	@Autowired
	private IncidenciaService incidenciaService;
	@Lazy
	@Autowired
	private StageManager stageManager;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		colId.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getId())));
		colFecha.setCellValueFactory(d -> new SimpleStringProperty(
				d.getValue().getFechaHora() != null ? d.getValue().getFechaHora().format(FMT) : ""));
		colTipo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTipo().name()));
		colEstado.setCellValueFactory(
				d -> new SimpleStringProperty(d.getValue().isResuelta() ? "RESUELTA" : "PENDIENTE"));
		colDesc.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDescripcion()));

		// Colorear filas según estado
		tablaIncidencias.setRowFactory(tv -> new TableRow<>() {
			@Override
			protected void updateItem(Incidencia item, boolean empty) {
				super.updateItem(item, empty);
				if (item == null || empty) {
					setStyle("");
				} else if (item.isResuelta()) {
					setStyle("-fx-background-color:#d5f5e3;");
				} else {
					setStyle("-fx-background-color:#fdebd0;");
				}
			}
		});

		tablaIncidencias.getSelectionModel().selectedItemProperty().addListener((obs, viejo, nueva) -> {
			if (nueva != null) {
				lblSeleccionada.setText("Seleccionada: ID " + nueva.getId() + " | " + nueva.getTipo() + " | "
						+ (nueva.isResuelta() ? "ya resuelta" : "pendiente"));
			}
		});

		cargarIncidencias();
	}

	private void cargarIncidencias() {
		List<Incidencia> pendientes = incidenciaService.findAll();
		tablaIncidencias.setItems(FXCollections.observableArrayList(pendientes));
	}

	@FXML
	private void resolver(ActionEvent e) {
		lblMensaje.setText("");
		Incidencia sel = tablaIncidencias.getSelectionModel().getSelectedItem();
		if (sel == null) {
			error("Selecciona una incidencia de la tabla.");
			return;
		}
		if (sel.isResuelta()) {
			error("Esta incidencia ya está resuelta.");
			return;
		}
		String acciones = taAcciones.getText();
		if (acciones == null || acciones.isBlank()) {
			error("Describe las acciones realizadas para resolver la incidencia.");
			return;
		}

		Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
				"¿Marcar como resuelta la incidencia ID " + sel.getId() + "?", ButtonType.YES, ButtonType.NO);
		confirm.setTitle("Confirmar resolución");
		confirm.showAndWait().ifPresent(btn -> {
			if (btn == ButtonType.YES) {
				try {
					incidenciaService.resolver(sel.getId(), acciones);
					ok("Incidencia resuelta correctamente.");
					taAcciones.clear();
					cargarIncidencias();
				} catch (Exception ex) {
					error("Error: " + ex.getMessage());
				}
			}
		});
	}

	@FXML
	private void refrescar(ActionEvent e) {
		cargarIncidencias();
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
