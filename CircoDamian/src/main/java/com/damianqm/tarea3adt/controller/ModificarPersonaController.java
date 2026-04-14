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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

import java.net.URL;
import java.time.LocalDate;
import java.util.*;

/**
 * CU3C: Modificar datos personales y profesionales de personas del circo.
 * Solo accesible por Admin.
 */
@Controller
public class ModificarPersonaController implements Initializable {

    // ── Selección de persona ──────────────────────────────────────────
    @FXML private ComboBox<Persona> cbPersona;

    // ── Datos personales ──────────────────────────────────────────────
    @FXML private TextField txtNombre;
    @FXML private TextField txtEmail;
    @FXML private ComboBox<String> cbNacionalidad;

    // ── Sección artista ───────────────────────────────────────────────
    @FXML private VBox panelArtista;
    @FXML private TextField txtApodo;
    @FXML private CheckBox chkAcrobacia;
    @FXML private CheckBox chkHumor;
    @FXML private CheckBox chkMagia;
    @FXML private CheckBox chkEquilibrismo;
    @FXML private CheckBox chkMalabarismo;

    // ── Sección coordinación ──────────────────────────────────────────
    @FXML private VBox panelCoordinacion;
    @FXML private CheckBox chkSenior;
    @FXML private DatePicker dpFechaSenior;

    // ── Feedback ──────────────────────────────────────────────────────
    @FXML private Label lblMensaje;
    @FXML private Label lblPerfil;

    @Autowired private PersonaService personaService;
    @Autowired private PaisesLoader   paisesLoader;
    @Lazy @Autowired private StageManager stageManager;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cargarPersonas();
        cargarPaises();

        cbPersona.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, nueva) -> { if (nueva != null) cargarDatosPersona(nueva); });

        chkSenior.selectedProperty().addListener(
                (obs, o, n) -> dpFechaSenior.setDisable(!n));
        dpFechaSenior.setDisable(true);

        panelArtista.setVisible(false);
        panelArtista.setManaged(false);
        panelCoordinacion.setVisible(false);
        panelCoordinacion.setManaged(false);
    }

    // ─── Carga inicial ────────────────────────────────────────────────

    private void cargarPersonas() {
        List<Persona> personas = personaService.findAllPersonas();
        // Excluir admin (id=1 con perfil fijo)
        cbPersona.setItems(FXCollections.observableArrayList(personas));
        cbPersona.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(Persona p) {
                if (p == null) return "";
                // Buscar su perfil a través de credenciales
                return personaService.findCredencialesByPersonaId(p.getId())
                        .map(c -> p.getNombre() + " [" + c.getPerfil() + "]")
                        .orElse(p.getNombre());
            }
            @Override public Persona fromString(String s) { return null; }
        });
    }

    private void cargarPaises() {
        Map<String, String> mapa = paisesLoader.getPaises();
        // Mostrar "ES – España" en el combo
        List<String> codigos = new ArrayList<>(mapa.keySet());
        cbNacionalidad.setItems(FXCollections.observableArrayList(codigos));
        cbNacionalidad.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(String codigo) {
                if (codigo == null) return "";
                String nombre = mapa.getOrDefault(codigo, codigo);
                return codigo + " – " + nombre;
            }
            @Override public String fromString(String s) {
                if (s == null || !s.contains(" – ")) return s;
                return s.split(" – ")[0].trim();
            }
        });
    }

    // ─── Carga de datos de la persona seleccionada ───────────────────

    private void cargarDatosPersona(Persona p) {
        txtNombre.setText(p.getNombre());
        txtEmail.setText(p.getEmail());
        cbNacionalidad.getSelectionModel().select(p.getNacionalidad());
        lblMensaje.setText("");

        // Determinar perfil
        personaService.findCredencialesByPersonaId(p.getId()).ifPresent(c -> {
            lblPerfil.setText("Perfil: " + c.getPerfil());
            boolean esArtista = c.getPerfil() == Perfil.ARTISTA;
            boolean esCoord   = c.getPerfil() == Perfil.COORDINACION;

            mostrarPanel(panelArtista, esArtista);
            mostrarPanel(panelCoordinacion, esCoord);

            if (esArtista) {
                personaService.findArtistaById(p.getId()).ifPresent(a -> {
                    txtApodo.setText(a.getApodo() != null ? a.getApodo() : "");
                    Set<Especialidad> esp = a.getEspecialidades();
                    chkAcrobacia.setSelected(esp.contains(Especialidad.ACROBACIA));
                    chkHumor.setSelected(esp.contains(Especialidad.HUMOR));
                    chkMagia.setSelected(esp.contains(Especialidad.MAGIA));
                    chkEquilibrismo.setSelected(esp.contains(Especialidad.EQUILIBRISMO));
                    chkMalabarismo.setSelected(esp.contains(Especialidad.MALABARISMO));
                });
            } else if (esCoord) {
                personaService.findCoordinacionById(p.getId()).ifPresent(coord -> {
                    chkSenior.setSelected(coord.isSenior());
                    dpFechaSenior.setValue(coord.getFechaSenior());
                    dpFechaSenior.setDisable(!coord.isSenior());
                });
            }
        });
    }

    private void mostrarPanel(VBox panel, boolean visible) {
        panel.setVisible(visible);
        panel.setManaged(visible);
    }

    // ─── Guardar cambios ──────────────────────────────────────────────

    @FXML
    private void guardar(ActionEvent event) {
        Persona seleccionada = cbPersona.getValue();
        if (seleccionada == null) {
            mostrarError("Selecciona una persona para modificar.");
            return;
        }
        try {
            // Obtener código ISO del combo de países
            String codigoNac = cbNacionalidad.getValue();
            if (codigoNac != null && codigoNac.contains(" – "))
                codigoNac = codigoNac.split(" – ")[0].trim();

            // 1. Datos personales
            personaService.modificarDatosPersonales(
                    seleccionada.getId(),
                    txtNombre.getText().trim(),
                    txtEmail.getText().trim(),
                    codigoNac
            );

            // 2. Datos profesionales según perfil
            personaService.findCredencialesByPersonaId(seleccionada.getId()).ifPresent(c -> {
                if (c.getPerfil() == Perfil.ARTISTA) {
                    Set<Especialidad> especialidades = recogerEspecialidades();
                    personaService.modificarDatosArtista(seleccionada.getId(),
                            txtApodo.getText().trim().isEmpty() ? null : txtApodo.getText().trim(),
                            especialidades);
                } else if (c.getPerfil() == Perfil.COORDINACION) {
                    LocalDate fechaSenior = chkSenior.isSelected() ? dpFechaSenior.getValue() : null;
                    personaService.modificarDatosCoordinacion(seleccionada.getId(),
                            chkSenior.isSelected(), fechaSenior);
                }
            });

            mostrarExito("Datos de " + seleccionada.getNombre() + " actualizados correctamente.");
            // Recargar para reflejar cambios
            cargarPersonas();

        } catch (IllegalArgumentException ex) {
            mostrarError(ex.getMessage());
        }
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

    // ─── Ayuda y navegación ───────────────────────────────────────────

    @FXML
    private void mostrarAyuda(ActionEvent e) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Ayuda – Modificar Persona");
        a.setHeaderText("¿Cómo modificar una persona?");
        a.setContentText(
            "1. Selecciona la persona en el desplegable superior.\n" +
            "2. Los campos se rellenarán automáticamente con sus datos actuales.\n" +
            "3. Modifica los campos que desees:\n" +
            "   · Datos personales: nombre, email, nacionalidad.\n" +
            "   · Si es Artista: apodo (opcional) y especialidades (mínimo 1).\n" +
            "   · Si es Coordinación: estado senior y fecha desde cuándo.\n" +
            "4. Pulsa 'Guardar cambios'.\n\n" +
            "Nota: No se pueden modificar las credenciales de acceso desde aquí."
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
