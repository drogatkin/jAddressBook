/* jaddressbook - KeyStorage.java
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
 *  $Id: KeyStorage.java,v 1.2 2006/09/20 03:46:30 rogatkin Exp $                
 *  Created on Aug 29, 2006
 *  @author Dmitriy
 */
package addressbook.servlet.model;

import java.io.Serializable;
import javax.crypto.SecretKey;

/** This class used for storing keys in session
 * 
 * @author Dmitriy
 *
 */
public class KeyStorage implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6560490159945167327L;

	public static final String SESS_ATR = "key_storage";
	
	private transient SecretKey key;
	
	private transient SecretKey genKey;


	public KeyStorage(SecretKey k) {
		key = k;
	}

	public void setGenKey(SecretKey key1) {
		genKey = key1;
	}

	public SecretKey getGenKey() {
		return genKey;
	}


	public void setKey(SecretKey key) {
		this.key = key;
	}

	public SecretKey getKey() {
		return key;
	}
	
	public void applyNewKey() {
		key = genKey;
		genKey = null;
	}

}
