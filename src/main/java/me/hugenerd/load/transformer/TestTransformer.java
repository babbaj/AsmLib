package me.hugenerd.load.transformer;

import net.futureclient.asm.transformer.annotation.Hook;
import net.futureclient.asm.transformer.annotation.Inject;
import net.futureclient.asm.transformer.annotation.Transformer;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import static org.objectweb.asm.Opcodes.*;

/**
 * Created by Babbaj on 5/17/2018.
 */
@Transformer(target = "me.hugenerd.Main")
public class TestTransformer {

    @Inject(target = "main([Ljava/lang/String;)V")
    public void inject(MethodNode node) {
        InsnList insnList = new InsnList();

        insnList.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(getClass()), "onMain", "()V", false));

        node.instructions.insert(insnList);
    }

    @Hook
    public static void onMain() {
        System.out.println("Hello from TestTransformer!");
    }

}
