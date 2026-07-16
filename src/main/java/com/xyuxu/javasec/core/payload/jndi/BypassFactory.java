package com.xyuxu.javasec.core.payload.jndi;

import javax.naming.Reference;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class BypassFactory {

    private static final Map<String, BypassGenerator> generators = new HashMap<>();

    static {
        generators.put("Tomcat EL", new TomcatELBypass());
        generators.put("Groove",new GrooveBypass());
        // generators.put("WebSphere", new WebsphereBypass());
    }

    public static Reference getReference(String name, String command) {
        BypassGenerator generator = generators.get(name);
        if (generator == null) {
            throw new IllegalArgumentException("Unknown Bypass Type: " + name);
        }
        return generator.generate(command);
    }
    public static Set<String> getAllTypes() {
        return generators.keySet();
    }
}
