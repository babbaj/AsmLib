package me.hugenerd.load.config;

import net.futureclient.asm.config.Config;

public final class MemeConfig extends Config {

    public MemeConfig() {
        super("meme-mod");
        //this.addClassTransformers(new MainTransformer());
        //this.addClassTransformers(TestTransformer.class);
        this.addClassTransformer("me.hugenerd.load.transformer.LambdaTestTransformer");
    }
}
