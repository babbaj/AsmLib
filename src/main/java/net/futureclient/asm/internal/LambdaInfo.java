package net.futureclient.asm.internal;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Type;

public class LambdaInfo {

    public final Handle targetMethod;
    public final Type realMethodDesc;
    public final String lambdaDesc;

    public LambdaInfo(Handle target, Type realMethodDesc, String lambdaDesc) {
        this.targetMethod = target;
        this.realMethodDesc = realMethodDesc;
        this.lambdaDesc = lambdaDesc;
    }
}
