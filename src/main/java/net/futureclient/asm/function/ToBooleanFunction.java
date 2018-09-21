package net.futureclient.asm.function;

@FunctionalInterface
public interface ToBooleanFunction<T> {
    boolean apply(T obj);
}
