package net.futureclient.asm.internal;

import org.objectweb.asm.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.objectweb.asm.Opcodes.*;
import static net.futureclient.asm.internal.AsmUtil.*;

public final class CarrierClass {
    private CarrierClass() {}

    public static <T> Class<?> createCarrierClass(final String className, final T dataInstance, final Class<T> type, final Type realMethodDesc, final Method abstractMethod) {
        if (!type.isInstance(dataInstance)) throw new IllegalArgumentException("Data is not an instance of the given type");

        ClassWriter cw = new ClassWriter(0);
        cw.visit(52, ACC_PUBLIC | ACC_SUPER, className, null, "java/lang/Object", null);

        {   // basic constructor, nothing special but it's required
            addBasicConstructor(cw, className, Type.getInternalName(Object.class));
        }

        { // the reference to the function
            FieldVisitor fv = cw.visitField(ACC_PUBLIC | ACC_STATIC | ACC_FINAL, "data", Type.getDescriptor(type), null, null);
            fv.visitEnd();
        }

        {   // the static function we will use to invoke the lambda
            final Type[] argTypes = Type.getArgumentTypes(abstractMethod);
            final Type abstractRetType = Type.getReturnType(abstractMethod);
            final Type realRetType = Type.getReturnType(realMethodDesc.getDescriptor());

            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC | ACC_STATIC | ACC_FINAL, abstractMethod.getName(), realMethodDesc.getDescriptor(), null, null);
            mv.visitCode();
            Label l0 = new Label();
            mv.visitLabel(l0);

            mv.visitFieldInsn(GETSTATIC, className, "data", Type.getDescriptor(type));
            // push arguments to stack
            for (int i = 0; i < argTypes.length; i++) {
                final Type t = argTypes[i];
                mv.visitVarInsn(getVariableOpcode(t), i);
            }
            mv.visitMethodInsn(INVOKEINTERFACE, Type.getInternalName(type), abstractMethod.getName(), Type.getMethodDescriptor(abstractMethod), true);

            Label l1 = new Label();
            mv.visitLabel(l1);

            // cast if we have the real type
            if (!abstractRetType.equals(realRetType)) {
                mv.visitTypeInsn(CHECKCAST, realRetType.getInternalName());
            }
            mv.visitInsn(getReturnOpcode(realRetType));

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
}
