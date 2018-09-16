package net.futureclient.asm.obfuscation;

public class RuntimeState {

    private static IMapper remapper = ObfuscatedRemapper.getInstance();

    // TODO: implement this
    public static MappingType getObfuscationState() {
        return MappingType.getCompiledMappingType();
    }

    public static IMapper getMapper() {
        return remapper;
    }


    @Deprecated
    public static void setRemapper(IMapper mapper) {
        remapper = mapper;
    }
}
