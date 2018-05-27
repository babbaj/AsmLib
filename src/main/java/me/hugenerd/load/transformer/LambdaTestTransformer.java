package me.hugenerd.load.transformer;

import me.hugenerd.Main;
import net.futureclient.asm.transformer.annotation.Inject;
import net.futureclient.asm.transformer.annotation.Transformer;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;

/**
 * Created by Babbaj on 5/21/2018.
 */
@Transformer(Main.class)
public class LambdaTestTransformer {

    @Inject(target = "main([Ljava/lang/String;)V")
    public void inject(MethodNode node) {
        //node.instructions.insert(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(getClass()), "onMain", "()V", false));
        node.instructions.insert(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(getClass()), "lambda$inject$0", "()V", false));

        test(() -> System.out.println("cool lambda"));
    }

    private void test(Runnable r) {}
}
