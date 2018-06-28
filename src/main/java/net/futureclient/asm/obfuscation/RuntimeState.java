package net.futureclient.asm.obfuscation;

public class RuntimeState {

    private static IMapper remapper = UnobfuscatedRemapper.getInstance();

    public static State getObfuscationState() {
        return State.NOTCH;
    }

    public static IMapper getMapper() {
        return remapper;
    }

    // for now just use forgehax remapping
    // TODO: implement remapping ourself
    public static void setRemapper(IMapper mapper) {
        remapper = mapper;
    }
}
