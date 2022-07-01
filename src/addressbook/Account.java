/* AddressBook - Account
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
 *  $Id: Account.java,v 1.9 2013/02/07 07:56:52 cvs Exp $
 */
package addressbook;

import java.io.IOException;
import java.io.OutputStream;
import java.security.Key;

import addressbook.servlet.model.CertificateOperations;

import static addressbook.DataBookIO.*;

public class Account extends GenericAttribute {
	public static final String NAME = "name";

	public static final String LINK = "link";

	public static final String PASSWORD = "password";
	
	public static final String MAGIC_PASSWORD = "********";

	protected transient String password; // prevent persistance for real password

	protected String accessUrl;

	protected transient String account;

	protected String encPassword;

	protected String encAccount;

	// indicates that the object requires to store as encrypted
	protected boolean encrypted;

	protected String encValue;

	public Account(String name, String description) {
		super(name, description);
	}

	public Account(String name, String description, String password, String account, String url) {
		super(name, description);
		setPassword(password);
		setName(account);
		setLink(url);
	}
	
	@Override
	public void update(Object [] params) {
		// String name, String description, String password, String account, String url
		if (params == null || params.length != 5)
			throw new IllegalArgumentException();
		value = assignWithDefault((String)params[0]);
		description = assignWithDefault((String)params[1]);
		setPassword((String)params[2]);
		setName((String)params[3]);
		setLink((String)params[04]);
	}

	public void setLink(String link) {
		accessUrl = link;
	}

	@Override
	public void setValue(String v) {
		if (encrypted == false || DataBookIO.CurrentKey.getCurrent() != null)
			value = v;
	}

	public void setPassword(String password) {
		if ((encrypted == false || DataBookIO.CurrentKey.getCurrent() != null) && !MAGIC_PASSWORD.equals(password))
			this.password = password;
		//System.err.printf("Password: %s%n", password);
	}

	public void setName(String name) {
		if (encrypted == false || DataBookIO.CurrentKey.getCurrent() != null)
			account = name;
	}

	public String getLink() {
		return accessUrl;
	}

	@Override
	public String getValue() {
		if (encrypted == false)
			return value;
		if ((value == null || value.length() == 0) && encValue != null) {
			Key key = DataBookIO.CurrentKey.getCurrent();
			if (key != null) {
				value = CertificateOperations.decryptString(encValue, key);
			}
		}
		if ((value == null || value.length() == 0) && encValue != null)
			return "IIIIII";
		//System.err.printf("Returning %s%n", value);
		return value;
	}

	public String getPassword() {
		if (encrypted == false)
			return password;
		else {
			if (password == null && encPassword != null) {
				Key key = DataBookIO.CurrentKey.getCurrent();
				if (key != null) {
					password = CertificateOperations.decryptString(encPassword, key);
				}
			}
			if (password == null && encPassword != null)
				return "XXXXXX";
			return password;
		}
	}

	public String getName() {
		if (encrypted == false)
			return account;
		else {
			if (account == null && encAccount != null) {
				Key key = DataBookIO.CurrentKey.getCurrent();
				if (key != null) {
					account = CertificateOperations.decryptString(encAccount, key);
				}
			}
			if (account == null && encAccount != null)
				return "AAAAAA";
			return account;
		}
	}
	
	public void merge(Account account) {
		password = account.password;
	}
	
	@Override
	public String toString() {
		return ""+value+"/"+account+"/"+accessUrl;
	}

	@Override
	public boolean equals(Object _o) {
		if (_o == this)
			return true;
		if (_o == null || _o instanceof Account == false)
			return false;
		return (value != null && value.equals(((Account)_o).value )
				&& account != null && account.equals(((Account)_o).account )
				&& accessUrl != null && accessUrl.equals(((Account)_o).accessUrl ));
	}

	@Override
	public int hashCode() {
		return super.hashCode() ^ (value != null?value.hashCode():0) ^ (account != null?account.hashCode():0)
		^ (accessUrl != null?accessUrl.hashCode():0);
	}
	
	@Override
	public boolean equalsToUpdate(Object _o) {
		if (_o == this)
			return true;
		if (_o == null || _o instanceof Account == false)
			return false;
		Account a = (Account)_o;
		return (value != null && value.equals(a.value )) &&
				(accessUrl != null && accessUrl.equals(a.accessUrl ));
	}
	
	@Override
	public boolean isEmpty() {
		if (encrypted == false)
			return super.isEmpty();
		return (encValue == null || encValue.trim().length() == 0) && super.isEmpty();
	}
	
	public boolean isHashed() {
		return encrypted &&  DataBookIO.CurrentKey.getCurrent() == null;
	}

	public void saveXML(OutputStream _out, String _enc, int _order) throws IOException {
		if (value == null)
			return;
		_out.write("<".getBytes(_enc));
		_out.write(ACCOUNT_TAG.getBytes(_enc));
		Key key = DataBookIO.CurrentKey.getCurrent();
		// new Exception("Save Account").printStackTrace();
		//System.err.printf("Getting current key %s%n", key);
		if (encrypted == false && key != null)
			encrypted = true;
		if (encrypted)
			_out.write((" " + ENCRYPTED_ATTR + "=\"1\"").getBytes(_enc));
		_out.write(">".getBytes(_enc));
		if (encrypted) {
			if (key == null) {
				saveSimpleTag(_out, _enc, ACCOUNT_LOGIN_TAG, this.encValue);
				saveSimpleTag(_out, _enc, ACCOUNT_NAME_TAG, this.encAccount);
				saveSimpleTag(_out, _enc, PASSWORD_TAG, this.encPassword);
			} else {
				saveSimpleTag(_out, _enc, ACCOUNT_LOGIN_TAG, CertificateOperations.encryptString(getValue(), key));
				saveSimpleTag(_out, _enc, ACCOUNT_NAME_TAG, CertificateOperations.encryptString(getName(), key));
				saveSimpleTag(_out, _enc, PASSWORD_TAG, CertificateOperations.encryptString(getPassword(), key));
			}
		} else {
			saveSimpleTag(_out, _enc, ACCOUNT_LOGIN_TAG, getValue());
			if (getName() != null)
				saveSimpleTag(_out, _enc, ACCOUNT_NAME_TAG, getName());
			if (getPassword() != null)
				saveSimpleTag(_out, _enc, PASSWORD_TAG, getPassword());
		}
		if (getLink() != null)
			saveSimpleTag(_out, _enc, LINK_TAG, getLink());
		if (getType() != null)
			saveSimpleTag(_out, _enc, TYPE_TAG, getType());
		if (description != null)
			saveSimpleTag(_out, _enc, COMMENT_TAG, description);
		_out.write("</".getBytes(_enc));
		_out.write(ACCOUNT_TAG.getBytes(_enc));
		_out.write(">".getBytes(_enc));
	}

	public void saveVCard(OutputStream _out, String _enc, int _order) throws IOException {
		// TODO Auto-generated method stub

	}

}
