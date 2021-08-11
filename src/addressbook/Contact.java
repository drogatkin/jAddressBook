/* AddressBook - Contact
 * Copyright (C) 1999-2006 Dmitriy Rogatkin.  All rights reserved.
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
 *  $Id: Contact.java,v 1.15 2008/03/02 03:10:10 dmitriy Exp $
 */
package addressbook;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.aldan3.util.inet.HttpUtils;

import addressbook.servlet.model.AbstractAttributeStorage;

public class Contact extends AbstractAttributeStorage implements XMLSaver {
	public static final String NAME = "name";

	public static final String OTHERNAME = "othername";

	public static final String EMAIL = "email";

	public static final String TPHONE = "tphone";

	public static final String CHAT = "chatid";

	public static final String BOOKMARK = "link";

	public static final String ACCOUNT = "account";

	public static final String ADDRESS = "address";

	public static final String DOB = "dob";

	public static final String TITLE = "title";

	public static final String PICTURE = "picture";

	public static final String COMMENTARY = "commentary";

	public static final String PGP = "pgp";

	public static final String RESUME = "resume";

	public static final String INTEREST = "interest";

	Name name;
	
	// other names this person can be known (actually it may include maiden names, pseudonims and so on)
	List<Name> otherNames;

	String title; // TODO convert to list, because multiple titles possible
	
	String language; // TODO a contact can speak more than one lang, consider that

	Date dob;

	List addresses, emails, links, chats, pgps;

	List<Commentary> comments;

	List<Telephone> telephones;

	List<Picture> pictures;

	List<Account> accounts;
	
	List<Resume> resumes;
	
	boolean self; // self contact

	private Date createdOn;

	// Classificator classificator;

	public Contact(Date date) {
		createdOn = date;
	}

	// note: use lazy instantiation
	public Contact(Name _name) {
		name = _name;
		createdOn = new Date();
	}

	public void setValue(Object _value) {
		if (_value instanceof Name)
			name = (Name) _value;
	}

	public void setDOB(Date _date) {
		dob = _date;
	}

	public Date getCreatedOn() {
		return createdOn;
	}

	public Date getDOB() {
		return dob;
	}

	public void setTitle(String _title) {
		title = _title;
	}

	public String getTitle() {
		return title;
	}

	public Name getName() {
		return name;
	}

	public void add(Address _address) {
		if (addresses == null)
			addresses = new ArrayList();
		if (_address.isEmpty() == false)
			addresses.add(_address);
	}

	public Address remove(Address _address) {
		if (addresses != null && addresses.remove(_address))
			return _address;
		return null;
	}

	public List getAddresses() {
		return addresses;
	}

	public void add(Telephone _telephone) {
		if (telephones == null)
			telephones = new ArrayList();
		if (_telephone.isEmpty() == false)
			telephones.add(_telephone);
	}

	public Telephone remove(Telephone _telephone) {
		if (telephones != null && telephones.remove(_telephone))
			return _telephone;
		return null;
	}

	public List getTelephones() {
		return telephones;
	}

	public void add(EMail _email) {
		if (emails == null)
			emails = new ArrayList();
		if (_email.isEmpty() == false)
			emails.add(_email);
	}

	public EMail remove(EMail _email) {
		if (emails != null && emails.remove(_email))
			return _email;
		return null;
	}

	public List getEMails() {
		return emails;
	}

	public void add(Link _link) {
		if (links == null)
			links = new ArrayList();
		if (_link.isEmpty() == false)
			links.add(_link);
	}

	public Link remove(Link _link) {
		if (links != null && links.remove(_link))
			return _link;
		return null;
	}

	public List getLinks() {
		return links;
	}

	public void add(Chat _chat) {
		if (chats == null)
			chats = new ArrayList();
		if (_chat.isEmpty() == false)
			chats.add(_chat);
	}

	public Chat remove(Chat _chat) {
		if (chats != null && chats.remove(_chat))
			return _chat;
		return null;
	}

	public List getChats() {
		return chats;
	}

	public void add(PGP _pgp) {
		if (pgps == null)
			pgps = new ArrayList();
		if (_pgp.isEmpty() == false)
			pgps.add(_pgp);
	}

	public PGP remove(PGP _pgp) {
		if (pgps != null && pgps.remove(_pgp))
			return _pgp;
		return null;
	}

	public List getPGPs() {
		return pgps;
	}

	public synchronized void addComment(String _comment) {
		if (comments == null)
			comments = new Vector<Commentary>();
		if (_comment != null && _comment.trim().length() > 0)
			comments.add(new Commentary(_comment, "" + comments.size()));
	}

	public synchronized void add(Commentary _comment) { // ?? synchronized
		if (comments == null)
			comments = new Vector<Commentary>();
		if (_comment != null)
			comments.add(_comment);
	}

	public Commentary removeComment(String _comment) {
		if (comments != null) {
			Iterator<Commentary> i = comments.iterator();
			while (i.hasNext()) {
				Commentary result = i.next();
				if (_comment.equals(result.getValue()) || _comment.equals(result.getDescription())) {
					i.remove();
					return result;
				}
			}
		}
		return null;
	}

	public List<Commentary> getComments() {
		return comments;
	}

	public List<Picture> getPictures() {
		return pictures;
	}

	public void add(Picture picture) {
		if (pictures == null)
			pictures = new ArrayList<Picture>();
		if (picture != null && picture.isEmpty() == false)
			pictures.add(picture);
	}

	public Picture remove(Picture pic) {
		if (pictures != null && pictures.remove(pic))
			return pic;
		return null;
	}

	public List<Account> getAccounts() {
		return accounts;
	}

	public void add(Account account) {
		if (accounts == null)
			accounts = new ArrayList<Account>();
		if (account != null && account.isEmpty() == false)
			accounts.add(account);
	}

	public Account remove(Account a) {
		if (accounts != null && accounts.remove(a))
			return a;
		return null;
	}

	public void add(Object o) {
		if (o instanceof Address)
			add((Address) o);
		else if (o instanceof Telephone)
			add((Telephone) o);
		else if (o instanceof EMail)
			add((EMail) o);
		else if (o instanceof Link)
			add((Link) o);
		else if (o instanceof Chat)
			add((Chat) o);
		else if (o instanceof PGP)
			add((PGP) o);
		else if (o instanceof String)
			addComment((String) o);
		else if (o instanceof Commentary)
			add((Commentary) o);
		else if (o instanceof Picture)
			add((Picture) o);
		else if (o instanceof Account)
			add((Account) o);
		else if (o instanceof List) {
			List v = (List) o;
			for (int i = 0; i < v.size(); i++)
				add(v.get(i));
		}
	}
	
	public Contact merge(Contact contact) {
		if (contact.accounts != null && contact.accounts.isEmpty() == false) {
			for (Account account:contact.accounts) {
				//System.err.printf("Merge %s%n", account);
				int pos = contact.accounts.indexOf(account);
				if (pos < 0)
					add(account);
				else {
					contact.accounts.get(pos).merge(account);
				}
			}
		}
		return this;
	}

	public String toString() {
		return "contact " + this.hashCode()+" ("+name+")";
	}

	public GenericAttribute getDefault(List<GenericAttribute> attrs) {
		for (GenericAttribute attr : attrs) {
			if (attr.isPreferable())
				return attr;
		}
		if (attrs.size() > 0)
			return attrs.get(0);
		return null;
	}

	protected void saveXML(List _l, OutputStream _out, String _enc) throws IOException,
			UnsupportedEncodingException {
		if (_l == null)
			return;
		for (int i = 0; i < _l.size(); i++)
			((GenericAttribute) _l.get(i)).saveXML(_out, _enc, i);

	}

	protected void saveVCard(List _l, OutputStream _out, String _enc) throws IOException,
			UnsupportedEncodingException {
		if (_l == null)
			return;
		for (int i = 0; i < _l.size(); i++)
			((GenericAttribute) _l.get(i)).saveVCard(_out, _enc, i);

	}

	final static SimpleDateFormat DOB_XML_FMT = new SimpleDateFormat(
			"'YEAR=\"'yyyy'\" MONTH=\"'MM'\" DAY=\"'dd'\"'");

	final static SimpleDateFormat DOB_ISO8601_FMT = new SimpleDateFormat("yyyy-MM-dd");

	final static SimpleDateFormat REV_ISO8601_FMT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

	public void saveXML(OutputStream _out, String _enc, int _order) throws IOException,
			UnsupportedEncodingException {
		_out.write("<PERSON CREATEDON=\"".getBytes(_enc));
		_out.write(REV_ISO8601_FMT.format(createdOn).getBytes(_enc));
		if (getOwner() != null)
			_out.write(("\" SHAREDBY=\"" + HttpUtils.htmlEncode(getOwner())).getBytes(_enc));
		_out.write("\">".getBytes(_enc));
		name.saveXML(_out, _enc, 0);
		if (dob != null) {
			_out.write("<DOB ".getBytes(_enc));
			_out.write(DOB_XML_FMT.format(dob).getBytes(_enc));
			_out.write("/>".getBytes(_enc));
		}
		if (title != null) {
			_out.write("<TITLE>".getBytes(_enc));
			_out.write(HttpUtils.htmlEncode(title).getBytes(_enc));
			_out.write("</TITLE>".getBytes(_enc));
		}
		saveXML(addresses, _out, _enc);
		saveXML(emails, _out, _enc);
		saveXML(links, _out, _enc);
		saveXML(chats, _out, _enc);
		saveXML(pgps, _out, _enc);
		saveXML(telephones, _out, _enc);
		saveXML(comments, _out, _enc);
		saveXML(pictures, _out, _enc);
		saveXML(accounts, _out, _enc);
		_out.write("</PERSON>".getBytes(_enc));
	}

	public void saveVCard(OutputStream _out, String _enc, int order) throws IOException {
		_out.write("BEGIN:VCARD".getBytes(_enc));
		_out.write(GenericAttribute.CRLF.getBytes(_enc));
		_out.write("VERSION:2.1".getBytes(_enc));
		_out.write(GenericAttribute.CRLF.getBytes(_enc));
		name.saveVCard(_out, _enc, 0);
		if (dob != null) {
			_out.write("BDAY:".getBytes(_enc));
			_out.write(DOB_ISO8601_FMT.format(dob).getBytes(_enc));
			_out.write(GenericAttribute.CRLF.getBytes(_enc));
		}
		saveVCard(addresses, _out, _enc);
		saveVCard(emails, _out, _enc);
		saveVCard(links, _out, _enc);
		saveVCard(chats, _out, _enc);
		saveVCard(pgps, _out, _enc);
		saveVCard(telephones, _out, _enc);
		saveVCard(comments, _out, _enc);
		saveVCard(pictures, _out, _enc);
		_out.write("REV:".getBytes(_enc));
		_out.write(REV_ISO8601_FMT.format(new Date()).getBytes(_enc));
		_out.write(GenericAttribute.CRLF.getBytes(_enc));
		_out.write("END:VCARD".getBytes(_enc));
		_out.write(GenericAttribute.CRLF.getBytes(_enc));
	}

	public String getStringAttribute(String attrName) {
		// TODO Auto-generated method stub
		return null;
	}

	public void setStringAttribute(String attrName, String attrValue) {
		// TODO Auto-generated method stub

	}

	public Object getAttribute(String attrName) {
		if (NAME.equals(attrName))
			return getName();
		else if (EMAIL.equals(attrName))
			return getEMails();
		else if (OTHERNAME.equals(attrName))
			return otherNames;
		else if (PICTURE.equals(attrName))
			return getPictures();
		return super.getAttribute(attrName);
	}

	public void setAttribute(String attrName, Object attrValue) {
		if (OTHERNAME.equals(attrName))
			otherNames = (List<Name>)attrValue;
	}
	
	/**
	 * overrides base object method note contact with different information
	 * besides name have to be compared using different mechanism
	 */
	public boolean equals(Object contact) {
		if (this == contact)
			return true;
		if (contact instanceof Contact)
			if (name == null)
				return ((Contact) contact).getName() == null;
			else
				return getName().equals(((Contact) contact).getName());
		return false;
	}

	public int hashCode() {
		return name != null ? name.hashCode() : super.hashCode();
	}

	public void saveXML(OutputStream os) throws IOException {
		saveXML(os, "utf-8", 0);
	}

	@Override
	public Object getId() {
		return getName();
	}
}