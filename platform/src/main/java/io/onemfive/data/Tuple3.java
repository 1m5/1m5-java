package io.onemfive.data;

import java.util.Objects;

public class Tuple3<A, B, C> {
    final public A first;
    final public B second;
    final public C third;

    public Tuple3(A first, B second, C third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tuple3)) return false;

        Tuple3<?, ?, ?> tuple3 = (Tuple3<?, ?, ?>) o;

        if (!Objects.equals(first, tuple3.first)) return false;
        if (!Objects.equals(second, tuple3.second)) return false;
        return Objects.equals(third, tuple3.third);

    }

    @Override
    public int hashCode() {
        int result = first != null ? first.hashCode() : 0;
        result = 31 * result + (second != null ? second.hashCode() : 0);
        result = 31 * result + (third != null ? third.hashCode() : 0);
        return result;
    }
}
