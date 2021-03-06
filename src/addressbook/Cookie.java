/* AddressBook - Cookie
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
 */
package addressbook;
import java.io.OutputStream;
import java.io.IOException;
import java.util.Date;

public class Cookie extends GenericAttribute {
	
	// add domain, exp
	String domain, purpose;
	Date expire;
	public Cookie(String _value, String _name, String _domain, String _purpose, Date _exp) {
		super(_value, _name);
		type = "COOKIE";
		domain = _domain;
		purpose = _purpose;
		expire = _exp;
	}
	@Override
	public void update(Object [] params) {
		if (params == null || (params.length != 5))
			throw new IllegalArgumentException();
		value = (String)params[0];
		description = (String)params[1];
		domain = (String)params[2];
		purpose = (String)params[3];
		expire = (Date)params[4];
	}

	public void saveXML(OutputStream _out, String _enc, int _order) throws IOException {
		saveAsTag(_out, _enc, _order, DataBookIO.COOKIE_TAG);
	}
	public void saveVCard(OutputStream _out, String _enc, int order) throws IOException {
		// TBD:
	}
}
