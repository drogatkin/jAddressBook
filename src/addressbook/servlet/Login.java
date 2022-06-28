/* jaddressbook - Login.java
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
 *  $Id: Login.java,v 1.17 2013/02/13 03:23:35 cvs Exp $
 * Created on Sep 14, 2005
 */

package addressbook.servlet;

import static addressbook.servlet.model.UserProfile.ACTIVE;
import static addressbook.servlet.model.UserProfile.LANGUAGE;
import static addressbook.servlet.model.UserProfile.NAME;
import static addressbook.servlet.model.UserProfile.TIMEZONE;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;
import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;

import org.aldan3.servlet.Static;

import addressbook.servlet.model.FolderOperations;
import addressbook.servlet.model.KeyStorage;
import addressbook.servlet.model.LogOperations;
import addressbook.servlet.model.UserOperations;
import addressbook.servlet.model.UserProfile;
import addressbook.servlet.model.UserOperations.NonExistingUser;

public class Login extends AddressBookProcessor {
	// TODO create error string constants
	static final String SIGN_OFF = "signoff";
	static final String PASSWORD_PARAM = "password";

	@Override
	protected Map getModel() {
		// TODO consider preserve a cookie with name
		if ("1".equals(getStringParameterValue(SIGN_OFF, null, 0))) {
			HttpSession session = req.getSession(false);
			if (session != null) {
				FolderOperations fo = getFolderOperations(); 
				String id = (String)session.getAttribute(HV_USER_ID);
				session.invalidate();
				fo.flush(id); // remove from cache (can be race condition if session listener in other thread?)
			} else 
                log("Logoff : no session", null);
		} 
			try {
				if (isAllowed(false))
					redirect(req, resp, getSubmitPage());
			} catch (ServletException e) {
			} catch (IOException e) {
				log("", e);
			}
		return fillWithForm(new HashMap(), NAME);
	}

	@Override
	protected Map doControl() {
                // TODO add any password break code
                // google uses IP based login attempt counts
                // and request additional challenge token from image
                // this solution doesn't look good for me
		String id = getStringParameterValue(NAME, null, 0);
		if (id == null || id.length() == 0) {
			return createErrorMap("error_nousername");
		}
		try {
			UserProfile up = getUPOperations().getUser(id);
			if (new Boolean(up.getStringAttribute(ACTIVE)) == false)
				return fillWithForm(createErrorMap("error_notactivates"), NAME);
			// use password to create decryptor
			
			if (up.matchPassword(getStringParameterValue(PASSWORD_PARAM, null, 0))) {
				setAllowed(true);
				logged(this, up);
				/*setAllowed(true);
				HttpSession s = getSession();
				s.setAttribute(HV_USER_ID, id = id.intern());
				s.setAttribute(ATTR_SESSION_FOLDER_OPER, getFolderOperations());
				UserOperations.applyLocale(up.getStringAttribute(LANGUAGE), s);
				UserOperations.applyTimezone(up.getStringAttribute(TIMEZONE), s);
				LogOperations lo = getLogOperations();
				s.setAttribute(ATTR_LASTLOGIN, lo.getLastLogin(id));
				lo.logLogin(id, req.getRemoteHost(), this.getHeader("user-agent"));
				applyKey(s, this);
				// setting mobile attribute in session
				if (mobile) 
					s.setAttribute(ATTR_WEBMOBILE, Boolean.TRUE); */
				// log out loging has to be session listener
			} else {
					Thread.sleep(3*1000); // to avoid brute force attack
					if(up.getStringAttribute(UserProfile.SECRET_ANSWER) != null) {
						Map result = fillWithForm(createErrorMap("error_password"), NAME);
						result.put("RECOVER", "1");
						return result;
					}
				return fillWithForm(createErrorMap("error_password"), NAME);
			}
		} catch (NonExistingUser|IllegalArgumentException e) {
			return fillWithForm(createErrorMap("error_nosuchuser"), NAME);
		} catch (InterruptedException e) {
		}
		return null;
	}
	
	static protected void logged(AddressBookProcessor abp, UserProfile up) {
		//abp.setAllowed(true);
		HttpSession s = abp.getSession();
		String id;
		s.setAttribute(HV_USER_ID, id = up.getStringAttribute(NAME));
		s.setAttribute(ATTR_SESSION_FOLDER_OPER, abp.getFolderOperations());
		UserOperations.applyLocale(up.getStringAttribute(LANGUAGE), s);
		UserOperations.applyTimezone(up.getStringAttribute(TIMEZONE), s);
		LogOperations lo = abp.getLogOperations();
		s.setAttribute(ATTR_LASTLOGIN, lo.getLastLogin(id));
		lo.logLogin(id, abp.getRemoteHost(), abp.getHeader("user-agent"));
		applyKey(s, abp);
		if (abp.mobile) 
			s.setAttribute(ATTR_WEBMOBILE, Boolean.TRUE);
	}

	public String processcheckCall() {
		log("processcheckCall", null);
		Map m = doControl();
		if (m == null)
			return "Ok";
		return  (String)m.get(Static.Variable.ERROR);
	}

	@Override
	protected String getViewName() {
		return getViewName("login.htm");
	}

	@Override
	protected String getSubmitPage() {
		return mobile?"Foldernavigator":"Abfrontview";
	}

	public boolean isPublic() {
		return true;
	}
	
	/** Bean method for reading key from a form and populate in a session
	 * 
	 *
	 */
	public static boolean applyKey(HttpSession s, AddressBookProcessor abp) {
		// TODO consider only one parameter, session can be taken
		SecretKey k = null;
		Object pko = abp.getObjectParameterValue("skey", null, 0, false);
		if (pko != null && pko instanceof byte[]) {
			ObjectInputStream ois;
			try {
				ois = new ObjectInputStream(new ByteArrayInputStream((byte[])pko));
				k = (SecretKey)ois.readObject();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		s.setAttribute(KeyStorage.SESS_ATR, new KeyStorage(k));
		return k != null;
	}
}
