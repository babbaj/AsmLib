package net.futureclient.asm.obfuscation;

import javax.annotation.Nullable;

public class UnobfuscatedRemapper implements IMapper {

    private static final UnobfuscatedRemapper INSTANCE = new UnobfuscatedRemapper();

    private UnobfuscatedRemapper() {}

    public static UnobfuscatedRemapper getInstance() {
        return INSTANCE;
    }

    @Nullable
    @Override
    public String getClassName(String className) {
        return className;
    }

    @Nullable
    @Override
    public String getMethodName(String parentClassName, String methodName, String methodDescriptor) {
        return methodName;
    }

    @Nullable
    @Override
    public String getFieldName(String parentClassName, String fieldName) {
        return fieldName;
    }

    @Nullable
    @Override
    public String getMcpClassName(String obfClassName) {
        return null;
    }
}
