package lusc.net.github.ui.db;

import javax.swing.*;
import javax.swing.event.*;

import java.awt.event.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.awt.*;
import java.text.*;
import java.util.*;

import javax.swing.table.AbstractTableModel;

import lusc.net.github.db.DataBaseController;

public class UserManagement extends JPanel implements ActionListener, TableModelListener{
	
	DataBaseController dbc;
	JFrame f=new JFrame();
	JTable table;
	ArrayList<Object[]> dat;
	JButton dropUser, addUser, changePassword;
	boolean DEBUG=true;
	
	private static String DROP_USER_COMMAND = "drop user";
	private static String ADD_USER_COMMAND = "add user";
    private static String PWORD_COMMAND = "pword";
	
	public UserManagement(DataBaseController dbc){
		this.dbc=dbc;
		
		LinkedList<String[]> s=dbc.getUserList();
		
		dat=new ArrayList<Object[]>();
		
		System.out.println("LENGTH: "+s.size());
		for (int i=0; i<s.size(); i++){
			String[] a=(String[])s.get(i);
			Object[] b=new Object[a.length];
			for (int j=0; j<a.length; j++){
				System.out.println("UNIT: "+a[j]);
				if (j==0){b[0]=a[j];}
				else if (j==1){
					if (a[j].equals("true")){
						b[1]=new Boolean(true);
					}
					else{
						b[1]=new Boolean(false);
					}
				}
			}
			dat.add(b);
		}
		
		table = new JTable(new MyTableModel());
		table.setRowHeight(25);
        table.setPreferredScrollableViewportSize(new Dimension(500, 200));
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getModel().addTableModelListener(this);
        
		
		dropUser=new JButton("Drop User");
		dropUser.addActionListener(this);
		dropUser.setActionCommand(DROP_USER_COMMAND);
		addUser=new JButton("Add User");
		addUser.addActionListener(this);
		addUser.setActionCommand(ADD_USER_COMMAND);
		changePassword=new JButton("Change Password");
		changePassword.addActionListener(this);
		changePassword.setActionCommand(PWORD_COMMAND);
		
		String t=dbc.getURLinfo();
		String u=" ";
		if (t!=null){
			u=t.substring(t.indexOf("://")+3);
		}
		
		JTextField infolabel=new JTextField(u);
		Font font=new Font("Sans-Serif", Font.PLAIN, 9);
		infolabel.setFont(font);
		JPanel sidebar=new JPanel(new BorderLayout());
		JPanel topbar=new JPanel();
		topbar.add(addUser);
		topbar.add(dropUser);
		topbar.add(changePassword);
		sidebar.add(topbar, BorderLayout.CENTER);
		sidebar.add(infolabel, BorderLayout.NORTH);
		
		JScrollPane scrollPane = new JScrollPane(table);
		this.setLayout(new BorderLayout());
		this.add(scrollPane, BorderLayout.CENTER);
		this.add(sidebar, BorderLayout.NORTH);
		f.requestFocus();
		f.setTitle("User Management");
		f.getContentPane().add(this);
		f.pack();
		f.setVisible(true);
		
		
	}
	
	public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        try{
			if (DROP_USER_COMMAND.equals(command)) {
				int a=table.getSelectedRow();
				Object[] x=dat.get(a);
				String s=(String)x[0];
				//String s=(String)dat[a][0];
				dbc.dropUser(s);
				((MyTableModel)table.getModel()).removeRow(a);
				table.revalidate();
			}
			if (ADD_USER_COMMAND.equals(command)) {
				JTextField unameField = new JTextField(20);
				JPasswordField pField1 = new JPasswordField(20);
				JPasswordField pField2 = new JPasswordField(20);
			    
			    JPanel myPanel = new JPanel(new GridLayout(0,2));
			    myPanel.add(new JLabel("User name:"));
			    myPanel.add(unameField);
			    myPanel.add(Box.createHorizontalStrut(15)); // a spacer
			    myPanel.add(Box.createHorizontalStrut(15)); 
			    myPanel.add(new JLabel("Password:"));
			    myPanel.add(pField1);
			    myPanel.add(Box.createHorizontalStrut(15)); // a spacer
			    myPanel.add(Box.createHorizontalStrut(15)); 
			    myPanel.add(new JLabel("Repeat Password:"));
			    myPanel.add(pField2);

			    int result = JOptionPane.showConfirmDialog(null, myPanel, 
			               "Please Enter User Name and Password", JOptionPane.OK_CANCEL_OPTION);
			    if (result == JOptionPane.OK_OPTION) {
			    	String p1=new String(pField1.getPassword());
			    	String p2=new String(pField2.getPassword());
			    	String u=unameField.getText();
			    	if (!p1.equals(p2)){
			    		JOptionPane.showMessageDialog(this, "Passwords did not match");
			    	}
			    	else if(u.equals("")){
			    		JOptionPane.showMessageDialog(this, "User name was blank");
			    	}
			    	else{
			    		((MyTableModel)table.getModel()).addRow(u);
			    		dbc.addUserPassword(u, p1);
			    	}
			    }
				//String p=JOptionPane.showInputDialog("Add new user name:");
				//((MyTableModel)table.getModel()).addRow(p);
				//dbc.addUser(p);
			}
			if (PWORD_COMMAND.equals(command)){
				JPasswordField pField1 = new JPasswordField(20);
				JPasswordField pField2 = new JPasswordField(20);
			    
			    JPanel myPanel = new JPanel(new GridLayout(0,2));
			    myPanel.add(new JLabel("Password:"));
			    myPanel.add(pField1);
			    myPanel.add(Box.createHorizontalStrut(15)); // a spacer
			    myPanel.add(Box.createHorizontalStrut(15)); 
			    myPanel.add(new JLabel("Password:"));
			    myPanel.add(pField2);

			    int result = JOptionPane.showConfirmDialog(null, myPanel, 
			               "Please Enter New Password", JOptionPane.OK_CANCEL_OPTION);
			    if (result == JOptionPane.OK_OPTION) {
			    	String p1=new String(pField1.getPassword());
			    	String p2=new String(pField2.getPassword());
			    	if (!p1.equals(p2)){
			    		JOptionPane.showMessageDialog(this, "Passwords did not match");
			    	}
			    	else{
			    		int a=table.getSelectedRow();
						Object[] x=dat.get(a);
						String s=(String)x[0];
						Boolean b=(Boolean)x[1];
			    		dbc.setPassword(s, p1);
			    	}
			    }
				
				
				/*
				JPasswordField pf = new JPasswordField();
				int okCxl = JOptionPane.showConfirmDialog(null, pf, "Enter Password", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

				if (okCxl == JOptionPane.OK_OPTION) {
				  String password = new String(pf.getPassword());
				  int a=table.getSelectedRow();
				  Object[] x=dat.get(a);
				  String s=(String)x[0];
				  Boolean b=(Boolean)x[1];
				  dbc.setPassword(s,password);
				}
				*/
			}
				
		}
		catch(Exception error){
			System.out.println(error);
			error.printStackTrace();
		}
    }
	
	public void tableChanged(TableModelEvent e) {
        int row = e.getFirstRow(); 
        Object[] x=dat.get(row);
        System.out.println(x[1]);
        dbc.alterAdmin((String)x[0], (Boolean)x[1]);
    }
	
	
	class MyTableModel extends AbstractTableModel {
        private String[] columnNames = {"User Name", "Administrator"};
        ArrayList<Object[]> arr = dat;
        //private Object[][] data = dat;
 
        public int getColumnCount() {
            return columnNames.length;
        }
 
        public int getRowCount() {
            return arr.size();
        }
 
        public String getColumnName(int col) {
            return columnNames[col];
        }
 
        public Object getValueAt(int row, int col) {
        	Object[] x=arr.get(row);
            return x[col];
        }
        
        /*
         * JTable uses this method to determine the default renderer/
         * editor for each cell.  If we didn't implement this method,
         * then the last column would contain text ("true"/"false"),
         * rather than a check box.
         */
        public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }
        
        public void removeRow(int row) {
            arr.remove(row);
            fireTableDataChanged();
        }
        
        public void addRow(String s){
        	Object[] p={s, new Boolean(false)};
        	arr.add(p);
        	fireTableDataChanged();
        }
 
        /*
         * Don't need to implement this method unless your table's
         * editable.
         */
        public boolean isCellEditable(int row, int col) {
            //Note that the data/cell address is constant,
            //no matter where the cell appears onscreen.
            if (col < 1) {
                return false;
            } else {
                return true;
            }
        }
 
        /*
         * Don't need to implement this method unless your table's
         * data can change.
         */
        public void setValueAt(Object value, int row, int col) {
            if (DEBUG) {
                System.out.println("Setting value at " + row + "," + col
                                   + " to " + value
                                   + " (an instance of "
                                   + value.getClass() + ")");
            }
 
            Object[] x=arr.get(row);
            x[col] = value;
            fireTableCellUpdated(row, col);
 
            if (DEBUG) {
                System.out.println("New value of data:");
                printDebugData();
            }
        }
 
        private void printDebugData() {
            int numRows = getRowCount();
            int numCols = getColumnCount();
 
            for (int i=0; i < numRows; i++) {
                System.out.print("    row " + i + ":");
                Object[] x=arr.get(i);
                for (int j=0; j < numCols; j++) {
                    System.out.print("  " + x[j]);
                }
                System.out.println();
            }
            System.out.println("--------------------------");
        }
    }

}
