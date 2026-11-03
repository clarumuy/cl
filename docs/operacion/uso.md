# uso del panel

Abrimos la dirección HTTPS del servidor y entramos con una cuenta autorizada. El panel tiene cinco secciones:

1. **Resumen:** cantidades de laboratorios, equipos, disponibles y pendientes.
2. **Laboratorios:** agregamos, editamos o eliminamos salas. Una sala con computadoras no se puede eliminar hasta resolver sus equipos.
3. **Computadoras:** consultamos, editamos y eliminamos registros. Podemos cargar una IP local manualmente, pero esos registros no se apagan a distancia hasta vincular un agente.
4. **Vincular equipos:** aprobamos las computadoras que se conectan por primera vez y las asignamos a una sala.
5. **Usuarios:** creamos y editamos cuentas. No se puede borrar la cuenta que estamos usando ni la última cuenta.

## apagar una computadora

Buscamos un equipo disponible en **Computadoras**, seleccionamos **Apagar** y confirmamos. El servidor guarda una orden que el agente recibirá en su siguiente consulta. El agente confirma que la aceptó y espera el momento programado para pedirle a su sistema operativo que se apague normalmente.

## apagar un laboratorio

En **Laboratorios** seleccionamos **Apagar todos**. Se crean órdenes para los agentes activos de esa sala, con un mismo momento de inicio, para que el apagado sea lo más coordinado posible. Los equipos desconectados o sin agente no reciben la orden.

Antes de hacerlo avisamos que se guarden los archivos abiertos. El cierre no es forzado por defecto, pero el sistema operativo puede impedir o demorar el apagado si hay tareas sin guardar.

## qué significa el estado

**Disponible** quiere decir que el agente se comunicó hace poco. **Sin respuesta** quiere decir que no recibimos noticias recientes. **Sin agente** corresponde a equipos cargados manualmente. Es importante no confundir estas etiquetas con una comprobación física del apagado.
