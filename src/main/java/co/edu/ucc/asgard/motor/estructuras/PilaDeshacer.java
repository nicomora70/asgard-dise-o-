package co.edu.ucc.asgard.motor.estructuras;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Pila LIFO con capacidad maxima para deshacer las ultimas series (RF07). Apilar y desapilar cuestan O(1). Si se
 * llena, se descarta el elemento mas antiguo (el del fondo).
 */
public class PilaDeshacer {

    private final List<String> elementos;
    private final int capacidad;

    public PilaDeshacer(int capacidad) {
        if (capacidad < 1) {
            throw new IllegalArgumentException("La capacidad debe ser al menos 1");
        }
        this.capacidad = capacidad;
        this.elementos = new ArrayList<>();
    }

    /** Reconstruye la pila desde la lista guardada en MongoDB (el ultimo elemento es la cima). */
    public static PilaDeshacer desdeLista(List<String> lista, int capacidad) {
        PilaDeshacer pila = new PilaDeshacer(capacidad);
        if (lista != null) {
            lista.forEach(pila::apilar);
        }
        return pila;
    }

    public void apilar(String serieId) {
        if (elementos.size() == capacidad) {
            elementos.remove(0);
        }
        elementos.add(serieId);
    }

    public Optional<String> desapilar() {
        return elementos.isEmpty() ? Optional.empty() : Optional.of(elementos.remove(elementos.size() - 1));
    }

    public Optional<String> cima() {
        return elementos.isEmpty() ? Optional.empty() : Optional.of(elementos.get(elementos.size() - 1));
    }

    public int tamano() {
        return elementos.size();
    }

    public boolean estaVacia() {
        return elementos.isEmpty();
    }

    /** Lista para guardar en MongoDB, del fondo a la cima. */
    public List<String> aLista() {
        return new ArrayList<>(elementos);
    }
}
