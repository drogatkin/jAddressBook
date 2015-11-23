/* AddressBook - BaseAttrTableModel
 * Copyright (C) 2000-2004 Dmitriy Rogatkin.  All rights reserved.
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
 * $Id: BaseAttrTableModel.java,v 1.4 2007/02/09 07:27:49 rogatkin Exp $
 */
package addressbook;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

public class BaseAttrTableModel extends AbstractTableModel {
	// sort
	final static int UN_SORT = 0;

	final static int NAME_SORT = 1;

	final static int E_MAIL_SORT = 2;

	final static int TPHONE_SORT = 3;

	final static int FIRSTNAME_SORT = 0;

	final static int LASTNAME_SORT = 1;

	List<Contact> values;

	Folder sourceFolder;

	protected JTableHeader tableHeader;

	protected MouseListener headerMouseListener;

	BaseAttrTableModel(Folder _contacts) {
		sourceFolder = _contacts;
		values = new ArrayList<Contact>(_contacts.getContent().size());
		for (Object o : _contacts.getContent())
			if (o instanceof Contact)
				values.add((Contact) o);
	}

	public void setTableHeader(JTableHeader tableHeader) {
		if (this.tableHeader != null)
			throw new IllegalArgumentException("Header can be set only once."); // illegal
		// state

		this.tableHeader = tableHeader;
		if (this.tableHeader != null) {
			this.tableHeader.addMouseListener(headerMouseListener);
			this.tableHeader.setDefaultRenderer(new SortableHeaderRenderer(this.tableHeader.getDefaultRenderer()));
		}
	}

	public int getRowCount() {
		return values.size();
	}

	public int getColumnCount() {
		return 4; // TODO: read from user profile (customizer)
	}

	public String getColumnName(int _col) {
		return AddressBookResources.HEADER_ADDRBOOK[_col];
	}

	public Object getValueAt(int row, int column) {
		// TODO: use customizer
		Contact contact = getValueAt(row);
		switch (column) {
		case 0:
			return contact.getName();
		case 1:
			return contact.getEMails();
		case 2:
			return contact.getTelephones();
		case 3:
			return contact.getChats();
		default:
			return null;
		}
	}

	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return columnIndex == 1;
	}

	public Class getColumnClass(int columnIndex) {
		if (columnIndex == 1)
			return JComboBox.class;
		return super.getColumnClass(columnIndex);
	}

	public Contact getValueAt(int row) {
		return (Contact) values.get(row);
	}

	public void removeValue(Contact _contact) {
		values.remove(_contact);
		sourceFolder.remove(_contact); // sync with folder
	}

	public void removeValue(int row) {
		sourceFolder.remove(values.remove(row));
	}

	public void removeAllElements() {
		values.clear();
		Iterator i = sourceFolder.getContent().iterator();
		while (i.hasNext())
			if (i.next() instanceof Contact)
				i.remove();
	}

	public void setValueAt(int row, Contact _contact) {
		sourceFolder.remove(values.get(row));
		values.set(row, _contact);
		sourceFolder.add(_contact);
	}

	public void addValue(Contact _contact) {
		values.add(_contact);
		sourceFolder.add(_contact);
	}

	void sort(int _field, final int _sub, final boolean _acc) {
		Contact[] contacts = new Contact[values.size()];
		values.toArray(contacts);
		// TODO: make all sorting objects static to avoid creation
		switch (_field) {
		case NAME_SORT:
			Arrays.sort(contacts, new Comparator<Contact>() {

				public int compare(Contact arg0, Contact arg1) {
					int result = 0;
					if (_sub == FIRSTNAME_SORT)
						result = ((Contact) arg0).getName().getFirst().compareTo(((Contact) arg1).getName().getFirst());
					else
						result = ((Contact) arg0).getName().getLast().compareTo(((Contact) arg1).getName().getLast());
					if (_acc)
						return result;
					return -result;
				}
			});
			break;
		case E_MAIL_SORT:
			Arrays.sort(contacts, new Comparator<Contact>() {

				public int compare(Contact arg0, Contact arg1) {
					List esa = ((Contact) arg0).getEMails();
					List esb = ((Contact) arg1).getEMails();
					if (esa == null || esa.size() == 0) {
						if (esb == null || esb.size() == 0)
							return 0;
						return _acc ? 1 : -1;
					} else {
						if (esb == null || esb.size() == 0)
							return _acc ? -1 : 1;
						int result = esa.get(0).toString().compareTo(esb.get(0).toString());
						if (_acc)
							return result;
						return -result;
					}
				}
			});
			break;
		case TPHONE_SORT:
			Arrays.sort(contacts, new Comparator<Contact>() {

				public int compare(Contact arg0, Contact arg1) {
					List esa = ((Contact) arg0).getTelephones();
					List esb = ((Contact) arg1).getTelephones();
					if (esa == null || esa.size() == 0) {
						if (esb == null || esb.size() == 0)
							return 0;
						return _acc ? 1 : -1;
					} else {
						if (esb == null || esb.size() == 0)
							return _acc ? -1 : 1;
						int result = esa.get(0).toString().compareTo(esb.get(0).toString());
						if (_acc)
							return result;
						return -result;
					}
				}
			});
			break;
		}
		values.clear();
		for (int i = 0; i < contacts.length; i++) {
			values.add(contacts[i]);
		}
	}

	protected class MouseHandler extends MouseAdapter {
		public void mouseClicked(MouseEvent e) {
			JTableHeader h = (JTableHeader) e.getSource();
			TableColumnModel columnModel = h.getColumnModel();
			int viewColumn = columnModel.getColumnIndexAtX(e.getX());
			int column = columnModel.getColumn(viewColumn).getModelIndex();
			if (column != -1) {
				int status = 0;// getSortingStatus(column);
				if (!e.isControlDown()) {
					// cancelSorting();
				}
				// Cycle the sorting states through {NOT_SORTED, ASCENDING,
				// DESCENDING} or
				// {NOT_SORTED, DESCENDING, ASCENDING} depending on whether
				// shift is pressed.
				status = status + (e.isShiftDown() ? -1 : 1);
				status = (status + 4) % 3 - 1; // signed mod, returning {-1, 0,
				// 1}
				// setSortingStatus(column, status);
			}
		}
	}

	protected static class Arrow implements Icon {
		private boolean descending;

		private int size;

		private int priority;

		public Arrow(boolean descending, int size, int priority) {
			this.descending = descending;
			this.size = size;
			this.priority = priority;
		}

		public void paintIcon(Component c, Graphics g, int x, int y) {
			Color color = c == null ? Color.GRAY : c.getBackground();
			// In a compound sort, make each succesive triangle 20%
			// smaller than the previous one.
			int dx = (int) (size / 2 * Math.pow(0.8, priority));
			int dy = descending ? dx : -dx;
			// Align icon (roughly) with font baseline.
			y = y + 5 * size / 6 + (descending ? -dy : 0);
			int shift = descending ? 1 : -1;
			g.translate(x, y);

			// Right diagonal.
			g.setColor(color.darker());
			g.drawLine(dx / 2, dy, 0, 0);
			g.drawLine(dx / 2, dy + shift, 0, shift);

			// Left diagonal.
			g.setColor(color.brighter());
			g.drawLine(dx / 2, dy, dx, 0);
			g.drawLine(dx / 2, dy + shift, dx, shift);

			// Horizontal line.
			if (descending) {
				g.setColor(color.darker().darker());
			} else {
				g.setColor(color.brighter().brighter());
			}
			g.drawLine(dx, 0, 0, 0);

			g.setColor(color);
			g.translate(-x, -y);
		}

		public int getIconWidth() {
			return size;
		}

		public int getIconHeight() {
			return size;
		}
	}

	protected class SortableHeaderRenderer implements TableCellRenderer {
		private TableCellRenderer tableCellRenderer;

		public SortableHeaderRenderer(TableCellRenderer tableCellRenderer) {
			this.tableCellRenderer = tableCellRenderer;
		}

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
				boolean hasFocus, int row, int column) {
			Component c = tableCellRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
					column);
			if (c instanceof JLabel) {
				JLabel l = (JLabel) c;
				l.setHorizontalTextPosition(JLabel.LEFT);
				int modelColumn = table.convertColumnIndexToModel(column);
				// l.setIcon(getHeaderRendererIcon(modelColumn,
				// l.getFont().getSize()));
			}
			return c;
		}
	}

	protected static class Directive {
		private int column;

		private int direction;

		public Directive(int column, int direction) {
			this.column = column;
			this.direction = direction;
		}
	}
}
