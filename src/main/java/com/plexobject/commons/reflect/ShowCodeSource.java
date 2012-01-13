/**
 * <B>CLASS COMMENTS</B>
 * Class Name: ShowCodeSource
 * Class Description: 
 *   ShowCodeSource shows directory where class is loaded
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


public class ShowCodeSource {
	public static void main(String[] args) {
		for (int i = 0; i < args.length; i++) {
			try {
				Class<?> type = Class.forName(args[i]);
				java.security.CodeSource cs = type.getProtectionDomain()
						.getCodeSource();
				if (cs.getLocation() != null) {
					System.out.println(type.getName() + " loaded from "
							+ cs.getLocation().toExternalForm());
				} else {
					System.out.println("Unable to find code source for "
							+ type.getName());
				}
			} catch (Exception e) {
				System.out.println("Unable to find code source for " + args[i]
						+ ": " + e);
			}
		}
	}
}
