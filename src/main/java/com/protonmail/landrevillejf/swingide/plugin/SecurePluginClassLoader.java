package com.protonmail.landrevillejf.swingide.plugin;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;

public class SecurePluginClassLoader extends URLClassLoader {
    private final List<String> allowedPackages = Arrays.asList(
            "com.protonmail.landrevillejf.swingide.plugin",
            "java.",
            "javax.swing."
    );

    public SecurePluginClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

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