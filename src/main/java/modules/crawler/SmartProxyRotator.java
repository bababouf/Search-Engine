package modules.crawler;


import org.apache.http.HttpHost;

import java.util.concurrent.atomic.AtomicLong;

class SmartProxyRotator {
    private final String username;
    private final String password;
    public static final String PROXY_HOST = "us.smartproxy.com";
    public static final int PROXY_PORT = 10000;
    private final AtomicLong requestCount = new AtomicLong(0);

    public SmartProxyRotator(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public HttpHost getHttpHost() {
        long count = requestCount.incrementAndGet();
        return new HttpHost(PROXY_HOST, PROXY_PORT);
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}