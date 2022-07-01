/* AddressBook - Address
 * Copyright (C) 1999-2000 Dmitriy Rogatkin.  All rights reserved.
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
 */
package addressbook;

import java.io.OutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import org.aldan3.util.inet.HttpUtils;

public class Address extends GenericAttribute {
	public String city, state, country, zip, title;

	public Address(String rawAddr, String description, String title) {
		super(null, description);
		update(new String[] {rawAddr, description, title});
	}

	public Address(String _street, String _city, String _state, String _country, String _zip, String _description,
			String _title) {
		super(_street, _description);
		city = assignWithDefault(_city);
		state = assignWithDefault(_state);
		country = assignWithDefault(_country);
		zip = assignWithDefault(_zip);
		title = assignWithDefault(_title);
	}
	
	@Override
	public void update(Object [] params) {
		// String name, String description, String password, String account, String url
		if (params == null || (params.length != 3 && params.length != 7 ))
			throw new IllegalArgumentException();
		if (params.length == 3) {
			description = (String)params[1];
			StringTokenizer st = new StringTokenizer((String)params[0], "");
			String line2 = null;
			try {
				value = st.nextToken("\n").trim();
				line2 = st.nextToken().trim();
				city = st.nextToken(",\n");
				state = st.nextToken(" ,");
				zip = st.nextToken(", ");
				value += ", "+line2.trim();
				line2 = null;
				country = st.nextToken("\r\n");
			} catch (NoSuchElementException nsee) {
				if (line2 != null) {
					st = new StringTokenizer(line2, "");
					city = st.nextToken(",\n");
					if (st.hasMoreTokens()) {
						state = st.nextToken(" ,");
						if (st.hasMoreTokens())
							zip = st.nextToken(", ");
					}
				}
			}
			city = assignWithDefault(city);
			state = assignWithDefault(state);
			country = assignWithDefault(country);
			zip = assignWithDefault(zip);
			this.title = assignWithDefault((String)params[2]);
		} else if (params.length == 7) {
			throw new IllegalArgumentException();
		}
	}

	public String getFormated() {
		return "" + value + '\n' + city + ", " + state + ' ' + zip
				+ (country != null && country.length() > 0 ? ", " : "") + country;
	}

	public void saveXML(OutputStream _out, String _enc, int order) throws IOException, UnsupportedEncodingException {
		_out.write(("<ADDRESS ORDER=\"" + order + "\"  TYPE=\"OTHER\" COMMENT=\"").getBytes(_enc));
		_out.write(HttpUtils.htmlEncode(description).getBytes(_enc));
		_out.write("\">".getBytes(_enc));
		_out.write("<TITLE>".getBytes(_enc));
		_out.write(HttpUtils.htmlEncode(title).getBytes(_enc));
		_out.write("</TITLE>".getBytes(_enc));
		_out.write("<STREET>".getBytes(_enc));
		_out.write(HttpUtils.htmlEncode(value).getBytes(_enc));
		_out.write("</STREET>".getBytes(_enc));
		_out.write("<CITY>".getBytes(_enc));
		_out.write(HttpUtils.htmlEncode(city).getBytes(_enc));
		_out.write("</CITY>".getBytes(_enc));
		_out.write("<STATE>".getBytes(_enc));
		_out.write(HttpUtils.htmlEncode(state).getBytes(_enc));
		_out.write("</STATE>".getBytes(_enc));
		_out.write("<COUNTRY>".getBytes(_enc));
		_out.write(HttpUtils.htmlEncode(country).getBytes(_enc));
		_out.write("</COUNTRY>".getBytes(_enc));
		_out.write("<ZIP>".getBytes(_enc));
		_out.write(zip.getBytes(_enc));
		_out.write("</ZIP>".getBytes(_enc));
		_out.write("</ADDRESS>".getBytes(_enc));
	}

	public void saveVCard(OutputStream _out, String _enc, int order) throws IOException {
		if (_enc != null)
			_out.write(("ADR;CHARSET=" + _enc + (country == null ? ";DOM:" : ";INTL:") + value + ';' + city + ';'
					+ state + ';' + zip + ';' + CRLF).getBytes(_enc));
		else
			_out.write(("ADR;CHARSET=" + _enc + ":" + value + ';' + city + ';' + state + ';' + zip + ';' + CRLF)
					.getBytes());
	}
}