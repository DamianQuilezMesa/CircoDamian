package com.damianqm.tarea3adt.controller;

import com.damianqm.tarea3adt.config.StageManager;
import com.damianqm.tarea3adt.modelo.Artista;
import com.damianqm.tarea3adt.modelo.Espectaculo;
import com.damianqm.tarea3adt.modelo.EspectaculoNumero;
import com.damianqm.tarea3adt.modelo.Numero;
import com.damianqm.tarea3adt.services.EspectaculoService;
import com.damianqm.tarea3adt.services.SesionService;
import com.damianqm.tarea3adt.util.PaisesLoader;
import com.damianqm.tarea3adt.view.FxmlView;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Ver espectáculos (CU1 y CU4).
 * Invitado: solo datos básicos (id, nombre, periodo).
 * Autenticado: además coordinador, números y artistas.
 */
@Controller
public class VerEspectaculosController implements Initializable {

    @FXML private ComboBox<Espectaculo> cbEspectaculo;
    @FXML private Label lblId;
    @FXML private Label lblNombre;
    @FXML private Label lblPeriodo;

    @FXML private VBox panelDetallCompleto;
    @FXML private VBox panelInfoCompleta;

    @FXML private Label lblCoordinador;
    @FXML private Label lblSenior;
    @FXML private TextArea taNumeros;
    @FXML private Label lblSinSeleccion;

    @Autowired private EspectaculoService espectaculoService;
    @Autowired private SesionService sesionService;
    @Autowired private PaisesLoader paisesLoader;
    @Lazy @Autowired private StageManager stageManager;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cbEspectaculo.setItems(FXCollections.observableArrayList(espectaculoService.findAll()));
        cbEspectaculo.setConverter(new StringConverter<Espectaculo>() {
            @Override
            public String toString(Espectaculo e) {
                if (e == null) return "";
                return "[" + e.getId() + "] " + e.getNombre()
                        + "  (" + e.getFechaInicio() + " → " + e.getFechaFin() + ")";
            }
            @Override
            public Espectaculo fromString(String s) { return null; }
        });

        // Empezamos con el detalle oculto
        panelDetallCompleto.setVisible(false);
        panelDetallCompleto.setManaged(false);
        lblSinSeleccion.setVisible(true);

        // Si es invitado, ocultamos la sección de info completa (CU4)
        boolean autenticado = sesionService.isAutenticado();
        panelInfoCompleta.setVisible(autenticado);
        panelInfoCompleta.setManaged(autenticado);

        cbEspectaculo.getSelectionModel().selectedItemProperty().addListener(
                (obs, viejo, nuevo) -> {
                    if (nuevo != null) mostrarDetalle(nuevo);
                });
    }

    private void mostrarDetalle(Espectaculo esp) {
        lblSinSeleccion.setVisible(false);
        panelDetallCompleto.setVisible(true);
        panelDetallCompleto.setManaged(true);

        // Datos básicos (CU1)
        lblId.setText(String.valueOf(esp.getId()));
        lblNombre.setText(esp.getNombre());
        lblPeriodo.setText(esp.getFechaInicio() + "  →  " + esp.getFechaFin());

        if (!sesionService.isAutenticado()) return;

        // Info completa (CU4)
        Optional<Espectaculo> completo = espectaculoService.findByIdCompleto(esp.getId());
        if (completo.isEmpty()) return;

        Espectaculo c = completo.get();
        lblCoordinador.setText(c.getCoordinador().getNombre()
                + "  |  " + c.getCoordinador().getEmail());

        if (c.getCoordinador().isSenior()) {
            lblSenior.setText("(Senior)");
            lblSenior.setStyle("-fx-font-weight:bold; -fx-text-fill:#1a7f37;");
        } else {
            lblSenior.setText("");
        }

        taNumeros.setText(construirTextoNumeros(c.getNumerosEnEspectaculo()));
    }

    /** Construye el listado de números y artistas del espectáculo en un String. */
    private String construirTextoNumeros(List<EspectaculoNumero> relaciones) {
        List<EspectaculoNumero> ordenados = relaciones.stream()
                .sorted(Comparator.comparingInt(EspectaculoNumero::getOrden))
                .collect(Collectors.toList());

        if (ordenados.isEmpty()) return "(Sin números asignados)";

        StringBuilder sb = new StringBuilder();
        for (EspectaculoNumero en : ordenados) {
            Numero n = en.getNumero();
            sb.append(en.getOrden()).append(". ").append(n.getNombre())
              .append("  (").append(n.getDuracionFormateada()).append(" min)\n");

            for (Artista a : n.getArtistas()) {
                String pais = paisesLoader.getNombrePais(a.getNacionalidad());
                if (pais == null) pais = a.getNacionalidad();
                String especialidades = a.getEspecialidades().stream()
                        .map(Enum::name).sorted().collect(Collectors.joining(", "));

                sb.append("   · ").append(a.getNombre());
                if (a.getApodo() != null && !a.getApodo().isBlank()) {
                    sb.append(" \"").append(a.getApodo()).append("\"");
                }
                sb.append("  |  ").append(pais)
                  .append("  |  ").append(especialidades).append("\n");
            }
        }
        return sb.toString();
    }

    @FXML
    private void mostrarAyuda(ActionEvent e) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Ayuda – Ver Espectáculos");
        a.setHeaderText("Consultar espectáculos del circo");
        a.setContentText(
            "Selecciona un espectáculo del desplegable.\n\n" +
            "Sin sesión: id, nombre y fechas (CU1).\n" +
            "Con sesión: además coordinador, números y artistas (CU4)."
        );
        a.showAndWait();
    }

    @FXML
    private void volver(ActionEvent e) {
        if (sesionService.isAutenticado()) {
            stageManager.switchScene(FxmlView.MAIN);
        } else {
            stageManager.switchScene(FxmlView.BIENVENIDA);
        }
    }
}
