package net.futureclient.asm.internal;

import com.google.common.collect.Streams;
import net.futureclient.asm.config.ConfigManager;
import net.futureclient.asm.config.Config;
import net.futureclient.asm.obfuscation.MappingType;
import net.futureclient.asm.obfuscation.RuntimeState;
import net.futureclient.asm.transformer.annotation.Inject;
import net.futureclient.asm.transformer.annotation.Transformer;
import net.futureclient.asm.transformer.util.AnnotationInfo;
import net.futureclient.asm.transformer.util.ObfUtils;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Collectors;

import static org.objectweb.asm.Opcodes.*;


/**
 * Created by Babbaj on 5/20/2018.
 * <p>
 * This class is responsible for processing the bytecode of transformers
 */
public final class TransformerPreProcessor implements IClassTransformer {

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (configContainsClass(transformedName)) {
            ClassNode cn = new ClassNode();
            ClassReader cr = new ClassReader(basicClass);
            cr.accept(cn, 0);

            if (!hasAnnotation(cn, Transformer.class)) {
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

    private boolean configContainsClass(String className) {
        return ConfigManager.INSTANCE
                .getConfigs().stream()
                .map(Config::getTransformerClassNames)
                .flatMap(Collection::stream)
                .anyMatch(className::equals);
    }


    private boolean hasAnnotation(ClassNode cn, Class<? extends Annotation> clazz) {
        return cn.visibleAnnotations.stream()
                .anyMatch(node -> node.desc.equals(Type.getDescriptor(clazz)));
    }


    private void processClass(ClassNode clazz) {
        clazz.methods.forEach(node -> injectAtLambdas(clazz, node));
        modifyAllLambdas(clazz);

        // TODO: only apply to lambda methods
        clazz.methods.stream()
                .filter(method -> (method.access & ACC_SYNTHETIC) != 0)
                .forEach(method -> {
                    method.access &= ~ACC_PRIVATE;
                    method.access |= ACC_PUBLIC;
                });

        clazz.visibleAnnotations.stream()
                .filter(node -> node.desc.equals(Type.getDescriptor(Transformer.class)))
                .findFirst()
                .ifPresent(this::processTransformer);

        clazz.methods.stream()
                .map(node -> node.visibleAnnotations)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .filter(node -> node.desc.equals(Type.getDescriptor(Inject.class)))
                .forEach(this::processInject);

        final Class<? extends TransformerDelegate> wrapperClass =
                TransformerDelegate.createDelegateClass(clazz, Type.getObjectType(clazz.name));
        TransformerDelegate.DELEGATES.put(clazz.name, wrapperClass);
    }

    // process @Transformer annotation
    @SuppressWarnings("unchecked")
    private void processTransformer(AnnotationNode node) {
        AnnotationInfo info = AnnotationInfo.fromAsm(node);
        if (info.getValue("target") == null) {
            node.values.add(0, "target");
            String newTarget = Optional.of(info.<Type>getValue("value"))
                    .map(Type::getInternalName)
                    .map(this::mcpClassName)
                    .get();
            node.values.add(1, newTarget);
        }
        final int i = node.values.indexOf("value");
        if (i != -1) {
            node.values.remove(i + 1);
            node.values.remove(i);
        }
    }

    // process @Inject annotation
    private void processInject(final AnnotationNode node) {
        AnnotationInfo info = AnnotationInfo.fromAsm(node);
        if (info.getValue("target") == null) {
            final String name = info.getValue("name");
            if (name == null)
                throw new IllegalArgumentException("Failed to supply method name");
            final String ret = Optional.ofNullable(info.<Type>getValue("ret"))
                .map(this::mcpClassType)
                .orElse(Type.VOID_TYPE)
                .getDescriptor();
            final String args = Optional.ofNullable(info.<List<Type>>getValue("args"))
                    .map(list -> list.stream()
                            .map(this::mcpClassType)
                            .map(Type::getDescriptor)
                            .collect(Collectors.joining()))
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
        // target value is set, remove the old annotation values because theyre unnecessary and cause problems
        String[] oldValueNames = {"name", "args", "ret"};
        for (String valueName : oldValueNames) {
            final int index = node.values.indexOf(valueName);
            if (index != -1) {
                node.values.remove(index + 1);
                node.values.remove(index);
            }
        }
    }

    // replace all lambbda method references with untyped wrapper methods to avoid premature class loading
    private void modifyAllLambdas(ClassNode clazz) {
        List<MethodNode> wrapperMethods = clazz.methods.stream()
                .map(method -> method.instructions)
                .flatMap(insnList -> Streams.stream(insnList.iterator()))
                .filter(InvokeDynamicInsnNode.class::isInstance).map(InvokeDynamicInsnNode.class::cast)
                .map(lambdaNode -> replaceLambdaMethodReference(clazz, lambdaNode))
                .collect(Collectors.toList());

        clazz.methods.addAll(wrapperMethods);
    }
    private MethodNode replaceLambdaMethodReference(ClassNode thisClass, InvokeDynamicInsnNode node) {
        final Handle methodRef = (Handle)node.bsmArgs[1];
        final Type refDesc = (Type)node.bsmArgs[2];

        final MethodNode wrapperMethod = AsmUtil.createWrapperFunction(methodRef.getName() + "#wrapper", methodRef.getOwner(), methodRef.getName(), methodRef.getDesc(), methodRef.getTag());

        node.bsmArgs[1] = new Handle(H_INVOKESTATIC, thisClass.name, wrapperMethod.name, wrapperMethod.desc);

        node.bsmArgs[0] = AsmUtil.toRawTypeDescriptor((Type)node.bsmArgs[0]);
        node.bsmArgs[2] = AsmUtil.toRawTypeDescriptor((Type)node.bsmArgs[2]);
        node.desc = AsmUtil.descriptorWithRawTypeArgs(Type.getMethodType(node.desc)).getDescriptor();

        return wrapperMethod;
    }

    // TODO: make sure we have java lambdas
    private void injectAtLambdas(ClassNode clazz, MethodNode method) {
        Streams.stream(method.instructions.iterator())
                .filter(InvokeDynamicInsnNode.class::isInstance)
                .map(InvokeDynamicInsnNode.class::cast)
                .forEach(node -> {
                    lambdaHook(clazz, method, node);
                });
    }
    private void lambdaHook(ClassNode clazz, MethodNode method, InvokeDynamicInsnNode node) {
        Handle methodRef = (Handle)node.bsmArgs[1];
        Type realType = (Type)node.bsmArgs[2]; // MethodType

        InsnList list = new InsnList();
        list.add(new InsnNode(DUP));
        // method reference
        list.add(new LdcInsnNode(methodRef.getOwner()));
        list.add(new LdcInsnNode(methodRef.getName()));
        list.add(new LdcInsnNode(methodRef.getDesc()));
        list.add(new LdcInsnNode(methodRef.getTag()));
        // instantiatedMethodType
        list.add(new LdcInsnNode(realType.getInternalName()));
        // InvokeDynamicInsnNode desc
        list.add(new LdcInsnNode(node.desc));
        list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(LambdaInfo.class), "addLambda",
                "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ILjava/lang/String;Ljava/lang/String;)V"));

        method.instructions.insert(node, list);
    }

    // if we we are compiled to notch we need to remap class literals back to mcp
    private String mcpClassName(String clazz) {
        if (RuntimeState.getRuntimeMappingType() == MappingType.NOTCH) {
            return ObfUtils.remapClassToMcp(clazz); // notch -> mcp
        }
        // already mcp
        return clazz;
    }

    private Type mcpClassType(Type t) { // TODO: fix
        if (RuntimeState.getRuntimeMappingType() == MappingType.NOTCH) {
            if (t.getSort() != Type.OBJECT && t.getSort() != Type.ARRAY) return t; // primitive
            final String clazz = t.getInternalName();
            return Type.getObjectType(ObfUtils.remapClassToMcp(clazz)); // notch -> mcp
        }
        return t;
    }
}
