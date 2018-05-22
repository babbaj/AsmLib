package net.futureclient.asm.internal.transformer;

import me.hugenerd.load.config.MemeConfig;
import net.futureclient.asm.AsmLib;
import net.futureclient.asm.config.Config;
import net.futureclient.asm.transformer.annotation.Transformer;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Created by Babbaj on 5/20/2018.
 *
 * This class is responsible for processing the bytecode of transformers
 *
 */
public class TransformerPreProcessor implements IClassTransformer {


    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        System.out.println("TransformerPreProcessor: " + transformedName);

        if (configContainsClass(transformedName)) {
            ClassNode cn = new ClassNode();
            ClassReader cr = new ClassReader(basicClass);
            cr.accept(cn, 0);

            // TODO: read annotation
            if (cn.visibleAnnotations.stream()
                    .noneMatch(node -> node.desc.equals(Transformer.class.getSimpleName()))) {
                AsmLib.LOGGER.error("Transformer Class {} is missing @{} annotation", transformedName, Transformer.class.getSimpleName());
                return basicClass;
            }

            processClass(cn);

            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
            cn.accept(cw);
            return cw.toByteArray();
        }

        return basicClass;
    }

    private void processClass(ClassNode clazz) {
        // TODO: process @Transformer annotation and lambdas
    }

    private boolean configContainsClass(String className) {
        return Stream.of(AsmLib.getConfigManager().getConfigs())
                .flatMap(Collection::stream)
                .map(Config::getTransformerClasses)
                .flatMap(Set::stream)
                //.peek(clazz -> System.out.println(":DDD " + clazz + " " + className))
                .anyMatch(className::equals);
    }
}
