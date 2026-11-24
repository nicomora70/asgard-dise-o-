# Datos de ejemplo

Datos para probar Asgard sin crear todo desde cero:

| Archivo | Colección | Contenido |
|---|---|---|
| `entrenadores.json` | `entrenadores` | 2 entrenadores (`ent_carlos`, `ent_paula`) |
| `socios.json` | `socios` | 2 socios: Laura (sin lesiones) y Andrés (lesión de hombros) |
| `ejercicios.json` | `ejercicios` | Catálogo de 30 ejercicios con su grafo de sustitutos (23 aristas, bidireccionales) |
| `rutinas.json` | `rutinas` | Rutina activa "Torso / Pierna" de Laura (lunes, martes, jueves y viernes) |

## Cargar con mongoimport

Desde esta carpeta, con MongoDB en `localhost:27017`:

```
mongoimport --db asgard --collection entrenadores --jsonArray --file entrenadores.json
mongoimport --db asgard --collection socios --jsonArray --file socios.json
mongoimport --db asgard --collection ejercicios --jsonArray --file ejercicios.json
mongoimport --db asgard --collection rutinas --jsonArray --file rutinas.json
```

Para MongoDB Atlas, agrega `--uri "mongodb+srv://usuario:clave@cluster.../asgard"` en lugar de `--db asgard`.

## Cargar con MongoDB Compass

Crea la base de datos `asgard`, luego cada colección, y usa **Add Data > Import JSON or CSV file** con el archivo
correspondiente.

## Probar con los datos cargados

- `GET /api/socios/soc_laura/sesiones/hoy`: sesión del día de Laura con la carga sugerida.
- `GET /api/ejercicios/ej_sentadilla/sustitutos`: vecinos de la sentadilla en el grafo.
- `GET /api/socios/soc_andres/progresion`: Andrés no tiene rutina; registra series y verás su progresión.
