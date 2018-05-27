package net.futureclient.asm.internal.transformer;

import jdk.internal.org.objectweb.asm.Type;
import net.futureclient.asm.AsmLib;
import net.futureclient.asm.transformer.annotation.Transformer;
import net.futureclient.asm.transformer.util.AnnotationInfo;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.lang.annotation.Annotation;
import java.util.List;

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
            if (!hasAnnotation(cn, Transformer.class)) {
                AsmLib.LOGGER.error("Transformer Class {} is missing @{} annotation", transformedName, Transformer.class.getSimpleName());
                return basicClass;
            }

            processClass(cn, transformedName);

            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
            cn.accept(cw);
            return cw.toByteArray();
        }

        return basicClass;
    }

    private boolean hasAnnotation(ClassNode cn, Class<? extends Annotation> clazz) {
        return cn.invisibleAnnotations.stream()
                .anyMatch(node -> node.desc.equals('L' + Type.getInternalName(clazz) + ';'));
    }

    private void processClass(ClassNode clazz, String name) {
        // TODO: process @Transformer annotation and lambdas
        AnnotationInfo info = AnnotationInfo.fromAsm(clazz, Transformer.class);
        AsmLib.transformerAnnotations.put(name, info);
        clazz.methods.stream()
                .filter(method -> (method.access & ACC_SYNTHETIC) != 0)
                .forEach(method -> {
                    method.access &= ~ACC_PRIVATE;
                    method.access |= ACC_PUBLIC;
                });
    }

    private boolean configContainsClass(String className) {
        return AsmLib.getConfigManager()
                .getClassTransformers().stream()
                .anyMatch(className::equals);
    }
}
