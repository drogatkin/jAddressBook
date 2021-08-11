/* jaddressbook - EmailValidatorOperations.java
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
 *  $Id: EmailValidatorOperations.java,v 1.7 2009/08/11 07:11:28 dmitriy Exp $
 * Created on Sep 16, 2005
 */

package addressbook.servlet.model;

import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.aldan3.model.ProcessException;
import org.aldan3.model.TemplateProcessor;
import org.aldan3.util.ResourceManager;
import org.aldan3.util.TemplateEngine;
import org.aldan3.util.inet.SendMail;

import addressbook.AddressBookFrame;
import addressbook.servlet.AddressBookProcessor;
import addressbook.servlet.Emailvalidator;

// TODO find a better name, like RegisterValidatorOperations
public class EmailValidatorOperations extends AbstractOperations {

	public static final String AB_MAIL = "AB_MAIL";

	public static final String EXPOSEDHOST = "EXPOSEDHOST";

	public static final String UNDIS_RECIP = "undisclosed-recipients:;";

	protected Properties validationProperties = new Properties();

	protected Properties emailHeaders;

	protected Properties properties;

	protected URL verificationRepository;

	protected static final int STRONG = 16;

	protected SendMail sendMail;

	protected String adminAddress;

	protected String resourceBaseName;
	
	protected TemplateProcessor tp;

	public void init(AddressBookProcessor abp) {
		properties = abp.getProperties();
		String epass = properties.getProperty("ENC" + SendMail.PROP_PASSWORD);
		if (epass != null) {
			epass = abp.getCipherOperations().decrypt(epass);
			if (epass != null)
				properties.setProperty(SendMail.PROP_PASSWORD, epass);
		}
		tp = abp.getTemplateProcessor();
		InputStream is = null;
		try {
			File f = new File(abp.getDataRoot() + File.separatorChar + AddressBookFrame.PROGRAMNAME
					+ File.separatorChar + "activations");
			if (f.exists() == false)
				f.mkdirs();
			verificationRepository = new File(f, "requests.properties").toURI().toURL();
			is = new FileInputStream(new File(verificationRepository.toURI()));
			validationProperties.load(is);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} finally {
			if (is != null)
				try {
					is.close();
				} catch (IOException e) {
				}
		}
		emailHeaders = new Properties();
		emailHeaders.put("To", UNDIS_RECIP);
		emailHeaders.put("Mime-Version", "1.0");
		emailHeaders.put("Content-Type", "text/plain; format=flowed; charset=UTF-8");
		sendMail = new SendMail(abp.getProperties());
		adminAddress = abp.getProperty(AB_MAIL);
		resourceBaseName = abp.getResourceName();
		// verificationRepository = abp("");
	}

	public String getUser(String verifToken) {
		return validationProperties.getProperty(verifToken);
	}

	public void removeUser(String verifToken) {
		validationProperties.remove(verifToken);
	}

	public String setUser(String id) {
		String token = generateToken();
		validationProperties.setProperty(token, id);
		return token;
	}

	public Enumeration<?> getPendingTokens() {
		return ((Properties) validationProperties.clone()).propertyNames(); // to
																			// avoid
																			// concurrent
																			// mod
	}

	public synchronized void flush() throws IOException {
		OutputStream os = null;
		try {
			File f = new File(new URL(verificationRepository, ".new").toURI());
			os = new FileOutputStream(f);
			validationProperties.store(os, "");
			os.close();
			os = null;
			// should synchronize be here instead of entire method?
			File f2 = new File(verificationRepository.toURI());
			if (f2.exists() && f2.delete() == false)
				throw new IOException("Can't delete " + f2);
			if (f.renameTo(f2) == false)
				throw new IOException("Can't rename " + f + " to " + f2);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (os != null)
				os.close();
		}
	}

	public void sendVerifEmail(UserProfile up, String verifToken, StringBuffer url, Locale locale, TimeZone tz)
			throws IOException {
		// threadPool.execute(new Runnable() {
		// add an e-mail job in a line
		Map map = new HashMap();
		map.put(Emailvalidator.TOKEN, verifToken);
		map.put(UserProfile.NAME, up.getStringAttribute(UserProfile.NAME));
		String exposedHost = properties.getProperty(EXPOSEDHOST);
		if (exposedHost != null && exposedHost.length() > 0) {
			int epp = url.indexOf("://");
			assert epp > 0;
			url.replace(epp + 3, url.indexOf("/", epp + 4), exposedHost);
		}
		int lsp = url.lastIndexOf("/");
		map.put("url", url.subSequence(0, lsp));
		// TODO use locale from user profile to send a note in required
		// language, res name in resourceBaseName
		// TODO subject has to be localized too and gotten from a resource
		// emailHeaders.put("Bcc:", up.getStringAttribute(UserProfile.EMAIL));
		CharArrayWriter w = new CharArrayWriter(200);
		try {
			tp.process(w, "activationemail.txt", map, properties, locale, tz);
		} catch (ProcessException e) {
			throw new IOException(e);
		}
		// TODO get subject from resource
		sendMail.send(adminAddress, up.getStringAttribute(UserProfile.EMAIL), "Activation notice", w.toString(),
				emailHeaders);
	}

	protected String generateToken() {
		StringBuffer token = new StringBuffer(16);
		// new java.util.Random
		for (int i = 0; i < STRONG; i++)
			token.append((char) ('a' + (int) (Math.random() * 26)));
		return token.toString();
	}

}
