/* jaddressbook - UserOperations.java
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
 *  $Id: UserOperations.java,v 1.4 2011/08/31 02:46:11 dmitriy Exp $
 * Created on Sep 13, 2005
 */

package addressbook.servlet.model;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;
import java.util.TimeZone;

import javax.servlet.http.HttpSession;

import org.aldan3.servlet.Static;
import org.aldan3.util.DataConv;
import org.aldan3.util.inet.HttpUtils;

import addressbook.AddressException;
import addressbook.servlet.AddressBookProcessor;
import addressbook.util.IoHelper;

public class UserOperations extends AbstractOperations {
	public static final String P_CIPHERSEED = "CIPHERSEED";
	protected IoHelper usersRepositoryBase;
	protected static String seed;

	public static class DuplicateUser extends AddressException {

		public DuplicateUser(String message) {
			super(message);
			// TODO Auto-generated constructor stub
		}
	}

	public static class NonExistingUser extends AddressException {

		public NonExistingUser(String message) {
			super(message);
			// TODO Auto-generated constructor stub
		}
	}

	public void addUser(String userName, UserProfile profile) throws DuplicateUser, IOException {
		InputStream is = null;
		try {
			is = usersRepositoryBase.getInStream(userName);
			if (is != null) {
				throw new DuplicateUser(userName);
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			if (is != null)
				try {
					is.close();
				} catch (IOException ioe) {
				}
		}
		OutputStream os = null;
		try {
			os = usersRepositoryBase.getOutStream(userName, true);
			if (os != null) {
				profile.setAttribute(UserProfile.ADDEDON, "" + System.currentTimeMillis());
				profile.saveXML(os);
			} else
				throw new IOException("IO error in open user profile");
		} catch (IOException ioe) {
			ioe.printStackTrace();
			throw ioe;
		} finally {
			if (os != null)
				try {
					os.close();
				} catch (IOException ioe) {
				}
		}
	}

	public UserProfile getUser(String userName) throws NonExistingUser {
		InputStream is = null;
		try {
			is = usersRepositoryBase.getInStream(userName);
			if (is == null) {
				throw new NonExistingUser(userName);
			}
			UserProfile up = new UserProfile();
			up.fromXML(is);
			return up;
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			if (is != null)
				try {
					is.close();
				} catch (IOException ioe) {
				}
		}

		throw new NonExistingUser(userName);
	}

	public void updateUser(String userName, UserProfile profile) throws NonExistingUser, IOException {
		OutputStream os = null;
		try {
			os = usersRepositoryBase.getOutStream(userName, false);
			if (os != null) {
				profile.saveXML(os);
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (DuplicateUser e) {
		} finally {
			if (os != null)
				try {
					os.close();
				} catch (IOException ioe) {
				}
		}
	}
	
	public void deleteUser(String userName) {
		usersRepositoryBase.delete(userName);
	}
	
	public Iterator<String> getUserNames(final String mask) {
		File f = usersRepositoryBase.getFile("");
		if (f.isDirectory() == false)
			return null;
		String [] names = f.list(new FilenameFilter() {
			public boolean accept(File arg0, String arg1) {				
				return mask == null || arg1.matches(mask);
			}			
		});
		for(int i=0; i<names.length; i++)
			try {
				names[i] = HttpUtils.htmlDecode(URLDecoder.decode(names[i], "utf-8"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		return Arrays.asList(names).iterator();
	}

	public void init(AddressBookProcessor abp) {
		usersRepositoryBase = new IoHelper(abp.getDataRoot(), "users", abp.getProperties());
		seed = abp.getProperty(P_CIPHERSEED, "");
	}

	public static void applyTimezone(String timezone, HttpSession session) {
		if (timezone != null)
			session.setAttribute(Static.Session.TIMEZONE, TimeZone.getTimeZone(timezone)); // check
	}

	public static void applyLocale(String locale, HttpSession session) {
		if (locale != null) {
			int sp = locale.indexOf('_');
			if (sp > 0) {
				session.setAttribute(Static.Session.LOCALE,
						new Locale(locale.substring(0, sp), locale.substring(sp + 1)));
			} else
				session.setAttribute(Static.Session.LOCALE, new Locale(locale));
		}
	}

	public static String getUserToken(String userName) {
		if (userName != null && userName.trim().length() > 0)
			try {
				// todo add config seed to avoid guesing
				MessageDigest md = MessageDigest.getInstance("MD5");
				md.update((userName+seed).getBytes("ISO-8859-1"));
				return DataConv.bytesToHex(md.digest());
			} catch (NullPointerException npe) {
				// ignore
			} catch (NoSuchAlgorithmException e) {
			} catch (UnsupportedEncodingException e) {
			}
		return null;
	}
}
