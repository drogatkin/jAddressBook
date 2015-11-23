/* jaddressbook - AttachmentOperations
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
 *  $Id: AttachmentOperations.java,v 1.5 2013/02/13 08:06:01 cvs Exp $
 * Created on Nov 14, 2005
 */
package addressbook.servlet.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Properties;

// import rogatkin.BaseController;

// import addressbook.Address;
import addressbook.AddressBookFrame;
import addressbook.Contact;
import addressbook.Picture;
import addressbook.servlet.AddressBookProcessor;

public class AttachmentOperations extends AbstractOperations {
	protected URL repositoryBase;

	protected URL rootBase;

	@Override
	public void init(AddressBookProcessor abp) {
		try {
			File f = new File(abp.getDataRoot() + File.separatorChar + AddressBookFrame.PROGRAMNAME
					+ File.separatorChar + "attachments" + File.separatorChar
					+ abp.getSession().getAttribute(AddressBookProcessor.HV_USER_ID));
			if (f.exists() == false)
				f.mkdirs();
			repositoryBase = f.toURI().toURL();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	public void init(Properties props) {
		// TODO if this init used, others shouldn't work
		try {
			File f = new File(AddressBookProcessor.getDataRoot(props.getProperty(AddressBookProcessor.DATAROOT))
					+ File.separatorChar + AddressBookFrame.PROGRAMNAME + File.separatorChar + "attachments");
			if (f.exists() == false)
				f.mkdirs();
			rootBase = f.toURI().toURL();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	public Picture connectAttachment(Picture picture, byte[] picBytes, String description, String type) {
		picture.setType(type == null ? "image" : type.startsWith("image")?"image":type);
		if (description != null)
			picture.setDescription(getLastName(description));
		OutputStream os = null;
		try {
			// BaseController.copyStream(
			// TODO consider it storing in XML attachment form like
			// 
			/*
			 * <soap:Envelope> <soap:Header> <xbinc:DoInclude soap:role='http://www.w3.org/2002/12/soap-envelope/role/next' soap:mustUnderstand='false'
			 * soap:relay='true' /> </soap:Header> <soap:Body> <m:data xmlns:m='http://example.org/stuff' > <m:photo xmime:MediaType='image/png' > /aWKKapGGyQ=
			 * </m:photo> </m:data> </soap:Body> </soap:Envelope>
			 */
			os = new FileOutputStream(new File(new URL(repositoryBase, URLEncoder.encode(picture.getValue(), "utf-8"))
					.toURI()));
			os.write(picBytes);
		} catch (IOException ioe) {
			ioe.printStackTrace();
			return null;
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return null;
		} finally {
			if (os != null)
				try {
					os.close();
				} catch (IOException ioe) {

				}
		}
		return picture;
	}

	public InputStream getAttachmentStream(Picture picture) throws IOException {
		try {
			return new FileInputStream(new File(new URL(repositoryBase, URLEncoder.encode(picture.getValue(), "utf-8"))
					.toURI()));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new IOException("" + e);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			throw new IOException("" + e);
		} catch (URISyntaxException e) {
			e.printStackTrace();
			throw new IOException("" + e);
		}
	}

	public InputStream getAttachmentStream(String fileName) throws IOException {
		fileName = fileName.replace('/', '-'); // to avoid simple hack
		if (fileName.length() == 0)
			throw new IllegalArgumentException("File name is not provided:"+fileName);
		try {
			return new FileInputStream(new File(new URL(repositoryBase, URLEncoder.encode(fileName, "utf-8")).toURI()));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new IOException("" + e);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			throw new IOException("" + e);
		} catch (URISyntaxException e) {
			e.printStackTrace();
			throw new IOException("" + e);
		}
	}

	public void detachAttachment(Contact contact, Picture picture) {
		try {
			// TODO consider like a transaction to avoid inconsistency
			File aFile = new File(new URL(repositoryBase, URLEncoder.encode(picture.getValue(), "utf-8")).toURI());
			if (aFile.exists() == false || aFile.delete())
				contact.remove(picture);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static String getLastName(String fullName) {
		if (fullName == null || fullName.length() < 2)
			return fullName;

		for (int i = fullName.length() - 2; i >= 0; i--) {
			char c = fullName.charAt(i);
			if (c == '/' || c == '\\' || c == ':')
				return fullName.substring(i + 1);
		}
		return fullName;
	}

	public void deleteAll(String user) {
		// TODO Auto-generated method stub
		try {
			File userDir = new File(new URL(rootBase, user).toURI());
			if (userDir.exists() && userDir.isDirectory()) {
				File[] userAtts = userDir.listFiles();
				for (File file : userAtts)
					if (file.delete() == false)
						System.err.println("AttachmentOperations.deleteAll() Couldn't delete:" + file);
				if (userDir.delete() == false)
					System.err.println("AttachmentOperations.deleteAll() Couldn't delete:" + userDir);
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
