package com.damianqm.tarea3adt.controller;

import com.damianqm.tarea3adt.config.StageManager;
import com.damianqm.tarea3adt.modelo.Artista;
import com.damianqm.tarea3adt.modelo.Coordinacion;
import com.damianqm.tarea3adt.modelo.Espectaculo;
import com.damianqm.tarea3adt.modelo.EspectaculoNumero;
import com.damianqm.tarea3adt.modelo.Numero;
import com.damianqm.tarea3adt.services.EspectaculoService;
import com.damianqm.tarea3adt.services.EspectaculoService.NumeroAsignado;
import com.damianqm.tarea3adt.services.PersonaService;
import com.damianqm.tarea3adt.services.SesionService;
import com.damianqm.tarea3adt.view.FxmlView;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Gestión de espectáculos (CU5A). El flujo tiene dos pasos: Paso 1: datos del
 * espectáculo (nombre, fechas, coordinador). Paso 2: asignar números con su
 * orden (mínimo 3).
 */
@Controller
public class GestionEspectaculoController implements Initializable {

	// Paso 1
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

	// Paso 2
	@FXML
	private VBox panelPaso2;
	@FXML
	private Label lblEspActual;
	@FXML
	private Label lblContador;
	@FXML
	private ComboBox<Numero> cbNumero;
	@FXML
	private TextField txtOrden;
	@FXML
	private Button btnAnadir;
	@FXML
	private TableView<EspectaculoNumero> tablaAsignados;
	@FXML
	private TableColumn<EspectaculoNumero, Integer> colOrden;
	@FXML
	private TableColumn<EspectaculoNumero, String> colNombre;
	@FXML
	private TableColumn<EspectaculoNumero, String> colDuracion;
	@FXML
	private TableColumn<EspectaculoNumero, String> colArtistas;
	@FXML
	private Button btnQuitar;
	@FXML
	private Button btnGuardar;

	// Panel izquierdo
	@FXML
	private ComboBox<Espectaculo> cbEspectaculo;
	@FXML
	private Label lblMensaje;

	/** Lista temporal de números asignados mientras el usuario edita. */
	private final ObservableList<EspectaculoNumero> asignados = FXCollections.observableArrayList();

	/** null = creación, no null = modificación. */
	private Espectaculo espEnEdicion = null;

	@Autowired
	private EspectaculoService espectaculoService;
	@Autowired
	private PersonaService personaService;
	@Autowired
	private SesionService sesionService;
	@Lazy
	@Autowired
	private StageManager stageManager;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		configurarCombos();
		configurarTabla();
		mostrarPaso(1);

		// Si el usuario es Coordinación (no Admin), se fija a sí mismo como coordinador
		if (sesionService.isCoordinacion() && !sesionService.isAdmin()) {
			Long id = sesionService.getUsuarioActual().getPersona().getId();
			Optional<Coordinacion> coord = personaService.findCoordinacionById(id);
			if (coord.isPresent()) {
				cbCoordinador.getSelectionModel().select(coord.get());
				cbCoordinador.setDisable(true);
			}
		}

		cbEspectaculo.getSelectionModel().selectedItemProperty().addListener((obs, viejo, nuevo) -> {
			if (nuevo != null)
				cargarEspectaculo(nuevo);
		});

		asignados.addListener((ListChangeListener<EspectaculoNumero>) c -> actualizarContador());
	}

	// ─── Paso 1 ───────────────────────────────────────────────────────

	@FXML
	private void siguiente(ActionEvent e) {
		try {
			Long idEx = espEnEdicion != null ? espEnEdicion.getId() : null;
			Long idCoord = cbCoordinador.getValue() != null ? cbCoordinador.getValue().getId() : null;

			espectaculoService.validarDatosEspectaculo(txtNombreEsp.getText().trim(), dpInicio.getValue(),
					dpFin.getValue(), idCoord, idEx);

			// Al modificar, cargar los números existentes una sola vez
			if (espEnEdicion != null && asignados.isEmpty()) {
				cargarNumerosExistentes(espEnEdicion);
			}
			String prefijo = espEnEdicion != null ? "Modificando: " : "Nuevo: ";
			lblEspActual.setText(prefijo + txtNombreEsp.getText().trim());
			mostrarPaso(2);
			actualizarContador();
		} catch (IllegalArgumentException ex) {
			error(ex.getMessage());
		}
	}

	@FXML
	private void nuevoEspectaculo(ActionEvent e) {
		espEnEdicion = null;
		asignados.clear();
		limpiarPaso1();
		cbEspectaculo.getSelectionModel().clearSelection();
		mostrarPaso(1);
		lblMensaje.setText("");
	}

	// ─── Paso 2 ───────────────────────────────────────────────────────

	@FXML
	private void anadirNumero(ActionEvent e) {
		Numero n = cbNumero.getValue();
		if (n == null) {
			error("Selecciona un número del desplegable.");
			return;
		}
		if (txtOrden.getText().isBlank()) {
			error("Indica el orden para este número.");
			return;
		}
		try {
			int orden = Integer.parseInt(txtOrden.getText().trim());
			if (orden <= 0) {
				error("El orden debe ser mayor o igual a 1.");
				return;
			}
			for (EspectaculoNumero a : asignados) {
				if (a.getOrden() == orden) {
					error("Ya hay un número con el orden " + orden + ".");
					return;
				}
				if (a.getNumero().getId().equals(n.getId())) {
					error("El número '" + n.getNombre() + "' ya está en la lista.");
					return;
				}
			}
			asignados.add(new EspectaculoNumero(null, n, orden));
			FXCollections.sort(asignados, Comparator.comparingInt(EspectaculoNumero::getOrden));
			txtOrden.clear();
			cbNumero.getSelectionModel().clearSelection();
			ok("Número '" + n.getNombre() + "' añadido con orden " + orden + ".");
		} catch (NumberFormatException ex) {
			error("El orden debe ser un número entero.");
		}
	}

	@FXML
	private void quitarNumero(ActionEvent e) {
		EspectaculoNumero sel = tablaAsignados.getSelectionModel().getSelectedItem();
		if (sel == null) {
			error("Selecciona un número de la tabla para quitarlo.");
			return;
		}
		asignados.remove(sel);
		ok("Número '" + sel.getNumero().getNombre() + "' quitado de la lista.");
	}

	@FXML
	private void volverPaso1(ActionEvent e) {
		mostrarPaso(1);
	}

	// ─── Guardar / Eliminar ──────────────────────────────────────────

	@FXML
	private void guardar(ActionEvent e) {
		try {
			List<NumeroAsignado> lista = new ArrayList<>();
			for (EspectaculoNumero en : asignados) {
				lista.add(new NumeroAsignado(en.getNumero().getId(), en.getOrden()));
			}
			Long idExistente = espEnEdicion != null ? espEnEdicion.getId() : null;

			Espectaculo guardado = espectaculoService.persistirEspectaculoCompleto(txtNombreEsp.getText().trim(),
					dpInicio.getValue(), dpFin.getValue(), cbCoordinador.getValue().getId(), lista, idExistente);

			new Alert(Alert.AlertType.INFORMATION,
					"Espectáculo '" + guardado.getNombre() + "' guardado con " + asignados.size() + " números.",
					ButtonType.OK).showAndWait();
			reset();
		} catch (IllegalArgumentException ex) {
			error(ex.getMessage());
		}
	}

	@FXML
	private void eliminarEspectaculo(ActionEvent e) {
		Espectaculo sel = cbEspectaculo.getValue();
		if (sel == null) {
			error("Selecciona un espectáculo para eliminarlo.");
			return;
		}
		Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
				"¿Eliminar '" + sel.getNombre() + "'?\n"
						+ "Los números NO se eliminarán, solo la relación con este espectáculo.",
				ButtonType.YES, ButtonType.NO);
		confirm.setTitle("Confirmar eliminación");
		Optional<ButtonType> resp = confirm.showAndWait();
		if (resp.isPresent() && resp.get() == ButtonType.YES) {
			try {
				espectaculoService.eliminarEspectaculo(sel.getId());
				new Alert(Alert.AlertType.INFORMATION, "Espectáculo eliminado.", ButtonType.OK).showAndWait();
				reset();
			} catch (IllegalArgumentException ex) {
				error(ex.getMessage());
			}
		}
	}

	// ─── Configuración de UI ─────────────────────────────────────────

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
		cbEspectaculo.setPromptText("-- Selecciona para modificar/eliminar --");

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
		// Celda personalizada para mostrar "(Senior)" en negrita verde
		cbCoordinador.setButtonCell(crearCeldaCoordinador());
		cbCoordinador.setCellFactory(lv -> crearCeldaCoordinador());

		cbNumero.setItems(FXCollections.observableArrayList(espectaculoService.findAllNumeros()));
		cbNumero.setConverter(new StringConverter<Numero>() {
			@Override
			public String toString(Numero n) {
				if (n == null)
					return "";
				String arts;
				if (n.getArtistas().isEmpty()) {
					arts = "sin artistas";
				} else {
					arts = n.getArtistas().stream().map(Artista::getNombre).sorted().collect(Collectors.joining(", "));
				}
				return "[" + n.getId() + "] " + n.getNombre() + " (" + n.getDuracionFormateada() + " min) — " + arts;
			}

			@Override
			public Numero fromString(String s) {
				return null;
			}
		});
		cbNumero.setPromptText("-- Selecciona un número --");
	}

	private void configurarTabla() {
		colOrden.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getOrden()).asObject());
		colNombre.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNumero().getNombre()));
		colDuracion.setCellValueFactory(
				d -> new SimpleStringProperty(d.getValue().getNumero().getDuracionFormateada() + " min"));
		colArtistas.setCellValueFactory(d -> {
			Set<Artista> arts = d.getValue().getNumero().getArtistas();
			if (arts == null || arts.isEmpty()) {
				return new SimpleStringProperty("Sin artistas");
			}
			String texto = arts.stream().map(Artista::getNombre).sorted().collect(Collectors.joining(", "));
			return new SimpleStringProperty(texto);
		});
		tablaAsignados.setItems(asignados);
	}

	/**
	 * Celda para el combo de coordinadores: muestra "(Senior)" en negrita verde.
	 */
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
		espEnEdicion = esp;
		asignados.clear();
		txtNombreEsp.setText(esp.getNombre());
		dpInicio.setValue(esp.getFechaInicio());
		dpFin.setValue(esp.getFechaFin());
		cbCoordinador.getSelectionModel().select(esp.getCoordinador());
		mostrarPaso(1);
		ok("Seleccionado: '" + esp.getNombre() + "'. Pulsa Siguiente para editar.");
	}

	/**
	 * Carga los números del espectáculo en edición para precargarlos en la tabla.
	 */
	private void cargarNumerosExistentes(Espectaculo esp) {
		Optional<Espectaculo> completo = espectaculoService.findByIdCompleto(esp.getId());
		if (completo.isEmpty())
			return;

		List<EspectaculoNumero> relaciones = completo.get().getNumerosEnEspectaculo().stream()
				.sorted(Comparator.comparingInt(EspectaculoNumero::getOrden)).collect(Collectors.toList());
		for (EspectaculoNumero en : relaciones) {
			asignados.add(new EspectaculoNumero(completo.get(), en.getNumero(), en.getOrden()));
		}
	}

	// ─── Utilidades de UI ────────────────────────────────────────────

	private void mostrarPaso(int paso) {
		panelPaso1.setVisible(paso == 1);
		panelPaso1.setManaged(paso == 1);
		panelPaso2.setVisible(paso == 2);
		panelPaso2.setManaged(paso == 2);
	}

	private void actualizarContador() {
		int total = asignados.size();
		int faltan = Math.max(0, 3 - total);
		String texto;
		if (total == 0) {
			texto = "Añade al menos 3 números.";
		} else if (faltan > 0) {
			texto = total + " número(s) — faltan " + faltan + " para el mínimo.";
		} else {
			texto = total + " número(s). Listo para guardar.";
		}
		lblContador.setText(texto);
		if (total >= 3) {
			lblContador.setStyle("-fx-text-fill:#27ae60; -fx-font-weight:bold;");
		} else {
			lblContador.setStyle("-fx-text-fill:#e67e22; -fx-font-weight:bold;");
		}
		btnGuardar.setDisable(total < 3);
	}

	private void reset() {
		espEnEdicion = null;
		asignados.clear();
		limpiarPaso1();
		cbEspectaculo.setItems(FXCollections.observableArrayList(espectaculoService.findAll()));
		cbNumero.setItems(FXCollections.observableArrayList(espectaculoService.findAllNumeros()));
		cbEspectaculo.getSelectionModel().clearSelection();
		mostrarPaso(1);
		lblMensaje.setText("");
	}

	private void limpiarPaso1() {
		txtNombreEsp.clear();
		dpInicio.setValue(null);
		dpFin.setValue(null);
		if (!cbCoordinador.isDisabled()) {
			cbCoordinador.getSelectionModel().clearSelection();
		}
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
	private void mostrarAyuda(ActionEvent e) {
		Alert a = new Alert(Alert.AlertType.INFORMATION);
		a.setTitle("Ayuda – Gestión de Espectáculos");
		a.setHeaderText("Cómo gestionar espectáculos");
		a.setContentText("CREAR:\n" + "  Pulsa 'Nuevo espectáculo', rellena el Paso 1 y pulsa Siguiente.\n"
				+ "  En el Paso 2, elige números, asigna orden y pulsa Añadir.\n"
				+ "  Con 3 o más números puedes guardar.\n\n" + "MODIFICAR:\n"
				+ "  Selecciona un espectáculo del desplegable izquierdo.\n"
				+ "  Sus datos se cargan automáticamente.\n\n" + "ELIMINAR:\n"
				+ "  Selecciona el espectáculo y pulsa Eliminar.\n" + "  Los números NO se eliminan del sistema.\n\n"
				+ "Para crear/modificar números usa 'Gestionar Números'.");
		a.showAndWait();
	}

	@FXML
	private void volver(ActionEvent e) {
		stageManager.switchScene(FxmlView.MAIN);
	}

	@FXML
	private void irGestionNumeros(ActionEvent e) {
		stageManager.switchScene(FxmlView.GESTION_NUMERO);
	}
}
