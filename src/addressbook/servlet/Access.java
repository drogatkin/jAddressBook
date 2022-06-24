/* jaddressbook - Access.java
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
 *  $Id: Access.java,v 1.6 2011/10/19 03:54:52 dmitriy Exp $
 * Created on Sep 14, 2005
 */

package addressbook.servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.aldan3.servlet.Static;
import org.aldan3.util.inet.Base64Codecs;

import addressbook.servlet.model.FolderOperations;
import addressbook.servlet.model.UserOperations.DuplicateUser;
import addressbook.servlet.model.UserOperations.NonExistingUser;
import addressbook.servlet.model.UserProfile;
import addressbook.util.IoHelper;

/**
 * Provides an access to user repository
 * 
 * @author Dmitriy
 * 
 */
public class Access extends AddressBookProcessor {

	@Override
	protected Map getModel() {
		// TODO it issues direct reading from file and problem
		// with sync with web, so it's better to use folder operations
		// to get folder status and save in XML to requester
		// TODO add loading pictures using pic index parameter
		// TODO check if use logged in and do not do BASIC auth
		String user = checkCredentials();
		if (user == null)
			return null;

		resp.setContentType("text/xml");
		resp.setHeader("Content-Disposition", "attachment; filename=datastorage.xml");
		resp.setHeader("Content-Description", "Address book data feed");
		OutputStream os = null;
		// TODO add user's key from session when available
		FolderOperations fo = getFolderOperations(user, null);
		try {
			fo.getXMLSerializer().write(fo.search(null, null), os = resp.getOutputStream(), "utf-8");
			// os.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
			try {
				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ioe.toString());
			} catch (IOException ioe2) {
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (os != null)
					os.close();
			} catch (IOException e) {
			}
		}
		return null;
	}

	@Override
	protected Map doControl() {
		String user = checkCredentials();
		if (user == null)
			return createErrorMap("error");
		// save and trigger preloaded folders to update
		Object o = getObjectParameterValue("data", null, 0, false);
		// System.err.println("validateFormData:"+o);
		if (o == null)
			return createErrorMap("error");
		FolderOperations fo = getFolderOperations(/* user */);
		// TODO do merge with currently logged
		IoHelper h = fo.getRepositoryAccess();
		OutputStream os = null;
		try {
			h.backup(user);
			os = h.getOutStream(user, true);
			if (o instanceof byte[])
				os.write((byte[]) o);
			else
				os.write(o.toString().getBytes("utf-8"));
			fo.flush(user);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DuplicateUser e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				os.close();
			} catch (Exception e) {

			}
		}
		return createErrorMap("ok");
	}

	@Override
	protected String getSubmitPage() {
		// TODO Auto-generated method stub
		return null;
	}

	protected boolean isLoginForm() {
		return true;
	}

	protected String checkCredentials() {
		String credentials = req.getHeader(Static.HTTP.AUTHORIZATION);
		if (credentials != null) {
			int p = credentials.indexOf(' ');
			if (p > 0 && credentials.substring(0, p).equalsIgnoreCase("Basic")) {
				credentials = Base64Codecs.base64Decode(credentials.substring(p + 1), null);
				p = credentials.indexOf(':');
				if (p > 0) {
					String password = credentials.substring(p + 1);
					String user = credentials.substring(0, p);
					try {
						UserProfile up = getUPOperations().getUser(user);
						if (up.matchPassword(password))
							return user;
					} catch (NonExistingUser e) {

					}
				}
			} else {
				try {
					resp.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED, "Not supported authorization type.");
				} catch (IOException e) {
					log(e.getMessage(), e);
				}
				return null;
			}
		}
		resp.setHeader(Static.HTTP.WWW_AUTHENTICATE, "Basic realm=\"Address book accessor\"");
		try {
			resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Provide your credentials for the authorization.");
			resp.flushBuffer();
		} catch (IOException e) {
			log(e.getMessage(), e);
		}
		return null;
	}

}
