package lusc.net.github;

import java.util.Arrays;

public class Matrix2DOperations {
	
	
	
	
	
	
	
	
	float[][] medianAccentXY(int rad, float amt, float[][]d, int type){

		float[][] filt=propFilterXY(rad, amt, d, type);
		float[][] out=matrixSubtract(d, filt);
		
		return out;
	}	
	
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
	
	float[][] matrixSubSameMax(float[][] a, float[][] b){
		float max=matrixMax(a);
		System.out.println(max);
		float[][] o=matrixSubtract(a, b);
		float max2=matrixMax(o);
		System.out.println(max2);
		o=matrixAdd(o, max-max2);
		return o;
	}
		
		
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

	float[][] erosionDilationNoiseRemoval(int rad, boolean zeroFloor, float[][]d){
		
		float[][] eroded=erosion(d, rad, zeroFloor);
		float[][] ed=dilation(eroded, rad, zeroFloor);
		return ed;
	}
	
	float[][] dilationErosionNoiseRemoval(int rad, boolean zeroFloor, float[][]d){
		
		float[][] dilated=dilation(d, rad, zeroFloor);
		float[][] ed=erosion(dilated, rad, zeroFloor);
		return ed;
	}
	
	float[][] edgeDetection(int rad, boolean zeroFloor, float[][] o){
		
		float[][] eroded=erosion(o, rad, zeroFloor);
		float[][] dilated=dilation(o, rad, zeroFloor);
		float[][] out=matrixSubtract(dilated, eroded);
		return out;
	}
	
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
	

	float[][] differenceOfGaussians(float[][] d){
		float[][] o1=gaussianBlur(5, d);
		float[][] o2=gaussianBlur(10, d);
		float[][] out=matrixSubtract(o1, o2);
		return out;
	}
	
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
	
	
	
	
		
	float[][] medianFilterXD(int rad, float amt, float sc, float[][]d, int offset){
		
		float[][] med=propFilterXD(rad, amt, d, offset);
		med=matrixAdd(med, sc);
		float[][]out=matrixMax(d, med);
		return out;
	}	
	
	float[][] medianFilterYD(int rad, float amt, float sc, float[][]d){
		
		float[][] med=propFilterYD(rad, amt, d);
		med=matrixAdd(med, sc);
		float[][]out=matrixMax(d, med);
		return out;
	}	

	float[][] medianFilterNR2(int rad, float amt, float sc, float[][]d, int offset){
		
		
		float[][] med=propFilterRange(rad, amt, d, offset);

		med=matrixMultiply(med, sc);
		
		float[][]out=matrixSubtract(d, med);
		return out;
	}
	
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
	
	public float[][] convolve(float[][] data, double[][] kernel, int rad){
		
		int a=kernel.length;
		int b=kernel[0].length;
		
		int x=data.length;
		int y=data[0].length;
		
		float[][] out=new float[x][y];
		
		double score=0;
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
	
	
	float[][] accentuateLines(float[][] d, int rad, int repeats){
		
		
		for (int i=0; i<repeats; i++){
			d=lineAccentuator2(d, rad);
		}
		return d;
		
	}
	
	private float[][] lineAccentuator2(float[][] o, int rad1){
		
		int x=o.length;
		int y=o[0].length;
		float[][] out2=new float[x][y];
		int diam=2*rad1+1;
		float inv=(float)(1/(diam+0.0));
		
		int[][] k=makeKernels(rad1);		
	
		for (int a=0; a<x; a++){			
			for (int b=0; b<y; b++){
				double bestsc=Float.NEGATIVE_INFINITY;
				double bestsc2=0;
				int loc=0;
				for (int c=0; c<k.length; c++){
					for (int d2=0; d2<2; d2++){
						double sc=0;
						double sc2=0;
						double scm=Double.NEGATIVE_INFINITY;
						double co=0;
						for (int d=0; d<=rad1; d++){
							int aa=a+k[c][d+(rad1*d2)];
								if ((aa>=0)&&(aa<x)){
									int bb=b+k[c][d+diam+(rad1*d2)];
									if ((bb>=0)&&(bb<y)){
										sc+=(o[aa][bb]-o[a][b])*(o[aa][bb]-o[a][b]);
										sc2+=o[aa][bb];
										if (o[aa][bb]>scm){scm=o[aa][bb];}
										co++;
									}
								}
						}
						sc=Math.sqrt(sc);
						sc/=co;
						sc2/=co;
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