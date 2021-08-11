/* jaddressbook - FolderOperations
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
 *  $Id: FolderOperations.java,v 1.19 2013/02/13 03:23:34 cvs Exp $
 * Created on Sep 19, 2005
 */
package addressbook.servlet.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.WeakHashMap;
import java.util.regex.Pattern;

import org.aldan3.app.Env;

import addressbook.AddressBookResources;
import addressbook.AddressException;
import addressbook.Contact;
import addressbook.DataBookIO;
import addressbook.Folder;
import addressbook.XMLSaver;
import addressbook.servlet.AddressBookProcessor;
import addressbook.servlet.model.UserOperations.DuplicateUser;
import addressbook.util.IoHelper;

public class FolderOperations extends AbstractOperations {

	public static final String MAX_CONTACTS = "MAX_CONTACTS";

	// TODO: should be a sort of cache to fit memory size
	protected Map<String, List<Folder>> userFolders;

	protected Folder sharedFolder;
	
	protected Folder localContacts;

	protected Properties properties;

	protected IoHelper dataRepositoryBase;

	protected IoHelper sharedRepositoryBase;

	protected int folderCapacity;

	protected DataBookIO io;
	
	protected CertificateOperations certOp;

	public static final Map<String, String> SHARE_MAP_ATTR = new HashMap<String, String>(2);
	//public FolderOperations() {
		//System.err.println("CREATTTTTTTTTTTTED FolderOperations");
	//}
	public List<Folder> getFolders(Folder parent) {
		List l = parent.getContent();
		if (l == null || l.size() == 0)
			return null;
		List<Folder> result = new ArrayList<Folder>();
		for (Object o : l) {
			if (o instanceof Folder)
				result.add((Folder) o);
		}
		return result;
	}

	public Folder addFolder(Folder parent, Folder newFolder) {
		// TODO adjust parent type to ANY
		if (parent != null)
			if (parent.getType() != Folder.ANY && parent.getType() != Folder.FOLDER) {
				System.err.printf("Can't add folder %s to :%s, because type of parrent %s%n", newFolder, parent, parent.getType());
				return null;
			} else
				return (Folder) parent.add(newFolder);
		else {
			List<Folder> folders = search(null, null);
			if (containsFolder(folders, newFolder.toString()) == false)
				return folders.add(newFolder) ? newFolder : null; // TODO unless it's there
		}
		return null;
	}

	public Folder addFolder(Folder parent, String name, int type) {
		Folder result = createFolder(name, type);
		return addFolder(parent, result);
	}

	public void deleteFolder(Folder parent, Folder folder) {
		if (parent != null)
			parent.remove(folder);
		else {
			String parentName = folder.getParenFolderName();
			List<Folder> folders = search(parentName, null);
			//System.err.println("looking for parent folder:" + parentName + ", found :" + folders);
			if (parentName == null)
				folders.remove(folder);
			else
				for (Folder pf : folders)
					if (pf.equals(parentName)) {
						//System.err.println("Deleted:" + folder);
						deleteFolder(pf, folder);
						break;
					}
		}
	}

	public void addContact(XMLSaver contact, Folder folder) {
		if (exeedFolderCapacity(folder) == false)
			folder.add(contact);
		else
			System.err.printf("Can't add %s in folder %s%n", contact, folder);
	}

	@Override
	public void init(AddressBookProcessor abp) {
		SHARE_MAP_ATTR.put(DataBookIO.SHARING_ATTR, "PUBLIC");
		properties = abp.getProperties();
		folderCapacity = Integer.parseInt(properties.getProperty(MAX_CONTACTS, "1000"));
		//System.err.printf("INITTTTTTTTTTTTTTTTT %d%n", folderCapacity);
		dataRepositoryBase = new IoHelper(abp.getDataRoot(), "data", abp.getProperties());
		// TODO reconsider weak map, or provide finalize save 
		userFolders = new WeakHashMap<String, List<Folder>>();
		sharedRepositoryBase = new IoHelper(abp.getDataRoot(), "data/shared", abp.getProperties());
		io = new DataBookIO(properties);
		this.certOp = abp.getCertificateOperations();
		readSharedFolder();
		if (Env.isAndroid()) 
			localContacts = new AndroidContacts(abp.getRuntimeContext());
	}

	public DataBookIO getXMLSerializer() {
		return io;
	}

	public IoHelper getRepositoryAccess() {
		return dataRepositoryBase;
	}

	protected List<Folder> getPersonalFolders(String areaId, Key key) {
		List<Folder> result = userFolders.get(areaId);
		if (result == null) {
			InputStream is = null;
			try {
				//System.err.printf("Usr %s data not found, read%n", areaId);
				// TODO cache in sesion since user sensitive
				String ext = dataRepositoryBase.createLock(areaId)?"":".bak";
				result = (List<Folder>) io.read(is = dataRepositoryBase.getInStream(areaId+ext), key);
				userFolders.put(areaId, result);
			} catch (AddressException ae) {
				// happens at data corruption
				// TODO a user has to be notified properly
				System.err.println("Exception at reading ab:" + ae);
			} catch (IOException ioe) {
				System.err.println("Couldn't load, " + ioe);
				userFolders.put(areaId, result = createStandardFolders());
			} finally {
				try {
					is.close();
				} catch (Exception e) {
				}
			}
			// TODO exclude shared from save or use assure instead of add
			assureSharedFolder(result);
			assureTrashFolder(result/* name, type */);
			assureLocalFolder(result);
		}

		return result;
	}

	public List<Folder> search(String query, Folder.FolderType[] ftypes) {
		throw new UnsupportedOperationException();
	}

	public void flush() {
		throw new UnsupportedOperationException();
	}

	public void flush(String areaId) {
		if (userFolders != null)
			userFolders.remove(areaId);
	}

	public static boolean containsFolder(List<Folder> folders, String folderName) {
		for (Folder f : folders) {
			if (f.toString().equals(folderName))
				return true;
		}
		return false;
	}

	protected List<Folder> search(String query, Folder.FolderType[] ftypes, List<Folder> folders) {
		if (query == null || query.length() == 0)
			return folders;
		Pattern p = Pattern.compile(massagePattern(query));
		EnumMap<Folder.FolderType, Folder.FolderType> mtypes = createTypeMap(ftypes);
		List<Folder> result = new ArrayList<Folder>();
		for (Folder folder : folders) {
			//System.err.println("Matching " + folder + ", type:" + folder.getType());
			if (p.matcher(folder.toString()).matches()
					&& (mtypes == null || mtypes.containsKey(Folder.type2Enum(folder.getType()))))
				result.add(folder);
			result.addAll(search(p, mtypes, folder));
		}
		return result;
	}

	protected List<Folder> search(String query, Folder.FolderType[] ftypes, Folder folder) {
		return search(Pattern.compile(query), createTypeMap(ftypes), folder);
	}

	protected List<Folder> search(Pattern p, EnumMap<Folder.FolderType, Folder.FolderType> mtypes, Folder folder) {
		List<Folder> result = new ArrayList<Folder>();
		if (folder.getType() == Folder.FOLDER || folder.getType() == Folder.ANY) {
			for (Object element : folder.getContent()) {
				if (element instanceof Folder) {
					Folder tf = (Folder) element;
					if (p.matcher(tf.toString()).matches()
							&& (mtypes == null || mtypes.containsKey(Folder.type2Enum(tf.getType()))))
						result.add(tf);
					result.addAll(search(p, mtypes, tf));
				}
			}
		}
		return result;
	}

	protected EnumMap<Folder.FolderType, Folder.FolderType> createTypeMap(Folder.FolderType[] ftypes) {
		EnumMap<Folder.FolderType, Folder.FolderType> mtypes = null;
		if (ftypes != null && ftypes.length > 0) {
			mtypes = new EnumMap<Folder.FolderType, Folder.FolderType>(Folder.FolderType.class);
			for (Folder.FolderType ftype : ftypes)
				mtypes.put(ftype, ftype);
		}
		return mtypes;
	}

	protected void save(String areaId) {
		List<Folder> folders = userFolders.get(areaId);
		if (folders != null) {
			folders = excludeFolder(folders, AddressBookResources.LABEL_SHARED, Folder.PERSON);
			OutputStream os = null;
			try {
				dataRepositoryBase.backup(areaId);
				dataRepositoryBase.createLock(areaId);
				io.write(folders, os = dataRepositoryBase.getOutStream(areaId, true), "utf-8", DataBookIO.CurrentKey.getCurrent());
				dataRepositoryBase.releaseLock(areaId);
			} catch (DuplicateUser du) {
				du.printStackTrace();
			} catch (Exception ioe) {
				System.err.println("Problem at writing back: " + ioe);
			} finally {
				try {
					os.close();
				} catch (Exception e) {
				}
			}
		}
	}
	
	public void merge(InputStream in, String areaId, Key key) throws AddressException {
		List<Folder> folders = userFolders.get(areaId);
		io.merge(folders, in, key);
	}
	
	public void merge(InputStream in) throws AddressException {
		throw new UnsupportedOperationException("save");
	}

	public void save() {
		throw new UnsupportedOperationException("save");
	}

	public void saveAll() {
		Iterator<String> i = userFolders.keySet().iterator();
		while (i.hasNext())
			save(i.next());
		// save shared
		saveSharedFolder();
	}
	
	/** Removes user's folder data
	 * 
	 * @exception FolderOperationException when data can't be removed
	 */
	public void remove() {
		throw new UnsupportedOperationException("remove");
	}

	public boolean canModifyContacts(Folder folder) {
		return canModifyContacts(folder.toString());
	}

	public boolean canModifyContacts(String folderName) {
		return isShared(folderName) == false;
	}

	public boolean isShared(Folder folder) {
		return folder == null || isShared(folder.toString());
	}

	public boolean isShared(String folderName) {
		return AddressBookResources.LABEL_SHARED.equals(folderName);
	}
	
	// TODO add method canDelete() with owner

	// TODO consider moving to contact operation
	public XMLSaver cloneContact(XMLSaver contact) {
		if (contact == null)
			return null;
		List<Folder> fs = new ArrayList<Folder>(1);
		Folder f = createFolder("temp", Folder.ANY);
		addContact(contact, f);
		fs.add(f);
		ByteArrayOutputStream os;
		try {
			io.write(fs, os = new ByteArrayOutputStream(100), "utf-8");
			fs = io.read(new ByteArrayInputStream(os.toByteArray()));
			return (XMLSaver) fs.get(0).getContent().get(0);
		} catch (AddressException ae) {
			ae.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	protected void assureSharedFolder(List<Folder> result) {
		if (sharedFolder == null)
			readSharedFolder();
		Iterator i = result.iterator();
		String shareFolderName = sharedFolder.toString(); 
		while (i.hasNext())
			if (i.next().equals(shareFolderName))
				i.remove();
		result.add(sharedFolder);
	}
	
	protected void assureLocalFolder(List<Folder> result) {
		if (localContacts != null)
			result.add(localContacts);
	}

	public Folder createFolder(String name, int type) {
		if (name == null)
			throw new NullPointerException("Name of folder null");
		Folder result = type == Folder.PERSON ? new Folder<Contact>(name) : new Folder<XMLSaver>(name);
		result.setType(type);
		return result;
	}

	protected void assureTrashFolder(List<Folder> folders) {
		for (Folder folder : folders) {
			if (folder.equals(AddressBookResources.LABEL_TRASH)) {
				folder.setType(Folder.ANY);
				return;
			}
		}
		folders.add(createFolder(AddressBookResources.LABEL_TRASH, Folder.ANY));
	}

	protected List<Folder> excludeFolder(List<Folder> folders, String folderName, int type) {
		List<Folder> result = new ArrayList<Folder>(folders.size());
		for (Folder folder : folders)
			if (folder.equals(folderName) == false)
				result.add(folder);
		return result;
	}

	protected List<Folder> createStandardFolders() {
		List<Folder> result = new ArrayList<Folder>();
		// temporary creating personal folder
		Folder<Contact> folder;
		result.add(createFolder(AddressBookResources.LABEL_PERSONS, Folder.PERSON));
		// result.add(createFolder(AddressBookResources.LABEL_TRASH,
		// Folder.ANY));
		return result;
	}

	protected void readSharedFolder() {
		InputStream is = null;
		try {
			List<Folder> sfs = (List<Folder>) io.read(is = sharedRepositoryBase
					.getInStream(AddressBookResources.LABEL_SHARED));
			for (Folder f : sfs) {
				if (f.equals(AddressBookResources.LABEL_SHARED)) {
					sharedFolder = f;
					if (sharedFolder.getType() == Folder.PERSON) {
						System.err.printf("Wrong type %d of 'shared' folder corrected to 'ANY'\n", sharedFolder
								.getType());
						sharedFolder.setType(Folder.ANY);
					}
					break;
				}
			}
		} catch (AddressException ae) {
			System.err.printf("Problem in loading shared folders %s\n", ae);
		} catch (IOException ioe) {
			System.err.printf("IO exception at loading shared folders %s\n", ioe);
		} finally {
			try {
				is.close();
			} catch (Exception e) {
			}
		}
		if (sharedFolder == null)
			sharedFolder = createFolder(AddressBookResources.LABEL_SHARED, Folder.PERSON);
	}
	
	/** Actual method for removing user's data
	 * 
	 * @param areaId
	 */
	protected void remove(String areaId) {
		if (dataRepositoryBase.delete(areaId, true) == false)
			System.err.println("Problem in removing data for "+areaId);
	}

	protected void saveSharedFolder() {
		List<Folder> folders = new ArrayList<Folder>();
		folders.add(sharedFolder);
		OutputStream os = null;
		try {
			sharedRepositoryBase.backup(AddressBookResources.LABEL_SHARED);
			io.write(folders, os = sharedRepositoryBase.getOutStream(AddressBookResources.LABEL_SHARED, true), "utf-8",
					SHARE_MAP_ATTR);
		} catch (IOException ioe) {
			System.err.println("IO problem at writing back: " + ioe);
		} catch (NullPointerException npe) {
		} catch (DuplicateUser du) {
			du.printStackTrace();
		} finally {
			try {
				os.close();
			} catch (Exception e) {
			}
		}

	}

	protected boolean exeedFolderCapacity(Folder folder) {
		return folder.size() >= folderCapacity;
	}

	/**
	 * utility method to massage query to be regexp query valid
	 * 
	 * @param query
	 *            to massage
	 * @return massaged query
	 */
	public static String massagePattern(String query) {
		int l;
		if (query == null || (l = query.length()) == 0)
			return query;
		StringBuffer sb = new StringBuffer(query.length());
		boolean changed = false;
		for (int i = 0; i < l; i++) {
			char c = query.charAt(i);
			if (c > '\u007f') {
				changed = true;
				String hs = Integer.toHexString(c);
				int hl = hs.length();
				sb.append("\\u");
				if (hl == 3)
					sb.append('0');
				else if (hl == 2)
					sb.append("00");
				sb.append(hs); 
			} else
				sb.append(c);
		}//System.err.println("ppp:"+sb);
		return changed ? sb.toString() : query;
	}
}
