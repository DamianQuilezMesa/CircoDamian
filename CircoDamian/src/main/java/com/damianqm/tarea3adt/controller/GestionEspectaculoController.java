package com.damianqm.tarea3adt.controller;

import com.damianqm.tarea3adt.config.StageManager;
import com.damianqm.tarea3adt.dto.EspectaculoBorrador;
import com.damianqm.tarea3adt.dto.NumeroBorrador;
import com.damianqm.tarea3adt.modelo.Artista;
import com.damianqm.tarea3adt.modelo.Coordinacion;
import com.damianqm.tarea3adt.modelo.Espectaculo;
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
 * NUEVO FLUJO: el espectáculo y sus números se montan en memoria
 * (EspectaculoBorrador) y NADA se persiste hasta pulsar "Guardar espectáculo".
 * En ese momento el servicio valida el conjunto completo (datos básicos +
 * mínimo 3 números + órdenes únicos + cada número con artista y duración
 * válida) y lo persiste todo en una sola transacción. Si algo falla, no se
 * guarda nada.
 * <p>
 * Editar un espectáculo existente lo carga a un borrador, permite cambios y
 * vuelve a validar el conjunto completo antes de re-persistir.
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
	private TableView<NumeroBorrador> tablaNumeros;
	@FXML
	private TableColumn<NumeroBorrador, Integer> colOrden;
	@FXML
	private TableColumn<NumeroBorrador, String> colNombre;
	@FXML
	private TableColumn<NumeroBorrador, String> colDuracion;
	@FXML
	private TableColumn<NumeroBorrador, String> colArtistas;
	@FXML
	private Button btnGuardarNumero;
	@FXML
	private ComboBox<Espectaculo> cbEspectaculo;
	@FXML
	private Label lblMensaje;

	/** Borrador en memoria del espectáculo en construcción/edición. */
	private EspectaculoBorrador borrador = new EspectaculoBorrador();

	/** Número del borrador cargado en el subformulario (null = crear nuevo). */
	private NumeroBorrador numeroEnEdicion = null;

	/** Lista observable de números del borrador (mostrada en la tabla). */
	private final ObservableList<NumeroBorrador> numerosObservable = FXCollections.observableArrayList();

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
		configurarTablaNumeros();
		configurarListaArtistas();
		mostrarPaso(1);

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

	// PASO 1: datos básicos

	/**
	 * Pasa al Paso 2 guardando los datos básicos en el borrador (sin persistir).
	 */
	@FXML
	private void siguiente(ActionEvent e) {
		try {
			Long idCoord = cbCoordinador.getValue() != null ? cbCoordinador.getValue().getId() : null;
			// Validación de datos básicos contra BD (nombre único, fechas, coordinador)
			espectaculoService.validarDatosEspectaculo(txtNombreEsp.getText().trim(), dpInicio.getValue(),
					dpFin.getValue(), idCoord, borrador.getId());

			// Guardar en el borrador en memoria (NO en BD)
			borrador.setNombre(txtNombreEsp.getText().trim());
			borrador.setFechaInicio(dpInicio.getValue());
			borrador.setFechaFin(dpFin.getValue());
			borrador.setCoordinador(cbCoordinador.getValue());

			lblEspActual.setText(
					"Espectáculo: " + borrador.getNombre() + (borrador.esNuevo() ? "  (sin guardar)" : "  (editando)"));
			mostrarPaso(2);
			modoNuevoNumero();
			actualizarContador();
			ok("Datos básicos listos. Añade los números (mínimo 3) y pulsa «Guardar espectáculo».");
		} catch (IllegalArgumentException ex) {
			error(ex.getMessage());
		}
	}

	/** Limpia todo y empieza un espectáculo nuevo en memoria. */
	@FXML
	private void nuevoEspectaculo(ActionEvent e) {
		borrador = new EspectaculoBorrador();
		numerosObservable.clear();
		limpiarPaso1();
		cbEspectaculo.getSelectionModel().clearSelection();
		mostrarPaso(1);
		lblMensaje.setText("");
	}

	// PASO 2: números en memoria

	/** Añade o actualiza un número EN EL BORRADOR (en memoria, sin tocar BD). */
	@FXML
	private void guardarNumero(ActionEvent e) {
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

			// Validar duración x,0 / x,5
			double dec = dur - Math.floor(dur);
			if (dur <= 0 || (Math.abs(dec) > 0.01 && Math.abs(dec - 0.5) > 0.01)) {
				error("La duración solo admite x,0 o x,5 (ej: 3,0 o 2,5).");
				return;
			}

			// Comprobar orden único dentro del borrador
			for (NumeroBorrador nb : borrador.getNumeros()) {
				if (nb != numeroEnEdicion && nb.getOrden() == orden) {
					error("Ya existe un número con el orden " + orden + " en este espectáculo.");
					return;
				}
			}

			Set<Artista> artistas = new HashSet<>(sel);

			if (numeroEnEdicion == null) {
				// Nuevo número en el borrador
				NumeroBorrador nb = new NumeroBorrador(null, nombre, dur, orden, artistas);
				borrador.getNumeros().add(nb);
				ok("Número '" + nombre + "' añadido (orden " + orden + "). Sin guardar todavía.");
			} else {
				// Actualizar el número en edición
				numeroEnEdicion.setNombre(nombre);
				numeroEnEdicion.setDuracion(dur);
				numeroEnEdicion.setOrden(orden);
				numeroEnEdicion.setArtistas(artistas);
				ok("Número '" + nombre + "' actualizado en memoria.");
			}
			recargarNumeros();
			actualizarContador();
			modoNuevoNumero();
		} catch (NumberFormatException ex) {
			error("Duración u orden inválidos. Usa el formato correcto (ej: 8,5 / 1).");
		}
	}

	/** Carga un número del borrador en el subformulario para editarlo. */
	@FXML
	private void editarNumero(ActionEvent e) {
		NumeroBorrador sel = tablaNumeros.getSelectionModel().getSelectedItem();
		if (sel == null) {
			error("Selecciona un número de la tabla para editar.");
			return;
		}

		numeroEnEdicion = sel;
		txtNombreNum.setText(sel.getNombre());
		txtDuracion.setText(sel.getDuracionFormateada());
		txtOrden.setText(String.valueOf(sel.getOrden()));

		listArtistas.getSelectionModel().clearSelection();
		for (Artista a : listArtistas.getItems()) {
			for (Artista asig : sel.getArtistas()) {
				if (asig.getId().equals(a.getId())) {
					listArtistas.getSelectionModel().select(a);
					break;
				}
			}
		}
		btnGuardarNumero.setText("Actualizar número");
		ok("Número '" + sel.getNombre() + "' cargado. Modifica y pulsa Actualizar.");
	}

	/** Elimina un número del borrador (en memoria). */
	@FXML
	private void eliminarNumero(ActionEvent e) {
		NumeroBorrador sel = tablaNumeros.getSelectionModel().getSelectedItem();
		if (sel == null) {
			error("Selecciona un número de la tabla para eliminarlo.");
			return;
		}

		Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "¿Eliminar el número '" + sel.getNombre() + "'?",
				ButtonType.YES, ButtonType.NO);
		confirm.setTitle("Confirmar eliminación");
		confirm.showAndWait().ifPresent(btn -> {
			if (btn == ButtonType.YES) {
				borrador.getNumeros().remove(sel);
				recargarNumeros();
				actualizarContador();
				modoNuevoNumero();
				ok("Número '" + sel.getNombre() + "' eliminado del borrador.");
			}
		});
	}

	/** Limpia el subformulario de número. */
	@FXML
	private void nuevoNumero(ActionEvent e) {
		modoNuevoNumero();
	}

	/** Vuelve al Paso 1 conservando el borrador. */
	@FXML
	private void volverPaso1(ActionEvent e) {
		mostrarPaso(1);
	}

	/**
	 * GUARDA EL ESPECTÁCULO COMPLETO: valida todo el conjunto y persiste en una
	 * sola transacción. Solo aquí se toca la base de datos.
	 */
	@FXML
	private void guardarEspectaculo(ActionEvent e) {
		try {
			Espectaculo guardado = espectaculoService.guardarEspectaculoCompleto(borrador);

			// Recargar el combo de espectáculos y dejar el borrador sincronizado
			refrescarComboEspectaculos();
			borrador = espectaculoService.cargarBorrador(guardado.getId());
			recargarNumeros();
			actualizarContador();
			lblEspActual.setText("Espectáculo: " + borrador.getNombre() + "  (guardado)");

			Alert info = new Alert(
					Alert.AlertType.INFORMATION, "Espectáculo '" + guardado.getNombre()
							+ "' guardado correctamente con " + guardado.getNumeros().size() + " números.",
					ButtonType.OK);
			info.setTitle("Guardado correcto");
			info.showAndWait();
			ok("Espectáculo guardado correctamente.");
		} catch (IllegalArgumentException ex) {
			error(ex.getMessage());
		}
	}

	// Helpers UI

	private void configurarCombos() {
		refrescarComboEspectaculos();
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

	private void refrescarComboEspectaculos() {
		cbEspectaculo.setItems(FXCollections.observableArrayList(espectaculoService.findAll()));
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

	/** Carga un espectáculo existente a borrador para editarlo. */
	private void cargarEspectaculo(Espectaculo esp) {
		borrador = espectaculoService.cargarBorrador(esp.getId());
		numerosObservable.setAll(borrador.getNumeros());
		txtNombreEsp.setText(borrador.getNombre());
		dpInicio.setValue(borrador.getFechaInicio());
		dpFin.setValue(borrador.getFechaFin());
		if (!cbCoordinador.isDisabled())
			cbCoordinador.getSelectionModel().select(borrador.getCoordinador());
		mostrarPaso(1);
		ok("Seleccionado: '" + esp.getNombre() + "'. Pulsa Siguiente para editar sus datos y números.");
	}

	private void recargarNumeros() {
		numerosObservable.setAll(borrador.getNumeros());
		numerosObservable.sort(Comparator.comparingInt(NumeroBorrador::getOrden));
	}

	private void modoNuevoNumero() {
		numeroEnEdicion = null;
		txtNombreNum.clear();
		txtDuracion.clear();
		txtOrden.setText(String.valueOf(borrador.siguienteOrden()));
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
			texto = total + " número(s). ✓ Listo para guardar.";
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

	/**
	 * Volver al menú. Como nada se persiste hasta "Guardar espectáculo", si hay
	 * cambios sin guardar simplemente se descartan (avisando al usuario).
	 */
	@FXML
	private void volver(ActionEvent e) {
		stageManager.switchScene(FxmlView.MAIN);
	}
}
