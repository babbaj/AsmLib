package net.futureclient.asm.transformer.util;

import com.google.common.collect.ImmutableMap;
import jdk.internal.org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Created by Babbaj on 5/26/2018.
 */
public class AnnotationInfo {

    private final Class<? extends Annotation> annotationClazz;
    private final ImmutableMap<String, ?> valueMap;

    public static AnnotationInfo fromAsm(ClassNode node, Class<? extends Annotation> clazz) {
        return Stream.concat(node.invisibleAnnotations.stream(), node.invisibleAnnotations.stream())
                .filter(annot -> annot.desc.equals('L' + Type.getInternalName(clazz) + ';'))
                .findFirst()
                .map(AnnotationInfo::fromAsm)
                .orElseThrow(() -> new IllegalArgumentException("ClassNode does not contain annotation " + clazz.getName()));
    }

    @SuppressWarnings("unchecked")
    public static AnnotationInfo fromAsm(AnnotationNode node) {
        if (!node.desc.matches("(?:L).+(?:;)")) throw new IllegalArgumentException("Invalid ASM Annotation Class Name: " + node.desc);
        String className = node.desc.substring(1, node.desc.length()-1); // strip 'L' and ';'
        Class<?> clazz;
        try {
            clazz = Class.forName(className.replace("/", "."));
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
        ImmutableMap<String, ?> map = ImmutableMap.copyOf(createValueMap(node));
        return new AnnotationInfo((Class<? extends Annotation>)clazz, map);
    }

    private AnnotationInfo(Class<? extends Annotation> clazz, ImmutableMap<String, ?> valueMap) {
        this.annotationClazz = clazz;
        this.valueMap = valueMap;
    }

    @SuppressWarnings("unchecked")
    public <T> T getValue(String name) {
        return (T)valueMap.get(name);
    }

    public Class<?> getAnnotationClass() {
        return this.annotationClazz;
    }

    /**
     * Creates a name-value map from an annotation's value list
     *
     * See {@link org.objectweb.asm.tree.AnnotationNode#values}
     */
    private static Map<String, ?> createValueMap(AnnotationNode annotation) {
        Iterator<Object> iter = annotation.values.iterator();
        Map<String, Object> out = new HashMap<>();
        while (iter.hasNext()) {
            out.put((String)iter.next(), iter.next());
        }
        return out;
    }
}
