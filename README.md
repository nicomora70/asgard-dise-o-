# Asgard - Progresión de Cargas en el Gimnasio (Backend)

![Java](https://img.shields.io/badge/Java%2021-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![MongoDB](https://img.shields.io/badge/MongoDB-4EA94B?style=for-the-badge&logo=mongodb&logoColor=white)
![Postman](https://img.shields.io/badge/Postman-FF6C37?style=for-the-badge&logo=postman&logoColor=white)

Backend de **Asgard**, un sistema para socios de gimnasio y sus entrenadores que reemplaza la libreta por un motor de
progresión de cargas. Guarda cada serie (peso, repeticiones y esfuerzo), estima la fuerza máxima de cada ejercicio,
sugiere la carga de la siguiente sesión, detecta estancamientos y propone una descarga o un ejercicio sustituto que no
cargue los grupos musculares lesionados.
El caso de estudio, los requisitos y los diagramas están en la
[wiki del repositorio](https://github.com/nicomora70/asgard-dise-o-/wiki), y las historias de usuario en los
[issues](https://github.com/nicomora70/asgard-dise-o-/issues).

| | |
|---|---|
| **API desplegada** | https://asgard-diseno.onrender.com |
| **Swagger UI** | https://asgard-diseno.onrender.com/swagger-ui.html |
| **Estado del servicio** | https://asgard-diseno.onrender.com/actuator/health |

> El servicio usa el plan gratuito de Render: tras 15 minutos sin uso se suspende y la primera peticion tarda unos
> 50 segundos en responder.

## Alcance

Este entregable corresponde **únicamente al backend** (API REST). No incluye interfaz gráfica: toda la interacción
se hace y se verifica a través de los endpoints REST, documentados con Swagger UI y probados con la colección de
Postman incluida en el repositorio.

## Stack

- Java 21 (LTS)
- Spring Boot 4.2 (Spring Web MVC, Spring Data MongoDB, Validation, Actuator)
- MongoDB en local y MongoDB Atlas en producción
- Swagger UI (springdoc) para documentar y probar la API
- Apache PDFBox para el reporte PDF de progreso
- JUnit 5, Mockito, AssertJ y MockMvc para las pruebas
- Maven (con Maven Wrapper) y Docker para el despliegue en Render

## Estructura del repositorio

```
asgard
├── src/main/java/co/edu/ucc/asgard
│   ├── controlador   -> endpoints REST (socios, entrenadores, ejercicios, rutinas, sesiones, series, progresion, reportes)
│   ├── servicio      -> logica de negocio y planificador diario de sesiones
│   ├── motor         -> motor de progresion de cargas (no depende de Spring Web ni de MongoDB)
│   │   └── estructuras -> matriz 2D, tabla hash, grafo, Max-Heap, pila LIFO y cola FIFO
│   ├── repositorio   -> acceso a datos (MongoRepository)
│   ├── dto           -> objetos de entrada y salida
│   ├── modelo        -> documentos de MongoDB + modelo.enums
│   ├── excepcion     -> excepciones de negocio + manejador global
│   └── config        -> parametros del motor y de las sesiones, Swagger y configuracion general
├── src/main/resources/application.properties
├── src/test/java     -> pruebas de las estructuras, el motor, los controladores y el flujo completo
├── postman           -> coleccion y entornos (local y produccion)
├── mongo             -> datos de ejemplo e instrucciones para cargarlos
├── wiki              -> copia de las paginas de la wiki y de los diagramas
├── Dockerfile        -> imagen de produccion
└── render.yaml       -> despliegue en Render
```

## Módulos

- **Socios y entrenadores:** registro del socio con datos físicos, lesiones y entrenador (que tiene cupo máximo).
- **Catálogo de ejercicios:** ejercicios con grupo muscular, tipo, equipamiento y grafo de sustitutos.
- **Rutinas:** rutina semanal asignada por el entrenador, con historial y una rutina activa.
- **Entrenamiento:** sesión del día (con cola de sesiones pendientes), registro de series con advertencia no
  bloqueante y deshacer.
- **Progresión:** 1RM estimado, carga sugerida, semáforo de progreso, estancamientos y prioridades.
- **Reportes:** historial de series y reporte PDF de progreso para el entrenador.

## Motor de progresión

| Regla | Detalle |
|---|---|
| 1RM estimado | Fórmula de Epley: `peso x (1 + repeticiones / 30)` |
| Doble progresión | Todas las series en `repsMax` con RPE dentro del objetivo: **SUBIR** (+2.5 kg tren superior, +5 kg tren inferior, +1 kg aislamiento) |
| Bajar | Series por debajo de `repsMin` en 2 sesiones seguidas: **BAJAR** 5 % |
| Semáforo | VERDE progresa; AMARILLO sin mejora en 3 sesiones (**DELOAD** −10 %); ROJO cae más de 5 % (**SUSTITUIR**) |
| Sustituto | Recorrido en anchura por el grafo: el más parecido que no esté estancado ni cargue un grupo lesionado |
| Prioridad | `diasSinEntrenar x factorSemaforo x factorTipo`, ordenado con un Max-Heap |

Todos los valores se configuran en `application.properties` (prefijo `asgard.motor`).

## Cómo ejecutar

**Opción A: sin instalar MongoDB.** Arranca la API con un MongoDB en memoria (los datos se pierden al detenerla):

```
./mvnw spring-boot:test-run          (Windows: mvnw.cmd spring-boot:test-run)
```

**Opción B: con MongoDB.**

1. Tener **Java 21** y **MongoDB** en `localhost:27017`. Para usar MongoDB Atlas, define la variable de entorno
   `MONGODB_URI` con la cadena `mongodb+srv://...`.
2. (Opcional) Cargar los datos de ejemplo de la carpeta [`mongo/`](mongo).
3. Ejecutar:

   ```
   ./mvnw spring-boot:run          (Windows: mvnw.cmd spring-boot:run)
   ```

En los dos casos, la documentación interactiva queda en `http://localhost:8080/swagger-ui.html`.

Pruebas automáticas: `./mvnw test` (33 pruebas: estructuras de datos, motor con prueba de rendimiento,
controladores y el flujo completo de la API contra un MongoDB en memoria).

## Pruebas con Postman

1. Importa los tres archivos de la carpeta [`postman/`](postman).
2. Elige el entorno **Asgard - Local** o **Asgard - Produccion**.
3. Ejecuta las carpetas en orden o toda la colección con **Run collection** (42 peticiones con aserciones). Los
   identificadores (`socioId`, `sentadillaId`, `sesionId`, ...) se guardan solos entre peticiones, y cada ejecución
   usa documentos y nombres nuevos, así que se puede repetir sin limpiar la base de datos.

## Endpoints principales

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/entrenadores` | Registrar entrenador (US002) |
| POST | `/api/socios` | Registrar socio con datos físicos, lesiones y entrenador (US001) |
| PUT | `/api/socios/{id}/lesiones` | Actualizar lesiones (RF10) |
| DELETE | `/api/socios/{id}` | Eliminar al socio y sus datos en cascada (RNF-006) |
| POST | `/api/ejercicios` | Crear ejercicio con sustitutos (US003) |
| GET | `/api/ejercicios/{id}/sustitutos` | Sustitutos en el grafo, del más parecido al menos parecido |
| POST | `/api/socios/{id}/rutinas` | Asignar rutina semanal (US004) |
| GET | `/api/socios/{id}/sesiones/hoy` | Sesión del día con carga sugerida (US005) |
| POST | `/api/socios/{id}/sesiones/{sesionId}/completar` | Completar la sesión (también `/omitir`) |
| GET | `/api/socios/{id}/series/validar` | Advertencia previa sin bloquear (US007) |
| POST | `/api/socios/{id}/series` | Registrar una serie en un solo paso (US006, US013) |
| POST | `/api/socios/{id}/series/deshacer` | Deshacer la última serie (US008) |
| GET | `/api/socios/{id}/progresion` | Carga sugerida, semáforo y decisión por ejercicio (US009 - US011) |
| GET | `/api/socios/{id}/prioridades?k=5` | Ejercicios que más atención necesitan (US012) |
| GET | `/api/socios/{id}/historial` | Historial de series (US014) |
| GET | `/api/socios/{id}/reporte/pdf` | Reporte PDF de progreso (US014) |

Los errores responden con el código HTTP correcto (400, 404, 409) y un mensaje en español.

## Despliegue en Render

1. En [MongoDB Atlas](https://www.mongodb.com/atlas), crea un clúster gratuito, un usuario de base de datos y
   permite el acceso desde `0.0.0.0/0`. Copia la cadena de conexión `mongodb+srv://...`.
2. En [Render](https://render.com), crea un **Blueprint** desde este repositorio: usa el archivo `render.yaml`.
3. En la variable `MONGODB_URI` del servicio, pega la cadena de Atlas. La base de datos `asgard` se usa aunque la
   cadena no la incluya.
4. Cuando el despliegue termine, revisa `https://<servicio>.onrender.com/actuator/health` y la documentación en
   `/swagger-ui.html`. Si el servicio tiene otro nombre, actualiza `baseUrl` en el entorno de producción de Postman.

> El plan gratuito de Render suspende el servicio tras 15 minutos sin uso; la primera petición tarda unos 50 segundos.
