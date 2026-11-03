# cómo instalamos el sistema

## servidor

Necesitamos una computadora fija de la institución, Docker con Compose y una dirección de red estable (por ejemplo, una IP reservada en el router). Desde GitHub Releases descargamos `control-laboratorios-server.zip` y lo extraemos. No hace falta instalar Java, Maven ni MariaDB en esa computadora.

1. Abrimos una terminal dentro de la carpeta `control-laboratorios-server`.
2. En Windows ejecutamos `scripts\servidor.bat`. En Linux, `bash scripts/servidor.sh IP-DE-LA-LAN`.
3. Si es la primera ejecución, el script crea contraseñas aleatorias y muestra la de `admin`. La anotamos en un lugar seguro.
4. Docker inicia MariaDB, Tomcat y Caddy. Cuando el servidor está listo, abrimos `https://IP-DE-LA-LAN`.
5. El archivo **`secrets/ca.crt`** es el certificado público de nuestra instalación. Lo copiamos a los paquetes de agentes usando USB o un medio institucional confiable. **Nunca** compartimos el directorio de claves privadas de Caddy.
6. Permitimos TCP 443 entre los equipos y el servidor, sin publicar el sistema en Internet.

En la primera visita al panel puede aparecer una advertencia del navegador porque la CA es local. Antes de confiar en ella verificamos con el administrador que estamos entrando al servidor correcto. No recomendamos aceptar certificados desconocidos sin verificar.

El archivo `.env` contiene claves propias de la institución. No lo subimos a GitHub. El almacenamiento de MariaDB y certificados queda en volúmenes Docker. **No ejecutamos `docker compose down -v` en una instalación con datos**, porque borra esos volúmenes.

## equipos clientes

Descargamos el agente que coincide con el sistema operativo y su arquitectura: `windows-x64`, `linux-x64`, `macos-x64` o `macos-arm64`. Verificamos el SHA-256 publicado en la Release, extraemos el paquete y copiamos en esa carpeta nuestro `ca.crt` del servidor.

**Windows:** abrimos PowerShell como administrador en la carpeta del agente y ejecutamos:

```powershell
.\instalar.ps1 -Server 'https://IP-DE-LA-LAN' -CaPath '.\ca.crt'
```

El instalador copia el ejecutable a `Program Files`, protege la identidad en `ProgramData` y crea una tarea que inicia con Windows, sin tener que entrar con un usuario.

**Linux:** `sudo bash ./instalar.sh 'https://IP-DE-LA-LAN' './ca.crt'`. Se registra un servicio systemd.

**macOS:** `sudo bash ./instalar.sh 'https://IP-DE-LA-LAN' './ca.crt'`. Se registra un LaunchDaemon. En Mac puede ser necesario autorizar el ejecutable, ya que estos paquetes de práctica no están firmados ni notarizados.

El equipo se presenta como **pendiente** en el panel. Nosotros lo aprobamos desde **Vincular equipos** y seleccionamos el laboratorio. Antes de aprobar comprobamos que el nombre corresponda a la PC que instalamos.

## volver a iniciar y resolver problemas

- Servidor: `docker compose up -d` desde la carpeta del despliegue.
- Estado: `docker compose ps`.
- Errores: `docker compose logs --tail=80 web caddy db`.
- Windows: revisamos la tarea `ControlLaboratoriosAgent`.
- Linux: `systemctl status control-laboratorios-agent`.
- macOS: `launchctl print system/uy.edu.isbo.control-agent`.

Si una computadora aparece **Sin respuesta**, revisamos que esté encendida, que el agente corra y que alcance `https://IP-DE-LA-LAN`. No la damos por apagada solamente por perder la conexión.
