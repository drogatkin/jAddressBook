/* AddressBook - Address
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
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE
 */
package addressbook.servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.http.HttpSession;

import org.aldan3.util.DataConv;

import addressbook.Contact;
import addressbook.EMail;
import addressbook.servlet.model.AbstractAttributeStorage;
import addressbook.servlet.model.PebbleAccount;
import addressbook.servlet.model.UserOperations;
import addressbook.servlet.model.UserProfile;
import addressbook.servlet.model.PebbleAccount.Problem;
import addressbook.servlet.model.UserOperations.DuplicateUser;
import addressbook.servlet.model.UserOperations.NonExistingUser;
import static addressbook.servlet.model.UserProfile.*;

public class Registry extends AddressBookProcessor {
	static final String P_CHALLENGE_TOKEN = "challengetoken";
	static final String  SECRET_ANSWER_PARAM = "secret_answer";

	protected Map getModel() {
		Map<String, Object> result = new HashMap<String, Object>();
		if (isLoginForm() == false) {
			// generally admin should be able to edit, but
			// note, we do not allow to admin change any user information
			// so for this reason there is no access rights and a parameter with
			// profile id
			String id = (String) getSessionAttribute(HV_USER_ID, null);
			if (id == null) {
				try {
					redirect(req, resp, this.getUnauthorizedPage());
				} catch (IOException e) {
				}
				return null;
			}

			try {
				UserProfile up = getUPOperations().getUser(id);
				up.copyResultMap(result, NAME, EMAIL, LANGUAGE, TIMEZONE, SECRET_QUESTION);
				result.put(P_MODE, "edit");
			} catch (NonExistingUser e) {
				log("User id in session can't be found.", e);
			}
		}
		return result;
	}

	protected boolean isLoginForm() {
		return "edit".equals(getStringParameterValue(P_MODE, "new", 0)) == false;
		// || signed on
	}

	public boolean isPublic() {
		return isLoginForm();
	}

	public Map processAjaxCall() {
		if (isLoginForm() == false)
			return null;
		String id = getStringParameterValue(HV_NAME, "", 0);
		if (id.length() == 0)
			return createAjaxResMap("3", null);
		try {
			getUPOperations().getUser(id);
			return createAjaxResMap("2", id + "32");
		} catch (NonExistingUser e) {
			log("No user '" + id, null);
		}
		return createAjaxResMap("0", null);
	}

	public String getAjaxViewName() {
		return "jsonresp.htmp";
	}

	@Override
	protected Map doControl() {
		String id = (String) getSessionAttribute(HV_USER_ID, getStringParameterValue(NAME, null, 0));
		if (id == null || id.length() == 0)
			if (isLoginForm())
				return createErrorMap("error_nousername");
			else {
				try {
					redirect(req, resp, getUnauthorizedPage());
				} catch (IOException e) {
				}
				return null;
			}
		String password = getStringParameterValue(Login.PASSWORD_PARAM, "", 0);
		if (isLoginForm() && password.length() == 0)
			return fillWithForm(createErrorMap("error_passwordempty"), NAME, EMAIL, LANGUAGE, TIMEZONE,
					SECRET_QUESTION, P_CHALLENGE_TOKEN);
		if (password.equals(getStringParameterValue(Login.PASSWORD_PARAM + '2', "", 0)) == false)
			return fillWithForm(createErrorMap("error_passwordsnotmatch"), NAME, EMAIL, LANGUAGE, TIMEZONE,
					SECRET_QUESTION, P_CHALLENGE_TOKEN, P_MODE);
		boolean instant_creation = "yes".equals(getProperty("NOACCOUNTCONFIRMATION"));
		String email = getStringParameterValue(EMAIL, "", 0);
		if (email.length() == 0 && instant_creation == false)
			return fillWithForm(createErrorMap("error_emailempty"), NAME, EMAIL, LANGUAGE, TIMEZONE, SECRET_QUESTION,
					P_CHALLENGE_TOKEN, P_MODE);
		UserProfile up = null;
		try {
			try {
				up = getUPOperations().getUser(id);
				if (isLoginForm()) {
					return fillWithForm(createErrorMap("error_existingname"), NAME, EMAIL, LANGUAGE, TIMEZONE,
							SECRET_QUESTION, P_CHALLENGE_TOKEN);
				}
				// TODO use a code with validation
				// TODO check if e-mail changed than initiate validation
				// procedure with account suspension
				fillWithForm(up, EMAIL, LANGUAGE, TIMEZONE, SECRET_QUESTION);
				fillWithFormFilled(up, ACCESS_KEY);
				String secretAnswer = getStringParameterValue(SECRET_ANSWER_PARAM, "", 0);
				if (!secretAnswer.isEmpty())
					up.setSecretAnswer(secretAnswer);
				password = getStringParameterValue(Login.PASSWORD_PARAM, "", 0);
				if (!password.isEmpty())
					up.setPassword(password);
				// TODO validate e-mail address form here
				getUPOperations().updateUser(id, up);
				updateBlogAccount(up);
				HttpSession s = getSession();
				UserOperations.applyLocale(up.getStringAttribute(LANGUAGE), s);
				UserOperations.applyTimezone(up.getStringAttribute(TIMEZONE), s);
			} catch (NonExistingUser e) {
				if (isLoginForm()) {
					String challengeToken = UserOperations.getUserToken(id);
					if (challengeToken == null || challengeToken.length() == 0)
						return fillWithForm(createErrorMap("error_challenge"), NAME, EMAIL, LANGUAGE, TIMEZONE,
								SECRET_QUESTION, P_CHALLENGE_TOKEN);
					if (challengeToken.length() > 5)
						challengeToken = challengeToken.substring(0, 5);

					if (challengeToken.equals(getStringParameterValue(P_CHALLENGE_TOKEN, null, 0)) == false)
						return fillWithForm(createErrorMap("error_challenge"), NAME, EMAIL, LANGUAGE, TIMEZONE,
								SECRET_QUESTION, P_CHALLENGE_TOKEN);

					up = new UserProfile();
					fillWithForm(up, NAME, EMAIL, ACCESS_KEY, LANGUAGE, TIMEZONE, SECRET_QUESTION);
					up.setPassword(getStringParameterValue(Login.PASSWORD_PARAM, "", 0));
					try {
						// order important to be able to escalate
						if (instant_creation) {
							up.setStringAttribute(UserProfile.ACTIVE, "true");
							up.setStringAttribute(UserProfile.LIFETIME, "true");
							getUPOperations().addUser(id, up);
						} else {
							String token = getEmailValidatorOperations().setUser(id);
							getUPOperations().addUser(id, up);
							Locale l = DataConv.localeFromString((String) up.getAttribute(LANGUAGE));
							if (l == null)
								l = getLocale();
							TimeZone tz = DataConv.timeZoneFromString(up.getStringAttribute(TIMEZONE));
							getEmailValidatorOperations().sendVerifEmail(up, token, req.getRequestURL(), l, tz);
							getEmailValidatorOperations().flush();
						}
					} catch (DuplicateUser e1) {
						return fillWithForm(createErrorMap("error_concurrency"), NAME, EMAIL, LANGUAGE, TIMEZONE,
								SECRET_QUESTION, P_CHALLENGE_TOKEN);
					}
				} else
					return fillWithForm(createErrorMap("error_consistency"), NAME, EMAIL, LANGUAGE, TIMEZONE,
							SECRET_QUESTION, P_MODE);
			} catch (Problem e) {
				return fillWithForm(createErrorMap(e.getMessage()), NAME, EMAIL, LANGUAGE, TIMEZONE, SECRET_QUESTION,
						P_MODE);
			}
		} catch (IOException ioe) {
			log("IO", ioe);
			return fillWithForm(createErrorMap("error_ioerror"), NAME, EMAIL, LANGUAGE, TIMEZONE, SECRET_QUESTION,
					P_CHALLENGE_TOKEN, P_MODE);
		}
		return null;
	}

	protected void updateBlogAccount(UserProfile up) throws Problem {
		// note blog account can be created only at e-mail confirmation
		// and change of an exisitng user account
		// find which account, currently hardcode to blog
		if (this.getIntParameterValue("pebble", 0, 0) != 1)
			return;
		List<AbstractAttributeStorage> l = getRecordOperations().search(".*", null, getFolderOperations());

		String email = (String) up.getAttribute(UserProfile.EMAIL);
		for (AbstractAttributeStorage c : l) {
			if (c instanceof Contact == false)
				continue;
			Contact contact = (Contact) c;
			List<EMail> el = contact.getEMails();
			if (el != null)
				for (EMail e : el) {
					if (email.equals(e.getValue())) {
						// System.err.printf("Pebble account synced by e-mail %s to contact %s%n",
						// email, contact);
						new PebbleAccount().create(up, getProperties(), contact);
						return;
					}
				}
		}
		throw new Problem(String.format(
				getResourceString("error_pebble_account", "No corresponding Pebble account for %s has been found"),
				email));
	}
	
	@Override
	protected String getSubmitPage() {
		boolean instant_creation = "yes".equals(getProperty("NOACCOUNTCONFIRMATION"));
		return isLoginForm() ? instant_creation?"Login":"Emailvalidator?" + fillUrlWithForm(NAME, EMAIL) : "Abfrontview";
	}

	protected Map createAjaxResMap(String error, String suggestion) {
		Map result = new HashMap();
		Map[] elements = new Map[1];
		result.put(HV_ELEMENT, elements);
		elements[0] = new HashMap();
		Map[] members = new Map[2];
		elements[0].put(HV_MEMBER, members);
		members[0] = new HashMap();
		members[0].put(HV_NAME, "error");
		members[0].put(HV_VALUE, error);
		members[1] = new HashMap();
		members[1].put(HV_NAME, "suggestion");
		members[1].put(HV_VALUE, suggestion);
		return result;
	}
}