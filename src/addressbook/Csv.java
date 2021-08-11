/* AddressBook - Csv.java
 * Copyright (C) 1999-2004 Dmitriy Rogatkin.  All rights reserved.
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
 *  $Id: Csv.java,v 1.3 2007/02/09 07:27:49 rogatkin Exp $
 * Created on Aug 6, 2004
 */

package addressbook;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
//import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import org.aldan3.util.CsvTokenizer;

/**
 * @author dmitriy
 *
 * 
 */
public class Csv {
	protected boolean mode;
	protected CsvTokenizer tokenizer;
	protected Map header;
	protected List row;
	/** Opens CSV file for processing
	 * 
	 * @param is InpuStream of CSV 
	 * @param direct boolean set to true if CSV file isn't big and you need a direct access
	 * @param encoding optional encoding, can be null
	 * @param separator separator is differ from ','
	 * @param withHeader tells that 1st line of CSV is a header
	 */
	public Csv(InputStream is, boolean direct, String encoding, char separator, boolean withHeader) throws IOException {
		tokenizer = new CsvTokenizer(null, new BufferedReader(new InputStreamReader(is, encoding==null?"iso-8859-1":encoding)),
				""+separator, false, 0);
		if (withHeader) {
			header = new HashMap();
			for(int col=1; tokenizer.hasMoreTokens(); col++) {
				header.put(tokenizer.nextToken(), new Integer(col));
			}
		}
	}
	
	public Map getMetaData() throws IOException {
		if (header == null)
			throw new IOException("No metadata requested, or available.");
		return header;
	}
	
	public boolean next() throws IOException {
		boolean result = tokenizer.advanceToNextLine();
		if (result) {
			row = new ArrayList(); // TODO: reuse previous row
			while(tokenizer.hasMoreTokens())
				row.add(tokenizer.nextToken());
		} else
			row = null;
		return result;
	}
	
	public String getString(int index) throws IOException {
		if (row == null)
			throw new IOException("Set exhausted."); 
		if (index < 1 || index >= row.size())
			return null;
		return (String)row.get(index-1);
	}
	
	public String getString(String colName) throws IOException {
		if (header == null)
			throw new IOException("MetaData info is not available.");
		Integer index = (Integer)header.get(colName);
		if (index == null)
			throw new IOException("Non-existent column name "+colName+".");
		return getString(index.intValue());
	}
}
