/* AddressBook - DataBookReader 
 * Copyright (C) 2000-2006 Dmitriy Rogatkin.  All rights reserved.
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
 * $Id: DataBookIO.java,v 1.26 2012/09/15 17:49:55 dmitriy Exp $
 */
package addressbook;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Stack;
import java.util.TimeZone;
import java.util.Vector;

import javax.xml.parsers.SAXParserFactory;

import org.aldan3.app.Env;
import org.aldan3.util.inet.HttpUtils;
import org.xml.sax.AttributeList;
import org.xml.sax.HandlerBase;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * This class used to read write address book in XML format Currently push parser approach is using, although pull parser can be considered see
 * http://www.xmlpull.org/, http://www.extreme.indiana.edu/xgws/xsoap/xpp/mxp1/index.html
 */
public class DataBookIO {

	public static final String DATABOOK_TAG = "DATABOOK";

	public static final String FOLDER_TAG = "FOLDER";

	public static final String PERSON_TAG = "PERSON";

	public static final String COOKIE_TAG = "COOKIE";

	public static final String NAME_TAG = "NAME";

	public static final String TITLE_TAG = "TITLE";

	public static final String DOB_TAG = "DOB";

	public static final String PHONE_TAG = "PHONE";

	public static final String ADDRESS_TAG = "ADDRESS";

	public static final String EMAIL_TAG = "EMAIL";

	public static final String LINK_TAG = "LINK";

	public static final String CHAT_TAG = "CHAT";

	public static final String PGP_TAG = "PGP";

	public static final String COMMENT_TAG = "COMMENT";

	public static final String LAST_TAG = "LAST";

	public static final String MAIDEN_TAG = "MAIDEN";

	public static final String FIRST_TAG = "FIRST";

	public static final String MIDDLE_TAG = "MIDDLE";

	public static final String MAJOR_TAG = "MAJOR";

	public static final String SALUT_TAG = "SALUT";

	public static final String NICK_TAG = "NICK";

	public static final String STREET_TAG = "STREET";

	public static final String CITY_TAG = "CITY";

	public static final String STATE_TAG = "STATE";

	public static final String COUNTRY_TAG = "COUNTRY";

	public static final String ZIP_TAG = "ZIP";

	public static final String PICTURE_TAG = "PICTURE";

	public static final String ACCOUNT_TAG = "ACCOUNT";

	public static final String ACCOUNT_NAME_TAG = "ANAME";

	public static final String ACCOUNT_LOGIN_TAG = "LNAME";

	public static final String PASSWORD_TAG = "PASSWORD";

	public static final String COLLECTION_TAG = "COLLECTION";

	public static final String TYPE_TAG = "TYPE";

	public static final String TYPE_ATTR = "TYPE";

	public static final String ROOM_ATTR = "ROOM";

	public static final String YEAR_ATTR = "YEAR";

	public static final String MONTH_ATTR = "MONTH";

	public static final String DAY_ATTR = "DAY";

	public static final String SERVER_ATTR = "SERVER";

	public static final String SUB_TYPE_ATTR = "SUB_TYPE";

	public static final String FORMAT_ATTR = "FORMAT";

	public static final String COMMENT_ATTR = "COMMENT";

	public static final String ORDER_ATTR = "ORDER";

	public static final String OWNER_ATTR = "OWNER";

	public static final String CREATED_ATTR = "CREATED";

	public static final String MODIFIED_ATTR = "MODIFIED";

	public static final String ENCRYPTED_ATTR = "ENCRYPTED";

	public static final String SHARING_ATTR = "SHARING";

	public static final String PGPKEY_ATTR = "PGPKEY";

	public static final String PROTOCOL_ATTR = "PROTOCOL";

	public static final String GENDER_ATTR = "GENDER";

	public static final String NAME_ATTR = "NAME";

	public static final String CREATED_ON_ATTR = "CREATEDON";

	public static final String SHARED_BY_ATTR = "SHAREDBY";

	public static final String PREFERABLE_ATTR = "PREFERABLE";

	public static final String DTD_HOME = "addressbook.DTD";

	public static final String DTD_NAME = "databook.dtd";

	public final static SimpleDateFormat DATE_XML_FMT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss zzz");
	static {
		// TimeZone tz = TimeZone.getTimeZone("GMT");
		// tz.setID("GMT");
		DATE_XML_FMT.setTimeZone(TimeZone.getTimeZone("GMT"));
	}

	Properties properties;

	public DataBookIO(Properties _properties) {
		properties = _properties;
	}

	/**
	 * reads XML data book
	 * 
	 * @param InputStream
	 *            with XML
	 * @return List of vectors of address book, links, cookies and others
	 */
	public List read(InputStream _in) throws AddressException {
		return read(_in, null);
	}

	/**
	 * reads address book data in XML format with optional encryption
	 * 
	 * @param _in
	 *            Input stream can be encrypted
	 * @param key
	 *            private key used for decryption stream data, cipher parameters specified in properties
	 * @return List of entries
	 * @throws AddressException
	 */
	public List read(InputStream _in, Key _key) throws AddressException {
		return read( new Vector(), _in, _key);
	}
	
	/** merges current read result in memory with new data, possible several merge scenarions thoug
	 * 
	 * @param _folders with data
	 * @param _in new xml stream
	 * @param _key cipher key
	 * @throws AddressException if something went wrong 
	 */
	public void merge(List _folders, InputStream _in, Key _key) throws AddressException {
		read(_folders, _in, _key);
	}

	protected List read(List result, InputStream _in, Key _key) throws AddressException {
		try {
			SAXParserFactory parserFactory = SAXParserFactory.newInstance();
			if (Env.isAndroid() == false)
				parserFactory.setValidating(true);
			parserFactory.newSAXParser().parse(_in, new DataBookHandler(result));
		} catch (Exception saxe) {
			System.err.println("Exception: " + saxe.getMessage());
			if (saxe instanceof SAXParseException) {
				System.err.println("** Parsing exception" + ", line:col " + ((SAXParseException) saxe).getLineNumber()
						+ ':' + ((SAXParseException) saxe).getColumnNumber() + ", "
						+ ((SAXParseException) saxe).getSystemId());
			}
			if (saxe instanceof SAXException && ((SAXException) saxe).getException() != null)
				((SAXException) saxe).getException().printStackTrace();
			else
				saxe.printStackTrace();
			throw new AddressException("Exception at reading or parsing AB from a stream.", saxe);
		} // no any other exception should happen
		return result;
	}
	
	public void write(List _folders, OutputStream _out, String _enc) throws IOException {
		write(_folders, _out, _enc, null, null);
	}

	public void write(List _folders, OutputStream _out, String _enc, Key _key) throws IOException {
		write(_folders, _out, _enc, null, _key);
	}

	public void write(List _folders, OutputStream _out, String _enc, Map _extraAttributes) throws IOException {
		write(_folders, _out, _enc, _extraAttributes, null);
	}

	/**
	 * Saves address book data with optional encryption
	 * 
	 * @param _folders
	 *            address book data
	 * @param _out
	 *            stream to save to
	 * @param _enc
	 *            encoding
	 * @param _extraAttributes
	 *            extra attributes
	 * @param _key
	 *            optional encryption key
	 * @throws IOException
	 */
	public void write(List _folders, OutputStream _out, String _enc, Map _extraAttributes, Key _key) throws IOException {
		CurrentKey.setKey(_key);
		try {
			_out.write(("<?xml version=\"1.0\" encoding=\"" + _enc + "\"?>").getBytes(_enc));
			String dtdLoc = properties.getProperty(DTD_HOME);
			if (dtdLoc != null) {
				_out.write("<!DOCTYPE DATABOOK SYSTEM \"".getBytes(_enc));
				_out.write(dtdLoc.getBytes(_enc));
				_out.write("/".getBytes(_enc));
				_out.write(DTD_NAME.getBytes(_enc));
				_out.write("\">".getBytes(_enc));
			}
			// TODO: add personal profile attributes
			_out.write("<DATABOOK MODIFIED=\"".getBytes(_enc));
			_out.write(DATE_XML_FMT.format(new Date()).getBytes(_enc));
			if (_extraAttributes == null || _extraAttributes.size() == 0)
				_out.write("\">".getBytes(_enc));
			else {
				_out.write("\"".getBytes(_enc));
				_out.write(formAttributeString(_extraAttributes).getBytes(_enc));
				_out.write(">".getBytes(_enc));
			}
			for (int k = 0; k < _folders.size(); k++) {
				((Folder) _folders.get(k)).saveXML(_out, _enc, k);
			}
			_out.write("</DATABOOK>".getBytes(_enc));
		} catch (UnsupportedEncodingException uee) {
			System.err.println("Exception: " + uee);
			throw new IOException("Unsupported encoding" + uee);
		}
	}

	public static String formAttributeString(Map extraAttributes) {
		Iterator<Map.Entry> i = extraAttributes.entrySet().iterator();
		if (i.hasNext() == false)
			return "";
		StringBuffer result = new StringBuffer();
		do {
			Map.Entry entry = i.next();
			result.append(' ').append(entry.getKey()).append("=\"").append(
					HttpUtils.htmlEncode(entry.getValue().toString())).append('"');
		} while (i.hasNext());
		return result.toString();
	}

	public static final String assureTagName(String _tag) {
		if (_tag.indexOf(' ') >= 0)
			return _tag.replace(' ', '_');
		return _tag;
	}

	// TODO: reconsider approach when every sub element can parse own part and
	// be
	// a current data handler
	class DataBookHandler extends HandlerBase {

		List folders;

		Stack<Folder> processFolders;

		Contact contact;

		String last, first, middle, salut, maiden, major, nick;

		String comment, type;

		String password, account, login, url;

		String street, title, city, state, country, zip;

		String[] attributes;

		StringBuffer buffer;

		boolean metAddress, wasAccount, pref;

		COI lastCollection;

		private boolean encrypted;
		
		private boolean mergeMode;

		DataBookHandler(List _folders) {
			folders = _folders;
			mergeMode = folders.size() > 0;
			processFolders = new Stack<Folder>();
		}

		public void setDocumentLocator(Locator _l) {
		}

		public void startDocument() throws SAXException {
			buffer = new StringBuffer(100);
		}

		public void endDocument() throws SAXException {
		}

		public void startElement(String _tag, AttributeList _attrs) throws SAXException {
			if (lastCollection != null) {
				lastCollection.startElement(_tag, _attrs);
				buffer.setLength(0);
			} else if (PERSON_TAG.equals(_tag)) {
				Date date = new Date();
				if (_attrs.getValue(CREATED_ON_ATTR) != null) {
					try {
						synchronized (DATE_XML_FMT) {
							date = DATE_XML_FMT.parse(_attrs.getValue(CREATED_ON_ATTR));
						}
					} catch (ParseException e) {
					}
				}
				contact = new Contact(date);
				contact.setOwner(_attrs.getValue(SHARED_BY_ATTR));
			} else if (PHONE_TAG.equals(_tag)) {
				comment = HttpUtils.htmlDecode(_attrs.getValue(COMMENT_ATTR));
				type = HttpUtils.htmlDecode(_attrs.getValue(TYPE_ATTR));
				pref = _attrs.getValue(PREFERABLE_ATTR) != null && "true".equals(_attrs.getValue(PREFERABLE_ATTR));
				buffer.setLength(0);
			} else if (LAST_TAG.equals(_tag)) {
				buffer.setLength(0);
			} else if (ADDRESS_TAG.equals(_tag)) {
				comment = HttpUtils.htmlDecode(_attrs.getValue(COMMENT_ATTR));
				metAddress = true;
				// TODO clear all address components to avoid using prev
			} else if (FOLDER_TAG.equals(_tag)) {
				// TODO: remove folder type or push it in XML
				Folder folder = new Folder<XMLSaver>(_attrs.getValue(NAME_ATTR));
				comment = _attrs.getValue(COMMENT_ATTR);
				if (comment != null)
					folder.comment = comment;
				folder.setType(_attrs.getValue(TYPE_ATTR));
				//System.err.printf("Created folder %s current was %s%n", folder, processFolders.isEmpty()?null:processFolders.peek());
				processFolders.push(folder);
			} else if (FIRST_TAG.equals(_tag)) {
				buffer.setLength(0);
			} else if (MIDDLE_TAG.equals(_tag)) {
				buffer.setLength(0);
			} else if (MAJOR_TAG.equals(_tag)) {
				buffer.setLength(0);
			} else if (SALUT_TAG.equals(_tag)) {
				buffer.setLength(0);
			} else if (EMAIL_TAG.equals(_tag)) {
				comment = HttpUtils.htmlDecode(_attrs.getValue(COMMENT_ATTR));
				type = HttpUtils.htmlDecode(_attrs.getValue(TYPE_ATTR));
				pref = _attrs.getValue(PREFERABLE_ATTR) != null && "true".equals(_attrs.getValue(PREFERABLE_ATTR));
				buffer.setLength(0);
			} else if (LINK_TAG.equals(_tag)) {
				if (_attrs.getValue(COMMENT_ATTR) != null)
					comment = HttpUtils.htmlDecode(_attrs.getValue(COMMENT_ATTR));
				buffer.setLength(0);
			} else if (CHAT_TAG.equals(_tag)) {
				comment = HttpUtils.htmlDecode(_attrs.getValue(COMMENT_ATTR));
				attributes = new String[] { HttpUtils.htmlDecode(_attrs.getValue(SERVER_ATTR)),
						HttpUtils.htmlDecode(_attrs.getValue(PROTOCOL_ATTR)),
						HttpUtils.htmlDecode(_attrs.getValue(ROOM_ATTR)) };
				buffer.setLength(0);
			} else if (PICTURE_TAG.equals(_tag)) {
				if (_attrs.getValue(COMMENT_ATTR) != null)
					comment = HttpUtils.htmlDecode(_attrs.getValue(COMMENT_ATTR));
				else
					comment = null;
				if (_attrs.getValue(TYPE_ATTR) != null)
					type = HttpUtils.htmlDecode(_attrs.getValue(TYPE_ATTR));
				else
					type = null;
				buffer.setLength(0);
			} else if (PGP_TAG.equals(_tag)) {
				comment = HttpUtils.htmlDecode(_attrs.getValue(COMMENT_ATTR));
				buffer.setLength(0);
			} else if (COMMENT_TAG.equals(_tag)) {
				if (_attrs.getValue(COMMENT_ATTR) != null)
					comment = _attrs.getValue(COMMENT_ATTR);
				buffer.setLength(0);
			} else if (DOB_TAG.equals(_tag)) {
				try {
					contact.setDOB(LocalDate.of(Integer.parseInt(_attrs.getValue(YEAR_ATTR)),
							Integer.parseInt(_attrs.getValue(MONTH_ATTR)), Integer.parseInt(_attrs
									.getValue(DAY_ATTR))));
				} catch (Exception e) { // number format & null pointer
					e.printStackTrace();
				}
			} else if (NICK_TAG.equals(_tag)) {
				try {
					int i = Integer.parseInt(_attrs.getValue(ORDER_ATTR));
					if (i > 0)
						buffer.append(' ');
					return;
				} catch (Exception e) { // null & number format
				}
				buffer.setLength(0);
			} else if (MAIDEN_TAG.equals(_tag)) {
				buffer.setLength(0);
			} else if (TITLE_TAG.equals(_tag)) {
				buffer.setLength(0);
			} else if (STREET_TAG.equals(_tag)) {
				buffer.setLength(0);
			} else if (CITY_TAG.equals(_tag)) {
				buffer.setLength(0);
			} else if (STATE_TAG.equals(_tag)) {
				buffer.setLength(0);
			} else if (COUNTRY_TAG.equals(_tag)) {
				buffer.setLength(0);
			} else if (ZIP_TAG.equals(_tag)) {
				buffer.setLength(0);
			} else if (ACCOUNT_TAG.equals(_tag)) {
				password = null;
				account = null;
				login = null;
				url = null;
				type = null;
				comment = null;
				wasAccount = true;
				encrypted = "1".equals(_attrs.getValue(ENCRYPTED_ATTR));
			} else if (PASSWORD_TAG.equals(_tag)) {
				buffer.setLength(0);
			} else if (ACCOUNT_LOGIN_TAG.equals(_tag)) {
				buffer.setLength(0);
			} else if (ACCOUNT_NAME_TAG.equals(_tag)) {
				buffer.setLength(0);
			} else if (TYPE_TAG.equals(_tag)) {
				buffer.setLength(0);
			} else if (COLLECTION_TAG.equals(_tag)) {
				lastCollection = new COI();
				lastCollection.setAttribute(GenericAttribute.VALUE, _attrs.getValue(GenericAttribute.VALUE
						.toUpperCase()));
				lastCollection.setAttribute(GenericAttribute.DESCRIPTION, _attrs.getValue(COMMENT_ATTR));
				lastCollection.setOwner(_attrs.getValue(SHARED_BY_ATTR));
			} else if (DATABOOK_TAG.equals(_tag)) {
				// collect some data book attributes
			}
		}

		public void endElement(String _tag) throws SAXException {
			if (COLLECTION_TAG.equals(_tag)) {
				Folder folder = processFolders.empty() ? null : processFolders.peek();
				// fix folder type
				if (folder.getType() != Folder.PERSON)
					folder.add(lastCollection);
				lastCollection = null;
			} else {
				if (lastCollection != null) {
					lastCollection.endElement(_tag, buffer);
				} else if (PERSON_TAG.equals(_tag)) {
					Folder folder = processFolders.empty() ? null : processFolders.peek();
					// fix folder type
					if (folder.getType() == Folder.UNDEFINED)
						folder.setType(Folder.PERSON);
					if (folder.getType() == Folder.PERSON || folder.getType() == Folder.ANY)
						folder.add(contact);
				} else if (NAME_TAG.equals(_tag)) {
					contact.setValue(new Name(last, first, middle, salut, maiden, major, nick));
				} else if (FOLDER_TAG.equals(_tag)) {
					Folder folder = processFolders.pop();
					folder.sort(true);
					if (processFolders.empty())
						folders.add(folder);
					else
						processFolders.peek().add(folder);
				} else if (PHONE_TAG.equals(_tag)) {
					Telephone tel;
					contact.add(tel = new Telephone(buffer.toString(), comment, type));
					if (pref)
						tel.setPreferable(true);
				} else if (ADDRESS_TAG.equals(_tag)) {
					contact.add(new Address(street, city, state, country, zip, comment, title));
					metAddress = false;
				} else if (LAST_TAG.equals(_tag)) {
					last = buffer.toString();
				} else if (FIRST_TAG.equals(_tag)) {
					first = buffer.toString();
				} else if (MIDDLE_TAG.equals(_tag)) {
					middle = buffer.toString();
				} else if (MAJOR_TAG.equals(_tag)) {
					major = buffer.toString();
				} else if (SALUT_TAG.equals(_tag)) {
					salut = buffer.toString();
				} else if (NICK_TAG.equals(_tag)) {
					nick = buffer.toString();
				} else if (MAIDEN_TAG.equals(_tag)) {
					maiden = buffer.toString();
				} else if (TITLE_TAG.equals(_tag)) {
					title = buffer.toString();
					if (metAddress == false) {
						contact.setTitle(title);
						title = null;
					}
				} else if (STREET_TAG.equals(_tag)) {
					street = buffer.toString();
				} else if (CITY_TAG.equals(_tag)) {
					city = buffer.toString();
				} else if (STATE_TAG.equals(_tag)) {
					state = buffer.toString();
				} else if (COUNTRY_TAG.equals(_tag)) {
					country = buffer.toString();
				} else if (ZIP_TAG.equals(_tag)) {
					zip = buffer.toString();
				} else if (EMAIL_TAG.equals(_tag)) {
					EMail email;
					contact.add(email = new EMail(buffer.toString(), comment, type));
					email.setPreferable(pref);
				} else if (LINK_TAG.equals(_tag)) {
					if (wasAccount)
						url = buffer.toString();
					else
						contact.add(new Link(buffer.toString(), comment));
				} else if (CHAT_TAG.equals(_tag)) {
					int chatType = Chat.type2Int(attributes[1]);
					contact.add(new Chat(buffer.toString(), attributes[0], chatType, comment, attributes[2]));
				} else if (PICTURE_TAG.equals(_tag)) {
					contact.add(new Picture(buffer.toString(), comment, type));
				} else if (PGP_TAG.equals(_tag)) {
					contact.add(new PGP(buffer.toString(), comment));
				} else if (COMMENT_TAG.equals(_tag)) {
					if (wasAccount)
						comment = buffer.toString();
					else {
						if (comment == null)
							contact.addComment(buffer.toString());
						else
							contact.add(new Commentary(buffer.toString(), comment));
					}
				} else if (PASSWORD_TAG.equals(_tag)) {
					password = buffer.toString();
					// TODO decipher if needed
				} else if (ACCOUNT_LOGIN_TAG.equals(_tag)) {
					login = buffer.toString();
				} else if (ACCOUNT_NAME_TAG.equals(_tag)) {
					account = buffer.toString();
				} else if (TYPE_TAG.equals(_tag)) {
					type = buffer.toString();
				} else if (ACCOUNT_TAG.equals(_tag)) {
					wasAccount = false;
					Account account = new Account(encrypted ? null : login, comment);
					account.encrypted = encrypted;
					if (account.encrypted) {
						account.encValue = login;
						account.encPassword = password;
						account.encAccount = this.account;

					} else {
						account.setPassword(password);
						account.setName(this.account);
					}
					account.setLink(url);
					account.setType(type);
					// comment = null;
					//System.err.printf("Set account %s for %s%n", account, this.account);
					contact.add(account);
				} else if (DATABOOK_TAG.equals(_tag)) {
					// collect some data book attributes
				}
			}
		}

		public void characters(char _buf[], int _offset, int _len) throws SAXException {
			buffer.append(_buf, _offset, _len);
		}

		public void ignorableWhitespace(char _buf[], int _offset, int _len) throws SAXException {
			// just ignore for a while
		}

		public void processingInstruction(String _target, String _data) throws SAXException {
		}
	}

	public static class CurrentKey {
		private static ThreadLocal<Key> ref = new ThreadLocal<Key>();

		public static/* synchronized */void setKey(Key key) {
			ref.set(key);
		}

		public static Key getCurrent() {
			return ref.get();
		}
	}

}
