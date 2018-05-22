package net.futureclient.asm.transformer;

import com.google.common.collect.Streams;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

/**
 * Created by Babbaj on 5/20/2018.
 */
public class MethodNodeWrapper {

    public final MethodNode methodNode;

    public MethodNodeWrapper(MethodNode node) {
        this.methodNode = node;
    }

    private Stream<AbstractInsnNode> stream() {
        return Streams.stream(methodNode.instructions.iterator());
    }

    public void forEach(Consumer<AbstractInsnNode> consumer) {
        methodNode.instructions.iterator().forEachRemaining(consumer);
    }

    @SuppressWarnings("unchecked")
    public <T> void modifyConstants(UnaryOperator<T> mapper, Class<T> type) {
        if (!(type == String.class || Number.class.isAssignableFrom(type) || type == Type.class))
            throw new IllegalArgumentException("Invalid LDC type");
        this.stream()
                .filter(LdcInsnNode.class::isInstance)
                .map(LdcInsnNode.class::cast)
                .filter(ldc -> type.isInstance(ldc.cst))
                .forEach(ldc -> ldc.cst = mapper.apply((T)ldc.cst));
    }

    public void modifyConstants(Consumer<LdcInsnNode> consumer) {
        this.stream()
                .filter(LdcInsnNode.class::isInstance)
                .map(LdcInsnNode.class::cast)
                .forEach(consumer);
    }
}
