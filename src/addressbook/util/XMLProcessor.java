/* jaddressbook - XMLProcessor.java
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
 *  $Id: XMLProcessor.java,v 1.1 2006/05/10 06:17:40 rogatkin Exp $                
 *  Created on May 3, 2006
 *  @author dmitriy
 */
package addressbook.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class XMLProcessor<T> extends DefaultHandler {
	protected List<Saxable> regestrants = new ArrayList<Saxable>(10);

	protected Stack<Saxable> current = new Stack<Saxable>();

	protected T result;

	public T parse(InputStream is) throws SAXException, IOException {
		SAXParserFactory parserFactory = SAXParserFactory.newInstance();
		//parserFactory.setValidating(true);
		try {
			parserFactory.newSAXParser().parse(is, this);		
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	public void register(Saxable sax) {
		regestrants.add(sax);
	}

	public void unregister(Saxable sax) {
		regestrants.remove(sax);
	}

	@Override
	public void characters(char[] arg0, int arg1, int arg2) throws SAXException {
		if (current.isEmpty() == false)
			current.peek().characters(arg0, arg1, arg2);
		else
			super.characters(arg0, arg1, arg2);
	}

	@Override
	public void endDocument() throws SAXException {
		// TODO Auto-generated method stub
		super.endDocument();
	}

	@Override
	public void endElement(String arg0, String arg1, String arg2) throws SAXException {
		if (current.isEmpty() == false) {
			Object r = current.pop().endElement(arg0, arg1, arg2);
			if (current.isEmpty() == false)
				current.peek().endChild(r);
			else
				result = (T)r;
		} else
			super.endElement(arg0, arg1, arg2);
	}

	@Override
	public void startDocument() throws SAXException {
		super.startDocument();
	}

	@Override
	public void startElement(String arg0, String arg1, String arg2, Attributes arg3) throws SAXException {
		for (Saxable regestrant: regestrants)
			if (regestrant.startElement(arg0, arg1, arg2, arg3)) {
				current.push(regestrant);
				return;
			}
		super.startElement(arg0, arg1, arg2, arg3);
	}

}
