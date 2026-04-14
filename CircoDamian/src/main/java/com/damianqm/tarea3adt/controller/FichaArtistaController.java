package com.damianqm.tarea3adt.controller;

import com.damianqm.tarea3adt.config.StageManager;
import com.damianqm.tarea3adt.modelo.*;
import com.damianqm.tarea3adt.repositorios.EspectaculoNumeroRepository;
import com.damianqm.tarea3adt.services.PersonaService;
import com.damianqm.tarea3adt.services.SesionService;
import com.damianqm.tarea3adt.util.PaisesLoader;
import com.damianqm.tarea3adt.view.FxmlView;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * CU6: Ver ficha del artista autenticado.
 * Datos personales, profesionales y trayectoria por espectáculo.
 */
@Controller
public class FichaArtistaController implements Initializable {

    @FXML private Label    lblNombre;
    @FXML private Label    lblEmail;
    @FXML private Label    lblNacionalidad;
    @FXML private Label    lblApodo;
    @FXML private Label    lblEspecialidades;
    @FXML private TextArea taTrayectoria;

    @Autowired private PersonaService              personaService;
    @Autowired private SesionService               sesionService;
    @Autowired private PaisesLoader                paisesLoader;
    @Autowired private EspectaculoNumeroRepository enRepository;
    @Lazy @Autowired private StageManager          stageManager;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Long idPersona = sesionService.getUsuarioActual().getPersona().getId();
        personaService.findArtistaConTrayectoria(idPersona)
                .ifPresentOrElse(this::cargarFicha,
                        () -> taTrayectoria.setText("No se encontraron datos del artista."));
    }

    private void cargarFicha(Artista a) {
        lblNombre.setText(a.getNombre());
        lblEmail.setText(a.getEmail());

        String pais = paisesLoader.getNombrePais(a.getNacionalidad());
        lblNacionalidad.setText(pais != null ? pais + " (" + a.getNacionalidad() + ")" : a.getNacionalidad());
        lblApodo.setText(a.getApodo() != null && !a.getApodo().isBlank() ? a.getApodo() : "—");

        String espec = a.getEspecialidades().stream()
                .map(Enum::name).sorted().collect(Collectors.joining(", "));
        lblEspecialidades.setText(espec.isEmpty() ? "—" : espec);

        // Trayectoria: para cada número donde participa, buscar en qué espectáculos está
        Set<Numero> numerosDelArtista = a.getNumeros();
        if (numerosDelArtista == null || numerosDelArtista.isEmpty()) {
            taTrayectoria.setText("Sin participaciones registradas en ningún espectáculo.");
            return;
        }

        // Agrupar por espectáculo
        Map<String, List<String>> porEspectaculo = new LinkedHashMap<>();
        for (Numero n : numerosDelArtista.stream()
                .sorted(Comparator.comparing(Numero::getNombre))
                .collect(Collectors.toList())) {

            List<EspectaculoNumero> apariciones = enRepository.findByNumeroId(n.getId());
            for (EspectaculoNumero en : apariciones) {
                String espKey = "[" + en.getEspectaculo().getId() + "] "
                        + en.getEspectaculo().getNombre()
                        + " (" + en.getEspectaculo().getFechaInicio()
                        + " → " + en.getEspectaculo().getFechaFin() + ")";
                porEspectaculo
                        .computeIfAbsent(espKey, k -> new ArrayList<>())
                        .add(en.getOrden() + ". " + n.getNombre()
                             + "  (" + n.getDuracionFormateada() + " min)");
            }
        }

        if (porEspectaculo.isEmpty()) {
            taTrayectoria.setText("Sin participaciones en espectáculos registradas.");
        } else {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, List<String>> entry : porEspectaculo.entrySet()) {
                sb.append("Espectaculo: ").append(entry.getKey()).append("\n");
                entry.getValue().forEach(s -> sb.append("   ").append(s).append("\n"));
                sb.append("\n");
            }
            taTrayectoria.setText(sb.toString());
        }
    }

    @FXML private void mostrarAyuda(ActionEvent e) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Ayuda – Mi Ficha");
        a.setHeaderText("Tu ficha en el circo");
        a.setContentText(
            "Muestra tu informacion completa:\n\n" +
            "- Datos personales: nombre, email, nacionalidad.\n" +
            "- Datos profesionales: apodo y especialidades.\n" +
            "- Trayectoria: espectaculos y numeros en los que has participado."
        );
        a.showAndWait();
    }

    @FXML private void volver(ActionEvent e) { stageManager.switchScene(FxmlView.MAIN); }
}
