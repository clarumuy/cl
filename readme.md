# control de laboratorios

Proyecto de **Programación Avanzada, 2026**. Somos del **2.º MJ matutino del ISBO** y desarrollamos una página web para que desde la Oficina de Control (Área 433) podamos registrar laboratorios, consultar las computadoras y enviar órdenes de apagado sin recorrer cada sala.

El servidor usa **Java 17, JSP, HTML, CSS y MariaDB**. Lo organizamos como un proyecto Maven que podemos abrir desde NetBeans. Para ejecutarlo elegimos Docker Compose, con autorización del docente, porque así no tenemos que instalar la base y Tomcat por separado. Los equipos se comunican con un agente Java pequeño que compilamos como ejecutable nativo.

## cómo está organizado

```text
server/       aplicación web (servlets, servicios, repositorios, JSP)
agent/        programa que corre en cada computadora
infra/        Docker y Caddy (HTTPS)
database/     creación de las tablas en MariaDB
scripts/      arranque del servidor
.github/      pruebas y publicación automática en Releases
tests/        pruebas del sistema completo
docs/         consigna, informe, diagramas y guías
pom.xml       proyecto Maven con los dos módulos
compose.yaml  los tres contenedores del servidor
```

## para instalarlo

**Servidor:** instalamos Docker con Compose, descargamos `control-laboratorios-server.zip` de una Release y ejecutamos `scripts\servidor.bat` (Windows) o `bash scripts/servidor.sh` (Linux). El script prepara `.env`, levanta Caddy/Tomcat/MariaDB y exporta el certificado público en `secrets/ca.crt`. Guardamos la contraseña inicial que nos muestra.

**Equipos:** descargamos el agente de la misma Release para el sistema operativo correcto. Copiamos al paquete el `ca.crt` exportado por el servidor, ejecutamos `instalar.ps1` como administrador o `instalar.sh` con `sudo`, y aprobamos la computadora desde **Vincular equipos**.

[Instrucciones paso a paso](docs/operacion/instalacion.md) · [Uso del panel](docs/operacion/uso.md)

## durante el uso

El agente arranca con el sistema operativo. Cada cinco segundos se conecta por HTTPS a nuestro servidor para informar que sigue disponible y consultar si tiene una orden. El servidor guarda todo en MariaDB. Una orden de apagado tiene ID y hora límite; el agente confirma su recepción antes de intentar ejecutarla. El estado **Sin respuesta** no demuestra por sí solo que la computadora esté apagada.

Para apagar una sala, todas las órdenes llevan un mismo momento de inicio. Esto busca coordinar los equipos, aunque cada computadora puede demorar distinto en terminar de apagarse. La demostración real requiere equipos autorizados y pruebas en la red del instituto.

## desarrollo

Abrimos el `pom.xml` de la raíz en NetBeans. También podemos compilar desde una terminal con Maven y Java 17:

```bash
mvn clean verify
```

La compilación del servidor genera `server/target/ROOT.war`. El módulo `agent` genera `agent/target/control-agent.jar`; en GitHub Actions se convierte en ejecutables para Windows, Linux y macOS con GraalVM Native Image.

Cuando publicamos una etiqueta `v1.0.0`, el workflow `release.yml` arma los paquetes en GitHub Releases. Las claves de cada instalación no se suben al repositorio.

## documentación

- [Consigna original](docs/consigna/requisitos-proyecto.pdf)
- [Informe académico](docs/academico/informe.pdf) y [versión editable](docs/academico/informe.md)
- [Requisitos y dónde se cumplen](docs/academico/requisitos.md)
- [Arquitectura](docs/desarrollo/arquitectura.md)
- [Protocolo del agente](docs/desarrollo/protocolo-agente.md)
- [Preparación para la defensa](docs/academico/defensa.md)

**Importante:** las pruebas automatizadas no sustituyen una prueba física del apagado. Los certificados y contraseñas reales se generan en el servidor al instalar, por eso no aparecen en el ZIP del código. Wake-on-LAN quedó fuera porque es opcional en la consigna.

En [verificación](docs/desarrollo/verificacion.md) anotamos qué pruebas pudimos hacer y cuáles se ejecutan en GitHub o requieren computadoras reales.
