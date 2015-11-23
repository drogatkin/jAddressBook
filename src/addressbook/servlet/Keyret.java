/* jaddressbook - Keyret.java
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
 *  $Id: Keyret.java,v 1.6 2007/12/12 06:58:52 dmitriy Exp $                
 *  Created on Aug 29, 2006
 *  @author Dmitriy
 */
package addressbook.servlet;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.security.Key;
import java.util.Map;

import addressbook.servlet.model.KeyStorage;

public class Keyret extends AddressBookProcessor {

	@Override
	protected String getSubmitPage() {
		return null;
	}

	@Override
	protected Map getModel() {
		resp.setContentType("application/octet-stream");
		resp.setHeader("Content-Disposition", "attachment; filename="+getSession().getAttribute(HV_USER_ID)+"_key.bin");
		resp.setHeader("Content-Description", "Key secret");
		OutputStream os = null;
		ObjectOutputStream oos = null;
		try {			
			KeyStorage ks = (KeyStorage) req.getSession().getAttribute(KeyStorage.SESS_ATR);
			if (ks != null) {
				Key k = ks.getGenKey();
				if (k != null) {
					oos = new ObjectOutputStream(resp.getOutputStream());
					oos.writeObject(k);
					// if private key downloaded it's considered as new key pair will be used
					ks.setKey(null);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (oos != null)
				try {
					oos.close();
				} catch (IOException e) {
				}
		}
		return null;
	}

	@Override
	protected Map doControl() {
		return null;
	}
}
