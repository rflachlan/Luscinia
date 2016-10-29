package lusc.net.github.ui.compmethods;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.text.NumberFormat;
import java.util.LinkedList;
import java.util.Random;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.swing.*;

import lusc.net.github.Defaults;
import lusc.net.github.Song;
import lusc.net.github.analysis.AnalysisGroup;
import lusc.net.github.db.DataBaseController;
import lusc.net.github.ui.DendrogramOptions;
import lusc.net.github.ui.PCPaneOptions;
import lusc.net.github.ui.SaveDocument;
import lusc.net.github.ui.spectrogram.SpectrPane;

public class ABXdiscrimination extends JPanel implements ActionListener{
	
	static int ABX_OPTION=0;
	static int XAB_OPTION=1;
	static int BXA_OPTION=2;
	
	int experimentType=1;
	int modalityType=0;  //0=Visual, 1=auditory, 2=Vis-aud
	int modType=0; //current modality
	
	int balanceType=0;	//0= not balanced; 1 means A and B are swapped and the experiment repeated...
	
	int repetitions=10; // if repetitions is <= 0, then playback continues until a choice is made
	
	double ABgap=250;	//gap between A and B stimuli in ms
	double BXgap=350;	//gap between B and X stimuli in ms
	double XAgap=500;	//gap between X and A stimuli in ms
	
	int Achannel=0;
	int Bchannel=1;		// channel for playback. 0 is left, 1 is right, 2 is stereo.
	int Xchannel=2;
	
	
	
	
	String auditoryInstructionString="You will hear three sounds. Is the first sound or the third sound more similar to the second sound?";
	String visualInstructionString="You will see three sonograms. Is the first one or the third one more similar to the second one?";
	
	int nTrials=100;	//number of trials
	int nt;
	
	boolean randomizeOrder=true;
	boolean shuffleTriplets=true;	//each ABX triplet is played back three times throughout the expt, with each stimulus taking each position
	
	AnalysisGroup ag;
	DataBaseController dbc;
	Defaults defaults;
	Random random=new Random(System.currentTimeMillis());
	
	JButton start, finish, left, right, next, repeat, back, manage, downloadResults, deleteAllResults;
	
	JTextField snameBox=new JTextField();
	
	//Below are components of the Manage Panel
	JFormattedTextField ABGapField, BXGapField, XAGapField, repField, trialField;
	String[] modalityOptions={"Visual", "Auditory", "Visual-Auditory", "Auditory-Visual"};
	String[] channelOptions={"Left", "Right", "Stereo"};
	String[] experimentOptions={"ABX", "XAB", "BXA"};
	
	JComboBox exptOpts, modalityOpts, AchannelOpts, BchannelOpts, XchannelOpts, deleteUser;
	
	DefaultComboBoxModel deleteModel=new DefaultComboBoxModel();
	
	
	Song[] songs;
	
	int n;
	
	AudioFormat af;
	SourceDataLine  line = null;
	byte[] soundData;
	boolean dostop=false;
	
	int currentPosition=-1;
	
	int[] responsesAuditory, responsesVisual;
	int[][] pbSet;
	
	JLabel progressLabel, currentChoiceLabel;
	
	public String[] userData;
	public int[][] otherData;
	
	JPanel imagePanel=new JPanel();
	
	
	boolean disabled=false;
	
	public ABXdiscrimination(AnalysisGroup ag, Defaults defaults){
		this.ag=ag;
		this.dbc=ag.getDBC();
		this.defaults=defaults;
		readDefaults();
		songs=ag.getSongs();
		n=songs.length;
		setUpResponses();
		makePanel();

	}
	
	public ABXdiscrimination(AnalysisGroup ag, Defaults defaults, boolean x){
		this.ag=ag;
		this.dbc=ag.getDBC();
		this.defaults=defaults;
	}
	
	public void setUpResponses(){
		nt=nTrials;
		if (balanceType==1){nt*=2;}
		responsesAuditory=new int[nt];
		responsesVisual=new int[nt];
	}
	
	public void restart(){
		disabled=true;
		//nt=nTrials;
		//if (balanceType==1){nt*=2;}
		//responsesAuditory=new int[nt];
		//responsesVisual=new int[nt];
		this.removeAll();
		this.revalidate();
		makePanel();
		this.revalidate();
		disabled=false;
		//resetPanel();
	}
	
	
	public void makePanel(){
		start=new JButton("Start");
		finish=new JButton("Finish");
		left=new JButton("Left");
		right=new JButton("Right");
		next=new JButton("Next");
		repeat=new JButton("Play stimulus again");
		back=new JButton("Back");
		manage=new JButton("Manage");
		start.addActionListener(this);
		finish.addActionListener(this);
		left.addActionListener(this);
		right.addActionListener(this);
		next.addActionListener(this);
		repeat.addActionListener(this);
		back.addActionListener(this);
		manage.addActionListener(this);
		
		this.setLayout(new BorderLayout());
		this.setPreferredSize(new Dimension(600, 400));
		
		JPanel mainPanel=new JPanel();
		mainPanel.setPreferredSize(new Dimension(600, 400));
		//mainPanel.setLayout(new BorderLayout());
		mainPanel.setLayout(new BorderLayout());
		
		JPanel topPanel=new JPanel(new FlowLayout());
		topPanel.setMaximumSize(new Dimension(300, 100));
		start.setEnabled(true);
		topPanel.add(start);
		finish.setEnabled(false);
		topPanel.add(finish);
		JLabel nameLabel=new JLabel("Subject name: ");
		
		//JPanel namePanel=new JPanel(new FlowLayout());
		JPanel namePanel=new JPanel(new BorderLayout());
		snameBox.setColumns(50);
		namePanel.add(nameLabel, BorderLayout.WEST);
		namePanel.add(snameBox, BorderLayout.CENTER);
		//namePanel.add(nameLabel);
		//namePanel.add(snameBox);
		
		topPanel.add(namePanel);
		manage.setEnabled(true);
		topPanel.add(manage);
		
		//mainPanel.add(topPanel, BorderLayout.NORTH);
		mainPanel.add(topPanel, BorderLayout.NORTH);
		
		mainPanel.add(imagePanel, BorderLayout.CENTER);
		
		JPanel bottomPanel=new JPanel(new GridLayout(0,1));
		
		
		JLabel instructionLabel=new JLabel();
		if (modType==0){instructionLabel.setText(visualInstructionString);}
		else{instructionLabel.setText(auditoryInstructionString);}
		
		bottomPanel.add(instructionLabel);
		
		
		
		//JPanel choicePanel=new JPanel(new BorderLayout());
		JPanel choicePanel=new JPanel(new FlowLayout());
		choicePanel.setMaximumSize(new Dimension(200, 100));

		//choicePanel.add(left, BorderLayout.WEST);
		//choicePanel.add(right, BorderLayout.EAST);
		choicePanel.add(left);
		choicePanel.add(right);
		if (modType==1){
			choicePanel.add(repeat);
		}
		
		currentChoiceLabel=new JLabel(" ");
		choicePanel.add(currentChoiceLabel);
		
		//mainPanel.add(choicePanel, BorderLayout.CENTER);
		bottomPanel.add(choicePanel);
		
		//JPanel progressPanel=new JPanel(new BorderLayout());
		JPanel progressPanel=new JPanel(new FlowLayout());
		progressPanel.setMaximumSize(new Dimension(300, 100));
		//progressPanel.add(back, BorderLayout.WEST);
		//progressPanel.add(repeat, BorderLayout.CENTER);
		//progressPanel.add(next, BorderLayout.EAST);
		progressPanel.add(back);
		//progressPanel.add(repeat);
		progressPanel.add(next);
		
		progressLabel=new JLabel("You have not started yet");
		
		progressPanel.add(progressLabel);
		
		//mainPanel.add(progressPanel, BorderLayout.SOUTH);
		bottomPanel.add(progressPanel);
		
		mainPanel.add(bottomPanel, BorderLayout.SOUTH);
		
		this.add(mainPanel, BorderLayout.CENTER);
		
	}
	
	public void resetPanel(){
		
		currentPosition=-1;
		start.setEnabled(true);
		finish.setEnabled(false);
		manage.setEnabled(true);
		currentChoiceLabel.setText(" ");
		progressLabel.setText("You have not started yet");
		
	}
	
	
	public JPanel makeManagePane(){
		JPanel managePane=new JPanel(new FlowLayout());
		managePane.setPreferredSize(new Dimension(600, 400));
		NumberFormat num=NumberFormat.getNumberInstance();
		num.setMaximumFractionDigits(0);
		NumberFormat num2=NumberFormat.getNumberInstance();
		num2.setMaximumFractionDigits(3);
		
		JPanel ABGapPanel=new JPanel(new BorderLayout());
		JLabel ABGapLabel=new JLabel("AB Gap (ms):");
		ABGapPanel.add(ABGapLabel, BorderLayout.WEST);
		ABGapField=new JFormattedTextField(num2);
		ABGapField.setColumns(10);
		ABGapField.setValue(new Double(ABgap));
		ABGapPanel.add(ABGapField, BorderLayout.CENTER);
		managePane.add(ABGapPanel);
		
		JPanel BXGapPanel=new JPanel(new BorderLayout());
		JLabel BXGapLabel=new JLabel("BX Gap (ms):");
		BXGapPanel.add(BXGapLabel, BorderLayout.WEST);
		BXGapField=new JFormattedTextField(num2);
		BXGapField.setColumns(10);
		BXGapField.setValue(new Double(BXgap));
		BXGapPanel.add(BXGapField, BorderLayout.CENTER);
		managePane.add(BXGapPanel);
		
		JPanel XAGapPanel=new JPanel(new BorderLayout());
		JLabel XAGapLabel=new JLabel("XA Gap (ms):");
		XAGapPanel.add(XAGapLabel, BorderLayout.WEST);
		XAGapField=new JFormattedTextField(num2);
		XAGapField.setColumns(10);
		XAGapField.setValue(new Double(XAgap));
		XAGapPanel.add(XAGapField, BorderLayout.CENTER);
		managePane.add(XAGapPanel);
		
		JPanel repPanel=new JPanel(new BorderLayout());
		JLabel repLabel=new JLabel("Number of Repetitions of Stimuli: ");
		repPanel.add(repLabel, BorderLayout.WEST);
		repField=new JFormattedTextField(num);
		repField.setColumns(10);
		repField.setValue(new Integer(repetitions));
		repPanel.add(repField, BorderLayout.CENTER);
		managePane.add(repPanel);
		
		JPanel trialPanel=new JPanel(new BorderLayout());
		JLabel trialLabel=new JLabel("Number of trials: ");
		trialPanel.add(trialLabel, BorderLayout.WEST);
		trialField=new JFormattedTextField(num);
		trialField.setColumns(10);
		trialField.setValue(new Integer(nTrials));
		trialPanel.add(trialField, BorderLayout.CENTER);
		managePane.add(trialPanel);
		
		exptOpts=new JComboBox(experimentOptions);
		modalityOpts=new JComboBox(modalityOptions);	
		AchannelOpts=new JComboBox(channelOptions);
		BchannelOpts=new JComboBox(channelOptions);
		XchannelOpts=new JComboBox(channelOptions);
		
		
		JPanel optsPanel=new JPanel(new BorderLayout());
		JLabel optsLabel=new JLabel("Experiment type: ");
		optsPanel.add(optsLabel, BorderLayout.WEST);
		exptOpts.setSelectedIndex(experimentType);
		optsPanel.add(exptOpts, BorderLayout.CENTER);
		managePane.add(optsPanel);
		
		JPanel modPanel=new JPanel(new BorderLayout());
		JLabel modLabel=new JLabel("Modality: ");
		modPanel.add(modLabel, BorderLayout.WEST);
		modalityOpts.setSelectedIndex(modalityType);
		modPanel.add(modalityOpts, BorderLayout.CENTER);
		managePane.add(modPanel);
		
		JPanel ACoptsPanel=new JPanel(new BorderLayout());
		JLabel ACoptsLabel=new JLabel("Stim A Channel: ");
		ACoptsPanel.add(ACoptsLabel, BorderLayout.WEST);
		AchannelOpts.setSelectedIndex(Achannel);
		ACoptsPanel.add(AchannelOpts, BorderLayout.CENTER);
		managePane.add(ACoptsPanel);
		
		JPanel BCoptsPanel=new JPanel(new BorderLayout());
		JLabel BCoptsLabel=new JLabel("Stim B Channel: ");
		BCoptsPanel.add(BCoptsLabel, BorderLayout.WEST);
		BchannelOpts.setSelectedIndex(Bchannel);
		BCoptsPanel.add(BchannelOpts, BorderLayout.CENTER);
		managePane.add(BCoptsPanel);
		
		JPanel XCoptsPanel=new JPanel(new BorderLayout());
		JLabel XCoptsLabel=new JLabel("Stim X Channel: ");
		XCoptsPanel.add(XCoptsLabel, BorderLayout.WEST);
		XchannelOpts.setSelectedIndex(Xchannel);
		XCoptsPanel.add(XchannelOpts, BorderLayout.CENTER);
		managePane.add(XCoptsPanel);
		
		deleteModel.removeAllElements();
		int[] x={1};
		LinkedList<String[]> participants=dbc.readFromDataBase("SELECT user FROM comparetriplet", x);
		
		LinkedList<String> participantsUnique=new LinkedList<String>();
		
		for (int i=0; i<participants.size(); i++){
			String s=participants.get(i)[0];
			boolean found=false;
			for (int j=0; j<participantsUnique.size(); j++){
				String t=participantsUnique.get(j);
				if (t.equals(s)){
					found=true;
					j=participantsUnique.size();
				}
			}
			if (!found){
				participantsUnique.add(s);
			}
		}
		
		
		for (int i=0; i<participantsUnique.size(); i++){
			//System.out.println(i+" "+participants.get(i)[0]);
			deleteModel.addElement(participantsUnique.get(i));
		}
		
		deleteUser=new JComboBox(deleteModel);
		deleteUser.setMaximumRowCount(50);
        deleteUser.addActionListener(this);
        JPanel deletePanel=new JPanel(new BorderLayout());
		JLabel deleteLabel=new JLabel("Delete participant: ");
		deletePanel.add(deleteLabel, BorderLayout.WEST);
		deletePanel.add(deleteUser, BorderLayout.CENTER);
		managePane.add(deletePanel);
		
		downloadResults=new JButton("Download");
		deleteAllResults=new JButton("Clear Database");
		
		deleteAllResults.addActionListener(this);
		managePane.add(deleteAllResults);
		
		downloadResults.addActionListener(this);
		managePane.add(downloadResults);
	
		return managePane;
	}
	
	public byte[] createSound(Song song, double gap, int channel){
		
		song.makeAudioFormat();
		AudioFormat af=song.getAf();
		
		int r=af.getFrameSize();
		float p=af.getSampleRate();
		int stereo=af.getChannels();
		byte[] rawdata=song.getRawData();
		if (stereo==1){
			byte[] rd2=new byte[rawdata.length*2];
			int s=rawdata.length/r;
			int a=0;
			int b=0;
			for (int i=0; i<s; i++){
				for (int j=0; j<r; j++){
					byte x=rawdata[a];
					a++;
					rd2[i*2+j]=x;
					rd2[i*2+r+j]=x;
				}
				
			}
			rawdata=rd2;
			r*=2;
		}

		
		int silencelength=(int)Math.round(r*p*gap*0.001);
		
		int length=rawdata.length+silencelength;
		byte[] output=new byte[length];
		System.arraycopy(rawdata, 0, output, 0, rawdata.length);
		if (channel<2){
			int q=r/2;
			int a=0;
			int b=q;
			if (channel==0){
				a=q;
				b=r;
			}
			
			int s=rawdata.length/r;
			for (int i=0; i<s; i++){
				int c=i*r;
				for (int j=a; j<b; j++){
					output[c+j]=(byte)0;
				}
			}
		}
		
		return output;
	}
	
	public ImageIcon createSpectrogramStimulus(int[] stims){
		
		
		
		if (songs[stims[0]].getRawData()==null){
			Song song=dbc.loadSongFromDatabase(songs[stims[0]].getSongID(), 0);
			if ((song.getMaxF()<=1)||(song.getDynRange()<1)){
				defaults.getSongParameters(song);
			}
			song.setFFTParameters();
			songs[stims[0]]=song;
		}
		Song songA=songs[stims[0]];
		if (songs[stims[1]].getRawData()==null){
			Song song=dbc.loadSongFromDatabase(songs[stims[1]].getSongID(), 0);
			if ((song.getMaxF()<=1)||(song.getDynRange()<1)){
				defaults.getSongParameters(song);
			}
			song.setFFTParameters();
			songs[stims[1]]=song;
		}
		Song songB=songs[stims[1]];
		if (songs[stims[2]].getRawData()==null){
			Song song=dbc.loadSongFromDatabase(songs[stims[2]].getSongID(), 0);
			if ((song.getMaxF()<=1)||(song.getDynRange()<1)){
				defaults.getSongParameters(song);
			}
			song.setFFTParameters();
			songs[stims[2]]=song;
		}
		Song songX=songs[stims[2]];
		
		
		SpectrPane spA=new SpectrPane(songA, defaults, true);
		BufferedImage imA=spA.getIm();
		
		SpectrPane spB=new SpectrPane(songB, defaults, true);
		BufferedImage imB=spB.getIm();
		SpectrPane spX=new SpectrPane(songX, defaults, true);
		BufferedImage imX=spX.getIm();
		
		int x=imA.getWidth()+imB.getWidth()+imX.getWidth();
		
		double scalef=(imagePanel.getWidth()-100)/(x+0.0);
		if (scalef>1){scalef=1;}	
		
		int y=(int)Math.round(imA.getHeight()*scalef +50);
		
		System.out.println("IMAGESCALING: "+x+" "+scalef+" "+y);
		
		BufferedImage imn=new BufferedImage(imagePanel.getWidth()-50, y, BufferedImage.TYPE_INT_ARGB);
		
		//BufferedImage imn=new BufferedImage(505, 105, BufferedImage.TYPE_INT_ARGB);
		
		Graphics2D g=imn.createGraphics();
		
		RenderingHints hints = new RenderingHints(
                RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHints(hints);
		
		//g.setColor(Color.BLACK);
		//g.fillRect(0, 0, imn.getWidth(), imn.getHeight());
		
		//g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
               // RenderingHints.VALUE_ANTIALIAS_ON);
		
		int x1=(int)Math.round(imA.getWidth()*scalef);
		int y1=(int)Math.round(imA.getHeight()*scalef);
		
		g.drawImage(imA, 0, 0, x1, y1, null);
		
		int x2=(int)Math.round(imX.getWidth()*scalef);
		int y2=(int)Math.round(imX.getHeight()*scalef);
		g.drawImage(imX, x1, 0, x2, y2, null);
		
		int x3=(int)Math.round(imB.getWidth()*scalef);
		int y3=(int)Math.round(imB.getHeight()*scalef);
		g.drawImage(imB, x1+x2, 0, x3, y3, null);
	
		
		System.out.println((x1+x2+x3)+" "+imn.getWidth());
		System.out.println(y1+" "+y2+" "+y3+" "+imn.getHeight());
		System.out.println(imagePanel.getWidth()+" "+imagePanel.getHeight());
		g.dispose();
		
		ImageIcon im=new ImageIcon(imn);
		
		System.out.println("ICON DIMS: "+im.getIconWidth()+" "+im.getIconHeight());
		
		return im;
	}
	
	
	public byte[] createSoundStimulus(int[] stims){
		
		if (songs[stims[0]].getRawData()==null){
			Song song=dbc.loadSongFromDatabase(songs[stims[0]].getSongID(), 0);
			songs[stims[0]]=song;
		}
		Song songA=songs[stims[0]];
		if (songs[stims[1]].getRawData()==null){
			Song song=dbc.loadSongFromDatabase(songs[stims[1]].getSongID(), 0);
			songs[stims[1]]=song;
		}
		Song songB=songs[stims[1]];
		if (songs[stims[2]].getRawData()==null){
			Song song=dbc.loadSongFromDatabase(songs[stims[2]].getSongID(), 0);
			songs[stims[2]]=song;
		}
		Song songX=songs[stims[2]];
		
		byte[] sa=createSound(songA, ABgap, Achannel);
		byte[] sb=createSound(songB, BXgap, Bchannel);
		byte[] sx=createSound(songX, XAgap, Xchannel);
		
		int length=sa.length+sb.length+sx.length;
		
		byte[] output=new byte[length];
		
		if (experimentType==ABX_OPTION){
			System.arraycopy(sa, 0, output, 0, sa.length);
			int x=sa.length;
			System.arraycopy(sb, 0, output, x, sb.length);
			x+=sb.length;
			System.arraycopy(sx, 0, output, x, sx.length);
		}
		else if (experimentType==XAB_OPTION){
			System.arraycopy(sx, 0, output, 0, sx.length);
			int x=sx.length;
			System.arraycopy(sa, 0, output, x, sa.length);
			x+=sa.length;
			System.arraycopy(sb, 0, output, x, sb.length);
		}
		else if (experimentType==BXA_OPTION){
			System.arraycopy(sb, 0, output, 0, sb.length);
			int x=sb.length;
			System.arraycopy(sx, 0, output, x, sx.length);
			x+=sx.length;
			System.arraycopy(sa, 0, output, x, sa.length);
		}
		
		return output;
	}
	
	
	
	public int[][] getTripletSet(){
		
		int[][] triplets=new int[nTrials][];
		
		for (int i=0; i<nTrials; i++){
			boolean found=true;
			while (found){
				int[] prospective=getNextTriplet();
				found=checkTriplet(prospective, triplets, i);
				if (!found){
					triplets[i]=prospective;
				}
			}
		}
		
		return triplets;
	}
	
	public int[][] balanceStimuli(int[][] input){
		
		int[][] triplets=new int[nt][];
		
		for (int i=0; i<nTrials; i++){
			int[] prospective=input[i];
			triplets[i]=prospective;
			triplets[i+nTrials]=new int[3];
			triplets[i+nTrials][0]=prospective[1];
			triplets[i+nTrials][1]=prospective[0];
			triplets[i+nTrials][2]=prospective[2];
		}
		
		for (int i=0; i<nTrials; i++){
			int j=random.nextInt(nTrials-i)+nTrials;
			int[] prospective=triplets[i+nTrials];
			triplets[i+nTrials]=triplets[j];
			triplets[j]=prospective;
		}

		return triplets;
	}
	
	public boolean checkTriplet(int[] p, int[][]r, int n){
		boolean found=false;
		
		for (int i=0; i<n; i++){
			int c=0;
			for (int j=0; j<3; j++){
				for (int k=0; k<3; k++){
					if (p[j]==r[i][k]){
						c++;
						k=3;
					}
				}
			}
			if (c==3){
				found=true;
				i=n;
			}
		}
		
		return found;
	}
	
	public int[] getNextTriplet(){
		
		int[] out=new int[3];
		
		for (int i=0; i<3; i++){
			
			boolean found=true;
			while (found){
				out[i]=random.nextInt(n);
				found=false;
				for (int j=0; j<i; j++){
					if (out[j]==out[i]){
						found=true;
						j=i;
					}
				}	
			}
		}
		return out;
	}
	
	
	
	 public void actionPerformed(ActionEvent e) {
		 if (!disabled){
		 Object object=e.getSource();
		 
		 System.out.println("ACTION TRIGGERED");
		 
		 if (object.equals(start)){
			 start.setEnabled(false);
			 finish.setEnabled(true);
			 //manage.setEnabled(false);
			 setUp();
			 newTrial();
		 }
		 else if(object.equals(left)){
			 currentChoiceLabel.setText("You have chosen LEFT");
			 if (modType==0){
				 System.out.println("HERE!!!!!!!!!!!");
				 responsesVisual[currentPosition]=-1;
			 }
			 else{
				 responsesAuditory[currentPosition]=-1;
			 }
		 }
		 else if(object.equals(right)){
			 currentChoiceLabel.setText("You have chosen RIGHT");
			 if (modType==0){
				 responsesVisual[currentPosition]=1;
			 }
			 else{
				 responsesAuditory[currentPosition]=1;
			 }
		 }
		 else if (object.equals(next)){
			 stopSound();
			 newTrial();
		 }
		 else if (object.equals(repeat)){
			  stopSound();
			  repeatPlayback();
		 }
		 else if (object.equals(back)){
			 stopSound();
			 previousTrial();
		 }
		 else if (object.equals(finish)){ 
			 int a=JOptionPane.showConfirmDialog(this, "Click YES to end the experiment and save your choices, NO to end the experiment without saving your choices.", "Thank you for participating", JOptionPane.YES_NO_CANCEL_OPTION);
			 if (a==JOptionPane.YES_OPTION){
				 writeResultsToDB();
				 currentPosition=-1;
				 setUpResponses();
				 restart();
			 }
			 else if (a==JOptionPane.NO_OPTION){
				 currentPosition=-1;
				 setUpResponses();
				 restart();
			 }
		 }	
		 else if (object.equals(manage)){
			 JPanel managePane=makeManagePane();
			 int a=JOptionPane.showConfirmDialog(this, managePane);
			 if (a==JOptionPane.OK_OPTION){
				 updateSettings();
				 writeDefaults();
				 defaults.writeProperties();
			 }
		 }
		 else if (object.equals(deleteUser)){
			 String s=(String)deleteUser.getSelectedItem();
			 int a=JOptionPane.showConfirmDialog(this, "Really delete participant: "+s+"?");
			 if (a==JOptionPane.OK_OPTION){
				 deleteModel.removeElement(s);
				 String t="DELETE FROM comparetriplet WHERE user='"+s+"'";
				 dbc.writeToDataBase(t);
			 }
		 }
		 
		 else if (object.equals(deleteAllResults)){
			 int a=JOptionPane.showConfirmDialog(this, "Really delete all results from database?");
			 if (a==JOptionPane.OK_OPTION){
				 //String t="DELETE ALL FROM comparetriplet";
				 String t="DELETE FROM comparetriplet";
				 dbc.writeToDataBase(t);
			 }
		 }
		 
		 else if (object.equals(downloadResults)){
			 downloadResultsFromDB();
		 }
		 }
	}	
	 
	public void getResultsFromDB(){
		dbc.getDataABXExpt(this);
	}
	 
	public void downloadResultsFromDB(){
		
		 //String s="INSERT into comparetriplet (user, songA, songB, songX, choice, trial, exptype) VALUES ('"+t+"' , "+songs[u[0]].getSongID()+" , "+songs[u[1]].getSongID()+" , "+songs[u[2]].getSongID()+" , "+responses[i]+" , "+(i+1)+" , "+experimentType+")";

		dbc.getDataABXExpt(this);
		
		LinkedList<Song> songlist=dbc.loadSongDetailsFromDatabase();
		
		SaveDocument sd=new SaveDocument(this, defaults);
		boolean readyToWrite=sd.makeFile();
		if (readyToWrite){
			sd.writeString("Participant");
			sd.writeString("Individual A");
			sd.writeString("Song A");
			sd.writeString("Individual B");
			sd.writeString("Song B");
			sd.writeString("Individual X");
			sd.writeString("Song X");
			sd.writeString("Choice");
			sd.writeString("Trial");
			sd.writeString("Type");
			sd.writeString("Modality");
			
			sd.writeLine();
			
			for (int i=0; i<userData.length; i++){
				sd.writeString(userData[i]);
				//System.out.println(otherData[i][0]+" "+otherData[i][1]+" "+otherData[i][2]+" "+songlist.size());
				
				Song song=dbc.loadSongFromDatabase(otherData[i][0], 0);
				//System.out.println(song.getSongID());
				sd.writeString(song.getIndividualName());
				sd.writeString(song.getName());
				song=dbc.loadSongFromDatabase(otherData[i][1], 0);
				//System.out.println(song.getSongID());
				sd.writeString(song.getIndividualName());
				sd.writeString(song.getName());
				song=dbc.loadSongFromDatabase(otherData[i][2], 0);
				//System.out.println(song.getSongID());
				sd.writeString(song.getIndividualName());
				sd.writeString(song.getName());	
				
				sd.writeInt(otherData[i][3]);
				sd.writeInt(otherData[i][4]);
				sd.writeInt(otherData[i][5]);
				sd.writeInt(otherData[i][6]);
				sd.writeLine();
	
			}
			sd.finishWriting();
		}
	}
	 
	public void updateSettings(){
		double y=(double)((Number)ABGapField.getValue()).doubleValue();
		if (y<0){y=0;}
		ABgap=y;
		y=(double)((Number)BXGapField.getValue()).doubleValue();
		if (y<0){y=0;}
		BXgap=y;
		y=(double)((Number)XAGapField.getValue()).doubleValue();
		if (y<0){y=0;}
		XAgap=y;
		
		int x=(int)((Number)repField.getValue()).intValue();
		repetitions=x;
		
		x=(int)((Number)trialField.getValue()).intValue();
		if (x<1){x=1;}
		nTrials=x;
		
		x=exptOpts.getSelectedIndex();
		if (x>=0){experimentType=x;}
		
		x=modalityOpts.getSelectedIndex();
		if (x>=0){
			modalityType=x;
			modType=modalityType;
			if (modalityType==2){modType=0;}
			if (modalityType==3){modType=1;}
		}
		
		x=AchannelOpts.getSelectedIndex();
		if (x>=0){Achannel=x;}
		x=BchannelOpts.getSelectedIndex();
		if (x>=0){Bchannel=x;}
		x=XchannelOpts.getSelectedIndex();
		if (x>=0){Xchannel=x;}
		setUpResponses();
		currentPosition=-1;
		restart();
		writeDefaults();
	}
	 
	 public void writeResultsToDB(){
		 
		 for (int i=0; i<nt; i++){
				//String comp6="comparetriplet (user CHAR(50), songA INT, songB INT, songX INT, choice INT, trial INT, exptype INT)";
				//writeToDataBase("INSERT INTO songdata (name, IndividualID) VALUES ('"+sname+"' , "+indID+")");
			 String t=snameBox.getText();
			 int[] u=pbSet[i];
			 if (modalityType==0){
				 String s="INSERT into comparetriplet (user, songA, songB, songX, choice, trial, exptype, modtype) VALUES ('"+t+"' , "+songs[u[0]].getSongID()+" , "+songs[u[1]].getSongID()+" , "+songs[u[2]].getSongID()+" , "+responsesVisual[i]+" , "+(i+1)+" , "+experimentType+" , "+modalityType+")";
				 dbc.writeToDataBase(s);
			 }
			 if (modalityType==1){
				 String s="INSERT into comparetriplet (user, songA, songB, songX, choice, trial, exptype, modtype) VALUES ('"+t+"' , "+songs[u[0]].getSongID()+" , "+songs[u[1]].getSongID()+" , "+songs[u[2]].getSongID()+" , "+responsesAuditory[i]+" , "+(i+1)+" , "+experimentType+" , "+modalityType+")";
				 dbc.writeToDataBase(s);
			 }
			 if (modalityType>1){
				 String s="INSERT into comparetriplet (user, songA, songB, songX, choice, trial, exptype, modtype) VALUES ('"+t+"' , "+songs[u[0]].getSongID()+" , "+songs[u[1]].getSongID()+" , "+songs[u[2]].getSongID()+" , "+responsesVisual[i]+" , "+(i+1)+" , "+experimentType+" , "+0+")";
				// System.out.println(s);
				 dbc.writeToDataBase(s);
				 s="INSERT into comparetriplet (user, songA, songB, songX, choice, trial, exptype, modtype) VALUES ('"+t+"' , "+songs[u[0]].getSongID()+" , "+songs[u[1]].getSongID()+" , "+songs[u[2]].getSongID()+" , "+responsesAuditory[i]+" , "+(i+1)+" , "+experimentType+" , "+1+")";
				 dbc.writeToDataBase(s);
				 //System.out.println(s);
			 }
				 //System.out.println(s);
			
		 }
	 }
	 
	 public void setUp(){
		 pbSet=getTripletSet();
		 if (balanceType==1){
			 pbSet=balanceStimuli(pbSet);
		 }
		 setUpPlayback();
	 }
	 
	 public void newTrial(){
		 System.out.println("NEW TRIAL TRIGGERED");
		 currentPosition++;
		 if (currentPosition==nt){
			 
			 if ((modalityType==2)&&(modType==0)){
				 modType=1;
				 currentPosition=0;
				 imagePanel=new JPanel();
				 restart();
				 progressLabel.setText("Trial "+currentPosition+" out of "+nt);
				 currentChoiceLabel.setText("You haven't chosen yet for this trial");
				 int[] d=pbSet[currentPosition];
				 soundData=createSoundStimulus(d);
				 playSound();
			 }
			 else if ((modalityType==3)&&(modType==1)){
				 modType=0;
				 currentPosition=0;
				 restart();
				 progressLabel.setText("Trial "+currentPosition+" out of "+nt);
				 currentChoiceLabel.setText("You haven't chosen yet for this trial");
				 int[] d=pbSet[currentPosition];
				 ImageIcon spectData=createSpectrogramStimulus(d);
				 JLabel label=new JLabel(spectData);
				 imagePanel.removeAll();
				 imagePanel.setSize(spectData.getIconWidth(), spectData.getIconHeight());
					
				 imagePanel.revalidate();
				 imagePanel.add(label);
				 imagePanel.revalidate();
				
				 //playSound();
			 }
			 else{
				 progressLabel.setText("Trial "+currentPosition+" out of "+nt);
				 JOptionPane.showMessageDialog(this, "Experiment completed");
				 next.setEnabled(false);
				 finish.setEnabled(true);
			 }
		 }
		 else{
			progressLabel.setText("Trial "+currentPosition+" out of "+nt);
			currentChoiceLabel.setText("You haven't chosen yet for this trial");
			int[] d=pbSet[currentPosition];
			if (modType==1){
				soundData=createSoundStimulus(d);
				playSound();
			}
			else if (modType==0){
				ImageIcon spectData=createSpectrogramStimulus(d);
				JLabel label=new JLabel(spectData);
				imagePanel.removeAll();
				imagePanel.setSize(spectData.getIconWidth(), spectData.getIconHeight());
				
				imagePanel.revalidate();
				imagePanel.add(label);
				imagePanel.revalidate();
			}
		 }
	 }
	 
	 public void repeatPlayback(){
		 playSound();
	 }
	 
	 public void previousTrial(){
		 currentPosition--;
		 if (currentPosition<0){currentPosition=0;}
		 progressLabel.setText("Trial "+currentPosition+" out of "+nt);
		 if (modType==0){
			 if (responsesVisual[currentPosition]==-1){
				 currentChoiceLabel.setText("You have chosen LEFT");
			 }
			 else if (responsesVisual[currentPosition]==0){
				 currentChoiceLabel.setText("You haven't chosen yet for this trial");
			 }
			 else {
				 currentChoiceLabel.setText("You have chosen RIGHT");
			 }
		 }
		 else{
			 if (responsesAuditory[currentPosition]==-1){
				 currentChoiceLabel.setText("You have chosen LEFT");
			 }
			 else if (responsesAuditory[currentPosition]==0){
				 currentChoiceLabel.setText("You haven't chosen yet for this trial");
			 }
			 else {
				 currentChoiceLabel.setText("You have chosen RIGHT");
			 }
			 int[] d=pbSet[currentPosition];
			 soundData=createSoundStimulus(d);
			 playSound();
		 } 
	 }

		/**
		 * This sets up the sound system for playback
		 */
		public void setUpPlayback(){
			try{
				Song song=dbc.loadSongFromDatabase(songs[0].getSongID(), 0);
				
				
				
				System.out.println("started audio...");
				
				song.makeAudioFormat();
				
				System.out.println("Got through this...");
				
				AudioFormat af2=song.getAf();
				boolean signed=false;
				AudioFormat.Encoding afe=af2.getEncoding();
				if (afe.toString().startsWith("PCM_SIGNED")){signed=true;}
				af=new AudioFormat(af2.getSampleRate(), af2.getSampleSizeInBits(), 2, signed, af2.isBigEndian());
				
				DataLine.Info   info = new DataLine.Info(SourceDataLine.class, af);
				
				if (line!=null){line.close();}
				line = null;
				line = (SourceDataLine) AudioSystem.getLine(info);
				line.open(af);
				
				System.out.println("Got through this...");
				
			} 
			catch (Exception e) {
				e.printStackTrace();
				System.out.println("Here's the problem!");
				System.out.println(e);
				//System.exit(0);
			}//
		}
		
		/**
		 * This shuts down playback on quitting.
		 */
		public void shutDownPlayback(){
			try{
				if (line.isOpen()){
					line.flush();
					//line.stop();
					line.close();
				}
			}
			catch(Exception e){}
		}
	
	 /**
	  * This method organizes playback of the song. It creates a PlaySound instance to actually
	  * carry out the playbacl.
	  * @param a
	  * @param b
	  */
	public void playSound (){
		try{	
			if ((line==null)||(!line.isOpen())){setUpPlayback();}
				
				line.start();
				
				Thread play=new Thread(new PlaySound());
				play.setPriority(Thread.MIN_PRIORITY);
				play.start();
			} 
			catch (Exception e) {
				System.out.println(e);
				System.exit(0);
			}//
		}
		
		/**
		 * This internal class plays the sound file encoded by the Song.
		 * @author Rob
		 *
		 */
		class PlaySound extends Thread{
			
			public PlaySound(){
			}

			public void run(){

				try{
					int p=repetitions;
					if (p<=0){p=1;}
					int i=0;
					while ((i<p)&&(!dostop)){
						line.write(soundData, 0, soundData.length);
						i++;
						if (repetitions<=0){i=0;}
					}
					dostop=false;
				}
				catch (Exception e) {
					e.printStackTrace();
					System.out.println("OWWW");
				}
			}
		}
		
		/**
		 * This is a method to stop playback of the sound 
		 */
		public void stopSound(){
			if (modType==1){
				try{
					dostop=true;
					if (line.isOpen()){
						line.stop();
						line.flush();
						//line.close();
					}
					dostop=false;
				}
				catch (Exception e) {
					System.out.println("Stop playback error");
				}
			}
		}
	
		
		public void writeDefaults(){
			
			defaults.setDoubleProperty("abxabgap", ABgap, 1000);
			defaults.setDoubleProperty("abxbxgap", BXgap, 1000);
			defaults.setDoubleProperty("abxxagap", XAgap, 1000);
			
			defaults.setIntProperty("abxreps", repetitions);
			defaults.setIntProperty("abxtrials", nTrials);
			
			defaults.setIntProperty("abxtype", experimentType);
			defaults.setIntProperty("abxmod", modalityType);
			defaults.setIntProperty("abxachan", Achannel);
			defaults.setIntProperty("abxbchan", Bchannel);
			defaults.setIntProperty("abxXchan", Xchannel);

		}
		
		public void readDefaults(){
			
			ABgap=defaults.getDoubleProperty("abxabgap", 1000, 250);
			BXgap=defaults.getDoubleProperty("abxbxgap", 1000, 300);
			XAgap=defaults.getDoubleProperty("abxxagap", 1000, 500);
			
			repetitions=defaults.getIntProperty("abxreps", 10);
			nTrials=defaults.getIntProperty("abxtrials", 10);
			
			experimentType=defaults.getIntProperty("abxtype", 0);
			modalityType=defaults.getIntProperty("abxmod", 0);
			
			modType=modalityType;
			if (modalityType==2){modType=0;}
			if (modalityType==3){modType=1;}
			
			Achannel=defaults.getIntProperty("abxachan", 0);
			Bchannel=defaults.getIntProperty("abxbchan", 1);
			Xchannel=defaults.getIntProperty("abxxchan", 2);

		}	
	
	

}
