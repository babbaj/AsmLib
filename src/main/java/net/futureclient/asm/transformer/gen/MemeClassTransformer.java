package net.futureclient.asm.transformer.gen;

import net.futureclient.asm.transformer.ClassTransformer;

/**
 * Created by Babbaj on 5/17/2018.
 */
public class MemeClassTransformer extends ClassTransformer {

    private Object instance;

    public MemeClassTransformer(Object instanceIn, String name) {
        super(name);
        this.instance = instanceIn;
    }
}
