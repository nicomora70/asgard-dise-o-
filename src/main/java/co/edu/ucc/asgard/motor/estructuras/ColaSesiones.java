package co.edu.ucc.asgard.motor.estructuras;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Cola FIFO enlazada de sesiones pendientes (RF04): si el socio falta el lunes, la sesion del lunes queda al frente
 * y es la primera que se le propone. Encolar y desencolar cuestan O(1).
 *
 * @param <T> tipo de los elementos
 */
public class ColaSesiones<T> {

    private Nodo<T> frente;
    private Nodo<T> fin;
    private int tamano;

    /** Reconstruye la cola desde la lista guardada en MongoDB (el primer elemento es el frente). */
    public static <T> ColaSesiones<T> desdeLista(List<T> lista) {
        ColaSesiones<T> cola = new ColaSesiones<>();
        if (lista != null) {
            lista.forEach(cola::encolar);
        }
        return cola;
    }

    public void encolar(T elemento) {
        Nodo<T> nuevo = new Nodo<>(elemento);
        if (fin == null) {
            frente = nuevo;
        } else {
            fin.siguiente = nuevo;
        }
        fin = nuevo;
        tamano++;
    }

    public Optional<T> desencolar() {
        if (frente == null) {
            return Optional.empty();
        }
        T valor = frente.valor;
        frente = frente.siguiente;
        if (frente == null) {
            fin = null;
        }
        tamano--;
        return Optional.of(valor);
    }

    public Optional<T> verFrente() {
        return frente == null ? Optional.empty() : Optional.of(frente.valor);
    }

    public boolean contiene(T elemento) {
        for (Nodo<T> n = frente; n != null; n = n.siguiente) {
            if (n.valor.equals(elemento)) {
                return true;
            }
        }
        return false;
    }

    public boolean estaVacia() {
        return tamano == 0;
    }

    public int tamano() {
        return tamano;
    }

    /** Lista para guardar en MongoDB, del frente al fin. */
    public List<T> aLista() {
        List<T> lista = new ArrayList<>(tamano);
        for (Nodo<T> n = frente; n != null; n = n.siguiente) {
            lista.add(n.valor);
        }
        return lista;
    }

    private static final class Nodo<T> {
        private final T valor;
        private Nodo<T> siguiente;

        Nodo(T valor) {
            this.valor = valor;
        }
    }
}
