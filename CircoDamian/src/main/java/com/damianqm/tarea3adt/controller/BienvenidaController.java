package com.damianqm.tarea3adt.controller;

import com.damianqm.tarea3adt.config.StageManager;
import com.damianqm.tarea3adt.view.FxmlView;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

/** Pantalla de Bienvenida (inicio). */
@Controller
public class BienvenidaController {

	@Lazy
	@Autowired
	private StageManager stageManager;

	@FXML
	private void verEspectaculos(ActionEvent event) {
		stageManager.switchScene(FxmlView.VER_ESPECTACULOS);
	}

	@FXML
	private void irALogin(ActionEvent event) {
		stageManager.switchScene(FxmlView.LOGIN);
	}

}
