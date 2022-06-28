package addressbook.servlet;

import static addressbook.servlet.model.UserProfile.NAME;

import java.util.HashMap;
import java.util.Map;

import addressbook.servlet.model.UserOperations.NonExistingUser;
import addressbook.servlet.model.UserProfile;

public class Recover extends AddressBookProcessor {
	@Override
	protected Map getModel() {
		try {
			UserProfile up = getUPOperations().getUser(getStringParameterValue(NAME, null, 0));
		
			Map result = fillWithForm(new HashMap(), NAME);
			result.put("secret_question_hint", up.getStringAttribute(UserProfile.SECRET_QUESTION).substring(0, 3));
		return result;
		} catch(NonExistingUser neu) {
			return createErrorMap("error_nousername");
		}
	}
	
	@Override
	protected Map doControl() {
		try {
			Thread.sleep(3*1000); // to avoid a brute force attack
		} catch(Exception e) {
		
		}
		try {
			UserProfile up = getUPOperations().getUser(getStringParameterValue(NAME, null, 0));
			// TODO change to store and validate an answer hash
			if (up.getStringAttribute(UserProfile.SECRET_QUESTION).equals(up.getStringAttribute(UserProfile.SECRET_QUESTION).substring(0, 3)
					+getStringParameterValue(UserProfile.SECRET_QUESTION, "", 0)) 
					&& up.getStringAttribute(UserProfile.SECRET_ANSWER).equals(getStringParameterValue(UserProfile.SECRET_ANSWER, "", 0))) {
				setAllowed(true);
				Login.logged(this,  up);
			return null;
			} else
				return createErrorMap("error_wronganswer");
		} catch(NonExistingUser neu) {
			return createErrorMap("error_nousername");
		}
	}
	
	@Override
	protected String getViewName() {
		return getViewName("recover.htm");
	}

	@Override
	protected String getSubmitPage() {
		return "Registry?mode=edit"; // change password
	}

	public boolean isPublic() {
		return true;
	}
}
