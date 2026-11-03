# actualizar una instalación

## servidor

Antes de cambiar de versión hacemos una copia de seguridad de la base de datos y conservamos `.env` y los volúmenes de Caddy. Descargamos la nueva Release, reemplazamos los archivos de despliegue pero **no** la configuración privada y ejecutamos `docker compose up -d --build`. La aplicación comprueba las columnas del esquema viejo y adapta las necesarias sin borrar usuarios ni equipos.

Nunca usamos `docker compose down -v` para actualizar. Si cambiamos de IP o de CA, debemos revisar cómo afectará a los agentes: no se migra automáticamente a un servidor nuevo sin validación.

## agentes

Descargamos el nuevo paquete para cada sistema operativo, verificamos su SHA-256 y volvemos a ejecutar el instalador con la misma URL y CA. Las credenciales locales no cambian, por eso no se necesitan aprobaciones nuevas.

La desinstalación no borra la identidad por defecto. Para retirarlo definitivamente también eliminamos el equipo desde el panel, que revoca su acceso.
