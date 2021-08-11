/* jaddressbook - BTHelper.java
 * Copyright (C) 1999-2012 Dmitriy Rogatkin.  All rights reserved.
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
 *  $Id: BTHelper.java,v 1.1 2012/01/08 07:00:43 dmitriy Exp $                
 *  Created on Jan 7, 2012
 *  @author Dmitriy
 */
package addressbook.util;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;

public class BTHelper {
	// private boolean available;
	private BluetoothAdapter mBluetoothAdapter;

	private static final int REQUEST_ENABLE_BT = 1;

	public BTHelper() {
		init();
	}

	void init() {
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

	}

	public boolean isAvailable() {
		return mBluetoothAdapter != null;
	}

	public boolean isEnabled() {
		return isAvailable() && mBluetoothAdapter.isEnabled();
	}

	public void enable(Activity activity) {
		if (isEnabled() == false) {
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}
	}

	public Set<String> getPairedDevices() {
		HashSet<String> result = new HashSet<String>();
		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
		// If there are paired devices
		if (pairedDevices.size() > 0) { // Loop through paired devices
			for (BluetoothDevice device : pairedDevices) {
				// Add the name and address to an array adapter to show in a
				// ListView
				result.add(device.getName() + "\n" + device.getAddress());
			}
		}
		return result;

	}

	protected BluetoothSocket connectTo(String deviceName) throws IOException {
		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
		// If there are paired devices
		if (pairedDevices.size() > 0) { // Loop through paired devices
			for (BluetoothDevice device : pairedDevices) {
				if (deviceName.equals(device.getName())) {
					BluetoothSocket tmpsock = null;
					int port = 1;
					try {
						Method m = device.getClass().getMethod("createRfcommSocket", new Class[] { int.class });
						tmpsock = (BluetoothSocket) m.invoke(device, port);

						tmpsock.connect();
						return tmpsock;
					} catch (Exception e) {
						throw new IOException("" + e, e);
					}
				}
			}
		}
		return null;
	}
}
