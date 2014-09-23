package lusc.net.github;
//
//  AboutBox.java
//  Luscinia
//
//  Created by Robert Lachlan on 9/29/05.
//  Copyright 2005 Roebrt Lachlan. All rights reserved.
//	This program is provided under the GPL 2.0 software licence. Please see accompanying material for details.

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class AboutBox extends JFrame implements ActionListener {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected JLabel titleLabel, aboutLabel[];
    protected static int labelCount = 8;
    protected static int aboutWidth = 500;
    protected static int aboutHeight = 230;
    protected static int aboutTop = 200;
    protected static int aboutLeft = 350;
    protected Font titleFont, bodyFont;

    public AboutBox(String version) {
        super("");
        this.setResizable(false);
        SymWindow aSymWindow = new SymWindow();
        this.addWindowListener(aSymWindow);	
		
        // Initialize useful fonts
        titleFont = new Font("Lucida Grande", Font.BOLD, 14);
        if (titleFont == null) {
            titleFont = new Font("SansSerif", Font.BOLD, 14);
        }
        bodyFont  = new Font("Lucida Grande", Font.PLAIN, 10);
        if (bodyFont == null) {
            bodyFont = new Font("SansSerif", Font.PLAIN, 10);
        }
		
        this.getContentPane().setLayout(new BorderLayout(15, 15));
	
        aboutLabel = new JLabel[labelCount];
        aboutLabel[0] = new JLabel("");
        aboutLabel[1] = new JLabel("Luscinia");
        aboutLabel[1].setFont(titleFont);
        aboutLabel[2] = new JLabel("Version "+version);
        aboutLabel[2].setFont(bodyFont);
        aboutLabel[3] = new JLabel("");
        aboutLabel[4] = new JLabel("");
        aboutLabel[5] = new JLabel("JDK " + System.getProperty("java.version"));
        aboutLabel[5].setFont(bodyFont);
        aboutLabel[6] = new JLabel("Copyright Robert Lachlan 2007-12");
        aboutLabel[6].setFont(bodyFont);
        aboutLabel[7] = new JLabel("");		
		
        Panel textPanel2 = new Panel(new GridLayout(labelCount, 1));
        for (int i = 0; i<labelCount; i++) {
            aboutLabel[i].setHorizontalAlignment(JLabel.CENTER);
            textPanel2.add(aboutLabel[i]);
        }
        this.getContentPane().add (textPanel2, BorderLayout.CENTER);
        this.pack();
        this.setLocation(aboutLeft, aboutTop);
        this.setSize(aboutWidth, aboutHeight);
		this.setVisible(true);
    }

    class SymWindow extends java.awt.event.WindowAdapter {
	    public void windowClosing(java.awt.event.WindowEvent event) {
		    setVisible(false);
	    }
    }
    
    public void actionPerformed(ActionEvent newEvent) {
        setVisible(false);
    }		
}