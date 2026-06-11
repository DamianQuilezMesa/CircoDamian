Esta carpeta contiene los JAR de ObjectDB 2.9.5 que no están en Maven Central.
El pom.xml los declara como dependencia normal con coordenadas com.objectdb:objectdb:2.9.5,
por lo que antes de la primera compilación hay que instalarlos en el repositorio local:

    mvn install:install-file -Dfile=lib/objectdb-2.9.5.jar ^
        -DgroupId=com.objectdb -DartifactId=objectdb ^
        -Dversion=2.9.5 -Dpackaging=jar

Una vez instalado, Maven lo localiza desde ~/.m2 y la dependencia se resuelve normalmente.
La carpeta objectdb-2.9.5/ contiene la distribución completa (binarios, doc, licencia)
solo a título informativo: no es necesaria para compilar ni ejecutar.
