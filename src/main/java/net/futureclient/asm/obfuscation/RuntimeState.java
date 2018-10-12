package net.futureclient.asm.obfuscation;

public class RuntimeState {

    private static IMapper remapper = ObfuscatedRemapper.getInstance();

    private static MappingType runtimeMappingType;

    public static MappingType getRuntimeMappingType() {
        return runtimeMappingType;
    }

    public static void setRuntimeMappingType(MappingType type) {
        switch (type) {
            case MCP:
                setRemapper(UnobfuscatedRemapper.getInstance());
                break;
            case SEARGE:
                throw new UnsupportedOperationException("searge");
            case NOTCH:
                setRemapper(ObfuscatedRemapper.getInstance());
                break;
            case CUSTOM:
                throw new UnsupportedOperationException("custom");
            default:
                throw new IllegalArgumentException();
        }
        runtimeMappingType = type;
    }

    public static IMapper getMapper() {
        return remapper;
    }


    private static void setRemapper(IMapper mapper) {
        remapper = mapper;
    }
}
