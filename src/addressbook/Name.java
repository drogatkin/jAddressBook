/* AddressBook - Name
 * Copyright (C) 2000-2005 Dmitriy Rogatkin.  All rights reserved.
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
 *  $Id: Name.java,v 1.7 2013/02/19 05:41:21 cvs Exp $
 */
package addressbook;
import java.util.Locale;
import java.util.StringTokenizer;
import java.io.OutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;

public class Name extends GenericAttribute {
	// salutation can be enumeration but specific for different
	// languages and countries
	String first, middle, major, maiden, nick;
	/** creates a new name
	 * @param _last - provides last name
	 * @param _first - provides first name
	 */
	public Name(
		String _last,
		String _first,
		String _middle,
		String _salut,
		String _maiden,
		String _major,
		String _nick) {
		super(_last, _salut); // name preffix
		first = assignWithDefault(_first);
		middle = assignWithDefault(_middle);
		major = assignWithDefault(_major); // name suffix
		maiden = assignWithDefault(_maiden);
		nick = assignWithDefault(_nick);
	}

	public Name(String _format) throws ParseException {
		this(null, null, null, null, null, null, null);
		parseName(_format);
	}

	public String getLast() {
		return value;
	}

	public String getFirst() {
		return first;
	}

	public String getMiddle() {
		return middle;
	}

	public String getMajor() {
		return major;
	}
	
	public Object getId() {
		return getLast();
	}

	public boolean equals(Object _name) {
		if (_name instanceof Name) {
			Name name = (Name) _name;
			return value.equalsIgnoreCase(name.getLast())
				&& first.equalsIgnoreCase(name.getFirst())
				&& middle.equalsIgnoreCase(name.getMiddle())
				&& major.equalsIgnoreCase(name.getMajor());
		}
		return false;
	}

	public int hashCode() {
		return (value.toUpperCase() + first.toUpperCase() + middle.toUpperCase() + major.toUpperCase()).hashCode();
	}

	public String toString() {
		if (Locale.getDefault().equals(Locale.US))
			return first
				+ ' '
				+ value
				+ (middle.length() > 0 ? " "+middle.charAt(0) : "")
				+ (description != null && description.length() > 0?", "+description:"" )
				+ (nick != null && nick.length() > 0?" ("+nick
						+ ')':"");
		return (description != null && description.length() > 0?description:"")
		     + (value != null && value.length() >0?' '+value :"") 
		     + (first != null && first.length() >0?", "+first:"")
		     + (middle.length() > 0 ? " "+middle.charAt(0) : "") + (nick != null && nick.length() > 0?" ("+nick
						+ ')':"");
	}

	public void parseName(String _format) throws ParseException {
		StringTokenizer st = new StringTokenizer(_format.trim(), " ,()", true);
		if (st.hasMoreElements()) {
			first = st.nextToken();
			if (st.hasMoreElements()) {
				value = st.nextToken();
				if (value.indexOf(' ') == 0 && st.hasMoreElements()) {
					value = st.nextToken();
				}
				if (st.hasMoreElements()) {
					middle = st.nextToken();
					if (middle.indexOf(' ') == 0 && st.hasMoreElements()) 
						middle = st.nextToken();
				}

				if (st.hasMoreElements()) {
					middle = st.nextToken();
					if (middle.indexOf(' ') == 0 && st.hasMoreElements()) 
						middle = st.nextToken();
				}

			} else {
				value = first;
				first = EMPTY;
			}
		} else
			throw new ParseException("Empty string", 0);

	}

	public void saveXML(OutputStream _out, String _enc, int order) throws IOException, UnsupportedEncodingException {
		_out.write("<NAME>".getBytes(_enc));
		saveSimpleTag(_out, _enc, "FIRST", first);
		saveSimpleTag(_out, _enc, "MIDDLE", middle);
		saveSimpleTag(_out, _enc, "LAST", value);
		saveSimpleTag(_out, _enc, "MAIDEN", maiden);
		saveSimpleTag(_out, _enc, "MAJOR", major);
		saveSimpleTag(_out, _enc, "SALUT", description);
		saveSimpleTag(_out, _enc, "NICK", nick);
		_out.write("</NAME>".getBytes(_enc));
	}

	public void saveVCard(OutputStream _out, String _enc, int order) throws IOException {
		if (_enc != null)
			_out.write(
				(
					"N;CHARSET="
						+ _enc
						+ ":"
						+ value
						+ ';'
						+ first
						+ ';'
						+ middle
						+ ';'
						+ description
						+ ';'
						+ major
						+ CRLF).getBytes(
					_enc));
		else
			_out.write(
				("N;CHARSET="
					+ _enc
					+ ":"
					+ value
					+ ';'
					+ first
					+ ';'
					+ middle
					+ ';'
					+ description
					+ ';'
					+ major
					+ CRLF)
					.getBytes());
	}
}
