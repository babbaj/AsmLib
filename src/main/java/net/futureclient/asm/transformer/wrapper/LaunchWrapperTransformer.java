package net.futureclient.asm.transformer.wrapper;

import net.minecraft.launchwrapper.IClassTransformer;

public final class LaunchWrapperTransformer implements IClassTransformer {

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        return basicClass;
    }
}
