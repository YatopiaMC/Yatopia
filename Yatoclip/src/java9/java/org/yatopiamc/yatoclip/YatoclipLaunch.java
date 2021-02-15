package org.yatopiamc.yatoclip;

import java.lang.instrument.Instrumentation;
import java.nio.file.Path;
import java.util.jar.JarFile;

public class YatoclipLaunch {

    private static Instrumentation inst = null;

    public static void premain(String args, Instrumentation inst) {
        YatoclipLaunch.inst = inst;
    }

    public static void agentmain(final String agentArgs, final Instrumentation inst) {
        YatoclipLaunch.inst = inst;
    }

    @SuppressWarnings("unused")
    static void injectClasspath(Path setup) throws Throwable {
        if(inst == null) {
            throw new RuntimeException("Instrumentation API handle not found");
        }
        inst.appendToSystemClassLoaderSearch(new JarFile(setup.toFile()));
        inst = null;
    }

}
