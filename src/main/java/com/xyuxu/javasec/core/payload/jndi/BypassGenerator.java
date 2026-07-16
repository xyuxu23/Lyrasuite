package com.xyuxu.javasec.core.payload.jndi;

import javax.naming.Reference;

public interface BypassGenerator {

        /**
         * 生成恶意的 JNDI Reference 对象
         *
         * @param command 要执行的命令
         * @return 构造好的 Reference (或 ResourceRef)
         */
        Reference generate(String command);

}

