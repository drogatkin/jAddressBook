/* AddressBook - Chat
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
import java.io.UnsupportedEncodingException;

import org.aldan3.util.inet.HttpUtils;

public class Chat extends GenericAttribute {
	public final static String HOST = "host";
	public final static String ROOM = "room";
	public static final int IRC_CHAT = 1;
	public static final int ICQ_CHAT = 2;
	public static final int MSNGR_CHAT = 3;
	public static final int MSNGR_FB = 4;
	public static final int MSNGR_TWITR = 5;
	public static final int MSNGR_LNKDIN = 6;
	public static final int MAX_CHAT_TYPE = MSNGR_LNKDIN;
	public static final String [] MNEMO = {"IRC", "ICQ", "MSNGR", "FB", "TWITR", "LNKDIN"};
	
	protected String host, room;
	
	public Chat(String _chatId, String _chatHost, int _chatType, String _comment, String _room) {
		super(_chatId, _comment);
		host = assignWithDefault(_chatHost);
		room = assignWithDefault(_room);
		if (_chatType < IRC_CHAT || _chatType > MAX_CHAT_TYPE)
			type = MNEMO[0];
			else
			type = MNEMO[_chatType-1];
	}
	
	@Override
	public void update(Object [] params) {
		// String name, String description, String password, String account, String url
		if (params == null || (params.length != 5))
			throw new IllegalArgumentException();
		value = (String)params[0];
		host = assignWithDefault((String)params[1]);
		room = assignWithDefault((String)params[4]);
		if ((Integer)params[2] < IRC_CHAT || (Integer)params[2] > MAX_CHAT_TYPE)
			type = MNEMO[0];
			else
			type = MNEMO[(Integer)params[2]-1];
		description = (String)params[3];
	}
	
	public String getHost() {
		return host;
	}
	
	public String getRoom() {
		return room;
	}
	
	public int getTypeInt() {
		return type2Int(type);
	}
	
	public static final int type2Int(String type) {
		if (type == null)
			return -1;
		int i=1;
		for(String t:MNEMO)
			if (t.equals(type))
				return i;
			else
				i++;
		return -1;
	}
	
	public void saveXML(OutputStream _out, String _enc, int order) throws IOException, UnsupportedEncodingException {
		_out.write(("<CHAT ORDER=\""+order+"\" COMMENT=\"").getBytes(_enc));
		_out.write(HttpUtils.htmlEncode(description).getBytes(_enc));
		_out.write("\" ROOM=\"".getBytes(_enc));
		_out.write(HttpUtils.htmlEncode(room).getBytes(_enc));
		_out.write("\" SERVER=\"".getBytes(_enc));
		_out.write(HttpUtils.htmlEncode(host).getBytes(_enc));
		_out.write("\" PROTOCOL=\"".getBytes(_enc));
		_out.write(type.getBytes(_enc));
		_out.write("\">".getBytes(_enc));
		_out.write(HttpUtils.htmlEncode(value).getBytes(_enc));
		_out.write("</CHAT>".getBytes(_enc));
	}
	public void saveVCard(OutputStream _out, String _enc, int order) throws IOException {
		// TBD:
	}
}
