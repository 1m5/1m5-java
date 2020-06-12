package io.onemfive.data;

import java.util.Objects;

public class Tuple5<A, B, C, D, E> {
    final public A first;
    final public B second;
    final public C third;
    final public D fourth;
    final public E fifth;

    public Tuple5(A first, B second, C third, D fourth, E fifth) {
        this.first = first;
        this.second = second;
        this.third = third;
        this.fourth = fourth;
        this.fifth = fifth;
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tuple5)) return false;

        Tuple5<?, ?, ?, ?, ?> tuple5 = (Tuple5<?, ?, ?, ?, ?>) o;

        if (!Objects.equals(first, tuple5.first)) return false;
        if (!Objects.equals(second, tuple5.second)) return false;
        if (!Objects.equals(third, tuple5.third)) return false;
        if (!Objects.equals(fourth, tuple5.fourth)) return false;
        return Objects.equals(fifth, tuple5.fifth);
    }

    @Override
    public int hashCode() {
        int result = first != null ? first.hashCode() : 0;
        result = 31 * result + (second != null ? second.hashCode() : 0);
        result = 31 * result + (third != null ? third.hashCode() : 0);
        result = 31 * result + (fourth != null ? fourth.hashCode() : 0);
        result = 31 * result + (fifth != null ? fifth.hashCode() : 0);
        return result;
    }
}
