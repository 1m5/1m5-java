package io.onemfive.data;

import java.util.Objects;

public class Tuple4<A, B, C, D> {
    final public A first;
    final public B second;
    final public C third;
    final public D fourth;

    public Tuple4(A first, B second, C third, D fourth) {
        this.first = first;
        this.second = second;
        this.third = third;
        this.fourth = fourth;
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tuple4)) return false;

        Tuple4<?, ?, ?, ?> tuple4 = (Tuple4<?, ?, ?, ?>) o;

        if (!Objects.equals(first, tuple4.first)) return false;
        if (!Objects.equals(second, tuple4.second)) return false;
        if (!Objects.equals(third, tuple4.third)) return false;
        return Objects.equals(fourth, tuple4.fourth);

    }

    @Override
    public int hashCode() {
        int result = first != null ? first.hashCode() : 0;
        result = 31 * result + (second != null ? second.hashCode() : 0);
        result = 31 * result + (third != null ? third.hashCode() : 0);
        result = 31 * result + (fourth != null ? fourth.hashCode() : 0);
        return result;
    }
}
