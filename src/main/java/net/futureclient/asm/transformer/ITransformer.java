package net.futureclient.asm.transformer;

public interface ITransformer {

    byte[] transformClass(String name, String transformedName, byte[] basicClass);
}
