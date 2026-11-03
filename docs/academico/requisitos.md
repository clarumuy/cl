# comprobación contra la consigna

Usamos el PDF de `docs/consigna` como referencia. Este archivo muestra en qué parte del proyecto pensamos cada punto.

| Punto | Dónde se encuentra |
|---|---|
| RF1 Laboratorios | `LaboratoryRepository`, `LaboratoryService`, `labs.jspf` |
| RF2 Computadoras | `ComputerRepository`, `ComputerService`, `pcs.jspf` |
| RF3 Usuarios | `UserRepository`, `UserService`, `users.jspf` |
| RF4 Consultas | `DashboardData`, tablas del panel |
| RF5 Estados | `AgentRepository.states`, reporte periódico del agente |
| RF6 Apagado individual | `ShutdownService.computer`, `ShutdownExecutor` |
| RF7 Apagado por laboratorio | `ShutdownService.laboratory`, órdenes con un mismo instante |
| RF8 MariaDB | `database/schema.sql`, `Database`, JDBC |
| RF9 Web HTML/CSS | JSP y `server/src/main/webapp/assets/css` |
| RF10 Cliente-servidor | Navegador, Tomcat, Caddy, MariaDB y agente |
| Asociación | Referencias de `AppServlet` a otros objetos |
| Agregación | `LaboratoryInventory` |
| Singleton | `Database.INSTANCE` |
| Colecciones | `List`, `ArrayList`, `Map` en modelos y repositorios |
| NetBeans | Proyecto Maven importable desde NetBeans |
| XAMPP | El docente autorizó usar Docker Compose para el despliegue en su lugar |

Para completar las evidencias de la defensa necesitamos ejecutar el sistema en la red del instituto y registrar capturas **reales** del panel y del apagado en equipos autorizados. Las imágenes de diseño incluidas en el informe son una vista previa, no una prueba de funcionamiento físico.
