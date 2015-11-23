/* jaddressbook - Collectionmc.java
 * Copyright (C) 1999-2006 Dmitriy Rogatkin.  All rights reserved.
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
 *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 *  DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 *  SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 *  CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 *  LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 *  OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 *  SUCH DAMAGE.
 *  
 *  Visit http://jaddressbook.sourceforge.net to get the latest infromation
 *  about Rogatkin's products.
 *  $Id: Collectionmc.java,v 1.19 2007/11/27 21:32:13 rogatkin Exp $
 *  Created on Sep 22, 2006
 *  @author Dmitriy
 */
package addressbook.servlet;

import static addressbook.Contact.NAME;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aldan3.util.TreeViewHelper;

import addressbook.COI;
import addressbook.Folder;
import addressbook.GenericAttribute;
import addressbook.Media;
import addressbook.servlet.model.FolderOperations;
import addressbook.servlet.model.GenericOperations;
import addressbook.servlet.model.UserProfile;
import addressbook.servlet.model.UserOperations.NonExistingUser;

/**
 * Thus class provide model/controller for personal record element as a list of items.
 * 
 * @author dmitriy
 * 
 */
public class Collectionmc extends AddressBookProcessor {

	// TODO consider common parrent recordmc for contactms, collectionmc

	@Override
	protected String getSubmitPage() {
		// TODO reuse used by Contactview
		try {
			return treeStateEncode("Abfrontview?nodeid="
					+ URLEncoder.encode(getStringParameterValue(P_FOLDER, "", 0), "UTF-8"), TreeViewHelper.HV_STATE, "");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("" + e);
		}
	}

	@Override
	protected Map getModel() {
		Map result = new HashMap();
		String folderName = getStringParameterValue(P_FOLDER, getStringParameterValue(P_NODE, "", 0), 0);
		result.put(P_FOLDER, folderName);
		result.put(TreeViewHelper.HV_STATE, getStringParameterValues(TreeViewHelper.HV_STATE));
		FolderOperations fo = getFolderOperations();
		return renderElements(result, getCollectionOperations().getContact(fo, folderName, NAME, P_HASH, this),
				getDisplayColumns());
	}

	@Override
	protected Map doControl() {
		String folderName = getStringParameterValue(P_FOLDER, getStringParameterValue(P_NODE, "", 0), 0);
		FolderOperations fo = getFolderOperations();
		COI coi = getCollectionOperations().getContact(fo, folderName, Contactview.P_BEFORENAME, P_HASH, this);
		// TODO introduce securityOperations.isChangable(user, folder, folderoperation)
		if (coi == null) {
			if (fo.isShared(folderName))
				return null;
			coi = new COI();
			List<Folder> folders = fo.search(GenericOperations.escapeMask(folderName), null);
			log("A new collection " + coi + " will be added in first folder in " + folders, null);
			if (folders.size() == 1)
				fo.addContact(coi, folders.get(0));
			else
				log("No folders to insert " + coi + " found, requested target folder was:" + folderName, null);
		}
		if (fo.isShared(folderName) && coi.getOwner().equals(getSession().getAttribute(HV_USER_ID)) == false)
			return null;
		coi.setAttribute(GenericAttribute.VALUE, getStringParameterValue(NAME, "unnamed", 0));
		coi.setAttribute(GenericAttribute.DESCRIPTION, getStringParameterValue(GenericAttribute.DESCRIPTION + "1", "",
				0));

		return null;
	}

	public Map processopenItemCall() {
		Map result = new HashMap<String, Object>();
		String folderName = getStringParameterValue(P_FOLDER, getStringParameterValue(P_NODE, "", 0), 0);
		COI coi = getCollectionOperations().getContact(getFolderOperations(), folderName, NAME, P_HASH, this);
		Media m = null;
		if (coi != null) {
			m = coi.getItem(getIntParameterValue("item", -1, 0));
		}
		if (m != null)
			result.put("element", m);
		List<Map> items = new ArrayList<Map>(10); // get exact size
		for (String attr : Media.buildCollectionFieldNames(Media.class)) {
			Map<String, Object> em = new HashMap<String, Object>(4);
			em.put(HV_NAME, attr);
			em.put(HV_LABEL, this.getResourceString(attr, attr)); // user locale ?
			if (m != null)
				em.put(HV_VALUE, m.getAttribute(attr));
			items.add(em);
		}
		result.put("items", items);
		return result;
	}

	public String getopenItemViewName() {
		return "item.htm";
	}

	public Map processupdateListCall() {
		// TODO doesn't work right on just created entry, better to create helper methods:
		// 1. find or create COI
		// and use them instead of validateFormData and producePageData and use in these methods too
		boolean delete = getStringParameterValue(P_DELETE, null, 0) != null;
		if (delete == false)
			doControl();
		boolean sort = getStringParameterValue("sort", null, 0) != null;
		String folderName = getStringParameterValue(P_FOLDER, getStringParameterValue(P_NODE, "", 0), 0);
		COI coi = getCollectionOperations().getContact(getFolderOperations(), folderName, NAME, null, this);
		if (coi != null && (getFolderOperations().isShared(folderName) == false || coi.getOwner().equals(getSession().getAttribute(HV_USER_ID)))) {
			if (delete) {
				String[] deleteMarks = this.getStringParameterValues("entry");
				if (deleteMarks != null) {
					for (String deleteId : deleteMarks)
						try {
							coi.deleteItem(coi.getItem(Integer.parseInt(deleteId)));
						} catch (Exception e) {
							log("Failed to delete " + deleteId, e);
						}
				}
			} else if (!sort) { // TODO sort can be applied after adding elements, so the solution should be reconsidered
				Media m = coi.getItem(getIntParameterValue("item", -1, 0));
				if (m == null) {
					m = new Media(null, null, null);
					coi.addItem(m);
				}
				for (String attr : Media.buildCollectionFieldNames(Media.class)) {
					String val = getStringParameterValue(attr, null, 0);
					if (val != null)
						m.setAttribute(attr, val);
				}
			}
		} else
			log(String.format("Can't find an entry for processing in %s", folderName), null);
		return renderElements(null, coi, getDisplayColumns());
	}

	public String getupdateListViewName() {
		return "list.htm";
	}

	// ///////////////////// helper methods ////////////////////////////

	protected Map renderElements(Map result, COI coi, String[] displayColumns) {
		if (result == null)
			result = new HashMap();

		if (coi != null) {
			log("".format("Rendering element %s", coi), null);
			result.put(V_CONTACT, coi);

			// coi.addItem(new Media("Star WARs III", "Geart", "SciFi"));
			// TODO add search and sort constraints
			List<Map<String, Object>> entries = new ArrayList<Map<String, Object>>();
			for (Media media : coi.getItems()) {
				List<Object> elements = new ArrayList<Object>();
				for (String name : displayColumns) {
					Object val = media.getAttribute(name);
					elements.add(val != null ? val : " ");
				}
				Map<String, Object> m = new HashMap<String, Object>(2);
				m.put("items", elements);
				m.put(HV_ELEMENT, media);
				entries.add(m);
			}
			// if sort specified
			Collections.sort(entries, new MediaEntriesComparator(getStringParameterValue("sort", null, 0), getIntParameterValue("acs", 1, 0) == 1));
			result.put(V_ENTRIES, entries);
		} else
            result.put(V_CONTACT, new COI());
		Contactview.addTypeList(result, "headers", displayColumns, this);
		return result;
	}

	/*protected String[] convert2labels(String[] displayColumns) {
		String[] result = new String[displayColumns.length];
		for (int i = 0; i < displayColumns.length; i++)
			result[i] = getResourceString(displayColumns[i], displayColumns[i]);
		return result;
	}*/
	
	protected String[] getDisplayColumns() {
		String[] displayColumns = null;

		try {
			displayColumns = this.getUPOperations().getUser((String) getSessionAttribute(HV_USER_ID, null))
					.getStringAttribute(UserProfile.MEDIA_COLUMNS).split(",");
		} catch (NonExistingUser e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NullPointerException npe) {

		}
		if (displayColumns == null)
			displayColumns = Media.buildCollectionFieldNames(Media.class);
		return displayColumns;

	}

	static class MediaEntriesComparator implements Comparator<Map<String, Object>> {
		private String sortAttr;

		int mulFact = 1;
		
		public MediaEntriesComparator(){}
		
		public MediaEntriesComparator(String sort, boolean desc) {
			sortAttr = sort;
			mulFact = desc?1:-1; 
		}
		// TODO add a switch for different comparisions
		public int compare(Map<String, Object> entry1, Map<String, Object> entry2) {
			Media m1 = (Media) entry1.get("element");
			Media m2 = (Media) entry2.get("element");
			if (m1 == null)
				if (m2 == null)
					return 0;
				else
					return -1;
			else if (m2 == null)
				return 1;

			if (sortAttr == null)
				return m1.getValue().compareToIgnoreCase(m2.getValue());
			else {
				try {
					return mulFact*((Comparable) m1.getAttribute(sortAttr)).compareTo(m2.getAttribute(sortAttr));
				} catch (NullPointerException npe) {
					if (m2.getAttribute(sortAttr) == null)
						return 0;
					return -mulFact;
				}
			}
		}
	}
}
