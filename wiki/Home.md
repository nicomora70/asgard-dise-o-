# Asgard - Progresión de cargas en el gimnasio

Backend de **Asgard**, un sistema para socios de gimnasio que reemplaza la libreta y la memoria por un motor de
progresión asistido. Registra cada serie (peso, repeticiones y esfuerzo), estima la fuerza máxima de cada
ejercicio, sugiere la carga de la siguiente sesión, detecta estancamientos y propone una descarga o un ejercicio
sustituto.

Proyecto de la materia **Diseño de Software**. Se pide una API REST en Java con Spring Boot, persistencia en MongoDB
y diagramas UML en Visual Paradigm, con énfasis en programación orientada a objetos, arquitectura por capas y las
estructuras de datos del caso.

## Contenido

| Página | Qué contiene |
|---|---|
| [00. Introducción](00.-Introducción) | Propósito, problema, solución, actores, convenciones y alcance |
| [01. Descripción General](01.-Descripción-General) | Las cinco vistas de la arquitectura con sus diagramas |
| [02. Características del Sistema](02.-Características-del-Sistema) | Módulos, requisitos funcionales, historias de usuario y trazabilidad |
| [03. Requisitos no Funcionales](03.-Requisitos-no-Funcionales) | Objetivos, restricciones, RNF, escenarios de calidad, motor y datos |

## Módulos

- **Socios y entrenadores:** registro de socios, datos físicos, lesiones y entrenador asignado.
- **Catálogo de ejercicios:** ejercicios con grupo muscular, tipo, equipamiento y sustitutos.
- **Rutinas:** rutina semanal que el entrenador asigna al socio.
- **Entrenamiento:** sesión del día, registro de series con advertencia no bloqueante y deshacer.
- **Progresión (motor):** carga sugerida, semáforo de progreso, estancamientos y prioridades.
- **Reportes:** historial y reporte PDF de progreso para el entrenador.

## Stack

- Java 21 (LTS)
- Spring Boot 4.2 (Spring Web MVC, Spring Data MongoDB, Validation, Actuator)
- MongoDB en local y MongoDB Atlas en producción
- Swagger UI (springdoc) para documentar y probar la API
- JUnit 5, Mockito, AssertJ, MockMvc y Postman para las pruebas
- Docker en Render para el despliegue
- Visual Paradigm para los diagramas UML
