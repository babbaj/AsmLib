package net.futureclient.asm.transformer.wrapper;

import net.futureclient.asm.transformer.ClassTransformer;
import net.minecraft.launchwrapper.IClassTransformer;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.util.List;

public final class LaunchWrapperTransformer implements IClassTransformer {

    private final Logger LOGGER = LogManager.getLogger("asmlib");

    public LaunchWrapperTransformer() {}

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        List<ClassTransformer> classTransformers = this.getTransformers(transformedName);

        if (!classTransformers.isEmpty()) {
            ClassNode cn = new ClassNode();
            ClassReader cr = new ClassReader(basicClass);
            cr.accept(cn, 0);


            classTransformers.forEach(transformer -> {
                try {
                    transformer.transform(cn);
                } catch (Throwable throwable) {
                    LOGGER.log(Level.ERROR, "Error transforming \"{}\" with transformer \"{}\".", transformedName, transformer.getClass().getName());

                    if (transformer.isRequired())
                        throw throwable;
                    else
                        throwable.printStackTrace();
                }
            });

            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
            cn.accept(cw);
            return cw.toByteArray();
        }

        return basicClass;
    }

    private List<ClassTransformer> getTransformers(String name) {
        return null;
    }
}
