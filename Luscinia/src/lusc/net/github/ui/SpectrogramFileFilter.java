package lusc.net.github.ui;
//
//  SpectrogramFileFilter.java
//  Luscinia
//
//  Created by Robert Lachlan on 01/10/2005.
//  Copyright 2005 __MyCompanyName__. All rights reserved.
//

//
//  SpectrogramFileFilter.java
//  SongDatabase
//
//  Created by Robert Lachlan on Thu Nov 11 2004.
//  Copyright 2005 Robert Lachlan. All rights reserved.
//	This program is provided under the GPL 2.0 software licence. Please see accompanying material for details.

import java.io.File;
import javax.swing.*;
import javax.swing.filechooser.*;

public class SpectrogramFileFilter extends FileFilter {
    //String[] names ={"wav", "aif", "aiff"};
	
	String names="wav";
	
	int fileType=0;
	
	public SpectrogramFileFilter(){
	}
	
	public SpectrogramFileFilter(String n){
		this.names=n;
	}
	
	public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }
        String extension = getExtension(f);
		boolean found=false;
        if (extension != null) {
			//for (int i=0; i<names.length; i++){	
			//	if (extension.equals(names[i])) {found=true;}
			//}
			if (extension.equals(names)) {found=true;}
		}
		return found;
    }

    public String getDescription() {
		
		//StringBuffer sb=new StringBuffer();
		
		//for (int i=0; i<names.length; i++){	
		//	sb.append(names[i]);
		//	if (i<names.length-1){
		//		sb.append(", ");
		//	}
		//}
        //return sb.toString();
		return names;
    }

    public static String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 &&  i < s.length() - 1) {
            ext = s.substring(i+1).toLowerCase();
        }
        return ext;
    }
}

