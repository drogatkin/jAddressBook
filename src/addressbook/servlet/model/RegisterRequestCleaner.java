/* jaddressbook - RegisterRequestCleaner
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
 *  $Id: RegisterRequestCleaner.java,v 1.6 2007/06/29 00:10:16 rogatkin Exp $
 * Created on Nov 7, 2005
 */
package addressbook.servlet.model;

import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import addressbook.AddressBookWeb;
import addressbook.servlet.AddressBookProcessor;
import addressbook.servlet.model.UserOperations.NonExistingUser;

import static addressbook.servlet.model.UserProfile.*;

public class RegisterRequestCleaner extends TimerTask implements ServletContextListener {
	public static long _24H = 1000 * 60 * 60 * 24;

	protected ServletContext sc;

	protected Timer timer; // hourly

	protected Timer dtimer; // daily

	public void contextInitialized(ServletContextEvent sce) {
		sc = sce.getServletContext();
		// sc.getInitParameter(NOT_ACTIVATE_IN);
		timer = new Timer("Abandoned registry record cleaner", true);
		// TODO consider configurable
		timer.scheduleAtFixedRate(this, 60 * 1000 * 10, 60 * 60 * 1000);
		dtimer = new Timer("Abandoned accounts cleaner", true);
		dtimer.scheduleAtFixedRate(new AbandonedAccountCleaner(), 60 * 1000 * 10, 24 * 60 * 60 * 1000);
	}

	public void contextDestroyed(ServletContextEvent sce) {
		if (timer != null)
			timer.cancel();
		if (dtimer != null)
			dtimer.cancel();
	}

	@Override
	public void run() {
		EmailValidatorOperations evo = (EmailValidatorOperations) sc.getAttribute(AddressBookProcessor.EMAILVALIDATOR);
		// note, operations will be null until a new registration happens since lazy initialization
		if (evo != null) {
			UserOperations uo = (UserOperations) sc.getAttribute(AddressBookProcessor.USERRECORD);
			if (uo != null) {
				Enumeration i = evo.getPendingTokens();
				while (i.hasMoreElements()) {
					String token = (String) i.nextElement();
					try {
						UserProfile up = uo.getUser(evo.getUser(token));
						if ("true".equals(up.getAttribute(ACTIVE)) == false) {
							// resend e-mail? delete?
							// TODO read from config timeout value
							if (System.currentTimeMillis() - up.getLongAttribute(ADDEDON, 0) > 60 * 1000 * 60 * 10)
								uo.deleteUser((String) up.getAttribute(NAME));
						} else 
							// remove?? concurrency
							evo.removeUser(token);
					} catch (NonExistingUser e) {
						evo.removeUser(token);
					}
				}
				try {
					evo.flush();
				} catch (IOException e) {					
					e.printStackTrace();
				}
			}
		}
	}

	protected class AbandonedAccountCleaner extends TimerTask {

		@Override
		public void run() {
			long abandonedInterval = 0;
			Properties props = (Properties) sc.getAttribute(AddressBookWeb.CONFIGURATION);
			if (props == null) {
				System.err.printf("No cofiguration data for abandoned accounts.\n");
				abandonedInterval = -1;
			} else
				abandonedInterval = Long.parseLong(props.getProperty(AddressBookProcessor.ABANDONED_AFTER, "-1"))
						* _24H;
			if (abandonedInterval < 0)
				return;
			UserOperations uo = (UserOperations) sc.getAttribute(AddressBookProcessor.USERRECORD);
			LogOperations lo = (LogOperations) sc.getAttribute(AddressBookProcessor.LOGOPER);
			FolderOperations fo = (FolderOperations) sc.getAttribute(AddressBookProcessor.ATTR_SESSION_FOLDER_OPER);
			if (uo != null && lo != null && fo != null) {
				Iterator<String> i = uo.getUserNames(null);
				while (i.hasNext()) {
					String user = i.next();
					Date ll = lo.getLastLogin(user);
					UserProfile up = null;
					try {
					up = uo.getUser(user);
					} catch (NonExistingUser e) {
						e.printStackTrace();
						continue;
					}
					if ("true".equals(up.getAttribute(LIFETIME)))
						continue;
					if (ll == null) {
							if ("true".equals(up.getAttribute(ACTIVE)) == false)
								continue; // skip not active users
							ll = new Date(up.getLongAttribute(ADDEDON, 0));
					}
					lo.unload(user);
					if (System.currentTimeMillis() - ll.getTime() > abandonedInterval) {
						// @start transaction
						fo.remove(user);
						// ?exception, roll back
						uo.deleteUser(user);
						// ?exception, roll back
						AttachmentOperations ao = new AttachmentOperations();
						ao.init((Properties) sc.getAttribute(AddressBookWeb.CONFIGURATION));
						ao.deleteAll(user); // consider also global delete user in one place
						lo.delete(user);
						try {
							deleteBlog(uo.getUser(user), props);
						} catch (NonExistingUser e) {
						}
						// @end transaction
						System.err.printf("Abandoned account %s removed from system.", user);
					}

				}
			}
		}

		private void deleteBlog(UserProfile user, Properties props) {
			if ("Pebble".equals(props.getProperty(AddressBookProcessor.BLOG)))
				new PebbleAccount().delete(user, props);
		}
	}
}
