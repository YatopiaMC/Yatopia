package org.yatopiamc.yatoclip;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;

public class YatoclipLaunch {

    public static void premain(String args, Instrumentation inst) {
    }

    static void injectClasspath(Path setup) throws Throwable {
        final ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
        if(!(systemClassLoader instanceof URLClassLoader))
            throw new ClassCastException("SystemClassLoader is not an instance of URLClassLoader");
        final URLClassLoader classLoader = (URLClassLoader) systemClassLoader;
        final Method addURL = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
        addURL.setAccessible(true);
        addURL.invoke(classLoader, setup.toUri().toURL());
    }

}
