package lusc.net.github.analysis;


import java.util.*;

import javax.swing.*;

import lusc.net.github.Defaults;
import lusc.net.github.Element;
import lusc.net.github.Song;
import lusc.net.github.db.DataBaseController;
import lusc.net.github.ui.SpectrogramSideBar;
import lusc.net.github.ui.compmethods.DTWSwingWorker;
import lusc.net.github.ui.spectrogram.SpectrPane;

public class AnalysisGroup {

	Song[] songs;
	String[] populations, species;
	int[] syllableRepetitions;
	int eleNumber, eleNumberC, syllNumber, transNumber, songNumber, individualNumber;
	int maxEleLength, maxEleCLength, maxSyllLength, maxTransLength, maxSongLength;
	int[][] lookUpEls, lookUpElsC, lookUpSyls, lookUpTrans, lookUpSongs;
	int[][] individualEle, individualSyl, individualTrans, individualSongs;
	//int[] lookUpSongs;
	int[] eleCounts, sylCounts;
	boolean[][] compScheme;
	float[][] scoresEle, scoresEleC, scoresSyll, scoresSyll2, scoreTrans, scoresSong;
	Defaults defaults;
	DataBaseController dbc;
	SpectrogramSideBar ssb=new SpectrogramSideBar(this);
	
	boolean compressElements=true; //THIS DOESNT BELONG HERE
	
	double alignmentCost=0.2;
	double stitchPunishment=150;
	double syllableRepetitionWeighting=0;

	
	public AnalysisGroup(Song[] songs, Defaults defaults, DataBaseController dbc){
		this.songs=songs;
		
		calculateSyllableRepetitions();
		
		this.defaults=defaults;
		this.dbc=dbc;
		songNumber=songs.length;
		
	}
	
	public AnalysisGroup(Song[] songs, Defaults defaults){
		this.songs=songs;
		this.defaults=defaults;
		songNumber=songs.length;
	}
	
	public AnalysisGroup(Song[] songs, boolean[][] compScheme, Defaults defaults, DataBaseController dbc){
		this.songs=songs;
		this.compScheme=compScheme;
		this.defaults=defaults;
		this.dbc=dbc;
		songNumber=songs.length;
	}
	
	public Song[] getSongs(){
		return songs;
	}
	
	public Song getSong(int a){
		return songs[a];
	}
	
	public boolean[][] getCompScheme(){
		return compScheme;
	}
	
	public String[] getPopulations(){
		return populations;
	}
	
	public Defaults getDefaults(){
		return defaults;
	}
	
	public DataBaseController getDBC(){
		return dbc;
	}
	
	public SpectrogramSideBar getSSB(){
		return ssb;
	}
	
	public boolean getCompressElements(){
		return compressElements;
	}
	
	public void setCompressElements(boolean a){
		compressElements=a;
	}
	
	public int getMaxLength(int a){
		int b=0;
		if (a==0){b=maxEleLength;}
		else if (a==1){b=maxEleCLength;}
		else if (a==2){b=maxSyllLength;}
		else if (a==3){b=maxTransLength;}
		else if (a==4){b=maxSongLength;}
		return b;
	}
	
	public float[][] getScoresEle(){
		return scoresEle;
	}
	
	public float[][] getScoresEleC(){
		return scoresEleC;
	}
	
	public float[][] getScoresSyll(){
		return scoresSyll;
	}
	
	public float[][] getScoresTrans(){
		return scoreTrans;
	}
	
	public float[][] getScoresSong(){
		return scoresSong;
	}
	
	public int getLengths(int a){
		int b=0;
		if (a==0){b=eleNumber;}
		else if (a==1){b=eleNumberC;}
		else if (a==2){b=syllNumber;}
		else if (a==3){b=transNumber;}
		else if (a==4){b=songNumber;}
		return b;
	}
	
	public int[][] getLookUp(int a){
		int[][] b=null;
		if (a==0){b=lookUpEls;}
		else if (a==1){b=lookUpElsC;}
		else if (a==2){b=lookUpSyls;}
		else if (a==3){b=lookUpTrans;}
		else if (a==4){b=lookUpSongs;}
		return b;
	}
	
	public int[] getId(int dataType, int i){
		int[] j=new int[2];
		if (dataType==0){
			j[0]=lookUpEls[i][0];
			j[1]=lookUpEls[i][1];
		}
		else if (dataType==1){
			j[0]=lookUpElsC[i][0];
			j[1]=lookUpElsC[i][1];
		}
		else if (dataType==2){
			j[0]=lookUpSyls[i][0];
			j[1]=lookUpSyls[i][1];
		}
		else if (dataType==3){
			j[0]=lookUpTrans[i][0];
			j[1]=lookUpTrans[i][1];
		}
		else{
			j[0]=i;
			j[1]=-1;
		}
		return (j);
	}
	
	public int[][] getIndivData(int a){
		int[][] s=null;
		if (a==0){s=individualEle;}
		else if (a==1){s=individualEle;}
		else if (a==2){s=individualSyl;}
		else if (a==3){s=individualTrans;}
		else if (a==4){s=individualSongs;}
		return s;
	}
	
	
	public double getAlignmentCost(){
		return alignmentCost;
	}
	
	public void setAlignmentCost(double a){
		alignmentCost=a;
	}
	
	public double getStitchPunishment(){
		return stitchPunishment;
	}
	
	public void setStitchPunishment(double a){
		stitchPunishment=a;
	}
	
	public double getSyllableRepetitionWeighting(){
		return syllableRepetitionWeighting;
	}
	
	public void setSyllableRepetitionWeighting(double a){
		syllableRepetitionWeighting=a;
	}
	
	
	public void setScores(int a, float[][] b){
		if (a==0){scoresEle=b;}
		else if (a==1){scoresEleC=b;}
		else if (a==2){scoresSyll=b;}
		else if (a==3){scoreTrans=b;}
		else if (a==4){scoresSong=b;}
		else if (a==5){scoresSyll2=b;}
	}
	
	public float[][] getScores(int a){
		float[][] b=null;
		if (a==0){b=scoresEle;}
		else if (a==1){b=scoresEleC;}
		else if (a==2){b=scoresSyll;}
		else if (a==3){b=scoreTrans;}
		else if (a==4){b=scoresSong;}
		else if (a==5){b=scoresSyll2;}
		return b;
	}
	
	public double[] getLabels(int a){
		double[] s=null;
		if (a==0){s=labelElements();}
		else if (a==1){s=labelElementsC();}
		else if (a==2){s=labelSyllables();}
		else if (a==3){s=labelTransitions();}
		else if (a==4){s=labelSongs();}
		return s;
	}
	
	public float[][] copy(int a){
		int b=0;
		if (a==0){b=eleNumber;}
		else if (a==1){b=eleNumberC;}
		else if (a==2){b=syllNumber;}
		else if (a==3){b=transNumber;}
		else if (a==4){b=songNumber;}
		
		float[][] temp=new float[b][];
		for (int i=0; i<b; i++){
			float[][] c=getScores(a);
			temp[i]=new float[c[i].length];
			System.arraycopy(c[i], 0, temp[i], 0, temp[i].length);
		}
		return temp;
	}
	
	
	//IS THIS ESSENTIAL?
	public void calculateSyllableRepetitions(){
		int n=0;
		for (int i=0; i<songs.length; i++){
			n+=songs[i].getNumPhrases();
		}
		
		syllableRepetitions=new int[n];
		int k=0;
		for (int i=0; i<songs.length; i++){
			for (int j=0; j<songs[i].getNumPhrases(); j++){
				int[][] p=(int[][])songs[i].getPhrase(j);
				syllableRepetitions[k]=p.length;
				k++;
			}
		}
	}
	
	public void makeEveryElementASyllable(){
		for (int i=0; i<songs.length; i++){
			LinkedList<int[][]> phrases=new LinkedList<int[][]>();
			for (int j=0; j<songs[i].getNumElements(); j++){
				int[][] a=new int[1][1];
				a[0][0]=j;
				phrases.add(a);
			}
			songs[i].setPhrases(phrases);
		}
	}
	
	public void pickJustOneExamplePerPhrase(){
		SyllableCompressor sc=new SyllableCompressor();
		for (int i=0; i<songs.length; i++){
			sc.compressSong2(songs[i]);
			//sc.compressSong3(songs[i]);
			songs[i]=sc.s;
		}
	}
	
	public void segmentSyllableBasedOnThreshold(double thresh){
		for (int i=0; i<songs.length; i++){
			LinkedList<int[][]> phrases=new LinkedList<int[][]>();
			
			int j=0;
			
			while (j<songs[i].getNumElements()){
				int k=j;
				Element ele=(Element)songs[i].getElement(j);
				while ((ele.getTimeAfter()<thresh)&&(ele.getTimeAfter()>-10000)){
					j++;
					ele=(Element)songs[i].getElement(j);
				}
				int[][] a=new int[1][j-k+1];
				for (int l=k; l<=j; l++){
					a[0][l-k]=l;
				}
				phrases.add(a);
				j++;
			}
			
			songs[i].setPhrases(phrases);
		}
	}
	
	public void checkAndLoadRawData(int i){
		if (songs[i].getRawData()==null){
			loadSongRawData(i);
		}
	}
	
	public void loadSongRawData(int i){
		Song song=dbc.loadSongFromDatabase(songs[i].getSongID(), 0);
		songs[i]=song;
		//System.out.println(songs[i].getMaxF()+" "+songs[i].getDynRange());
		if ((songs[i].getMaxF()<=1)||(songs[i].getDynRange()<1)){
			defaults.getSongParameters(songs[i]);
		}
		//System.out.println(songs[i].getRawDataLength());
		
	}
	
	public void makeNames(){
		countEleNumber();
		calculateMaxima();
		makeEleNames();
		makeEleNamesC();
		makeSyllNames();
		makeTransNames();
		makeSongNames();
		//labelElements();
		//labelSyllables();
		//labelTransitions();
		//labelSongs();
		getPopulationNames();
	}
	
	public void countEleNumber(){
		eleNumber=0;
		syllNumber=0;
		eleNumberC=0;
		lookUpSongs=new int[songNumber][2];
		sylCounts=new int[songNumber];
		eleCounts=new int[songNumber];
		int songCount=songNumber;
		for (int i=0; i<songs.length; i++){
			//if (songs[i].phrases==null){songs[i].interpretSyllables();}
			lookUpSongs[i][0]=i;
			lookUpSongs[i][1]=eleNumber;
			if (songs[i].getNumPhrases()==0){
				//System.out.println("alert");
				songCount--;
			}
			//System.out.println(i+" "+songs[i].getNumPhrases());
			sylCounts[i]=songs[i].getNumPhrases();
			eleCounts[i]=songs[i].getNumElements();
			eleNumber+=songs[i].getNumElements();

			int a=songs[i].getNumPhrases();
			if (a>0){
				syllNumber+=a;
				for (int j=0; j<a; j++){
					int[][] p=(int[][])songs[i].getPhrase(j);
					eleNumberC+=p[0].length;
				}
			}
		}
		transNumber=syllNumber-songCount;
		//System.out.println(syllNumber+" "+songNumber+" "+songCount);
		if (transNumber<0){
			transNumber=0;
		}
		//transNumber=syllNumber;
		lookUpEls=new int[eleNumber][2];
		lookUpElsC=new int[eleNumberC][3];
		lookUpSyls=new int[syllNumber][2];
		lookUpTrans=new int[transNumber][4];
		
		int count1=0;
		int count2=0;
		int count3=0;
		int count4=0;
		for (int i=0; i<songs.length; i++){
			int a1=songs[i].getNumElements();
			if (a1>0){
				
				for (int j=0; j<a1; j++){
					lookUpEls[count1][0]=i;
					lookUpEls[count1][1]=j;
					count1++;
				}
			}
			int a=songs[i].getNumPhrases();
			if (a>0){
				int c=0;
				for (int j=0; j<a; j++){
					int[][] p=(int[][])songs[i].getPhrase(j);
					for (int k=0; k<p[0].length; k++){
						lookUpElsC[count2][0]=i;
						
						int b=p.length-1;
						while (p[b][k]==-1){b--;}
					
						lookUpElsC[count2][1]=p[b][k];
						lookUpElsC[count2][2]=c;
						count2++;
						c++;
					}				
					lookUpSyls[count3][0]=i;
					lookUpSyls[count3][1]=j;
					
					if (j>0){
						lookUpTrans[count4][0]=i;
						lookUpTrans[count4][1]=j;
						lookUpTrans[count4][2]=count3-1;
						lookUpTrans[count4][3]=count3;
						count4++;
					}
					count3++;
				}
				
				//lookUpTrans[count4][0]=i;
				//lookUpTrans[count4][1]=-1;
				//count4++;
				
			}
		}
	}
	
	public void calculateMaxima(){
		maxEleLength=0;
		maxSyllLength=0;
		maxTransLength=0;
		maxSongLength=0;
		int a;
		for (int i=0; i<songs.length; i++){
			for (int j=0; j<songs[i].getNumElements(); j++){
				Element ele=(Element)songs[i].getElement(j);
				a=ele.getLength();
				if (a>maxEleLength){maxEleLength=a;}
			}
			int syllLengthPrev=0;
			for (int j=0; j<songs[i].getNumPhrases(); j++){
				int[][]p=(int[][])songs[i].getPhrase(j);
				boolean problem=false;
				for (int k=0; k<p.length; k++){
					if (p[k].length==0){problem=true;}
				}
				if (problem){
					String s="There seems to be a problem with individual "+songs[i].getIndividualName()+", song "+songs[i].getName()+", syllable "+(j+1);
					JOptionPane.showMessageDialog(null,s,"Alert!", JOptionPane.ERROR_MESSAGE);
				}
				else{
					a=p.length-1;
					while (p[a][p[a].length-1]==-1){a--;}
				
					Element ele1=(Element)songs[i].getElement(p[a][0]);
					Element ele2=(Element)songs[i].getElement(p[a][p[a].length-1]);
				
					int syllLength=ele2.getBeginTime()+ele2.getLength()-ele1.getBeginTime();
				
					if (syllLength>maxSyllLength){maxSyllLength=syllLength;}
					
					int transLength=syllLength+syllLengthPrev;
					if (transLength>maxTransLength){maxTransLength=transLength;}
					
					syllLengthPrev=(int)Math.round(syllLength+(ele2.getTimeAfter()/ele2.getTimeStep()));
					
				}
			}
			Element ele1=(Element)songs[i].getElement(0);
			Element ele2=(Element)songs[i].getElement(songs[i].getNumElements()-1);
			
			int songLength=ele2.getBeginTime()+ele2.getLength()-ele1.getBeginTime();
			if (songLength>maxSongLength){maxSongLength=songLength;}
		}
	}

	public String[] getNames(int a){
		String[] b=null;
		if (a==0){b=makeEleNames();}
		else if (a==1){b=makeEleNamesC();}
		else if (a==2){b=makeSyllNames();}
		else if (a==3){b=makeTransNames();}
		else if (a==4){b=makeSongNames();}
		return b;
	}
	
	public String[] makeEleNames(){
		String [] eleNames=new String[eleNumber];
		int count=0;
		for (int i=0; i<songs.length; i++){
			String n3=songs[i].getName();
			if (n3.endsWith(".wav")){
				int length=n3.length();
				n3=songs[i].getName().substring(0, length-4);
			}
			for (int j=0; j<songs[i].getNumElements(); j++){
				Integer gr=new Integer(j+1);
				eleNames[count]=songs[i].getIndividualName()+":"+n3+","+gr.toString();
				//eleNames[count]=songs[i].getIndividualName();
				count++;
			}
		}
		return eleNames;
	}
	
	public String[] makeEleNamesC(){
		String[] eleNamesC=new String[eleNumberC];
		int count=0;
		int maxLength=0;
		for (int i=0; i<songs.length; i++){
			if (songs[i].getNumElements()>maxLength){
				maxLength=songs[i].getNumElements();
			}
		}
		String[] numberString=new String[maxLength+1];
		for (int i=0; i<numberString.length; i++){
			Integer gr1=new Integer(i);
			numberString[i]=gr1.toString();
		}
		for (int i=0; i<songs.length; i++){
			String n3=songs[i].getName();
			if (n3.endsWith(".wav")){
				int length=n3.length();
				n3=songs[i].getName().substring(0, length-4);
			}
			for (int j=0; j<songs[i].getNumPhrases(); j++){
				int[][] p=(int[][])songs[i].getPhrase(j);
				
				for (int k=0; k<p[0].length; k++){
					StringBuffer sb=new StringBuffer();
					
					for (int w=0; w<p.length; w++){
						if (p[w].length>k){
							sb.append(numberString[p[w][k]+1]+",");
						}
					}
					sb.deleteCharAt(sb.length()-1);
					eleNamesC[count]=songs[i].getIndividualName()+": "+n3+": "+sb.toString();
					
					//eleNamesC[count]=songs[i].getIndividualName()+":"+n3+","+numberString[j+1]+"."+numberString[k+1];
					
					count++;
				}
			}
		}
		return eleNamesC;
	}
	
	public String[] makeSyllNames(){
		String[] syllNames=new String[syllNumber];
		//syllLabels=new double[syllNumber];
		int count=0;
		for (int i=0; i<songs.length; i++){
			String n3=songs[i].getName();
			if (n3.endsWith(".wav")){
				int length=n3.length();
				n3=songs[i].getName().substring(0, length-4);
			}
			for (int j=0; j<songs[i].getNumPhrases(); j++){
				Integer gr=new Integer(j+1);
				syllNames[count]=songs[i].getIndividualName()+":"+n3+","+gr.toString();
				
				
				if (syllNames[count].startsWith("ElementOne")){
					syllNames[count]="1";
					//syllLabels[count]=0;
				}
				if (syllNames[count].startsWith("ElementTwo")){
					syllNames[count]="2";
					//syllLabels[count]=0.2;
				}
				if (syllNames[count].startsWith("ElementThree")){
					syllNames[count]="3";
					//syllLabels[count]=0.4;
				}
				if (syllNames[count].startsWith("ElementFour")){
					syllNames[count]="4";
					//syllLabels[count]=0.6;
				}
				if (syllNames[count].startsWith("ElementFive")){
					syllNames[count]="5";
					//syllLabels[count]=0.8;
				}
				if (syllNames[count].startsWith("ElementSix")){
					syllNames[count]="6";
					//syllLabels[count]=1;
				}
				
				count++;
			}
		}
		return syllNames;
	}
	
	public String[] makeTransNames(){
		String[] transNames=new String[transNumber];
		
		for (int i=0; i<transNumber; i++){
			String n=songs[lookUpTrans[i][0]].getName();
			if (n.endsWith(".wav")){
				int length=n.length();
				n=n.substring(0, length-4);
			}
			Integer gr=new Integer(lookUpTrans[i][1]+1);
			transNames[i]=songs[lookUpTrans[i][0]].getIndividualName()+":"+n+","+gr.toString();
		}
		return transNames;
	}
	
	public String[] makeSongNames(){
		String[] songNames=new String[songNumber];
		for (int i=0; i<songs.length; i++){
			String n3=songs[i].getName();
			if (n3.endsWith(".wav")){
				int length=n3.length();
				n3=songs[i].getName().substring(0, length-4);
			}
			
			//songNames[i]=songs[i].getIndividualName()+":"+n3;
			songNames[i]=n3;
		}
		return songNames;
	}
	
	public double[] labelElements(){
		double[] eleLabels=new double[eleNumber];
		for (int i=0; i<eleNumber; i++){
			//eleLabels[i]=lookUpEls[i][1]/(songs[lookUpEls[i][0]].getNumElements()-1.0);
			eleLabels[i]=lookUpEls[i][0]/(songNumber-1.0);
		}
		return eleLabels;
	}
	
	public double[] labelElementsC(){
		double[] eleLabels=new double[eleNumberC];
		for (int i=0; i<eleNumberC; i++){
			int j=lookUpElsC[i][0];
			double a=0;
			for (int k=0; k<songs[j].getNumPhrases(); k++){
				int[][]p=(int[][])songs[j].getPhrase(k);
				a+=p[0].length;			
			}
			a--;
			eleLabels[i]=lookUpElsC[i][2]/a;
		}
		return eleLabels;
	}
	
	public double[] setSyllLabels(int[] dat){
		double[] syllLabels=new double[syllNumber];
		System.out.println("Setting syll labels 1");
		if (dat.length==syllNumber){
	
			int max=0;
			int min=100000;
			
			for (int i=0; i<syllNumber; i++){
				if (dat[i]>max){
					max=dat[i];
				}
				if ((dat[i]>=0)&&(dat[i]<min)){
					min=dat[i];
				}
			}
			double maxd=max;
			double mind=min;
			for (int i=0; i<syllNumber; i++){
				syllLabels[i]=(float)((dat[i]-mind)/(maxd-mind));
			}
		}
		return syllLabels;
	}
	
	public double[] labelSyllables(){
				
		double[] syllLabels=new double[syllNumber];
		double max=0;
		for (int i=0; i<songs.length; i++){
			if (songs[i].getNumPhrases()>max){max=songs[i].getNumPhrases();}
		}
		for (int i=0; i<syllNumber; i++){
			/*
			int j=lookUpSyls[i][0];
			String s=songs[j].species;
			if (s.startsWith("L")){
				syllLabels[i]=0;
			}
			else if(s.startsWith("H")){
				syllLabels[i]=1;
			}
			else{
				syllLabels[i]=0.5;
			}
			*/
			/*
			int j=lookUpSyls[i][0];
			String s=songs[j].individualName;
			if (s.startsWith("cap")){
				syllLabels[i]=0;
			}
			else if(s.startsWith("vin")){
				syllLabels[i]=1;
			}
			else{
				syllLabels[i]=0.5;
			}
			*/
			//syllLabels[i]=(songs[lookUpSyls[i][0]].getNumPhrases()-lookUpSyls[i][1])/(max);	
			syllLabels[i]=lookUpSyls[i][1]/(songs[lookUpSyls[i][0]].getNumPhrases()-1.0);
			//System.out.println(syllLabels[i]+" "+lookUpSyls[i][1]+" "+songs[lookUpSyls[i][0]].getNumPhrases());
			/*int j=lookUpSyls[i][0];
			if (songs[j].name.startsWith("H")){
				syllLabels[i]=0;
			}
			else if (songs[j].name.startsWith("G")){
				syllLabels[i]=0.5;
			}
			else{
				syllLabels[j]=1;
			}*/
		}
		return syllLabels;
	}

	public double[] labelTransitions(){
		double[] transLabels=new double[transNumber];
		for (int i=0; i<transNumber; i++){
			transLabels[i]=(lookUpTrans[i][1]-1.0)/(songs[lookUpTrans[i][0]].getNumPhrases()-2.0);
			if (songs[lookUpTrans[i][0]].getNumPhrases()<=2){
				transLabels[i]=0.5;
			}
		}
		return transLabels;
	}
	
	public double[] labelSongs(){
		double[] songLabels=new double[songs.length];
		
		for (int i=0; i<songs.length; i++){
		
			String s=songs[i].getIndividualName();
			if (s.startsWith("cap")){
				songLabels[i]=0;
			}
			else if(s.startsWith("vin")){
				songLabels[i]=1;
			}
			else{
				songLabels[i]=0.5;
			}
		}
		return songLabels;
	}
	
	public void getPopulationNames(){
		LinkedList<String> populationName=new LinkedList<String>();
		for (int i=0; i<songs.length; i++){
			String s=songs[i].getPopulation();
			boolean matched=false;
			for (int j=0; j<populationName.size(); j++){
				String t=populationName.get(j);
				if (t.startsWith(s)){
					matched=true;
					j=populationName.size();
				}
			}
			if (!matched){
				//System.out.println("NEW POPULATION: "+s);
				populationName.add(s);
			}
		}
		populations=new String[populationName.size()];
		
		populations=populationName.toArray(populations);
	}
	
	public void getSpeciesNames(){
		LinkedList<String> speciesName=new LinkedList<String>();
		for (int i=0; i<songs.length; i++){
			String s=songs[i].getSpecies();
			boolean matched=false;
			for (int j=0; j<speciesName.size(); j++){
				String t=speciesName.get(j);
				if (t.startsWith(s)){
					matched=true;
					j=speciesName.size();
				}
			}
			if (!matched){
				//System.out.println("NEW POPULATION: "+s);
				speciesName.add(s);
			}
		}
		species=new String[speciesName.size()];
		
		species=speciesName.toArray(species);
	}
	
	public int lookUpPopulation(int a, int b){
		int c=0;
		if (a==0){
			c=lookUpEls[b][0];
		}
		else if (a==1){
			c=lookUpElsC[b][0];
		}
		else if (a==2){
			c=lookUpSyls[b][0];
		}
		else if (a==3){
			c=lookUpTrans[b][0];
		}
		else{
			c=b;
		}
		int r=0;
		for (int i=0; i<populations.length; i++){
			if (populations[i].startsWith(songs[c].getPopulation())){
				r=i;
				i=populations.length;
			}
		}
		return r;
	}
		
	
	public int[] getSpeciesListArray(int h){
		getSpeciesNames();
		int[] results=new int[1];
		if (h==0){
			results=new int[lookUpEls.length];
		
			for (int i=0; i<lookUpEls.length; i++){
				for (int j=0; j<species.length; j++){
					if (songs[lookUpEls[i][0]].getSpecies().equals(species[j])){
						results[i]=j;
						j=species.length;
					}
				}
			}
		}
		if (h==1){
			results=new int[lookUpElsC.length];
		
			for (int i=0; i<lookUpElsC.length; i++){
				for (int j=0; j<species.length; j++){
					if (songs[lookUpElsC[i][0]].getSpecies().equals(species[j])){
						results[i]=j;
						j=species.length;
					}
				}
			}
		}
		if (h==2){
			results=new int[lookUpSyls.length];
		
			for (int i=0; i<lookUpSyls.length; i++){
				for (int j=0; j<species.length; j++){
					if (songs[lookUpSyls[i][0]].getSpecies().equals(species[j])){
						results[i]=j;
						j=species.length;
					}
				}
			}
		}
		else if (h==3){
			results=new int[lookUpTrans.length];
			
			for (int i=0; i<lookUpTrans.length; i++){
				for (int j=0; j<species.length; j++){
					if (songs[lookUpTrans[i][0]].getSpecies().equals(species[j])){
						results[i]=j;
						j=species.length;
					}
				}
			}
		}
		else if (h==4){
			results=new int[songs.length];
			for (int i=0; i<songs.length; i++){
				for (int j=0; j<species.length; j++){
					if (songs[i].getSpecies().equals(species[j])){
						results[i]=j;
						j=species.length;
					}
				}
			}
		}
		return results;
	}
		
	
	public int[] getPositionListArray(int h){
		
		int[] results=new int[1];
		
		if (h==1){

			results=new int[lookUpSyls.length];
		
			for (int i=0; i<lookUpSyls.length; i++){
				
				int j=i;
				while ((j<lookUpSyls.length)&&(lookUpSyls[j][0]==lookUpSyls[i][0])){
					j++;
				}
				j--;
			
				double p=lookUpSyls[i][1]/(lookUpSyls[j][1]+0.0);
			
				results[i]=(int)Math.round(p*6);
			
			}
		}
		else if (h==2){
			results=new int[lookUpTrans.length];
			
			for (int i=0; i<lookUpTrans.length; i++){
				
				int j=i;
				while ((j<lookUpTrans.length)&&(lookUpTrans[j][0]==lookUpTrans[i][0])){
					j++;
				}
				j--;
				
				double p=lookUpTrans[i][1]/(lookUpTrans[j][1]+0.0);
				
				results[i]=(int)Math.round(p*6);
				
			}
		}
		
		return results;
	}
	
	//This method identifies which Individual is associated with which song/song unit.
	public int[] calculateSongAssignments(int type){
		int[] results;
		int[][] lookUp=getLookUp(type);
		results=new int[lookUp.length];
		
		for (int i=0; i<lookUp.length; i++){
			for (int j=0; j<individualSongs.length; j++){
				for (int k=0; k<individualSongs[j].length; k++){
					if (individualSongs[j][k]==lookUp[i][0]){
						results[i]=j;
					}
				}
			}
		}		
		return results;
	}
	
	public int[] getPopulationListArray(int type){
		getPopulationNames();
		int[][] lookUp=getLookUp(type);
		int[] results=new int[lookUp.length];
		
		for (int i=0; i<lookUp.length; i++){
			for (int j=0; j<populations.length; j++){
				if (songs[lookUp[i][0]].getPopulation().equals(populations[j])){
					results[i]=j;
					j=populations.length;
				}
			}
		}
		return results;
	}
	
	public void calculateIndividuals(){
		//This method calculates the class arrays "individuals" which contains the song-types, transitions and elements owned by each individual in the set of songs
		int countInds=0;
		int[][] indLocs=new int[songNumber][5];
		for (int i=0; i<songNumber; i++){
			boolean found=false;
			for (int j=0; j<countInds; j++){
				if (songs[i].getIndividualID()==indLocs[j][0]){
					indLocs[j][1]++;
					indLocs[j][2]+=songs[i].getNumElements();
					indLocs[j][3]+=songs[i].getNumPhrases();
					indLocs[j][4]+=songs[i].getNumPhrases()-1;
					found=true; 
					j=countInds;
				}
			}
			if(!found){
				indLocs[countInds][0]=songs[i].getIndividualID();
				System.out.println((countInds+1)+" "+songs[i].getIndividualName());
				indLocs[countInds][1]=1;
				indLocs[countInds][2]=songs[i].getNumElements();
				indLocs[countInds][3]=songs[i].getNumPhrases();
				indLocs[countInds][4]=songs[i].getNumPhrases()-1;
				countInds++;
			}
		}
		individualSongs=new int[countInds][];
		for (int i=0; i<countInds; i++){
			individualSongs[i]=new int[indLocs[i][1]];
			int count2=0;
			for (int j=0; j<songNumber; j++){
				if (songs[j].getIndividualID()==indLocs[i][0]){
					individualSongs[i][count2]=j; 
					count2++;
				}
			}
		}
		individualTrans=new int[countInds][];
		for (int i=0; i<countInds; i++){
			individualTrans[i]=new int[indLocs[i][4]];
			int count2=0;
			for (int j=0; j<lookUpTrans.length; j++){
				int p=lookUpTrans[j][0];
				if (songs[p].getIndividualID()==indLocs[i][0]){
					individualTrans[i][count2]=j; 
					count2++;
				}
			}
		}
		individualSyl=new int[countInds][];
		for (int i=0; i<countInds; i++){
			individualSyl[i]=new int[indLocs[i][3]];
			int count2=0;
			for (int j=0; j<lookUpSyls.length; j++){
				int p=lookUpSyls[j][0];
				if (songs[p].getIndividualID()==indLocs[i][0]){
					individualSyl[i][count2]=j; 
					count2++;
				}
			}
		}
		individualEle=new int[countInds][];
		for (int i=0; i<countInds; i++){
			individualEle[i]=new int[indLocs[i][2]];
			int count2=0;
			for (int j=0; j<lookUpEls.length; j++){
				int p=lookUpEls[j][0];
				if (songs[p].getIndividualID()==indLocs[i][0]){
					individualEle[i][count2]=j; 
					count2++;
				}
			}
		}
		indLocs=null;
		individualNumber=countInds;
	}
	
	public void compressElements(){
		if (scoresEle!=null){
			CompressComparisons cc=new CompressComparisons();
			scoresEleC=cc.compareElements(scoresEle, songs);
		}
	}
	
	public void compressSyllables(){
		CompressComparisons cc=new CompressComparisons();
		scoresSyll=cc.phraseComp(scoresEle, songs, (float)alignmentCost);		
	}
	
	public void compressSyllablesBoth(){
		//this is what happens when you get both stitched and non-stitched syllable comparisons
		//this method compares the two comparisons...
		
		if (scoresSyll2!=null){
			CompressComparisons cc=new CompressComparisons();
			scoresSyll2=cc.compareSyllables5(scoresSyll2, songs, alignmentCost);
		
			for (int i=0; i<scoresSyll.length; i++){
				for (int j=0; j<i; j++){
				System.out.println(i+" "+j+" "+scoresSyll[i][j]+" "+scoresSyll2[i][j]);
				scoresSyll[i][j]=(float)Math.max(scoresSyll[i][j], scoresSyll2[i][j]+stitchPunishment);
				}
			}
		}
	}
	
	public void compressSyllablesStitch(){
		CompressComparisons cc=new CompressComparisons();
		scoresSyll=cc.compareSyllables5(scoresSyll2, songs, alignmentCost);
	}
	
	public void compressSyllableTransitions(){
		CompressComparisons cc=new CompressComparisons();
		scoreTrans=cc.compareSyllableTransitions2(scoresSyll, songs);
		
	}
	
	public void compressSongs(boolean useTrans, double lowerProp, double upperProp){
		try{
			System.out.println("Compressing songs");
			CompressComparisons cc=new CompressComparisons();
			//float[][]sy=logisticTransform(scoresSyll, true, 0.02, 10);
			//float[][]sy=gateScores(scoresSyll, 0.5, 20);
			if (!useTrans){
				scoresSong=cc.compareSongsDigram(scoresSyll, songs, useTrans);
			}
			else{
				scoresSong=cc.compareSongsDTW(scoresSyll, songs);	
			}
		}
		catch(Exception e){e.printStackTrace();}
	}
	
	public double getMatrixAv(float[][] score){
		double a=0;
		double b=0;
		for (int i=0; i<score.length; i++){
			for (int j=0; j<i; j++){
				a+=score[i][j];
				b++;
			}
		}
		double av=a/b;
		return av;
	}
	
	public void augmentSyllDistanceMatrixWithSyllableReps(){
		if (syllableRepetitions.length!=scoresSyll.length){
			System.out.println("AUGMENTATION FAILED: ERROR IN MATRIX SIZE");
		}
		else{
			double sd1=getMatrixAv(scoresSyll);
			//sd=syllableRepetitionWeighting*sd;
			float[][] tempMat=new float[syllableRepetitions.length][];
			for (int i=0; i<syllableRepetitions.length; i++){
				tempMat[i]=new float[i+1];
				for (int j=0; j<i; j++){
					double q=Math.log(syllableRepetitions[i])-Math.log(syllableRepetitions[j]);
					tempMat[i][j]=(float)Math.sqrt(q*q);
				}
			}
			double sd2=getMatrixAv(tempMat);
			//sd2=0.749591084;
			float weight=(float)(syllableRepetitionWeighting*sd1/sd2);
			System.out.println("WEIGHT: "+weight+" "+sd2);
			//weight=0.12161444f;
			//weight=0.05111025f;
			//weight=0.1022205f;
			for (int i=0; i<syllableRepetitions.length; i++){
				for (int j=0; j<i; j++){
					scoresSyll[i][j]+=weight*tempMat[i][j];
				}
			}
			
		}
	}
	
	public int getSharedSyllCount(int a, int b, double threshold){
		int count=0;
		int counta=0;
		int countb=0;
		double mindiff=1000000000;
		for (int i=0; i<syllNumber; i++){
			//System.out.println(lookUpSyls[i][0]);
			if (lookUpSyls[i][0]==a){	
				counta++;
				int p=0;
				for (int j=0; j<i; j++){
					if (lookUpSyls[j][0]==b){
						countb++;
						if (scoresSyll[i][j]<threshold){
							p++;
						}
						if (scoresSyll[i][j]<mindiff){
							mindiff=scoresSyll[i][j];
						}
					}
				}
				for (int j=i; j<syllNumber; j++){
					if (lookUpSyls[j][0]==b){
						countb++;
						if (scoresSyll[j][i]<threshold){
							p++;
						}
						if (scoresSyll[j][i]<mindiff){
							mindiff=scoresSyll[j][i];
						}
					}
				}
				if (p>0){count++;}
				//count+=p;
			}
		}
		//System.out.println(syllNumber+" "+lookUpSyls.length+" "+scoresSyll.length);
		System.out.println("Syll sharing: "+a+" "+b+" "+counta+" "+countb+" "+count+" "+threshold+" "+mindiff);
		return count;
	}
	
	public int getSharedTransCount(int a, int b, double threshold){
		int count=0;
		for (int i=0; i<transNumber; i++){
			if (lookUpTrans[i][0]==a){				
				for (int j=0; j<i; j++){
					if (lookUpTrans[j][0]==b){
						if ((scoresSyll[lookUpTrans[i][2]][lookUpTrans[j][2]]<threshold)&&(scoresSyll[lookUpTrans[i][3]][lookUpTrans[j][3]]<threshold)){
							count++;
						}
					}
				}
				for (int j=i; j<transNumber; j++){
					if (lookUpTrans[j][0]==b){
						if ((scoresSyll[lookUpTrans[j][2]][lookUpTrans[i][2]]<threshold)&&(scoresSyll[lookUpTrans[j][3]][lookUpTrans[i][3]]<threshold)){
							count++;
						}
					}
				}
			}
		}
		System.out.println("Trans sharing: "+a+" "+b+" "+count);
		return count;
	}
	
	public double getWeightedSharedTransCount(int a, int b, double threshold){
		double count=0;
		for (int i=0; i<transNumber; i++){
			//if ((lookUpTrans[i][0]==a)&&(lookUpTrans[i][1]>0)){		
			if (lookUpTrans[i][0]==a){	
				double c=0;
				for (int j=0; j<i; j++){
					if (lookUpTrans[j][0]==b){
						if ((scoresSyll[lookUpTrans[i][2]][lookUpTrans[j][2]]<threshold)&&(scoresSyll[lookUpTrans[i][3]][lookUpTrans[j][3]]<threshold)){
							c++;
						}
					}
				}
				for (int j=i; j<transNumber; j++){
					if (lookUpTrans[j][0]==b){
						if ((scoresSyll[lookUpTrans[j][2]][lookUpTrans[i][2]]<threshold)&&(scoresSyll[lookUpTrans[j][3]][lookUpTrans[i][3]]<threshold)){
							c++;
						}
					}
				}
				if (c>0){
					int x1=lookUpTrans[i][2];
					int x2=lookUpTrans[i][3];
					double d=0;
					
					for (int j=0; j<syllNumber; j++){
						if (lookUpSyls[j][0]==b){
							if ((j>x1)&&(scoresSyll[j][x1]<threshold)){
								d++;
							}
							if ((j<x1)&&(scoresSyll[x1][j]<threshold)){
								d++;
							}
							if ((j>x2)&&(scoresSyll[j][x2]<threshold)){
								d++;
							}
							if ((j<x2)&&(scoresSyll[x2][j]<threshold)){
								d++;
							}
						}
					}
					count+=2*c/d;
				}
			}
		}
		System.out.println("Weighted sharing: "+a+" "+b+" "+count);
		return count;
	}
	
	public float[][] splitMatrix(int[] label, int type, int ind){
		int c=0;
		for (int i=0; i<label.length; i++){
			if (label[i]==ind){c++;}
		}
		
		float[][] results=new float[c][];
		float[][] scores=getScores(type);
		for (int i=0; i<c; i++){
			results[i]=new float[i+1];
		}
		
		int ii=0;
		int jj=0;
		for (int i=0; i<label.length; i++){
			jj=0;
			if (label[i]==ind){
				for (int j=0; j<i; j++){
					if (label[j]==ind){
						results[ii][jj]=scores[i][j];
						jj++;
					}
				}
				ii++;
			}
		}
		return results;
	}
	
	public int[] splitCounts(int[] label, int h, int ind){
		int c=0;
		for (int i=0; i<label.length; i++){
			if (label[i]==ind){c++;}
		}
		
		int[] results=new int[c];
		
		int ii=0;
		for (int i=0; i<label.length; i++){
			if (label[i]==ind){
				if (h==0){
					results[ii]=eleCounts[i];
				}
				else if (h==1){
					results[ii]=eleCounts[i];
				}
				else if (h==2){
					results[ii]=sylCounts[i];
				}
				ii++;
			}
		}
		return results;
	}
	
	public int[][] splitIndSongs(int[] label, int ind){
		int c=0;
		for (int i=0; i<individualSongs.length; i++){
			int j=individualSongs[i][0];
			if (label[j]==ind){c++;}
		}
		
		int[][] results=new int[c][];
		
		int[] mat=new int[label.length];
		int d=0;
		for (int i=0; i<label.length; i++){
			if (label[i]==ind){
				mat[i]=d;
				d++;
			}
			else{
				mat[i]=-1;
			}
		}
		
		int ii=0;
		for (int i=0; i<individualSongs.length; i++){
			int j=individualSongs[i][0];
			if (label[j]==ind){
				results[ii]=new int[individualSongs[i].length];
				for (int k=0; k<individualSongs[i].length; k++){
					results[ii][k]=mat[individualSongs[i][k]];
				}
				ii++;
			}
		}
		return results;
	}
	
	public int[][] splitLookUps(int[] label, int type, int ind){
		int c=0;
		for (int i=0; i<label.length; i++){
			if (label[i]==ind){c++;}
		}
		
		int[][] results=new int[c][];
		int[][] lookUp=getLookUp(type);
		
		int ii=0;
		for (int i=0; i<label.length; i++){
			if (label[i]==ind){
				results[ii]=new int[lookUp[i].length];
				System.arraycopy(lookUp[i], 0, results[ii], 0, lookUp[i].length);
				ii++;
			}
		}
		return results;
	}
	
	public void cleanUp(){
		songs=null;
		//data=null;
		individualSongs=null;
		individualTrans=null;
		individualSyl=null;
		individualEle=null;
		lookUpEls=null;
		lookUpSyls=null;
		lookUpTrans=null;
		lookUpSongs=null;
		//scoresEle=null;
		//scoresEleC=null;
		//scoresSyll=null;
		//scoresSyll2=null;
		//scoreTrans=null;
		//scoresSong=null;
		compScheme=null;
	}
	
}
