# guía para nuestra defensa

La idea es que podamos explicar el proyecto entre todos, sin aprendernos un texto palabra por palabra.

## en grupo

1. Contamos el problema del Área 433: equipos encendidos y tiempo que lleva recorrer las salas.
2. Mostramos la arquitectura: navegador, Caddy, Tomcat, MariaDB y agentes.
3. Explicamos cómo usamos Java, JSP, CSS, Maven y Docker y por qué elegimos polling.
4. Mostramos la base: laboratorios, computadoras, usuarios, agentes y órdenes.
5. Señalamos asociación, agregación, Singleton y colecciones en clases reales.
6. Hacemos una prueba: crear sala, vincular PC, ver estado y pedir un apagado autorizado.

## individual

Cada uno debe poder encontrar las clases, explicar qué cambia al modificar un campo, editar un formulario y ajustar una consulta JDBC. Practicamos con ejemplos: agregar una columna visible en la tabla, una validación de nombre, un campo en el modelo o una condición de búsqueda.

Si nos preguntan qué significa «Disponible», respondemos que el agente se comunicó recientemente. Si nos preguntan por qué el apagado grupal no es exactamente simultáneo, explicamos que las computadoras pueden demorar distinto en terminar y que nosotros coordinamos el **inicio** del apagado.

No afirmamos que CI demuestre un apagado físico: esa parte se verifica con las computadoras del instituto.
