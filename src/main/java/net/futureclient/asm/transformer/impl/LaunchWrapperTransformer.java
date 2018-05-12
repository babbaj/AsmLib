package net.futureclient.asm.transformer.impl;

import net.futureclient.asm.transformer.ITransformer;
import net.minecraft.launchwrapper.IClassTransformer;

public class LaunchWrapperTransformer implements IClassTransformer, ITransformer {

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        return this.transformClass(name, transformedName, basicClass);
    }

    @Override
    public byte[] transformClass(String name, String transformedName, byte[] basicClass) {
        return new byte[0];
    }
}
