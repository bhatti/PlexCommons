/**
 * <B>CLASS COMMENTS</B>
 * Class Name: ShowDepend
 * Class Description: 
 *   ShowIDepend shows dependencies statements for a class
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

public class ShowDepend {
	public static final String DISALLOWED_PACKAGES = "DISALLOWED_PACKAGES";
	public static final String[] SUN_PACKAGES = { "java.", "javax.", "sun.",
			"com.sun.", "org.omg." };
	public static final String[] SUN_CLASSES = { "boolean", "byte", "char",
			"float", "double", "int", "long", "short", "I", "B", "C", "F", "D",
			"J", "S", "V", "Z", "void" };

	public String[] getDepends(final String name) {
		List<String> importlist = new ArrayList<String>();
		try {
			if (includes(skipList, name)) {
				if (verbose)
					System.err.println("# ** found in skip list, skipping "
							+ name);
				return new String[0];
			}
			if (mustList.size() > 0 && includes(mustList, name)) {
				if (verbose)
					System.err.println("# ** not in must list, skipping "
							+ name);
				return new String[0];
			}
			// /
			Class<?> clazz = Class.forName(name);
			Class<?> type = TypesExtractor.getComponentType(clazz);

			String[] reftypes = TypesExtractor
					.extractTypesUsingReflection(type);
			for (int i = 0; i < reftypes.length; i++) {
				if (acceptClass(type, reftypes[i]))
					importlist.add(reftypes[i]);
			}
			List<String> extracted = new ArrayList<String>();
			TypesExtractor.extractTypesUsingJavap(type, extracted);
			for (String extype : extracted) {
				if (importlist.indexOf(extype) == -1
						&& acceptClass(type, extype)) {
					importlist.add(extype);
				}
			}
			String[] ltypes = TypesExtractor.extractTypesUsingListing(type);
			for (int i = 0; i < ltypes.length; i++) {
				if (importlist.indexOf(ltypes[i]) == -1
						&& acceptClass(type, ltypes[i])) {
					importlist.add(ltypes[i]);
				}
			}
		} catch (java.lang.ClassNotFoundException e) {
			System.err.println("Failed to add (" + name + ") " + e);
		} catch (java.lang.NoClassDefFoundError e) {
			System.err.println("Failed to add (" + name + ") " + e);
		} catch (Throwable e) {
			e.printStackTrace();
		}

		String[] imports = new String[importlist.size()];
		int i = 0;
		for (String fname : importlist) {
			imports[i++] = fname.replace('$', '.');
		}
		Arrays.sort(imports);
		return imports;
	}

	private void printDotSyntax(final String filename) {
		PrivilegedAction<Object> action = new PrivilegedAction<Object>() {
			public Object run() {
				try {
					File file = new File(filename);
					if (verbose)
						System.err.println("# Writing "
								+ file.getAbsolutePath());
					PrintStream out = new PrintStream(
							new FileOutputStream(file));
					printDotSyntax(out);
					out.close();
				} catch (Throwable e) {
					e.printStackTrace();
				}
				return null;
			}
		};
		AccessController.doPrivileged(action);
	}

	private void printDotSyntax(PrintStream out) {
		Map<String, Boolean> duplicates = new HashMap<String, Boolean>();
		out.println("digraph G {");
		// out.println("  orientation = landscape;");
		// out.println("  size=\"10,8\";");
		// out.println("  ratio=\"fill\";");
		// out.println("  page=\"8.5,11\";");
		for (String key : dependencies.keySet()) {
			String[] depend = (String[]) dependencies.get(key);
			for (int i = 0; depend != null && i < depend.length; i++) {
				if (acceptClass(depend[i])) {
					String line = "\"" + key + "\"" + " -> " + "\"" + depend[i]
							+ "\"";
					String oline = "\"" + depend[i] + "\"" + " -> " + "\""
							+ key + "\"";
					if (duplicates.get(line) == null) {
						duplicates.put(line, Boolean.TRUE);
						out.println(line);
						if (duplicates.get(oline) != null) {
							out.println("#--duplicate " + line);
						}
					}
				}
			}
		}
		out.println("}");
	}

	private boolean includes(List<String> list, String pattern) {
		for (String name : list) {
			if (pattern.indexOf(name) != -1)
				return true;
		}
		return false;
	}

	private void addClassDepend(String klass) {
		if (checkSM && System.getSecurityManager() != null) {
			if (verbose)
				System.err.println("# set security manager");
			checkSM = false;
		}
		if (includes(skipList, klass)) {
			if (verbose)
				System.err.println("# found in skip list, skipping " + klass);
			return;
		}
		if (mustList.size() > 0 && includes(mustList, klass)) {
			if (verbose)
				System.err.println("# not in must list, skipping " + klass);
			return;
		}
		if (verbose)
			System.err.println("# adding " + klass);
		String[] deps = getDepends(klass);
		if (packageOnly) {
			klass = klass.substring(0, klass.lastIndexOf('.'));
			for (int i = 0; i < deps.length; i++) {
				deps[i] = deps[i].substring(0, deps[i].lastIndexOf('.'));
			}
		}
		dependencies.put(klass, deps);
	}

	private void addZipDepend(String zip) {
		JarResources jr = new JarResources(zip);
		String[] names = jr.getResourceNames();
		for (int i = 0; i < names.length; i++) {
			if (names[i].endsWith(".class"))
				addClassDepend(file2class(names[i]));
		}
	}

	private void addDirDepend(String dirname) {
		File dir = new File(dirname);
		List<String> files = new ArrayList<String>();
		getFiles(dir.getAbsolutePath().length(), dir, files);
		for (String name : files) {
			addClassDepend(file2class(name));
		}
	}

	private void getFiles(int top, File dir, List<String> files) {
		File[] list = dir.listFiles();
		for (int i = 0; list != null && i < list.length; i++) {
			if (list[i].isDirectory())
				getFiles(top, list[i], files);
			else {
				String name = list[i].getAbsolutePath();
				if (name.endsWith(".class"))
					files.add(name.substring(top + 1));
				else if (name.endsWith(".jar"))
					addZipDepend(name);
			}
		}
	}

	private String file2class(String name) {
		int n = name.indexOf(".class");
		if (n != -1)
			name = name.substring(0, n);
		name = name.replace('\\', '.');
		name = name.replace('/', '.');
		return name;
	}

	public static void main(String[] args) {
		try {
			// System.setSecurityManager(new RMISecurityManager());
			ShowDepend si = new ShowDepend();
			if (System.getProperty(DISALLOWED_PACKAGES) != null) {
				List<String> list = new ArrayList<String>();
				StringTokenizer st = new StringTokenizer(
						System.getProperty(DISALLOWED_PACKAGES), ",");
				while (st.hasMoreTokens()) {
					list.add(st.nextToken().trim());
				}
				si.disallowedPackages = new String[list.size()];
				list.toArray(si.disallowedPackages);
			} else {
				si.disallowedPackages = SUN_PACKAGES;
			}

			String filename = null;
			for (int i = 0; i < args.length; i++) {
				File file = new File(args[i]);
				if (args[i].equals("-P"))
					si.packageOnly = true;
				else if (args[i].equals("-p")) {
					String packages = args[++i];
					if (packages == null || packages.length() == 0) {
						si.pkgNames = new String[0];
					} else {
						StringTokenizer st = new StringTokenizer(packages, ",");
						List<String> list = new ArrayList<String>();
						while (st.hasMoreTokens()) {
							String next = st.nextToken();
							list.add(next.trim());
						}
						si.pkgNames = new String[list.size()];
						list.toArray(si.pkgNames);
					}
				} else if (args[i].equals("-v")) {
					verbose = true;
				} else if (args[i].equals("-o")) {
					filename = args[++i];
					if (verbose)
						System.err.println("# will save output to " + filename);
				} else if (args[i].equals("-s")) {
					si.skipList.add(args[++i]);
					if (verbose)
						System.err.println("# adding to skip list: " + args[i]);
				} else if (args[i].equals("-m")) {
					si.mustList.add(args[++i]);
					if (verbose)
						System.err.println("# adding to must list: " + args[i]);
				} else if (args[i].endsWith(".jar") || args[i].endsWith(".zip"))
					si.addZipDepend(args[i]);
				else if (file.exists() && file.isDirectory())
					si.addDirDepend(args[i]);
				else if (file.exists() && file.isFile())
					si.addClassDepend(si.file2class(args[i]));
				else
					si.addClassDepend(args[i]);
			}
			if (filename == null)
				si.printDotSyntax(System.out);
			else
				si.printDotSyntax(filename);
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// //////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	private boolean acceptClass(String name) {
		// if (processed.indexOf(name) != -1) return false;
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
		for (int j = 0; j < pkgNames.length; j++) {
			if (name.startsWith(pkgNames[j]))
				return true;
		}
		return false;
	}

	private boolean acceptClass(Class<?> originaltype, String name) {
		// System.out.println("acceptClass(" + originaltype + "," + name + ")");
		if (name.startsWith("java.lang.") && name.indexOf('.', 10) == -1)
			return false;
		// inner or nested class
		if (name.startsWith(originaltype.getName()))
			return false;
		// same package
		if (originaltype.getPackage() != null
				&& name.startsWith(originaltype.getPackage().getName())
				&& name.indexOf('.', originaltype.getPackage().getName()
						.length() + 2) == -1)
			return false;

		// skip anonymous classes
		int n = name.lastIndexOf('$');
		if (n != -1 && Character.isDigit(name.charAt(n + 1))) {
			return false;
		}
		for (int i = 0; i < SUN_CLASSES.length; i++) {
			if (name.equals(SUN_CLASSES[i]))
				return false;
		}
		return true;
	}

	private Map<String, String[]> dependencies = new HashMap<String, String[]>();
	private boolean checkSM = true;
	private List<String> skipList = new ArrayList<String>();
	private List<String> mustList = new ArrayList<String>();
	private static boolean verbose = false;
	private boolean packageOnly;
	private String[] pkgNames;
	private String[] disallowedPackages;
}
