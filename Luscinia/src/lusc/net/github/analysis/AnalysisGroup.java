package lusc.net.github.analysis;


import java.util.*;

import javax.swing.*;

import lusc.net.github.Defaults;
import lusc.net.github.Element;
import lusc.net.github.Song;
import lusc.net.github.db.DataBaseController;
import lusc.net.github.ui.SpectrogramSideBar;


/**
 * AnalysisGroup is a large class that is at the core of comparison-based analysis. At its core are
 * various float[][] matrices that contain the output of comparisons between sets of elements,
 * syllables, syllable transitions and songs 
 * @author Rob
 *
 */
public class AnalysisGroup {

	Song[] songs;
	String[] populations, species;
	int[] syllableRepetitions;
	int songNumber;
	boolean[][] compScheme;
	int eleNumber, eleNumberC, syllNumber, transNumber;
	ComparisonResults scoresEle, scoresEleC, scoresSyll, scoresSyll2, scoreTrans, scoresSong, scoresInd;
	Defaults defaults;
	DataBaseController dbc;
	SpectrogramSideBar ssb=new SpectrogramSideBar(this);
	
	boolean compressElements=true; //THIS DOESNT BELONG HERE
	
	double alignmentCost=0.2;
	double stitchPunishment=150;
	double syllableRepetitionWeighting=0;

	/**
	 * This constructor takes an array of songs, a Defaults objects (to pass to various 
	 * UI components? And a DataBaseController - which allows the object to load the actual
	 * song data from a database. 
	 * @param songs array of {@link lusc.net.github.Song} objects
	 * @param defaults a {@link lusc.net.github.Defaults} object
	 * @param dbc a {@link lusc.net.github.db.DataBaseController} object
	 */
	public AnalysisGroup(Song[] songs, Defaults defaults, DataBaseController dbc){
		this.songs=songs;
		
		calculateSyllableRepetitions();
		countEleNumber();
		songNumber=songs.length;
		this.defaults=defaults;
		this.dbc=dbc;
		
	}
	
	/**
	 * This constructor takes an Array of songs and a Defaults object (almost certainly not needed)
	 * @param songs array of {@link lusc.net.github.Song} objects
	 * @param defaults a {@link lusc.net.github.Defaults} object
	 */
	
	public AnalysisGroup(Song[] songs, Defaults defaults){
		this.songs=songs;
		calculateSyllableRepetitions();
		countEleNumber();
		songNumber=songs.length;
		this.defaults=defaults;
	}
	
	/**
	 * This constructor is for more complex comparison designs, where only certain comparisons
	 * are required
	 * @param songs array of {@link lusc.net.github.Song} objects
	 * @param compScheme a boolean[][] that tells the object which pairs of elements to compare
	 * @param defaults a {@link lusc.net.github.Defaults} object
	 * @param dbc a {@link lusc.net.github.db.DataBaseController} object
	 */
	
	public AnalysisGroup(Song[] songs, boolean[][] compScheme, Defaults defaults, DataBaseController dbc){
		this.songs=songs;
		this.compScheme=compScheme;
		calculateSyllableRepetitions();
		countEleNumber();
		songNumber=songs.length;
		this.defaults=defaults;
		this.dbc=dbc;
	}
	
	/**
	 * Gets the array of Songs used for the analysis
	 * @return an array of {@link lusc.net.github.Song} objects
	 */
	
	public Song[] getSongs(){
		return songs;
	}
	
	/**
	 * Gets a particular Song used for the analysis
	 * @param a index of particular Song to get
	 * @return a {@link lusc.net.github.Song} object
	 */
	
	public Song getSong(int a){
		return songs[a];
	}
	
	/**
	 * Gets the comparison scheme used for complex comparisons
	 * @return a boolean[][] indicating which pairs of songs are to be compared
	 */
	
	public boolean[][] getCompScheme(){
		return compScheme;
	}
	
	/**
	 * Gets an array of names for the populations used in the analysis
	 * @return a String[] containing the names of the populations underlying the songs in the
	 * comparison
	 */
	public String[] getPopulations(){
		return populations;
	}
	
	/**
	 * Gets the Defaults object 
	 * @return a {@link lusc.net.github.Defaults} object
	 */
	public Defaults getDefaults(){
		return defaults;
	}
	
	/**
	 * gets the DataBaseController object
	 * @return a {@link lusc.net.github.db.DataBaseController} object
	 */
	public DataBaseController getDBC(){
		return dbc;
	}
	
	/**
	 * gets the SpectrogramSideBar object - an object containing sketches of subsets of elements
	 * or songs in the list of Songs
	 * @return a {@link lusc.net.github.ui.SpecctrogramSideBar} object
	 */
	
	public SpectrogramSideBar getSSB(){
		return ssb;
	}
	
	/**
	 * gets the boolean switch compressElements which says whether raw element comparisons should
	 * be compressed within a phrase
	 * @return a boolean value
	 */
	
	public boolean getCompressElements(){
		return compressElements;
	}
	
	/**
	 * sets the boolean switch compressElements which says whether raw element comparisons should
	 * be compressed within a phrase
	 * @param a boolean value
	 */
	
	public void setCompressElements(boolean a){
		compressElements=a;
	}
	
	
	
	
	/**
	 * gets the lookUp object for various units
	 * @param a an integer index that determines which unit to use: 0=element,
	 * 1=compressed element, 2=transition, 3=song
	 * @return an integer [][] array for the selected lookUp object
	 */
	
	public int[][] getLookUp(int a){
		int[][] b=null;
		if (a==0){b=scoresEle.getLookUp();}
		else if (a==1){b=scoresEleC.getLookUp();}
		else if (a==2){b=scoresSyll.getLookUp();}
		else if (a==3){b=scoreTrans.getLookUp();}
		else if (a==4){b=scoresSong.getLookUp();}
		return b;
	}
	
	/**
	 * Calculates and returns the population id for each unit.
	 * @param type hierarchical level (from Element to Song)
	 * @return an int[] giving the population id for each unit.
	 */
	
	public int[] getPopulationListArray(int a){
		int[] b=null;
		if (a==0){b=scoresEle.getPopulationListArray();}
		else if (a==1){b=scoresEleC.getPopulationListArray();}
		else if (a==2){b=scoresSyll.getPopulationListArray();}
		else if (a==3){b=scoreTrans.getPopulationListArray();}
		else if (a==4){b=scoresSong.getPopulationListArray();}
		return b;
	}
	
	/**
	 * gets the id for the relevant song/individual for varioua units
	 * @param dataType an integer index that determines which unit to use: 0=element,
	 * 1=compressed element, 2=transition, 3=song
	 * @param i the index of the particular element etc to get from the array 
	 * @return an integer [] array containing the song and individual for the selected element
	 */
	
	public int[] getId(int dataType, int i){
		int[] j=new int[2];
		if (dataType==0){
			j=scoresEle.getID(i);
		}
		else if (dataType==1){
			j=scoresEleC.getID(i);
		}
		else if (dataType==2){
			j=scoresSyll.getID(i);
		}
		else if (dataType==3){
			j=scoreTrans.getID(i);
		}
		else{
			j=scoresSong.getID(i);
		}
		return (j);
	}
	
	/**
	 * gets an int[][] in which the first index lists individuals in the sample, and the second 
	 * lists the units that belong to that individual
	 * @param a an integer index that determines which unit to use: 0=element,
	 * 1=compressed element, 2=transition, 3=song
	 * @return an int[][] containing individual lookups
	 */
	
	public int[][] getIndivData(int a){
		int[][] s=null;
		if (a==0){s=scoresEle.getIndividuals();}
		else if (a==1){s=scoresEleC.getIndividuals();}
		else if (a==2){s=scoresSyll.getIndividuals();}
		else if (a==3){s=scoreTrans.getIndividuals();}
		else if (a==4){s=scoresSong.getIndividuals();}
		return s;
	}
	
	/**
	 * gets the alignmentCost parameter for comparisons 
	 * @return a double value of alignmentCost
	 */
	public double getAlignmentCost(){
		return alignmentCost;
	}
	
	/**
	 * sets the alignmentCost parameter for comparisons 
	 * @param a double value for alignmentCost
	 */
	
	public void setAlignmentCost(double a){
		alignmentCost=a;
	}
	
	
	/**
	 * gets the stichPunishment parameter for comparisons 
	 * @return a double value for stitchPunishment
	 */
	public double getStitchPunishment(){
		return stitchPunishment;
	}
	
	/**
	 * sets the stichPunishment parameter for comparisons 
	 * @param a double value for stitchPunishment
	 */
	public void setStitchPunishment(double a){
		stitchPunishment=a;
	}
	
	/**
	 * gets the syllableRepetitionWeighting parameter for comparisons 
	 * @return a double value for syllableRepetitionWeighting
	 */
	public double getSyllableRepetitionWeighting(){
		return syllableRepetitionWeighting;
	}
	
	/**
	 * sets the syllableRepetitionWeighting parameter for comparisons 
	 * @param a double value for syllableRepetitionWeighting
	 */
	public void setSyllableRepetitionWeighting(double a){
		syllableRepetitionWeighting=a;
	}
	
	/**
	 * Sets the various float[][] score matrices at the heart of this class.
	 * @param a an index from 0 for elements to 4 for songs, and 5 for a second syllable matrix
	 * @param b a float[][] array containing dissimilarities between song units
	 */
	public void setScores(int a, double[][] b){
		if (a==0){
			scoresEle=new ComparisonResults(songs, b, a);
		}
		else if (a==1){
			scoresEleC=new ComparisonResults(songs, b, a);
		}
		else if (a==2){
			scoresSyll=new ComparisonResults(songs, b, a);
		}
		else if (a==3){
			scoreTrans=new ComparisonResults(songs, b, a);
		}
		else if (a==4){
			scoresSong=new ComparisonResults(songs, b, a);
		}
		else if (a==6){
			scoresSyll2=new ComparisonResults(songs, b, a);
		}
		else if (a==5){
			scoresInd=new ComparisonResults(songs, b, a);
		}
	}
	
	/**
	 * Gets the various float[][] score matrices at the heart of this class.
	 * @param a an index from 0 for elements to 4 for songs, and 5 for a second syllable matrix
	 * @return a float[][] array containing dissimilarities between song units
	 */
	public ComparisonResults getScores(int a){
		ComparisonResults b=null;
		if (a==0){b=scoresEle;}
		else if (a==1){b=scoresEleC;}
		else if (a==2){b=scoresSyll;}
		else if (a==3){b=scoreTrans;}
		else if (a==4){b=scoresSong;}
		else if (a==5){b=scoresInd;}
		else if (a==6){b=scoresSyll2;}
		return b;
	}
	
	/**
	 * Gets the labels assigned to each unit.
	 * @param a an index from 0 for elements to 4 for songs
	 * @return a double[] array containing labels for that unit
	 */
	public double[] getLabels(int a){
		double[] s=null;
		if (a==0){s=scoresEle.getPositionListArray();}
		else if (a==1){s=scoresEleC.getPositionListArray();}
		else if (a==2){s=scoresSyll.getPositionListArray();}
		else if (a==3){s=scoreTrans.getPositionListArray();}
		else if (a==4){s=scoresSong.getPositionListArray();}
		return s;
	}
	
	
	/**
	 * This method calculates the length of different units and removes songs with no elements marked
	 */
	public void countEleNumber(){
		eleNumber=0;
		syllNumber=0;
		eleNumberC=0;
		transNumber=0;
		boolean[] x=new boolean[songs.length];
		int songcount=0;
		for (int i=0; i<songs.length; i++){
			int q=songs[i].getNumElements();
			eleNumber+=q;
			if (q==0){
				x[i]=false;
			}
			else{
				x[i]=true;
				songcount++;
			}
			int a=songs[i].getNumPhrases();
			if (a>0){
				syllNumber+=a;
				transNumber+=a-1;
				for (int j=0; j<a; j++){
					int[][] p=(int[][])songs[i].getPhrase(j);
					eleNumberC+=p[0].length;
				}
			}
		}
		if (songcount<songs.length){
			System.out.println("REMOVING EMPTY SONGS");
			Song[] songs2=new Song[songcount];
			songcount=0;
			for (int i=0; i<songs.length; i++){
				int q=songs[i].getNumElements();
				if (q>0){
					songs2[songcount]=songs[i];
					songcount++;
				}
			}
			songs=songs2;
		}
		
		
	}
	
	public int getLengths(int a){
		int b=0;
		if (a==0){
			b=eleNumber;
		}
		else if (a==1){
			b=eleNumberC;
		}
		else if (a==2){
			b=syllNumber;
		}
		else if (a==4){
			b=songNumber;
		}
		return b;	
	}
	
	/**
	 * This method calculates the number of repetitions for each phrase in the sample
	 */
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
				//System.out.println("REPEAT: "+k+" "+syllableRepetitions[k]);
				k++;
			}
		}
		
	}
	
	/**
	 * This method turns every element in the sample into a phrase
	 */
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
		calculateSyllableRepetitions();
		countEleNumber();
	}
	
	/**
	 * This method selects one exemplar syllable for each phrase, using a dtw comparison
	 * within the phrase
	 * @see lusc.net.github.analysis.SyllableCompressor
	 */
	public void pickJustOneExamplePerPhrase(){
		SyllableCompressor sc=new SyllableCompressor();
		for (int i=0; i<songs.length; i++){
			sc.compressSong2(songs[i]);
			//sc.compressSong3(songs[i]);
			songs[i]=sc.s;
		}
		//calculateSyllableRepetitions();
		countEleNumber();
		songNumber=songs.length;
	}
	
	/**
	 * This method re-segments songs into syllables based on a simple time gap threshold
	 * @param thresh a threshold in ms
	 */
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
	
	/**
	 * This method is the first step for loading the raw song data (audio data) into
	 * the song object. This may be useful (in future) for methods like spectrogram
	 * cross correlation, where new spectrograms are needed. This version of the method
	 * loads raw audio data for all songs in the song object.
	 */
	public void checkAndLoadRawData(){
		for (int i=0; i<songs.length; i++){
			checkAndLoadRawData(i);
		}
	}
	
	/**
	 * This method is the first step for loading the raw song data (audio data) into
	 * the song object. This may be useful (in future) for methods like spectrogram
	 * cross correlation, where new spectrograms are needed
	 * @param i index for the song
	 */
	public void checkAndLoadRawData(int i){
		System.out.println("CHECKING RAW DATA: "+i);
		if (songs[i].getRawData()==null){
			System.out.println("RAW DATA LOADING: "+i);
			loadSongRawData(i);
		}
	}
	
	/**
	 * This method is the second step for loading the raw song data (it is called from
	 * the previous method). If you call this method directly, there is no time-saving
	 * check whether the song is already loaded.
	 * @param i index for the song
	 */
	void loadSongRawData(int i){
		Song song=dbc.loadSongFromDatabase(songs[i].getSongID(), 0);
		songs[i]=song;
		//System.out.println(songs[i].getMaxF()+" "+songs[i].getDynRange());
		if ((songs[i].getMaxF()<=1)||(songs[i].getDynRange()<1)){
			defaults.getSongParameters(songs[i]);
		}
		System.out.println(songs[i].getRawDataLength()+" "+songs[i].getSongID());
		
	}
	
	
	
	/**
	 * This function takes Element comparisons and outputs compressed Element comparisons (averages for all versions of an element within a phrase)
	 */
	
	public void compressElements(){
		if (scoresEle!=null){
			CompressComparisons cc=new CompressComparisons();
			double[][] b=cc.compareElements(scoresEle.getDiss(), songs);
			scoresEleC=new ComparisonResults(songs, b, 1);
		}
	}
	
	/**
	 * This function compresses syllables - takes arrays of syllable comparisons, and outputs arrays of phrase comparisons
	 */
	
	public void compressSyllables(){
		CompressComparisons cc=new CompressComparisons();
		double[][] b=cc.phraseComp(scoresEle.getDiss(), songs, (float)alignmentCost);
		scoresSyll=new ComparisonResults(songs, b, 2);	
	}
	
	/**
	 * this is what happens when you get both stitched and non-stitched syllable comparisons
	 * this method compares the two comparisons... and outputs the highest scoring (least similar) one
	 */
	
	public void compressSyllablesBoth(){

		if (scoresSyll2!=null){
			CompressComparisons cc=new CompressComparisons();
			double[][] b=cc.compareSyllables5(scoresSyll2.getDiss(), songs, alignmentCost);
			double[][] a=scoresSyll.getDiss();
			
			for (int i=0; i<a.length; i++){
				for (int j=0; j<i; j++){
					a[i][j]=Math.max(a[i][j], b[i][j]+stitchPunishment);
				}
			}
			scoresSyll.setDiss(a);
		}
	}
	
	/**
	 * This function compresses stitched syllable comparisons into phrase comparisons
	 */
	
	public void compressSyllablesStitch(){
		CompressComparisons cc=new CompressComparisons();
		double[][] b=cc.compareSyllables5(scoresSyll2.getDiss(), songs, alignmentCost);
		scoresSyll=new ComparisonResults(songs, b, 2);
	}
	
	/**
	 * This function compresses syllable-phrase comparisons into transition comparison
	 */
	
	public void compressSyllableTransitions(){
		CompressComparisons cc=new CompressComparisons();
		double[][] b=cc.compareSyllableTransitions2(scoresSyll.getDiss(), songs);
		scoreTrans=new ComparisonResults(songs, b, 3);
		
	}
	
	/**
	 * This function constructs song comparisons, based on phrase or transition comparisons
	 * @param useTrans this flags whether or not to use transition data (otherwise phrase data)
	 * @param lowerProp This is a parameter for a gating function
	 * @param upperProp This is a parameter for a gating function
	 */
	
	public void compressSongs(boolean dtwComp, boolean useTrans, boolean cycle, double upperProp, double lowerProp){
		try{
			System.out.println("Compressing songs: "+dtwComp+" "+useTrans+" "+cycle);
			CompressComparisons cc=new CompressComparisons();
			//float[][]sy=logisticTransform(scoresSyll, true, 0.02, 10);
			//float[][]sy=gateScores(scoresSyll, 0.5, 20);
			if (!dtwComp){
				//double[][] b=cc.compareSongsDigram(scoresSyll.getDiss(), songs, useTrans);
				double q=0;
				if (lowerProp>0){
					q=scoresSyll.calculatePercentile(lowerProp);
				}
				double r=1000000000;
				if (upperProp<100){
					r=scoresSyll.calculatePercentile(upperProp);
				}
				
				double[][] b=cc.compareSongsDigram(scoresSyll, useTrans, cycle, q, r);
				scoresSong=new ComparisonResults(songs, b, 4);
			}
			else{
				//double[][]b=cc.compareSongsDTW(scoresSyll.getDiss(), songs);
				if (useTrans&&(scoreTrans!=null)){
					double q=0;
					if (lowerProp>0){
						q=scoreTrans.calculatePercentile(lowerProp);
					}
					double r=1000000000;
					if (upperProp<100){
						r=scoreTrans.calculatePercentile(upperProp);
					}
					double[][]b=cc.compareSongsDTW(scoreTrans, cycle, q, r);
					scoresSong=new ComparisonResults(songs, b, 4);
				}
				else{
					double q=0;
					if (lowerProp>0){
						q=scoresSyll.calculatePercentile(lowerProp);
					}
					double r=1000000000;
					if (upperProp<100){
						r=scoresSyll.calculatePercentile(upperProp);
					}
					double[][]b=cc.compareSongsDTW(scoresSyll, cycle, q, r);
					scoresSong=new ComparisonResults(songs, b, 4);
				}
			}
		}
		catch(Exception e){e.printStackTrace();}
	}
	
	/**
	 * This function constructs individual comparisons, based on song comparisons
	 */
	public void compressIndividuals(){
		CompressComparisons cc=new CompressComparisons();
		double[][] b=cc.compareIndividuals(scoresSong.getDiss(), scoresSong.getIndividuals());
		scoresInd=new ComparisonResults(songs, b, 5);
	}
	
	/**
	 * This method calculates the mean dissimilarity within a matrix
	 * @param score input triangular dissimilarity matrix
	 * @return mean dissimilarity
	 */
	
	public double getMatrixAv(double[][] score){
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
	
	/**
	 * This method augments phrase dissimilarities by including a component related to their
	 * number of repetitions
	 */
	
	public void augmentSyllDistanceMatrixWithSyllableReps(){
		double[][] b=scoresSyll.getDiss();
		if (syllableRepetitions.length!=b.length){
			System.out.println("AUGMENTATION FAILED: ERROR IN MATRIX SIZE");
		}
		else{
			double sd1=scoresSyll.getMatrixAv();
			//sd=syllableRepetitionWeighting*sd;
			double[][] tempMat=new double[syllableRepetitions.length][];
			for (int i=0; i<syllableRepetitions.length; i++){
				tempMat[i]=new double[i+1];
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
					b[i][j]+=weight*tempMat[i][j];
				}
			}
			scoresSyll.setDiss(b);
			sd2=getMatrixAv(b);
			System.out.println(sd2);
		}
	}
	
	/**
	 * This function is used to calculate the number of shared syllables within a dataset between two individuals
	 * @param a first individual id
	 * @param b second individual id
	 * @param threshold dissimilarity threshold
	 * @return number of shared syllables
	 */
	
	public int getSharedSyllCount(int a, int b, double threshold){
		int count=0;
		int counta=0;
		int countb=0;
		double mindiff=1000000000;
		int[][] lookUps=scoresSyll.getLookUp();
		double[][] scores=scoresSyll.getDiss();
		for (int i=0; i<syllNumber; i++){
			//System.out.println(lookUpSyls[i][0]);
			if (lookUps[i][0]==a){	
				counta++;
				int p=0;
				for (int j=0; j<i; j++){
					if (lookUps[j][0]==b){
						countb++;
						if (scores[i][j]<threshold){
							p++;
						}
						if (scores[i][j]<mindiff){
							mindiff=scores[i][j];
						}
					}
				}
				for (int j=i; j<syllNumber; j++){
					if (lookUps[j][0]==b){
						countb++;
						if (scores[j][i]<threshold){
							p++;
						}
						if (scores[j][i]<mindiff){
							mindiff=scores[j][i];
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
	
	/**
	  * This function is used to calculate the number of shared transitions within a dataset between two individuals
	 * @param a
	 * @param b
	 * @param threshold
	 * @return
	 */
	
	public int getSharedTransCount(int a, int b, double threshold){
		int count=0;
		
		int[][] lookUpTrans=scoreTrans.getLookUp();
		double[][] scores=scoreTrans.getDiss();
		
		for (int i=0; i<transNumber; i++){
			if (lookUpTrans[i][0]==a){				
				for (int j=0; j<i; j++){
					if (lookUpTrans[j][0]==b){
						if ((scores[lookUpTrans[i][2]][lookUpTrans[j][2]]<threshold)&&(scores[lookUpTrans[i][3]][lookUpTrans[j][3]]<threshold)){
							count++;
						}
					}
				}
				for (int j=i; j<transNumber; j++){
					if (lookUpTrans[j][0]==b){
						if ((scores[lookUpTrans[j][2]][lookUpTrans[i][2]]<threshold)&&(scores[lookUpTrans[j][3]][lookUpTrans[i][3]]<threshold)){
							count++;
						}
					}
				}
			}
		}
		System.out.println("Trans sharing: "+a+" "+b+" "+count);
		return count;
	}
	
	/**
	 * 
	 * @param a
	 * @param b
	 * @param threshold
	 * @return
	 */
	
	public double getWeightedSharedTransCount(int a, int b, double threshold){
		double count=0;
		int[][] lookUpTrans=scoreTrans.getLookUp();
		int[][] lookUpSyls=scoresSyll.getLookUp();
		double[][] scores=scoreTrans.getDiss();
		double[][] scoresSy=scoreTrans.getDiss();
		
		for (int i=0; i<transNumber; i++){
			//if ((lookUpTrans[i][0]==a)&&(lookUpTrans[i][1]>0)){		
			if (lookUpTrans[i][0]==a){	
				double c=0;
				for (int j=0; j<i; j++){
					if (lookUpTrans[j][0]==b){
						if ((scores[lookUpTrans[i][2]][lookUpTrans[j][2]]<threshold)&&(scores[lookUpTrans[i][3]][lookUpTrans[j][3]]<threshold)){
							c++;
						}
					}
				}
				for (int j=i; j<transNumber; j++){
					if (lookUpTrans[j][0]==b){
						if ((scores[lookUpTrans[j][2]][lookUpTrans[i][2]]<threshold)&&(scores[lookUpTrans[j][3]][lookUpTrans[i][3]]<threshold)){
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
							if ((j>x1)&&(scoresSy[j][x1]<threshold)){
								d++;
							}
							if ((j<x1)&&(scoresSy[x1][j]<threshold)){
								d++;
							}
							if ((j>x2)&&(scoresSy[j][x2]<threshold)){
								d++;
							}
							if ((j<x2)&&(scoresSy[x2][j]<threshold)){
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
	
	
	
	
	/**
	 * This function cleans up some of the large objects in Analysis when an analysis is complete
	 */
	
	public void cleanUp(){
		songs=null;
		//data=null;
		scoresEle=null;
		scoresEleC=null;
		scoresSyll=null;
		scoresSyll2=null;
		scoreTrans=null;
		scoresSong=null;
		compScheme=null;
	}
	
}
