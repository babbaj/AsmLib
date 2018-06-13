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

    public static <T> Class<?> createCarrierClass(String className, T dataInstance, Class<T> type) {
        if (!type.isInstance(dataInstance)) throw new IllegalArgumentException("Bad type");

        ClassWriter cw = new ClassWriter(0);
        cw.visit(52, ACC_PUBLIC | ACC_SUPER, className, null, "java/lang/Object", null);

        {
            FieldVisitor fv = cw.visitField(ACC_PUBLIC | ACC_STATIC | ACC_FINAL, "data", Type.getDescriptor(type), null, null);
            fv.visitEnd();
        }

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
