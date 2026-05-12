package com.damianqm.tarea3adt.controller;

import com.damianqm.tarea3adt.config.StageManager;
import com.damianqm.tarea3adt.modelo.db4o.LogOperacion;
import com.damianqm.tarea3adt.modelo.db4o.TipoOperacion;
import com.damianqm.tarea3adt.services.LogService;
import com.damianqm.tarea3adt.view.FxmlView;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * CU10 – Consultar historial de operaciones (solo Administrador).
 */
@Controller
public class HistorialController implements Initializable {

    // ── Filtros ──────────────────────────────────────────
    @FXML private TextField txtUsuario;
    @FXML private CheckBox chkNuevo;
    @FXML private CheckBox chkActualizacion;
    @FXML private CheckBox chkBorrado;
    @FXML private DatePicker dpDesde;
    @FXML private DatePicker dpHasta;

    // ── Tabla ─────────────────────────────────────────────
    @FXML private TableView<LogOperacion> tablaHistorial;
    @FXML private TableColumn<LogOperacion, Long>          colId;
    @FXML private TableColumn<LogOperacion, String>        colFechaHora;
    @FXML private TableColumn<LogOperacion, String>        colUsuario;
    @FXML private TableColumn<LogOperacion, TipoOperacion> colTipo;
    @FXML private TableColumn<LogOperacion, String>        colResumen;

    @FXML private Label lblResultados;
    @FXML private Label lblError;

    @Autowired
    private LogService logService;

    @Lazy
    @Autowired
    private StageManager stageManager;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurarColumnas();
        // Seleccionar todos los tipos por defecto
        chkNuevo.setSelected(true);
        chkActualizacion.setSelected(true);
        chkBorrado.setSelected(true);
    }

    private void configurarColumnas() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUsuario.setCellValueFactory(new PropertyValueFactory<>("usuario"));
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipoOperacion"));
        colResumen.setCellValueFactory(new PropertyValueFactory<>("resumen"));

        // fechaHora formateada
        colFechaHora.setCellValueFactory(data -> {
            LogOperacion log = data.getValue();
            String texto = log.getFechaHora() != null
                    ? log.getFechaHora().format(FORMATTER)
                    : "";
            return new javafx.beans.property.SimpleStringProperty(texto);
        });

        colResumen.prefWidthProperty().bind(
                tablaHistorial.widthProperty().subtract(colId.getPrefWidth()
                        + colFechaHora.getPrefWidth() + colUsuario.getPrefWidth()
                        + colTipo.getPrefWidth() + 20));
    }

    @FXML
    private void buscar(ActionEvent e) {
        lblError.setText("");
        lblResultados.setText("");

        String usuario = txtUsuario.getText();
        if (usuario == null || usuario.isBlank()) {
            lblError.setText("Debes introducir un nombre de usuario.");
            return;
        }

        Set<TipoOperacion> tipos = new HashSet<>();
        if (chkNuevo.isSelected())        tipos.add(TipoOperacion.NUEVO);
        if (chkActualizacion.isSelected()) tipos.add(TipoOperacion.ACTUALIZACION);
        if (chkBorrado.isSelected())       tipos.add(TipoOperacion.BORRADO);

        if (tipos.isEmpty()) {
            lblError.setText("Selecciona al menos un tipo de operacion.");
            return;
        }

        LocalDateTime desde = null;
        LocalDateTime hasta = null;
        try {
            if (dpDesde.getValue() != null)
                desde = dpDesde.getValue().atStartOfDay();
            if (dpHasta.getValue() != null)
                hasta = dpHasta.getValue().atTime(LocalTime.MAX);
            if (desde != null && hasta != null && desde.isAfter(hasta)) {
                lblError.setText("La fecha de inicio no puede ser posterior a la de fin.");
                return;
            }
        } catch (Exception ex) {
            lblError.setText("Fecha no válida.");
            return;
        }

        try {
            List<LogOperacion> resultado = logService.consultarHistorial(usuario.trim(), tipos, desde, hasta);
            tablaHistorial.setItems(FXCollections.observableArrayList(resultado));
            lblResultados.setText("Se han encontrado " + resultado.size() + " registro(s).");
        } catch (IllegalArgumentException iae) {
            lblError.setText(iae.getMessage());
        } catch (Exception ex) {
            lblError.setText("Error al consultar el historial: " + ex.getMessage());
        }
    }

    @FXML
    private void limpiar(ActionEvent e) {
        txtUsuario.clear();
        chkNuevo.setSelected(true);
        chkActualizacion.setSelected(true);
        chkBorrado.setSelected(true);
        dpDesde.setValue(null);
        dpHasta.setValue(null);
        tablaHistorial.getItems().clear();
        lblResultados.setText("");
        lblError.setText("");
    }

    @FXML
    private void volver(ActionEvent e) {
        stageManager.switchScene(FxmlView.MAIN);
    }
}
