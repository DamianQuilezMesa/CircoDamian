package com.damianqm.tarea3adt.config;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ResourceBundle;

// Carga los FXML usando Spring para que los controladores tengan los @Autowired inyectados.
@Component
public class SpringFXMLLoader {

	private final ApplicationContext context;
	private final ResourceBundle resourceBundle;

	@Autowired
	public SpringFXMLLoader(ApplicationContext context, ResourceBundle resourceBundle) {
		this.context = context;
		this.resourceBundle = resourceBundle;
	}

	public Parent load(String fxmlPath) throws IOException {
		return prepararLoader(fxmlPath).load();
	}

	public FXMLLoader loadWithLoader(String fxmlPath) throws IOException {
		FXMLLoader loader = prepararLoader(fxmlPath);
		loader.load();
		return loader;
	}

	private FXMLLoader prepararLoader(String fxmlPath) {
		FXMLLoader loader = new FXMLLoader();
		loader.setControllerFactory(context::getBean);
		loader.setResources(resourceBundle);
		loader.setLocation(getClass().getResource(fxmlPath));
		return loader;
	}
}
