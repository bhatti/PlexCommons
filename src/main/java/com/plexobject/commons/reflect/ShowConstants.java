/**
 * <B>CLASS COMMENTS</B>
 * Class Name: ShowConstants
 * Class Description: 
 *   ShowConstants shows import statements for a class
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

public class ShowConstants {
  public static void main(String[] args) {
    try {
      for (int i=0; i<args.length; i++) {
         System.out.println("---- " + args[i] + " -----");
        String filepath = null;
        if (args[i].endsWith(".class")) filepath = args[i];
        else filepath = TypesExtractor.getPath(args[i]);
        if (filepath == null) return;
        ClassParser parser = new ClassParser();
        ClassParser.ClassFile[] classInfo = parser.process(filepath);
				String[] consts = ClassParser.getConstants(classInfo);
        for (int j=0; j<consts.length; j++) {
          System.out.println(consts[j]);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
