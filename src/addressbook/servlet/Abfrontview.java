/* jaddressbook - Abfrontview.java
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
 *  $Id: Abfrontview.java,v 1.23 2012/09/15 17:49:55 dmitriy Exp $
 * Created on Sep 16, 2005
 */

package addressbook.servlet;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.aldan3.util.TreeViewHelper;

import addressbook.AddressException;
import addressbook.COI;
import addressbook.Folder;
import addressbook.GenericAttribute;
import addressbook.Name;
import addressbook.XMLSaver;
import addressbook.servlet.model.AbstractAttributeStorage;
import addressbook.servlet.model.FolderOperations;
import addressbook.servlet.model.FolderTravel;
import addressbook.servlet.model.GenericOperations;
import addressbook.util.Alphabet;
import addressbook.Contact;
import static addressbook.Contact.*;
import addressbook.AddressBookResources;
import static addressbook.AddressBookResources.*;

public class Abfrontview extends AddressBookProcessor {
	public static final String V_ALPHABET = "alphabet";

	public static final String P_CHECKED = "checked";

	public static final String P_NEWNODE = "newnode";
	
	public static final String  V_OPER = "oper";
	
	public static final String  V_PATH = "path";

	@Override
	protected Map getModel() {
		Map result = new HashMap();
		FolderOperations fo = getFolderOperations();
		if (mobile == false) { // add tree
			// TODO cache it in context
			new TreeViewHelper(P_FOLDER).apply(result, new FolderTravel(fo, getResourceName()), getTreeState(), null, null);
		}
		// ContactOperations co = getContactOperations();
		String selNode = getStringParameterValue(P_NODE, getDefaultFolder(), 0);
		result.put(P_NODE, selNode);
		addAlphabet(result, V_ALPHABET, null);
		if (getLocale() != null && getLocale().getLanguage().length() > 0 && "en".equals(getLocale().getLanguage()) == false) 
			addAlphabet(result, V_ALPHABET + "_n", getLocale());
		searchResult(result, selNode, fo, false);
		// TODO disable/enable some buttons, for example no Share for Shared,
		return result;
	}

	public Map processSearchCall() {
		return searchResult(null, getStringParameterValue(P_NODE, getDefaultFolder(), 0), getFolderOperations(), false);
	}

	public String getSearchViewName() {
		return "contacts.htm";
	}

	public Map processTreeChangeCall() {
		FolderOperations fo = getFolderOperations();
		String currentFolderName = getStringParameterValue(P_NODE, null, 0);
		Folder currentFolder = getRootFolder(currentFolderName, fo.search(GenericOperations.escapeMask(currentFolderName), null));
		String oper = getStringParameterValue(P_MODE, "", 0);
		String markedFolder = null;
		if (AddressBookResources.CMD_ADD.equals(oper)) {
			// TODO consider get type from user
			String newNode = getStringParameterValue(P_NEWNODE, null, 0);
			
			if (newNode == null || newNode.length() == 0 || fo.addFolder(currentFolder, newNode, Folder.ANY) == null)
				System.err.printf("Can't add folder %s to folder %s%n", newNode,
						currentFolder);
			else
				markedFolder = currentFolder == null ? null : currentFolder.toString();
		} else {
			if (currentFolder != null) {
				if (AddressBookResources.CMD_DELETE.equals(oper)) {
					if (fo.isShared(currentFolder) == false) {
						fo.deleteFolder(null, currentFolder);
						if (currentFolder.toString().startsWith(LABEL_TRASH + Folder.levelSeparator) == false)
							fo.addFolder(getRootFolder(LABEL_TRASH, fo.search(LABEL_TRASH, null)), currentFolder);
					}
				} else if (AddressBookResources.CMD_MODIFY.equals(oper)) {
					if (fo.isShared(currentFolder)==false && fo.canModifyContacts(currentFolder)) {
						String newName = getStringParameterValue(P_NEWNODE, "", 0).replace(Folder.levelSeparator, ' ');
						if (newName.length() > 0)
							currentFolder.rename(newName);
						markedFolder = currentFolder.toString();
					}
				} else if (AddressBookResources.CMD_COPY.equals(oper)) {
					String targetFoldername = getStringParameterValue(P_NEWNODE, null, 0);
					Folder targetFolder = targetFoldername == null ? null : getRootFolder(targetFoldername, fo.search(
							GenericOperations.escapeMask(targetFoldername), null));
					fo.deleteFolder(null, currentFolder); // Or create a clone
					fo.addFolder(targetFolder, currentFolder);
				}
			} else
				log("Folder:" + currentFolderName + " not found.", null);
		}
		Map result = new HashMap();
		TreeViewHelper tvh = new TreeViewHelper(P_FOLDER);
		tvh.apply(result, new FolderTravel(fo, getResourceName()), getTreeState(), markedFolder, null, null);
		return result;
	}

	public String getTreeChangeViewName() {
		return (getStringParameterValue(P_MODE, "", 0).equals(AddressBookResources.CMD_SELECT) ? "mv" : "")
				+ "tree.htm";
	}

	public String processCheckTriggerCall() {
		String folderName = getStringParameterValue(P_FOLDER, getStringParameterValue(P_NODE, "", 0), 0);
		AbstractAttributeStorage contact = getRecordOperations().getContact(getFolderOperations(), folderName, NAME,
				P_HASH, this);
		// System.err.printf("Triggering %s to %s%n", contact, getStringParameterValue(P_CHECKED, null, 0));
		if (contact != null) {
			contact.setChecked(getStringParameterValue(P_CHECKED, String.valueOf(Boolean.FALSE), 0).equals(
					Boolean.TRUE.toString()));
			return "Ok";
		}
		return "Error";
	}
	
	public String processDeleteCall() {
		if (doControl() == null)
			return "Ok";
		return "error";
	}
	
	public String processmergeCall() {
		switch(getIntParameterValue("importtype", 0, 0)) {
		case 1:
			System.err.println("!1 selected");
			break;
		}
		
		try {
			getFolderOperations().merge(new ByteArrayInputStream(((String) getObjectParameterValue("importdata", "<?xml version=\"1.0\" encoding=\"utf-8\"?><DATABOOK/>", 0, false)).getBytes("utf-8")));
		} catch (AddressException e) {
			return "error"+':'+e;
		} catch (UnsupportedEncodingException e) {
			return "error"+":"+e;
		}
		return "Ok";
	}

	protected String getDefaultFolder() {
		return AddressBookResources.LABEL_PERSONS;
	}

	protected Map searchResult(Map result, String node, FolderOperations fo, boolean checked) {
		List<AbstractAttributeStorage> l = getRecordOperations().search(getStringParameterValue(P_SEARCH, null, 0),
				node, fo);
		log("Search entries:" + l.size(), null);
		List lr = new ArrayList();
		for (AbstractAttributeStorage storage : l) {
			if (storage instanceof Contact) {
				Contact contact = (Contact) storage;
				if (checked && contact.isChecked() == false)
					continue;
				Map m = new HashMap();
				m.put(V_CONTACT, contact);
				m.put(V_OPER, "editContact");
				m.put(NAME, contact.getName());
				if (contact.getEMails() != null && contact.getEMails().size() > 0)
					m.put(EMAIL, contact.getDefault(contact.getEMails()));
				if (contact.getTelephones() != null && contact.getTelephones().size() > 0)
					m.put(TPHONE, contact.getDefault(contact.getTelephones()));
				if (contact.getChats() != null && contact.getChats().size() > 0)
					m.put(CHAT, contact.getDefault(contact.getChats()));
				lr.add(m);
			} else if (storage instanceof AbstractAttributeStorage) {
				Map<String, Object> m = new HashMap<String, Object>();
				m.put(V_CONTACT, storage);
				m.put(NAME, new Idable(((AbstractAttributeStorage)storage).getStringAttribute(GenericAttribute.VALUE)));
				m.put(V_OPER, "editCollection");
				lr.add(m);
			} /*else if (storage instanceof Folder) {
				Map<String, Object> m = new HashMap<String, Object>();
				m.put(V_CONTACT, storage);
				m.put(NAME, ((Folder)storage).getShortName());
				m.put(V_OPER, "browseFolder");
				m.put(V_PATH, storage);
				lr.add(m);
			}*/
		}
		if (result == null)
			result = new HashMap();
		result.put(V_ENTRIES, lr); // TODO const
		return result;
	}

	@Override
	protected Map doControl() {
		String destFolder = getStringParameterValue(P_GET_THEM, null, 0);
		String sourceFolder = getStringParameterValue(P_FOLDER, null, 0);
		FolderOperations fo = getFolderOperations();
		boolean delete = getStringParameterValue(P_DELETE, null, 0) != null;
		boolean share = getStringParameterValue(P_SHARE, null, 0) != null;
		boolean sourceShared = fo.isShared(sourceFolder);
		Folder workFolder = null;
		if (delete)
			workFolder = getRootFolder(LABEL_TRASH, fo.search(LABEL_TRASH, null));
		else if (share)
			workFolder = getRootFolder(LABEL_SHARED, fo.search(LABEL_SHARED, null));
		else if (destFolder != null)
			workFolder = getRootFolder(destFolder, fo.search(GenericOperations.escapeMask(destFolder), null));
		// System.err.println("Shared="+share+" folder:"+workFolder);
		// list of selection generally less than entire folder size so it's
		// preferrable to
		// traverse it first
		// note that for indexed repository direct search against selected can
		// be smarter
		Folder<XMLSaver> currentFolder = getRootFolder(sourceFolder, fo.search(GenericOperations.escapeMask(sourceFolder), null));
		if (currentFolder.equals(workFolder))
			workFolder = null; // avoid reinsert
		// TODO for trash folder assign workFolder as Persons
		List<XMLSaver> l = currentFolder.getContent();
		String marked = getStringParameterValue(V_CONTACT, null, 0);
		List<Object[]> markings = new ArrayList<Object[]>();
		for (int i = 1; marked != null; i++) {
			int up = marked.indexOf('_');
			if (up > 0)
				markings.add(new Object[] { new Integer(marked.substring(0, up)), marked.substring(up + 1) });
			marked = getStringParameterValue(V_CONTACT, null, i);
		}
		if (markings.size() > 0) {
			Iterator<XMLSaver> i = l.iterator();
			synchronized (l) { // prevent concurrent update of the same folder
				while (i.hasNext()) {
					XMLSaver element = i.next();
					if (element instanceof Contact) {
						Contact contact = (Contact) element;
						for (Object[] e : markings) {
							Name name = contact.getName();
							if (name.hashCode() == (Integer) e[0] && name.getLast().equals(e[1])) {
								boolean canDelete = sourceShared == false
										|| contact.getOwner().equals(getSession().getAttribute(HV_USER_ID));
								if (workFolder != null)
									if (fo.canModifyContacts(workFolder))
										workFolder.add(contact);
									else {
										contact.setOwner((String) getSession().getAttribute(HV_USER_ID));
										workFolder.add(contact = (Contact) fo.cloneContact(contact));
									}
								contact.setChecked(false);
								//System.err.println("Adding in " + workFolder + " contact " + contact + " share "
								//		+ share + " delete " + delete + " owner:" + contact.getOwner());
								if (canDelete && delete)
									i.remove();
								break;
							}
						}
					} else if (element instanceof COI) {
						COI coi = (COI) element;
						if (coi.isChecked()) {
							boolean canDelete = sourceShared == false || coi.getOwner() == null
									|| coi.getOwner().equals(getSession().getAttribute(HV_USER_ID));
							if (workFolder != null && workFolder.getType() != Folder.PERSON) {
								if (fo.canModifyContacts(workFolder))
									workFolder.add(coi);
								else {
									coi.setOwner((String) getSession().getAttribute(HV_USER_ID));
									workFolder.add((COI) fo.cloneContact(coi));
								}
								//System.err.println("Adding in " + workFolder + " collection " + coi + " share " + share
								//		+ " delete " + delete);
							}
							if (canDelete && delete)
								i.remove();
							// System.err.printf("No operation del: %b shar: %b happened on %s %d%n", delete, share, )
							coi.setChecked(false);
						}
					} else
						System.err.printf("Unsupported type of element %s to apply delete %b%n", element, delete);
				}
			}
		}
		return null;
	}

	@Override
	protected String getSubmitPage() {
		try {
			return treeStateEncode("Abfrontview?nodeid="
					+ URLEncoder.encode(getStringParameterValue(P_FOLDER, null, 0), "UTF-8"), TreeViewHelper.HV_STATE,
					"");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("" + e);
		}
	}

	protected Folder getRootFolder(String folderName, List<Folder> folders) {
		if (folders == null)
			return null;
		for (Folder folder : folders) {
			if (folder.toString().equals(folderName))
				return folder;
		}
		return null;
	}

	protected void addAlphabet(Map result, String name, Locale locale) {
		Alphabet a = locale == null ? Alphabet.ENGLISH : new Alphabet(locale);
		List<Map> ml = new ArrayList<Map>(a.size());
		for (char c = a.getFirst(); c != 0; c = a.getAfter(c)) {
			Map m = new HashMap(2);
			m.put("uc", "" + c);
			m.put("lc", "" + a.toLowerCase(c));
			m.put("ucx", String.format("\\\\u%04x", (int)c));
			m.put("lcx", String.format("\\\\u%04x", (int)a.toLowerCase(c)));
			ml.add(m);
		}
		result.put(name, ml);
	}

	protected static class Idable {
		String name;

		Idable(String n) {
			name = n;
		}

		public Object getId() {
			return name;
		}

		@Override
		public String toString() {
			return (String) getId();
		}

		@Override
		public int hashCode() {
			final int PRIME = 31;
			int result = 1;
			result = ((name == null) ? PRIME * result + 0 : name.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			final Idable other = (Idable) obj;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			return true;
		}
	}
}