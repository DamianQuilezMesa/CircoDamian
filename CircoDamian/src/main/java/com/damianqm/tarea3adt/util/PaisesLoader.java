package com.damianqm.tarea3adt.util;

import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Carga el listado de países (código ISO 3166-1 alpha-2 → nombre).
 * Se usa para validar nacionalidades y mostrarlas en los combos.
 */
@Component
public class PaisesLoader {

    private final Map<String, String> paises = new LinkedHashMap<>();

    public PaisesLoader() {
        paises.put("AF", "Afganistán");
        paises.put("AL", "Albania");
        paises.put("DE", "Alemania");
        paises.put("AD", "Andorra");
        paises.put("AO", "Angola");
        paises.put("AR", "Argentina");
        paises.put("AM", "Armenia");
        paises.put("AU", "Australia");
        paises.put("AT", "Austria");
        paises.put("AZ", "Azerbaiyán");
        paises.put("BE", "Bélgica");
        paises.put("BO", "Bolivia");
        paises.put("BA", "Bosnia y Herzegovina");
        paises.put("BR", "Brasil");
        paises.put("BG", "Bulgaria");
        paises.put("CA", "Canadá");
        paises.put("CL", "Chile");
        paises.put("CN", "China");
        paises.put("CO", "Colombia");
        paises.put("KR", "Corea del Sur");
        paises.put("CR", "Costa Rica");
        paises.put("HR", "Croacia");
        paises.put("CU", "Cuba");
        paises.put("DK", "Dinamarca");
        paises.put("EC", "Ecuador");
        paises.put("EG", "Egipto");
        paises.put("SV", "El Salvador");
        paises.put("AE", "Emiratos Árabes Unidos");
        paises.put("SK", "Eslovaquia");
        paises.put("SI", "Eslovenia");
        paises.put("ES", "España");
        paises.put("US", "Estados Unidos");
        paises.put("EE", "Estonia");
        paises.put("ET", "Etiopía");
        paises.put("PH", "Filipinas");
        paises.put("FI", "Finlandia");
        paises.put("FR", "Francia");
        paises.put("GE", "Georgia");
        paises.put("GH", "Ghana");
        paises.put("GR", "Grecia");
        paises.put("GT", "Guatemala");
        paises.put("HN", "Honduras");
        paises.put("HU", "Hungría");
        paises.put("IN", "India");
        paises.put("ID", "Indonesia");
        paises.put("IQ", "Irak");
        paises.put("IR", "Irán");
        paises.put("IE", "Irlanda");
        paises.put("IS", "Islandia");
        paises.put("IL", "Israel");
        paises.put("IT", "Italia");
        paises.put("JP", "Japón");
        paises.put("JO", "Jordania");
        paises.put("KZ", "Kazajistán");
        paises.put("KE", "Kenia");
        paises.put("LV", "Letonia");
        paises.put("LB", "Líbano");
        paises.put("LY", "Libia");
        paises.put("LT", "Lituania");
        paises.put("LU", "Luxemburgo");
        paises.put("MK", "Macedonia del Norte");
        paises.put("MY", "Malasia");
        paises.put("MA", "Marruecos");
        paises.put("MX", "México");
        paises.put("MD", "Moldavia");
        paises.put("MO", "Mónaco");
        paises.put("MN", "Mongolia");
        paises.put("ME", "Montenegro");
        paises.put("MZ", "Mozambique");
        paises.put("NA", "Namibia");
        paises.put("NP", "Nepal");
        paises.put("NI", "Nicaragua");
        paises.put("NG", "Nigeria");
        paises.put("NO", "Noruega");
        paises.put("NZ", "Nueva Zelanda");
        paises.put("NL", "Países Bajos");
        paises.put("PK", "Pakistán");
        paises.put("PA", "Panamá");
        paises.put("PY", "Paraguay");
        paises.put("PE", "Perú");
        paises.put("PL", "Polonia");
        paises.put("PT", "Portugal");
        paises.put("PR", "Puerto Rico");
        paises.put("GB", "Reino Unido");
        paises.put("CZ", "República Checa");
        paises.put("DO", "República Dominicana");
        paises.put("RO", "Rumanía");
        paises.put("RU", "Rusia");
        paises.put("RS", "Serbia");
        paises.put("SG", "Singapur");
        paises.put("SY", "Siria");
        paises.put("SO", "Somalia");
        paises.put("LK", "Sri Lanka");
        paises.put("ZA", "Sudáfrica");
        paises.put("SD", "Sudán");
        paises.put("SE", "Suecia");
        paises.put("CH", "Suiza");
        paises.put("TH", "Tailandia");
        paises.put("TW", "Taiwán");
        paises.put("TZ", "Tanzania");
        paises.put("TN", "Túnez");
        paises.put("TR", "Turquía");
        paises.put("UA", "Ucrania");
        paises.put("UG", "Uganda");
        paises.put("UY", "Uruguay");
        paises.put("UZ", "Uzbekistán");
        paises.put("VE", "Venezuela");
        paises.put("VN", "Vietnam");
        paises.put("YE", "Yemen");
        paises.put("ZM", "Zambia");
        paises.put("ZW", "Zimbabue");
    }

    /** Devuelve el mapa completo código → nombre. */
    public Map<String, String> getPaises() {
        return paises;
    }

    /** Devuelve el nombre del país para un código ISO, o null si no existe. */
    public String getNombrePais(String codigo) {
        if (codigo == null) return null;
        return paises.get(codigo.trim().toUpperCase());
    }

    /** Comprueba si un código ISO es válido. */
    public boolean esCodigoValido(String codigo) {
        if (codigo == null) return false;
        return paises.containsKey(codigo.trim().toUpperCase());
    }
}
