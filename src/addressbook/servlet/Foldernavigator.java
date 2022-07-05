package addressbook.servlet;

import java.util.HashMap;
import java.util.List;

import addressbook.Folder;
import addressbook.servlet.model.GenericOperations;

public class Foldernavigator extends AddressBookProcessor {

	@Override
	protected Object doControl() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Object getModel() {
		HashMap<String, Object> model = new HashMap<String, Object>();
		String parentFolderPath = getStringParameterValue(P_NODE, null, 0);
		if (parentFolderPath == null || parentFolderPath.length() == 0) {
			model.put(P_FOLDER, getFolderOperations().search("", null));
			model.put(HV_PARENT, parentFolderPath);
		} else {
			List<Folder> parents = getFolderOperations().search("", null);
			Folder folder = getFolder(parentFolderPath, parents);
			if (folder != null) {
				model.put(P_FOLDER, getFolderOperations().getFolders(folder));
				model.put(HV_PARENT, folder);
			}
		}
		//log("folders of "+parentFolderPath+" are "+model.get(P_FOLDER), null);

		return model;
	}

	@Override
	protected String getSubmitPage() {
		// TODO Auto-generated method stub
		return null;
	}
	
	protected Folder getFolder(String folderName, List<Folder> folders) {
		if (folders == null)
			return null;
		for (Folder folder : folders) {
			if (folder.toString().equals(folderName))
				return folder;
		}
		return null;
	}

}
