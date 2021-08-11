/* jaddressbook - CipherOperations.java
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
 *  $Id: CipherOperations.java,v 1.5 2008/07/20 21:23:32 dmitriy Exp $                
 *  Created on Dec 15, 2005
 *  @author dmitriy
 */
package addressbook.servlet.model;

import org.aldan3.util.Crypto;

import addressbook.servlet.AddressBookProcessor;

public class CipherOperations extends AbstractOperations {

	private Crypto crypto;

	@Override
	public void init(AddressBookProcessor abp) {
		init(abp.getProperty(UserOperations.P_CIPHERSEED, "11111"));
	}

	protected void init(String password) {
		crypto = new Crypto(password);
	}

	public String encrypt(String src) {
		return crypto.encrypt(src);
	}

	public String decrypt(String src) {
		return crypto.decrypt(src);
	}

	public static void main(String... strings) {
		if (strings.length != 2) {
			System.out.println("Use: bee crypt -- seed passcode");
			System.exit(-1);
		}
		CipherOperations co = new CipherOperations();
		co.init(strings[0]);
		System.out.println(co.encrypt(strings[1]));
	}
}
