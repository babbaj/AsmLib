package net.futureclient.asm.obfuscation;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.JsonObject;
import net.futureclient.asm.obfuscation.structs.ClassInfo;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ObfuscatedStateMapper implements IMapper {
    private ObfuscatedStateMapper() {}

    private static final ObfuscatedStateMapper INSTANCE = new ObfuscatedStateMapper();

    // mcp - obf
    private BiMap<String, String> classBiMap = HashBiMap.create();

    // mcp -> fields & methods
    private Map<String, ClassInfo> classToMembers = new HashMap<>();


    public static ObfuscatedStateMapper getInstance() {
        return INSTANCE;
    }

    // TODO: finish implementing this
    public void addMappings(JsonObject root) {
        final JsonObject mappings = root.getAsJsonObject("mappings");
        mappings.entrySet().forEach(entry -> {
            final String className = entry.getKey();
            final JsonObject data = entry.getValue().getAsJsonObject();
            final String notchClassName = data.getAsJsonPrimitive("notch").getAsString();

            final JsonObject fields = data.getAsJsonObject("fields");
            final JsonObject methods = data.getAsJsonObject("fields");
        });
    }

    @Nullable
    @Override
    public String getClassName(String className) {
        return classBiMap.get(className);
    }

    @Nullable
    @Override
    public String getMethodName(String parentClassName, String methodName, String methodDescriptor) {
        return Optional.ofNullable(classToMembers.get(parentClassName))
                .map(ClassInfo::getMethods)
                .map(map -> map.get(methodName + methodDescriptor))
                .map(member -> member.notch)
                .orElse(null);
    }

    @Nullable
    @Override
    public String getFieldName(String parentClassName, String fieldName) {
        return Optional.ofNullable(classToMembers.get(parentClassName))
                .map(ClassInfo::getFields)
                .map(map -> map.get(fieldName))
                .map(info -> info.notch)
                .orElse(null);
    }

    @Nullable
    @Override
    public String getMcpClassName(String obfClassName) {
        return classBiMap.inverse().get(obfClassName);
    }
}
