package modules.crawler;

import modules.decompression.BrotliDecompressingEntity;
import modules.decompression.DeflateDecompressingEntity;
import modules.decompression.GzipDecompressingEntity;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.net.*;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import javax.net.ssl.SSLContext;

public class WebCrawler {
    private final String baseDomain;
    private final String baseUrl;
    private final int maxDepth;
    private final Set<String> visitedUrls;
    private final ConcurrentLinkedQueue<PageContent> pageContents;
    private final ExecutorService executorService;
    private final AtomicInteger activeThreads;
    private final RobotsTxtParser robotsTxtParser;

    private static final int INITIAL_DELAY = 1000; // Initial delay in milliseconds (1 second)
    private static final int MAX_DELAY = 60000; // Max delay in milliseconds (1 minute)
    private static final double BACKOFF_FACTOR = 2.0; // Exponential backoff factor
    private static final int MAX_THREADS = 5; // Adjust based on your needs and system capabilities
    private static final int CONNECTION_TIMEOUT = 5000; // 5 seconds
    private static final int READ_TIMEOUT = 5000; // 5 seconds
    private int currentDelay = INITIAL_DELAY;
    private static final int MAX_RETRIES = 10;
    private static final int RETRY_DELAY = 5000; // 5 seconds
    private final CloseableHttpClient httpClient;
    private final SmartProxyRotator proxyRotator;

    // Broadened User-Agent pool
    private static final String[] USER_AGENTS = {
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Safari/537.36",

            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Safari/537.36",

            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Safari/537.36",

            "Mozilla/5.0 (iPhone; CPU iPhone OS 17_7 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) CriOS/129.0.6668.69 Mobile/15E148 Safari/604.1",

            "Mozilla/5.0 (iPad; CPU OS 17_7 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) CriOS/129.0.6668.69 Mobile/15E148 Safari/604.1",

            "Mozilla/5.0 (iPod; CPU iPhone OS 17_7 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) CriOS/129.0.6668.69 Mobile/15E148 Safari/604.1",

            "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.6668.69 Mobile Safari/537.36",

    };

    public WebCrawler(String baseUrl, int maxDepth, String smartProxyUsername, String smartProxyPassword) throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException
    {
        this.baseUrl = normalizeUrl(baseUrl);
        this.baseDomain = extractDomain(this.baseUrl);
        this.maxDepth = maxDepth;
        this.visitedUrls = Collections.synchronizedSet(new HashSet<>());
        this.pageContents = new ConcurrentLinkedQueue<>();
        this.executorService = Executors.newFixedThreadPool(MAX_THREADS);
        this.activeThreads = new AtomicInteger(0);
        this.robotsTxtParser = new RobotsTxtParser(this.baseUrl);
        this.proxyRotator = new SmartProxyRotator(smartProxyUsername, smartProxyPassword);
        this.httpClient = createHttpClient();
    }

    private CloseableHttpClient createHttpClient() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException
    {
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(
                new AuthScope(proxyRotator.PROXY_HOST, proxyRotator.PROXY_PORT),
                new UsernamePasswordCredentials(proxyRotator.getUsername(), proxyRotator.getPassword())
        );

        // Create a custom SSL context
        SSLContext sslContext = new SSLContextBuilder()
                .loadTrustMaterial(null, (chain, authType) -> true)
                .build();

        // Define the cipher suites to use (matching common browser configurations)
        String[] browserCipherSuites = {
                "TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256",

                "TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384",

                "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256",

                "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384",

                "TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256",

                "TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256",

        };

        // Create a custom SSLConnectionSocketFactory
        SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(
                sslContext,
                new String[]{"TLSv1.2", "TLSv1.3"}, // Protocols
                browserCipherSuites,
                NoopHostnameVerifier.INSTANCE
        );

        // Build the HttpClient with the custom SSL configuration
        return HttpClients.custom()
                .setDefaultCredentialsProvider(credentialsProvider)
                .setSSLSocketFactory(sslConnectionSocketFactory)
                .build();
    }

    private String normalizeUrl(String url) {
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            return "https://" + url;
        }
        return url;
    }

    private String extractDomain(String url) {
        try {
            URI uri = new URI(url);
            String domain = uri.getHost();
            return domain.startsWith("www.") ? domain.substring(4) : domain;
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid URL: " + url, e);
        }
    }

    private boolean isPartOfDomain(String url) {
        try {
            URI uri = new URI(url);
            String host = uri.getHost();
            return host != null && (host.equals(baseDomain) || host.endsWith("." + baseDomain));
        } catch (URISyntaxException e) {
            return false;
        }
    }

    public List<PageContent> crawl() {

        System.out.println("Starting crawl process");
        crawlAsync(baseUrl, 0);

        System.out.println("Waiting for threads to complete");
        while (activeThreads.get() > 0) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        System.out.println("All threads completed");
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }

        System.out.println("Crawl process finished. Pages crawled: " + pageContents.size());
        return new ArrayList<>(pageContents);
    }

    private long calculateRandomDelay() {
        // Random delay between 1000 ms (1 second) and 5000 ms (5 seconds)
        return ThreadLocalRandom.current().nextLong(500, 6000);
    }

    private void crawlAsync(String url, int depth) {

        if (depth > maxDepth || !visitedUrls.add(url) || !robotsTxtParser.isAllowed(url)) {
            return;
        }

        activeThreads.incrementAndGet();

        executorService.submit(() -> {
            try {
                crawlWithRetry(url, depth);
            } finally {
                activeThreads.decrementAndGet();
            }
        });
    }

    private void crawlWithRetry(String url, int depth) {
        for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
            try {
                long delay = calculateRandomDelay(); // Call the random delay method
                Thread.sleep(delay); // Sleep for the random delay

                HttpHost proxy = proxyRotator.getHttpHost();
                HttpGet request = new HttpGet(url);
                request.setConfig(RequestConfig.custom()
                        .setProxy(proxy)
                        .setConnectTimeout(CONNECTION_TIMEOUT)
                        .setSocketTimeout(READ_TIMEOUT)
                        .build());

                // Set Accept-Encoding header
                request.setHeader("Accept-Encoding", "gzip, deflate, br");
                setRandomizedFingerprint(request);

                try (CloseableHttpResponse response = httpClient.execute(request)) {
                    int statusCode = response.getStatusLine().getStatusCode();
                    String ipUsed = checkIp(proxy);

                    System.out.printf("URL: %s, Status: %d, IP: %s%n", url, statusCode, ipUsed);

                    if (statusCode == 403) {
                        currentDelay = Math.min(currentDelay * (int) BACKOFF_FACTOR, MAX_DELAY);
                        continue;
                    }

                    if (statusCode != 200) {
                        continue;
                    }

                    currentDelay = INITIAL_DELAY;

                    // Print out headers for 200 responses
                    System.out.println("Response Headers:");
                    for (Header header : response.getAllHeaders()) {
                        System.out.printf("%s: %s%n", header.getName(), header.getValue());
                    }

                    // Handle response based on content encoding
                    String encoding = response.getFirstHeader("Content-Encoding") != null ?
                            response.getFirstHeader("Content-Encoding").getValue() : "";

                    String html;
                    if ("gzip".equalsIgnoreCase(encoding)) {
                        // Handle Gzip encoding
                        html = EntityUtils.toString(new GzipDecompressingEntity(response.getEntity()));
                    } else if ("deflate".equalsIgnoreCase(encoding)) {
                        // Handle Deflate encoding
                        html = EntityUtils.toString(new DeflateDecompressingEntity(response.getEntity()));
                    } else if ("br".equalsIgnoreCase(encoding)) {
                        // Handle Brotli encoding
                        html = EntityUtils.toString(new BrotliDecompressingEntity(response.getEntity()));
                    } else {
                        // Handle uncompressed response
                        html = EntityUtils.toString(response.getEntity());
                    }

                    Document doc = Jsoup.parse(html, url);
                    processDocument(doc, url, depth);

                    return; // Successful crawl
                }

            } catch (IOException | InterruptedException e) {

                try {
                    Thread.sleep(RETRY_DELAY);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    private void setRandomizedFingerprint(HttpGet request) {
        // Initialize an ordered map to maintain header order
        LinkedHashMap<String, String> headers = new LinkedHashMap<>();

        // Set headers to mimic Chrome's request structure in the correct order
        headers.put("User-Agent", getRandomUserAgent()); // Randomized User-Agent
        headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        headers.put("Accept-Language", "en-US,en;q=0.5"); // English only
        headers.put("Accept-Encoding", "gzip, deflate, br");
        headers.put("Referer", baseUrl); // Referer header
        headers.put("Upgrade-Insecure-Requests", "1");
        headers.put("Cache-Control", "max-age=0");
        headers.put("Pragma", "no-cache");

        // Chrome-specific security-related headers
        headers.put("Sec-Fetch-Dest", "document");
        headers.put("Sec-Fetch-Mode", "navigate");
        headers.put("Sec-Fetch-Site", "same-origin");
        headers.put("Sec-Fetch-User", "?1");

        // Optional Do Not Track header
        if (ThreadLocalRandom.current().nextBoolean()) {
            headers.put("DNT", "1"); // or null to omit the header
        }

        // Set headers on the HttpGet request object
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            if (entry.getValue() != null) {
                request.setHeader(entry.getKey(), entry.getValue());
            }
        }
    }
    private String getRandomAcceptLanguage() {
        String[] languages = {"en-US,en;q=0.9", "en-GB,en;q=0.8", "fr-FR,fr;q=0.9", "de-DE,de;q=0.8", "es-ES,es;q=0.9"};
        return languages[ThreadLocalRandom.current().nextInt(languages.length)];
    }

    private void processDocument(Document doc, String url, int depth) {
        System.out.println("Processing document.");
        System.out.println("URL: " + url);
        System.out.println("Depth " + depth);
        String title = doc.title();
        String body = doc.body().text();
        pageContents.offer(new PageContent(title, body, url));

        Elements links = doc.select("a[href]");
        for (Element link : links) {
            String nextUrl = link.absUrl("href");

            // Check if the link is visible
            if (isLinkVisible(link)) {
                if (isPartOfDomain(nextUrl)) {
                    crawlAsync(nextUrl, depth + 1);
                }
            }
        }
    }

    private boolean isLinkVisible(Element link) {
        // Check for display:none or visibility:hidden in the style attribute
        String style = link.attr("style");
        if (style.contains("display: none") || style.contains("visibility: hidden")) {
            return false; // Link is hidden
        }

        // Retrieve the color from the style attribute
        String color = link.attr("color"); // Attempt to get the color directly from the link
        if (color.isEmpty()) {
            return true;
        }

        if (color.equals("rgb(255, 255, 255)") || color.equals("#fff") || color.equals("#ffffff")) {
            return false; // Link is effectively invisible
        }

        return true; // Link is visible
    }

    private String checkIp(HttpHost proxy) throws IOException {
        HttpGet ipRequest = new HttpGet("https://ip.smartproxy.com/ip");
        ipRequest.setConfig(RequestConfig.custom().setProxy(proxy).build());

        try (CloseableHttpResponse ipResponse = httpClient.execute(ipRequest)) {
            return EntityUtils.toString(ipResponse.getEntity()).trim();
        }
    }

    private String getRandomUserAgent() {
        return USER_AGENTS[ThreadLocalRandom.current().nextInt(USER_AGENTS.length)];
    }

    public static class PageContent {
        private final String title;
        private final String body;
        private final String url;

        public PageContent(String title, String body, String url) {
            this.title = title;
            this.body = body;
            this.url = url;
        }

        public String getTitle() {
            return title;
        }

        public String getBody() {
            return body;
        }

        public String getUrl() {
            return url;
        }
    }

}

