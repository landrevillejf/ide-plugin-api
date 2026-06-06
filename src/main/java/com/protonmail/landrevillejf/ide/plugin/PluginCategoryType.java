package com.protonmail.landrevillejf.ide.plugin;

import com.protonmail.landrevillejf.AdvancedMissingIcon;
import com.protonmail.landrevillejf.IconColor;
import com.protonmail.landrevillejf.IconManager;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Enumération des types de catégories avec leurs mots-clés associés et leurs icônes correspondantes
 */
public enum PluginCategoryType {
    // Catégories existantes
    GAMES("games", IconManager.IconCategory.MEDIA, "Movie", "games"),
    TOOLS("tools", IconManager.IconCategory.DEVELOPMENT, "Application", "tools"),
    DEVELOPMENT("development", IconManager.IconCategory.DEVELOPMENT, "EnterpriseJavaBean", "development"),
    ANALYTICS("analytics", IconManager.IconCategory.DEVELOPMENT, "ApplicationDeploy", "analytics"),
    VERSION_CONTROL("version control", IconManager.IconCategory.DEVELOPMENT, "Server", "git", "version control"),
    CONTAINER("container", IconManager.IconCategory.DEVELOPMENT, "Host", "docker", "container"),
    DATABASE("database", IconManager.IconCategory.DEVELOPMENT, "Database", "database"),

    // Nouvelles catégories
    WEB("web", IconManager.IconCategory.DEVELOPMENT, "Web", "web", "frontend", "javascript", "typescript", "html", "css", "react", "vue", "angular"),
    JAVA("java", IconManager.IconCategory.DEVELOPMENT, "Java", "java", "spring", "maven", "gradle", "jvm", "kotlin", "scala"),
    PYTHON("python", IconManager.IconCategory.DEVELOPMENT, "Python", "python", "django", "flask", "pandas", "numpy", "jupyter"),
    MOBILE("mobile", IconManager.IconCategory.DEVELOPMENT, "Mobile", "mobile", "android", "ios", "swift", "kotlin", "flutter", "react-native"),
    CLOUD("cloud", IconManager.IconCategory.DEVELOPMENT, "Cloud", "cloud", "aws", "azure", "gcp", "heroku", "serverless"),
    TESTING("testing", IconManager.IconCategory.DEVELOPMENT, "Test", "testing", "qa", "junit", "selenium", "test", "mockito", "cucumber"),
    SECURITY("security", IconManager.IconCategory.DEVELOPMENT, "Security", "security", "auth", "authentication", "oauth", "jwt", "ssl"),
    DOCUMENTATION("documentation", IconManager.IconCategory.GENERAL, "Document", "documentation", "wiki", "docs", "markdown", "asciidoc"),
    DESIGN("design", IconManager.IconCategory.GENERAL, "Design", "design", "ui", "ux", "figma", "sketch", "adobe"),
    CONFIGURATION("configuration", IconManager.IconCategory.GENERAL, "Settings", "configuration", "config", "settings", "properties", "yml"),
    MONITORING("monitoring", IconManager.IconCategory.DEVELOPMENT, "Console", "monitoring", "logs", "logging", "metrics", "grafana", "prometheus"),
    API("api", IconManager.IconCategory.DEVELOPMENT, "Api", "api", "rest", "graphql", "webservice", "soap", "endpoint"),
    AI("ai", IconManager.IconCategory.DEVELOPMENT, "AI", "ai", "machine-learning", "ml", "deep-learning", "tensorflow", "pytorch"),
    DATA("data", IconManager.IconCategory.DEVELOPMENT, "Data", "data", "bigdata", "etl", "data-science", "hadoop", "spark"),
    MESSAGING("messaging", IconManager.IconCategory.DEVELOPMENT, "Message", "messaging", "queue", "kafka", "rabbitmq", "jms", "activemq"),
    AUTOMATION("automation", IconManager.IconCategory.DEVELOPMENT, "Build", "automation", "ci/cd", "jenkins", "pipeline", "github-actions", "gitlab-ci"),
    VIRTUALIZATION("virtualization", IconManager.IconCategory.DEVELOPMENT, "VirtualMachine", "virtualization", "vm", "vagrant", "virtualbox", "hypervisor"),
    NETWORKING("networking", IconManager.IconCategory.DEVELOPMENT, "Network", "networking", "network", "tcp/ip", "dns", "load-balancer"),

    // Catégorie par défaut
    DEFAULT("default", IconManager.IconCategory.GENERAL, "About");

    private final String displayName;
    private final IconManager.IconCategory iconCategory;
    private final String iconName;
    private final Set<String> keywords;
    private final int iconWidth = 16;
    private final int iconHeight = 16;

    // Cache pour les icônes transformées
    private Icon cachedIcon;
    private Icon cachedTintedIcon;
    private Icon cachedShadowIcon;
    private Icon cachedRoundedIcon;

    PluginCategoryType(String displayName, IconManager.IconCategory iconCategory, String iconName, String... keywords) {
        this.displayName = displayName;
        this.iconCategory = iconCategory;
        this.iconName = iconName;
        this.keywords = new HashSet<>(Arrays.asList(keywords));
    }

    /**
     * Récupère l'icône de base de la catégorie
     */
    public Icon getIcon() {
        if (cachedIcon == null) {
            cachedIcon = IconManager.loadIcon(iconCategory, iconName, iconWidth, iconHeight);
        }
        return cachedIcon;
    }

    /**
     * Récupère l'icône avec une teinte spécifique
     */
    public Icon getTintedIcon(Color color) {
        String cacheKey = iconName + "_tint_" + color.getRGB();
        if (cachedTintedIcon == null) {
            cachedTintedIcon = IconManager.loadIcon(iconCategory, iconName, iconWidth, iconHeight, color);
        }
        return cachedTintedIcon;
    }

    /**
     * Récupère l'icône avec une ombre
     */
    public Icon getIconWithShadow() {
        if (cachedShadowIcon == null) {
            cachedShadowIcon = IconManager.loadIconWithShadow(iconCategory, iconName, iconWidth, iconHeight, 3);
        }
        return cachedShadowIcon;
    }

    /**
     * Récupère l'icône avec des coins arrondis
     */
    public Icon getRoundedIcon(int cornerRadius) {
        if (cachedRoundedIcon == null) {
            cachedRoundedIcon = IconManager.loadIconWithRoundedCorners(iconCategory, iconName, iconWidth, iconHeight, cornerRadius);
        }
        return cachedRoundedIcon;
    }

    /**
     * Récupère l'icône pivotée
     */
    public Icon getRotatedIcon(double degrees) {
        return IconManager.loadIcon(iconCategory, iconName, iconWidth, iconHeight, degrees);
    }

    /**
     * Récupère l'icône avec effet de verre
     */
    public Icon getGlassIcon() {
        return IconManager.createGlassIcon(getDominantColor(), null, iconWidth, 5);
    }

    /**
     * Récupère l'icône avec un badge (nombre de notifications)
     */
    public Icon getIconWithBadge(int count, Color badgeColor) {
        Icon baseIcon = getIcon();
        return IconManager.createBadgeIcon(baseIcon, count, badgeColor);
    }

    /**
     * Récupère la couleur dominante de l'icône
     */
    public Color getDominantColor() {
        Icon icon = getIcon();
        Color[] colors = IconManager.extractDominantColors(icon, 1);
        return colors.length > 0 ? colors[0] : IconColor.BLUE.getColor();
    }

    /**
     * Vérifie si l'icône existe
     */
    public boolean iconExists() {
        return !(getIcon() instanceof AdvancedMissingIcon);
    }

    public String getDisplayName() {
        return displayName;
    }

    public Set<String> getKeywords() {
        return keywords;
    }

    /**
     * Recherche la catégorie correspondant à un mot-clé
     * @param keyword le mot-clé à rechercher
     * @return le CategoryType correspondant ou DEFAULT si non trouvé
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
     * Récupère toutes les catégories sauf DEFAULT
     */
    public static List<PluginCategoryType> getValidCategories() {
        return Arrays.stream(values())
                .filter(category -> category != DEFAULT)
                .collect(Collectors.toList());
    }
}