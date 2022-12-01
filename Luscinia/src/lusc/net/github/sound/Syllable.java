package lusc.net.github.sound;

import java.util.LinkedList;

public class Syllable {
	
	LinkedList<Element> eles=new LinkedList<Element>();
	LinkedList<Element> eles2;
	LinkedList<Syllable> children=new LinkedList<Syllable>();
	LinkedList<Syllable> parents=new LinkedList<Syllable>();
	
	int maxLevel=0;
	int maxSyllLength;
	int start=0;
	int end=0;
	
	int id=0;
	int songid=0;
	int analysisid=0;
	
	int offset=0;
	
	public Syllable(int[] s, int id, int songid){
		start=s[0];
		end=s[1];
		this.id=id;
		this.songid=songid;
	}
	
	public Syllable(int[] s,int songid){
		start=s[0];
		end=s[1];
		this.songid=songid;
	}
	
	public Syllable(LinkedList<Element> ele, int songid){
		start=Integer.MAX_VALUE;
		end=-1;
		for (Element el: ele){
			int starta=(int)Math.floor(el.getBeginTime()*el.getTimeStep());
			int enda=(int)Math.ceil(starta+el.getLength()*el.getTimeStep());
			if (starta<start){start=starta;}
			if (enda>end){end=enda;}
		}
		eles=ele;
		this.songid=songid;
	}
	
	public void checkMaxLevel(){
		int max=0;
		for (Syllable c: children){
			if (c.maxLevel>max){max=c.maxLevel;}
		}
		maxLevel=max+1;
		
		for (Syllable p: parents){
			p.checkMaxLevel();
		}	
	}
	
	public void addFamily(LinkedList<Syllable> syls, LinkedList<Element> eleList){	
		
		int maxstart=-1;
		int loc=-1;
		int maxSyllLength=0;
		for (Syllable s : syls){
			if ((this.start>=s.start)&&(this.end<=s.end)){
				parents.add(s);
				if (eles.size()>maxSyllLength){
					maxSyllLength=eles.size();
				}
				s.children.add(this);
			}
			if ((this.start<=s.start)&&(this.end>=s.end)){
				children.add(s);
				if (s.eles.size()>maxSyllLength){
					maxSyllLength=s.eles.size();
				}
				s.parents.add(this);
			}
			if (this.start<=s.start){
				s.id++;
			}
			if ((this.start>s.start)&&(s.start>maxstart)){
				maxstart=s.start;
				loc=s.id;
			}
		}
		id=loc+1;
		
		for (Element ele : eleList){
			double startE=ele.getBeginTime()*ele.getTimeStep();
			double endE=startE+ele.getLength()*ele.getTimeStep();
			//System.out.println(startE+" "+endE+" "+start+" "+end);
			if ((start<=startE)&&(end>=endE)){
				eles.add(ele);
				ele.syls.add(this);
				if (children.size()==0) {ele.syl=this;}				
				//System.out.println(start+" "+end+" "+startE+" "+endE+" "+ele.getTimeStep());
				//System.out.println(ele.syls.size());
				//for (Syllable sy: ele.syls){
					//System.out.println(sy.start+" "+sy.end);
				//}
			}
		}
	}
	
	
	public void remove(){
		
		for (Syllable p : parents){
			p.children.remove(this);
		}
		
		for (Syllable c : children){
			c.parents.remove(this);
		}
		
		for (Syllable p : parents){
			p.checkMaxLevel();
		}
		
		for (Syllable c : children){
			c.checkMaxLevel();
		}
		
		for (Element e : eles){
			e.syls.remove(this);
		}

	}
	
	
	public void mergeElements(double threshold){
		eles2=new LinkedList<Element>();
		
		int[] x=new int[eles.size()];
		for (int i=0; i<x.length-1; i++){
			Element ele1=eles.get(i);
			Element ele2=eles.get(i+1);
			if (ele2.getBeginTime()-ele1.getEndTime()<threshold/ele1.getTimeStep()){
				x[i+1]=-1;
				int p=i;
				while (x[p]==-1){
					p--;
				}
				x[p]=i+1;
			}
		}
		for (int i=0; i<x.length; i++){
			if (x[i]==0){
				eles2.add(eles.get(i));
			}
			else if (x[i]>0){
				Element[] marray=new Element[x[i]-i+1];
				for (int j=i; j<=x[i]; j++){
					marray[j-i]=eles.get(j);
					if (marray[j-i]==null){
						System.out.println("ALERT! "+i+" "+j);
					}
				}
				
				Element ele=new Element(marray);
				ele.syl=this;
				eles2.add(ele);
			}
		}
		//System.out.println(eles.size()+" "+eles2.size());
	}
	
	public int[] getEleIds(){
		int[] out=new int[eles.size()];
		int i=0;
		for (Element ele: eles){
			out[i]=ele.id;
			i++;
		}
		return out;
	}
	
	public int getEleId(int x){
		Element ele=eles.get(x);
		return ele.id;
	}
	
	public int[] getEleIds2(){
		int[] out=new int[eles2.size()];
		int i=0;
		for (Element ele: eles2){
			out[i]=ele.id;
			i++;
		}
		//System.out.println(eles2.size());
		return out;
	}
	
	public int getEleId2(int x){
		Element ele=eles2.get(x);
		return ele.id;
	}
	
	public int[] getLoc(){
		int[] c={start, end};
		return c;
	}
	
	public int getNumEles(){
		return eles.size();
	}
	
	public int getNumEles2(){
		return eles2.size();
	}
	
	public int getMaxSyllLength(){
		return maxSyllLength;
	}
	
	public LinkedList<Syllable> getChildrenX(){
		return children;
	}
	
	public Syllable getChildX(int i){
		return children.get(i);
	}
	
	public int getID() {
		return id;
	}
	
	public LinkedList<Syllable> getSyllables(){
		if (children.size()>0) {return children;}
		LinkedList<Syllable>out=new LinkedList<Syllable>();
		out.add(this);
		return out;
	}
	
	public Syllable getSyllable(int i){
		if (children.size()>0) {return children.get(i);}
		return this;
	}
	
	public Syllable getLastCompleteChild(){
		Syllable s=null;
		for (int i=children.size()-1; i>=0; i--){
			s=children.get(i);
			if (s.getNumEles()==maxSyllLength){
				i=-1;
			}
		}
		return s;
	}
	
	public LinkedList<Element> getElements(){
		return eles;
	}
	
	public LinkedList<Element> getElements2(){
		return eles2;
	}
	
	public Element getElement(int i){
		return eles.get(i);
	}
	
	public Element getElement2(int i){
		return eles2.get(i);
	}
	
	public int getNumChildren(){
		return children.size();
	}
	
	public int getNumSyllables(){
		return Math.max(children.size(), 1);
	}
	
	public int getNumParents(){
		return parents.size();
	}
	
	public int getLengthElements(boolean withTimeAfter){
		int startq=Integer.MAX_VALUE;
		int endq=-1;
		int ta=0;
		for (Element el: eles){
			int starta=el.getBeginTime();
			int enda=starta+el.getLength();
			if (starta<startq){startq=starta;}
			if (enda>endq){
				endq=enda;
				if (el.getTimeAfter()==-10000){
					ta=0;
				}
				else{
					ta=(int)Math.round(el.getTimeAfter()/el.getTimeStep());
				}
			}
		}
		int d=endq-startq;
		if (withTimeAfter){d+=ta;}
		return d;
	}
	
	public void setOffset(int o){
		offset=o;
	}
	
	public int getOffset(){
		return offset;
	}
	
	public void calculateOffsets(){
		for (Syllable s: children){
			if (s.eles.size()<maxSyllLength){
				measureOffset(s);
			}
		}
	}
	
	void measureOffset(Syllable syl){
		int p=0;
		for (Syllable s: children){
			p+=calculateBestOffset(syl, s);
		}
		syl.offset=(int)Math.round(p/(children.size()+0.0));
	}
	
	int calculateBestOffset(Syllable s, Syllable t){
		
		int ns=s.eles.size();
		int nt=t.eles.size();		
		double[][]d=new double[ns][nt];
		
		for (int i=0; i<ns; i++){
			Element es=s.getElement(i);
			for (int j=0; j<nt; j++){
				Element et=t.getElement(j);
				double a=es.getLength()-et.getLength();
				d[i][j]=Math.sqrt(a*a);
			}
		}
		
		double bestsc=Double.MAX_VALUE;
		int loc=0;
		for (int i=0; i<nt-ns; i++){
			double score=0;
			for (int j=0; j<ns; j++){
				score+=d[j][i+j];
			}
			if (score<bestsc){
				bestsc=score;
				loc=i;
			}	
		}
		return loc;	
	}
	
	public double[] calculateElementMeasurementAverages(boolean logFrequencies) {
		
		
		double[] averages=null;
		double totlength=0;
		
		
		double t[]=new double[4];
		double u=0;
		int a=11;
		for (Element ele : eles) {
			double[][] m=ele.getMeasurements();
			int n1=m.length;			
			double[] out=new double[4];
			for (int j=5; j<n1; j++){
				double c=m[j][a];
				if (c<1) {c=1;}
				//double c=Math.exp(m[j][a]);
				//u++;
				u+=c;
				
				for (int i=0; i<4; i++){

					if (logFrequencies){
						t[i]+=Math.log(m[j][i])*c;
						//t[i]+=Math.log(m[j][i]);
					}
					else{
						//t+=m[j][i]*m[j][a];
						//t+=m[j][i];
						t[i]+=m[j][i]*c;
					}
				
				}
				
			}
			
			
		}
		
		
		averages=new double[4];
		for (int i=0; i<averages.length; i++) {
			averages[i]=t[i]/u;
			//System.out.print(Math.exp(averages[i])+" ");
		}
		//System.out.println();
			
		/*	
			//double[] avele=ele.getAverages(measu, logFrequencies);	
			int s=ele.getLength();
			
			if (averages==null) {
				averages=new double[out.length];				
			}
			for (int i=0; i<out.length; i++) {
				averages[i]+=out[i]*s;
			}
			totlength+=s;
			
			System.out.println(songid+" "+averages[0]+" "+averages[3]+" "+s);
			
		}
		
		for (int i=0; i<averages.length; i++) {
			averages[i]/=totlength;
		}
		*/
		
		return averages;
		
	}
	
	public boolean checkInRange(int x) {
		
		if ((x>=start)&&(x<=end)) {
			return true;
		}
		return false;
		
	}
	
	
}
