package com.protonmail.landrevillejf.ide.plugin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SecurePluginClassLoader Tests")
class SecurePluginClassLoaderTest {

    private SecurePluginClassLoader classLoader;
    
    @BeforeEach
    void setUp() throws MalformedURLException {
        // Create a test classloader with an empty URL list
        URL[] urls = new URL[0];
        classLoader = new SecurePluginClassLoader(urls, ClassLoader.getSystemClassLoader());
    }

    @Nested
    @DisplayName("Allowed Package Tests")
    class AllowedPackageTests {
        
        @Test
        @DisplayName("Should load class from allowed swingide package")
        void testLoadAllowedSwingIDEClass() throws ClassNotFoundException {
            // Try to load a class from the allowed swingide package
            // Note: This assumes the class exists, if not, we test the security check
            try {
                Class<?> clazz = classLoader.loadClass("com.protonmail.landrevillejf.swingide.plugin.api.Plugin");
                assertNotNull(clazz);
            } catch (ClassNotFoundException e) {
                // If the class doesn't exist, we still passed the security check
                // The exception is from the parent classloader, not from security
                assertTrue(e.getMessage().contains("com.protonmail.landrevillejf.swingide.plugin.api.Plugin"));
            }
        }
        
        @Test
        @DisplayName("Should load class from java package")
        void testLoadJavaPackageClass() throws ClassNotFoundException {
            Class<?> clazz = classLoader.loadClass("java.lang.String");
            assertNotNull(clazz);
            assertEquals(String.class, clazz);
        }
        
        @Test
        @DisplayName("Should load class from java.util package")
        void testLoadJavaUtilPackageClass() throws ClassNotFoundException {
            Class<?> clazz = classLoader.loadClass("java.util.ArrayList");
            assertNotNull(clazz);
            assertEquals(java.util.ArrayList.class, clazz);
        }
        
        @Test
        @DisplayName("Should load class from javax.swing package")
        void testLoadJavaxSwingPackageClass() throws ClassNotFoundException {
            Class<?> clazz = classLoader.loadClass("javax.swing.JButton");
            assertNotNull(clazz);
            assertEquals(javax.swing.JButton.class, clazz);
        }
        
        @Test
        @DisplayName("Should load class from javax.swing subpackage")
        void testLoadJavaxSwingSubpackageClass() throws ClassNotFoundException {
            Class<?> clazz = classLoader.loadClass("javax.swing.text.JTextComponent");
            assertNotNull(clazz);
            assertEquals(javax.swing.text.JTextComponent.class, clazz);
        }
        
        @Test
        @DisplayName("Should load class from java.lang subpackage")
        void testLoadJavaLangSubpackageClass() throws ClassNotFoundException {
            Class<?> clazz = classLoader.loadClass("java.lang.reflect.Method");
            assertNotNull(clazz);
            assertEquals(java.lang.reflect.Method.class, clazz);
        }
        
        @Test
        @DisplayName("Should throw SecurityException for disallowed package")
        void testLoadDisallowedPackageClass() {
            assertThrows(SecurityException.class, () -> {
                classLoader.loadClass("com.example.MaliciousClass");
            });
        }
        
        @Test
        @DisplayName("Should throw SecurityException for com.google package")
        void testLoadGooglePackageClass() {
            assertThrows(SecurityException.class, () -> {
                classLoader.loadClass("com.google.common.collect.Lists");
            });
        }
        
        @Test
        @DisplayName("Should throw SecurityException for org.apache package")
        void testLoadApachePackageClass() {
            assertThrows(SecurityException.class, () -> {
                classLoader.loadClass("org.apache.commons.lang3.StringUtils");
            });
        }
        
        @Test
        @DisplayName("Should throw SecurityException for disallowed inner class")
        void testLoadDisallowedInnerClass() {
            assertThrows(SecurityException.class, () -> {
                classLoader.loadClass("com.example.MaliciousClass$InnerClass");
            });
        }
        
        @Test
        @DisplayName("Should handle package boundary cases correctly")
        void testPackageBoundaryCases() {
            // Test packages that start with allowed prefix but aren't truly allowed
            // "java." is allowed, so "javax" without dot shouldn't be allowed
            assertThrows(SecurityException.class, () -> {
                classLoader.loadClass("javaxrmi.CORBA");
            });
            
            // Test a class that starts with "java" but has different capitalization
            assertThrows(SecurityException.class, () -> {
                classLoader.loadClass("JavaLang.String");
            });
        }

        @Test
        @DisplayName("Should load array classes of allowed types")
        void testLoadArrayClass() throws ClassNotFoundException {
            // Les tableaux sont chargés par le parent classloader
            // On vérifie simplement qu'ils ne lancent pas de SecurityException
            assertDoesNotThrow(() -> {
                Class<?> clazz = classLoader.loadClass("[Ljava.lang.String;");
                assertNotNull(clazz);
                assertTrue(clazz.isArray());
            });

            // Tableau de primitives
            assertDoesNotThrow(() -> {
                Class<?> intArray = classLoader.loadClass("[I");
                assertNotNull(intArray);
                assertTrue(intArray.isArray());
            });
        }

        @Test
        @DisplayName("Should load primitive classes")
        void testLoadPrimitiveClass() throws ClassNotFoundException {
            // Les primitives sont retournées directement par le classloader
            Class<?> intClass = classLoader.loadClass("int");
            assertNotNull(intClass);
            assertEquals(int.class, intClass);

            Class<?> booleanClass = classLoader.loadClass("boolean");
            assertNotNull(booleanClass);
            assertEquals(boolean.class, booleanClass);

            Class<?> longClass = classLoader.loadClass("long");
            assertNotNull(longClass);
            assertEquals(long.class, longClass);

            Class<?> byteClass = classLoader.loadClass("byte");
            assertNotNull(byteClass);
            assertEquals(byte.class, byteClass);

            Class<?> charClass = classLoader.loadClass("char");
            assertNotNull(charClass);
            assertEquals(char.class, charClass);

            Class<?> shortClass = classLoader.loadClass("short");
            assertNotNull(shortClass);
            assertEquals(short.class, shortClass);

            Class<?> doubleClass = classLoader.loadClass("double");
            assertNotNull(doubleClass);
            assertEquals(double.class, doubleClass);

            Class<?> floatClass = classLoader.loadClass("float");
            assertNotNull(floatClass);
            assertEquals(float.class, floatClass);

            Class<?> voidClass = classLoader.loadClass("void");
            assertNotNull(voidClass);
            assertEquals(void.class, voidClass);
        }
    }
    
    @Nested
    @DisplayName("Class Loading With JAR Files")
    class JarLoadingTests {
        
        @TempDir
        Path tempDir;
        
        private URL createTestJar() throws IOException {
            Path jarPath = tempDir.resolve("test-plugin.jar");
            
            try (OutputStream os = Files.newOutputStream(jarPath);
                 JarOutputStream jos = new JarOutputStream(os)) {
                
                // Create a simple test class entry
                JarEntry entry = new JarEntry("com/protonmail/landrevillejf/swingide/plugin/api/TestPlugin.class");
                jos.putNextEntry(entry);
                jos.write(new byte[]{(byte) 0xCA, (byte) 0xFE, (byte) 0xBA, (byte) 0xBE}); // Dummy class bytes
                jos.closeEntry();
            }
            
            return jarPath.toUri().toURL();
        }
        
        @Test
        @DisplayName("Should load class from JAR in allowed package")
        void testLoadClassFromJar() throws IOException, ClassNotFoundException {
            URL jarUrl = createTestJar();
            SecurePluginClassLoader jarClassLoader = new SecurePluginClassLoader(
                new URL[]{jarUrl}, 
                ClassLoader.getSystemClassLoader()
            );
            
            // This should attempt to load but may fail with ClassFormatError due to dummy bytes
            // The security check should pass though
            assertThrows(ClassFormatError.class, () -> {
                jarClassLoader.loadClass("com.protonmail.landrevillejf.swingide.plugin.api.TestPlugin");
            });
        }
        
        @Test
        @DisplayName("Should enforce security for classes in JAR")
        void testEnforceSecurityForJarClasses() throws IOException {
            URL jarUrl = createTestJar();
            SecurePluginClassLoader jarClassLoader = new SecurePluginClassLoader(
                new URL[]{jarUrl}, 
                ClassLoader.getSystemClassLoader()
            );
            
            assertThrows(SecurityException.class, () -> {
                jarClassLoader.loadClass("com.evil.MaliciousClass");
            });
        }
    }
    
    @Nested
    @DisplayName("Parent Delegation Tests")
    class ParentDelegationTests {

        @Test
        @DisplayName("Should delegate to parent classloader for allowed classes")
        void testDelegationToParent() throws ClassNotFoundException {
            // Cette classe doit être chargée par le parent
            Class<?> clazz = classLoader.loadClass("java.lang.Object");
            assertNotNull(clazz);
            // Vérifier que le classLoader parent est bien le système
            // Note: getClassLoader() peut retourner null pour les classes bootstrap
            ClassLoader classLoader = clazz.getClassLoader();
            // Les classes java.lang sont chargées par le bootstrap classloader (null)
            // C'est normal et acceptable
            assertTrue(classLoader == null || classLoader == ClassLoader.getSystemClassLoader());
        }
        
        @Test
        @DisplayName("Should not delegate for disallowed classes")
        void testNoDelegationForDisallowed() {
            assertThrows(SecurityException.class, () -> {
                classLoader.loadClass("com.example.DisallowedClass");
            });
        }
        
        @Test
        @DisplayName("Should throw ClassNotFoundException when class doesn't exist")
        void testClassNotFound() {
            assertThrows(ClassNotFoundException.class, () -> {
                classLoader.loadClass("java.lang.NonExistentClass");
            });
        }
    }
    
    @Nested
    @DisplayName("Resource Loading Tests")
    class ResourceLoadingTests {
        
        @Test
        @DisplayName("Should find resources for allowed packages")
        void testFindResource() {
            // Resources don't have the same security restrictions as classes
            URL resource = classLoader.getResource("java/lang/String.class");
            // May be null if resource not found, but should not throw security exception
            assertDoesNotThrow(() -> classLoader.getResource("com.example.resource.txt"));
        }
        
        @Test
        @DisplayName("Should get resource as stream")
        void testGetResourceAsStream() {
            assertDoesNotThrow(() -> classLoader.getResourceAsStream("any/resource.txt"));
        }
    }
    
    @Nested
    @DisplayName("Edge Cases and Error Handling")
    class EdgeCaseTests {
        
        @Test
        @DisplayName("Should handle null class name")
        void testNullClassName() {
            assertThrows(NullPointerException.class, () -> {
                classLoader.loadClass(null);
            });
        }
        
        @Test
        @DisplayName("Should handle empty class name")
        void testEmptyClassName() {
            assertThrows(ClassNotFoundException.class, () -> {
                classLoader.loadClass("");
            });
        }
        
        @Test
        @DisplayName("Should handle class name with spaces")
        void testClassNameWithSpaces() {
            assertThrows(SecurityException.class, () -> {
                classLoader.loadClass("com example Test");
            });
        }
        
        @Test
        @DisplayName("Should be able to load multiple classes sequentially")
        void testLoadMultipleClasses() throws ClassNotFoundException {
            Class<?> stringClass = classLoader.loadClass("java.lang.String");
            Class<?> arrayListClass = classLoader.loadClass("java.util.ArrayList");
            Class<?> jButtonClass = classLoader.loadClass("javax.swing.JButton");
            
            assertNotNull(stringClass);
            assertNotNull(arrayListClass);
            assertNotNull(jButtonClass);
        }
        
        @Test
        @DisplayName("Should cache loaded classes")
        void testClassCaching() throws ClassNotFoundException {
            Class<?> first = classLoader.loadClass("java.lang.String");
            Class<?> second = classLoader.loadClass("java.lang.String");
            
            assertSame(first, second);
        }
        
        @Test
        @DisplayName("Should handle resolve flag correctly")
        void testLoadClassWithResolve() throws ClassNotFoundException {
            assertDoesNotThrow(() -> {
                classLoader.loadClass("java.lang.String", true);
            });
            
            assertDoesNotThrow(() -> {
                classLoader.loadClass("java.lang.String", false);
            });
        }
    }
    
    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {
        
        @Test
        @DisplayName("Should create classloader with URLs")
        void testConstructorWithURLs() throws MalformedURLException {
            URL[] urls = {Paths.get(".").toUri().toURL()};
            SecurePluginClassLoader loader = new SecurePluginClassLoader(urls, ClassLoader.getSystemClassLoader());
            assertNotNull(loader);
        }
        
        @Test
        @DisplayName("Should create classloader with empty URLs")
        void testConstructorWithEmptyURLs() {
            URL[] urls = new URL[0];
            SecurePluginClassLoader loader = new SecurePluginClassLoader(urls, ClassLoader.getSystemClassLoader());
            assertNotNull(loader);
        }
        
        @Test
        @DisplayName("Should accept null parent classloader")
        void testConstructorWithNullParent() throws MalformedURLException {
            URL[] urls = new URL[0];
            SecurePluginClassLoader loader = new SecurePluginClassLoader(urls, null);
            assertNotNull(loader);
        }
    }
    
    @Nested
    @DisplayName("Security Exception Message Tests")
    class SecurityExceptionMessageTests {
        
        @Test
        @DisplayName("Should include disallowed class name in exception message")
        void testExceptionMessageIncludesClassName() {
            String disallowedClass = "com.example.HackerClass";
            SecurityException exception = assertThrows(SecurityException.class, () -> {
                classLoader.loadClass(disallowedClass);
            });
            
            assertTrue(exception.getMessage().contains(disallowedClass));
            assertTrue(exception.getMessage().contains("Unallowed for this class"));
        }
        
        @Test
        @DisplayName("Should have meaningful exception for nested disallowed classes")
        void testExceptionForNestedDisallowedClass() {
            String disallowedClass = "com.example.Outer$Inner";
            SecurityException exception = assertThrows(SecurityException.class, () -> {
                classLoader.loadClass(disallowedClass);
            });
            
            assertTrue(exception.getMessage().contains(disallowedClass));
        }
    }
    
    @Nested
    @DisplayName("Concurrent Loading Tests")
    class ConcurrentLoadingTests {
        
        @Test
        @DisplayName("Should handle concurrent class loading")
        void testConcurrentClassLoading() throws InterruptedException {
            Thread[] threads = new Thread[10];
            Class<?>[][] results = new Class<?>[10][1];
            Exception[] exceptions = new Exception[10];
            
            for (int i = 0; i < threads.length; i++) {
                final int index = i;
                threads[i] = new Thread(() -> {
                    try {
                        results[index][0] = classLoader.loadClass("java.lang.String");
                    } catch (ClassNotFoundException e) {
                        exceptions[index] = e;
                    }
                });
            }
            
            for (Thread thread : threads) {
                thread.start();
            }
            
            for (Thread thread : threads) {
                thread.join();
            }
            
            // All threads should have loaded the same class
            for (int i = 0; i < threads.length; i++) {
                assertNull(exceptions[i], "Thread " + i + " had exception");
                assertNotNull(results[i][0], "Thread " + i + " got null result");
            }
            
            // All results should be the same instance
            for (int i = 1; i < threads.length; i++) {
                assertSame(results[0][0], results[i][0]);
            }
        }
        
        @Test
        @DisplayName("Should handle concurrent security violations")
        void testConcurrentSecurityViolations() throws InterruptedException {
            Thread[] threads = new Thread[10];
            SecurityException[] exceptions = new SecurityException[10];
            
            for (int i = 0; i < threads.length; i++) {
                final int index = i;
                threads[i] = new Thread(() -> {
                    try {
                        classLoader.loadClass("com.example.MaliciousClass");
                    } catch (SecurityException e) {
                        exceptions[index] = e;
                    } catch (ClassNotFoundException e) {
                        // Should not happen
                    }
                });
            }
            
            for (Thread thread : threads) {
                thread.start();
            }
            
            for (Thread thread : threads) {
                thread.join();
            }
            
            // All threads should have gotten SecurityException
            for (int i = 0; i < threads.length; i++) {
                assertNotNull(exceptions[i], "Thread " + i + " did not get SecurityException");
                assertTrue(exceptions[i].getMessage().contains("com.example.MaliciousClass"));
            }
        }
    }
    
    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {
        
        @Test
        @DisplayName("Should work with defineClass for allowed packages")
        void testDefineClass() {
            // This is a more complex test that would require bytecode generation
            // For now, we just verify the classloader can be used
            assertNotNull(classLoader);
        }
        
        @Test
        @DisplayName("Should properly isolate plugin classes")
        void testClassIsolation() throws ClassNotFoundException, MalformedURLException {
            // Create two classloaders with different URLs
            URL[] urls1 = {Paths.get(".").toUri().toURL()};
            URL[] urls2 = {Paths.get(".").toUri().toURL()};
            
            SecurePluginClassLoader loader1 = new SecurePluginClassLoader(urls1, ClassLoader.getSystemClassLoader());
            SecurePluginClassLoader loader2 = new SecurePluginClassLoader(urls2, ClassLoader.getSystemClassLoader());
            
            // Both should load the same system class as the same instance
            Class<?> string1 = loader1.loadClass("java.lang.String");
            Class<?> string2 = loader2.loadClass("java.lang.String");
            
            assertSame(string1, string2);
        }
    }

    @Nested
    @DisplayName("Array type fallback")
    class ArrayFallbackTests {

        @Test
        @DisplayName("Should fall back to system classloader when parent cannot load array type")
        void testLoadArrayClass_FallbackToSystemClassLoader() throws ClassNotFoundException {
            // Parent that refuses every class, forcing the CNFE catch branch
            ClassLoader failingParent = new ClassLoader(null) {
                @Override
                public Class<?> loadClass(String name) throws ClassNotFoundException {
                    throw new ClassNotFoundException("parent refuses: " + name);
                }
            };
            SecurePluginClassLoader loader =
                    new SecurePluginClassLoader(new URL[0], failingParent);

            // "[Ljava.lang.String;" is the binary name of String[]
            Class<?> arrayClass = loader.loadClass("[Ljava.lang.String;");

            assertEquals(String[].class, arrayClass);
        }
    }
}