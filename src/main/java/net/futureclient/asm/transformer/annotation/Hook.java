package net.futureclient.asm.transformer.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Babbaj on 5/16/2018.
 *
 * Annotation for methods that are to be called by injected code, might not end up using this.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Hook {
    String name() default ""; // probably gay
}
