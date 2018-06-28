package net.futureclient.asm.transformer.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Babbaj on 5/17/2018.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Transformer {

    String[] targets() default {};

    /**
     * The contents of this will be moved to {@code targets} by the PreProcessor to prevent classes from being loaded.
     * This is expected to always be empty.
     **/
    Class<?>[] value() default {};

    // Should obfuscation mappings be applied to this transformer.
    boolean remap() default true;

    // Shut everything down if this transformer fails
    // TODO: implement
    boolean required() default false;
}
