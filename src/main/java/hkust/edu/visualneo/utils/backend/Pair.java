package hkust.edu.visualneo.utils.backend;

import java.util.Objects;

public record Pair<E>(E head, E tail) {

    public Pair {
        Objects.requireNonNull(head, "Null element!");
        Objects.requireNonNull(tail, "Null element!");
        if (head.equals(tail))
            throw new IllegalArgumentException("Elements in a pair should be distinct!");
    }

    public static <E extends Comparable<? super E>> Pair<E> ordered(E first, E second) {
        Objects.requireNonNull(first, "Null element!");
        Objects.requireNonNull(second, "Null element!");
        if (first.equals(second))
            throw new IllegalArgumentException("Elements in a pair should be distinct!");

        if (first.compareTo(second) < 0) {
            return new Pair<>(first, second);
        }
        else {
            return new Pair<>(second, first);
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

    @Override
    public String toString() {
        return String.format("(%1$s, %2$s)", head, tail);
    }
}
