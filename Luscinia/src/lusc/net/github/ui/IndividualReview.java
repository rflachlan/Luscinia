package lusc.net.github.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedList;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import lusc.net.github.Defaults;
import lusc.net.github.Song;
import lusc.net.github.db.DataBaseController;
import lusc.net.github.ui.spectrogram.GuidePanel;
import lusc.net.github.ui.spectrogram.SpectrPane;

public class IndividualReview implements KeyListener, WindowListener, ActionListener{
	
	int[] d;
	int n=0;
	LinkedList<SpectrogramContainer> spectrogramList=new LinkedList<SpectrogramContainer>();
	LinkedList<SpectrogramContainer> exampleList=new LinkedList<SpectrogramContainer>();
	
	JPanel mainPanel=new JPanel();
	JPanel statusPanel=new JPanel();
	JPanel topPanel=new JPanel();
	JPanel bottomPanel=new JPanel();
	JLabel statusLabel=new JLabel(" ");
	JTextField tbq, tbt, bbq, bbt;
	
	int toploc=0;
	int bottomloc=0;
	DataBaseController dbc;
	Defaults defaults;
	boolean pressable=true;
	Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
	
	public IndividualReview(int[] d, Defaults defaults, DataBaseController dbc){
		this.d=d;
		this.dbc=dbc;
		this.defaults=defaults;
		n=d.length;
		
		dim.setSize(dim.getWidth()-50, dim.getHeight()/2);
		
		for (int i=0; i<n; i++){
			SpectrogramContainer sc=new SpectrogramContainer(d[i]);
			
			spectrogramList.add(sc);
			
		}
		mainPanel.addKeyListener(this);
		setTop();
		
		statusPanel=new JPanel (new BorderLayout());
		JButton saveButton=new JButton("Save results");
		saveButton.addActionListener(this);
		makeStatusLabel();
		statusPanel.add(saveButton, BorderLayout.WEST);
		statusPanel.add(statusLabel, BorderLayout.CENTER);
		
		mainPanel.add(statusPanel, BorderLayout.SOUTH);
		
		JFrame f=new JFrame();
		f.setPreferredSize(dim);
		f.requestFocus();
		f.getContentPane().add(mainPanel);
		f.pack();
		f.setVisible(true);
		f.addWindowListener(this);
	
	}

	
	public void makeStatusLabel(){
		String s=" ";
		SpectrogramContainer sc=spectrogramList.get(toploc);
		String name1=sc.song.getName();
		if (exampleList.size()>0){
			SpectrogramContainer sc2=exampleList.get(bottomloc);
			String name2=sc2.song.getName();
			s="Song "+name1+", "+(toploc+1)+" of "+(spectrogramList.size())+". Example "+name2+", "+(bottomloc+1)+" of "+(exampleList.size());
			
		}
		else{
			s="Song "+name1+", "+(toploc+1)+" of "+(spectrogramList.size())+". No examples yet";
		}
		statusLabel.setText(s);
	}


	public void setTop(){
		
		mainPanel.removeAll();
		mainPanel.addKeyListener(this);
		mainPanel.setFocusable(true);
		mainPanel.setLayout(new BorderLayout());
		topPanel=new JPanel(new BorderLayout());
		SpectrogramContainer sc=spectrogramList.get(toploc);
		
		if (!sc.drawn){
			sc.loadSong();
			sc.makeSpectrogram();
			sc.gp.addKeyListener(this);
		}
		
		topPanel.add(sc.gp, BorderLayout.EAST);
		JPanel detailsPanel=getTopDetails(sc.song);
		topPanel.add(detailsPanel, BorderLayout.WEST);
		
		makeStatusLabel();
		
		int height=bottomPanel.getHeight();
		int width=bottomPanel.getWidth();
		if (topPanel.getHeight()>height){height=topPanel.getHeight();}
		if (topPanel.getWidth()>width){width=topPanel.getWidth();}
		
		mainPanel.setPreferredSize(new Dimension(width, 2*height));
		
		mainPanel.add(topPanel, BorderLayout.NORTH);
		mainPanel.add(bottomPanel, BorderLayout.CENTER);
		mainPanel.add(statusPanel, BorderLayout.SOUTH);
		mainPanel.revalidate();
		statusPanel.revalidate();
		mainPanel.repaint();

	}
	
	public void setBottom(){
		makeStatusLabel();
		mainPanel.removeAll();
		bottomPanel=new JPanel(new BorderLayout());
		SpectrogramContainer sc=exampleList.get(bottomloc);
		
		bottomPanel.add(sc.gp, BorderLayout.EAST);
		JPanel detailsPanel=getBottomDetails(sc.song);
		bottomPanel.add(detailsPanel, BorderLayout.WEST);
		
		int height=bottomPanel.getHeight();
		int width=bottomPanel.getWidth();
		if (topPanel.getHeight()>height){height=topPanel.getHeight();}
		if (topPanel.getWidth()>width){width=topPanel.getWidth();}
		
		mainPanel.add(topPanel, BorderLayout.NORTH);
		mainPanel.add(bottomPanel, BorderLayout.CENTER);
		mainPanel.add(statusPanel, BorderLayout.SOUTH);
		mainPanel.setPreferredSize(new Dimension(width, 2*height));
		statusPanel.revalidate();
		mainPanel.revalidate();
		mainPanel.repaint();
	}
	
	public JPanel getTopDetails(Song song){
		String quality=song.getQuality();
		String type=song.getType();
		JPanel p=new JPanel(new GridLayout(0,1));
		JLabel qlab=new JLabel("Quality:");
		JLabel tlab=new JLabel("Type:");
		tbq=new JTextField(quality);
		tbq.setColumns(5);
		tbt=new JTextField(type);
		tbt.setColumns(5);
		JPanel qpan=new JPanel(new BorderLayout());
		qpan.add(qlab, BorderLayout.WEST);
		qpan.add(tbq, BorderLayout.CENTER);
		JPanel tpan=new JPanel(new BorderLayout());
		tpan.add(tlab, BorderLayout.WEST);
		tpan.add(tbt, BorderLayout.CENTER);
		p.add(qpan);
		p.add(tpan);
		return p;
	}
	
	public JPanel getBottomDetails(Song song){
		String quality=song.getQuality();
		String type=song.getType();
		JPanel p=new JPanel(new GridLayout(0,1));
		JLabel qlab=new JLabel("Quality:");
		JLabel tlab=new JLabel("Type:");
		bbq=new JTextField(quality);
		bbq.setColumns(5);
		bbt=new JTextField(type);
		bbt.setColumns(5);
		JPanel qpan=new JPanel(new BorderLayout());
		qpan.add(qlab, BorderLayout.WEST);
		qpan.add(bbq, BorderLayout.CENTER);
		JPanel tpan=new JPanel(new BorderLayout());
		tpan.add(tlab, BorderLayout.WEST);
		tpan.add(bbt, BorderLayout.CENTER);
		p.add(qpan);
		p.add(tpan);
		return p;
	}
	
	
	public void keyMessage(KeyEvent e){
		
		int keyCode=e.getKeyCode();
		System.out.println("here: "+keyCode);
		if(keyCode==KeyEvent.VK_UP){
			SpectrogramContainer sc=spectrogramList.get(toploc);
			sc.song.setQuality(tbq.getText());
			sc.song.setType(tbt.getText());
			toploc--;
			if (toploc<0){toploc=spectrogramList.size()-1;}
			setTop();
		}
		else if (keyCode==KeyEvent.VK_DOWN){
			SpectrogramContainer sc=spectrogramList.get(toploc);
			sc.song.setQuality(tbq.getText());
			sc.song.setType(tbt.getText());
			toploc++;
			if (toploc>=spectrogramList.size()){toploc=0;}
			setTop();
		}
		else if (keyCode==KeyEvent.VK_LEFT){
			if (exampleList.size()>0){
				SpectrogramContainer sc=exampleList.get(bottomloc);
				sc.song.setQuality(bbq.getText());
				sc.song.setType(bbt.getText());
				bottomloc--;
				if (bottomloc<0){bottomloc=exampleList.size()-1;}
				setBottom();
			}
		}
		else if (keyCode==KeyEvent.VK_RIGHT){
			if (exampleList.size()>0){
				SpectrogramContainer sc=exampleList.get(bottomloc);
				sc.song.setQuality(bbq.getText());
				sc.song.setType(bbt.getText());
				bottomloc++;
				if (bottomloc>=exampleList.size()){bottomloc=0;}
				setBottom();
			}
		}
		else if (keyCode==KeyEvent.VK_R){
			SpectrogramContainer sc=spectrogramList.get(toploc);
			sc.song.setQuality(tbq.getText());
			sc.song.setType(tbt.getText());
			exampleList.remove(bottomloc);
			SpectrogramContainer sc2=new SpectrogramContainer(sc);
			exampleList.add(bottomloc, sc2);
			setBottom();
		}
		else if (keyCode==KeyEvent.VK_A){
			SpectrogramContainer sc=spectrogramList.get(toploc);
			sc.song.setQuality(tbq.getText());
			sc.song.setType(tbt.getText());
			SpectrogramContainer sc2=new SpectrogramContainer(sc);
			exampleList.add(sc2);
			bottomloc=exampleList.size()-1;
			setBottom();
		}
	}
	
	@Override
	public void keyTyped(KeyEvent e) {
		//System.out.println("KEY TYPED :"+e.getKeyChar());
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (pressable){
			keyMessage(e);
			pressable=false;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		pressable=true;
	}
	
	public void windowClosing(WindowEvent e){
		cleanUp();
	}
	public void windowClosed(WindowEvent e){
		cleanUp();
	}
	public void windowActivated(WindowEvent e){}
	public void windowDeactivated(WindowEvent e){}
	public void windowIconified(WindowEvent e){}
	public void windowDeiconified(WindowEvent e){}
	public void windowOpened(WindowEvent e){}
	
	
	public void save(){
		for (SpectrogramContainer sc : spectrogramList){
			if (sc.song!=null){
				Song song=sc.song;
				System.out.println(song.getSongID());
				dbc.writeSongInfo(song);	
			}
		}
	}
	
	public void cleanUp(){
		System.out.println("Cleaning Up");
		save();
		spectrogramList=null;
		exampleList=null;
		System.gc();
	}
	
	class SpectrogramContainer{
		
		int id=0;
		Song song=null;
		JButton gp=null;
		boolean drawn=false;
		
		public SpectrogramContainer(int id){
			this.id=id;
		}
		
		public SpectrogramContainer(SpectrogramContainer sc){
			this.id=sc.id;
			song=sc.song;
			makeSpectrogram();
		}
		
		public void loadSong(){
			song=dbc.loadSongFromDatabase(id, 0);
			//song.setSongID(id);
		}
		
		public void makeSpectrogram(){
			//System.out.println(id+ " "+song.getName());
			if ((song.getMaxF()<=1)||(song.getDynRange()<1)){
				defaults.getSongParameters(song);
			}
			song.setTimeStep(1);
			
			
			
			song.setFFTParameters();
			SpectrPane sp=new SpectrPane(song, defaults, true);
			BufferedImage im=sp.getImf();
			
			double p1=im.getWidth()/(dim.getWidth()-200);
			
			double p2=im.getHeight()/(0.5*dim.getHeight()-100);
			System.out.println(p1+" "+p2);
			if (p2>p1){p1=p2;}
			
			int w=(int)Math.round(im.getWidth()/p1);
			int h=(int)Math.round(im.getHeight()/p1);
			System.out.println(w+" "+im.getWidth()+" "+h+" "+im.getHeight()+" "+p1);
			
			BufferedImage imn=new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g=imn.createGraphics();
			RenderingHints hints = new RenderingHints(
	                RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setRenderingHints(hints);
			
			g.drawImage(im, 0, 0, imn.getWidth(), imn.getHeight(), null);
			g.dispose();
			ImageIcon imic=new ImageIcon(imn);
			JLabel label=new JLabel(imic);
			
			gp=new JButton();
			gp.setIcon(imic);
			//gp.add(label);
			drawn=true;
			
		}
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		save();
	}
	
}
