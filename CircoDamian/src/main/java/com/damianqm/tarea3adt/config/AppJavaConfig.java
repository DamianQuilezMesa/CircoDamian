package com.damianqm.tarea3adt.config;

import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.util.ResourceBundle;

// El StageManager es @Lazy porque el Stage todavía no existe cuando Spring
// arranca; se crea en start() de la clase principal una vez JavaFX está listo.
@Configuration
public class AppJavaConfig {

	@Autowired
	private SpringFXMLLoader springFXMLLoader;

	@Bean
	public ResourceBundle resourceBundle() {
		return ResourceBundle.getBundle("Bundle");
	}

	@Bean
	@Lazy
	public StageManager stageManager(Stage stage) {
		return new StageManager(springFXMLLoader, stage);
	}
}
