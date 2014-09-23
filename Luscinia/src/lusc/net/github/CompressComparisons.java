package lusc.net.github;
//
//  CompressComparisons.java
//  Luscinia
//
//  Created by Robert Lachlan on 10/14/06.
//  Copyright 2006 __MyCompanyName__. All rights reserved.
//

import java.util.*;

public class CompressComparisons {
	
	double syntaxPenalty=0.0;
	public CompressComparisons(){
	
	
	}
	
	public float[][] compareElements(float[][] scores, Song[] songs){
		int aa=songs.length;
		LinkedList phrases=new LinkedList();
		int prev_song_eles=0;
		for (int i=0; i<aa; i++){
			if (songs[i].phrases==null){songs[i].interpretSyllables();}
			System.out.println(songs[i].phrases.size());
			for (int j=0; j<songs[i].phrases.size(); j++){
				int[][] q=(int[][])songs[i].phrases.get(j);
				int[][] r=new int[q.length][];
				for (int g=0; g<q.length; g++){
					r[g]=new int[q[g].length];
					for (int h=0; h<q[g].length; h++){
						if (q[g][h]!=-1){
							r[g][h]=q[g][h]+prev_song_eles;
						}
						else{
							r[g][h]=-1;
						}
					}
				}
				
				phrases.add(r);
			}
			prev_song_eles+=songs[i].eleList.size();
		}
		
		int ele_count=0;
		for (int i=0; i<phrases.size(); i++){
			int[][] p=(int[][])phrases.get(i);
			ele_count+=p[0].length;
		}

		float[][]scoreEle=new float[ele_count][];
		for (int i=0; i<ele_count; i++){
			scoreEle[i]=new float[i+1];
		}
		
		int place1=0;
		
		for (int i=0; i<phrases.size(); i++){
			int[][] p1=(int[][])phrases.get(i);
			
			int place2=0;
			
			for (int j=0; j<=i; j++){
				int[][]p2=(int[][])phrases.get(j);
				
				for (int g=0; g<p1[0].length; g++){
					for (int h=0; h<p2[0].length; h++){
						if (place1+g>place2+h){
							double bscore=0;
							double bcount=0;
							for (int v=0; v<p1.length; v++){
								for (int w=0; w<p2.length; w++){
									if ((p1[v][g]!=-1)&&(p2[w][h]!=-1)){
										if (p1[v][g]>p2[w][h]){
											bscore+=scores[p1[v][g]][p2[w][h]];
										}
										else{
											bscore+=scores[p2[w][h]][p1[v][g]];
										}
										bcount++;
									}
								}
							}
							scoreEle[place1+g][place2+h]=(float)(bscore/bcount);
						}
					}
				}
				place2+=p2[0].length;
			}
			place1+=p1[0].length;
		}
		return scoreEle;
	}
	
	public float[][] compareSyllables3(float[][] scores, Song[] songs){
		int aa=songs.length;
		LinkedList phrases=new LinkedList();
		int prev_song_eles=0;
		for (int i=0; i<aa; i++){
			if (songs[i].phrases==null){songs[i].interpretSyllables();}
			for (int j=0; j<songs[i].phrases.size(); j++){
				int[][] q=(int[][])songs[i].phrases.get(j);
				int[][] r=new int[q.length][];
				for (int g=0; g<q.length; g++){
					r[g]=new int[q[g].length];
					for (int h=0; h<q[g].length; h++){
						if (q[g][h]!=-1){
							r[g][h]=q[g][h]+prev_song_eles;
						}
						else{
							r[g][h]=-1;
						}
					}
				}
				
				phrases.add(r);
			}
			prev_song_eles+=songs[i].eleList.size();
		}
		
		int phrase_count=phrases.size();
		float[][]scoreSyll=new float[phrase_count][];
		for (int i=0; i<phrase_count; i++){
			scoreSyll[i]=new float[i+1];
		}
		
		
		for (int i=0; i<phrase_count; i++){
			int[][] p1=(int[][])phrases.get(i);
			for (int j=0; j<i; j++){
				int[][]p2=(int[][])phrases.get(j);
				
				int[][]q1=p1;
				int[][]q2=p2;
				
				if (p2[0].length>p1[0].length){
					q1=p2;
					q2=p1;
				}
				
				double bscore=1000000;
				//for (int k=0; k<q2[0].length; k++){
				for (int k=0; k<1; k++){
					double mscore=0;
					int b=k-1;
					for (int a=0; a<q1[0].length; a++){
					
						b++;
						if (b==q2[0].length){
							b=0;
							mscore+=syntaxPenalty;
						}
						
						double count=0;
						//double tscore=1000000;
						double tscore=0;
						for (int h=0; h<q1.length; h++){
							for (int g=0; g<q2.length; g++){
								if ((q1[h][a]!=-1)&&(q2[g][b]!=-1)){
									count++;
									if (q1[h][a]>q2[g][b]){
										tscore+=scores[q1[h][a]][q2[g][b]];
										
										//if (tscore>scores[q1[h][a]][q2[g][b]]){
										//	tscore=scores[q1[h][a]][q2[g][b]];
										//}
										
									}
									else{
										tscore+=scores[q2[g][b]][q1[h][a]];
										//if (tscore>scores[q2[g][b]][q1[h][a]]){
										//	tscore=scores[q2[g][b]][q1[h][a]];
										//}
									}
								}
							}
						}
						tscore/=count;
						mscore+=tscore;
					}
					mscore/=q1[0].length+0.0;
					if (mscore<bscore){
						bscore=mscore;
					}
				}
				scoreSyll[i][j]=(float)bscore;
			}
		}
		return scoreSyll;
	}
	
	public float[][] compareSyllables4(float[][] scores, Song[] songs){
	
		int aa=songs.length;
	
		double averageScore=0;
		double sum=0;
		
		for (int i=0; i<scores.length; i++){
			for (int j=0; j<i; j++){
				averageScore+=scores[i][j];
				sum++;
			}
		}
		averageScore/=sum;
		syntaxPenalty=averageScore*0.0;
		
		
		//System.out.println(averageScore+" "+syntaxPenalty+" "+sum);
		
		LinkedList phrases=new LinkedList();
		int prev_song_eles=0;
		
		double[] lengths=new double[scores.length];
		int jj=0;
		for (int i=0; i<aa; i++){
			for (int j=0; j<songs[i].eleList.size(); j++){
				Element ele=(Element)songs[i].eleList.get(j);
				lengths[jj]=Math.log(ele.length);
				jj++;
			}
			if (songs[i].phrases==null){songs[i].interpretSyllables();}
			for (int j=0; j<songs[i].phrases.size(); j++){
				int[][] q=(int[][])songs[i].phrases.get(j);
				int[][] r=new int[q.length][];
				for (int g=0; g<q.length; g++){
					r[g]=new int[q[g].length];
					for (int h=0; h<q[g].length; h++){
						if (q[g][h]!=-1){
							r[g][h]=q[g][h]+prev_song_eles;
						}
						else{
							r[g][h]=-1;
						}
					}
				}
				
				phrases.add(r);
			}
			prev_song_eles+=songs[i].eleList.size();
		}
		
		int phrase_count=phrases.size();
		float[][]scoreSyll=new float[phrase_count][];
		for (int i=0; i<phrase_count; i++){
			scoreSyll[i]=new float[i+1];
		}
		
		
		for (int i=0; i<phrase_count; i++){
			int[][] p1=(int[][])phrases.get(i);
			for (int j=0; j<i; j++){
				int[][]p2=(int[][])phrases.get(j);
				
				int[][]q1=p1;
				int[][]q2=p2;
				
				if (p2[0].length>p1[0].length){
					q1=p2;						//q1 is the phrase with the most elements per syllable
					q2=p1;
				}
				/*
				System.out.println("LENGTHS: "+p1[0].length+" "+p2[0].length);
				
				for (int x=0; x<q1.length; x++){
					for (int y=0; y<q1[x].length; y++){
						System.out.print(q1[x][y]+" ");
					}
					System.out.println();
				}
				System.out.println();
				for (int x=0; x<q2.length; x++){
					for (int y=0; y<q2[x].length; y++){
						System.out.print(q2[x][y]+" ");
					}
					System.out.println();
				}
				*/
				
				int longphrase=q1.length;		//longphrase means most repeats
				int shortphrase=q2.length;
				boolean invert=false;
				if (q2.length>q1.length){
					longphrase=q2.length;
					shortphrase=q1.length;
					invert=true;
				}
				double mscore=0;
				double count2=0;				
				for (int x=0; x<longphrase; x++){
					double bscore=10000000;
					for (int y=0; y<shortphrase; y++){
						int a=x;
						int b=y;
						if (invert){
							a=y;
							b=x;
						}
						double tscore=0;
						double ttscore=0;
						double count=0;
						int d=0;
						for (int c=0; c<q1[0].length; c++){
							//System.out.println(a+" "+c+" "+b+" "+d+" "+q1[a][c]+" "+q2[b][d]);
							if ((q1[a][c]!=-1)&&(q2[b][d]!=-1)){
								double avlength=0.5*(lengths[q1[a][c]]+lengths[q2[b][d]]);
								
								count+=avlength;
								if (q1[a][c]>q2[b][d]){
									tscore+=scores[q1[a][c]][q2[b][d]]*avlength;
										
									//if (tscore<scores[q1[a][c]][q2[b][d]]){
									//	tscore=scores[q1[a][c]][q2[b][d]];
									//}
										
								}
								else{
									tscore+=scores[q2[b][d]][q1[a][c]]*avlength;
									//if (tscore<scores[q2[b][d]][q1[a][c]]){
									//	tscore=scores[q2[b][d]][q1[a][c]];
									//}
								}
							}
							else{
								count=-100000;
							}
							d++;
							if (d==q2[0].length){
								ttscore=tscore;
								d=0;
							}
						}
						if (count>0){
							if (q1[0].length!=q2[0].length){
								tscore+=syntaxPenalty;
								//ttscore=tscore+(q1[0].length-q2[0].length)*averageScore;
								//if (ttscore<tscore){
								//	tscore=ttscore;
								//}
							}
							tscore/=count;
							if (tscore<bscore){
								bscore=tscore;
							}
						}
						//System.out.println(count+" "+i+" "+j+" "+tscore+" "+ttscore+" "+count);
					}
					if (bscore<10000000){
						mscore+=bscore;
						count2++;
					}
				}
				scoreSyll[i][j]=(float)(mscore/count2);
			}
		}
		return scoreSyll;
	}
	
	public float[][] penalizeLengthDifference(float[][] scores, Song[] songs, double sp){
		int aa=songs.length;
		
		double averageScore=0;
		double sum=0;
		
		for (int i=0; i<scores.length; i++){
			for (int j=0; j<i; j++){
				if (!Float.isNaN(scores[i][j])){
					averageScore+=scores[i][j];
					sum++;
				}
				else{
					System.out.println("NAN Error: "+i+" "+j);
					scores[i][j]=0;
				}
			}
		}
		averageScore/=sum;
		float syntaxPenalty=(float)(sp*averageScore);
		System.out.println("Syntax penalty: "+sp+" "+averageScore+" "+sum+" "+syntaxPenalty);
		int [] syllLengths=new int[scores.length];
		int p=0;
		
		for (int i=0; i<aa; i++){
			if (songs[i].phrases==null){songs[i].interpretSyllables();}
			for (int j=0; j<songs[i].phrases.size(); j++){
				int[][] q=(int[][])songs[i].phrases.get(j);
				
				for (int g=0; g<q.length; g++){
					int a=0;
					for (int h=0; h<q[g].length; h++){
						if (q[g][h]!=-1){
							a++;
						}
					}
					syllLengths[p]=a;
					System.out.println(songs[i].name+" "+j+" "+g+" "+a);
					p++;
				}
				
				
			}
		}
		
		for (int i=0; i<scores.length; i++){
			for (int j=0; j<i; j++){
				if (syllLengths[i]!=syllLengths[j]){
					scores[i][j]+=syntaxPenalty;
				}
			}
		}
		
		return scores;
	}
	
	public float[][] phraseComp(float[][] scores, Song[] songs, float sp){
		
		int aa=songs.length;
		
		double averageScore=0;
		double sum=0;
		
		for (int i=0; i<scores.length; i++){
			for (int j=0; j<i; j++){
				if (!Float.isNaN(scores[i][j])){
					averageScore+=scores[i][j];
					sum++;
				}
				else{
					System.out.println("NAN Error: "+i+" "+j);
					scores[i][j]=0;
				}
			}
		}
		averageScore/=sum;
		float syntaxPenalty=(float)(sp*averageScore);
		System.out.println("Syntax penalty: "+sp+" "+averageScore+" "+sum+" "+syntaxPenalty);
		LinkedList sylls=new LinkedList();
		int prev_song_eles=0;
		
		double[] lengths=new double[scores.length];
		int jj=0;
		
		int syllCount=0;
		
		for (int i=0; i<aa; i++){
			if (songs[i].phrases==null){songs[i].interpretSyllables();}
			for (int j=0; j<songs[i].phrases.size(); j++){
				int[][] q=(int[][])songs[i].phrases.get(j);
				syllCount+=q.length;
				for (int g=0; g<q.length; g++){
					
					int[] r=new int[2];
					boolean found=false;
					for (int h=0; h<q[g].length; h++){
						if (q[g][h]!=-1){
							if (!found){
								r[0]=q[g][h]+prev_song_eles;
								found=true;
							}
							r[1]++;
						}
					}
					sylls.add(r);
					//System.out.println(songs[i].name+" "+r[0]+" "+r[1]);
				}
				
				
			}
			prev_song_eles+=songs[i].eleList.size();
		}
		
		float[][]scoreSyll=new float[syllCount][];
		for (int i=0; i<syllCount; i++){
			scoreSyll[i]=new float[i+1];
		}
		
		for (int i=0; i<syllCount; i++){
			int[] p1=(int[])sylls.get(i);
			
			for (int j=0; j<i; j++){
				int[]p2=(int[])sylls.get(j);
				
				scoreSyll[i][j]=syllableComp(scores, p1[0], p1[1], p2[0], p2[1], syntaxPenalty);
			}
		}
		
		return compareSyllables5(scoreSyll, songs, 0);
	}
	
	
	
	
	
	
	public float syllableComp(float[][] eleScores, int a, int n, int b, int m, float syntaxPenalty){
		
		// make sure n>m
		
		double exp=1;
		
		if (n<m){
			int x=a;
			int y=n;
			a=b;
			n=m;
			b=x;
			m=y;
		}
		
		//System.out.println(a+" "+n+" "+b+" "+m);
		
		float bestScore=1000000f;
		
		for (int i=0; i<m; i++){
			double score=0;
			
			int k=i;
			for (int j=0; j<n; j++){
				
				int x=j+a;
				int y=k+b;
				
				if (x>y){
					score+=Math.pow(eleScores[x][y], exp);
					//score+=eleScores[x][y];
				}
				else{
					score+=Math.pow(eleScores[y][x], exp);
					//score+=eleScores[y][x];
				}
				
				k++;
				if (k==m){
					k=0;
				}
			}
			
			score/=n+0.0;
			score=Math.pow(score, 1/exp);
			if (n!=m){score+=syntaxPenalty;}
			if (i!=0){score+=syntaxPenalty;}
			if (score<bestScore){bestScore=(float)score;}
		}
		return bestScore;		
	}
	
	public float[][] compareSyllables5(float[][] syScores, Song[] songs, double synco){
	
		if (synco>0){
			syScores=penalizeLengthDifference(syScores, songs, synco);
		}
		
		int[] phraseID=new int[syScores.length];
		
		int phraseCount=0;
		int syllCount=0;
		for (int i=0; i<songs.length; i++){
			for (int j=0; j<songs[i].phrases.size(); j++){
				int[][] p=(int[][])songs[i].phrases.get(j);
				int a=p.length;
				
				for (int k=0; k<a; k++){
					boolean incomplete=false;
					for (int l=0; l<p[k].length; l++){
						if (p[k][l]==-1){
							incomplete=true;
							l=p[k].length;
						}
					}
					
					//if (incomplete){
					//	phraseID[syllCount]=-1;
					//}
					//else{
						phraseID[syllCount]=phraseCount;
					//}
					syllCount++;
				}
				
				phraseCount++;
			}
		}
	
		float[][] results=new float[phraseCount][];
		float[][] count=new float[phraseCount][];
		
		for (int i=0; i<phraseCount; i++){
			results[i]=new float[i+1];
			for (int j=0; j<i; j++){
				//results[i][j]=1000000f;
			}
			count[i]=new float[i+1];
		}
		
		for (int i=0; i<syScores.length; i++){
			for (int j=0; j<i; j++){
				//if ((phraseID[i]>=0)&&(phraseID[j]>=0)){
					//if (syScores[i][j]<results[phraseID[i]][phraseID[j]]){
						//results[phraseID[i]][phraseID[j]]=syScores[i][j];
					//}
					
					results[phraseID[i]][phraseID[j]]+=syScores[i][j];
					count[phraseID[i]][phraseID[j]]++;
				//}
			}
		}
		for (int i=0; i<phraseCount; i++){
			for (int j=0; j<i; j++){
				results[i][j]/=count[i][j];
				//results[i][j]*=results[i][j];
				if (count[i][j]==0){System.out.println("ALERT! "+i+" "+j);}
			}
			results[i][i]=0;
		}
		count=null;
		
		return results;
	}
	
	public float[][] syntaxCompression(float[][] input, int[][] lookUp, boolean checkProgress){
		
		//checkProgress=false;
		int n=lookUp.length;
	
		double[][] scores=new double[n][];
		double[][] scores2=new double[n][];
		for (int i=0; i<n;i++){
			scores[i]=new double[i+1];
			scores2[i]=new double[i+1];
			for (int j=0; j<i; j++){
				scores[i][j]=Math.sqrt(input[i][j]);
			}
		}
		
		
	
		
		boolean[] hasNext=new boolean[n];
		boolean[] hasPrevious=new boolean[n];
		for (int i=0; i<n; i++){
			if ((i<n-1)&&(lookUp[i][0]==lookUp[i+1][0])){
				hasNext[i]=true;
			}
			else{
				hasNext[i]=false;
			}
			if ((i>0)&&(lookUp[i][0]==lookUp[i-1][0])){
				hasPrevious[i]=true;
			}
			else{
				hasPrevious[i]=false;
			}
		}
		
		normalizeMatrix(scores);
		
		int iterations=1;
		float increment=0.5f;
		
		for (int i=0; i<iterations; i++){
		
			for (int j=0; j<n; j++){
				for (int k=0; k<j; k++){
					scores2[j][k]=scores[j][k];
					if ((hasPrevious[j])&&(hasPrevious[k])){
						scores2[j][k]+=increment*scores[j-1][k-1];
					}
					else if ((hasNext[j])&&(hasNext[k])){
						scores2[j][k]+=increment*scores[j+1][k+1];;
					}
					else{
						scores2[j][k]+=increment*scores[j][k];
					}
				}
			}
			for (int j=0; j<n; j++){
				for (int k=0; k<j; k++){
					if ((hasNext[j])&&(hasNext[k])){
						scores2[j][k]+=increment*scores[j+1][k+1];
					}
					else if ((hasPrevious[j])&&(hasPrevious[k])){
						scores2[j][k]+=increment*scores[j-1][k-1];
					}
					else{
						scores2[j][k]+=increment*scores[j][k];
					}
				}
			}
			
			normalizeMatrix(scores2);
			if (checkProgress){
				double d=calculateMeanMatrixDist(scores, scores2);
				System.out.println(d);
			}
			for (int j=0; j<n; j++){
				for (int k=0; k<j; k++){
					scores[j][k]=scores2[j][k];
				}
			}
		}
		scores2=null;
		float[][] out=new float[n][];
		for (int i=0; i<n;i++){
			out[i]=new float[i+1];
			for (int j=0; j<i; j++){
				if (scores[i][j]>0){
					out[i][j]=(float)(scores[i][j]*scores[i][j]);
				}
			}
		}
		scores=null;
		
		return out;
	}
	
	public void syntaxCompression(float[][] input, float[][] output, int[][] lookUp){
		
		//checkProgress=false;
		int n=lookUp.length;
		
		boolean[] hasNext=new boolean[n];
		boolean[] hasPrevious=new boolean[n];
		for (int i=0; i<n; i++){
			if ((i<n-1)&&(lookUp[i][0]==lookUp[i+1][0])){
				hasNext[i]=true;
			}
			else{
				hasNext[i]=false;
			}
			if ((i>0)&&(lookUp[i][0]==lookUp[i-1][0])){
				hasPrevious[i]=true;
			}
			else{
				hasPrevious[i]=false;
			}
		}
				
		float increment=0.5f;
		
		for (int i=0; i<n; i++){
			for (int j=0; j<i; j++){
				output[i][j]=input[i][j];
				if ((hasPrevious[i])&&(hasPrevious[j])){
					output[i][j]+=increment*input[i-1][j-1];
				}
				else if ((hasNext[i])&&(hasNext[j])){
					output[i][j]+=increment*input[i+1][j+1];;
				}
				else{
					output[i][j]+=increment*input[i][j];
				}
			}
		}
		for (int i=0; i<n; i++){
			for (int j=0; j<i; j++){
				if ((hasNext[i])&&(hasNext[j])){
					output[i][j]+=increment*input[i+1][j+1];
				}
				else if ((hasPrevious[i])&&(hasPrevious[j])){
					output[i][j]+=increment*input[i-1][j-1];
				}
				else{
					output[i][j]+=increment*input[i][j];
				}
			}
		}
	}
	
	public void normalizeMatrix(double[][] mat){
		BasicStatistics bs=new BasicStatistics();
		
		double sd=bs.calculateSD(mat, false);
		int n=mat.length;
		for (int i=0; i<n; i++){
			for (int j=0; j<i; j++){
				mat[i][j]=(mat[i][j]/sd);
			}
		}
	}
	
	public double calculateMeanMatrixDist(float[][] mat1, float[][] mat2){
	
		int n=mat1.length;
		double a;
		double d=0;
		double count=0;
		for (int i=0; i<n; i++){
			for (int j=0; j<i; j++){
				a=mat1[i][j]-mat2[i][j];
				d+=Math.abs(a);
				count++;
			}
		}
		return (d/count);
	}
	
	public double calculateMeanMatrixDist(double[][] mat1, double[][] mat2){
	
		int n=mat1.length;
		double a;
		double d=0;
		double count=0;
		for (int i=0; i<n; i++){
			for (int j=0; j<i; j++){
				a=mat1[i][j]-mat2[i][j];
				d+=Math.abs(a);
				count++;
			}
		}
		return (d/count);
	}

		
	

	
	public float[][] compareSyllables(float[][] scores, Song[] songs){
		int aa=songs.length;
		LinkedList phrases=new LinkedList();
		int prev_song_eles=0;
		for (int i=0; i<aa; i++){
			if (songs[i].phrases==null){songs[i].interpretSyllables();}
			for (int j=0; j<songs[i].phrases.size(); j++){
				int[][] q=(int[][])songs[i].phrases.get(j);
				
				double[][] s=new double[q[0].length][2];
				
				
				for (int g=0; g<q.length; g++){
					for (int h=0; h<q[g].length; h++){
						if (q[g][h]!=-1){
							Element ele=(Element)songs[i].eleList.get(q[g][h]);
							if (ele.timeAfter>-10000){
								s[h][0]+=ele.timeAfter;
								s[h][1]++;
							}
							else{
								s[h][0]+=500;
								s[h][1]++;
							}
						}
					}
				}
				
				double maxGap=0;
				int maxGapLoc=0;
				
				for (int g=0; g<s.length; g++){
					if (s[g][1]>0){
						double sc=s[g][0]/s[g][1];
						if (sc>maxGap){
							maxGap=sc;
							maxGapLoc=g;
						}
					}
				}
				//System.out.println(maxGap+" "+maxGapLoc+" "+q[0].length);
				
				maxGapLoc++;
				if (maxGapLoc==q[0].length){maxGapLoc=0;}
				
				
				int[][] r=new int[q.length][];
				for (int g=0; g<q.length; g++){
					r[g]=new int[q[g].length];
					for (int h=0; h<q[g].length; h++){
						int hh=h-maxGapLoc;
						if (hh<0){h+=q[g].length;}
						if (q[g][h]!=-1){
							r[g][hh]=q[g][h]+prev_song_eles;
						}
						else{
							r[g][hh]=-1;
						}
					}
				}
				
				phrases.add(r);
			}
			prev_song_eles+=songs[i].eleList.size();
		}
		
		int phrase_count=phrases.size();
		float[][]scoreSyll=new float[phrase_count][];
		for (int i=0; i<phrase_count; i++){
			scoreSyll[i]=new float[i+1];
		}
		
		
		for (int i=0; i<phrase_count; i++){
			int[][] p1=(int[][])phrases.get(i);
			for (int j=0; j<i; j++){
				int[][]p2=(int[][])phrases.get(j);
				
				int[][]q1=p1;
				int[][]q2=p2;
				
				if (p2[0].length>p1[0].length){
					q1=p2;
					q2=p1;
				}
				
				double mscore=0;
				int b=0;
				for (int k=0; k<q1[0].length; k++){
					b=k;
					while (b>=q2[0].length){b-=q2[0].length;}
					double count=0;
					double tscore=1000000;
					for (int h=0; h<q1.length; h++){
						for (int g=0; g<q2.length; g++){
							if ((q1[h][k]!=-1)&&(q2[g][b]!=-1)){
								count++;
								if (q1[h][k]>q2[g][b]){
									if (tscore>scores[q1[h][k]][q2[g][b]]){
										tscore=scores[q1[h][k]][q2[g][b]];
									}
								}
								else{
									if (tscore>scores[q2[g][b]][q1[h][k]]){
										tscore=scores[q2[g][b]][q1[h][k]];
									}
								}
							}
						}
					}
					tscore/=count;
					mscore+=tscore;
				}
				if (b!=q2[0].length-1){mscore+=syntaxPenalty;}
				scoreSyll[i][j]+=mscore/(q1[0].length+0.0);
			}
		}
		return scoreSyll;
	}

	
	public float[][] compareSyllables2(float[][] scores, Song[] songs){

		int aa=songs.length;
		LinkedList phrases=new LinkedList();
		int count=0;
		for (int i=0; i<aa; i++){
			if (songs[i].phrases==null){songs[i].interpretSyllables();}
			for (int j=0; j<songs[i].phrases.size(); j++){
				int[][] q=(int[][])songs[i].phrases.get(j);
				
				int[]r={count, count+q.length};
				count+=q.length;
				phrases.add(r);
			}
		}
		
		int phrase_count=phrases.size();
		float[][]scoreSyll=new float[phrase_count][];
		for (int i=0; i<phrase_count; i++){
			scoreSyll[i]=new float[i+1];
		}
		
		
		for (int i=0; i<phrase_count; i++){
			int[] p1=(int[])phrases.get(i);
			for (int j=0; j<i; j++){
				int[]p2=(int[])phrases.get(j);
				
				
				double x=0;
				double y=0;
				for (int g=p1[0]; g<p1[1]; g++){
					for (int h=p2[0]; h<p2[1]; h++){
						x+=scores[g][h];
						y++;
						//if (scores[g][h]>x){x=scores[g][h];}
					}
				}
				//System.out.println(x+" "+y+" "+p1[0]+" "+p1[1]+" "+p2[0]+" "+p2[1]);
				scoreSyll[i][j]+=x/y;
				//scoreSyll[i][j]+=x;
			}
		}
		return scoreSyll;
	}
	
	public float[][] compareSyllableTransitions(float[][] scores, Song[] songs){
		
		float endStatePenalty=2f;
		
		int songNumber=songs.length;
		int sylNumber=scores.length+songNumber;		//For each song, there are n+1 transitions, where n=number of phrases!
		System.out.println(sylNumber+" "+songNumber+" "+scores.length);
		int[][] songLocs=new int[sylNumber][2];
		int count=0;
		int count2=0;
		for (int i=0; i<songNumber; i++){
			int p=songs[i].phrases.size();
			System.out.println(p+" "+count);
			
			for (int j=0; j<=p; j++){
				songLocs[count][0]=count2-1;
				if (j==0){songLocs[count][0]=-1;}
				songLocs[count][1]=count2;
				
				System.out.println(" "+songLocs[count][0]+" "+songLocs[count][1]);
				
				count++;
				count2++;
			}
			songLocs[count-1][1]=-1;
			count2--;
		}
		
		System.out.println(count+" "+sylNumber+" "+count2+" "+scores.length);
		
		float[][] scoreTrans=new float[sylNumber][];
		for (int i=0; i<sylNumber; i++){
			scoreTrans[i]=new float[i+1];
		}
		
		for (int i=0; i<sylNumber; i++){
			for (int j=0; j<i; j++){
				for (int k=0; k<2; k++){
					int ii=songLocs[i][k];
					int jj=songLocs[j][k];
			
					if ((ii>=0)&&(jj>=0)){
						scoreTrans[i][j]+=scores[ii][jj];
					}
					else if ((ii<0)&&(jj<0)){
						scoreTrans[i][j]+=0;
					}
					else{
						scoreTrans[i][j]+=endStatePenalty;
					}
				}
				scoreTrans[i][j]*=0.5f;
			}
		}
		return scoreTrans;
	}
	
	public float[][] compareSyllableTransitions2(float[][] scores, Song[] songs){
		//calculates Transitions without end states
		
		int songNumber=songs.length;
		int sylNumber=scores.length-songNumber;		//For each song, there are n-1 transitions, where n=number of phrases!
		System.out.println(sylNumber+" "+songNumber+" "+scores.length);
		int[] songLocs=new int[sylNumber];
		int count=0;
		int count2=0;
		for (int i=0; i<songNumber; i++){
			int p=songs[i].phrases.size();
			//System.out.println(p+" "+count);
			int p1=p-1;
			for (int j=0; j<p; j++){
				if (j<p1){
					songLocs[count]=count2;
					count++;
				}
				count2++;
			}
		}
		System.out.println(sylNumber+" "+scores.length);
		float[][] scoreTrans=new float[sylNumber][];
		for (int i=0; i<sylNumber; i++){
			scoreTrans[i]=new float[i+1];
		}
		
		for (int i=0; i<sylNumber; i++){
			for (int j=0; j<i; j++){
				int i1=songLocs[i];
				int j1=songLocs[j];
				int i2=songLocs[i]+1;
				int j2=songLocs[j]+1;
				//scoreTrans[i][j]=(float)Math.sqrt(scores[i1][j1]*scores[i1][j1]+scores[i2][j2]*scores[i2][j2]);
				scoreTrans[i][j]=(float)Math.sqrt(scores[i1][j1]*scores[i2][j2]);
				//scoreTrans[i][j]=Math.max(scores[i1][j1], scores[i2][j2]);
				//scoreTrans[i][j]=scores[i1][j1]+scores[i2][j2];
				//scoreTrans[i][j]*=0.5f;
			}
		}
		songLocs=null;
		return scoreTrans;
	}


	public float[][] compareSongs(float[][] scores, Song[] songs){

		
		int songNumber=songs.length;
		LinkedList songlocs=new LinkedList();
		int count=0;
		for (int i=0; i<songNumber; i++){
			int p=songs[i].phrases.size();
			int[]r={count, count+p};
			songlocs.add(r);
			count+=p;
		}

		
		float[][] scoreSong=new float[songNumber][];
		for (int i=0; i<songNumber; i++){
			scoreSong[i]=new float[i+1];
		}
		
		for (int i=0; i<songNumber; i++){
			int[] p1=(int[])songlocs.get(i);
			for (int j=0; j<i; j++){
				int[]p2=(int[])songlocs.get(j);
				
				int[]q1=p1;
				int[]q2=p2;
				if (q1[1]-q1[0]<q2[1]-q2[0]){
					q1=p2;
					q2=p1;
				}
				
				double x=0;
				double y=0;
				double xbest=0;
				for (int g=q1[0]; g<q1[1]; g++){
					double bestscore=100000000;
					for (int h=q2[0]; h<q2[1]; h++){
						if (g>h){
							if (scores[g][h]<bestscore){bestscore=scores[g][h];}
						}
						else{
							if (scores[h][g]<bestscore){bestscore=scores[h][g];}
						}
					}
					x+=bestscore;
					y++;
					if (bestscore>xbest){xbest=bestscore;}
				}
				scoreSong[i][j]+=(x/y);
			}
		}
		return scoreSong;
	}
	
	public float[][] compareSongs(float[][] scores, int[][] lookUp){

		
		int syllNumber=scores.length;
		
		int songNumber=1;
		for (int i=0; i<syllNumber-1; i++){
			if (lookUp[i][0]!=lookUp[i+1][0]){
				songNumber++;
			}
		}
		songNumber++;
		
		int[] start=new int[songNumber];
		int[] end=new int[songNumber];
		
		songNumber=0;
		for (int i=1; i<syllNumber; i++){
			if (lookUp[i][0]!=lookUp[i-1][0]){
				end[songNumber]=i;
				songNumber++;
				if (songNumber<start.length){
					start[songNumber]=i;
				}
			}
		}
		
		end[songNumber]=syllNumber;
		songNumber++;
		
		for (int i=0; i<songNumber; i++){
			System.out.println(songNumber+" "+start[i]+" "+end[i]+" "+syllNumber);
		}
		
		float[][] results=new float[songNumber][];
		for (int i=0; i<songNumber; i++){
			results[i]=new float[i+1];
		}
		
		for (int i=0; i<songNumber; i++){
			for (int j=0; j<i; j++){
				
				double score1=0;
				
				for (int a=start[i]; a<end[i]; a++){
					double bestScore=100000000;
					for (int b=start[j]; b<end[j]; b++){
						if (scores[a][b]<bestScore){
							bestScore=scores[a][b];
						}
					}
					score1+=bestScore;
				}
				score1/=end[i]-start[i]+0.0;
				
				double score2=0;
				
				for (int a=start[j]; a<end[j]; a++){
					double bestScore=100000000;
					for (int b=start[i]; b<end[i]; b++){
						if (scores[b][a]<bestScore){
							bestScore=scores[b][a];
						}
					}
					score2+=bestScore;
				}
				
				score2/=end[j]-start[j]+0.0;
				System.out.println(score1+" "+score2);
				
				//results[i][j]=(float)(0.5*(score1+score2));	
				results[i][j]=(float)(Math.max(score1,score2));			
			
			}
		}
		return results;
	}
	
	
	
	public float[][] compareSongsAsymm(float[][] scores, Song[] songs, boolean[][] compScheme){

		
		int songNumber=songs.length;
		LinkedList songlocs=new LinkedList();
		int count=0;
		for (int i=0; i<songNumber; i++){
			int p=songs[i].phrases.size();
			int[]r={count, count+p};
			songlocs.add(r);
			count+=p;
		}

		
		float[][] scoreSong=new float[songNumber][];
		for (int i=0; i<songNumber; i++){
			scoreSong[i]=new float[i+1];
		}
		
		for (int i=0; i<songNumber; i++){
			int[] p1=(int[])songlocs.get(i);
			for (int j=0; j<i; j++){
				if ((compScheme[i][j]==false)&&(compScheme[j][i]==false)){
					scoreSong[i][j]=-1;
				}
				else{
					System.out.println(songs[i].name+" "+songs[j].name);
					int[]p2=(int[])songlocs.get(j);
				
					int[]q1=p1;
					int[]q2=p2;
					
					if (q1[1]-q1[0]>q2[1]-q2[0]){
						q1=p2;
						q2=p1;
					}
					
					double x=0;
					double y=0;
					for (int g=q1[0]; g<q1[1]; g++){
						double bestscore=100000000;
						for (int h=q2[0]; h<q2[1]; h++){
							if (g>h){
								if (scores[g][h]<bestscore){bestscore=scores[g][h];}
							}
							else{
								if (scores[h][g]<bestscore){bestscore=scores[h][g];}
							}
						}
						x+=bestscore;
						y++;
					}
					scoreSong[i][j]+=x/y;
					System.out.println(x+" "+y+" "+scoreSong[i][j]);
				}
			}
		}
		return scoreSong;
	}

	
	public float[][] compareSongsDigram(float[][] scores, Song[] songs, boolean useTrans){

		
		int songNumber=songs.length;
		LinkedList songlocs=new LinkedList();
		int count=0;
		int adj=-1;
		if (!useTrans){adj=0;}
		for (int i=0; i<songNumber; i++){
			int p=songs[i].phrases.size();
			
			int[]r={count, count+p+adj};
			songlocs.add(r);
			count+=p;
		}

		
		float[][] scoreSong=new float[songNumber][songNumber];
		float[][] results=new float[songNumber][];
		for (int i=0; i<songNumber; i++){
			results[i]=new float[i+1];
		}
		
		for (int i=0; i<songNumber; i++){
			int[] p1=(int[])songlocs.get(i);
			for (int j=0; j<songNumber; j++){
				int[]p2=(int[])songlocs.get(j);
				
				double x=0;
				double y=0;
				double score=0;
				double songMax=0;
				for (int g=p1[0]; g<p1[1]; g++){
					double bestscore=100000000;
					for (int h=p2[0]; h<p2[1]; h++){
						if (g>=h){
							if (useTrans){
								//score=Math.max(scores[g][h],scores[g+1][h+1]);
								score=scores[g][h]+scores[g+1][h+1];
							}
							else{
								score=scores[g][h];
							}
						}
						else{
							if (useTrans){
								//score=Math.max(scores[h][g],scores[h+1][g+1]);
								score=scores[h][g]+scores[h+1][g+1];
							}
							else{
								score=scores[h][g];
							}
						}
						if (score<bestscore){bestscore=score;}
					}
					x+=bestscore;
					y++;
					
					if (bestscore>songMax){songMax=bestscore;}

				}
				scoreSong[i][j]=(float)(x/y);
			}
		}
		
		for (int i=0; i<songNumber; i++){
			for (int j=0; j<i; j++){
				results[i][j]=Math.max(scoreSong[i][j], scoreSong[j][i]);
			}
		}
		
		return results;
	}

	public  float[][] compareSongsDTW(float[][] scores, Song[] songs){
				
		int songNumber=songs.length;
		//LinkedList songlocs=new LinkedList();
		int[][] songlocs=new int[songNumber][];
		int count=0;
		for (int i=0; i<songNumber; i++){
			int p=songs[i].phrases.size();
			int[]r=new int[p];
			for (int j=0; j<p; j++){
				r[j]=count;
				count++;
			}
			//int[]r={count, count+p};
			songlocs[i]=r;
			//count+=p;
			//System.out.println(r[0]+" "+ r[1]+" "+ scores.length);
		}
		
		double tot=0;
		double minScore=1000000;
		for (int i=0; i<scores.length; i++){
			for (int j=0; j<i; j++){
				tot+=scores[i][j];
				if (scores[i][j]<minScore){minScore=scores[i][j];}
			}
		}
		tot/=scores.length*(scores.length-1)*0.5;
		float penalty=(float)(tot*0.25);
		
		System.out.println("Mismatch penalty: "+tot+" "+minScore);
		float[][] results=new float[songNumber][];
		for (int i=0; i<songNumber; i++){
			results[i]=new float[i+1];
		}
		
		double[][] dtw;
		double bestscore;
		for (int i=0; i<songNumber; i++){
			int[] r1=songlocs[i];
			int r1l=r1.length;
			for (int j=0; j<i; j++){
				int[]r2=songlocs[j];
				int r2l=r2.length;
				dtw=new double[r1l][r2l];
				for (int a=0; a<r1l; a++){
					for (int b=0; b<r2l; b++){
						dtw[a][b]=scores[r1[a]][r2[b]];
						
						//dtw[a][b]=scores[r1[a]][r2[b]]*scores[r1[a]][r2[b]];
						//if (dtw[a][b]>penalty){dtw[a][b]=penalty;}
						
						bestscore=1000000;
						if (a>0){
							bestscore=dtw[a-1][b]+penalty;
						}
						if ((b>0)&&(dtw[a][b-1]+penalty<bestscore)){
							bestscore=dtw[a][b-1]+penalty;
						}
						if ((b>0)&&(a>0)&&(dtw[a-1][b-1]<bestscore)){
							bestscore=dtw[a-1][b-1];
						}
						if (bestscore<1000000){
							dtw[a][b]=bestscore+dtw[a][b];
						}
					}
				}
				//double d=Math.sqrt(dtw[r1l-1][r2l-1]);
				double d=dtw[r1l-1][r2l-1];
				//results[i][j]=(float)(d/(0.0+Math.min(r1l,r2l)));
				results[i][j]=(float)(d/(0.0+Math.max(r1l,r2l)));
				//results[i][j]=(float)(d/(0.5*(r1l+r2l)));
				
			}
				
		}
		
		return results;
		
		
	}


}
