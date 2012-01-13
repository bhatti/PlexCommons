/**
 * <B>CLASS COMMENTS</B>
 * Class Name: Jwhich
 * Class Description: 
 *   Jwhich shows directory where class is loaded
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

public class Jwhich {
	public static String which(String name) throws Exception {
		Class<?> type = Class.forName(name);
		java.security.CodeSource cs = type.getProtectionDomain()
				.getCodeSource();
		if (cs != null && cs.getLocation() != null) {
			String location = cs.getLocation().toExternalForm();
			if (location.endsWith(".jar")) {
				return cs.getLocation().toExternalForm();
			} else {
				return cs.getLocation().toExternalForm();
			}
		}
		return "";
	}

	public static void main(String[] args) {
		for (int i = 0; i < args.length; i++) {
			try {
				String loc = which(args[i]);
				System.out.println(loc);
			} catch (Exception e) {
				System.out.println("Unable to find code source for " + args[i]
						+ ": " + e);
				e.printStackTrace(System.out);
			}
		}
	}

}
