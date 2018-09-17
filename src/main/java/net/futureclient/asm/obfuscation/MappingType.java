package net.futureclient.asm.obfuscation;

import com.google.common.io.ByteStreams;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public enum MappingType {
    MCP, // dev
    SEARGE,
    NOTCH,
    CUSTOM; // unsupported

    // returns the mapping type defined in the asmlib.mappingtype file, or empty if it couldnt be found
    // it is recommended to use RuntimeState:: getRuntimeMappingType instead of this
    // TODO: put this somewhere else
    public static Optional<MappingType> getCompiledMappingType() {
        try (final InputStream is = MappingType.class.getClassLoader().getResourceAsStream("asmlib.mappingtype")) {
            if (is != null) {
                String str = new String(ByteStreams.toByteArray(is));
                return Optional.of(MappingType.valueOf(str));
            } else {
                // dev
                return Optional.empty();
            }
        } catch (IOException ex) {
            throw new Error(ex);
        }
    }
}
