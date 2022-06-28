/* jaddressbook - UserProfile.java
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
 *  $Id: UserProfile.java,v 1.4 2007/06/29 00:10:16 rogatkin Exp $
 * Created on Sep 13, 2005
 */

package addressbook.servlet.model;

import org.mindrot.jbcrypt.BCrypt;

public class UserProfile extends AbstractAttributeStorage {
    public static final String NAME = "name";
    public static final String EMAIL = "email";
    private static final String PASSWORD = "password";
    public static final String SECRET_QUESTION = "secret_question";
    public static final String SECRET_ANSWER = "secret_answer";
    public static final String ACCESS_KEY = "access_key";
    //public static final String SIGANTURE = "signature";
    public static final String ADDEDON = "added_on";
    public static final String ACTIVE = "active";
    public static final String LIFETIME = "lifetime";
    public static final String ACCEPTED = "accepted";
    public static final String LANGUAGE = "language";
    public static final String TIMEZONE = "timezone";
    
    // temporary here, then go to user preferences
    public static final String MEDIA_COLUMNS = "media_columns";
    
	@Override
	public Object getId() {
		return getAttribute(NAME);
	}
	
	public void setPassword(String password) {
		if (password != null && !password.isEmpty())
			setStringAttribute(PASSWORD, BCrypt.hashpw(password, BCrypt.gensalt()));
		else
			setAttribute(PASSWORD, null);
	}
	
	public boolean matchPassword(String password) {
		return BCrypt.checkpw(password, getStringAttribute(PASSWORD));
	}
	
	public String getPasswordHash() {
		return getStringAttribute(PASSWORD);
	}
}
