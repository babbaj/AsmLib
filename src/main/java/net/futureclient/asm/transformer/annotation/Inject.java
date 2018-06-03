package net.futureclient.asm.transformer.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Babbaj on 5/16/2018.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Inject {
    /**
     * name/descriptor of target method
     *
     * ex: "main([Ljava/lang/String;)V"
     */
    String target() default "";


    /**
     * These parameters will be converted to a String representation and will be returned by {@code target}.
     * If {@code target} has already been defined then these will be ignored
     */

    String name() default "";

    Class<?>[] args() default {};

    Class<?> ret() default void.class;
}
