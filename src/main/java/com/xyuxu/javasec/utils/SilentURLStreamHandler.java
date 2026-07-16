package com.xyuxu.javasec.utils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

public class SilentURLStreamHandler extends URLStreamHandler {
    @Override
    protected URLConnection openConnection(URL u) throws IOException {
        return null;
    }

    @Override
    protected synchronized InetAddress getHostAddress(URL u) {
        return null;
    }
}