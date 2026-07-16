package com.xyuxu.javasec.core.payload.gadgets;

import org.reflections.Reflections;

import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public interface ObjectPayload <T> {

     T getObject(final String command) throws Exception;

}
