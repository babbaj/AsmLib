package net.futureclient.asm.transformer;

import org.objectweb.asm.tree.FieldNode;

public abstract class FieldTransformer {

    private final String fieldName;
    private final ClassTransformer classTransformer;

    public FieldTransformer(final String fieldName, final ClassTransformer classTransformer) {
        this.fieldName = fieldName;
        this.classTransformer = classTransformer;
    }

    public abstract void inject(FieldNode fieldNode);

    public String getFieldName() {
        return this.fieldName;
    }

    public final ClassTransformer getClassTransformer() {
        return this.classTransformer;
    }
}
