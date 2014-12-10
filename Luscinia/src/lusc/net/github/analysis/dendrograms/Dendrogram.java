package lusc.net.github.analysis.dendrograms;


public class Dendrogram {

	TreeDat [] dat;
	//double[] silhouette, dunn1, dunn2 ,knearest;
	
	int mode=1;		//mode==0 means UPGMA; mode==1 means Ward's method
	double alphap=0;
	double alphaq=0;
	double beta=0;
	double gamma=0;
	int le;

	public Dendrogram (float[][] input, int mode){
		
		double E=0;
		le=input.length;
		int numNodes=le*2-1;
		dat=new TreeDat[numNodes];
		boolean active[]=new boolean[le];
		float[][] scores=new float[le][];
		float[] count=new float[le];
		int index[]=new int[le];
		for (int i=0; i<le; i++){
			active[i]=true;
			index[i]=i;
			int[] child3={i};
			dat[i] =new TreeDat(0, child3);
			count[i]=1;
		}

		for (int i=0; i<le; i++){
			scores[i]=new float[i+1];
			for (int j=0; j<i; j++){
				scores[i][j]=(float)(input[i][j]*input[i][j]);
			}
		}
		
		int g,h,i,j, loca, locb;
		float min=0;
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
			E+=0.5*min;
			//E=min;
			dat[g]=new TreeDat(E, dat[index[loca]].child, dat[index[locb]].child, index[loca], index[locb]);
			index[loca]=g;
			active[locb]=false;
			float newcount=count[loca]+count[locb];
			for (h=0; h<le; h++){
				if ((active[h])&&(h!=loca)){
					
					
					if (mode==0){
						alphap=(count[loca])/newcount;
						alphaq=(count[locb])/newcount;
						beta=0;
						gamma=0;
					}
					if (mode==1){
						double tt=count[h]+newcount;
						alphap=(count[loca]+count[h])/tt;
						alphaq=(count[locb]+count[h])/tt;
						beta=-1*count[h]/tt;
						gamma=0;
					}
					if (mode==2){
						beta=-0.25;
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
						scores[loca][h]=(float)(score);
					}
					else{
						scores[h][loca]=(float)(score);
					}					
				}
			}
		}
		
		double maxDist=dat[numNodes-1].dist;
		for (i=0; i<numNodes; i++){
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
	
	public void calcAverages(double[] labels){
		for (int i=0; i<dat.length; i++){
			dat[i].colval=dat[i].calculateClusterAverage(labels);
		}
	}
	
	public TreeDat[] getDat(){
		return dat;
	}

	
}
