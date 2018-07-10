package net.futureclient.asm.internal;

import com.google.common.collect.Streams;
import javafx.util.Pair;
import net.minecraft.launchwrapper.Launch;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.AnnotationNode;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Iterator;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import static org.objectweb.asm.Opcodes.*;

/**
 * Created by Babbaj on 5/21/2018.
 */
public final class TransformerUtil {


    public static int getReturnOpcode(Type returnType) {
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

    public static int getVariableOpcode(Type type) {
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

    public static void makePublic(final Field f) {
        try {
            f.setAccessible(true);
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(f, f.getModifiers() & ~Modifier.FINAL);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public static <T> Class<? extends T> createClass(byte[] bytes, String name) {
        try {
            Method defineClass = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);
            defineClass.setAccessible(true);
            return (Class<? extends T>)defineClass.invoke(Launch.classLoader, name, bytes, 0, bytes.length);
        } catch (Exception e) {
            throw new RuntimeException("Oy Vey!!", e);
        }
    }

    public static void addBasicConstructor(ClassWriter cw, String className, String ownerClass) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        Label l0 = new Label();
        mv.visitLabel(l0);

        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, ownerClass, "<init>", "()V", false);
        mv.visitInsn(RETURN);
        Label l1 = new Label();
        mv.visitLabel(l1);

        mv.visitLocalVariable("this", 'L' + className + ';', null, l0, l1, 0);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }

    public static Stream<Pair<String, Object>> streamAnnotation(AnnotationNode annotation) {
        return Streams.stream(new AnnotationIterator(annotation));
    }

    public static class AnnotationIterator implements Iterator<Pair<String, Object>> {
        private final Iterator<Object> listIterator;

        public AnnotationIterator(AnnotationNode source) {
            if (source.values != null)
                this.listIterator = source.values.iterator();
            else
                this.listIterator = Collections.emptyIterator();
        }

        @Override
        public boolean hasNext() {
            return listIterator.hasNext();
        }

        @Override
        public Pair<String, Object> next() {
            return new Pair<>((String)listIterator.next(), listIterator.next());
        }

        // TODO: verify this
        @Override
        public void remove() {
            listIterator.remove();
            listIterator.next();
            listIterator.remove();
        }

        public void forEachRemaining(BiConsumer<String, Object> consumer) {
            while (listIterator.hasNext()) {
                consumer.accept((String)listIterator.next(), listIterator.next());
            }
        }
    }

}
