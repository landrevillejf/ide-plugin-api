package com.protonmail.landrevillejf.ide.plugin;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;

/**
 * Secure class loader for plugins with package-based access control.
 * <p>
 * This class loader restricts plugin access to only allowed packages,
 * providing a security sandbox for plugin code execution.
 * </p>
 *
 * @author landrevillejf
 * @version 1.0.0
 * @since 1.0.0
 */
public class SecurePluginClassLoader extends URLClassLoader {
    private final List<String> allowedPackages = Arrays.asList(
            "com.protonmail.landrevillejf.swingide.plugin",
            "java.",
            "javax.swing."
    );

    /**
     * Creates a new secure plugin class loader.
     *
     * @param urls the URLs from which to load classes and resources
     * @param parent the parent class loader for delegation
     */
    public SecurePluginClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    /**
     * Loads a class with security restrictions.
     * <p>
     * Only allows loading classes from allowed packages. Primitive types
     * and array types are handled specially.
     * </p>
     *
     * @param name the binary name of the class
     * @param resolve whether to resolve the class
     * @return the loaded class
     * @throws ClassNotFoundException if the class cannot be loaded or is not allowed
     * @throws NullPointerException if the class name is null
     */
    @Override
    protected Class<?> loadClass(String name, boolean resolve)
            throws ClassNotFoundException {

        // Gérer les noms de classe null ou vides
        if (name == null) {
            throw new NullPointerException("Class name cannot be null");
        }

        if (name.isEmpty()) {
            throw new ClassNotFoundException("Class name cannot be empty");
        }

        Class<?> primitiveType = getPrimitiveType(name);
        if (primitiveType != null) {
            return primitiveType;
        }

        if (name.startsWith("[")) {
            try {
                // Utiliser Class.forName avec le classloader parent
                return Class.forName(name, resolve, getParent());
            } catch (ClassNotFoundException e) {
                // Essayer avec le classloader système
                return Class.forName(name, resolve, ClassLoader.getSystemClassLoader());
            }
        }

        for (String allowed : allowedPackages) {
            if (name.startsWith(allowed)) {
                return super.loadClass(name, resolve);
            }
        }

        throw new SecurityException("Unallowed for this class: " + name);
    }

    /**
     * Returns the primitive type class for a given primitive name.
     *
     * @param name the primitive type name
     * @return the primitive class, or {@code null} if not a primitive
     */
    private Class<?> getPrimitiveType(String name) {
        return switch (name) {
            case "int" -> int.class;
            case "long" -> long.class;
            case "double" -> double.class;
            case "float" -> float.class;
            case "boolean" -> boolean.class;
            case "byte" -> byte.class;
            case "char" -> char.class;
            case "short" -> short.class;
            case "void" -> void.class;
            default -> null;
        };
    }
}