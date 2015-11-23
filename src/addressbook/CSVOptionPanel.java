/* jaddressbook - CSVOptionPanel.java
 * Copyright (C) 1999-2005 Dmitriy Rogatkin.  All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *  THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 *  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 *  ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE FOR
 *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *  $Id: CSVOptionPanel.java,v 1.2 2007/02/09 07:27:49 rogatkin Exp $
 * Created on Jan 18, 2005
 */

package addressbook;

import java.awt.event.ActionEvent;
import java.awt.Frame;
import java.awt.Component;
import java.io.File;
import java.util.List;

import java.io.IOException;
import java.io.FileWriter;
import java.io.Writer;


import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JDialog;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.JCheckBox;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.AbstractAction;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.DefaultCellEditor;
import javax.swing.SwingUtilities;

import org.aldan3.app.ui.FixedGridLayout;

/**
 * @author dmitriy
 *
 * 
 */
public class CSVOptionPanel extends JPanel {
    static final String [] COL_NAMES = {"Name", "SR", "X"};
    static final String[] CONT_FIELDS = {"First", "Last", "Full", "Phone", "E-mail", "Address", "URL", "Type", "Note"};

    JTable tb_mapping;
    JTextField tf_saveFile;
    int returnOption = JFileChooser.CANCEL_OPTION;
    
    CSVOptionPanel() {
        setLayout(new FixedGridLayout(6, 11, AddressBookResources.CTRL_VERT_SIZE, AddressBookResources.CTRL_VERT_GAP,
				   AddressBookResources.CTRL_HORIS_INSET, AddressBookResources.CTRL_HORIZ_GAP));
        add(new JLabel("Select default"), "0,0,0");
        add(new JComboBox(AddressBookResources.LABELS_IMP_EXP_FMT_NAME), "0,1,4");
        add(new JLabel("Define CSV fields mapping"), "0,2,0");
        add(new JScrollPane(tb_mapping = new JTable(new CSVMappingModel())), "0,3,6,5");
        add(new JCheckBox("Add header line"), "0,8,0");
        add(tf_saveFile = new JTextField(), "0,9,4"); add(new JButton(new AbstractAction("...") {
            public void actionPerformed(ActionEvent ae) {
		        JFileChooser chooser = new JFileChooser() {
					public boolean accept(File f) {
						return f.isDirectory() || f.getName().toLowerCase().endsWith(".csv");
					}
				};
				if (chooser.CANCEL_OPTION != chooser.showOpenDialog(SwingUtilities.getWindowAncestor(CSVOptionPanel.this))) {
				    tf_saveFile.setText(chooser.getSelectedFile().getPath());
				}
	        }
            }), "4,9,0");
        add(new JButton(new AbstractAction("Export") {
            public void actionPerformed(ActionEvent ae) {
                if (tf_saveFile.getText().trim().length() == 0)
                    return;
                returnOption = JFileChooser.APPROVE_OPTION;
                SwingUtilities.getWindowAncestor(CSVOptionPanel.this).dispose();
            }
            }), "2,10,2");
        add(new JButton(new AbstractAction("Cancel") {
            public void actionPerformed(ActionEvent ae) {
                SwingUtilities.getWindowAncestor(CSVOptionPanel.this).dispose();
            }
            }), "4,10,2");
        TableColumn col = tb_mapping.getColumnModel().getColumn(0);
        col.setCellEditor(new CSVFieldComboBoxEditor(CONT_FIELDS));
        col.setCellRenderer(new CSVFieldComboBoxRenderer(CONT_FIELDS));
        for (int i=1;i<3;i++) {
            col = tb_mapping.getColumnModel().getColumn(i);
            col.setCellEditor(new CSVFieldCheckBoxEditor());
            col.setCellRenderer(new CSVFieldCheckBoxRenderer());
        }
    }
    
    
    static void doExport (Folder folder, Frame w) {
        JDialog jd = new JDialog(w, "Define CSV Mapping");
        jd.setModal(true);
        CSVOptionPanel op = new CSVOptionPanel(); 
        jd.setContentPane(op);
        jd.pack();
        jd.setVisible(true);
        if (op.returnOption == JFileChooser.APPROVE_OPTION) {
            try {
                FileWriter wr = new FileWriter(op.tf_saveFile.getText());
                List cv = folder.getContent();
                for (int i=0; i<cv.size(); i++) {
                    Contact c = (Contact)cv.get(i);
                    TableModel tm = op.tb_mapping.getModel();
                    List ts = c.getTelephones();
                    List as = c.getAddresses();
                    List es = c.getEMails();
                    List ls = c.getLinks();
                    int fi=0, ai=0, ei=0, li=0;
                    for(int m=0; m<tm.getRowCount();m++) {
                        if (Boolean.FALSE.equals(tm.getValueAt(m,2)))
                                continue;
                        if (m>0)
                            wr.write(',');
                        
                        if (CONT_FIELDS[0].equals(tm.getValueAt(m,0))) {
                            wr.write(c.getName().getFirst());
                        } else if (CONT_FIELDS[1].equals(tm.getValueAt(m,0))) {
                            wr.write(c.getName().getLast());
                        } if (CONT_FIELDS[2].equals(tm.getValueAt(m,0))) {
                            wr.write(c.getName().toString());
                        } if (CONT_FIELDS[3].equals(tm.getValueAt(m,0))) { //phone
                            fi = writeNext(ts, fi, wr);
                        } if (CONT_FIELDS[4].equals(tm.getValueAt(m,0))) { // e-mail
                            ei = writeNext(es, ei, wr);
                        } if (CONT_FIELDS[5].equals(tm.getValueAt(m,0))) { // addr
                            ai = writeNext(as, ai, wr);
                        } if (CONT_FIELDS[6].equals(tm.getValueAt(m,0))) { // URL
                            li = writeNext(ls, li, wr);
                        } if (CONT_FIELDS[7].equals(tm.getValueAt(m,0))) { // type
                            
                        } if (CONT_FIELDS[8].equals(tm.getValueAt(m,0))) { // note
                            writeNext(c.getComments(), 0, wr);
                        }
                    }
                    wr.write("\r\n");
                }
                wr.close();
            } catch(IOException ioe) {
                // show error dialog, update status
                ioe.printStackTrace();
            }
        }
    }

    static int writeNext(List l, int i, Writer w) throws IOException {
        if (l != null && i<l.size()) {
            GenericAttribute a = (GenericAttribute)l.get(i);
            w.write(a.getNormalized());
        }
        return ++i;
    }
    
    static class CSVMappingModel extends AbstractTableModel {
        
        String[] mapping;
        boolean [] duplicates, uses;
        CSVMappingModel() {
            mapping = new String[CONT_FIELDS.length];
            System.arraycopy(CONT_FIELDS, 0, mapping, 0, CONT_FIELDS.length);
            duplicates = new boolean[CONT_FIELDS.length];
            uses = new boolean[CONT_FIELDS.length];
        }
        public String 	getColumnName(int column) {
            return COL_NAMES[column];
        }
        public int getColumnCount() { return 3; }
        public int getRowCount() { return CONT_FIELDS.length;}
        public Object getValueAt(int row, int col) {
            switch(col) {
            case 0: return mapping[row];
            case 1: return duplicates[row]?Boolean.TRUE:Boolean.FALSE;
            case 2: return uses[row]?Boolean.TRUE:Boolean.FALSE;
            }
            return null;
        }
        public boolean 	isCellEditable(int rowIndex, int columnIndex) {
            return true;
        }
        public void setValueAt(Object aValue,
                int rowIndex,
                int columnIndex) {
            switch(columnIndex) {
            case 0: mapping[rowIndex] = aValue.toString();
            case 1: duplicates[rowIndex] = Boolean.TRUE.equals(aValue);
            case 2: uses[rowIndex] = Boolean.TRUE.equals(aValue);
            }
        }
    }
    
    static class CSVFieldComboBoxRenderer extends JComboBox implements TableCellRenderer {
        public CSVFieldComboBoxRenderer(String[] items) {
            super(items);
        }
    
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            if (isSelected) {
                setForeground(table.getSelectionForeground());
                super.setBackground(table.getSelectionBackground());
            } else {
                setForeground(table.getForeground());
                setBackground(table.getBackground());
            }
    
            // Select the current value
            setSelectedItem(value);
            return this;
        }
    }
    
    public class CSVFieldComboBoxEditor extends DefaultCellEditor {
        public CSVFieldComboBoxEditor(String[] items) {
            super(new JComboBox(items));
        }
    }
    
    static class CSVFieldCheckBoxRenderer extends JCheckBox implements TableCellRenderer {
        CSVFieldCheckBoxRenderer() {
            //setSelected(value);
        }
        
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
	            if (isSelected) {
	                setForeground(table.getSelectionForeground());
	                super.setBackground(table.getSelectionBackground());
	            } else {
	                setForeground(table.getForeground());
	                setBackground(table.getBackground());
	            }
            
            	setSelected(((Boolean)value).booleanValue());
            	return this;
        }
    }
    
    public class CSVFieldCheckBoxEditor extends DefaultCellEditor {
        public CSVFieldCheckBoxEditor() {
            super(new JCheckBox());
        }
    }
}
