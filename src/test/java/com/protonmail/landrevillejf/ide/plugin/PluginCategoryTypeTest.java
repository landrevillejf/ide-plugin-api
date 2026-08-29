package com.protonmail.landrevillejf.ide.plugin;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PluginCategoryTypeTest {

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

    // ==================== VALUE_OF TESTS ====================

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
