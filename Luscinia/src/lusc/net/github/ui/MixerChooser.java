package lusc.net.github.ui;
//
//  MixerChooser.java
//  Luscinia
//
//  Created by Robert Lachlan on 3/5/10.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

import javax.sound.sampled.*;
import java.util.*;

public class MixerChooser {

	
	
	
	public Mixer.Info[] getMixerInfo(DataLine.Info info){
		
		Mixer.Info[] mixers=AudioSystem.getMixerInfo();
		
		
		Line.Info[] lines=AudioSystem.getSourceLineInfo(info);
		
		
		for (int i=0; i<lines.length; i++){
			System.out.println("Line "+(i+1)+": "+lines.toString());
		}
		
		
		for (int i=0; i<mixers.length; i++){
			Mixer mixer=AudioSystem.getMixer(mixers[i]);
			
			if (mixer.isLineSupported(info)){
				System.out.println(mixers[i].getName());
			}
			
		}
		
		
		
		
		return mixers;
	}
	
	
}
