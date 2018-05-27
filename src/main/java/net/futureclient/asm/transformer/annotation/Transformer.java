package net.futureclient.asm.transformer.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Babbaj on 5/17/2018.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface Transformer {

    Class<?>[] value() default {};

    String[] targets() default {};

    // Should obfuscation mappings be applied to this transformer.
    // TODO: implement
    boolean remap() default true;

    // Shut everything down if this transformer fails
    // TODO: implement
    boolean required() default false;
}
