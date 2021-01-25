/*
Copyright (c) 2014, SpigotMC. All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright notice, this
list of conditions and the following disclaimer.

Redistributions in binary form must reproduce the above copyright notice,
this list of conditions and the following disclaimer in the documentation
and/or other materials provided with the distribution.

The name of the author may not be used to endorse or promote products derived
from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
 */

package org.yatopia.yatoclip;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.concurrent.atomic.AtomicReference;

public class SpecialSourceLauncher {

	private static final AtomicReference<SpecialSourceClassLoader> classLoader = new AtomicReference<>(new SpecialSourceClassLoader(new URL[0], SpecialSourceLauncher.class.getClassLoader().getParent()));
	private static final AtomicReference<String> mainClass = new AtomicReference<>("");

	static void setSpecialSourceJar(File specialSourceJar) {
		synchronized (classLoader) {
			System.out.println("Setting up SpecialSource: " + specialSourceJar);
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
			System.out.println("Releasing SpecialSource");
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

		if (command[0].equals("java")) {
			command[0] = System.getProperty("java.home") + "/bin/" + command[0];
		}

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
