/* jaddressbook - 
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
 *  $Id: IoHelper.java,v 1.5 2012/09/15 17:49:54 dmitriy Exp $
 * Created on Sep 19, 2005
 */
package addressbook.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Properties;

import org.aldan3.util.inet.HttpUtils;

import addressbook.AddressBookFrame;
import addressbook.servlet.model.UserOperations.DuplicateUser;

public class IoHelper {
	protected URL repositoryBase;

	public IoHelper(String rootName, String nodeName, Properties property) {
		try {
			File f = new File(rootName + File.separatorChar + AddressBookFrame.PROGRAMNAME + File.separatorChar
					+ nodeName);
			if (f.exists() == false)
				f.mkdirs();
			else if (f.isFile()) {
				// TODO log warning
				if (f.delete())
					f.mkdirs();
			}
			repositoryBase = f.toURI().toURL();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	public InputStream getInStream(String userName) throws IOException {
		// TODO: add authentication
		URLConnection uc = new URL(repositoryBase, URLEncoder.encode(HttpUtils.htmlEncode(userName), "utf-8"))
				.openConnection();
		if (uc instanceof HttpURLConnection) {
			HttpURLConnection htuc = (HttpURLConnection) uc;
			htuc.connect();
			if (htuc.getResponseCode() == HttpURLConnection.HTTP_OK)
				return htuc.getInputStream();
			else
				htuc.disconnect();
		} else
			return uc.getInputStream();
		return null;
	}

	public OutputStream getOutStream(String userName, boolean create) throws DuplicateUser, IOException {
		// TODO: check if URL http(s) based and use servlet to
		File pf = getFile(userName);
		if (pf != null) {
			if (pf.exists() && create)
				throw new DuplicateUser(userName);
			// System.err.println("Saved in "+pf+" for "+repositoryBase);
			return new FileOutputStream(pf);
		}
		return null;
	}

	public void backup(String userName) throws IOException {
		File tf = getFile(userName + ".bak");
		if (tf != null) {
			if (tf.exists())
				if (tf.delete() == false)
					throw new IOException("Can't delete previous backup file");
		} else
			throw new IOException ("Can't manage backup file");
		File sf = getFile(userName);
		if (sf != null && sf.exists() && sf.renameTo(tf) == false)
			throw new IOException("Can't rename " + sf + " to backup " + tf);
	}

	public boolean delete(String userName) {
		return delete(userName, false);
	}

	public boolean delete(String userName, boolean bak) {
		File sf = getFile(userName);
		if (sf != null && bak && sf.delete())
			sf = getFile(userName + ".bak");
		return sf != null && sf.delete();
	}

	public boolean createLock(String name) {
		File lock = getFile(name+".lck");
		if (lock == null)
			throw new RuntimeException("Inconsistent state, can't form lock file name");
		try {
			return lock.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public boolean releaseLock(String name) {
		File lock = getFile(name+".lck");
		if (lock == null)
			throw new RuntimeException("Inconsistent state, can't form lock file name");
		return lock.delete();
	}
	
	public File getFile(String name) {
		try {
			return new File(new URL(repositoryBase, URLEncoder.encode(HttpUtils.htmlEncode(name), "utf-8")).toURI());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return null;
	}
}
