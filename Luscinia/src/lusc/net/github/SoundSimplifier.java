package lusc.net.github;
//
//  SoundSimplifier.java
//  Luscinia
//
//  Created by Robert Lachlan on 06/10/2005.
//  Copyright 2005 __MyCompanyName__. All rights reserved.
//

public class SoundSimplifier {
	int data2[][];
	
	public SoundSimplifier(int[][] data, int p){
		
		double eval=Math.log(2);
		int size=data.length;
		boolean[] count=new boolean[size];
		for (int i=0; i<size; i++){count[i]=true;}
		double[] model_scores=new double[size];
		double[] replacement_scores=new double[size];
		for (int i=0; i<size; i++){replacement_scores[i]=data[i][p];}
		double mdl_old=1000000;
		double mdl_new=1000000;
		double cum_data_score=0;
		int num_left=size-2;
		double top_score, score, temp;
		int h, i, j, k, loc;
		while ((mdl_old>=mdl_new)&&(num_left>0)){
			mdl_old=mdl_new;
			top_score=10000000;
			loc=0;
			score=0;
			for (i=1; i<size-1; i++){
				if (count[i]){
					j=i-1;
					while (!count[j]){j--;}
					k=i+1;
					while (!count[k]){k++;}
					score=0;
					temp=(data[k][p]-data[j][p])/(k-j+0.0);
					for (h=j+1; h<k; h++){
						//score-=model_scores[h];
						score+=Math.abs(data[j][p]+temp*(h-j)-data[h][p]);
					}
					if (top_score>score){
						top_score=score;
						loc=i;
					}
				}
			}
			j=loc-1;
			while (!count[j]){j--;}
			k=loc+1;
			while (!count[k]){k++;}
			temp=(data[k][p]-data[j][p])/(k-j+0.0);
			for (h=j+1; h<k; h++){
				cum_data_score-=model_scores[h];
				replacement_scores[h]=data[j][p]+temp*(h-j);
				model_scores[h]=Math.abs(data[j][p]+temp*(h-j)-data[h][p]);
				cum_data_score+=model_scores[h];
			}
			count[loc]=false;
			num_left--;
			//cum_data_score+=Math.log(top_score+1)/eval;
			//System.out.println("a "+loc+" "+top_score+" "+Math.log(top_score+1)/eval+" "+cum_data_score);
			mdl_new=24*num_left+cum_data_score;
		}
		/*
		data2=new int[num_left+2][];
		j=0;
		for (i=0; i<size; i++){
			if (count[i]){
				k=data[i].length;
				data2[j]=new int[k];
				for (h=0; h<k; h++){
					data2[j][h]=data[i][h];
				}
				//System.out.println(data2[j][0]+" "+data2[j][1]);
				j++;
			}
		}
		*/
		data2=new int[size][];
		for (i=0; i<size; i++){
			k=data[i].length;
			data2[i]=new int[k];
			for (j=0; j<k; j++){
				data2[i][j]=data[i][j];
			}
			data2[i][p]=(int)replacement_scores[i];
			//System.out.println(data2[i][p]);
		}
		
		//System.out.println();
	}

}
