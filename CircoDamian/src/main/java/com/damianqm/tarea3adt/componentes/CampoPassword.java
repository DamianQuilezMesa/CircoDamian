package com.damianqm.tarea3adt.componentes;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Componente personalizado para campos de contraseña.
 * Tiene un PasswordField y un TextField apilados en un StackPane
 * (sincronizados), un botón para alternar ver/ocultar y una etiqueta
 * de validación en tiempo real.
 *
 * Reglas: no espacios y más de 2 caracteres.
 *
 * Uso en FXML:
 *   &lt;CampoPassword fx:id="cpPassword"/&gt;
 */
public class CampoPassword extends VBox {

    private final PasswordField pfOculto = new PasswordField();
    private final TextField tfVisible = new TextField();
    private final Button btnMostrar = new Button("👁");
    private final Label lblValidacion = new Label();

    private final StringProperty textProperty = new SimpleStringProperty("");

    public CampoPassword() {
        // Sincronizar los dos campos en ambos sentidos
        tfVisible.textProperty().bindBidirectional(pfOculto.textProperty());
        textProperty.bindBidirectional(pfOculto.textProperty());

        // Al inicio, el campo visible está oculto
        tfVisible.setVisible(false);
        tfVisible.setManaged(false);

        // Que ocupen todo el ancho disponible
        HBox.setHgrow(pfOculto, Priority.ALWAYS);
        HBox.setHgrow(tfVisible, Priority.ALWAYS);
        pfOculto.setPrefHeight(32);
        tfVisible.setPrefHeight(32);
        pfOculto.setMaxWidth(Double.MAX_VALUE);
        tfVisible.setMaxWidth(Double.MAX_VALUE);

        btnMostrar.setPrefWidth(40);
        btnMostrar.setPrefHeight(32);
        btnMostrar.setTooltip(new Tooltip("Mostrar/ocultar contraseña"));
        btnMostrar.setFocusTraversable(false);
        btnMostrar.setOnAction(e -> alternarVisibilidad());

        // Apilamos los dos campos para que ocupen el mismo sitio
        StackPane contenedor = new StackPane(pfOculto, tfVisible);
        HBox.setHgrow(contenedor, Priority.ALWAYS);

        HBox fila = new HBox(5, contenedor, btnMostrar);
        fila.setAlignment(Pos.CENTER_LEFT);

        lblValidacion.setStyle("-fx-font-size:11; -fx-text-fill:#888;");
        lblValidacion.setPadding(new Insets(2, 0, 0, 2));

        this.setSpacing(2);
        this.getChildren().addAll(fila, lblValidacion);

        pfOculto.textProperty().addListener((obs, viejo, nuevo) -> validar(nuevo));
        validar("");
    }

    private void alternarVisibilidad() {
        boolean mostrar = !tfVisible.isVisible();
        tfVisible.setVisible(mostrar);
        tfVisible.setManaged(mostrar);
        pfOculto.setVisible(!mostrar);
        pfOculto.setManaged(!mostrar);
        btnMostrar.setText(mostrar ? "🚫" : "👁");
    }

    /** Actualiza el mensaje y color de validación según el texto. */
    private void validar(String valor) {
        if (valor == null || valor.isEmpty()) {
            lblValidacion.setText("La contraseña debe tener más de 2 caracteres y no contener espacios.");
            lblValidacion.setStyle("-fx-font-size:11; -fx-text-fill:#888;");
            return;
        }
        if (valor.contains(" ")) {
            lblValidacion.setText("✗ La contraseña no puede contener espacios.");
            lblValidacion.setStyle("-fx-font-size:11; -fx-text-fill:#c0392b; -fx-font-weight:bold;");
            return;
        }
        if (valor.length() <= 2) {
            lblValidacion.setText("✗ Faltan " + (3 - valor.length()) + " caracteres (mínimo 3).");
            lblValidacion.setStyle("-fx-font-size:11; -fx-text-fill:#c0392b; -fx-font-weight:bold;");
            return;
        }
        lblValidacion.setText("✓ Contraseña válida.");
        lblValidacion.setStyle("-fx-font-size:11; -fx-text-fill:#27ae60; -fx-font-weight:bold;");
    }

    public String getText()             { return pfOculto.getText(); }
    public void setText(String t)       { pfOculto.setText(t); }
    public StringProperty textProperty() { return textProperty; }

    public void setPromptText(String texto) {
        pfOculto.setPromptText(texto);
        tfVisible.setPromptText(texto);
    }

    public void clear() {
        pfOculto.clear();
    }
}
