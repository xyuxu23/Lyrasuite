#!/bin/bash
# LyraSuite 启动脚本

JAR_PATH="./target/LyraSuite-Web-2.0.0.jar"

# JVM 参数
JAVA_OPTS="-Xmx1024M -Xms256M"
JAVA_OPTS="$JAVA_OPTS -XX:TieredStopAtLevel=1"
JAVA_OPTS="$JAVA_OPTS -Dspring.output.ansi.enabled=always"
JAVA_OPTS="$JAVA_OPTS -Dfile.encoding=UTF-8"

# --add-opens (JDK 17+ 模块访问)
JAVA_OPTS="$JAVA_OPTS --add-opens java.base/java.lang=ALL-UNNAMED"
JAVA_OPTS="$JAVA_OPTS --add-opens java.base/java.lang.reflect=ALL-UNNAMED"
JAVA_OPTS="$JAVA_OPTS --add-opens java.base/java.util=ALL-UNNAMED"
JAVA_OPTS="$JAVA_OPTS --add-opens java.base/java.text=ALL-UNNAMED"
JAVA_OPTS="$JAVA_OPTS --add-opens java.base/java.io=ALL-UNNAMED"
JAVA_OPTS="$JAVA_OPTS --add-opens java.base/java.net=ALL-UNNAMED"
JAVA_OPTS="$JAVA_OPTS --add-opens java.base/java.nio=ALL-UNNAMED"
JAVA_OPTS="$JAVA_OPTS --add-opens java.base/sun.reflect.annotation=ALL-UNNAMED"
JAVA_OPTS="$JAVA_OPTS --add-opens java.base/sun.net.www.protocol.http=ALL-UNNAMED"
JAVA_OPTS="$JAVA_OPTS --add-opens java.base/sun.net.www.protocol.jar=ALL-UNNAMED"
JAVA_OPTS="$JAVA_OPTS --add-opens java.base/jdk.internal.reflect=ALL-UNNAMED"
JAVA_OPTS="$JAVA_OPTS --add-opens java.base/jdk.internal.loader=ALL-UNNAMED"
JAVA_OPTS="$JAVA_OPTS --add-opens java.desktop/java.awt.font=ALL-UNNAMED"
JAVA_OPTS="$JAVA_OPTS --add-opens java.desktop/javax.swing.undo=ALL-UNNAMED"
JAVA_OPTS="$JAVA_OPTS --add-opens java.desktop/javax.swing.event=ALL-UNNAMED"
JAVA_OPTS="$JAVA_OPTS --add-opens java.desktop/sun.awt.X11=ALL-UNNAMED"
JAVA_OPTS="$JAVA_OPTS --add-opens java.xml/com.sun.org.apache.xalan.internal.xsltc=ALL-UNNAMED"
JAVA_OPTS="$JAVA_OPTS --add-opens java.xml/com.sun.org.apache.xalan.internal.xsltc.trax=ALL-UNNAMED"
JAVA_OPTS="$JAVA_OPTS --add-opens java.xml/com.sun.org.apache.xalan.internal.xsltc.runtime=ALL-UNNAMED"
JAVA_OPTS="$JAVA_OPTS --add-opens java.xml/com.sun.org.apache.xalan.internal.xsltc.dom=ALL-UNNAMED"
JAVA_OPTS="$JAVA_OPTS --add-opens java.xml/com.sun.org.apache.xml.internal.dtm=ALL-UNNAMED"
JAVA_OPTS="$JAVA_OPTS --add-opens java.xml/com.sun.org.apache.xpath.internal.objects=ALL-UNNAMED"
JAVA_OPTS="$JAVA_OPTS --add-opens java.xml/com.sun.org.apache.xml.internal.serializer=ALL-UNNAMED"
JAVA_OPTS="$JAVA_OPTS --add-opens java.rmi/sun.rmi.server=ALL-UNNAMED"
JAVA_OPTS="$JAVA_OPTS --add-opens java.rmi/sun.rmi.transport=ALL-UNNAMED"
JAVA_OPTS="$JAVA_OPTS --add-opens jdk.naming.rmi/com.sun.jndi.rmi.registry=ALL-UNNAMED"
JAVA_OPTS="$JAVA_OPTS --add-opens java.naming/com.sun.jndi.ldap=ALL-UNNAMED"
JAVA_OPTS="$JAVA_OPTS --add-opens java.management/javax.management=ALL-UNNAMED"

# 应用参数 (可通过环境变量覆盖)
SERVER_PORT="${SERVER_PORT:-8080}"
ADMIN_USERNAME="${ADMIN_USERNAME:-admin}"
ADMIN_PASSWORD="${ADMIN_PASSWORD:-admin123}"

APP_OPTS="--server.port=$SERVER_PORT"
APP_OPTS="$APP_OPTS --myapp.admin.username=$ADMIN_USERNAME"
APP_OPTS="$APP_OPTS --myapp.admin.password=$ADMIN_PASSWORD"

echo "Starting LyraSuite on port $SERVER_PORT ..."
java $JAVA_OPTS -jar "$JAR_PATH" $APP_OPTS "$@"
