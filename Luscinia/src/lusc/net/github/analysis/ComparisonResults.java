package lusc.net.github.analysis;

import java.util.LinkedList;

import lusc.net.github.Element;
import lusc.net.github.Song;
import lusc.net.github.analysis.multivariate.MultiDimensionalScaling;
import lusc.net.github.ui.AnalysisSwingWorker;

public class ComparisonResults {
	
	public static int ELEMENT_TYPE=0;
	public static int COMP_ELEMENT_TYPE=1;
	public static int SYLLABLE_TYPE=2;
	public static int TRANSITION_TYPE=3;
	public static int SONG_TYPE=4;
	public static int INDIVIDUAL_TYPE=5;
	
	
	double[][] diss, dissT;
	Song[] songs;
	MultiDimensionalScaling mds=null;
	int type=0;
	int[][] lookUps, individuals;
	int[] lookUpIndividual;
	int n, maxLength, songNumber, individualNumber, tndi;
	String[] names, individualNames, populations, species;
	
	public ComparisonResults(Song[] songs, double[][] diss, int type){
		this.songs=songs;
		this.diss=diss;
		this.type=type;
		n=diss.length;
		songNumber=songs.length;
		System.out.println("making comparison results "+type);
		makeLookUps();
		calculateMaximumLength();
		calculateIndividuals();
		makeNames();
		
		makePopulationNames();
		makeSpeciesNames();
		System.out.println("made comparison results "+type);
	}
	
	/**
	 * gets the Song array that was the basis for the comparison.
	 * @return Song[] array
	 */
	public Song[] getSongs(){
		return songs;
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
		return dissT;
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
	 * gets the individuals table for the comparison.
	 * @return
	 */
	public int[][] getIndividuals(){
		System.out.println("get inds: "+individuals.length);
		return individuals;
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
	 * gets the names of species in the comparison.
	 * @return String[] array of species names
	 */
	public String[] getSpeciesNames(){
		return species;
	}
	
	
	
	public void makeLookUps(){
		
		lookUps=new int[n][2];

		if (type==ELEMENT_TYPE){
			int count1=0;
			for (int i=0; i<songs.length; i++){
				int a1=songs[i].getNumElements();
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
			}			
		}
		else if (type==SYLLABLE_TYPE){
			int count3=0;
			for (int i=0; i<songs.length; i++){
				int a=songs[i].getNumPhrases();
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
				int a=songs[i].getNumPhrases();
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
				for (int j=0; j<songs[i].getNumPhrases(); j++){
					int[][]p=(int[][])songs[i].getPhrase(j);
					a=p.length-1;
					while (p[a][p[a].length-1]==-1){a--;}
				
					Element ele1=(Element)songs[i].getElement(p[a][0]);
					Element ele2=(Element)songs[i].getElement(p[a][p[a].length-1]);
				
					int syllLength=ele2.getBeginTime()+ele2.getLength()-ele1.getBeginTime();
					if (syllLength>maxLength){maxLength=syllLength;}
				}
			}
		}
		else if (type==TRANSITION_TYPE){
			int syllLengthPrev=0;
			for (int i=0; i<songs.length; i++){
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
				for (int j=0; j<songs[i].getNumElements(); j++){
					Integer gr=new Integer(j+1);
					names[count]=songs[i].getIndividualName()+":"+n3+","+gr.toString();
					count++;
				}
			}
		}
		else if (type==COMP_ELEMENT_TYPE){
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
						names[count]=songs[i].getIndividualName()+": "+n3+": "+sb.toString();						
						count++;
					}
				}
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
				for (int j=0; j<songs[i].getNumPhrases(); j++){
					Integer gr=new Integer(j+1);
					names[count]=songs[i].getIndividualName()+":"+n3+","+gr.toString();	
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
				names[i]=songs[lookUps[i][0]].getIndividualName()+":"+n2+","+gr.toString();
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
		int[][] indLocs=new int[songNumber][2];
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
		}
		
		lookUpIndividual=new int [lookUps.length];
		
		for (int i=0; i<lookUps.length; i++){
			for (int j=0; j<indLocs.length; j++){
				if (songs[lookUps[i][0]].getIndividualID()==indLocs[j][0]){
					lookUpIndividual[i]=j;
				}
			}
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
		
		System.out.println("Comp res: "+type+" inds "+individualNumber);
		
		
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
		if (mds==null){
			calculateMDS(ndi, asw);
			makeDissFromMDS();
		}
	}

	public void calculateMDS(int ndi, AnalysisSwingWorker asw){
		
		tndi=ndi;
		if (n<=tndi){
			tndi=n-1;
		}
		mds=new MultiDimensionalScaling();
		
		try{
			mds.RunNonMetricAnalysis(diss, tndi, asw);
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
	
	
	

}
