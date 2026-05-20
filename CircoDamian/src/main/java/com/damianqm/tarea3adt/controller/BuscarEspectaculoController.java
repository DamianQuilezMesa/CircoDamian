package com.damianqm.tarea3adt.controller;

import com.damianqm.tarea3adt.config.StageManager;
import com.damianqm.tarea3adt.modelo.Artista;
import com.damianqm.tarea3adt.modelo.Espectaculo;
import com.damianqm.tarea3adt.modelo.Numero;
import com.damianqm.tarea3adt.services.EspectaculoService;
import com.damianqm.tarea3adt.services.InformeXmlService;
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
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

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
	@FXML
	private Button btnExportarXml;
	@FXML
	private Label lblExportMsg;

	@Autowired
	private EspectaculoService espectaculoService;
	@Autowired
	private InformeXmlService informeXmlService;
	@Autowired
	private PaisesLoader paisesLoader;
	@Lazy
	@Autowired
	private StageManager stageManager;

	/** Espectáculo completo actualmente cargado en el panel. */
	private Espectaculo espectaculoActual;

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
		lblExportMsg.setText("");
		btnExportarXml.setDisable(true);

		cbEspectaculo.getSelectionModel().selectedItemProperty().addListener((obs, viejo, nuevo) -> {
			if (nuevo != null)
				mostrarDetalle(nuevo);
			lblExportMsg.setText("");
		});
	}

	private void mostrarDetalle(Espectaculo esp) {
		lblSinSeleccion.setVisible(false);
		panelCompleto.setVisible(true);
		panelCompleto.setManaged(true);
		btnExportarXml.setDisable(false);

		lblId.setText(String.valueOf(esp.getId()));
		lblNombre.setText(esp.getNombre());
		lblPeriodo.setText(esp.getFechaInicio() + "  →  " + esp.getFechaFin());

		Optional<Espectaculo> completo = espectaculoService.findByIdCompleto(esp.getId());
		if (completo.isEmpty())
			return;

		espectaculoActual = completo.get();

		lblCoordinador.setText(espectaculoActual.getCoordinador().getNombre() + "  |  "
				+ espectaculoActual.getCoordinador().getEmail());

		if (espectaculoActual.getCoordinador().isSenior()) {
			lblSenior.setText("(Senior)");
			lblSenior.setStyle("-fx-font-weight:bold; -fx-text-fill:#1a7f37;");
		} else {
			lblSenior.setText("");
		}

		taNumeros.setText(construirTextoNumeros(espectaculoActual.getNumeros()));
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
	private void exportarXml(ActionEvent e) {
		if (espectaculoActual == null)
			return;

		lblExportMsg.setText("");

		Alert confirm = new Alert(
				Alert.AlertType.CONFIRMATION, "Se generará el informe XML del espectáculo '"
						+ espectaculoActual.getNombre() + "' y se guardará en /ficheros y en eXistDB.\n\n¿Continuar?",
				ButtonType.YES, ButtonType.NO);
		confirm.setTitle("Exportar informe XML");
		confirm.showAndWait().ifPresent(btn -> {
			if (btn == ButtonType.YES) {
				try {
					Path ruta = informeXmlService.generarYExportar(espectaculoActual);
					ok("✔  Informe generado: " + ruta.getFileName()
							+ "\nGuardado en /ficheros y en la colección /informes de eXistDB.");
				} catch (Exception ex) {
					error("Error al exportar: " + ex.getMessage());
				}
			}
		});
	}

	@FXML
	private void volver(ActionEvent e) {
		stageManager.switchScene(FxmlView.MAIN);
	}

	private void ok(String m) {
		lblExportMsg.setStyle("-fx-text-fill:#1a7f37; -fx-font-weight:bold;");
		lblExportMsg.setText(m);
	}

	private void error(String m) {
		lblExportMsg.setStyle("-fx-text-fill:#c0392b; -fx-font-weight:bold;");
		lblExportMsg.setText(m);
	}
}
