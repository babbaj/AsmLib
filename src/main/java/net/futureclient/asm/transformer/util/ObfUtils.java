package net.futureclient.asm.transformer.util;

import java.util.function.UnaryOperator;
import net.futureclient.asm.obfuscation.RuntimeState;
import org.objectweb.asm.Type;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class ObfUtils {
    private ObfUtils() {}

    public static String remapMethodName(String parentClassName, String methodName, String methodDescriptor) {
        return Optional.ofNullable(RuntimeState.getMapper().getMethodName(parentClassName, methodName, methodDescriptor))
                .orElseGet(() -> {
                    System.err.println("Failed to find obfuscation mapping for method: " + parentClassName+ "::" + methodName + methodDescriptor);
                    return methodName;
                });
    }

    public static String remapMethodDesc(String descriptor) {
        final Type typeDesc = Type.getMethodType(descriptor);
        final String remappedArgs =
                Stream.of(typeDesc.getArgumentTypes())
                    .map(Type::getDescriptor)
                    .map(ObfUtils::remapType)
                    .collect(Collectors.joining());
        final String remappedRet = remapType(typeDesc.getReturnType().getDescriptor());

        return String.format("(%s)%s", remappedArgs, remappedRet);
    }

    // if the remapper returns null then use the same name
    public static String remapClass(String className) {
        return remapClass(className, RuntimeState.getMapper()::getClassName, "obf");
    }

    public static String remapClassToMcp(String className) {
        return remapClass(className, RuntimeState.getMapper()::getMcpClassName, "mcp");
    }

    private static String remapClass(String className, UnaryOperator<String> remapper, String type) {
        if (className.length() == 1) return className;
        return Optional.ofNullable(remapper.apply(className.replace(".", "/")))
            .orElseGet(() -> {
                System.err.println("Failed to find " + type + " mapping for: " + className);
                return className;
            });
    }

    // may be an array
    public static String remapType(String typeDesc) {
        Type type = Type.getType(typeDesc);
        final int dims = type.getSort() == Type.ARRAY ? type.getDimensions() : 0;
        String elementType = dims > 0 ? type.getElementType().getDescriptor() : type.getDescriptor();
        elementType = elementType.replaceAll("^L|;$", ""); // convert descriptor to internal name
        final String remapped = elementType.length() > 1 ? 'L' + remapClass(elementType) + ";"
                : elementType; // remap if not primitive
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < dims; i++)
            sb.append("[");
        sb.append(remapped);

        return sb.toString();
    }
    // works for primitives
    public static String getInternalName(Type type) {
        if (type.getSort() == Type.OBJECT || type.getSort() == Type.ARRAY)
            return type.getInternalName();
        return type.getDescriptor();
    }
}
