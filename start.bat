@echo off
REM LyraSuite 启动脚本 (Windows)

set JAR_PATH=.\target\LyraSuite-Web-2.0.0.jar

REM JVM 参数
set JAVA_OPTS=-Xmx1024M -Xms256M
set JAVA_OPTS=%JAVA_OPTS% -XX:TieredStopAtLevel=1
set JAVA_OPTS=%JAVA_OPTS% -Dspring.output.ansi.enabled=always
set JAVA_OPTS=%JAVA_OPTS% -Dfile.encoding=UTF-8

REM --add-opens (JDK 17+ 模块访问)
set JAVA_OPTS=%JAVA_OPTS% --add-opens java.base/java.lang=ALL-UNNAMED
set JAVA_OPTS=%JAVA_OPTS% --add-opens java.base/java.lang.reflect=ALL-UNNAMED
set JAVA_OPTS=%JAVA_OPTS% --add-opens java.base/java.util=ALL-UNNAMED
set JAVA_OPTS=%JAVA_OPTS% --add-opens java.base/java.text=ALL-UNNAMED
set JAVA_OPTS=%JAVA_OPTS% --add-opens java.base/java.io=ALL-UNNAMED
set JAVA_OPTS=%JAVA_OPTS% --add-opens java.base/java.net=ALL-UNNAMED
set JAVA_OPTS=%JAVA_OPTS% --add-opens java.base/java.nio=ALL-UNNAMED
set JAVA_OPTS=%JAVA_OPTS% --add-opens java.base/sun.reflect.annotation=ALL-UNNAMED
set JAVA_OPTS=%JAVA_OPTS% --add-opens java.base/sun.net.www.protocol.http=ALL-UNNAMED
set JAVA_OPTS=%JAVA_OPTS% --add-opens java.base/sun.net.www.protocol.jar=ALL-UNNAMED
set JAVA_OPTS=%JAVA_OPTS% --add-opens java.base/jdk.internal.reflect=ALL-UNNAMED
set JAVA_OPTS=%JAVA_OPTS% --add-opens java.base/jdk.internal.loader=ALL-UNNAMED
set JAVA_OPTS=%JAVA_OPTS% --add-opens java.desktop/java.awt.font=ALL-UNNAMED
set JAVA_OPTS=%JAVA_OPTS% --add-opens java.desktop/javax.swing.undo=ALL-UNNAMED
set JAVA_OPTS=%JAVA_OPTS% --add-opens java.desktop/javax.swing.event=ALL-UNNAMED
set JAVA_OPTS=%JAVA_OPTS% --add-opens java.desktop/sun.awt.X11=ALL-UNNAMED
set JAVA_OPTS=%JAVA_OPTS% --add-opens java.xml/com.sun.org.apache.xalan.internal.xsltc=ALL-UNNAMED
set JAVA_OPTS=%JAVA_OPTS% --add-opens java.xml/com.sun.org.apache.xalan.internal.xsltc.trax=ALL-UNNAMED
set JAVA_OPTS=%JAVA_OPTS% --add-opens java.xml/com.sun.org.apache.xalan.internal.xsltc.runtime=ALL-UNNAMED
set JAVA_OPTS=%JAVA_OPTS% --add-opens java.xml/com.sun.org.apache.xalan.internal.xsltc.dom=ALL-UNNAMED
set JAVA_OPTS=%JAVA_OPTS% --add-opens java.xml/com.sun.org.apache.xml.internal.dtm=ALL-UNNAMED
set JAVA_OPTS=%JAVA_OPTS% --add-opens java.xml/com.sun.org.apache.xpath.internal.objects=ALL-UNNAMED
set JAVA_OPTS=%JAVA_OPTS% --add-opens java.xml/com.sun.org.apache.xml.internal.serializer=ALL-UNNAMED
set JAVA_OPTS=%JAVA_OPTS% --add-opens java.rmi/sun.rmi.server=ALL-UNNAMED
set JAVA_OPTS=%JAVA_OPTS% --add-opens java.rmi/sun.rmi.transport=ALL-UNNAMED
set JAVA_OPTS=%JAVA_OPTS% --add-opens jdk.naming.rmi/com.sun.jndi.rmi.registry=ALL-UNNAMED
set JAVA_OPTS=%JAVA_OPTS% --add-opens java.naming/com.sun.jndi.ldap=ALL-UNNAMED
set JAVA_OPTS=%JAVA_OPTS% --add-opens java.management/javax.management=ALL-UNNAMED

REM 应用参数 (可通过环境变量覆盖)
if "%SERVER_PORT%"=="" set SERVER_PORT=8080
if "%ADMIN_USERNAME%"=="" set ADMIN_USERNAME=admin
if "%ADMIN_PASSWORD%"=="" set ADMIN_PASSWORD=admin123

set APP_OPTS=--server.port=%SERVER_PORT%
set APP_OPTS=%APP_OPTS% --myapp.admin.username=%ADMIN_USERNAME%
set APP_OPTS=%APP_OPTS% --myapp.admin.password=%ADMIN_PASSWORD%

echo Starting LyraSuite on port %SERVER_PORT% ...
java %JAVA_OPTS% -jar %JAR_PATH% %APP_OPTS% %*
