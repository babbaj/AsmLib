package net.futureclient.asm.transformer.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Babbaj on 5/17/2018.
 */
// TODO: allow target to be a class
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
//@Retention(RetentionPolicy.CLASS)
public @interface Transformer {
    String target();
}
