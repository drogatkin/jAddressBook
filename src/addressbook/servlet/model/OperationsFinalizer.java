/* jaddressbook - OperationsFinalizer
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
 *  $Id: OperationsFinalizer.java,v 1.7 2012/09/15 17:49:54 dmitriy Exp $
 * Created on Nov 3, 2005
 */
package addressbook.servlet.model;

import javax.servlet.ServletContextAttributeEvent;
import javax.servlet.ServletContextAttributeListener;
import javax.servlet.ServletRequestAttributeEvent;
import javax.servlet.ServletRequestAttributeListener;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import addressbook.DataBookIO;
import addressbook.servlet.AddressBookProcessor;

/**
 * @author Dmitriy
 * 
 */
public class OperationsFinalizer implements ServletContextAttributeListener, HttpSessionListener,
		ServletRequestAttributeListener, ServletRequestListener {

	public void attributeAdded(ServletContextAttributeEvent ae) {
		// not interested
	}

	public void attributeRemoved(ServletContextAttributeEvent ae) {
		if (AddressBookProcessor.FOLDEROPER.equals(ae.getName())) {
			Object val = ae.getValue();
			if (val instanceof FolderOperations)
				((FolderOperations) val).saveAll();
		} else if (AddressBookProcessor.LOGOPER.equals(ae.getName())) {
			Object val = ae.getValue();
			if (val instanceof LogOperations)
				((LogOperations) val).unload(null);
		}
	}

	public void attributeReplaced(ServletContextAttributeEvent ae) {
		// not interested
	}

	// ////////////// session listener ////////////
	public void sessionCreated(HttpSessionEvent se) {
		// not interested
	}

	public void sessionDestroyed(HttpSessionEvent se) {
		//new Exception("sessionDestroyed").printStackTrace();
		try {
			HttpSession s = se.getSession();
			FolderOperations fo = (FolderOperations) s.getAttribute(AddressBookProcessor.ATTR_SESSION_FOLDER_OPER);
			KeyStorage ks = (KeyStorage) s.getAttribute(KeyStorage.SESS_ATR);
			if (ks != null)
				DataBookIO.CurrentKey.setKey(ks.getKey());
			if (fo != null)
				fo.save();
			String user = (String) s.getAttribute(AddressBookProcessor.HV_USER_ID);
			if (user != null) {
				LogOperations lo = (LogOperations) s.getServletContext().getAttribute(AddressBookProcessor.LOGOPER);
				if (lo != null)
					lo.logLogOut(user);
			} else
				System.err.println("Can't log logout");
		} catch (Exception e) {
			System.err.println("A problem's happened at processing a session destroy: " + e);
			e.printStackTrace();
		} finally {
			// clear key
			DataBookIO.CurrentKey.setKey(null);
		}
	}

	public void attributeAdded(ServletRequestAttributeEvent arg0) {
		if (__DEBUG)
			System.err.printf("Req attr %s with %s added in %s\n", arg0.getName(), arg0.getValue(),
					arg0.getServletRequest());
	}

	public void attributeRemoved(ServletRequestAttributeEvent arg0) {
		if (__DEBUG)
			System.err.printf("Req attr %s with %s removed in %s\n", arg0.getName(), arg0.getValue(),
					arg0.getServletRequest());
	}

	public void attributeReplaced(ServletRequestAttributeEvent arg0) {
		if (__DEBUG)
			System.err.printf("Req attr %s with %s replaced in %s\n", arg0.getName(), arg0.getValue(),
					arg0.getServletRequest());
	}

	public void requestDestroyed(ServletRequestEvent arg0) {
		DataBookIO.CurrentKey.setKey(null);
	}

	public void requestInitialized(ServletRequestEvent arg0) {
		HttpSession s = ((HttpServletRequest) arg0.getServletRequest()).getSession(false);
		if (s != null) {
			try {
				KeyStorage ks = (KeyStorage) s.getAttribute(KeyStorage.SESS_ATR);
				if (ks != null)
					DataBookIO.CurrentKey.setKey(ks.getKey());
			} catch (Exception e) {
				System.err.printf("Couldn't set keystorage in request %s%n", s.getAttribute(KeyStorage.SESS_ATR));
			}
		}
	}

	private static final boolean __DEBUG = false;
}
