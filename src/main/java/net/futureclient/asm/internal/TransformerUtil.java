package net.futureclient.asm.internal;

import net.minecraft.launchwrapper.Launch;
import org.objectweb.asm.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static org.objectweb.asm.Opcodes.*;

/**
 * Created by Babbaj on 5/21/2018.
 */
public final class TransformerUtil {

    public static <T> Class<?> createCarrierClass(final String className, final T dataInstance, final Class<T> type, final Method abstractMethod) {
        if (!type.isInstance(dataInstance)) throw new IllegalArgumentException("Data is not an instance of the given type");

        ClassWriter cw = new ClassWriter(0);
        cw.visit(52, ACC_PUBLIC | ACC_SUPER, className, null, "java/lang/Object", null);

        {   // basic constructor, nothing special but it's required
            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
            mv.visitCode();
            Label l0 = new Label();
            mv.visitLabel(l0);
            mv.visitLineNumber(6, l0);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            mv.visitInsn(RETURN);
            Label l1 = new Label();
            mv.visitLabel(l1);
            mv.visitLocalVariable("this", 'L' + className + ';', null, l0, l1, 0);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }

        {
            FieldVisitor fv = cw.visitField(ACC_PUBLIC | ACC_STATIC | ACC_FINAL, "data", Type.getDescriptor(type), null, null);
            fv.visitEnd();
        }

        {   // the static function we will use to invoke the lambda
            final Type[] argTypes = Type.getArgumentTypes(abstractMethod);
            final Type retType = Type.getReturnType(abstractMethod);

            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, abstractMethod.getName(), Type.getMethodDescriptor(abstractMethod), null, null);
            mv.visitCode();
            Label l0 = new Label();
            mv.visitLabel(l0);
            mv.visitLineNumber(11, l0);

            mv.visitFieldInsn(GETSTATIC, className, "data", Type.getDescriptor(type));
            for (int i = 0; i < argTypes.length; i++) {
                final Type t = argTypes[i];
                mv.visitVarInsn(getVariableOpcode(t), i);
            }
            mv.visitMethodInsn(INVOKEINTERFACE, Type.getInternalName(type), abstractMethod.getName(), Type.getMethodDescriptor(abstractMethod), true);

            Label l1 = new Label();
            mv.visitLabel(l1);
            mv.visitLineNumber(12, l1);

            mv.visitInsn(getReturnOpcode(retType));

            for (int i = 0; i < argTypes.length; i++) {
                final Type t = argTypes[i];
                mv.visitLocalVariable("arg" + i, t.getDescriptor(), null, l0, l1, i);
            }

            mv.visitMaxs(1 + argTypes.length, argTypes.length);
            mv.visitEnd();
        }

        cw.visitEnd();

        byte[] bytes = cw.toByteArray();
        Class<?> clazz = createClass(bytes, className);

        try {
            Field f = clazz.getField("data");
            makePublic(f);
            f.set(null, dataInstance);
            return clazz;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static int getReturnOpcode(Type returnType) {
        switch(returnType.getSort()) {
            case Type.VOID:
                return RETURN;
            case Type.BOOLEAN:
            case Type.CHAR:
            case Type.BYTE:
            case Type.SHORT:
            case Type.INT:
                return IRETURN;
            case Type.FLOAT:
                return FRETURN;
            case Type.DOUBLE:
                return DRETURN;
            case Type.ARRAY:
            case Type.OBJECT:
                return ARETURN;
            case Type.METHOD:
                throw new IllegalArgumentException("Illegal return type METHOD");
            default:
                throw new IllegalArgumentException("Unknown Type Sort: " + returnType.getSort());
        }
    }

    private static int getVariableOpcode(Type type) {
        switch (type.getSort()) {
            case Type.BOOLEAN:
            case Type.CHAR:
            case Type.BYTE:
            case Type.SHORT:
            case Type.INT:
                return ILOAD;
            case Type.FLOAT:
                return FLOAD;
            case Type.DOUBLE:
                return DLOAD;
            case Type.ARRAY:
            case Type.OBJECT:
                return ALOAD;
            case Type.METHOD:
                throw new IllegalArgumentException("Illegal variable type METHOD");
            case Type.VOID:
                throw new IllegalArgumentException("Illegal variable type VOID");
            default:
                throw new IllegalArgumentException("Unknown Type Sort: " + type.getSort());
        }
    }

    private static void makePublic(final Field f) {
        try {
            f.setAccessible(true);
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(f, f.getModifiers() & ~Modifier.FINAL);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static Class<?> createClass(byte[] bytes, String name) {
        try {
            Method defineClass = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);
            defineClass.setAccessible(true);
            return (Class<?>)defineClass.invoke(Launch.classLoader, name, bytes, 0, bytes.length);
        } catch (Exception e) {
            throw new RuntimeException("Oy Vey!!", e);
        }
    }

}
