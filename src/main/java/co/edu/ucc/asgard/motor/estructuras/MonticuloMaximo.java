package co.edu.ucc.asgard.motor.estructuras;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Cola de prioridad implementada como Max-Heap binario sobre un arreglo (RF11). La raiz es siempre el elemento
 * mayor segun el comparador. Insertar y extraer el maximo cuestan O(log n).
 *
 * @param <T> tipo de los elementos
 */
public class MonticuloMaximo<T> {

    private final List<T> elementos = new ArrayList<>();
    private final Comparator<? super T> comparador;

    public MonticuloMaximo(Comparator<? super T> comparador) {
        this.comparador = comparador;
    }

    public void insertar(T elemento) {
        elementos.add(elemento);
        subir(elementos.size() - 1);
    }

    public Optional<T> verMaximo() {
        return elementos.isEmpty() ? Optional.empty() : Optional.of(elementos.get(0));
    }

    public Optional<T> extraerMaximo() {
        if (elementos.isEmpty()) {
            return Optional.empty();
        }
        T maximo = elementos.get(0);
        T ultimo = elementos.remove(elementos.size() - 1);
        if (!elementos.isEmpty()) {
            elementos.set(0, ultimo);
            bajar(0);
        }
        return Optional.of(maximo);
    }

    public int tamano() {
        return elementos.size();
    }

    public boolean estaVacio() {
        return elementos.isEmpty();
    }

    private void subir(int i) {
        while (i > 0) {
            int padre = (i - 1) / 2;
            if (comparador.compare(elementos.get(i), elementos.get(padre)) <= 0) {
                return;
            }
            intercambiar(i, padre);
            i = padre;
        }
    }

    private void bajar(int i) {
        int n = elementos.size();
        while (true) {
            int izquierdo = 2 * i + 1;
            int derecho = izquierdo + 1;
            int mayor = i;
            if (izquierdo < n && comparador.compare(elementos.get(izquierdo), elementos.get(mayor)) > 0) {
                mayor = izquierdo;
            }
            if (derecho < n && comparador.compare(elementos.get(derecho), elementos.get(mayor)) > 0) {
                mayor = derecho;
            }
            if (mayor == i) {
                return;
            }
            intercambiar(i, mayor);
            i = mayor;
        }
    }

    private void intercambiar(int a, int b) {
        T temporal = elementos.get(a);
        elementos.set(a, elementos.get(b));
        elementos.set(b, temporal);
    }
}
