/* AddressBook - TabbedArrayOfComponents
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
 * $Id: TabbedArrayOfComponents.java,v 1.3 2006/05/10 06:17:37 rogatkin Exp $
 */
package addressbook;
import java.util.*;
import javax.swing.*;

public abstract class TabbedArrayOfComponents extends JTabbedPane {

		TabbedArrayOfComponents(int tabPlacement) {
			super(tabPlacement);
		}

		abstract JComponent produce();

		void addElement(String name) {
			if (indexOfTab(name) > 0) {
				setSelectedIndex(indexOfTab(name));
				return;
			}
			addTab(name, produce());
		}
		
		void modifyElement(String name, String newname) {
			int i = indexOfTab(name);
			if (i >= 0)
				setTitleAt(i, newname);
		}
		
		void modifyElement(String newname) {
			int i = getSelectedIndex();
			if (i >= 0)
				setTitleAt(i, newname);
		}
		
		void deleteElement() {
			int i = getSelectedIndex();
			if (i >= 0)
				removeTabAt(i);
		}

		void deleteElement(String name) {
			int i = indexOfTab(name);
			if (i >= 0)
				removeTabAt(i);
		}

		JComponent getElement(String name) {
			int i = indexOfTab(name);
			if (i < 0)
				return null;
			return (JComponent)getComponentAt(i);
		}
	}
