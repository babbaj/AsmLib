package net.futureclient.asm.obfuscation;

import javax.annotation.Nullable;

/**
 * Created by Babbaj on 5/28/2018.
 *
 * Maps a name to its obfuscated name or {@code null} if there is none
 */
public interface IMapper {


    /**
     * Remaps class name
     * @param className The mcp class name
     * @return The obfuscated class name
     */
    @Nullable String getClassName(String className);

    /**
     * Remap method name
     * @param parentClassName The mcp name of the class the method is defined in
     * @param methodName The mcp name of the method
     * @param methodDescriptor The descriptor with mcp class names
     * @return the obf name of the method
     */
    @Nullable String getMethodName(String parentClassName, String methodName, String methodDescriptor);


    @Nullable String getFieldName(String parentClassName, String fieldName);

    // When compiling for vanilla, class literals will be compiled to obfuscated names while the given method name will still be mcp
    // so we need to get the mcp names of these classes to be able to get the obfuscated method name
    @Nullable String getMcpClassName(String obfClassName);
}
