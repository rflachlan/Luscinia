package lusc.net.github;
//
//  SaveImage.java
//  Luscinia
//
//  Created by Robert Lachlan on 01/10/2005.
//  Copyright 2005 Robert Lachlan. All rights reserved.
//	This program is provided under the GPL 2.0 software licence. Please see accompanying material for details.

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.*;
import java.awt.event.*;
import java.awt.Toolkit;
import javax.imageio.*;
import javax.imageio.stream.*;
import javax.imageio.metadata.*;
import java.io.*;
import java.util.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.text.*;

public class SaveImage extends JPanel implements PropertyChangeListener, ActionListener{
	String thpath, name;
	File file;
	DisplayPane panel;
	Defaults defaults;
	int dpi=Toolkit.getDefaultToolkit().getScreenResolution();
	double dpcm=dpi/2.54;
	
	String[] unitNames={"pixel", "cm", "inch"};
	JComboBox units=new JComboBox(unitNames);
	
	String[] formatNames;
	JComboBox formats;
	
	JFormattedTextField xSize, ySize, resolution;
	JSlider compressionSlider=new JSlider();
	JLabel xlab=new JLabel("X dimension");
	JLabel ylab=new JLabel("Y dimension");
	JLabel rlab=new JLabel("Resolution (dpi)");
	JLabel ulab=new JLabel("Units");
	JLabel flab=new JLabel("Image Formats");
	JLabel clab=new JLabel("Image Quality");
	
	int nx, ny, ux, uy;
	double ratio;
	boolean fixed=true;
	boolean canCompress=true;
	double dpiUser=dpi;
	double dpcmUser=dpcm;
	int compression=50;
	NumberFormat num;
	JButton saveButton=new JButton("Save");
	
	JFrame frame=new JFrame();
	
	
	public SaveImage(BufferedImage imf, SpectrPane s, Defaults defaults){
		this.panel=s;
		this.defaults=defaults;
		setUp(imf);
	}
	
	public SaveImage(BufferedImage imf, DendrogramPanel d, Defaults defaults){
		this.panel=d;
		this.defaults=defaults;
		setUp(imf);
	}
	

	public SaveImage(BufferedImage imf, DisplayPane panel, Defaults defaults){
		
		this.panel=panel;
		this.defaults=defaults;
		setUp(imf);
	}
		
	public void setUp(BufferedImage imf){
		this.setLayout(new BorderLayout());
		
		
		nx=imf.getWidth();
		ny=imf.getHeight();
		
		ux=nx;
		uy=ny;
		
		ratio=nx/(ny+0.0);
		
		num=NumberFormat.getNumberInstance();
		num.setMaximumFractionDigits(10);
		Font font=new Font("Sans-Serif", Font.PLAIN, 9);

		
		
		ylab.setLabelFor(ySize);
		ySize=new JFormattedTextField(num);
		ySize.setColumns(6);
		ySize.addPropertyChangeListener("value", this);
		ySize.setValue(new Double(ny));
		ySize.setFont(font);
		
		xlab.setLabelFor(xSize);
		xSize=new JFormattedTextField(num);
		xSize.setColumns(6);
		xSize.addPropertyChangeListener("value", this);
		xSize.setValue(new Double(nx));
		xSize.setFont(font);
		
		rlab.setLabelFor(ySize);
		
		dpiUser=defaults.getDoubleProperty("sidpi", 1);
		if (dpiUser==0){dpiUser=300;}
		
		resolution=new JFormattedTextField(num);
		resolution.setColumns(6);
		resolution.addPropertyChangeListener("value", this);
		resolution.setValue(new Double(dpiUser));
		resolution.setFont(font);
		
		units.setSelectedIndex(defaults.getIntProperty("siunits"));
		
		units.addActionListener(this);
								
		JPanel resizePanel=new JPanel(new GridLayout(4,2));
		resizePanel.add(xlab);
		resizePanel.add(xSize);
		resizePanel.add(ylab);
		resizePanel.add(ySize);
		resizePanel.add(rlab);
		resizePanel.add(resolution);
		resizePanel.add(ulab);
		resizePanel.add(units);
		
		this.add(resizePanel, BorderLayout.NORTH);
		
		fixed=false;
		
		
		formatNames=getFormats();
		formats=new JComboBox(formatNames);
		
		int formatDefault=defaults.getIntProperty("sifnam");
		if (formatDefault<formatNames.length){
			formats.setSelectedIndex(formatDefault);
		}
		
		formats.addActionListener(this);
		
		compression=defaults.getIntProperty("sicomp");
		
		compressionSlider=new JSlider(0, 100, compression);
		
		checkCompression(formatNames[formats.getSelectedIndex()]);
		
		JPanel formatsPanel=new JPanel(new GridLayout(2,2));
		formatsPanel.add(flab);
		formatsPanel.add(formats);
		formatsPanel.add(clab);
		formatsPanel.add(compressionSlider);
		
		this.add(formatsPanel, BorderLayout.CENTER);
				
		saveButton.addActionListener(this);
		this.add(saveButton, BorderLayout.SOUTH);

		
		frame.setContentPane(this);
        frame.pack();
        frame.setVisible(true);
		
		
		//im=new BufferedImage(nx, ny, BufferedImage.TYPE_INT_RGB);
		//Graphics2D h=im.createGraphics();
		//h.drawImage(imf, 0,0, panel);
		//h.dispose();
		//imf=null;
	}
	
	public int transformToPixel(double p){
		int q=units.getSelectedIndex();
		int r=0;
		if (q==0){
			r=(int)Math.round(p);
		}
		else if (q==1){
			r=(int)Math.round(p*dpcmUser);
		}
		else{
			r=(int)Math.round(p*dpiUser);
		}
		return r;
	}
	
	public double transformToSelectedUnit(int p){
		int q=units.getSelectedIndex();
		double r=0;
		if (q==0){
			r=p;
		}
		else if (q==1){
			r=p/dpcmUser;
		}
		else{
			r=p/dpiUser;
		}
		return r;
	}
	
	public String[] getFormats() {
        String[] formats = ImageIO.getWriterFormatNames();
        TreeSet<String> formatSet = new TreeSet<String>();
        boolean jpgcounter=false;
        for (String s : formats) {
        	if ((!s.startsWith("wbmp"))&&(!s.startsWith("WBMP"))){
        		if ((s.startsWith("jp"))||(s.startsWith("JP"))){
        			if (!jpgcounter){
        				formatSet.add(s.toLowerCase());
        				jpgcounter=true;
        			}
        		}
        		else{
        			formatSet.add(s.toLowerCase());
        		}
        	}
        }
        
        return formatSet.toArray(new String[0]);
    }	
	
	public void updateSize(Object source){
		if ((source==xSize)||(source==null)){
			double q=((Number)xSize.getValue()).doubleValue();
			ux=transformToPixel(q);
			uy=(int)Math.round(ux/ratio);
			double r=transformToSelectedUnit(uy);
			ySize.setValue(new Double(r));
		}
		if ((source==ySize)||(source==null)){
			double q=((Number)ySize.getValue()).doubleValue();
			uy=transformToPixel(q);
			ux=(int)Math.round(uy*ratio);
			double r=transformToSelectedUnit(ux);
			xSize.setValue(new Double(r));
		}
		if ((source==resolution)||(source==null)){
			double q=((Number)resolution.getValue()).doubleValue();
			int r=units.getSelectedIndex();
			if (r==1){
				dpcmUser=q;
				dpiUser=q*2.54;
			}
			else{
				dpiUser=q;
				dpcmUser=q/2.54;
			}
			
			double s=transformToSelectedUnit(ux);
			xSize.setValue(new Double(s));
			double t=transformToSelectedUnit(uy);
			ySize.setValue(new Double(t));
		}
	}
	
	
	public void propertyChange(PropertyChangeEvent e) {
        Object source = e.getSource();
		if (!fixed){
			fixed=true;
			updateSize(source);
			fixed=false;
		}
	}
		
	public void actionPerformed(ActionEvent e){	
		Object source=e.getSource();
		if (source==units){
			double s=transformToSelectedUnit(ux);
			double t=transformToSelectedUnit(uy);
			fixed=true;
			xSize.setValue(new Double(s));
			ySize.setValue(new Double(t));
			int r=units.getSelectedIndex();
			if (r==1){
				resolution.setValue(new Double(dpcmUser));
				rlab.setText("Resolution (dpcm)");
			}
			else{
				resolution.setValue(new Double(dpiUser));
				rlab.setText("Resolution (dpi)");
			}
			fixed=false;
		}
		else if (source==formats){
			String s=formatNames[formats.getSelectedIndex()];
			checkCompression(s);
		}
		else if (source==saveButton){
			BufferedImage imf=panel.resizeImage(ux/(nx+0.0));
			writeDefaults();
			boolean succeeded=save(imf);
			if (succeeded){
				frame.dispose();
			}
		}
			
			
			
	}
	
	private void writeDefaults(){
		defaults.setIntProperty("sinorm", compression);
		defaults.setIntProperty("sifnam", formats.getSelectedIndex());
		defaults.setIntProperty("siunits", units.getSelectedIndex());
		defaults.setDoubleProperty("sidpi", dpiUser, 1);
		defaults.writeProperties();
	}
		
	public void checkCompression(String s){
		Iterator writers = ImageIO.getImageWritersByFormatName(s);
		ImageWriter writer = (ImageWriter)writers.next();
		ImageWriteParam iwp=writer.getDefaultWriteParam();
		canCompress=iwp.canWriteCompressed();
		compressionSlider.setEnabled(canCompress);
	}
	
	private void setDPI(IIOMetadata metadata, int type) throws IIOInvalidTreeException {
		
		double q=10/dpcmUser;
		if (type==1){		//javas png bug for setting resolution
			q=dpcmUser*0.1;
		}
		if (type==2){		//java's jpg bug for setting resolution
			q=0.1/dpcmUser;
		}
		
		System.out.println("Setting dpi! "+type+" "+dpcmUser+" "+q);
	    

	    IIOMetadataNode horiz = new IIOMetadataNode("HorizontalPixelSize");
	    horiz.setAttribute("value", Double.toString(q));

	    IIOMetadataNode vert = new IIOMetadataNode("VerticalPixelSize");
	    vert.setAttribute("value", Double.toString(q));

	    IIOMetadataNode dim = new IIOMetadataNode("Dimension");
	    dim.appendChild(horiz);
	    dim.appendChild(vert);

	    IIOMetadataNode root = new IIOMetadataNode("javax_imageio_1.0");
	    root.appendChild(dim);

	    metadata.mergeTree("javax_imageio_1.0", root);
	 }
	
	public boolean save(BufferedImage imf){
		nx=imf.getWidth();
		ny=imf.getHeight();
		System.out.println("DIMS: "+nx+" "+ny);
		updateSize(null);
		JFileChooser fc=new JFileChooser();
		thpath=defaults.props.getProperty("path");
		
		
		if (thpath!=null){
			try{
				fc=new JFileChooser(thpath);
			}
			catch(Exception e){
				e.printStackTrace();
				fc=new JFileChooser();
			}
		}
		else{fc=new JFileChooser();}
		fc.setDialogType(JFileChooser.SAVE_DIALOG);
		fc.setAcceptAllFileFilterUsed(false);
		
		int returnVal = fc.showSaveDialog(panel);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			String s=formatNames[formats.getSelectedIndex()];
			file = fc.getSelectedFile();
			thpath=file.getPath();
			file=new File(thpath+"."+s);
			int cont=0;
			if (file.exists()){
				cont= JOptionPane.showConfirmDialog(panel,"Do you really want to overwrite this file?\n"+"(It will be deleted permanently)","Confirm Overwrite", JOptionPane.YES_NO_OPTION);
			}
			if (cont==0){
				thpath=file.getParent();
				name=file.getName();
				defaults.props.setProperty("path", thpath);
				defaults.props.setProperty("filename", name);
		
				try{
					Iterator writers = ImageIO.getImageWritersByFormatName(s);
					ImageWriter writer = (ImageWriter)writers.next();
					ImageWriteParam iwp=writer.getDefaultWriteParam();
					
					if (canCompress){
						compression=compressionSlider.getValue();
						float comp=compression/100f;
						//iwp.setCompressionMode(ImageWriteParam.MODE_DEFAULT);
						
						iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
						String compType=iwp.getCompressionType();
						iwp.setCompressionType(compType);
						if (s.startsWith("jp")){
							iwp.setCompressionQuality(comp);
						}
					}
					
					BufferedImage bufferedImage = new BufferedImage(imf.getWidth(null), imf.getHeight(null), BufferedImage.TYPE_INT_RGB);
					Graphics g = bufferedImage.createGraphics();
					g.drawImage(imf, 0, 0, bufferedImage.getWidth(), bufferedImage.getHeight(), Color.WHITE, null);
					
					ImageTypeSpecifier typeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_RGB);
					IIOMetadata metadata = writer.getDefaultImageMetadata(typeSpecifier, iwp);
				    if (!metadata.isReadOnly() && metadata.isStandardMetadataFormatSupported()) {
				    	if (s.startsWith("png")){
				    		setDPI(metadata,1);
				    	}
				    	else if (s.startsWith("jp")){
				    		setDPI(metadata,2);
				    	}
				    	else{
				    		setDPI(metadata,0);
				    	}
				    }
					
				       
				       
					ImageOutputStream ios = ImageIO.createImageOutputStream(file);
					writer.setOutput(ios);
					IIOImage im1=new IIOImage(bufferedImage, null, metadata);
					writer.write(metadata, im1, iwp);
					return(true);
				}
				catch(IOException e){
					e.printStackTrace();
				}
			}
		}
		return false;
	}
	
}
