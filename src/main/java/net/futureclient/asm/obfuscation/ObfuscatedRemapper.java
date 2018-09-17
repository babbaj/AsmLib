package net.futureclient.asm.obfuscation;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.futureclient.asm.obfuscation.structs.ClassInfo;
import net.futureclient.asm.obfuscation.structs.ClassMember;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class ObfuscatedRemapper implements IMapper {
    private ObfuscatedRemapper() {}

    private static final ObfuscatedRemapper INSTANCE = new ObfuscatedRemapper();

    // mcp - obf
    private BiMap<String, String> classBiMap = HashBiMap.create();

    // mcp -> fields & methods
    private Map<String, ClassInfo> classToMembers = new HashMap<>();

    // TODO: might want a separate mapper for every config
    public static ObfuscatedRemapper getInstance() {
        return INSTANCE;
    }

    public void addMappings(JsonObject root) {
        final JsonObject mappings = root.getAsJsonObject("mappings");
        mappings.entrySet().forEach(entry -> {
            final String className = entry.getKey();
            final JsonObject data = entry.getValue().getAsJsonObject();
            final String notchClassName = data.getAsJsonPrimitive("notch").getAsString();

            JsonObject fields = data.getAsJsonObject("fields");
            JsonObject methods = data.getAsJsonObject("methods");
            final Map<String, ClassMember> fieldMap = jsonToMemberMap(fields);
            final Map<String, ClassMember> methodMap = jsonToMemberMap(methods);

            classBiMap.put(className, notchClassName);
            classToMembers.put(className, new ClassInfo(fieldMap, methodMap));
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

    private Map<String, ClassMember> jsonToMemberMap(JsonObject object) {
        return object.entrySet().stream()
                .collect(jsonToMemberMapCollector());
    }

    private static Collector<Map.Entry<String, JsonElement>, ?, Map<String, ClassMember>> jsonToMemberMapCollector() {
        return mapToSelf(Map.Entry::getKey, entry -> {
            final JsonObject obj = entry.getValue().getAsJsonObject();
            return new ClassMember(obj.get("notch").getAsString(), obj.get("searge").getAsString());
        });
    }

    private static <T, K, V> Collector<T, ?, Map<K, V>> mapToSelf(Function<T, K> keyExtractor,
                                                                  Function<T, V> valueMapper)
    {
        return Collectors.toMap(
                keyExtractor,
                valueMapper,
                (k1, k2) -> {
                    throw new IllegalStateException("Duplicate key: " + k1);
                },
                LinkedHashMap::new
        );
    }
}
