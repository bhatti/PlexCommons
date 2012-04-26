/**
 * <B>CLASS COMMENTS</B>
 * Class Name: ClassVersion
 * Class Description: 
 *   ClassVersion parses class 
 * @Author: SAB
 * $Author: shahzad $
 * Known Bugs:
 *   None
 * Concurrency Issues:
 *   None
 * Invariants:
 *   N/A
 *
 */

package com.plexobject.commons.reflect;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ClassVersion {
	public static void printVersion(String type) throws java.io.IOException,
			ClassFormatError, ClassNotFoundException {
		printVersion(getStream(type));
	}

	public static void printVersion(DataInputStream in)
			throws java.io.IOException, ClassFormatError {
		@SuppressWarnings("unused")
		int magic = in.readInt();
		int minor_version = in.readUnsignedShort();
		int major_version = in.readUnsignedShort();
		System.out.println(major_version + "." + minor_version);
	}

	protected static DataInputStream getStream(String type)
			throws java.io.IOException, ClassNotFoundException {
		if (type.endsWith(".class")) {
			return new DataInputStream(new BufferedInputStream(
					new FileInputStream(type)));
		}
		Class<?> clazz = Class.forName(type);
		java.security.CodeSource cs = clazz.getProtectionDomain()
				.getCodeSource();
		if (cs != null && cs.getLocation() != null) {
			String location = cs.getLocation().toExternalForm();
			if (!location.endsWith(".jar")) {
				location = location + separator
						+ clazz.getName().replace('.', separator) + ".class";
			}
			URL url = new URL(location);
			if (location.endsWith(".jar")) {
				return loadFromJar(
						clazz.getName().replace('.', '/') + ".class",
						url.openStream());
			} else {
				return new DataInputStream(new BufferedInputStream(
						url.openStream()));
			}
		} else {
			throw new IOException("Failed to find " + type);
		}
	}

	protected static DataInputStream loadFromJar(String type, InputStream in)
			throws IOException {
		BufferedInputStream bis = new BufferedInputStream(in);
		ZipInputStream zis = new ZipInputStream(bis);
		ZipEntry ze = null;
		while ((ze = zis.getNextEntry()) != null) {
			if (ze.isDirectory())
				continue;
			if (ze.getName().equals(type)) {
				int size = (int) ze.getSize();
				if (size < 0)
					return new DataInputStream(zis);
				byte[] b = new byte[(int) size];
				int rb = 0;
				int chunk = 0;
				while (((int) size - rb) > 0) {
					chunk = zis.read(b, rb, (int) size - rb);
					if (chunk == -1)
						break;
					rb += chunk;
				}
				return new DataInputStream(new ByteArrayInputStream(b));
			}
		}
		throw new IOException("Unable to load " + type);
	}

	public static void main(String[] args) throws Exception {
		for (int i = 0; i < args.length; i++) {
			printVersion(args[i]);
		}
	}

	private static char separator = System.getProperty("file.separator")
			.charAt(0);
}
