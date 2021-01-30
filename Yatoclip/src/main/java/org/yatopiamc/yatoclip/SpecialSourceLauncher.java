package org.yatopiamc.yatoclip;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

public class SpecialSourceLauncher {

	private static final AtomicReference<SpecialSourceClassLoader> classLoader = new AtomicReference<>(new SpecialSourceClassLoader(new URL[0], SpecialSourceLauncher.class.getClassLoader().getParent()));
	private static final AtomicReference<String> mainClass = new AtomicReference<>("");

	static void setSpecialSourceJar(File specialSourceJar) {
		synchronized (classLoader) {
			System.err.println("Setting up SpecialSource: " + specialSourceJar);
			try {
				classLoader.get().addURL(specialSourceJar.toURI().toURL());
				mainClass.set(Yatoclip.getMainClass(specialSourceJar.toPath()));
			} catch (IOException e) {
				throw new IllegalArgumentException(e);
			}
		}
	}

	static void resetSpecialSourceClassloader() {
		synchronized (classLoader) {
			if(!classLoader.get().isLoaded) return;
			System.err.println("Releasing SpecialSource");
			try {
				classLoader.get().close();
				classLoader.set(new SpecialSourceClassLoader(new URL[0], SpecialSourceLauncher.class.getClassLoader().getParent()));
				mainClass.set("");
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public static void runProcess(String... command) throws IOException {
		if (!(command != null && command.length > 0)) throw new IllegalArgumentException();

		System.err.println("Invoking SpecialSource with arguments: " + Arrays.toString(command));

		AtomicReference<Throwable> thrown = new AtomicReference<>(null);
		final Thread thread = new Thread(() -> {
			try {
				final Class<?> mainClass = Class.forName(SpecialSourceLauncher.mainClass.get(), true, classLoader.get());
				final Method mainMethod = mainClass.getMethod("main", String[].class);
				if (!Modifier.isStatic(mainMethod.getModifiers()) || !Modifier.isPublic(mainMethod.getModifiers()))
					throw new IllegalArgumentException();
				mainMethod.invoke(null, new Object[]{command});
			} catch (Throwable t) {
				thrown.set(t);
			}
		});
		thread.setName("SpecialSource Thread");
		thread.setContextClassLoader(classLoader.get());
		thread.start();
		while (thread.isAlive())
			try {
				thread.join();
			} catch (InterruptedException ignored) {
			}
		if (thrown.get() != null)
			throw new RuntimeException(thrown.get());

	}

	private static class SpecialSourceClassLoader extends URLClassLoader {

		private volatile boolean isLoaded = false;

		public SpecialSourceClassLoader(URL[] urls, ClassLoader parent) {
			super(urls, parent);
		}

		@Override
		protected synchronized void addURL(URL url) {
			if (isLoaded) throw new IllegalStateException();
			this.isLoaded = true;
			super.addURL(url);
		}
	}

}
