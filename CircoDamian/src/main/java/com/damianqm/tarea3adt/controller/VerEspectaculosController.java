package com.damianqm.tarea3adt.controller;

import com.damianqm.tarea3adt.config.StageManager;
import com.damianqm.tarea3adt.modelo.*;
import com.damianqm.tarea3adt.services.EspectaculoService;
import com.damianqm.tarea3adt.services.SesionService;
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
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

@Controller
public class VerEspectaculosController implements Initializable {

    @FXML private ComboBox<Espectaculo> cbEspectaculo;
    @FXML private Label lblId;
    @FXML private Label lblNombre;
    @FXML private Label lblPeriodo;
    @FXML private VBox  panelDetallCompleto;
    @FXML private Label lblCoordinador;
    @FXML private Label lblSenior;
    @FXML private TextArea taNumeros;
    @FXML private Label lblSinSeleccion;

    @Autowired private EspectaculoService espectaculoService;
    @Autowired private SesionService      sesionService;
    @Autowired private PaisesLoader       paisesLoader;
    @Lazy @Autowired private StageManager stageManager;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cbEspectaculo.setItems(FXCollections.observableArrayList(espectaculoService.findAll()));
        cbEspectaculo.setConverter(new StringConverter<>() {
            @Override public String toString(Espectaculo e) {
                return e == null ? "" : "[" + e.getId() + "] " + e.getNombre() +
                        "  (" + e.getFechaInicio() + " → " + e.getFechaFin() + ")";
            }
            @Override public Espectaculo fromString(String s) { return null; }
        });
        panelDetallCompleto.setVisible(false);
        panelDetallCompleto.setManaged(false);
        lblSinSeleccion.setVisible(true);
        boolean autenticado = sesionService.isAutenticado();
        lblCoordinador.setVisible(autenticado);
        lblSenior.setVisible(autenticado);
        taNumeros.setVisible(autenticado);

        cbEspectaculo.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, esp) -> { if (esp != null) mostrarDetalle(esp); });
    }

    private void mostrarDetalle(Espectaculo esp) {
        lblSinSeleccion.setVisible(false);
        panelDetallCompleto.setVisible(true);
        panelDetallCompleto.setManaged(true);
        lblId.setText(String.valueOf(esp.getId()));
        lblNombre.setText(esp.getNombre());
        lblPeriodo.setText(esp.getFechaInicio() + "  →  " + esp.getFechaFin());

        if (!sesionService.isAutenticado()) return;

        espectaculoService.findByIdCompleto(esp.getId()).ifPresent(completo -> {
            lblCoordinador.setText(completo.getCoordinador().getNombre()
                    + "  |  " + completo.getCoordinador().getEmail());
            lblSenior.setText("Senior: " + (completo.getCoordinador().isSenior() ? "Sí" : "No"));

            List<EspectaculoNumero> ens = completo.getNumerosEnEspectaculo().stream()
                    .sorted(java.util.Comparator.comparingInt(EspectaculoNumero::getOrden))
                    .collect(Collectors.toList());

            if (ens.isEmpty()) { taNumeros.setText("(Sin números asignados)"); return; }

            StringBuilder sb = new StringBuilder();
            for (EspectaculoNumero en : ens) {
                Numero n = en.getNumero();
                sb.append(en.getOrden()).append(". ").append(n.getNombre())
                  .append("  (").append(n.getDuracionFormateada()).append(" min)\n");
                for (Artista a : n.getArtistas()) {
                    String pais  = paisesLoader.getNombrePais(a.getNacionalidad());
                    if (pais == null) pais = a.getNacionalidad();
                    String espec = a.getEspecialidades().stream()
                            .map(Enum::name).sorted().collect(Collectors.joining(", "));
                    sb.append("   · ").append(a.getNombre());
                    if (a.getApodo() != null && !a.getApodo().isBlank())
                        sb.append(" \"").append(a.getApodo()).append("\"");
                    sb.append("  |  ").append(pais).append("  |  ").append(espec).append("\n");
                }
            }
            taNumeros.setText(sb.toString());
        });
    }

    @FXML private void mostrarAyuda(ActionEvent e) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Ayuda – Ver Espectáculos");
        a.setHeaderText("Consultar espectáculos del circo");
        a.setContentText(
            "Selecciona un espectáculo del desplegable.\n\n" +
            "Sin sesión: ID, nombre y fechas (CU1).\n" +
            "Con sesión: coordinador, números con orden y duración, " +
            "y artistas de cada número con país y especialidades (CU4)."
        );
        a.showAndWait();
    }

    @FXML private void volver(ActionEvent e) {
        stageManager.switchScene(sesionService.isAutenticado() ? FxmlView.MAIN : FxmlView.BIENVENIDA);
    }
}
