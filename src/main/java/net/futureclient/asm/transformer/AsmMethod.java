package net.futureclient.asm.transformer;

import me.hugenerd.load.transformer.LambdaTestTransformer;
import net.futureclient.asm.AsmLib;
import net.futureclient.asm.config.Config;
import net.futureclient.asm.internal.LambdaManager;
import net.futureclient.asm.internal.TransformerUtil;
import net.minecraft.launchwrapper.Launch;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.*;

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
    private Config config;

    // Maps config to number of carrier classes
    static Map<Config, Integer> meme = new HashMap<>();

    public AsmMethod(MethodNode methodIn, ClassNode parentClass) {
        this.method = methodIn;
        this.parent = parentClass;
        this.cursor = method.instructions.getFirst();
        this.config = AsmLib.getConfigManager().getDefaultConfig();
    }

    public AsmMethod(MethodNode methodIn, ClassNode parentClass, Config configIn) {
        this(methodIn, parentClass);
        this.config = configIn;
    }


    public void run(Runnable r) {
        String[] func = LambdaManager.lambdas.get(r);
        if (func != null) {
            this.method.instructions.insertBefore(cursor,
                    new MethodInsnNode(INVOKESTATIC, func[0], func[1], func[2]));
        } else {
            // TODO: get unique class name
            final String className = "#lambda$0";
            TransformerUtil.createCarrierClass(className, r, Runnable.class);
            this.method.instructions.insertBefore(cursor,
                    new FieldInsnNode(GETSTATIC, className, "data", Type.getDescriptor(Runnable.class)));
            this.method.instructions.insertBefore(cursor,
                    new MethodInsnNode(INVOKEINTERFACE, Type.getInternalName(Runnable.class), "run", "()V"));
        }
    }

    // Injects a set of instructions to the class's static init function
    /*private void injectClinit(InsnList toAdd) {
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
    }*/
}
