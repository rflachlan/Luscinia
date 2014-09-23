package lusc.net.sourceforge;
//
//  SaveDocument.java
//  Luscinia
//
//  Created by Robert Lachlan on 13/10/2005.
//  Copyright 2007 Robert Lachlan.
//	Luscinia is provided under the GPL 2.0 software licence. Please see accompanying material for details.

//   This class uses POI, made by the Apache community. It is:
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at

//       http://www.apache.org/licenses/LICENSE-2.0

//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.



import java.util.*;
import java.io.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import org.apache.poi.hssf.usermodel.*;
import org.apache.*;


public class SaveDocument {

	Component host;
	Defaults defaults;
	String thpath=null;
	String name=null;
	JFileChooser fc;
	int fileType=0;
	HSSFWorkbook wb;
	HSSFSheet sheet;
	HSSFRow row;
	PrintWriter Results;
	File file;
	short rownum=0;
	short colnum=0;
	Cursor c;
	boolean suppressChoice=false;
	
	public SaveDocument(Component host){
		this.host=host;
		c=host.getCursor();
	}
	
	public SaveDocument(Component host, Defaults defaults){
		this.defaults=defaults;
		this.host=host;
		c=host.getCursor();
		thpath=defaults.props.getProperty("path");
		name=defaults.props.getProperty("name");
		//file=new File(thpath, name);
	}
	
	public SaveDocument(String path, String name){
		thpath=path;
		name=name;
		file=new File(thpath, name);
		suppressChoice=true;
		fileType=2;
	}
	
	public boolean makeFile(){
		
		int returnVal = JFileChooser.APPROVE_OPTION;
		boolean readyToWrite=false;
		if (!suppressChoice){
			String[] formats={"xls", "csv", "txt"};
			
			try{
				thpath=defaults.props.getProperty("path");
				fc=new JFileChooser(thpath);
			}
			catch(Exception e){
				fc=new JFileChooser();
			}
			fc.setDialogType(JFileChooser.SAVE_DIALOG);
			fc.setAcceptAllFileFilterUsed(false);
			
			SpectrogramFileFilter sffxls=new SpectrogramFileFilter("xls");
			sffxls.fileType=0;
			fc.addChoosableFileFilter(sffxls);
			SpectrogramFileFilter sffcsv=new SpectrogramFileFilter("csv");
			sffcsv.fileType=1;
			fc.addChoosableFileFilter(sffcsv);
			SpectrogramFileFilter sfftxt=new SpectrogramFileFilter("txt");
			sfftxt.fileType=2;
			fc.addChoosableFileFilter(sfftxt);
			
			int p=defaults.getDefaultDocFormat();
			if (p==0){
				fc.setFileFilter(sffxls);
			}
			else if (p==1){
				fc.setFileFilter(sffcsv);
			}
			else if (p==2){
				fc.setFileFilter(sfftxt);
			}

			returnVal = fc.showSaveDialog(host);
		}
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			
			int cont=0;
			
			if (!suppressChoice){
				file = fc.getSelectedFile();
				thpath=file.getPath();
				String parent=file.getParent();
				name=file.getName();
				SpectrogramFileFilter sf2=(SpectrogramFileFilter) fc.getFileFilter();
				file=new File(thpath+"."+sf2.names);
				if (defaults!=null){
					defaults.props.setProperty("path", parent);
					defaults.props.setProperty("filename", name);
				}
				if (!thpath.endsWith("."+sf2.names)){file=new File(thpath+"."+sf2.names);}
				else{file=new File(thpath);}
				fileType=sf2.fileType;
				
				defaults.setDefaultDocFormat(fileType);
				
				if (file.exists()){
					cont= JOptionPane.showConfirmDialog(host,"Do you really want to overwrite this file?\n"+"(It will be deleted permanently)","Confirm Overwrite", JOptionPane.YES_NO_OPTION);
				}
			}
			
			if (cont==0){
				
				try{
					if (!suppressChoice){host.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));}
					
					if (fileType==0){
						wb = new HSSFWorkbook();
						sheet = wb.createSheet("Results");
						row = sheet.createRow((short)0);
					}
					else{
						Results = new PrintWriter(new FileWriter(file));
					}

					
					readyToWrite=true;
				}
				catch(IOException e2){
					if (!suppressChoice){
						host.setCursor(c);
						cont= JOptionPane.showConfirmDialog(host,"Couldn't save the file","Try again?", JOptionPane.YES_NO_OPTION);
						if (cont==0){
							//SaveDocument sd=new SaveDocument(data, host);
						}
					}
					else{System.exit(3);}
				}
			}
		}
		if (readyToWrite){
			try{
				defaults.props.setProperty("path", thpath);
				defaults.props.setProperty("name", name);
				defaults.writeProperties();
			}
			catch(Exception e){}
		}
		return readyToWrite;
	}
	
	public void writeSheet(String s){
		if (fileType==0){
			sheet=wb.createSheet(s);
			rownum=0;
			row = sheet.createRow((short)0);
		}
		else if (fileType==1){
			Results.println(csvEntry(s));
		}
		else if (fileType==2){
			Results.println(textEntry(s));
		}
	}
	
	public void setSheetName(String s){
		if (fileType==0){
			wb.setSheetName(0, s);
		}
		else if (fileType==1){
			Results.println(csvEntry(s));
		}
		else if (fileType==2){
			Results.println(textEntry(s));
		}
	}
	
	public void writeBoolean(boolean b){
		if (b){writeString("true");}
		else{writeString("false");}
	}
	
	public void writeString(String s){
		try{
			if (fileType==0){
				row.createCell(colnum).setCellValue(s);
				colnum++;
			}
			else if (fileType==1){
				Results.print(csvEntry(s));
			}
			else if (fileType==2){
				Results.print(textEntry(s));
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void writeInt(int i){
		try{
			if (fileType==0){
				row.createCell(colnum).setCellValue(i);
				colnum++;
			}
			else if (fileType==1){
				Results.print(i+",");
			}
			else if (fileType==2){
				Results.print(i+"\u0009");
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}	
	}
	
	public void writeFloat(float i){
		try{
			if (fileType==0){
				row.createCell(colnum).setCellValue(i);
				colnum++;
			}
			else if (fileType==1){
				Results.print(i+",");
			}
			else if (fileType==2){
				Results.print(i+"\u0009");
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void writeDouble(double i){
		try{
			if (fileType==0){
				row.createCell(colnum).setCellValue(i);
				colnum++;
			}
			else if (fileType==1){
				Results.print(i+",");
			}
			else if (fileType==2){
				Results.print(i+"\u0009");
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	public void writeLine(){
		if (fileType==0){
			row = sheet.createRow(++rownum);
			colnum=0;
		}
		else {
			Results.println();
		}
	}
	
	public void finishWriting(){
		if (fileType==0){
			try{
				FileOutputStream fileOut = new FileOutputStream(file);
				wb.write(fileOut);
				fileOut.close();
				if (defaults!=null){
					defaults.props.setProperty("path", thpath);
					defaults.props.setProperty("filename", name);
				}
			}
			catch (IOException e){
				e.printStackTrace();
			}
		}
		else{
			Results.close();
		}
		if (!suppressChoice){host.setCursor(c);}
	}
	
	public String csvEntry(String s){
		char quot='"';
		StringBuffer sb=new StringBuffer(s);
		boolean encap=true;
		int i=s.lastIndexOf(',');
		int j=s.lastIndexOf(' ');
		if ((i>-1)||(j>-1)){
			sb.insert(0, quot);
			sb.append(quot);
			encap=true;
		}
		j=0; 
		int k=sb.length();
		if (encap){
			j++;
			k--;
		}
		for (i=j; i<k; i++){
			if (sb.charAt(i)==quot){
				sb.insert(i, quot);
				i+=2;
				sb.insert(i, quot);
				k+=2;
			}
		}
		sb.append(",");
		return sb.toString();
	}
	
	public String textEntry(String s){
		String t="\u0009";
		StringBuffer sb=new StringBuffer(s);
		sb.append(t);
		return sb.toString();
	}

}
