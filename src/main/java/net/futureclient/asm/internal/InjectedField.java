package net.futureclient.asm.internal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Babbaj on 6/7/2018.
 *
 * Annotation added to all fields added by a transformer
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface InjectedField {

    // Class that the field was added by
    String className();
}
