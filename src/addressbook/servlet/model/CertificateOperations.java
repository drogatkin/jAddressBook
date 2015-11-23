/* jaddressbook - CertificateOperations.java
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
 *  $Id: CertificateOperations.java,v 1.6 2015/10/24 06:30:42 cvs Exp $                
 *  Created on Nov 27, 2005
 */
package addressbook.servlet.model;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.interfaces.DSAParams;
import java.security.interfaces.DSAPrivateKey;
import java.util.Properties;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;

import org.aldan3.util.inet.Base64Codecs;

import addressbook.servlet.AddressBookProcessor;

public class CertificateOperations extends AbstractOperations {

	public final static String DSA = "DSA";

	public final static String DES = "DES";

	public final static String RSA = "RSA";

	public final static String DES_TRANSFORM = "DES/ECB/PKCS5Padding";

	public final static String RSA_TRANSFORM = "RSA/ECB/PKCS1Padding";

	public static final String CIPHER_ALGORITHM_PROP = "CipherAlgorithm";

	public static final String CIPHER_TRANSFORM_PROP = "CipherTransformation";

	static protected Properties properties;

	@Override
	public void init(AddressBookProcessor abp) {
		properties = abp.getProperties();
		// use password
	}

	public Key createKey() {
		KeyGenerator keygen;
		try {
			keygen = KeyGenerator.getInstance(properties.getProperty(CIPHER_ALGORITHM_PROP, DES));
			return keygen.generateKey();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public OutputStream encryptStream(OutputStream os, Key key) {
		Cipher c = null;
		try {
			c = Cipher.getInstance(properties.getProperty(CIPHER_TRANSFORM_PROP, DES_TRANSFORM));
			c.init(Cipher.ENCRYPT_MODE, key);
			return new CipherOutputStream(os, c);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return os;
	}

	public InputStream decryptStream(InputStream is, Key key) {
		Cipher c = null;
		try {
			c = Cipher.getInstance(properties.getProperty(CIPHER_TRANSFORM_PROP, DES_TRANSFORM));
			c.init(Cipher.DECRYPT_MODE, key);
			return new CipherInputStream(is, c);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return is;
	}

	/** Encrypt string using a key
	 * 
	 * @param s sourse 
	 * @param key
	 * @return base64 encoded encrypted string, or null if any errors happened
	 * @exception NullPointerException if s is null
	 */
	public static String encryptString(String s, Key key) {
		// TODO possible inconsistency cipher not matching to a key
		try {
			Cipher cipher = Cipher.getInstance(properties.getProperty(CIPHER_TRANSFORM_PROP, DES_TRANSFORM));
			cipher.init(Cipher.ENCRYPT_MODE, key);
			return Base64Codecs.base64Encode(cipher.doFinal(s.getBytes("UNICODE")));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/** Decrypt a string using key
	 * 
	 * @param s encrypted and base64 encoded string
	 * @param k a key
	 * @return decrypted string or null if not possible
	 * @exception NullPointerException if string parameter or a key are null
	 */
	public static String decryptString(String s, Key k) {
		Cipher cipher;
		try {
			cipher = Cipher.getInstance(properties.getProperty(CIPHER_TRANSFORM_PROP, DES_TRANSFORM));
			cipher.init(Cipher.DECRYPT_MODE, k);
			return new String(cipher.doFinal(Base64Codecs.decode64(s)), "UNICODE");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return null;
	}

	protected void storeKey(File _file, boolean _private) {
		try {
			Properties key = new Properties();
			// key.setProperty("P", p.toString(16));
			// key.setProperty("G", g.toString(16));
			// key.setProperty("Q", q.toString(16));
			// key.setProperty(_private?"X":"Y", (_private?x:y).toString(16));
			// key.store(new FileOutputStream(_file), "MetricStream, Inc. "+(_private?"private":"public")+" key.");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public KeyPair createCertificate(String algorithm) throws NoSuchAlgorithmException {
		KeyPairGenerator kpg = KeyPairGenerator.getInstance(algorithm);
		SecureRandom sr = new SecureRandom();
		kpg.initialize(512, new SecureRandom(sr.generateSeed(8)));
		return kpg.genKeyPair();
	}

	class A implements DSAPrivateKey /* DSAKey */{
		byte[] k;

		DSAParams param;

		BigInteger x;

		public A(File _key) {
			try {
				Properties kp = new Properties();
				// kp.load(new FileInputStream(_key));
				x = new BigInteger(kp.getProperty("X"), 16);
				param = new B(new BigInteger(kp.getProperty("P"), 16), new BigInteger(kp.getProperty("Q"), 16),
						new BigInteger(kp.getProperty("G"), 16));
			} catch (Exception e) {
				System.err.println("problem reading a key." + e);
			}
		}

		public String getAlgorithm() {
			return DSA;
		}

		public String getFormat() {
			return "X.509";
		}

		public byte[] getEncoded() {
			return k;
		}

		public DSAParams getParams() {
			return param;
		}

		public BigInteger getX() {
			return x;
		}
	}

	class B implements DSAParams {
		BigInteger p, q, g;

		B(BigInteger _p, BigInteger _q, BigInteger _g) {
			p = _p;
			q = _q;
			g = _g;
		}

		public BigInteger getP() {
			return p;
		}

		public BigInteger getQ() {
			return q;
		}

		public BigInteger getG() {
			return g;
		}
	}

}
