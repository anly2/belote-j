package me.aanchev.belotej.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.micronaut.serde.annotation.Serdeable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Serdeable
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WNES<T> {
    private T w;
    private T n;
    private T e;
    private T s;


    public final void reset() {
        w = null;
        n = null;
        e = null;
        s = null;
    }

    @JsonIgnore
    public final boolean isEmpty() {
        return w == null && n == null && e == null && s == null;
    }


    public final T get(int index) {
        return switch (index % 4) {
            case 0 -> s;
            case 1 -> w;
            case 2 -> n;
            case 3 -> e;
            default -> null; // not possible
        };
    }

    public final T get(RelPlayer player) {
        return switch (player) {
            case s -> this.s;
            case w -> this.w;
            case n -> this.n;
            case e -> this.e;
        };
    }

    public final void set(RelPlayer player, T value) {
        switch (player) {
            case s -> this.s = value;
            case w -> this.w = value;
            case n -> this.n = value;
            case e -> this.e = value;
        };
    }

    public final List<T> toList() {
        List<T> r = new ArrayList<>(s == null ? 3 : 4);
        if (w != null) r.add(w);
        if (n != null) r.add(n);
        if (e != null) r.add(e);
        if (s != null) r.add(s);
        return r;
    }

    public final <R> WNES<R> map(Function<T, R> mapper) {
        return new WNES<>(
                mapper.apply(w),
                mapper.apply(n),
                mapper.apply(e),
                mapper.apply(s)
        );
    }



    public static <E> WNES<E> wnes() {
        return new WNES<>();
    }
    public static <E> WNES<E> wnes(E w, E n, E e) {
        return wnes(w, n, e, null);
    }
    public static <E> WNES<E> wnes(E w, E n, E e, E s) {
        return new WNES<>(w, n, e, s);
    }

    public static <E> WNES<E> wnes(WNES<E> source) {
        return new WNES<>(source.w, source.n, source.e, source.s);
    }
    public static <E> WNES<E> wnes(Supplier<E> factory) {
        return new WNES<>(factory.get(), factory.get(), factory.get(), factory.get());
    }
}
