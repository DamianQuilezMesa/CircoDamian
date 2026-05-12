package com.damianqm.tarea3adt.controller;

import com.damianqm.tarea3adt.config.StageManager;
import com.damianqm.tarea3adt.modelo.Artista;
import com.damianqm.tarea3adt.modelo.Coordinacion;
import com.damianqm.tarea3adt.modelo.Espectaculo;
import com.damianqm.tarea3adt.modelo.Numero;
import com.damianqm.tarea3adt.services.EspectaculoService;
import com.damianqm.tarea3adt.services.PersonaService;
import com.damianqm.tarea3adt.services.SesionService;
import com.damianqm.tarea3adt.view.FxmlView;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.StringConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Gestión de espectáculos (CU5A + CU5B integrado).
 * <p>
 * Flujo: Paso 1 → datos básicos del espectáculo (nombre, fechas, coordinador).
 * Al guardar se persiste el espectáculo (nuevo o actualizado). Paso 2 → gestión
 * de sus números: crear, editar y eliminar números directamente asociados al
 * espectáculo.
 * <p>
 * La relación Espectáculo → Número es 1:N; cada número pertenece a un único
 * espectáculo y tiene su orden dentro de él.
 */
@Controller
public class GestionEspectaculoController implements Initializable {
	@FXML
	private VBox panelPaso1;
	@FXML
	private TextField txtNombreEsp;
	@FXML
	private DatePicker dpInicio;
	@FXML
	private DatePicker dpFin;
	@FXML
	private ComboBox<Coordinacion> cbCoordinador;
	@FXML
	private VBox panelPaso2;
	@FXML
	private Label lblEspActual;
	@FXML
	private Label lblContador;
	@FXML
	private TextField txtNombreNum;
	@FXML
	private TextField txtDuracion;
	@FXML
	private TextField txtOrden;
	@FXML
	private ListView<Artista> listArtistas;
	@FXML
	private TableView<Numero> tablaNumeros;
	@FXML
	private TableColumn<Numero, Integer> colOrden;
	@FXML
	private TableColumn<Numero, String> colNombre;
	@FXML
	private TableColumn<Numero, String> colDuracion;
	@FXML
	private TableColumn<Numero, String> colArtistas;
	@FXML
	private Button btnGuardarNumero;
	@FXML
	private ComboBox<Espectaculo> cbEspectaculo;
	@FXML
	private Label lblMensaje;
	/** Espectáculo que se está editando (nunca null en el Paso 2). */
	private Espectaculo espActual = null;

	/** Número cargado en el subformulario para modificar (null = crear nuevo). */
	private Numero numeroEnEdicion = null;

	/** Lista observable de números del espectáculo (mostrada en la tabla). */
	private final ObservableList<Numero> numerosObservable = FXCollections.observableArrayList();
	@Autowired
	private EspectaculoService espectaculoService;
	@Autowired
	private PersonaService personaService;
	@Autowired
	private SesionService sesionService;
	@Lazy
	@Autowired
	private StageManager stageManager;
	// Inicialización

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		configurarCombos();
		configurarTablaNumeros();
		configurarListaArtistas();
		mostrarPaso(1);

		// Si el usuario es Coordinación (no Admin) se fija como coordinador
		if (sesionService.isCoordinacion() && !sesionService.isAdmin()) {
			Long id = sesionService.getUsuarioActual().getPersona().getId();
			personaService.findCoordinacionById(id).ifPresent(coord -> {
				cbCoordinador.getSelectionModel().select(coord);
				cbCoordinador.setDisable(true);
			});
		}

		cbEspectaculo.getSelectionModel().selectedItemProperty().addListener((obs, viejo, nuevo) -> {
			if (nuevo != null)
				cargarEspectaculo(nuevo);
		});
	}
	// Paso 1: datos básicos del espectáculo

	/** Valida y persiste el espectáculo; si todo va bien pasa al Paso 2. */
	@FXML
	private void siguiente(ActionEvent e) {
		try {
			Long idEx = espActual != null ? espActual.getId() : null;
			Long idCoord = cbCoordinador.getValue() != null ? cbCoordinador.getValue().getId() : null;

			espectaculoService.validarDatosEspectaculo(txtNombreEsp.getText().trim(), dpInicio.getValue(),
					dpFin.getValue(), idCoord, idEx);

			espActual = espectaculoService.persistirEspectaculo(txtNombreEsp.getText().trim(), dpInicio.getValue(),
					dpFin.getValue(), idCoord, idEx);

			recargarNumeros();
			lblEspActual.setText("Espectáculo: " + espActual.getNombre());
			mostrarPaso(2);
			modoNuevoNumero();
			actualizarContador();
			ok("Espectáculo guardado. Gestiona sus números a continuación.");
		} catch (IllegalArgumentException ex) {
			error(ex.getMessage());
		}
	}

	/** Limpia el formulario y pone la pantalla en modo creación. */
	@FXML
	private void nuevoEspectaculo(ActionEvent e) {
		espActual = null;
		numerosObservable.clear();
		limpiarPaso1();
		cbEspectaculo.getSelectionModel().clearSelection();
		mostrarPaso(1);
		lblMensaje.setText("");
	}
	// Paso 2: gestión de números

	/** Guarda el número del subformulario (crea o modifica según el modo). */
	@FXML
	private void guardarNumero(ActionEvent e) {
		if (espActual == null) {
			error("Primero guarda el espectáculo.");
			return;
		}

		String nombre = txtNombreNum.getText().trim();
		String durStr = txtDuracion.getText().trim().replace(",", ".");
		String ordenStr = txtOrden.getText().trim();
		List<Artista> sel = listArtistas.getSelectionModel().getSelectedItems();

		if (nombre.isBlank()) {
			error("El nombre del número es obligatorio.");
			return;
		}
		if (durStr.isBlank()) {
			error("La duración es obligatoria (ej: 5,0).");
			return;
		}
		if (ordenStr.isBlank()) {
			error("El orden es obligatorio.");
			return;
		}
		if (sel.isEmpty()) {
			error("Selecciona al menos un artista.");
			return;
		}

		try {
			double dur = Double.parseDouble(durStr);
			int orden = Integer.parseInt(ordenStr);
			Set<Long> idsArts = sel.stream().map(Artista::getId).collect(Collectors.toSet());

			if (numeroEnEdicion == null) {
				espectaculoService.crearNumero(espActual.getId(), nombre, dur, orden, idsArts);
				ok("Número '" + nombre + "' creado con orden " + orden + ".");
			} else {
				espectaculoService.modificarNumero(numeroEnEdicion.getId(), nombre, dur, orden, idsArts);
				ok("Número '" + nombre + "' actualizado.");
			}
			recargarNumeros();
			actualizarContador();
			modoNuevoNumero();
		} catch (NumberFormatException ex) {
			error("Duración u orden inválidos. Usa el formato correcto (ej: 8,5 / 1).");
		} catch (IllegalArgumentException ex) {
			error(ex.getMessage());
		}
	}

	/**
	 * Carga el número seleccionado en la tabla en el subformulario para editarlo.
	 */
	@FXML
	private void editarNumero(ActionEvent e) {
		Numero sel = tablaNumeros.getSelectionModel().getSelectedItem();
		if (sel == null) {
			error("Selecciona un número de la tabla para editar.");
			return;
		}

		espectaculoService.findNumeroByIdConArtistas(sel.getId()).ifPresent(n -> {
			numeroEnEdicion = n;
			txtNombreNum.setText(n.getNombre());
			txtDuracion.setText(n.getDuracionFormateada());
			txtOrden.setText(String.valueOf(n.getOrden()));

			listArtistas.getSelectionModel().clearSelection();
			for (Artista a : listArtistas.getItems()) {
				for (Artista asig : n.getArtistas()) {
					if (asig.getId().equals(a.getId())) {
						listArtistas.getSelectionModel().select(a);
						break;
					}
				}
			}
			btnGuardarNumero.setText("Actualizar número");
			ok("Número '" + n.getNombre() + "' cargado. Modifica y pulsa Actualizar.");
		});
	}

	/** Elimina el número seleccionado (solo si el espectáculo tiene >3). */
	@FXML
	private void eliminarNumero(ActionEvent e) {
		Numero sel = tablaNumeros.getSelectionModel().getSelectedItem();
		if (sel == null) {
			error("Selecciona un número de la tabla para eliminarlo.");
			return;
		}

		Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "¿Eliminar el número '" + sel.getNombre() + "'?",
				ButtonType.YES, ButtonType.NO);
		confirm.setTitle("Confirmar eliminación");
		confirm.showAndWait().ifPresent(btn -> {
			if (btn == ButtonType.YES) {
				try {
					espectaculoService.eliminarNumero(sel.getId());
					recargarNumeros();
					actualizarContador();
					modoNuevoNumero();
					ok("Número '" + sel.getNombre() + "' eliminado.");
				} catch (IllegalArgumentException ex) {
					error(ex.getMessage());
				}
			}
		});
	}

	/** Limpia el subformulario de número para crear uno nuevo. */
	@FXML
	private void nuevoNumero(ActionEvent e) {
		modoNuevoNumero();
	}

	/** Vuelve al Paso 1 sin perder el espectáculo en edición. */
	@FXML
	private void volverPaso1(ActionEvent e) {
		mostrarPaso(1);
	}
	// Helpers de configuración UI

	private void configurarCombos() {
		cbEspectaculo.setItems(FXCollections.observableArrayList(espectaculoService.findAll()));
		cbEspectaculo.setConverter(new StringConverter<Espectaculo>() {
			@Override
			public String toString(Espectaculo e) {
				if (e == null)
					return "";
				return "[" + e.getId() + "] " + e.getNombre() + "  (" + e.getFechaInicio() + " → " + e.getFechaFin()
						+ ")";
			}

			@Override
			public Espectaculo fromString(String s) {
				return null;
			}
		});
		cbEspectaculo.setPromptText("-- Selecciona para modificar --");
		cbCoordinador.setItems(FXCollections.observableArrayList(personaService.findAllCoordinadores()));
		cbCoordinador.setConverter(new StringConverter<Coordinacion>() {
			@Override
			public String toString(Coordinacion c) {
				if (c == null)
					return "";
				return c.getNombre() + (c.isSenior() ? " (Senior)" : "");
			}

			@Override
			public Coordinacion fromString(String s) {
				return null;
			}
		});
		cbCoordinador.setPromptText("-- Selecciona coordinador --");
		cbCoordinador.setButtonCell(crearCeldaCoordinador());
		cbCoordinador.setCellFactory(lv -> crearCeldaCoordinador());
	}

	private void configurarTablaNumeros() {
		colOrden.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getOrden()).asObject());
		colNombre.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombre()));
		colDuracion.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDuracionFormateada() + " min"));
		colArtistas.setCellValueFactory(d -> {
			Set<Artista> arts = d.getValue().getArtistas();
			if (arts == null || arts.isEmpty())
				return new SimpleStringProperty("Sin artistas");
			return new SimpleStringProperty(
					arts.stream().map(Artista::getNombre).sorted().collect(Collectors.joining(", ")));
		});
		tablaNumeros.setItems(numerosObservable);
	}

	private void configurarListaArtistas() {
		listArtistas.setItems(FXCollections.observableArrayList(espectaculoService.findAllArtistas()));
		listArtistas.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		listArtistas.setCellFactory(lv -> new ListCell<Artista>() {
			@Override
			protected void updateItem(Artista a, boolean empty) {
				super.updateItem(a, empty);
				if (empty || a == null) {
					setText(null);
					return;
				}
				String esp = a.getEspecialidades().stream().map(Enum::name).sorted().collect(Collectors.joining(", "));
				String apodo = a.getApodo() != null ? " \"" + a.getApodo() + "\"" : "";
				setText(a.getNombre() + apodo + "  [" + esp + "]");
			}
		});
	}

	private ListCell<Coordinacion> crearCeldaCoordinador() {
		return new ListCell<Coordinacion>() {
			@Override
			protected void updateItem(Coordinacion c, boolean empty) {
				super.updateItem(c, empty);
				if (empty || c == null) {
					setText(null);
					setGraphic(null);
					return;
				}
				if (c.isSenior()) {
					Text nombre = new Text(c.getNombre() + " ");
					Text senior = new Text("(Senior)");
					senior.setFont(Font.font(null, FontWeight.BOLD, 13));
					senior.setFill(javafx.scene.paint.Color.web("#1a7f37"));
					setText(null);
					setGraphic(new TextFlow(nombre, senior));
				} else {
					setText(c.getNombre());
					setGraphic(null);
				}
			}
		};
	}

	private void cargarEspectaculo(Espectaculo esp) {
		espActual = esp;
		numerosObservable.clear();
		txtNombreEsp.setText(esp.getNombre());
		dpInicio.setValue(esp.getFechaInicio());
		dpFin.setValue(esp.getFechaFin());
		cbCoordinador.getSelectionModel().select(esp.getCoordinador());
		mostrarPaso(1);
		ok("Seleccionado: '" + esp.getNombre() + "'. Pulsa Siguiente para editar.");
	}

	private void recargarNumeros() {
		if (espActual == null)
			return;
		List<Numero> nums = espectaculoService.findNumerosPorEspectaculo(espActual.getId());
		numerosObservable.setAll(nums);
	}

	private void modoNuevoNumero() {
		numeroEnEdicion = null;
		txtNombreNum.clear();
		txtDuracion.clear();
		if (espActual != null) {
			int sig = espectaculoService.siguienteOrden(espActual.getId());
			txtOrden.setText(String.valueOf(sig));
		} else {
			txtOrden.clear();
		}
		listArtistas.getSelectionModel().clearSelection();
		if (btnGuardarNumero != null)
			btnGuardarNumero.setText("Añadir número");
	}

	private void actualizarContador() {
		int total = numerosObservable.size();
		int faltan = Math.max(0, 3 - total);
		String texto;
		if (total == 0)
			texto = "Añade al menos 3 números.";
		else if (faltan > 0)
			texto = total + " número(s) — faltan " + faltan + " para el mínimo.";
		else
			texto = total + " número(s). ✓";
		lblContador.setText(texto);
		lblContador.setStyle(total >= 3 ? "-fx-text-fill:#27ae60; -fx-font-weight:bold;"
				: "-fx-text-fill:#e67e22; -fx-font-weight:bold;");
	}

	private void mostrarPaso(int paso) {
		panelPaso1.setVisible(paso == 1);
		panelPaso1.setManaged(paso == 1);
		panelPaso2.setVisible(paso == 2);
		panelPaso2.setManaged(paso == 2);
	}

	private void limpiarPaso1() {
		txtNombreEsp.clear();
		dpInicio.setValue(null);
		dpFin.setValue(null);
		if (!cbCoordinador.isDisabled())
			cbCoordinador.getSelectionModel().clearSelection();
	}

	private void error(String m) {
		lblMensaje.setStyle("-fx-text-fill:red;");
		lblMensaje.setText(m);
		new Alert(Alert.AlertType.WARNING, m, ButtonType.OK).showAndWait();
	}

	private void ok(String m) {
		lblMensaje.setStyle("-fx-text-fill:#27ae60;");
		lblMensaje.setText(m);
	}

	@FXML
	private void volver(ActionEvent e) {
		stageManager.switchScene(FxmlView.MAIN);
	}
}
