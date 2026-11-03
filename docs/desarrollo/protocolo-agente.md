# comunicación del agente

No usamos WebSockets ni otro servidor dentro de las computadoras. Cada agente consulta por HTTPS cada **5 segundos**.

### consultar

`POST /agent-api/poll` con `Authorization: Bearer TOKEN` y cuerpo `id=UUID&name=NOMBRE` (`application/x-www-form-urlencoded`). El servidor actualiza el último contacto y responde `none` o `shutdown|ID|MILISEGUNDOS_RESTANTES`.

Un agente pendiente puede informar su estado, pero no recibe órdenes hasta que nosotros lo aprobamos en el panel.

### confirmar

`POST /agent-api/ack` con el mismo token y cuerpo `id=UUID&commandId=ID`. Si la orden sigue vigente y pertenece a ese equipo, el servidor responde `accepted`. Un ACK repetido se acepta mientras el plazo no haya llegado.

El agente guarda localmente el último ID aceptado antes de intentar apagar. Esto evita repetirlo después de reiniciar. No ejecutamos comandos arbitrarios: la única acción que reconoce el cliente es `shutdown`.

### apagado grupal

Se registran las órdenes de los agentes disponibles de la sala con el mismo tiempo objetivo (25 segundos después de pedir el apagado). El servidor responde a cada agente el tiempo restante según su reloj, así que no depende de que todos los relojes de las PCs estén sincronizados. Cada máquina comienza el proceso lo más cerca posible de ese momento; no podemos garantizar que termine de apagarse al mismo segundo.

### limitaciones reales

Un ACK demuestra recepción, **no** que el apagado haya terminado. Una falla después del ACK puede impedir la ejecución. Las órdenes vencidas no se entregan tarde. Por eso, para comprobar el funcionamiento hay que hacer una prueba real con equipos autorizados.
