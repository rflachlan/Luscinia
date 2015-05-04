package lusc.net.github.analysis.clustering;

import java.util.*;

import lusc.net.github.analysis.AnalysisGroup;
import lusc.net.github.analysis.dendrograms.TreeDat;
import lusc.net.github.analysis.dendrograms.UPGMA;


/**
 * This class carries out various external cluster validate metrics, including the Silhouette Index.
 * Takes as input data a hierarchically-clustered data-set.
 * @author Rob
 *
 */

public class ClusterValidation {

	//UPGMA upgma;
	int dmode=1;
	double beta=-0.25;
	double[][] pcRep;
	Random random=new Random(System.currentTimeMillis());
	
	
	/**
	 * 
	 * @param upgma
	 * @param pcRep
	 * @param matrix
	 * @param dmode
	 */
	
	public ClusterValidation(double[][] pcRep, int dmode, double beta){
		this.pcRep=pcRep;
		this.dmode=dmode;
		this.beta=beta;
	}
	
	
	/**
	 * Constructor takes a dendrogramMode parameter. Inelegant...
	 * @param dmode
	 */
	public ClusterValidation(int dmode, double beta){
		this.dmode=dmode;
		this.beta=beta;
	}
	
	/**
	 * Constructs a randomly simulated matrix. Deprecated
	 * @param matrix - dissimilarity matrix to be simulated
	 * @param p - number of dimensions for simulated data to be calculated.
	 */
	public void randomizeMatrix(double[][] matrix, int p){
	
		int n=matrix.length;
		double[][] pcrand=new double[n][p];
		
		for (int i=0; i<n; i++){
			for (int j=0; j<4; j++){
				pcrand[i][j]=random.nextDouble();
			}
		}
		
		for (int i=0; i<n; i++){
			for (int j=0; j<i; j++){
				double score=0;
				for (int k=0; k<p; k++){
					score+=(pcrand[i][k]-pcrand[j][k])*(pcrand[i][k]-pcrand[j][k]);
				}
				matrix[i][j]=Math.sqrt(score);
			}
		}
		
		pcrand=null;
	}
	
	
	/**
	 * Calculate Silhouette Index with an approximate p-value.
	 * See Rousseeuw 1987
	 * @param upgma Input dendrogram
	 * @param matrix Dissimilarity matrix (that underlies dendrogram)
	 * @param sds standard deviations of dimensions associated with the dendrogram (e.g. of MDS dimensions)
	 * @return a 2-D double[][] array containing silhouette indices in the first column, and p-values in the second
	 */
	
	public double[][] silhouettePValue(UPGMA upgma, double[][] matrix, double[] sds){
		
		int repeat=25;
		
		int p=sds.length;
		//double[] realResults=calculateValidityPerCluster(upgma, matrix);

		double[] realResults=calculateValidityPerCluster2(upgma, matrix);
		realResults=getAverageClusterV2(realResults, true, upgma);

		UPGMA tempUPGMA;
		TreeDat[] dat=upgma.getDat();
		double[] simTotals=new double[dat.length];
		double[] pcounter=new double[dat.length];
		double simResults[];
		double[][] results=new double[2][dat.length];
		
		double[][] mat=new double[matrix.length][p];
		double[][] dmat=new double[matrix.length][];
		for (int i=0; i<matrix.length; i++){
			dmat[i]=new double[i+1];
		}
		for (int a=0; a<repeat; a++){
		
			for (int i=0; i<matrix.length; i++){
				for (int j=0; j<p; j++){
					mat[i][j]=(random.nextDouble()*sds[j]);
				}
			}
			for (int i=0; i<matrix.length; i++){
				for (int j=0; j<i; j++){
					double tot=0;
					for (int k=0; k<p; k++){
						tot+=(mat[i][k]-mat[j][k])*(mat[i][k]-mat[j][k]);
					}
					dmat[i][j]=Math.sqrt(tot);
				}
			}		
			
			tempUPGMA=new UPGMA(dmat, dmode, beta);
				
			//simResults=calculateValidityPerCluster(tempUPGMA, dmat);
			simResults=calculateValidityPerCluster2(tempUPGMA, dmat);
			simResults=getAverageClusterV2(simResults, true, tempUPGMA);
			for (int i=0; i<simResults.length; i++){
				simTotals[i]+=simResults[i];
				if (simResults[i]>realResults[i]){
					pcounter[i]++;
				}
			}
		}
		for (int i=0; i<realResults.length; i++){
			simTotals[i]/=repeat+0.0;
			pcounter[i]/=repeat+0.0;
		}
		
		for (int i=0; i<realResults.length; i++){
			results[0][i]=(realResults[i]-simTotals[i])/(1-simTotals[i]);
			results[1][i]=pcounter[i];
		}
		
		return results;
	}
	
	/**
	 * Deprecated
	 * @param upgma
	 * @param matrix
	 * @return
	 */
	
	public double[] calculateValidityPerClusterX(UPGMA upgma, double[][] matrix){
		TreeDat[] td=upgma.getDat();
		int n=td.length;				
		double[] sil=new double[n];
		int p, q, pc, pc2, qc;
		double avbetweenscore, avwithinscore, avoverallscore;
		double score=0;
		for (int i=n-2; i>=0; i--){
			if (td[i].child.length>1){
				p=td[i].parent;
				q=td[p].daughters[0];
				if (q==i){
					q=td[p].daughters[1];
				}
				avoverallscore=0;
				for (int j=0; j<td[i].child.length; j++){
					avbetweenscore=0;
					avwithinscore=0;
					pc=td[i].child[j];
					for (int k=0; k<td[i].child.length; k++){
						pc2=td[i].child[k];
						if (pc2>pc){
							score=matrix[pc2][pc];
						}
						else{
							score=matrix[pc][pc2];
						}
				
						avwithinscore+=score;
					}
					
					for (int k=0; k<td[q].child.length; k++){
				
						qc=td[q].child[k];
						if (qc>pc){
							score=matrix[qc][pc];
						}
						else{
							score=matrix[pc][qc];
						}
				
						avbetweenscore+=score;
					}
					avwithinscore/=td[i].child.length-1.0;
					avbetweenscore/=td[q].child.length+0.0;
					avoverallscore+=(avbetweenscore-avwithinscore)/Math.max(avbetweenscore, avwithinscore);
				}				
				sil[i]=avoverallscore/(td[i].child.length+0.0);
			}
		}
		
		return sil;
	}
	
	/**
	 * Calculates global Silhouette index at each slice of a dendrogram
	 * @param upgma input dendrogram
	 * @param matrix input dissimilarity matri
	 * @return double[] with Silhouette indices
	 */
	
	public double[] calculateValidityPerCluster2(UPGMA upgma, double[][] matrix){
		TreeDat[] td=upgma.getDat();
		int[][] partitions=getPartitionMembers(td);
		int n=td.length;				
		double[] sil=new double[n];
		int q, pc, pc2, qc;
		double avbetweenscore, avwithinscore, avoverallscore;
		double score=0;
		for (int i=0; i<n-1; i++){
			if (td[i].child.length>1){
				avoverallscore=0;
				for (int j=0; j<td[i].child.length; j++){
					avwithinscore=0;
					pc=td[i].child[j];
					for (int k=0; k<td[i].child.length; k++){
						pc2=td[i].child[k];
						if (pc2>pc){
							score=matrix[pc2][pc];
						}
						else{
							score=matrix[pc][pc2];
						}
				
						avwithinscore+=score;
					}
					avwithinscore/=td[i].child.length-1.0;
					avbetweenscore=1000000;
					for (int ii=0; ii<partitions[i].length; ii++){
						if (partitions[i][ii]!=i){
							q=partitions[i][ii];
							double betweens=0;
							for (int k=0; k<td[q].child.length; k++){
					
								qc=td[q].child[k];
								if (qc>pc){
									score=matrix[qc][pc];
								}
								else{
									score=matrix[pc][qc];
								}
				
								betweens+=score;
							}
							betweens/=td[q].child.length+0.0;
							if (betweens<avbetweenscore){
								avbetweenscore=betweens;
							}
						}
					}
					avoverallscore+=(avbetweenscore-avwithinscore)/Math.max(avbetweenscore, avwithinscore);
				}				
				sil[i]=avoverallscore/(td[i].child.length+0.0);
			}
			//else{sil[i]=1;}
		}
		
		return sil;
	}	

	/**
	 * Calculates the mean overall within cluster distance for each slice of a dendrogram
	 * @param upgma input dendrogram
	 * @param matrix input dissimilarity matrix
	 * @return output array containing mean within-cluster distances
	 */
	
	public double[] calculateWithinClusterDistance(UPGMA upgma, double[][] matrix){
		TreeDat[] td=upgma.getDat();
		int n=td.length;				
		double[] sil=new double[n];
		//double silref=0;
		int pc, pc2;
		double avwithinscore;
		double score=0;
		for (int i=n-1; i>=0; i--){
			avwithinscore=0;
			for (int j=0; j<td[i].child.length; j++){
				pc=td[i].child[j];
				for (int k=0; k<td[i].child.length; k++){
					pc2=td[i].child[k];
					if (pc2>pc){
						score=matrix[pc2][pc];
					}
					else{
						score=matrix[pc][pc2];
					}
			
					avwithinscore+=score;
				}
			
			}
			int t=td[i].child.length-1;
			if (t==0){t=1;}
			avwithinscore/=td[i].child.length*t*1.0;
			//avwithinscore/=t*1.0;
			sil[i]=avwithinscore;
			//if (i==n-1){
			//	silref=sil[i];
			//}
			//sil[i]/=silref;
		}
		return sil;
	}	
	
	
	/**
	 * Calculates the Levine & Domany (2007) resampling index for cluster validation
	 * @param sds an array of standard deviations for the dimensions that went into calculating the dissimilarity matrix
	 * @param upgma input dendrogram
	 * @param matrix dissimilarity matrix
	 * @return output 
	 */
	public double[] levineDomanyPValue2(double[] sds, UPGMA upgma, double[][] matrix){
		
		int repeat=5;
		
		int p=2;
		double[] realResults=resamplingMethod(500, upgma, null);
		realResults=getAverageClusterV(realResults, false, upgma);
		
		double simResults[];
		
		double[][] mat=new double[matrix.length][p];
		double[][] dmat=new double[matrix.length][];
		for (int i=0; i<matrix.length; i++){
			dmat[i]=new double[i+1];
		}
	
		for (int i=0; i<matrix.length; i++){
			for (int j=0; j<p; j++){
				mat[i][j]=(random.nextDouble());
			}
		}
		for (int i=0; i<matrix.length; i++){
			for (int j=0; j<i; j++){
				double tot=0;
				for (int k=0; k<p; k++){
					tot+=(mat[i][k]-mat[j][k])*(mat[i][k]-mat[j][k]);
				}
				dmat[i][j]=Math.sqrt(tot);
			}
		}		
		upgma=new UPGMA(dmat, dmode, beta);
		simResults=resamplingMethod(repeat, upgma, null);
		//upgma=tempUPGMA;
		return simResults;
	}
	
	
	/**
	 * Deprecated Levine Domany algorithm
	 * @param upgma
	 * @param matrix
	 * @return
	 */
	public double[] levineDomanyPValue(UPGMA upgma, double[][] matrix){
		
		int repeat=20;
		
		int p=5;
		double[] realResults=resamplingMethod(500, upgma, matrix);
		realResults=getAverageClusterV(realResults, false, upgma);
		UPGMA tempUPGMA;
		TreeDat[] dat=upgma.getDat();
		double[] simTotals=new double[dat.length];
		double simResults[];
		
		double[][] mat=new double[matrix.length][p];
		double[][] dmat=new double[matrix.length][];
		for (int i=0; i<matrix.length; i++){
			dmat[i]=new double[i+1];
		}
		for (int a=0; a<repeat; a++){
		
			for (int i=0; i<matrix.length; i++){
				for (int j=0; j<p; j++){
					mat[i][j]=(random.nextDouble());
				}
			}
			for (int i=0; i<matrix.length; i++){
				for (int j=0; j<i; j++){
					double tot=0;
					for (int k=0; k<p; k++){
						tot+=(mat[i][k]-mat[j][k])*(mat[i][k]-mat[j][k]);
					}
					dmat[i][j]=Math.sqrt(tot);
				}
			}		
			
			tempUPGMA=new UPGMA(dmat, dmode, beta);
				
			simResults=resamplingMethod(500, tempUPGMA, dmat);
			simResults=getAverageClusterV(simResults, false, tempUPGMA);
			for (int i=0; i<simResults.length; i++){
				simTotals[i]+=simResults[i];
			}
		}
		for (int i=0; i<realResults.length; i++){
			simTotals[i]/=repeat+0.0;
		
		}
		
		for (int i=0; i<realResults.length; i++){
		//	System.out.println(realResults[i]+" "+simTotals[i]+" "+sds[0]+" "+sds[1]+" "+sds[2]+" "+sds[3]+" "+sds[4]+" "+sds[5]);
			//realResults[i]=(realResults[i]-simTotals[i])/(1-simTotals[i]);
			//realResults[i]=realResults[i]-simTotals[i];
			//realResults[i]=simTotals[i];
			System.out.println(realResults[i]);
		}
		
		return realResults;
	}
	
	/**
	 * This resamples 67% of the tips in the tree and looks at the distributions of resulting
	 * dendrograms
	 * @param repeat number of repeats
	 * @param nupgma input upgma
	 * @param matrix dissimilarity matrix
	 * @return array containing the proportion of branches at a depth matching that of the corresponding branch in the input data
	 */
	public double[] resamplingMethod(int repeat, UPGMA nupgma, double[][] matrix){
	
		//int repeat=500;
		double prop=0.67;
		
		int n=matrix.length;
		
		int m=(int)Math.round(n*prop);
		double[][] treeMat=new double[n][];
		for (int i=0; i<n; i++){
			treeMat[i]=new double[i+1];
		}
		calculateTreeDistance(treeMat, nupgma.getDat());
				
		int[] sampled=new int[n];
		int[] selected=new int[m];
		double[][] rMat=new double[m][];
		double[][] rTreeMat=new double[m][];
		for (int i=0; i<m; i++){
			rMat[i]=new double[i+1];
			rTreeMat[i]=new double[i+1];
		}
		
		int nl=nupgma.getDat().length;
		double[] results=new double[nl];
		double[] countsamp=new double[nl];
		
		UPGMA supgma;
	
		for (int i=0; i<repeat; i++){
		
			for (int j=0; j<n; j++){
				sampled[j]=-1;
			}
	
			for (int j=0; j<m; j++){
				int t=random.nextInt(n);
				while (sampled[t]>=0){t=random.nextInt(n);}
				selected[j]=t;
				sampled[t]=j;
				
				for (int k=0; k<j; k++){
					if (selected[j]>selected[k]){
						rMat[j][k]=matrix[selected[j]][selected[k]];
					}
					else{
						rMat[j][k]=matrix[selected[k]][selected[j]];
					}
				}
			}
			
			supgma=new UPGMA(rMat, dmode, beta);
			calculateTreeDistance(rTreeMat, supgma.getDat());
			TreeDat[] ndat=nupgma.getDat();
			for (int j=0; j<ndat.length; j++){
			
				int clusteredTogether=0;
				int sampledTogether=0;

				for (int a=0; a<m; a++){
					
					for (int b=0; b<a; b++){
						int aa=selected[a];
						int bb=selected[b];
						if (aa<bb){
							bb=aa;
							aa=selected[b];
						}
						if (treeMat[aa][bb]<=ndat[j].dist){
							if (rTreeMat[a][b]<=ndat[j].dist){
								clusteredTogether++;
							}
							sampledTogether++;
						}
					}
				}
				
				if (sampledTogether>0){
					results[j]+=clusteredTogether/(sampledTogether+0.0);
					countsamp[j]++;
				}
			}
		}		
		
		for (int i=0; i<results.length; i++){
			if (countsamp[i]>0){
				results[i]/=countsamp[i];
			}
		}
		
		return results;
		
	}
	
	
	/**
	 * Calculates a dissimilarity matrix based on dendrogram data
	 * @param mat dissimilarity matrix to be filled
	 * @param dat input dendrogram branches
	 */
	
	public void calculateTreeDistance(double[][] mat, TreeDat[] dat){
	
	
		int n=dat.length;
		
		for (int i=n-1; i>=0; i--){
		
			for (int j=0; j<dat[i].child.length; j++){
				for (int k=0; k<j; k++){
					if (dat[i].child[j]>dat[i].child[k]){
						mat[dat[i].child[j]][dat[i].child[k]]=(dat[i].dist*dat[n-1].dist);
						//mat[dat[i].child[j]][dat[i].child[k]]=n-i;
						
					}
					else{
						mat[dat[i].child[k]][dat[i].child[j]]=(dat[i].dist*dat[n-1].dist);
						//mat[dat[i].child[k]][dat[i].child[j]]=n-i;
					}
				}
			}	
		}
	}
	
	/**
	 * Averages scores based on number of items per cluster
	 * @param perCluster
	 * @param weightByClusterSize
	 * @param upgma
	 * @return
	 */
	
	public double[] getAverageClusterV(double[] perCluster, boolean weightByClusterSize, UPGMA upgma){
		int n=perCluster.length;
		int n2=n-1;
		double[] results=new double[n];
		TreeDat[] dat=upgma.getDat();
		
		for (int i=1; i<n; i++){
			double thresh=dat[i].dist;
			if (thresh>0){
				double score=0;
				double count=0;
				for (int j=0; j<n2; j++){
					int k=dat[j].parent;
					if ((dat[j].dist<thresh)&&(dat[k].dist>=thresh)){
						if (dat[j].children>1){
							if (weightByClusterSize){
								score+=perCluster[j]*dat[j].children;
								count+=dat[j].children;
							}
							else{
								score+=perCluster[j];
								count++;
							}
						}
					}
				}
				if (count>0){
					results[i]=score/count;
				}
			}
			//results[i]=perCluster[i];
		}
		return results;
	}

	public double[] getAverageClusterV2(double[] perCluster, boolean weightByClusterSize, UPGMA upgma){
		TreeDat[] dat=upgma.getDat();
		int[][] parts=getPartitionMembers(dat);
		int n=perCluster.length;
		int n2=n-1;
		double[] results=new double[n];
		for (int i=0; i<n2; i++){
			double score=0;
			double count=0;
			if (parts[i]!=null){
				for (int j=0; j<parts[i].length; j++){
					int a=parts[i][j];
					if (dat[a].children>1){
						if (weightByClusterSize){
							score+=perCluster[a]*dat[a].children;
							count+=dat[a].children;
						}
						//else{
						//	score+=perCluster[a];
						//	count++;
						//}
					}
					//else{
						//score++;
						//count++;
					//}
				}
				if (count>0){
					results[i+1]=score/count;
				}
			}
		}
		return results;	
	
	
	}
	
	
	/**
	 * Calculates the partition members for each branch of a dendrogram
	 * @param dat input array of TreeDat tree branches
	 * @return an int[][] array that gives the tips of each branch of the dendrogram
	 */
	public int[][] getPartitionMembers(TreeDat[] dat){
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
				//System.out.println(i+" "+thresh+" "+n);
			}
		}
		return results;
	}
	
	
	/**
	 * Deprecated (unnecessarily?)
	 * @param upgma
	 * @param sg
	 * @return
	 */
	public double[] runCompositionAnalysis(UPGMA upgma, AnalysisGroup sg){
		TreeDat[] dat=upgma.getDat();
		int[][] pt=getPartitionMembers(dat);
		//int[] counts=sg.getIDCounts();
		
		double[] results=new double[pt.length];
		
		for (int i=pt.length-1; i>pt.length-20; i--){
			if (pt[i]!=null){
				int[][] od=new int[pt[i].length][];
			
				for (int j=0; j<pt[i].length; j++){
					//int[] t=sg.getIDLabels(dat[pt[i][j]].child);
					//od[j]=new int[t.length];
					//System.arraycopy(t, 0, od[j], 0, t.length);
				}
				//results[i]=compositionAnalysis(od, counts);
				System.out.println("COMPOSITION: "+i+" "+results[i]);
			}
		}
		return results;
	}

	/**
	 * Deprecated???
	 * @param observedData
	 * @param counts
	 * @return
	 */
	public double compositionAnalysis(int[][] observedData, int[] counts){
	
		int numRepeats=10000;
	
		int n=observedData.length;
		int m=counts.length;
		
		int[] groupSizes=new int[n];
		for (int i=0; i<n; i++){
			for (int j=0; j<m; j++){
				groupSizes[i]+=observedData[i][j];
			}
		}
		
		int p=0;
		for (int i=0; i<m; i++){
			p+=counts[i];
		}
		
		double[][] expectedData=new double[n][m];
		for (int i=0; i<n; i++){
			for (int j=0; j<m; j++){
				expectedData[i][j]=groupSizes[i]*(counts[j]/(p+0.0));
			}
		}
		
		double[] testData=new double[n];
		double overallTest=0;
		for (int i=0; i<n; i++){
			for (int j=0; j<m; j++){
				testData[i]+=(observedData[i][j]-expectedData[i][j])*(observedData[i][j]-expectedData[i][j])/expectedData[i][j];
				//testData[i]+=2*obervedData[i][j]*Math.log(observedData[i][j]/expectedData[i][j]);
				//System.out.println(i+" "+j+" "+observedData[i][j]+" "+expectedData[i][j]+" "+testData[i]+" "+counts[j]);
				overallTest+=testData[i];
			}
		}
		
		int[] oge=new int[n];
		int overallStat=0;
		int[] c=new int[p];
		double[] avvk=new double[n];
		
		for (int i=0; i<numRepeats; i++){
			/*
			int q=0;
			for (int j=0; j<m; j++){
				for (int k=0; k<counts[j]; k++){
					c[q]=j;
					q++;
				}
			}
			for (int j=0; j<p; j++){
				int r=random.nextInt(p-j);
				r+=j;
				int s=c[r];
				c[r]=c[j];
				c[j]=s;
			}
			*/
			for (int j=0; j<p; j++){
				int r=random.nextInt(p);
				int t=0;
				for (int k=0; k<m; k++){
					if ((r>=t)&&(r<t+counts[k])){
						c[j]=k;
					}
					t+=counts[k];
				}
			}
			int t=0;
			double overallw=0;
			for (int j=0; j<n; j++){
				int u=t+groupSizes[j];
				int[] v=new int[m];
				for (int k=t; k<u; k++){
					v[c[k]]++;
				}
				double w=0;
				for (int k=0; k<m; k++){
					w+=(v[k]-expectedData[j][k])*(v[k]-expectedData[j][k])/expectedData[j][k];
					//w+=2*v[k]*Math.log(v[k]/expectedData[j][k]);
				}
				if (w>testData[j]){
					oge[j]++;
				}
				avvk[j]+=w;
				overallw+=w;
				t=u;
			}
			if (overallw>overallTest){
				overallStat++;
			}
		}		
		double results=0;
		for (int i=0; i<n; i++){
			avvk[i]/=numRepeats+0.0;
			System.out.println(oge[i]+" "+avvk[i]+" "+testData[i]);
			results+=oge[i]/(numRepeats+0.0);
		}
		results/=n+0.0;
		results=overallStat/(numRepeats+0.0);

		return results;
	}

}
