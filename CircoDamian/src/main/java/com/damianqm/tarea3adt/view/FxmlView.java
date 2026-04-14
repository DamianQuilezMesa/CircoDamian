package com.damianqm.tarea3adt.view;

import java.util.ResourceBundle;

/**
 * Enumerado con todas las vistas FXML de la aplicación CIRCO.
 */
public enum FxmlView {

    BIENVENIDA {
        @Override public String getTitle()    { return getStr("bienvenida.title"); }
        @Override public String getFxmlFile() { return "/fxml/Bienvenida.fxml"; }
    },
    LOGIN {
        @Override public String getTitle()    { return getStr("login.title"); }
        @Override public String getFxmlFile() { return "/fxml/Login.fxml"; }
    },
    MAIN {
        @Override public String getTitle()    { return getStr("main.title"); }
        @Override public String getFxmlFile() { return "/fxml/Main.fxml"; }
    },
    REGISTRO_PERSONA {
        @Override public String getTitle()    { return getStr("registro.title"); }
        @Override public String getFxmlFile() { return "/fxml/RegistroPersona.fxml"; }
    },
    MODIFICAR_PERSONA {
        @Override public String getTitle()    { return getStr("modificar.title"); }
        @Override public String getFxmlFile() { return "/fxml/ModificarPersona.fxml"; }
    },
    GESTION_NUMERO {
        @Override public String getTitle()    { return getStr("numero.gestion.title"); }
        @Override public String getFxmlFile() { return "/fxml/GestionNumero.fxml"; }
    },
    GESTION_ESPECTACULO {
        @Override public String getTitle()    { return getStr("espectaculo.gestion.title"); }
        @Override public String getFxmlFile() { return "/fxml/GestionEspectaculo.fxml"; }
    },
    VER_ESPECTACULOS {
        @Override public String getTitle()    { return getStr("espectaculo.ver.title"); }
        @Override public String getFxmlFile() { return "/fxml/VerEspectaculos.fxml"; }
    },
    BUSCAR_ESPECTACULO {
        @Override public String getTitle()    { return getStr("espectaculo.buscar.title"); }
        @Override public String getFxmlFile() { return "/fxml/BuscarEspectaculo.fxml"; }
    },
    ASIGNAR_ARTISTA {
        @Override public String getTitle()    { return getStr("asignar.artista.title"); }
        @Override public String getFxmlFile() { return "/fxml/AsignarArtista.fxml"; }
    },
    FICHA_ARTISTA {
        @Override public String getTitle()    { return getStr("ficha.artista.title"); }
        @Override public String getFxmlFile() { return "/fxml/FichaArtista.fxml"; }
    },
    USER {
        @Override public String getTitle()    { return getStr("user.title"); }
        @Override public String getFxmlFile() { return "/fxml/User.fxml"; }
    };

    public abstract String getTitle();
    public abstract String getFxmlFile();

    String getStr(String key) {
        return ResourceBundle.getBundle("Bundle").getString(key);
    }
    
}
