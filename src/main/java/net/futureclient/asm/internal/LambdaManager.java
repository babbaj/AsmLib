package net.futureclient.asm.internal;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Babbaj on 6/7/2018.
 */
public class LambdaManager {

    // maps lambda objects to their underlying method.
    // the String[] contains the methods class name, method name, and method descriptor in that order
    // TODO: do this better
    public static final Map<Object, String[]> lambdas = new HashMap<>();

    // this method's signature can not be changed without changing the asm code in TransformerPreProcessor#addLambdaMeme
    public static void addLambda(Object instance, String mOwner, String mName, String mDesc, int tagType) {
        lambdas.put(instance, new String[] {mOwner, mName, mDesc, String.valueOf(tagType)});
    }
}
