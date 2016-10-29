package lusc.net.github.analysis;

import java.util.Arrays;
import java.util.LinkedList;

import lusc.net.github.Element;
import lusc.net.github.Song;
import lusc.net.github.Syllable;
import lusc.net.github.analysis.clustering.AffinityPropagation;
import lusc.net.github.analysis.clustering.KMedoids;
import lusc.net.github.analysis.clustering.SNNDensity;
import lusc.net.github.analysis.multivariate.MultiDimensionalScaling;
import lusc.net.github.analysis.syntax.EntropyAnalysis;
import lusc.net.github.ui.AnalysisSwingWorker;

/**
 * ComparisonResults is a class that holds the output of a simple comparison. At its heart is a dissimilarity matrix, representing the dissimilarity between the units involved in the comparison.
 * It also provides linked data about the id of the sound units, and some clustering analyses of the comparison
 * @author Rob
 *
 */


public class ComparisonResults {
	
	public static int ELEMENT_TYPE=0;
	public static int COMP_ELEMENT_TYPE=1;
	public static int SYLLABLE_TYPE=2;
	public static int PHRASE_TYPE=3;
	public static int TRANSITION_TYPE=4;
	public static int SONG_TYPE=5;
	public static int INDIVIDUAL_DAY_TYPE=6;
	public static int INDIVIDUAL_TYPE=7;
	
	
	double[][] diss, dissT;
	Song[] songs;
	MultiDimensionalScaling mds=null;
	int type=0;
	int[][] lookUps, individuals, individualDays;
	int[] lookUpIndividual, lookUpIndividualDays, lookUpTypes;
	int n, maxLength, songNumber, individualNumber, individualDayNumber,  tndi;
	String[] names, individualNames, individualDayNames, populations, species, types, sexes, ages, ranks;
	long[] times;
	long maxTime, minTime;
	
	KMedoids km;
	SNNDensity snn;
	EntropyAnalysis ent;
	AffinityPropagation ap;
	
	/**
	 * Constructor for a ComparisonResults
	 * @param songs set of {@link Song songs} that were compared
	 * @param diss the square dissimilarity matrix that was the output of the comparison
	 * @param type the type of song unit compared (from {@link Element} to {@link Individual}.
	 */
	public ComparisonResults(Song[] songs, double[][] diss, int type){
		this.songs=songs;
		this.diss=diss;
		this.type=type;
		n=diss.length;
		songNumber=songs.length;
		//System.out.println("making comparison results "+type);
		makeLookUps();
		calculateMaximumLength();
		calculateIndividuals();
		makeNames();
		//System.out.println("made comparison results halfway "+type);
		makePopulationNames();
		makeSpeciesNames();
		makeSexNames();
		makeAgeNames();
		makeRankNames();
		makeTimes();
		makeTypeNames();
		lookUpTypes=getTypeListArray();
		
		//System.out.println("made comparison results "+type);
	}
	
	/**
	 * gets the Song array that was the basis for the comparison.
	 * @return Song[] array
	 */
	public Song[] getSongs(){
		return songs;
	}
	
	
	public long[] getSongDates(){
		
		long[] x=new long[songs.length];
		for (int i=0; i<songs.length; i++){
			x[i]=songs[i].getTDate();
		}
		return x;
	}
	
	/**
	 * gets the dissimilarity matrix for the comparison.
	 * @return
	 */
	public double[][] getDiss(){
		return diss;
	}
	
	/**
	 * gets the MDS dissimilarity matrix for the comparison.
	 * @return
	 */
	public double[][] getDissT(){
		if (dissT==null){
			return diss;
		}
		else{
			return dissT;
		}
	}
	
	
	
	/**
	 * This method copies/clones one of the score arrays into a new float[][] array and
	 * returns it
	 * @param a an index from 0 for elements to 4 for songs
	 * @return a float[][] dissimilarity matrix
	 */
	public double[][] copy(int a){
		
		double[][] temp=new double[n][];
		for (int i=0; i<n; i++){
			temp[i]=new double[diss[i].length];
			System.arraycopy(diss[i], 0, temp[i], 0, temp[i].length);
		}
		return temp;
	}
	
	/**
	 * sets the dissimilarity matrix for the comparison.
	 * @param b new dissimilarity matrix
	 */
	public void setDiss(double[][] b){
		diss=b;
	}
	
	/**
	 * gets the lookUp table for the comparison.
	 * @return
	 */
	public int[][] getLookUp(){
		return lookUps;
	}
	
	/**
	 * gets the lookUp table for individuals for the comparison.
	 * @return
	 */
	public int[] getLookUpIndividuals(){
		return lookUpIndividual;
	}
	
	
	/**
	 * gets the lookUp table for types for the comparison.
	 * @return
	 */
	public int[] getLookUpTypes(){
		return lookUpTypes;
	}
	
	
	
	
	/**
	 * gets the individuals table for the comparison.
	 * @return
	 */
	public int[][] getIndividuals(){
		//System.out.println("get inds: "+individuals.length);
		return individuals;
	}
	

	/**
	 * gets the individuals table for the comparison.
	 * @return
	 */
	public int[][] getIndividualDays(){
		//System.out.println("get inds: "+individuals.length);
		return individualDays;
	}
	
	/**
	 * gets the songs belonging to each individual for the comparison.
	 * @return
	 */
	public int[][] getIndividualSongs(){
		
		int[][] out=new int[individuals.length][];
		
		for (int i=0; i<individuals.length; i++){
			int[] temp=new int[individuals[i].length];
			int count=0;
			for (int j=0; j<individuals[i].length; j++){
				int p=lookUps[individuals[i][j]][0];
				boolean found=false;
				for (int k=0; k<count; k++){
					if (p==temp[k]){
						found=true;
						k=count;
					}
				}
				if (!found){
					temp[count]=p;
					count++;
				}
			}
			count--;
			out[i]=new int[count];
			for (int j=0; j<count; j++){
				out[i][j]=temp[j];
			}
		}		
		return out;
	}
	
	/**
	 * gets the labels (position) for the comparison.
	 * @return
	 */
	public double[] getPosition(){
		return getPositionListArray();
	}
	
	/**
	 * gets the MDS object for the comparison.
	 * @return
	 */
	public MultiDimensionalScaling getMDS(){
		return mds;
	}
	
	/**
	 * gets the id for the relevant song/individual for various units
	 * @param i the index of the particular element etc to get from the array 
	 * @return an integer [] array containing the song and individual for the selected element
	 */
	
	public int[] getID( int i){
		int[] j=new int[2];
		j[0]=lookUps[i][0];
		j[1]=lookUps[i][1];
		
		return (j);
	}
	
	/**
	 * gets the names array for the comparison.
	 * @return String[] array of names
	 */
	public String[] getNames(){
		return names;
	}
	
	/**
	 * gets the names array for the comparison.
	 * @return String[] array of names
	 */
	public String[] getNames(boolean incspec, boolean incpop, boolean incind, boolean incday, boolean incsong, boolean inctype){
		
		String[] out=new String[n];
		String sep=":";
		
		int[] spec=null;
		if (incspec){spec=getSpeciesListArray();}
		int[] pop=null;
		if (incpop){pop=getPopulationListArray();}
		int[] ind=null;
		if (incind){ind=lookUpIndividual;}
		int[] typ=null;
		if (inctype){typ=getTypeListArray();}
		int[] indday=null;
		if (incday) {indday=lookUpIndividualDays;}
		
		
		for (int i=0; i<n; i++){
			boolean started=false;
			StringBuffer sb=new StringBuffer();
			if (inctype){
				sb.append(types[typ[i]]);
				started=true;
			}
			if (incspec){
				sb.append(species[spec[i]]);
				started=true;
			}
			if (incpop){
				if (started){sb.append(sep);}
				sb.append(populations[pop[i]]);
				started=true;
			}
			if (incind){
				if (started){sb.append(sep);}
				sb.append(individualNames[ind[i]]);
				started=true;
			}
			if (incday) {
				if (started) {sb.append(sep);}
				sb.append(individualDayNames[indday[i]]);
				started=true;
			}
			if (incsong){
				if (started){sb.append(sep);}
				sb.append(names[i]);
				started=true;
			}
			out[i]=sb.toString();
		}
		return out;
	}
	
	/**
	 * gets the names array for the comparison.
	 * @return String[] array of names
	 */
	public String[][] getNamesArray(boolean incspec, boolean incpop, boolean incind, boolean incsong, boolean inctype){
		
		int count=0;
		
		int[] spec=null;
		if (incspec){spec=getSpeciesListArray(); count++;}
		int[] pop=null;
		if (incpop){pop=getPopulationListArray(); count++;}
		int[] ind=null;
		if (incind){ind=lookUpIndividual; count++;}
		int[] typ=null;
		if (inctype){typ=getTypeListArray(); count++;}
		
		
		if (incsong){count++;}
		
		String[][] out=new String[n][count];	
		
		for (int i=0; i<n; i++){
			count=0;
			if (inctype){
				out[i][count]=types[typ[i]];
				count++;
			}
			if (incspec){
				out[i][count]=species[spec[i]];
				count++;
			}
			if (incpop){
				out[i][count]=populations[pop[i]];
				count++;
			}
			if (incind){
				out[i][count]=individualNames[ind[i]];
				count++;
			}
			if (incsong){
				out[i][count]=songs[lookUps[i][0]].getName();
				count++;
			}
		}
		return out;
	}
	
	
	/**
	 * gets the type for the comparison.
	 * @return int index for type
	 */
	public int getType(){
		return type;
	}
	
	/**
	 * gets the maximum length of the unit of the comparison.
	 * @return int index for type
	 */
	public int getMaxLength(){
		return maxLength;
	}
	
	/**
	 * gets the names of vocalisation types in the comparison.
	 * @return String[] array of types
	 */
	public String[] getTypeNames(){
		return types;
	}
	
	/**
	 * gets the names of individuals in the comparison.
	 * @return String[] array of individual names
	 */
	public String[] getIndividualNames(){
		return individualNames;
	}
	
	
	
	/**
	 * gets the names of populations in the comparison.
	 * @return String[] array of population names
	 */
	public String[] getPopulationNames(){
		return populations;
	}
	
	/**
	 * gets the names of sexes in the comparison.
	 * @return String[] array of sex names
	 */
	public String[] getSexNames(){
		return sexes;
	}
	
	/**
	 * gets the names of ages in the comparison.
	 * @return String[] array of age names
	 */
	public String[] getAgeNames(){
		return ages;
	}
	
	/**
	 * gets the names of ranks in the comparison.
	 * @return String[] array of rank names
	 */
	public String[] getRankNames(){
		return ranks;
	}
	
	/**
	 * gets the names of species in the comparison.
	 * @return String[] array of species names
	 */
	public String[] getSpeciesNames(){
		return species;
	}
	
	
	public double[] getRelativeTimes(){
		double[] out=new double[n];
		for (int i=0; i<n; i++){
			out[i]=(times[i]-minTime)/(maxTime-minTime+0.0);
			//System.out.println(out[i]);
		}
		return out;
	}
	
	public long[] getTimes(){
		return times;
	}
	
	public void setKMedoids(KMedoids km){
		this.km=km;
	}
	
	public void setSNNCluster(SNNDensity snn){
		this.snn=snn;
	}
	
	public void setSyntaxCluster(EntropyAnalysis ent){
		this.ent=ent;
	}
	
	public void setAPCluster(AffinityPropagation ap){
		this.ap=ap;
	}
	
	public int getSylExemplar(int p) {
		
		int q=0;
		int r=0;
		for (int i=0; i<songs.length; i++) {
			LinkedList<Syllable> sy=songs[i].getPhrases();
			for (int j=0; j<sy.size(); j++) {
				System.out.println(q+" "+r+" "+i+" "+j);
				Syllable s=sy.get(j);
				int t=s.getNumChildren();
				if (t==0) {t=1;}
				if (q==p) {
					r+=t/2;
					j=sy.size();
					i=songs.length;
				}
				else {
					q++;
					r+=t;
				}
			}
			
		}
		
		System.out.println("CR: SYL EXEMPLAR: "+p+" "+r);
		return r;
	}
	
	public void makeLookUps(){
		
		lookUps=new int[n][2];

		if (type==ELEMENT_TYPE){
			int count1=0;
			for (int i=0; i<songs.length; i++){
				int a1=songs[i].getNumElements2();
				if (a1>0){
					for (int j=0; j<a1; j++){
						lookUps[count1][0]=i;
						lookUps[count1][1]=j;
						count1++;
					}
				}
			
			}
		}
		else if (type==COMP_ELEMENT_TYPE){
			lookUps=new int[n][3];
			int count2=0;
			for (int i=0; i<songs.length; i++){
				
				LinkedList<Syllable> phr=songs[i].getPhrases();
				for (Syllable sy:phr){
					int c=0;
					int max=sy.getMaxSyllLength();
					int p=sy.getNumSyllables()-1;
					Syllable syl=sy.getSyllable(p);
					while (syl.getNumEles()<max){
						p--;
						syl=sy.getSyllable(p);
					}
					int[] q=syl.getEleIds();
					for (int j=0; j<syl.getNumEles(); j++){
						lookUps[count2][0]=i;
						lookUps[count2][1]=q[i];
						lookUps[count2][2]=c;
						count2++;
						c++;
					}	
				}
				/*
				int a=songs[i].getNumPhrases();
				if (a>0){
					int c=0;
					for (int j=0; j<a; j++){
						int[][] p=(int[][])songs[i].getPhrase(j);
						for (int k=0; k<p[0].length; k++){
							lookUps[count2][0]=i;
							int b=p.length-1;
							while (p[b][k]==-1){b--;}
							lookUps[count2][1]=p[b][k];
							lookUps[count2][2]=c;
							count2++;
							c++;
						}				
					}	
				}
				*/
			}			
		}
		else if (type==SYLLABLE_TYPE){
			lookUps=new int[n][3];
			int count3=0;
			for (int i=0; i<songs.length; i++){
				LinkedList<Syllable>phr=songs[i].getPhrases();
				int k=0;
				int jj=0;
				for (Syllable s: phr){
					int a=s.getNumSyllables();
					for (int j=0; j<a; j++){			
						lookUps[count3][0]=i;
						lookUps[count3][1]=jj;
						lookUps[count3][2]=k;
						count3++;
						jj++;
					}
					k++;
				}
				/*
				int a=songs[i].getNumSyllables(1);
				if(a>0){
					for (int j=0; j<a; j++){			
						lookUps[count3][0]=i;
						lookUps[count3][1]=j;
						count3++;
					}					
				}
				*/
			}			
		}
		else if (type==PHRASE_TYPE){
			int count3=0;
			for (int i=0; i<songs.length; i++){
				
				int a=songs[i].getNumSyllables(2);

				if (a>0){
					for (int j=0; j<a; j++){			
						lookUps[count3][0]=i;
						lookUps[count3][1]=j;
						count3++;
					}					
				}
			}			
		}
		else if (type==TRANSITION_TYPE){
			lookUps=new int[n][4];
			int count3=0;
			int count4=0;
			for (int i=0; i<songs.length; i++){
				int a=songs[i].getNumSyllables(2);
				//int a=songs[i].getNumPhrases();
				if (a>0){
					for (int j=0; j<a; j++){			
						if (j>0){
							lookUps[count4][0]=i;
							lookUps[count4][1]=j;
							lookUps[count4][2]=count3-1;
							lookUps[count4][3]=count3;
							count4++;
						}
						count3++;
					}					
				}
			}			
		}
		else if (type==SONG_TYPE){
			//int eleNumber=0;
			for (int i=0; i<songs.length; i++){
				lookUps[i][0]=i;
				lookUps[i][1]=1;
				//eleNumber+=songs[i].getNumElements();
			}
		}
		else if (type==INDIVIDUAL_DAY_TYPE) {
			int[] ids=new int[n];
			int count=0;
			for (int i=0; i<songs.length; i++){
				int p=songs[i].getIndividualDayID();
				boolean found=false;
				for (int j=0; j<count; j++){
					if (ids[j]==p){
						found=true;
						j=count;
					}
				}
				if (!found){
					ids[count]=p;
					lookUps[count][0]=i;
					lookUps[count][1]=1;
					count++;
				}
			}
			
			if (count!=n){
				System.out.println("PARSING ERROR!");
			}
			
			
			
			
		}
		else if (type==INDIVIDUAL_TYPE){
			
			int[] ids=new int[n];
			int count=0;
			for (int i=0; i<songs.length; i++){
				int p=songs[i].getIndividualID();
				boolean found=false;
				for (int j=0; j<count; j++){
					if (ids[j]==p){
						found=true;
						j=count;
					}
				}
				if (!found){
					ids[count]=p;
					lookUps[count][0]=i;
					lookUps[count][1]=1;
					count++;
				}
			}
			
			if (count!=n){
				System.out.println("PARSING ERROR!");
			}
			
			
		}
	}
	
	
	public void augmentScores(double[][] b) {
		int newsize=b.length;
		int counterswitch=0;
		int counternoswitch=0;
		if (newsize==n) {
			
			for (int i=0; i<n; i++) {
				for (int j=0; j<i; j++) {
					if (b[i][j]<diss[i][j]) {
						diss[i][j]=b[i][j];
						counterswitch++;
						
					}	
					else {counternoswitch++;}
				}
			}	
			
			System.out.println("Augmentation switches: "+counterswitch+" "+counternoswitch);
		}
		else {
			
			System.out.println("ERROR ARRAY SIZES DON'T MATCH: "+newsize+" "+n);
		}
	}
	
	
	
	/**
	 * This method calculates maximum lengths for some units
	 */
	public void calculateMaximumLength(){
		maxLength=0;
		int a;
		
		if ((type==ELEMENT_TYPE)||(type==COMP_ELEMENT_TYPE)){
		
			for (int i=0; i<songs.length; i++){
				for (int j=0; j<songs[i].getNumElements(); j++){
					Element ele=(Element)songs[i].getElement(j);
					a=ele.getLength();
					if (a>maxLength){maxLength=a;}
				}
			}
		}
		else if (type==SYLLABLE_TYPE){
			for (int i=0; i<songs.length; i++){
				
				for (Syllable sy: songs[i].getBaseLevelSyllables()){
					int syllLength=sy.getLengthElements(false);
					if (syllLength>maxLength){maxLength=syllLength;}
				}
				
				/*
				int sy=songs[i].getNumSyllables(false);
				for (int j=0; j<sy; j++){
					
					int[] p=(int[])songs[i].getSyllable(j);
					
					Element ele1=(Element)songs[i].getElement(p[0]);
					Element ele2=(Element)songs[i].getElement(p[p.length-1]);
				
					int syllLength=ele2.getBeginTime()+ele2.getLength()-ele1.getBeginTime();
					if (syllLength>maxLength){maxLength=syllLength;}
				}
				*/
			}
		}
		else if (type==PHRASE_TYPE){
			for (int i=0; i<songs.length; i++){
				for (Syllable sy: songs[i].getPhrases()){
					for (Syllable s: sy.getSyllables()){
						int syllLength=s.getLengthElements(false);
						if (syllLength>maxLength){maxLength=syllLength;}
					}
				}		
				/*
				for (int j=0; j<songs[i].getNumPhrases(); j++){
					int[][]p=(int[][])songs[i].getPhrase(j);
					a=p.length-1;
					while (p[a][p[a].length-1]==-1){a--;}
				
					Element ele1=(Element)songs[i].getElement(p[a][0]);
					Element ele2=(Element)songs[i].getElement(p[a][p[a].length-1]);
				
					int syllLength=ele2.getBeginTime()+ele2.getLength()-ele1.getBeginTime();
					if (syllLength>maxLength){maxLength=syllLength;}
				}
				*/
			}
		}
		else if (type==TRANSITION_TYPE){
			int syllLengthPrev=0;
			for (int i=0; i<songs.length; i++){
				LinkedList<Syllable> syls=songs[i].getPhrases();
				
				
				for (int j=0; j<syls.size()-1; j++){
					int maxl=0;
					Syllable sy=syls.get(j);
					for (Syllable s: sy.getSyllables()){
						int syllLength=s.getLengthElements(false);
						if (syllLength>maxl){maxl=syllLength;}
					}
					int maxl2=0;
					sy=syls.get(j+1);
					for (Syllable s: sy.getSyllables()){
						int syllLength=s.getLengthElements(false);
						if (syllLength>maxl2){maxl2=syllLength;}
					}
					maxl+=maxl2;
					if (maxl>maxLength) {maxLength=maxl;}
				}
				
				/*
				for (int j=0; j<songs[i].getNumSyllables(2)-1; j++){
					Syllable sy=syls.get(i);
					Syllable s1=sy.getLastCompleteChild();
					
					sy=syls.get(i+1);
					Syllable s2=sy.getLastCompleteChild();
					
					int transLength=s1.getLengthElements(true)+s2.getLengthElements(false);
				}
				*/
				/*
				for (int j=0; j<songs[i].getNumPhrases(); j++){
					int[][]p=(int[][])songs[i].getPhrase(j);
					a=p.length-1;
					while (p[a][p[a].length-1]==-1){a--;}
				
					Element ele1=(Element)songs[i].getElement(p[a][0]);
					Element ele2=(Element)songs[i].getElement(p[a][p[a].length-1]);
				
					int syllLength=ele2.getBeginTime()+ele2.getLength()-ele1.getBeginTime();
									
					int transLength=syllLength+syllLengthPrev;
					if (transLength>maxLength){maxLength=transLength;}
					
					syllLengthPrev=(int)Math.round(syllLength+(ele2.getTimeAfter()/ele2.getTimeStep()));
					
				}
				*/
			}
		}
		else if (type==SONG_TYPE){
			for (int i=0; i<songs.length; i++){
				Element ele1=(Element)songs[i].getElement(0);
				Element ele2=(Element)songs[i].getElement(songs[i].getNumElements()-1);
			
				int songLength=ele2.getBeginTime()+ele2.getLength()-ele1.getBeginTime();
				if (songLength>maxLength){maxLength=songLength;}
			}
		}
	}
	
	/**
	 * makes names for each Element in the sample
	 * @return a String array of element names
	 * @see lusc.net.github.Element
	 */
	
	public void makeNames(){
		names=new String[n];
		
		if (type==ELEMENT_TYPE){
			int count=0;
			for (int i=0; i<songs.length; i++){
				String n3=songs[i].getName();
				if (n3.endsWith(".wav")){
					int length=n3.length();
					n3=songs[i].getName().substring(0, length-4);
				}
				for (int j=0; j<songs[i].getNumElements2(); j++){
					Integer gr=new Integer(j+1);
					//names[count]=songs[i].getIndividualName()+":"+n3+","+gr.toString();
					names[count]=n3+","+gr.toString();
					count++;
				}
			}
		}
		else if (type==COMP_ELEMENT_TYPE){
			int count=0;
			int maxLength=0;
			for (int i=0; i<songs.length; i++){
				if (songs[i].getNumElements2()>maxLength){
					maxLength=songs[i].getNumElements2();
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
				LinkedList<Syllable> syl=songs[i].getPhrases();
				for (Syllable sy: syl){
					int p=sy.getMaxSyllLength();
					for (int k=0; k<p; k++){
						StringBuffer sb=new StringBuffer();
						for (Syllable s: sy.getSyllables()){
							int[] x=s.getEleIds();
							if (x.length>k){
								sb.append(numberString[x[k]+1]+",");
							}
						}
						sb.deleteCharAt(sb.length()-1);
						names[count]=n3+": "+sb.toString();	
						count++;
					}
				}
				
				/*
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
						//names[count]=songs[i].getIndividualName()+": "+n3+": "+sb.toString();	
						names[count]=n3+": "+sb.toString();	
						count++;
					}
				}
				*/
			}
		}
		else if (type==SYLLABLE_TYPE){
			int count=0;
			for (int i=0; i<songs.length; i++){
				String n3=songs[i].getName();
				if (n3.endsWith(".wav")){
					int length=n3.length();
					n3=songs[i].getName().substring(0, length-4);
				}
				int sy=songs[i].getNumSyllables(1);
				for (int j=0; j<sy; j++){
					Integer gr=new Integer(j+1);
					//names[count]=songs[i].getIndividualName()+":"+n3+","+gr.toString();	
					names[count]=n3+","+gr.toString();	
					count++;
				}
			}
		}
		else if (type==PHRASE_TYPE){
			int count=0;
			for (int i=0; i<songs.length; i++){
				String n3=songs[i].getName();
				if (n3.endsWith(".wav")){
					int length=n3.length();
					n3=songs[i].getName().substring(0, length-4);
				}
				for (int j=0; j<songs[i].getNumSyllables(2); j++){
					Integer gr=new Integer(j+1);
					//names[count]=songs[i].getIndividualName()+":"+n3+","+gr.toString();	
					names[count]=n3+","+gr.toString();	
					count++;
				}
			}
		}
		else if (type==TRANSITION_TYPE){
			for (int i=0; i<n; i++){
				String n2=songs[lookUps[i][0]].getName();
				if (n2.endsWith(".wav")){
					int length=n2.length();
					n2=n2.substring(0, length-4);
				}
				Integer gr=new Integer(lookUps[i][1]+1);
				//names[i]=songs[lookUps[i][0]].getIndividualName()+":"+n2+","+gr.toString();
				names[i]=n2+","+gr.toString();
			}
		}
		else if (type==SONG_TYPE){
			for (int i=0; i<songs.length; i++){
				String n3=songs[i].getName();
				if (n3.endsWith(".wav")){
					int length=n3.length();
					n3=songs[i].getName().substring(0, length-4);
				}				
				names[i]=n3;
			}
		}
		else if (type==INDIVIDUAL_DAY_TYPE){
			for (int i=0; i<n; i++){
				
				//System.out.println(i+" "+names.length+" "+individualDayNames.length+" "+lookUpIndividualDays.length+" "+lookUpIndividualDays[i]);
				
				names[i]=individualDayNames[lookUpIndividualDays[i]];
			}
		}
		else if (type==INDIVIDUAL_TYPE){
			for (int i=0; i<n; i++){
				names[i]=individualNames[lookUpIndividual[i]];
			}
		}
	}
	
	/**
	 *This method calculates the class arrays "individuals" and "individualsO" which match songs/song units to individuals
	 */
	
	public void calculateIndividuals(){
		
		
		individualNumber=0;
		individualDayNumber=0;
		int[][] indLocs=new int[songNumber][2];
		int[][] indDayLocs=new int[songNumber][2];
		for (int i=0; i<lookUps.length; i++){
			boolean found=false;
			for (int j=0; j<individualNumber; j++){
				if (songs[lookUps[i][0]].getIndividualID()==indLocs[j][0]){
					indLocs[j][1]++;
					found=true; 
					j=individualNumber;
				}
			}
			if(!found){
				indLocs[individualNumber][0]=songs[lookUps[i][0]].getIndividualID();
				indLocs[individualNumber][1]=1;
				individualNumber++;
			}
			found=false;
			for (int j=0; j<individualDayNumber; j++){
				if (songs[lookUps[i][0]].getIndividualDayID()==indDayLocs[j][0]){
					indDayLocs[j][1]++;
					found=true; 
					j=individualDayNumber;
				}
			}
			if(!found){
				indDayLocs[individualDayNumber][0]=songs[lookUps[i][0]].getIndividualDayID();
				indDayLocs[individualDayNumber][1]=1;
				individualDayNumber++;
			}
		}
		
		lookUpIndividual=new int [lookUps.length];
		
		for (int i=0; i<lookUps.length; i++){
			for (int j=0; j<indLocs.length; j++){
				if (songs[lookUps[i][0]].getIndividualID()==indLocs[j][0]){
					lookUpIndividual[i]=j;
				}
			}
		}
		
		lookUpIndividualDays=new int [lookUps.length];
		
		for (int i=0; i<lookUps.length; i++){
			for (int j=0; j<indDayLocs.length; j++){
				if (songs[lookUps[i][0]].getIndividualDayID()==indDayLocs[j][0]){
					lookUpIndividualDays[i]=j;
					
				}
			}
			//System.out.println("LOOKUP: "+i+" "+lookUpIndividualDays[i]+" "+songs[lookUps[i][0]].getIndividualDayID());
		}
		
		individuals=new int[individualNumber][];
		individualNames=new String[individualNumber];
		
		for (int i=0; i<individualNumber; i++){
			individuals[i]=new int[indLocs[i][1]];
			int count2=0;
			for (int j=0; j<lookUps.length; j++){
				if (songs[lookUps[j][0]].getIndividualID()==indLocs[i][0]){
					individuals[i][count2]=j; 
					individualNames[i]=songs[lookUps[j][0]].getIndividualName();
					count2++;
				}
			}
		}
		
		individualDays=new int[individualDayNumber][];
		individualDayNames=new String[individualDayNumber];
		
		//System.out.println("INDIVIDUAL DAYS");
		
		for (int i=0; i<individualDayNumber; i++){
			individualDays[i]=new int[indDayLocs[i][1]];
			int count2=0;
			
			
			//System.out.print(i+" ");
			for (int j=0; j<lookUps.length; j++){
				if (songs[lookUps[j][0]].getIndividualDayID()==indDayLocs[i][0]){
					individualDays[i][count2]=j; 
					//System.out.print(j+" ");
					individualDayNames[i]=songs[lookUps[j][0]].getIndividualName()+" "+songs[lookUps[j][0]].getIndividualDayID();
					count2++;
				}
			}
			//System.out.println();
		}
		
		//System.out.println("Comp res: "+type+" inds "+individualNumber);
		
		
	}
	
	/**
	 *This method calculates which units belong to each song in the array 
	 */
	
	public int[][] calculateSongs(){
		
		
		int count=0;
		int[] songIds=new int[n];
		int[] songCounts=new int[n];
		//System.out.println("1");
		for (int i=0; i<n; i++){
			boolean found=false;
			for (int j=0; j<count; j++){
				if (lookUps[i][0]==songIds[j]){
					songCounts[j]++;
					found=true;
					j=count;
				}
			}
			if (!found){
				songIds[count]=lookUps[i][0];
				songCounts[count]=1;
				count++;
			}
		}
		//System.out.println("2");
		int[][] out=new int[count][];
		for (int i=0; i<count; i++){
			out[i]=new int[songCounts[i]];
		}
		songCounts=new int[n];
		
		for (int i=0; i<n; i++){
			for (int j=0; j<count; j++){
				if (lookUps[i][0]==songIds[j]){
					out[j][songCounts[j]]=i;
					songCounts[j]++;
					j=count;
				}
			}
		}
		//System.out.println("3");
		/*for (int i=0; i<count; i++){
			for (int j=0; j<out[i].length; j++){
				System.out.print(out[i][j]+" ");
			}
			System.out.println();
		}
		*/
		
		return out;
	}
	
	/**
	 * calculates type names - extracting all the type names in the sample of songs to be analyzed.
	 */
	
	public void makeTypeNames(){
		LinkedList<String> typeName=new LinkedList<String>();
		for (int i=0; i<songs.length; i++){
			String s=songs[i].getType();
			if (s==null){s=" ";}
			boolean matched=false;
			for (int j=0; j<typeName.size(); j++){
				String t=typeName.get(j);
				if (t.startsWith(s)){
					matched=true;
					j=typeName.size();
				}
			}
			if (!matched){
				//System.out.println("NEW POPULATION: "+s);
				typeName.add(s);
			}
		}
		types=new String[typeName.size()];
		
		types=typeName.toArray(types);
	}
	
	

	/**
	 * calculates population names - extracting all the population names found in the sample of songs to be analyzed.
	 */
	
	public void makePopulationNames(){
		LinkedList<String> populationName=new LinkedList<String>();
		for (int i=0; i<songs.length; i++){
			String s=songs[i].getPopulation();
			boolean matched=false;
			for (int j=0; j<populationName.size(); j++){
				String t=populationName.get(j);
				if (t.equals(s)){
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
	
	/**
	 * calculates sex names - extracting all the sex names found in the sample of songs to be analyzed.
	 */
	
	public void makeSexNames(){
		LinkedList<String> sexName=new LinkedList<String>();
		for (int i=0; i<songs.length; i++){
			String s=songs[i].getSex();
			boolean matched=false;
			for (int j=0; j<sexName.size(); j++){
				String t=sexName.get(j);
				if (t.equals(s)){
					matched=true;
					j=sexName.size();
				}
			}
			if (!matched){
				//System.out.println("NEW Sex: "+s);
				sexName.add(s);
			}
		}
		sexes=new String[sexName.size()];
		
		sexes=sexName.toArray(sexes);
	}
	
	/**
	 * calculates age names - extracting all the age names found in the sample of songs to be analyzed.
	 */
	
	public void makeAgeNames(){
		LinkedList<String> ageName=new LinkedList<String>();
		for (int i=0; i<songs.length; i++){
			String s=songs[i].getAge();
			boolean matched=false;
			for (int j=0; j<ageName.size(); j++){
				String t=ageName.get(j);
				if (t.equals(s)){
					matched=true;
					j=ageName.size();
				}
			}
			if (!matched){
				//System.out.println("NEW Age: "+s);
				ageName.add(s);
			}
		}
		ages=new String[ageName.size()];
		
		ages=ageName.toArray(ages);
	}
	
	/**
	 * calculates rank names - extracting all the rank names found in the sample of songs to be analyzed.
	 */
	
	public void makeRankNames(){
		LinkedList<String> rankName=new LinkedList<String>();
		for (int i=0; i<songs.length; i++){
			String s=songs[i].getRank();
			boolean matched=false;
			for (int j=0; j<rankName.size(); j++){
				String t=rankName.get(j);
				if (t.equals(s)){
					matched=true;
					j=rankName.size();
				}
			}
			if (!matched){
				//System.out.println("NEW Rank: "+s);
				rankName.add(s);
			}
		}
		ranks=new String[rankName.size()];
		
		ranks=rankName.toArray(ranks);
	}
	
	/**
	 * Calculates species names - looking up all the species in the analyzed sample
	 */
	
	public void makeSpeciesNames(){
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
	
	
	public void makeTimes(){
		times=new long[n];
		maxTime=-1;
		minTime=100000000000000l;
		for (int i=0; i<n; i++){
			times[i]=songs[lookUps[i][0]].getTDate();
			if (times[i]>maxTime){maxTime=times[i];}
			if (times[i]<minTime){minTime=times[i];}
			//System.out.println(times[i]+" "+maxTime+" "+minTime);
			
			if (type<2) {
				Element ele=songs[lookUps[i][0]].getEleList2().get(lookUps[i][1]);
				times[i]+=(int)Math.round(ele.getBeginTime()*ele.getTimeStep());	
			}
			else if (type==SYLLABLE_TYPE) {
				Syllable syl=songs[lookUps[i][0]].getBaseLevelSyllables().get(lookUps[i][1]);
				Element ele=syl.getElement2(0);
				//System.out.println("INITIAL: "+times[i]+" "+ele.getBeginTime()+" "+ele.getTimeStep());
				times[i]+=(int)Math.round(ele.getBeginTime()*ele.getTimeStep());
				//System.out.println("AFTER: "+times[i]);
			}
			
			
		}	
	}
	
	
	
	public int lookUpPopulation(int b){
		int c=lookUps[b][0];
		int r=0;
		for (int i=0; i<populations.length; i++){
			if (populations[i].startsWith(songs[c].getPopulation())){
				r=i;
				i=populations.length;
			}
		}
		return r;
	}
	
	/**
	 * returns an array containing the species id for each item
	 * @param h hierarchical level (from Element to Song)
	 * @return an int[] containint the species id for each unit with the sample (see getSpeciesNames)
	 */
	
	public int[] getSpeciesListArray(){
		int[] results=new int[lookUps.length];
		
		for (int i=0; i<lookUps.length; i++){
			for (int j=0; j<species.length; j++){
				if (songs[lookUps[i][0]].getSpecies().equals(species[j])){
					results[i]=j;
					j=species.length;
				}
			}
		}
		return results;
	}
	
	/**
	 * Calculates and returns the type id for each unit.
	 * @param type hierarchical level (from Element to Song)
	 * @return an int[] giving the population id for each unit.
	 */
	
	public int[] getTypeListArray(){
		int[] results=new int[lookUps.length];
		
		for (int i=0; i<lookUps.length; i++){
			for (int j=0; j<types.length; j++){
				String s=songs[lookUps[i][0]].getType();
				if (s==null){s=" ";}
				if (s.equals(types[j])){
					results[i]=j;
					j=types.length;
				}
			}
		}
		return results;
	}
	
	
	/**
	 * Calculates and returns the population id for each unit.
	 * @param type hierarchical level (from Element to Song)
	 * @return an int[] giving the population id for each unit.
	 */
	
	public int[] getPopulationListArray(){
		int[] results=new int[lookUps.length];
		
		for (int i=0; i<lookUps.length; i++){
			for (int j=0; j<populations.length; j++){
				if (songs[lookUps[i][0]].getPopulation().equals(populations[j])){
					results[i]=j;
					j=populations.length;
				}
			}
			//System.out.println("POP: "+results[i]);
		}
		return results;
	}
	
	/**
	 * Calculates and returns the sex id for each unit.
	 * @param type hierarchical level (from Element to Song)
	 * @return an int[] giving the sex id for each unit.
	 */
	
	public int[] getSexListArray(){
		int[] results=new int[lookUps.length];
		
		for (int i=0; i<lookUps.length; i++){
			for (int j=0; j<sexes.length; j++){
				if (songs[lookUps[i][0]].getSex().equals(sexes[j])){
					results[i]=j;
					j=sexes.length;
				}
			}
			//System.out.println("POP: "+results[i]);
		}
		return results;
	}
	
	/**
	 * Calculates and returns the rank id for each unit.
	 * @param type hierarchical level (from Element to Song)
	 * @return an int[] giving the rank id for each unit.
	 */
	
	public int[] getRankListArray(){
		int[] results=new int[lookUps.length];
		
		for (int i=0; i<lookUps.length; i++){
			for (int j=0; j<ranks.length; j++){
				if (songs[lookUps[i][0]].getRank().equals(ranks[j])){
					results[i]=j;
					j=ranks.length;
				}
			}
			//System.out.println("POP: "+results[i]);
		}
		return results;
	}
	
	/**
	 * Calculates and returns the age id for each unit.
	 * @param type hierarchical level (from Element to Song)
	 * @return an int[] giving the age id for each unit.
	 */
	
	public int[] getAgeListArray(){
		int[] results=new int[lookUps.length];
		
		for (int i=0; i<lookUps.length; i++){
			for (int j=0; j<ages.length; j++){
				if (songs[lookUps[i][0]].getAge().equals(ages[j])){
					results[i]=j;
					j=ages.length;
				}
			}
			//System.out.println("POP: "+results[i]);
		}
		return results;
	}
	
	/**
	 * calculates and returns an int array for the position of units within the song
	 * @param h hierarchical level (from Element to Syllable)
	 * @return an int[] indicating position.
	 */
		
	public double[] getPositionListArray(){
		
		double[] results=new double[lookUps.length];
		
		for (int i=0; i<lookUps.length; i++){
			int j=i;
			while ((j<lookUps.length)&&(lookUps[j][0]==lookUps[i][0])){
				j++;
			}
			j--;
			results[i]=lookUps[i][1]/(lookUps[j][1]+0.0);
		}
		return results;
	}
	
	/**
	 * This method gets a subset of the dissimilarity matrix. It does this by using an int[] of 
	 * labels and an int for a particular label value, and extracting all dissimilarity scores where
	 * label values match.
	 * @param label int[] of labels
	 * @param ind int of particular label value
	 * @return double[][] of all dissimilarity scores where label=ind
	 */
	
	public double[][] splitMatrix(int[] label, int ind){
		int c=0;
		for (int i=0; i<label.length; i++){
			if (label[i]==ind){c++;}
		}
		
		double[][] results=new double[c][];
		for (int i=0; i<c; i++){
			results[i]=new double[i+1];
		}
		
		int ii=0;
		int jj=0;
		for (int i=0; i<label.length; i++){
			jj=0;
			if (label[i]==ind){
				for (int j=0; j<i; j++){
					if (label[j]==ind){
						results[ii][jj]=diss[i][j];
						jj++;
					}
				}
				ii++;
			}
		}
		return results;
	}
	
	
	
	/**
	 * This function looks up a subset of the individuals data based on label and ind input data
	 * @param label int[] of values for each song
	 * @param ind an int value to look up in label
	 * @return an int[][] containing ids for each individual in the subset
	 */
	
	public int[][] splitIndSongs(int[] label, int ind){
		int c=0;
		for (int i=0; i<individuals.length; i++){
			int j=individuals[i][0];
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
		for (int i=0; i<individuals.length; i++){
			int j=individuals[i][0];
			if (label[j]==ind){
				results[ii]=new int[individuals[i].length];
				for (int k=0; k<individuals[i].length; k++){
					results[ii][k]=mat[individuals[i][k]];
				}
				ii++;
			}
		}
		return results;
	}
	
	/**
	 * This function extracts from lookup tables all data of a particular class, by matching
	 * an int, ind to an int[] array label. 
	 * @param label int[] array to match data to
	 * @param type hierarchical level to use (from Element to Song)
	 * @param ind label to look for
	 * @return an int[][] of lookUps for a particular subclass of the dataset
	 */
	
	public int[][] splitLookUps(int[] label, int ind){
		int c=0;
		for (int i=0; i<label.length; i++){
			if (label[i]==ind){c++;}
		}
		
		int[][] results=new int[c][];
		
		int ii=0;
		for (int i=0; i<label.length; i++){
			if (label[i]==ind){
				results[ii]=new int[lookUps[i].length];
				System.arraycopy(lookUps[i], 0, results[ii], 0, lookUps[i].length);
				ii++;
			}
		}
		return results;
	}
	
	
	public ComparisonResults splitCompResults(int a){
		boolean[] include=new boolean[n];
		
		int[] pops=getPopulationListArray();
		int count=0;
		for (int i=0; i<n; i++){
			if (pops[i]==a){
				include[i]=true;
				count++;
			}
			else{
				include[i]=false;
			}
		}
		
		double[][] mat=new double[count][];
		for (int i=0; i<count; i++){
			mat[i]=new double[i+1];
		}
		
		int c1=0;
		for (int i=0; i<n; i++){
			if (include[i]){
				int c2=0;
				for (int j=0; j<i; j++){
					if (include[j]){
						mat[c1][c2]=dissT[i][j];
						c2++;
					}
				}
				c1++;
			}
		}
		
		boolean[] includeSongs=new boolean[songs.length];
		for (int i=0; i<songs.length; i++){
			includeSongs[i]=false;
		}
		for (int i=0; i<n; i++){
			if (include[i]){
				includeSongs[lookUps[i][0]]=true;
			}
		}
		int countSongs=0;
		for (int i=0; i<songs.length; i++){
			if(includeSongs[i]){countSongs++;}
		}
		
		Song[] songn=new Song[countSongs];
		c1=0;
		for (int i=0; i<songs.length; i++){
			if (includeSongs[i]){
				songn[c1]=songs[i];
				c1++;
			}
		}
		
		ComparisonResults crn=new ComparisonResults(songn, mat, type);
		
		return crn;
	}


	/**
	 * This method calculates the mean dissimilarity within a matrix
	 * @param score input triangular dissimilarity matrix
	 * @return mean dissimilarity
	 */
	
	public double getMatrixAv(){
		double a=0;
		double b=0;
		for (int i=0; i<diss.length; i++){
			for (int j=0; j<i; j++){
				a+=diss[i][j];
				b++;
			}
		}
		double av=a/b;
		return av;
	}

	public void checkMakeMDS(int ndi, AnalysisSwingWorker asw){
		boolean calc=false;
		
		tndi=ndi;
		if (n<=tndi){
			tndi=n-1;
		}
		
		if (mds==null){calc=true;}
		else if(mds.getnpcs()!=tndi){
			calc=true;
		}
		
		if (calc){
			calculateMDS(tndi, asw);
			makeDissFromMDS();
		}
	}

	public void calculateMDS(int ndi, AnalysisSwingWorker asw){
		
		
		mds=new MultiDimensionalScaling();
		
		try{
			mds.RunNonMetricAnalysis(diss, ndi, asw);
		}
		catch(Exception e){e.printStackTrace();}
	}
	
	public void makeDissFromMDS(){
		dissT=mds.getDistanceMatrix();
	}
	
	public void calculateDissimilarityMatrix(double[][] input){
		int n=input.length;
		int m=input[0].length;
		
		diss=new double[n][];
		for (int i=0; i<n; i++){
			diss[i]=new double[i+1];
		}
		
		for (int i=0; i<n; i++){
			for (int j=0; j<i; j++){
				double s=0;
				for (int k=0; k<m; k++){
					double t=input[i][k]-input[j][k];
					s+=t*t;
				}
				diss[i][j]=Math.sqrt(s);
			}
		}
	}
	
	public double calculatePercentile(double x){
		int m=n*(n-1)/2;
		int y=(int)Math.round(m*x*0.01);
		double[] temp=new double[m];
		
		int c=0;
		for (int i=0; i<n; i++){
			for (int j=0; j<i; j++){
				temp[c]=diss[i][j];
				c++;
			}
		}
		Arrays.sort(temp);
		double out=temp[y];
		temp=null;
		return out;	
	}
	
	public double calculateMax(){
		double c=0;
		for (int i=0; i<n; i++){
			for (int j=0; j<i; j++){
				if(diss[i][j]>c){
					c=diss[i][j];
				}
			}
		}
		return c;
	}
	
	public double calculateMin(){
		double c=Double.MAX_VALUE;
		for (int i=0; i<n; i++){
			for (int j=0; j<i; j++){
				if(diss[i][j]<c){
					c=diss[i][j];
				}
			}
		}
		return c;
	}
	
	public void merge(int p, LinkedList<int[]> pos){
		int[] y=pos.get(p);
		
		//System.out.println(p+" "+y[2]+" "+y[0]+" "+y[1]+" "+pos.size());
		
		pos.remove(p);
		int minScore=Integer.MAX_VALUE;
		int loc=-1;
		for (int i=0; i<pos.size(); i++){
			int[] x=pos.get(i);
			int q=Math.abs(x[3]-y[3]);
			if (q<minScore){
				minScore=q;
				loc=i;
			}
		}
		
		int[]x=pos.get(loc);
		pos.remove(loc);
		
		int[]z=new int[4];
		z[0]=Math.min(x[0], y[0]);
		z[1]=Math.max(x[1], y[1]);
		z[2]=z[1]-z[0];
		z[3]=(int)Math.round(getAverageTime(z[0], z[1]));
		pos.add(loc, z);
		//System.out.println(loc+" "+z[2]+" "+z[0]+ " "+z[1]);
	}
	
	public LinkedList<int[]> getSplits(){
		LinkedList<int[]> output=new LinkedList<int[]>();
		for (int i=0; i<individualDays.length; i++) {
			int[] x=individualDays[i];
			Arrays.sort(x);
			int[] z=new int[4];
			z[0]=lookUps[x[0]][0];
			z[1]=lookUps[x[x.length-1]][0];
			z[2]=z[1]-z[0];
			z[3]=(int)Math.round(getAverageTime(z[0], z[1]));
			output.add(z);
			//System.out.println("SPLITS: "+i+" "+z[0]+" "+z[1]+" "+z[2]+" "+z[3]);
		}
		
		
		/*output=getSplits(40, 72);
		for (int[] z: output) {
			System.out.println(z[0]+" "+z[1]+" "+z[2]+" "+z[3]);
		}
	*/
		
		
		return output;
	}
	
	public LinkedList<int[]> getSplits (int minSongs, int numhrs){
		LinkedList <int[]> pos=new LinkedList<int[]>();
		
		long period=3600000*numhrs;
		
		int loc=0;
		
		long[] xt=getSongDates();
		
		long p=xt[0];
		
		for (int i=0; i<=xt.length; i++){
			if ((i==xt.length)||(xt[i]>p+period)){
				int a=loc;
				int b=i;
				int c=i-loc;
				int d=(int)Math.round(getAverageTime(a, b));
								
				int[] x={a,b,c,d};
				pos.add(x);
				loc=i;
				if (i<xt.length){p=xt[i];}
			}
		}
		
		boolean found=false;
		while (found==false){
			found=true;
			for (int i=0; i<pos.size(); i++){
				int[] x=pos.get(i);
				if (x[2]<minSongs){
					//System.out.println("MERGE");
					if (pos.size()>1){
						merge(i, pos);
						found=false;
					}
					i=pos.size();
				}
			}
		}
		
		return pos;
	}
	
	public double getAverageTime(int a, int b){
		
		long[] x=getSongDates();
		
		double p=0;
		for (int i=a; i<b; i++){
			p+=x[i]-x[0];
		}
		p/=(b-a-0.0);
		p/=1000.0*3600;
		return p;
	}
	

}
