/* jaddressbook - Media.java
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
 *  $Id: Media.java,v 1.8 2007/02/09 07:27:49 rogatkin Exp $                
 *  Created on Sep 6, 2006
 *  @author Dmitriy
 */
package addressbook;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.aldan3.util.inet.HttpUtils;

/**
 * This class used to define media items as movie, song and others
 * 
 * @author Dmitriy
 * 
 */
public class Media extends GenericAttribute {
	@propertyname
	public static final String TITLE = VALUE;

	@propertyname()
	public static final String DIRECTOR = "director";

	@propertyname
	public static final String GENRE = TYPE;

	@propertyname
	public static final String ABOUT = DESCRIPTION;

	@propertyname
	public static final String YEAR = "year";

	@propertyname
	public static final String STAR_ACTOR = "star actor";

	@propertyname
	public static final String PERFORMER = "performer";

	@propertyname
	public static final String COMPOSER = "composer";

	@propertyname
	public static final String WRITER = "writer";

	@propertyname
	public static final String FORMAT = "format";

	@propertyname
	public static final String QUALITY = "quality";

	@propertyname
	public static final String RATING = "rating";

	@propertyname
	public static final String RATE = "rate";

	@propertyname
	public static final String PUBLISHER = "publisher";

	public static final String STUDIO = PUBLISHER;

	@propertyname
	public static final String DURATION = "duration";

	@propertyname
	public static final String COUNTRY_OF_ORIGIN = "country";

	public static final String MEDIA_TAG = "MEDIA";

	private Map<String, Object> values;
	
	private int id;

	public Media(String _value, String _description, String _type) {
		super(_value, _description, _type);
		values = new HashMap<String, Object>();
		id = -1;
	}

	public String getTitle() {
		return getValue();
	}

	public void saveVCard(OutputStream _out, String _enc, int _order) throws IOException {
		// TODO Auto-generated method stub

	}

	public void saveXML(OutputStream _out, String _enc, int _order) throws IOException {
		_out.write(("<MEDIA ORDER=\"" + _order + "\"  COMMENT=\"").getBytes(_enc));
		_out.write(HttpUtils.htmlEncode(description).getBytes(_enc));
		_out.write("\">".getBytes(_enc));
		for (String attr : buildCollectionFieldNames(this.getClass())) {
			Object attrVal = getAttribute(attr);
			if (attrVal != null)
				saveSimpleTag(_out, _enc, DataBookIO.assureTagName(attr.toUpperCase()), attrVal.toString());
		}
		_out.write("</MEDIA>".getBytes(_enc));
	}

	public Object getAttribute(String attr) {
		if (TITLE.equals(attr))
			return getValue();
		else if (TYPE.equals(attr))
			return this.getType();
		else if (DESCRIPTION.equals(attr))
			return this.getDescription();
		else if (attr != null)
			return values.get(DataBookIO.assureTagName(attr).toLowerCase());
		else
			return null;
	}

	public void setAttribute(String _tag, Object _value) {
		assert isValidAttributeName(_tag);
		if (TITLE.equalsIgnoreCase(_tag))
			setValue((String) _value);
		else if (TYPE.equalsIgnoreCase(_tag))
			setType((String) _value);
		else if (DESCRIPTION.equalsIgnoreCase(_tag))
			setDescription((String) _value);
		else
			values.put(DataBookIO.assureTagName(_tag).toLowerCase(), _value);
	}
	
	public int getId() {
		return id;
	}

	public boolean isValidAttributeName(String _name) {
		return true;
	}

	public void setId(int lastIndex) {
		id = lastIndex;
	}

}
