# CIRCO – Tarea 3 Acceso a Datos + Interfaces

**Autor:** Damián Quílez Mesa – 2º DAM – curso 2025/2026

Proyecto de escritorio en Java para gestionar una empresa circense, combinando
**Spring Boot** (capa de datos con JPA/Hibernate) y **JavaFX** (interfaz gráfica).

## Requisitos

- **Java 21** o superior
- **Maven 3.8+**
- **MySQL** corriendo en `localhost:3306` con usuario `root` sin contraseña
  (si tu configuración es distinta, modifica `src/main/resources/application.properties`).

## Arranque

No hace falta crear la base de datos manualmente. Al ejecutar la app:

1. **Spring crea la base de datos** `circo_damianqm` si no existe
   (gracias al parámetro `createDatabaseIfNotExist=true`).
2. **Hibernate crea las tablas** (`ddl-auto=update`).
3. **Se ejecuta `data.sql`** e inserta los datos de prueba.

Los inserts usan `INSERT IGNORE`, así que si ya hay datos no se duplican:
puedes arrancar la app las veces que quieras sin problemas.

### Comandos

```bash
mvn clean install
mvn javafx:run
```

## Usuarios de prueba

| Usuario   | Contraseña   | Perfil       |
|-----------|--------------|--------------|
| admin     | admin        | ADMIN        |
| laura     | laura123     | COORDINACION |
| carlos    | carlos123    | COORDINACION |
| marco     | marco123     | ARTISTA      |
| sofia     | sofia123     | ARTISTA      |
| ivan      | ivan123      | ARTISTA      |
| ana       | ana123       | ARTISTA      |
| kenji     | kenji123     | ARTISTA      |
| elena     | elena123     | ARTISTA      |
| luis      | luis123      | ARTISTA      |

## Estructura del proyecto

```
src/main/java/com/damianqm/tarea3adt
├── Tarea3AdtApplication.java   (clase principal)
├── componentes/                 (componente CampoPassword)
├── config/                      (Spring + JavaFX: StageManager, FXMLLoader)
├── controller/                  (controladores JavaFX)
├── modelo/                      (entidades JPA)
├── repositorios/                (Spring Data repositories)
├── services/                    (lógica de negocio)
├── util/                        (PaisesLoader)
└── view/                        (enum FxmlView)

src/main/resources
├── application.properties       (configuración Spring/MySQL)
├── data.sql                     (datos iniciales)
├── paises.xml                   (lista de códigos ISO)
├── Bundle.properties            (textos de las ventanas)
├── styles/Styles.css            (hoja de estilos)
└── fxml/                        (vistas FXML)
```

## Atajos

- **F1**: abre la ayuda contextual de la pantalla actual (en todas las pantallas).
