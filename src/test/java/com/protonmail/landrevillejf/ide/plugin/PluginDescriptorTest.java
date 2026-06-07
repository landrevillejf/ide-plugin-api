package com.protonmail.landrevillejf.ide.plugin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class PluginDescriptorTest {

    private PluginDescriptor descriptor;
    private PluginDescriptor fullDescriptor;

    @BeforeEach
    void setUp() {
        descriptor = new PluginDescriptor();

        fullDescriptor = new PluginDescriptor(
                "test-id",
                "Test Plugin",
                "2.0.0",
                "com.test.MainClass",
                "This is a test plugin description",
                "Test Author"
        );
        fullDescriptor.setAuthorEmail("author@test.com");
        fullDescriptor.setCategory("Testing");
        fullDescriptor.setRequiredHostVersion("1.5.0");
        fullDescriptor.setSpecificationTitle("Test Spec");
        fullDescriptor.setSpecificationVersion("1.0");
        fullDescriptor.setSpecificationVendor("Test Vendor");
        fullDescriptor.setImplementationVersion("2.0.0");
        fullDescriptor.setEnabledByDefault(true);
        fullDescriptor.setAutoStart(true);
        fullDescriptor.setProvidesMenu(true);
        fullDescriptor.setProvidesToolbar(true);
        fullDescriptor.setProvidesServices(true);
        fullDescriptor.setRequiresNetwork(true);
    }

    // ==================== CONSTRUCTOR TESTS ====================

    @Test
    void noArgsConstructor_ShouldCreateEmptyDescriptor() {
        final PluginDescriptor emptyDescriptor = new PluginDescriptor();

        assertNotNull(emptyDescriptor);
        assertNull(emptyDescriptor.getId());
        assertNull(emptyDescriptor.getName());
        assertNull(emptyDescriptor.getVersion());
    }

    @Test
    void allArgsConstructor_ShouldSetAllFields() {
        final Date now = new Date();
        final PluginDescriptor allArgsDescriptor = new PluginDescriptor(
                "id", "name", "1.0", "main.Main", "desc", "author",
                "email@test.com", "Category", "1.0.0",
                "SpecTitle", "SpecVersion", "SpecVendor", "ImplVersion",
                now, now, true, true, true, true, true, true
        );

        assertEquals("id", allArgsDescriptor.getId());
        assertEquals("name", allArgsDescriptor.getName());
        assertEquals("1.0", allArgsDescriptor.getVersion());
        assertEquals("main.Main", allArgsDescriptor.getMainClass());
        assertEquals("desc", allArgsDescriptor.getDescription());
        assertEquals("author", allArgsDescriptor.getAuthor());
        assertEquals("email@test.com", allArgsDescriptor.getAuthorEmail());
        assertEquals("Category", allArgsDescriptor.getCategory());
        assertEquals("1.0.0", allArgsDescriptor.getRequiredHostVersion());
        assertEquals("SpecTitle", allArgsDescriptor.getSpecificationTitle());
        assertEquals("SpecVersion", allArgsDescriptor.getSpecificationVersion());
        assertEquals("SpecVendor", allArgsDescriptor.getSpecificationVendor());
        assertEquals("ImplVersion", allArgsDescriptor.getImplementationVersion());
        assertEquals(now, allArgsDescriptor.getCreationDate());
        assertEquals(now, allArgsDescriptor.getLastModifiedDate());
        assertTrue(allArgsDescriptor.isEnabledByDefault());
        assertTrue(allArgsDescriptor.isAutoStart());
        assertTrue(allArgsDescriptor.isProvidesMenu());
        assertTrue(allArgsDescriptor.isProvidesToolbar());
        assertTrue(allArgsDescriptor.isProvidesServices());
        assertTrue(allArgsDescriptor.isRequiresNetwork());
    }

    @Test
    void minimalArgsConstructor_ShouldCreateValidDescriptor() {
        assertNotNull(fullDescriptor);
        assertEquals("test-id", fullDescriptor.getId());
        assertEquals("Test Plugin", fullDescriptor.getName());
        assertEquals("2.0.0", fullDescriptor.getVersion());
        assertEquals("com.test.MainClass", fullDescriptor.getMainClass());
        assertEquals("This is a test plugin description", fullDescriptor.getDescription());
        assertEquals("Test Author", fullDescriptor.getAuthor());
        assertNotNull(fullDescriptor.getCreationDate());
        assertNotNull(fullDescriptor.getLastModifiedDate());
    }

    // ==================== COPY TESTS ====================

    @Test
    void copy_ShouldCreateNewInstanceWithSameData() {
        final PluginDescriptor copy = fullDescriptor.copy();

        assertNotSame(fullDescriptor, copy);
        assertEquals(fullDescriptor.getId(), copy.getId());
        assertEquals(fullDescriptor.getName(), copy.getName());
        assertEquals(fullDescriptor.getVersion(), copy.getVersion());
        assertEquals(fullDescriptor.getMainClass(), copy.getMainClass());
        assertEquals(fullDescriptor.getDescription(), copy.getDescription());
        assertEquals(fullDescriptor.getAuthor(), copy.getAuthor());
        assertEquals(fullDescriptor.getAuthorEmail(), copy.getAuthorEmail());
        assertEquals(fullDescriptor.getCategory(), copy.getCategory());
        assertEquals(fullDescriptor.getRequiredHostVersion(), copy.getRequiredHostVersion());
        assertEquals(fullDescriptor.getSpecificationTitle(), copy.getSpecificationTitle());
        assertEquals(fullDescriptor.getSpecificationVersion(), copy.getSpecificationVersion());
        assertEquals(fullDescriptor.getSpecificationVendor(), copy.getSpecificationVendor());
        assertEquals(fullDescriptor.getImplementationVersion(), copy.getImplementationVersion());
        assertEquals(fullDescriptor.isEnabledByDefault(), copy.isEnabledByDefault());
        assertEquals(fullDescriptor.isAutoStart(), copy.isAutoStart());
        assertEquals(fullDescriptor.isProvidesMenu(), copy.isProvidesMenu());
        assertEquals(fullDescriptor.isProvidesToolbar(), copy.isProvidesToolbar());
        assertEquals(fullDescriptor.isProvidesServices(), copy.isProvidesServices());
        assertEquals(fullDescriptor.isRequiresNetwork(), copy.isRequiresNetwork());
    }

    @Test
    void copy_ShouldCreateNewDateInstances() {
        final PluginDescriptor copy = fullDescriptor.copy();

        assertNotSame(fullDescriptor.getCreationDate(), copy.getCreationDate());
        assertEquals(fullDescriptor.getCreationDate(), copy.getCreationDate());
    }

    @Test
    void copy_WithNullCreationDate_ShouldNotThrowException() {
        fullDescriptor.setCreationDate(null);

        assertDoesNotThrow(() -> fullDescriptor.copy());

        final PluginDescriptor copy = fullDescriptor.copy();
        assertNull(copy.getCreationDate());
    }

    // ==================== VALIDATION TESTS ====================

    @Test
    void isValid_ShouldReturnTrue_WhenAllRequiredFieldsAreSet() {
        assertTrue(fullDescriptor.isValid());
    }

    @Test
    void isValid_ShouldReturnFalse_WhenIdIsNull() {
        fullDescriptor.setId(null);

        assertFalse(fullDescriptor.isValid());
    }

    @Test
    void isValid_ShouldReturnFalse_WhenIdIsEmpty() {
        fullDescriptor.setId("");

        assertFalse(fullDescriptor.isValid());
    }

    @Test
    void isValid_ShouldReturnFalse_WhenIdIsBlank() {
        fullDescriptor.setId("   ");

        assertFalse(fullDescriptor.isValid());
    }

    @Test
    void isValid_ShouldReturnFalse_WhenNameIsNull() {
        fullDescriptor.setName(null);

        assertFalse(fullDescriptor.isValid());
    }

    @Test
    void isValid_ShouldReturnFalse_WhenNameIsEmpty() {
        fullDescriptor.setName("");

        assertFalse(fullDescriptor.isValid());
    }

    @Test
    void isValid_ShouldReturnFalse_WhenVersionIsNull() {
        fullDescriptor.setVersion(null);

        assertFalse(fullDescriptor.isValid());
    }

    @Test
    void isValid_ShouldReturnFalse_WhenVersionIsEmpty() {
        fullDescriptor.setVersion("");

        assertFalse(fullDescriptor.isValid());
    }

    @Test
    void isValid_ShouldReturnFalse_WhenMainClassIsNull() {
        fullDescriptor.setMainClass(null);

        assertFalse(fullDescriptor.isValid());
    }

    @Test
    void isValid_ShouldReturnFalse_WhenMainClassIsEmpty() {
        fullDescriptor.setMainClass("");

        assertFalse(fullDescriptor.isValid());
    }

    @Test
    void isValid_ShouldReturnFalse_WhenDescriptionIsNull() {
        fullDescriptor.setDescription(null);

        assertFalse(fullDescriptor.isValid());
    }

    @Test
    void isValid_ShouldReturnFalse_WhenDescriptionIsEmpty() {
        fullDescriptor.setDescription("");

        assertFalse(fullDescriptor.isValid());
    }

    @Test
    void isValid_ShouldReturnFalse_WhenAuthorIsNull() {
        fullDescriptor.setAuthor(null);

        assertFalse(fullDescriptor.isValid());
    }

    @Test
    void isValid_ShouldReturnFalse_WhenAuthorIsEmpty() {
        fullDescriptor.setAuthor("");

        assertFalse(fullDescriptor.isValid());
    }

    // ==================== GET_PLUGIN_ID TESTS ====================

    @Test
    void getPluginId_ShouldReturnNameVersionFormat() {
        assertEquals("Test Plugin-2.0.0", fullDescriptor.getPluginId());
    }

    @Test
    void getPluginId_ShouldUpdate_WhenNameOrVersionChanges() {
        fullDescriptor.setName("New Name");
        fullDescriptor.setVersion("3.0.0");

        assertEquals("New Name-3.0.0", fullDescriptor.getPluginId());
    }

    // ==================== TOUCH TESTS ====================

    @Test
    void touch_ShouldUpdateLastModifiedDate() {
        final Date beforeTouch = fullDescriptor.getLastModifiedDate();

        // Wait a tiny bit to ensure time difference
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        fullDescriptor.touch();
        final Date afterTouch = fullDescriptor.getLastModifiedDate();

        assertNotEquals(beforeTouch, afterTouch);
        assertTrue(afterTouch.after(beforeTouch));
    }

    // ==================== GET_SUMMARY TESTS ====================

    @Test
    void getSummary_ShouldReturnFormattedString() {
        final String summary = fullDescriptor.getSummary();

        assertTrue(summary.contains("Test Plugin"));
        assertTrue(summary.contains("2.0.0"));
        assertTrue(summary.contains("Test Author"));
        assertTrue(summary.contains("This is a test plugin description"));
    }

    @Test
    void getSummary_ShouldTruncateLongDescription() {
        final String longDescription = "This is a very long description that exceeds fifty characters in length";
        fullDescriptor.setDescription(longDescription);

        final String summary = fullDescriptor.getSummary();

        assertTrue(summary.length() < longDescription.length());
        assertTrue(summary.endsWith("..."));
    }

    @Test
    void getSummary_ShouldNotTruncateShortDescription() {
        final String shortDescription = "Short desc";
        fullDescriptor.setDescription(shortDescription);

        final String summary = fullDescriptor.getSummary();

        assertTrue(summary.contains(shortDescription));
        assertFalse(summary.endsWith("..."));
    }

    // ==================== COMPATIBILITY TESTS ====================

    @Test
    void isCompatibleWith_ShouldReturnTrue_WhenRequiredVersionIsNull() {
        fullDescriptor.setRequiredHostVersion(null);

        assertTrue(fullDescriptor.isCompatibleWith("1.0.0"));
    }

    @Test
    void isCompatibleWith_ShouldReturnTrue_WhenRequiredVersionIsEmpty() {
        fullDescriptor.setRequiredHostVersion("");

        assertTrue(fullDescriptor.isCompatibleWith("1.0.0"));
    }

    @Test
    void isCompatibleWith_ShouldReturnTrue_WhenHostVersionIsGreater() {
        assertTrue(fullDescriptor.isCompatibleWith("2.0.0"));
    }

    @Test
    void isCompatibleWith_ShouldReturnTrue_WhenHostVersionIsEqual() {
        assertTrue(fullDescriptor.isCompatibleWith("1.5.0"));
    }

    @Test
    void isCompatibleWith_ShouldReturnFalse_WhenHostVersionIsLower() {
        assertFalse(fullDescriptor.isCompatibleWith("1.0.0"));
    }

    @Test
    void isCompatibleWith_ShouldHandleThreePartVersions() {
        fullDescriptor.setRequiredHostVersion("1.2.3");

        assertTrue(fullDescriptor.isCompatibleWith("1.2.3"));
        assertTrue(fullDescriptor.isCompatibleWith("1.2.4"));
        assertFalse(fullDescriptor.isCompatibleWith("1.2.2"));
    }

    @Test
    void isCompatibleWith_ShouldHandleDifferentVersionLengths() {
        fullDescriptor.setRequiredHostVersion("1.2");

        assertTrue(fullDescriptor.isCompatibleWith("1.2.0"));
        assertTrue(fullDescriptor.isCompatibleWith("1.2.5"));
        assertFalse(fullDescriptor.isCompatibleWith("1.1.9"));
    }

    @Test
    void isCompatibleWith_ShouldReturnFalse_WhenVersionParseFails() {
        fullDescriptor.setRequiredHostVersion("invalid.version");

        assertFalse(fullDescriptor.isCompatibleWith("1.0.0"));
    }

    // ==================== GETTER/SETTER TESTS ====================

    @Test
    void getId_ShouldReturnSetValue() {
        final String id = "new-id";
        fullDescriptor.setId(id);

        assertEquals(id, fullDescriptor.getId());
    }

    @Test
    void getName_ShouldReturnSetValue() {
        final String name = "New Plugin Name";
        fullDescriptor.setName(name);

        assertEquals(name, fullDescriptor.getName());
    }

    @Test
    void getVersion_ShouldReturnSetValue() {
        final String version = "3.0.0";
        fullDescriptor.setVersion(version);

        assertEquals(version, fullDescriptor.getVersion());
    }

    @Test
    void getMainClass_ShouldReturnSetValue() {
        final String mainClass = "com.new.Main";
        fullDescriptor.setMainClass(mainClass);

        assertEquals(mainClass, fullDescriptor.getMainClass());
    }

    @Test
    void getDescription_ShouldReturnSetValue() {
        final String description = "New description";
        fullDescriptor.setDescription(description);

        assertEquals(description, fullDescriptor.getDescription());
    }

    @Test
    void getAuthor_ShouldReturnSetValue() {
        final String author = "New Author";
        fullDescriptor.setAuthor(author);

        assertEquals(author, fullDescriptor.getAuthor());
    }

    @Test
    void getAuthorEmail_ShouldReturnSetValue() {
        final String email = "new@email.com";
        fullDescriptor.setAuthorEmail(email);

        assertEquals(email, fullDescriptor.getAuthorEmail());
    }

    @Test
    void getCategory_ShouldReturnSetValue() {
        final String category = "New Category";
        fullDescriptor.setCategory(category);

        assertEquals(category, fullDescriptor.getCategory());
    }

    @Test
    void getRequiredHostVersion_ShouldReturnSetValue() {
        final String requiredVersion = "2.0.0";
        fullDescriptor.setRequiredHostVersion(requiredVersion);

        assertEquals(requiredVersion, fullDescriptor.getRequiredHostVersion());
    }

    @Test
    void getSpecificationTitle_ShouldReturnSetValue() {
        final String title = "New Spec Title";
        fullDescriptor.setSpecificationTitle(title);

        assertEquals(title, fullDescriptor.getSpecificationTitle());
    }

    @Test
    void getSpecificationVersion_ShouldReturnSetValue() {
        final String version = "2.0";
        fullDescriptor.setSpecificationVersion(version);

        assertEquals(version, fullDescriptor.getSpecificationVersion());
    }

    @Test
    void getSpecificationVendor_ShouldReturnSetValue() {
        final String vendor = "New Vendor";
        fullDescriptor.setSpecificationVendor(vendor);

        assertEquals(vendor, fullDescriptor.getSpecificationVendor());
    }

    @Test
    void getImplementationVersion_ShouldReturnSetValue() {
        final String implVersion = "2.0.0";
        fullDescriptor.setImplementationVersion(implVersion);

        assertEquals(implVersion, fullDescriptor.getImplementationVersion());
    }

    @Test
    void getCreationDate_ShouldReturnSetValue() {
        final Date date = new Date(123456789L);
        fullDescriptor.setCreationDate(date);

        assertEquals(date, fullDescriptor.getCreationDate());
    }

    @Test
    void getLastModifiedDate_ShouldReturnSetValue() {
        final Date date = new Date(987654321L);
        fullDescriptor.setLastModifiedDate(date);

        assertEquals(date, fullDescriptor.getLastModifiedDate());
    }

    @Test
    void isEnabledByDefault_ShouldReturnSetValue() {
        fullDescriptor.setEnabledByDefault(true);
        assertTrue(fullDescriptor.isEnabledByDefault());

        fullDescriptor.setEnabledByDefault(false);
        assertFalse(fullDescriptor.isEnabledByDefault());
    }

    @Test
    void isAutoStart_ShouldReturnSetValue() {
        fullDescriptor.setAutoStart(true);
        assertTrue(fullDescriptor.isAutoStart());

        fullDescriptor.setAutoStart(false);
        assertFalse(fullDescriptor.isAutoStart());
    }

    @Test
    void isProvidesMenu_ShouldReturnSetValue() {
        fullDescriptor.setProvidesMenu(true);
        assertTrue(fullDescriptor.isProvidesMenu());

        fullDescriptor.setProvidesMenu(false);
        assertFalse(fullDescriptor.isProvidesMenu());
    }

    @Test
    void isProvidesToolbar_ShouldReturnSetValue() {
        fullDescriptor.setProvidesToolbar(true);
        assertTrue(fullDescriptor.isProvidesToolbar());

        fullDescriptor.setProvidesToolbar(false);
        assertFalse(fullDescriptor.isProvidesToolbar());
    }

    @Test
    void isProvidesServices_ShouldReturnSetValue() {
        fullDescriptor.setProvidesServices(true);
        assertTrue(fullDescriptor.isProvidesServices());

        fullDescriptor.setProvidesServices(false);
        assertFalse(fullDescriptor.isProvidesServices());
    }

    @Test
    void isRequiresNetwork_ShouldReturnSetValue() {
        fullDescriptor.setRequiresNetwork(true);
        assertTrue(fullDescriptor.isRequiresNetwork());

        fullDescriptor.setRequiresNetwork(false);
        assertFalse(fullDescriptor.isRequiresNetwork());
    }

    // ==================== EQUALS TESTS ====================

    @Test
    void testEquals_ShouldReturnTrue_ForSameInstance() {
        assertEquals(fullDescriptor, fullDescriptor);
    }

    @Test
    void testEquals_ShouldReturnFalse_ForNull() {
        assertNotEquals(null, fullDescriptor);
    }

    @Test
    void testEquals_ShouldReturnFalse_ForDifferentClass() {
        assertNotEquals("not a descriptor", fullDescriptor);
    }

    @Test
    void testEquals_ShouldReturnTrue_ForEqualDescriptors() {
        final PluginDescriptor other = new PluginDescriptor(
                "test-id", "Test Plugin", "2.0.0", "com.test.MainClass",
                "This is a test plugin description", "Test Author"
        );
        other.setAuthorEmail("author@test.com");
        other.setCategory("Testing");
        other.setRequiredHostVersion("1.5.0");

        assertEquals(fullDescriptor, other);
    }

    @Test
    void testEquals_ShouldReturnFalse_ForDifferentId() {
        final PluginDescriptor other = fullDescriptor.copy();
        other.setId("different-id");

        assertNotEquals(fullDescriptor, other);
    }

    @Test
    void canEqual_ShouldReturnTrue_ForSameClass() {
        assertTrue(fullDescriptor.canEqual(new PluginDescriptor()));
    }

    @Test
    void canEqual_ShouldReturnFalse_ForDifferentClass() {
        assertFalse(fullDescriptor.canEqual("not a descriptor"));
    }

    // ==================== HASHCODE TESTS ====================

    @Test
    void testHashCode_ShouldBeConsistent() {
        final int firstHash = fullDescriptor.hashCode();
        final int secondHash = fullDescriptor.hashCode();

        assertEquals(firstHash, secondHash);
    }

    @Test
    void testHashCode_ShouldBeEqual_ForEqualObjects() {
        final PluginDescriptor other = new PluginDescriptor(
                "test-id", "Test Plugin", "2.0.0", "com.test.MainClass",
                "This is a test plugin description", "Test Author"
        );

        assertEquals(fullDescriptor.hashCode(), other.hashCode());
    }

    // ==================== TOSTRING TESTS ====================

    @Test
    void testToString_ShouldNotReturnNull() {
        assertNotNull(fullDescriptor.toString());
    }

    @Test
    void testToString_ShouldContainKeyFields() {
        final String toString = fullDescriptor.toString();

        assertTrue(toString.contains("test-id"));
        assertTrue(toString.contains("Test Plugin"));
        assertTrue(toString.contains("2.0.0"));
        assertTrue(toString.contains("com.test.MainClass"));
        assertTrue(toString.contains("Test Author"));
        assertTrue(toString.contains("Testing"));
    }

    @Test
    void testToString_OnEmptyDescriptor_ShouldNotThrowException() {
        final PluginDescriptor empty = new PluginDescriptor();

        assertDoesNotThrow(empty::toString);
        assertNotNull(empty.toString());
    }
}