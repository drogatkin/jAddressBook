/* AddressBook - EMail
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

import org.aldan3.util.inet.HttpUtils;

public class EMail extends GenericAttribute {

	public EMail(String _email, String _description, String _type) {
		super(_email, _description, _type);
	}

	public EMail(String _email, String _description) {
		super(_email, _description);
	}
	
	@Override
	public void update(Object [] params) {
		if (params == null || (params.length != 3))
			throw new IllegalArgumentException();
		value = (String)params[0];
		description = (String)params[1];
		type = (String)params[2];
	}

	public void saveXML(OutputStream _out, String _enc, int order) throws IOException, UnsupportedEncodingException {
		_out.write(("<EMAIL ORDER=\"" + order + "\"  TYPE=\""+(type==null?"GENERIC":type)+"\" COMMENT=\"").getBytes(_enc));
		_out.write(HttpUtils.htmlEncode(description).getBytes(_enc));
		_out.write(("\" PREFERABLE=\""+isPreferable()).getBytes(_enc));
		_out.write("\">".getBytes(_enc));
		_out.write(HttpUtils.htmlEncode(value).getBytes(_enc));
		_out.write("</EMAIL>".getBytes(_enc));
	}

	public void saveVCard(OutputStream _out, String _enc, int order) throws IOException {
		if (_enc != null)
			_out.write(("EMAIL;"+type+":" + value + CRLF).getBytes(_enc));
		else
			_out.write(("EMAIL;"+type+":" + value + CRLF).getBytes());
	}
}