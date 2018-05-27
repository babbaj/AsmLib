package net.futureclient.asm.transformer.util;

import net.futureclient.asm.transformer.ClassTransformer;

/**
 * Created by Babbaj on 5/17/2018.
 */
public class ReflectiveClassTransformer extends ClassTransformer {

    private Object instance; // unused

    public ReflectiveClassTransformer(Object instanceIn, String name) {
        super(name);
        this.instance = instanceIn;
    }
}
