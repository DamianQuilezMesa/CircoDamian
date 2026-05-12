package com.damianqm.tarea3adt.controller;

import com.damianqm.tarea3adt.config.StageManager;
import com.damianqm.tarea3adt.modelo.Artista;
import com.damianqm.tarea3adt.modelo.Espectaculo;
import com.damianqm.tarea3adt.modelo.Numero;
import com.damianqm.tarea3adt.services.EspectaculoService;
import com.damianqm.tarea3adt.services.PersonaService;
import com.damianqm.tarea3adt.view.FxmlView;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Vista auxiliar para consultar y modificar números circenses agrupados por
 * espectáculo (CU5B — edición rápida de artistas/duración/orden).
 * <p>
 * La creación de nuevos números se hace desde
 * {@link GestionEspectaculoController}. Aquí solo se modifican números ya
 * existentes.
 */
@Controller
public class GestionNumeroController implements Initializable {
	@FXML
	private ComboBox<Espectaculo> cbEspectaculo;

	// Selector de número dentro del espectáculo
	@FXML
	private ComboBox<Numero> cbNumero;
	@FXML
	private TextField txtNombre;
	@FXML
	private TextField txtDuracion;
	@FXML
	private TextField txtOrden;
	@FXML
	private ListView<Artista> listArtistas;
	@FXML
	private TableView<Artista> tablaArtistasAsignados;
	@FXML
	private TableColumn<Artista, String> colNombreArtista;
	@FXML
	private TableColumn<Artista, String> colEspecArtista;
	@FXML
	private Button btnGuardar;
	@FXML
	private Label lblMensaje;
	@FXML
	private Label lblTitulo;

	/**
	 * Número en edición (siempre un número existente; null = ninguno seleccionado).
	 */
	private Numero numeroEnEdicion = null;

	@Autowired
	private EspectaculoService espectaculoService;
	@Autowired
	private PersonaService personaService;
	@Lazy
	@Autowired
	private StageManager stageManager;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		configurarComboEspectaculos();
		configurarComboNumeros();
		configurarTablaArtistasAsignados();
		cargarListaArtistas();
		limpiarFormulario();

		cbEspectaculo.getSelectionModel().selectedItemProperty().addListener((obs, viejo, nuevo) -> {
			if (nuevo != null)
				recargarNumerosEspectaculo(nuevo.getId());
			else
				cbNumero.getItems().clear();
			limpiarFormulario();
		});

		cbNumero.getSelectionModel().selectedItemProperty().addListener((obs, viejo, nuevo) -> {
			if (nuevo != null)
				cargarNumero(nuevo);
		});
	}

	/** Guarda los cambios sobre el número en edición. */
	@FXML
	private void guardar(ActionEvent e) {
		if (numeroEnEdicion == null) {
			error("Selecciona un número primero.");
			return;
		}
		if (txtNombre.getText().isBlank()) {
			error("El nombre es obligatorio.");
			return;
		}
		if (txtDuracion.getText().isBlank()) {
			error("La duración es obligatoria (ej: 5,0).");
			return;
		}
		if (txtOrden.getText().isBlank()) {
			error("El orden es obligatorio.");
			return;
		}

		List<Artista> seleccionados = listArtistas.getSelectionModel().getSelectedItems();
		if (seleccionados.isEmpty()) {
			error("Selecciona al menos un artista.");
			return;
		}

		try {
			double dur = Double.parseDouble(txtDuracion.getText().trim().replace(",", "."));
			int orden = Integer.parseInt(txtOrden.getText().trim());
			Set<Long> idsArts = seleccionados.stream().map(Artista::getId).collect(Collectors.toSet());

			espectaculoService.modificarNumero(numeroEnEdicion.getId(), txtNombre.getText().trim(), dur, orden,
					idsArts);

			new Alert(Alert.AlertType.INFORMATION, "Número actualizado correctamente.", ButtonType.OK).showAndWait();

			Espectaculo espSel = cbEspectaculo.getValue();
			if (espSel != null)
				recargarNumerosEspectaculo(espSel.getId());
			limpiarFormulario();
		} catch (NumberFormatException ex) {
			error("Duración u orden inválidos. Usa formato x,0 o x,5 y un entero para el orden.");
		} catch (IllegalArgumentException ex) {
			error(ex.getMessage());
		}
	}

	/** Limpia el formulario para deseleccionar el número. */
	@FXML
	private void limpiar(ActionEvent e) {
		cbNumero.getSelectionModel().clearSelection();
		limpiarFormulario();
	}

	private void cargarNumero(Numero n) {
		espectaculoService.findNumeroByIdConArtistas(n.getId()).ifPresent(completo -> {
			numeroEnEdicion = completo;
			txtNombre.setText(completo.getNombre());
			txtDuracion.setText(completo.getDuracionFormateada());
			txtOrden.setText(String.valueOf(completo.getOrden()));
			tablaArtistasAsignados.setItems(FXCollections.observableArrayList(completo.getArtistas()));

			listArtistas.getSelectionModel().clearSelection();
			for (Artista a : listArtistas.getItems()) {
				for (Artista asig : completo.getArtistas()) {
					if (asig.getId().equals(a.getId())) {
						listArtistas.getSelectionModel().select(a);
						break;
					}
				}
			}
			lblTitulo.setText("Modificando: " + completo.getNombre());
			ok("Número cargado. Modifica los campos y pulsa Guardar.");
		});
	}

	private void limpiarFormulario() {
		numeroEnEdicion = null;
		txtNombre.clear();
		txtDuracion.clear();
		txtOrden.clear();
		listArtistas.getSelectionModel().clearSelection();
		tablaArtistasAsignados.setItems(FXCollections.observableArrayList());
		lblTitulo.setText("Selecciona un espectáculo y un número");
		lblMensaje.setText("");
	}

	private void configurarComboEspectaculos() {
		cbEspectaculo.setItems(FXCollections.observableArrayList(espectaculoService.findAll()));
		cbEspectaculo.setConverter(new StringConverter<Espectaculo>() {
			@Override
			public String toString(Espectaculo e) {
				if (e == null)
					return "";
				return "[" + e.getId() + "] " + e.getNombre();
			}

			@Override
			public Espectaculo fromString(String s) {
				return null;
			}
		});
		cbEspectaculo.setPromptText("-- Selecciona espectáculo --");
	}

	private void configurarComboNumeros() {
		cbNumero.setConverter(new StringConverter<Numero>() {
			@Override
			public String toString(Numero n) {
				if (n == null)
					return "";
				return "[" + n.getOrden() + "] " + n.getNombre() + " (" + n.getDuracionFormateada() + " min)";
			}

			@Override
			public Numero fromString(String s) {
				return null;
			}
		});
		cbNumero.setPromptText("-- Selecciona un número --");
	}

	private void recargarNumerosEspectaculo(Long idEsp) {
		List<Numero> nums = espectaculoService.findNumerosPorEspectaculo(idEsp);
		cbNumero.setItems(FXCollections.observableArrayList(nums));
	}

	private void configurarTablaArtistasAsignados() {
		colNombreArtista.setCellValueFactory(new PropertyValueFactory<>("nombre"));
		colEspecArtista.setCellValueFactory(d -> {
			String esp = d.getValue().getEspecialidades().stream().map(Enum::name).sorted()
					.collect(Collectors.joining(", "));
			return new SimpleStringProperty(esp);
		});
	}

	private void cargarListaArtistas() {
		listArtistas.setItems(FXCollections.observableArrayList(personaService.findAllArtistas()));
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
