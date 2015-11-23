/* jaddressbook - Propertiesview.java
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
 *  $Id: Settingsview.java,v 1.11 2007/12/12 06:48:48 dmitriy Exp $                
 *  Created on Nov 28, 2005
 */
package addressbook.servlet;

import java.security.Key;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import addressbook.servlet.model.CertificateOperations;
import addressbook.servlet.model.KeyStorage;

// import javax.servlet.ServletException;
// import javax.servlet.UnavailableException;

/**
 * @author Dmitriy
 * 
 */
public class Settingsview extends AddressBookProcessor {

	/*
	 * (non-Javadoc)
	 * 
	 * @see rogatkin.servlet.BaseFormProcessor#producePageData()
	 */
	@Override
	protected Map getModel() {
		Map result = new HashMap();
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see rogatkin.servlet.BaseFormProcessor#validateFormData()
	 */
	@Override
	protected Map doControl() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see rogatkin.servlet.BaseFormProcessor#getSubmitPage()
	 */
	@Override
	protected String getSubmitPage() {
		return "Abfrontview";
	}

	public String processGenCall() {
		CertificateOperations co = this.getCertificateOperations();
		Key k = co.createKey();
		KeyStorage ks = (KeyStorage) req.getSession().getAttribute(KeyStorage.SESS_ATR);
		if (ks == null)
			return "Error";
		ks.setGenKey((SecretKey)k);
		return V_OK;
	}

	public String processClrCall() {
		KeyStorage ks = (KeyStorage) req.getSession().getAttribute(KeyStorage.SESS_ATR);
		if (ks != null)
			ks.setGenKey(null);
		return V_OK;
	}
	
	public String processAplCall() {
		KeyStorage ks = (KeyStorage) req.getSession().getAttribute(KeyStorage.SESS_ATR);
		if (ks == null || ks.getKey() != null)
			return getResourceString("not_downloaded", "Secret key has not been downloaded");
		ks.applyNewKey();
		return getResourceString("OK", V_OK);
	}

	public String processUplCall() {
		if (Login.applyKey(getSession(), this))
			return getResourceString("OK", V_OK);
		return getResourceString("error", "Error");
	}
}
