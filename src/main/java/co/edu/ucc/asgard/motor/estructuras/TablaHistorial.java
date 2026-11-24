package co.edu.ucc.asgard.motor.estructuras;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Tabla hash con encadenamiento (RF08): guarda el historial de cada ejercicio por su id. Insercion y busqueda en
 * O(1) promedio; se redimensiona al doble cuando el factor de carga supera 0.75.
 *
 * @param <K> tipo de la clave
 * @param <V> tipo del valor
 */
public class TablaHistorial<K, V> {

    private static final int CAPACIDAD_INICIAL = 16;
    private static final double FACTOR_CARGA = 0.75;

    private Nodo<K, V>[] cubetas;
    private int tamano;

    public TablaHistorial() {
        this(CAPACIDAD_INICIAL);
    }

    @SuppressWarnings("unchecked")
    public TablaHistorial(int capacidad) {
        cubetas = (Nodo<K, V>[]) new Nodo[Math.max(1, capacidad)];
    }

    /** Inserta o reemplaza el valor de una clave. */
    public void poner(K clave, V valor) {
        Objects.requireNonNull(clave, "La clave no puede ser nula");
        int i = indice(clave, cubetas.length);
        for (Nodo<K, V> n = cubetas[i]; n != null; n = n.siguiente) {
            if (n.clave.equals(clave)) {
                n.valor = valor;
                return;
            }
        }
        cubetas[i] = new Nodo<>(clave, valor, cubetas[i]);
        tamano++;
        if (tamano > cubetas.length * FACTOR_CARGA) {
            redimensionar();
        }
    }

    public Optional<V> obtener(K clave) {
        if (clave == null) {
            return Optional.empty();
        }
        for (Nodo<K, V> n = cubetas[indice(clave, cubetas.length)]; n != null; n = n.siguiente) {
            if (n.clave.equals(clave)) {
                return Optional.ofNullable(n.valor);
            }
        }
        return Optional.empty();
    }

    public boolean contiene(K clave) {
        return obtener(clave).isPresent();
    }

    public List<K> claves() {
        List<K> claves = new ArrayList<>(tamano);
        for (Nodo<K, V> cabeza : cubetas) {
            for (Nodo<K, V> n = cabeza; n != null; n = n.siguiente) {
                claves.add(n.clave);
            }
        }
        return claves;
    }

    public List<V> valores() {
        List<V> valores = new ArrayList<>(tamano);
        for (Nodo<K, V> cabeza : cubetas) {
            for (Nodo<K, V> n = cabeza; n != null; n = n.siguiente) {
                valores.add(n.valor);
            }
        }
        return valores;
    }

    public int tamano() {
        return tamano;
    }

    public int capacidad() {
        return cubetas.length;
    }

    private static int indice(Object clave, int capacidad) {
        int h = clave.hashCode();
        h ^= (h >>> 16); // mezcla los bits altos con los bajos
        return Math.floorMod(h, capacidad);
    }

    @SuppressWarnings("unchecked")
    private void redimensionar() {
        Nodo<K, V>[] anteriores = cubetas;
        cubetas = (Nodo<K, V>[]) new Nodo[anteriores.length * 2];
        for (Nodo<K, V> cabeza : anteriores) {
            Nodo<K, V> n = cabeza;
            while (n != null) {
                Nodo<K, V> siguiente = n.siguiente;
                int i = indice(n.clave, cubetas.length);
                n.siguiente = cubetas[i];
                cubetas[i] = n;
                n = siguiente;
            }
        }
    }

    /** Nodo de la lista encadenada de cada cubeta. */
    private static final class Nodo<K, V> {
        private final K clave;
        private V valor;
        private Nodo<K, V> siguiente;

        Nodo(K clave, V valor, Nodo<K, V> siguiente) {
            this.clave = clave;
            this.valor = valor;
            this.siguiente = siguiente;
        }
    }
}
