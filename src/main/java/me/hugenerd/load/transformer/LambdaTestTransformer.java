package me.hugenerd.load.transformer;

import me.hugenerd.Main;
import net.futureclient.asm.internal.InjectedField;
import net.futureclient.asm.transformer.AsmMethod;
import net.futureclient.asm.transformer.annotation.Inject;
import net.futureclient.asm.transformer.annotation.Transformer;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.function.Function;
import java.util.function.Supplier;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;

/**
 * Created by Babbaj on 5/21/2018.
 */
@Transformer(Main.class)
public class LambdaTestTransformer {

    @Inject(name = "main", args = {String[].class})
    public void inject(AsmMethod method) {
        method.run(() -> System.out.println("cool lambda"));
    }
}
