package com.damianqm.tarea3adt.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de ObjectDB (modo embebido con fichero local).
 * La URL, usuario y contraseña se leen de application.properties.
 * El EntityManagerFactory lo gestiona directamente IncidenciaRepository.
 */
@Configuration
public class ObjectDbServerConfig {

    @Value("${objectdb.url}")
    private String url;

    @Value("${objectdb.username}")
    private String username;

    @Value("${objectdb.password}")
    private String password;

    public String getUrl()      { return url; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
}
