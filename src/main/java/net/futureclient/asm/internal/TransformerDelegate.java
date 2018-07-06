package net.futureclient.asm.internal;

import net.futureclient.asm.transformer.wrapper.LaunchWrapperTransformer;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.MethodNode;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static net.futureclient.asm.internal.TransformerUtil.*;
import static org.objectweb.asm.Opcodes.*;

public abstract class TransformerDelegate {

    public abstract void setInstance(Object instance);

    // map classname to delegate
    public static Map<String, Class<? extends TransformerDelegate>> DELEGATES = new HashMap<>();

    // return the class name along with its definition as a byte[]
    public static Class<? extends TransformerDelegate> createDelegateClass(final MethodNode[] transformerMethods, final Type targetClassType) {
        if (targetClassType.getSort() != Type.OBJECT)
            throw new IllegalArgumentException("Expected an object type for sourceClass");

        final String className = String.format("%s#%s", targetClassType.getInternalName(), "delegate");
        final String fieldName = "instance";

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        cw.visit(52, ACC_PUBLIC | ACC_FINAL, className, null, Type.getInternalName(TransformerDelegate.class), null);

        {// the reference to the instance
            FieldVisitor fv = cw.visitField(ACC_PRIVATE | ACC_STATIC, fieldName, targetClassType.getDescriptor(), null, null);
            fv.visitEnd();
        }

        {// basic constructor
            addConstructor(cw, className, Type.getInternalName(TransformerDelegate.class));
        }

        {// implements setInstance(Object)
            addSetter(cw, className, targetClassType, fieldName);
        }

        {// add static methods
            addMethods(cw, className, transformerMethods, targetClassType);
        }

        cw.visitEnd();

        final byte[] bytes = cw.toByteArray();
        LaunchWrapperTransformer.log("meme.class", bytes);
        return createClass(bytes, className.replace("/", "."));
    }

    private static void addSetter(ClassWriter cw, String className, Type targetClass, String fieldName) {
        final MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "setInstance", "(Ljava/lang/Object;)V", null, null);
        mv.visitCode();
        Label l0 = new Label();
        mv.visitLabel(l0);

        mv.visitVarInsn(ALOAD, 1);
        mv.visitTypeInsn(CHECKCAST, targetClass.getInternalName());
        mv.visitFieldInsn(PUTSTATIC, className, fieldName, targetClass.getDescriptor());
        mv.visitInsn(RETURN);

        Label l1 = new Label();
        mv.visitLabel(l1);

        mv.visitLocalVariable("this", 'L' + className + ';', null, l0, l1, 0);
        mv.visitLocalVariable("arg0", Type.getDescriptor(Object.class), null, l0, l1, 1);

        mv.visitEnd();
        mv.visitMaxs(1, 2);
    }

    private static void addMethods(ClassWriter cw, String className, MethodNode[] transformerMethods, Type targetClass) {
        Stream.of(transformerMethods)
                .filter(m -> !m.name.equals("<init>"))
                .filter(m -> (m.access & ACC_SYNTHETIC) == 0)
                .forEach(method -> {
                    final Type sourceDesc = Type.getMethodType(method.desc);
                    final Type[] argTypes = sourceDesc.getArgumentTypes();
                    // set return type to void
                    final String desc = Type.getMethodType(Type.VOID_TYPE, sourceDesc.getArgumentTypes()).getDescriptor();

                    final MethodVisitor mv = cw.visitMethod(ACC_PUBLIC | ACC_STATIC, method.name, desc, null, null);
                    mv.visitCode();
                    Label l0 = new Label();
                    mv.visitLabel(l0);

                    mv.visitFieldInsn(GETSTATIC, className, "fieldName", targetClass.getDescriptor());
                    for (int i = 0; i < argTypes.length; i++) {
                        final Type t = argTypes[i];
                        mv.visitVarInsn(getVariableOpcode(t), i);
                    }
                    mv.visitMethodInsn(INVOKEVIRTUAL, targetClass.getInternalName(), method.name, method.desc);
                    if (sourceDesc.getReturnType() != Type.VOID_TYPE) {
                        mv.visitInsn(POP);
                    }
                    mv.visitInsn(RETURN);

                    Label l1 = new Label();
                    mv.visitLabel(l1);

                    for (int i = 0; i < argTypes.length; i++) {
                        final Type t = argTypes[i];
                        mv.visitLocalVariable("arg" + i, t.getDescriptor(), null, l0, l1, i);
                    }

                    annotateMethod(mv, method);

                    mv.visitEnd();
                    mv.visitMaxs(argTypes.length + 1, argTypes.length);
                });
    }

    // copy the annotations from the target method
    private static void annotateMethod(MethodVisitor method, MethodNode source) {

    }


}
