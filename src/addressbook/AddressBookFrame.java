/*
 * AddressBook - AddressBookFrame Copyright (C) 1999-2008 Dmitriy Rogatkin. All
 * rights reserved. Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following conditions
 * are met: 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer. 2.
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution. THIS SOFTWARE IS
 * PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * Visit http://jaddressbook.sourceforge.net to get the latest information about
 * Rogatkin's products.
 * 
 *  $Id: AddressBookFrame.java,v 1.37 2012/10/11 08:18:00 cvs Exp $
 */
package addressbook;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.AbstractCellEditor;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import org.aldan3.app.Desktop;
import org.aldan3.app.Registry;
import org.aldan3.model.ServiceProvider;
import org.aldan3.servlet.Static;
import org.aldan3.util.DataConv;
import org.aldan3.util.IniPrefs;
import org.aldan3.util.inet.Base64Codecs;

import addressbook.util.ActionPerformer;

public class AddressBookFrame extends JFrame implements ActionListener, ServiceProvider, VersionConstant {


	public final static String CRLF = "\r\n";

	// TODO: consider application base number for components
	public static final Integer COMP_ACTIONPERFORMER = new Integer(101);

	public static final Integer COMP_ADDRESSBOOK = new Integer(102);

	// config sections and properties
	public static final String ABVIEW = "DataBookView";

	public static final String VIEW = "View";

	public static final String SORTBY = "SortBy";

	public static final String NAMESORT = "LastNameSort";

	public static final String SORTORDER = "SortOrder";

	public static final String BOUNDS = "Bounds";

	public static final String HOME = PROGRAMNAME + IniPrefs.HOMEDIRSUFX;

	public static final String STOR_NAME = "datastorage.xml";

	final static int MENU = 1;

	final static int TOOL = 2;

	final static int STATUS = 4;

	final static int SEARCH = 8;

	final static int FOLDER = 16;

	int view = MENU + TOOL + STATUS + SEARCH + FOLDER;

	boolean standalone;

	int sortField;

	int subSortField;

	boolean sortDir;

	Registry registry;

	List folders;

	DataBookIO io;

	Properties properties;

	protected JLabel statusLine;

	protected boolean readFailed;

	public static final String PREFS_NAME = "preferences";

	JCheckBoxMenuItem m_toolBar, m_statusBar, m_folder;

	public AddressBookFrame(Registry baseReg) {
		super(PROGRAMNAME);
		this.registry = baseReg;
		if (this.registry == null)
			this.registry = new Registry();
		this.registry.register(this);
		properties = System.getProperties();
		try {
			io = new DataBookIO(properties);
		} catch (NoClassDefFoundError ncde) {
			JOptionPane.showMessageDialog(this, "Exception: " + ncde);
			return;
		}
		setIconImage(getResourceIcon(AddressBookResources.IMG_PROGRAM).getImage());
		IniPrefs prefs = (IniPrefs) registry.getService(PREFS_NAME);
		if (prefs == null) {
			prefs = new IniPrefs(getName()) {
				public String getPreferredServiceName() {
					return PREFS_NAME;
				}
			};
			prefs.load();
			registry.register(prefs);
			standalone = true;
		}
		load();

		Container c = getContentPane();
		if ((view & MENU) != 0)
			setJMenuBar(createMenu());
		if ((view & TOOL) != 0)
			c.add(createToolBar(JToolBar.HORIZONTAL), "North");
		c.add(createBook(), "Center");
		if ((view & STATUS) != 0)
			c.add(createStatusBar(), "South");
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				actionPerformed(new ActionEvent(this, 0, standalone ? AddressBookResources.MENU_EXIT
						: AddressBookResources.MENU_CLOSE));
			}
		});
		pack();
		setVisible(true);
		setStatusText(PROGRAMNAME);
	}

	public static ImageIcon getResourceIcon(String name) {
		name = "resource/image/" + name;
		try {
			ClassLoader cl = AddressBookFrame.class.getClassLoader();
			if (cl instanceof java.net.URLClassLoader)
				return new ImageIcon(((java.net.URLClassLoader) cl).findResource(name));
			else
				return new ImageIcon(cl.getResource(name));
		} catch (Exception e) { System.err.printf("resource %s load error%n", name);
		}
		return  new ImageIcon();
	}

	public String getName() {
		return PROGRAMNAME;
	}

	public String getVersion() {
		return VERSION;
	}

	public void save() {
		IniPrefs s = (IniPrefs) registry.getService(PREFS_NAME);
		s.setProperty(ABVIEW, VIEW, new Integer(view));
		s.setProperty(ABVIEW, SORTBY, new Integer(sortField));
		s.setProperty(ABVIEW, NAMESORT, new Integer(subSortField));
		s.setProperty(ABVIEW, SORTORDER, new Integer(sortDir ? 1 : 0));
		Rectangle r = getBounds();
		Integer[] boundsHolder = new Integer[] { new Integer(r.x), new Integer(r.y), new Integer(r.width),
				new Integer(r.height) };
		s.setProperty(ABVIEW, BOUNDS, boundsHolder);
		if (standalone)
			s.save();
		if (readFailed) {
			if (JOptionPane.showConfirmDialog(this, AddressBookResources.LABEL_CONFIRM_OVERWRITE,
					AddressBookResources.TITLE_CONFIRM, JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.CANCEL_OPTION)
				return;
		}
		boolean locAcc = s.getInt(s.getProperty(OptionsFrame.OPTIONS, OptionsFrame.ACCESS), 0) == 0;
		String loc = (String) s.getProperty(OptionsFrame.OPTIONS, OptionsFrame.LOCATION);
		// TODO smarter save procedure, when can't save remotely try to save
		// locally
		OutputStream os = null;
		try {
			if (locAcc) {
				if (loc != null && loc.length() > 0)
					os = new FileOutputStream(loc);
				else
					os = new FileOutputStream(properties.getProperty(HOME, "." + File.separatorChar)
							+ File.separatorChar + STOR_NAME);
				
				System.err.printf("Loc: %s and file %s%n", loc, properties.getProperty(HOME, "." + File.separatorChar)
							+ File.separatorChar + STOR_NAME);
				io.write(folders, os, "utf-8");
			} else { // upload
				if (loc.indexOf('?') > 0)
					loc += "&submit.x=1";
				else
					loc += "?submit.x=1";
				// System.err.println("Save:"+loc);
				URLConnection con = new URL(loc).openConnection();
				// TODO switch based on selected authentication schema
				String pass = (String) s.getProperty(OptionsFrame.OPTIONS, OptionsFrame.PASSWORD);
				if (pass != null) {
					if (pass.length() > 0)
						pass = pass.substring(1);
					if (AddressBookResources.LABEL_AUTHONTEFICATS[1].equals((String) s.getProperty(
							OptionsFrame.OPTIONS, OptionsFrame.AUTH)))
						con.setRequestProperty(Static.HTTP.AUTHORIZATION, "Basic "
								+ Base64Codecs.base64Encode((""
										+ s.getProperty(OptionsFrame.OPTIONS, OptionsFrame.LOGIN) + ":" + DataConv
										.encryptXor(new String(DataConv.hexToBytes(pass), Static.CharSet.ASCII)))
										.getBytes()));
				}
				String boundary = Long.toHexString(new Random().nextLong());
				con.setRequestProperty(Static.HTTP.CONTENT_TYPE, Static.HTTP.MULTIPARTDATA + "; "
						+ Static.HTTP.BOUNDARY_EQ + boundary);
				con.setDoOutput(true);
				os = con.getOutputStream();
				os.write((Static.HTTP.BOUNDARY_END_SFX + boundary + CRLF).getBytes());
				os.write((Static.HTTP.CONTENT_DISP + ": " + Static.HTTP.FORM_DATA + "; " + Static.HTTP.NAME_EQ_QT
						+ "data" + "\"; " + Static.HTTP.FILENAME_EQ_QT + STOR_NAME + "\"" + CRLF).getBytes());
				os.write((Static.HTTP.CONTENT_TYPE + ": " + "text/xml" + "; charset=utf-8" + CRLF).getBytes());
				os.write((CRLF).getBytes());
				io.write(folders, os, "utf-8");

				os.write((CRLF + Static.HTTP.BOUNDARY_END_SFX + boundary + Static.HTTP.BOUNDARY_END_SFX + CRLF)
						.getBytes());
				os.flush();
				System.err.println("code:" + ((HttpURLConnection) con).getResponseCode());
			}
		} catch (FileNotFoundException fnfe) {
			System.err.println("Can't create file " + properties.getProperty(HOME, "./") + STOR_NAME + ' ' + fnfe);
		} catch (IOException ioe) {
			System.err.println("IO problem at writing back: " + ioe);
		} catch (NullPointerException npe) {
			npe.printStackTrace();
		} finally {
			try {
				os.close();
			} catch (Exception e) {
			}
		}
	}

	public void load() {
		IniPrefs s = (IniPrefs) registry.getService(PREFS_NAME);
		if (standalone)
			s.load();
		view = s.getInt(s.getProperty(ABVIEW, VIEW), MENU + TOOL + STATUS + SEARCH + FOLDER);
		sortField = s.getInt(s.getProperty(ABVIEW, SORTBY), BaseAttrTableModel.UN_SORT);
		subSortField = s.getInt(s.getProperty(ABVIEW, NAMESORT), BaseAttrTableModel.FIRSTNAME_SORT);
		sortDir = s.getInt(s.getProperty(ABVIEW, SORTORDER), 0) != 0;
		Object[] boundsHolder = (Object[]) s.getProperty(ABVIEW, BOUNDS);
		;
		if (boundsHolder != null)
			setBounds(((Integer) boundsHolder[0]).intValue(), ((Integer) boundsHolder[1]).intValue(),
					((Integer) boundsHolder[2]).intValue(), ((Integer) boundsHolder[3]).intValue());
		// TODO: consider as a separate procedure and do reload on options
		// changing
		// some kind of properties/options change listener
		InputStream is = null;
		boolean locAcc = s.getInt(s.getProperty(OptionsFrame.OPTIONS, OptionsFrame.ACCESS), 0) == 0;
		String loc = (String) s.getProperty(OptionsFrame.OPTIONS, OptionsFrame.LOCATION);

		if (locAcc) {
			if (loc == null)
				loc = properties.getProperty(HOME, "." + File.separatorChar) + File.separatorChar + STOR_NAME;
			try {
				System.err.printf("Loc read:%s%n", loc);
				is = new FileInputStream(loc);
			} catch (FileNotFoundException fnfe) {
				System.err.println("Couldn't load from " + loc + ", " + fnfe);
			}
		} else {
			try {
				URLConnection con = new URL(loc).openConnection();
				// TODO switch based on selected authentication schema
				String pass = (String) s.getProperty(OptionsFrame.OPTIONS, OptionsFrame.PASSWORD);
				if (pass != null) {
					if (pass.length() > 0)
						pass = pass.substring(1);
					if (AddressBookResources.LABEL_AUTHONTEFICATS[1].equals((String) s.getProperty(
							OptionsFrame.OPTIONS, OptionsFrame.AUTH)))
						con.setRequestProperty(Static.HTTP.AUTHORIZATION, "Basic "
								+ Base64Codecs.base64Encode((""
										+ s.getProperty(OptionsFrame.OPTIONS, OptionsFrame.LOGIN) + ":" + DataConv
										.encryptXor(new String(DataConv.hexToBytes(pass), Static.CharSet.ASCII)))
										.getBytes()));
				}
				is = con.getInputStream();
				locAcc = false;
			} catch (Exception e) { // IO, MalFormedURL
				System.err.println("Can't open URL " + loc + ", " + e + ", file attempt will be taken.");
			}
			if (is == null)
				try {
					is = new FileInputStream(loc);
				} catch (FileNotFoundException fnfe) {
					System.err.println("Couldn't load from " + loc + ", " + fnfe);
				}
		}
		if (is != null) {
			// s.setProperty(OptionsFrame.OPTIONS, OptionsFrame.LOCATION, loc);
			// s.setProperty(OptionsFrame.OPTIONS, OptionsFrame.ACCESS, locAcc ?
			// AddressBookResources.YES_INT
			// : AddressBookResources.NO_INT);
			try {
				folders = io.read(is);
			} catch (AddressException ae) {
				readFailed = true;
				System.err.println("Exception at reading ab:" + ae);
			} finally {
				try {
					is.close();
				} catch (IOException ioe) {
				}
			}
		} else
			readFailed = true;
		if (folders == null) {
			folders = new Vector();
			folders.add(new Folder<Contact>(AddressBookResources.LABEL_PERSONS));
		}
		OptionsFrame.applyLocale(s);
	}

	public void setStatusText(String _text) {
		if (statusLine != null)
			statusLine.setText(_text);
	}

	JMenuBar createMenu() {
		JMenuBar menubar = new JMenuBar();
		JMenu menu, menu2;
		JMenuItem item;
		// file
		menubar.add(menu = new JMenu(AddressBookResources.MENU_FILE));
		createNewMenu(menu, this);
		menu.addSeparator();
		menu.add(item = new JMenuItem(AddressBookResources.MENU_PROPERTIES));
		item.addActionListener(this);
		menu.add(item = new JMenuItem(AddressBookResources.MENU_DELETE));
		item.addActionListener(this);
		menu.addSeparator();
		menu.add(menu2 = new JMenu(AddressBookResources.MENU_IMPORT));
		menu2.add(item = new JMenuItem(AddressBookResources.MENU_ADDRBOOK));
		menu2.add(item = new JMenuItem(AddressBookResources.MENU_BUSCARD));
		menu2.add(new AbstractAction(AddressBookResources.MENU_OTHERADDRBOOK) {
			public void actionPerformed(ActionEvent ae) {
				Object importType = JOptionPane.showInputDialog(AddressBookFrame.this,
						AddressBookResources.LABEL_SELECT_IMPORT_FORMAT, AddressBookResources.TITLE_IMPORT_TYPE,
						JOptionPane.QUESTION_MESSAGE, null, AddressBookResources.LABELS_IMP_EXP_FMT_NAME,
						"Outlook .CSV format");
				if ("Outlook .CSV format".equals(importType)) {
					JFileChooser chooser = new JFileChooser() {
						public boolean accept(File f) {
							return f.isDirectory() || f.getName().toLowerCase().endsWith(".csv");
						}
					};
					if (chooser.CANCEL_OPTION != chooser.showOpenDialog(AddressBookFrame.this)) {
						InputStream fis = null;
						try {
							if (folders == null)
								folders = new ArrayList();
							if (folders.size() == 0)
								folders.add(new Folder<Contact>(AddressBookResources.LABEL_PERSONS));
							Folder contacts = (Folder) folders.get(0);
							Csv csv = new Csv(fis = new FileInputStream(chooser.getSelectedFile()), false, "utf-8",
									',', true);
							Map header = csv.getMetaData();
							// "Title","First Name","Middle Name","Last
							// Name","Suffix","Company","Department",
							// "Job Title","Business Street","Business Street
							// 2","Business Street 3","Business City",
							// "Business State","Business Postal Code","Business
							// Country","Home Street","Home Street 2",
							// "Home Street 3","Home City","Home State","Home
							// Postal Code","Home Country","Other Street",
							// "Other Street 2","Other Street 3","Other
							// City","Other State","Other Postal Code","Other
							// Country",
							// "Assistant's Phone","Business Fax","Business
							// Phone","Business Phone 2","Callback","Car Phone",
							// "Company Main Phone","Home Fax","Home
							// Phone","Home Phone 2","ISDN","Mobile
							// Phone","Other Fax",
							// "Other Phone","Pager","Primary Phone","Radio
							// Phone","TTY/TDD
							// Phone","Telex","Account","Anniversary",
							// "Assistant's Name","Billing
							// Information","Birthday","Categories","Children","Directory
							// Server",
							// "E-mail Address","E-mail Display Name","E-mail 2
							// Address","E-mail 2 Display Name",
							// "E-mail 3 Address","E-mail 3 Display
							// Name","Gender","Government ID
							// Number","Hobby","Initials",
							// "Internet Free
							// Busy","Keywords","Language","Location","Manager's
							// Name","Mileage","Notes",
							// "Office Location","Organizational ID Number","PO
							// Box","Priority","Private","Profession",
							// "Referred By","Sensitivity","Spouse","User
							// 1","User 2","User 3","User 4","Web Page"

							// google outlook mapping Name,E-mail,Notes,E-mail
							// 2,E-mail 3,Mobile Phone,Pager,Company,Job
							// Title,Home Phone,Home Phone 2,Home Fax,Home
							// Address,Business Phone,Business Phone 2,Business
							// Fax,Business Address,Other Phone,Other Fax,Other
							// Address
							// find name fields
							int[] names = new int[4]; // first(name) last
							// middle, suff
							Arrays.fill(names, 0, names.length - 1, -1);
							int titleIdx = -1;
							for (Object fn : header.keySet()) {
								String s = ((String) fn).toLowerCase();
								// System.out.printf("Checking %s of %s\n", s,
								// fn);
								if (s.indexOf("name") >= 0) {
									if (s.indexOf("last") >= 0)
										names[1] = (Integer) header.get(fn);
									else if (s.indexOf("first") >= 0)
										names[0] = (Integer) header.get(fn);
									else if (s.indexOf("middle") >= 0)
										names[2] = (Integer) header.get(fn);
									else if (names[0] < 0) // not quite robust
										names[0] = (Integer) header.get(fn);

								} else if (s.indexOf("suff") >= 0)
									names[3] = (Integer) header.get(fn);
								else if (s.indexOf("title") >= 0)
									titleIdx = (Integer) header.get(fn);
							}
							// System.out.printf("Name ics: %d, %d, %d, %d\n",
							// names[0], names[1], names[2], names[3]);
							while (csv.next()) {
								try {
									// TODO: add merge option, contact can be
									// already there
									Contact c = new Contact(new Name((names[0] > 0 ? csv.getString(names[0]) : "")
											+ " " + (names[1] > 0 ? csv.getString(names[1]) : "") + ", "
											+ (names[2] > 0 ? csv.getString(names[2]) : "")
											+ (names[3] > 0 ? "(" + csv.getString(names[0]) + ")" : "")));
									if (titleIdx > 0)
										c.setTitle(csv.getString(titleIdx));
									for (String fn : (Set<String>) header.keySet()) {
										String s = fn.toLowerCase();
										if (s.indexOf("e-mail") >= 0) {
											String e = csv.getString(fn);
											if (e != null) {
												String dn = fn;
												try {
													dn = csv.getString(fn + " Display Name");
												} catch (IOException ioe) {
												}
												c.add(new EMail(e, dn, s));
											}
										} else if (s.indexOf("phone") >= 0 || s.indexOf("fax") >= 0
												|| s.indexOf("pager") >= 0) {
											String n = csv.getString(fn);
											if (n != null)
												c.add(new Telephone(n, fn, s));
										} else if (s.indexOf("address") >= 0 && s.indexOf("e-mail") < 0) {
											String a = csv.getString(fn);
											if (a != null)
												c.add(new Address(a, fn, s));
										} else if (s.indexOf("street") >= 0) {
											String pr = fn.substring(0, s.indexOf("street"));
											String a = csv.getString(pr + "Street");
											if (a != null && a.length() > 0) {
												a += csv.getString(pr + "Street 2") + csv.getString(pr + "Street 3")
														+ '\n' + csv.getString(pr + "City") + ','
														+ csv.getString(pr + "Postal Code") + '\n'
														+ csv.getString(pr + "Country");
												c.add(new Address(a, fn, s));
											}
										} else if (s.indexOf("web") >= 0) {
											String l = csv.getString(fn);
											if (l != null)
												c.add(new Link(l, fn));
										} else if (s.indexOf("birthday") >= 0) {
											String b = csv.getString(fn);
											if (b != null)
												try {
													c.setDOB(new SimpleDateFormat("MM/dd/yy").parse(b));
												} catch (ParseException pe) {
												}
										}
									}
									// TODO: consider as adding unique contact,
									// so can be merging
									contacts.add(c);
								} catch (java.text.ParseException pe) {
									System.err.println("Couldn't parse");
								}
							}
						} catch (IOException ioe) {
							ioe.printStackTrace();
						} finally {
							try {
								fis.close();
							} catch (Exception e) {

							}
						}
						tree.setModel(new DefaultTreeModel(createTreeModel(folders), false));
					}
				}
			}
		});
		menu.add(menu2 = new JMenu(AddressBookResources.MENU_EXPORT));
		menu2.add(new AbstractAction(AddressBookResources.MENU_ADDRBOOK) {
			public void actionPerformed(ActionEvent ae) {
				JFileChooser chooser = new JFileChooser() {
					public boolean accept(File f) {
						return f.isDirectory() || f.getName().toLowerCase().endsWith(".csv");
					}
				};
				if (chooser.CANCEL_OPTION != chooser.showOpenDialog(AddressBookFrame.this)) {

				}
			}
		});
		menu2.add(item = new JMenuItem(AddressBookResources.MENU_BUSCARD));
		// TODO: consider to remane in vCard
		menu2.add(new AbstractAction(AddressBookResources.MENU_IPODCONTACTS) {
			public void actionPerformed(ActionEvent a) {
				OutputStream out = null;
				try {
					IniPrefs s = (IniPrefs) registry.getService(AddressBookFrame.PREFS_NAME);
					boolean locAcc = s.getInt(s.getProperty(OptionsFrame.OPTIONS, OptionsFrame.ACCESS), 0) == 0;
					if (locAcc) {
						String loc = (String) s.getProperty("IpodOptionsTab", OptionsFrame.IPOD_DEVICE);
						if (loc != null && loc.length() > 0) {
							Folder f = (Folder) folders.get(0);
							List<XMLSaver> l = f.getContent();
							for (XMLSaver saver : l) {
								try {
									out = new FileOutputStream(loc + "/Contacts/" + saver + ".vcf");
									saver.saveVCard(out, "UTF-8", 0);
								} finally {
									if (out != null)
										try {
											out.close();
										} catch (IOException ioe) {
										}
									out = null;
								}
							}
						} else {
							JFileChooser fc = new JFileChooser();
							fc.setDialogType(JFileChooser.SAVE_DIALOG);
							fc.showSaveDialog(AddressBookFrame.this);
							File targetPath = fc.getSelectedFile();
							if (targetPath != null)
								out = new FileOutputStream(targetPath);
							((XMLSaver) folders.get(0)).saveVCard(out, "UTF-8", 0);
						}
					}
				} catch (IOException ioe) {
					ioe.printStackTrace();
				} finally {
					if (out != null)
						try {
							out.close();
						} catch (IOException ioe) {
						}
				}
			}
		});
		menu2.add(new AbstractAction(AddressBookResources.MENU_OTHERADDRBOOK) {
			public void actionPerformed(ActionEvent ae) {
				CSVOptionPanel.doExport((Folder) folders.get(0), AddressBookFrame.this);
			}
		});
		menu.addSeparator();
		menu.add(item = new JMenuItem(AddressBookResources.MENU_PRINT));
		item.addActionListener(this);
		menu.addSeparator();
		menu.add(item = new JMenuItem(standalone ? AddressBookResources.MENU_EXIT : AddressBookResources.MENU_CLOSE));
		item.addActionListener(this);
		// edit
		menubar.add(menu = new JMenu(AddressBookResources.MENU_EDIT));
		menu.add(item = new JMenuItem(AddressBookResources.MENU_COPY));
		item.setAccelerator(KeyStroke.getKeyStroke("control C"));
		item.addActionListener(this);
		menu.add(item = new JMenuItem(AddressBookResources.MENU_PASTE));
		item.setAccelerator(KeyStroke.getKeyStroke("control V"));
		item.addActionListener(this);
		menu.addSeparator();
		menu.add(item = new JMenuItem(AddressBookResources.MENU_SELECTALL));
		item.setAccelerator(KeyStroke.getKeyStroke("control A"));
		item.addActionListener(this);
		menu.addSeparator();
		menu.add(item = new JMenuItem(AddressBookResources.MENU_PROFILE));
		item.addActionListener(this);
		menu.addSeparator();
		menu.add(item = new JMenuItem(AddressBookResources.MENU_FINDPEOPLE));
		item.setAccelerator(KeyStroke.getKeyStroke("control F"));
		item.addActionListener(this);
		// view
		menubar.add(menu = new JMenu(AddressBookResources.MENU_VIEW));
		menu.add(m_toolBar = new JCheckBoxMenuItem(AddressBookResources.MENU_TOOLBAR));
		m_toolBar.addActionListener(this);
		m_toolBar.setSelected((view & TOOL) != 0);
		menu.add(m_statusBar = new JCheckBoxMenuItem(AddressBookResources.MENU_STATUSBAR));
		m_statusBar.addActionListener(this);
		m_statusBar.setSelected((view & STATUS) != 0);
		menu.add(m_folder = new JCheckBoxMenuItem(AddressBookResources.MENU_FOLDERGROUP));
		m_folder.addActionListener(this);
		m_folder.setSelected((view & FOLDER) != 0);
		menu.addSeparator();
		menu.add(menu2 = new JMenu(AddressBookResources.MENU_SORTBY));
		ButtonGroup bg = new ButtonGroup();
		menu2.add(item = new JRadioButtonMenuItemEx(new RadioAction(AddressBookResources.MENU_NAME)));
		bg.add(item);
		item.setSelected(BaseAttrTableModel.NAME_SORT == sortField);
		menu2.add(item = new JRadioButtonMenuItemEx(new RadioAction(AddressBookResources.MENU_EMAILADDR)));
		bg.add(item);
		item.setSelected(BaseAttrTableModel.E_MAIL_SORT == sortField);
		menu2.add(item = new JRadioButtonMenuItemEx(new RadioAction(AddressBookResources.MENU_PHONE)));
		bg.add(item);
		item.setSelected(BaseAttrTableModel.TPHONE_SORT == sortField);
		menu2.addSeparator();
		bg = new ButtonGroup();
		menu2.add(item = new JRadioButtonMenuItemEx(new RadioAction(AddressBookResources.MENU_FIRSTNAME)));
		bg.add(item);
		item.setSelected(BaseAttrTableModel.FIRSTNAME_SORT == subSortField);
		menu2.add(item = new JRadioButtonMenuItemEx(new RadioAction(AddressBookResources.MENU_LASTNAME)));
		bg.add(item);
		item.setSelected(BaseAttrTableModel.LASTNAME_SORT == subSortField);
		menu2.addSeparator();
		bg = new ButtonGroup();
		menu2.add(item = new JRadioButtonMenuItemEx(new RadioAction(AddressBookResources.MENU_ASC)));
		bg.add(item);
		item.setSelected(sortDir);
		menu2.add(item = new JRadioButtonMenuItemEx(new RadioAction(AddressBookResources.MENU_DESC)));
		bg.add(item);
		item.setSelected(sortDir);
		menu.addSeparator();
		menu.add(item = new JMenuItem(AddressBookResources.MENU_REFRESH));
		item.setAccelerator(KeyStroke.getKeyStroke("F5"));
		item.addActionListener(this);
		// tools
		menubar.add(menu = new JMenu(AddressBookResources.MENU_TOOLS));
		menu.add(item = new JMenuItem(AddressBookResources.MENU_ACCOUNTS));
		item.addActionListener(this);
		menu.addSeparator();
		menu.add(item = new JMenuItem(AddressBookResources.MENU_OPTIONS));
		item.addActionListener(this);
		menu.addSeparator();
		menu.add(menu2 = new JMenu(AddressBookResources.MENU_ACTION));
		createActionMenu(menu2, this);
		menu.addSeparator();
		menu.add(item = new JMenuItem(AddressBookResources.MENU_SYNCHRONIZE));
		item.addActionListener(this);
		// help
		menubar.add(menu = new JMenu(AddressBookResources.MENU_HELP));
		menu.add(item = new JMenuItem(AddressBookResources.MENU_CONTENTS));
		item.setAccelerator(KeyStroke.getKeyStroke("F1"));
		item.addActionListener(this);
		menu.addSeparator();
		menu.add(item = new JMenuItem(AddressBookResources.MENU_ABOUT + AddressBookFrame.PROGRAMNAME));
		item.addActionListener(this);
		return menubar;
	}

	JComponent createNewMenu(JComponent wrapper, ActionListener listener) {
		JMenuItem item;
		wrapper.add(item = new JMenuItem(AddressBookResources.MENU_NEWCONTACT));
		item.setAccelerator(KeyStroke.getKeyStroke("control N"));
		item.addActionListener(listener);
		wrapper.add(item = new JMenuItem(AddressBookResources.MENU_NEWBOOKMARK));
		item.setAccelerator(KeyStroke.getKeyStroke("control B"));
		item.addActionListener(listener);
		wrapper.add(item = new JMenuItem(AddressBookResources.MENU_NEWCOOKIE));
		item.setAccelerator(KeyStroke.getKeyStroke("control O"));
		item.addActionListener(listener);
		wrapper.add(item = new JMenuItem(AddressBookResources.MENU_NEWGROUP));
		item.setAccelerator(KeyStroke.getKeyStroke("control G"));
		item.addActionListener(listener);
		wrapper.add(item = new JMenuItem(AddressBookResources.MENU_NEWFOLDER));
		item.setAccelerator(KeyStroke.getKeyStroke("control R"));
		item.addActionListener(listener);
		return wrapper;
	}

	JComponent createActionMenu(JComponent wrapper, ActionListener listener) {
		JMenuItem item;
		for (int i = 0; i < AddressBookResources.MENUS_ACTION.length; i++) {
			wrapper.add(item = new JMenuItem(AddressBookResources.MENUS_ACTION[i]));
			item.addActionListener(listener);
		}
		return wrapper;
	}

	JToolBar createToolBar(int orientation) {
		JToolBar toolbar = new JToolBar(orientation);
		JButton btn;
		btn = toolbar.add(new ToolAction(AddressBookResources.IMG_NEW, null, AddressBookResources.TTIP_NEW_ELEMENT));
		btn = toolbar.add(new ToolAction(AddressBookResources.IMG_PROPERTIES, AddressBookResources.MENU_PROPERTIES,
				AddressBookResources.TTIP_PROPERTY));
		btn = toolbar.add(new ToolAction(AddressBookResources.IMG_DELETE, AddressBookResources.MENU_DELETE));
		btn = toolbar.add(new ToolAction(AddressBookResources.IMG_FINDPEOPLE, AddressBookResources.MENU_FINDPEOPLE));
		btn = toolbar.add(new ToolAction(AddressBookResources.IMG_PRINT, AddressBookResources.MENU_PRINT));
		btn = toolbar.add(new ToolAction(AddressBookResources.IMG_ACTION));
		return toolbar;
	}

	JTable table;

	JTree tree;

	JComponent lastFocused;

	JComponent createBook() {
		JPanel abp = new JPanel();
		abp.setLayout(new BorderLayout());
		JPanel sp = new JPanel();
		sp.setLayout(new FlowLayout());
		sp.add(new JLabel(AddressBookResources.LABEL_TNAMEORLIST));
		JTextField tfSearch;
		sp.add(tfSearch = new JTextField(20));
		SearchPerformer searcher = new SearchPerformer();
		tfSearch.getDocument().addDocumentListener(searcher);
		tfSearch.addActionListener(searcher);
		abp.add(sp, "North");
		// TODO: show selected folders
		table = new JTable(null /*
								 * new
								 * BaseAttrTableModel(((Folder)folders.elementAt(0)).getContent())
								 */
		);
		// FocusTracer ft;
		table.addFocusListener(new FocusTracer());
		ComboCellRenderer cellEdRer;
		table.setDefaultRenderer(JComboBox.class, cellEdRer = new ComboCellRenderer());
		table.setDefaultEditor(JComboBox.class, cellEdRer);
		table.addMouseListener(new MouseInputAdapter() {
			public void mouseClicked(MouseEvent e) {
				if ((e.getModifiers() & InputEvent.BUTTON3_MASK) > 0) {
					// TODO: getRMouseMenu().show(PhotoCollectionPanel.this,
					// e.getX(), e.getY());
				} else if (e.getClickCount() == 2) {
					actionPerformed(new ActionEvent(this, 0, AddressBookResources.MENU_PROPERTIES));
				}
			}
		});
		abp.add(new JScrollPane(table), "Center");
		if ((view & FOLDER) != 0) {
			tree = new JTree(createTreeModel(folders));
			tree.addFocusListener(new FocusTracer());
			tree.setRootVisible(false);
			tree.addTreeSelectionListener(new TreeSelectionListener() {
				public void valueChanged(TreeSelectionEvent e) {
					// Stupid, stupid Sun-Netscape who decoupled an object with
					// its visualisation?
					try {
						// we do nott need to do sorting, because data stored
						// sorted
						// BaseAttrTableModel bam = new
						// BaseAttrTableModel(((Folder)getSelectedValueByName(folders,
						// tree.getLastSelectedPathComponent().toString())).getContent());
						// bam.sort(sortField, subSortField, sortDir);
						table.setModel(new BaseAttrTableModel(((Folder) getSelectedValueByName(folders, tree
								.getLastSelectedPathComponent().toString()))));
					} catch (NullPointerException npe) {
						table.setModel(new BaseAttrTableModel(new Folder("")));
					}
				}
			});
			tree.setSelectionRow(0); // setSelectionPath(tree.getRoot()
			return new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, new JScrollPane(tree), new JScrollPane(abp));
		} else {
			return abp;
		}
	}

	static Object getSelectedValueByName(List _elements, String _name) {
		// TODO use folder operation to get folder by name
		for (int i = 0; i < _elements.size(); i++)
			if (_elements.get(i).toString().equals(_name))
				return _elements.get(i);
		return null;
	}

	static TreeNode createTreeModel(List _elements) {

		return new FolderTreeNode(_elements);
	}

	public static class FolderTreeNode implements TreeNode {
		List<Folder> folders;

		FolderTreeNode parent;

		String name;

		FolderTreeNode(List _folders) {
			folders = _folders;
			name = PROGRAMNAME;
		}

		FolderTreeNode(Folder _folder, FolderTreeNode _parent) {
			folders = new ArrayList<Folder>();
			for (Object o : _folder.getContent()) {
				if (o instanceof Folder)
					folders.add((Folder) o);
			}
			name = _folder.getShortName();
			parent = _parent;
		}

		public TreeNode getChildAt(int arg0) {
			if (folders != null && folders.size() > 0 && arg0 >= 0 && arg0 < folders.size())
				return new FolderTreeNode(folders.get(arg0), this);
			return null;
		}

		public int getChildCount() {
			return folders == null ? 0 : folders.size();
		}

		public TreeNode getParent() {
			return parent;
		}

		public int getIndex(TreeNode arg0) {
			for (int i = 0; i < folders.size(); i++)
				if (folders.get(i).getShortName().equals(arg0.toString()))
					return i;
			throw new IllegalArgumentException("No such node:" + arg0);
		}

		public boolean getAllowsChildren() {
			return true;
		}

		public boolean isLeaf() {
			return getChildCount() == 0;
		}

		public Enumeration children() {
			throw new RuntimeException();
		}

		public String toString() {
			return name;
		}

	}

	public static JPanel createButtonPanel(ActionListener al) {
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

	JPanel createStatusBar() {
		JPanel p = new JPanel();
		p.add(statusLine = new JLabel(AddressBookResources.LABEL_STATUS_BAR, SwingConstants.RIGHT));
		p.setBorder(new BevelBorder(BevelBorder.LOWERED));
		return p;
	}

	public void actionPerformed(ActionEvent a) {
		String cmd = a.getActionCommand();
		if (cmd == null)
			return;
		if (cmd.equals(AddressBookResources.MENU_FINDPEOPLE)) {
		} else if (cmd.equals(AddressBookResources.MENU_NEWCONTACT)) {
			if (table.getModel() != null && table.getModel() instanceof BaseAttrTableModel)
				new ContactFrame(this);
			else
				JOptionPane.showMessageDialog(this, AddressBookResources.LABEL_NOFOLDER);
		} else if (cmd.equals(AddressBookResources.MENU_NEWFOLDER)) {
			String folderName = JOptionPane.showInputDialog(this, AddressBookResources.LABEL_FOLDER_NAME,
					AddressBookResources.TITLE_ENTER, JOptionPane.QUESTION_MESSAGE);
			if (folderName != null) {
				if (getSelectedValueByName(folders, folderName) == null) {
					folders.add(new Folder<Contact>(folderName));
					tree.setModel(new DefaultTreeModel(createTreeModel(folders), false));
				} else {
					JOptionPane.showMessageDialog(this, AddressBookResources.LABEL_DUP_FLDR,
							AddressBookResources.TITLE_WARNING, JOptionPane.WARNING_MESSAGE);
				}
			}
		} else if (cmd.equals(AddressBookResources.MENU_PROPERTIES)) {
			if (table.hasFocus() || lastFocused == table) {
				int sel = table.getSelectedRow();
				if (sel > -1)
					new ContactFrame(this, sel);
			} else if (tree.hasFocus() || lastFocused == tree) {
				try {
					Folder folder = (Folder) getSelectedValueByName(folders, tree.getLastSelectedPathComponent()
							.toString());
					if (folder != null)
						JOptionPane.showMessageDialog(this, "Properties of folder " + folder);
				} catch (Exception e) { // just ignore all this scam
				}
			}
		} else if (cmd.equals(AddressBookResources.MENU_DELETE)) {
			if (table.hasFocus() || lastFocused == table) {
				int[] sel = table.getSelectedRows();
				if (sel != null && sel.length > 0) {
					if (JOptionPane.showConfirmDialog(this, AddressBookResources.LABEL_CONF_DEL_CONT,
							AddressBookResources.TITLE_CONFIRM, // TODO:
							// how
							// many
							// items
							JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
						// TODO: can use iterator?
						Contact[] contacts = new Contact[sel.length];
						for (int i = 0; i < sel.length; i++)
							contacts[i] = ((BaseAttrTableModel) table.getModel()).getValueAt(sel[i]);
						for (int i = 0; i < contacts.length; i++)
							((BaseAttrTableModel) table.getModel()).removeValue(contacts[i]);
						table.revalidate();
					}
				}
			} else if (tree.hasFocus() || lastFocused == tree) {
				try {
					if (JOptionPane
							.showConfirmDialog(this, AddressBookResources.LABEL_CONF_DEL_FLDR,
									AddressBookResources.TITLE_CONFIRM, JOptionPane.YES_NO_OPTION,
									JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
						folders.remove(getSelectedValueByName(folders, tree.getLastSelectedPathComponent().toString()));
						tree.setModel(new DefaultTreeModel(createTreeModel(folders), false));
					}
				} catch (Exception e) { // just ignore all this scam
				}
			}
		} else if (cmd.equals(AddressBookResources.MENU_OPTIONS)) {
			new OptionsFrame(this);
		} else if (cmd.equals(AddressBookResources.MENU_EXIT)) {
			save();
			dispose();
			System.exit(0);
			// TODO: all action code should be unified, using polymorphism
		} else if (cmd.equals(AddressBookResources.MENU_SENDMAIL)) {
			Contact contact = getSelectedContact();
			if (contact != null) {
				List mails = contact.getEMails();
				if (mails != null) {
					if (mails.size() == 1)
						sendMail(((EMail) mails.get(0)).getValue());
					else if (mails.size() > 1) {
						EMail[] mailsArr = new EMail[mails.size()];
						mails.toArray(mailsArr);
						mailsArr[0] = (EMail) JOptionPane.showInputDialog(this,
								AddressBookResources.LABEL_SELECT_EMAIL, AddressBookResources.TITLE_ENTER,
								JOptionPane.QUESTION_MESSAGE, null, mailsArr, mailsArr[0]);
						if (mailsArr[0] != null)
							sendMail(mailsArr[0].getValue());
					}
				} else
					System.err.printf("No e-mails defined in contact%s%n", contact);
			}
		} else if (cmd.equals(AddressBookResources.MENU_SHOWURL)) {
			Contact contact = getSelectedContact();
			if (contact != null) {
				List links = contact.getLinks();
				if (links != null) {
					if (links.size() == 1)
						showBookmark(((Link) links.get(0)).getValue());
					else if (links.size() > 1) {
						Link[] linksArr = new Link[links.size()];
						links.toArray(linksArr);
						linksArr[0] = (Link) JOptionPane.showInputDialog(this, AddressBookResources.LABEL_SELECT_LINK,
								AddressBookResources.TITLE_ENTER, JOptionPane.QUESTION_MESSAGE, null, linksArr,
								linksArr[0]);
						if (linksArr[0] != null)
							showBookmark(linksArr[0].getValue());
					}
				}
			}
		} else if (cmd.equals(AddressBookResources.MENU_GETDIRECTIONS)) {
		} else if (cmd.equals(AddressBookResources.MENU_DIALTO)) {
			Contact contact = getSelectedContact();
			if (contact != null) {
				List phones = contact.getTelephones();
				if (phones != null) {
					if (phones.size() == 1)
						dialPhone(((EMail) phones.get(0)).getValue());
					else if (phones.size() > 1) {
						Telephone[] phonesArr = new Telephone[phones.size()];
						phones.toArray(phonesArr);
						phonesArr[0] = (Telephone) JOptionPane.showInputDialog(this,
								AddressBookResources.LABEL_SELECT_PHONE, AddressBookResources.TITLE_ENTER,
								JOptionPane.QUESTION_MESSAGE, null, phonesArr, phonesArr[0]);
						if (phonesArr[0] != null)
							dialPhone(phonesArr[0].getValue());
					}
				}
			}
		} else if (cmd.equals(AddressBookResources.MENU_COPY)) {
		} else if (cmd.equals(AddressBookResources.MENU_PASTE)) {
		} else if (cmd.equals(AddressBookResources.MENU_SELECTALL)) {
			table.setRowSelectionInterval(0, table.getRowCount() - 1);
		} else if (cmd.equals(AddressBookResources.MENU_CHAT_TO)) {
		} else if (cmd.equals(AddressBookResources.MENU_CLOSE)) {
			//System.err.printf("Saving in%n");
			save();
		} else if (cmd.equals(AddressBookResources.MENU_TOOLBAR)) {
			if (m_toolBar.isSelected())
				view |= TOOL;
			else
				view &= ~TOOL;
			// updateUI();
		} else if (cmd.equals(AddressBookResources.MENU_STATUSBAR)) {
			if (m_statusBar.isSelected())
				view |= STATUS;
			else
				view &= ~STATUS;
			// updateUI();
		} else if (cmd.equals(AddressBookResources.MENU_FOLDERGROUP)) {
			if (m_folder.isSelected())
				view |= FOLDER;
			else
				view &= ~FOLDER;
			// updateUI();
			// sorting group menu
		} else if (cmd.equals(AddressBookResources.MENU_NAME)) {
			sortField = BaseAttrTableModel.NAME_SORT;
			sortModel();
		} else if (cmd.equals(AddressBookResources.MENU_EMAILADDR)) {
			sortField = BaseAttrTableModel.E_MAIL_SORT;
			sortModel();
		} else if (cmd.equals(AddressBookResources.MENU_PHONE)) {
			sortField = BaseAttrTableModel.TPHONE_SORT;
			sortModel();
		} else if (cmd.equals(AddressBookResources.MENU_FIRSTNAME)) {
			subSortField = BaseAttrTableModel.FIRSTNAME_SORT;
			sortModel();
		} else if (cmd.equals(AddressBookResources.MENU_LASTNAME)) {
			subSortField = BaseAttrTableModel.LASTNAME_SORT;
			sortModel();
		} else if (cmd.equals(AddressBookResources.MENU_ASC)) {
			sortDir = true;
			sortModel();
		} else if (cmd.equals(AddressBookResources.MENU_DESC)) {
			sortDir = false;
			sortModel();
		} else if (cmd.equals(AddressBookResources.MENU_REFRESH)) {
			sortModel();
		} else if (cmd.equals(AddressBookResources.MENU_PROFILE)) {
			new ProfileFrame();
		} else if (cmd.indexOf(AddressBookResources.MENU_ABOUT) >= 0) {
			JOptionPane.showMessageDialog(this, "<html><i>" + PROGRAMNAME + "\n" + VERSION + '.' + BUILD + '\n'
					+ "For " + DEDICATED + '\n' + COPYRIGHT + '\n' + "Java " + System.getProperty("java.version")
					+ "JVM " + System.getProperty("java.vendor") + " OS " + System.getProperty("os.name") + ' '
					+ System.getProperty("os.version") + ' ' + System.getProperty("os.arch") + '\n'
					+ Locale.getDefault().getDisplayName(), AddressBookResources.MENU_ABOUT, JOptionPane.PLAIN_MESSAGE,
					getResourceIcon(AddressBookResources.IMG_PROGRAM));
		} else if (cmd.equals(AddressBookResources.MENU_CONTENTS)) {
			Desktop.showUrl(AddressBookResources.URL_HELP);
		}
	}

	void sortModel() {
		if (table.getModel() instanceof BaseAttrTableModel) {
			BaseAttrTableModel bam = (BaseAttrTableModel) table.getModel();
			if (bam != null) {
				bam.sort(sortField, subSortField, sortDir);
				table.revalidate();
				table.repaint();
			}
		}
	}

	Contact getSelectedContact() {
		int sel = table.getSelectedRow();
		if (sel > -1) {
			return ((BaseAttrTableModel) table.getModel()).getValueAt(sel);
		}
		return null;
	}

	void sendMail(String _address) {
		// if (_address == null)
		// return;
		ActionPerformer ap = null;
		try {
			//System.err.printf("Action %s%n", registry.getService("" + COMP_ACTIONPERFORMER).getServiceProvider());
			if (registry.getService("" + COMP_ACTIONPERFORMER) != null)
				ap = (ActionPerformer) registry.getService("" + COMP_ACTIONPERFORMER).getServiceProvider();
		} catch (Exception e) { // class cast
			//e.printStackTrace();
		}
		if (ap != null) {
			ap.act(ActionPerformer.SENDMAIL, _address);
			setState(Frame.ICONIFIED);
		} else {
			// checkSystemClipboardAccess AWTPermission("accessClipboard")
			Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
			cb.setContents(new StringSelection(_address), null);
		}
	}

	void showBookmark(String _bookmark) {
		// if (_bookmark == null)
		// return;
		ActionPerformer ap = null;
		try {
			ap = (ActionPerformer) registry.getService("" + COMP_ACTIONPERFORMER);
		} catch (Exception e) { // class cast
		}
		if (ap != null) {
			ap.act(ActionPerformer.SHOWBOOKMARK, _bookmark);
			setState(Frame.ICONIFIED);
		} else {
			// checkSystemClipboardAccess AWTPermission("accessClipboard")
			Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
			cb.setContents(new StringSelection(_bookmark), null);
			String lcbm = _bookmark.toLowerCase();
			if (lcbm.startsWith("http://") || lcbm.startsWith("ftp://"))
				Desktop.showUrl(_bookmark);
		}
	}

	void dialPhone(String _number) {
		Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
		cb.setContents(new StringSelection(_number), null);
	}

	class SearchPerformer implements ActionListener, DocumentListener {
		public void actionPerformed(ActionEvent e) {
			try {
				if (doSearch(((JTextField) e.getSource()).getText()))
					table.requestFocus();
			} catch (Exception ex) {
			}
		}

		public void changedUpdate(DocumentEvent e) {
			try {
				doSearch(e.getDocument().getText(0, e.getDocument().getLength()));
			} catch (javax.swing.text.BadLocationException ble) {
			}
		}

		public void insertUpdate(DocumentEvent e) {
			changedUpdate(e);
		}

		public void removeUpdate(DocumentEvent e) {
			changedUpdate(e);
		}

		private boolean doSearch(String _text) {
			Object tm = table.getModel();
			if (tm != null && tm instanceof BaseAttrTableModel) {
				// TODO: use bisectional search
				BaseAttrTableModel atm = (BaseAttrTableModel) tm;
				for (int row = 0; row < atm.getRowCount(); row++) {
					Contact contact = atm.getValueAt(row);
					if (sortField == BaseAttrTableModel.NAME_SORT) {
						if (_text.regionMatches(true, 0, subSortField == BaseAttrTableModel.FIRSTNAME_SORT ? contact
								.getName().getFirst() : contact.getName().getLast(), 0, _text.length())) {
							table.setRowSelectionInterval(row, row);
							return true;
						}
					} else if (sortField == BaseAttrTableModel.E_MAIL_SORT
							|| sortField == BaseAttrTableModel.TPHONE_SORT) {
						// TODO: reconsider the algorithm
						List esa = sortField == BaseAttrTableModel.TPHONE_SORT ? contact.getTelephones() : contact
								.getEMails();
						for (int j = 0; j < esa.size(); j++)
							if (_text.regionMatches(true, 0, esa.get(j).toString(), 0, _text.length())) {
								table.setRowSelectionInterval(row, row);
								return true;
							}
					}
				}
			}
			return false;
		}
	}

	class FocusTracer extends FocusAdapter {
		public void focusGained(FocusEvent e) {
			lastFocused = null;
		}

		public void focusLost(FocusEvent e) {
			lastFocused = (JComponent) e.getSource();
		}
	}

	static class ComboCellRenderer extends AbstractCellEditor implements TableCellRenderer, TableCellEditor {
		Component cellEditor;

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
				boolean hasFocus, int row, int column) {
			Component cellRenderer = null;
			if (value != null) {
				if (value instanceof List)
					return new JComboBox(((List) value).toArray());
				else if (value instanceof Vector)
					return new JComboBox((Vector) value);
				try {
					cellRenderer = new JComboBox((String[]) table.getModel().getClass().getDeclaredMethod("getTypes",
							new Class[] {}).invoke(table.getModel(), new Object[] {}));
					((JComboBox) cellRenderer).setSelectedItem(value);
				} catch (Exception e) {
					// cellRenderer = null;
				}
			}

			return cellRenderer;
		}

		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
			cellEditor = getTableCellRendererComponent(table, value, isSelected, true, row, column);
			return cellEditor;
		}

		public Object getCellEditorValue() {
			if (cellEditor != null && cellEditor instanceof JComboBox)
				return ((JComboBox) cellEditor).getSelectedItem();
			return null;
		}

	}

	class ToolAction extends AbstractAction {
		// See interface Action for keys details
		Icon im;

		ToolAction(String name) {
			this(name, null);
		}

		ToolAction(String name, String cmd) {
			this(name, cmd, null);
		}

		ToolAction(String name, String cmd, String _toolTip) {
			super(name);
			if (cmd != null)
				putValue(ACTION_COMMAND_KEY, cmd);
			if (_toolTip != null)
				putValue(SHORT_DESCRIPTION, _toolTip);
			im = getResourceIcon(name + AddressBookResources.EXT_GIF);
		}

		public Object getValue(String key) {
			if (key == SMALL_ICON) // we can use == instead of equals here
				return im;
			return super.getValue(key);
		}

		public void actionPerformed(ActionEvent a) {
			if (AddressBookResources.IMG_NEW.equals(getValue(NAME))) {
				Rectangle r = ((Component) a.getSource()).getBounds();
				Point p = new Point(0, r.height);
				p = SwingUtilities.convertPoint((Component) a.getSource(), p, AddressBookFrame.this);
				((JPopupMenu) createNewMenu(new JPopupMenu(), AddressBookFrame.this)).show(AddressBookFrame.this, p.x,
						p.y);
			} else if (AddressBookResources.IMG_ACTION.equals(getValue(NAME))) {
				Rectangle r = ((Component) a.getSource()).getBounds();
				Point p = new Point(0, r.height);
				p = SwingUtilities.convertPoint((Component) a.getSource(), p, AddressBookFrame.this);
				((JPopupMenu) createActionMenu(new JPopupMenu(), AddressBookFrame.this)).show(AddressBookFrame.this,
						p.x, p.y);
			} else
				AddressBookFrame.this.actionPerformed(new ActionEvent(a.getSource(), a.getID(),
						(String) getValue(ACTION_COMMAND_KEY)));
		}
	}

	class RadioAction extends AbstractAction {
		RadioAction(String name) {
			super(name);
		}

		public void actionPerformed(ActionEvent a) {

			AddressBookFrame.this.actionPerformed(a);
		}
	}

	class JRadioButtonMenuItemEx extends JRadioButtonMenuItem {
		Action action;

		PropertyChangeListener actionPropertyChangeListener;

		JRadioButtonMenuItemEx(Action action) {
			setAction(action);
		}

		public Action getAction() {
			return action;
		}

		public void setAction(Action new_action) {
			Action prev_action = getAction();
			if (action == null || !action.equals(new_action)) {
				action = new_action;
				if (prev_action != null) {
					removeActionListener(prev_action);
					prev_action.removePropertyChangeListener(actionPropertyChangeListener);
					actionPropertyChangeListener = null;
				}
				configurePropertiesFromAction(action);
				if (action != null) {
					addActionListener(action);
					actionPropertyChangeListener = createActionPropertyChangeListener(action);
					action.addPropertyChangeListener(actionPropertyChangeListener);
				}
				firePropertyChange("action", prev_action, action);
				revalidate();
				repaint();
			}
		}

		protected void configurePropertiesFromAction(Action action) {
			setEnabled(action.isEnabled());
			setText((String) action.getValue(Action.NAME));
		}

		protected PropertyChangeListener createActionPropertyChangeListener(Action action) {
			return new PropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent evt) {
				}
			};
		}
	}

	static public void main(String[] args) {
		try {
			new AddressBookFrame(null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getPreferredServiceName() {
		return getName();
	}

	public Object getServiceProvider() {
		return this;
	}
}