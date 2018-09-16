package net.futureclient.asm.obfuscation;

import com.google.common.io.ByteStreams;

import java.io.IOException;
import java.io.InputStream;

public enum MappingType {
    MCP, // dev
    SEARGE,
    NOTCH,
    CUSTOM; // unsupported

    static final MappingType compiledMappingType;

    static {
        try (final InputStream is = MappingType.class.getClassLoader().getResourceAsStream("asmlib.mappingtype")) {
            String str = new String(ByteStreams.toByteArray(is));
            compiledMappingType = MappingType.valueOf(str);
        } catch (IOException ex) {
            throw new Error(ex);
        }
    }

    // fallback to this if not in forge
    public static MappingType getCompiledMappingType() {
        return compiledMappingType;
    }
}
