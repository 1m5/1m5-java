package io.onemfive.data;

import java.io.Serializable;
import java.util.Objects;

public class Tuple2<A, B> implements Serializable {
    private static final long serialVersionUID = 1;

    final public A first;
    final public B second;

    public Tuple2(A first, B second) {
        this.first = first;
        this.second = second;
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tuple2)) return false;

        Tuple2<?, ?> tuple2 = (Tuple2<?, ?>) o;

        if (!Objects.equals(first, tuple2.first)) return false;
        return Objects.equals(second, tuple2.second);

    }

    @Override
    public int hashCode() {
        int result = first != null ? first.hashCode() : 0;
        result = 31 * result + (second != null ? second.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Tuple2{" +
                "\n     first=" + first +
                ",\n     second=" + second +
                "\n}";
    }
}
