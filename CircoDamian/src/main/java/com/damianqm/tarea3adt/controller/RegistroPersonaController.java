package com.damianqm.tarea3adt.controller;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

import java.net.URL;
import java.time.LocalDate;
import java.util.*;

/**
 * CU3A + CU3B: Registrar una nueva persona (Artista o Coordinación).
 * Solo accesible por Admin.
 */
@Controller
public class RegistroPersonaController implements Initializable {

    @FXML private TextField    txtNombre;
    @FXML private TextField    txtEmail;
    @FXML private ComboBox<String> cbNacionalidad;

    @FXML private RadioButton  rbArtista;
    @FXML private RadioButton  rbCoordinacion;
    @FXML private ToggleGroup  tgPerfil;

    @FXML private TextField    txtApodo;
    @FXML private CheckBox     chkAcrobacia;
    @FXML private CheckBox     chkHumor;
    @FXML private CheckBox     chkMagia;
    @FXML private CheckBox     chkEquilibrismo;
    @FXML private CheckBox     chkMalabarismo;

    @FXML private CheckBox     chkSenior;
    @FXML private DatePicker   dpFechaSenior;

    @FXML private TextField    txtUsuario;
    @FXML private PasswordField pfPassword;
    @FXML private Label        lblMensaje;

    @Autowired private PersonaService personaService;
    @Autowired private PaisesLoader   paisesLoader;
    @Lazy @Autowired private StageManager stageManager;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cargarPaises();
        tgPerfil.selectedToggleProperty().addListener((obs, oldT, newT) -> actualizarPaneles());
        actualizarPaneles();
        chkSenior.selectedProperty().addListener((obs, o, n) -> dpFechaSenior.setDisable(!n));
        dpFechaSenior.setDisable(true);
    }

    private void cargarPaises() {
        Map<String, String> mapa = paisesLoader.getPaises();
        List<String> codigos = new ArrayList<>(mapa.keySet());
        cbNacionalidad.setItems(FXCollections.observableArrayList(codigos));
        cbNacionalidad.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(String codigo) {
                if (codigo == null) return "";
                return codigo + " – " + mapa.getOrDefault(codigo, codigo);
            }
            @Override public String fromString(String s) {
                if (s == null || !s.contains(" – ")) return s;
                return s.split(" – ")[0].trim();
            }
        });
    }

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
            if (rbArtista.isSelected()) registrarArtista();
            else registrarCoordinacion();
        } catch (IllegalArgumentException ex) {
            mostrarError(ex.getMessage());
        }
    }

    private void registrarArtista() {
        Set<Especialidad> especialidades = recogerEspecialidades();
        // La validación de mínimo 1 especialidad la hace PersonaService,
        // pero damos feedback inmediato también aquí:
        if (especialidades.isEmpty()) {
            mostrarError("Selecciona al menos una especialidad para el artista.");
            return;
        }
        String codigoNac = obtenerCodigoNacionalidad();
        personaService.registrarArtista(
                txtNombre.getText().trim(),
                txtEmail.getText().trim(),
                codigoNac,
                txtApodo.getText().trim().isEmpty() ? null : txtApodo.getText().trim(),
                especialidades,
                txtUsuario.getText().trim(),
                pfPassword.getText()
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
        String codigoNac = obtenerCodigoNacionalidad();
        personaService.registrarCoordinacion(
                txtNombre.getText().trim(),
                txtEmail.getText().trim(),
                codigoNac,
                chkSenior.isSelected(),
                fechaSenior,
                txtUsuario.getText().trim(),
                pfPassword.getText()
        );
        mostrarExito("Persona de coordinación registrada correctamente.");
        limpiar();
    }

    /** Extrae el código ISO del ComboBox (el valor puede ser "ES – España" o solo "ES") */
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
        txtNombre.clear(); txtEmail.clear(); cbNacionalidad.getSelectionModel().clearSelection();
        txtApodo.clear(); txtUsuario.clear(); pfPassword.clear();
        chkAcrobacia.setSelected(false); chkHumor.setSelected(false);
        chkMagia.setSelected(false); chkEquilibrismo.setSelected(false);
        chkMalabarismo.setSelected(false); chkSenior.setSelected(false);
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
            "Solo el Administrador puede registrar nuevas personas.\n\n" +
            "1. Rellena nombre, email y selecciona la nacionalidad del desplegable.\n" +
            "2. Elige el perfil: Artista o Coordinación.\n" +
            "   · Artista: apodo opcional y al menos una especialidad.\n" +
            "   · Coordinación: indica si es senior y desde cuándo.\n" +
            "3. Asigna credenciales:\n" +
            "   · Usuario: solo letras minúsculas sin tildes, más de 2 caracteres.\n" +
            "   · Contraseña: sin espacios, más de 2 caracteres.\n" +
            "4. Pulsa 'Registrar'."
        );
        a.showAndWait();
    }

    @FXML private void volver(ActionEvent e) { stageManager.switchScene(FxmlView.MAIN); }

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
