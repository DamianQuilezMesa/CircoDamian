package com.damianqm.tarea3adt.config;

import com.damianqm.tarea3adt.view.FxmlView;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Controla el Stage principal y el cambio de pantallas. Tamaño fijo, no redimensionable.
public class StageManager {

	private static final Logger LOG = LoggerFactory.getLogger(StageManager.class);

	private static final double W = 1120;
	private static final double H = 760;

	private final Stage primaryStage;
	private final SpringFXMLLoader springFXMLLoader;

	public StageManager(SpringFXMLLoader springFXMLLoader, Stage stage) {
		this.springFXMLLoader = springFXMLLoader;
		this.primaryStage = stage;
	}

	public void switchScene(FxmlView view) {
		try {
			FXMLLoader loader = springFXMLLoader.loadWithLoader(view.getFxmlFile());
			mostrar(loader.getRoot(), view.getTitle());
		} catch (Exception e) {
			LOG.error("Error al cargar la vista: " + view.getFxmlFile(), e);
			Platform.exit();
		}
	}

	private void mostrar(Parent root, String title) {
		Scene scene = primaryStage.getScene();
		if (scene == null) {
			scene = new Scene(root, W, H);
		} else {
			scene.setRoot(root);
		}

		primaryStage.setTitle(title);
		primaryStage.setScene(scene);
		primaryStage.setMaximized(false);
		primaryStage.setFullScreen(false);
		primaryStage.setWidth(W);
		primaryStage.setHeight(H);
		primaryStage.setMinWidth(W);
		primaryStage.setMinHeight(H);
		primaryStage.setMaxWidth(W);
		primaryStage.setMaxHeight(H);
		primaryStage.setResizable(false);
		primaryStage.centerOnScreen();
		primaryStage.show();
	}
}
