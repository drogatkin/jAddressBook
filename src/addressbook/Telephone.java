/* AddressBook - Telephone
 * Copyright (C) 1999-2004 Dmitriy Rogatkin.  All rights reserved.
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
 * $Id: Telephone.java,v 1.6 2007/02/09 07:27:49 rogatkin Exp $
 */
package addressbook;

import java.io.OutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class Telephone extends GenericAttribute {
	public Telephone(String _telephone, String _description) {
		super(_telephone, _description);
	}

	public Telephone(String _telephone, String _description, String _type) {
		super(_telephone, _description, _type);
	}
	
	@Override
	public void update(Object [] params) {
		if (params == null || (params.length != 3))
			throw new IllegalArgumentException();
		value = (String)params[0];
		description = (String)params[1];
		type = (String)params[2];
	}

	public String getNormalized() {
		// TODO: note that normalizer provides phone format US specific
		// generally format should be taken from locale settings
		String s = getValue();
		if (s == null)
			return null;
		// remove not numbers
		char[] ca = s.toCharArray();
		int ln = -1;
		int x = -1;
		for (int i = 0; i < ca.length; i++) {
			if (Character.isDigit(ca[i])) {
				ln++;
				if (ln != i)
					ca[ln] = ca[i];
			} else if (ca[i] == 'x' || ca[i] == 'X') { // cut extension
				x = i;
				break;
			}
		}

		if (ln >= 6) {
			StringBuffer result = new StringBuffer(ln + 3);
			if (ln > 9)
				result.append(ca, 0, ln - 9);
			if (ln > 6)
				result.append('(').append(ca, Math.max(ln - 9, 0), Math.min(ln - 6, 3)).append(')');
			result.append(ca, ln - 6, 3).append('-').append(ca, ln - 3, 4);
			if (x > 0)
				result.append(s.substring(x));
			return result.toString();
		}
		if (ln > 0)
			return new String(ca, 0, ln);
		return "";
	}

	public void saveXML(OutputStream _out, String _enc, int _order) throws IOException,
			UnsupportedEncodingException {
		saveAsTag(_out, _enc, _order, DataBookIO.PHONE_TAG);
	}

	public void saveVCard(OutputStream _out, String _enc, int order) throws IOException {
		if (_enc != null)
			_out.write(("TEL;" + type + ":" + value + CRLF).getBytes(_enc));
		else
			_out.write(("TEL;" + type + ":" + value + CRLF).getBytes());
	}

}