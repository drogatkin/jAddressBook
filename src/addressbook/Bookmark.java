/* AddressBook - Bookmark
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
 * $Id: Bookmark.java,v 1.3 2007/02/09 07:27:49 rogatkin Exp $
 */

package addressbook;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
public class Bookmark extends Link { 
	Date visited;
	int length;
	Bookmark(String _link, String _description, Date _visited, int _length) {
		super(_link, _description);
		visited = _visited;
		length = _length;
		type = "OTHER";
	}

	public void saveXML(OutputStream _out, String _enc, int _order) throws IOException {
		_out.write("<BOOKMARK".getBytes(_enc));
		if (visited != null) {
			_out.write(" VISITED=\"".getBytes(_enc));
			_out.write(visited.toGMTString().getBytes(_enc));
			_out.write("\"".getBytes(_enc));
		}
		if (length > 0) {
			_out.write(" CONTENT_LENGTH=\"".getBytes(_enc));
			_out.write(Integer.toString(length).getBytes(_enc));
			_out.write("\"".getBytes(_enc));
		}
		_out.write(">".getBytes(_enc));
		super.saveAsTag(_out, _enc, _order, DataBookIO.LINK_TAG);
		_out.write("</BOOKMARK>" .getBytes(_enc));
	}

}
