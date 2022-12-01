package lusc.net.github.analysis;

import java.util.Calendar;

import lusc.net.github.Song;

public class IndividualDays {

	Song[] songs;
	
	public IndividualDays(Song[] songs) {
		this.songs=songs;
		
	}
	
	public void setIndividualDayId(int[] x) {
		int n=songs.length;
		for (int i=0; i<n; i++) {
			songs[i].setIndividualDayID(x[i]);
		}
	}
	
	public void calculateTimeCategoriesByDay() {
		int n=songs.length;
		int[] date=new int[n];
		int[] inds=new int[n];
		
		for (int i=0; i<n; i++) {
			Calendar cal=Calendar.getInstance();
			cal.setTimeInMillis(songs[i].getTDate());
			
			int day=cal.get(Calendar.DAY_OF_MONTH);
			int monthid=cal.get(Calendar.MONTH);
			int year=cal.get(Calendar.YEAR);
			
			int x=year*365+monthid*12+day;
			date[i]=x;
			inds[i]=songs[i].getIndividualID();
		}
		
		int a=1;
		int[] di=new int[n];
		for (int i=0; i<n; i++) {
			boolean found=false;
			for (int j=0; j<i; j++) {
				if ((date[i]==date[j])&&(inds[i]==inds[j])) {
					di[i]=di[j];
					j=i;
					found=true;
				}
			}
			if (!found) {
				di[i]=a;
				a++;
			}
			System.out.println("INDIVIDUAL DAY!: "+di[i]);
		}
		setIndividualDayId(di);
	}
	
	public void calculateTimeCategoriesByDayAndAmount(int nsongs, int timemax) {
		int n=songs.length;
		int[] date=new int[n];
		int[] inds=new int[n];
		int minx=Integer.MAX_VALUE;
		int maxx=Integer.MIN_VALUE;
		
		
		for (int i=0; i<n; i++) {
			Calendar cal=Calendar.getInstance();
			cal.setTimeInMillis(songs[i].getTDate());
			
			int day=cal.get(Calendar.DAY_OF_YEAR);
			//int monthid=cal.get(Calendar.MONTH);
			int year=cal.get(Calendar.YEAR);
			
			int x=year*365+day;
			if (x<minx) {minx=x;}
			if (x>maxx) {maxx=x;}
			date[i]=x;
			inds[i]=songs[i].getIndividualID();
		}
		
		int a=0;
		int[] di=new int[n];
		double[] dates=new double[n];
		int[] counts=new int[n];
		int[] inds2=new int[n];
		
		for (int i=0; i<n; i++) {
			boolean found=false;
			for (int j=0; j<i; j++) {
				if ((date[i]==date[j])&&(inds[i]==inds[j])) {
					di[i]=di[j];
					j=i;
					found=true;
					counts[di[j]]++;
				}
			}
			if (!found) {
				di[i]=a;
				dates[a]=date[i];
				counts[a]=1;
				inds2[a]=inds[i];
				a++;
				
			}
			//System.out.println("INDIVIDUAL DAY!: "+di[i]);
		}
		boolean completed=false;
		while (!completed) {
			completed=true;
			for (int i=1; i<a; i++) {
				if ((counts[i]>0)&&(counts[i]<nsongs)){
					double ddif=timemax;
					int id=-1;
					for (int j=0; j<a; j++) {
						if(i!=j) {
							double p=Math.abs(dates[i]-dates[j]);
							if ((counts[j]>0)&&(p<ddif)&&(inds2[i]==inds2[j])) {
								ddif=p;
								id=j;
							}
						}
					}
					//System.out.println(i+" "+id+" "+ddif+" "+counts[i]);
					if (id>=0) {
						completed=false;
						for (int j=0; j<n; j++) {
							if (di[j]==i) {
								di[j]=id;
							}
						}
						dates[id]=(counts[i]*dates[i]+counts[id]*dates[id])/(counts[i]+counts[id]+0.0);
						counts[id]+=counts[i];
						counts[i]=0;
						i=a;
					}
				}
			}	
		}

		/*
		for (int i=0; i<a; i++) {
			System.out.println("ID BY DAY: "+i+" "+counts[i]+" "+dates[i]);
		}
		
		for (int i=0; i<n; i++) {
			System.out.println("ID: "+i+" "+date[i]+" "+di[i]);
		}
		*/
		
		int dc=0;
		for (int i=0; i<a; i++) {
			if (counts[i]>0) {dc++;}
		}
		
		
		int[] di2=new int[n];
		
		for (int i=0; i<dc; i++) {
			double mindate=Integer.MAX_VALUE;
			int loc=-1;
			for (int j=0; j<a; j++) {
				if ((counts[j]>0)&&(dates[j]<mindate)){
					mindate=dates[j];
					loc=j;
				}
			}
			
			for (int j=0; j<n; j++) {
				if (di[j]==loc) {di2[j]=i+1;}
			}
			
			dates[loc]=Integer.MAX_VALUE;
			
			
		}
		
		for (int i=0; i<n; i++) {
			System.out.println("ID: "+i+" "+date[i]+" "+di[i]+" "+di2[i]);
		}
		
		setIndividualDayId(di2);
		
	}
	
	
}

