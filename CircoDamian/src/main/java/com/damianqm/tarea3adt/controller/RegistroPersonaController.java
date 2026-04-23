package com.damianqm.tarea3adt.controller;

import com.damianqm.tarea3adt.componentes.CampoPassword;
import com.damianqm.tarea3adt.config.StageManager;
import com.damianqm.tarea3adt.modelo.Especialidad;
import com.damianqm.tarea3adt.services.PersonaService;
import com.damianqm.tarea3adt.util.PaisesLoader;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

/** Registro de nueva persona (CU3A + CU3B). Solo Admin. */
@Controller
public class RegistroPersonaController implements Initializable {

    @FXML private TextField txtNombre;
    @FXML private TextField txtEmail;
    @FXML private ComboBox<String> cbNacionalidad;

    @FXML private RadioButton rbArtista;
    @FXML private RadioButton rbCoordinacion;
    @FXML private ToggleGroup tgPerfil;

    @FXML private TextField txtApodo;
    @FXML private CheckBox chkAcrobacia;
    @FXML private CheckBox chkHumor;
    @FXML private CheckBox chkMagia;
    @FXML private CheckBox chkEquilibrismo;
    @FXML private CheckBox chkMalabarismo;

    @FXML private CheckBox chkSenior;
    @FXML private DatePicker dpFechaSenior;

    @FXML private TextField txtUsuario;
    @FXML private CampoPassword cpPassword;

    @FXML private Label lblMensaje;

    @Autowired private PersonaService personaService;
    @Autowired private PaisesLoader paisesLoader;
    @Lazy @Autowired private StageManager stageManager;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cargarPaises();
        tgPerfil.selectedToggleProperty().addListener((obs, viejo, nuevo) -> actualizarPaneles());
        actualizarPaneles();

        // El DatePicker solo se habilita si se marca "senior"
        chkSenior.selectedProperty().addListener((obs, viejo, nuevo) -> dpFechaSenior.setDisable(!nuevo));
        dpFechaSenior.setDisable(true);
    }

    private void cargarPaises() {
        Map<String, String> mapa = paisesLoader.getPaises();
        List<String> codigos = new ArrayList<>(mapa.keySet());
        cbNacionalidad.setItems(FXCollections.observableArrayList(codigos));
        cbNacionalidad.setConverter(new StringConverter<String>() {
            @Override
            public String toString(String codigo) {
                if (codigo == null) return "";
                return codigo + " – " + mapa.getOrDefault(codigo, codigo);
            }
            @Override
            public String fromString(String s) {
                if (s == null || !s.contains(" – ")) return s;
                return s.split(" – ")[0].trim();
            }
        });
    }

    /** Habilita los campos del perfil seleccionado (artista o coordinación). */
    private void actualizarPaneles() {
        boolean esArtista = rbArtista.isSelected();
        txtApodo.setDisable(!esArtista);
        chkAcrobacia.setDisable(!esArtista);
        chkHumor.setDisable(!esArtista);
        chkMagia.setDisable(!esArtista);
        chkEquilibrismo.setDisable(!esArtista);
        chkMalabarismo.setDisable(!esArtista);
        chkSenior.setDisable(esArtista);
        dpFechaSenior.setDisable(true);
    }

    @FXML
    private void registrar(ActionEvent event) {
        try {
            if (rbArtista.isSelected()) {
                registrarArtista();
            } else {
                registrarCoordinacion();
            }
        } catch (IllegalArgumentException ex) {
            mostrarError(ex.getMessage());
        }
    }

    private void registrarArtista() {
        Set<Especialidad> especialidades = recogerEspecialidades();
        if (especialidades.isEmpty()) {
            mostrarError("Selecciona al menos una especialidad para el artista.");
            return;
        }
        String apodo = txtApodo.getText().trim();
        personaService.registrarArtista(
                txtNombre.getText().trim(),
                txtEmail.getText().trim(),
                obtenerCodigoNacionalidad(),
                apodo.isEmpty() ? null : apodo,
                especialidades,
                txtUsuario.getText().trim(),
                cpPassword.getText()
        );
        mostrarExito("Artista registrado correctamente.");
        limpiar();
    }

    private void registrarCoordinacion() {
        LocalDate fechaSenior = chkSenior.isSelected() ? dpFechaSenior.getValue() : null;
        if (chkSenior.isSelected() && fechaSenior == null) {
            mostrarError("Indica la fecha desde la que es senior.");
            return;
        }
        personaService.registrarCoordinacion(
                txtNombre.getText().trim(),
                txtEmail.getText().trim(),
                obtenerCodigoNacionalidad(),
                chkSenior.isSelected(),
                fechaSenior,
                txtUsuario.getText().trim(),
                cpPassword.getText()
        );
        mostrarExito("Persona de coordinación registrada correctamente.");
        limpiar();
    }

    /** Extrae solo el código ISO del combo de países ("ES – España" → "ES"). */
    private String obtenerCodigoNacionalidad() {
        String val = cbNacionalidad.getValue();
        if (val == null) return "";
        if (val.contains(" – ")) return val.split(" – ")[0].trim();
        return val.trim();
    }

    private Set<Especialidad> recogerEspecialidades() {
        Set<Especialidad> set = new HashSet<>();
        if (chkAcrobacia.isSelected())    set.add(Especialidad.ACROBACIA);
        if (chkHumor.isSelected())        set.add(Especialidad.HUMOR);
        if (chkMagia.isSelected())        set.add(Especialidad.MAGIA);
        if (chkEquilibrismo.isSelected()) set.add(Especialidad.EQUILIBRISMO);
        if (chkMalabarismo.isSelected())  set.add(Especialidad.MALABARISMO);
        return set;
    }

    private void limpiar() {
        txtNombre.clear();
        txtEmail.clear();
        cbNacionalidad.getSelectionModel().clearSelection();
        txtApodo.clear();
        txtUsuario.clear();
        cpPassword.clear();
        chkAcrobacia.setSelected(false);
        chkHumor.setSelected(false);
        chkMagia.setSelected(false);
        chkEquilibrismo.setSelected(false);
        chkMalabarismo.setSelected(false);
        chkSenior.setSelected(false);
        dpFechaSenior.setValue(null);
        rbArtista.setSelected(true);
        lblMensaje.setText("");
    }

    @FXML
    private void mostrarAyuda(ActionEvent e) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Ayuda – Registrar Persona");
        a.setHeaderText("¿Cómo registrar una persona?");
        a.setContentText(
            "Solo el Administrador puede registrar personas.\n\n" +
            "1. Rellena nombre, email y nacionalidad.\n" +
            "2. Elige perfil: Artista o Coordinación.\n" +
            "3. Rellena los datos específicos (apodo y especialidades, o senior/fecha).\n" +
            "4. Asigna usuario (letras minúsculas, >2 chars) y contraseña (sin espacios, >2 chars).\n" +
            "5. Pulsa 'Registrar'."
        );
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
