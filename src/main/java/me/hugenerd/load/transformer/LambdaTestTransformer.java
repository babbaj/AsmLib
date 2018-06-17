package me.hugenerd.load.transformer;

import me.hugenerd.Main;
import net.futureclient.asm.transformer.AsmMethod;
import net.futureclient.asm.transformer.annotation.Inject;
import net.futureclient.asm.transformer.annotation.Transformer;

import java.io.PrintStream;


/**
 * Created by Babbaj on 5/21/2018.
 */
@Transformer(Main.class)
public class LambdaTestTransformer {

    @Inject(name = "main", args = {String[].class})
    public void inject(AsmMethod method) {
        method.get(() -> System.out);
        method.get(() -> "👌👌👌👌👌👌👌👌");
        method.<PrintStream, String>consume_2(PrintStream::println);
    }
}
