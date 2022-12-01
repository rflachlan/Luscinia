package lusc.net.github.sound;

import java.util.Arrays;

/**
 * This class facilitates some matrix functions, especially on spectrograms.
 * It is called by {@link Song} in particular. Most of the functions here are not
 * currently being used, but may be useful in the future for automatic detection of elements
 * in a spectrogram, or similar purposes. Most functions are not highly optimized!
 * @author Rob
 *
 */
public class Matrix2DOperations {
	
	

	/**
	 * This is an earlier attempt at simple medianFilterNoiseRemoval
	 */
	void medianFilterNoiseRemoval(float[][] out){
		
		int kr=10;
		
		int x=out.length;
		int y=out[0].length;
		
		float[][] filtered=new float[x][y];
		
		float[] buffer=new float[(2*kr+1)*(2*kr+1)];
		
		int a1, a2, b1, b2, w;
		double summer=0;	
		for (int a=0; a<x; a++){
			a1=a-kr;
			if (a1<0){a1=0;}
			a2=a+kr;
			if (a2>=x){a2=x-1;}
			
			for (int b=0; b<y; b++){
				
				b1=b-kr;
				if (b1<0){b1=0;}
				b2=b+kr;
				if (b2>=y){b2=y-1;}
				
				w=0;
				for (int c=a1; c<=a2; c++){
					for (int d=b1; d<=b2; d++){
						buffer[w]=out[c][d];
						w++;
					}
				}
				if (w<=buffer.length){
					for (int c=w-1; c<buffer.length; c++){
						buffer[c]=0;
					}
				}
				Arrays.sort(buffer);
				filtered[a][b]=buffer[buffer.length/2];
				summer+=filtered[a][b];
			}
		}
		summer/=x*y*1.0;
		for (int i=0; i<x; i++){
			for (int j=0; j<y; j++){
				out[i][j]-=(float)(0.5*(filtered[i][j]-summer));
			}
		}
		//out=filtered;
	}	
	
	/**
	 * @param edges
	 */
	void findBlobs(float[][] edges, float[][] out){

		int x=out.length; 
		int y=out[0].length;
		float[][] out3=new float[x][y];
		int[] xd={-1, -1, -1, 0, 0, 1, 1, 1};
		int[] yd={-1, 0, 1, -1, 1, -1, 0, 1};
		
		for (int i=0; i<x; i++){
			for (int j=0; j<y; j++){
				float r=-1000f;
				float p=edges[i][j];
				float p1=out[i][j];
				for (int k=0; k<8; k++){
					
					for (int g=1; g<10; g++){
						int xq=i+g*xd[k];
						int yq=j+g*yd[k];
							
						if ((xq>=0)&&(yq>=0)&&(xq<x)&&(yq<y)){
							float q=edges[xq][yq];
							float q1=out[xq][yq];
							if (q1<0){q1=0;}
							xq=i-g*xd[k];
							yq=j-g*yd[k];
							if ((xq>=0)&&(yq>=0)&&(xq<x)&&(yq<y)){
								float s=edges[xq][yq];
								float s1=out[xq][yq];
								if (s1<0){s1=0;}
								q=Math.min(s,q)-p;
								
								q+=p1-Math.min(s1,q1);
								if(q>r){
									r=q;
								}
							}
						}
					}	
				}	
				out3[i][j]=0.5f*(r+out[i][j]);

			}
		}
		out=out3;	
	}
	
	/**
	 * Deprecated
	 * @return a float[][] spectrogram
	 */
	float[][] blobAccentuator2(float[][] out){
		
		//out=gaussianBlur(2);
		int x=out.length;
		int y=out[0].length;
		float[][] out2=new float[x][y];
		
		int[] xd={-1, -1, -1, 0, 0, 1, 1, 1};
		int[] yd={-1, 0, 1, -1, 1, -1, 0, 1};
		
		for (int i=0; i<x; i++){
			for (int j=0; j<y; j++){
				
				int xp=i;
				int yp=j;
				float p=out[i][j];
				float q=0;
				boolean cont=true;
				while (cont){
					cont=false;
					int kc=-1;
					for (int k=0; k<8; k++){
						int xq=xp+xd[k];
						int yq=yp+yd[k];
						if ((xq>=0)&&(yq>=0)&&(xq<x)&&(yq<y)&&(out[xq][yq]>p)){
							p=out[xq][yq];
							cont=true;
							kc=k;
						}
					}
					if (cont){
						float r=out[xp][yp]-p;
						if (r>q){q=r;}
						xp+=xd[kc];
						yp+=yd[kc];
					}
				}
				p-=q;
				if (p>10){p=10;}
				out2[i][j]=out[i][j]+p;	
			}
		}
		
		return out2;
	}
	

	
	/*
	
	void blobAccentuator(){
		int repeats=3;
		int rad1=2;
		int kw=2*rad1+1;
		float[][] out2=gaussianBlur(rad1);
		float[][] kernel=makeGaussianKernel(rad1);
		
		int x=out2.length;
		int y=out2[0].length;
		
		int[][] top=new int[x][y];
		int[][] sec=new int[x][y];
		float[][] out3=new float[x][y];
		
		int[] xs={-kw, -kw, -kw, 0, kw, kw, kw, 0};
		int[] ys={-kw, 0, kw, kw, kw, 0, -kw, -kw};
		for (int i=0; i<x; i++){
			for (int j=0; j<y; j++){
				//if(out[i][j]>0){
					float first=-10000;
					float second=-10000;
					int locf=-1;
					int locs=-1;
					for (int k=0; k<8; k++){
						int ii=i+xs[k];
						if ((ii>=0)&&(ii<x)){
							int jj=j+ys[k];
							if ((jj>=0)&&(jj<y)){
								float p=out2[ii][jj];
								if (p>first){
									second=first;
									locs=locf;
									first=p;
									locf=k;
								}
								else if (p>second){
									second=p;
									locs=k;
								}
							}
						}
					}
					top[i][j]=locf;
					sec[i][j]=locs;
				//}
			}
		}
		for (int i=0; i<x; i++){
			for (int j=0; j<y; j++){
				out2[i][j]=out[i][j];
				out3[i][j]=out[i][j];
			}
		}
		for (int r=0; r<repeats; r++){
			float[][] counter=new float[x][y];
			for (int i=0; i<x; i++){
				for (int j=0; j<y; j++){
					if(out[i][j]>0){
						int e=top[i][j];
						/*
						for (int f=xs[e]-rad1; f<=xs[e]+rad1; f++){
							int ff=f+i;
							if((ff>=0)&&(ff<x)){
								for (int g=ys[e]-rad1; g<=ys[e]+rad1; g++){
									int gg=g+j;
									if((gg>=0)&&(gg<y)&&(out3[ff][gg]<out[i][j])){
										out3[ff][gg]+=out[i][j]*kernel[f-xs[e]+rad1][g-ys[e]+rad1];
										if (out3[ff][gg]>out[i][j]){out3[ff][gg]=out[i][j];}
										counter[ff][gg]+=kernel[f-xs[e]+rad1][g-ys[e]+rad1];
									}
								}
							}
						}
						
						e=sec[i][j];
						for (int f=xs[e]-rad1; f<=xs[e]+rad1; f++){
							int ff=f+i;
							if((ff>=0)&&(ff<x)){
								for (int g=ys[e]-rad1; g<=ys[e]+rad1; g++){
									int gg=g+j;
									if((gg>=0)&&(gg<y)&&(out3[ff][gg]<out[i][j])){
										out3[ff][gg]+=out[i][j]*kernel[f-xs[e]+rad1][g-ys[e]+rad1];
										//if (out3[ff][gg]>out[i][j]){out3[ff][gg]=out[i][j];}
										counter[ff][gg]+=kernel[f-xs[e]+rad1][g-ys[e]+rad1];
									}
								}
							}
						}
					}
					
				}
			}
			for (int i=0; i<x; i++){
				for (int j=0; j<y; j++){
					//out[i][j]=out3[i][j]/(1+counter[i][j]);
					out[i][j]=out3[i][j];
					out3[i][j]=out[i][j];
				}
			}
		}	
		for (int i=0; i<x; i++){
			for (int j=0; j<y; j++){
				//out[i][j]=out[i][j]+out2[i][j];
			}
		}
	}
	
	void blobAccentuator3(float[][] out2){
		//float[][] out2=medianFilterXY(10, 0.45f);
		//float[][] out2=out;
		int x=out.length; 
		int y=out[0].length;
		float[][] out3=new float[x][y];
		int[] xd={-1, -1, -1, 0, 0, 1, 1, 1};
		int[] yd={-1, 0, 1, -1, 1, -1, 0, 1};
		
		for (int i=0; i<x; i++){
			for (int j=0; j<y; j++){
				float r=-1000f;
				float p=out2[i][j];
				for (int k=0; k<8; k++){
					
					for (int g=1; g<10; g++){
						int xq=i+g*xd[k];
						int yq=j+g*yd[k];
							
						if ((xq>=0)&&(yq>=0)&&(xq<x)&&(yq<y)){
							float q=out2[xq][yq];
							//if (q<0){q=0;}
							xq=i-g*xd[k];
							yq=j-g*yd[k];
							if ((xq>=0)&&(yq>=0)&&(xq<x)&&(yq<y)){
								float s=out2[xq][yq];
								//if(s<0){s=0;}
								q=p-(0.5f)*(s+q);
								if(q>r){
									r=q;
								}
							}
						}
					}	
				}	
				out3[i][j]=r;
				if (out[i][j]<out3[i][j]){out3[i][j]=out[i][j];}
				out3[i][j]+=out[i][j];
			}
		}
		out=out2;	
	}
	
void blobAccentuator4(){
		
		int x=out.length; 
		int y=out[0].length;
		float[][] out2=new float[x][y];
		int[] xd={-1, -1, -1, 0, 0, 1, 1, 1};
		int[] yd={-1, 0, 1, -1, 1, -1, 0, 1};
		
		for (int i=0; i<x; i++){
			for (int j=0; j<y; j++){
				
				if (out[i][j]>0){
					
					float p=out[i][j];
					
					int xp=i;
					int yp=j;
					int kk=0;
					while (kk>=0){
						if (out[i][j]>out2[xp][yp]){
							out2[xp][yp]=out[i][j];
						}
						float r=0;
						kk=-1;
						for (int k=0; k<8; k++){
							int xq=xp+xd[k];
							int yq=yp+yd[k];
							
							if ((xq>=0)&&(yq>=0)&&(xq<x)&&(yq<y)){
								float q=out[xq][yq];
								if((q>r)&&(q<p)){
									r=q;
									kk=k;
								}
							}
						}
						if (kk>=0){
							xp=xp+xd[kk];
							yp=yp+yd[kk];
							p=r;
						}
					}	
				}	
			}
		}
		out=out2;	
	}
	*/
	/*
	void lineAccentuator(){
		
		//this method carries out a simple line detection convolution
		
		int rad1=5;
		int diam=2*rad1+1;
		float inv=(float)(1/(diam+0.0));
		
		int[][] k=makeKernels(rad1);		
		
		int x=out.length;
		int y=out[0].length;
		
		int[][] filtered=new int[x][y];
		float[] medf=new float[diam];
		
		for (int a=0; a<x; a++){			
			for (int b=0; b<y; b++){
				double bestsc=-10000;
				double worstsc=10000;
				int loc=0;
				for (int c=0; c<k.length; c++){
					double sc=0;
					for (int d=0; d<diam; d++){
						int aa=a+k[c][d];
						if ((aa>=0)&&(aa<x)){
							int bb=b+k[c][d+diam];
							if ((bb>=0)&&(bb<y)){
								if(out[aa][bb]<out[a][b]){sc+=(out[aa][bb]-out[a][b])*(out[aa][bb]-out[a][b]);}
								//sc+=out[aa][bb];
							}
						}
					}
					
					if (sc>bestsc){bestsc=sc;}
					if (sc<worstsc){worstsc=sc;  filtered[a][b]=c;}
				}
			}
		}
		double sum=0;
		for (int a=0; a<x; a++){
			for (int b=0; b<y; b++){
				//out[a][b]=filtered[a][b];
				
				sum+=out[a][b];
				//out[a][b]+=100;
			}
		}
		
		sum/=x*y*1.0;
		//float out2[][]=new float[x][y];
		for (int repeat=0; repeat<1; repeat++){
			for (int a=0; a<x; a++){
				for (int b=0; b<y; b++){
					int loc=filtered[a][b];
					double sc=0;
					double co=0;
					for (int d=a-1; d<=a+1; d++){
						if ((d>=0)&&(d<x)){
							for (int e=b-1; e<b+1; e++){
								if ((e>=0)&&(e<y)){
								//if (out[a][b]>out[aa][bb]){
									int di=Math.abs(loc-filtered[d][e]);
									if (di>rad1){di=diam-di;}
									sc+=di;
									co++;
									//out2[aa][bb]=(0.25f*out[a][b]+out[aa][bb]);
								}
							}
						}
					}
					sc/=(co*rad1);
					out[a][b]+=(float)(10*(1-sc));
				}
			}
			//for (int a=0; a<x; a++){
			//	for (int b=0; b<y; b++){
			//		out[a][b]=out2[a][b];
			//	}
			//}
		}
		double sum2=0;
		for (int a=0; a<x; a++){
			for (int b=0; b<y; b++){
				sum2+=out[a][b];
			}
		}
		sum2/=x*y*1.0;
		System.out.println(sum+" "+sum2);
		float adj=(float)(sum-sum2);
		for (int a=0; a<x; a++){
			for (int b=0; b<y; b++){
				//out[a][b]+=adj;
			}
		}
		
	}
	
	
	
	*/
	

	
	/**
	 * This method is an effort to enhance the distinctiveness of signals in a spectrogram.
	 * It isn't used at present
	 * @param wei degree of enhancement
	 * @param p1 radius of a kernel
	 * @param p2 radius of a second kernel
	 * @param maxCh
	 * @return a float[][] of same size as the spectrogram
	 */
	public float[][] enhanceDistinctiveness(double wei, int p1, int p2, float maxCh, int anx, int ny, float[][] out){
		
		int rad1=p1;
		int rad2=p2;
		
		int er1=2*rad1+1;
		int er2=2*rad2+1;
				
		if (er1>anx){er1=anx;}
		if (er2>anx){er2=anx;}
		
		float ec=(float)(wei);
		
		float[][] t=new float[ny][anx];
		float[] buffer1=new float[er1];
		float[] buffer2=new float[er2];

		float[] maxout=new float[ny];
		float[] maxt=new float[ny];
		for (int i=0; i<ny; i++){
			maxout[i]=Float.NEGATIVE_INFINITY;
			maxt[i]=Float.NEGATIVE_INFINITY;
		}
		
		for (int i=0; i<ny; i++){
			
			
			for (int j=0; j<er1; j++){
				buffer1[j]=out[i][j];
			}
			Arrays.sort(buffer1);
			
			for (int j=0; j<er2; j++){
				buffer2[j]=out[i][j];
			}
			Arrays.sort(buffer2);
			
			int place1=0-rad1;
			int place2=0-rad2;
			
			for (int j=0; j<anx; j++){
				if(out[i][j]>maxout[i]){maxout[i]=out[i][j];}
			}
			for (int j=0; j<anx; j++){
				//The following code continually adjusts the the frame, keeping its ascending order by substituting out one and adding one.
				if ((place1>0)&&(place1<anx-er1)){
					int kk=0;
					float p=out[i][place1-1];
					boolean found=false;
					for (int k=0; k<er1-1; k++){
						if ((!found)&&(buffer1[k]==p)){
							kk++;
							found=true;
						}
						buffer1[k]=buffer1[kk];
						kk++;
					}
					p=out[i][place1+er1];
					for (int k=er1-2; k>=0; k--){
						if (buffer1[k]<=p){
							buffer1[k+1]=p;
							k=-1;
						}
						else{
							buffer1[k+1]=buffer1[k];
						}
					}
					if (p<buffer1[0]){buffer1[0]=p;}
				}
				
				if ((place2>0)&&(place2<anx-er2)){
					int kk=0;
					float p=out[i][place2-1];
					boolean found=false;
					for (int k=0; k<er2-1; k++){
						if ((!found)&&(buffer2[k]==p)){
							kk++;
							found=true;
						}
						buffer2[k]=buffer2[kk];
						kk++;
					}
					p=out[i][place2+er2];
					for (int k=er2-2; k>=0; k--){
						if (buffer2[k]<=p){
							buffer2[k+1]=p;
							k=-1;
						}
						else{
							buffer2[k+1]=buffer2[k];
						}
					}
					if (p<buffer2[0]){buffer2[0]=p;}
				}
				
				t[i][j]=out[i][j];
				
				float res1=out[i][j]-buffer1[rad1];
				float res2=out[i][j]-buffer2[rad2];
				
				
				//if(res1>0){
				//	res1=(res1/40f)*(maxout[i]-out[i][j]);
				//}
				//if(res2>0){
				//	res2=(res2/40f)*(maxout[i]-out[i][j]);
				//}
				
				
				if ((res1)>(res2)){
					if(res1>maxCh){res1=maxCh;}
					t[i][j]+=ec*res1;
				}
				else{
					if (res2>maxCh){res2=maxCh;}
					t[i][j]+=ec*res2;
				}
				if(t[i][j]>maxout[i]+20){t[i][j]=maxout[i]+20;}
				
				
				//t[i][j]+=ec*(out[i][j]-buffer1[medLoc1]);

				
				if(t[i][j]>maxt[i]){maxt[i]=t[i][j];}
				
				place1++;
				place2++;
				
			}
		}	
		/*
		for (int i=0; i<ny; i++){
			float adj=(maxout[i]-maxt[i]);
			for (int j=0; j<anx; j++){
				//t[i][j]+=adj;
			}
		}
		*/
		return t;
	}
	
	/**
	 * This method is not used at present. It is an effort to enhance the distinctiveness of
	 * potential signals in a spectrogram
	 * @param wei a parameter for the degree of emphasis
	 */
	void enhanceDistinctivenessFreq(double wei, float[][] out, int ny, int anx){
		
		int er=ny;
		float ec=(float)(wei);
		
		float[][] t=new float[ny][anx];
		float[] buffer=new float[er];
		int medLoc=er/2;
		//float avout=0;
		//float avt=0;
		for (int i=0; i<anx; i++){
			
			
			for (int j=0; j<er; j++){
				buffer[j]=out[j][i];
			}
			Arrays.sort(buffer);

			for (int j=0; j<ny; j++){
				t[j][i]=out[j][i]-ec*buffer[medLoc];
				//avout+=out[j][i];
				//avt+=t[j][i];				
			}
		}		
		//float adj=(avout-avt)/(ny*anx*1f);
		for (int i=0; i<ny; i++){
			for (int j=0; j<anx; j++){
				out[i][j]=t[i][j];
			}
		}
	}
	
	
	
	
	
	/**
	 * This method carries out a simple effort at denoising. It isn't used at present.
	 */
	void denoiser(int ny, int anx, float[][] out){
		int sampleLength=100;
		double[] means=new double[ny];
		for (int i=0; i<ny; i++){
			for (int j=0; j<sampleLength; j++){
				means[i]+=out[i][j];
			}
		}
		for (int i=0; i<ny; i++){
			means[i]/=sampleLength+0.0;
		}
		double[] sds=new double[ny];
		for (int i=0; i<ny; i++){
			for (int j=0; j<sampleLength; j++){
				double p=out[i][j]-means[i];
				sds[i]+=p*p;
			}
		}
		for (int i=0; i<ny; i++){
			sds[i]/=sampleLength+0.0;
			sds[i]=Math.sqrt(i);
		}
		float cutOffs[]=new float[ny];
		for (int i=0; i<ny; i++){
			double p=means[i];
			if (p>0){
				cutOffs[i]=(float)p;
			}
			
		}
		for (int i=0; i<ny; i++){
			for (int j=0; j<anx; j++){
				out[i][j]-=cutOffs[i];
			}
		}

	}
	
	
	
	
	/**
	 * A filter that attempts to remove median data from a spectrogram
	 * @param rad filter radius
	 * @param amt the proportion filtered (0.5=median)
	 * @param d input spectrogram data
	 * @param type index, see propFilterXY
	 * @return a float[][] of same dimensions as d
	 */
	float[][] medianAccentXY(int rad, float amt, float[][]d, int type){

		float[][] filt=propFilterXY(rad, amt, d, type);
		float[][] out=matrixSubtract(d, filt);
		
		return out;
	}	
	
	/**
	 * Median filters in x and y planes and returns a combination of the two
	 * @param rad the radius of the filter
	 * @param amt the proportion filtered (0.5=median)
	 * @param d the input spectrogram data
	 * @param type an index: 0=average, 1=min, 2=max, when combining the two filters
	 * @return a filtered float[][] of same dimensions as d
	 */
	float[][] propFilterXY(int rad, float amt, float[][]d, int type){
	
		float[][] ox=propFilterXD(rad, amt, d, 0);
		float[][] oy=propFilterYD(rad, amt, d);

		float[][] out=null;
		if (type==0){
			out=matrixAv(ox, oy);
		}
		else if (type==1){
			out=matrixMin(ox, oy);
		}
		else if (type==2){
			out=matrixMax(ox, oy);
		}
		return out;
	}

	/**
	 * Calculates the mean values of two input float[][] matrices
	 * Matrices should be of the same row and column order! This is NOT checked by
	 * the function!!!
	 * @param a first input matrix
	 * @param b second input matrix
	 * @return float[][] of same size as inputs.
	 */
	float[][] matrixAv(float[][] a, float[][] b){
		int x=a.length;
		int y=a[0].length;
		float[][] o=new float[x][y];
		for (int i=0; i<x; i++){
			for (int j=0; j<y; j++){
				o[i][j]=0.5f*(a[i][j]+b[i][j]);
			}
		}
		return o;
	}
	
	/**
	 * Subtracts Matrix a from Matrix b, but keeps the maximum value as matrix a
	 * Matrices should be of the same row and column order! This is NOT checked by
	 * the function!!!
	 * @param a first input matrix
	 * @param b second input matrix
	 * @return float[][] of same size as inputs.
	 */
	float[][] matrixSubSameMax(float[][] a, float[][] b){
		float max=matrixMax(a);
		float[][] o=matrixSubtract(a, b);
		float max2=matrixMax(o);
		o=matrixAdd(o, max-max2);
		return o;
	}
		
		
	/** Subtract one matrix from another
	 * Matrices should be of the same row and column order! This is NOT checked by
	 * the function!!!
	 * @param a first input matrix
	 * @param b second input matrix
	 * @return float[][] of same size as inputs.
	 */
	float[][] matrixSubtract(float[][] a, float[][] b){
			
		int x=a.length;
		int y=a[0].length;
		float[][] o=new float[x][y];
		for (int i=0; i<x; i++){
			for (int j=0; j<y; j++){
				o[i][j]=a[i][j]-b[i][j];
			}
		}
		return o;
	}
	
	/** Add one matrix to another
	 * Matrices should be of the same row and column order! This is NOT checked by
	 * the function!!!
	 * @param a first input matrix
	 * @param b second input matrix
	 * @return float[][] of same size as inputs.
	 */
	float[][] matrixAdd(float[][] a, float[][] b){
		
		int x=a.length;
		int y=a[0].length;
		float[][] o=new float[x][y];
		for (int i=0; i<x; i++){
			for (int j=0; j<y; j++){
				o[i][j]=a[i][j]+b[i][j];
			}
		}
		return o;
	}
	
	/** Makes a new matrix from two input matrices, each cell of which represents the minimum of the two cells
	 * of the input matrices.
	 * Matrices should be of the same row and column order! This is NOT checked by
	 * the function!!!
	 * @param a first input matrix
	 * @param b second input matrix
	 * @return float[][] of same size as inputs.
	 */
	float[][] matrixMin(float[][] a, float[][] b){
		
		int x=a.length;
		int y=a[0].length;
		float[][] o=new float[x][y];
		for (int i=0; i<x; i++){
			for (int j=0; j<y; j++){
				if (a[i][j]<b[i][j]){
					o[i][j]=a[i][j];
				}
				else{
					o[i][j]=b[i][j];
				}
			}
		}
		return o;
	}
	
	/** Makes a new matrix from two input matrices, each cell of which represents the maximum of the two cells
	 * of the input matrices.
	 * Matrices should be of the same row and column order! This is NOT checked by
	 * the function!!!
	 * @param a first input matrix
	 * @param b second input matrix
	 * @return float[][] of same size as inputs.
	 */
	float[][] matrixMax(float[][] a, float[][] b){
		
		int x=a.length;
		int y=a[0].length;
		float[][] o=new float[x][y];
		for (int i=0; i<x; i++){
			for (int j=0; j<y; j++){
				if (a[i][j]<b[i][j]){
					o[i][j]=b[i][j];
				}
				else{
					o[i][j]=a[i][j];
				}
			}
		}
		return o;
	}
	
	/**
	 * Adds a scalar value to each cell of a matrix
	 * @param a input matrix
	 * @param sc scalar value to add to the matrix
	 * @return float[][] of same size as input.
	 */
	float[][] matrixAdd(float[][] a, float sc){
	
		int x=a.length;
		int y=a[0].length;
		float[][] o=new float[x][y];
		for (int i=0; i<x; i++){
			for (int j=0; j<y; j++){
				o[i][j]=a[i][j]+sc;
			}
		}
		return o;
	}
	
	/**
	 * Multiplies scalar value with each cell of a matrix
	 * @param a input matrix
	 * @param sc scalar value to multiply with the matrix
	 * @return float[][] of same size as input.
	 */
	float[][] matrixMultiply(float[][] a, float sc){
		
		int x=a.length;
		int y=a[0].length;
		float[][] o=new float[x][y];
		for (int i=0; i<x; i++){
			for (int j=0; j<y; j++){
				o[i][j]=a[i][j]*sc;
			}
		}
		return o;
	}
	
	/**
	 * Creates a new matrix, each cell of which is the maximum of an input matrix and a scalar
	 * value
	 * @param a input matrix
	 * @param sc scalar value to compare with the matrix
	 * @return float[][] of same size as input.
	 */
	float[][] matrixMax(float[][] a, float sc){
		
		int x=a.length;
		int y=a[0].length;
		float[][] o=new float[x][y];
		for (int i=0; i<x; i++){
			for (int j=0; j<y; j++){
				if (a[i][j]>sc){
					o[i][j]=a[i][j];
				}
				else{
					a[i][j]=sc;
				}
			}
		}
		return o;
	}
	
	/**
	 * Calculates the maximum value within a matrix
	 * @param a input matrix
	 * @param sc scalar value to add to the matrix
	 * @return maximum value (float) in the matrix
	 */
	float matrixMax(float[][] a){
		
		int x=a.length;
		int y=a[0].length;
		float sc=-1000000f;
		for (int i=0; i<x; i++){
			for (int j=0; j<y; j++){
				if (a[i][j]>sc){
					sc=a[i][j];
				}
			}
		}
		return sc;
	}
	
	/**
	 * Creates a new matrix, each cell of which is the minimum of an input matrix and a scalar
	 * value
	 * @param a input matrix
	 * @param sc scalar value to compare with the matrix
	 * @return float[][] of same size as input.
	 */
	float[][] matrixMin(float[][] a, float sc){
		
		int x=a.length;
		int y=a[0].length;
		float[][] o=new float[x][y];
		for (int i=0; i<x; i++){
			for (int j=0; j<y; j++){
				if (a[i][j]<sc){
					o[i][j]=a[i][j];
				}
				else{
					a[i][j]=sc;
				}
			}
		}
		return o;
	}
	
	/**
	 * This applies a %ile filter in the time direction to spectrogram data
	 * @param rad the radius of the filter
	 * @param amt the %ile to be selected in the median filter- 0.5 makes it median
	 * @param d input data, a float[][] spectrogram
	 * @param offset
	 * @return a float[][] of same dimensions to d, with filter applied
	 */
	float[][] propFilterXD(int rad, float amt, float[][] d, int offset){
		int er=2*rad+1;
		
		int x=d.length;
		int y=d[0].length;
		if (er>y){
			er=y;
			rad=(int)Math.floor(er/2.0);
		}
		int r2=(int)Math.floor(er*amt);
		if (r2>=er){
			r2=er-1;
		}
		
		float[] buffer=new float[er];
		float[][]out=new float[x][y];
		
		//int cl=rad;
		//double[] ct=new double[er];
		
		int i,j;
		for (i=0; i<x; i++){
			for (j=0; j<er; j++){
				buffer[j]=d[i][j];
			}
			Arrays.sort(buffer);
			int place=-(rad+offset);
			for (j=0; j<y; j++){
				
				
				if ((place>0)&&(place<y-er)){
					int kk=0;
					float p=d[i][place-1];
					boolean found=false;
					for (int k=0; k<er-1; k++){
						if ((!found)&&(buffer[k]==p)){
							kk++;
							found=true;
						}
						buffer[k]=buffer[kk];
						kk++;
					}
					p=d[i][j+rad-offset];
					
					
					for (int k=er-2; k>=0; k--){
						if (buffer[k]<=p){
							buffer[k+1]=p;
							k=-1;
						}
						else{
							buffer[k+1]=buffer[k];
						}
					}
					if (p<buffer[0]){buffer[0]=p;}
				}
				
				//for (int k=0; k<er; k++){
				//	ct[k]+=buffer[k]-buffer[cl];
				//}
				
				out[i][j]=buffer[r2];
				
				place++;
			}
		}
		
		//for (i=0; i<er; i++){
		//	System.out.println(i+" "+ct[i]/(x*y*1.0));
		//}
		return out;
	}
	
	/**
	 * This function (not currently used) filters within a range.
	 * @param rad radius of the filter
	 * @param amt amount to remove
	 * @param d spectrogram float[][] data
	 * @param offset an offset parameter
	 * @return a float[][] of same dimensions as d
	 */
	float[][] propFilterRange(int rad, float amt, float[][] d, int offset){
		int er=2*rad+1;

		int x=d.length;
		int y=d[0].length;
		if (er>y){
			er=y;
			rad=(int)Math.floor(er/2.0);
		}
		int r2=(int)Math.floor(er*amt);
		if (r2>=er){r2=er-1;}
		int r1=er-r2;
		
		float[] buffer=new float[er];
		float[][]out=new float[x][y];
		
		int i,j;
		for (i=0; i<x; i++){
			for (j=0; j<er; j++){
				buffer[j]=d[i][j];
			}
			Arrays.sort(buffer);
			int place=-(rad+offset);
			for (j=0; j<y; j++){
				
				
				if ((place>0)&&(place<y-er)){
					int kk=0;
					float p=d[i][place-1];
					boolean found=false;
					for (int k=0; k<er-1; k++){
						if ((!found)&&(buffer[k]==p)){
							kk++;
							found=true;
						}
						buffer[k]=buffer[kk];
						kk++;
					}
					p=d[i][j+rad-offset];
					
					
					for (int k=er-2; k>=0; k--){
						if (buffer[k]<=p){
							buffer[k+1]=p;
							k=-1;
						}
						else{
							buffer[k+1]=buffer[k];
						}
					}
					if (p<buffer[0]){buffer[0]=p;}
				}

				out[i][j]=buffer[r1]-buffer[r2];
				
				place++;
			}
		}
		
		return out;
	}
	
	/**
	 * This applies a %ile filter in the frequency direction to spectrogram data
	 * @param rad the radius of the filter
	 * @param amt the %ile to be selected in the median filter- 0.5 makes it median
	 * @param d input data, a float[][] spectrogram
	 * @return a float[][] of same dimensions to d, with filter applied
	 */
	float[][] propFilterYD(int rad, float amt, float[][] d){
		int er=2*rad+1;

		int x=d[0].length;
		int y=d.length;
		if (er>y){
			er=y;
			rad=(int)Math.floor(er/2.0);
		}
		int r2=(int)Math.round(er*amt);
		if (r2>=er){r2=er-1;}
		
		float[] buffer=new float[er];
		float[][]out=new float[y][x];
		int i,j;
		for (i=0; i<x; i++){
			for (j=0; j<er; j++){
				buffer[j]=d[j][i];
			}
			Arrays.sort(buffer);
			int place=-rad;
			for (j=0; j<y; j++){

				if ((place>0)&&(place<y-er)){
					int kk=0;
					float p=d[place-1][i];
					boolean found=false;
					for (int k=0; k<er-1; k++){
						if ((!found)&&(buffer[k]==p)){
							kk++;
							found=true;
						}
						buffer[k]=buffer[kk];
						kk++;
					}
					p=d[j+rad][i];
					
					
					for (int k=er-2; k>=0; k--){
						if (buffer[k]<=p){
							buffer[k+1]=p;
							k=-1;
						}
						else{
							buffer[k+1]=buffer[k];
						}
					}
					if (p<buffer[0]){buffer[0]=p;}
				}
				
				out[j][i]=buffer[r2];
				
				place++;
			}
		}
		return out;
	}
	
	/**
	 * Performs the erosion component of the erosion-dilation algorithm
	 * @param o spectrogram float[][] data 
	 * @param kr range to appyl algorithm over in time dimension
	 * @param zeroFloor if true, values less than zero are set to zero
	 * @return a float[][] of the same dimensions as o
	 */
	float[][] erosion(float[][] o, int kr, boolean zeroFloor){
		
		int x=o.length;
		int y=o[0].length;
		
		float[][] eroded=new float[x][y];

		int a1, a2, b1, b2;
		float min;		
		
		for (int a=0; a<x; a++){
			a1=a-kr;
			if (a1<0){a1=0;}
			a2=a+kr;
			if (a2>=x){a2=x-1;}
			
			for (int b=0; b<y; b++){
				
				b1=b-kr;
				if (b1<0){b1=0;}
				b2=b+kr;
				if (b2>=y){b2=y-1;}
				
				min=Float.MAX_VALUE;
				for (int c=a1; c<=a2; c++){
					for (int d=b1; d<=b2; d++){
						if (o[c][d]<min){min=o[c][d];}
					}
				}
				eroded[a][b]=min;
				if ((zeroFloor)&&(min<0)){
					eroded[a][b]=0;
				}
			}
		}
		return eroded;
	}
	
	/**
	 * Performs the dilation component of the erosion-dilation algorithm
	 * @param o spectrogram float[][] data 
	 * @param kr range to apply algorithm over in time dimension
	 * @param zeroFloor if true, values less than zero are set to zero
	 * @return a float[][] of the same dimensions as o
	 */
	float[][] dilation(float[][] o, int kr, boolean zeroFloor){
		int x=o.length;
		int y=o[0].length;
		float[][] dilation=new float[x][y];
		int a1, a2, b1, b2;
		float max;
		
		for (int a=0; a<x; a++){
			a1=a-kr;
			if (a1<0){a1=0;}
			a2=a+kr;
			if (a2>=x){a2=x-1;}
			
			for (int b=0; b<y; b++){
				
				b1=b-kr;
				if (b1<0){b1=0;}
				b2=b+kr;
				if (b2>=y){b2=y-1;}
				
				max=Float.NEGATIVE_INFINITY;
				for (int c=a1; c<=a2; c++){
					for (int d=b1; d<=b2; d++){
						if (o[c][d]>max){max=o[c][d];}
					}
				}
				dilation[a][b]=max;
				if ((zeroFloor)&&(max<0)){
					dilation[a][b]=0;
				}
			}
		}
		return dilation;
	}

	/**
	 * Carries out the erosion/dilation noise removal algorithm
	 * @param rad radius for the algorithm
	 * @param zeroFloor whether or not values less than 0 are set to 0
	 * @param d spectrogram float[][] data
	 * @return a float[][] of same dimensions as d
	 */
	float[][] erosionDilationNoiseRemoval(int rad, boolean zeroFloor, float[][]d){
		
		float[][] eroded=erosion(d, rad, zeroFloor);
		float[][] ed=dilation(eroded, rad, zeroFloor);
		return ed;
	}
	
	/**
	 * Carries out a dilation/erosion noise removal algorithm (is this
	 * something silly that I invented?)
	 * @param rad radius for the algorithm
	 * @param zeroFloor whether or not values less than 0 are set to 0
	 * @param d spectrogram float[][] data
	 * @return a float[][] of same dimensions as d
	 */
	float[][] dilationErosionNoiseRemoval(int rad, boolean zeroFloor, float[][]d){
		
		float[][] dilated=dilation(d, rad, zeroFloor);
		float[][] ed=erosion(dilated, rad, zeroFloor);
		return ed;
	}
	
	/**
	 * Implementation of an edge detection algorithm using erosion dilation
	 * Subtracts dilated from eroded data
	  * @param rad radius for the algorithm
	 * @param zeroFloor whether or not values less than 0 are set to 0
	 * @param o spectrogram float[][] data
	 * @return a float[][] of same dimensions as o
	 */
	float[][] edgeDetection(int rad, boolean zeroFloor, float[][] o){
		
		float[][] eroded=erosion(o, rad, zeroFloor);
		float[][] dilated=dilation(o, rad, zeroFloor);
		float[][] out=matrixSubtract(dilated, eroded);
		return out;
	}
	
	/**
	 * This method is unused/deprecated
	 * @param sp
	 * @param ed
	 * @param upper
	 * @param lower
	 * @param edgeThreshold
	 * @return a float[][] spectrogram
	 */
	float[][] fillDetects(float[][] sp, float[][] ed, double upper, double lower, double edgeThreshold){
		
		int x=sp.length;
		int y=sp[0].length;
		
		float[][] out1=new float[x][y];
		int[][] marked=new int[x][y];
		float[][] ref=new float[x][y];

		int count=1;
		
		for (int i=0; i<x; i++){
			for (int j=0; j<y; j++){
				if (sp[i][j]>=upper){
					marked[i][j]=1;
					ref[i][j]=ed[i][j];
				}
				else{
					marked[i][j]=0;
				}
			}
		}
		
		while (count>0){
			System.out.println(count);
			count=0;
			for (int i=0; i<x; i++){
				for (int j=0; j<y; j++){
					if (marked[i][j]==1){
						marked[i][j]=2;
						int x1=i-1;
						if (x1<0){x1=0;}
						int x2=i+1;
						if (x2==x){x2--;}
						int y1=j-1;
						if (y1<0){y1=0;}
						int y2=j+1;
						if(y2==y){y2--;}
						for (int g=x1; g<=x2; g++){
							for (int h=y1; h<=y2; h++){
								if ((marked[g][h]==0)&&(sp[g][h]>=lower)){
									if(ed[g][h]<edgeThreshold){
										marked[g][h]=1;
									}
									else{
										marked[g][h]=2;
									}
									ref[g][h]=Math.max(ref[i][j], ed[g][h]);
									count++;
								}
								
							}
						}

					}
				}
			}	
		}
		for (int i=0; i<x; i++){
			for (int j=0; j<y; j++){
				if (marked[i][j]>0){
					out1[i][j]=sp[i][j];
				}
			}
		}
		return out1;	
	}
	

	/**
	 * Carries out a difference of Gaussians approach (edge accentuation)
	 * This is unused, was a trial effort
	 * @param d input float[][] spectrogram data
	 * @return float[][] of same dimensions as d
	 */
	float[][] differenceOfGaussians(float[][] d){
		float[][] o1=gaussianBlur(5, d);
		float[][] o2=gaussianBlur(10, d);
		float[][] out=matrixSubtract(o1, o2);
		return out;
	}
	
	/**
	 * Constructs a Gaussian kernel for e.g. Gaussian blur
	 * @param rad radius of the kernel
	 * @return a kernel float[][] object.
	 */
	float[][] makeGaussianKernel(int rad){
		double vari=rad/3.0;
		int diam=2*rad+1;
		float[][] kernel=new float[diam][diam];
		double mult=1/Math.sqrt(2*Math.PI*vari);
		double den=-1/(2*vari);
		for (int i=0; i<diam; i++){
			int ii=i-rad;
			ii*=ii;
			for (int j=0; j<diam; j++){
				int jj=j-rad;
				jj*=jj;
				kernel[i][j]=(float)(mult*Math.exp(den*(ii+jj)));
			}
		}
		return kernel;
	}
	
	/**
	 * Carries out a Gaussian blur on spectrogram data.
	 * @param rad radius of the Gaussian kernel
	 * @param d input spectrogram float[][] data
	 * @return a float[][] of same size as d
	 */
	float[][] gaussianBlur(int rad, float[][] d){
		float[][] kernel=makeGaussianKernel(rad);
		int diam=kernel.length;
		
		int x=d.length;
		int y=d[0].length;
		
		float[][]out=new float[x][y];
		float tot=0;
		int g,h,i,j,gg,hh;
		for (i=0; i<x; i++){
			for (j=0; j<y; j++){
				tot=0;
				for (g=0; g<diam; g++){
					gg=i+g-rad;
					if ((gg>=0)&&(gg<x)){
						for (h=0; h<diam; h++){
							hh=j+h-rad;
							if((hh>=0)&&(hh<y)){
								out[i][j]+=d[gg][hh]*kernel[g][h];
								tot+=kernel[g][h];
							}
						}
					}
				}
				out[i][j]/=tot;
			}
		}
		return out;
	}
	
	/**
	 * Carries out a Gaussian blur only in the time dimension
	 * @param rad radius for a Gaussian kernel
	 * @param d intput spectrogram float[][] data
	 * @return a spectrogram of the same size as d
	 */
	float[][] gaussianBlurXD(int rad, float[][] d){
		int diam=2*rad+1;
		
		int x=d.length;
		int y=d[0].length;
		
		float[] kernel=new float[diam];
		double vari=rad/3.0;
		double mult=1/Math.sqrt(2*Math.PI*vari);
		double den=-1/(2*vari);
		for (int i=0; i<diam; i++){
			int ii=i-rad;
			ii*=ii;
			kernel[i]=(float)(mult*Math.exp(den*ii));
		}
		
		float[][]out=new float[x][y];
		float tot=0;
		int g,i,j,gg;
		for (i=0; i<x; i++){
			for (j=0; j<y; j++){
				tot=0;
				for (g=0; g<diam; g++){
					gg=j+g-rad;
					if ((gg>=0)&&(gg<y)){
						out[i][j]+=d[i][gg]*kernel[g];
						tot+=kernel[g];
					}
				}
				out[i][j]/=tot;
			}
		}
		return out;
	}
	
	/**
	 * An unused deprecated function.
	 * @param rad
	 * @param d
	 * @return a float[][] spectrogram
	 */
	float[][] blocker(int rad, float[][] d){
		int x=d.length;
		int y=d[0].length;
		
		float[][]out=new float[x][y];
		for (int i=0; i<x; i++){
			for (int j=0; j<y; j++){
				float max=-100f;
				for (int ii=i-rad; ii<=i+rad; ii++){
					if ((ii>=0)&&(ii<x)){
						for (int jj=j-rad; jj<=j+rad; jj++){
							if ((jj>=0)&&(jj<y)){
								if (d[ii][jj]>max){
									max=d[ii][jj];
								}
							}
						}
					}
						
				}
				out[i][j]=max;
			}
		}
		return out;
	}
	
	
	
	
		
	/**
	 * This is an unused function used in development of median filter.
	 * @param rad
	 * @param amt
	 * @param sc
	 * @param d
	 * @param offset
	 * @return a float[][] spectrogram
	 */
	float[][] medianFilterXD(int rad, float amt, float sc, float[][]d, int offset){
		
		float[][] med=propFilterXD(rad, amt, d, offset);
		med=matrixAdd(med, sc);
		float[][]out=matrixMax(d, med);
		return out;
	}	
	
	/**
	 * This is an unused function 
	 * @param rad
	 * @param amt
	 * @param sc
	 * @param d
	 * @return a float[][] spectrogram
	 */
	float[][] medianFilterYD(int rad, float amt, float sc, float[][]d){
		
		float[][] med=propFilterYD(rad, amt, d);
		med=matrixAdd(med, sc);
		float[][]out=matrixMax(d, med);
		return out;
	}	

	/**
	 * This is an alternative noise removal algorithm that isn't being used at the moment
	 * @param rad
	 * @param amt
	 * @param sc
	 * @param d
	 * @param offset
	 * @return a float[][] spectrogram
	 */
	float[][] medianFilterNR2(int rad, float amt, float sc, float[][]d, int offset){
		
		
		float[][] med=propFilterRange(rad, amt, d, offset);

		med=matrixMultiply(med, sc);
		
		float[][]out=matrixSubtract(d, med);
		return out;
	}
	
	/**
	 * This function is the first step of noise removal
	 * @param rad a convolution radius
	 * @param amt the amount of noise removal
	 * @param sc
	 * @param d spectrogram data
	 * @param offset
	 * @return a float[][] of same dimensions as d
	 */
	float[][] medianFilterNR(int rad, float amt, float sc, float[][]d, int offset){
		
		//float[][] x1=propFilterXD(5, amt, d,0);
		//d=matrixMax(d, x1);
		
		//d=gaussianBlurXD(10, d);
		
		
		//float[][] bl=propFilterXD(10, 0.5f,d, 0);
		//float[][] bm=matrixMax(d, bl);
		float[][] medb=propFilterYD(20, 0.5f, d);
		float[][] med=propFilterXD(rad, amt, medb, offset);
		
		
		
		//float[][] med=propFilterXD(rad, amt, d, offset);
		
		//float[][] med=propFilterRange(rad, amt, d, offset);
		
		
		
		
		med=matrixAdd(med, sc);
		med=matrixMax(med, 0f);
		//float[][] d2=matrixMultiply(d, 2);
		float[][]out=matrixSubtract(d, med);
		//out=matrixMin(out, d);
		return out;
	}
	
	/**
	 * This function is the second stage of the Median Noise removal filter.
	 * @param rad convolution radius
	 * @param amt a parameter for the amount of noise removal
	 * @param sc
	 * @param d spectrogram-type data
	 * @param offset
	 * @return a float[][] of same dimensions as d that has been subjected to noise removal
	 */
	float[][] medianFilterNR1(int rad, float amt, float sc, float[][]d, int offset){
		
		//float[][] x1=propFilterXD(5, amt, d,0);
		//d=matrixMax(d, x1);
		
		//d=gaussianBlurXD(10, d);
		
		
		//float[][] bl=propFilterYD(20, 0.5f,d);
		float[][] meda=propFilterXD(rad, amt, d, offset);
		float[][] medb=propFilterYD(rad, amt, d);
		float[][] med=matrixMin(meda, medb);
		//float[][] med=propFilterRange(rad, amt, d, offset);
		
		med=matrixAdd(med, sc);
		med=matrixMax(med, 0f);
		med=matrixSubtract(med, d);
		med=matrixMax(med, 0f);
		
		float[][]out=matrixSubtract(d, med);
		return out;
	}
	
	/**
	 * This method convolves a data object (typically from Song) and a kernel.
	 * @param data	typically, spectrogram float[][] data
	 * @param kernel a convolution kernel
	 * @param rad kernel radius
	 * @return a float[][] of same dimensions as data, which has been convolved
	 */
	public float[][] convolve(float[][] data, double[][] kernel, int rad){
		
		int a=kernel.length;
		int b=kernel[0].length;
		
		int x=data.length;
		int y=data[0].length;
		
		float[][] out=new float[x][y];
		
		//double score=0;
		for (int i=0; i<a; i++){
			
			int i1=i-rad;
			int i2=i1+a;
			if (i1<0){i1=0;}
			if (i2>=x){i2=x-1;}
			
			for (int j=0; j<b; j++){
				
				int j1=j-rad;
				int j2=j1+a;
				if (j1<0){j1=0;}
				if (j2>=y){j2=y-1;}
			
				for (int g=i1; g<=i2; g++){
					int gg=g-rad;
					for (int h=j1; h<=j2; h++){
						int hh=h-rad;
						out[i][j]+=data[g][h]*kernel[gg][hh];
					}
				}
			}

		}
		return out;
	}
	
	
	/**
	 * This is a method that attempts to accentuate lines in the spectrogram.
	 * This is part of an effort to move towards automatic measurement
	 * @param d input float[][] spectrogram data
	 * @param rad radius of a kernel
	 * @param repeats number of times to apply the lineAccentuator function
	 * @return a float[][] of the same size as d
	 */
	float[][] accentuateLines(float[][] d, int rad, int repeats){
		
		
		for (int i=0; i<repeats; i++){
			d=lineAccentuator(d, rad);
		}
		return d;
		
	}
	
	/**
	 * An algorithm to accentuate lines by searching for correlations with line 'primitives'
	 * @param o input float[][] spectrogram data
	 * @param rad1 kernel radius.
	 * @return a float[][] spectrogram
	 */
	private float[][] lineAccentuator(float[][] o, int rad1){
		
		int x=o.length;
		int y=o[0].length;
		float[][] out2=new float[x][y];
		int diam=2*rad1+1;
		//float inv=(float)(1/(diam+0.0));
		
		int[][] k=makeKernels(rad1);		
	
		for (int a=0; a<x; a++){			
			for (int b=0; b<y; b++){
				double bestsc=Float.NEGATIVE_INFINITY;
				double bestsc2=0;
				//int loc=0;
				for (int c=0; c<k.length; c++){
					for (int d2=0; d2<2; d2++){
						double sc=0;
						//double sc2=0;
						double scm=Double.NEGATIVE_INFINITY;
						double co=0;
						for (int d=0; d<=rad1; d++){
							int aa=a+k[c][d+(rad1*d2)];
								if ((aa>=0)&&(aa<x)){
									int bb=b+k[c][d+diam+(rad1*d2)];
									if ((bb>=0)&&(bb<y)){
										sc+=(o[aa][bb]-o[a][b])*(o[aa][bb]-o[a][b]);
										//sc2+=o[aa][bb];
										if (o[aa][bb]>scm){scm=o[aa][bb];}
										co++;
									}
								}
						}
						sc=Math.sqrt(sc);
						sc/=co;
						//sc2/=co;
						sc=-sc;
						if (sc>bestsc){
							bestsc=sc;
							bestsc2=scm;
						}
					}
				}
				out2[a][b]=(float)(bestsc2);
			}
		}
	
		return(out2);
	}	
	
	/**
	 * This method constructs a set of convolution kernels of set radius, consisting of different
	 * primitive lines.
	 * @param rad1	kernel radius
	 * @return int[][] of a kernel
	 */
	public int[][] makeKernels(int rad1){
		
		
		int diam=2*rad1+1;
		
		int[][] kernels=new int[4*rad1][2*diam];
		
		for (int i=-rad1; i<rad1; i++){			
			for (int j=0; j<diam; j++){		
				int jj=j-rad1;
				kernels[i+rad1][j]=jj;
				int q=(int)Math.round(i-j*(i/(rad1+0.0)));
				kernels[i+rad1][j+diam]=q;	
			}	
		}
		
		for (int i=0; i<2*rad1; i++){
			for (int j=0; j<diam; j++){
				kernels[2*rad1+i][j]=kernels[i][j+diam];
				kernels[2*rad1+i][j+diam]=-1*kernels[i][j];
			}
		}
		
		return kernels;
		
	}
	
}