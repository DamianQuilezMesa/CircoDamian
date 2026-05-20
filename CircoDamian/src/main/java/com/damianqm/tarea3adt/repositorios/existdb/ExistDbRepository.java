package com.damianqm.tarea3adt.repositorios.existdb;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.w3c.dom.Document;
import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Database;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.XMLResource;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;

// Clase dedicada exclusivamente a la conexión, persistencia y recuperación
// de documentos XML en eXistDB. Los datos de conexión vienen de application.properties.
@Repository
public class ExistDbRepository {

	private static final String DRIVER = "org.exist.xmldb.DatabaseImpl";

	@Value("${existdb.url}")
	private String existdbUrl;

	@Value("${existdb.collection}")
	private String existdbCollection;

	@Value("${existdb.username}")
	private String existdbUsername;

	@Value("${existdb.password}")
	private String existdbPassword;

	private void registrarDriver() throws XMLDBException, ReflectiveOperationException {
		Database db = (Database) Class.forName(DRIVER).getDeclaredConstructor().newInstance();
		db.setProperty("create-database", "true");
		DatabaseManager.registerDatabase(db);
	}

	// Abre la colección /informes; si no existe la crea
	private Collection obtenerColeccion() throws Exception {
		registrarDriver();
		String uri = existdbUrl + existdbCollection;
		Collection col = DatabaseManager.getCollection(uri, existdbUsername, existdbPassword);

		if (col == null) {
			Collection root = DatabaseManager.getCollection(existdbUrl, existdbUsername, existdbPassword);
			if (root == null)
				throw new RuntimeException("No se puede conectar a eXistDB en: " + existdbUrl);

			org.xmldb.api.base.Service svc = root.getService("CollectionManagementService", "1.0");
			org.xmldb.api.modules.CollectionManagementService cms = (org.xmldb.api.modules.CollectionManagementService) svc;
			col = cms.createCollection(existdbCollection.replace("/", ""));
			root.close();
		}
		return col;
	}

	// CU4B – guarda el documento DOM en la colección informes de eXistDB
	public void guardarInforme(String nombreFichero, Document documento) throws Exception {
		Collection col = obtenerColeccion();
		try {
			String xmlString = domAString(documento);
			XMLResource recurso = (XMLResource) col.createResource(nombreFichero, "XMLResource");
			recurso.setContent(xmlString);
			col.storeResource(recurso);
		} finally {
			col.close();
		}
	}

	// Recupera el XML almacenado como String, o null si no existe
	public String recuperarInforme(String nombreFichero) throws Exception {
		Collection col = obtenerColeccion();
		try {
			XMLResource recurso = (XMLResource) col.getResource(nombreFichero);
			if (recurso == null)
				return null;
			return (String) recurso.getContent();
		} finally {
			col.close();
		}
	}

	public boolean existeInforme(String nombreFichero) throws Exception {
		Collection col = obtenerColeccion();
		try {
			return col.getResource(nombreFichero) != null;
		} finally {
			col.close();
		}
	}

	private String domAString(Document doc) throws Exception {
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(javax.xml.transform.OutputKeys.ENCODING, "UTF-8");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

		StringWriter writer = new StringWriter();
		transformer.transform(new DOMSource(doc), new StreamResult(writer));
		return writer.toString();
	}
}
