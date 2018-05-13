package me.hugenerd.load.transformer;

import net.futureclient.asm.transformer.ClassTransformer;
import net.futureclient.asm.transformer.MethodTransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public final class MainTransformer extends ClassTransformer {

    public MainTransformer() {
        super("me.hugenerd.Main");
        this.addMethodTransformers(new MethodTransformer("main", this) {
            @Override
            public void inject(MethodNode methodNode) {
                InsnList insnList = new InsnList();

                insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "me/hugenerd/load/transformer/MessageCheatingTransformer", "onMain", "()V", false));

                methodNode.instructions.insert(insnList);
            }
        });
    }

    public static void onMain() {
        System.out.println("hello from MainTransformer!");
    }
}
