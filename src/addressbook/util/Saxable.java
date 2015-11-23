/* jaddressbook - Saxable.java
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
 *  $Id: Saxable.java,v 1.1 2006/05/10 06:17:40 rogatkin Exp $                
 *  Created on May 3, 2006
 *  @author dmitriy
 */
package addressbook.util;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public interface Saxable<T, C> {

	/** called at start of element of regular SAX
	 * parser, returns true if it know how to parse
	 * @param uri
	 * @param localName
	 * @param qName
	 * @param attrs
	 * @return true if ut can be handled
	 */
	boolean startElement(String uri, String localName, String qName, Attributes attrs) throws SAXException;
	
	/** works on value of element
	 * 
	 * @param ch
	 * @param start
	 * @param length
	 * @throws SAXException
	 */
	void characters(char[] ch, int start, int length) throws SAXException;
	
	/** Finishes element
	 * 
	 * @param uri
	 * @param localName
	 * @param qName
	 * @return
	 * @throws SAXException
	 */
	T endElement(String uri,
            String localName,
            String qName)
            throws SAXException;
	
	/** Notification from container child done
	 * 
	 * @param c
	 * @throws SAXException 
	 */
	void endChild(C c) throws SAXException;
}
