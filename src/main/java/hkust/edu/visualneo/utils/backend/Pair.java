package hkust.edu.visualneo.utils.backend;

public class Pair<E extends Comparable<? super E>> {

    final E head;
    final E tail;

    Pair(E first, E second) throws IllegalArgumentException {
        if (first == null || second == null)
            throw new IllegalArgumentException("Null element(s)!");
        if (first.equals(second))
            throw new IllegalArgumentException("Elements in a pair should be distinct!");

        if (first.compareTo(second) < 0) {
            head = first;
            tail = second;
        }
        else {
            head = second;
            tail = first;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof Pair<?> other))
            return false;
        return (head.equals(other.head) && tail.equals(other.tail));
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash = 37 * hash + head.hashCode();
        hash = 37 * hash + tail.hashCode();
        return hash;
    }
}
