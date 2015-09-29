package lusc.net.github.ui.statistics;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import lusc.net.github.analysis.CompareThread;
import lusc.net.github.analysis.PrepareDTW;

	

public class DisplayDTW extends JPanel{
	PrepareDTW pdtw;
	CompareThread ct;
	BufferedImage imf;
	double[][]q;
	
	int width=500;
	int height=500;
	
	public DisplayDTW(int[] wrap, PrepareDTW pdtw, CompareThread ct){
		this.pdtw=pdtw;
		this.ct=ct;
		
		double[][] p=ct.getCompMatrix();
		
		int[] lengths=ct.getLengths();
		double max=0;
		q=new double[lengths[0]][lengths[1]];
		for (int i=0; i<lengths[0]; i++){
			for (int j=0; j<lengths[1]; j++){
				q[i][j]=p[i][j];
				System.out.println(i+" "+j+" "+p[i][j]);
				if (p[i][j]>max){
					max=p[i][j];
				}
			}
		}
		//if (max<2){max=2;}
		//max=5;
		
		for (int i=0; i<lengths[0]; i++){
			for (int j=0; j<lengths[1]; j++){
				q[i][j]/=max;
			}
		}
		
		int maxlength=lengths[0];
		if (lengths[1]>lengths[0]){maxlength=lengths[1];}
		
		
		imf=new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		this.setPreferredSize(new Dimension(width, height));
		Graphics2D g=imf.createGraphics();
		
		double unit=(width-100)/(maxlength+0.0);
		double greyscale=0.3;
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, imf.getWidth(), imf.getHeight());
		int unit2=(int)Math.ceil(unit);
		if (unit2==0){unit2=1;}
		for (int i=0; i<lengths[0]; i++){
			int x=(int)(50+i*unit);
			for (int j=0; j<lengths[1]; j++){
				int y=(int)(unit*lengths[1]+50-unit*j);
				
				float co=(float)Math.pow(q[i][j], greyscale);
				Color c=new Color(co, co, co);
				g.setColor(c);
				g.fillRect(x, y, unit2, unit2);
			}
		}
		
		int[][] trajectory=ct.getTrajectory();
		
		boolean ended=false;
		for (int i=0; i<trajectory.length; i++){
			if(!ended){
				int xco=trajectory[i][0];
				int yco=trajectory[i][1];
				int x=(int)(50+xco*unit);
				int y=(int)(unit*lengths[1]+50-unit*yco);
				float co=(float)Math.pow(q[xco][yco], greyscale);
				Color c=new Color(1, co, co);
				g.setColor(c);
				g.fillRect(x, y, unit2, unit2);
				if ((xco==0)&&(yco==0)){
					ended=true;
				}
			}
		}
		
		g.dispose();
		repaint();
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);  //paint background
		g.drawImage(imf, 0, 0, this);
	}
	

}
