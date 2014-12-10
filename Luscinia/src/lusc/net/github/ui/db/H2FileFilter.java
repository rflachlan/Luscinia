package lusc.net.github.ui.db;
//
//  HSQLDBFileFilter.java
//  Luscinia
//
//  Created by Robert Lachlan on 2/18/08.
//  Copyright 2008 __MyCompanyName__. All rights reserved.
//

import java.io.File;
import javax.swing.*;
import javax.swing.filechooser.*;

public class H2FileFilter extends FileFilter {

	public final static String data = "luscdb";
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }

        String extension = getExtension(f);
        if (extension != null) {
            if (extension.equals(data))
			 {
                    return true;
            } else {
                return false;
            }
        }

        return false;
    }

    //The description of this filter
    public String getDescription() {
        return "Luscinia H2 database";
    }
	
	public static String getExtension(File f){
		String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 &&  i < s.length() - 1) {
            ext = s.substring(i+1).toLowerCase();
        }
        return ext;
	}
}
