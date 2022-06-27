/* jaddressbook - Contactview
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
 *  $Id: Contactview.java,v 1.30 2014/06/30 00:56:47 cvs Exp $
 * Created on Sep 19, 2005
 */
package addressbook.servlet;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.aldan3.util.TreeViewHelper;
import org.aldan3.util.inet.HttpUtils;

import addressbook.Account;
import addressbook.Address;
import addressbook.Chat;
import addressbook.Commentary;
import addressbook.Contact;
import addressbook.ContactConstant;
import addressbook.EMail;
import addressbook.Folder;
import addressbook.GenericAttribute;
import addressbook.Link;
import addressbook.Name;
import addressbook.Picture;
import addressbook.Telephone;
import addressbook.servlet.model.FolderOperations;
import addressbook.servlet.model.GenericOperations;
import static addressbook.Contact.*;

public class Contactview extends AddressBookProcessor {
	public static final String P_BEFORENAME = "beforename";

	public static final String R_DOB_FORMAT = "dobformat";

	public static final String FILENAME = "filename";

	static final String[] TABNAMES = { "mail", "phone", "notes", "links", "accnt", "address", "attach", "chat", "secret" };

	private static final String CONTACT = "CONTACT";

	@Override
	protected Map getModel() {
		Map result = new HashMap();
		// TODO: folder name supposes look like root/node1/node2...
		String folderName = getStringParameterValue(P_FOLDER, getStringParameterValue(P_NODE, "", 0), 0);
		result.put(P_FOLDER, folderName);
		result.put(TreeViewHelper.HV_STATE, getStringParameterValues(TreeViewHelper.HV_STATE));
		addTypeList(result, TPHONE + V_TYPE, ContactConstant.TELEPHONE_TYPE, this);
		addTypeList(result, V_TYPE, ContactConstant.EMAIL_TYPE, this);
		addTypeList(result, CHAT + V_TYPE, Chat.MNEMO, this);

		FolderOperations fo = getFolderOperations();
		Contact contact = getContactOperations().getContact(fo, folderName, NAME, P_HASH, this);
		if (contact == null)
			contact = (Contact)req.getAttribute(CONTACT);
		if (contact != null) {
			System.err.println("OOOOOOOO contact found"+contact.getPictures());
			result.put(V_CONTACT, contact);
			addEmails(result, contact);
			addTelephones(result, contact);
			addAddresses(result, contact);
			// can be omitted
			result.put(NAME, contact.getName());
			result.put(DOB, contact.getDOB());
			result.put(ADDRESS, contact.getAddresses());
			result.put(COMMENTARY, contact.getComments());
			result.put(CHAT, contact.getChats());
			result.put(BOOKMARK, contact.getLinks());
			result.put(PICTURE, contact.getPictures());
			result.put(ACCOUNT, contact.getAccounts());
			result.put(PGP, contact.getPGPs());
			result.put(V_READONLY, fo.canModifyContacts(folderName) == false
					&& getSession().getAttribute(HV_USER_ID).equals(contact.getOwner()) == false);
		} else {
			contact = new Contact(new Name("","","","","","",""));
		
			result.put(V_CONTACT, contact);
		}
		req.setAttribute(V_CIPHER, getCipherOperations()); // to init()
		addTabsMenu(result);
		result.put("modeview", "mobile/" + getStringParameterValue("tab", "mail", 0) + "tab.htm");
		return result;
	}

	@Override
	protected Map doControl() {
		// temporary disable to modify contacts from Shared, until not
		// modifiable attr for contact introduced
		Contact contact = getContact();
		if (contact == null)
			return null;
		updateSet(contact, contact.getEMails(), EMAIL, EMail.class, false);
		updateSet(contact, contact.getTelephones(), TPHONE, Telephone.class, false);
		updateSet(contact, contact.getAddresses(), ADDRESS, Address.class, false);
		updateSet(contact, contact.getComments(), COMMENTARY, Commentary.class, true);
		updateSet(contact, contact.getChats(), CHAT, Chat.class,
		// TDOD: prefill these lists in a host object, like
		// Chat.getContructorDef(), Chat.getParamDef()
				new String[] { Chat.HOST, Chat.TYPE, Chat.DESCRIPTION, Chat.ROOM }, new Class[] { String.class,
						int.class, String.class, String.class });
		// TODO do not update accounts if got page encrypted
		boolean encrypted = false;
		String hv = null;
		for (int i = 0; (hv = getStringParameterValue(ACCOUNT + "hash", null, i)) != null; i++) {
			if (hv.equals(Boolean.TRUE.toString())) {
				encrypted = true;
				break;
			}
		}

		if (encrypted == false) {
			updateSet(contact, contact.getAccounts(), ACCOUNT, Account.class, new String[] { Account.DESCRIPTION,
				Account.PASSWORD, Account.NAME, Account.LINK }, new Class[] { String.class, String.class,
				String.class, String.class }, !mobile); // was true
			// decrypt passwords
			List<Account> accounts = contact.getAccounts();
			if (accounts != null && !mobile) {
				Iterator<Account> ai = accounts.iterator();
				while (ai.hasNext()) {
					Account a = ai.next();
					String ep = getStringParameterValue("enc" + ACCOUNT + a.offset + Account.PASSWORD, null, 0); // because numbered
					if (ep == null) {
						log("Error: inconsistent form data, encrypted password not found for index "+a.offset+" for "+a, null);
						//break;
					}
					if (" ".equals(ep) == false)
						a.setPassword(getCipherOperations().decrypt(ep));
					//log("password "+ ep+" and "+( " ".equals(ep)?"":getCipherOperations().decrypt(ep)), null);
				}
			}
		}
		updateSet(contact, contact.getLinks(), BOOKMARK, Link.class, true);
		try {
			log("Used format:" + getResourceString(R_DOB_FORMAT, "MM/dd/yyyy"), null);
			contact.setDOB(new SimpleDateFormat(getResourceString(R_DOB_FORMAT, "MM/dd/yyyy"))
					.parse(getStringParameterValue(DOB, null, 0)));
		} catch (NullPointerException npe) {
			contact.setDOB(null);
		} catch (ParseException e) {
		}
		contact.setTitle(getStringParameterValue(TITLE, "", 0));
		// TODO replace with BaseFormProcessor.Attachment
		// TODO use Ajax for adding/deleting pictures
		List<Picture> pictures = contact.getPictures();
		List<Picture> deletedPictures = new ArrayList<Picture>();
		for (int i = 0;; i++) {
			int di = getIntParameterValue("delete" + PICTURE, -1, i);
			if (di < 0)
				break;
			if (di >= 0 && di < pictures.size())
				deletedPictures.add(pictures.get(di));
		}
		for (Picture picture : deletedPictures)
			getAttachmentOperations().detachAttachment(contact, picture);
		addAttachment(contact);
		// if a tab is specified then 
		String tab = getStringParameterValue("tab", "", 0);
		if (!tab.isEmpty()) {
			req.setAttribute(CONTACT, contact);
			return getModel();
		}
		return null;
	}
	
	protected Contact getContact() {
		String folderName = getStringParameterValue(P_FOLDER, "", 0);
		FolderOperations fo = getFolderOperations();
		/*
		 * try { log("Name from page:'"+getStringParameterValue(NAME, "",
		 * 0)+"', and after name:"+new Name(getStringParameterValue(NAME, "",
		 * 0)), null); } catch (ParseException e1) { e1.printStackTrace(); }
		 */
		Contact contact = getContactOperations().getContact(fo, folderName, P_BEFORENAME, P_HASH, this);
		// validate fields as e-mails, phones, dob, urls
		if (contact == null) {
			contact = new Contact(new Date());

			if (fo.canModifyContacts(folderName) == false)
				contact.setOwner((String) getSession().getAttribute(HV_USER_ID));
			List<Folder> folders = fo.search(GenericOperations.escapeMask(folderName), null);
			log("A new contact " + contact + " will be added in first folder of " + folders + " found by name "
					+ folderName, null);
			if (folders.size() > 0)
				fo.addContact(contact, folders.get(0));
			else
				log("No folders to insert " + contact + " found, requested target folder was:" + folderName, null);
		} else {
			if (fo.canModifyContacts(folderName) == false
					&& getSession().getAttribute(HV_USER_ID).equals(contact.getOwner()) == false)
				return null; // error should be
		}
		try { 
			contact.setValue(new Name(getStringParameterValue(NAME, "", 0)));
		} catch (ParseException e) {
			log("", e);
		}
		return contact;
	}

	/**
	 * Ajax call for returning password
	 * 
	 * @return password based on uniqueness account and and URL If neither of
	 *         them can be provided or ambigous, then wrong password can be
	 *         retrieved
	 */
	public String processgetPasswordCall() {
		log("producegetPasswordData", null);
		// TODO consider some hash code which can identify record uniquely,
		// finding all matching records and raise a problem can be good too
		Contact contact = getContactOperations().getContact(getFolderOperations(),
				getStringParameterValue(P_FOLDER, getStringParameterValue(P_NODE, "", 0), 0), NAME, P_HASH, this);
		if (contact == null)
			return "no contact";
		String s = getStringParameterValue(ACCOUNT, "", 0);
		String u = getStringParameterValue(ACCOUNT + Account.LINK, "", 0);
		if (s.length() == 0 || u.length() == 0)
			return "account or link empty";
		for (Account account : contact.getAccounts()) {
			if (s.equals(account.getValue()) && u.equals(account.getLink()))
				return account.getPassword() != null ? HttpUtils.htmlEncode(account.getPassword()) : "empty";
		}
		return "not found";
	}

	public Map processFileUploadCall() {
		boolean uploadRequested = getIntParameterValue("upload", -1, 0) == 1;
		Map<String, Object> result = new HashMap<String, Object>(10);
		fillWithForm(result, NAME, P_FOLDER, P_HASH);
		if (uploadRequested == false) {
			result.put("result", "false");
			return result;
		}
		result.put("result", "true");
		Contact contact = getContactOperations().getContact(getFolderOperations(),
				getStringParameterValue(P_FOLDER, getStringParameterValue(P_NODE, "", 0), 0), NAME, P_HASH, this);
		if (contact == null) {
			result.put(V_INDEX, -1);
			return fillMessage(result, GenericAttribute.DESCRIPTION, "error_no_contact", null);
		}
		Picture pic = addAttachment(contact);
		if (pic == null) {
			result.put(V_INDEX, -1);
			return fillMessage(result, GenericAttribute.DESCRIPTION, "error_no_attachment", null);
		}
		// TODO consider just putting a picture with field processing in a
		// template
		result.put(GenericAttribute.VALUE, pic.getValue());
		result.put(V_INDEX, contact.getPictures().size() - 1);
		result.put(GenericAttribute.DESCRIPTION, pic.getDescription());
		return result;
	}

	public String getFileUploadViewName() {
		return "attupload.htm";
	}

	public String processupdateAccntCall() {
		try {
			Contact contact = getContactOperations().getContact(getFolderOperations(),
					getStringParameterValue(P_FOLDER, getStringParameterValue(P_NODE, "", 0), 0), NAME, P_HASH, this);
			if (contact == null)
				throw new Exception("Contact can't be found");
			updateContact(contact, contact.getAccounts(), ACCOUNT, Account.class, new String[] { Account.DESCRIPTION,
					Account.PASSWORD, Account.NAME, Account.LINK }, new Class[] { String.class, String.class,
					String.class, String.class });
			return "Ok";
		} catch (Exception e) {
			return "Error " + e;
		}
	}
	
	public HashMap processgetAccntInfoCall() {
		Contact contact = getContactOperations().getContact(getFolderOperations(),
				getStringParameterValue(P_FOLDER, getStringParameterValue(P_NODE, "", 0), 0), NAME, P_HASH, this);
		HashMap<String, Object> res = new HashMap<String, Object>();
		if (contact == null) {
			res.put("model", new Account("error", "Contact can't be found"));
			return res;
		}
		String s = getStringParameterValue(ACCOUNT, "", 0);
		String u = getStringParameterValue(ACCOUNT + Account.LINK, "", 0);
		if (s.length() == 0 || u.length() == 0) {
			res.put("model", new Account("error", "account or link empty"));
		} else
			for (Account account : contact.getAccounts()) {
				if (s.equals(account.getValue()) && u.equals(account.getLink())) {
					res.put("model", account);
					break;
				}
			}
		return res;
	}
	
	public String processcreateContactCall() {
		Contact contact = getContact();
		if (contact == null)
			return "";
		return String.valueOf(contact.hashCode());
	}
	
	public String getgetAccntInfoViewName() {
		return "accnt.json.htm";
	}

	@Override
	protected String getSubmitPage() {
		try {
			return treeStateEncode(
					"Abfrontview?nodeid=" + URLEncoder.encode(getStringParameterValue(P_FOLDER, "", 0), "UTF-8"),
					TreeViewHelper.HV_STATE, "");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("" + e);
		}
	}

	protected static void addTypeList(Map pageMap, String name, String[] values, AddressBookProcessor abp) {
		Map<String, String>[] types = new Map[values.length];
		for (int i = 0; i < types.length; i++) {
			types[i] = new HashMap<String, String>(1);
			types[i].put(V_TYPE, values[i]);
			types[i].put(HV_LABEL, abp.getResourceString(values[i], values[i]));
			types[i].put(V_INDEX, ""+i);
		}
		pageMap.put(name, types);
	}

	protected void addTabsMenu(Map pageMap) {
		if (mobile == false)
			return;
		pageMap.put("tab", TABNAMES);
	}

	protected Picture addAttachment(Contact contact) {
		Picture result = null;
		byte[] picBytes = null;
		int i = 0;
		String file = null;
		while ((picBytes = (byte[]) getObjectParameterValue(PICTURE, null, i++, false)) != null) {
			contact.add(result = getAttachmentOperations().connectAttachment(
					getContactOperations().createPicture(contact), picBytes,
					file = getStringParameterValue(PICTURE + '+' + FILENAME, null, 0),
							frontController.getServletContext().getMimeType(file)));
			// log("Picture:"+getStringParameterValue(PICTURE+'+'+FILENAME,
			// null, 0), null);
		}
		return result;
	}

	protected void addEmails(Map pageMap, Contact contact) {
		pageMap.put(Contact.EMAIL, contact.getEMails());
	}

	protected void addTelephones(Map pageMap, Contact contact) {
		pageMap.put(Contact.TPHONE, contact.getTelephones());
	}

	protected void addAddresses(Map pageMap, Contact contact) {
		// TODO: address types
		pageMap.put(Contact.ADDRESS, contact.getAddresses());
	}

	protected void updateSet(Contact contact, List list, String setName, Class kind, boolean noType) {
		if (noType)
			updateSet(contact, list, setName, kind, new String[] { GenericAttribute.DESCRIPTION },
					new Class[] { String.class });
		else
			updateSet(contact, list, setName, kind,
					new String[] { GenericAttribute.DESCRIPTION, GenericAttribute.TYPE }, new Class[] { String.class,
							String.class });
	}

	Constructor getConstractorFor(Class kind, String[] paramNames, Class[] paramTypes) {
		Constructor c = null;
		Class[] paramClasses = new Class[paramTypes.length + 1];
		paramClasses[0] = String.class; // reserved for VALUE
		System.arraycopy(paramTypes, 0, paramClasses, 1, paramTypes.length);
		try {
			c = kind.getConstructor(paramClasses);
		} catch (SecurityException e) {
			log("", e);
		} catch (NoSuchMethodException e) {
			log("", e);
		}
		return c;
	}

	Object[] fillParams(int offset, String setName, String[] paramNames, Class[] paramTypes, Object[] placeholders) {
		return fillParams(offset, setName, paramNames, paramTypes, placeholders, false);
	}
	
	Object[] fillParams(int offset, String setName, String[] paramNames, Class[] paramTypes, Object[] placeholders, boolean useOffset) {
		if (placeholders == null)
			placeholders = new Object[paramNames.length + 1];
		if (useOffset) {
			setName += String.valueOf(offset);
			offset = 0;
		}
		String ea = getStringParameterValue(setName + GenericAttribute.VALUE, null, offset);
		if (ea == null)
			return null;
		placeholders[0] = ea;
		for (int k = 0; k < paramNames.length; k++) {
			if (paramTypes[k] == Integer.class || paramTypes[k] == int.class)
				placeholders[k + 1] = getIntParameterValue(setName + paramNames[k], -1, offset);
			else
				placeholders[k + 1] = getStringParameterValue(setName + paramNames[k], null, offset);
		}
		return placeholders;
	}

	protected <T extends GenericAttribute> void updateContact(Contact contact, List<T> list, String setName,
			Class<T> kind, String[] paramNames, Class[] paramTypes) throws Exception {
		Constructor c = getConstractorFor(kind, paramNames, paramTypes);
		if (c == null)
			throw new Exception("Incosistent constructor metadata");
		Object[] params = new Object[paramNames.length + 1];
		params = fillParams(0, setName, paramNames, paramTypes, params);
		if (params == null)
			throw new Exception("No data found");
		GenericAttribute ga = (GenericAttribute) c.newInstance(params);
		if (list != null)
			for (int i = 0; i < list.size(); i++) {
				GenericAttribute cga = list.get(i);
				if (cga.getValue().equals(ga.getValue())) {
					list.set(i, (T) ga);
					return;
				}
			}
		contact.add((T) ga);
	}

	protected void updateSet(Contact contact, List list, String setName, Class kind, String[] paramNames,
			Class[] paramTypes) {
		updateSet(contact,list, setName, kind, paramNames, paramTypes, false);
	}
	
	protected void updateSet(Contact contact, List list, String setName, Class kind, String[] paramNames,
			Class[] paramTypes, boolean numbered) {
		// TODO check field size, because can be bug and too much
		Constructor c = getConstractorFor(kind, paramNames, paramTypes);
		if (c == null)
			return;
		int th = 0;
		if (list != null) {
			th = list.size();
			if (mobile) {
				
			} else
			    list.clear();
		}
		int di = getIntParameterValue(setName + "default", -1, 0);
		int i = 0;
		Object[] params = new Object[paramNames.length + 1];
		do {
			params = fillParams(i, setName, paramNames, paramTypes, params, numbered);
			if (params == null)
				if (i > th)
					break;
				else {
					i++;
					continue;
				}
			try {
				GenericAttribute ga = (GenericAttribute) c.newInstance(params);
				ga.offset = i;
				if (list != null && mobile) {
// look in contacts if update needed
					for(int il=0, n=list.size(); il< n; il++) {
						GenericAttribute ga1 = (GenericAttribute)list.get(il);
						if (ga1.equalsToUpdate(ga)) {
							if (kind.equals(Account.class)) {
								if ("********".equals(((Account)ga).getPassword()))
									((Account)ga).setPassword(((Account)ga1).getPassword());
							}
							list.remove(il);
							break;
						}
					}
				} 
				contact.add(ga );
				if (di == i)
					ga.setPreferable(true);
			} catch (IllegalArgumentException e) {
				log("", e);
			} catch (InstantiationException e) {
				log("", e);
			} catch (IllegalAccessException e) {
				log("", e);
			} catch (InvocationTargetException e) {
				log("", e);
			}
			i++;
		} while (true);
	}
	/*
	 * public static class Tab { public String name; public String label; public
	 * String node; }
	 */
}
