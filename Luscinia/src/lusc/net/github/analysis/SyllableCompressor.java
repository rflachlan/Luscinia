package lusc.net.github.analysis;

import java.util.LinkedList;

import lusc.net.github.Element;
import lusc.net.github.Song;
import lusc.net.github.Syllable;

public class SyllableCompressor {
	
	Song s;
	int[] com;
	double mismatch=1000;
	
	public SyllableCompressor(){}
	
	public void compressSong2(Song s){
		this.s=s;
		LinkedList<Element> eleList=new LinkedList<Element>();
		LinkedList<Syllable> sylList=new LinkedList<Syllable>();

		try{
			int eleCount=0;
			
			LinkedList<Syllable> syls=s.getPhrases();
			
			int m=syls.size();	
			for (int x=0; x<m; x++){
			
				//int[][] ph=(int[][])s.getPhrase(x);
				Syllable phr=syls.get(x);
				
				
				Element ele1, ele2;
				//int a=ph.length;
				//int b=ph[0].length;
				int a=phr.getNumSyllables();
				int b=phr.getMaxSyllLength();
				double[][]mat2=new double[a][a];
				for (int i=0; i<a; i++){
					//Syllable s1=syls.get(i);
					Syllable s1=phr.getSyllable(i);
					for (int j=0; j<a; j++){
						//Syllable s2=syls.get(j);
						Syllable s2=phr.getSyllable(j);
						double sc=0;
						if (s1.getNumEles()!=s2.getNumEles()){
							sc+=mismatch;
						}
						
						for (int k=0; k<b; k++){
							if ((k<s1.getNumEles())&&(k<s2.getNumEles())){
								ele1=s1.getElement(k);
								ele2=s2.getElement(k);
								double q=compare2(ele1, ele2);
								sc+=q;
							}
						}
						
						/*
						
						for (int k=0; k<b; k++){
							if ((ph[i][k]==-1)&&(ph[j][k]>=0)){sc+=mismatch;}
							if ((ph[j][k]==-1)&&(ph[i][k]>=0)){sc+=mismatch;}
							if ((ph[i][k]>=0)&&(ph[j][k]>=0)){
								ele1=(Element)s.getElement(ph[i][k]);
								ele2=(Element)s.getElement(ph[j][k]);
								double q=compare2(ele1, ele2);
								sc+=q;
							}
						}
						*/
						mat2[i][j]=sc;
					}
				}
				
				double bestsc=1000000000;
				int bestph=-1;
				
				int amin=(int)Math.floor(a*0.1);
				int amax=(int)Math.ceil(a*0.9);
				
				for (int i=amin; i<amax; i++){
					double t=0;
					for (int j=amin; j<amax; j++){
						t+=mat2[i][j];
					}
					if (t<bestsc){
						bestsc=t;
						bestph=i;
					}
				}
				
				Syllable sy=phr.getSyllable(bestph);
				
				eleList.addAll(sy.getElements());
				//System.out.println(s.getName()+" "+eleList.size());
				Syllable syn=new Syllable(sy.getElements(), s.getSongID());
				syn.addFamily(sylList, eleList);
				syn.checkMaxLevel();
				sylList.add(syn);
				
				
				/*
				
				
				int len=0;
				for (int i=0; i<b; i++){
					if (ph[bestph][i]>=0){len++;}
				}
				
				int ph2[][]=new int[1][len];
				len=0;
				int[] sy={Integer.MAX_VALUE, Integer.MIN_VALUE};
				for (int k=0; k<b; k++){
					if(ph[bestph][k]>=0){
						Element ele=(Element)s.getElement(ph[bestph][k]);
						eleList.add(ele);
						ph2[0][len]=eleCount;
						eleCount++;
						len++;
						int startE=(int)Math.floor(ele.getBeginTime()*ele.getTimeStep());
						int endE=(int)Math.ceil(startE+ele.getLength()*ele.getTimeStep());
						if (startE<sy[0]){sy[0]=startE;}
						if (endE>sy[1]){sy[1]=endE;}
					}
				}
				sy[0]-=1;
				sy[1]+=1;
				syllList.add(sy);
				phrases.add(ph2);
				*/
			}
		}
		catch(Exception e){e.printStackTrace();}
		s.setSylList(sylList);
		s.setEleList(eleList);
		
		//System.out.println(s.getName()+" "+sylList.size()+" "+eleList.size());
		
		/*
		s.setPhrases(phrases);
		s.setEleList(eleList);
		s.setSyllList(syllList);
		s.makeSylList();
		*/
		
		
	}
	
	
	/*
	public void compressSong3XXX(Song s){
		this.s=s;
		LinkedList<Element> eleList=new LinkedList<Element>();
		LinkedList<int[][]> phrases=new LinkedList<int[][]>();
		try{
			int eleCount=0;
			int m=s.getNumPhrases();	
			for (int x=0; x<m; x++){
			
				int[][] ph=(int[][])s.getPhrase(x);
				Element ele1, ele2;
				int a=ph.length;
				int b=ph[0].length;
				
				int c=a/2;
				
				int d=makeCom(ph);
				while (com[c]!=d){c--;}	
				
				int len=0;
				for (int i=0; i<b; i++){
					if (ph[c][i]>=0){len++;}
				}
				
				int ph2[][]=new int[1][len];
				len=0;
				for (int k=0; k<b; k++){
					if(ph[c][k]>=0){
						Element ele=(Element)s.getElement(ph[c][k]);
						eleList.add(ele);
						ph2[0][len]=eleCount;
						eleCount++;
						len++;
					}
				}
				phrases.add(ph2);
			}
		}
		catch(Exception e){e.printStackTrace();}
		s.setPhrases(phrases);
		s.setEleList(eleList);
	}
	*/
	/*
	public void compressSong(Song s){
		this.s=s;
		LinkedList<Element> eleList=new LinkedList<Element>();
		LinkedList<int[][]> phrases=new LinkedList<int[][]>();
		try{
		
		
		int eleCount=0;
		for (int j=0; j<s.getNumPhrases(); j++){
			int[][] ph=(int[][])s.getPhrase(j);
			
			makeCom(ph);
			
			for (int a=0; a<ph.length; a++){
				for (int b=0; b<ph[a].length; b++){
					//System.out.print(ph[a][b]+" ");
				}
				//System.out.println();
			}

			if (ph.length>1){

				int b=ph.length/2;
				if (com[b]<ph.length){
					b=ph.length-1;
					while (com[b]<ph.length){
						b--;
					}
				}
						
				for (int k=0; k<ph[b].length; k++){
					Element ele=(Element)s.getElement(ph[b][k]);	
					Element ele2=matchElement(ele, ph, k, b);
					eleList.add(ele2);
				}
				int d=ph[b].length;

				int ph2[][]=new int[1][d];
				for (int k=0; k<d; k++){
					ph2[0][k]=eleCount;
					eleCount++;
				}
				if (d>0){
					phrases.add(ph2);
				}
			}
			else{
				int ph2[][]=new int[1][ph[0].length];
				for (int k=0; k<ph[0].length; k++){
					Element ele=(Element)s.getElement(ph[0][k]);
					eleList.add(ele);
					ph2[0][k]=eleCount;
					eleCount++;
				}
				phrases.add(ph2);
			}
				
		}
		}
		catch(Exception e){e.printStackTrace();}
		s.setPhrases(phrases);
		s.setEleList(eleList);
		//System.out.println("SELECTED: "+s.phrases.size()+" "+s.eleList.size());
	}
	*/
	
	public Element matchElement(Element ele, int[][] phrase, int position, int pr){
		int n=phrase.length;
		int m=phrase[0].length;
		
		Element eleR=new Element(ele);
		
		
		int[][] paths=new int[n][];
		Element[] els=new Element[n];
		for (int i=0; i<n; i++){
			if((i!=pr)&&(com[i]==m)){
				els[i]=(Element)s.getElement(phrase[i][position]);
				paths[i]=compare(eleR, els[i]);
				//System.out.println(i+" "+phrase[i][position]+" "+els[i].length+" "+paths[i].length);
			}	
		}
		//System.out.println();
		matchPaths(eleR, paths, els);
		return eleR;
	}
	
	public int[] compare(Element ele, Element ec){
		int n=ele.getLength();
		int m=ec.getLength()-1;
		double[][] mat=new double[n][m];
		double[][] mat2=new double[n][m];
		int ii, jj, j1, j2, loc;
		double x, y1, y2, d1, d2, sc, bs;
		double[][] measurements1=ele.getMeasurements();
		double[][] measurements2=ec.getMeasurements();
		for (int i=0; i<n; i++){
			for (int j=0; j<m; j++){
				ii=i+5;
				j1=j+5;
				j2=j+6;
				x=measurements1[ii][3];
				y1=measurements2[j1][3];
				y2=measurements2[j2][3];
				d1=x-y1;
				d2=x-y2;
				if ((d1>0)&&(d2<0)){mat[i][j]=0;}
				else if ((d1<0)&&(d2>0)){mat[i][j]=0;}
				else{
					d1=0.5*(d1+d2);
					mat[i][j]=Math.sqrt(d1*d1);
				}
				
			}
		}
		int[] p={-1, -1, 0};
		int[] q={0, -1, -1};
		for (int i=0; i<n; i++){
			for (int j=0; j<m; j++){
				mat2[i][j]=mat[i][j];
				bs=1000000;
				loc=-1;
				for (int k=0; k<3; k++){
					ii=i+p[k];
					jj=j+q[k];
					if ((ii>=0)&&(ii<n)&&(jj>=0)&&(jj<m)){
						sc=mat2[ii][jj];
						if (sc<bs){
							bs=sc;
							loc=k;
						}
					}
				}
				if (loc>=0){
					ii=i+p[loc];
					jj=j+q[loc];
					mat2[i][j]+=mat2[ii][jj];
				}
			}
		}
		double[] best=new double[m];
		for (int i=0; i<m; i++){best[i]=1000000000;}
		int[] path=new int[m];
		int i=n-1;
		int j=m-1;
		best[j]=mat2[i][j];
		path[j]=i;
		while ((i>0)||(j>0)){
			bs=1000000;
			loc=-1;
			for (int k=0; k<3; k++){
				ii=i+p[k];
				jj=j+q[k];
				if ((ii>=0)&&(ii<n)&&(jj>=0)&&(jj<m)){
					sc=mat2[ii][jj];
					if (sc<bs){
						bs=sc;
						loc=k;
					}
				}
			}
			i+=p[loc];
			j+=q[loc];
			if (bs<best[j]){
				best[j]=bs;
				path[j]=i;
			}	
		}
		
		return path;
	}
	
	public double compare2(Element ele, Element ec){
		int n=ele.getLength();
		int m=ec.getLength()-1;
		double[][] mat=new double[n][m];
		double[][] mat2=new double[n][m];
		int ii, jj, j1, j2, loc;
		double x, y1, y2, d1, d2, sc, bs;
		double[][] measurements1=ele.getMeasurements();
		double[][] measurements2=ec.getMeasurements();
		for (int i=0; i<n; i++){
			for (int j=0; j<m; j++){
				ii=i+5;
				j1=j+5;
				j2=j+6;
				x=measurements1[ii][3];
				y1=measurements2[j1][3];
				y2=measurements2[j2][3];
				d1=x-y1;
				d2=x-y2;
				if ((d1>0)&&(d2<0)){mat[i][j]=0;}
				else if ((d1<0)&&(d2>0)){mat[i][j]=0;}
				else{
					d1=0.5*(d1+d2);
					mat[i][j]=Math.sqrt(d1*d1);
				}
				
			}
		}
		int[] p={-1, -1, 0};
		int[] q={0, -1, -1};
		for (int i=0; i<n; i++){
			for (int j=0; j<m; j++){
				mat2[i][j]=mat[i][j];
				bs=1000000;
				loc=-1;
				for (int k=0; k<3; k++){
					ii=i+p[k];
					jj=j+q[k];
					if ((ii>=0)&&(ii<n)&&(jj>=0)&&(jj<m)){
						sc=mat2[ii][jj];
						if (sc<bs){
							bs=sc;
							loc=k;
						}
					}
				}
				if (loc>=0){
					ii=i+p[loc];
					jj=j+q[loc];
					mat2[i][j]+=mat2[ii][jj];
				}
			}
		}
		double l=1/(1.0*Math.max(n, m));
		return mat2[n-1][m-1]*l;
	}
	
	public void matchPaths(Element ele, int[][] paths, Element[] els){
		int n=paths.length; 
		int a=ele.getLength();
		double[][] emeasurements=ele.getMeasurements();
		int b=emeasurements[0].length;
		
		double[][] measurements=new double[a][b];
		double[] count=new double[a];
		
		for (int i=0; i<a; i++){
			int ii=i+5;
			for (int j=0; j<b; j++){
				measurements[i][j]=emeasurements[ii][j];
			}
			count[i]++;
		}
		
		for (int i=0; i<n; i++){
			double[][] imeasurements=els[i].getMeasurements();
			if (paths[i]!=null){
				//System.out.println(paths[i].length);
				for (int j=0; j<a; j++){
					int[] po=getMatch(paths[i], j);
					
					if(po[0]>=0){
						for (int k=0; k<b; k++){
							measurements[j][k]+=getMeasurement(emeasurements, imeasurements, j, k, po);
						}
						count[j]++;
					}
				}
			}
		}
		
		for (int i=0; i<a; i++){
			for (int j=0; j<b; j++){
				measurements[i][j]/=count[i];
			}
		}
		ele.setMeasurements(measurements);
		ele.calculateStatistics();
	}
	
	public double getMeasurement(double[][] m1, double[][] m2, int a, int b, int[] out){
		
		int ii=a+5;
		double result=0;
		if (out[1]==-1){
			int j1=out[0]+5;
			int j2=j1+1;
			double x=m1[ii][b];
			double y1=m2[j1][b];
			double y2=m2[j2][b];
			double d1=x-y1;
			double d2=x-y2;
			double sc=0;
			if ((d1>0)&&(d2<0)){sc=0;}
			else if ((d1<0)&&(d2>0)){sc=0;}
			else{
				sc=0.5*(d1+d2);
			}
			result=x+sc;	
		}
		else{
			
			int j1=out[0]+6;
			int j2=out[1]+5;
			if(j2>=m2.length){
				//System.out.println(s.name+" "+m1.length+" "+m2.length+" "+a+" "+b+" "+out[0]+" "+out[1]);
			}
			double x=m1[ii][b];
			double y1=m2[j1][b];
			double y2=m2[j2][b];
			double d1=x-y1;
			double d2=x-y2;
			double sc=0;
			if ((d1>0)&&(d2<0)){sc=0;}
			else if ((d1<0)&&(d2>0)){sc=0;}
			else{
				sc=0.5*(d1+d2);
			}
			result=x+sc;	
		}
		return result;
	}
	
	public int[] getMatch(int[] paths, int a){
		
		int[] out={-1, -1};
		
		for (int i=0; i<paths.length; i++){
			if (paths[i]==a){
				out[0]=i;
				out[1]=-1;
			}
		}
		//System.out.println(out[0]+" "+out[1]);
		if (out[0]==-1){
			for (int i=0; i<paths.length-1; i++){
				if ((paths[i]<a)&&(paths[i+1]>a)){
					out[0]=i;
					out[1]=i+1;
					//System.out.println(out[0]+" "+out[1]+" "+paths[i]+" "+paths[i+1]);
				}
			}
		}
		return out;
	}
	
	public int makeCom(int[][]ph){
		int n=ph.length;
		com=new int[n];
		
		int[] counter=new int[ph[0].length+1];
		
		for (int i=0; i<n; i++){
			com[i]=0;
			int a=0;
			for (int j=0; j<ph[i].length; j++){
				if (ph[i][j]>=0){a++;}
			}
			com[i]=a;
			counter[a]++;
			//System.out.print(com[i]+" ");
		}
		int max=0;
		int loc=0;
		for (int i=0; i<counter.length; i++){
			if (counter[i]>max){
				max=counter[i];
				loc=i;
			}
		}
		return loc;
		//System.out.println();
	}
	

}
