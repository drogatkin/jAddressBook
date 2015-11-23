/* AddressBook - ContactFrame
 * Copyright (C) 1999-2004 Dmitriy Rogatkin.  All rights reserved.
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
 * $Id: ContactFrame.java,v 1.11 2011/09/02 04:10:18 dmitriy Exp $
 */
package addressbook;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;

import org.aldan3.app.ui.FixedGridLayout;


// TODO: add custom tabs for new person attributes

public class ContactFrame extends JFrame implements ActionListener, ContactConstant {
	// TODO: provide localization mechanism for types
	// TODO: move to email object, actually this list defined in e-mail type object

	JTable table;

	Contact contact;

	final static String EMAIL_PAT = AddressBookResources.LABEL_EMAIL_PTRN;

	final static String PHONE_PAT = AddressBookResources.LABEL_PHONE_PTRN;

	final static String NEW_PAT = AddressBookResources.LABEL_NEW_ITEM_PTRN;

	final static EMail NEW_EMAIL_ENTRY = new EMail(EMAIL_PAT, NEW_PAT);

	final static Telephone NEW_PHONE_ENTRY = new Telephone(PHONE_PAT, NEW_PAT);

	final static SimpleDateFormat DOB_FMT = new SimpleDateFormat("MM-dd-yyyy");

	ContactFrame(AddressBookFrame _abf) {
		this(_abf, -1);
	}

	ContactFrame(AddressBookFrame _abf, int _index) {
		super(AddressBookResources.TITLE_NEWCONTACT);
		table = _abf.table;
		if (_index > -1) {
			contact = ((BaseAttrTableModel) table.getModel()).getValueAt(_index);
			setTitle(contact.getName().toString());
		}
		JTabbedPane tabbedpane = new JTabbedPane(SwingConstants.TOP);
		tabbedpane.insertTab(AddressBookResources.TAB_GENERAL, (Icon) null, new GeneralTab(),
				AddressBookResources.TTIP_GENERALCONTACTINFO, 0);
		tabbedpane.insertTab(AddressBookResources.TAB_DETAILS, (Icon) null, new DetailsTab(),
				AddressBookResources.TTIP_DETAILCONTACTINFO, 1);
		tabbedpane.insertTab(AddressBookResources.TAB_CHAT_N_WEB, (Icon) null, new ChatWebTab(),
				AddressBookResources.TTIP_CHAT_N_WEB, 2);
		// TODO: add tab For Memorizing
		// with: Gender, Spouse, Children, Birthday, Anniversary, University
		tabbedpane.insertTab(AddressBookResources.TAB_CERTIFICATES, (Icon) null, new CertificatesTab(),
				AddressBookResources.TTIP_CERTIFICATES, 3);
		getContentPane().add(tabbedpane, "Center");
		getContentPane().add(createButtons(this), "South");
		setIconImage(_abf.getResourceIcon(AddressBookResources.IMG_NEW + AddressBookResources.EXT_GIF)
				.getImage());
		pack();
		setVisible(true);
	}

	public JPanel createButtons(ActionListener al) {
		JButton btn;
		JPanel result = new JPanel();
		result.setLayout(new FlowLayout(FlowLayout.RIGHT));
		result.add(btn = new JButton(AddressBookResources.CMD_OK));
		btn.addActionListener(al);
		result.add(btn = new JButton(AddressBookResources.CMD_APPLY));
		btn.addActionListener(al);
		result.add(btn = new JButton(AddressBookResources.CMD_CANCEL));
		btn.addActionListener(al);
		return result;
	}

	public void actionPerformed(ActionEvent a) {
		String cmd = a.getActionCommand();
		if (AddressBookResources.CMD_OK.equals(cmd)) {
			if (updatePerson())
				dispose();
		} else if (AddressBookResources.CMD_CANCEL.equals(cmd)) {
			dispose();
		} else if (AddressBookResources.CMD_APPLY.equals(cmd)) {
			updatePerson();
		}
	}

	boolean updatePerson() {
		Name name = null;
		try {
			name = new Name(tf_name.getText());
		} catch (ParseException pe) {
		}
		if (name == null) {
			JOptionPane.showMessageDialog(this, AddressBookResources.LABEL_NONAME,
					AddressBookResources.TITLE_WARNING, JOptionPane.WARNING_MESSAGE);
			return false;
		}
		if (contact == null) {
			contact = new Contact(new Date());
			((BaseAttrTableModel) table.getModel()).addValue(contact);
			// add everything already created
			contact.add(((GeneralTab.BaseAttrTableModel) tb_emails.getModel()).getActualElements());
			contact.add(((GeneralTab.BaseAttrTableModel) tb_tphones.getModel()).getActualElements());
		}
		contact.setValue(name);
		if (contact.getComments() != null)
			contact.getComments().clear();
		contact.add(ta_cmnt.getText());
		try {
			contact.setDOB(DOB_FMT.parse(dob.getText()));
		} catch (java.text.ParseException pe) {
		}
		if (contact.getAddresses() != null)
			contact.getAddresses().clear();
		for (int i = 0; i < addrs.getTabCount(); i++) {
			String cmnt = addrs.getTitleAt(i);
			contact.add(new Address(((JTextArea) addrs.getElement(cmnt)).getText(), cmnt, (String) null));
		}
		if (contact.getChats() != null)
			contact.getChats().clear();
		for (int i = 0; i < chats.getTabCount(); i++) {
			String cmnt = chats.getTitleAt(i);
			contact.add(((ChatAttrs) chats.getElement(cmnt)).getChat(cmnt));
		}

		if (contact.getLinks() != null)
			contact.getLinks().clear();
		for (int i = 0; i < webs.getTabCount(); i++) {
			String cmnt = webs.getTitleAt(i);
			contact.add(new Link(((JTextArea) webs.getElement(cmnt)).getText(), cmnt));
		}

		table.revalidate();
		return true;
	}

	JTextField tf_name, dob;

	JTextArea ta_cmnt;

	TabbedArrayOfComponents addrs, chats, webs, pgps;

	JTable tb_tphones, tb_emails;

	class GeneralTab extends JPanel {
		GeneralTab() {
			setLayout(new FixedGridLayout(2, 10, AddressBookResources.CTRL_VERT_SIZE,
					AddressBookResources.CTRL_VERT_GAP, AddressBookResources.CTRL_HORIS_INSET,
					AddressBookResources.CTRL_HORIZ_GAP));
			add(new JLabel(AddressBookResources.LABEL_NAME), "0,0,1");
			add(tf_name = new JTextField(), "0,1,2");
			AddressBookFrame.ComboCellRenderer cellEdRen;
			tb_emails = new JTable(new EmailsTableModel());
			tb_emails.setDefaultRenderer(JComboBox.class,
					cellEdRen = new AddressBookFrame.ComboCellRenderer());
			tb_emails.setDefaultEditor(JComboBox.class, cellEdRen);
			tb_emails.getColumn(tb_emails.getModel().getColumnName(0)).setMaxWidth(16);
			tb_emails.addKeyListener(new RowRemover());
			tb_emails.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
			add(new JScrollPane(tb_emails), "0,2,2,4,0,12");
			tb_tphones = new JTable(new PnonesTableModel());
			tb_tphones.setDefaultRenderer(JComboBox.class,
					cellEdRen = new AddressBookFrame.ComboCellRenderer());
			tb_tphones.setDefaultEditor(JComboBox.class, cellEdRen);
			tb_tphones.addKeyListener(new RowRemover());
			tb_tphones.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
			add(new JScrollPane(tb_tphones), "0,6,2,4,0,12");
			if (contact != null) {
				Name name = contact.getName();
				tf_name.setText(name.getFirst() + ' ' + name.getLast());
			}
		}

		class RowRemover extends KeyAdapter {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_DELETE && e.getSource() instanceof JTable) {
					JTable t = (JTable) e.getSource();
					int dr = t.getSelectedRow();
					if (dr > -1) {
						((BaseAttrTableModel) t.getModel()).remove(dr);
						t.revalidate();
					}
				}
			}
		}

		abstract class BaseAttrTableModel extends AbstractTableModel {
			List elements;

			// REMARK: can't use this part as a constructor
			void init() {

				if (contact != null) {
					elements = getElements();
					if (elements == null) {
						contact.add(getPatternElement());
						elements = getElements();
						elements.clear();
					}
				} else {
					elements = new ArrayList();
				}
			}

			abstract List getElements();

			abstract GenericAttribute createElement();

			abstract GenericAttribute getPatternElement();

			abstract String getTitle();

			abstract String getNewValuePattern();

			abstract String[] getTypes();

			abstract boolean isPreffered();

			public boolean isCellEditable(int _rowIndex, int _columnIndex) {
				return true;
			}

			public String getColumnName(int column) {
				if (getTypes() != null) {
					if (isPreffered()) {
						switch (column) {
						case 0:
							return AddressBookResources.LABEL_M;
						case 1:
							return AddressBookResources.LABEL_TYPE;
						case 2:
							return AddressBookResources.LABEL_ABREV;
						default:
						}
					} else {
						switch (column) {
						case 0:
							return AddressBookResources.LABEL_TYPE;
						case 1:
							return AddressBookResources.LABEL_ABREV;
						default:
						}
					}
				} else {
					if (isPreffered()) {
						switch (column) {
						case 0:
							return AddressBookResources.LABEL_M;
						case 1:
							return AddressBookResources.LABEL_ABREV;
						default:
						}
					} else {
						switch (column) {
						case 0:
							return AddressBookResources.LABEL_ABREV;
						default:
						}
					}
				}
				return getTitle();
			}

			public Class getColumnClass(int columnIndex) {
				if (getTypes() != null) {
					if (isPreffered()) {
						switch (columnIndex) {
						case 0:
							return Boolean.class;
						case 1:
							return JComboBox.class;
						default:
						}
					} else {
						switch (columnIndex) {
						case 0:
							return JComboBox.class;
						default:
						}
					}
				} else {
					if (isPreffered()) {
						switch (columnIndex) {
						case 0:
							return Boolean.class;
						default:
						}
					}
				}
				return String.class;
			}

			public int getRowCount() {
				return (elements != null ? elements.size() : 0) + 1;
			}

			public int getColumnCount() {
				int result = 2;
				if (getTypes() != null)
					result++;
				if (isPreffered())
					result++;
				return result;
			}

			public Object getValueAt(int row, int column) {
				if (elements != null) {
					GenericAttribute ga = row > (elements.size() - 1) ? null : (GenericAttribute) elements
							.get(row);
					if (getTypes() != null) {
						if (isPreffered()) {
							switch (column) {
							case 0:
								return ga != null && ga.isPreferable()? Boolean.TRUE : Boolean.FALSE;
							case 1:
								return ga == null ? "" : ga.getType();
							case 2:
								return ga == null ? NEW_PAT : ga.getShortDescription();
							default:
							}
						} else {
							switch (column) {
							case 0:
								return ga == null ? "" : ga.getType();
							case 1:
								return ga == null ? NEW_PAT : ga.getShortDescription();
							default:
							}
						}
					} else {
						if (isPreffered()) {
							switch (column) {
							case 0:
								return ga != null && ga.isPreferable() ? Boolean.TRUE : Boolean.FALSE;
							case 1:
								return ga == null ? NEW_PAT : ga.getShortDescription();
							default:
							}
						} else {
							switch (column) {
							case 0:
								return ga == null ? NEW_PAT : ga.getShortDescription();
							default:
							}
						}
					}
					return ga == null ? getNewValuePattern() : ga.getNormalized();
				}
				return null;
			}

			public void setValueAt(Object value, int row, int col) {
				GenericAttribute ga = row > (elements.size() - 1) ? null : (GenericAttribute) elements
						.get(row);
				if (ga == null)
					elements.add(ga = createElement());
				int convCol = 0;
				if (getTypes() != null) {
					if (isPreffered()) {
						switch (col) {
						case 1:
							convCol = 2;
							break;
						case 2:
							convCol = 1;
						default:
						}
					} else {
						switch (col) {
						case 0:
							convCol = 2;
							break;
						case 1:
							convCol = 1;
						default:
						}
					}
				} else {
					if (isPreffered()) {
						switch (col) {
						case 1:
							convCol = 1;
						default:
						}
					} else {
						switch (col) {
						case 0:
							convCol = 1;
						default:
						}
					}
				}

				switch (convCol) {
				case 0:
					ga.setValue(value.toString());
					break;
				case 1:
					ga.setDescription(value.toString());
					break;
				case 2:
					ga.setType(value.toString());
				}
			}

			void remove(int _index) {
				if (_index < elements.size() && _index > -1)
					elements.remove(_index);
			}

			public List getActualElements() {
				return elements;
			}
		}

		class EmailsTableModel extends BaseAttrTableModel {

			EmailsTableModel() {
				init();
			}

			List getElements() {
				return contact.getEMails();
			}

			GenericAttribute createElement() {
				return new EMail(EMAIL_PAT, NEW_PAT);
			}

			GenericAttribute getPatternElement() {
				return NEW_EMAIL_ENTRY;
			}

			String getTitle() {
				return "E-Mail";
			}

			String getNewValuePattern() {
				return EMAIL_PAT;
			}

			String[] getTypes() {
				return EMAIL_TYPE;
			}

			boolean isPreffered() {
				return true;
			}
		}

		class PnonesTableModel extends BaseAttrTableModel {
			PnonesTableModel() {
				init();
			}

			List getElements() {
				return contact.getTelephones();
			}

			GenericAttribute createElement() {
				return new Telephone(PHONE_PAT, NEW_PAT);
			}

			GenericAttribute getPatternElement() {
				return NEW_PHONE_ENTRY;
			}

			String getTitle() {
				// TODO: localize
				return "T-Phone";
			}

			String getNewValuePattern() {
				return PHONE_PAT;
			}

			String[] getTypes() {
				return TELEPHONE_TYPE;
			}

			boolean isPreffered() {
				return false;
			}
		}
	}

	class DetailsTab extends JPanel implements ActionListener {
		DetailsTab() {
			setLayout(new FixedGridLayout(4, 10, AddressBookResources.CTRL_VERT_SIZE,
					AddressBookResources.CTRL_VERT_GAP, AddressBookResources.CTRL_HORIS_INSET,
					AddressBookResources.CTRL_HORIZ_GAP));
			add(new JLabel(AddressBookResources.LABEL_ADDRESSES), "0,0,2");
			add(tf_name = new JTextField(), "3,1");
			JButton b;
			add(b = new JButton(AddressBookResources.CMD_ADD), "3,2,1");
			// only graphical button for add/modify a new addr
			b.addActionListener(this);
			add(b = new JButton(AddressBookResources.CMD_MODIFY), "3,3,1");
			// only graphical button for add/modify a new addr
			b.addActionListener(this);
			add(b = new JButton(AddressBookResources.CMD_DELETE), "3,4,1");
			// only graphical button for add/modify a new addr
			b.addActionListener(this);
			// TODO: add it to tabbed pane to specify many addresses, like home,
			// company, parents, friend
			// button view map, follows to the link:
			// http://www.expediamaps.com/AddressFinder.asp?Street=230+Twin+Dolphin+Dr&City=Redwood+City&State=CA&Zip=94065
			add(addrs = new TabbedArrayOfComponents(JTabbedPane.BOTTOM) {
				JComponent produce() {
					return new JTextArea();
				}
			}, "0,1,3,4");
			add(new JLabel(AddressBookResources.LABEL_PHOTO), "2,5,1");
			// add(new JLabel(BaseController.getResourceIcon()), "2,5,2,3");
			add(new JLabel(AddressBookResources.LABEL_BIRTHDAY), "0,5,2");
			add(dob = new JTextField(), "0,6,1");
			add(new JLabel(AddressBookResources.LABEL_COMMENT), "0,9,1");
			add(new JScrollPane(ta_cmnt = new JTextArea()), "1,9,3,2");
			if (contact != null) {
				Date dateOB = contact.getDOB();
				if (dateOB != null)
					dob.setText(DOB_FMT.format(dateOB));
				List elements = contact.getComments();
				for (int i = 0; elements != null && i < elements.size(); i++)
					ta_cmnt.append(elements.get(i).toString());
				elements = contact.getAddresses();
				for (int i = 0; elements != null && i < elements.size(); i++) {
					Address adr = (Address) elements.get(i);
					addrs.addElement(adr.getShortDescription());
					((JTextArea) addrs.getElement(adr.getShortDescription())).setText(adr.getFormated());
				}
			}
		}

		public void actionPerformed(ActionEvent a) {
			String cmd = a.getActionCommand();
			if (AddressBookResources.CMD_ADD.equals(cmd)) {
				addrs.addElement(tf_name.getText());
				tf_name.setText("");
			} else if (AddressBookResources.CMD_MODIFY.equals(cmd)) {
				addrs.modifyElement(tf_name.getText());
				tf_name.setText("");
			} else if (AddressBookResources.CMD_DELETE.equals(cmd)) {
				addrs.deleteElement();
			}
		}

		JTextField tf_name;
	}

	class ChatWebTab extends JPanel {
		JTextField tf_name, tf_name1;

		ChatWebTab() {
			setLayout(new FixedGridLayout(4, 10, AddressBookResources.CTRL_VERT_SIZE,
					AddressBookResources.CTRL_VERT_GAP, AddressBookResources.CTRL_HORIS_INSET,
					AddressBookResources.CTRL_HORIZ_GAP));
			add(new JLabel(AddressBookResources.LABEL_CHATS), "0,0,2");
			add(tf_name = new JTextField(), "3,1");
			add(chats = new TabbedArrayOfComponents(JTabbedPane.BOTTOM) {
				JComponent produce() {
					return new ChatAttrs();
				}
			}, "0,1,3,5");
			AttrActionHandler tah = new AttrActionHandler(chats, tf_name);
			JButton b;
			add(b = new JButton(AddressBookResources.CMD_ADD), "3,2,1");
			// only graphical button for add/modify a new addr
			b.addActionListener(tah);
			add(b = new JButton(AddressBookResources.CMD_MODIFY), "3,3,1");
			// only graphical button for add/modify a new addr
			b.addActionListener(tah);
			add(b = new JButton(AddressBookResources.CMD_DELETE), "3,4,1");
			// only graphical button for add/modify a new addr
			b.addActionListener(tah);
			add(webs = new TabbedArrayOfComponents(JTabbedPane.BOTTOM) {
				JComponent produce() {
					return new JTextArea();
				}
			}, "0,7,3,3");
			add(new JLabel(AddressBookResources.LABEL_WEBS), "0,6,2");
			add(tf_name1 = new JTextField(), "3,6");
			tah = new AttrActionHandler(webs, tf_name1);
			add(b = new JButton(AddressBookResources.CMD_ADD), "3,7,1");
			// only graphical button for add/modify a new addr
			b.addActionListener(tah);
			add(b = new JButton(AddressBookResources.CMD_MODIFY), "3,8,1");
			// only graphical button for add/modify a new addr
			b.addActionListener(tah);
			add(b = new JButton(AddressBookResources.CMD_DELETE), "3,9,1");
			// only graphical button for add/modify a new addr
			b.addActionListener(tah);
			if (contact != null) {
				List elements = contact.getChats();
				for (int i = 0; elements != null && i < elements.size(); i++) {
					Chat chat = (Chat) elements.get(i);
					chats.addElement(chat.getShortDescription());
					((ChatAttrs) chats.getElement(chat.getShortDescription())).setChat(chat);
				}
				elements = contact.getLinks();
				for (int i = 0; elements != null && i < elements.size(); i++) {
					Link link = (Link) elements.get(i);
					webs.addElement(link.getShortDescription());
					((JTextArea) webs.getElement(link.getShortDescription())).setText(link.toString());
				}
			}
		}

		class AttrActionHandler implements ActionListener {
			TabbedArrayOfComponents ta;

			JTextField tf;

			AttrActionHandler(TabbedArrayOfComponents _ta, JTextField _tf) {
				ta = _ta;
				tf = _tf;
			}

			public void actionPerformed(ActionEvent a) {
				String cmd = a.getActionCommand();
				if (AddressBookResources.CMD_ADD.equals(cmd)) {
					ta.addElement(tf.getText());
					tf_name.setText("");
				} else if (AddressBookResources.CMD_MODIFY.equals(cmd)) {
					ta.modifyElement(tf.getText());
					tf.setText("");
				} else if (AddressBookResources.CMD_DELETE.equals(cmd)) {
					ta.deleteElement();
				}
			}
		}
	}

	class ChatAttrs extends JPanel {
		JTextField tf_chatId, tf_chatHost, tf_room;

		JComboBox sl_chatType;

		ChatAttrs() {
			setLayout(new FixedGridLayout(3, 4, AddressBookResources.CTRL_VERT_SIZE,
					AddressBookResources.CTRL_VERT_GAP, AddressBookResources.CTRL_HORIS_INSET,
					AddressBookResources.CTRL_HORIZ_GAP));
			add(new JLabel(AddressBookResources.LABEL_CHAT_ID), "0,0,1");
			add(tf_chatId = new JTextField(), "1,0,2");
			add(new JLabel(AddressBookResources.LABEL_HOST), "0,1,1");
			add(tf_chatHost = new JTextField(), "1,1,2");
			add(new JLabel(AddressBookResources.LABEL_CHAT_TYPE), "0,2,1");
			add(sl_chatType = new JComboBox(Chat.MNEMO), "1,2,1");
			add(new JLabel(AddressBookResources.LABEL_ROOM), "0,3,1");
			add(tf_room = new JTextField(), "1,3,2");
		}

		Chat getChat(String _cmnt) {
			return new Chat(tf_chatId.getText(), tf_chatHost.getText(), sl_chatType.getSelectedIndex() + 1,
					_cmnt, tf_room.getText());
		}

		void setChat(Chat _chat) {
			tf_chatId.setText(_chat.getValue());
			tf_chatHost.setText(_chat.getHost());
			tf_room.setText(_chat.getRoom());
			sl_chatType.setSelectedItem(_chat.getType());
		}
	}

	class CertificatesTab extends JPanel {
		CertificatesTab() {
			setLayout(new FixedGridLayout(4, 10, AddressBookResources.CTRL_VERT_SIZE,
					AddressBookResources.CTRL_VERT_GAP, AddressBookResources.CTRL_HORIS_INSET,
					AddressBookResources.CTRL_HORIZ_GAP));
		}
	}
}