# pruebas que preparamos

- `mvn clean verify`: compila y ejecuta las pruebas de Java de ambos módulos.
- `bash tests/agent-smoke.sh`: compila el agente sin dependencias y comprueba configuración, identidad persistente y rechazo de cambio de servidor.
- `bash tests/installer-unix-smoke.sh`: verifica sintaxis de instaladores Unix.
- `bash tests/integration.sh`: sobre un Docker descartable, prueba login, sesión, CSRF, altas, edición, bajas, agentes, órdenes, ACK y datos después de reiniciar los contenedores.
- `.github/workflows/ci.yml`: ejecuta pruebas Java e integración automáticamente al subir cambios.
- `.github/workflows/release.yml`: compila WAR y ejecutables nativos y los publica solo si los pasos de compilación terminan sin errores.

Para el informe necesitamos, además, una prueba desde la red del instituto con al menos dos computadoras: instalar agentes, aprobarlos, ver estados, probar apagado individual y por laboratorio. No debemos decir que un equipo se apagó si solo vimos que perdió la conexión.
