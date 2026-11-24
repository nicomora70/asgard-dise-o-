# Asgard - Sistema Inteligente de Progresión de Cargas para el Entrenamiento en Gimnasio

![Java](https://img.shields.io/badge/Java%2021-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![MongoDB](https://img.shields.io/badge/MongoDB-4EA94B?style=for-the-badge&logo=mongodb&logoColor=white)
![Postman](https://img.shields.io/badge/Postman-FF6C37?style=for-the-badge&logo=postman&logoColor=white)

El presente repositorio contiene el backend de **Asgard**, una aplicación del sector deportivo (*fitness*) para los
socios de un gimnasio y sus entrenadores. El sistema reemplaza la libreta y la memoria por un **motor de progresión de
cargas**: guarda cada serie con su peso, repeticiones y esfuerzo, estima la fuerza máxima de cada ejercicio, **sugiere
la carga de la siguiente sesión**, detecta los estancamientos y propone una descarga o un ejercicio sustituto.

Es un proyecto académico de la materia **Diseño de Software**. Los requisitos del curso son una API REST hecha con
Java y Spring Boot, persistencia en MongoDB y diagramas UML en Visual Paradigm. Se prioriza la programación orientada
a objetos, la arquitectura por capas y las estructuras de datos que pide el caso de estudio.

| | |
|---|---|
| **Código fuente** | [nicomora70/asgard-dise-o-](https://github.com/nicomora70/asgard-dise-o-) |
| **Historias de usuario** | [Issues del repositorio](https://github.com/nicomora70/asgard-dise-o-/issues) |

## Índice de la wiki

| Página | Contenido |
|---|---|
| [00. Introducción](00.-Introducción) | Propósito, contexto del problema, convenciones, alcance y referencias |
| [01. Descripción general](01.-Descripción-General) | Vistas arquitectónicas (contexto, funcional, lógica, desarrollo y despliegue) |
| [02. Características del sistema](02.-Características-del-Sistema) | Módulos, requisitos funcionales, historias de usuario y matriz de trazabilidad |
| [03. Requisitos no funcionales](03.-Requisitos-no-Funcionales) | Objetivos, restricciones, RNF, motor de progresión, datos en MongoDB y pruebas |

## Módulos

- **Socios y entrenadores:** registro del socio, sus datos físicos, sus lesiones y su entrenador asignado.
- **Catálogo de ejercicios:** ejercicios con grupo muscular, tipo, equipamiento y sustitutos.
- **Rutinas:** rutina semanal que el entrenador le asigna al socio.
- **Entrenamiento:** sesión del día, registro de series con advertencia no bloqueante y deshacer.
- **Progresión:** carga sugerida, semáforo de progreso, estancamientos y ejercicios prioritarios.
- **Reportes:** historial y reporte PDF de progreso para el entrenador.

## Stack tecnológico

- **Lenguaje:** Java 21 (LTS)
- **Framework:** Spring Boot 4.2 (Spring Web MVC, Spring Data MongoDB, Validation, Actuator)
- **Base de datos:** MongoDB (local) y MongoDB Atlas (producción)
- **Documentación de la API:** Swagger UI (springdoc)
- **Pruebas:** JUnit 5, Mockito, AssertJ, MockMvc y Postman
- **Despliegue:** Docker en Render
- **Diagramas:** UML 2.5.1

## Problema que resuelve

Un socio que entrena tres o cuatro veces por semana registra cientos de series al mes y debe decidir, en cada una,
cuánto peso levantar. De memoria no es posible recordar qué hizo la semana pasada en cada ejercicio, y por inseguridad
termina repitiendo siempre la misma carga o subiéndola sin criterio. Subir demasiado rápido causa **lesiones**; no subir
causa **estancamiento**, desmotivación y abandono del gimnasio. Asgard guarda cada serie y calcula, con un motor de
progresión, cuánto conviene levantar en la siguiente sesión.
