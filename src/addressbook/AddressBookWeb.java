/* jaddressbook - AddressBookWeb.java
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
 *  $Id: AddressBookWeb.java,v 1.3 2007/02/09 07:27:49 rogatkin Exp $                
 *  Created on May 6, 2006
 *  @author Dmitriy
 */
package addressbook;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.aldan3.servlet.Main;
import org.aldan3.servlet.Static;

public class AddressBookWeb extends Main {
	public static final String CONFIGURATION = Static.IP_PROPERTIES;
	@Override
	public void init(ServletConfig _config) throws ServletException {
		super.init(_config);
		this.getServletContext().setAttribute(CONFIGURATION, getProperties());
	}

	@Override
	public String toString() {
		return this.getServletInfo();
	}
}
