package com.protonmail.landrevillejf.ide.plugin;

import lombok.Getter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Enumeration of category types with their associated keywords.
 *
 * @author landrevillejf
 * @version 1.2.0
 */
@Getter
public enum PluginCategoryType {
    // Existing categories
    GAMES("games", "games"),
    TOOLS("tools", "tools"),
    DEVELOPMENT("development", "development"),
    ANALYTICS("analytics", "analytics"),
    VERSION_CONTROL("version control", "git", "version control"),
    CONTAINER("container", "docker", "container"),
    DATABASE("database", "database"),

    // Additional categories
    WEB("web", "web", "frontend", "javascript", "typescript", "html", "css", "react", "vue", "angular"),
    JAVA("java", "java", "spring", "maven", "gradle", "jvm", "kotlin", "scala"),
    PYTHON("python", "python", "django", "flask", "pandas", "numpy", "jupyter"),
    MOBILE("mobile", "mobile", "android", "ios", "swift", "kotlin", "flutter", "react-native"),
    CLOUD("cloud", "cloud", "aws", "azure", "gcp", "heroku", "serverless"),
    TESTING("testing", "testing", "qa", "junit", "selenium", "test", "mockito", "cucumber"),
    SECURITY("security", "security", "auth", "authentication", "oauth", "jwt", "ssl"),
    DOCUMENTATION("documentation", "documentation", "wiki", "docs", "markdown", "asciidoc"),
    DESIGN("design", "design", "ui", "ux", "figma", "sketch", "adobe"),
    CONFIGURATION("configuration", "configuration", "config", "settings", "properties", "yml"),
    MONITORING("monitoring", "monitoring", "logs", "logging", "metrics", "grafana", "prometheus"),
    API("api", "api", "rest", "graphql", "webservice", "soap", "endpoint"),
    AI("ai", "ai", "machine-learning", "ml", "deep-learning", "tensorflow", "pytorch"),
    DATA("data", "data", "bigdata", "etl", "data-science", "hadoop", "spark"),
    MESSAGING("messaging", "messaging", "queue", "kafka", "rabbitmq", "jms", "activemq"),
    AUTOMATION("automation", "automation", "ci/cd", "jenkins", "pipeline", "github-actions", "gitlab-ci"),
    VIRTUALIZATION("virtualization", "virtualization", "vm", "vagrant", "virtualbox", "hypervisor"),
    NETWORKING("networking", "networking", "network", "tcp/ip", "dns", "load-balancer"),

    // Default category
    DEFAULT("default");

    private final String displayName;
    private final Set<String> keywords;

    PluginCategoryType(String displayName, String... keywords) {
        this.displayName = displayName;
        this.keywords = new HashSet<>(Arrays.asList(keywords));
    }

    /**
     * Finds the category matching a keyword.
     *
     * @param keyword the keyword to search for
     * @return the matching PluginCategoryType, or DEFAULT if not found
     */
    public static PluginCategoryType fromKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return DEFAULT;
        }

        String lowerKeyword = keyword.toLowerCase().trim();

        return Arrays.stream(values())
                .filter(category -> category != DEFAULT)
                .filter(category -> category.getKeywords().contains(lowerKeyword))
                .findFirst()
                .orElse(DEFAULT);
    }

    /**
     * Returns all categories except DEFAULT.
     *
     * @return list of valid categories
     */
    public static List<PluginCategoryType> getValidCategories() {
        return Arrays.stream(values())
                .filter(category -> category != DEFAULT)
                .collect(Collectors.toList());
    }
}
