package com.damianqm.tarea3adt.controller;

import com.damianqm.tarea3adt.config.StageManager;
import com.damianqm.tarea3adt.modelo.Artista;
import com.damianqm.tarea3adt.modelo.Espectaculo;
import com.damianqm.tarea3adt.modelo.Numero;
import com.damianqm.tarea3adt.services.EspectaculoService;
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
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/** Buscar un espectáculo y ver su detalle completo (CU4). */
@Controller
public class BuscarEspectaculoController implements Initializable {

	@FXML
	private ComboBox<Espectaculo> cbEspectaculo;
	@FXML
	private Label lblId;
	@FXML
	private Label lblNombre;
	@FXML
	private Label lblPeriodo;
	@FXML
	private Label lblCoordinador;
	@FXML
	private Label lblSenior;
	@FXML
	private Label lblSinSeleccion;
	@FXML
	private VBox panelCompleto;
	@FXML
	private TextArea taNumeros;

	@Autowired
	private EspectaculoService espectaculoService;
	@Autowired
	private PaisesLoader paisesLoader;
	@Lazy
	@Autowired
	private StageManager stageManager;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		cbEspectaculo.setItems(FXCollections.observableArrayList(espectaculoService.findAll()));
		cbEspectaculo.setConverter(new StringConverter<Espectaculo>() {
			@Override
			public String toString(Espectaculo e) {
				if (e == null)
					return "";
				return "[" + e.getId() + "] " + e.getNombre() + "  (" + e.getFechaInicio() + " → " + e.getFechaFin()
						+ ")";
			}

			@Override
			public Espectaculo fromString(String s) {
				return null;
			}
		});

		panelCompleto.setVisible(false);
		panelCompleto.setManaged(false);
		lblSinSeleccion.setVisible(true);

		cbEspectaculo.getSelectionModel().selectedItemProperty().addListener((obs, viejo, nuevo) -> {
			if (nuevo != null)
				mostrarDetalle(nuevo);
		});
	}

	private void mostrarDetalle(Espectaculo esp) {
		lblSinSeleccion.setVisible(false);
		panelCompleto.setVisible(true);
		panelCompleto.setManaged(true);

		lblId.setText(String.valueOf(esp.getId()));
		lblNombre.setText(esp.getNombre());
		lblPeriodo.setText(esp.getFechaInicio() + "  →  " + esp.getFechaFin());

		Optional<Espectaculo> completo = espectaculoService.findByIdCompleto(esp.getId());
		if (completo.isEmpty())
			return;

		Espectaculo c = completo.get();
		lblCoordinador.setText(c.getCoordinador().getNombre() + "  |  " + c.getCoordinador().getEmail());

		if (c.getCoordinador().isSenior()) {
			lblSenior.setText("(Senior)");
			lblSenior.setStyle("-fx-font-weight:bold; -fx-text-fill:#1a7f37;");
		} else {
			lblSenior.setText("");
		}

		taNumeros.setText(construirTextoNumeros(c.getNumeros()));
	}

	private String construirTextoNumeros(List<Numero> numeros) {
		if (numeros == null || numeros.isEmpty())
			return "(Sin números asignados)";

		StringBuilder sb = new StringBuilder();
		for (Numero n : numeros) {
			sb.append(n.getOrden()).append(". ").append(n.getNombre()).append("  (").append(n.getDuracionFormateada())
					.append(" min)\n");

			for (Artista a : n.getArtistas()) {
				String pais = paisesLoader.getNombrePais(a.getNacionalidad());
				if (pais == null)
					pais = a.getNacionalidad();
				String especialidades = a.getEspecialidades().stream().map(Enum::name).sorted()
						.collect(Collectors.joining(", "));

				sb.append("   · ").append(a.getNombre());
				if (a.getApodo() != null && !a.getApodo().isBlank())
					sb.append(" \"").append(a.getApodo()).append("\"");
				sb.append("  |  ").append(pais).append("  |  ").append(especialidades).append("\n");
			}
		}
		return sb.toString();
	}

	@FXML
	private void volver(ActionEvent e) {
		stageManager.switchScene(FxmlView.MAIN);
	}
}
