package com.plexobject.commons.reflect;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

public class SlimJar {
	public static final String DISALLOWED_PACKAGES = "DISALLOWED_PACKAGES";
	public static final String[] SUN_PACKAGES = { "java.", "javax.", "sun.",
			"com.sun.", "org.omg." };
	public static final String[] SUN_CLASSES = { "boolean", "byte", "char",
			"float", "double", "int", "long", "short", "I", "B", "C", "F", "D",
			"J", "S", "V", "Z", "void" };
	// Attributes
	private java.util.jar.JarOutputStream jarStream;
	private DependencyLocator locator;
	private String[] pkgNames;
	private String[] skipClasses;
	private String[] disallowedPackages;
	private List<String> processed;
	private List<String> mainClasses;
	private static char fileSep = System.getProperty("file.separator")
			.charAt(0);
	public static boolean verbose = false;

	private class DependencyLocator extends ClassLoader {

		private JarOutputStream jarStream;
		private java.io.IOException ioError;
		private ClassLoader realClassLoader;
		private Map<String, Class<?>> cache = new HashMap<String, Class<?>>();
		private int entries;
		private String location;

		private DependencyLocator(java.util.jar.JarOutputStream jarStream) {
			this(jarStream, null);
		}

		private DependencyLocator(java.util.jar.JarOutputStream jarStream,
				ClassLoader realClassLoader) {
			if (realClassLoader == null)
				realClassLoader = getClass().getClassLoader();
			this.jarStream = jarStream;
			this.realClassLoader = realClassLoader;
			entries = 0;
		}

		public Class<?> loadClass(String name) throws ClassNotFoundException {
			return loadClass(name, true);
		}

		public synchronized Class<?> loadClass(String name, boolean resolveIt)
				throws ClassNotFoundException {
			// System.out.println("loadClass " + name);
			Class<?> result = cache.get(name);
			if (result != null)
				return result;
			result = findClass(name);
			if (result != null && resolveIt)
				resolveClass(result);
			cache.put(name, result);
			return result;
		}

		public Class<?> findClass(String name) throws ClassNotFoundException {
			// System.out.println("findClass(" + name + ")");
			// if (!acceptClass(name, false)) {
			if (!acceptClass(name)) {
				// System.out.println("Skipping " + name);
				return super.findSystemClass(name);
			}

			byte[] buffer = loadClassData(name);
			if (buffer == null) {
				throw new ClassNotFoundException(name);
			}
			if (ioError != null) {
				throw new ClassNotFoundException(ioError.getMessage());
			}
			return defineClass(name, buffer, 0, buffer.length);
		}

		private byte[] loadClassData(String name) {
			// if (name == null || name.length() == 0 || name.indexOf("$") !=
			// -1) return null;
			// System.out.println("loadClassData(" + name + ")");
			java.io.ByteArrayOutputStream result = new java.io.ByteArrayOutputStream();
			try {
				String resource = name.replace('.', fileSep).concat(".class");
				java.io.InputStream in = realClassLoader
						.getResourceAsStream(resource);
				/*
				 * if (in == null) { in =
				 * ClassLoader.getSystemClassLoader().getResourceAsStream
				 * (resource); }
				 */
				if (in == null) {
					StringBuffer sb = new StringBuffer();
					in = TypesExtractor.getInputStreamUsingClassInfo(name, sb);
					if (in == null) {
						System.out.println("Resource " + resource
								+ " not found");
						return null;
					}
					location = sb.toString();
				} else {
					location = realClassLoader.getResource(resource)
							.toExternalForm();
				}
				int ndx = location.indexOf(resource);
				if (ndx != -1)
					location = location.substring(0, ndx);
				if (location.startsWith("file:"))
					location = location.substring(5);

				resource = resource.replace('\\', '/');
				java.util.zip.ZipEntry entry = new java.util.zip.ZipEntry(
						resource);
				// System.out.println("Writing resource " + resource);
				jarStream.putNextEntry(entry);
				byte[] buffer = new byte[8192];
				int len;
				int tot = 0;
				while ((len = in.read(buffer)) != -1) {
					jarStream.write(buffer, 0, len);
					result.write(buffer, 0, len);
					tot += len;
				}
				entries++;
				if (verbose)
					System.out.println("SlimJar.loadClassData Adding "
							+ resource + " from " + location + ", length "
							+ tot + ", entries " + entries);
				return result.toByteArray();
			} catch (java.io.IOException e) {
				System.out.println("IOError " + e);
				ioError = e;
				return null;
			}
		}

	}

	public SlimJar(String[] pkgNames, String[] skipClasses, File jarFile)
			throws java.io.IOException {
		this(pkgNames, skipClasses, jarFile, null);
	}

	public SlimJar(String[] pkgNames, String[] skipClasses, File jarFile,
			Manifest manifest) throws java.io.IOException {
		this.pkgNames = pkgNames;
		this.skipClasses = skipClasses;
		if (manifest == null) {
			jarStream = new java.util.jar.JarOutputStream(
					new java.io.BufferedOutputStream(
							new java.io.FileOutputStream(jarFile)));
		} else {
			jarStream = new java.util.jar.JarOutputStream(
					new java.io.BufferedOutputStream(
							new java.io.FileOutputStream(jarFile)), manifest);
		}
		locator = this.new DependencyLocator(jarStream);
		processed = new ArrayList<String>();
		mainClasses = new ArrayList<String>();
		if (System.getProperty(DISALLOWED_PACKAGES) != null) {
			List<String> list = new ArrayList<String>();
			StringTokenizer st = new StringTokenizer(
					System.getProperty(DISALLOWED_PACKAGES), ",");
			while (st.hasMoreTokens()) {
				String next = st.nextToken().trim();
				System.out.println("Disallowing package [" + next + "]");
				list.add(next);
			}
			disallowedPackages = new String[list.size()];
			list.toArray(disallowedPackages);
		} else {
			disallowedPackages = SUN_PACKAGES;
		}
	}

	public void addResource(String name, File file) throws java.io.IOException {
		BufferedInputStream in = new BufferedInputStream(new FileInputStream(
				file));
		String resource = name.replace('\\', '/');
		java.util.zip.ZipEntry entry = new java.util.zip.ZipEntry(resource);
		jarStream.putNextEntry(entry);
		byte[] buffer = new byte[8192];
		int len;
		int tot = 0;
		while ((len = in.read(buffer)) != -1) {
			jarStream.write(buffer, 0, len);
			tot += len;
		}
		locator.entries++;
		if (verbose)
			System.out.println("SlimJar.loadClassData Adding " + resource
					+ " from " + file + ", length " + tot + ", entries "
					+ locator.entries);
	}

	public void addMain(String name) throws ClassNotFoundException,
			java.io.IOException {
		mainClasses.add(name);
		add(name);
	}

	public void add(final String name) throws ClassNotFoundException,
			java.io.IOException {
		if (locator.ioError != null)
			throw locator.ioError;
		Class<?> clazz = null;
		try {
			clazz = locator.loadClass(name);
		} catch (SecurityException e) {
			System.out.println("Class " + name + " cannot be defined");
			return;
		} catch (ClassNotFoundException e) {
			System.out.println("Class " + name + " is not found");
			return;
		}
		Class<?> type = TypesExtractor.getComponentType(clazz);

		if (processed.indexOf(type.getName()) != -1) {
			return;
		}

		try {
			if (clazz.isArray()) {
				locator.loadClass(type.getName());
			}
		} catch (ClassNotFoundException e) {
			return;
		}
		if (locator.ioError != null)
			throw locator.ioError;

		processed.add(type.getName());

		String[] reftypes = TypesExtractor.extractTypesUsingReflection(type);
		for (int i = 0; i < reftypes.length; i++) {
			if (acceptClass(reftypes[i]))
				add(reftypes[i]);
		}

		List<String> extracted = new ArrayList<String>();
		TypesExtractor.extractTypesUsingJavap(type, extracted);
		for (String extype : extracted) {
			if (acceptClass(extype))
				add(extype);
		}

		String[] ltypes = TypesExtractor.extractTypesUsingListing(type);
		for (int i = 0; i < ltypes.length; i++) {
			if (acceptClass(ltypes[i]))
				add(ltypes[i]);
		}
	}

	// //////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	public void done() throws java.io.IOException {
		if (locator.ioError != null)
			throw locator.ioError;
		jarStream.close();
	}

	private boolean acceptClass(String name) {
		return acceptClass(name, true);
	}

	private boolean acceptClass(String name, boolean checkMain) {
		if (checkMain && mainClasses.indexOf(name) != -1)
			return true;

		if (processed.indexOf(name) != -1)
			return false;

		for (int j = 0; j < skipClasses.length; j++) {
			if (name.equals(skipClasses[j]))
				return false;
		}
		for (int j = 0; j < pkgNames.length; j++) {
			if (name.startsWith(pkgNames[j]))
				return true;
		}
		for (int i = 0; i < disallowedPackages.length; i++) {
			if (name.startsWith(disallowedPackages[i]))
				return false;
		}
		for (int i = 0; i < SUN_CLASSES.length; i++) {
			if (name.equals(SUN_CLASSES[i]))
				return false;
		}
		if (pkgNames.length == 0)
			return true;
		else
			return false;
	}

	private static void usage() {
		System.err
				.println("Usage: java "
						+ SlimJar.class.getName()
						+ " [-v] [-p package1,package2,..] -j jarfile -s skip-classes classes");
		System.err.println("For example:\n java " + SlimJar.class.getName()
				+ " -p com.plexobject.util -j myjar.jar mypackage.MyMain");
		System.exit(1);
	}

	public static void main(String[] args) {
		try {
			int optind;
			String packages = null;
			String skipClasses = null;
			String jarname = null;
			for (optind = 0; optind < args.length; optind++) {
				if (args[optind].equals("-p")) {
					packages = args[++optind];
				} else if (args[optind].equals("-s")) {
					skipClasses = args[++optind];
				} else if (args[optind].equals("-j")) {
					jarname = args[++optind];
				} else if (args[optind].equals("--")) {
					optind++;
					break;
				} else if (args[optind].equals("-v")) {
					verbose = true;
				} else if (args[optind].equals("-h")) {
					usage();
				} else if (args[optind].equals("help")) {
					usage();
				} else if (args[optind].startsWith("-")) {
					usage();
				} else {
					break;
				}
			}
			String[] pkgNames = null;
			if (packages == null || packages.length() == 0) {
				pkgNames = new String[0];
			} else {
				pkgNames = packages.split(",");
			}

			String[] skipClassNames = null;
			if (skipClasses == null || skipClasses.length() == 0) {
				skipClassNames = new String[0];
			} else {
				skipClassNames = skipClasses.split(",");
			}

			if (jarname == null) {
				System.err.println("No jar file specified");
				usage();
			}
			if (optind >= args.length) {
				System.err.println("No main class names specified");
				usage();
			}
			File jarFile = new File(jarname);
			System.out.println("Creating " + jarFile.getAbsolutePath());
			SlimJar jar = new SlimJar(pkgNames, skipClassNames, jarFile);
			for (; optind < args.length; optind++) {
				// System.out.println("********** args[" + optind + "] " +
				// args[optind]);
				args[optind] = args[optind].replace('/', fileSep);
				jar.addMain(args[optind]);
			}
			jar.done();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (java.io.IOException e) {
			e.printStackTrace();
		}
	}

}
