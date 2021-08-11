/* jaddressbook - AbstractAttributeStorage.java
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
 *  $Id: AbstractAttributeStorage.java,v 1.4 2006/11/30 05:02:17 rogatkin Exp $
 * Created on Sep 19, 2005
 */

package addressbook.servlet.model;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Properties;

public abstract class AbstractAttributeStorage implements AttributeStorage {
	private Properties properties = new Properties();

	private transient boolean checked;

	private String owner;

	public String getStringAttribute(String attrName) {
		return properties.getProperty(attrName);
	}

	public Object getAttribute(String attrName) {
		return properties.get(attrName);
	}

	public long getLongAttribute(String attrName, long nullValue) {
		long result = nullValue;
		try {
			result = Long.parseLong((String) properties.get(attrName));
		} catch (NullPointerException npe) {
			// defaulting
		}
		return result;
	}

	public void setAttribute(String attrName, Object attrValue) {
		if (attrValue != null)
			properties.put(attrName, attrValue);
		else
			properties.remove(attrName);
	}

	public void setStringAttribute(String attrName, String attrValue) {
		if (attrValue != null)
			properties.setProperty(attrName, attrValue);
		else
			properties.remove(attrName);
	}

	public String toXML() {
		try {
			ByteArrayOutputStream baos;
			properties.storeToXML(baos = new ByteArrayOutputStream(), "", "UTF-8");
			return baos.toString("UTF-8");
		} catch (IOException ioe) {
		}
		return null;
	}

	public void saveXML(OutputStream os) throws IOException {
		properties.storeToXML(os, "Issued by attribute storage");
	}

	public void copyResultMap(Map<String, Object> result, String... strings) {
		for (String attrName : strings)
			result.put(attrName, getAttribute(attrName));
	}

	public void fromXML(InputStream in) throws IOException {
		properties.loadFromXML(in);
	}

	public boolean isChecked() {
		return this.checked;
	}

	public void setChecked(boolean on) {
		this.checked = on;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public abstract Object getId();
}
