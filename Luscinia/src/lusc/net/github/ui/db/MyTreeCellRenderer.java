package lusc.net.github.ui.db;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import lusc.net.github.Defaults;
import lusc.net.github.Song;
import lusc.net.github.db.DataBaseController;

public class MyTreeCellRenderer extends DefaultTreeCellRenderer{

	DataBaseController dbc;
	Defaults defaults;
	
	
	public MyTreeCellRenderer(DataBaseController dbc, Defaults defaults){
		this.dbc=dbc;
		this.defaults=defaults;
	}
	
	public Component getTreeCellRendererComponent(JTree tree, Object  value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus){
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
		
		
		myNode n=(myNode)((DefaultMutableTreeNode) value);
		
		if (n.song){
			
			//Song song=dbc.loadSongFromDatabase(n.dex, 0);
			
			//int p=song.getNumElements();

			//if (p==0){
				//setForeground(Color.GRAY);
			//}
			
			if (!n.isMeasured){
				setForeground(Color.GRAY);
			}
			
		}
		
		if (leaf){
			
		}
		
		return this;
	}
	
	
	
}
