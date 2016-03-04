package lusc.net.github.ui.spectrogram;
//
//  MainPanel.java
//  Luscinia
//
//  Created by Robert Lachlan on 01/10/2005.
//  Copyright 2005 Robert Lachlan. All rights reserved.
//	This program is provided under the GPL 2.0 software licence. Please see accompanying material for details.

import javax.swing.*;
import javax.swing.event.*;

import java.awt.event.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.awt.*;
import java.text.*;
import java.util.*;

import lusc.net.github.Defaults;
import lusc.net.github.Element;
import lusc.net.github.Song;
import lusc.net.github.db.DataBaseController;
import lusc.net.github.ui.SaveImage;
import lusc.net.github.ui.db.DatabaseView;
import lusc.net.github.ui.SaveSound;
import lusc.net.github.ui.SyllableInduction;

public class MainPanel extends JPanel implements PropertyChangeListener, ChangeListener, ActionListener, WindowListener, KeyListener{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2730534628398136970L;
	SpectrPane s;
	GuidePanel gp;
	Defaults defaults;
	JScrollPane sp;
	JFrame f=new JFrame();
	JButton save=new JButton("Save to database");
	JButton saveSound=new JButton("Save sound");
	JButton saveImage=new JButton("Save image");
	JButton playAll=new JButton(" \u25B6 all ");
	JButton playScreen=new JButton("\u25B6 screen");
	JButton stop=new JButton("\u25FC  ");
	JButton redo=new JButton("Re-do");
	JButton undo=new JButton("Undo");
	JButton automatic=new JButton("Select All");
	JButton mode=new JButton("Mode: elements");
	JCheckBox erase=new JCheckBox("Erase");
	JButton update=new JButton("Update spectrograph");
	JButton defaultSettings=new JButton("Default settings");
	JButton saveDefault=new JButton("Set default");
	//JButton selectModel=new JButton("Select model");
	JButton previousDetails=new JButton("Use last saved details");
	JButton setMaxDyn=new JButton("Custom compression");
	JButton zoomTimeAll=new JButton("Zoom to fit all");
	JButton zoomTime100=new JButton("Zoom to 100%");
	JButton reestimate=new JButton("Re-measure");
	JButton reestAll=new JButton("Re-measure all");
	JButton displayMode=new JButton("Display mode: spectrogram");
	JButton forwardButton= new JButton(">");
	JButton backButton=new JButton("<");
	JButton fForwardButton= new JButton(">>");
	JButton fBackButton=new JButton("<<");
	
	JButton calculateSyllableStructure=new JButton("Calculate syllables automatically");
	
	boolean LISTEN_TO_DROP_DOWNS=true;
	
	private static String FORWARD_COMMAND = "forward";
	private static String BACKWARD_COMMAND = "backward";
	private static String F_FORWARD_COMMAND = "fast forward";
	private static String F_BACKWARD_COMMAND = "fast backward";
	private static String SAVE_COMMAND = "save";
	private static String SAVE_SOUND_COMMAND = "save sound";
	private static String SAVE_IMAGE_COMMAND = "save image";
	private static String PLAY_COMMAND = "play";
	private static String PLAY_SPEED_COMMAND = "play speed";
	private static String PLAY_ALL_COMMAND = "play all";
	private static String PLAY_SCREEN_COMMAND = "play screen";
	private static String STOP_COMMAND = "stop";
	private static String REDO_COMMAND = "redo";
	private static String UNDO_COMMAND = "undo";
	private static String AUTOMATIC_COMMAND = "automatic";
	private static String MODE_COMMAND = "mode";
	private static String ERASE_COMMAND = "erase";
	private static String UPDATE_COMMAND = "update";
	//private static String SELECT_COMMAND = "select";
	private static String DELETE_COMMAND = "delete";
	private static String DELETES_COMMAND = "deletes";
	private static String MERGE_COMMAND = "merge";
	private static String BRUSH_COMMAND = "brush";
	private static String SIGNAL_COMMAND = "signal";
	private static String PEAK_COMMAND = "peak";
	private static String FUND_COMMAND = "fund";
	private static String MEAN_COMMAND = "mean";
	private static String MEDIAN_COMMAND = "median";
	private static String WIENER_COMMAND = "wiener";
	private static String HARM_COMMAND = "harm";
	private static String AMPLITUDE_COMMAND = "amplitude";
	private static String PEAK_FREQ_CHANGE_COMMAND = "peak frequency change";
	private static String FUND_FREQ_CHANGE_COMMAND = "fundamental frequency change";
	private static String MEAN_FREQ_CHANGE_COMMAND = "mean frequency change";
	private static String MEDIAN_FREQ_CHANGE_COMMAND = "median frequency change";
	private static String BANDWIDTH_COMMAND = "bandwidth";
	private static String VIEW_ELEMENTS_COMMAND = "elements";
	private static String VIEW_SYLLABLES_COMMAND = "syllables";
	private static String VIEW_TRILL_AMP_COMMAND = "view trill amp";
	private static String VIEW_TRILL_RATE_COMMAND = "view trill rate";
	private static String VIEW_REVERB_COMMAND = "view reverb";
	private static String DEFAULT_COMMAND= "default settings";
	private static String SAVE_DEFAULT_COMMAND = "save defaults";
	private static String WINDOW_COMMAND = "window method";
	private static String PREVIOUS_COMMAND = "previous";
	private static String SET_TO_MAX_COMMAND = "set to max";
	private static String ZOOM_TO_ALL_COMMAND = "zoom to all";
	private static String ZOOM_TO_100_COMMAND = "zoom to 100";
	private static String REESTIMATE_COMMAND="reestimate";
	private static String REEST_ALL_COMMAND="reestimate all";
	private static String CALC_SYLL_COMMAND="recalculate syllables automatically";
	private static String DISPLAY_MODE="display mode";
	
	private static String TIME_UNIT_S="time unit s";
	private static String TIME_UNIT_MS="time unit ms";
	private static String FREQ_UNIT_HZ="freq unit Hz";
	private static String FREQ_UNIT_KHZ="freq unit kHz";
	private static String SHOW_FRAME="show frame";
	private static String INTERIOR_TICK_MARK="interior tick mark";
	private static String SHOW_MAJOR_TIME_TICK_MARK="show major time tick mark";
	private static String SHOW_MINOR_TIME_TICK_MARK="show minor time tick mark";
	private static String SHOW_MAJOR_FREQ_TICK_MARK="show major freq tick mark";
	private static String SHOW_MINOR_FREQ_TICK_MARK="show minor freq tick mark";
	private static String TICK_LABEL_FONT_STYLE="tick label font style";
	private static String AXIS_LABEL_FONT_STYLE="axis label font style";
	private static String FONT_FACE="font face";
	private static String CLICK_DRAG="click drag";
	
	String [] brushSizes={"2", "5", "10", "20", "50", "all", "custom"};
	int[] brushSizeInd={2, 5, 10, 20, 50};
	JComboBox brush=new JComboBox(brushSizes);
	
	String[] playbackSpeeds={"Full", "Half", "Quarter", "Eighth"};
	JComboBox pbSpeed=new JComboBox(playbackSpeeds);
	
	String [] windowMethods={"Gaussian", "Hamming", "Hann"};
	JComboBox windowMethod=new JComboBox(windowMethods);
	JComboBox delete, deleteS, merge, playSyllable;
	
	DefaultComboBoxModel mergeModel=new DefaultComboBoxModel();
	DefaultComboBoxModel playModel=new DefaultComboBoxModel();
	DefaultComboBoxModel deleteModel=new DefaultComboBoxModel();
	DefaultComboBoxModel deleteModelS=new DefaultComboBoxModel();
	
	Calendar cal=Calendar.getInstance();
	String []mo={"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
	JTextField equipmentField=new JTextField(40);
	JTextField recordistField=new JTextField(40);
	JTextField locationField=new JTextField(40);
	JTextArea notesField=new JTextArea();
	JFormattedTextField dynRange, dynM, echoAmt, dynEqual, echoRange, noiseAmt, noiseRange1, noiseRange2, harmonic, minGap, filterCutOff, minLength,
		maxFreq, frameLength, timeStep, spectPoints, spectOverlap, timeZoom, freqZoom, upperLoop, lowerLoop, gpMaxF, fundJumpSuppression;
	JSlider dynRangeSL, echoAmtSL, dynEqualSL, dynMSL, echoRangeSL, noiseAmtSL, noiseRange1SL, noiseRange2SL, filterCutOffSL, maxFreqSL, 
		frameLengthSL, timeStepSL, timeZoomSL, freqZoomSL, spectPointsSL, spectOverlapSL;
	JSpinner timeSpinner, dateSpinner;
	SpinnerDateModel timeModel, dateModel;
	
	
	JLabel brushS=new JLabel("Brush Size: ", JLabel.TRAILING);
	JLabel dynRangeL=new JLabel("Dynamic range (dB): ", JLabel.TRAILING);
	JLabel dynML=new JLabel("Dynamic comp. (%): ", JLabel.TRAILING);
	JLabel deleteLE=new JLabel(" Delete Element: ", JLabel.TRAILING);
	JLabel deleteLS=new JLabel("Delete Syllable: ", JLabel.TRAILING);
	JLabel mergeLE=new JLabel("Merge Elements: ", JLabel.TRAILING);
	JLabel echoAmtL=new JLabel("Dereverberation (%): ", JLabel.TRAILING);
	JLabel echoRangeL=new JLabel("Dereverb. range (ms): ", JLabel.TRAILING);
	
	JLabel noiseAmtL=new JLabel("Noise removal (dB): ", JLabel.TRAILING);
	JLabel noiseRange1L=new JLabel("NR range1 (ms): ", JLabel.TRAILING);
	JLabel noiseRange2L=new JLabel("NR range2 (ms): ", JLabel.TRAILING);
	
	JLabel dynamicEqualizationL=new JLabel("Dynamic equal. (ms): ", JLabel.TRAILING);
	
	JLabel minGapL=new JLabel("Min. gap (ms): ", JLabel.TRAILING);
	JLabel minLengthL=new JLabel("Min. length (ms): ", JLabel.TRAILING);
	JLabel filterCutOffL=new JLabel("High Pass threshold (Hz): ", JLabel.TRAILING); 
	JLabel playL=new JLabel("    Play syllable:  ", JLabel.TRAILING);
	JLabel playSpeedL=new JLabel("Playback speed:  ", JLabel.TRAILING);
	JLabel guidePanelL=new JLabel("Guide panel max. freq.:  ", JLabel.TRAILING);
	JLabel maxFreqL=new JLabel("Max. frequency (Hz): ", JLabel.TRAILING);
	JLabel frameLengthL=new JLabel("Frame length (ms): ", JLabel.TRAILING);
	JLabel spectPointsL=new JLabel("Spectrograph points: ", JLabel.TRAILING);
	JLabel spectOverlapL=new JLabel("Spect. overlap (%): ", JLabel.TRAILING);
	JLabel timeStepL=new JLabel("Time step (ms): ", JLabel.TRAILING);
	JLabel windowMethodL=new JLabel("       Windowing function: ");
	JLabel timeZoomL=new JLabel("Time zoom (%): ");
	JLabel freqZoomL=new JLabel("Frequency zoom (%): ");
	JLabel dateL=new JLabel("  Recording date (dd:mm:yyyy): ");
	JLabel timeL=new JLabel("Recording time (hh:mm:ss:mmm): ");
	JLabel locationL=new JLabel("    Recording location: ");
	JLabel notesL=new JLabel("Context and notes: ");
	JLabel equipmentL=new JLabel("Recording equipment: ");
	JLabel recordistL=new JLabel("Recordist: ");
	
	JLabel majorTickMarkLengthL=new JLabel("Length of major tick marks (px): ");
	JLabel minorTickMarkLengthL=new JLabel("Length of minor tick marks (px): ");
	JLabel lineWeightL=new JLabel("Line weight (pt): ");
	JLabel axisLabelL=new JLabel("   Axis label: ");
	JLabel fontSizeL=new JLabel("Font size: ");
	JLabel tickLabelL=new JLabel("   Tick label: ");
	JLabel fontStyleL=new JLabel("Font style: ");
	JLabel fontFaceL=new JLabel("Font face: ");
	JLabel timeUnitL=new JLabel("Time unit: ");
	JLabel freqUnitL=new JLabel("Frequency unit: ");
	
	JLabel lowerLoopL=new JLabel("Lower hysteresis cutoff (dB): ");
	JLabel upperLoopL=new JLabel("Upper hysteresis cutoff (dB): ");
	JLabel harmonicL=new JLabel("FF bias: ", JLabel.TRAILING);
	JLabel jumpSuppressionL=new JLabel("FF jump suppression: ", JLabel.TRAILING);
	
	
	JRadioButton timeUnitS=new JRadioButton("s");
	JRadioButton timeUnitMS=new JRadioButton("ms");
	JRadioButton freqUnitKHZ=new JRadioButton("kHz");
	JRadioButton freqUnitHZ=new JRadioButton("Hz");
	JCheckBox showFrame=new JCheckBox("Show frame");
	JCheckBox interiorTickMarkLabels=new JCheckBox("Interior tick-marks");
	JCheckBox showMajorTimeTickMark=new JCheckBox("Show major time tick-marks");
	JCheckBox showMinorTimeTickMark=new JCheckBox("Show minor time tick-marks");
	JCheckBox showMajorFreqTickMark=new JCheckBox("Show major frequency tick-marks");
	JCheckBox showMinorFreqTickMark=new JCheckBox("Show minor frequency tick-marks");
	
	JCheckBox clickDrag=new JCheckBox("Click and drag");
	
	JComboBox fontFace, axisLabelFontStyle, tickLabelFontStyle;
	JSpinner majorTickMarkLength, minorTickMarkLength, axisLabelFontSize, tickLabelFontSize, lineWeight;
	
	String[] fontFamilies;
	String[] fontStyles={"Regular", "Bold", "Italic", "Bold Italic"};
	String[] commonFamilies={"SansSerif", "Serif", "Arial", "Times"};

	JCheckBox viewSignal=new JCheckBox("View signal", true);
	JCheckBox viewPeak=new JCheckBox("View peak freq   ", true); 
	JCheckBox viewFund=new JCheckBox("View fund freq", true);
	JCheckBox viewMean=new JCheckBox("View mean freq", true); 
	JCheckBox viewMedian=new JCheckBox("View median freq", true);
	JCheckBox viewHarm=new JCheckBox("View harmonicity", true);
	JCheckBox viewWiener=new JCheckBox("View Wiener entropy", true);
	JCheckBox viewFundFreqChange=new JCheckBox("View fund freq change", false);
	JCheckBox viewPeakFreqChange=new JCheckBox("View peak freq change  ", false);
	JCheckBox viewMeanFreqChange=new JCheckBox("View mean freq change", false);
	JCheckBox viewMedianFreqChange=new JCheckBox("View median freq change", false);
	JCheckBox viewBandwidth=new JCheckBox("View bandwidth", true);
	JCheckBox viewAmplitude=new JCheckBox("View amplitude", true);
	JCheckBox viewElements=new JCheckBox("View elements", true);
	JCheckBox viewSyllables=new JCheckBox("View syllables", true);
	JCheckBox viewTrillAmp=new JCheckBox("View vibrato ampliture", true);
	JCheckBox viewTrillRate=new JCheckBox("View vibrato rate ", true);
	JCheckBox viewReverb=new JCheckBox("View vibrato asymmetry", true);
	
	double second;
	
	int xoffset, yoffset, ysize, xsize;
	
	JPanel controls, controlPane, settingsPane, measurePane, parameterPane, recordPane, appearancePane;
	JTabbedPane tabPane;
	NumberFormat num;
	String details="null";
	boolean started=false;
	boolean player=true;
	boolean updateable=true;
	boolean stopPlaying=false;
	boolean resetSpect=false;

	DataBaseController dbc;
	Song song;
	int songID;
	double ecorrect=Math.log(10);
	double stopRatio=0.5;
	int guidePanelMaxFrequency=10000;

	Color R_GREEN=new Color (0, 0.5f, 0, 0.5f);
	Color R_RED=new Color (0.5f, 0, 0, 0.5f);
	Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
	
	LinkedList<LinkedList<LinkedList<Object>>> undoList=new LinkedList<LinkedList<LinkedList<Object>>>();
	LinkedList<LinkedList> redoList=new LinkedList<LinkedList>();
	LinkedList host;
	int maxUndoListSize=10;	
	
	DatabaseView dbv;
	boolean editMode=false;
	
	public MainPanel(DataBaseController dbc, int songID, String user, Defaults defaults){
		this.dbc=dbc;
		this.songID=songID;
		this.defaults=defaults;
		editMode=true;
		song=dbc.loadSongFromDatabase(songID, 0);
		song.setUpPlayback();
		//defaults.getSongParameters(song);
		cloneLists();
	}
	
	public MainPanel(DataBaseController dbc, int songID, Defaults defaults){
		this.dbc=dbc;
		this.songID=songID;
		this.defaults=defaults;
		editMode=true;
		song=dbc.loadSongFromDatabase(songID, 0);
		song.setUpPlayback();
		//defaults.getSongParameters(song);
		cloneLists();
	}
	
	public MainPanel(DataBaseController dbc, int songID, Defaults defaults, LinkedList host){
		this.dbc=dbc;
		this.songID=songID;
		this.defaults=defaults;
		this.host=host;
		editMode=true;
		song=dbc.loadSongFromDatabase(songID, 0);
		song.setUpPlayback();
		//defaults.getSongParameters(song);
		cloneLists();
	}
	
	public MainPanel(DataBaseController dbc, int songID, Defaults defaults, LinkedList host, DatabaseView dbv){
		this.dbc=dbc;
		this.songID=songID;
		this.defaults=defaults;
		this.host=host;
		this.dbv=dbv;
		//editMode=false;
		editMode=true;
		song=dbc.loadSongFromDatabase(songID, 0);
		song.setUpPlayback();
		//defaults.getSongParameters(song);
		cloneLists();
	}
	
	public MainPanel(DataBaseController dbc, Song song, Defaults defaults, LinkedList host, DatabaseView dbv){
		this.dbc=dbc;
		this.song=song;
		this.defaults=defaults;
		this.host=host;
		this.dbv=dbv;
		editMode=false;
		song.setUpPlayback();
		//defaults.getSongParameters(song);
		cloneLists();
	}
	
	public Song getSong(){
		return song;
	}
	
	public void startDrawing(){
		this.setLayout(new BorderLayout());
		this.setFocusable(true);
		this.addKeyListener(this);
		d.width=(int)(d.getWidth()-50);
		num=NumberFormat.getNumberInstance();
		num.setMaximumFractionDigits(10);
		
		Font font=new Font("Sans-Serif", Font.PLAIN, 9);
		Font font2=new Font("Sans-Serif", Font.PLAIN, 9);
		Dimension dimSlider=new Dimension(100, 25);
		if (song.getMaxF()<=0){
			defaults.getSongParameters(song);
		}
		
		defaults.getMiscellaneousSongParameters(song);
		//System.out.println("maxfhere3: "+song.maxf);
		song.setFFTParameters();
		s=new SpectrPane(song, true, false, this);
		
		defaults.getParameterViews(s);
		
		if (!editMode){
			s.syllable=true;
			song.setBrushType(2);
			s.viewParameters[2]=true;
		}
		
		
		s.clickDrag=defaults.getBooleanProperty("clickdrag", false);
		guidePanelMaxFrequency=defaults.getIntProperty("GPMAXF", 10000);
		
		gp=new GuidePanel(100);
		s.setGP(gp);
		s.makeGuidePanel(d.getWidth()-20, guidePanelMaxFrequency);
		gp.draw();
		JScrollPane guidePanelScrollPane=new JScrollPane(gp, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		
		
		
		//this.add(sp, BorderLayout.CENTER);				
	
		//this.add(gp, BorderLayout.SOUTH);
		
		
		
		//this.add(guidePanelScrollPane, BorderLayout.SOUTH);
		
		s.stretchX=defaults.getDoubleProperty("stretchX", 1000, 1);
		s.stretchY=defaults.getDoubleProperty("stretchY", 1000, 1);
		if (s.stretchX<=0.5){
			s.stretchX=0.501;
		}
		s.restart();
		
		int x=s.nnx+20;
		if (x>d.getWidth()){x=(int)d.getWidth()-1;}
		int y=s.nny+10;
		if (y>d.getHeight()){y=(int)d.getHeight()-10;}
		sp=new JScrollPane(s, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		s.setPreferredSize(new Dimension(s.nnx, s.nny));
		sp.setPreferredSize(new Dimension(x, y));
		
		forwardButton.addActionListener(this);
		backButton.addActionListener(this);
		forwardButton.setActionCommand(FORWARD_COMMAND);
		backButton.setActionCommand(BACKWARD_COMMAND);
		fForwardButton.addActionListener(this);
		fBackButton.addActionListener(this);
		fForwardButton.setActionCommand(F_FORWARD_COMMAND);
		fBackButton.setActionCommand(F_BACKWARD_COMMAND);
		
		guidePanelScrollPane.setPreferredSize(new Dimension(x-50, 120));
		JPanel bPane=new JPanel(new BorderLayout());
		JPanel bpPane=new JPanel(new GridLayout(0,1));
		bpPane.add(fForwardButton);
		bpPane.add(forwardButton);
		bpPane.add(backButton);
		bpPane.add(fBackButton);
		bPane.add(bpPane, BorderLayout.WEST);
		bPane.add(guidePanelScrollPane, BorderLayout.CENTER);
		JSplitPane bottomSplitPane=new JSplitPane(JSplitPane.VERTICAL_SPLIT, sp, bPane);
		bottomSplitPane.setResizeWeight(1);
		
		
		started=false;
		controls=new JPanel(new BorderLayout());
		controls.setPreferredSize(new Dimension(s.nnx, 170));
		controls.setMinimumSize(new Dimension(0, 0));
		
		tabPane=new JTabbedPane();
		tabPane.setBackground(R_GREEN);
		
		populateDeleteS();
		populateDelete();
		populateMerge();

		brushS.setFont(font2);
		dynRangeL.setFont(font);
		deleteLE.setFont(font2);
		deleteLS.setFont(font2);
		mergeLE.setFont(font2);
		echoAmtL.setFont(font);
		echoRangeL.setFont(font);
		noiseAmtL.setFont(font);
		noiseRange1L.setFont(font);
		noiseRange2L.setFont(font);
		dynamicEqualizationL.setFont(font);
		harmonicL.setFont(font2);
		jumpSuppressionL.setFont(font2);
		minGapL.setFont(font2);
		minLengthL.setFont(font2);
		filterCutOffL.setFont(font);
		playL.setFont(font);
		playSpeedL.setFont(font);
		guidePanelL.setFont(font);
		maxFreqL.setFont(font);
		frameLengthL.setFont(font);
		timeStepL.setFont(font);
		windowMethodL.setFont(font);
		timeZoomL.setFont(font);
		freqZoomL.setFont(font);
		dateL.setFont(font);
		timeL.setFont(font);
		locationL.setFont(font);
		notesL.setFont(font);
		equipmentL.setFont(font);
		recordistL.setFont(font);
		dynML.setFont(font);
		spectOverlapL.setFont(font);
		spectPointsL.setFont(font);
		timeUnitL.setFont(font);
		freqUnitL.setFont(font);
		lineWeightL.setFont(font);
		
		lowerLoopL.setFont(font);
		upperLoopL.setFont(font);
		
		majorTickMarkLengthL.setFont(font);
		minorTickMarkLengthL.setFont(font);
		axisLabelL.setFont(font);
		tickLabelL.setFont(font);
		fontStyleL.setFont(font);
		fontSizeL.setFont(font);
		fontFaceL.setFont(font);
	
			
		//COMPONENTS FOR THE CONTROLS TAB
		playSyllable=new JComboBox(playModel);
		playSyllable.setMaximumRowCount(30);
		playSyllable.setActionCommand(PLAY_COMMAND);
        playSyllable.addActionListener(this);
		playSyllable.setFont(font);
		
		pbSpeed.setActionCommand(PLAY_SPEED_COMMAND);
		pbSpeed.addActionListener(this);
		pbSpeed.setFont(font);
		
		playAll.setActionCommand(PLAY_ALL_COMMAND);
		playAll.addActionListener(this);
		playAll.setFont(font);
		
		playScreen.setActionCommand(PLAY_SCREEN_COMMAND);
		playScreen.addActionListener(this);
		playScreen.setFont(font);
		
		guidePanelL.setLabelFor(gpMaxF);
		gpMaxF=new JFormattedTextField(num);
		gpMaxF.setColumns(6);
		gpMaxF.addPropertyChangeListener("value", this);
		gpMaxF.setValue(new Double(guidePanelMaxFrequency));
		gpMaxF.setFont(font);
		
		clickDrag.setSelected(s.clickDrag);
		clickDrag.setActionCommand(CLICK_DRAG);
		clickDrag.addActionListener(this);
		clickDrag.setFont(font);
		
				
		zoomTimeAll.setActionCommand(ZOOM_TO_ALL_COMMAND);
		zoomTimeAll.addActionListener(this);
		zoomTimeAll.setFont(font);
		
		zoomTime100.setActionCommand(ZOOM_TO_100_COMMAND);
		zoomTime100.addActionListener(this);
		zoomTime100.setFont(font);
		
		stop.setActionCommand(STOP_COMMAND);
		stop.addActionListener(this);
		stop.setFont(font);
						
		save.setMnemonic(KeyEvent.VK_S);
		save.setFont(font);
		save.setDisplayedMnemonicIndex(0);
		save.setActionCommand(SAVE_COMMAND);
        save.addActionListener(this);
				
		saveSound.setFont(font);
		saveSound.setActionCommand(SAVE_SOUND_COMMAND);
        saveSound.addActionListener(this);
						
		saveImage.setFont(font);
		saveImage.setActionCommand(SAVE_IMAGE_COMMAND);
        saveImage.addActionListener(this);
		
		displayMode.setFont(font);
		displayMode.setActionCommand(DISPLAY_MODE);
		displayMode.addActionListener(this);
		
		timeZoomL.setLabelFor(timeZoom);
		timeZoom=new JFormattedTextField(num);
		timeZoom.setColumns(6);
		timeZoom.addPropertyChangeListener("value", this);
		timeZoom.setValue(new Double(100/s.stretchX));
		timeZoom.setFont(font);
		
		int fsl=(int)(50/s.stretchX);
		if (fsl<0){fsl=0;}
		if (fsl>100){fsl=100;}
		timeZoomSL=new JSlider(0, 100, fsl);
		timeZoomSL.addChangeListener(this);
		timeZoomSL.setFont(font);
		timeZoomSL.setPreferredSize(dimSlider);
				
		freqZoomL.setLabelFor(freqZoom);
		freqZoom=new JFormattedTextField(num);
		freqZoom.setColumns(6);
		freqZoom.addPropertyChangeListener("value", this);
		freqZoom.setValue(new Double(100*s.stretchY));
		freqZoom.setFont(font);
		
		fsl=(int)(50*s.stretchY);
		if (fsl<0){fsl=0;}
		if (fsl>100){fsl=100;}
		freqZoomSL=new JSlider(0, 100, fsl);
		freqZoomSL.addChangeListener(this);
		freqZoomSL.setFont(font);
		freqZoomSL.setPreferredSize(dimSlider);
				
		//NEXT COMPONENTS FOR THE APPEARANCE TAB
		
		update.setMnemonic(KeyEvent.VK_U);
		update.setDisplayedMnemonicIndex(0);
		update.setActionCommand(UPDATE_COMMAND);
        update.addActionListener(this);
		update.setFont(font);
		
		defaultSettings.setMnemonic(KeyEvent.VK_D);
		defaultSettings.setDisplayedMnemonicIndex(0);
		defaultSettings.setActionCommand(DEFAULT_COMMAND);
        defaultSettings.addActionListener(this);
		defaultSettings.setFont(font);
		
		saveDefault.setActionCommand(SAVE_DEFAULT_COMMAND);
		saveDefault.addActionListener(this);
		saveDefault.setFont(font);
		
		setMaxDyn.setActionCommand(SET_TO_MAX_COMMAND);
		setMaxDyn.addActionListener(this);
		setMaxDyn.setFont(font);
			
		windowMethod.setFont(font);
		windowMethod.setActionCommand(WINDOW_COMMAND);
        windowMethod.addActionListener(this);
		windowMethod.setSelectedIndex(song.getWindowMethod()-1);

		
		echoAmtL.setLabelFor(echoAmt);
		echoAmt=new JFormattedTextField(num);
		echoAmt.setColumns(6);
		echoAmt.addPropertyChangeListener("value", this);
		echoAmt.setValue(new Double(song.getEchoComp()*100));
		echoAmt.setFont(font);
		
		int sco=(int)Math.round(song.getEchoComp()*100);
		if (sco<0){sco=0;}
		if (sco>100){sco=100;}
		echoAmtSL=new JSlider(0, 100, sco);
		echoAmtSL.addChangeListener(this);
		echoAmtSL.setFont(font);
		echoAmtSL.setPreferredSize(dimSlider);
		
		noiseAmtL.setLabelFor(noiseAmt);
		noiseAmt=new JFormattedTextField(num);
		noiseAmt.setColumns(6);
		noiseAmt.addPropertyChangeListener("value", this);
		noiseAmt.setValue(new Double(song.getNoiseRemoval()));
		noiseAmt.setFont(font);
		
		sco=(int)Math.round(song.getNoiseRemoval()*5);
		if (sco<0){sco=0;}
		if (sco>100){sco=100;}
		noiseAmtSL=new JSlider(0, 100, sco);
		noiseAmtSL.addChangeListener(this);
		noiseAmtSL.setFont(font);
		noiseAmtSL.setPreferredSize(dimSlider);
				
		dynRangeL.setLabelFor(dynRange);
		dynRange=new JFormattedTextField(num);
		dynRange.setColumns(6);
		dynRange.addPropertyChangeListener("value", this);
		dynRange.setValue(new Double(song.getDynRange()));
		dynRange.setFont(font);
		
		int sgs=(int)Math.round(song.getDynRange());
		if (sgs<0){sgs=0;}
		if (sgs>100){sgs=100;}
		dynRangeSL=new JSlider(0, 100, sgs);
		dynRangeSL.addChangeListener(this);
		dynRangeSL.setFont(font);
		dynRangeSL.setPreferredSize(dimSlider);
		
		dynML.setLabelFor(dynM);
		dynM=new JFormattedTextField(num);
		dynM.setColumns(6);
		dynM.addPropertyChangeListener("value", this);
		dynM.setValue(new Integer((int)Math.round(song.getDynMax()*100)));
		dynM.setFont(font);
		if (song.getSetRangeToMax()){dynM.setEnabled(false);}
		
		int sgm=(int)Math.round(song.getDynMax()*100);
		if (sgm<0){sgm=0;}
		if (sgm>100){sgm=100;}
		dynMSL=new JSlider(0, 100, sgm);
		dynMSL.addChangeListener(this);
		dynMSL.setFont(font);
		if (song.getSetRangeToMax()){dynMSL.setEnabled(false);}
		dynMSL.setPreferredSize(dimSlider);
		
		echoRangeL.setLabelFor(echoRange);
		echoRange=new JFormattedTextField(num);
		echoRange.setColumns(6);
		echoRange.addPropertyChangeListener("value", this);
		echoRange.setValue(new Double(song.getEchoRange()));
		echoRange.setFont(font);
		
		int stas=(int)Math.round(song.getEchoRange());
		if (stas<0){stas=0;}
		if (stas>100){stas=100;}
		echoRangeSL=new JSlider(0, 100, stas);
		echoRangeSL.addChangeListener(this);
		echoRangeSL.setFont(font);
		echoRangeSL.setPreferredSize(dimSlider);
		
		noiseRange1L.setLabelFor(noiseRange1);
		noiseRange1=new JFormattedTextField(num);
		noiseRange1.setColumns(6);
		noiseRange1.addPropertyChangeListener("value", this);
		noiseRange1.setValue(new Double(song.getNoiseLength1()));
		noiseRange1.setFont(font);
		
		stas=(int)Math.round(song.getNoiseLength1()/10);
		if (stas<0){stas=0;}
		if (stas>100){stas=100;}
		noiseRange1SL=new JSlider(0, 100, stas);
		noiseRange1SL.addChangeListener(this);
		noiseRange1SL.setFont(font);
		noiseRange1SL.setPreferredSize(dimSlider);
		
		noiseRange2L.setLabelFor(noiseRange2);
		noiseRange2=new JFormattedTextField(num);
		noiseRange2.setColumns(6);
		noiseRange2.addPropertyChangeListener("value", this);
		noiseRange2.setValue(new Double(song.getNoiseLength2()));
		noiseRange2.setFont(font);
		
		stas=(int)Math.round(song.getNoiseLength2());
		if (stas<0){stas=0;}
		if (stas>100){stas=100;}
		noiseRange2SL=new JSlider(0, 100, stas);
		noiseRange2SL.addChangeListener(this);
		noiseRange2SL.setFont(font);
		noiseRange2SL.setPreferredSize(dimSlider);
						
		dynamicEqualizationL.setLabelFor(dynEqual);
		dynEqual=new JFormattedTextField(num);
		dynEqual.setColumns(6);
		dynEqual.addPropertyChangeListener("value", this);
		dynEqual.setValue(new Double(song.getDynEqual()));
		dynEqual.setFont(font);

		int sdyc=(int)Math.round(song.getDynEqual());
		if (sdyc<0){sdyc=0;}
		if (sdyc>100){sdyc=100;}
		dynEqualSL=new JSlider(0, 100, sdyc);
		dynEqualSL.addChangeListener(this);
		dynEqualSL.setFont(font);
		dynEqualSL.setPreferredSize(dimSlider);
				
		filterCutOffL.setLabelFor(filterCutOff);
		filterCutOff=new JFormattedTextField(num);
		filterCutOff.setColumns(6);
		filterCutOff.addPropertyChangeListener("value", this);
		filterCutOff.setValue(new Double(song.getFrequencyCutOff()));
		filterCutOff.setFont(font);
		
		int sfco=(int)Math.round(song.getFrequencyCutOff()/20.0);
		if (sfco<0){sfco=0;}
		if (sfco>100){sfco=100;}
		filterCutOffSL=new JSlider(0, 100, sfco);
		filterCutOffSL.addChangeListener(this);
		filterCutOffSL.setFont(font);
		filterCutOffSL.setPreferredSize(dimSlider);
				
		maxFreqL.setLabelFor(maxFreq);
		maxFreq=new JFormattedTextField(num);
		maxFreq.setColumns(6);
		maxFreq.addPropertyChangeListener("value", this);
		maxFreq.setValue(new Double(song.getMaxF()));
		maxFreq.setFont(font);
		
		int mfs=(int)Math.round(song.getMaxF()/200.0);
		if (mfs<0){mfs=0;}
		if (mfs>100){mfs=100;}
		maxFreqSL=new JSlider(0, 100, mfs);
		maxFreqSL.addChangeListener(this);
		maxFreqSL.setFont(font);
		maxFreqSL.setPreferredSize(dimSlider);
		
		frameLengthL.setLabelFor(frameLength);
		frameLength=new JFormattedTextField(num);
		frameLength.setColumns(6);
		frameLength.addPropertyChangeListener("value", this);
		frameLength.setValue(new Double(song.getFrameLength()));
		frameLength.setFont(font);

		int mfls=(int)Math.round(song.getFrameLength()*5);
		if (mfls<0){mfls=0;}
		if (mfls>100){mfls=100;}
		frameLengthSL=new JSlider(0, 100, mfls);
		frameLengthSL.addChangeListener(this);
		frameLengthSL.setFont(font);
		frameLengthSL.setPreferredSize(dimSlider);

		timeStepL.setLabelFor(timeStep);
		timeStep=new JFormattedTextField(num);
		timeStep.setColumns(6);
		timeStep.addPropertyChangeListener("value", this);
		timeStep.setValue(new Double(song.getTimeStep()));
		timeStep.setFont(font);
		
		int mts=(int)Math.round(song.getTimeStep()*10);;
		if (mts<0){mts=0;}
		if (mts>100){mts=100;}
		timeStepSL=new JSlider(0, 100, mts);
		timeStepSL.addChangeListener(this);
		timeStepSL.setFont(font);
		timeStepSL.setPreferredSize(dimSlider);
		
		spectOverlapL.setLabelFor(spectOverlap);
		spectOverlap=new JFormattedTextField(num);
		spectOverlap.setColumns(6);
		spectOverlap.addPropertyChangeListener("value", this);
		spectOverlap.setValue(new Double(song.getOverlap()*100));
		spectOverlap.setFont(font);
		
		int mto=(int)Math.round(song.getOverlap()*100);
		if (mto<0){mto=0;}
		if (mto>100){mto=100;}
		spectOverlapSL=new JSlider(0, 100, mto);
		spectOverlapSL.addChangeListener(this);
		spectOverlapSL.setFont(font);
		spectOverlapSL.setPreferredSize(dimSlider);

		spectPointsL.setLabelFor(spectPoints);
		spectPoints=new JFormattedTextField(num);
		spectPoints.setColumns(6);
		spectPoints.addPropertyChangeListener("value", this);
		spectPoints.setValue(new Double(song.getFrame()));
		spectPoints.setFont(font);
		
		int mfps=(int)Math.round(Math.log(song.getFrame())/Math.log(2));
		if (mfps<0){mfps=0;}
		if (mfps>12){mfps=12;}
		spectPointsSL=new JSlider(0, 12, mfps);
		spectPointsSL.addChangeListener(this);
		spectPointsSL.setFont(font);
		spectPointsSL.setPreferredSize(dimSlider);
		SpringLayout slayout1=new SpringLayout();
		controlPane=new JPanel(slayout1);
				
		Spring spring=Spring.constant(1, 10, 20);
		controlPane.add(save);
		controlPane.add(saveSound);
		controlPane.add(saveImage);
		controlPane.add(playAll);
		controlPane.add(playScreen);
		controlPane.add(stop);
		controlPane.add(playSyllable);
		controlPane.add(playL);
		controlPane.add(pbSpeed);
		controlPane.add(playSpeedL);
		controlPane.add(displayMode);
		controlPane.add(guidePanelL);
		controlPane.add(gpMaxF);
		controlPane.add(clickDrag);
		
		slayout1.putConstraint(SpringLayout.WEST, save, spring, SpringLayout.WEST, controlPane);
		slayout1.putConstraint(SpringLayout.NORTH, save, 2, SpringLayout.NORTH, controlPane);
		
		slayout1.putConstraint(SpringLayout.WEST, saveSound, 0, SpringLayout.WEST, save);
		slayout1.putConstraint(SpringLayout.NORTH, saveSound, spring, SpringLayout.SOUTH, save);
		
		slayout1.putConstraint(SpringLayout.WEST, saveImage, 0, SpringLayout.WEST, save);
		slayout1.putConstraint(SpringLayout.NORTH, saveImage, spring, SpringLayout.SOUTH, saveSound);
		
		slayout1.putConstraint(SpringLayout.WEST, playAll, spring, SpringLayout.EAST, save);
		slayout1.putConstraint(SpringLayout.NORTH, playAll, 2, SpringLayout.NORTH, controlPane);
		
		slayout1.putConstraint(SpringLayout.WEST, playScreen, 0, SpringLayout.WEST, playAll);
		slayout1.putConstraint(SpringLayout.NORTH, playScreen, spring, SpringLayout.SOUTH, playAll);
		
		slayout1.putConstraint(SpringLayout.WEST, stop, 0, SpringLayout.WEST, playAll);
		slayout1.putConstraint(SpringLayout.NORTH, stop, spring, SpringLayout.SOUTH, playScreen);
			
		slayout1.putConstraint(SpringLayout.WEST, playL, spring, SpringLayout.EAST, playScreen);
		slayout1.putConstraint(SpringLayout.NORTH, playL, 5, SpringLayout.NORTH, controlPane);
		
		slayout1.putConstraint(SpringLayout.WEST, playSyllable, spring, SpringLayout.EAST, playL);
		slayout1.putConstraint(SpringLayout.NORTH, playSyllable, 2, SpringLayout.NORTH, controlPane);
		
		slayout1.putConstraint(SpringLayout.WEST, playSpeedL, 0, SpringLayout.WEST, playL);
		slayout1.putConstraint(SpringLayout.NORTH, playSpeedL, 0, SpringLayout.NORTH, playScreen);
		
		slayout1.putConstraint(SpringLayout.WEST, pbSpeed, spring, SpringLayout.EAST, playSpeedL);
		slayout1.putConstraint(SpringLayout.NORTH, pbSpeed, 0, SpringLayout.NORTH, playScreen);
		
		slayout1.putConstraint(SpringLayout.WEST, displayMode, 0, SpringLayout.WEST, playSpeedL);
		slayout1.putConstraint(SpringLayout.NORTH, displayMode,0, SpringLayout.NORTH, stop);
		
		slayout1.putConstraint(SpringLayout.WEST, guidePanelL, spring, SpringLayout.EAST, playSyllable);
		slayout1.putConstraint(SpringLayout.NORTH, guidePanelL, 5, SpringLayout.NORTH, controlPane);
		
		slayout1.putConstraint(SpringLayout.WEST, gpMaxF, spring, SpringLayout.EAST, guidePanelL);
		slayout1.putConstraint(SpringLayout.NORTH, gpMaxF, 2, SpringLayout.NORTH, controlPane);
		
		slayout1.putConstraint(SpringLayout.WEST, clickDrag, 0, SpringLayout.WEST, gpMaxF);
		slayout1.putConstraint(SpringLayout.NORTH, clickDrag, spring, SpringLayout.SOUTH, gpMaxF);
		
		slayout1.putConstraint(SpringLayout.SOUTH, controlPane, spring, SpringLayout.SOUTH, saveImage);

		SpringLayout slayout2=new SpringLayout();
		settingsPane=new JPanel(slayout2);
		
		settingsPane.add(update);
		settingsPane.add(defaultSettings);
		settingsPane.add(saveDefault);
		settingsPane.add(maxFreq);
		settingsPane.add(frameLength);
		settingsPane.add(timeStep);
		settingsPane.add(spectOverlap);
		settingsPane.add(spectPoints);
		settingsPane.add(dynRange);
		settingsPane.add(echoAmt);
		settingsPane.add(echoRange);
		settingsPane.add(filterCutOff);
		settingsPane.add(dynEqual);
		settingsPane.add(maxFreqL);
		settingsPane.add(frameLengthL);
		settingsPane.add(timeStepL);
		settingsPane.add(spectOverlapL);
		settingsPane.add(spectPointsL);
		settingsPane.add(dynRangeL);
		settingsPane.add(echoAmtL);
		settingsPane.add(echoRangeL);
		settingsPane.add(filterCutOffL);
		settingsPane.add(dynamicEqualizationL);
		settingsPane.add(maxFreqSL);
		settingsPane.add(frameLengthSL);
		settingsPane.add(timeStepSL);
		settingsPane.add(spectOverlapSL);
		settingsPane.add(spectPointsSL);
		settingsPane.add(dynRangeSL);
		settingsPane.add(echoAmtSL);
		settingsPane.add(echoRangeSL);
		settingsPane.add(filterCutOffSL);
		settingsPane.add(dynEqualSL);
		settingsPane.add(windowMethodL);
		settingsPane.add(windowMethod);
		settingsPane.add(dynML);
		settingsPane.add(dynM);
		settingsPane.add(dynMSL);
		settingsPane.add(setMaxDyn);
		settingsPane.add(timeZoom);
		settingsPane.add(timeZoomL);
		settingsPane.add(timeZoomSL);
		settingsPane.add(freqZoom);
		settingsPane.add(freqZoomL);
		settingsPane.add(freqZoomSL);
		settingsPane.add(zoomTimeAll);
		settingsPane.add(zoomTime100);
		settingsPane.add(noiseAmtL);
		settingsPane.add(noiseAmt);
		settingsPane.add(noiseAmtSL);
		settingsPane.add(noiseRange1L);
		settingsPane.add(noiseRange1);
		settingsPane.add(noiseRange1SL);
		settingsPane.add(noiseRange2L);
		settingsPane.add(noiseRange2);
		settingsPane.add(noiseRange2SL);
		
		slayout2.putConstraint(SpringLayout.WEST, update, spring, SpringLayout.WEST, settingsPane);
		slayout2.putConstraint(SpringLayout.NORTH, update, 2, SpringLayout.NORTH, settingsPane);
		
		slayout2.putConstraint(SpringLayout.WEST, defaultSettings, 0, SpringLayout.WEST, update);
		slayout2.putConstraint(SpringLayout.NORTH, defaultSettings, spring, SpringLayout.SOUTH, update);
		
		slayout2.putConstraint(SpringLayout.WEST, saveDefault, 0, SpringLayout.WEST, update);
		slayout2.putConstraint(SpringLayout.NORTH, saveDefault, spring, SpringLayout.SOUTH, defaultSettings);
		
		slayout2.putConstraint(SpringLayout.WEST, setMaxDyn, 0, SpringLayout.WEST, update);
		slayout2.putConstraint(SpringLayout.NORTH, setMaxDyn, spring, SpringLayout.SOUTH, saveDefault);
		
		slayout2.putConstraint(SpringLayout.WEST, maxFreqL, spring, SpringLayout.EAST, setMaxDyn);
		slayout2.putConstraint(SpringLayout.NORTH, maxFreqL, 7, SpringLayout.NORTH, settingsPane);
		
		slayout2.putConstraint(SpringLayout.WEST, maxFreq, 5, SpringLayout.EAST, maxFreqL);
		slayout2.putConstraint(SpringLayout.NORTH, maxFreq, 5, SpringLayout.NORTH, settingsPane);
		
		slayout2.putConstraint(SpringLayout.WEST, maxFreqSL, 5, SpringLayout.EAST, maxFreq);
		slayout2.putConstraint(SpringLayout.NORTH, maxFreqSL, 0, SpringLayout.NORTH, settingsPane);
		
		slayout2.putConstraint(SpringLayout.WEST, frameLength, 0, SpringLayout.WEST, maxFreq);
		slayout2.putConstraint(SpringLayout.NORTH, frameLength, spring, SpringLayout.SOUTH, maxFreq);
		
		slayout2.putConstraint(SpringLayout.WEST, timeStep, 0, SpringLayout.WEST, maxFreq);
		slayout2.putConstraint(SpringLayout.NORTH, timeStep, spring, SpringLayout.SOUTH, frameLength);
		
		slayout2.putConstraint(SpringLayout.WEST, spectPoints, 0, SpringLayout.WEST, maxFreq);
		slayout2.putConstraint(SpringLayout.NORTH, spectPoints, spring, SpringLayout.SOUTH, timeStep);
		
		slayout2.putConstraint(SpringLayout.WEST, spectOverlap, 0, SpringLayout.WEST, maxFreq);
		slayout2.putConstraint(SpringLayout.NORTH, spectOverlap, spring, SpringLayout.SOUTH, spectPoints);
		
		slayout2.putConstraint(SpringLayout.EAST, frameLengthL, -5, SpringLayout.WEST, frameLength);
		slayout2.putConstraint(SpringLayout.NORTH, frameLengthL, 2, SpringLayout.NORTH, frameLength);
		
		slayout2.putConstraint(SpringLayout.EAST, spectPointsL, -5, SpringLayout.WEST, spectPoints);
		slayout2.putConstraint(SpringLayout.NORTH, spectPointsL, 2, SpringLayout.NORTH, spectPoints);
		
		slayout2.putConstraint(SpringLayout.EAST, spectOverlapL, -5, SpringLayout.WEST, spectOverlap);
		slayout2.putConstraint(SpringLayout.NORTH, spectOverlapL, 2, SpringLayout.NORTH, spectOverlap);
		
		slayout2.putConstraint(SpringLayout.EAST, timeStepL, -5, SpringLayout.WEST, timeStep);
		slayout2.putConstraint(SpringLayout.NORTH, timeStepL, 2, SpringLayout.NORTH, timeStep);
		
		slayout2.putConstraint(SpringLayout.WEST, frameLengthSL, 5, SpringLayout.EAST, frameLength);
		slayout2.putConstraint(SpringLayout.NORTH, frameLengthSL, -5, SpringLayout.NORTH, frameLength);
		
		slayout2.putConstraint(SpringLayout.WEST, timeStepSL, 5, SpringLayout.EAST, timeStep);
		slayout2.putConstraint(SpringLayout.NORTH, timeStepSL, -5, SpringLayout.NORTH, timeStep);
		
		slayout2.putConstraint(SpringLayout.WEST, spectPointsSL, 5, SpringLayout.EAST, spectPoints);
		slayout2.putConstraint(SpringLayout.NORTH, spectPointsSL, -5, SpringLayout.NORTH, spectPoints);
		
		slayout2.putConstraint(SpringLayout.WEST, spectOverlapSL, 5, SpringLayout.EAST, spectOverlap);
		slayout2.putConstraint(SpringLayout.NORTH, spectOverlapSL, -5, SpringLayout.NORTH, spectOverlap);
		
		slayout2.putConstraint(SpringLayout.WEST, dynRangeL, spring, SpringLayout.EAST, maxFreqSL);
		slayout2.putConstraint(SpringLayout.NORTH, dynRangeL, 7, SpringLayout.NORTH, settingsPane);
		
		slayout2.putConstraint(SpringLayout.WEST, dynRange, 5, SpringLayout.EAST, dynRangeL);
		slayout2.putConstraint(SpringLayout.NORTH, dynRange, 5, SpringLayout.NORTH, settingsPane);
		
		slayout2.putConstraint(SpringLayout.WEST, dynRangeSL, 5, SpringLayout.EAST, dynRange);
		slayout2.putConstraint(SpringLayout.NORTH, dynRangeSL, 0, SpringLayout.NORTH, settingsPane);
		
		slayout2.putConstraint(SpringLayout.WEST, dynEqual, 0, SpringLayout.WEST, dynRange);
		slayout2.putConstraint(SpringLayout.NORTH, dynEqual, spring, SpringLayout.SOUTH, dynRange);
		
		slayout2.putConstraint(SpringLayout.WEST, dynM, 0, SpringLayout.WEST, dynRange);
		slayout2.putConstraint(SpringLayout.NORTH, dynM, spring, SpringLayout.SOUTH, dynEqual);
		
		slayout2.putConstraint(SpringLayout.WEST, echoAmt, 0, SpringLayout.WEST, dynRange);
		slayout2.putConstraint(SpringLayout.NORTH, echoAmt, spring, SpringLayout.SOUTH, dynM);
		
		slayout2.putConstraint(SpringLayout.WEST, echoRange, 0, SpringLayout.WEST, echoAmt);
		slayout2.putConstraint(SpringLayout.NORTH, echoRange, spring, SpringLayout.SOUTH, echoAmt);
		
		slayout2.putConstraint(SpringLayout.EAST, dynamicEqualizationL, -5, SpringLayout.WEST, dynEqual);
		slayout2.putConstraint(SpringLayout.NORTH, dynamicEqualizationL, 2, SpringLayout.NORTH, dynEqual);
		
		slayout2.putConstraint(SpringLayout.EAST, dynML, -5, SpringLayout.WEST, dynM);
		slayout2.putConstraint(SpringLayout.NORTH, dynML, 2, SpringLayout.NORTH, dynM);
		
		slayout2.putConstraint(SpringLayout.EAST, echoAmtL, -5, SpringLayout.WEST, echoAmt);
		slayout2.putConstraint(SpringLayout.NORTH, echoAmtL, 2, SpringLayout.NORTH, echoAmt);
		
		slayout2.putConstraint(SpringLayout.EAST, echoRangeL, -5, SpringLayout.WEST, echoRange);
		slayout2.putConstraint(SpringLayout.NORTH, echoRangeL, 2, SpringLayout.NORTH, echoRange);
		
		slayout2.putConstraint(SpringLayout.WEST, dynEqualSL, 5, SpringLayout.EAST, dynEqual);
		slayout2.putConstraint(SpringLayout.NORTH, dynEqualSL, -5, SpringLayout.NORTH, dynEqual);
		
		slayout2.putConstraint(SpringLayout.WEST, dynMSL, 5, SpringLayout.EAST, dynM);
		slayout2.putConstraint(SpringLayout.NORTH, dynMSL, -5, SpringLayout.NORTH, dynML);
		
		slayout2.putConstraint(SpringLayout.WEST, echoAmtSL, 5, SpringLayout.EAST, echoAmt);
		slayout2.putConstraint(SpringLayout.NORTH, echoAmtSL, -5, SpringLayout.NORTH, echoAmt);
		
		slayout2.putConstraint(SpringLayout.WEST, echoRangeSL, 5, SpringLayout.EAST, echoRange);
		slayout2.putConstraint(SpringLayout.NORTH, echoRangeSL, -5, SpringLayout.NORTH, echoRange);
		
		
		
		slayout2.putConstraint(SpringLayout.WEST, windowMethodL,  spring, SpringLayout.EAST, dynRangeSL);
		slayout2.putConstraint(SpringLayout.NORTH, windowMethodL,7, SpringLayout.NORTH, settingsPane);
		
		slayout2.putConstraint(SpringLayout.WEST, windowMethod, 5, SpringLayout.EAST, windowMethodL);
		slayout2.putConstraint(SpringLayout.NORTH, windowMethod,  5, SpringLayout.NORTH, settingsPane);
		
		slayout2.putConstraint(SpringLayout.WEST, filterCutOff, 0, SpringLayout.WEST, windowMethod); 
		slayout2.putConstraint(SpringLayout.NORTH, filterCutOff, spring, SpringLayout.SOUTH, windowMethod);
		
		slayout2.putConstraint(SpringLayout.EAST, filterCutOffL, -5, SpringLayout.WEST, filterCutOff);
		slayout2.putConstraint(SpringLayout.NORTH, filterCutOffL, 0, SpringLayout.NORTH, filterCutOff);
		
		slayout2.putConstraint(SpringLayout.WEST, filterCutOffSL, 5, SpringLayout.EAST, filterCutOff);
		slayout2.putConstraint(SpringLayout.NORTH, filterCutOffSL, -5, SpringLayout.NORTH, filterCutOff);
		
		slayout2.putConstraint(SpringLayout.WEST, freqZoom, 0, SpringLayout.WEST, filterCutOff);
		slayout2.putConstraint(SpringLayout.NORTH, freqZoom, spring, SpringLayout.SOUTH, filterCutOff);
		
		slayout2.putConstraint(SpringLayout.EAST, freqZoomL, -5, SpringLayout.WEST, freqZoom);
		slayout2.putConstraint(SpringLayout.NORTH, freqZoomL, 0, SpringLayout.NORTH, freqZoom);
		
		slayout2.putConstraint(SpringLayout.WEST, freqZoomSL, 5, SpringLayout.EAST, freqZoom);
		slayout2.putConstraint(SpringLayout.NORTH, freqZoomSL, 0, SpringLayout.NORTH, freqZoom);
		
		slayout2.putConstraint(SpringLayout.WEST, timeZoom, 0, SpringLayout.WEST, freqZoom);
		slayout2.putConstraint(SpringLayout.NORTH, timeZoom, spring, SpringLayout.SOUTH, freqZoom);
		
		slayout2.putConstraint(SpringLayout.EAST, timeZoomL, -5, SpringLayout.WEST, timeZoom);
		slayout2.putConstraint(SpringLayout.NORTH, timeZoomL, 0, SpringLayout.NORTH, timeZoom);
				
		slayout2.putConstraint(SpringLayout.WEST, timeZoomSL, 5, SpringLayout.EAST, timeZoom);
		slayout2.putConstraint(SpringLayout.NORTH, timeZoomSL, 0, SpringLayout.NORTH, timeZoom);
		
		slayout2.putConstraint(SpringLayout.EAST, zoomTimeAll, 0, SpringLayout.EAST, timeZoomL);
		slayout2.putConstraint(SpringLayout.NORTH, zoomTimeAll, 7, SpringLayout.SOUTH, timeZoomL);
		
		slayout2.putConstraint(SpringLayout.WEST, zoomTime100, 5, SpringLayout.EAST, zoomTimeAll);
		slayout2.putConstraint(SpringLayout.NORTH, zoomTime100, 0, SpringLayout.NORTH, zoomTimeAll);
		
		
		slayout2.putConstraint(SpringLayout.WEST, noiseAmtL,  spring, SpringLayout.EAST, filterCutOffSL);
		slayout2.putConstraint(SpringLayout.NORTH, noiseAmtL,7, SpringLayout.NORTH, settingsPane);
		
		slayout2.putConstraint(SpringLayout.WEST, noiseAmt, 5, SpringLayout.EAST, noiseAmtL);
		slayout2.putConstraint(SpringLayout.NORTH, noiseAmt,  5, SpringLayout.NORTH, settingsPane);
		
		slayout2.putConstraint(SpringLayout.WEST, noiseAmtSL, 5, SpringLayout.EAST, noiseAmt);
		slayout2.putConstraint(SpringLayout.NORTH, noiseAmtSL, 0, SpringLayout.NORTH, noiseAmt);
		
		slayout2.putConstraint(SpringLayout.WEST, noiseRange1, 0, SpringLayout.WEST, noiseAmt);
		slayout2.putConstraint(SpringLayout.NORTH, noiseRange1, spring, SpringLayout.SOUTH, noiseAmt);
		
		slayout2.putConstraint(SpringLayout.EAST, noiseRange1L, -5, SpringLayout.WEST, noiseRange1);
		slayout2.putConstraint(SpringLayout.NORTH, noiseRange1L, 0, SpringLayout.NORTH, noiseRange1);
				
		slayout2.putConstraint(SpringLayout.WEST, noiseRange1SL, 5, SpringLayout.EAST, noiseRange1);
		slayout2.putConstraint(SpringLayout.NORTH, noiseRange1SL, 0, SpringLayout.NORTH, noiseRange1);
		
		slayout2.putConstraint(SpringLayout.WEST, noiseRange2, 0, SpringLayout.WEST, noiseAmt);
		slayout2.putConstraint(SpringLayout.NORTH, noiseRange2, spring, SpringLayout.SOUTH, noiseRange1);
		
		slayout2.putConstraint(SpringLayout.EAST, noiseRange2L, -5, SpringLayout.WEST, noiseRange2);
		slayout2.putConstraint(SpringLayout.NORTH, noiseRange2L, 0, SpringLayout.NORTH, noiseRange2);
				
		slayout2.putConstraint(SpringLayout.WEST, noiseRange2SL, 5, SpringLayout.EAST, noiseRange2);
		slayout2.putConstraint(SpringLayout.NORTH, noiseRange2SL, 0, SpringLayout.NORTH, noiseRange2);
		
		
		
		//slayout2.putConstraint(SpringLayout.SOUTH, settingsPane, spring, SpringLayout.SOUTH, setMaxDyn);
		slayout2.putConstraint(SpringLayout.SOUTH, settingsPane, spring, SpringLayout.SOUTH, zoomTimeAll);
		
		tabPane.addTab("Controls", controlPane);
		tabPane.addTab("Settings", settingsPane);
		
		
		controls.add(tabPane, BorderLayout.CENTER);
		
		//this.add(controls, BorderLayout.NORTH);
		
		JSplitPane mainSplitPane=new JSplitPane(JSplitPane.VERTICAL_SPLIT, controls, bottomSplitPane);
		mainSplitPane.setDividerLocation(170 + mainSplitPane.getInsets().top);
		mainSplitPane.setResizeWeight(0);
		this.add(mainSplitPane, BorderLayout.CENTER);
			
		redo.setMnemonic(KeyEvent.VK_R);
		redo.setFont(font2);
		redo.setDisplayedMnemonicIndex(0);
		redo.setActionCommand(REDO_COMMAND);
        redo.addActionListener(this);
		redo.setEnabled(false);
		
		undo.setMnemonic(KeyEvent.VK_N);
		undo.setFont(font2);
		undo.setDisplayedMnemonicIndex(1);
		undo.setActionCommand(UNDO_COMMAND);
        undo.addActionListener(this);
		undo.setEnabled(false);
		
		//selectModel.setMnemonic(KeyEvent.VK_L);
		//selectModel.setFont(font2);
		//selectModel.setDisplayedMnemonicIndex(2);
		//selectModel.setActionCommand(SELECT_COMMAND);
        //selectModel.addActionListener(this);

		brushS.setLabelFor(brush);
		//brush.setSelectedIndex(5);
		if (song.getBrushType()==1){
			for (int i=0; i<5; i++){
				if (brushSizeInd[i]==song.getBrushSize()){
					brush.setSelectedIndex(i);
				}
			}
		}
		else {
			brush.setSelectedIndex(3+song.getBrushType());
		}
		brush.setFont(font2);
		brush.setActionCommand(BRUSH_COMMAND);
        brush.addActionListener(this);
		
		delete=new JComboBox(deleteModel);
		delete.setMaximumRowCount(50);
		delete.setActionCommand(DELETE_COMMAND);
		delete.setFont(font2);
        delete.addActionListener(this);
		
		merge=new JComboBox(mergeModel);
		merge.setMaximumRowCount(50);	
		merge.setActionCommand(MERGE_COMMAND);
		merge.setFont(font2);
        merge.addActionListener(this);
		
		deleteS=new JComboBox(deleteModelS);
		deleteS.setMaximumRowCount(30);
		deleteS.setActionCommand(DELETES_COMMAND);
		deleteS.setFont(font2);
        deleteS.addActionListener(this);
		
		automatic.setMnemonic(KeyEvent.VK_T);
		automatic.setFont(font2);
		automatic.setDisplayedMnemonicIndex(5);
		automatic.setActionCommand(AUTOMATIC_COMMAND);
        automatic.addActionListener(this);		
			
		reestimate.setFont(font2);
		reestimate.setActionCommand(REESTIMATE_COMMAND);
		reestimate.addActionListener(this);	
		
		reestAll.setFont(font2);
		reestAll.setActionCommand(REEST_ALL_COMMAND);
		reestAll.addActionListener(this);	
		
		calculateSyllableStructure.setFont(font2);
		calculateSyllableStructure.setActionCommand(CALC_SYLL_COMMAND);
		calculateSyllableStructure.addActionListener(this);
		
		mode.setMnemonic(KeyEvent.VK_M);
		mode.setFont(font2);
		mode.setDisplayedMnemonicIndex(0);
		mode.setActionCommand(MODE_COMMAND);
        mode.addActionListener(this);
		
        
        erase.setFont(font2);
        erase.setActionCommand(ERASE_COMMAND);
        erase.addActionListener(this);
        
		jumpSuppressionL.setLabelFor(fundJumpSuppression);
		fundJumpSuppression=new JFormattedTextField(num);
		fundJumpSuppression.setFont(font2);
		fundJumpSuppression.setColumns(6);
		fundJumpSuppression.addPropertyChangeListener("value", this);
		fundJumpSuppression.setValue(new Double(song.getFundJumpSuppression()));
		
		harmonicL.setLabelFor(harmonic);
		harmonic=new JFormattedTextField(num);
		harmonic.setFont(font2);
		harmonic.setColumns(6);
		harmonic.addPropertyChangeListener("value", this);
		harmonic.setValue(new Double(song.getFundAdjust()));
		
		
		minGapL.setLabelFor(minGap);
		minGap=new JFormattedTextField(num);
		minGap.setFont(font2);
		minGap.setColumns(6);
		minGap.addPropertyChangeListener("value", this);
		minGap.setValue(new Double(song.getMinGap()));
		minGap.setFont(new Font("Sans-Serif", Font.PLAIN, 10));
		
		minLengthL.setLabelFor(minLength);
		minLength=new JFormattedTextField(num);
		minLength.setFont(font2);
		minLength.setColumns(6);
		minLength.addPropertyChangeListener("value", this);
		minLength.setValue(new Double(song.getMinLength()));
		minLength.setFont(new Font("Sans-Serif", Font.PLAIN, 10));
		
		upperLoopL.setLabelFor(upperLoop);
		upperLoop=new JFormattedTextField(num);
		upperLoop.setColumns(6);
		upperLoop.addPropertyChangeListener("value", this);
		upperLoop.setValue(new Double(song.getUpperLoop()));
		upperLoop.setFont(font);
		
		lowerLoopL.setLabelFor(lowerLoop);
		lowerLoop=new JFormattedTextField(num);
		lowerLoop.setColumns(6);
		lowerLoop.addPropertyChangeListener("value", this);
		lowerLoop.setValue(new Double(song.getLowerLoop()));
		lowerLoop.setFont(font);
		
		viewSignal.setSelected(s.viewParameters[0]);
		viewSignal.setActionCommand(SIGNAL_COMMAND);
        viewSignal.addActionListener(this);
		viewSignal.setFont(font2);
		viewPeak.setSelected(s.viewParameters[4]);
		viewPeak.setActionCommand(PEAK_COMMAND);
        viewPeak.addActionListener(this);
		viewPeak.setFont(font2);
		viewFund.setSelected(s.viewParameters[5]);
		viewFund.setActionCommand(FUND_COMMAND);
        viewFund.addActionListener(this);
		viewFund.setFont(font2);
		viewMean.setSelected(s.viewParameters[6]);
		viewMean.setActionCommand(MEAN_COMMAND);
		viewMean.addActionListener(this);
		viewMean.setFont(font2);
		viewMedian.setSelected(s.viewParameters[7]);
		viewMedian.setActionCommand(MEDIAN_COMMAND);
        viewMedian.addActionListener(this);
		viewMedian.setFont(font2);
		viewHarm.setSelected(s.viewParameters[12]);
		viewHarm.setActionCommand(HARM_COMMAND);
        viewHarm.addActionListener(this);
		viewHarm.setFont(font2);
		viewWiener.setSelected(s.viewParameters[13]);
		viewWiener.setActionCommand(WIENER_COMMAND);
        viewWiener.addActionListener(this);
		viewWiener.setFont(font2);
		viewAmplitude.setSelected(s.viewParameters[15]);
		viewAmplitude.setActionCommand(AMPLITUDE_COMMAND);
        viewAmplitude.addActionListener(this);
		viewAmplitude.setFont(font2);
		viewPeakFreqChange.setSelected(s.viewParameters[8]);
		viewPeakFreqChange.setActionCommand(PEAK_FREQ_CHANGE_COMMAND);
        viewPeakFreqChange.addActionListener(this);
		viewPeakFreqChange.setFont(font2);
		viewFundFreqChange.setSelected(s.viewParameters[9]);
		viewFundFreqChange.setActionCommand(FUND_FREQ_CHANGE_COMMAND);
        viewFundFreqChange.addActionListener(this);
		viewFundFreqChange.setFont(font2);
		viewMeanFreqChange.setSelected(s.viewParameters[10]);
		viewMeanFreqChange.setActionCommand(MEAN_FREQ_CHANGE_COMMAND);
        viewMeanFreqChange.addActionListener(this);
		viewMeanFreqChange.setFont(font2);
		viewMedianFreqChange.setSelected(s.viewParameters[11]);
		viewMedianFreqChange.setActionCommand(MEDIAN_FREQ_CHANGE_COMMAND);
        viewMedianFreqChange.addActionListener(this);
		viewMedianFreqChange.setFont(font2);
		viewBandwidth.setSelected(s.viewParameters[14]);
		viewBandwidth.setActionCommand(BANDWIDTH_COMMAND);
        viewBandwidth.addActionListener(this);
		viewBandwidth.setFont(font2);
		viewElements.setSelected(s.viewParameters[1]);
		viewElements.setActionCommand(VIEW_ELEMENTS_COMMAND);
        viewElements.addActionListener(this);
		viewElements.setFont(font2);
		viewSyllables.setSelected(s.viewParameters[2]);
		viewSyllables.setActionCommand(VIEW_SYLLABLES_COMMAND);
        viewSyllables.addActionListener(this);
		viewSyllables.setFont(font2);
		viewTrillAmp.setSelected(s.viewParameters[17]);
		viewTrillAmp.setActionCommand(VIEW_TRILL_AMP_COMMAND);
        viewTrillAmp.addActionListener(this);
		viewTrillAmp.setFont(font2);
		viewTrillRate.setSelected(s.viewParameters[18]);
		viewTrillRate.setActionCommand(VIEW_TRILL_RATE_COMMAND);
        viewTrillRate.addActionListener(this);
		viewTrillRate.setFont(font2);
		viewReverb.setSelected(s.viewParameters[16]);
		viewReverb.setActionCommand(VIEW_REVERB_COMMAND);
        viewReverb.addActionListener(this);
		viewReverb.setFont(font2);
		
		
		SpringLayout slayout3=new SpringLayout();
		measurePane=new JPanel(slayout3);
		
		SpringLayout slayout4=new SpringLayout();
		parameterPane=new JPanel(slayout4);

		if (editMode){
			measurePane.add(mode);
			measurePane.add(erase);
			measurePane.add(undo);
			measurePane.add(redo);
			measurePane.add(automatic);
			measurePane.add(reestimate);
			measurePane.add(reestAll);
			measurePane.add(calculateSyllableStructure);
			//measurePane.add(selectModel);
		
			measurePane.add(delete);
			measurePane.add(deleteS);
			measurePane.add(merge);
			measurePane.add(brush);
			measurePane.add(harmonic);
			measurePane.add(minGap);
			measurePane.add(minLength);
			measurePane.add(deleteLE);
			measurePane.add(deleteLS);
			measurePane.add(mergeLE);
			measurePane.add(brushS);
			measurePane.add(harmonicL);
			measurePane.add(minGapL);
			measurePane.add(minLengthL);
			measurePane.add(upperLoop);
			measurePane.add(upperLoopL);
			measurePane.add(lowerLoop);
			measurePane.add(lowerLoopL);
			measurePane.add(jumpSuppressionL);
			measurePane.add(fundJumpSuppression);
		}
		else{
			measurePane.add(undo);
			measurePane.add(redo);
			measurePane.add(automatic);
		}
		
		parameterPane.add(viewElements);
		parameterPane.add(viewSyllables);
		parameterPane.add(viewSignal);
		parameterPane.add(viewPeak);
		parameterPane.add(viewFund);
		parameterPane.add(viewMean);
		parameterPane.add(viewMedian);
		parameterPane.add(viewHarm);
		parameterPane.add(viewWiener);
		parameterPane.add(viewAmplitude);
		parameterPane.add(viewPeakFreqChange);
		parameterPane.add(viewFundFreqChange);
		parameterPane.add(viewMeanFreqChange);
		parameterPane.add(viewMedianFreqChange);
		parameterPane.add(viewBandwidth);
		parameterPane.add(viewTrillAmp);
		parameterPane.add(viewTrillRate);
		parameterPane.add(viewReverb);
		
		if (editMode){
			slayout3.putConstraint(SpringLayout.WEST, mode, spring, SpringLayout.WEST, measurePane);
			slayout3.putConstraint(SpringLayout.NORTH, mode, 2, SpringLayout.NORTH, measurePane);
		
			slayout3.putConstraint(SpringLayout.WEST, erase, 0, SpringLayout.WEST, mode);
			slayout3.putConstraint(SpringLayout.NORTH, erase, spring, SpringLayout.SOUTH, mode);
			
			slayout3.putConstraint(SpringLayout.WEST, undo, 0, SpringLayout.WEST, mode);
			slayout3.putConstraint(SpringLayout.NORTH, undo, spring, SpringLayout.SOUTH, erase);
		
			slayout3.putConstraint(SpringLayout.WEST, redo, 0, SpringLayout.WEST, mode);
			slayout3.putConstraint(SpringLayout.NORTH, redo, spring, SpringLayout.SOUTH, undo);
		
			//slayout3.putConstraint(SpringLayout.WEST, selectModel, spring, SpringLayout.EAST, mode);
			//slayout3.putConstraint(SpringLayout.NORTH, selectModel, 2, SpringLayout.NORTH, measurePane);
		
			slayout3.putConstraint(SpringLayout.WEST, automatic, spring, SpringLayout.EAST, mode);
			slayout3.putConstraint(SpringLayout.NORTH, automatic, 2, SpringLayout.NORTH, measurePane);
		
			slayout3.putConstraint(SpringLayout.WEST, reestimate, 0, SpringLayout.WEST, automatic);
			slayout3.putConstraint(SpringLayout.NORTH, reestimate, spring, SpringLayout.SOUTH, automatic);
		
			slayout3.putConstraint(SpringLayout.WEST, reestAll, 0, SpringLayout.WEST, automatic);
			slayout3.putConstraint(SpringLayout.NORTH, reestAll, spring, SpringLayout.SOUTH, reestimate);
		
			slayout3.putConstraint(SpringLayout.WEST, calculateSyllableStructure, 0, SpringLayout.WEST, automatic);
			slayout3.putConstraint(SpringLayout.NORTH, calculateSyllableStructure, spring, SpringLayout.SOUTH, reestAll);
		
			slayout3.putConstraint(SpringLayout.WEST, deleteLE, spring, SpringLayout.EAST, calculateSyllableStructure);
			slayout3.putConstraint(SpringLayout.NORTH, deleteLE, 10, SpringLayout.NORTH, measurePane);
		
			slayout3.putConstraint(SpringLayout.WEST, delete, 5, SpringLayout.EAST, deleteLE);
			slayout3.putConstraint(SpringLayout.NORTH, delete, 5, SpringLayout.NORTH, measurePane);
		
			slayout3.putConstraint(SpringLayout.WEST, deleteS, 0, SpringLayout.WEST, delete);
			slayout3.putConstraint(SpringLayout.NORTH, deleteS, spring, SpringLayout.SOUTH, delete);
		
			slayout3.putConstraint(SpringLayout.WEST, merge, 0, SpringLayout.WEST, delete);
			slayout3.putConstraint(SpringLayout.NORTH, merge, spring, SpringLayout.SOUTH, deleteS);
		
			slayout3.putConstraint(SpringLayout.EAST, deleteLS, -5, SpringLayout.WEST, deleteS);
			slayout3.putConstraint(SpringLayout.NORTH, deleteLS, 2, SpringLayout.NORTH, deleteS);
		
			slayout3.putConstraint(SpringLayout.EAST, mergeLE, -5, SpringLayout.WEST, merge);
			slayout3.putConstraint(SpringLayout.NORTH, mergeLE, 2, SpringLayout.NORTH, merge);
		
			slayout3.putConstraint(SpringLayout.WEST, jumpSuppressionL, spring, SpringLayout.EAST, delete);
			slayout3.putConstraint(SpringLayout.NORTH, jumpSuppressionL, 10, SpringLayout.NORTH, measurePane);
		
			slayout3.putConstraint(SpringLayout.WEST, fundJumpSuppression, 5, SpringLayout.EAST, jumpSuppressionL);
			slayout3.putConstraint(SpringLayout.NORTH, fundJumpSuppression, 5, SpringLayout.NORTH, measurePane);
		
			slayout3.putConstraint(SpringLayout.WEST, harmonic, 0, SpringLayout.WEST, fundJumpSuppression);
			slayout3.putConstraint(SpringLayout.NORTH, harmonic, spring, SpringLayout.SOUTH, fundJumpSuppression);
		
			slayout3.putConstraint(SpringLayout.EAST, harmonicL, -5, SpringLayout.WEST, harmonic);
			slayout3.putConstraint(SpringLayout.NORTH, harmonicL, 2, SpringLayout.NORTH, harmonic);
		
			slayout3.putConstraint(SpringLayout.WEST, minGap,  0, SpringLayout.WEST, fundJumpSuppression);
			slayout3.putConstraint(SpringLayout.NORTH, minGap,  spring, SpringLayout.SOUTH, harmonic);
		
			slayout3.putConstraint(SpringLayout.EAST, minGapL, -5, SpringLayout.WEST, minGap);
			slayout3.putConstraint(SpringLayout.NORTH, minGapL, 2, SpringLayout.NORTH, minGap);
		
			slayout3.putConstraint(SpringLayout.WEST, brush, 0, SpringLayout.WEST, minGap);
			slayout3.putConstraint(SpringLayout.NORTH, brush, spring, SpringLayout.SOUTH, minGap);
				
			slayout3.putConstraint(SpringLayout.EAST, brushS, -5, SpringLayout.WEST, brush);
			slayout3.putConstraint(SpringLayout.NORTH, brushS, 2, SpringLayout.NORTH, brush);
		
			slayout3.putConstraint(SpringLayout.WEST, upperLoopL, spring, SpringLayout.EAST, brush);
			slayout3.putConstraint(SpringLayout.NORTH, upperLoopL, 10, SpringLayout.NORTH, measurePane);
		
			slayout3.putConstraint(SpringLayout.WEST, upperLoop, 5, SpringLayout.EAST, upperLoopL);
			slayout3.putConstraint(SpringLayout.NORTH, upperLoop, 5, SpringLayout.NORTH, measurePane);
		
			slayout3.putConstraint(SpringLayout.WEST, lowerLoop, 0, SpringLayout.WEST, upperLoop);
			slayout3.putConstraint(SpringLayout.NORTH, lowerLoop, spring, SpringLayout.SOUTH, upperLoop);
		
			slayout3.putConstraint(SpringLayout.EAST, lowerLoopL, -5, SpringLayout.WEST, lowerLoop);
			slayout3.putConstraint(SpringLayout.NORTH, lowerLoopL, 0, SpringLayout.NORTH, lowerLoop);
		
			slayout3.putConstraint(SpringLayout.WEST, minLength, 0, SpringLayout.WEST, lowerLoop);
			slayout3.putConstraint(SpringLayout.NORTH, minLength, spring, SpringLayout.SOUTH, lowerLoop);
			
			slayout3.putConstraint(SpringLayout.EAST, minLengthL, -5, SpringLayout.WEST, minLength);
			slayout3.putConstraint(SpringLayout.NORTH, minLengthL, 0, SpringLayout.NORTH, minLength);
			
			//slayout3.putConstraint(SpringLayout.SOUTH, parameterPane, spring, SpringLayout.SOUTH, brushS);
			slayout3.putConstraint(SpringLayout.SOUTH, measurePane, spring, SpringLayout.SOUTH, calculateSyllableStructure);
		}
		else{
			slayout3.putConstraint(SpringLayout.WEST, undo, spring, SpringLayout.WEST, measurePane);
			slayout3.putConstraint(SpringLayout.NORTH, undo, 2, SpringLayout.NORTH, measurePane);
		
			slayout3.putConstraint(SpringLayout.WEST, redo, 0, SpringLayout.WEST, undo);
			slayout3.putConstraint(SpringLayout.NORTH, redo, spring, SpringLayout.SOUTH, undo);
		
			slayout3.putConstraint(SpringLayout.WEST, automatic, spring, SpringLayout.EAST, undo);
			slayout3.putConstraint(SpringLayout.NORTH, automatic, 2, SpringLayout.NORTH, measurePane);
		
		}
		slayout4.putConstraint(SpringLayout.WEST, viewElements, spring, SpringLayout.WEST, parameterPane);
		slayout4.putConstraint(SpringLayout.NORTH, viewElements, 5, SpringLayout.NORTH, parameterPane);
		
		slayout4.putConstraint(SpringLayout.WEST, viewSyllables, 0, SpringLayout.WEST, viewElements);
		slayout4.putConstraint(SpringLayout.NORTH, viewSyllables, spring, SpringLayout.SOUTH, viewElements);
		
		slayout4.putConstraint(SpringLayout.WEST, viewSignal, 0, SpringLayout.WEST, viewElements);
		slayout4.putConstraint(SpringLayout.NORTH, viewSignal, spring, SpringLayout.SOUTH, viewSyllables);
		
		slayout4.putConstraint(SpringLayout.WEST, viewHarm, spring, SpringLayout.EAST, viewElements);
		slayout4.putConstraint(SpringLayout.NORTH, viewHarm, 5, SpringLayout.NORTH, parameterPane);
		
		slayout4.putConstraint(SpringLayout.WEST, viewWiener, 0, SpringLayout.WEST, viewHarm);
		slayout4.putConstraint(SpringLayout.NORTH, viewWiener, spring, SpringLayout.SOUTH, viewHarm);
		
		slayout4.putConstraint(SpringLayout.WEST, viewAmplitude, 0, SpringLayout.WEST, viewHarm);
		slayout4.putConstraint(SpringLayout.NORTH, viewAmplitude, spring, SpringLayout.SOUTH, viewWiener);

		slayout4.putConstraint(SpringLayout.WEST, viewBandwidth, 0, SpringLayout.WEST, viewHarm);
		slayout4.putConstraint(SpringLayout.NORTH, viewBandwidth, spring, SpringLayout.SOUTH, viewAmplitude);

		slayout4.putConstraint(SpringLayout.WEST, viewPeak, spring, SpringLayout.EAST, viewWiener);
		slayout4.putConstraint(SpringLayout.NORTH, viewPeak,  5, SpringLayout.NORTH, parameterPane);
		
		slayout4.putConstraint(SpringLayout.WEST, viewFund, 0, SpringLayout.WEST, viewPeak);
		slayout4.putConstraint(SpringLayout.NORTH, viewFund, spring, SpringLayout.SOUTH, viewPeak);
		
		slayout4.putConstraint(SpringLayout.WEST, viewMean, 0, SpringLayout.WEST, viewPeak);
		slayout4.putConstraint(SpringLayout.NORTH, viewMean, spring, SpringLayout.SOUTH, viewFund);
		
		slayout4.putConstraint(SpringLayout.WEST, viewMedian, 0, SpringLayout.WEST, viewPeak);
		slayout4.putConstraint(SpringLayout.NORTH, viewMedian, spring, SpringLayout.SOUTH, viewMean);
		
		slayout4.putConstraint(SpringLayout.WEST, viewPeakFreqChange, spring, SpringLayout.EAST, viewPeak);
		slayout4.putConstraint(SpringLayout.NORTH, viewPeakFreqChange, 5, SpringLayout.NORTH, parameterPane);

		slayout4.putConstraint(SpringLayout.WEST, viewFundFreqChange, 0, SpringLayout.WEST, viewPeakFreqChange);
		slayout4.putConstraint(SpringLayout.NORTH, viewFundFreqChange, spring, SpringLayout.SOUTH, viewPeakFreqChange);
		
		slayout4.putConstraint(SpringLayout.WEST, viewMeanFreqChange, 0, SpringLayout.WEST, viewPeakFreqChange);
		slayout4.putConstraint(SpringLayout.NORTH, viewMeanFreqChange, spring, SpringLayout.SOUTH, viewFundFreqChange);
		
		slayout4.putConstraint(SpringLayout.WEST, viewMedianFreqChange, 0, SpringLayout.WEST, viewPeakFreqChange);
		slayout4.putConstraint(SpringLayout.NORTH, viewMedianFreqChange, spring, SpringLayout.SOUTH, viewMeanFreqChange);
		
		slayout4.putConstraint(SpringLayout.WEST, viewTrillAmp, spring, SpringLayout.EAST, viewPeakFreqChange);
		slayout4.putConstraint(SpringLayout.NORTH, viewTrillAmp, 5, SpringLayout.NORTH, parameterPane);

		slayout4.putConstraint(SpringLayout.WEST, viewTrillRate, 0, SpringLayout.WEST, viewTrillAmp);
		slayout4.putConstraint(SpringLayout.NORTH, viewTrillRate, spring, SpringLayout.SOUTH, viewTrillAmp);

		slayout4.putConstraint(SpringLayout.WEST, viewReverb, 0, SpringLayout.WEST, viewTrillRate);
		slayout4.putConstraint(SpringLayout.NORTH, viewReverb, spring, SpringLayout.SOUTH, viewTrillRate);

		
		slayout4.putConstraint(SpringLayout.SOUTH, parameterPane, spring, SpringLayout.SOUTH, viewMedianFreqChange);
		
		tabPane.addTab("Measurements", measurePane);
		if (editMode){	
			tabPane.addTab("Parameters", parameterPane);
		}
		SpringLayout slayout5=new SpringLayout();
		recordPane=new JPanel(slayout5);
				
		cal.setTimeInMillis(song.getTDate());
		Date initDate = cal.getTime();
        cal.setTimeInMillis(System.currentTimeMillis());
		Date latestDate = cal.getTime();
		cal.add(Calendar.YEAR, -50);
		Date earliestDate = cal.getTime();
		cal.setTimeInMillis(song.getTDate());
		
        dateModel = new SpinnerDateModel(initDate, earliestDate,latestDate, Calendar.DAY_OF_MONTH);
		dateSpinner=new JSpinner(dateModel);
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "dd/MM/yyyy"));
		
		
		cal.setTimeInMillis(song.getTDate());
		Date initTime = cal.getTime();
		
		cal.add(Calendar.HOUR, 24);
					
		cal.add(Calendar.HOUR, -48);
		
		cal.setTimeInMillis(song.getTDate());
		
		timeModel = new SpinnerDateModel(initTime, earliestDate, latestDate, Calendar.MILLISECOND);
		timeSpinner=new JSpinner(timeModel);
        timeSpinner.setEditor(new JSpinner.DateEditor(timeSpinner, "HH:mm:ss:SSS"));
		timeModel.setCalendarField(Calendar.MILLISECOND);
		
		
		previousDetails.setMnemonic(KeyEvent.VK_P);
		previousDetails.setFont(font2);
		previousDetails.setDisplayedMnemonicIndex(0);
		previousDetails.setActionCommand(PREVIOUS_COMMAND);
        previousDetails.addActionListener(this);
		
		locationField.setFont(font2);
		locationField.setText(song.getLocation());
		
		notesField.setFont(font2);
		notesField.setBorder(BorderFactory.createLoweredBevelBorder());
		notesField.setText(song.getNotes());
		notesField.setColumns(20);
		notesField.setRows(6);
		notesField.setLineWrap(true);
		notesField.setWrapStyleWord(true);
		notesField.setDragEnabled(true);
		
		equipmentField.setFont(font2);
		equipmentField.setText(song.getRecordEquipment());
		
		recordistField.setFont(font2);
		recordistField.setText(song.getRecordist());
		
		recordPane.add(previousDetails);
		recordPane.add(timeL);
		recordPane.add(timeSpinner);
		recordPane.add(dateSpinner);
		recordPane.add(dateL);
		recordPane.add(locationField);
		recordPane.add(notesField);
		recordPane.add(equipmentField);
		recordPane.add(recordistField);
		recordPane.add(locationL);
		recordPane.add(notesL);
		recordPane.add(equipmentL);
		recordPane.add(recordistL);
		
		slayout5.putConstraint(SpringLayout.WEST, previousDetails, spring, SpringLayout.WEST, recordPane);
		slayout5.putConstraint(SpringLayout.NORTH, previousDetails, 2, SpringLayout.NORTH, recordPane);
		
		slayout5.putConstraint(SpringLayout.WEST, timeL, 0, SpringLayout.WEST, previousDetails);
		slayout5.putConstraint(SpringLayout.NORTH, timeL, 20, SpringLayout.SOUTH, previousDetails);
		
		slayout5.putConstraint(SpringLayout.WEST, timeSpinner, spring, SpringLayout.EAST, timeL);
		slayout5.putConstraint(SpringLayout.NORTH, timeSpinner, 0, SpringLayout.NORTH, timeL);
		
		slayout5.putConstraint(SpringLayout.WEST, dateL, 0, SpringLayout.WEST, previousDetails);
		slayout5.putConstraint(SpringLayout.NORTH, dateL, spring, SpringLayout.SOUTH, timeSpinner);
		
		slayout5.putConstraint(SpringLayout.WEST, dateSpinner, spring, SpringLayout.EAST, dateL);
		slayout5.putConstraint(SpringLayout.NORTH, dateSpinner, 0, SpringLayout.NORTH, dateL);
	
		slayout5.putConstraint(SpringLayout.WEST, locationL, spring, SpringLayout.EAST, timeSpinner);
		slayout5.putConstraint(SpringLayout.NORTH, locationL, 2, SpringLayout.NORTH, recordPane);
		
		slayout5.putConstraint(SpringLayout.WEST, locationField, 5, SpringLayout.EAST, locationL);
		slayout5.putConstraint(SpringLayout.NORTH, locationField, 2, SpringLayout.NORTH, recordPane);
		
		slayout5.putConstraint(SpringLayout.WEST, recordistField, 0, SpringLayout.WEST, locationField);
		slayout5.putConstraint(SpringLayout.NORTH, recordistField, spring, SpringLayout.SOUTH, locationField);
		
		slayout5.putConstraint(SpringLayout.WEST, equipmentField, 0, SpringLayout.WEST, locationField);
		slayout5.putConstraint(SpringLayout.NORTH, equipmentField, spring, SpringLayout.SOUTH, recordistField);
		
		slayout5.putConstraint(SpringLayout.EAST, recordistL, -5, SpringLayout.WEST, recordistField);
		slayout5.putConstraint(SpringLayout.NORTH, recordistL, 0, SpringLayout.NORTH, recordistField);
		
		slayout5.putConstraint(SpringLayout.EAST, equipmentL, -5, SpringLayout.WEST, equipmentField);
		slayout5.putConstraint(SpringLayout.NORTH, equipmentL, 0, SpringLayout.NORTH, equipmentField);
		
		slayout5.putConstraint(SpringLayout.WEST, notesL, spring, SpringLayout.EAST, locationField);
		slayout5.putConstraint(SpringLayout.NORTH, notesL, 2, SpringLayout.NORTH, recordPane);
		
		slayout5.putConstraint(SpringLayout.WEST, notesField, spring, SpringLayout.EAST, notesL);
		slayout5.putConstraint(SpringLayout.NORTH, notesField, 2, SpringLayout.NORTH, recordPane);
				
		tabPane.addTab("Records", recordPane);
		
	
	
		
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		String[] systemFamilies = ge.getAvailableFontFamilyNames();
		
		fontFamilies=new String[systemFamilies.length+commonFamilies.length];
		for (int i=0; i<commonFamilies.length; i++){
			fontFamilies[i]=commonFamilies[i];
		}
		for (int i=0; i<systemFamilies.length; i++){
			fontFamilies[i+commonFamilies.length]=systemFamilies[i];
		}
		
		fontFace=new JComboBox(fontFamilies);
		fontFace.setMaximumRowCount(25);
		fontFace.setFont(font2);
		fontFaceL.setLabelFor(fontFace);
		fontFace.addActionListener(this);
		fontFace.setActionCommand(FONT_FACE);
		axisLabelFontStyle=new JComboBox(fontStyles);
		axisLabelFontStyle.setFont(font2);
		axisLabelFontStyle.addActionListener(this);
		axisLabelFontStyle.setActionCommand(AXIS_LABEL_FONT_STYLE);
		tickLabelFontStyle=new JComboBox(fontStyles);
		tickLabelFontStyle.setFont(font2);
		tickLabelFontStyle.addActionListener(this);
		tickLabelFontStyle.setActionCommand(TICK_LABEL_FONT_STYLE);
		
		SpinnerNumberModel snm1=new SpinnerNumberModel(5, 0, 20, 1);
		SpinnerNumberModel snm2=new SpinnerNumberModel(5, 0, 20, 1);
		majorTickMarkLength=new JSpinner(snm1);
		minorTickMarkLength=new JSpinner(snm2);
		
		majorTickMarkLengthL.setLabelFor(majorTickMarkLength);
		majorTickMarkLength.addChangeListener(this);
		majorTickMarkLength.setFont(font2);
		minorTickMarkLengthL.setLabelFor(minorTickMarkLength);
		minorTickMarkLength.addChangeListener(this);
		minorTickMarkLength.setFont(font2);
		
		SpinnerNumberModel snm5=new SpinnerNumberModel(1, 1, 5, 1);
		lineWeight=new JSpinner(snm5);
		lineWeightL.setLabelFor(lineWeight);
		lineWeight.setFont(font);
		lineWeight.addChangeListener(this);
		
		
		SpinnerNumberModel snm3=new SpinnerNumberModel(14, 6, 50, 1);
		SpinnerNumberModel snm4=new SpinnerNumberModel(12, 6, 50, 1);
		axisLabelFontSize=new JSpinner(snm3);
		tickLabelFontSize=new JSpinner(snm4);
		
		axisLabelFontSize.addChangeListener(this);
		axisLabelFontSize.setFont(font2);
		tickLabelFontSize.addChangeListener(this);
		tickLabelFontSize.setFont(font2);
		
		timeUnitS.setSelected(true);
		timeUnitS.setFont(font2);
		timeUnitMS.setFont(font2);
		ButtonGroup timeUnitBG=new ButtonGroup();
		timeUnitBG.add(timeUnitS);
		timeUnitBG.add(timeUnitMS);
		
		timeUnitS.addActionListener(this);
		timeUnitS.setActionCommand(TIME_UNIT_S);
		timeUnitMS.addActionListener(this);
		timeUnitMS.setActionCommand(TIME_UNIT_MS);
		
		freqUnitKHZ.setSelected(true);
		freqUnitKHZ.setFont(font2);
		freqUnitHZ.setFont(font2);
		ButtonGroup freqUnitBG=new ButtonGroup();
		freqUnitBG.add(freqUnitKHZ);
		freqUnitBG.add(freqUnitHZ);
		
		freqUnitHZ.addActionListener(this);
		freqUnitHZ.setActionCommand(FREQ_UNIT_HZ);
		freqUnitKHZ.addActionListener(this);
		freqUnitKHZ.setActionCommand(FREQ_UNIT_KHZ);
				
		showFrame.setSelected(true);
		showFrame.setFont(font2);
		showFrame.addActionListener(this);
		showFrame.setActionCommand(SHOW_FRAME);
		
		interiorTickMarkLabels.setFont(font2);
		interiorTickMarkLabels.addActionListener(this);
		interiorTickMarkLabels.setActionCommand(INTERIOR_TICK_MARK);
		
		showMajorTimeTickMark.setFont(font2);
		showMinorTimeTickMark.setFont(font2);
		showMajorFreqTickMark.setFont(font2);
		showMinorFreqTickMark.setFont(font2);
		
		showMajorTimeTickMark.setSelected(true);
		showMajorTimeTickMark.addActionListener(this);
		showMajorTimeTickMark.setActionCommand(SHOW_MAJOR_TIME_TICK_MARK);
		showMinorTimeTickMark.setSelected(true);
		showMinorTimeTickMark.addActionListener(this);
		showMinorTimeTickMark.setActionCommand(SHOW_MINOR_TIME_TICK_MARK);
		showMajorFreqTickMark.setSelected(true);
		showMajorFreqTickMark.addActionListener(this);
		showMajorFreqTickMark.setActionCommand(SHOW_MAJOR_FREQ_TICK_MARK);
		showMinorFreqTickMark.setSelected(true);
		showMinorFreqTickMark.addActionListener(this);
		showMinorFreqTickMark.setActionCommand(SHOW_MINOR_FREQ_TICK_MARK);
		
		
		SpringLayout slayout6=new SpringLayout();
		appearancePane=new JPanel(slayout6);
		
		appearancePane.add(fontFace);
		appearancePane.add(fontFaceL);
		appearancePane.add(axisLabelFontStyle);
		appearancePane.add(axisLabelL);
		appearancePane.add(tickLabelFontStyle);
		appearancePane.add(tickLabelL);
		appearancePane.add(axisLabelFontSize);
		appearancePane.add(fontSizeL);
		appearancePane.add(tickLabelFontSize);
		appearancePane.add(fontStyleL);
		
		appearancePane.add(freqUnitL);
		appearancePane.add(freqUnitKHZ);
		appearancePane.add(freqUnitHZ);
		appearancePane.add(timeUnitL);
		appearancePane.add(timeUnitS);
		appearancePane.add(timeUnitMS);
		
		appearancePane.add(showFrame);
		appearancePane.add(interiorTickMarkLabels);
		
		appearancePane.add(showMajorTimeTickMark);
		appearancePane.add(showMinorTimeTickMark);
		appearancePane.add(showMajorFreqTickMark);
		appearancePane.add(showMinorFreqTickMark);
		
		appearancePane.add(majorTickMarkLength);
		appearancePane.add(majorTickMarkLengthL);
		appearancePane.add(minorTickMarkLength);
		appearancePane.add(minorTickMarkLengthL);
		appearancePane.add(lineWeight);
		appearancePane.add(lineWeightL);
		
		int ha=-5;
		
		
		slayout6.putConstraint(SpringLayout.WEST, fontFaceL, spring, SpringLayout.WEST, appearancePane);
		slayout6.putConstraint(SpringLayout.NORTH, fontFaceL, 7, SpringLayout.NORTH, appearancePane);
		
		slayout6.putConstraint(SpringLayout.WEST, fontFace, spring, SpringLayout.EAST, fontFaceL);
		slayout6.putConstraint(SpringLayout.NORTH, fontFace, ha, SpringLayout.NORTH, fontFaceL);
		
		
		slayout6.putConstraint(SpringLayout.WEST, axisLabelL, 0, SpringLayout.WEST, fontFace);
		slayout6.putConstraint(SpringLayout.NORTH, axisLabelL, spring, SpringLayout.SOUTH, fontFace);
		
		slayout6.putConstraint(SpringLayout.WEST, axisLabelFontStyle, 0, SpringLayout.WEST, axisLabelL);
		slayout6.putConstraint(SpringLayout.NORTH, axisLabelFontStyle, spring, SpringLayout.SOUTH, axisLabelL);
		
		slayout6.putConstraint(SpringLayout.WEST, tickLabelFontStyle, spring, SpringLayout.EAST, axisLabelFontStyle);
		slayout6.putConstraint(SpringLayout.NORTH, tickLabelFontStyle, 0, SpringLayout.NORTH, axisLabelFontStyle);
		
		slayout6.putConstraint(SpringLayout.WEST, fontStyleL, 0, SpringLayout.WEST, fontFaceL);
		slayout6.putConstraint(SpringLayout.NORTH, fontStyleL, -1*ha, SpringLayout.NORTH, axisLabelFontStyle);
		
		slayout6.putConstraint(SpringLayout.WEST, tickLabelL, 0, SpringLayout.WEST, tickLabelFontStyle);
		slayout6.putConstraint(SpringLayout.NORTH, tickLabelL, 0, SpringLayout.NORTH, axisLabelL);
		
		slayout6.putConstraint(SpringLayout.WEST, axisLabelFontSize, 0, SpringLayout.WEST, axisLabelL);
		slayout6.putConstraint(SpringLayout.NORTH, axisLabelFontSize, spring, SpringLayout.SOUTH, axisLabelFontStyle);
		
		slayout6.putConstraint(SpringLayout.WEST, tickLabelFontSize, 0, SpringLayout.WEST, tickLabelFontStyle);
		slayout6.putConstraint(SpringLayout.NORTH, tickLabelFontSize, 0, SpringLayout.NORTH, axisLabelFontSize);
				
		slayout6.putConstraint(SpringLayout.WEST, fontSizeL, 0, SpringLayout.WEST, fontFaceL);
		slayout6.putConstraint(SpringLayout.NORTH, fontSizeL, -1*ha, SpringLayout.NORTH, axisLabelFontSize);
		
		
		
		
		slayout6.putConstraint(SpringLayout.WEST, freqUnitL, spring, SpringLayout.EAST, fontFace);
		slayout6.putConstraint(SpringLayout.NORTH, freqUnitL, 7, SpringLayout.NORTH, appearancePane);
		
		slayout6.putConstraint(SpringLayout.WEST, freqUnitKHZ, spring, SpringLayout.EAST, freqUnitL);
		slayout6.putConstraint(SpringLayout.NORTH, freqUnitKHZ, ha, SpringLayout.NORTH, freqUnitL);
		
		slayout6.putConstraint(SpringLayout.WEST, freqUnitHZ, spring, SpringLayout.EAST, freqUnitKHZ);
		slayout6.putConstraint(SpringLayout.NORTH, freqUnitHZ, 0, SpringLayout.NORTH, freqUnitKHZ);
		
		slayout6.putConstraint(SpringLayout.WEST, timeUnitL, 0, SpringLayout.WEST, freqUnitL);
		slayout6.putConstraint(SpringLayout.NORTH, timeUnitL, spring, SpringLayout.SOUTH, freqUnitKHZ);
		
		slayout6.putConstraint(SpringLayout.WEST, timeUnitS, 0, SpringLayout.WEST, freqUnitKHZ);
		slayout6.putConstraint(SpringLayout.NORTH, timeUnitS, ha, SpringLayout.NORTH, timeUnitL);
		
		slayout6.putConstraint(SpringLayout.WEST, timeUnitMS, 0, SpringLayout.WEST, freqUnitHZ);
		slayout6.putConstraint(SpringLayout.NORTH, timeUnitMS, 0, SpringLayout.NORTH, timeUnitS);
		
		slayout6.putConstraint(SpringLayout.WEST, showFrame, 0, SpringLayout.WEST, timeUnitL);
		slayout6.putConstraint(SpringLayout.NORTH, showFrame, spring, SpringLayout.SOUTH, timeUnitS);
		
		slayout6.putConstraint(SpringLayout.WEST, interiorTickMarkLabels, 0, SpringLayout.WEST, showFrame);
		slayout6.putConstraint(SpringLayout.NORTH, interiorTickMarkLabels, spring, SpringLayout.SOUTH, showFrame);
		
		slayout6.putConstraint(SpringLayout.WEST, showMajorTimeTickMark, spring, SpringLayout.EAST, freqUnitHZ);
		slayout6.putConstraint(SpringLayout.NORTH, showMajorTimeTickMark, 3, SpringLayout.NORTH, appearancePane);
		
		slayout6.putConstraint(SpringLayout.WEST, showMinorTimeTickMark, 0, SpringLayout.WEST, showMajorTimeTickMark);
		slayout6.putConstraint(SpringLayout.NORTH, showMinorTimeTickMark, spring, SpringLayout.SOUTH, showMajorTimeTickMark);
		
		slayout6.putConstraint(SpringLayout.WEST, showMajorFreqTickMark, 0, SpringLayout.WEST, showMinorTimeTickMark);
		slayout6.putConstraint(SpringLayout.NORTH, showMajorFreqTickMark, spring, SpringLayout.SOUTH, showMinorTimeTickMark);
		
		slayout6.putConstraint(SpringLayout.WEST, showMinorFreqTickMark, 0, SpringLayout.WEST, showMajorFreqTickMark);
		slayout6.putConstraint(SpringLayout.NORTH, showMinorFreqTickMark, spring, SpringLayout.SOUTH, showMajorFreqTickMark);

		slayout6.putConstraint(SpringLayout.WEST, majorTickMarkLengthL, spring, SpringLayout.EAST, showMajorFreqTickMark);
		slayout6.putConstraint(SpringLayout.NORTH, majorTickMarkLengthL, 7, SpringLayout.NORTH, appearancePane);
		
		slayout6.putConstraint(SpringLayout.WEST, majorTickMarkLength, spring, SpringLayout.EAST, majorTickMarkLengthL);
		slayout6.putConstraint(SpringLayout.NORTH, majorTickMarkLength, ha, SpringLayout.NORTH, majorTickMarkLengthL);
		
		slayout6.putConstraint(SpringLayout.WEST, minorTickMarkLengthL, 0, SpringLayout.WEST, majorTickMarkLengthL);
		slayout6.putConstraint(SpringLayout.NORTH, minorTickMarkLengthL, spring, SpringLayout.SOUTH, majorTickMarkLength);
		
		slayout6.putConstraint(SpringLayout.WEST, minorTickMarkLength, 0, SpringLayout.WEST, majorTickMarkLength);
		slayout6.putConstraint(SpringLayout.NORTH, minorTickMarkLength, ha, SpringLayout.NORTH, minorTickMarkLengthL);

		slayout6.putConstraint(SpringLayout.WEST, lineWeightL, 0, SpringLayout.WEST, minorTickMarkLengthL);
		slayout6.putConstraint(SpringLayout.NORTH, lineWeightL, spring, SpringLayout.SOUTH, minorTickMarkLength);
		
		slayout6.putConstraint(SpringLayout.WEST, lineWeight, 0, SpringLayout.WEST, minorTickMarkLength);
		slayout6.putConstraint(SpringLayout.NORTH, lineWeight, ha, SpringLayout.NORTH, lineWeightL);
		
		slayout6.putConstraint(SpringLayout.SOUTH, appearancePane, spring, SpringLayout.SOUTH, showMinorFreqTickMark);
		
		tabPane.addTab("Appearance", appearancePane);

		
		f.requestFocus();
		String s=song.getName();
		f.setTitle(s);
		f.getContentPane().add(this);
		f.pack();
		f.setVisible(true);
		f.addWindowListener(this);
		started=true;
		
	}
	
		
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		//this.setRequestFocusEnabled(true);
		this.requestFocusInWindow();
		if (LISTEN_TO_DROP_DOWNS){
			if (DELETE_COMMAND.equals(command)) {
				JComboBox cb = (JComboBox)e.getSource();
				int ind=cb.getSelectedIndex()-2;
				
				int ind2=song.getNumElements()-ind-1;
				delete.setSelectedIndex(0);
				if (ind>=0){
					song.removeElement(ind2);
				}
				if (ind==-1){
					song.clearElements();
				}
				s.paintFound();
				gp.draw();
				updateElementLists();
				cloneLists();
				redo.setEnabled(false);
			}
			if (DELETES_COMMAND.equals(command)) {
				player=false;
				JComboBox cb = (JComboBox)e.getSource();
				int ind=cb.getSelectedIndex()-2;
				int ind2=song.getNumSyllables()-ind-1;
				
				if (ind>=0){
					song.removeSyllable(ind2);
				}
				if (ind==-1){
					song.clearSyllables();
				}
				updateSyllableLists();
				player=true;
				s.paintFound();
				gp.draw();
				cloneLists();
				redo.setEnabled(false);
			}
			if (MERGE_COMMAND.equals(command)) {
				JComboBox cb = (JComboBox)e.getSource();
				int ind=cb.getSelectedIndex()-1;
				if (ind>-1){
					s.merge(ind);
					merge.setSelectedIndex(0);
					cloneLists();
					updateElementLists();
				}
			}
			if (PLAY_SPEED_COMMAND.equals(command)) {
				JComboBox cb = (JComboBox)e.getSource();
				int ind=cb.getSelectedIndex();
				int p=0;
				if (ind==0){p=1;}
				else if (ind==1){p=2;}
				else if (ind==2){p=4;}
				else if (ind==3){p=8;}
				song.setPlaybackDivider(p);
				song.setUpPlayback();
			}
			if (PLAY_COMMAND.equals(command)) {
				if ((player)&&(song.getNumSyllables()>0)){
					JComboBox cb = (JComboBox)e.getSource();
					int ind=cb.getSelectedIndex()-1;
					ind=song.getNumSyllables()-ind-1;
					int[] syll=(int[])song.getSyllable(ind);
					double[] x=song.prepPlayback(syll);
					drawProgress(x[0], x[1], song.getPlaybackDivider());
					
					//I'VE MOVED MOST OF THE BELOW TO SONG.PREPPLAYBACK. CHECK FOR BUGS!
					/*int a=0;
					int b=0;
					ind=song.getNumSyllables()-ind-1;
					int[] syll=(int[])song.getSyllable(ind);
					
					
					
					
					if (song.getSizeInBits()<=16){
						int p1=syll[0]-7;
						if (p1<0){p1=0;}
						int p2=syll[1]+7;
						if (p2>=song.getOverallLength()){
							p2=song.getOverallLength()-1;
						}
						a=(int)(p1*Math.round(song.getSampleRate()*song.getStereo()*0.002));
						b=(int)(p2*Math.round(song.getSampleRate()*song.getStereo()*0.002));
					}
					if(song.getRawData.length<=b){
						b=a;
					}		
					
					song.playSound(a, b);
					
					drawProgress(a/song.rawData.length, b/song.rawData.length, song.playbackDivider);
					*/
				}
			}
			if (CLICK_DRAG.equals(command)){
				s.setClickDrag(clickDrag.isSelected());
				
			}
		}
		
		if (DISPLAY_MODE.equals(command)) {
			if (s.displayMode==0){
				s.displayMode=1;
				displayMode.setText("Display mode: amplitude envelope");
				song.setBrushType(2);
				brush.setEditable(false);
				brush.setEnabled(false);
				s.paintFound();
			}
			else if (s.displayMode==1){
				s.displayMode=2;
				displayMode.setText("Display mode: pitch");
				
				s.updatePixelValsPitchThreaded(s.im);
				s.paintFrame();
				s.paintFound();
			}
			else{
				s.displayMode=0;
				displayMode.setText("Display mode: spectrogram");
				if (s.syllable=false){
					resetBrush();
					brush.setEditable(true);
					brush.setEnabled(true);
				}
				s.updatePixelVals(s.im);
				s.paintFrame();
				s.paintFound();
			}
		
		}
		
		if (PLAY_ALL_COMMAND.equals(command)) {
			if (player){
				song.playSound(0, song.getRawDataLength());
				drawProgress(0, 1, song.getPlaybackDivider());
			}
		}
		
		if (PLAY_SCREEN_COMMAND.equals(command)) {
			if (player){
				System.out.println(s.dx);
				
				song.prepPlaybackScreen(s.getCurrentMinX(), s.getCurrentMaxX());
				
				drawProgress(s.getCurrentMinX()/(song.getNx()+0.0), s.getCurrentMaxX()/(song.getNx()+0.0), song.getPlaybackDivider());
			}
		}
		
		if (STOP_COMMAND.equals(command)) {
			if (player){
				song.stopSound();
				stopDrawingProgress();
			}
		}

		if (UNDO_COMMAND.equals(command)) {
			System.out.println("undo1 "+undoList.size());
			int a=undoList.size();
			LinkedList p=undoList.get(a-2);
			song.setEleList((LinkedList)p.get(0));
			song.setSyllList((LinkedList)p.get(1));
			LinkedList q=undoList.get(a-1);
			redoList.add(q);
			undoList.removeLast();
			undoList.removeLast();
			cloneLists();
			if (a<=2) {undo.setEnabled(false);}
			s.paintFound();
			gp.draw();
			updateElementLists();
			updateSyllableLists();
			System.out.println("undo2 "+undoList.size());
			redo.setEnabled(true);
		}
		
		if (REDO_COMMAND.equals(command)) {
			System.out.println("redo"+undoList.size());
			LinkedList<LinkedList<Object>> p=redoList.get(redoList.size()-1);
			song.setEleList((LinkedList)p.get(0));
			song.setSyllList((LinkedList)p.get(1));
			redoList.removeLast();
			undoList.add(p);
			s.paintFound();
			gp.draw();
			updateElementLists();
			updateSyllableLists();
			if (redoList.size()==0){
				redo.setEnabled(false);
			}
			if (!undo.isEnabled()){
				undo.setEnabled(true);
			}
			System.out.println("redo"+redoList.size());
		}
		
		if (UPDATE_COMMAND.equals(command)) {replot();}
		if (AUTOMATIC_COMMAND.equals(command)) {
			if (editMode){
				s.selectAll();
				cloneLists();
			}
			else{
				s.searchSongs();
			}
		}
		if (REESTIMATE_COMMAND.equals(command)) {
			s.reEstimateElements();
			//s.updateTrillMeasures();
			//s.updateEleStartTimes();
			updateElementLists();
			//cloneLists();
		}
		if (REEST_ALL_COMMAND.equals(command)) {
			s.remeasureAll();
			//s.updateTrillMeasures();
			//s.updateEleStartTimes();
			updateElementLists();
			//cloneLists();
		}
		
		if (CALC_SYLL_COMMAND.equals(command)) {
			//song.syllList=new LinkedList();
			//LinkedList songList=new LinkedList();
			//songList.add(song);
			//SyllableInduction si=new SyllableInduction(dbc, songList, defaults, this);
			@SuppressWarnings("unused")
			SyllableInduction si=new SyllableInduction(dbc, song, defaults, this);
		}
		if (MODE_COMMAND.equals(command)) {
			if (s.syllable){
				s.syllable=false;
				mode.setText("Mode: elements");
				tabPane.setBackground(R_GREEN);
				//redo.setEnabled(true);
				redo.setEnabled(false);
				if (s.displayMode==0){
					resetBrush();
					brush.setEditable(true);
					brush.setEnabled(true);
				}
			}
			else{
				s.syllable=true;
				mode.setText("Mode: syllables");
				tabPane.setBackground(R_RED);
				redo.setEnabled(false);
				song.setBrushType(2);
				brush.setEditable(false);
				brush.setEnabled(false);
			}
		}
		if (ERASE_COMMAND.equals(command)){
			s.erase=erase.isSelected();
		}
		if (SAVE_COMMAND.equals(command)) {
			boolean proceed = writeResults();
			if (proceed){cleanUp();}
			//f.dispose();
		}
		if (SAVE_SOUND_COMMAND.equals(command)) {
			song.makeAudioFormat();
			SaveSound ss=new SaveSound(song, song.getAf(), this, 0, song.getRawDataLength(), defaults); 
		}
		if (SAVE_IMAGE_COMMAND.equals(command)) {
			SaveImage si=new SaveImage(s.imf, s, defaults);
			//si.save();
		}
		//if (SELECT_COMMAND.equals(command)) {s.modelSearch();}
		if (BRUSH_COMMAND.equals(command)) {
			resetBrush();
		}
		if (SIGNAL_COMMAND.equals(command)) {
			s.viewParameters[0]=!s.viewParameters[0];
			s.paintFound();
		}
		if (PEAK_COMMAND.equals(command)) {
			s.viewParameters[4]=!s.viewParameters[4];
			s.paintFound();
		}
		if (FUND_COMMAND.equals(command)) {
			s.viewParameters[5]=!s.viewParameters[5];
			s.paintFound();
		}
		if (MEAN_COMMAND.equals(command)) {
			s.viewParameters[6]=!s.viewParameters[6];
			s.paintFound();
		}
		if (MEDIAN_COMMAND.equals(command)) {
			s.viewParameters[7]=!s.viewParameters[7];
			s.paintFound();
		}
		if (HARM_COMMAND.equals(command)) {
			s.viewParameters[12]=!s.viewParameters[12];
			s.paintFound();
		}
		if (WIENER_COMMAND.equals(command)) {
			s.viewParameters[13]=!s.viewParameters[13];
			s.paintFound();
		}
		if (AMPLITUDE_COMMAND.equals(command)) {
			s.viewParameters[15]=!s.viewParameters[15];
			s.paintFound();
		}
		if (BANDWIDTH_COMMAND.equals(command)) {
			s.viewParameters[14]=!s.viewParameters[14];
			s.paintFound();
		}
		if (PEAK_FREQ_CHANGE_COMMAND.equals(command)) {
			s.viewParameters[8]=!s.viewParameters[8];
			s.paintFound();
		}
		if (FUND_FREQ_CHANGE_COMMAND.equals(command)) {
			s.viewParameters[9]=!s.viewParameters[9];
			s.paintFound();
		}
		if (MEAN_FREQ_CHANGE_COMMAND.equals(command)) {
			s.viewParameters[10]=!s.viewParameters[10];
			s.paintFound();
		}
		if (MEDIAN_FREQ_CHANGE_COMMAND.equals(command)) {
			s.viewParameters[11]=!s.viewParameters[10];
			s.paintFound();
		}
		if (VIEW_ELEMENTS_COMMAND.equals(command)) {
			s.viewParameters[1]=!s.viewParameters[1];
			s.paintFound();
		}
		if (VIEW_SYLLABLES_COMMAND.equals(command)) {
			s.viewParameters[2]=!s.viewParameters[2];
			s.paintFound();
		}
		if (VIEW_TRILL_AMP_COMMAND.equals(command)) {
			s.viewParameters[17]=!s.viewParameters[17];
			s.paintFound();
		}
		if (VIEW_TRILL_RATE_COMMAND.equals(command)) {
			s.viewParameters[18]=!s.viewParameters[18];
			s.paintFound();
		}
		if (VIEW_REVERB_COMMAND.equals(command)) {
			s.viewParameters[16]=!s.viewParameters[16];
			s.paintFound();
		}
		if (DEFAULT_COMMAND.equals(command)) {
			defaults.getSongParameters(song);
			setValues();
			resetSpect=true;
			replot();
		}
		
		if (SAVE_DEFAULT_COMMAND.equals(command)) {
			defaults.setDoubleProperty("stretchX", s.stretchX, 1000);
			defaults.setDoubleProperty("stretchY", s.stretchY, 1000);
			defaults.setIntProperty("GPMAXF", guidePanelMaxFrequency);
			defaults.setBooleanProperty("clickdrag", s.clickDrag);
			defaults.setSongParameters(song);
			defaults.setParameterViews(s);
			defaults.writeProperties();
		}
		
		if (WINDOW_COMMAND.equals(command)) {
			JComboBox cb = (JComboBox)e.getSource();
			song.setWindowMethod(cb.getSelectedIndex()+1);
			resetSpect=true;
		}
		
		if (PREVIOUS_COMMAND.equals(command)) {
			defaults.getSongDetails(song);
			equipmentField.setText(song.getRecordEquipment());
			recordistField.setText(song.getRecordist());
			locationField.setText(song.getLocation());
			setValues();
		}
		
		if (SET_TO_MAX_COMMAND.equals(command)) {
			if (song.getSetRangeToMax()){
				song.setSetRangeToMax(false);
				setMaxDyn.setText("Automatic compression");
				dynM.setEnabled(true);
				dynMSL.setEnabled(true);
			}
			else{
				song.setSetRangeToMax(true);
				setMaxDyn.setText("Custom compression");
				dynM.setEnabled(false);
				dynMSL.setEnabled(false);
				song.setDynMax(song.getMaxDB());
				replot();
			}
			
		}
		
		if (ZOOM_TO_ALL_COMMAND.equals(command)){
			double ratio=song.getNx()/(s.multiplier*(s.nnx-s.xspace-s.xspace2-0.0));
			ratio=100*ratio;
			timeZoom.setValue(new Double(ratio));
		}	
		if (ZOOM_TO_100_COMMAND.equals(command)){
			timeZoom.setValue(new Double(100));
		}		
		
		if (FONT_FACE.equals(command)){
			s.fontFace=(String)fontFace.getSelectedItem();
			s.resetFonts();
			s.restart();
			s.paintFound();
		}
		if (AXIS_LABEL_FONT_STYLE.equals(command)){
			int p=axisLabelFontStyle.getSelectedIndex();
			if (p==0){s.axisLabelFontStyle=Font.PLAIN;}
			else if (p==1){s.axisLabelFontStyle=Font.BOLD;}
			else if (p==2){s.axisLabelFontStyle=Font.ITALIC;}
			else if (p==3){s.axisLabelFontStyle=Font.BOLD+Font.ITALIC;}
			s.resetFonts();
			s.restart();
			s.paintFound();
		}
		if (TICK_LABEL_FONT_STYLE.equals(command)){
			int p=tickLabelFontStyle.getSelectedIndex();
			if (p==0){s.tickLabelFontStyle=Font.PLAIN;}
			else if (p==1){s.tickLabelFontStyle=Font.BOLD;}
			else if (p==2){s.tickLabelFontStyle=Font.ITALIC;}
			else if (p==3){s.tickLabelFontStyle=Font.BOLD+Font.ITALIC;}
			s.resetFonts();
			s.restart();
			s.paintFound();
		}
		if (FREQ_UNIT_KHZ.equals(command)){
			s.frequencyUnit=true;
			s.resetFonts();
			s.restart();
			s.paintFound();
		}
		if (FREQ_UNIT_HZ.equals(command)){
			s.frequencyUnit=false;
			s.resetFonts();
			s.restart();
			s.paintFound();
		}
		if (TIME_UNIT_S.equals(command)){
			s.timeUnit=true;
			s.resetFonts();
			s.restart();
			s.paintFound();
		}
		if (TIME_UNIT_MS.equals(command)){
			s.timeUnit=false;
			s.resetFonts();
			s.restart();
			s.paintFound();
		}
		if (SHOW_FRAME.equals(command)){
			if (showFrame.isSelected()){
				s.showFrame=true;
			}
			else{
				s.showFrame=false;
			}
			s.resetFonts();
			s.paintFound();
		}
		if (INTERIOR_TICK_MARK.equals(command)){
			if (interiorTickMarkLabels.isSelected()){
				s.interiorTickMarks=true;
			}
			else{
				s.interiorTickMarks=false;
			}
			s.resetFonts();
			s.paintFound();
		}
		if (SHOW_MAJOR_TIME_TICK_MARK.equals(command)){
			if (showMajorTimeTickMark.isSelected()){
				s.showMajorTimeTickMarks=true;
			}
			else{
				s.showMajorTimeTickMarks=false;
			}
			s.resetFonts();
			s.paintFound();
		}
		if (SHOW_MINOR_TIME_TICK_MARK.equals(command)){
			if (showMinorTimeTickMark.isSelected()){
				s.showMinorTimeTickMarks=true;
			}
			else{
				s.showMinorTimeTickMarks=false;
			}
			s.resetFonts();
			s.paintFound();
		}
		
		if (SHOW_MAJOR_FREQ_TICK_MARK.equals(command)){
			if (showMajorFreqTickMark.isSelected()){
				s.showMajorFreqTickMarks=true;
			}
			else{
				s.showMajorFreqTickMarks=false;
			}
			s.resetFonts();
			s.paintFound();
		}
		if (SHOW_MINOR_FREQ_TICK_MARK.equals(command)){
			if (showMinorFreqTickMark.isSelected()){
				s.showMinorFreqTickMarks=true;
			}
			else{
				s.showMinorFreqTickMarks=false;
			}
			s.resetFonts();
			s.paintFound();
		}
		if (FORWARD_COMMAND.equals(command)){
			s.moveForward(0.33);
		}
		if (BACKWARD_COMMAND.equals(command)){
			s.moveBackward(0.33);
		}
		if (F_FORWARD_COMMAND.equals(command)){
			s.moveForward(1);
		}
		if (F_BACKWARD_COMMAND.equals(command)){
			s.moveBackward(1);
		}
	}
	
	void resetBrush(){
		int c= brush.getSelectedIndex();
		if (c>4){
			if (c==5){song.setBrushType(2);}
			if (c==6){
				JPanel btpanel=new JPanel();
				JLabel maxlab=new JLabel("Maximum frequency (Hz):");
				JLabel minlab=new JLabel("Minimum frequency (Hz):");
				JFormattedTextField maxFField = new JFormattedTextField();
				maxFField.setValue(new Integer(song.getMaxBrush()));
				maxFField.setColumns(10);
				JFormattedTextField minFField = new JFormattedTextField();
				minFField.setValue(new Integer(song.getMinBrush()));
				minFField.setColumns(10);
				btpanel.add(maxlab);
				btpanel.add(maxFField);
				btpanel.add(minlab);
				btpanel.add(minFField);
				JOptionPane.showMessageDialog(this, btpanel, "Choose a frequency range", JOptionPane.QUESTION_MESSAGE);
				Integer maxb=(Integer)maxFField.getValue();
				song.setMaxBrush(maxb.intValue());
				Integer minb=(Integer)minFField.getValue();
				song.setMinBrush(minb.intValue());
				song.setBrushType(3);
			}
		}
		else{
			song.setBrushType(1);
			song.setBrushSize(brushSizeInd[c]);
		}
	}

	public void stateChanged(ChangeEvent e) {
		
		this.requestFocusInWindow();
		
		if (tabPane.getSelectedComponent()==appearancePane){
			
			if ((updateable)&&(started)) {
				JSpinner source = (JSpinner)e.getSource();
				updateable=false;
					//JSpinner majorTickMarkLength, minorTickMarkLength, axisLabelFontSize, tickLabelFontSize;
				
				SpinnerNumberModel snm=(SpinnerNumberModel)source.getModel();
				Number num=snm.getNumber();
				int val=num.intValue();
				if (source==majorTickMarkLength){
					s.majorTickMarkLength=val;
				}
				if (source==minorTickMarkLength){
					s.minorTickMarkLength=val;
				}
				if (source==lineWeight){
					s.lineWeight=(float)(val);
				}
				if (source==axisLabelFontSize){
					s.axisLabelFontSize=val;
				}
				if (source==tickLabelFontSize){
					s.tickLabelFontSize=val;
				}
				
				updateable=true;
				s.resetFonts();
				s.paintFound();
			}
		}
		else{
	
        //JSlider source = (JSlider)e.getSource();
		JSlider source = (JSlider)e.getSource();

		if ((!source.getValueIsAdjusting())&&(updateable)&&(started)) {
			int fps = (int)source.getValue();
			updateable=false;
			System.out.println("SLIDER CALLED MOVE");
			if (source==maxFreqSL){
				int mfs=fps*200;
				maxFreq.setValue(new Integer(mfs));
			}
			if (source==frameLengthSL){
				double mfls=fps*0.2;
				frameLength.setValue(new Double(mfls));
			}
			if (source==timeStepSL){
				double mts=fps*0.1;
				timeStep.setValue(new Double(mts));
			}
			if (source==spectPointsSL){
				int sps=(int)Math.pow(2, fps);
				spectPoints.setValue(new Integer(sps));			
			}
			if (source==spectOverlapSL){
				spectOverlap.setValue(new Integer(fps));
			}
			if (source==filterCutOffSL){
				int sfco=fps*20;
				filterCutOff.setValue(new Integer(sfco));
			}
			if (source==dynRangeSL){
				dynRange.setValue(new Integer(fps));
			}
			if (source==echoAmtSL){
				echoAmt.setValue(new Double(fps));
			}
			if (source==echoRangeSL){
				echoRange.setValue(new Integer(fps));
			}
			if (source==noiseAmtSL){
				noiseAmt.setValue(new Double(fps*0.2));
			}
			if (source==noiseRange1SL){
				noiseRange1.setValue(new Integer(fps*10));
			}
			if (source==noiseRange2SL){
				noiseRange2.setValue(new Integer(fps));
			}
			if (source==dynEqualSL){
				dynEqual.setValue(new Integer(fps));
			}
			if (source==timeZoomSL){
				double tz=Math.pow(((fps*0.01)+0.5), 2)*100;
				timeZoom.setValue(new Double(tz));
			}
			if (source==freqZoomSL){
				double fz=Math.pow(((fps*0.01)+0.5), 2)*100;
				freqZoom.setValue(new Double(fz));
			}
			if (source==dynMSL){
				dynM.setValue(new Integer(fps));
			}
			updateable=true;
			replot();
		}
		}
		updateable=true;
    }
	
		
	public void populateDelete(){
		int size=song.getNumElements();
		deleteModel.addElement(" ");
		deleteModel.addElement("         ALL");
		for (int i=1; i<=size; i++){
			int j=size+1-i;
			String st="   "+j;
			deleteModel.addElement(st);
		}
		if (delete!=null){
			delete.setSelectedIndex(-1);
		}
	}
	
	public void populateDeleteS(){
		int size=song.getNumSyllables();
		deleteModelS.addElement("  ");
		playModel.addElement(" ");
		deleteModelS.addElement("         ALL");
		for (int i=1; i<=size; i++){
			int j=size+1-i;
			String st="   "+j;
			deleteModelS.addElement(st);
			playModel.addElement(st);
		}
		if (deleteS!=null){
			deleteS.setSelectedIndex(-1);
		}
	}
	
	public void populateMerge(){
		int size=song.getNumElements()-1;
		mergeModel.addElement(" ");
		for (int i=0; i<size; i++){
			int j=i+1;
			int k=i+2;
			String entry=j+" and "+k;
			mergeModel.addElement(entry);
		}
	}

	public void updateElementLists(){
		LISTEN_TO_DROP_DOWNS=false;
		deleteModel.removeAllElements();
		populateDelete();
		mergeModel.removeAllElements();
		populateMerge();
		LISTEN_TO_DROP_DOWNS=true;
	}
	
	public void updateSyllableLists(){
		LISTEN_TO_DROP_DOWNS=false;
		playModel.removeAllElements();
		deleteModelS.removeAllElements();
		populateDeleteS();
		LISTEN_TO_DROP_DOWNS=true;
	}
	
	public void updateSyllables(){
		updateSyllableLists();
		//System.out.println("NEW SYL LIST: "+song.getNumSyllables());
		s.paintFound();
		gp.draw();
		cloneLists();
	}
	
	public void propertyChange(PropertyChangeEvent e) {
        Object source = e.getSource();
        //this.setRequestFocusEnabled(true);
        //this.requestFocus();
        this.requestFocusInWindow();
		if (started){
			//System.out.println("TEXT BOX CALLED");
			started=false;
			if (source==maxFreq){
				int s = (int)((Number)maxFreq.getValue()).doubleValue();
				if (s>song.getSampleRate()/2){s=(int)song.getSampleRate()/2;}
				if (s<1){s=1;}
				song.setMaxF(s);
				maxFreq.setValue(new Double(s));
				resetSpect=true;
				int mfs=(int)Math.round(s/200.0);
				if (mfs<0){mfs=0;}
				if (mfs>100){mfs=100;}
				maxFreqSL.setValue(mfs);
			}
			if (source==gpMaxF){
				guidePanelMaxFrequency = (int)((Number)gpMaxF.getValue()).doubleValue();
				if (guidePanelMaxFrequency>song.getSampleRate()/2){guidePanelMaxFrequency=(int)song.getSampleRate()/2;}
				if (guidePanelMaxFrequency<1){guidePanelMaxFrequency=1;}
				gpMaxF.setValue(new Double(guidePanelMaxFrequency));
				s.makeGuidePanel(d.getWidth()-20, guidePanelMaxFrequency);
				gp.draw();
			}
			if (source==frameLength){
				double s = ((Number)frameLength.getValue()).doubleValue();
				if (s>1000){s=1000;}
				if (s<0.1){s=0.1;}
				frameLength.setValue(new Double(s));
				song.setFrameLength(s);
				song.setFFTParameters();
				spectPoints.setValue(new Double(song.getFrame()));
				spectOverlap.setValue(new Double(song.getOverlap()*100));
				resetSpect=true;
				int mfls=(int)Math.round(song.getFrameLength()*5);
				if (mfls<0){mfls=0;}
				if (mfls>100){mfls=100;}
				frameLengthSL.setValue(mfls);
				int mto=(int)Math.round(song.getOverlap()*100);
				if (mto<0){mto=0;}
				if (mto>100){mto=100;}
				spectOverlapSL.setValue(mto);
				int mfps=(int)Math.round(Math.log(song.getFrame())/Math.log(2));
				if (mfps<0){mfps=0;}
				if (mfps>12){mfps=12;}
				spectPointsSL.setValue(mfps);
			}
			if (source==timeStep){
				double s = ((Number)timeStep.getValue()).doubleValue();
				timeStep.setValue(new Double(s));
				song.setTimeStep(s);
				song.setFFTParameters();
				spectOverlap.setValue(new Double(song.getOverlap()*100));
				resetSpect=true;
				int mts=(int)Math.round(song.getTimeStep()*10);
				if (mts<0){mts=0;}
				if (mts>100){mts=100;}
				timeStepSL.setValue(mts);
				int mto=(int)Math.round(song.getOverlap()*100);
				if (mto<0){mto=0;}
				if (mto>100){mto=100;}
				spectOverlapSL.setValue(mto);
			}
			if (source==spectPoints){
				double s=((Number)spectPoints.getValue()).doubleValue();
				s*=1000/song.getSampRate();
				song.setFrameLength(s); //???
				song.setFFTParameters();
				frameLength.setValue(new Double(song.getFrameLength()));
				spectPoints.setValue(new Double(song.getFrame()));
				spectOverlap.setValue(new Double(song.getOverlap()*100));
				resetSpect=true;
				int mfls=(int)Math.round(song.getFrameLength()*5);
				if (mfls<0){mfls=0;}
				if (mfls>100){mfls=100;}
				frameLengthSL.setValue(mfls);
				int mto=(int)Math.round(song.getOverlap()*100);
				if (mto<0){mto=0;}
				if (mto>100){mto=100;}
				spectOverlapSL.setValue(mto);
				int mfps=(int)Math.round(Math.log(song.getFrame())/Math.log(2));
				if (mfps<0){mfps=0;}
				if (mfps>12){mfps=12;}
				spectPointsSL.setValue(mfps);
			}
			if (source==spectOverlap){
				double s=((Number)spectOverlap.getValue()).doubleValue()*0.01;
				if (s>1){s=1;}
				song.setOverlap(s);
				song.setTimeStep((1-s)*song.getFrameLength());
				song.setFFTParameters();
				spectPoints.setValue(new Double(song.getFrame()));
				spectOverlap.setValue(new Double(song.getOverlap()*100));
				timeStep.setValue(new Double(song.getTimeStep()));
				resetSpect=true;
				int mts=(int)Math.round(song.getTimeStep()*10);;
				if (mts<0){mts=0;}
				if (mts>100){mts=100;}
				timeStepSL.setValue(mts);
				int mto=(int)Math.round(song.getOverlap()*100);
				if (mto<0){mto=0;}
				if (mto>100){mto=100;}
				spectOverlap.setValue(mto);
			}
			if (source==filterCutOff){
				double te = ((Number)filterCutOff.getValue()).doubleValue();
				if (te<0){te=0;}
				if (te>song.getMaxF()){te=song.getMaxF();}
				song.setFrequencyCutOff(te);
				resetSpect=true;
				filterCutOff.setValue(new Double(te));
				int sfco=(int)Math.round(te/20.0);;
				if (sfco<0){sfco=0;}
				if (sfco>100){sfco=100;}
				filterCutOffSL.setValue(sfco);
			}
			if (source==dynRange){
				double s = ((Number)dynRange.getValue()).doubleValue();
				if (s<=0){s=0.00001;}
				song.setDynRange(s);
				dynRange.setValue(new Double(s));
				int sgs=(int)Math.round(s);
				if (sgs<0){sgs=0;}
				if (sgs>100){sgs=100;}
				dynRangeSL.setValue(sgs);
			}
			if (source==dynM){
				float s = (float)((Number)dynM.getValue()).doubleValue();
				s*=0.01f;
				if (s<=0){s=0.00001f;}
				song.setDynMax(s);
				dynM.setValue(new Double(s*100));
				int sgs=(int)Math.round(s*100);
				if (sgs<0){sgs=0;}
				if (sgs>100){sgs=100;}
				dynMSL.setValue(sgs);
			}
			if (source==echoAmt){
				double s = ((Number)echoAmt.getValue()).doubleValue();
				s*=0.01;
				if (s<0){s=0;}
				//if (song.echoComp>1){song.echoComp=1;}
				song.setEchoComp(s);
				echoAmt.setValue(new Double(s*100));
				int sco=(int)Math.round(s*100);
				if (sco<0){sco=0;}
				//if (sco>100){sco=100;}
				echoAmtSL.setValue(sco);
			}
			if (source==echoRange){
				int s = (int)((Number)echoRange.getValue()).doubleValue();
				if (s<1){s=1;}
				song.setEchoRange(s);
				echoRange.setValue(new Double(s));
				int stas=(int)Math.round(s);
				if (stas<0){stas=0;}
				if (stas>100){stas=100;}
				echoRangeSL.setValue(stas);
			}
			if (source==noiseAmt){
				float s = ((Number)noiseAmt.getValue()).floatValue();
				if (s<0){s=0;}
				song.setNoiseRemoval(s);
				noiseAmt.setValue(new Double(s));
				int sco=(int)Math.round(s*5);
				if (sco<0){sco=0;}
				noiseAmtSL.setValue(sco);
			}
			if (source==noiseRange1){
				int s = (int)((Number)noiseRange1.getValue()).doubleValue();
				if (s<1){s=1;}
				song.setNoiseLength1(s);
				noiseRange1.setValue(new Double(s));
				int stas=(int)Math.round(s/10);
				if (stas<0){stas=0;}
				if (stas>100){stas=100;}
				noiseRange1SL.setValue(stas);
			}
			if (source==noiseRange2){
				int s = (int)((Number)noiseRange2.getValue()).doubleValue();
				if (s<1){s=1;}
				song.setNoiseLength2(s);
				noiseRange2.setValue(new Double(s));
				int stas=(int)Math.round(s);
				if (stas<0){stas=0;}
				if (stas>100){stas=100;}
				noiseRange2SL.setValue(stas);
			}
			if (source==harmonic){
				double s = ((Number)harmonic.getValue()).doubleValue();
				song.setFundAdjust(s);
				harmonic.setValue(new Double(s));
			}
			if (source==fundJumpSuppression){
				double s = ((Number)fundJumpSuppression.getValue()).doubleValue();
				if (s<0){s=0;}
				song.setFundJumpSuppression(s);
				fundJumpSuppression.setValue(new Double(s));
			}
			if (source==dynEqual){
				int te = ((Number)dynEqual.getValue()).intValue();
				if (te<0){te=0;}
				song.setDynEqual(te);
				dynEqual.setValue(new Double(te));
				int sdyc=(int)Math.round(te);
				if (sdyc<0){sdyc=0;}
				if (sdyc>100){sdyc=100;}
				dynEqualSL.setValue(sdyc);
			}
			if (source==minGap){
				double te = ((Number) minGap.getValue()).doubleValue();
				if (te<-1000){te=-100;}
				if (te>1000){te=1000;}
				song.setMinGap(te);
				minGap.setValue(new Double(te));
			}
			if (source==minLength){
				double te = ((Number) minLength.getValue()).doubleValue();
				if (te<0){te=0;}
				song.setMinLength(te);
				minLength.setValue(new Double(te));
			}
			if ((source==timeZoom)||(source==freqZoom)){
				double te = ((Number) timeZoom.getValue()).doubleValue();
				double tf = ((Number) freqZoom.getValue()).doubleValue();
				
				int fpt=(int)Math.round((Math.sqrt(te*0.01)-0.5)*100);
				if (fpt<0){fpt=0;}
				if (fpt>100){fpt=100;}
				timeZoomSL.setValue(fpt);
				int fpf=(int)Math.round((Math.sqrt(tf*0.01)-0.5)*100);
				if (fpf<0){fpf=0;}
				if (fpf>100){fpf=100;}
				freqZoomSL.setValue(fpf);
				
				int tzi=(int)Math.round(te);
				int fzi=(int)Math.round(tf);
				timeZoom.setValue(new Integer(tzi));
				freqZoom.setValue(new Integer(fzi));
				
				s.stretchX=100/te;
				s.stretchY=tf/100.0;
				s.restart();
				
				int y=s.nny+20;
				if (gp.getHeight()+controls.getHeight()+s.nny>d.height){
					y=d.height-220-gp.getHeight();
				}
				int x=s.nnx+20;
				if (x>d.width){
					x=d.width-20;
				}
				sp.setPreferredSize(new Dimension(x, y));
				sp.revalidate();
				this.revalidate();
				f.pack();
			}
			if (source==upperLoop){
				double te = ((Number) upperLoop.getValue()).doubleValue();
				if (te<song.getLowerLoop()){
					te=song.getLowerLoop();
				}
				song.setUpperLoop(te);
				upperLoop.setValue(te);
			}
			if (source==lowerLoop){
				double te = ((Number) lowerLoop.getValue()).doubleValue();
				if (te>song.getUpperLoop()){
					te=song.getUpperLoop();
				}
				song.setLowerLoop(te);
				lowerLoop.setValue(te);
			}
			started=true;
		}
	}
		
	void cloneLists(){
		System.out.println("clone"+undoList.size());
		LinkedList<LinkedList<Object>> temp=new LinkedList<LinkedList<Object>>();
		LinkedList<Object> temp1=new LinkedList<Object>();
		int ne=song.getNumElements();
		for (int i=0; i<ne; i++){
			Element ele=(Element)song.getElement(i);
			temp1.addLast(ele);
		}
		temp.addLast(temp1);
		LinkedList<Object> temp2=new LinkedList<Object>();
		int ns=song.getNumSyllables();
		for (int i=0; i<ns; i++){
			int[] syl=(int[])song.getSyllable(i);
			temp2.addLast(syl);
		}
		temp.addLast(temp2);
		undoList.addLast(temp);
		if (undoList.size()>maxUndoListSize){undoList.remove(0);}
		if (!undo.isEnabled()){undo.setEnabled(true);}
		System.out.println("clone"+undoList.size());
		redo.setEnabled(false);
	}
	
	public void replot(){
		Cursor c=this.getCursor();
		this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		if (resetSpect){
			song.setFFTParameters();
			resetSpect=false;
			s.restart();
		}
		else{
			s.relocate(s.location);
		}
		if (song.getSetRangeToMax()){
			updateCompressionBox();
		}
		this.setCursor(c);
	}
	
	public void updatePosition(int x){
		if (updateable){
			updateable=false;
			s.relocate(x);
			updateable=true;
			if (song.getSetRangeToMax()){
				updateCompressionBox();
			}
		}
	}
	
	public void updateCompressionBox(){
		updateable=false;
		started=false;
		int sgm=(int)Math.round(song.getDynMax()*100);
		if (sgm<0){sgm=0;}
		if (sgm>100){sgm=100;}
		dynMSL.setEnabled(true);
		dynM.setEnabled(true);
		dynMSL.setValue(sgm);
		dynM.setValue(new Integer((int)Math.round(song.getDynMax()*100)));
		dynMSL.setEnabled(false);
		dynM.setEnabled(false);
		updateable=true;
		started=true;
	}
	
	public void setValues(){
		windowMethod.setSelectedIndex(song.getWindowMethod()-1);
		echoAmt.setValue(new Double(song.getEchoComp()*100));
		dynRange.setValue(new Double(song.getDynRange()));		
		dynM.setValue(new Integer((int)Math.round(song.getDynMax()*100)));
		echoRange.setValue(new Double(song.getEchoRange()));
		dynEqual.setValue(new Double(song.getDynEqual()));
		filterCutOff.setValue(new Double(song.getFrequencyCutOff()));
		maxFreq.setValue(new Double(song.getMaxF()));
		frameLength.setValue(new Double(song.getFrameLength()));
		timeStep.setValue(new Double(song.getTimeStep()));
		harmonic.setValue(new Double(song.getFundAdjust()));
		minGap.setValue(new Double(song.getMinGap()));
	}
		
	public boolean writeResults(){
		Calendar timeCal=Calendar.getInstance();
		timeCal.setTime(timeModel.getDate());
		Calendar dateCal=Calendar.getInstance();
		dateCal.setTime(dateModel.getDate());
		dateCal.add(Calendar.HOUR, timeCal.get(Calendar.HOUR)-dateCal.get(Calendar.HOUR));
		dateCal.add(Calendar.MINUTE, timeCal.get(Calendar.MINUTE)-dateCal.get(Calendar.MINUTE));
		dateCal.add(Calendar.SECOND, timeCal.get(Calendar.SECOND)-dateCal.get(Calendar.SECOND));
		dateCal.add(Calendar.MILLISECOND, timeCal.get(Calendar.MILLISECOND)-dateCal.get(Calendar.MILLISECOND));
		song.setTDate(dateCal.getTimeInMillis());
		song.setLocation(locationField.getText());
		song.setNotes(notesField.getText());
		song.setRecordEquipment(equipmentField.getText());
		song.setRecordist(recordistField.getText());
		
		if (editMode){
			boolean proceed=song.checkSong(this);
			if (!proceed){return false;}
			dbc.writeSongInfo(song);
			song.sortSyllsEles();
			song.calculateGaps();
			dbc.writeSongMeasurements(song);	
		}
		else{
			Song[] songs=song.splitSong();
			LinkedList ustore=new LinkedList();
			int[] sonq={1,2,3};
			//dbc.writeToDataBase("INSERT INTO songdata (name, IndividualID) VALUES ('"+nam+"' , "+par.dex+")");
			for (int i=0; i<songs.length; i++){
				System.out.println(songs[i].getName());
				dbc.writeToDataBase("INSERT INTO songdata (name, IndividualID) VALUES ('"+songs[i].getName()+"' , "+songs[i].getIndividualID()+")");
				ustore=new LinkedList();
				//String query="SELECT id, IndividualID FROM songdata WHERE Name='"+songs[i].getName()+"'";
				String query="SELECT name, id, IndividualID FROM songdata WHERE IndividualID="+songs[i].getIndividualID();
				ustore.addAll(dbc.readFromDataBase(query, sonq));
				int maxind=-1;
				for (int j=0; j<ustore.size(); j++){
					String[]nam2=(String[])ustore.get(j);
					Integer q1=Integer.valueOf(nam2[2]);
					int q=q1.intValue();
					if (q==songs[i].getIndividualID()){
					System.out.println(nam2[0]+" "+nam2[1]);
					Integer p1=Integer.valueOf(nam2[1]);
					int p=p1.intValue();
					if (p>maxind){maxind=p;}
					}
				}
				System.out.println("CHECK ID"+maxind);
				songs[i].setSongID(maxind);
				dbc.writeSongIntoDatabase(songs[i]);
				dbc.writeSongInfo(songs[i]);
			}
			dbv.refreshTree();
			
		}
		defaults.setSongDetails(song);
		defaults.setIntProperty("GPMAXF", guidePanelMaxFrequency);
		defaults.setDoubleProperty("stretchX", s.stretchX, 1000);
		defaults.setDoubleProperty("stretchY", s.stretchY, 1000);
		return true;
	}
	
	
	public void cleanUp(){
		try{
			if (host!=null){
				host.remove(this);
			}
		}
		catch (Exception e) {
			
		}
		try{
			System.out.println("CLEAN UP!!!!");
			s.clearUp();
			s=null;
			song.clearUp();
			song=null;
			gp.clearUp();
			gp=null;
			controls=null;
			controlPane=null;
			settingsPane=null;
			appearancePane=null;
			measurePane=null;
			recordPane=null;
			tabPane=null;
			
			for (int i=0; i<undoList.size(); i++){
				LinkedList p=undoList.get(i);
				p=null;
			}
			for (int i=0; i<redoList.size(); i++){
				LinkedList p=redoList.get(i);
				p=null;
			}
			undoList=null;
			redoList=null;
			
			
			f.dispose();
			f=null;
			System.gc();
		}
		catch(Exception e){}
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
		
	public void drawProgress(double a, double b, int playbackDivider){
		try{
			Thread draw=new Thread(new DrawProgress(a, b, playbackDivider));
			draw.setPriority(Thread.MIN_PRIORITY);
			draw.start();
		} 
		catch (Exception e) {
			System.out.println(e);
			System.exit(0);
		}//
	}

	class DrawProgress extends Thread{
		
		double start, end;
		int playbackDivider;
		
		public DrawProgress(double start, double end, int playbackDivider){
			this.start=start;
			this.end=end;
			this.playbackDivider=playbackDivider;
		}

		public void run(){
			long startTime=System.currentTimeMillis();
			long currentTime;
			double position;
			double length=song.getOverallLength();
			while (!stopPlaying){
				currentTime=(int)(System.currentTimeMillis()-startTime);
				position=currentTime/(length*playbackDivider);
				position+=start;
				if (position>end){
					stopPlaying=true;
				}
				else{
					gp.drawLine(position);
					try{
						Thread.sleep(50l);
					}
					catch (InterruptedException e){}
				}
			}
			stopPlaying=false;
			gp.draw();
		}
	}
	
	public void stopDrawingProgress(){
		stopPlaying=true;
	}

	@Override
	public void keyTyped(KeyEvent e) {
		System.out.println("KEY TYPED :"+e.getKeyChar());
		char c=e.getKeyChar();
		if (c=='m'){
			mode.doClick();
		}
		else if (c=='p'){
			playAll.doClick();
		}
		else if (c=='P'){
			playScreen.doClick();
		}
		else if (c=='o'){
			stop.doClick();
		}
		else if (c=='s'){
			save.doClick();
		}
		else if (c=='U'){
			update.doClick();
		}
		else if (c=='z'){
			zoomTimeAll.doClick();
		}
		else if (c=='Z'){
			zoomTime100.doClick();
		}
		else if (c=='v'){
			viewElements.doClick();
		}
		else if (c=='V'){
			viewSyllables.doClick();
		}
		else if (c=='g'){
			viewSignal.doClick();
		}
		else if (c=='u'){
			undo.doClick();
		}
		else if (c=='q'){
			redo.doClick();
		}
		else if (c=='a'){
			automatic.doClick();
		}
		else if (c=='d'){
			defaultSettings.doClick();
		}
		else if (c=='D'){
			saveDefault.doClick();
		}
		else if (c=='f'){
			forwardButton.doClick();
		}
		else if (c=='F'){
			fForwardButton.doClick();
		}
		else if (c=='b'){
			backButton.doClick();
		}
		else if (c=='B'){
			fBackButton.doClick();
		}
		else if (c=='r'){
			reestimate.doClick();
		}
		else if (c=='R'){
			reestAll.doClick();
		}
		else if (c=='e'){
			erase.doClick();
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

}