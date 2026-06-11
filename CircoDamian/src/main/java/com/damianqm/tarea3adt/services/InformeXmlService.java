package com.damianqm.tarea3adt.services;

import com.damianqm.tarea3adt.modelo.Artista;
import com.damianqm.tarea3adt.modelo.Espectaculo;
import com.damianqm.tarea3adt.modelo.Numero;
import com.damianqm.tarea3adt.repositorios.existdb.ExistDbRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InformeXmlService {

	private static final DateTimeFormatter FMT_FECHA_HORA = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
	private static final DateTimeFormatter FMT_FECHA = DateTimeFormatter.ofPattern("dd-MM-yyyy");

	private static final String CARPETA_FICHEROS = "ficheros";

	@Autowired
	private ExistDbRepository existDbRepository;

	// CU4B: genera el XML con DOM, lo guarda en /ficheros y lo sube a eXistDB
	public Path generarYExportar(Espectaculo espectaculo) throws Exception {
		// Construye el documento DOM
		Document doc = construirDocumentoDom(espectaculo);

		// Calcular nombre del fichero
		String nombreFichero = nombreFichero(espectaculo.getId());

		// Guardar en /ficheros (disco local)
		Path rutaLocal = guardarEnDisco(doc, nombreFichero);

		// Persistir en eXistDB
		existDbRepository.guardarInforme(nombreFichero, doc);

		return rutaLocal;
	}

	// Devuelve el nombre del fichero del informe para un id de espectáculo.
	private String nombreFichero(Long idEspectaculo) {
		return String.format("informe_espectaculo%02d.xml", idEspectaculo);
	}

	// Indica si ya se ha exportado alguna vez el informe XML de este espectáculo
	// (comprobando su existencia en la carpeta /ficheros).
	public boolean existeXml(Long idEspectaculo) {
		if (idEspectaculo == null)
			return false;
		return Files.exists(Paths.get(CARPETA_FICHEROS, nombreFichero(idEspectaculo)));
	}

	// Regenera el informe SOLO si ya existía previamente (mismo criterio que se usa
	// al modificar un espectáculo). Es a prueba de fallos: si eXistDB no está
	// disponible, el fichero local se regenera igualmente y NO se propaga el error,
	// de modo que un eXistDB caído nunca bloquea la edición del espectáculo.
	public void regenerarSiExiste(Espectaculo espectaculo) {
		if (espectaculo == null || !existeXml(espectaculo.getId()))
			return;
		try {
			generarYExportar(espectaculo);
		} catch (Exception e) {
			System.err.println("Aviso: no se pudo regenerar/subir el informe XML del espectáculo " + espectaculo.getId()
					+ " (¿eXistDB arrancado?): " + e.getMessage());
		}
	}

	private Document construirDocumentoDom(Espectaculo espectaculo) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.newDocument();

		// <informe>
		Element raiz = doc.createElement("informe");
		doc.appendChild(raiz);

		// <fechahora>
		raiz.appendChild(texto(doc, "fechahora", LocalDateTime.now().format(FMT_FECHA_HORA)));

		// <espectaculo>
		Element elEsp = doc.createElement("espectaculo");
		raiz.appendChild(elEsp);

		elEsp.appendChild(texto(doc, "id", String.valueOf(espectaculo.getId())));
		elEsp.appendChild(texto(doc, "nombre", espectaculo.getNombre()));
		elEsp.appendChild(texto(doc, "fechaini", espectaculo.getFechaInicio().format(FMT_FECHA)));
		elEsp.appendChild(texto(doc, "fechafin", espectaculo.getFechaFin().format(FMT_FECHA)));

		// <coordinacion>
		Element elCoord = doc.createElement("coordinacion");
		elCoord.appendChild(texto(doc, "nombre", espectaculo.getCoordinador().getNombre()));
		elCoord.appendChild(texto(doc, "email", espectaculo.getCoordinador().getEmail()));
		elCoord.appendChild(texto(doc, "senior", String.valueOf(espectaculo.getCoordinador().isSenior())));
		elEsp.appendChild(elCoord);

		// <numeros>
		Element elNumeros = doc.createElement("numeros");
		List<Numero> numerosOrdenados = espectaculo.getNumeros().stream()
				.sorted(Comparator.comparingInt(Numero::getOrden)).collect(Collectors.toList());

		for (Numero numero : numerosOrdenados) {
			elNumeros.appendChild(construirNumero(doc, numero));
		}
		elEsp.appendChild(elNumeros);

		return doc;
	}

	private Element construirNumero(Document doc, Numero numero) {
		Element elNumero = doc.createElement("numero");

		elNumero.appendChild(texto(doc, "orden", String.valueOf(numero.getOrden())));
		elNumero.appendChild(texto(doc, "nombre", numero.getNombre()));
		elNumero.appendChild(texto(doc, "duracion", String.valueOf(numero.getDuracion())));

		Element elArtistas = doc.createElement("artistas");

		for (Artista a : numero.getArtistas()) {
			elArtistas.appendChild(construirArtista(doc, a));
		}

		elNumero.appendChild(elArtistas);
		return elNumero;
	}

	private Element construirArtista(Document doc, Artista artista) {
		Element el = doc.createElement("artista");

		el.appendChild(texto(doc, "nombre", artista.getNombre()));
		el.appendChild(texto(doc, "nacionalidad", artista.getNacionalidad()));
		el.appendChild(texto(doc, "email", artista.getEmail()));

		// Especialidades separadas por coma, ordenadas alfabéticamente
		String especialidades = artista.getEspecialidades().stream().map(Enum::name).sorted()
				.collect(Collectors.joining(","));
		el.appendChild(texto(doc, "especialidades", especialidades));

		// <apodo> solo si tiene valor
		if (artista.getApodo() != null && !artista.getApodo().isBlank()) {
			el.appendChild(texto(doc, "apodo", artista.getApodo()));
		}

		return el;
	}

	private Path guardarEnDisco(Document doc, String nombreFichero) throws Exception {
		Path carpeta = Paths.get(CARPETA_FICHEROS);
		Files.createDirectories(carpeta);

		Path ruta = carpeta.resolve(nombreFichero);

		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

		transformer.transform(new DOMSource(doc), new StreamResult(new File(ruta.toString())));

		return ruta.toAbsolutePath();
	}

	private Element texto(Document doc, String tag, String valor) {
		Element el = doc.createElement(tag);
		el.appendChild(doc.createTextNode(valor != null ? valor : ""));
		return el;
	}
}
