package net.futureclient.asm.internal;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Type;

import java.util.HashMap;
import java.util.Map;

public class LambdaInfo {

    public final Handle targetMethod;
    public final Type realMethodDesc;
    public final String lambdaDesc;

    // maps lambda objects to their underlying method.
    public static final Map<Object, LambdaInfo> lambdas = new HashMap<>();

    public LambdaInfo(Handle target, Type realMethodDesc, String lambdaDesc) {
        this.targetMethod = target;
        this.realMethodDesc = realMethodDesc;
        this.lambdaDesc = lambdaDesc;
    }

    // this method's signature can not be changed without changing the asm code in TransformerPreProcessor#injectAtLambda
    public static void addLambda(Object instance, String mOwner, String mName, String mDesc, int tag, String realMethodDesc, String nodeDesc) {
        Handle target = new Handle(tag, mOwner, mName, mDesc);
        Type type = Type.getMethodType(realMethodDesc);
        lambdas.put(instance, new LambdaInfo(target, type, nodeDesc));
    }
}
