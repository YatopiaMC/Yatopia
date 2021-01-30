package org.yatopia.yatoclip;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarInputStream;

public class Yatoclip {

    public static void main(String... args) throws IOException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final Path setup = ServerSetup.setup();
        launch(setup, args);
    }

    private static void launch(Path setup, String[] args) throws MalformedURLException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        final URLClassLoader classLoader = new URLClassLoader(new URL[]{new URL(null, setup.toUri().toString())}, Yatoclip.class.getClassLoader().getParent());
        final Class<?> mainClassInstance = Class.forName("org.bukkit.craftbukkit.Main", true, classLoader);
        final Method mainMethod = mainClassInstance.getMethod("main", String[].class);
        if(!Modifier.isPublic(mainMethod.getModifiers()) || !Modifier.isStatic(mainMethod.getModifiers())) throw new IllegalArgumentException();
        Thread.currentThread().setContextClassLoader(classLoader);
        mainMethod.invoke(null, new Object[]{args});
    }

    static String getMainClass(Path jarPath) throws IOException {
        final String mainClass;
        try (
                InputStream inputStream = Files.newInputStream(jarPath);
                JarInputStream jar = new JarInputStream(inputStream)
                ) {
            mainClass = jar.getManifest().getMainAttributes().getValue("Main-Class");
        }
        return mainClass;
    }

}
