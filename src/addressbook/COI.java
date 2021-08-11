/* jaddressbook - LOI.java
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
 *  $Id: COI.java,v 1.6 2007/02/09 07:27:49 rogatkin Exp $                
 *  Created on Sep 21, 2006
 *  @author Dmitriy
 */
package addressbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;

import org.aldan3.util.inet.HttpUtils;
import org.xml.sax.AttributeList;
import org.xml.sax.SAXException;


import addressbook.servlet.model.AbstractAttributeStorage;

/**
 * Placeholder for keeping list of items, like movies, songs, books and so on
 * 
 * @author Dmitriy
 * 
 */
public class COI extends AbstractAttributeStorage implements XMLSaver {

	protected Collection<Media> items;
	
	private Media lastMedia; // parsing business
	
	private int lastIndex;

	public COI() {
		items = new ArrayList<Media>();
	}
	
	public Collection<Media> getItems() {
		return items;
	}

	public void addItem(Media media) {
		media.setId(++lastIndex);
		items.add(media);
	}

	public void deleteItem(Media media) {
		items.remove(media);
	}

	public Media getItem(int id) {
		for (Media m : items) {
			if (m.getId() == id)
				return m;
		}
		return null;
	}
	
	public Collection<Media> getItemsLike(Media media) {
		Collection<Media> result = new ArrayList<Media>(items.size());
		for (Media m : items) {
			// TODO check for match to media
			result.add(m);
		}
		return result;
	}

	public void saveVCard(OutputStream _out, String _enc, int _order) throws IOException {
		// TODO Auto-generated method stub

	}

	public void saveXML(OutputStream _out, String _enc, int _order) throws IOException {
		if (getId() != null)
			_out.write(toXML().getBytes(_enc));
	}

	@Override
	public Object getId() {
		return getAttribute(GenericAttribute.VALUE);
	}
	
	@Override
	public int hashCode() {
		String name = (String)getId();
		return name != null ? name.hashCode() : super.hashCode();
	}


	@Override
	public String toXML() {
		try {
			ByteArrayOutputStream os = new java.io.ByteArrayOutputStream();
			os.write("<COLLECTION".getBytes("utf-8"));
			String value = getStringAttribute(GenericAttribute.VALUE);
			if (value != null) {
				os.write(" VALUE=\"".getBytes("utf-8"));
				os.write(HttpUtils.htmlEncode(value).getBytes("utf-8"));
				os.write('"');
			}
			String comment = getStringAttribute(GenericAttribute.DESCRIPTION);
			if (value != null) {
				os.write(" COMMENT=\"".getBytes("utf-8"));
				os.write(HttpUtils.htmlEncode(comment).getBytes("utf-8"));
				os.write('"');
			}
			if (getOwner() != null) {
				os.write((" "+DataBookIO.SHARED_BY_ATTR+"=\"").getBytes("utf-8"));
				os.write(HttpUtils.htmlEncode(getOwner()).getBytes("utf-8"));
				os.write('"');
			}
			os.write('>');
			int c = 0;
			for (Media media : getItemsLike(null)) {
				media.saveXML(os, "utf-8", ++c);
			}
			os.write("</COLLECTION>".getBytes("utf-8"));
			return os.toString("utf-8");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public void startElement(String _tag, AttributeList _attrs) throws SAXException {
		// TODO check if tag is allowed, otherwise rise SAXException
		if (Media.MEDIA_TAG.equals(_tag)) 
			lastMedia = new Media(null, null, null);
	}

	public void endElement(String _tag, StringBuffer _buffer) throws SAXException {
		if (Media.MEDIA_TAG.equals(_tag)) {
			addItem(lastMedia);
			lastMedia = null;
		} else if (lastMedia != null)
			lastMedia.setAttribute(_tag, _buffer.toString());
	}

	@Override
	public String toString() {
		if (getId() != null)
			return getId().toString();
		return super.toString();
	}
}
