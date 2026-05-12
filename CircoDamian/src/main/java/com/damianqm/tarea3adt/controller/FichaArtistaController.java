package com.damianqm.tarea3adt.controller;

import com.damianqm.tarea3adt.config.StageManager;
import com.damianqm.tarea3adt.modelo.Artista;
import com.damianqm.tarea3adt.modelo.Espectaculo;
import com.damianqm.tarea3adt.modelo.Numero;
import com.damianqm.tarea3adt.services.PersonaService;
import com.damianqm.tarea3adt.services.SesionService;
import com.damianqm.tarea3adt.util.PaisesLoader;
import com.damianqm.tarea3adt.view.FxmlView;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/** Ficha del artista autenticado (CU6). */
@Controller
public class FichaArtistaController implements Initializable {

	@FXML
	private Label lblNombre;
	@FXML
	private Label lblEmail;
	@FXML
	private Label lblNacionalidad;
	@FXML
	private Label lblApodo;
	@FXML
	private Label lblEspecialidades;
	@FXML
	private TextArea taTrayectoria;

	@Autowired
	private PersonaService personaService;
	@Autowired
	private SesionService sesionService;
	@Autowired
	private PaisesLoader paisesLoader;
	@Lazy
	@Autowired
	private StageManager stageManager;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		Long idPersona = sesionService.getUsuarioActual().getPersona().getId();
		personaService.findArtistaConTrayectoria(idPersona).ifPresentOrElse(this::cargarFicha,
				() -> taTrayectoria.setText("No se encontraron datos del artista."));
	}

	private void cargarFicha(Artista a) {
		lblNombre.setText(a.getNombre());
		lblEmail.setText(a.getEmail());

		String pais = paisesLoader.getNombrePais(a.getNacionalidad());
		lblNacionalidad.setText(pais != null ? pais : a.getNacionalidad());

		lblApodo.setText(a.getApodo() != null && !a.getApodo().isBlank() ? a.getApodo() : "—");

		String espec = a.getEspecialidades().stream().map(Enum::name).sorted().collect(Collectors.joining(", "));
		lblEspecialidades.setText(espec.isEmpty() ? "—" : espec);

		cargarTrayectoria(a);
	}

	private void cargarTrayectoria(Artista a) {
		if (a.getNumeros() == null || a.getNumeros().isEmpty()) {
			taTrayectoria.setText("Sin participaciones registradas.");
			return;
		}

		Map<Long, List<Numero>> porEspId = new LinkedHashMap<>();
		Map<Long, Espectaculo> espMap = new LinkedHashMap<>();

		List<Numero> ordenados = a.getNumeros().stream().sorted(Comparator.comparingInt(Numero::getOrden))
				.collect(Collectors.toList());

		for (Numero n : ordenados) {
			Espectaculo esp = n.getEspectaculo();
			if (esp == null)
				continue;
			porEspId.computeIfAbsent(esp.getId(), k -> new ArrayList<>()).add(n);
			espMap.putIfAbsent(esp.getId(), esp);
		}

		if (porEspId.isEmpty()) {
			taTrayectoria.setText("Sin participaciones en espectáculos.");
			return;
		}

		StringBuilder sb = new StringBuilder();
		for (Map.Entry<Long, List<Numero>> entry : porEspId.entrySet()) {
			Espectaculo esp = espMap.get(entry.getKey());
			sb.append("Espectáculo: [").append(esp.getId()).append("] ").append(esp.getNombre()).append(" (")
					.append(esp.getFechaInicio()).append(" → ").append(esp.getFechaFin()).append(")\n");

			for (Numero n : entry.getValue()) {
				sb.append("   ").append(n.getOrden()).append(". ").append(n.getNombre()).append("  (")
						.append(n.getDuracionFormateada()).append(" min)\n");
			}
			sb.append("\n");
		}
		taTrayectoria.setText(sb.toString());
	}

	@FXML
	private void volver(ActionEvent e) {
		stageManager.switchScene(FxmlView.MAIN);
	}
}
