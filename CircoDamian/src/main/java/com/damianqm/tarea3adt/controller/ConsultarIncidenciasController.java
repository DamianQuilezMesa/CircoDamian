package com.damianqm.tarea3adt.controller;

import com.damianqm.tarea3adt.config.StageManager;
import com.damianqm.tarea3adt.modelo.Espectaculo;
import com.damianqm.tarea3adt.modelo.Numero;
import com.damianqm.tarea3adt.modelo.objectdb.Incidencia;
import com.damianqm.tarea3adt.modelo.objectdb.ResolucionIncidencia;
import com.damianqm.tarea3adt.modelo.objectdb.TipoIncidencia;
import com.damianqm.tarea3adt.services.EspectaculoService;
import com.damianqm.tarea3adt.services.IncidenciaService;
import com.damianqm.tarea3adt.services.PersonaService;
import com.damianqm.tarea3adt.services.SesionService;
import com.damianqm.tarea3adt.view.FxmlView;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

// CU11 – consultar incidencias / CU9 – resolver incidencia (vista unificada)
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

	// Panel de detalle
	@FXML
	private TitledPane panelDetalle;

	/** ID y nombre de la persona que reportó la incidencia. */
	@FXML
	private Label lblReportadaPor;
	/** Muestra "RESUELTA" / "PENDIENTE" con color. */
	@FXML
	private Label lblEstadoResolucion;
	/** Nombre de la persona que resolvió (vacío si pendiente). */
	@FXML
	private Label lblResueltaPor;
	/** Descripción de las acciones de resolución (vacío si pendiente). */
	@FXML
	private TextArea taResolucionDetalle;
	/** ID de la entidad ResolucionIncidencia (para verificación en pruebas). */
	@FXML
	private Label lblResolucionId;

	/**
	 * Subpanel que solo se muestra si la incidencia es pendiente y el usuario puede
	 * resolverla.
	 */
	@FXML
	private VBox panelResolver;
	@FXML
	private TextArea taAcciones;

	// Servicios
	@Autowired
	private IncidenciaService incidenciaService;
	@Autowired
	private EspectaculoService espectaculoService;
	@Autowired
	private PersonaService personaService;
	@Autowired
	private SesionService sesionService;
	@Lazy
	@Autowired
	private StageManager stageManager;

	private final Map<Long, String> nombreEspectaculo = new HashMap<>();
	private final Map<Long, String> nombreNumero = new HashMap<>();

	private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		cargarCacheEspectaculos();
		configurarFiltros();
		configurarTabla();
		configurarSeleccion();
		buscar(null);
	}

	private void cargarCacheEspectaculos() {
		for (Espectaculo e : espectaculoService.findAll()) {
			nombreEspectaculo.put(e.getId(), e.getNombre());
			for (Numero n : espectaculoService.findNumerosPorEspectaculo(e.getId())) {
				nombreNumero.put(n.getId(), n.getNombre());
			}
		}
	}

	private void configurarFiltros() {
		cbTipo.setItems(FXCollections.observableArrayList(TipoIncidencia.values()));
		cbTipo.setPromptText("Todas");

		cbEstado.setItems(FXCollections.observableArrayList("Todas", "Pendientes", "Resueltas"));
		cbEstado.setValue("Todas");

		cbEspectaculo.setItems(FXCollections.observableArrayList(espectaculoService.findAll()));
		cbEspectaculo.setPromptText("Todos");
		cbEspectaculo.setConverter(new StringConverter<Espectaculo>() {
			@Override
			public String toString(Espectaculo e) {
				return e == null ? "" : e.getNombre();
			}

			@Override
			public Espectaculo fromString(String s) {
				return null;
			}
		});

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
				return n == null ? "" : "[" + n.getId() + "] " + n.getNombre();
			}

			@Override
			public Numero fromString(String s) {
				return null;
			}
		});
	}

	private void configurarTabla() {
		colFecha.setCellValueFactory(d -> new SimpleStringProperty(
				d.getValue().getFechaHora() != null ? d.getValue().getFechaHora().format(FMT) : ""));
		colTipo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTipo().name()));
		colEstado.setCellValueFactory(
				d -> new SimpleStringProperty(d.getValue().isResuelta() ? "RESUELTA" : "PENDIENTE"));

		// ID + nombre en las columnas de espectáculo y número
		colEsp.setCellValueFactory(d -> {
			Long id = d.getValue().getIdEspectaculo();
			if (id == null)
				return new SimpleStringProperty("—");
			return new SimpleStringProperty(id + " – " + nombreEspectaculo.getOrDefault(id, "?"));
		});
		colNum.setCellValueFactory(d -> {
			Long id = d.getValue().getIdNumero();
			if (id == null)
				return new SimpleStringProperty("—");
			return new SimpleStringProperty(id + " – " + nombreNumero.getOrDefault(id, "?"));
		});

		colDesc.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDescripcion()));

		// Color de fila según estado
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
	}

	private void configurarSeleccion() {
		tablaIncidencias.getSelectionModel().selectedItemProperty().addListener((obs, viejo, nueva) -> {
			if (nueva == null) {
				ocultarDetalle();
				return;
			}
			mostrarDetalle(nueva);
		});
	}

	private void mostrarDetalle(Incidencia inc) {
		panelDetalle.setVisible(true);
		panelDetalle.setManaged(true);

		// Reportada por (siempre visible)
		Long idReporta = inc.getIdPersonaReporta();
		String nombreReporta = personaService.findNombrePersonaById(idReporta);
		lblReportadaPor.setText(idReporta + " – " + nombreReporta);

		if (inc.isResuelta()) {
			ResolucionIncidencia res = inc.getResolucion();
			if (res != null) {
				lblEstadoResolucion.setText("RESUELTA");
				lblEstadoResolucion.setStyle("-fx-text-fill:#27ae60; -fx-font-weight:bold;");
				Long idResuelve = res.getIdPersonaResuelve();
				lblResueltaPor.setText(idResuelve + " – " + personaService.findNombrePersonaById(idResuelve));
				taResolucionDetalle.setText(res.getAccionesRealizadas());
				lblResolucionId.setText("ID " + res.getId());
			} else {
				// Incidencia marcada resuelta pero sin objeto resolución (datos legacy)
				lblEstadoResolucion.setText("RESUELTA (sin detalle)");
				lblEstadoResolucion.setStyle("-fx-text-fill:#27ae60;");
				lblResueltaPor.setText("—");
				taResolucionDetalle.setText("");
				lblResolucionId.setText("—");
			}
			panelResolver.setVisible(false);
			panelResolver.setManaged(false);

		} else {
			// Pendiente
			lblEstadoResolucion.setText("PENDIENTE");
			lblEstadoResolucion.setStyle("-fx-text-fill:#e67e22; -fx-font-weight:bold;");
			lblResueltaPor.setText("—");
			taResolucionDetalle.setText("");
			lblResolucionId.setText("—");

			// Mostrar el formulario solo si tiene permiso
			boolean puedeResolver = sesionService.isCoordinacion();
			panelResolver.setVisible(puedeResolver);
			panelResolver.setManaged(puedeResolver);
			if (puedeResolver)
				taAcciones.clear();
		}
	}

	private void ocultarDetalle() {
		panelDetalle.setVisible(false);
		panelDetalle.setManaged(false);
		panelResolver.setVisible(false);
		panelResolver.setManaged(false);
	}

	@FXML
	private void buscar(ActionEvent e) {
		lblMensaje.setText("");
		ocultarDetalle();

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
			error("La fecha 'Hasta' debe ser posterior a 'Desde'.");
			return;
		}

		try {
			List<Incidencia> resultados = incidenciaService.consultar(tipo, resuelta, idEsp, idNum, desde, hasta);
			tablaIncidencias.setItems(FXCollections.observableArrayList(resultados));
			lblResultados.setText("Resultados: " + resultados.size());
		} catch (Exception ex) {
			error("Error al consultar: " + ex.getMessage());
		}
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
				"¿Marcar como resuelta la incidencia del " + sel.getFechaHora().format(FMT) + "?", ButtonType.YES,
				ButtonType.NO);
		confirm.setTitle("Confirmar resolución");
		confirm.showAndWait().ifPresent(btn -> {
			if (btn == ButtonType.YES) {
				try {
					incidenciaService.resolver(sel.getId(), acciones);
					ok("Incidencia resuelta correctamente.");
					buscar(null);
				} catch (Exception ex) {
					error("Error: " + ex.getMessage());
				}
			}
		});
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

	private void error(String m) {
		lblMensaje.setStyle("-fx-text-fill:#c0392b; -fx-font-weight:bold;");
		lblMensaje.setText(m);
	}

	private void ok(String m) {
		lblMensaje.setStyle("-fx-text-fill:#27ae60; -fx-font-weight:bold;");
		lblMensaje.setText(m);
	}
}
