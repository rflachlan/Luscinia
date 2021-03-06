package lusc.net.github.analysis;
//
//  BasicStatistics.java
//  Luscinia
//
//  Created by Robert Lachlan on 7/27/07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//
import java.util.*;

/**
 * BasicStatistics is a utility class containing methods for some standard summary
 * statistics  - as applied to single and double-indexed arrays.
 * @author Rob
 *
 */

public class BasicStatistics {

	/**
	 * casts float data into double.
	 * @param data in put array
	 * @return output double[] array
	 */
	public double[] getDoubleVals(float[] data){
		int n=data.length;
		double[]data2=new double[n];
		for (int i=0; i<n; i++){
			data2[i]=data[i];
		}
		return (data2);
	}
	
	/**
	 * copies double data into a new double array
	 * @param data input array
	 * @return	output double[] array
	 */
	public double[] getDoubleVals(int[] data){
		int n=data.length;
		double[]data2=new double[n];
		for (int i=0; i<n; i++){
			data2[i]=data[i];
		}
		return (data2);
	}
	
	/**
	 * Calculates the mean of an input array.
	 * @param data input double[] array
	 * @return a double value of the mean of the array
	 */
	public double calculateMean(double[] data){
		int n=data.length;
		double sum=0;
		for (int i=0; i<n; i++){
			sum+=data[i];
		}
		return (sum/(n+0.0));
	}
	
	/**
	 * Calculates the mean of an input array.
	 * @param data input float[] array
	 * @return a double value of the mean of the array
	 */
	public double calculateMean(float[] data){
		double[] data2=getDoubleVals(data);
		return(calculateMean(data2));
	}
	
	/**
	 * Calculates the mean of an input array.
	 * @param data input int[] array
	 * @return a double value of the mean of the array
	 */
	public double calculateMean(int[] data){
		double[] data2=getDoubleVals(data);
		return (calculateMean(data2));
	}
	
	/**
	 * Calculates the mean of a 2-D input array.
	 * @param data input double[][] array
	 * @return a double value of the mean of the array
	 */
	public double calculateMean(double[][] data){
		int n=data.length;
		double count1=0;
		double count2=0;
		for (int i=0; i<n; i++){
			int m=data[i].length;
			for (int j=0; j<m; j++){
				count1+=data[i][j];
				count2++;
			}
		}
		return (count1/count2);
	}
	
	/**
	 * Calculates the mean of a 2-D input array.
	 * @param data input float[][] array
	 * @return a double value of the mean of the array
	 */
	public float calculateMean(float[][] data){
		int n=data.length;
		double count1=0;
		double count2=0;
		for (int i=0; i<n; i++){
			int m=data[i].length;
			for (int j=0; j<m; j++){
				count1+=data[i][j];
				count2++;
			}
		}
		return (float)(count1/count2);
	}
	
	/**
	 * Calculates the sum of squares of an input array.
	 * @param data input double[] array
	 * @return a double value of the sum of squares of the array
	 */
	public double calculateSumSquares(double[] data){
		double ss=0;
		double mean=calculateMean(data);
		int n=data.length;
		double a=0;
		for (int i=0; i<n; i++){
			a=data[i]-mean;
			ss+=a*a;
		}
		return ss;
	}
	
	/**
	 * Calculates the sum of squares of a 2-D input array.
	 * @param data input double[][] array
	 * @return a double value of the sum of squares of the array
	 */
	public double calculateSumSquares(double[][] data){
		double ss=0;
		double mean=calculateMean(data);
		int n=data.length;
		double a=0;
		for (int i=0; i<n; i++){
			for (int j=0; j<i; j++){
				a=data[i][j]-mean;
				ss+=a*a;
			}
		}
		return ss;
	}
	
	/**
	 * Calculates the sum of squares of a 2-D input array.
	 * @param data input float[][] array
	 * @return a double value of the sum of squares of the array
	 */
	public float calculateSumSquares(float[][] data){
		double ss=0;
		double mean=calculateMean(data);
		int n=data.length;
		double a=0;
		for (int i=0; i<n; i++){
			for (int j=0; j<i; j++){
				a=data[i][j]-mean;
				ss+=a*a;
			}
		}
		return (float)ss;
	}
	
	
	
	/**
	 * Calculates the standard deviation of an input array.
	 * @param data data input double[] array
	 * @param adjustSample a boolean value that determines whether the denominator is n-1 (true) or n (false)
	 * @return double valued standard deviation
	 */
	public double calculateSD(double[] data, boolean adjustSample){
		double ss=calculateSumSquares(data);	
		double a=0;
		if (adjustSample){a=1.0;}
		int n=data.length;
		double n2=n-a;
		return Math.sqrt(ss/n2);
	}

	/**
	 * Calculates the standard deviation of an input array.
	 * @param data data input float[] array
	 * @param adjustSample a boolean value that determines whether the denominator is n-1 (true) or n (false)
	 * @return double valued standard deviation
	 */
	public double calculateSD(float[] data, boolean adjustSample){
		double[] data2=getDoubleVals(data);
		return(calculateSD(data2, adjustSample));
	}
	
	
	/**
	 * Calculates the standard deviation of an input array.
	 * @param data data input int[] array
	 * @param adjustSample a boolean value that determines whether the denominator is n-1 (true) or n (false)
	 * @return double valued standard deviation
	 */
	public double calculateSD(int[] data, boolean adjustSample){
		double[] data2=getDoubleVals(data);
		return (calculateSD(data2, adjustSample));
	}
	
	
	/**
	 * Calculates the standard deviation of an input array.
	 * @param data data input double[][] array
	 * @param adjustSample a boolean value that determines whether the denominator is n-1 (true) or n (false)
	 * @return double valued standard deviation
	 */
	public double calculateSD(double[][] data, boolean adjustSample){
		double ss=calculateSumSquares(data);
		double a=0;
		if (adjustSample){a=1.0;}
		int n=data.length*(data.length+1)/2;
		double n2=n-a;
		return Math.sqrt(ss/n2);
	}
	
	/**
	 * Calculates the standard deviation of an input array.
	 * @param data data input float[][] array
	 * @param adjustSample a boolean value that determines whether the denominator is n-1 (true) or n (false)
	 * @return double valued standard deviation
	 */
	public double calculateSD(float[][] data, boolean adjustSample){
		double ss=calculateSumSquares(data);
		double a=0;
		if (adjustSample){a=1.0;}
		int n=data.length*(data.length-1)/2;
		double n2=n-a;
		return Math.sqrt(ss/n2);
	}
	
	/**
	 * Calculates the median of an input array
	 * @param data input double[] array
	 * @return the median value of the array.
	 */
	public double calculateMedian(double[] data){
		int n=data.length;
		double[] data2=new double[n];
		System.arraycopy(data, 0, data2, 0, n);
		Arrays.sort(data2);
		boolean evenOrOdd=true;
		int n2=n/2;
		double n3=n*0.5;
		double MINVAL=0.00001;
		if (Math.abs(n2-n3)>MINVAL){
			evenOrOdd=false;
		}
		double result=0;
		if (evenOrOdd){
			double a=data2[n2];
			double b=data2[n2-1];
			result=0.5*(a+b);
		}
		else{
			int n4=(int)Math.floor(n3);
			result=data2[n4];
		}
		data2=null;
		return result;
	}
	
	
	/**
	 * Calculates percentile values of an input array.
	 * @param data a double[] input array
	 * @param percentile the percentile to calculate
	 * @param upperOrLower returns the upper (true) or lower (false) percentile
	 * @return the requested percentile as a double
	 */
	public double calculatePercentile(double[] data, double percentile, boolean upperOrLower){
		int n=data.length;
		double[] data2=new double[n];
		System.arraycopy(data, 0, data2, 0, n);
		Arrays.sort(data2);
		int p=(int)Math.floor(percentile*0.01*n);
		if (upperOrLower){
			p=n-p-1;
		}
		double result=data2[p];
		data2=null;
		return result;	
	}
	
	/**
	 * Calculates percentile values of an input array.
	 * @param data a float[] input array
	 * @param percentile the percentile to calculate
	 * @param upperOrLower returns the upper (true) or lower (false) percentile
	 * @return the requested percentile as a float
	 */
	public float calculatePercentile(float[] data, double percentile, boolean upperOrLower){
		int n=data.length;
		float[] data2=new float[n];
		System.arraycopy(data, 0, data2, 0, n);
		Arrays.sort(data2);
		int p=(int)Math.floor(percentile*0.01*n);
		if (upperOrLower){
			p=n-p-1;
		}
		float result=data2[p];
		data2=null;
		return result;	
	}
	
	/**
	 * Calculates percentile values of an input array.
	 * @param data an int[] input array
	 * @param percentile the percentile to calculate
	 * @param upperOrLower returns the upper (true) or lower (false) percentile
	 * @return the requested percentile as an int
	 */
	public int calculatePercentile(int[] data, double percentile, boolean upperOrLower){
		int n=data.length;
		int[] data2=new int[n];
		System.arraycopy(data, 0, data2, 0, n);
		Arrays.sort(data2);
		int p=(int)Math.floor(percentile*0.01*n);
		if (upperOrLower){
			p=n-p-1;
		}
		int result=data2[p];
		data2=null;
		return result;	
	}
	
	
}
