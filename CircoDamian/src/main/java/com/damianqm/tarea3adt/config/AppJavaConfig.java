package com.damianqm.tarea3adt.config;

import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.util.ResourceBundle;

/**
 * Configuración de Spring para integrar JavaFX.
 * El StageManager se marca como Lazy porque el Stage no existe
 * hasta que JavaFX ha arrancado y llama a start().
 */
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
