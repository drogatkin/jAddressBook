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
 *  $Id: PersonalFolderOperations.java,v 1.4 2007/09/20 04:28:10 rogatkin Exp $
 * Created on Sep 19, 2005
 */
package addressbook.servlet.model;

import java.io.InputStream;
import java.security.Key;
import java.util.List;

import addressbook.AddressException;
import addressbook.Contact;
import addressbook.Folder;
import addressbook.XMLSaver;
// decorator class
public class PersonalFolderOperations extends FolderOperations {
	protected String areaId;
	protected FolderOperations delegateOp;
	protected Key key;
	
	public PersonalFolderOperations(String personId, FolderOperations operations, Key k) {
		areaId = personId;
		delegateOp = operations;
		io = delegateOp.getXMLSerializer();
		key = k;
	}
	
	public List<Folder> search(String query, Folder.FolderType[] ftypes) {
		return delegateOp.search(query, ftypes, delegateOp.getPersonalFolders(areaId, key));
	}
	
	public void save() {
		delegateOp.save(areaId);
	}
	
	public void merge(InputStream in) throws AddressException {
		delegateOp.merge(in, areaId, key);
	}
	
	public void remove() {
		delegateOp.remove(areaId);
	}

	public void flush() {
		delegateOp.flush(areaId);
	}
	
	@Override
	public void addContact(XMLSaver contact, Folder folder) {
		delegateOp.addContact(contact, folder);
	}
	
	@Override
	public XMLSaver cloneContact(XMLSaver contact) {
		return delegateOp.cloneContact(contact);
	}
}
