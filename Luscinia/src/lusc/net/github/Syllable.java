package lusc.net.github;

import java.util.LinkedList;

public class Syllable {
	
	LinkedList<Element> eles=new LinkedList<Element>();
	LinkedList<Syllable> children=new LinkedList<Syllable>();
	LinkedList<Syllable> parents=new LinkedList<Syllable>();
	
	int maxLevel=0;
	
	int start=0;
	int end=0;
	
	int id=0;
	
	public Syllable(int[] s, int id){
		start=s[0];
		end=s[1];
		this.id=id;
	}
	
	public void checkMaxLevel(){
		int max=0;
		for (Syllable c: children){
			if (c.maxLevel>max){max=c.maxLevel;}
		}
		maxLevel=max+1;
		
		for (Syllable p: parents){
			p.checkMaxLevel();
		}	
	}
	
	public void addFamily(LinkedList<Syllable> syls, LinkedList<Element> eleList){		
		for (Syllable s : syls){
			if ((this.start>s.start)&&(this.end<s.end)){
				parents.add(s);
				s.children.add(this);
			}
			if ((this.start<s.start)&&(this.end>s.end)){
				children.add(s);
				s.parents.add(this);
			}
		}
		
		for (Element ele : eleList){
			int startE=ele.getBeginTime();
			int endE=startE+ele.getLength();
			if ((start<=startE)&&(end>=endE)){
				eles.add(ele);
				ele.syls.add(this);
			}
		}
	}
	
	
	public void remove(){
		
		for (Syllable p : parents){
			p.children.remove(this);
		}
		
		for (Syllable c : children){
			c.parents.remove(this);
		}
		
		for (Syllable p : parents){
			p.checkMaxLevel();
		}
		
		for (Syllable c : children){
			c.checkMaxLevel();
		}
		
		for (Element e : eles){
			e.syls.remove(this);
		}

	}
	
}
