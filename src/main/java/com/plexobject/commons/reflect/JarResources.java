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

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * JarResources: JarResources maps all resources included in a Zip or Jar file.
 * Additionaly, it provides a method to extract one as a blob.
 */
public final class JarResources {
	// external debug flag
	public boolean debugOn = false;

	// jar resource mapping tables
	private Map<String, Integer> htSizes = new HashMap<String, Integer>();
	private Map<String, byte[]> htJarContents = new HashMap<String, byte[]>();

	// a jar file
	private String jarFileName;

	/**
	 * creates a JarResources. It extracts all resources from a Jar into an
	 * internal hashtable, keyed by resource names.
	 * 
	 * @param jarFileName
	 *            a jar or zip file
	 */
	public JarResources(String jarFileName) {
		this.jarFileName = jarFileName;
		init();
	}

	/**
	 * Extracts a jar resource as a blob.
	 * 
	 * @param name
	 *            a resource name.
	 */
	public byte[] getResource(String name) {
		return (byte[]) htJarContents.get(name);
	}

	public String[] getResourceNames() {
		String[] names = new String[htJarContents.size()];
		int i = 0;
		Iterator<String> it = htJarContents.keySet().iterator();
		while (it.hasNext()) {
			names[i++] = it.next();
		}
		return names;
	}

	/** initializes internal hash tables with Jar file resources. */
	private void init() {
		try {
			// extracts just sizes only.
			ZipFile zf = new ZipFile(jarFileName);
			Enumeration<? extends ZipEntry> e = zf.entries();
			while (e.hasMoreElements()) {
				ZipEntry ze = e.nextElement();
				htSizes.put(ze.getName(), new Integer((int) ze.getSize()));
			}
			zf.close();
			FileInputStream fis = new FileInputStream(jarFileName);
			BufferedInputStream bis = new BufferedInputStream(fis);
			ZipInputStream zis = new ZipInputStream(bis);
			ZipEntry ze = null;
			while ((ze = zis.getNextEntry()) != null) {
				if (ze.isDirectory())
					continue;
				int size = (int) ze.getSize();
				// -1 means unknown size.
				if (size == -1) {
					size = ((Integer) htSizes.get(ze.getName())).intValue();
				}

				byte[] b = new byte[(int) size];
				int rb = 0;
				int chunk = 0;
				while (((int) size - rb) > 0) {
					chunk = zis.read(b, rb, (int) size - rb);
					if (chunk == -1)
						break;
					rb += chunk;
				}

				// add to internal resource hashtable
				htJarContents.put(ze.getName(), b);
			} // while
		} catch (NullPointerException e) { // done}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Dumps a zip entry into a string.
	 * 
	 * @param ze
	 *            a ZipEntry
	 */
	String dumpZipEntry(ZipEntry ze) {
		StringBuffer sb = new StringBuffer();
		if (ze.isDirectory())
			sb.append("d ");
		else
			sb.append("f ");
		if (ze.getMethod() == ZipEntry.STORED)
			sb.append("stored   ");
		else
			sb.append("defalted ");
		sb.append(ze.getName());
		sb.append("\t");
		sb.append("" + ze.getSize());
		if (ze.getMethod() == ZipEntry.DEFLATED)
			sb.append("/" + ze.getCompressedSize());
		return (sb.toString());
	}

	/**
	 * Is a test driver. Given a jar file and a resource name, it trys to
	 * extract the resource and then tells us whether it could or not.
	 * 
	 * <strong>Example</strong> Let's say you have a JAR file which jarred up a
	 * bunch of gif image files. Now, by using JarResources, you could extract,
	 * create, and display those images on-the-fly.
	 * 
	 * <pre>
	 *     ...
	 *     JarResources JR=new JarResources("GifBundle.jar");
	 *     Image image=Toolkit.createImage(JR.getResource("logo.gif");
	 *     Image logo=Toolkit.getDefaultToolkit().createImage(
	 *                   JR.getResources("logo.gif")
	 *                   );
	 *     ...
	 * </pre>
	 */
	public static void main(String[] args) throws IOException {
		if (args.length != 2) {
			System.err
					.println("usage: java JarResources <jar file name> <resource name>");
			System.exit(1);
		}

		JarResources jr = new JarResources(args[0]);
		byte[] buff = jr.getResource(args[1]);
		if (buff == null) {
			System.out.println("Could not find " + args[1] + ".");
		} else {
			System.out.println("Found " + args[1] + " (length=" + buff.length
					+ ").");
		}
	}
} // End of JarResources class.
