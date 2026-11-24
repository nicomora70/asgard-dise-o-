package co.edu.ucc.asgard.motor.estructuras;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Grafo no dirigido y ponderado de ejercicios sustitutos (RF02, RF10). Cada vertice es un ejercicio y cada arista
 * une dos ejercicios intercambiables con su grado de similitud (0 a 1). Se representa con listas de adyacencia.
 */
public class GrafoEjercicios {

    private final Map<String, List<Arista>> adyacencia = new HashMap<>();

    public void agregarVertice(String id) {
        adyacencia.computeIfAbsent(id, k -> new ArrayList<>());
    }

    /** Agrega (o actualiza) la arista a-b en ambos sentidos, porque el grafo es no dirigido. */
    public void agregarArista(String a, String b, double similitud) {
        if (a.equals(b)) {
            return;
        }
        conectar(a, b, similitud);
        conectar(b, a, similitud);
    }

    public List<Arista> vecinos(String id) {
        return Collections.unmodifiableList(adyacencia.getOrDefault(id, List.of()));
    }

    public boolean contiene(String id) {
        return adyacencia.containsKey(id);
    }

    public int vertices() {
        return adyacencia.size();
    }

    /**
     * Busca un sustituto con un recorrido en anchura (BFS) desde el origen. En cada nivel elige el vecino admitido de
     * mayor similitud; si ningun vecino del nivel sirve, pasa al siguiente nivel (sustitutos de los sustitutos).
     * Complejidad O(V + E).
     *
     * @param origen   ejercicio a reemplazar
     * @param admitido condicion que debe cumplir el sustituto (por ejemplo, no cargar un grupo lesionado)
     */
    public Optional<String> buscarSustituto(String origen, Predicate<String> admitido) {
        if (!contiene(origen)) {
            return Optional.empty();
        }
        Set<String> visitados = new HashSet<>();
        visitados.add(origen);
        List<String> nivel = List.of(origen);
        while (!nivel.isEmpty()) {
            List<String> siguienteNivel = new ArrayList<>();
            String mejor = null;
            double mejorSimilitud = -1;
            for (String actual : nivel) {
                for (Arista arista : adyacencia.getOrDefault(actual, List.of())) {
                    String destino = arista.destino();
                    if (!visitados.add(destino)) {
                        continue;
                    }
                    siguienteNivel.add(destino);
                    if (admitido.test(destino) && arista.similitud() > mejorSimilitud) {
                        mejor = destino;
                        mejorSimilitud = arista.similitud();
                    }
                }
            }
            if (mejor != null) {
                return Optional.of(mejor);
            }
            nivel = siguienteNivel;
        }
        return Optional.empty();
    }

    private void conectar(String desde, String hacia, double similitud) {
        List<Arista> lista = adyacencia.computeIfAbsent(desde, k -> new ArrayList<>());
        lista.removeIf(a -> a.destino().equals(hacia));
        lista.add(new Arista(hacia, similitud));
        adyacencia.computeIfAbsent(hacia, k -> new ArrayList<>());
    }

    /** Arista hacia un ejercicio sustituto con su similitud. */
    public record Arista(String destino, double similitud) {
    }
}
