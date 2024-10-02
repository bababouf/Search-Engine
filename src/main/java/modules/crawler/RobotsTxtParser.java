package modules.crawler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class RobotsTxtParser {
    private final String baseUrl;
    private final List<String> disallowedPaths;

    public RobotsTxtParser(String baseUrl) {
        this.baseUrl = baseUrl;
        this.disallowedPaths = new ArrayList<>();
        parse();
    }

    private void parse() {
        try {
            String robotsTxtUrl = baseUrl + "/robots.txt";
            Document doc = Jsoup.connect(robotsTxtUrl).get();
            String[] lines = doc.body().text().split("\\r?\\n");

            boolean relevantUserAgent = false;
            for (String line : lines) {
                line = line.trim().toLowerCase();
                if (line.startsWith("user-agent:")) {
                    relevantUserAgent = line.contains("*") || line.contains("googlebot");
                } else if (relevantUserAgent && line.startsWith("disallow:")) {
                    String path = line.substring("disallow:".length()).trim();
                    if (!path.isEmpty()) {
                        disallowedPaths.add(path);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error parsing robots.txt: " + e.getMessage());
        }
    }

    public boolean isAllowed(String url) {
        for (String disallowedPath : disallowedPaths) {
            if (url.startsWith(baseUrl + disallowedPath)) {
                return false;
            }
        }
        return true;
    }
}