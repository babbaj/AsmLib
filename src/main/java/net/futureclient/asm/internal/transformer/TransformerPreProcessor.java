package net.futureclient.asm.internal.transformer;

import jdk.internal.org.objectweb.asm.Type;
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

import static org.objectweb.asm.Opcodes.*;


/**
 * Created by Babbaj on 5/20/2018.
 * <p>
 * This class is responsible for processing the bytecode of transformers
 */
public class TransformerPreProcessor implements IClassTransformer {


    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (configContainsClass(transformedName)) {
            ClassNode cn = new ClassNode();
            ClassReader cr = new ClassReader(basicClass);
            cr.accept(cn, 0);

            // TODO: read annotation
            if (cn.visibleAnnotations.stream()
                    .noneMatch(node -> node.desc.equals('L' + Type.getInternalName(Transformer.class) + ';'))) {
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
        clazz.methods.stream()
                .filter(method -> (method.access & ACC_SYNTHETIC) != 0)
                .forEach(method -> {
                    method.access &= ~ACC_PRIVATE;
                    method.access |= ACC_PUBLIC;
                });
    }

    private boolean configContainsClass(String className) {
        return Stream.of(AsmLib.getConfigManager().getConfigs())
                .flatMap(Collection::stream)
                .map(Config::getTransformerClasses)
                .flatMap(Set::stream)
                .anyMatch(className::equals);
    }
}
