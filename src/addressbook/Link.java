/* AddressBook - Link
 * Copyright (C) 1999 Dmitriy Rogatkin.  All rights reserved.
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
 * $Id: Link.java,v 1.3 2006/05/10 06:17:37 rogatkin Exp $
 */
package addressbook;

import java.io.OutputStream;
import java.io.IOException;

public class Link extends GenericAttribute {
		
	public Link(String _link, String _description) {
		super(_link, _description);
		type = "OTHER";
	}
	
	@Override
	public void update(Object [] params) {
		if (params == null || (params.length != 2))
			throw new IllegalArgumentException();
		value = (String)params[0];
		description = (String)params[1];
	}
	
	public String getNormalized() {
		if (value == null)
			return null;
		int cp = value.indexOf(':'); 
		if (cp < 0 || cp > 7)
			return "http://"+value;
		return value;
	}

	public void saveXML(OutputStream _out, String _enc, int _order) throws IOException {
		saveAsTag(_out, _enc, _order, DataBookIO.LINK_TAG);
	}
	public void saveVCard(OutputStream _out, String _enc, int order) throws IOException {
		// TBD:
	}
}