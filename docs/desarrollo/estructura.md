# estructura del código

Organizamos el repositorio como un Maven con dos módulos: `server` y `agent`. El primero crea el WAR para Tomcat; el segundo crea un JAR común que GitHub Actions compila con GraalVM para cada sistema operativo.

En `server/src/main/java/uy/edu/isbo/control/` separamos:

- `model`: datos y relaciones entre objetos.
- `repository`: consultas JDBC a MariaDB, sin HTML.
- `service`: validaciones y reglas, por ejemplo cómo se programa un apagado.
- `web/controller`: servlets que reciben formularios y solicitudes.
- `web/view`: información para las páginas JSP.
- `web/filter` y `web/support`: cabeceras y utilidades pequeñas.
- `security`: sesiones, contraseñas, tokens y CSRF.
- `config`: conexiones, inicio y actualización del esquema.

En `agent` dejamos cinco clases: `Agent` para arrancar, `AgentConfig` para la identidad, `AgentTls` para HTTPS, `AgentClient` para consultar órdenes y `ShutdownExecutor` para apagarlas. Los instaladores están en `agent/packaging/`.

Intentamos que las clases sean cortas y tengan una tarea principal. No creamos interfaces ni herencia solo para aparentar más POO: usamos los conceptos pedidos donde tienen sentido.
