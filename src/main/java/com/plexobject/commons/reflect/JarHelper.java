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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class JarHelper {
  public static InputStream getResource(File jar, String name) 
	throws IOException {
    FileInputStream fis=new FileInputStream(jar);
    BufferedInputStream bis=new BufferedInputStream(fis);
    ZipInputStream zis=new ZipInputStream(bis);
    ZipEntry ze=null;
    while ((ze=zis.getNextEntry())!=null) {
      if (ze.getName().equals(name)) {
        int size=(int)ze.getSize();
        if (size==-1) size = 8192;
	ByteArrayOutputStream bout = new ByteArrayOutputStream(size);
	int c;
	while ((c=zis.read()) != -1) bout.write(c);
        fis.close();
        bis.close();
        zis.close();
        return new ByteArrayInputStream(bout.toByteArray());
      }
    }
    fis.close();
    bis.close();
    zis.close();
    return null;
  }
}
