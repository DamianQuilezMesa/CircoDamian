package com.damianqm.tarea3adt.controller;

import com.damianqm.tarea3adt.config.StageManager;
import com.damianqm.tarea3adt.modelo.Artista;
import com.damianqm.tarea3adt.modelo.EspectaculoNumero;
import com.damianqm.tarea3adt.modelo.Numero;
import com.damianqm.tarea3adt.repositorios.EspectaculoNumeroRepository;
import com.damianqm.tarea3adt.services.PersonaService;
import com.damianqm.tarea3adt.services.SesionService;
import com.damianqm.tarea3adt.util.PaisesLoader;
import com.damianqm.tarea3adt.view.FxmlView;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/** Ficha del artista autenticado (CU6). */
@Controller
public class FichaArtistaController implements Initializable {

    @FXML private Label lblNombre;
    @FXML private Label lblEmail;
    @FXML private Label lblNacionalidad;
    @FXML private Label lblApodo;
    @FXML private Label lblEspecialidades;
    @FXML private TextArea taTrayectoria;

    @Autowired private PersonaService personaService;
    @Autowired private SesionService sesionService;
    @Autowired private PaisesLoader paisesLoader;
    @Autowired private EspectaculoNumeroRepository enRepository;
    @Lazy @Autowired private StageManager stageManager;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Long idPersona = sesionService.getUsuarioActual().getPersona().getId();
        Optional<Artista> artistaOpt = personaService.findArtistaConTrayectoria(idPersona);
        if (artistaOpt.isPresent()) {
            cargarFicha(artistaOpt.get());
        } else {
            taTrayectoria.setText("No se encontraron datos del artista.");
        }
    }

    private void cargarFicha(Artista a) {
        lblNombre.setText(a.getNombre());
        lblEmail.setText(a.getEmail());

        String pais = paisesLoader.getNombrePais(a.getNacionalidad());
        lblNacionalidad.setText(pais != null ? pais : a.getNacionalidad());

        lblApodo.setText(
                a.getApodo() != null && !a.getApodo().isBlank() ? a.getApodo() : "—");

        String espec = a.getEspecialidades().stream()
                .map(Enum::name).sorted().collect(Collectors.joining(", "));
        lblEspecialidades.setText(espec.isEmpty() ? "—" : espec);

        cargarTrayectoria(a);
    }

    /**
     * Agrupa los números del artista por espectáculo y los muestra en el TextArea.
     */
    private void cargarTrayectoria(Artista a) {
        if (a.getNumeros() == null || a.getNumeros().isEmpty()) {
            taTrayectoria.setText("Sin participaciones registradas.");
            return;
        }

        // Ordenar números por nombre y agrupar por espectáculo
        List<Numero> numerosOrdenados = a.getNumeros().stream()
                .sorted(Comparator.comparing(Numero::getNombre))
                .collect(Collectors.toList());

        Map<String, List<String>> porEspectaculo = new LinkedHashMap<>();
        for (Numero n : numerosOrdenados) {
            List<EspectaculoNumero> apariciones = enRepository.findByNumeroId(n.getId());
            for (EspectaculoNumero en : apariciones) {
                String clave = "[" + en.getEspectaculo().getId() + "] "
                        + en.getEspectaculo().getNombre()
                        + " (" + en.getEspectaculo().getFechaInicio()
                        + " → " + en.getEspectaculo().getFechaFin() + ")";
                if (!porEspectaculo.containsKey(clave)) {
                    porEspectaculo.put(clave, new ArrayList<>());
                }
                porEspectaculo.get(clave).add(
                        en.getOrden() + ". " + n.getNombre()
                        + "  (" + n.getDuracionFormateada() + " min)");
            }
        }

        if (porEspectaculo.isEmpty()) {
            taTrayectoria.setText("Sin participaciones en espectáculos.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, List<String>> entry : porEspectaculo.entrySet()) {
            sb.append("Espectáculo: ").append(entry.getKey()).append("\n");
            for (String linea : entry.getValue()) {
                sb.append("   ").append(linea).append("\n");
            }
            sb.append("\n");
        }
        taTrayectoria.setText(sb.toString());
    }

    @FXML
    private void mostrarAyuda(ActionEvent e) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Ayuda – Mi Ficha");
        a.setHeaderText("Tu ficha en el circo");
        a.setContentText(
            "Muestra tu información completa:\n\n" +
            "• Datos personales: nombre, email, país.\n" +
            "• Datos profesionales: apodo y especialidades.\n" +
            "• Trayectoria: espectáculos y números en los que participas."
        );
        a.showAndWait();
    }

    @FXML
    private void volver(ActionEvent e) {
        stageManager.switchScene(FxmlView.MAIN);
    }
}
