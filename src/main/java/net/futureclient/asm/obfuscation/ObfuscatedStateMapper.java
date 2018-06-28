package net.futureclient.asm.obfuscation;

import javax.annotation.Nullable;

public class ObfuscatedStateMapper implements IMapper {

    private static final ObfuscatedStateMapper INSTANCE = new ObfuscatedStateMapper();

    private ObfuscatedStateMapper() {}

    public static ObfuscatedStateMapper getInstance() {
        return INSTANCE;
    }

    @Nullable
    @Override
    public String getClassName(String className) {
        return null;
    }

    @Nullable
    @Override
    public String getMethodName(String parentClassName, String methodName, String methodDescriptor) {
        return null;
    }

    @Nullable
    @Override
    public String getFieldName(String parentClassName, String fieldName) {
        return null;
    }
}
