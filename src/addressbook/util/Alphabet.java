/* jaddressbook - Alphabet
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
 *  $Id: Alphabet.java,v 1.2 2006/07/06 06:23:23 rogatkin Exp $
 * Created on Nov 25, 2005
 */
package addressbook.util;

import java.io.Serializable;
import java.util.Locale;

public class Alphabet implements Serializable, Cloneable {

	public static final Alphabet RUSSIAN = new Alphabet(new Locale("ru"));

	public static final Alphabet ENGLISH = new Alphabet(Locale.ENGLISH);

	private Locale locale;

	public Alphabet(Locale locale) {
		if (locale == null)
			throw new NullPointerException("Locale null");
		this.locale = locale;
	}

	public static Alphabet[] getAvailableAlphabets() {
		return new Alphabet[] { ENGLISH, RUSSIAN };
	}

	public char getFirst() {
		String lang = locale.getLanguage();
		if (lang.length() == 0 || lang.equals(Locale.ENGLISH.getLanguage()))
			return 'A';
		else if (lang.equals("ru"))
			return '\u0410';
		System.err.println("Alpha first - Language '"+lang+"' not supported, English defaulted.");
		return 'A';
//		throw new RuntimeException("Language '"+lang+"' not supported.");
	}

	public char getLast() {
		String lang = locale.getLanguage();
		if (lang.length() == 0 || lang.equals(Locale.ENGLISH.getLanguage()))
			return 'Z';
		else if (lang.equals("ru"))
			return '\u042F';
		System.err.println("Alpha last - Language '"+lang+"' not supported, English defaulted.");
		return 'Z';
//		throw new RuntimeException("Language '"+lang+"' not supported.");
	}

	public char getAfter(char c) {
		if (c >= getFirst() && c < getLast() 
				|| c >= Character.toLowerCase(getFirst()) && c < Character.toLowerCase(getLast()))
			return ++c;
		return 0;
	}
	
	public char getBefore(char c) {
		return 0;
	}
	
	public boolean isIn(char c) {
		// return c >= getFirst() && c <= getLast()
		// || c >= Character.toLowerCase(getFirst()) && c <=
		// Character.toLowerCase(getLast());
		return isUpperCase(c) || isLowerCase(c);
	}

	public int size() {
		// note commonly return getLast() - getFirst() won't work
		int s = 0;
		for (char c = getFirst(); c != 0; c = getAfter(c), s++)
			;
		return s;
	}

	public boolean isUpperCase(char c) {
		return c >= getFirst() && c <= getLast();
	}

	public boolean isLowerCase(char c) {
		return c >= Character.toLowerCase(getFirst()) && c <= Character.toLowerCase(getLast());
	}

	public char toUpperCase(char c) {
		if (isUpperCase(c))
			return c;
		if (isLowerCase(c))
			return Character.toUpperCase(c);
		return 0;
	}

	public char toLowerCase(char c) {
		if (isLowerCase(c))
			return c;
		if (isUpperCase(c))
			return Character.toLowerCase(c);
		return 0;
	}
}
