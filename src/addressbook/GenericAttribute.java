/* AddressBook - GenericAttribute
 * Copyright (C) 2000 Dmitriy Rogatkin.  All rights reserved.
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
 *  $Id: GenericAttribute.java,v 1.11 2012/10/12 03:53:55 cvs Exp $
 */
package addressbook;
import java.io.OutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.aldan3.util.inet.HttpUtils;

abstract public class GenericAttribute implements XMLSaver {
	public static final String VALUE = "value";
	public static final String DESCRIPTION = "description";
	public static final String TYPE = "type";
	static final String EMPTY = "";
	static final String CRLF = "\r\n";

	protected String value, description, type;
	
	protected boolean preferable;
	
	protected GenericAttribute(String _value, String _description, String _type) {
		value = assignWithDefault(_value);
		description = assignWithDefault(_description);
		//type = assignWithDefault(_type);
		type = _type;
	}

	protected GenericAttribute(String _value, String _description) {
		value = assignWithDefault(_value);
		description = assignWithDefault(_description);
	}

	protected GenericAttribute(String _description) {
		description = assignWithDefault(_description);
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String _description) {
		description = _description;
	}

	public String getShortDescription() {
		if (description.length() > 10)
			return description.substring(0,9)+'\u00BB';
		return description;
	}
	
	public String getValue() {
		return value;
	}

	public void setValue(String _value) {
		value = _value;
	}

	public String getNormalized() {
		return getValue();
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String _type) {
		type = _type;
	}
	
	public boolean isEmpty() {
		return value == null || value.trim().length() == 0;
	}
	
	@Override
	public boolean equals(Object _o) {
		if (_o instanceof GenericAttribute)
			return getNormalized().equals(((GenericAttribute)_o).getNormalized());
		return false;
	}
	
	public boolean equalsToUpdate(Object _o) {
		return equals(_o);
	}
	
	public boolean isPreferable() {
		return preferable;
	}
	
	public void setPreferable(boolean on) {
		preferable = on;
	}
	
	@Override
	public int hashCode() {
		return getNormalized().hashCode();
	}
	
	@Override
	public String toString() {
		return getNormalized();
	}

	protected String assignWithDefault(String _s) {
		if (_s == null)
			return EMPTY;
		return _s.trim();
	}

	public static void saveSimpleTag(OutputStream _out, String _enc, String _tag, String _value) throws IOException, UnsupportedEncodingException  {
		saveSimpleTag(_out, _enc, _tag, _value, null);
	}
	
	public static void saveSimpleTag(OutputStream _out, String _enc, String _tag, String _value, Map attributes) throws IOException, UnsupportedEncodingException {
		_out.write("<".getBytes(_enc));
		_out.write(_tag.getBytes(_enc));
		if (attributes != null)
			_out.write(DataBookIO.formAttributeString(attributes).getBytes(_enc));
		_out.write(">".getBytes(_enc));
		_out.write(HttpUtils.htmlEncode(_value).getBytes(_enc));
		_out.write("</".getBytes(_enc));
		_out.write(_tag.getBytes(_enc));
		_out.write(">".getBytes(_enc));
	}

	protected void saveAsTag(OutputStream _out, String _enc, int _order, String _tag) throws IOException {
		_out.write(("<"+_tag+" ORDER=\""+_order+"\"  TYPE=\""+type+"\" COMMENT=\"").getBytes(_enc));
		_out.write(HttpUtils.htmlEncode(description).getBytes(_enc));
		_out.write(("\" PREFERABLE=\""+isPreferable()).getBytes(_enc));
		_out.write("\">".getBytes(_enc));
		_out.write(HttpUtils.htmlEncode(value).getBytes(_enc));
		_out.write(("</"+_tag+">").getBytes(_enc));
	}
	
	public static String [] buildCollectionFieldNames(Class cl) {
		List<String> result = new ArrayList<String>(10);
		for(Field field:cl.getFields()) {
			if (field.getAnnotation(propertyname.class) != null)
				try {
					result.add((String)field.get(null));
				} catch (IllegalArgumentException e) {
				} catch (IllegalAccessException e) {
				}
		}
		return (String[])result.toArray(new String[result.size()]);
	}

}
