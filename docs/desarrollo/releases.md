# cómo publicamos una versión

Subimos el repositorio a GitHub. Cada cambio dispara `ci.yml`, que compila, prueba y revisa las funciones de servidor y agente.

Cuando estamos conformes, creamos una etiqueta de versión:

```bash
git tag v1.0.0
git push origin v1.0.0
```

`release.yml` prepara `control-laboratorios-server.zip`, el WAR y agentes nativos de Windows x64, Linux x64, macOS Intel y macOS Apple Silicon. El mismo workflow genera `SHA256SUMS` para verificar las descargas. Los ejecutables se compilan en runners del sistema operativo correspondiente: no se puede crear un ejecutable de macOS directamente con el compilador Windows.

Los paquetes de agentes **no llevan certificados institucionales ni tokens de equipos**. Esos datos se preparan después, al instalar. En macOS no firmamos ni notarizamos automáticamente los binarios de práctica.
