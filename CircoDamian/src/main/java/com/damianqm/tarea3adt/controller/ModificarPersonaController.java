package com.damianqm.tarea3adt.controller;

import com.damianqm.tarea3adt.config.StageManager;
import com.damianqm.tarea3adt.modelo.*;
import com.damianqm.tarea3adt.services.PersonaService;
import com.damianqm.tarea3adt.util.PaisesLoader;
import com.damianqm.tarea3adt.view.FxmlView;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;

/** Modificar datos de una persona (CU3C). Solo Admin. */
@Controller
public class ModificarPersonaController implements Initializable {

	@FXML
	private ComboBox<Persona> cbPersona;

	@FXML
	private TextField txtNombre;
	@FXML
	private TextField txtEmail;
	@FXML
	private ComboBox<String> cbNacionalidad;

	@FXML
	private VBox panelArtista;
	@FXML
	private TextField txtApodo;
	@FXML
	private CheckBox chkAcrobacia;
	@FXML
	private CheckBox chkHumor;
	@FXML
	private CheckBox chkMagia;
	@FXML
	private CheckBox chkEquilibrismo;
	@FXML
	private CheckBox chkMalabarismo;

	@FXML
	private VBox panelCoordinacion;
	@FXML
	private CheckBox chkSenior;
	@FXML
	private DatePicker dpFechaSenior;

	@FXML
	private Label lblMensaje;
	@FXML
	private Label lblPerfil;

	@Autowired
	private PersonaService personaService;
	@Autowired
	private PaisesLoader paisesLoader;
	@Lazy
	@Autowired
	private StageManager stageManager;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		cargarPersonas();
		cargarPaises();

		cbPersona.getSelectionModel().selectedItemProperty().addListener((obs, viejo, nueva) -> {
			if (nueva != null)
				cargarDatosPersona(nueva);
		});

		chkSenior.selectedProperty().addListener((obs, viejo, nuevo) -> dpFechaSenior.setDisable(!nuevo));
		dpFechaSenior.setDisable(true);

		// Los paneles específicos arrancan ocultos
		mostrarPanel(panelArtista, false);
		mostrarPanel(panelCoordinacion, false);
	}

	/** Combo de personas: muestra "Nombre [PERFIL]". */
	private void cargarPersonas() {
		List<Persona> personas = personaService.findAllPersonas();
		cbPersona.setItems(FXCollections.observableArrayList(personas));
		cbPersona.setConverter(new StringConverter<Persona>() {
			@Override
			public String toString(Persona p) {
				if (p == null)
					return "";
				Optional<Credenciales> c = personaService.findCredencialesByPersonaId(p.getId());
				if (c.isPresent()) {
					return p.getNombre() + " [" + c.get().getPerfil() + "]";
				}
				return p.getNombre();
			}

			@Override
			public Persona fromString(String s) {
				return null;
			}
		});
	}

	private void cargarPaises() {
		Map<String, String> mapa = paisesLoader.getPaises();
		List<String> codigos = new ArrayList<>(mapa.keySet());
		cbNacionalidad.setItems(FXCollections.observableArrayList(codigos));
		cbNacionalidad.setConverter(new StringConverter<String>() {
			@Override
			public String toString(String codigo) {
				if (codigo == null)
					return "";
				return codigo + " – " + mapa.getOrDefault(codigo, codigo);
			}

			@Override
			public String fromString(String s) {
				if (s == null || !s.contains(" – "))
					return s;
				return s.split(" – ")[0].trim();
			}
		});
	}

	/** Rellena el formulario con los datos de la persona seleccionada. */
	private void cargarDatosPersona(Persona p) {
		txtNombre.setText(p.getNombre());
		txtEmail.setText(p.getEmail());
		cbNacionalidad.getSelectionModel().select(p.getNacionalidad());
		lblMensaje.setText("");

		Optional<Credenciales> credOpt = personaService.findCredencialesByPersonaId(p.getId());
		if (credOpt.isEmpty())
			return;

		Credenciales cred = credOpt.get();
		lblPerfil.setText("Perfil: " + cred.getPerfil());
		boolean esArtista = cred.getPerfil() == Perfil.ARTISTA;
		boolean esCoord = cred.getPerfil() == Perfil.COORDINACION;

		mostrarPanel(panelArtista, esArtista);
		mostrarPanel(panelCoordinacion, esCoord);

		if (esArtista) {
			Optional<Artista> aOpt = personaService.findArtistaById(p.getId());
			if (aOpt.isPresent()) {
				Artista a = aOpt.get();
				txtApodo.setText(a.getApodo() != null ? a.getApodo() : "");
				Set<Especialidad> esp = a.getEspecialidades();
				chkAcrobacia.setSelected(esp.contains(Especialidad.ACROBACIA));
				chkHumor.setSelected(esp.contains(Especialidad.HUMOR));
				chkMagia.setSelected(esp.contains(Especialidad.MAGIA));
				chkEquilibrismo.setSelected(esp.contains(Especialidad.EQUILIBRISMO));
				chkMalabarismo.setSelected(esp.contains(Especialidad.MALABARISMO));
			}
		} else if (esCoord) {
			Optional<Coordinacion> cOpt = personaService.findCoordinacionById(p.getId());
			if (cOpt.isPresent()) {
				Coordinacion c = cOpt.get();
				chkSenior.setSelected(c.isSenior());
				dpFechaSenior.setValue(c.getFechaSenior());
				dpFechaSenior.setDisable(!c.isSenior());
			}
		}
	}

	private void mostrarPanel(VBox panel, boolean visible) {
		panel.setVisible(visible);
		panel.setManaged(visible);
	}

	@FXML
	private void guardar(ActionEvent event) {
		Persona seleccionada = cbPersona.getValue();
		if (seleccionada == null) {
			mostrarError("Selecciona una persona para modificar.");
			return;
		}
		try {
			String codigoNac = cbNacionalidad.getValue();
			if (codigoNac != null && codigoNac.contains(" – ")) {
				codigoNac = codigoNac.split(" – ")[0].trim();
			}

			personaService.modificarDatosPersonales(seleccionada.getId(), txtNombre.getText().trim(),
					txtEmail.getText().trim(), codigoNac);

			// Según el perfil, guardar los datos específicos
			Optional<Credenciales> credOpt = personaService.findCredencialesByPersonaId(seleccionada.getId());
			if (credOpt.isPresent()) {
				Perfil perfil = credOpt.get().getPerfil();
				if (perfil == Perfil.ARTISTA) {
					String apodo = txtApodo.getText().trim();
					personaService.modificarDatosArtista(seleccionada.getId(), apodo.isEmpty() ? null : apodo,
							recogerEspecialidades());
				} else if (perfil == Perfil.COORDINACION) {
					LocalDate fecha = chkSenior.isSelected() ? dpFechaSenior.getValue() : null;
					personaService.modificarDatosCoordinacion(seleccionada.getId(), chkSenior.isSelected(), fecha);
				}
			}

			mostrarExito("Datos de " + seleccionada.getNombre() + " actualizados.");
			cargarPersonas();
		} catch (IllegalArgumentException ex) {
			mostrarError(ex.getMessage());
		}
	}

	private Set<Especialidad> recogerEspecialidades() {
		Set<Especialidad> set = new HashSet<>();
		if (chkAcrobacia.isSelected())
			set.add(Especialidad.ACROBACIA);
		if (chkHumor.isSelected())
			set.add(Especialidad.HUMOR);
		if (chkMagia.isSelected())
			set.add(Especialidad.MAGIA);
		if (chkEquilibrismo.isSelected())
			set.add(Especialidad.EQUILIBRISMO);
		if (chkMalabarismo.isSelected())
			set.add(Especialidad.MALABARISMO);
		return set;
	}

	@FXML
	private void mostrarAyuda(ActionEvent e) {
		Alert a = new Alert(Alert.AlertType.INFORMATION);
		a.setTitle("Ayuda – Modificar Persona");
		a.setHeaderText("¿Cómo modificar una persona?");
		a.setContentText("1. Selecciona la persona en el desplegable.\n" + "2. Edita los campos.\n"
				+ "3. Pulsa 'Guardar cambios'.\n\n" + "Las credenciales no se pueden modificar desde aquí.");
		a.showAndWait();
	}

	@FXML
	private void volver(ActionEvent e) {
		stageManager.switchScene(FxmlView.MAIN);
	}

	private void mostrarError(String msg) {
		lblMensaje.setStyle("-fx-text-fill: red;");
		lblMensaje.setText(msg);
		new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK).showAndWait();
	}

	private void mostrarExito(String msg) {
		lblMensaje.setStyle("-fx-text-fill: green;");
		lblMensaje.setText(msg);
		new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK).showAndWait();
	}
}
