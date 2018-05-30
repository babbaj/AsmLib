package net.futureclient.asm.obfuscation;

import javax.annotation.Nullable;

/**
 * Created by Babbaj on 5/28/2018.
 */
public interface IMapper {

    /**
     * Maps a name to its obfuscated name or {@code null} if there is none
     */

    @Nullable String getClassName(String className);

    @Nullable String getMethodName(String parentClassName, String methodName, String methodDescriptor);

    @Nullable String getFieldName(String parentClassName, String fieldName);
}
