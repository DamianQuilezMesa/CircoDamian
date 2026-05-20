package com.damianqm.tarea3adt.view;

import java.util.ResourceBundle;

public enum FxmlView {

	BIENVENIDA {
		@Override
		public String getTitle() {
			return getTexto("bienvenida.title");
		}

		@Override
		public String getFxmlFile() {
			return "/fxml/Bienvenida.fxml";
		}
	},
	LOGIN {
		@Override
		public String getTitle() {
			return getTexto("login.title");
		}

		@Override
		public String getFxmlFile() {
			return "/fxml/Login.fxml";
		}
	},
	MAIN {
		@Override
		public String getTitle() {
			return getTexto("main.title");
		}

		@Override
		public String getFxmlFile() {
			return "/fxml/Main.fxml";
		}
	},
	REGISTRO_PERSONA {
		@Override
		public String getTitle() {
			return getTexto("registro.title");
		}

		@Override
		public String getFxmlFile() {
			return "/fxml/RegistroPersona.fxml";
		}
	},
	MODIFICAR_PERSONA {
		@Override
		public String getTitle() {
			return getTexto("modificar.title");
		}

		@Override
		public String getFxmlFile() {
			return "/fxml/ModificarPersona.fxml";
		}
	},
	GESTION_NUMERO {
		@Override
		public String getTitle() {
			return getTexto("numero.gestion.title");
		}

		@Override
		public String getFxmlFile() {
			return "/fxml/GestionNumero.fxml";
		}
	},
	GESTION_ESPECTACULO {
		@Override
		public String getTitle() {
			return getTexto("espectaculo.gestion.title");
		}

		@Override
		public String getFxmlFile() {
			return "/fxml/GestionEspectaculo.fxml";
		}
	},
	VER_ESPECTACULOS {
		@Override
		public String getTitle() {
			return getTexto("espectaculo.ver.title");
		}

		@Override
		public String getFxmlFile() {
			return "/fxml/VerEspectaculos.fxml";
		}
	},
	BUSCAR_ESPECTACULO {
		@Override
		public String getTitle() {
			return getTexto("espectaculo.buscar.title");
		}

		@Override
		public String getFxmlFile() {
			return "/fxml/BuscarEspectaculo.fxml";
		}
	},
	FICHA_ARTISTA {
		@Override
		public String getTitle() {
			return getTexto("ficha.artista.title");
		}

		@Override
		public String getFxmlFile() {
			return "/fxml/FichaArtista.fxml";
		}
	},
	HISTORIAL {
		@Override
		public String getTitle() {
			return getTexto("historial.title");
		}

		@Override
		public String getFxmlFile() {
			return "/fxml/Historial.fxml";
		}
	},
	REGISTRAR_INCIDENCIA {
		@Override
		public String getTitle() {
			return getTexto("incidencia.registrar.title");
		}

		@Override
		public String getFxmlFile() {
			return "/fxml/RegistrarIncidencia.fxml";
		}
	},
	CONSULTAR_INCIDENCIAS {
		@Override
		public String getTitle() {
			return getTexto("incidencia.consultar.title");
		}

		@Override
		public String getFxmlFile() {
			return "/fxml/ConsultarIncidencias.fxml";
		}
	};

	public abstract String getTitle();

	public abstract String getFxmlFile();

	String getTexto(String clave) {
		return ResourceBundle.getBundle("Bundle").getString(clave);
	}
}
