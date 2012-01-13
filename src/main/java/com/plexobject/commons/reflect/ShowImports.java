/**
 * <B>CLASS COMMENTS</B>
 * Class Name: ShowImports
 * Class Description: 
 *   ShowImports shows import statements for a class
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ShowImports {
	public static final String[] SUN_CLASSES = { "boolean", "byte", "char",
			"float", "double", "int", "long", "short", "I", "B", "C", "F", "D",
			"J", "S", "V", "Z", "void" };

	public static void main(String[] args) {
		try {
			ShowImports si = new ShowImports();
			String[] imports;
			for (int i = 0; i < args.length; i++) {
				System.out.println("// Import statements for " + args[i]);
				imports = si.getImports(args[i]);
				for (int j = 0; j < imports.length; j++) {
					System.out.println("import " + imports[j] + ";");
				}
				System.out.println();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String[] getImports(final String name) {
		List<String> importlist = new ArrayList<String>();
		try {
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
		} catch (Exception e) {
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

	// //////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	private boolean acceptClass(Class<?> originaltype, String name) {
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
}
