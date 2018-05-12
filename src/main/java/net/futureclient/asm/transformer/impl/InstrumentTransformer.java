package net.futureclient.asm.transformer.impl;

import net.futureclient.asm.transformer.ITransformer;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

public class InstrumentTransformer implements ClassFileTransformer, ITransformer {

    @Override
    public byte[] transform(ClassLoader loader, String name, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] basicClass) {
        return this.transformClass(name, name, basicClass);
    }

    @Override
    public byte[] transformClass(String name, String transformedName, byte[] basicClass) {
        return new byte[0];
    }
}
