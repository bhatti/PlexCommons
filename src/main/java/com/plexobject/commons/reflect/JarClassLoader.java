/**
 * <B>CLASS COMMENTS</B>
 * Class Name: JarResource
 * Class Description:
 *   JarResource fetches a resource from jar file
 * @Author: SAB
 * $Author: shahzad $
 * Known Bugs:
 *   None
 * Concurrency Issues:
 *   None
 * Invariants:
 *   N/A
 * Modification History
 * Initial      Date            Changes
 * SAB          Apr 22, 1999    Created
 */

package com.plexobject.commons.reflect;

import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarClassLoader extends ClassLoader {
	private JarFile file;

	public JarClassLoader(JarFile file) {
		if (file == null)
			throw new NullPointerException();
		this.file = file;
	}

	public synchronized Class<?> findClass(String name)
			throws ClassNotFoundException {
		JarEntry entry;
		if ((entry = file.getJarEntry(name + ".class")) == null)
			throw new ClassNotFoundException("Class " + name + " not found");
		byte[] data = loadClassBytes(entry);
		return defineClass(name, data, 0, data.length);
	}

	private byte[] loadClassBytes(JarEntry entry) throws ClassNotFoundException {
		try {
			if (entry == null)
				throw new IllegalArgumentException("null entry");
			InputStream in = file.getInputStream(entry);
			byte[] data = new byte[in.available()];
			in.read(data);
			return data;
		} catch (IOException e) {
			throw new ClassNotFoundException(e.toString());
		}
	}
}
