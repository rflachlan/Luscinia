package lusc.net.github.analysis;

public class Rhythm {
	
	ComparisonResults cr;
	int n;
	public double[] ISI;
	public long[] ISItimes;
	public double nPVI;
	public String[][] ISInames;
	
	public Rhythm(ComparisonResults cr) {
		this.cr=cr;
		
		calculateISIs();
		calculatenPVI();
			
	}
	
	public void calculatenPVI() {
		nPVI=0;
		for (int i=1; i<ISI.length; i++) {
			nPVI+=Math.abs((ISI[i]-ISI[i-1])/(ISI[i]+ISI[i-1]+0.0));
		}
		nPVI*=50/(ISI.length-1.0);
	}
	
	
	public void calculateISIs() {
		long[] times=cr.getTimes();
		int[][] l=cr.getLookUp();
		String[][] names=cr.getNamesArray(false, false, true, true, true);
		n=times.length-1;
		double[] x=new double[n];
		long[] y=new long[n];
		String[][] names2=new String[n][];
		int j=0;
		for (int i=0; i<n; i++) {
			if (l[i][0]==l[i+1][0]) {
				x[j]=0.001*(times[i+1]-times[i]);
				y[j]=times[i];
				names2[j]=names[i];
				j++;
			}
				//System.out.println("R: "+i+" "+ ISI[i]+" "+times[i+1]+" "+times[i]);
		}
		
		ISI=new double[j];
		System.arraycopy(x, 0, ISI, 0, j);
		ISItimes=new long[j];
		System.arraycopy(y, 0, ISItimes, 0, j);
		ISInames=new String[j][];
		System.arraycopy(names2, 0, ISInames, 0, j);
	}
}
