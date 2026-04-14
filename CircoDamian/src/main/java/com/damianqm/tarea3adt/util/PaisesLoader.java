package com.damianqm.tarea3adt.util;

import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Carga el listado de países desde /resources/paises.xml.
 * Devuelve un Map<código ISO, nombre> ordenado alfabéticamente por nombre.
 * Se cachea en memoria tras la primera carga.
 */
@Component
public class PaisesLoader {

    /** Mapa código → nombre. Se carga una sola vez. */
    private Map<String, String> paises;

    /**
     * Devuelve el mapa de países (código ISO → nombre).
     * Si ya estaba cargado devuelve la copia en caché.
     */
    public Map<String, String> getPaises() {
        if (paises == null) {
            paises = cargarDesdeXml();
        }
        return paises;
    }

    /**
     * Comprueba si un código ISO es válido según el XML.
     */
    public boolean esCodidoValido(String codigo) {
        if (codigo == null || codigo.isBlank()) return false;
        return getPaises().containsKey(codigo.toUpperCase().trim());
    }

    /**
     * Devuelve el nombre del país para un código, o null si no existe.
     */
    public String getNombrePais(String codigo) {
        return getPaises().get(codigo.toUpperCase().trim());
    }

    // ─── Carga XML ────────────────────────────────────────────────────

    private Map<String, String> cargarDesdeXml() {
        Map<String, String> resultado = new LinkedHashMap<>();
        try (InputStream is = getClass().getResourceAsStream("/paises.xml")) {
            if (is == null) {
                System.err.println("[PaisesLoader] No se encontró /paises.xml en el classpath.");
                return resultado;
            }
            Document doc = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder().parse(is);
            doc.getDocumentElement().normalize();
            NodeList nodos = doc.getElementsByTagName("pais");
            for (int i = 0; i < nodos.getLength(); i++) {
                var nodo = nodos.item(i);
                String id = "";
                String nombre = "";
                var hijos = nodo.getChildNodes();
                for (int j = 0; j < hijos.getLength(); j++) {
                    var hijo = hijos.item(j);
                    if ("id".equals(hijo.getNodeName()))
                        id = hijo.getTextContent().trim();
                    else if ("nombre".equals(hijo.getNodeName()))
                        nombre = hijo.getTextContent().trim();
                }
                if (!id.isEmpty() && !nombre.isEmpty())
                    resultado.put(id.toUpperCase(), nombre);
            }
            // Ordenar por nombre
            Map<String, String> ordenado = new LinkedHashMap<>();
            resultado.entrySet().stream()
                    .sorted(Map.Entry.comparingByValue())
                    .forEach(e -> ordenado.put(e.getKey(), e.getValue()));
            return ordenado;
        } catch (Exception ex) {
            System.err.println("[PaisesLoader] Error leyendo paises.xml: " + ex.getMessage());
            return resultado;
        }
    }
}
