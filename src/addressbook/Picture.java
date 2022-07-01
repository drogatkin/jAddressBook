/* AddressBook - Picture
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
 *  $Id: Picture.java,v 1.2 2013/02/13 03:23:35 cvs Exp $
 */
package addressbook;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import addressbook.servlet.model.AttributeStorage;
import static addressbook.DataBookIO.*;

// TODO extend from Attachment, assure type == image
public class Picture extends GenericAttribute implements XMLSaver, AttributeStorage {
	public static final String LOCATION = "location";
	
	public Picture(String url, String description, String type) {
		super(url, description, type);
	}
	
	@Override
	public void update(Object [] params) {
		if (params == null || (params.length != 3))
			throw new IllegalArgumentException();
		value = (String)params[0];
		description = (String)params[1];
		type = (String)params[2];
	}

	public void saveXML(OutputStream _out, String _enc, int _order) throws IOException {		
		saveAsTag(_out, _enc, _order, PICTURE_TAG);
	}

	public void saveVCard(OutputStream _out, String _enc, int _order) throws IOException {
		// TODO Auto-generated method stub

	}

	public String getStringAttribute(String attrName) {
		if (LOCATION.equals(attrName) || VALUE.equals(attrName))
			return value;
		else if (TYPE.equals(attrName))
			return type;
		else if (DESCRIPTION.equals(attrName))
			return description;
		
		return null;
	}

	public void setStringAttribute(String attrName, String attrValue) {
		if (LOCATION.equals(attrName) || VALUE.equals(attrName))
			value = attrValue;
		else if (TYPE.equals(attrName))
			type = attrValue;
		else if (DESCRIPTION.equals(attrName))
			description = attrValue;
	}

	public Object getAttribute(String attrName) {
		if (LOCATION.equals(attrName))
			try {
				return new URL(value);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch(NullPointerException npe) {
				
			}
		return null;
	}

	public void setAttribute(String attrName, Object attrValue) {
		if (LOCATION.equals(attrName))
			value = attrValue==null?null:attrValue.toString();
	}

	public String toXML() {
		// TODO look into SOAP attachments
		return null;
	}

	public void saveXML(OutputStream os) throws IOException {
		
	}

	public void fromXML(InputStream in) throws IOException {
		
	}

	public void copyResultMap(Map<String, Object> result, String... strings) {
		
	}

}
