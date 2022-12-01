package lusc.net.github.analysis;
//
//  CompressComparisons.java
//  Luscinia
//
//  Created by Robert Lachlan on 10/14/06.
//  Copyright 2006 __MyCompanyName__. All rights reserved.
//

import java.util.*;

import lusc.net.github.Element;
import lusc.net.github.Song;
import lusc.net.github.Syllable;
import lusc.net.github.ui.ComparisonScheme;

public class CompressComparisons {
	
	double syntaxPenalty=0.0;
	
	long l4=0l;
	
	public CompressComparisons(){
	
	
	}
	
	public int getBestOffset(int[] els1, int[] els2, double[][] scores){
		
		double[][] submat=new double[els1.length][els2.length];
		
		for (int i=0; i<els1.length; i++){
			for (int j=0; j<els2.length; j++){
				if (els1[i]<els2[j]){
					submat[i][j]=scores[els2[j]][els1[i]];
				}
				else{
					submat[i][j]=scores[els1[i]][els2[j]];
				}
			}
		}
		
		int loc=0;
		double bestsc=Double.MAX_VALUE;
		for (int i=0; i<els2.length-els1.length; i++){
			double sc=0;
			for (int j=0; j<els1.length; j++){
				sc+=submat[j][i+j];
			}
			if (sc<bestsc){
				bestsc=sc;
				loc=i;
			}
		}
		return loc;
	}
	
	public double[][] compareElements(double[][] scores, Song[] songs){
		
		LinkedList<Syllable>phrases=new LinkedList<Syllable>();
		int ele_count=0;
		for (int i=0; i<songs.length; i++){
			LinkedList<Syllable> phr=songs[i].getPhrases();
			for (Syllable sy:phr){
				phrases.add(sy);
				ele_count+=sy.getMaxSyllLength();
				for (Syllable s:sy.getSyllables()){
					if (s.getNumEles()<sy.getMaxSyllLength()){
						int[] els=s.getEleIds2();
						double p=0;
						for (Syllable t:sy.getSyllables()){
							int[] els2=s.getEleIds2();
							p+=getBestOffset(els, els2, scores);
							
						}
						s.setOffset((int)Math.round(p/(sy.getNumSyllables()+0.0)));
						
					}
				}
			}
		}
		
		double[][]scoreEle=new double[ele_count][];
		for (int i=0; i<ele_count; i++){
			scoreEle[i]=new double[i+1];
		}
		
		
		int p1=0;
		for (int i=0; i<phrases.size(); i++){
			Syllable ph1=phrases.get(i);
			
			for (int j=0; j<ph1.getMaxSyllLength(); j++){
				int p2=0;
				for (int g=0; g<=i; g++){
					Syllable ph2=phrases.get(i);
					
					for (int h=0; h<ph2.getMaxSyllLength(); h++){
						if (p2<p1){scoreEle[p1][p2]=getOScore(ph1, ph2, j, h, scores);}
						p2++;
					}	
				}
				p1++;	
			}	
		}
		return scoreEle;
	}
	
	
	public double getOScore(Syllable p1, Syllable p2, int a, int b, double[][] d){
		LinkedList<Syllable> syls1=p1.getSyllables();
		LinkedList<Syllable> syls2=p2.getSyllables();
		
		double sc=0;
		double count=0;
		
		for (Syllable s1: syls1){
			
			if (a+s1.getOffset()<s1.getNumEles2()){
				
				int x=s1.getEleId2(a+s1.getOffset());
				
				for (Syllable s2: syls2){
					if (b+s2.getOffset()<s2.getNumEles2()){
						int y=s2.getEleId2(b+s2.getOffset());
						count++;
						if (x>y){
							sc+=d[x][y];
						}
						else{
							sc+=d[y][x];
						}	
					}
				}
			}
		}
		sc/=count;
		
		return sc;
	}
		
		
	/*	
		int aa=songs.length;
		LinkedList phrases=new LinkedList();
		int prev_song_eles=0;
		for (int i=0; i<aa; i++){
			for (int j=0; j<songs[i].getNumPhrases(); j++){
				int[][] q=(int[][])songs[i].getPhrase(j);
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
			prev_song_eles+=songs[i].getNumElements();
		}
		
		int ele_count=0;
		for (int i=0; i<phrases.size(); i++){
			int[][] p=(int[][])phrases.get(i);
			ele_count+=p[0].length;
		}

		double[][]scoreEle=new double[ele_count][];
		for (int i=0; i<ele_count; i++){
			scoreEle[i]=new double[i+1];
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
							scoreEle[place1+g][place2+h]=(bscore/bcount);
						}
					}
				}
				place2+=p2[0].length;
			}
			place1+=p1[0].length;
		}
		return scoreEle;
	}
	/*
	public float[][] compareSyllables3(float[][] scores, Song[] songs){
		int aa=songs.length;
		LinkedList phrases=new LinkedList();
		int prev_song_eles=0;
		for (int i=0; i<aa; i++){
			for (int j=0; j<songs[i].getNumPhrases(); j++){
				int[][] q=(int[][])songs[i].getPhrase(j);
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
			prev_song_eles+=songs[i].getNumElements();
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
			for (int j=0; j<songs[i].getNumElements(); j++){
				Element ele=(Element)songs[i].getElement(j);
				lengths[jj]=Math.log(ele.getLength());
				jj++;
			}
			for (int j=0; j<songs[i].getNumPhrases(); j++){
				int[][] q=(int[][])songs[i].getPhrase(j);
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
			prev_song_eles+=songs[i].getNumElements();
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
				*//*
				
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
	
	*/
	
	public double[][] penalizeLengthDifference(double[][] scores, Song[] songs, double sp){
		int aa=songs.length;
		
		double averageScore=0;
		double sum=0;
		
		for (int i=0; i<scores.length; i++){
			for (int j=0; j<i; j++){
				if (!Double.isNaN(scores[i][j])){
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
		double syntaxPenalty=(sp*averageScore);
		//System.out.println("Syntax penalty: "+sp+" "+averageScore+" "+sum+" "+syntaxPenalty);
		int [] syllLengths=new int[scores.length];
		int p=0;
		
		for (int i=0; i<aa; i++){
			LinkedList<Syllable> ph=songs[i].getPhrases();
			
			for (int j=0; j<ph.size(); j++){
				
				Syllable sy=ph.get(j);
				for (int g=0; g<sy.getNumSyllables(); g++){
					Syllable s=sy.getSyllable(g);
					syllLengths[p]=s.getNumEles2();
					p++;
				}	
			}
		}
		/*
		for (int i=0; i<aa; i++){
			for (int j=0; j<songs[i].getNumPhrases(); j++){
				int[][] q=(int[][])songs[i].getPhrase(j);
				
				for (int g=0; g<q.length; g++){
					int a=0;
					for (int h=0; h<q[g].length; h++){
						if (q[g][h]!=-1){
							a++;
						}
					}
					syllLengths[p]=a;
					System.out.println(songs[i].getName()+" "+j+" "+g+" "+a);
					p++;
				}
				
				
			}
		}
		*/
		for (int i=0; i<scores.length; i++){
			for (int j=0; j<i; j++){
				if (syllLengths[i]!=syllLengths[j]){
					scores[i][j]+=syntaxPenalty;
				}
			}
		}
		
		return scores;
	}
	
	
	/**
	 * This method generates a phrase x phrase dissimilarity matrix from syllable scores
	 * @param scores
	 * @param songs
	 * @param sp
	 * @return
	 */
	
	public double[][] phraseComp(double[][] scores, Song[] songs, double sp){
		int aa=songs.length;
		
		double averageScore=0;
		double sum=0;
		//long l1=System.nanoTime();
		for (int i=0; i<scores.length; i++){
			for (int j=0; j<i; j++){
				if (!Double.isNaN(scores[i][j])){
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
		double syntaxPenalty=(sp*averageScore);
		
		//System.out.println(averageScore+" "+syntaxPenalty);
		
		LinkedList<Syllable> sylls=new LinkedList<Syllable>();
		for (int i=0; i<aa; i++){
			LinkedList<Syllable> syl=songs[i].getBaseLevelSyllables();
			sylls.addAll(syl);
		}
		
		int n=sylls.size();
		//System.out.println("NUMBER OF SYLS 2: " +n);
		
		double[][] scoreSyll=new double[n][];
		
		for (int i=0; i<n; i++){
			scoreSyll[i]=new double[i+1];
		}
		
		int[][] p=new int[n][];
		for (int i=0; i<n; i++){
			Syllable syll=sylls.get(i);
			p[i]=syll.getEleIds2();
		}
		
		//long l2=System.nanoTime();
		//l4=0;
		for (int i=0; i<n; i++){
			int[] p1=p[i];
			
			//Syllable syll=sylls.get(i);
			//int[] p1=syll.getEleIds2();
			if (p1.length==0){
				//System.out.println(i);
			}
			
			for (int j=0; j<i; j++){
				int[] p2=p[j];
				//Syllable syllb=sylls.get(j);
				//int[] p2=syllb.getEleIds2();
				scoreSyll[i][j]=syllableComp(scores, p1[0], p1[p1.length-1]-p1[0]+1, p2[0], p2[p2.length-1]-p2[0]+1, syntaxPenalty);	
				//System.out.print(scoreSyll[i][j]+" ");
			}
			
			//System.out.println();
		}
		//long l3=System.nanoTime();
		
		//System.out.println("COMPRESSION TIMING: "+(l2-l1)+" "+(l3-l2)+" "+l4);
		
		return scoreSyll;
		
	}
	
	/**
	 * This method generates a phrase x phrase dissimilarity matrix from syllable scores
	 * @param scores
	 * @param songs
	 * @param sp
	 * @return
	 */
	/*
	public double[][] phraseCompX(double[][] scores, Song[] songs, double sp){
		
		int aa=songs.length;
		
		double averageScore=0;
		double sum=0;
		
		for (int i=0; i<scores.length; i++){
			for (int j=0; j<i; j++){
				if (!Double.isNaN(scores[i][j])){
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
		double syntaxPenalty=(sp*averageScore);
		//System.out.println("Syntax penalty: "+sp+" "+averageScore+" "+sum+" "+syntaxPenalty);
		LinkedList sylls=new LinkedList();
		int prev_song_eles=0;
		
		double[] lengths=new double[scores.length];
		int jj=0;
		
		int syllCount=0;
		
		for (int i=0; i<aa; i++){
			for (int j=0; j<songs[i].getNumPhrases(); j++){
				int[][] q=(int[][])songs[i].getPhrase(j);
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
			prev_song_eles+=songs[i].getNumElements();
		}
		
		double[][]scoreSyll=new double[syllCount][];
		for (int i=0; i<syllCount; i++){
			scoreSyll[i]=new double[i+1];
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
	*/
	
	public double[][] phraseComp2(double[][] scores, Song[] songs, double sp, LinkedList<int[][]> details){
		
		int aa=songs.length;
		
		double averageScore=0;
		double sum=0;
		
		for (int i=0; i<scores.length; i++){
			for (int j=0; j<i; j++){
				if (!Double.isNaN(scores[i][j])){
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
		double syntaxPenalty=(sp*averageScore);	
		
		
		LinkedList<int[]> sylls=new LinkedList<int[]>();
		
		for (int i=0; i<details.size(); i++){
			int[][] x1=details.get(i);
			int[] r=new int[2];
			r[0]=i;
			r[1]=1;
			for (int j=i+1; j<details.size(); j++){
				int[][] x2=details.get(j);
				if (x1[0][3]!=x2[0][3]-1){
					i=j-1;
					j=details.size();
				}
				else{
					r[1]++;
				}
			}
			//System.out.println(i+" "+r[0]+" "+r[1]);
			sylls.add(r);
		}
		
		
		int syllCount=sylls.size();
		
		//System.out.println(syllCount+" "+aa+" "+details.size());
		
		
		double[][]scoreSyll=new double[syllCount][];
		for (int i=0; i<syllCount; i++){
			scoreSyll[i]=new double[i+1];
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
	
	
	
	
	
	
	public double syllableComp(double[][] eleScores, int a, int n, int b, int m, double syntaxPenalty){
		//long l1=System.nanoTime();
		// make sure n>m
		
		//double exp=2;
		double q=0;
		if (n<m){
			int x=a;
			int y=n;
			a=b;
			n=m;
			b=x;
			m=y;
		}
		
		//System.out.println(a+" "+n+" "+b+" "+m);
		
		double bestScore=1000000;
		
		for (int i=0; i<m; i++){
			double score=0;
			//double worst=0;
			int k=i;
			for (int j=0; j<n; j++){
				
				int x=j+a;
				int y=k+b;
				
				if (x>y){
					q=eleScores[x][y];
					//score+=Math.pow(eleScores[x][y], exp);
					//if (eleScores[x][y]>worst){worst=eleScores[x][y];}
					//score+=eleScores[x][y];
				}
				else{
					q=eleScores[y][x];
					//score+=Math.pow(eleScores[y][x], exp);
					//if (eleScores[y][x]>worst){worst=eleScores[y][x];}
					//score+=eleScores[y][x];
				}
				score+=q*q;
				k++;
				if (k==m){
					k=0;
				}
			}

			score/=n+0.0;
			//score=Math.pow(score, 1/exp);
			score=Math.sqrt(score);
			//score=worst;
			
			if (n!=m){score+=syntaxPenalty;}
			if (i!=0){score+=syntaxPenalty;}
			if (score<bestScore){bestScore=score;}
		}
		
		//long l2=System.nanoTime();
		
		//l4+=l2-l1;
		
		return bestScore;		
	}
	
	/**
	 * This method takes stitched syllable dissimilarities and outputs a phrase dissimilarity matrix
	 * @param syScores
	 * @param songs
	 * @param synco
	 * @return
	 */
	
	
	public double[][] compareSyllables5(double[][] syScores, Song[] songs, double synco){
	
		if (synco>0){
			syScores=penalizeLengthDifference(syScores, songs, synco);
		}
		
		//System.out.println("Syls: "+syScores.length);
		
		int[] phraseID=new int[syScores.length];
		
		int phraseCount=0;
		int syllCount=0;
		
		//long l1=System.nanoTime();
		
		for (int i=0; i<songs.length; i++){
			LinkedList<Syllable> syl=songs[i].getPhrases();
			phraseCount+=syl.size();
		}
		
		int[][] ph=new int[phraseCount][];
		
		phraseCount=0;
		int maxLength=0;
		for (int i=0; i<songs.length; i++){
			
			//System.out.println(i+" "+songs[i].getNumPhrases()+" "+syScores.length+" "+syllCount+" "+songs.length);
			LinkedList<Syllable> syl=songs[i].getPhrases();
			for (int j=0; j<syl.size(); j++){
				Syllable sy=syl.get(j);
				int ns=sy.getNumSyllables();
				int[] x=new int[ns];
				if (ns>maxLength) {maxLength=ns;}
				for (int k=0; k<ns; k++){
					phraseID[syllCount]=phraseCount;
					x[k]=syllCount;			
					syllCount++;
				}
				
				ph[phraseCount]=x;
				
				phraseCount++;
			}
		}
		
		//int[] counter=new int[phraseCount];
		//for (int i=0; i<syScores.length; i++){
		//	counter[phraseID[i]]++;
		//}
	
		//long l2=System.nanoTime();
		
		double[][] results=new double[phraseCount][];
		//double[][] count=new double[phraseCount][];
		/*
		for (int i=0; i<results.length; i++){
			results[i]=new double[i+1];
			int c1=counter[i];		
			for (int j=0; j<i; j++){
				int c2=counter[j];
				double[][] submat=new double[c1][c2];
				int a=0;
				int b=0;
				for (int g=0; g<syScores.length; g++){
					if (phraseID[g]==i){
						b=0;
						for (int h=0; h<syScores.length; h++){
							if (phraseID[h]==j){
								if (g>h){
									submat[a][b]=syScores[g][h];
								}
								else{
									submat[a][b]=syScores[h][g];
								}
								b++;
							}
						}
						a++;
					}	
				}
				results[i][j]=median(submat);
				
			}
		}
		*/
		
		/*
		long l6=0;
		long l7=0;
		for (int i=0; i<results.length; i++){
			results[i]=new double[i+1];
			int c1=counter[i];		
			for (int j=0; j<i; j++){
				long l3=System.nanoTime();
				int c2=counter[j];
				double[] submat=new double[c1*c2];
				int a=0;
				
				for (int g=0; g<syScores.length; g++){
					if (phraseID[g]==i){
						for (int h=0; h<syScores.length; h++){
							if (phraseID[h]==j){
								if (g>h){
									submat[a]=syScores[g][h];
								}
								else{
									submat[a]=syScores[h][g];
								}
								a++;
							}
						}
					}	
				}
				long l4=System.nanoTime();
				results[i][j]=median(submat);
				long l5=System.nanoTime();
				l6+=l4-l3;
				l7+=l5-l4;
				
			}
		}
		
		*/

		
		//long l6=0;
		//long l7=0;
		
		int gg, hh;
		double[] submat=new double[maxLength*maxLength];
		for (int i=0; i<results.length; i++){
			results[i]=new double[i+1];
			for (int j=0; j<i; j++){
				//System.out.println(i+" "+j);
				//long l3=System.nanoTime();				
				int a=0;
				for (int g=0; g<ph[i].length; g++){
					gg=ph[i][g];
					for (int h=0; h<ph[j].length; h++){
						hh=ph[j][h];
						if (gg>hh){
							submat[a]=syScores[gg][hh];
						}
						else{
							submat[a]=syScores[hh][gg];
						}
						a++;
					}	
				}
				//long l4=System.nanoTime();
				results[i][j]=median(submat, a);
				//long l5=System.nanoTime();
				//l6+=l4-l3;
				//l7+=l5-l4;
				
			}
		}
		
		//System.out.println((l2-l1)+" "+l6+" "+l7);
		
		//count=null;
		
		return results;
	}
	
	public double DTW(double[][] input){
		int n=input.length;
		int m=input[0].length;
		
		double[][] temp=new double[n][m];
		
		int[] x1={-1, 0, -1};
		int[] y1={-1, -1, 0};
		
		for (int i=0; i<n; i++){
			for (int j=0; j<m; j++){
				
				temp[i][j]=input[i][j];
				
				double min=10000000;
				for (int k=0; k<3; k++){
					int a=i+x1[k];
					int b=j+y1[k];
					if ((a>=0)&&(b>=0)){
						if (temp[a][b]<min){
							min=temp[a][b];
						}
					}
				}
				if (min<10000000){
					temp[i][j]+=min;
				}	
			}
		}
		
		double p=Math.max(n, m);
		
		return(temp[n-1][m-1]/p);
		
	}
	
	public double median(double[][] input){
		int n=input.length;
		int m=input[0].length;
		
		double[] temp=new double[n*m];
		
		int a=0;
		
		for (int i=0; i<n; i++){
			for (int j=0; j<m; j++){
				
				temp[a]=input[i][j];
				a++;
			}
		}
				
		Arrays.sort(temp);
	
		double median;
		if (temp.length % 2 == 0)
		    median = ((double)temp[temp.length/2] + (double)temp[temp.length/2 - 1])/2;
		else
		    median = (double) temp[temp.length/2];
		
		return(median);
		
	}
	
	public double median(double[] input){
		int n=input.length;
				
		Arrays.sort(input);	
		double median;
		if (n % 2 == 0)
		    median = ((double)input[n/2] + (double)input[n/2 - 1])/2;
		else
		    median = (double) input[n/2];
		
		return(median);		
	}
	
	public double median(double[] input, int n){
		//int n=input.length;
		double x=0;
		double y=0;
		int a=n/2;
		int b=n/2 -1;
		int c=0;
		for (int i=0; i<n; i++) {
			//System.out.println(i+" "+input[i]);
			c=0;
			for (int j=0; j<n; j++) {
				if (input[i]<input[j]) {
					c++;
				}
			}
			if (c==a) {
				x=input[i];
			}
			if (c==b) {
				y=input[i];
			}
		}
		
		double median=x;
		if (n % 2 == 0) {
			median=0.5*(x+y);
		}
		//System.out.println(median+" "+n+" "+x+" "+y+" "+a+" "+b);
		
		return(median);		
	}
	
	/*
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
			for (int j=0; j<songs[i].getNumPhrases(); j++){
				int[][] q=(int[][])songs[i].getPhrase(j);
				
				double[][] s=new double[q[0].length][2];
				
				
				for (int g=0; g<q.length; g++){
					for (int h=0; h<q[g].length; h++){
						if (q[g][h]!=-1){
							Element ele=(Element)songs[i].getElement(q[g][h]);
							if (ele.getTimeAfter()>-10000){
								s[h][0]+=ele.getTimeAfter();
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
			prev_song_eles+=songs[i].getNumElements();
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
			for (int j=0; j<songs[i].getNumPhrases(); j++){
				int[][] q=(int[][])songs[i].getPhrase(j);
				
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
			int p=songs[i].getNumPhrases();
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
	
	*/
	
	public double[][] compareSyllableTransitions2(double[][] scores, Song[] songs){
		//calculates Transitions without end states
		
		int songNumber=songs.length;
		int sylNumber=scores.length-songNumber;		//For each song, there are n-1 transitions, where n=number of phrases!
		//System.out.println(sylNumber+" "+songNumber+" "+scores.length);
		int[] songLocs=new int[sylNumber];
		int count=0;
		int count2=0;
		for (int i=0; i<songNumber; i++){
			int p=songs[i].getNumSyllables(2);
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
		//System.out.println(sylNumber+" "+scores.length);
		double[][] scoreTrans=new double[sylNumber][];
		for (int i=0; i<sylNumber; i++){
			scoreTrans[i]=new double[i+1];
		}
		
		for (int i=0; i<sylNumber; i++){
			for (int j=0; j<i; j++){
				int i1=songLocs[i];
				int j1=songLocs[j];
				int i2=songLocs[i]+1;
				int j2=songLocs[j]+1;
				//scoreTrans[i][j]=(double)Math.sqrt(scores[i1][j1]*scores[i1][j1]+scores[i2][j2]*scores[i2][j2]);
				scoreTrans[i][j]=(double)Math.sqrt(scores[i1][j1]*scores[i2][j2]);
				//scoreTrans[i][j]=Math.max(scores[i1][j1], scores[i2][j2]);
				//scoreTrans[i][j]=scores[i1][j1]+scores[i2][j2];
				//scoreTrans[i][j]*=0.5f;
			}
		}
		songLocs=null;
		return scoreTrans;
	}

	/*
	public double[][] compareSongs(float[][] scores, Song[] songs){

		
		int songNumber=songs.length;
		LinkedList songlocs=new LinkedList();
		int count=0;
		for (int i=0; i<songNumber; i++){
			int p=songs[i].getNumPhrases();
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
			int p=songs[i].getNumPhrases();
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
					System.out.println(songs[i].getName()+" "+songs[j].getName());
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
	
	*/

	
	public double[][] compareSongsDigram(double[][] scores, Song[] songs, boolean useTrans, boolean cycle){

		
		int songNumber=songs.length;
		LinkedList songlocs=new LinkedList();
		int count=0;
		int adj=-1;
		if (!useTrans){adj=0;}
		for (int i=0; i<songNumber; i++){
			int p=songs[i].getNumSyllables(2);
			
			int[]r={count, count+p+adj};
			songlocs.add(r);
			count+=p;
		}

		
		double[][] scoreSong=new double[songNumber][songNumber];
		double[][] results=new double[songNumber][];
		for (int i=0; i<songNumber; i++){
			results[i]=new double[i+1];
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
				scoreSong[i][j]=(x/y);
			}
		}
		
		for (int i=0; i<songNumber; i++){
			for (int j=0; j<i; j++){
				results[i][j]=Math.max(scoreSong[i][j], scoreSong[j][i]);
			}
		}
		
		return results;
	}

	public double[][] compareSongsDigramOld(ComparisonResults cs, boolean useTrans, boolean cycle, double min, double max){

		int[][] id=cs.calculateSongs();
		
		int songNumber=id.length;
		double[][] scores=cs.getDiss();
		double[][] scoreSong=new double[songNumber][songNumber];
		double[][] results=new double[songNumber][];
		for (int i=0; i<songNumber; i++){
			results[i]=new double[i+1];
		}
		
		for (int i=0; i<songNumber; i++){
			int[] p1=id[i];
			for (int j=0; j<songNumber; j++){
				int[]p2=id[j];
				
				double x=0;
				double y=0;
				double score=0;
				double score2=0;
				double songMax=0;
				
				int p1l=p1.length;
				if ((useTrans)&&(!cycle)){
					p1l--;
				}
				
				int p2l=p2.length;
				if ((useTrans)&&(!cycle)){
					p2l--;
				}
				
				for (int g=0; g<p1l; g++){
					double bestscore=100000000;
					
					for (int h=0; h<p2l; h++){
						if (p1[g]>=p2[h]){
							score=scores[p1[g]][p2[h]];
							if (score<min){score=min;}
							if (score>max){score=max;}
							if (useTrans){
								int g2=g+1;
								int h2=h+1;
								if (cycle){
									if (g==p1.length-1){
										g2=0;
									}
									if (h==p2.length-1){
										h2=0;
									}
								}
								score2=scores[p1[g2]][p2[h2]];
								if (score2<min){score2=min;}
								if (score2>max){score2=max;}
								score+=score2;
							}
						}
						else{
							score=scores[p2[h]][p1[g]];
							if (score<min){score=min;}
							if (score>max){score=max;}
							if (useTrans){
								int g2=g+1;
								int h2=h+1;
								if (cycle){
									if (g==p1.length-1){
										g2=0;
									}
									if (h==p2.length-1){
										h2=0;
									}
								}
								score2=scores[p2[h2]][p1[g2]];
								if (score2<min){score2=min;}
								if (score2>max){score2=max;}
								score+=score2;
							}
						}
						if (score<bestscore){bestscore=score;}
					}
					x+=bestscore;
					y++;
					
					if (bestscore>songMax){songMax=bestscore;}

				}
				scoreSong[i][j]=(x/y);
			}
		}
		
		for (int i=0; i<songNumber; i++){
			for (int j=0; j<i; j++){
				results[i][j]=Math.max(scoreSong[i][j], scoreSong[j][i]);
			}
		}
		
		return results;
	}
	
	public double[][] compareSongsDigram(ComparisonResults cs, boolean useTrans, boolean cycle, boolean lt, double min, double max){

		System.out.println("Compressing songs digram: "+useTrans+" "+cycle+" "+lt);
		int[][] id=cs.calculateSongs();
		
		double minposs=cs.calculateMin();
		
		int songNumber=id.length;
		double[][] scores=cs.getDiss();
		double[][] scoreSong=new double[songNumber][songNumber];
		double[][] results=new double[songNumber][];
		for (int i=0; i<songNumber; i++){
			results[i]=new double[i+1];
		}
		
		boolean missLast=false;
		if ((useTrans)&&(!cycle)){
			missLast=true;
		}
		
		for (int i=0; i<songNumber; i++){
			int[] p1=id[i];
			for (int j=0; j<songNumber; j++){
				int[]p2=id[j];			
				
				double[][] sm=getSubMatrix(scores, p1, p2, 0, useTrans, missLast, lt, min, max, minposs);
				
				double[] comp=getBestMatches(sm);
				scoreSong[i][j]=getAverage(comp);
				if (lt){scoreSong[i][j]=Math.exp(scoreSong[i][j])*minposs;}
				
			}
		}
		
		for (int i=0; i<songNumber; i++){
			for (int j=0; j<i; j++){
				results[i][j]=Math.max(scoreSong[i][j], scoreSong[j][i]);
			}
		}
		
		return results;
	}

	
	
	
	public  double[][] compareSongsDTW(double[][] scores, Song[] songs){
				
		int songNumber=songs.length;
		//LinkedList songlocs=new LinkedList();
		int[][] songlocs=new int[songNumber][];
		int count=0;
		for (int i=0; i<songNumber; i++){
			int p=songs[i].getNumSyllables(2);
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
		double penalty=(tot*0.25);
		
		//System.out.println("Mismatch penalty: "+tot+" "+minScore);
		double[][] results=new double[songNumber][];
		for (int i=0; i<songNumber; i++){
			results[i]=new double[i+1];
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
						//dtw[a][b]=scores[r1[a]][r2[b]];
						
						dtw[a][b]=scores[r1[a]][r2[b]]*scores[r1[a]][r2[b]];
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
				//results[i][j]=(double)(d/(0.0+Math.min(r1l,r2l)));
				//results[i][j]=(d/(0.0+Math.min(r1l,r2l)));
				results[i][j]=Math.sqrt(d/(0.0+Math.min(r1l,r2l)));
				//results[i][j]=(float)(d/(0.5*(r1l+r2l)));
				
			}
				
		}
		
		return results;
		
		
	}
	
	public  double[][] compareSongsDTWOld(ComparisonResults cr, boolean cycle, double min, double max){
		
		int[][] id=cr.calculateSongs();	
		int songNumber=id.length;
		double[][] scores=cr.getDiss();		
		/*
		double tot=0;
		double minScore=1000000;
		for (int i=0; i<scores.length; i++){
			for (int j=0; j<i; j++){
				tot+=scores[i][j];
				if (scores[i][j]<minScore){minScore=scores[i][j];}
			}
		}
		tot/=scores.length*(scores.length-1)*0.5;
		double penalty=(tot*0.25);
		penalty=0.0;
		*/
		double penalty=cr.calculatePercentile(5);
		
		
		//System.out.println("Mismatch penalty: "+tot+" "+minScore);
		
		double[][] results=new double[songNumber][];
		for (int i=0; i<songNumber; i++){
			results[i]=new double[i+1];
		}
		
		double[][] dtw;
		double bestscore;
		for (int i=0; i<songNumber; i++){
			int[] r1=id[i];
			int r1l=r1.length;
			int r3=1;
			if (cycle){r3=r1l;}
			for (int j=0; j<i; j++){
				int[]r2=id[j];
				int r2l=r2.length;
				int r4=1;
				if (cycle){r4=r2l;}
				dtw=new double[r1l][r2l];
				double oscore=100000000;
				for (int c=0; c<r3; c++){
					for (int a=0; a<r1l; a++){
						int aa=a+c;
						if (aa>=r1l){aa-=r1l;}
						for (int d=0; d<r4; d++){
						
						
							for (int b=0; b<r2l; b++){
								int bb=b+d;
								if (bb>=r2l){bb-=r2l;}
							
								if (r1[aa]>=r2[bb]){
									dtw[a][b]=scores[r1[aa]][r2[bb]];
								}
								else{
									dtw[a][b]=scores[r2[bb]][r1[aa]];	
								}
							
								if (dtw[a][b]<min){dtw[a][b]=min;}
								if (dtw[a][b]>max){dtw[a][b]=max;}
							
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
									//if (bestscore>max){bestscore=max;}
									dtw[a][b]=bestscore+dtw[a][b];
								}
							}
						}
					}
					double d=dtw[r1l-1][r2l-1];
					if (d<oscore){
						oscore=d;
					}
				}
				//double d=Math.sqrt(dtw[r1l-1][r2l-1]);
				
				//results[i][j]=(double)(d/(0.0+Math.min(r1l,r2l)));
				results[i][j]=(oscore/(0.0+Math.max(r1l,r2l)));
				//results[i][j]=(oscore/(0.5*(r1l+r2l)));
				//results[i][j]=(float)(d/(0.5*(r1l+r2l)));
				
			}
				
		}
		
		return results;	
	}
	
	public  double[][] compareSongsDTW(ComparisonResults cr, boolean cycle, boolean useTrans, boolean lt, boolean linearity, double min, double max){
		
		int[][] id=cr.calculateSongs();	
		int songNumber=id.length;
		double[][] scores=cr.getDiss();		
		
		
		double minposs=cr.calculateMin();
		
		double penalty=cr.calculatePercentile(25);
		
		if (lt){penalty=Math.log(penalty/minposs);}
		
		double[][] results=new double[songNumber][];
		for (int i=0; i<songNumber; i++){
			results[i]=new double[i+1];
		}
		
		boolean missLast=false;
		if ((useTrans)&&(!cycle)){
			missLast=true;
		}
		
		int mode=0;
		if (linearity){mode=2;}
		
		for (int i=0; i<songNumber; i++){
			int[] r1=id[i];
			int rx=r1.length;
			if (!cycle){
				rx=1;
			}
			
			for (int j=0; j<i; j++){
				int[]r2=id[j];
				double bestScore=Double.MAX_VALUE;				
				
				for (int k=0; k<rx; k++){
					
					double[][] sm=getSubMatrix(scores, r1, r2, k, useTrans, missLast, lt, min, max, minposs);
					
					double[] s=getDTWalignment(sm, mode, penalty);					
					double o=getSum(s);
					if (o<bestScore){bestScore=o;}
				}
				
				results[i][j]=(bestScore/(0.0+Math.max(r1.length,r2.length)));
				if ((lt)&&(!linearity)){results[i][j]=Math.exp(results[i][j])*minposs;}
			}
				
		}
		
		return results;	
	}
	
	

	
	public double[][] compareIndividuals(double[][] songDiffs, int[][] indIds, boolean av){
		
		
		
		int n=indIds.length;
		int m=songDiffs.length;
				
		double[][] temp=new double[m][m];
		
		
		double[][] out = new double[n][];
		for (int i=0; i<m; i++){
			for (int j=0; j<=i; j++){
				temp[i][j]=songDiffs[i][j];
				temp[j][i]=songDiffs[i][j];
			}
		}
		for (int i=0; i<n; i++){
			out[i]=new double[i+1];
		}
		
		for (int i=0; i<n; i++){
			for (int j=0; j<=i; j++){
				//if (i!=j){
					double u=0;
					double u2=0;
					for (int a=0; a<indIds[i].length; a++){
						double sc=1000000000;
						double sc2=0;
						for (int b=0; b<indIds[j].length; b++){
							double t=temp[indIds[i][a]][indIds[j][b]];
							if (t<sc){sc=t;}
							sc2+=t;
						}
						u+=sc;
						u2+=sc2/(indIds[j].length+0.0);
					}
					if (i==j){
						u/=indIds[i].length-1.0;
						if (u==0){u=1;}
						u=0;
					}
					else{
						u/=indIds[i].length-0.0;
					}
					//u/=indIds[i].length+0.0;
					u2/=indIds[i].length+0.0;
					double v=0;
					double v2=0;
					for (int a=0; a<indIds[j].length; a++){
						double sc=1000000000;
						double sc2=0;
						for (int b=0; b<indIds[i].length; b++){
							double t=temp[indIds[j][a]][indIds[i][b]];
							if (t<sc){sc=t;}
							sc2+=t;
						}
						v+=sc;
						v2+=sc2/(indIds[i].length+0.0);
					}
					if (i==j){
						v/=indIds[j].length-1.0;
						if (v==0){v=1;}
						v=0;
					}
					else{
						v/=indIds[j].length-0.0;
					}
					v2/=indIds[j].length+0.0;
					if (av){
						out[i][j]=u+v;
					}
					else{
						out[i][j]=u2+v2;
					}
					//System.out.println(i+" "+j+" "+out[i][j]);
					
				//}
			}
		}
		return out;
	}

	
	public double[] getDTWalignment(double[][] dtw, int mode, double penalty){
		
		int n=dtw.length; 
		int m=dtw[0].length;

		
		
		double[][] t=new double[n][m];
		int[][] dir=new int[n][m];
		
		int[] x={-1,0,-1};
		int[] y={0,-1,-1};
		double[] z={penalty, penalty, 0};
		//System.out.println("Syllable DTW penalty: "+penalty);
		for (int i=0; i<n; i++){
			for (int j=0; j<m; j++){
				t[i][j]=dtw[i][j];
				double bestS=Double.MAX_VALUE;
				int d=-1;
				for (int a=0; a<3; a++){
					int b=i+x[a];
					int c=j+y[a];
					if ((b>=0)&&(c>=0)){
						
						double q=t[b][c]+z[a];
						//System.out.print(a+" "+q+" "+bestS+" ");
						if (q<bestS){
							bestS=q;
							d=a;
						}
						//if(a==2){
							//System.out.println(i+" "+j+" "+b+" "+c+" "+q+" "+bestS);
						//}
					}			
				}
				if(d<0){
					//System.out.println(d+" "+i+" "+j);
				}
				if(d>=0){
					dir[i][j]=d;
					t[i][j]+=bestS;
					
				}
				//System.out.print((int)Math.round(1000*t[i][j])+" ");
				//System.out.print((int)Math.round(1000*dtw[i][j])+" ");
			}
			//System.out.println();
		}
		
		//for (int i=0; i<n; i++){
		//	for (int j=0; j<m; j++){
		//		System.out.print((int)Math.round(1000*dtw[i][j])+" ");
		//	}
		//	System.out.println();
		//}
		
		int a=n-1;
		int b=m-1;
		
		while ((a>0)||(b>0)){
			int c=dir[a][b];
			dir[a][b]+=10;
			a+=x[c];
			b+=y[c];
			//System.out.println(a+" "+b+" "+dir[a][b]);
		}
		dir[0][0]=12;
		double[] out=new double[n];
		for (int i=0; i<n; i++){		
			for (int j=0; j<m; j++){
				if (dir[i][j]>=10){
					if (mode==0){
						out[i]+=dtw[i][j];
					}
					else if (mode==1){
						if (dtw[i][j]>out[i]){
							out[i]=dtw[i][j];
						}
					}	
					else if (mode==2){
						if (dir[i][j]==12){
							out[i]=0;
						}
						else{
							out[i]+=1;
						}
					}
				}		
			}	
			//System.out.println("O: "+out[i]+" "+i+" "+n+" "+m);
		}
		
		return out;
	}
	
	public double[][] getSubMatrix(double[][] mat, int[] f1, int[] f2, int offset, boolean useTrans, boolean missLast, boolean lt, double min, double max, double minposs){
		
		//System.out.println("SubMat: "+offset+" "+useTrans+" "+missLast+" "+lt);
		int n=f1.length; 
		int m=f2.length;
		
		//for(int i=0; i<n; i++){System.out.print(f1[i]+" ");}
		//System.out.println();
		//for(int i=0; i<m; i++){System.out.print(f2[i]+" ");}
		//System.out.println();
		
		
		if ((useTrans)&&(missLast)){
			n--;
			m--;
		}
		double max2=max;
		double min2=min;
		if(max<=min){
			max2=1;
			if (!lt){min2=0;}
		}
		
		double[][] out=new double[n][m];		
		for (int i=0; i<n; i++){
			int k=i+offset;
			if (k>=n){
				k-=n;
			}
			for (int j=0; j<m; j++){
				double p=0;
				if (f1[k]>f2[j]){
					p=mat[f1[k]][f2[j]];
				}
				else{
					p=mat[f2[j]][f1[k]];
				}
				if (p<min){p=min2;}
				if (p>max){p=max2;}
				if (lt){
					p=Math.log(p/minposs);
				}
				out[i][j]=p;
				if (useTrans){
					int a=k+1;
					if (a>=n){a-=n;}
					int b=j+1;
					if (b>=m){b-=m;}
					if (f1[a]>f2[b]){
						p=mat[f1[a]][f2[b]];
					}
					else{
						p=mat[f2[b]][f1[a]];
					}
					if (p<min){p=min2;}
					if (p>max){p=max2;}
					if (lt){
						p=Math.log(p/minposs);
					}
					out[i][j]+=p;
					out[i][j]*=0.5;
				}	
			}
		}	
		return out;
	}
	
	
	public double[] getBestMatches(double[][] comp){
		
		int n=comp.length;
		int m=comp[0].length;
		
		double[] out=new double[n];
		for (int i=0; i<n; i++){
			
			double bestMatch=Double.MAX_VALUE;
			
			for (int j=0; j<m; j++){
				double score=comp[i][j];
				
				if (score<bestMatch){
					bestMatch=score;
				}	
				
			}
			out[i]=bestMatch;
		}

		return out;
	}
	
	
	public double getScore(double[][] comp, int a, int b){
		if (a>b){
			return comp[a][b];
		}
		else{
			return comp[b][a];
		}	
	}
	
	public double getAverage(double[] d){
		double n=d.length;
		double s=0;
		for (int i=0; i<n; i++){
			s+=d[i];
		}
		s/=n;
		
		return s;
	}
	
	public double getSum(double[] d){
		double n=d.length;
		double s=0;
		for (int i=0; i<n; i++){
			s+=d[i];
		}
		
		return s;
	}
	
	public double getEuclideanDistance(double[] d){
		double n=d.length;
		double s=0;
		for (int i=0; i<n; i++){
			s+=d[i]*d[i];
		}
		s/=n;
		s=Math.sqrt(s);
		
		return s;
	}
}
