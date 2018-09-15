package net.futureclient.asm.obfuscation.structs;

import java.util.Collections;
import java.util.Map;

public class ClassInfo {

    private Map<String, ClassMember> fields;

    // full name+args+return type descriptor (e.g "getMinecraft()Lnet/minecraft/client/Minecraft;")
    private Map<String, ClassMember> methods;

    public ClassInfo(Map<String, ClassMember> fields, Map<String, ClassMember> methods) {
        this.fields = Collections.unmodifiableMap(fields);
        this.methods = Collections.unmodifiableMap(methods);
    }

    public Map<String, ClassMember> getFields() {
        return this.fields;
    }

    public Map<String, ClassMember> getMethods() {
        return this.methods;
    }

}
