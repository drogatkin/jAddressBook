/* jaddressbook - ContactOperations.java
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
 *  $Id: GenericOperations.java,v 1.7 2013/02/13 03:23:34 cvs Exp $
 * Created on Sep 19, 2005
 */

package addressbook.servlet.model;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import addressbook.Account;
import addressbook.COI;
import addressbook.Commentary;
import addressbook.Contact;
import addressbook.Folder;
import addressbook.GenericAttribute;
import addressbook.Picture;
import addressbook.XMLSaver;
import addressbook.servlet.AddressBookProcessor;

public class GenericOperations<RT extends AbstractAttributeStorage> extends AbstractOperations {

	public void init(AddressBookProcessor abp) {
	}

	public List<RT> search2(String query, String folderName, FolderOperations folderOperations) {
		// TODO redefine API to provide a list of desirable types
		List<Folder> folders = folderOperations.search(escapeMask(folderName), new Folder.FolderType[] {
				Folder.FolderType.Contact, Folder.FolderType.Mixed });
		//System.err.println("Found fs:" + folderName + "\n" + folders);
		if (folders == null)
			return null;
		// TODO check for Unicode letter and convert to \ uNNNN
		if (query == null || query.length() == 0)
			query = ".*";
		else
			query = FolderOperations.massagePattern(query);
		Pattern p = Pattern.compile(query);
		//System.err.println("Search pat:"+query); // !!
		List<RT> result = new ArrayList<RT>();
		for (Folder folder : folders) {
			if (folderName == null || folderName.equals(folder.toString())) {
				// TODO folder type contact
				// System.err.println("Folder :"+folder+" found.");
				for (XMLSaver xs : (List<XMLSaver>) folder.getContent()) {
					// System.err.println("C:"+contact.getAttribute(Contact.NAME).toString()+"
					// "+p);
					
					if (xs instanceof Contact) {
						RT record = (RT) xs;
						//System.err.println("Matching:"+record.getAttribute(Contact.NAME).toString()+"|");
						// TODO search also for other contact attributes
						if (p.matcher(record.getAttribute(Contact.NAME).toString()).matches()) 
							result.add(record);
			//			else
				//			System.err.println("NO");
					} else if (xs instanceof COI) {
						String name = ((COI)xs).getStringAttribute(GenericAttribute.VALUE);
						if (name != null && p.matcher(name).matches())
							result.add((RT)xs);
					}
				}
			}
		}
		return result;
	}
	
	public List<RT> search(String query, String folderName, FolderOperations folderOperations) {
		// TODO redefine API to provide a list of desirable types
		List<Folder> folders = folderOperations.search(escapeMask(folderName), new Folder.FolderType[] {
				Folder.FolderType.Contact, Folder.FolderType.Mixed });
		//System.err.println("Found fs:" + folderName + "\n" + folders);
		if (folders == null)
			return null;
		List<RT> result = new ArrayList<RT>();
		for (Folder folder : folders) {
			if (folderName == null || folderName.equals(folder.toString())) {
				folder_content: for (XMLSaver xs : (List<XMLSaver>) folder.getContent()) {
					if (xs instanceof AbstractAttributeStorage == false)
						continue; // do not add folders for now
					if (query == null || query.isBlank()) {
						result.add((RT)xs);
					} else if (xs instanceof Contact) {
						Contact contact = (Contact) xs;
						System.err.printf("Checking %s in %s%n", query, contact.getAttribute(Contact.NAME));
						//new Exception("trace").printStackTrace();
						if (contact.getAttribute(Contact.NAME) != null && contact.getAttribute(Contact.NAME).toString().contains(query)) 
							result.add((RT)xs);
						else { // check also for 
							if (contact.getAccounts() != null)
								for (Account acnt: contact.getAccounts()) {
									if (acnt.contains(query)) {
										result.add((RT)xs);
										continue folder_content;
									}
								}
							if (contact.getComments() != null)
								for (Commentary comm: contact.getComments()) {
									if (comm.contains(query)) {
										result.add((RT)xs);
										continue folder_content;
									}
								}
						}
					} else if (xs instanceof COI) {
							result.add((RT)xs);
					}
				}
			}
		}
		return result;
	}

	public RT getContact(FolderOperations fo, String folderName, String lastParName, String hashParName,
			AddressBookProcessor abp) {
		// find a record to update
		String name = abp.getStringParameterValue(lastParName, "", 0);
		List<RT> records = search(name, folderName, fo);
		int hash = hashParName==null?name.hashCode():abp.getIntParameterValue(hashParName, 0, 0);
		//System.err.printf("Looking for '%s', hash: %d, in- %s%n", name, hash, folderName);
		if (hash != 0) {
			for (RT record : records) {
				//System.err.printf("Comapring %s / %d%n", record.getId(), record.getId().hashCode());
				if (record.getId().hashCode() == hash) {
					return record;
				}
			}
		}
		return null;
	}

	public Picture createPicture(RT record) {
		String baseName = record.getId().toString() + '_';
		List<Picture> pictures = (List<Picture>)record.getAttribute(Contact.PICTURE);
		String currentName;
		if (pictures != null) {
			int k = 0;
			boolean found;
			do {
				currentName = baseName + k;
				found = false;
				for (Picture picture : pictures) {
					if (picture.getValue().equals(currentName)) {
						found = true;
						break;
					}
				}
				k++;
			} while (found && k < 100);
		} else
			currentName = baseName + 0;

		return new Picture(currentName, null, null);
	}
	
	public static String escapeMask(String s) {
		if (s == null || s.length() == 0)
			return s;
		// TODO use an approach used in GenericOperations of group reg exp replace
		return s.replace("(", "\\(").replace(")", "\\)").replace(".", "\\.").replace("*", "\\*");
	}
	
	public static int increment(int v) {
		return ++v;
	}

}
