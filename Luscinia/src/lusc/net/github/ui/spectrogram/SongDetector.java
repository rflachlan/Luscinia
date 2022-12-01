package lusc.net.github.ui.spectrogram;

import lusc.net.github.*;
import java.util.*;

public class SongDetector {

	Song song;
	SpectrPane sp;
	
	
	public SongDetector(SpectrPane sp) {
		this.sp=sp;
		this.song=sp.song;
		
		
		
	}
	
	
	public void detectFreqRange(int minFreq, int maxFreq, int windowLength, double thresh) {
		sp.makeSpectrogramWholeSong();
		float[][] nout=song.getOut();
		
		int nx=song.getNx();
		int ny=song.getNy();
		double maxf=song.getMaxF()+0.0;
		double dx=song.getDx();
		
		int min=(int)Math.round(ny*minFreq/maxf);
		int max=(int)Math.round(ny*maxFreq/maxf);
		
		int wl=(int)Math.round(windowLength/dx);
		
		if (wl>=nx) {wl=nx-1;}
		
		int wl2=wl/2;
		int wl3=nx-wl2;
		
		
		System.out.println(nx+" "+ny+" "+wl+" "+wl2+" "+wl3+" "+windowLength);
		
		double[] frameInt=new double[nx];
		
		double[] geom=new double[nx];
		double[] arith=new double[nx];
		
		for (int i=min; i<max; i++) {
			for (int j=0; j<nx; j++) {
				if(nout[i][j]>frameInt[j]) {frameInt[j]=nout[i][j];}
				//frameInt[j]+=nout[i][j];
				arith[j]+=Math.pow(10, nout[i][j]-1);
				geom[j]+=nout[i][j];
			}	
		}
		
		double[] wiener=new double[nx];
		
		for (int i=0; i<nx; i++) {
			arith[i]/=max-min-0.0;
			geom[i]=Math.pow(10, ((geom[i]/(max-min+0.0))-1));
			wiener[i]=-1*Math.log(geom[i]/arith[i]);
		}
		//double bins=max-min;
		//for (int i=0; i<nx; i++) {
		//	frameInt[i]/=bins;
		//}
		
		
		double average=0;
		for (int i=0; i<nx; i++) {
			average+=frameInt[i];
		}
		average/=nx+0.0;
		
		double threshx=(average+thresh)*wl;

		System.out.println("AVERAGE INT: "+average+" "+threshx);
		
		double[] rollingav=new double[nx];
		double[] rollingavW=new double[nx];
		
		double sc=0;
		for (int i=0; i<wl; i++) {
			sc+=frameInt[i];
		}
		int p=wl;
		int q=0;
		rollingav[wl2]=sc;
		for (int i=wl2+1; i<wl3; i++) {
			sc-=frameInt[q];
			sc+=frameInt[p];
			rollingav[i]=sc;
			q++;
			p++;
			//System.out.println(i+" "+rollingav[i]+""+threshx);
		}
		
		
		sc=0;
		for (int i=0; i<wl; i++) {
			sc+=wiener[i];
		}
		p=wl;
		q=0;
		rollingavW[wl2]=sc;
		for (int i=wl2+1; i<wl3; i++) {
			sc-=wiener[q];
			sc+=wiener[p];
			rollingavW[i]=sc;
			q++;
			p++;
			//System.out.println(i+" "+rollingavW[i]+" "+rollingav[i]+" "+threshx);
		}
		
		
		
		
		LinkedList<int[]> songlocs=new LinkedList<int[]>();
		
		int a=0;
		while (a<nx) {
			if (rollingav[a]<threshx) {
				a++;
			}
			else {
				int[]loc=new int[2];
				loc[0]=a-wl2;
				if (loc[0]<0) {loc[0]=0;}
				
				while ((a<nx)&&(rollingav[a]>=threshx)) {
					a++;
				}
				loc[1]=a+wl2;
				if (loc[1]>=nx) {
					loc[1]=nx-1;
				}
				boolean found=false;
				for (int[] t : songlocs) {
					if (t[1]>loc[0]) {
						songlocs.remove(t);
						t[1]=loc[1];
						songlocs.add(t);
						found=true;
					}
				}
				if (!found) {
					songlocs.add(loc);
				}
			}
		}
		
		updateSongLocs(songlocs);
		song.setFFTParameters();
		sp.restart();
		
	}
	
	
	public void updateSongLocs(LinkedList<int[]> songlocs) {
		
		double dx=song.getDx();
		
		for (int[] x: songlocs) {
			System.out.println("Song detected: "+x[0]+" "+x[1]);
			int[] s={(int)Math.round(dx*x[0]), (int)Math.round(dx*x[1])};
			int p=s[0]+s[1];
			int count=0;
			LinkedList<Syllable> syls=song.getSyllList();
			for (Syllable sy: syls){
				int[] s2=sy.getLoc();
				int p2=s2[0]+s2[1];
				if (p2<p){count++;}
			}
			song.addSyllable(count, s);
		}
		
		
		
	}
	
	
}
