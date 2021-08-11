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
 *  $Id: FolderTravel.java,v 1.3 2012/01/18 18:47:32 dmitriy Exp $
 * Created on Sep 19, 2005
 */
package addressbook.servlet.model;

import java.util.ArrayList;
import java.util.List;

import org.aldan3.model.TreeModel;

import addressbook.AddressBookResources;
import addressbook.Folder;
import addressbook.VersionConstant;

public class FolderTravel implements TreeModel {
	protected FolderOperations folderOperations;
	protected String urlPref;
	
	public FolderTravel(FolderOperations fo, String servName) {
		folderOperations = fo;
		urlPref = servName==null?"":servName;
	}

	public List getChildren(Object _parent) {
		if (_parent == null)
			return folderOperations.search("", null);
		if (_parent instanceof Folder) {			
			Folder folder = (Folder) _parent;
			if (folder.getType() == Folder.FOLDER || folder.getType() == Folder.ANY) {
				List result = new ArrayList<Folder>(10);
				for (Object object:folder.getContent())
					if (object instanceof Folder)
						result.add(object);
				return result;
			}
		}
		return new ArrayList();
	}

	public String getLabel(Object _object) {
		if (_object == null)
			return VersionConstant.PROGRAMNAME;
		if (_object instanceof Folder)
			return ((Folder)_object).getShortName();
		return _object.toString();
	}

	public String getId(Object _object) {
		if (_object == null)
			return VersionConstant.PROGRAMNAME;
		return _object.toString();
	}

	public String getAssociatedReference(Object _object) {
		return "";//getPage(_object)+"?nodeid="+getId(_object);
	}

	public String getImageModifier(Object _object) {
		if (_object instanceof Folder) {
			if (((Folder)_object).getType() == Folder.UNDEFINED)
				return "error";
			else if (_object.equals(AddressBookResources.LABEL_TRASH))
				return "trash"; // use const
			else if (_object.equals(AddressBookResources.LABEL_SHARED))
				return "net";
			else if (((Folder)_object).getType() == Folder.PERSON)
				return "person";
			return getChildren(_object).size()>0?"folder":"document";
		}
		return "document";
	}

	public String getSwitchReference(Object _object) {
		return urlPref;
	}

	public boolean isId(Object _object, String _object2) {
		return getId(_object).equals(_object2);
	}
	
	public boolean canMark(Object _object, boolean _opened) {
		return true;
	}

	public String getToolTip(Object _object) {
		return null;
	}
}
