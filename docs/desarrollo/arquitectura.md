# cómo funciona la arquitectura

El servidor corre en Docker Compose con tres contenedores: Caddy (HTTPS), Tomcat (página y API) y MariaDB (datos). El navegador se comunica con Caddy, Caddy pasa la solicitud a Tomcat y Tomcat usa JDBC para guardar o leer datos.

Dentro de Java seguimos un recorrido fácil de entender: **Servlet → Servicio → Repositorio → MariaDB**. Los JSP muestran los datos, no deciden permisos ni ejecutan consultas SQL.

Los agentes se conectan desde las computadoras hacia el mismo Caddy. No abrimos puertos en las PCs, ni necesitamos saber su IP para entregarles órdenes. Cada uno presenta su UUID y un token aleatorio. El servidor guarda el hash de ese token y no acepta que alguien tome una identidad registrada con otra credencial. El agente nuevo queda pendiente hasta que lo aprobamos.

Para HTTPS usamos la CA local de Caddy. El certificado público viaja al cliente por USB o un medio confiable, separado de las claves privadas. Java valida el certificado y el nombre del servidor con los mecanismos estándar de TLS.

## conceptos de POO

- **Asociación:** `AppServlet` tiene referencias a objetos como `DashboardData` y `AdminActions`, que colaboran para responder solicitudes.
- **Agregación:** `LaboratoryInventory` reúne un `Laboratory` y una lista de `Computer`. Si dejamos de usar ese inventario, los objetos y registros siguen existiendo.
- **Singleton:** `Database.INSTANCE` es un `enum` con una sola instancia que administra el acceso a las conexiones.
- **Colecciones:** usamos `List`, `ArrayList` y `Map` para devolver consultas, contar equipos y agrupar computadoras por laboratorio.

Las clases modelo principales son `Laboratory`, `Computer`, `User`, `LaboratoryInventory` y los modelos del agente. Los records permiten representar datos simples sin getters y setters repetitivos.
