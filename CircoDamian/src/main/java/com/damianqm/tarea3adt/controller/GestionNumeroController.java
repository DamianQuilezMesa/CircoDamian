package com.damianqm.tarea3adt.controller;

import com.damianqm.tarea3adt.config.StageManager;
import com.damianqm.tarea3adt.modelo.*;
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
 * CU5B: Gestión de Números circenses.
 * Accesible para Coordinación y Admin.
 * Permite: crear, modificar y eliminar números.
 * (Un número solo puede eliminarse si no está asignado a ningún espectáculo.)
 */
@Controller
public class GestionNumeroController implements Initializable {

    // ── Selector de número ────────────────────────────────────────────
    @FXML private ComboBox<Numero> cbNumero;

    // ── Formulario ────────────────────────────────────────────────────
    @FXML private TextField txtNombre;
    @FXML private TextField txtDuracion;

    // ── Lista de artistas ─────────────────────────────────────────────
    @FXML private ListView<Artista> listArtistas;

    // ── Tabla de artistas asignados al número seleccionado ────────────
    @FXML private TableView<Artista>           tablaArtistasAsignados;
    @FXML private TableColumn<Artista, String> colNombreArtista;
    @FXML private TableColumn<Artista, String> colEspecArtista;

    // ── Botones ───────────────────────────────────────────────────────
    @FXML private Button btnGuardar;
    @FXML private Button btnEliminar;
    @FXML private Button btnNuevo;

    // ── Feedback ─────────────────────────────────────────────────────
    @FXML private Label lblMensaje;
    @FXML private Label lblTitulo;

    // ── Estado ───────────────────────────────────────────────────────
    private Numero numeroEnEdicion = null;

    @Autowired private EspectaculoService espectaculoService;
    @Autowired private PersonaService     personaService;
    @Lazy @Autowired private StageManager stageManager;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurarComboNumeros();
        configurarTablaArtistasAsignados();
        cargarListaArtistas();
        modoNuevo();

        cbNumero.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, n) -> { if (n != null) cargarNumero(n); });
    }

    // ─── Selección ───────────────────────────────────────────────────

    private void cargarNumero(Numero n) {
        // Recargar con artistas para evitar lazy
        espectaculoService.findNumeroByIdConArtistas(n.getId()).ifPresent(nCompleto -> {
            numeroEnEdicion = nCompleto;
            txtNombre.setText(nCompleto.getNombre());
            txtDuracion.setText(nCompleto.getDuracionFormateada());
            // Mostrar artistas asignados en la tabla
            tablaArtistasAsignados.setItems(
                    FXCollections.observableArrayList(nCompleto.getArtistas()));
            // Pre-seleccionar en la lista de artistas disponibles
            listArtistas.getSelectionModel().clearSelection();
            listArtistas.getItems().forEach(a -> {
                if (nCompleto.getArtistas().stream().anyMatch(aa -> aa.getId().equals(a.getId())))
                    listArtistas.getSelectionModel().select(a);
            });
            lblTitulo.setText("Modificando numero: " + nCompleto.getNombre());
            btnEliminar.setDisable(false);
            ok("Numero cargado. Modifica los campos y pulsa Guardar.");
        });
    }

    // ─── CRUD ────────────────────────────────────────────────────────

    @FXML private void guardar(ActionEvent e) {
        if (txtNombre.getText().isBlank())   { error("El nombre es obligatorio."); return; }
        if (txtDuracion.getText().isBlank()) { error("La duracion es obligatoria (ej: 5,0)."); return; }

        var selArtistas = listArtistas.getSelectionModel().getSelectedItems();
        if (selArtistas.isEmpty()) { error("Selecciona al menos un artista."); return; }

        try {
            double dur = Double.parseDouble(txtDuracion.getText().trim().replace(",", "."));
            Set<Long> idsArts = selArtistas.stream().map(Artista::getId).collect(Collectors.toSet());

            if (numeroEnEdicion == null) {
                Numero nuevo = espectaculoService.crearNumero(
                        txtNombre.getText().trim(), dur, idsArts);
                new Alert(Alert.AlertType.INFORMATION,
                        "Numero '" + nuevo.getNombre() + "' creado.", ButtonType.OK).showAndWait();
            } else {
                espectaculoService.modificarNumero(
                        numeroEnEdicion.getId(), txtNombre.getText().trim(), dur, idsArts);
                new Alert(Alert.AlertType.INFORMATION,
                        "Numero actualizado.", ButtonType.OK).showAndWait();
            }
            recargarTodo();
            modoNuevo();
        } catch (NumberFormatException ex) {
            error("Duracion invalida. Usa formato x,0 o x,5 (ej: 8,5).");
        } catch (IllegalArgumentException ex) {
            error(ex.getMessage());
        }
    }

    @FXML private void eliminar(ActionEvent e) {
        if (numeroEnEdicion == null) { error("Selecciona un numero para eliminar."); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Eliminar el numero '" + numeroEnEdicion.getNombre() + "'?\n" +
                "(Solo es posible si no esta asignado a ningun espectaculo.)",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirmar eliminacion");
        confirm.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.YES) {
                try {
                    espectaculoService.eliminarNumero(numeroEnEdicion.getId());
                    new Alert(Alert.AlertType.INFORMATION,
                            "Numero eliminado.", ButtonType.OK).showAndWait();
                    recargarTodo();
                    modoNuevo();
                } catch (IllegalArgumentException ex) { error(ex.getMessage()); }
            }
        });
    }

    @FXML private void nuevo(ActionEvent e) {
        cbNumero.getSelectionModel().clearSelection();
        modoNuevo();
    }

    private void modoNuevo() {
        numeroEnEdicion = null;
        txtNombre.clear(); txtDuracion.clear();
        listArtistas.getSelectionModel().clearSelection();
        tablaArtistasAsignados.setItems(FXCollections.observableArrayList());
        lblTitulo.setText("Crear nuevo numero");
        btnEliminar.setDisable(true);
        lblMensaje.setText("");
    }

    // ─── Configuración ────────────────────────────────────────────────

    private void configurarComboNumeros() {
        cbNumero.setItems(FXCollections.observableArrayList(espectaculoService.findAllNumeros()));
        cbNumero.setConverter(new StringConverter<>() {
            @Override public String toString(Numero n) {
                return n == null ? "" : "["+n.getId()+"] "+n.getNombre()
                        +" ("+n.getDuracionFormateada()+" min)";
            }
            @Override public Numero fromString(String s) { return null; }
        });
        cbNumero.setPromptText("-- Selecciona un numero para modificar --");
    }

    private void configurarTablaArtistasAsignados() {
        colNombreArtista.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colEspecArtista.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getEspecialidades().stream()
                        .map(Enum::name).sorted().collect(Collectors.joining(", "))));
    }

    private void cargarListaArtistas() {
        listArtistas.setItems(FXCollections.observableArrayList(personaService.findAllArtistas()));
        listArtistas.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        listArtistas.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Artista a, boolean empty) {
                super.updateItem(a, empty);
                if (empty || a == null) { setText(null); return; }
                String espec = a.getEspecialidades().stream()
                        .map(Enum::name).sorted().collect(Collectors.joining(", "));
                setText(a.getNombre()
                        + (a.getApodo() != null ? " \""+a.getApodo()+"\"" : "")
                        + "  ["+espec+"]");
            }
        });
    }

    private void recargarTodo() {
        cbNumero.setItems(FXCollections.observableArrayList(espectaculoService.findAllNumeros()));
    }

    // ─── Helpers ─────────────────────────────────────────────────────
    private void error(String m) {
        lblMensaje.setStyle("-fx-text-fill:red;"); lblMensaje.setText(m);
        new Alert(Alert.AlertType.WARNING, m, ButtonType.OK).showAndWait();
    }
    private void ok(String m) { lblMensaje.setStyle("-fx-text-fill:#27ae60;"); lblMensaje.setText(m); }

    @FXML private void mostrarAyuda(ActionEvent e) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Ayuda — Gestion de Numeros");
        a.setHeaderText("Como gestionar numeros circenses");
        a.setContentText(
            "CREAR: Rellena nombre, duracion (x,0 o x,5) y selecciona\n" +
            "  artistas (Ctrl+clic para varios). Pulsa Guardar.\n\n" +
            "MODIFICAR: Selecciona el numero del desplegable,\n" +
            "  edita los campos y pulsa Guardar.\n\n" +
            "ELIMINAR: Selecciona el numero y pulsa Eliminar.\n" +
            "  Solo es posible si el numero NO esta asignado a\n" +
            "  ningun espectaculo.\n\n" +
            "Para asignar numeros a espectaculos, usa el boton\n" +
            "'Gestionar Espectaculos' del menu principal."
        );
        a.showAndWait();
    }

    @FXML private void volver(ActionEvent e) { stageManager.switchScene(FxmlView.MAIN); }
}
