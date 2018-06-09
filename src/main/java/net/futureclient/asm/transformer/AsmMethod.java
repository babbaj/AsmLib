package net.futureclient.asm.transformer;

import me.hugenerd.load.transformer.LambdaTestTransformer;
import net.futureclient.asm.internal.LambdaManager;
import net.minecraft.launchwrapper.Launch;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;


/**
 * Created by Babbaj on 6/5/2018.
 *
 * Wrapper for MethodNode that provides helper functions.
 */
public class AsmMethod {

    public final MethodNode method;
    private AbstractInsnNode cursor;
    public final ClassNode parent;

    public AsmMethod(MethodNode methodIn, ClassNode parentClass) {
        this.method = methodIn;
        this.parent = parentClass;
        this.cursor = method.instructions.getFirst();
    }


    public void run(Runnable r) {
        String[] func = LambdaManager.lambdas.get(r);
        if (func != null) {
            this.method.instructions.insertBefore(cursor,
                    new MethodInsnNode(INVOKESTATIC, func[0], func[1], func[2]));
        } else {
            FieldNode field = new FieldNode(ACC_PRIVATE | ACC_STATIC, "#lambda$0", Type.getDescriptor(Runnable.class), null, null);
            // TODO: set the field's value
            throw new IllegalStateException("Failed to find method from lambda object");
        }
    }

    // Injects a set of instructions to the classes static init function
    private void injectClinit(InsnList toAdd) {
        MethodNode clinit = parent.methods.stream()
                .filter(node -> node.name.equals("<clinit>"))
                .findFirst()
                .orElseGet(() -> {
                    MethodNode node = new MethodNode(ACC_STATIC, "<clinit>", "()V", null, null);
                    node.visitInsn(RETURN);
                    parent.methods.add(node);
                    return node;
                });
        clinit.instructions.insert(toAdd);
    }
}
