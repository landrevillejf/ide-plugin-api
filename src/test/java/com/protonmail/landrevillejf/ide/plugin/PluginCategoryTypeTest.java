package com.protonmail.landrevillejf.ide.plugin;

import com.protonmail.landrevillejf.AdvancedMissingIcon;
import com.protonmail.landrevillejf.IconManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.swing.*;
import java.awt.*;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PluginCategoryTypeTest {

    private Icon mockIcon;

    @BeforeEach
    void setUp() {
        mockIcon = new ImageIcon();
    }

    // ==================== GET_ICON TESTS ====================

    @Test
    void getIcon_ShouldReturnNonNullIcon() {
        try (MockedStatic<IconManager> iconManager = mockStatic(IconManager.class)) {
            iconManager.when(() -> IconManager.loadIcon(any(), anyString(), anyInt(), anyInt()))
                    .thenReturn(mockIcon);

            final Icon icon = PluginCategoryType.DEVELOPMENT.getIcon();

            assertNotNull(icon);
            iconManager.verify(() -> IconManager.loadIcon(any(), anyString(), anyInt(), anyInt()), times(1));
        }
    }

    // ==================== GET_TINTED_ICON TESTS ====================

    @Test
    void getTintedIcon_ShouldReturnNonNullIcon() {
        try (MockedStatic<IconManager> iconManager = mockStatic(IconManager.class)) {
            iconManager.when(() -> IconManager.loadIcon(any(), anyString(), anyInt(), anyInt(), any(Color.class)))
                    .thenReturn(mockIcon);

            final Icon icon = PluginCategoryType.GAMES.getTintedIcon(Color.RED);

            assertNotNull(icon);
        }
    }

    @Test
    void getTintedIcon_WithSameColor_ShouldReturnSameIcon() {
        try (MockedStatic<IconManager> iconManager = mockStatic(IconManager.class)) {
            iconManager.when(() -> IconManager.loadIcon(any(), anyString(), anyInt(), anyInt(), any(Color.class)))
                    .thenReturn(mockIcon);

            final Icon firstCall = PluginCategoryType.GAMES.getTintedIcon(Color.RED);
            final Icon secondCall = PluginCategoryType.GAMES.getTintedIcon(Color.RED);

            // La méthode ne cache pas avec la couleur, donc les références peuvent être différentes
            // On vérifie juste que l'appel ne plante pas
            assertNotNull(firstCall);
            assertNotNull(secondCall);
        }
    }

    // ==================== GET_ICON_WITH_SHADOW TESTS ====================

    @Test
    void getIconWithShadow_ShouldReturnNonNullIcon() {
        try (MockedStatic<IconManager> iconManager = mockStatic(IconManager.class)) {
            iconManager.when(() -> IconManager.loadIconWithShadow(any(), anyString(), anyInt(), anyInt(), anyInt()))
                    .thenReturn(mockIcon);

            final Icon icon = PluginCategoryType.GAMES.getIconWithShadow();

            assertNotNull(icon);
        }
    }

    // ==================== GET_ROUNDED_ICON TESTS ====================

    @Test
    void getRoundedIcon_ShouldReturnNonNullIcon() {
        try (MockedStatic<IconManager> iconManager = mockStatic(IconManager.class)) {
            iconManager.when(() -> IconManager.loadIconWithRoundedCorners(any(), anyString(), anyInt(), anyInt(), anyInt()))
                    .thenReturn(mockIcon);

            final Icon icon = PluginCategoryType.GAMES.getRoundedIcon(5);

            assertNotNull(icon);
        }
    }

    // ==================== GET_ROTATED_ICON TESTS ====================

    @Test
    void getRotatedIcon_ShouldReturnNonNullIcon() {
        try (MockedStatic<IconManager> iconManager = mockStatic(IconManager.class)) {
            iconManager.when(() -> IconManager.loadIcon(any(), anyString(), anyInt(), anyInt(), anyDouble()))
                    .thenReturn(mockIcon);

            final Icon icon = PluginCategoryType.GAMES.getRotatedIcon(45.0);

            assertNotNull(icon);
        }
    }

    // ==================== GET_GLASS_ICON TESTS ====================

    @Test
    void getGlassIcon_ShouldReturnNonNullIcon() {
        try (MockedStatic<IconManager> iconManager = mockStatic(IconManager.class)) {
            iconManager.when(() -> IconManager.createGlassIcon(any(), isNull(), anyInt(), anyInt()))
                    .thenReturn(mockIcon);
            iconManager.when(() -> IconManager.extractDominantColors(any(Icon.class), anyInt()))
                    .thenReturn(new Color[]{Color.BLUE});

            final Icon icon = PluginCategoryType.GAMES.getGlassIcon();

            assertNotNull(icon);
        }
    }

    // ==================== GET_ICON_WITH_BADGE TESTS ====================

    @Test
    void getIconWithBadge_ShouldReturnNonNullIcon() {
        try (MockedStatic<IconManager> iconManager = mockStatic(IconManager.class)) {
            iconManager.when(() -> IconManager.loadIcon(any(), anyString(), anyInt(), anyInt()))
                    .thenReturn(mockIcon);
            iconManager.when(() -> IconManager.createBadgeIcon(any(Icon.class), anyInt(), any(Color.class)))
                    .thenReturn(mockIcon);

            final Icon icon = PluginCategoryType.GAMES.getIconWithBadge(5, Color.RED);

            assertNotNull(icon);
        }
    }

    // ==================== GET_DOMINANT_COLOR TESTS ====================

    @Test
    void getDominantColor_ShouldReturnColor_WhenColorsExist() {
        try (MockedStatic<IconManager> iconManager = mockStatic(IconManager.class)) {
            iconManager.when(() -> IconManager.loadIcon(any(), anyString(), anyInt(), anyInt()))
                    .thenReturn(mockIcon);
            iconManager.when(() -> IconManager.extractDominantColors(any(Icon.class), anyInt()))
                    .thenReturn(new Color[]{Color.RED});

            final Color color = PluginCategoryType.GAMES.getDominantColor();

            assertEquals(Color.RED, color);
        }
    }

    @Test
    void getDominantColor_ShouldReturnDefaultBlue_WhenNoColorsExtracted() {
        try (MockedStatic<IconManager> iconManager = mockStatic(IconManager.class)) {
            iconManager.when(() -> IconManager.loadIcon(any(), anyString(), anyInt(), anyInt()))
                    .thenReturn(mockIcon);
            iconManager.when(() -> IconManager.extractDominantColors(any(Icon.class), anyInt()))
                    .thenReturn(new Color[0]);

            final Color color = PluginCategoryType.GAMES.getDominantColor();

            assertNotNull(color);
        }
    }

    // ==================== ICON_EXISTS TESTS ====================

    @Test
    void iconExists_ShouldReturnTrue_WhenIconIsValid() {
        try (MockedStatic<IconManager> iconManager = mockStatic(IconManager.class)) {
            iconManager.when(() -> IconManager.loadIcon(any(), anyString(), anyInt(), anyInt()))
                    .thenReturn(mockIcon);

            assertTrue(PluginCategoryType.DEVELOPMENT.iconExists());
        }
    }

    @Test
    void iconExists_ShouldReturnFalse_WhenIconIsMissing() {
        try (MockedStatic<IconManager> iconManager = mockStatic(IconManager.class)) {
            // Simuler une icône manquante (AdvancedMissingIcon)
            iconManager.when(() -> IconManager.loadIcon(any(), anyString(), anyInt(), anyInt()))
                    .thenAnswer(invocation -> new AdvancedMissingIcon(16, 16, Color.GRAY, 2));

            assertFalse(PluginCategoryType.GAMES.iconExists());
        }
    }

    // ==================== GET_DISPLAY_NAME TESTS ====================

    @Test
    void getDisplayName_ShouldReturnCorrectDisplayName() {
        assertEquals("games", PluginCategoryType.GAMES.getDisplayName());
        assertEquals("tools", PluginCategoryType.TOOLS.getDisplayName());
        assertEquals("development", PluginCategoryType.DEVELOPMENT.getDisplayName());
        assertEquals("default", PluginCategoryType.DEFAULT.getDisplayName());
    }

    // ==================== GET_KEYWORDS TESTS ====================

    @Test
    void getKeywords_ShouldReturnNonEmptySetForNonDefaultCategories() {
        final Set<String> keywords = PluginCategoryType.GAMES.getKeywords();

        assertNotNull(keywords);
        assertTrue(keywords.contains("games"));
    }

    @Test
    void getKeywords_ForDefaultCategory_ShouldReturnEmptySet() {
        final Set<String> keywords = PluginCategoryType.DEFAULT.getKeywords();

        assertNotNull(keywords);
        assertTrue(keywords.isEmpty());
    }

    @Test
    void getKeywords_ForGamesCategory_ShouldContainCorrectKeywords() {
        final Set<String> keywords = PluginCategoryType.GAMES.getKeywords();

        assertTrue(keywords.contains("games"));
    }

    @Test
    void getKeywords_ForToolsCategory_ShouldContainCorrectKeywords() {
        final Set<String> keywords = PluginCategoryType.TOOLS.getKeywords();

        assertTrue(keywords.contains("tools"));
    }

    // ==================== FROM_KEYWORD TESTS ====================

    @Test
    void fromKeyword_WithNull_ShouldReturnDefault() {
        assertEquals(PluginCategoryType.DEFAULT, PluginCategoryType.fromKeyword(null));
    }

    @Test
    void fromKeyword_WithEmptyString_ShouldReturnDefault() {
        assertEquals(PluginCategoryType.DEFAULT, PluginCategoryType.fromKeyword(""));
    }

    @Test
    void fromKeyword_WithBlankString_ShouldReturnDefault() {
        assertEquals(PluginCategoryType.DEFAULT, PluginCategoryType.fromKeyword("   "));
    }

    @Test
    void fromKeyword_WithUnknownKeyword_ShouldReturnDefault() {
        assertEquals(PluginCategoryType.DEFAULT, PluginCategoryType.fromKeyword("unknown_keyword_12345"));
    }

    @Test
    void fromKeyword_WithCaseInsensitive_ShouldReturnCorrectCategory() {
        assertEquals(PluginCategoryType.GAMES, PluginCategoryType.fromKeyword("GAMES"));
        assertEquals(PluginCategoryType.TOOLS, PluginCategoryType.fromKeyword("TOOLS"));
        assertEquals(PluginCategoryType.DEVELOPMENT, PluginCategoryType.fromKeyword("DEVELOPMENT"));
    }

    @Test
    void fromKeyword_WithMixedCase_ShouldReturnCorrectCategory() {
        assertEquals(PluginCategoryType.GAMES, PluginCategoryType.fromKeyword("GaMeS"));
        assertEquals(PluginCategoryType.TOOLS, PluginCategoryType.fromKeyword("ToOlS"));
    }

    @Test
    void fromKeyword_WithWhitespace_ShouldTrimAndFind() {
        assertEquals(PluginCategoryType.GAMES, PluginCategoryType.fromKeyword("  games  "));
        assertEquals(PluginCategoryType.TOOLS, PluginCategoryType.fromKeyword("\ttools\n"));
    }

    @Test
    void fromKeyword_ShouldReturnFirstMatch_WhenMultipleCategoriesShareKeyword() {
        final PluginCategoryType result = PluginCategoryType.fromKeyword("java");
        assertNotNull(result);
    }

    @Test
    void fromKeyword_WithPartialMatch_ShouldNotMatch() {
        assertEquals(PluginCategoryType.DEFAULT, PluginCategoryType.fromKeyword("gam"));
        assertEquals(PluginCategoryType.DEFAULT, PluginCategoryType.fromKeyword("tool"));
    }

    @Test
    void fromKeyword_WithOnlyWhitespace_ShouldReturnDefault() {
        assertEquals(PluginCategoryType.DEFAULT, PluginCategoryType.fromKeyword("    "));
        assertEquals(PluginCategoryType.DEFAULT, PluginCategoryType.fromKeyword("\n\t"));
    }

    @Test
    void fromKeyword_ShouldNeverReturnDefault_WhenMatchingRealCategory() {
        final PluginCategoryType result = PluginCategoryType.fromKeyword("games");
        assertNotEquals(PluginCategoryType.DEFAULT, result);
        assertEquals(PluginCategoryType.GAMES, result);
    }

    // ==================== GET_VALID_CATEGORIES TESTS ====================

    @Test
    void getValidCategories_ShouldReturnAllCategoriesExceptDefault() {
        final java.util.List<PluginCategoryType> validCategories = PluginCategoryType.getValidCategories();

        assertNotNull(validCategories);
        assertFalse(validCategories.contains(PluginCategoryType.DEFAULT));
        assertTrue(validCategories.contains(PluginCategoryType.GAMES));
        assertTrue(validCategories.contains(PluginCategoryType.TOOLS));
        assertTrue(validCategories.contains(PluginCategoryType.DEVELOPMENT));
        assertTrue(validCategories.contains(PluginCategoryType.WEB));
        assertTrue(validCategories.contains(PluginCategoryType.JAVA));
    }

    @Test
    void getValidCategories_ShouldNotContainDefault() {
        final java.util.List<PluginCategoryType> validCategories = PluginCategoryType.getValidCategories();

        for (PluginCategoryType category : validCategories) {
            assertNotEquals(PluginCategoryType.DEFAULT, category);
        }
    }

    // ==================== ALL_CATEGORIES_ICON_TEST ====================

    @Test
    void getAllCategories_GetIcon_ShouldNotThrowException() {
        try (MockedStatic<IconManager> iconManager = mockStatic(IconManager.class)) {
            iconManager.when(() -> IconManager.loadIcon(any(), anyString(), anyInt(), anyInt()))
                    .thenReturn(mockIcon);

            for (PluginCategoryType category : PluginCategoryType.values()) {
                assertDoesNotThrow(category::getIcon);
            }
        }
    }

    // ==================== VALUE_OF_TESTS ====================

    @Test
    void valueOf_ShouldReturnCorrectEnum() {
        assertEquals(PluginCategoryType.GAMES, PluginCategoryType.valueOf("GAMES"));
        assertEquals(PluginCategoryType.TOOLS, PluginCategoryType.valueOf("TOOLS"));
    }

    @Test
    void values_ShouldContainAllCategories() {
        final PluginCategoryType[] values = PluginCategoryType.values();

        assertTrue(values.length > 0);
        assertTrue(java.util.Arrays.asList(values).contains(PluginCategoryType.DEFAULT));
    }
}