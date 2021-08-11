/* jaddressbook - Emailvalidator.java
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
 *  $Id: Emailvalidator.java,v 1.3 2008/04/15 23:12:29 dmitriy Exp $
 * Created on Sep 16, 2005
 */
package addressbook.servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import addressbook.servlet.model.UserProfile;
import addressbook.servlet.model.UserOperations.NonExistingUser;
import static addressbook.servlet.model.UserProfile.NAME;

public class Emailvalidator extends AddressBookProcessor {

    public static final String TOKEN = "token";

    @Override
    protected Map getModel() {
        return fillWithForm(new HashMap(), UserProfile.NAME, UserProfile.EMAIL);
    }

    @Override
    protected Map doControl() {
        String id = getStringParameterValue(TOKEN, null, 0);
        if (id == null) {
            return fillWithForm(createErrorMap("error_token"), NAME, TOKEN);
        }
        id = getEmailValidatorOperations().getUser(id);
        if (id == null) {
            return fillWithForm(createErrorMap("error_notoken"), NAME, TOKEN);
        }
        try {
            UserProfile up = getUPOperations().getUser(id);
            up.setAttribute(UserProfile.ACTIVE, "" + true);
            getUPOperations().updateUser(id, up);
            getEmailValidatorOperations().removeUser(getStringParameterValue(TOKEN, null, 0));
            getEmailValidatorOperations().flush();
        } catch (NonExistingUser e) {
            return fillWithForm(createErrorMap("error_expired"), NAME);
        } catch (IOException e) {
            e.printStackTrace();
            return fillWithForm(createErrorMap("error_activate"), NAME);
        }

        return null;
    }

    @Override
    protected String getViewName() {
        return "evalidator.htm";
    }

    @Override
    protected String getSubmitPage() {
        return "Login";
    }

    public boolean isPublic() {
        return true;
    }
}
