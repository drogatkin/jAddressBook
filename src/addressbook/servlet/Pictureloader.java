/* jaddressbook - Pictureloader
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
 *  $Id: Pictureloader.java,v 1.4 2013/02/14 04:15:59 cvs Exp $
 * Created on Nov 15, 2005
 */
package addressbook.servlet;

import static addressbook.Contact.NAME;
import static addressbook.GenericAttribute.VALUE;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import org.aldan3.util.Stream;

import addressbook.Contact;
import addressbook.Picture;
import addressbook.servlet.model.FolderOperations;
import addressbook.servlet.model.GenericOperations;

public class Pictureloader extends AddressBookProcessor {

	@Override
	protected Map getModel() {
		GenericOperations<Contact> co = getContactOperations();
		FolderOperations fo = getFolderOperations();
		
		InputStream is = null;
		OutputStream os = null;
		Contact contact = co.getContact(fo, getStringParameterValue(P_FOLDER, getStringParameterValue(P_NODE, "", 0), 0),
				NAME, P_HASH, this);
		Picture pic = null;
		if (contact != null) {
			List<Picture> pictures = contact.getPictures();
			// TODO use template call getDefault(contact.getPictures())
			if (pictures != null && pictures.size()>0)				
				pic = pictures.get(getIntParameterValue(V_INDEX, 0, 0));
		}
		try {
			if (pic != null) {
				resp.setContentType(pic.getType());
				/*
				 * if (1 = getIntParameterValue(V_DOWNLOAD, 0, 0)) {
				 * add download header
				 */
				Stream.copyStream(is = getAttachmentOperations().getAttachmentStream(pic),
						os = resp.getOutputStream());
				os.close();
				return null;
			} else {
				String picFile = getStringParameterValue(VALUE, null, 0);
				if (picFile != null) {
					resp.setContentType(frontController.getServlet().getServletContext().getMimeType(picFile));
					Stream.copyStream(is = getAttachmentOperations().getAttachmentStream(picFile),
							os = resp.getOutputStream());
					os.close();
					return null;
					
				}
			}
			// TODO use resource manager to access picture
			resp.setContentType("image/png");
			Stream.copyStream(is = frontController.getServletContext().getResourceAsStream("/image/"+"Nopicture.png"),
					os = resp.getOutputStream());
			os.close();
		} catch (IOException ioe) {
			try {
				resp.sendError(resp.SC_INTERNAL_SERVER_ERROR, ioe.toString());
			} catch (IOException ioe2) {
			}
		} finally {
			try {
				if (is != null)
					is.close();
			} catch (IOException e) {
			}
		}
		return null;
	}

	@Override
	protected Map doControl() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String getSubmitPage() {
		// TODO Auto-generated method stub
		return null;
	}

}
