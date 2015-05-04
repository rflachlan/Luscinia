package lusc.net.github.analysis.dendrograms;


public class UPGMA {
	TreeDat [] dat;
	
	int mode=1;		//mode==0 means UPGMA; mode==1 means Ward's method; mode==2 means flexible beta=-0.25
	double alphap=0;
	double alphaq=0;
	double beta=0;
	double gamma=0;
	int le;
	double maxDist;
	double[][] input;
	double betaOpt=-0.25;
	
	public UPGMA (double[][] input, int mode, double betaOpt){
		this.input=input;
		this.mode=mode;
		this.betaOpt=betaOpt;
		
		double E=0;
		le=input.length;
		int numNodes=le*2-1;
		dat=new TreeDat[numNodes];
		boolean active[]=new boolean[le];
		double[][] scores=new double[le][];
		double[] count=new double[le];
		int index[]=new int[le];
		for (int i=0; i<le; i++){
			active[i]=true;
			index[i]=i;
			int[] child3={i};
			dat[i] =new TreeDat(0, child3);
			count[i]=1;
		}

		for (int i=0; i<le; i++){
			scores[i]=new double[i+1];
			for (int j=0; j<i; j++){
				if (mode==1){
					scores[i][j]=(input[i][j]*input[i][j]);
				}
				else{
					scores[i][j]=input[i][j];
				}
			}
		}
		
		int g,h,i,j, loca, locb;
		double min=0;
		double score;
		
		for (g=le; g<numNodes; g++){
			min=10000000f;
			loca=0;
			locb=0;
			for (h=0; h<le; h++){
				if (active[h]){
					
					for (i=0; i<h; i++){
						if (active[i]){
							if (scores[h][i]<min){
								min=scores[h][i];
								loca=h;
								locb=i;
							}
						}
					}
				}
			}
			//E+=0.5*min;
			E=min;
			dat[g]=new TreeDat(E, dat[index[loca]].child, dat[index[locb]].child, index[loca], index[locb]);
			index[loca]=g;
			active[locb]=false;
			double newcount=count[loca]+count[locb];
			if (mode==0){
				alphap=(count[loca])/newcount;
				alphaq=(count[locb])/newcount;
				beta=0;
				gamma=0;
			}
			if (mode==2){
				beta=betaOpt;
				alphap=(1-beta)*0.5;
				alphaq=alphap;
				gamma=0;
			}
			if (mode==3){
				beta=0;
				alphap=0.5;
				alphaq=0.5;
				gamma=0.5;
			}
			if (mode==4){
				beta=0;
				alphap=0.5;
				alphaq=0.5;
				gamma=-0.5;
			}
			for (h=0; h<le; h++){
				if ((active[h])&&(h!=loca)){
					
					if (mode==1){
						double tt=count[h]+newcount;
						alphap=(count[loca]+count[h])/tt;
						alphaq=(count[locb]+count[h])/tt;
						beta=-1*count[h]/tt;
						gamma=0;
					}
					
					score=0;
					
					double score1=0;
					double score2=0;
					
					if (h<loca){
						score1=scores[loca][h];
					}
					else{
						score1=scores[h][loca];
					}
					if (h<locb){
						score2=scores[locb][h];
					}
					else{
						score2=scores[h][locb];
					}
					score=alphap*score1+alphaq*score2+beta*min;
					if (gamma>0){
						score+=gamma*Math.abs(score1-score2);
					}
					
					if (h<loca){
						scores[loca][h]=(score);
					}
					else{
						scores[h][loca]=(score);
					}					
				}
			}
		}
		
		maxDist=dat[numNodes-1].dist;
		if (mode==1){
			maxDist=Math.sqrt(maxDist);
		}
		for (i=0; i<numNodes; i++){
			if (mode==1){
				dat[i].dist=Math.sqrt(dat[i].dist);
				
			}
			//System.out.println("UPGMA CHECK: "+i+" "+dat[i].dist);
			dat[i].dist/=maxDist;
			if (dat[i].daughters[0]>-1){
				for (j=0; j<2; j++){
					dat[dat[i].daughters[j]].parent=i;
				}
			}
		}
		scores=null;
		active=null;
	}
	
	public TreeDat[] getDat(){
		return dat;
	}
	
	public double getMaxDist(){
		return maxDist;
	}
	
	public int getLength(){
		return le;
	}
	
	public double[][] getInput(){
		return input;
	}
	
	public int[][] getFirstGroups(){
		int f=dat.length-1;
		
		int[] p={dat[f].daughters[0], dat[f].daughters[1], dat[dat[f].daughters[0]].daughters[0], dat[dat[f].daughters[0]].daughters[1], dat[dat[f].daughters[1]].daughters[0], dat[dat[f].daughters[1]].daughters[1],};
		
		int[][] results=new int[6][];
		
		for (int i=0; i<6; i++){
			results[i]=new int[dat[p[i]].child.length];
			for (int j=0; j<results[i].length; j++){
				results[i][j]=dat[p[i]].child[j];
			}
		}
		return results;	
	}
	
	public int[][] getFirstGroups2(int num){
				
		int[][] results=new int[num][];
		int n=dat.length-1;
		for (int i=0; i<num; i++){
			results[i]=new int[dat[n-i].child.length];
			for (int j=0; j<results[i].length; j++){
				results[i][j]=dat[n-i].child[j];
			}
		}
		return results;	
	}
	
	public int[][] getPartitionMembers(){
		int n=dat.length;
		
		int[][] results=new int[n][];
		
		
		for (int i=0; i<n; i++){
			double thresh=dat[i].dist;
			if (thresh>0){
				int count=0;
				for (int j=0; j<i; j++){
					int k=dat[j].parent;
					if ((dat[j].dist<thresh)&&(dat[k].dist>thresh)){
						count++;
					}
				}
				results[i]=new int[count+1];
				count=1;
				results[i][0]=i;
				for (int j=0; j<i; j++){
					int k=dat[j].parent;
					if ((dat[j].dist<thresh)&&(dat[k].dist>thresh)){
						results[i][count]=j;
						//System.out.print(j+" ");
						count++;
					}
				}
				//System.out.println(i+" "+thresh+" "+n+" "+count);
			}
		}
		return results;
	}
	
	public int[][] calculateClassificationMembers(int q){
		int[][] partitions=getPartitionMembers();
		
		if (q>partitions.length/2){q=partitions.length/2;}

		int[][] cats=new int[le][q];

		for (int i=1; i<q; i++){
			int ii=partitions.length-i-1;
			if (partitions[ii]!=null){
				for (int j=0; j<partitions[ii].length; j++){
					for (int k=0; k<dat[partitions[ii][j]].child.length; k++){
						cats[dat[partitions[ii][j]].child[k]][i]=j;
					}
				}
			}
		}
		return cats;
	}
		
	public double[][] calculateMeanClusterDistances(int q){
		int[][] cats=calculateClassificationMembers(q);
		q=cats[0].length;
		double[][] r=new double[le][q];
		for (int i=0; i<q; i++){
			for (int j=0; j<le; j++){
				double s=0;
				double c=0;
				
				for (int k=0; k<le; k++){
					if (cats[j][i]==cats[k][i]){
						c++;
						if (j>k){
							s+=input[j][k];
						}
						else{
							s+=input[k][j];
						}
					}
				}
				r[j][i]=s/c;
			}
		}
		
		return r;
		
	}
	
}
