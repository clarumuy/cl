# qué revisamos antes de la entrega

Reorganizamos el servidor y el agente, revisamos la estructura Maven y dejamos preparadas las pruebas automatizadas. Queremos diferenciar las pruebas que pudimos hacer de las que necesitan un entorno real, para no decir que algo funciona si todavía no lo comprobamos.

## comprobaciones realizadas en el entorno de trabajo

- Compilamos el código del agente con `javac --release 17`.
- Probamos la creación y conservación de su identidad, la lectura del certificado público y el rechazo de una configuración equivocada.
- Comprobamos la sintaxis de los instaladores Bash.
- Compilamos sintácticamente las clases principales del servidor usando interfaces locales de prueba. **Esto no reemplaza** compilar el WAR con las dependencias de Maven.
- Revisamos los archivos YAML del despliegue y de los workflows.
- Generamos el informe PDF y revisamos sus páginas y diagramas.

## pruebas preparadas pero todavía no ejecutadas aquí

- `mvn clean verify` con las dependencias reales del servidor.
- `docker compose up --build` y la integración entre Tomcat, Caddy y MariaDB.
- Compilación GraalVM Native Image en los cuatro sistemas de GitHub Actions.
- Instalación, inicio automático y conexión HTTPS en máquinas Windows, Linux y macOS reales.
- Apagado físico individual y coordinado de dos o más equipos autorizados.

El workflow `ci.yml` ejecuta las primeras pruebas de Java y de integración en GitHub. `release.yml` compila y publica los paquetes cuando creemos una etiqueta de versión, siempre que sus trabajos terminen correctamente. Como todavía no ejecutamos esos workflows desde GitHub, no afirmamos que hayan pasado.

Para completar la evidencia de la consigna, después de levantar el servidor tenemos que tomar capturas reales y demostrar un apagado con equipos de prueba. Las imágenes que dejamos en `docs/academico/capturas/` son ejemplos visuales, no capturas de una ejecución real.
