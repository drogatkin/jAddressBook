package addressbook.servlet;

import java.util.HashMap;

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
		if (parentFolderPath == null || parentFolderPath.length() == 0)
			model.put(P_FOLDER, getFolderOperations().search("", null));
		else
			model.put(P_FOLDER, getFolderOperations().search(parentFolderPath, null));
		return model;
	}

	@Override
	protected String getSubmitPage() {
		// TODO Auto-generated method stub
		return null;
	}

}
