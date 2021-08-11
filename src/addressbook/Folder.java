/* AddressBook - Folder 
 * Copyright (C) 2000-2004 Dmitriy Rogatkin.  All rights reserved.
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
 *  $Id: Folder.java,v 1.11 2007/09/23 03:44:37 rogatkin Exp $
 */
package addressbook;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.io.OutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.aldan3.util.inet.HttpUtils;

import static addressbook.DataBookIO.*;

public class Folder<T extends XMLSaver> implements XMLSaver { // GenericAttribute
	public static final int UNDEFINED = 0;

	public static final int PERSON = 1;

	public static final int FOLDER = 2;

	public static final int LIST_ITEMS = 3;

	public static final int ANY = 4;

	// public static final int TRASH = 5;

	public static char levelSeparator = '.';

	public static final String[] TYPE_NAMES = { "Undefined", "Contact", "Folder", "ListItems", "Mixed" };

	public enum FolderType {
		Undefined, Contact, ListItems, Folder, Mixed
	};

	List<T> content;

	String name;

	FolderType type;

	String comment;

	public Folder(String _name) {
		name = _name;
		content = new ArrayList<T>();
		// System.err.println(content.getClass().getTypeParameters());
		// if (List<T> instanceof List<Contact>)
		// type = FolderType.Contact;
	}

	public String toString() {
		return name;
	}

	public String getShortName() {
		int sp = name.lastIndexOf(levelSeparator);
		if (sp > 0)
			return name.substring(sp + 1);
		return name;
	}

	public String getParenFolderName() {
		int sp = name.lastIndexOf(levelSeparator);
		if (sp < 0)
			return null;
		return name.substring(0, sp);
	}

	public void rename(String newName) {
		String parentName = getParenFolderName();
		if (parentName == null)
			name = newName;
		else
			name = parentName + levelSeparator + newName;
	}

	public boolean equals(Object other) {
		if (other instanceof Folder)
			return other == this || name.equals(((Folder) other).name) && content.equals(((Folder) other).content);
		else if (other instanceof String)
			return name.equals((String) other);
		return false;
	}

	public void sort(boolean acs) {
		Collections.sort(content, new Comparator<T>() {

			public int compare(T arg0, T arg1) {
				if (arg0 instanceof Folder && arg1 instanceof Folder)
					((Folder) arg0).getShortName().compareTo(((Folder) arg1).getShortName());
				if (arg0 != null) {
					if (arg1 == null)
						return 1;
					if (arg0 instanceof Contact && arg1 instanceof Contact)
						return ((Contact) arg0).getName().toString().compareTo(((Contact) arg1).getName().toString());
					return arg0.toString().compareTo(arg1.toString());
				} else {
					if (arg1 != null)
						return -1;
				}
				return 0;
			}

		});
	}

	public int hashCode() {
		return name.hashCode() ^ content.hashCode();
	}

	public static int type2Int(FolderType ft) {
		if (ft != null)
			switch (ft) {
			case Undefined:
				return UNDEFINED;
			case Contact:
				return PERSON;
			case Folder:
				return FOLDER;
			case ListItems:
				return LIST_ITEMS;
			case Mixed:
				return ANY;
			}
		return UNDEFINED;
	}

	public static FolderType type2Enum(int type) {
		switch (type) {
		case UNDEFINED:
			return FolderType.Undefined;
		case PERSON:
			return FolderType.Contact;
		case FOLDER:
			return FolderType.Folder;
		case LIST_ITEMS:
			return FolderType.ListItems;
		case ANY:
			return FolderType.Mixed;
		}
		return FolderType.Undefined;
	}

	public int getType() {
		return type2Int(type);
	}

	public void setType(int _type) {
		// if (FolderType.Undefined == type)
		type = type2Enum(_type);
	}

	public void setType(String _type) {
		int i = 0;
		for (String stype : TYPE_NAMES) {
			if (stype.equals(_type)) {
				setType(i);
				break;
			}
			i++;
		}
	}

	public List<T> getContent() {
		return content;
	}

	/**
	 * Adds an element in a folder
	 * 
	 * @param _element
	 * @return the element if can be added
	 */
	public T add(T _element) {
		// TODO consider optimization
		String on = null;
		if (_element instanceof Folder) {
			on = ((Folder) _element).name;
			((Folder) _element).parentTo(this);
		}
		// TODO do not allow name duplication even when content is different
		if (content.contains(_element) == false) {
			content.add(_element);
			return _element;
		}
		if (_element instanceof Folder)
			((Folder) _element).name = on;

		return null;
	}

	public synchronized T merge(T _element) {
		if (_element instanceof Folder) {
			if (equals(_element))
			for(Object element_t:((Folder)_element).getContent()) {
				merge((T)element_t);
			}
			return null; // decide later
		}
		//System.err.printf("searching for %s in %s%n", _element, content);
		int pos = content.indexOf(_element);
		if (pos < 0) {
			//System.err.printf("merging as add because %s not found%n", _element);
			return add(_element);
		}
		if (_element instanceof Contact) {
			//System.err.printf("Merging %s%n", _element);
			return (T) ((Contact) content.get(pos)).merge((Contact) _element);
		}
		return null;
	}

	protected void parentTo(Folder parent) {
		name = parent.name + levelSeparator + getShortName();
		for (T element : content) {
			if (element instanceof Folder)
				((Folder) element).parentTo(this);
		}
	}

	public void remove(XMLSaver _element) {
		content.remove(_element);
	}

	public String getCommentary() {
		return comment;
	}

	public void saveXML(OutputStream _out, String _enc, int _order) throws IOException, UnsupportedEncodingException {
		_out.write("<FOLDER NAME=\"".getBytes(_enc));
		// _out.write(HttpUtils.htmlEncode(name).getBytes(_enc));
		_out.write(HttpUtils.htmlEncode(getShortName()).getBytes(_enc));
		if (comment != null) {
			_out.write(("\" " + COMMENT_ATTR + "=\"").getBytes(_enc));
			_out.write(HttpUtils.htmlEncode(comment).getBytes(_enc));
		}
		_out.write(("\" " + TYPE_ATTR + "=\"" + TYPE_NAMES[getType()]).getBytes(_enc));
		_out.write(("\" " + ORDER_ATTR + "=\"" + _order).getBytes(_enc));
		_out.write("\">".getBytes(_enc));
		for (int i = 0; i < content.size(); i++)
			content.get(i).saveXML(_out, _enc, i);
		_out.write("</FOLDER>".getBytes(_enc));
	}

	public void saveVCard(OutputStream _out, String _enc, int order) throws IOException {
		for (int i = 0; i < content.size(); i++) {
			XMLSaver xs = content.get(i);
			xs.saveVCard(_out, _enc, i);
		}
	}

	public int size() {
		return content.size();
	}
}
