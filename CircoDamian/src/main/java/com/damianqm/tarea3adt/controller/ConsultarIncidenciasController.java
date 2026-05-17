package com.damianqm.tarea3adt.controller;

import com.damianqm.tarea3adt.config.StageManager;
import com.damianqm.tarea3adt.modelo.Espectaculo;
import com.damianqm.tarea3adt.modelo.Numero;
import com.damianqm.tarea3adt.modelo.objectdb.Incidencia;
import com.damianqm.tarea3adt.modelo.objectdb.TipoIncidencia;
import com.damianqm.tarea3adt.services.EspectaculoService;
import com.damianqm.tarea3adt.services.IncidenciaService;
import com.damianqm.tarea3adt.view.FxmlView;
import javafx.beans.property.SimpleStringProperty;
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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

/**
 * CU11 – Consultar incidencias (cualquier usuario autenticado). Filtros: tipo,
 * estado, espectáculo, número, rango de fechas. Usa JPQL a través de
 * IncidenciaService/IncidenciaRepository.
 */
@Controller
public class ConsultarIncidenciasController implements Initializable {

	// Filtros
	@FXML
	private ComboBox<TipoIncidencia> cbTipo;
	@FXML
	private ComboBox<String> cbEstado;
	@FXML
	private ComboBox<Espectaculo> cbEspectaculo;
	@FXML
	private ComboBox<Numero> cbNumero;
	@FXML
	private DatePicker dpDesde;
	@FXML
	private DatePicker dpHasta;

	// Tabla
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
	private TableColumn<Incidencia, String> colEsp;
	@FXML
	private TableColumn<Incidencia, String> colNum;
	@FXML
	private TableColumn<Incidencia, String> colDesc;

	@FXML
	private Label lblResultados;
	@FXML
	private Label lblMensaje;

	private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

	@Autowired
	private IncidenciaService incidenciaService;
	@Autowired
	private EspectaculoService espectaculoService;
	@Lazy
	@Autowired
	private StageManager stageManager;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// Combo tipo (con opción "Todas")
		cbTipo.setItems(FXCollections.observableArrayList(TipoIncidencia.values()));
		cbTipo.setPromptText("Todas");

		// Combo estado
		cbEstado.setItems(FXCollections.observableArrayList("Todas", "Pendientes", "Resueltas"));
		cbEstado.setValue("Todas");

		// Combo espectáculo
		cbEspectaculo.setItems(FXCollections.observableArrayList(espectaculoService.findAll()));
		cbEspectaculo.setPromptText("Todos");
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

		// Al elegir espectáculo, cargar sus números
		cbEspectaculo.getSelectionModel().selectedItemProperty().addListener((obs, viejo, nuevo) -> {
			cbNumero.getItems().clear();
			cbNumero.getSelectionModel().clearSelection();
			if (nuevo != null) {
				cbNumero.setItems(
						FXCollections.observableArrayList(espectaculoService.findNumerosPorEspectaculo(nuevo.getId())));
			}
		});

		cbNumero.setPromptText("Todos");
		cbNumero.setConverter(new StringConverter<Numero>() {
			@Override
			public String toString(Numero n) {
				return n == null ? "" : "[" + n.getOrden() + "] " + n.getNombre();
			}

			@Override
			public Numero fromString(String s) {
				return null;
			}
		});

		// Columnas
		colId.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getId())));
		colFecha.setCellValueFactory(d -> new SimpleStringProperty(
				d.getValue().getFechaHora() != null ? d.getValue().getFechaHora().format(FMT) : ""));
		colTipo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTipo().name()));
		colEstado.setCellValueFactory(
				d -> new SimpleStringProperty(d.getValue().isResuelta() ? "RESUELTA" : "PENDIENTE"));
		colEsp.setCellValueFactory(d -> new SimpleStringProperty(
				d.getValue().getIdEspectaculo() != null ? String.valueOf(d.getValue().getIdEspectaculo()) : "—"));
		colNum.setCellValueFactory(d -> new SimpleStringProperty(
				d.getValue().getIdNumero() != null ? String.valueOf(d.getValue().getIdNumero()) : "—"));
		colDesc.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDescripcion()));

		// Colorear según estado
		tablaIncidencias.setRowFactory(tv -> new TableRow<>() {
			@Override
			protected void updateItem(Incidencia item, boolean empty) {
				super.updateItem(item, empty);
				if (item == null || empty)
					setStyle("");
				else if (item.isResuelta())
					setStyle("-fx-background-color:#d5f5e3;");
				else
					setStyle("-fx-background-color:#fdebd0;");
			}
		});

		buscar(null);
	}

	@FXML
	private void buscar(ActionEvent e) {
		lblMensaje.setText("");

		TipoIncidencia tipo = cbTipo.getValue();
		String est = cbEstado.getValue();
		Espectaculo esp = cbEspectaculo.getValue();
		Numero num = cbNumero.getValue();

		Boolean resuelta = null;
		if ("Pendientes".equals(est))
			resuelta = false;
		else if ("Resueltas".equals(est))
			resuelta = true;

		Long idEsp = esp != null ? esp.getId() : null;
		Long idNum = num != null ? num.getId() : null;

		LocalDateTime desde = dpDesde.getValue() != null ? dpDesde.getValue().atStartOfDay() : null;
		LocalDateTime hasta = dpHasta.getValue() != null ? dpHasta.getValue().atTime(LocalTime.MAX) : null;

		if (desde != null && hasta != null && hasta.isBefore(desde)) {
			lblMensaje.setStyle("-fx-text-fill:#c0392b;");
			lblMensaje.setText("La fecha 'Hasta' debe ser posterior a 'Desde'.");
			return;
		}

		try {
			List<Incidencia> resultados = incidenciaService.consultar(tipo, resuelta, idEsp, idNum, desde, hasta);
			tablaIncidencias.setItems(FXCollections.observableArrayList(resultados));
			lblResultados.setText("Resultados: " + resultados.size());
		} catch (Exception ex) {
			lblMensaje.setStyle("-fx-text-fill:#c0392b;");
			lblMensaje.setText("Error al consultar: " + ex.getMessage());
		}
	}

	@FXML
	private void limpiar(ActionEvent e) {
		cbTipo.getSelectionModel().clearSelection();
		cbEstado.setValue("Todas");
		cbEspectaculo.getSelectionModel().clearSelection();
		cbNumero.getItems().clear();
		dpDesde.setValue(null);
		dpHasta.setValue(null);
		lblMensaje.setText("");
		buscar(null);
	}

	@FXML
	private void volver(ActionEvent e) {
		stageManager.switchScene(FxmlView.MAIN);
	}
}
