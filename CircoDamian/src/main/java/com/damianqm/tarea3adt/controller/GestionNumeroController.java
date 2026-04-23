package com.damianqm.tarea3adt.controller;

import com.damianqm.tarea3adt.config.StageManager;
import com.damianqm.tarea3adt.modelo.Artista;
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
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Gestión de números circenses (CU5B).
 * Permite crear, modificar y eliminar números. Solo se puede eliminar
 * un número si NO está asignado a ningún espectáculo.
 */
@Controller
public class GestionNumeroController implements Initializable {

    @FXML private ComboBox<Numero> cbNumero;
    @FXML private TextField txtNombre;
    @FXML private TextField txtDuracion;

    @FXML private ListView<Artista> listArtistas;
    @FXML private TableView<Artista> tablaArtistasAsignados;
    @FXML private TableColumn<Artista, String> colNombreArtista;
    @FXML private TableColumn<Artista, String> colEspecArtista;

    @FXML private Button btnGuardar;
    @FXML private Button btnEliminar;
    @FXML private Button btnNuevo;

    @FXML private Label lblMensaje;
    @FXML private Label lblTitulo;

    /** null = crear nuevo, no null = modificar existente. */
    private Numero numeroEnEdicion = null;

    @Autowired private EspectaculoService espectaculoService;
    @Autowired private PersonaService personaService;
    @Lazy @Autowired private StageManager stageManager;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurarComboNumeros();
        configurarTablaArtistasAsignados();
        cargarListaArtistas();
        modoNuevo();

        cbNumero.getSelectionModel().selectedItemProperty().addListener(
                (obs, viejo, nuevo) -> {
                    if (nuevo != null) cargarNumero(nuevo);
                });
    }

    /** Carga el número seleccionado en el formulario y preselecciona sus artistas. */
    private void cargarNumero(Numero n) {
        Optional<Numero> completoOpt = espectaculoService.findNumeroByIdConArtistas(n.getId());
        if (completoOpt.isEmpty()) return;

        Numero completo = completoOpt.get();
        numeroEnEdicion = completo;
        txtNombre.setText(completo.getNombre());
        txtDuracion.setText(completo.getDuracionFormateada());
        tablaArtistasAsignados.setItems(
                FXCollections.observableArrayList(completo.getArtistas()));

        // Preseleccionar en la lista los artistas del número
        listArtistas.getSelectionModel().clearSelection();
        for (Artista a : listArtistas.getItems()) {
            for (Artista asig : completo.getArtistas()) {
                if (asig.getId().equals(a.getId())) {
                    listArtistas.getSelectionModel().select(a);
                    break;
                }
            }
        }

        lblTitulo.setText("Modificando número: " + completo.getNombre());
        btnEliminar.setDisable(false);
        ok("Número cargado. Modifica los campos y pulsa Guardar.");
    }

    @FXML
    private void guardar(ActionEvent e) {
        if (txtNombre.getText().isBlank()) {
            error("El nombre es obligatorio.");
            return;
        }
        if (txtDuracion.getText().isBlank()) {
            error("La duración es obligatoria (ej: 5,0).");
            return;
        }

        List<Artista> seleccionados = listArtistas.getSelectionModel().getSelectedItems();
        if (seleccionados.isEmpty()) {
            error("Selecciona al menos un artista.");
            return;
        }

        try {
            // Admite coma o punto como separador decimal
            double dur = Double.parseDouble(txtDuracion.getText().trim().replace(",", "."));
            Set<Long> idsArts = seleccionados.stream()
                    .map(Artista::getId)
                    .collect(Collectors.toSet());

            if (numeroEnEdicion == null) {
                Numero creado = espectaculoService.crearNumero(
                        txtNombre.getText().trim(), dur, idsArts);
                new Alert(Alert.AlertType.INFORMATION,
                        "Número '" + creado.getNombre() + "' creado.",
                        ButtonType.OK).showAndWait();
            } else {
                espectaculoService.modificarNumero(
                        numeroEnEdicion.getId(), txtNombre.getText().trim(), dur, idsArts);
                new Alert(Alert.AlertType.INFORMATION,
                        "Número actualizado.", ButtonType.OK).showAndWait();
            }
            recargarCombo();
            modoNuevo();
        } catch (NumberFormatException ex) {
            error("Duración inválida. Usa formato x,0 o x,5 (ej: 8,5).");
        } catch (IllegalArgumentException ex) {
            error(ex.getMessage());
        }
    }

    @FXML
    private void eliminar(ActionEvent e) {
        if (numeroEnEdicion == null) {
            error("Selecciona un número para eliminar.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Eliminar el número '" + numeroEnEdicion.getNombre() + "'?\n"
                + "(Solo si no está asignado a ningún espectáculo.)",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirmar eliminación");
        Optional<ButtonType> resp = confirm.showAndWait();
        if (resp.isPresent() && resp.get() == ButtonType.YES) {
            try {
                espectaculoService.eliminarNumero(numeroEnEdicion.getId());
                new Alert(Alert.AlertType.INFORMATION,
                        "Número eliminado.", ButtonType.OK).showAndWait();
                recargarCombo();
                modoNuevo();
            } catch (IllegalArgumentException ex) {
                error(ex.getMessage());
            }
        }
    }

    @FXML
    private void nuevo(ActionEvent e) {
        cbNumero.getSelectionModel().clearSelection();
        modoNuevo();
    }

    /** Limpia el formulario y prepara la pantalla para crear un número nuevo. */
    private void modoNuevo() {
        numeroEnEdicion = null;
        txtNombre.clear();
        txtDuracion.clear();
        listArtistas.getSelectionModel().clearSelection();
        tablaArtistasAsignados.setItems(FXCollections.observableArrayList());
        lblTitulo.setText("Crear nuevo número");
        btnEliminar.setDisable(true);
        lblMensaje.setText("");
    }

    private void configurarComboNumeros() {
        cbNumero.setItems(FXCollections.observableArrayList(espectaculoService.findAllNumeros()));
        cbNumero.setConverter(new StringConverter<Numero>() {
            @Override
            public String toString(Numero n) {
                if (n == null) return "";
                return "[" + n.getId() + "] " + n.getNombre()
                        + " (" + n.getDuracionFormateada() + " min)";
            }
            @Override
            public Numero fromString(String s) { return null; }
        });
        cbNumero.setPromptText("-- Selecciona un número para modificar --");
    }

    private void configurarTablaArtistasAsignados() {
        colNombreArtista.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colEspecArtista.setCellValueFactory(d -> {
            String esp = d.getValue().getEspecialidades().stream()
                    .map(Enum::name).sorted().collect(Collectors.joining(", "));
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
                String esp = a.getEspecialidades().stream()
                        .map(Enum::name).sorted().collect(Collectors.joining(", "));
                String apodo = "";
                if (a.getApodo() != null) {
                    apodo = " \"" + a.getApodo() + "\"";
                }
                setText(a.getNombre() + apodo + "  [" + esp + "]");
            }
        });
    }

    private void recargarCombo() {
        cbNumero.setItems(FXCollections.observableArrayList(espectaculoService.findAllNumeros()));
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
        a.setTitle("Ayuda – Gestión de Números");
        a.setHeaderText("Cómo gestionar números circenses");
        a.setContentText(
            "CREAR: Rellena nombre, duración (x,0 o x,5) y selecciona artistas\n" +
            "  (Ctrl+clic para varios). Pulsa Guardar.\n\n" +
            "MODIFICAR: Selecciona el número en el desplegable, edita y Guarda.\n\n" +
            "ELIMINAR: Solo es posible si NO está asignado a ningún espectáculo.\n" +
            "Si lo está, quítalo primero de los espectáculos."
        );
        a.showAndWait();
    }

    @FXML
    private void volver(ActionEvent e) {
        stageManager.switchScene(FxmlView.MAIN);
    }
}
