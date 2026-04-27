package com.damianqm.tarea3adt.config;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ResourceBundle;

/**
 * Carga los archivos FXML integrándolos con Spring. Los controladores
 * declarados en fx:controller se obtienen del contexto de Spring, así vienen
 * con sus @Autowired ya inyectados.
 */
@Component
public class SpringFXMLLoader {

	private final ApplicationContext context;
	private final ResourceBundle resourceBundle;

	@Autowired
	public SpringFXMLLoader(ApplicationContext context, ResourceBundle resourceBundle) {
		this.context = context;
		this.resourceBundle = resourceBundle;
	}

	/** Carga un FXML y devuelve el nodo raíz. */
	public Parent load(String fxmlPath) throws IOException {
		return prepararLoader(fxmlPath).load();
	}

	/**
	 * Carga un FXML y devuelve el FXMLLoader. Útil para obtener también el
	 * controller con getController(), necesario por ejemplo para enganchar F1 a su
	 * método de ayuda.
	 */
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
