/* jaddressbook - 
 * Copyright (C) 1999-2005 Dmitriy Rogatkin.  All rights reserved.
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
 *  $Id: LogOperations.java,v 1.2 2006/11/09 09:04:34 rogatkin Exp $
 * Created on Sep 19, 2005
 */
package addressbook.servlet.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import addressbook.GenericAttribute;
import addressbook.servlet.AddressBookProcessor;
import addressbook.servlet.model.UserOperations.DuplicateUser;
import addressbook.util.IoHelper;
import addressbook.util.Saxable;
import addressbook.util.XMLProcessor;

public class LogOperations extends AbstractOperations {
	private static final int LOGDEEPTH = 5;

	protected Map<String, List<LogRecord>> logs;

	protected IoHelper logsRepositoryBase;

	public enum LogTypes {
		Signin, Signout, Addrec, Deletrec, Updaterec
	};

	public void logLogin(String user, /* long time, */String ip_host, String userAgent) {
		addLog(user, new LogRecord(ip_host, userAgent, LogTypes.Signin.toString()));
	}

	public void logLogOut(String user/* , long time */) {
		addLog(user, new LogRecord(null, null, LogTypes.Signout.toString()));
		unload(user);
	}

	public void logAddContact(String user, String contactName) {
		addLog(user, new LogRecord(null, null, LogTypes.Addrec.toString()));
	}
	
	public Date getLastLogin(String user) {
		List<LogRecord> l = getLog(user);
		if (l.size() > 0)
			return new Date(l.get(l.size()-1).getTime());
		return null;
	}

	@Override
	public void init(AddressBookProcessor abp) {
		// TODO: use cache with possible unload logs or WeakHashMap may not work here
		logs = new HashMap<String, List<LogRecord>>();
		logsRepositoryBase = new IoHelper(abp.getDataRoot(), "logs", abp.getProperties());
	}
	
	protected void delete(String user) {
		logsRepositoryBase.delete(user);
	}

	protected void addLog(String user, LogRecord rec) {
		List<LogRecord> userLog = getLog(user);
		if (userLog != null)
			synchronized (this) {
				userLog.add(rec);
			}
		else
			System.err.println("No log found for : " + user);
	}

	protected List<LogRecord> getLog(String user) {
		List<LogRecord> result = logs.get(user);
		if (result != null)
			return result;
		synchronized (this) {
			result = logs.get(user);
			if (result != null)
				return result;
			result = loadLog(user);
			if (result == null)
				result = new ArrayList<LogRecord>();
			logs.put(user, result);
		}
		return result;
	}

	protected void unload(String user) {		
		if (user != null) {
			saveLog(user);
			synchronized (this) {
				logs.remove(user);
			}
		} else {
			synchronized (this) {
				System.err.printf("Users: %d to unload()\n", logs.size());
				Iterator<Entry<String, List<LogRecord>>> lei = logs.entrySet().iterator();
				while(lei.hasNext()) {
					Entry<String, List<LogRecord>> e = lei.next();
					saveLog(e.getKey());
					lei.remove();
				}
			}
		}
	}

	protected List<LogRecord> loadLog(String user) {
		List<LogRecord> result = null;
		InputStream is = null;
		try {
			is = logsRepositoryBase.getInStream(user);
			XMLProcessor<List<LogRecord>> xp = new XMLProcessor<List<LogRecord>>();
			xp.register(new LogRecord(null, null, null));
			xp.register(new LogRecords());
			result = xp.parse(is);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (is != null)
					is.close();
			} catch (IOException e) {
			}
		}
		return result;
	}

	protected void saveLog(String user) {
		List<LogRecord> lr = null;
		OutputStream os = null;
		try {
			os = logsRepositoryBase.getOutStream(user, false);
			os.write(("<?xml version=\"1.0\" encoding=\"" + "utf-8" + "\"?>\n").getBytes("utf-8"));
			os.write(("<LOGS>\n").getBytes("utf-8"));
			synchronized (this) { // is it really required?
				lr = logs.get(user);
				if (lr != null) {
					// saves only 5 records of every type considering they are ordered as latest is greatest
					int n = lr.size();
					int d = Math.min(n, LOGDEEPTH);
					for (int i = 0; i < d; i++)
						lr.get(n - d + i).saveXML(os, "utf-8", i);
				}
			}
			os.write(("</LOGS>\n").getBytes("utf-8"));

		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (DuplicateUser e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (os != null)
				try {
					os.close();
				} catch (IOException e) {
				}
		}
	}	

	public static class LogRecord extends GenericAttribute implements Saxable<LogRecord, Object> {
		static final String TAG_NAME = "REC";
		// TODO think about reusing names from parent class
		static final String ATTR_TIME = "TIME";
		static final String ATTR_AGENT = "AGENT";
		static final String ATTR_TYPE = "TYPE";
		protected long timeStamp;
		
		LogRecord(String _host, String _userAgent, String _type) {
			super(_host, _userAgent, _type);
			timeStamp = System.currentTimeMillis();
		}
		
		public long getTime() {
			return timeStamp;
		}

		public void saveXML(OutputStream _out, String _enc, int _order) throws IOException {
			Map<String, String> m = new HashMap<String, String>(3);
			m.put(ATTR_TIME, Long.toString(timeStamp));
			m.put(ATTR_AGENT, this.description);
			m.put(ATTR_TYPE, this.type);
			saveSimpleTag(_out, "utf-8", TAG_NAME, value, m);

		}

		protected Object clone() {
			LogRecord result = new LogRecord(value, description, type);
			result.timeStamp = this.timeStamp;
			return result;
		}
		
		public void saveVCard(OutputStream _out, String _enc, int _order) throws IOException {
		}

		public boolean startElement(String uri, String localName, String qName, Attributes attrs) throws SAXException {
			if (TAG_NAME.equals(qName)) {
				description = attrs.getValue(ATTR_AGENT);
				type = attrs.getValue(ATTR_TYPE);
				timeStamp = Long.parseLong(attrs.getValue(ATTR_TIME));
				value = null;
				return true;
			}
			return false;
		}

		public void characters(char[] ch, int start, int length) throws SAXException {
			if (value == null)
				value = new String(ch, start, length);
			else
				value += new String(ch, start, length);
		}

		public LogRecord endElement(String uri, String localName, String qName) throws SAXException {
			if (TAG_NAME.equals(qName) == false)
				throw new SAXException("Invalid document stracture, tag /REC expected.");
			return (LogRecord)clone();
		}

		public void endChild(Object c) {
			// no children we may care
		}

	}
	
	static class LogRecords  implements Saxable<List<LogRecord>, LogRecord> {

		List<LogRecord> result;
		
		public boolean startElement(String uri, String localName, String qName, Attributes attrs) throws SAXException {
			 if ("LOGS".equals(qName)) {
				 result = new ArrayList<LogRecord>(10);
				 return true;
			 }
				 
			return false;
		}

		public void characters(char[] ch, int start, int length) throws SAXException {
			// just ignore any
			//throw new SAXException("Characters:"+new String(ch, start, length));
		}

		public List<LogRecord> endElement(String uri, String localName, String qName) throws SAXException {
			return result;
		}

		public void endChild(LogRecord c)  throws SAXException {
			if (result != null)
				result.add(c);
			else
				throw new SAXException("Invalid document structure.");
		}
	}
}
