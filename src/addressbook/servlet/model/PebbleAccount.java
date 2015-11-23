/* jaddressbook - PebbleAccount.java
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
 *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 *  DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 *  SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 *  CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 *  LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 *  OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 *  SUCH DAMAGE.
 *  
 *  Visit http://jaddressbook.sourceforge.net to get the latest infromation
 *  about Rogatkin's products.                                                        
 *  $Id: PebbleAccount.java,v 1.5 2007/12/11 05:02:43 rogatkin Exp $                
 *  Created on Jul 4, 2006
 *  @author Dmitriy
 */
package addressbook.servlet.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

import org.aldan3.util.DataConv;

import addressbook.AddressException;
import addressbook.Contact;

public class PebbleAccount {
	public static final String WEBSITE = "website";

	public static final String NAME = "name";

	public static final String PASSWORD = "password";

	public static final String ROLES = "roles";

	public static final String EMAILADDRESS = "emailAddress";

	public static final String PEBBLE_DATA_ROOT = "PEBBLE_DATA_ROOT";

	public static final String PEBBLE_ADMIN = "PEBBLE_ADMIN";
	
	public static final String PEBBLE_PROFILE = "profile";

	public static final String PEBBLE_USER_ROLES = "PEBBLE_USER_ROLES";

	private static final String ROLE_VALUES = "ROLE_BLOG_READER,ROLE_BLOG_ADMIN,ROLE_BLOG_OWNER,ROLE_BLOG_PUBLISHER,ROLE_BLOG_CONTRIBUTOR";

	public static class Problem extends AddressException {
		public Problem(String message) {
			super(message);
		}
	}

	public void create(UserProfile up, Properties sysProps, Contact contact) throws Problem {
		Properties pebbleProfile = new Properties();
		if (up.getStringAttribute(UserProfile.NAME).equals(sysProps.getProperty(PEBBLE_ADMIN)))
			pebbleProfile.setProperty(ROLES, ROLE_VALUES);
		else
			pebbleProfile.setProperty(ROLES, sysProps.getProperty(PEBBLE_USER_ROLES,
					"ROLE_BLOG_PUBLISHER,ROLE_BLOG_CONTRIBUTOR"));
		if (up.getStringAttribute(UserProfile.EMAIL) != null)
			pebbleProfile.setProperty(EMAILADDRESS, up.getStringAttribute(UserProfile.EMAIL));
		if (contact != null && contact.getLinks() != null && contact.getLinks().size() > 0)
			pebbleProfile.setProperty(WEBSITE, contact.getLinks().get(0).toString());
		else
			pebbleProfile.setProperty(WEBSITE, "http://jaddressbook.sourceforge.net");

		pebbleProfile.setProperty(PASSWORD, encode(up.getStringAttribute(UserProfile.PASSWORD), up
				.getStringAttribute(UserProfile.NAME)));
		if (contact != null && contact.getName() != null)
			pebbleProfile.setProperty(NAME, contact.getName().toString());
		else
			pebbleProfile.setProperty(NAME, up.getStringAttribute(UserProfile.NAME));
		pebbleProfile.setProperty("detailsUpdateable","true");
		// check first if Pebble isn't in a standard location
		String pebbleRoot = getRootFolder(sysProps);
		FileOutputStream fos = null;
		try {
			pebbleProfile.store(fos = new FileOutputStream(pebbleRoot + File.separator + "realm" + File.separator
					+ up.getStringAttribute(UserProfile.NAME) + ".properties"), "User : "
					+ up.getStringAttribute(UserProfile.NAME));
			//System.err.printf("Pebble account updated/created at %s%n", pebbleRoot + File.separator + "realm" + File.separator
				//	+ up.getStringAttribute(UserProfile.NAME) + ".properties");
		} catch (FileNotFoundException e) {
			throw new Problem("Couldn't find Pebble account file: "+e);
		} catch (IOException e) {
			throw new Problem("IO at updating Pebble account: "+e);
		} finally {
			if (fos != null)
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}

	public void delete(UserProfile up, Properties sysProps) {
		File accntFile = new File(getRootFolder(sysProps) + File.separator + "realm" + File.separator
				+ up.getStringAttribute(UserProfile.NAME) + ".properties");
		if (accntFile.delete() == false)
			System.err.printf("Can't delete user account %s file %s.%n", up.getStringAttribute(UserProfile.NAME),
					accntFile);
	}

	private String getRootFolder(Properties props) {
		String pebbleRoot = props.getProperty(PEBBLE_DATA_ROOT);
		if (pebbleRoot == null)
			pebbleRoot = System.getProperty("user.home") + File.separator + "pebble";
		return pebbleRoot;
	}

	// TODO consider to move in Cipher Operations
	private String encode(String password, String salt) {
		String saltedPass = mergePasswordAndSalt(password, salt, false);

		MessageDigest messageDigest = getMessageDigest();

		byte[] digest = messageDigest.digest(saltedPass.getBytes());

		return DataConv.bytesToHex(digest);
	}

	private final MessageDigest getMessageDigest() throws IllegalArgumentException {
		try {
			return MessageDigest.getInstance("SHA");
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalArgumentException("No such algorithm [" + "MD5" + "]");
		}
	}

	private String mergePasswordAndSalt(String password, Object salt, boolean strict) {
		if (password == null) {
			password = "";
		}

		if (strict && (salt != null)) {
			if ((salt.toString().lastIndexOf("{") != -1) || (salt.toString().lastIndexOf("}") != -1)) {
				throw new IllegalArgumentException("Cannot use { or } in salt.toString()");
			}
		}
		if ((salt == null) || "".equals(salt)) {
			return password;
		} else {
			return password + "{" + salt.toString() + "}";
		}
	}

	public static void main(String... args) {
		UserProfile up = new UserProfile();
		up.setAttribute(UserProfile.PASSWORD, args[0]);
		up.setAttribute(UserProfile.NAME, args[1]);
		try {
			new PebbleAccount().create(up, new Properties(), null);
		} catch (Problem e) {
			e.printStackTrace();
		}
	}
}
