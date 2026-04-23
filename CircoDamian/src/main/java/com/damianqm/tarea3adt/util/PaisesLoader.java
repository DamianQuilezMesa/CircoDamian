package com.damianqm.tarea3adt.util;

import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Carga los países desde /resources/paises.xml.
 * Guarda un Map<códigoISO, nombre> ordenado alfabéticamente por nombre.
 */
@Component
public class PaisesLoader {

    private Map<String, String> paises;

    public Map<String, String> getPaises() {
        if (paises == null) {
            paises = cargarDesdeXml();
        }
        return paises;
    }

    /** Comprueba si un código ISO existe. */
    public boolean esCodigoValido(String codigo) {
        if (codigo == null || codigo.isBlank()) return false;
        return getPaises().containsKey(codigo.toUpperCase().trim());
    }

    /** Devuelve el nombre del país, o null si el código no existe. */
    public String getNombrePais(String codigo) {
        if (codigo == null) return null;
        return getPaises().get(codigo.toUpperCase().trim());
    }

    private Map<String, String> cargarDesdeXml() {
        Map<String, String> resultado = new LinkedHashMap<>();
        try (InputStream is = getClass().getResourceAsStream("/paises.xml")) {
            if (is == null) {
                System.err.println("No se encontró /paises.xml en el classpath.");
                return resultado;
            }
            Document doc = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder().parse(is);
            doc.getDocumentElement().normalize();
            NodeList nodos = doc.getElementsByTagName("pais");
            for (int i = 0; i < nodos.getLength(); i++) {
                Node nodo = nodos.item(i);
                String id = "";
                String nombre = "";
                NodeList hijos = nodo.getChildNodes();
                for (int j = 0; j < hijos.getLength(); j++) {
                    Node hijo = hijos.item(j);
                    if ("id".equals(hijo.getNodeName())) {
                        id = hijo.getTextContent().trim();
                    } else if ("nombre".equals(hijo.getNodeName())) {
                        nombre = hijo.getTextContent().trim();
                    }
                }
                if (!id.isEmpty() && !nombre.isEmpty()) {
                    resultado.put(id.toUpperCase(), nombre);
                }
            }
            // Devolver ordenado por nombre
            Map<String, String> ordenado = new LinkedHashMap<>();
            resultado.entrySet().stream()
                    .sorted(Map.Entry.comparingByValue())
                    .forEach(e -> ordenado.put(e.getKey(), e.getValue()));
            return ordenado;
        } catch (Exception ex) {
            System.err.println("Error leyendo paises.xml: " + ex.getMessage());
            return resultado;
        }
    }
}
