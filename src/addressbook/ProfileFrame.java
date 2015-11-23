/* AddressBook - ProfileFrame 
 * Copyright (C) 2000 Dmitriy Rogatkin.  All rights reserved.
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
 */
package addressbook;
/** This class provide basic location profile, like
 * statring address, time zone, language, dial props
 * and other infromation
 */
import javax.swing.*;

public class ProfileFrame extends JFrame {
	public static final String PROFILE = "DataBookProfile";
	public static final String STREET = "Street";
	public static final String CITY = "City";
	public static final String STATE = "State";
	public static final String ZIP = "ZIP";
	public static final String TIMEZONE = "TimeZone";
	public static final String DIALOUT = "DialOut";
	public static final String AREACODE = "AreaCode";
	public static final String LONGDISTPRFX = "LongDistancePrefix";
	public static final String INTLLONGDISTPRFX = "IntlLongDistancePrefix";
	public static final String CALLCARDACCESS = "CallingCardAccess#";
	public static final String CALLCARDPIN = "CallingCardPIN#";

	ProfileFrame() {
		super(AddressBookResources.TITLE_LOCAL_PROFILE);
		pack();
		setVisible(true);
	}
}
