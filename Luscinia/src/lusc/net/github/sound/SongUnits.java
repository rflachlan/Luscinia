package lusc.net.github.sound;

import java.awt.Component;
import java.util.LinkedList;

import javax.swing.JOptionPane;


public class SongUnits {
	
	
	Song song;
	
	//Analysis stored data
	LinkedList<Element> eleList;
	LinkedList<Element> eleList2;
	LinkedList<Syllable> sylList;
	boolean[] phraseId;
	
	int numSylls=0;
		
		
	public SongUnits(Song song) {
		
		this.song=song;
		sylList=new LinkedList<Syllable>();
		eleList=new LinkedList<Element>();
	}
	
	public void clearUp() {
		sylList=null;
		eleList=null;
		eleList2=null;
	}
	
	public void setUpElements() {
		setEleGaps();
		sortEles();
	}
	
	
	
	/**
	 * This method splits a song object into multiple song objects by its syllables
	 * making a set of new 'songs' that are returned in an array.
	 */
	public Song[] splitSong(boolean phraseOnly){
		
		Sound sound=song.sound;
		
		Song[] songs=new Song[sylList.size()];
		for (int i=0; i<sylList.size(); i++){
			
			Syllable sy=sylList.get(i);
			if ((!phraseOnly)||(sy.getNumParents()==0)) {
				int[] syll=sy.getLoc();
				
				
				Song s=new Song();
				Sound s2=sound.getSubSound(s, (int)Math.round(syll[0]-20), (int)Math.round(syll[1]+20));
				
				s.sound=s2;
				String name=song.name;
				String sn=name;
				if (name.endsWith(".wav")){
					sn=name.substring(0, name.length()-4);
				}
				if (name.endsWith(".mp3")){
					sn=name.substring(0, name.length()-4);
				}
				if (name.endsWith(".aif")){
					sn=name.substring(0, name.length()-4);
				}
				if (name.endsWith(".aiff")){
					sn=name.substring(0, name.length()-5);
				}
				s.name=sn+"_"+(i+1);
				s.individualID=song.individualID;
				s.ind=song.ind;
				s.tDate=song.tDate+syll[0];
				s.recordEquipment=song.recordEquipment;
				s.recordist=song.recordist;
				s.location=song.location;
				s.notes=song.notes;
				s.quality=song.quality;
				s.type=song.type;
				s.custom[0]=song.custom[0];
				s.custom[1]=song.custom[1];
				songs[i]=s;
			}
		}
		//System.out.println("A list of "+syllList.size()+" songs has been made and returned");
		return songs;
	}
	
	
	
	public int getNumSyllables(int type){
		if (sylList==null){
			return 0;
		}
		if (type==0){
			return sylList.size();
		}
		else if (type==1){
			return getBaseLevelSyllables().size();
		}
		return getPhrases().size();
	}
	
	
	
	public LinkedList<Syllable> getBaseLevelSyllables(){
		
		LinkedList<Syllable> base=new LinkedList<Syllable>();
		
		for (Syllable sy: sylList){
			if (sy.getNumChildren()==0){
				base.add(sy);
			}
		}
		return base;
		
	}
	
	public Syllable getBaseLevelSyllable(int a){
		
		LinkedList<Syllable> base=getBaseLevelSyllables();
		
		
		return base.get(a);
		
	}
	
	public LinkedList<Syllable> getPhrases(){
		
		LinkedList<Syllable> phrases=new LinkedList<Syllable>();
		
		for (Syllable sy: sylList){
			if (sy.getNumParents()==0){
				phrases.add(sy);
			}
		}
		return phrases;
		
	}
	
	public Syllable getPhrase(int a){
		LinkedList<Syllable> phrases=getPhrases();
		return phrases.get(a);
	}
	
	
	

	/**
	 * removes a specified syllable from the syllList
	 * @param a location of the syllable to be removed from the syllList
	 */
	
	public void removeSyllable(int a){
		if (a<sylList.size()){
			Syllable syll=sylList.get(a);
			sylList.remove(a);
			for (Syllable sy: sylList){
				sy.children.remove(syll);
				sy.parents.remove(syll);
			}
		}
	}
	
	
	public void setNumSylls(int a){
		numSylls=a;
	}
	
	public int getNumSylls(){
		return numSylls;
	}
	
	/**
	 * adds a new syllable at a specific location in the syllList
	 * @param a location to add the new syllable
	 * @param syl an int[] specifying a syllable
	 */
	public void addSyllable(int a, int[] syl){
		
		Syllable syllable=new Syllable(syl, song.songID);
		syllable.addFamily(sylList, eleList);
		syllable.checkMaxLevel();
		sylList.add(a, syllable);
		//syllList.add(a, syl);
	}

	/**
	 * adds a new syllable to the end of the syllList
	 * @param syl an int[] specifying a syllable
	 */
	public void addSyllable(int[] syl){
		Syllable syllable=new Syllable(syl, song.songID);
		syllable.addFamily(sylList, eleList);
		syllable.checkMaxLevel();
		sylList.add(syllable);
		//syllList.add(syl);
	}

	/**
	 * Empties the whole list of syllables
	 */
	public void clearSyllables(){
		sylList.clear();
		//syllList.clear();
	}

	/**
	 * Sets the whole eleList to an external LinkedList
	 * @param a a LinkedList of Elements
	 * @see Element
	 */
	public void setEleList(LinkedList<Element> a){
		eleList=a;
	}

	/**
	 * Gets a specified Element from the eleList
	 * @param a index of Element to get
	 * @return an {@link Element}
	 */
	public Element getElement(int a){
		if (a>=eleList.size()){
			return null;
		}
		return eleList.get(a);
	}

	/**
	 * Gets the number of elements in this Song object
	 * @return the number of Elements
	 * @see Element
	 */
	public int getNumElements(){
		if (eleList==null){
			return 0;
		}
		return eleList.size();
	}
	
	/**
	 * Gets the number of elements in list 2 of this Song object
	 * @return the number of Elements
	 * @see Element
	 */
	public int getNumElements2(){
		if (eleList2==null){
			return eleList.size();
		}
		return eleList2.size();
	}
	
	/**
	 * Gets the list 2 of elements of this Song object
	 * @return Element list
	 * @see Element
	 */
	public LinkedList<Element> getEleList2(){
		if (eleList2==null){
			return eleList;
		}
		return eleList2;
	}

	/**
	 * Removes a specified element from eleList
	 * @param a index of Element to remove
	 * @see Element
	 */
	public void removeElement(int a){
		if (a<eleList.size()){
			eleList.remove(a);
		}
	}

	/**
	 * Adds an element to the eleList, at a specific location
	 * @param a location to add a new Element
	 * @param ele an Element to add
	 * @see Element
	 */
	public void addElement(int a, Element ele){
		eleList.add(a, ele);
	}

	/**
	 * Adds an element to the eleList, at the end
	 * @param ele an Element to add
	 * @see Element
	 */
	public void addElement(Element ele){
		eleList.add(ele);
	}

	/**
	 * Empties the linked list of elements
	 * @see Element
	 */
	public void clearElements(){
		eleList.clear();
	}
		
	/**
	 * This method calculates gaps before and after elements in eleList
	 */
	public void calculateGaps(){
		float gapbefore=0f;
		float gapafter=0f;
		for (int i=0; i<eleList.size(); i++){
			Element ele=eleList.get(i);
			if (i==0){gapbefore=-10000f;}
			gapafter=-10000f;
			if (i<eleList.size()-1){
				Element ele2=eleList.get(i+1);
				gapafter=(float)(ele2.signal[0][0]*ele2.timeStep-ele.signal[ele.length-1][0]*ele.timeStep);
			}
			ele.timeBefore=gapbefore;
			ele.timeAfter=gapafter;
		}
	}
		
		
	/**
	 * This method sorts syllables into chronological order
	 */
	public void sortSylls(){
		if (sylList!=null){
			int num=sylList.size();
			int [] dat;
			while (num>0){
				int min=10000000;
				int loc=0;
				for (int i=0; i<num; i++){
					dat=sylList.get(i).getLoc();
					if (dat[0]<min){
						loc=i;
						min=dat[0];
					}
				}
				Syllable sy=sylList.get(loc);
				sylList.remove(loc);
				sylList.addLast(sy);
				num--;
			}
			dat=null;
		}
	}
	
	public void addPhrase(){
		int min=Integer.MAX_VALUE;
		int max=Integer.MIN_VALUE;
			
		for (Syllable s: sylList){
			if (s.start<min){min=s.start;}
			if (s.end>max){max=s.end;}
		}
		int[] p={min-1, max+1};
		Syllable s=new Syllable(p, song.songID);
			
		s.addFamily(sylList, eleList);
		s.checkMaxLevel();
		sylList.add(s);						
	}
		
	/**
	 * This method sorts elements into chronological order
	*/
	public void sortEles(){
		if (eleList!=null){
			int num=eleList.size();
			while (num>0){
				double min=10000000;
				int loc=0;
				for (int i=0; i<num; i++){
					Element ele=eleList.get(i);
					double j=ele.signal[0][0]+0.5*(ele.signal[ele.signal.length-1][0]-ele.signal[0][0]);
					if (j<min){
						loc=i;
						min=j;
					}
				}
				Element ele=eleList.get(loc);
				eleList.remove(loc);
				eleList.addLast(ele);
				num--;
			}
		}
	}
		
	public void mergeEleList(double threshold){
		eleList2=new LinkedList<Element>();
		for (Syllable s : sylList){
				if (s.maxLevel==1){
					//System.out.println("merge");
					s.mergeElements(threshold);
					eleList2.addAll(s.eles2);
				}
				//a++;
			}
			//System.out.println("Syllable merging: "+eleList.size()+" "+threshold+" "+eleList2.size());
		}
		
		public void compressElements(double reductionFactor, int minPoints, boolean logFrequencies, double slopeATanTransform){
			for (Element ele: eleList){
				ele.compressElements(reductionFactor, minPoints, logFrequencies, slopeATanTransform);
			}
		}
		
		public void makeEveryElementASyllable(){
			
			sylList=new LinkedList<Syllable>();
			
			for (Element ele : eleList){
				LinkedList<Element>el=new LinkedList<Element>();
				el.add(ele);
				Syllable syl=new Syllable(el, song.songID);
				syl.addFamily(sylList, eleList);
				sylList.add(syl);
			}
			
			for (Syllable syl : sylList){
				syl.checkMaxLevel();
			}

		}
		
		/**
		 * This method re-segments songs into syllables based on a simple time gap threshold
		 * @param thresh a threshold in ms
		 */
		public void segmentSyllableBasedOnThreshold(double thresh){
			
			sylList=new LinkedList<Syllable>();
			
			int currentStart=Integer.MIN_VALUE;
			LinkedList<Element>el=new LinkedList<Element>();
			boolean start=true;
			for (Element ele : eleList){
				
				
				if ((start)||((ele.getTimeAfter()<thresh)&&(ele.getTimeAfter()>-10000))){
					el.add(ele);
				}
				else{
					Syllable sy=new Syllable(el, song.songID);
					sy.addFamily(sylList, eleList);
					sylList.add(sy);
					el=new LinkedList<Element>();
				}
				if (start){start=false;}
			}
			
			for (Syllable syl : sylList){
				syl.checkMaxLevel();
			}
		}
		
		public void loadSyllables(LinkedList<int[]> ds){
			sylList=new LinkedList<Syllable>();
			
			for (int[] d: ds){
				Syllable syl=new Syllable(d, song.songID);
				//System.out.println("SYLL!: "+syl.getLoc()[0]+" "+syl.getLoc()[1]);
				syl.addFamily(sylList, eleList);
				syl.checkMaxLevel();
				sylList.add(syl);
			}
			
			sortSylls();
			
		}
		
		public LinkedList<String[]> checkSyllables(){
			
			LinkedList<String[]> output=new LinkedList<String[]>();
			
			LinkedList<Integer> oe=checkOrphanElements();
			
			if (oe.size()>0){
				StringBuffer sb=new StringBuffer();
			
				for (Integer a: oe){
					sb.append(a+", ");
				}	
				
				
				String[] x=new String[4];
			
				x[0]=song.ind.getName();
				x[1]=song.name;
				x[2]="orphaned elements: ";
				x[3]=sb.toString();
				output.add(x);

			}
			
			LinkedList<Integer> me=checkMalformedPhrase(sylList);
			
			if (me.size()>0){
				StringBuffer sb=new StringBuffer();
			
				for (Integer a: me){
					sb.append(a+", ");
				}	
				
				
				String[] x=new String[4];
			
				x[0]=song.ind.getName();
				x[1]=song.name;
				x[2]="elements in malformed phrase: ";
				x[3]=sb.toString();
				output.add(x);

			}
			
			LinkedList<Syllable> os=checkChildlessSyllables(sylList);
			
			if (os.size()>0){
				StringBuffer sb=new StringBuffer();
			
				for (Syllable a: os){
					sb.append((a.id+1)+", ");
				}	
				
				
				String[] x=new String[4];
				
				x[0]=song.ind.getName();
				x[1]=song.name;
				x[2]="childless syllables: ";
				x[3]=sb.toString();
				output.add(x);
			}
			
			LinkedList<Syllable> ov=checkOverlappingSyllable(sylList);
			
			if (ov.size()>0){
				StringBuffer sb=new StringBuffer();
				
				for (Syllable a: ov){
					sb.append((a.id+1)+", ");
				}	
				
				
				String[] x=new String[4];
				
				x[0]=song.ind.getName();
				x[1]=song.name;
				x[2]="overlapping syllables: ";
				x[3]=sb.toString();
				output.add(x);
			}
			
			LinkedList<Syllable> ls=checkSyllableLevels(sylList);
			
			if (ls.size()>0){
				StringBuffer sb=new StringBuffer();
			
				for (Syllable a: ls){
					sb.append((a.id+1)+", ");
				}	
				
				String[] x=new String[4];
				
				x[0]=song.ind.getName();
				x[1]=song.name;
				x[2]="incorrect syllable hierarchy: ";
				x[3]=sb.toString();
				output.add(x);
			}
			
			return output;
		}
		
		
		
		//UI ASPECTS SHOULD NOT BE HERE!
		public boolean checkSong(Component parentComponent){		
			
			for (Syllable sy: sylList){
				sy.checkMaxLevel();
			}
			
			LinkedList<Integer> oe=checkOrphanElements();
			
			if (oe.size()>0){
				StringBuffer sb=new StringBuffer();
			
				for (Integer a: oe){
					sb.append(a+", ");
				}	
				
				String s="The following elements are orphans (belong to no syllable): " + sb.toString() +" do you want to add syllables for them?";
				
				int c=JOptionPane.showConfirmDialog(parentComponent, s, "Warning", JOptionPane.YES_NO_CANCEL_OPTION);
				
				if (c==JOptionPane.YES_OPTION){
					adoptOrphanedElements(oe, sylList);
				}
				else if (c==JOptionPane.CANCEL_OPTION){
					return false;	
				}	
			}
			
			LinkedList<Syllable> os=checkChildlessSyllables(sylList);
			
			if (os.size()>0){
				StringBuffer sb=new StringBuffer();
			
				for (Syllable a: os){
					sb.append((a.id+1)+", ");
				}	
				
				String s="The following syllables are childless (have no elements): " + sb.toString() +" do you want to delete them?";
				
				int c=JOptionPane.showConfirmDialog(parentComponent, s, "Warning", JOptionPane.YES_NO_CANCEL_OPTION);
				
				if (c==JOptionPane.YES_OPTION){
					deleteSyllables(os, sylList);
				}
				else if (c==JOptionPane.CANCEL_OPTION){
					return false;	
				}	
			}
			
			LinkedList<Syllable> ls=checkSyllableLevels(sylList);
			
			if (ls.size()>0){
				StringBuffer sb=new StringBuffer();
			
				for (Syllable a: ls){
					sb.append((a.id+1)+", ");
				}	
				
				String s="The following syllables have a hierarcical level > 2: " + sb.toString() +" do you want to delete them?";
				
				int c=JOptionPane.showConfirmDialog(parentComponent, s, "Warning", JOptionPane.YES_NO_CANCEL_OPTION);
				
				if (c==JOptionPane.YES_OPTION){
					deleteSyllables(ls, sylList);
				}
				else if (c==JOptionPane.CANCEL_OPTION){
					return false;	
				}	
			}
			
			
			return true;
		}
		
		public LinkedList<Syllable> checkSyllableLevels(LinkedList<Syllable> syls){
			LinkedList<Syllable> levels=new LinkedList<Syllable>();
			
			for (Syllable sy: syls){
				if (sy.maxLevel>2){
					levels.add(sy);				
				}	
			}
			
			return levels;
		}
		
		public LinkedList<Syllable> checkOverlappingSyllable(LinkedList<Syllable> syls){
			LinkedList<Syllable> overlaps=new LinkedList<Syllable>();
			
			for (Syllable sy: syls){
				for (Syllable sy2 : syls){
					if ((sy.start<sy2.start)&&(sy.end>sy2.start)&&(sy.end<sy2.end)){
						overlaps.add(sy);
					}
					
				}
			}
			
			return overlaps;
		}
		
		public LinkedList<Syllable> checkChildlessSyllables(LinkedList<Syllable> syls){
			
			LinkedList<Syllable> childless=new LinkedList<Syllable>();
			
			for (Syllable sy: syls){
				if (sy.eles.size()==0){
					childless.add(sy);				
				}	
			}

			return childless;
		}
		
		public void deleteSyllables(LinkedList<Syllable> oe, LinkedList<Syllable> syls){
			/*
			LinkedList<int[]> rlist=new LinkedList<int[]>();
			for (Syllable sy : oe){
				rlist.add(syllList.get(sy.id));
				sy.remove();
			}
			
			syls.removeAll(oe);
			syllList.removeAll(rlist);
			*/
			sylList.removeAll(oe);

		}
		
		public LinkedList<Integer> checkOrphanElements(){
			
			LinkedList<Integer> orphans=new LinkedList<Integer>();
			
			for (int i=0; i<eleList.size(); i++){
				Element ele=eleList.get(i);
				
				if (ele.syls.size()==0){
					orphans.add(new Integer(i+1));
				}	
			}
			return orphans;
		}
		
		
		public LinkedList<Integer> checkMalformedPhrase(LinkedList<Syllable> syls){
			LinkedList<Integer> orphans=new LinkedList<Integer>();
			
			for (int i=0; i<eleList.size(); i++){
				Element ele=eleList.get(i);
				
				int p=ele.getMinLevel();
				int q=ele.syls.size();
				
				if ((p>1)||(q>2)){
					System.out.println(i+" "+p+" "+q+" "+syls.size());
					for (Syllable sy: ele.syls){
						System.out.println(sy.start+" "+sy.end);
					}
					orphans.add(new Integer(i+1));
				}	
			}
			
			for (Syllable sy: syls){
				int p=-1;
				for (Element ele : sy.eles){
					int q=ele.syls.size();
					
					if (p==-1){p=q;}
					else if (p!=q){
						System.out.println(ele.id+" "+q+" "+ele.getMinLevel());
						orphans.add(new Integer(ele.id));
					}
				}
			}
			
			return orphans;
		}
		
		public void adoptOrphanedElements(LinkedList<Integer> oe, LinkedList<Syllable> syls){
			
			for (Integer a: oe){
				
				int b=a.intValue()-1;
				
				Element ele=eleList.get(b);
				int start=(int)Math.round(ele.getBeginTime()*ele.getTimeStep());
				int end=(int)Math.round((ele.getLength()*ele.getTimeStep())+start);
				
				System.out.println(start+" "+ele.getBeginTime()+" "+end+" "+ele.getLength());
				
				
				int[] syl={start-1, end+1};
				Syllable sy=new Syllable(syl, song.songID);
				sy.addFamily(syls, eleList);
				sy.checkMaxLevel();
				syls.add(sy);	
			}
		
		}
	
		public void updateElements(){
			for (int i=0; i<eleList.size(); i++){
				Element ele=(Element)eleList.get(i);
				ele.begintime-=14;
				for (int j=0; j<ele.signal.length; j++){
					ele.signal[j][0]-=14;
				}
			}	
		}
		
		
		public void setEleGaps(){
			
			for (int i=0; i<eleList.size()-1; i++){
				Element ele1=eleList.get(i);
				Element ele2=eleList.get(i+1);
				ele2.setTimeBefore(ele1.getTimeAfter());	
			}	
		}
		/*
		public boolean checkElements(){
			System.out.println("Checking: "+name);
			for (Element ele : eleList){
				
				int a=ele.checkValues(this);
				
				
			}
			
			
			return true;
		}
		*/
		
		public LinkedList<String[]> checkElements(){
			System.out.println("Checking: "+song.name);
			
			LinkedList<String[]> output=new LinkedList<String[]>();
			
			for (int i=0; i<eleList.size(); i++){
				Element ele=eleList.get(i);
				String[] a=ele.checkValues(this);
				
				if (a!=null){
					String[] x=new String[5];		
					x[0]=song.ind.getName();;
					x[1]=song.name;
					x[2]=(i+1)+"";
					x[3]=a[0];
					x[4]=a[1];
					output.add(x);
				}
				
			}
			
			
			return output;
		}
		
		/*
		public LinkedList<Syllable> getSyllList(int level){
			LinkedList<Syllable> sy=new LinkedList<Syllable>();
			
			for (Syllable syl : sylList){
				if (syl.maxLevel==level){
					sy.add(syl);
				}
			}		
			return sy;
		}
		*/
		
		public Syllable getSyllable(int a){
			return sylList.get(a);
		}
		
		public LinkedList<Syllable> getSyllList(){		
			return sylList;
		}
		
		/*
		public void makeSylList(){
			//System.out.println("MAKING SYLL LIST");
			sylList=new LinkedList<Syllable>();
			for (Element ele: eleList){
				ele.syls=new LinkedList<Syllable>();
			}
			
			for (int i=0; i< syllList.size(); i++){
				int [] syl=syllList.get(i);
				Syllable sy=new Syllable(syl, i, songID);
				sy.addFamily(sylList, eleList);
				sy.checkMaxLevel();
				sylList.add(sy);
				//System.out.println(i+" "+syllList.size()+" "+syl[0]+" "+syl[1]+" "+sy.children.size());
			}
		}
		*/

		public void checkFreqs() {
			
			for (Element ele: eleList) {
				//System.out.println(name+" "+ele.id);
				double p=ele.checkDiff(song.spectrogram.maxf);
				if (p>500) {
					System.out.println("CheckFreq: "+song.name);
				}
			}
			
		}
		
}
