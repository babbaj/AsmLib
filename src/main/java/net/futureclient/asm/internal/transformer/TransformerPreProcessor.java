package net.futureclient.asm.internal.transformer;

import net.futureclient.asm.AsmLib;
import net.futureclient.asm.transformer.annotation.Inject;
import net.futureclient.asm.transformer.annotation.Transformer;
import net.futureclient.asm.transformer.util.AnnotationInfo;
import net.minecraft.launchwrapper.IClassTransformer;
import org.apache.logging.log4j.core.helpers.Assert;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Collectors;
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
        return cn.visibleAnnotations.stream()
                .anyMatch(node -> node.desc.equals('L' + Type.getInternalName(clazz) + ';'));
    }

    private void processClass(ClassNode clazz, String name) {
        // TODO: process @Transformer annotation and lambdas
        clazz.methods.stream()
                .filter(method -> (method.access & ACC_SYNTHETIC) != 0)
                .forEach(method -> {
                    method.access &= ~ACC_PRIVATE;
                    method.access |= ACC_PUBLIC;
                });

        clazz.methods.stream()
                .map(node -> node.visibleAnnotations)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .filter(node -> node.desc.equals('L' + Type.getInternalName(Inject.class) + ';'))
                .findFirst()
                .ifPresent(node -> processInject(node));

        clazz.visibleAnnotations.stream()
                .filter(node -> node.desc.equals('L' + Type.getInternalName(Transformer.class) + ';'))
                .findFirst()
                .ifPresent(node -> processTransformer(node));
    }

    // process @Transformer annotation
    @SuppressWarnings("unchecked")
    private static void processTransformer(AnnotationNode node) {
        AnnotationInfo info = AnnotationInfo.fromAsm(node);
        if (info.getValue("targets") == null) {
            node.values.add(0, "targets");
            node.values.add(1, new ArrayList<String>());
        }
        List<String> targets = (List<String>)node.values.get(node.values.indexOf("targets") + 1);
        if (node.values.contains("value")) {
            targets.addAll(info.<List<Type>>getValue("value").stream()
                    .map(Type::getClassName)
                    .collect(Collectors.toList()));
            int i = node.values.indexOf("value");
            node.values.remove(i + 1);
            node.values.remove(i);
        }

    }

    // process @Inject annotation
    private static void processInject(AnnotationNode node) {
        AnnotationInfo info = AnnotationInfo.fromAsm(node);
        if (info.getValue("target") == null) {
            String name = info.getValue("name");
            if (name == null) throw new IllegalArgumentException("Failed to supply method name");
            String ret = Optional.ofNullable(info.<Type>getValue("ret")).orElse(Type.VOID_TYPE).getDescriptor();
            String args = Optional.ofNullable(info.<List<Type>>getValue("args"))
                    .map(list -> list.stream().map(Type::getDescriptor).collect(Collectors.joining()))
                    .orElse("");

            final String fullDesc = String.format("%s(%s)%s", name, args, ret);
            int i = node.values.indexOf("target");
            if (i == -1) {
                node.values.add(0, "target");
                node.values.add(1, fullDesc);
            } else {
                node.values.set(i + 1, fullDesc);
            }
        }
    }

    private boolean configContainsClass(String className) {
        return AsmLib.getConfigManager()
                .getClassTransformers().stream()
                .anyMatch(className::equals);
    }
}
