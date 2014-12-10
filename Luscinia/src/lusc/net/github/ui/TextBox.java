package lusc.net.github.ui;
//
//  TextBox.java
//  Luscinia
//
//  Created by Robert Lachlan on 1/3/07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//

import java.awt.*;
import javax.swing.*;


public class TextBox extends JPanel{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2615964394673090487L;
	JTextField name=new JTextField(25);

	public TextBox(){

		this.setLayout(new GridLayout(0,1,10,10));
		JPanel nameP=new JPanel();
		nameP.setLayout(new BoxLayout(nameP, BoxLayout.LINE_AXIS));
		nameP.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
		nameP.add(Box.createHorizontalGlue());
		nameP.add(name);

		this.add(nameP);
	}
	
	String getTextBoxContents(){
		return(name.getText());
	}
	
}
