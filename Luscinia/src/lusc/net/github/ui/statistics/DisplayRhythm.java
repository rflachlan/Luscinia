package lusc.net.github.ui.statistics;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;

import lusc.net.github.Defaults;
import lusc.net.github.analysis.Rhythm;
import lusc.net.github.ui.SaveDocument;
import lusc.net.github.ui.SaveImage;

public class DisplayRhythm extends DisplayPane{
	Rhythm r;
	float scaler;
	float fontSize=12f;
	BufferedImage imf;
	int width, height;
	int xshift1=50;
	int yshift1=50;
	int legendSpacerDefault=50;
	double iconSize=2;
	float lineWeight=1.0f;
	float lineWeight2=1.0f;
	int gridSizeDefault=400;
	double[][] location;
	float alpha=0.2f;
	Defaults defaults;
	
	public DisplayRhythm(Rhythm r, Defaults defaults) {
		this.r=r;
		this.defaults=defaults;
		scaler=(float)defaults.getScaleFactor();
		width=800;
		height=800;
		imf=new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_ARGB);
		
		paintPanel(scaler);
		repaint();
		
	}
	
	public float[] getColorScore(float p){
		//float[] z=getRobPalette(p);
		//float[] z=getHSBPalette(p, 0.5f, 1.0f, 0.85f, 1f);
		//Color[] cols={new Color(255,237,160), new Color(254,178,76), new Color(240,59,32)};
		//Color[] cols={new Color(165,0,38), new Color(215,48,39), new Color(244,109,67), new Color(253,174,97), new Color(254,224,144), new Color(245,245,191), new Color(224,233,248), new Color(171,217,233), new Color(116,173,209), new Color(69,117,180), new Color(49,54,149)};
		//Color[] cols={new Color(236,226,240), new Color(208,209,230), new Color(166,189,219), new Color(103,169,207), new Color(54,144,192), new Color(2,129,138), new Color(1,108,89), new Color(1,70,54)};
		//Color[] cols={Color.GREEN, Color.YELLOW, Color.RED};
		Color[] cols={new Color(77,77,255), new Color(235, 235, 100), new Color(239,21,21)};
		
		float[] z=getLinearPalette(p, cols);
		
		return z;
	}
	
	public float[] getLinearPalette(float p, Color[] c){
		
		int n=c.length;
		
		int x=(int)Math.floor(p*(n-1));
		if (x==n-1){x=n-2;}
		
		float y=p*(n-1)-x;
		//System.out.println(n+" "+p+" "+x+" "+y);
		Color a=c[x];
		Color b=c[x+1];
		
		float[] ra=a.getColorComponents(null);
		float[] rb=b.getColorComponents(null);
		
		for (int i=0; i<ra.length; i++){
			ra[i]=rb[i]*y+ra[i]*(1-y);
		}
		return ra;
		
	}
	
	
	public void paintPanel(double ratio){
		
		int w=(int)scaler*width;
		int h=(int)scaler*height;
		
		imf=new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		
		int n=r.ISI.length;
		
		double absmax=0;

		for (int i=0; i<n; i++){
			if (r.ISI[i]>absmax){
				absmax=r.ISI[i];
			}
		}
		//if (absmax>0.4) {absmax=0.4;}

		
		Graphics2D g=imf.createGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0,0,imf.getWidth(), imf.getHeight());
		g.setColor(Color.BLACK);
		BasicStroke fs=new BasicStroke(lineWeight*scaler, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER);
		g.setStroke(fs);
		BasicStroke fs2=new BasicStroke(lineWeight*scaler*2, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER);
		BasicStroke fs3=new BasicStroke(lineWeight*scaler, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER);
		
		
		RenderingHints hints =new RenderingHints(RenderingHints.KEY_RENDERING,
				 RenderingHints.VALUE_RENDER_QUALITY);
				hints.put(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		
		g.setRenderingHints(hints);
		int fsz=(int)Math.round(fontSize*scaler);
		
		Font fontDef=g.getFont();
		Font font=new Font(fontDef.getName(), fontDef.getStyle(), fsz);
		g.setFont(font);
		DecimalFormat nf=new DecimalFormat("#.#");
        FontRenderContext frc = g.getFontRenderContext();
        
       // Font labelFont=new Font(fontDef.getName(), fontDef.getStyle(), fsz/2);
    
		
		int xsh=(int)Math.round(xshift1*scaler);
		int ysh=(int)Math.round(yshift1*scaler);
		
		int legendSpacer=(int)Math.round(legendSpacerDefault*scaler);
		
		int tickLength=(int)Math.round(3*scaler);
		int textSpacer=(int)Math.round(5*scaler);
		
		int barWidth=(int)Math.round(25*scaler);
		int barHeight=(int)Math.round(200*scaler);
		
		double x,y;
		//location=new double[n][2];
		
		int gridsize=(int)Math.round(gridSizeDefault*scaler);
		
		System.out.println("LEGLOC: "+xsh+gridsize+legendSpacer+" "+barWidth);
		
		for (int i=0; i<barHeight; i++){
				
			float ii=i/(barHeight-1.0f);
			float[]f=getColorScore(ii);
			Color tc=new Color(f[0], f[1], f[2]);
			g.setColor(tc);
			g.fillRect(xsh+gridsize+legendSpacer, i+ysh, barWidth, 1);
				
		}
			
		g.setColor(Color.BLACK);
		g.drawRect(xsh+gridsize+legendSpacer, ysh, barWidth, barHeight);
			
		for (int i=0; i<=10; i++){
			int ypl=(int)Math.round(ysh+i*barHeight*0.1);
			g.drawLine(xsh+gridsize+legendSpacer+barWidth, ypl, xsh+gridsize+legendSpacer+barWidth+tickLength, ypl); 
		}
			
		TextLayout layoutS = new TextLayout("Beginning", font, frc);
		Rectangle r1 = layoutS.getPixelBounds(null, 0, 0);
		layoutS.draw(g, xsh+gridsize+legendSpacer+barWidth+textSpacer, ysh+0.5f*r1.height);
		TextLayout layoutE = new TextLayout("End", font, frc);
		Rectangle r2 = layoutE.getPixelBounds(null, 0, 0);
		layoutE.draw(g, xsh+gridsize+legendSpacer+barWidth+textSpacer, ysh+barHeight+0.5f*r2.height);
		TextLayout layoutM = new TextLayout("Mid", font, frc);
		
		layoutM.draw(g, xsh+gridsize+legendSpacer+barWidth+textSpacer, ysh+barHeight*0.5f+0.5f*r2.height);
			
		
		int maxlabel=(int)Math.floor(absmax*10);
		
		for (int i=0; i<=maxlabel; i++) {
			int q=(int)Math.round(i*0.1*gridsize/absmax);
			g.drawLine(xsh+q, ysh+gridsize, xsh+q, ysh+gridsize+tickLength);
			g.drawLine(xsh-tickLength, ysh+gridsize-q, xsh, ysh+gridsize-q);
			double j=i*0.1;
			String s=nf.format(j);
			TextLayout layout = new TextLayout(s, font, frc);
			Rectangle r = layout.getPixelBounds(null, 0, 0);
			float x1=(float)(q-0.5f*r.width+xsh);
			float y1=ysh+gridsize+textSpacer+r.height;
			float x2=xsh-textSpacer-r.width;
			float y2=(float)(ysh+gridsize-q+0.5f*r.height);

			layout.draw(g, x1, y1);
			layout.draw(g, x2, y2);
		}
		
		g.drawLine(xsh, ysh, xsh+gridsize, ysh);
		g.drawLine(xsh, ysh+gridsize, xsh+gridsize, ysh+gridsize);
		
		g.drawLine(xsh, ysh, xsh, ysh+gridsize);
		g.drawLine(xsh+gridsize, ysh, xsh+gridsize, ysh+gridsize);
		
		

	
		g.setStroke(fs3);
		
		
		location=new double[n-1][2];
		
		for (int i=1; i<n; i++){
			
			double px=r.ISI[i-1]/absmax;

			double py=r.ISI[i]/absmax;
			
			x=px*gridsize+xsh;
			y=ysh+gridsize-py*gridsize;
			
			location[i-1][0]=x;
			location[i-1][1]=y;
			//System.out.println("LOC: "+x+" "+y+" "+absmax);
			if (i>=2){
				
				float p=i/(n-1f);
				
				float[] c=getColorScore(p);
				g.setColor(new Color(c[0], c[1], c[2], alpha));
				
				Line2D.Double line=new Line2D.Double(location[i-2][0], location[i-2][1], location[i-1][0], location[i-1][1]);
				g.draw(line);
				
			}
		}
		g.dispose();
		
	}
	
	public void paintComponent(Graphics g){
		super.paintComponent(g);  //paint background
		Graphics2D g2=(Graphics2D) g;
		g2.scale(1/scaler, 1/scaler);
		//System.out.println(imf.getWidth()+" "+imf.getHeight());
		g2.drawImage(imf, 0, 0, this);
	}
	
	public void saveImage(){
		new SaveImage(imf, this, defaults);
		//si.save();
	}
	
	public BufferedImage resizeImage(double ratio){
		paintPanel(ratio);

		BufferedImage imf1=imf;
		
		return imf1;
	}	
	
	public void export(){
		SaveDocument sd=new SaveDocument(this, defaults);
		boolean readyToWrite=sd.makeFile();
		if (readyToWrite){
			sd.writeString("Time (ms)");
			sd.writeString("ISI (s)");
			sd.writeLine();
			for (int i=0; i<r.ISI.length; i++) {
				sd.writeLong(r.ISItimes[i]);
				sd.writeDouble(r.ISI[i]);
				sd.writeLine();
			}

			sd.finishWriting();
		}
	}
	
}
