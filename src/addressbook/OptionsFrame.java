/* AddressBook - OptionsFrame
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
 * 
 * $Id: OptionsFrame.java,v 1.7 2007/07/27 02:54:18 rogatkin Exp $
 */
package addressbook;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.UnsupportedEncodingException;
import java.util.Locale;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.aldan3.app.Registry;
import org.aldan3.app.ui.FixedGridLayout;
import org.aldan3.servlet.Static;
import org.aldan3.util.DataConv;
import org.aldan3.util.IniPrefs;

public class OptionsFrame extends JFrame implements ActionListener {
	public static final String OPTIONS = "DataBookOptions";

	public static final String LOCATION = "Location";

	public static final String ACCESS = "Access";

	public static final String LOGIN = "Login";

	public static final String PASSWORD = "Password";

	public static final String SHARE = "Share";

	public static final String SYNCHRO = "Synchronize";

	public static final String AUTH = "Authentication";

	public static final String ENCRYPT = "Encryption";

	public static final String LOCALE = "DataBookLocale";

	public static final String LANG = "Language";

	public static final String COUNTRY = "Country";

	public final static String IPOD_DEVICE = "iPod_HD";

	interface Persistable {
		void load();

		void save();
	}

	Registry registry;

	JTabbedPane tabbedpane;

	OptionsFrame(AddressBookFrame abf) {
		super(AddressBookResources.MENU_OPTIONS);
		this.registry = abf.registry;
		Container c = getContentPane();
		tabbedpane = new JTabbedPane(SwingConstants.TOP);
		tabbedpane.insertTab(AddressBookResources.TAB_STORAGE, (Icon) null, new StorageTab(),
				AddressBookResources.TTIP_STORAGE, 0);
		c.add(tabbedpane, "Center");
		c.add(AddressBookFrame.createButtonPanel(this), "South");
		setIconImage(abf.getResourceIcon(AddressBookResources.IMG_PROGRAM).getImage());
		pack();
		setVisible(true);
		load();
	}

	public void actionPerformed(ActionEvent a) {
		String cmd = a.getActionCommand();
		// TODO: migrate resources
		if (cmd.equals(AddressBookResources.CMD_OK)) {
			save();
			dispose();
			// setVisible(false);
		} else if (cmd.equals(AddressBookResources.CMD_APPLY)) {
			save();
		} else if (cmd.equals(AddressBookResources.CMD_CANCEL)) {
			dispose();
			// setVisible(false);
		}
	}

	public void save() {
		for (int i = 0; i < tabbedpane.getTabCount(); i++) {
			((Persistable) tabbedpane.getComponentAt(i)).save();
		}
	}

	public void load() {
		for (int i = 0; i < tabbedpane.getTabCount(); i++) {
			((Persistable) tabbedpane.getComponentAt(i)).load();
		}
	}

	static void applyLocale(IniPrefs _s) {
		String country = (String) _s.getProperty(OptionsFrame.LOCALE, OptionsFrame.COUNTRY);
		if (country != null) {
			String lang = (String) _s.getProperty(OptionsFrame.LOCALE, OptionsFrame.LANG);
			if (lang != null)
				Locale.setDefault(new Locale(country, lang));
		}
	}

	class StorageTab extends JPanel implements ChangeListener, Persistable {
		ButtonGroup group;

		StorageTab() {
			setLayout(new FixedGridLayout(5, 6, AddressBookResources.CTRL_VERT_SIZE,
					AddressBookResources.CTRL_VERT_GAP, AddressBookResources.CTRL_HORIS_INSET,
					AddressBookResources.CTRL_HORIZ_GAP));
			group = new ButtonGroup();
			add(rb_global = new JRadioButton(AddressBookResources.LABEL_GLOBAL), "0,0,1");
			group.add(rb_global);
			rb_global.addChangeListener(this);
			add(new JLabel(AddressBookResources.LABEL_HOST), "1,0,1");
			add(tf_addr_host = new JTextField(), "2,0,3");
			add(new JLabel(AddressBookResources.LABEL_AUTHONTICATION), "1,1,1");
			add(cb_auth = new JComboBox(AddressBookResources.LABEL_AUTHONTEFICATS), "2,1,1");
			add(new JLabel(AddressBookResources.LABEL_ENCRYPTION), "3,1,1");
			add(cb_encrypt = new JComboBox(AddressBookResources.LABEL_ENCRYPTIONS), "4,1,1");

			add(rb_local = new JRadioButton(AddressBookResources.LABEL_LOCAL), "0,2,2");
			group.add(rb_local);
			rb_local.addChangeListener(this);
			add(new JLabel(AddressBookResources.LABEL_DATABOOK), "1,2,1");
			add(tf_db_url = new JTextField(), "2,2,3");
			// TODO: move the locales to another tab as only ...
			add(new JLabel(AddressBookResources.LABEL_LOCALES/*
																 * ,
																 * SwingConstants.RIGHT
																 */), "2,3");
			add(cb_country = new JComboBox(AddressBookResources.COUNTRIES), "3,3");
			add(cb_lang = new JComboBox(AddressBookResources.LANGUAGES), "4,3");

			add(cb_share_adr = new JCheckBox(AddressBookResources.LABEL_SHAREADDR), "0,4,2");
			add(cb_synchro = new JCheckBox(AddressBookResources.LABEL_SYNCHRONIZE), "0,5,2");

			add(new JLabel(AddressBookResources.LABEL_LOGIN), "2,4,1");
			add(tf_login = new JTextField(), "3,4,2");
			add(new JLabel(AddressBookResources.LABEL_PASSWORD), "2,5,1");
			add(tf_password = new JPasswordField(), "3,5,2");
		}

		public void stateChanged(ChangeEvent e) {
			boolean global_enabled = rb_global.isSelected();
			tf_addr_host.setEnabled(global_enabled);
			tf_db_url.setEnabled(!global_enabled);
		}

		public void save() {
			IniPrefs s = (IniPrefs) registry.getService(AddressBookFrame.PREFS_NAME);
			String oldLoc = (String) s.getProperty(OPTIONS, LOCATION), newLoc;
			Integer access;
			if (rb_local.isSelected()) {
				access = AddressBookResources.NO_INT;
				newLoc = tf_db_url.getText();
			} else {
				access = AddressBookResources.YES_INT;
				newLoc = tf_addr_host.getText();
			}
			if (!newLoc.equals(oldLoc)) {
				// save old loc and load new loc
				s.save();
				s.setProperty(OPTIONS, ACCESS, access);
				s.setProperty(OPTIONS, LOCATION, newLoc);
				s.save();
			} else {
				s.setProperty(OPTIONS, ACCESS, access);
				s.setProperty(OPTIONS, LOCATION, newLoc);
			}
			s.setProperty(OPTIONS, LOGIN, tf_login.getText());
			try {
				s.setProperty(OPTIONS, PASSWORD, ":"
						+ DataConv.bytesToHex(DataConv.encryptXor(new String(tf_password.getPassword())).getBytes(
								Static.CharSet.ASCII)));
			} catch (UnsupportedEncodingException uee) {
			}
			s.setProperty(OPTIONS, SHARE, cb_share_adr.isSelected() ? AddressBookResources.YES_INT
					: AddressBookResources.NO_INT);
			s.setProperty(OPTIONS, SYNCHRO, cb_synchro.isSelected() ? AddressBookResources.YES_INT
					: AddressBookResources.NO_INT);
			s.setProperty(OPTIONS, AUTH, cb_auth.getSelectedItem());
			s.setProperty(OPTIONS, ENCRYPT, cb_encrypt.getSelectedItem());

			s.setProperty(LOCALE, COUNTRY, cb_country.getSelectedItem());
			s.setProperty(LOCALE, LANG, cb_lang.getSelectedItem());
			applyLocale(s);
		}

		public void load() {
			IniPrefs s = (IniPrefs) registry.getService(AddressBookFrame.PREFS_NAME);
			rb_local.setSelected(s.getInt(s.getProperty(OPTIONS, ACCESS), 0) == 0);
			rb_global.setSelected(s.getInt(s.getProperty(OPTIONS, ACCESS), 0) == 1);
			String ss = (String) s.getProperty(OPTIONS, LOGIN);
			tf_login.setText(ss != null ? ss : "");
			ss = (String) s.getProperty(OPTIONS, PASSWORD);
			if (ss != null && ss.length() > 0) {
				ss = ss.substring(1);
				try {
					tf_password.setText(DataConv.encryptXor(new String(DataConv.hexToBytes(ss), Static.CharSet.ASCII)));
				} catch (UnsupportedEncodingException uee) {
				}
			}
			ss = (String) s.getProperty(OPTIONS, LOCATION);
			if (rb_local.isSelected())
				tf_db_url.setText(ss);
			else
				tf_addr_host.setText(ss);
			cb_share_adr.setSelected(s.getInt(s.getProperty(OPTIONS, SHARE), 0) == 1);
			cb_synchro.setSelected(s.getInt(s.getProperty(OPTIONS, SYNCHRO), 0) == 1);
			ss = (String) s.getProperty(OPTIONS, AUTH);
			if (ss != null)
				cb_auth.setSelectedItem(ss);
			ss = (String) s.getProperty(OPTIONS, ENCRYPT);
			if (ss != null)
				cb_encrypt.setSelectedItem(ss);

			ss = (String) s.getProperty(LOCALE, COUNTRY);
			if (ss == null)
				ss = Locale.getDefault().getCountry();
			cb_country.setSelectedItem(ss);
			ss = (String) s.getProperty(LOCALE, LANG);
			if (ss == null)
				ss = Locale.getDefault().getLanguage();
			cb_lang.setSelectedItem(ss);
		}

		JRadioButton rb_global, rb_local;

		JTextField tf_addr_host, tf_db_url, tf_login;

		JPasswordField tf_password;

		JCheckBox cb_share_adr, cb_synchro;

		JComboBox cb_country, cb_lang, cb_auth, cb_encrypt;
	}
}