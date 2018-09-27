package net.futureclient.asm.internal;

import com.google.common.collect.Streams;
import javafx.util.Pair;
import net.minecraft.launchwrapper.Launch;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import static org.objectweb.asm.Opcodes.*;

/**
 * Created by Babbaj on 5/21/2018.
 */
public final class AsmUtil {

    public static final Type OBJECT_TYPE = Type.getType("Ljava/lang/Object;");

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

    public static int opcodeFromTag(int tag) {
        switch (tag) {
            case H_INVOKESTATIC:
                return INVOKESTATIC;
            case H_INVOKEVIRTUAL:
                return INVOKEVIRTUAL;
            case H_INVOKEINTERFACE:
                return INVOKEINTERFACE;
            case H_INVOKESPECIAL:
                return INVOKESPECIAL;
            default:
                throw new IllegalArgumentException("Invalid handle kind: " + Integer.toHexString(tag));
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

    // virtual methods will be converted to static methods that accept the instance as the first argument
    // TODO: clean this up
    // TODO: accept Handle
    public static MethodNode createWrapperFunction(final String newName, final String targetClass, final String name, final String descriptor, final int handleTag) {
        switch (handleTag) {
            case H_INVOKEVIRTUAL:   break;
            case H_INVOKEINTERFACE: break;
            case H_INVOKESPECIAL:   break;
            case H_INVOKESTATIC:    break;
            default: throw new IllegalArgumentException("Invalid handle tag: " + handleTag);
        }
        // if the method is virtual the new method will have an extra argument to accept the instance
        List<Type> argumentTypes  = new ArrayList<>(Arrays.asList(Type.getArgumentTypes(descriptor)));// + (handleTag  == H_INVOKESTATIC ? 1 : 0);
        if (handleTag  != H_INVOKESTATIC) {
            argumentTypes.add(0, Type.getObjectType(targetClass));
        }
        final Type[] variableTypes = argumentTypes.stream()
                .map(type -> isObjectType(type) ? getRawType(type) : type)
                .toArray(Type[]::new);

        final StringBuilder descBuilder = new StringBuilder();
        descBuilder.append('(');
        for (Type t : variableTypes) descBuilder.append(t.getDescriptor());
        descBuilder.append(')');
        descBuilder.append(getRawType(Type.getReturnType(descriptor)).getDescriptor());

        final MethodNode wrapperMethod = new MethodNode(ACC_PUBLIC | ACC_STATIC, newName, descBuilder.toString(), null, null);
        final LabelNode start = new LabelNode();
        final LabelNode end = new LabelNode();
        final InsnList insnList = new InsnList();
        insnList.add(start);
        for (int i = 0; i < variableTypes.length; i++) {
            final int varOpcode = getVariableOpcode(variableTypes[i]);
            insnList.add(new VarInsnNode(varOpcode, i));
            if (isObjectType(variableTypes[i]))
                insnList.add(new TypeInsnNode(CHECKCAST, argumentTypes.get(i).getInternalName()));
        }

        insnList.add(new MethodInsnNode(opcodeFromTag(handleTag), targetClass, name, descriptor, handleTag == H_INVOKEINTERFACE));
        insnList.add(new InsnNode(getReturnOpcode(Type.getReturnType(descriptor))));
        insnList.add(end);

        wrapperMethod.instructions = insnList;

        for (int i = 0; i < variableTypes.length; i++) {
            final Type t = variableTypes[i];
            wrapperMethod.visitLocalVariable("arg" + i, t.getDescriptor(), null, start.getLabel(), end.getLabel(), i);
        }

        return wrapperMethod;
    }

    private static boolean isObjectType(Type t) {
        return t.getSort() == Type.OBJECT || t.getSort() == Type.ARRAY;
    }

    public static Type toRawTypeDescriptor(Type desc) {
        final Type ret = getRawType(desc.getReturnType());
        final Type[] args = Stream.of(desc.getArgumentTypes())
                .map(AsmUtil::getRawType)
                .toArray(Type[]::new);
        return Type.getMethodType(ret, args);
    }

    // similar to toRawTypeDescriptor but does not change return type
    // TODO: avoid code duplication
    public static Type descriptorWithRawTypeArgs(Type desc) {
        final Type[] args = Stream.of(desc.getArgumentTypes())
                .map(AsmUtil::getRawType)
                .toArray(Type[]::new);
        return Type.getMethodType(desc.getReturnType(), args);
    }

    // if the type is an array or object it is reduced to Object, else it is a primitive type
    private static Type getRawType(final Type t) {
        if (t.getSort() == Type.METHOD) throw new IllegalArgumentException("method is not a valid type");
        if (t.getSort() == Type.OBJECT || t.getSort() == Type.ARRAY) {
            return OBJECT_TYPE;
        }
        return t;
    }

}
