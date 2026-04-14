package com.damianqm.tarea3adt.controller;

import com.damianqm.tarea3adt.config.StageManager;
import com.damianqm.tarea3adt.modelo.*;
import com.damianqm.tarea3adt.services.EspectaculoService;
import com.damianqm.tarea3adt.services.EspectaculoService.NumeroEnEspectaculo;
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
import javafx.util.StringConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

import java.net.URL;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * CU5A: Gestionar Espectáculos.
 * Permite: crear, modificar y eliminar espectáculos.
 * La composición del espectáculo se hace seleccionando números ya existentes en el sistema.
 * Para crear o modificar números, usar la pantalla de Gestión de Números.
 *
 * Flujo:
 *  PASO 1 — Datos (nombre, fechas, coordinador)
 *  PASO 2 — Asignar números existentes (mínimo 3, con orden único)
 *            → Guardar todo en BD en una transacción atómica.
 */
@Controller
public class GestionEspectaculoController implements Initializable {

    // ── Paso 1 ────────────────────────────────────────────────────────
    @FXML private VBox       panelPaso1;
    @FXML private TextField  txtNombreEsp;
    @FXML private DatePicker dpInicio;
    @FXML private DatePicker dpFin;
    @FXML private ComboBox<Coordinacion> cbCoordinador;

    // ── Paso 2 ────────────────────────────────────────────────────────
    @FXML private VBox  panelPaso2;
    @FXML private Label lblEspActual;
    @FXML private Label lblContador;

    // Selector de número existente
    @FXML private ComboBox<Numero> cbNumero;
    @FXML private TextField        txtOrden;
    @FXML private Button           btnAnadir;

    // Tabla de asignaciones actuales
    @FXML private TableView<EspectaculoNumero>           tablaAsignados;
    @FXML private TableColumn<EspectaculoNumero, Integer> colOrden;
    @FXML private TableColumn<EspectaculoNumero, String>  colNombre;
    @FXML private TableColumn<EspectaculoNumero, String>  colDuracion;
    @FXML private TableColumn<EspectaculoNumero, String>  colArtistas;
    @FXML private Button                                   btnQuitar;
    @FXML private Button                                   btnGuardar;

    // ── Selector de espectáculo ───────────────────────────────────────
    @FXML private ComboBox<Espectaculo> cbEspectaculo;

    // ── Feedback ─────────────────────────────────────────────────────
    @FXML private Label lblMensaje;

    // ── Estado ───────────────────────────────────────────────────────
    /**
     * Lista en memoria de los números asignados al espectáculo en edición.
     * Cada fila es un EspectaculoNumero temporal (espectaculo puede ser null
     * si es creación nueva) — solo se usa para mostrar en tabla.
     * Al guardar se convierten a NumeroEnEspectaculo para el servicio.
     */
    private final ObservableList<EspectaculoNumero> asignados =
            FXCollections.observableArrayList();
    private Espectaculo espEnEdicion = null;

    @Autowired private EspectaculoService espectaculoService;
    @Autowired private PersonaService     personaService;
    @Autowired private SesionService      sesionService;
    @Lazy @Autowired private StageManager stageManager;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurarCombos();
        configurarTabla();
        mostrarPaso(1);

        if (sesionService.isCoordinacion() && !sesionService.isAdmin()) {
            Long id = sesionService.getUsuarioActual().getPersona().getId();
            personaService.findCoordinacionById(id).ifPresent(c -> {
                cbCoordinador.getSelectionModel().select(c);
                cbCoordinador.setDisable(true);
            });
        }

        cbEspectaculo.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, nuevo) -> { if (nuevo != null) cargarEspectaculo(nuevo); });

        asignados.addListener(
                (javafx.collections.ListChangeListener<EspectaculoNumero>) c ->
                        actualizarContador());
    }

    // ─── Paso 1 ───────────────────────────────────────────────────────

    @FXML private void siguiente(ActionEvent e) {
        try {
            Long idEx = espEnEdicion != null ? espEnEdicion.getId() : null;
            espectaculoService.validarDatosEspectaculo(
                    txtNombreEsp.getText().trim(),
                    dpInicio.getValue(), dpFin.getValue(),
                    cbCoordinador.getValue() != null ? cbCoordinador.getValue().getId() : null,
                    idEx);
            // Precargar números si estamos modificando y la lista está vacía
            if (espEnEdicion != null && asignados.isEmpty())
                cargarNumerosExistentes(espEnEdicion);
            lblEspActual.setText((espEnEdicion != null ? "Modificando: " : "Nuevo: ")
                    + txtNombreEsp.getText().trim());
            mostrarPaso(2);
            actualizarContador();
        } catch (IllegalArgumentException ex) { error(ex.getMessage()); }
    }

    @FXML private void nuevoEspectaculo(ActionEvent e) {
        espEnEdicion = null;
        asignados.clear();
        limpiarPaso1();
        cbEspectaculo.getSelectionModel().clearSelection();
        mostrarPaso(1);
        lblMensaje.setText("");
    }

    // ─── Paso 2 ───────────────────────────────────────────────────────

    @FXML private void anadirNumero(ActionEvent e) {
        Numero n = cbNumero.getValue();
        if (n == null) { error("Selecciona un número del desplegable."); return; }
        if (txtOrden.getText().isBlank()) { error("Indica el orden para este número."); return; }
        try {
            int orden = Integer.parseInt(txtOrden.getText().trim());
            espectaculoService.validarOrden(orden);
            // Comprobar orden duplicado
            if (asignados.stream().anyMatch(a -> a.getOrden() == orden)) {
                error("Ya existe un número con orden " + orden + "."); return;
            }
            // Comprobar número duplicado
            if (asignados.stream().anyMatch(a -> a.getNumero().getId().equals(n.getId()))) {
                error("El número '" + n.getNombre() + "' ya está en la lista."); return;
            }
            asignados.add(new EspectaculoNumero(null, n, orden));
            FXCollections.sort(asignados, Comparator.comparingInt(EspectaculoNumero::getOrden));
            txtOrden.clear();
            cbNumero.getSelectionModel().clearSelection();
            ok("Número '" + n.getNombre() + "' añadido con orden " + orden + ".");
        } catch (NumberFormatException ex) { error("El orden debe ser un número entero."); }
          catch (IllegalArgumentException ex) { error(ex.getMessage()); }
    }

    @FXML private void quitarNumero(ActionEvent e) {
        EspectaculoNumero sel = tablaAsignados.getSelectionModel().getSelectedItem();
        if (sel == null) { error("Selecciona un número de la tabla para quitarlo."); return; }
        asignados.remove(sel);
        ok("Número '" + sel.getNumero().getNombre() + "' quitado de la lista.");
    }

    @FXML private void volverPaso1(ActionEvent e) { mostrarPaso(1); }

    // ─── Guardar ─────────────────────────────────────────────────────

    @FXML private void guardar(ActionEvent e) {
        try {
            List<NumeroEnEspectaculo> lista = asignados.stream()
                    .map(a -> new NumeroEnEspectaculo(a.getNumero().getId(), a.getOrden()))
                    .collect(Collectors.toList());
            Espectaculo guardado = espectaculoService.persistirEspectaculoCompleto(
                    txtNombreEsp.getText().trim(),
                    dpInicio.getValue(), dpFin.getValue(),
                    cbCoordinador.getValue().getId(),
                    lista,
                    espEnEdicion != null ? espEnEdicion.getId() : null);

            new Alert(Alert.AlertType.INFORMATION,
                    "Espectaculo '" + guardado.getNombre() + "' guardado con "
                    + asignados.size() + " numeros.", ButtonType.OK).showAndWait();
            reset();
        } catch (IllegalArgumentException ex) { error(ex.getMessage()); }
    }

    // ─── Eliminar espectáculo ─────────────────────────────────────────

    @FXML private void eliminarEspectaculo(ActionEvent e) {
        Espectaculo sel = cbEspectaculo.getValue();
        if (sel == null) { error("Selecciona un espectaculo para eliminarlo."); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Eliminar '" + sel.getNombre() + "'?\n" +
                "Los numeros NO se eliminaran, solo la relacion con este espectaculo.",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirmar eliminacion");
        confirm.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.YES) {
                try {
                    espectaculoService.eliminarEspectaculo(sel.getId());
                    new Alert(Alert.AlertType.INFORMATION,
                            "Espectaculo eliminado.", ButtonType.OK).showAndWait();
                    reset();
                } catch (IllegalArgumentException ex) { error(ex.getMessage()); }
            }
        });
    }

    // ─── Configuración ────────────────────────────────────────────────

    private void configurarCombos() {
        // Espectáculos
        cbEspectaculo.setItems(FXCollections.observableArrayList(espectaculoService.findAll()));
        cbEspectaculo.setConverter(espConverter());
        cbEspectaculo.setPromptText("-- Selecciona para modificar/eliminar --");

        // Coordinadores
        cbCoordinador.setItems(FXCollections.observableArrayList(personaService.findAllCoordinadores()));
        cbCoordinador.setConverter(coordConverter());
        cbCoordinador.setPromptText("-- Selecciona coordinador --");

        // Números existentes
        cbNumero.setItems(FXCollections.observableArrayList(espectaculoService.findAllNumeros()));
        cbNumero.setConverter(numConverter());
        cbNumero.setPromptText("-- Selecciona un numero --");
    }

    private void configurarTabla() {
        colOrden.setCellValueFactory(d ->
                new SimpleIntegerProperty(d.getValue().getOrden()).asObject());
        colNombre.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getNumero().getNombre()));
        colDuracion.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getNumero().getDuracionFormateada() + " min"));
        colArtistas.setCellValueFactory(d -> {
            Set<Artista> arts = d.getValue().getNumero().getArtistas();
            if (arts == null || arts.isEmpty()) return new SimpleStringProperty("Sin artistas");
            return new SimpleStringProperty(arts.stream()
                    .map(Artista::getNombre).sorted().collect(Collectors.joining(", ")));
        });
        tablaAsignados.setItems(asignados);
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

    private void cargarNumerosExistentes(Espectaculo esp) {
        // Cargar con fetch join para evitar LazyInit
        espectaculoService.findByIdCompleto(esp.getId()).ifPresent(completo ->
            completo.getNumerosEnEspectaculo().stream()
                .sorted(Comparator.comparingInt(EspectaculoNumero::getOrden))
                .forEach(en -> asignados.add(
                        new EspectaculoNumero(completo, en.getNumero(), en.getOrden())))
        );
    }

    // ─── Helpers ──────────────────────────────────────────────────────

    private void mostrarPaso(int paso) {
        panelPaso1.setVisible(paso == 1); panelPaso1.setManaged(paso == 1);
        panelPaso2.setVisible(paso == 2); panelPaso2.setManaged(paso == 2);
    }

    private void actualizarContador() {
        int total = asignados.size(), falt = Math.max(0, 3 - total);
        String txt = total == 0 ? "Anade al menos 3 numeros." :
                     falt > 0  ? total + " numero(s) — faltan " + falt + " para el minimo." :
                                 total + " numero(s). Listo para guardar.";
        lblContador.setText(txt);
        lblContador.setStyle(total >= 3
                ? "-fx-text-fill:#27ae60; -fx-font-weight:bold;"
                : "-fx-text-fill:#e67e22; -fx-font-weight:bold;");
        btnGuardar.setDisable(total < 3);
    }

    private void reset() {
        espEnEdicion = null; asignados.clear(); limpiarPaso1();
        cbEspectaculo.setItems(FXCollections.observableArrayList(espectaculoService.findAll()));
        cbNumero.setItems(FXCollections.observableArrayList(espectaculoService.findAllNumeros()));
        cbEspectaculo.getSelectionModel().clearSelection();
        mostrarPaso(1); lblMensaje.setText("");
    }

    private void limpiarPaso1() {
        txtNombreEsp.clear(); dpInicio.setValue(null); dpFin.setValue(null);
        if (!cbCoordinador.isDisabled()) cbCoordinador.getSelectionModel().clearSelection();
    }

    private void error(String m) {
        lblMensaje.setStyle("-fx-text-fill:red;"); lblMensaje.setText(m);
        new Alert(Alert.AlertType.WARNING, m, ButtonType.OK).showAndWait();
    }
    private void ok(String m) { lblMensaje.setStyle("-fx-text-fill:#27ae60;"); lblMensaje.setText(m); }

    // Converters
    private StringConverter<Espectaculo> espConverter() {
        return new StringConverter<>() {
            @Override public String toString(Espectaculo e) {
                return e == null ? "" : "["+e.getId()+"] "+e.getNombre()
                        +"  ("+e.getFechaInicio()+" → "+e.getFechaFin()+")";
            }
            @Override public Espectaculo fromString(String s) { return null; }
        };
    }
    private StringConverter<Coordinacion> coordConverter() {
        return new StringConverter<>() {
            @Override public String toString(Coordinacion c) {
                return c == null ? "" : c.getNombre() + (c.isSenior() ? " ★" : "");
            }
            @Override public Coordinacion fromString(String s) { return null; }
        };
    }
    private StringConverter<Numero> numConverter() {
        return new StringConverter<>() {
            @Override public String toString(Numero n) {
                if (n == null) return "";
                String arts = n.getArtistas().isEmpty() ? "sin artistas" :
                        n.getArtistas().stream().map(Artista::getNombre)
                                .sorted().collect(Collectors.joining(", "));
                return "["+n.getId()+"] "+n.getNombre()
                        +" ("+n.getDuracionFormateada()+" min) — "+arts;
            }
            @Override public Numero fromString(String s) { return null; }
        };
    }

    @FXML private void mostrarAyuda(ActionEvent e) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Ayuda — Gestion de Espectaculos");
        a.setHeaderText("Como gestionar espectaculos");
        a.setContentText(
            "CREAR:\n" +
            "  Pulsa 'Nuevo espectaculo', rellena los datos en el Paso 1\n" +
            "  y pulsa Siguiente. En el Paso 2 elige numeros del desplegable,\n" +
            "  asignales un orden y pulsa Anadir. Con 3 o mas numeros\n" +
            "  podras guardar el espectaculo.\n\n" +
            "MODIFICAR:\n" +
            "  Selecciona un espectaculo del desplegable izquierdo.\n" +
            "  Sus datos y numeros se cargan automaticamente.\n\n" +
            "ELIMINAR:\n" +
            "  Selecciona el espectaculo y pulsa Eliminar.\n" +
            "  Los numeros NO se eliminan del sistema.\n\n" +
            "Para crear o modificar numeros, usa el boton\n" +
            "'Gestionar Numeros' del menu principal."
        );
        a.showAndWait();
    }

    @FXML private void volver(ActionEvent e) { stageManager.switchScene(FxmlView.MAIN); }
    @FXML private void irGestionNumeros(ActionEvent e) { stageManager.switchScene(FxmlView.GESTION_NUMERO); }
}
