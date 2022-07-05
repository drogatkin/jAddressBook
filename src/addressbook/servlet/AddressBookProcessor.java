/* jaddressbook - AddressBookProcessor.java
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
 *  $Id: AddressBookProcessor.java,v 1.28 2013/02/13 03:23:35 cvs Exp $
 * Created on Sep 15, 2005
 */

package addressbook.servlet;

import java.io.UnsupportedEncodingException;
import java.io.File;
import java.net.URLEncoder;
import java.security.Key;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.http.HttpSession;

import org.aldan3.model.TemplateProcessor;
import org.aldan3.servlet.BasePageService;
import org.aldan3.servlet.Constant;
import org.aldan3.servlet.Static;
import org.aldan3.util.ResourceException;
import org.aldan3.util.ResourceManager;
import org.aldan3.util.TreeViewHelper;
import org.aldan3.app.Env;

import addressbook.COI;
import addressbook.Contact;
import addressbook.VersionConstant;
import addressbook.servlet.model.AbstractAttributeStorage;
import addressbook.servlet.model.AbstractOperations;
import addressbook.servlet.model.AttachmentOperations;
import addressbook.servlet.model.AttributeStorage;
import addressbook.servlet.model.CertificateOperations;
import addressbook.servlet.model.CipherOperations;
import addressbook.servlet.model.EmailValidatorOperations;
import addressbook.servlet.model.FolderOperations;
import addressbook.servlet.model.GenericOperations;
import addressbook.servlet.model.KeyStorage;
import addressbook.servlet.model.LogOperations;
import addressbook.servlet.model.PersonalFolderOperations;
import addressbook.servlet.model.UserOperations;

public abstract class AddressBookProcessor extends BasePageService {
	public static final String P_FOLDER = "folder";

	public static final String P_HASH = "hash";

	public static final String P_NODE = "nodeid"; // "folder";

	public static final String P_DELETE = "delete";

	public static final String P_SHARE = "share";

	public static final String P_GET_THEM = "getthem";

	public static final String P_SEARCH = "search";

	static final String P_MODE = "mode";

	public static final String V_CONTACT = "contact";

	public static final String V_TYPE = "type";

	public static final String V_INDEX = "index";

	public static final String V_READONLY = "readonly";

	public static final String V_OK = "Ok";

	public static final String V_ENTRIES = "entries";

	public static final String V_CIPHER = "cipher";

	public static final String HV_LABEL = "label";

	public static final String HV_NAME = "name";

	public static final String HV_VALUE = "value";

	public static final String HV_ELEMENT = "element";

	public static final String HV_MEMBER = "member";
	
	public static final String HV_PARENT = "parent";
	
	//public static final String HV_APP = "app";

	public static final String USERRECORD = "addressbook.servlet.user_records";

	public static final String EMAILVALIDATOR = "addressbook.servlet.email_validator";

	public static final String CONTACTOPER = "addressbook.servlet.contact_operations";

	public static final String FOLDEROPER = "addressbook.servlet.folder_operations";

	public static final String CIPHEROPER = "addressbook.servlet.cipher_operations";

	public static final String LOGOPER = "addressbook.servlet.log_operations";

	public static final String CERTOPER = "addressbook.servlet.certificate_operations";

	public static final String ATTR_SESSION_FOLDER_OPER = FOLDEROPER;

	// properties
	public static final String DATAROOT = "DATAROOT";

	public static final String BLOG = "BLOG";

	/**
	 * used to store in session last login
	 * 
	 */
	public static final String ATTR_LASTLOGIN = "lastlogin";
	
	public static final String ATTR_WEBMOBILE = "webmobile";

	public static final String ABANDONED_AFTER = "ABANDONED_AFTER";

	public static final String HV_USER_ID = "user_id";

	protected UserOperations userOperationsCache;

	protected EmailValidatorOperations emailValidatorOperations;

	protected GenericOperations recordOperationsCache;

	protected FolderOperations folderOperationscache;

	protected CipherOperations cipherOperationsCache;

	protected LogOperations logOperationsCache;

	protected CertificateOperations certOperCache;
	
	protected boolean mobile;

	public HttpSession getSession() {
		return req.getSession(); // TODO think if creation session has no side affect 
	}

	// TODO user Registry to unify access to services from different places
	// Registry stored in context, everything else in Registry
	protected UserOperations getUPOperations() {
		if (userOperationsCache != null)
			return userOperationsCache;
		userOperationsCache = getOperations(USERRECORD, UserOperations.class);
		return userOperationsCache;
	}

	protected EmailValidatorOperations getEmailValidatorOperations() {
		if (emailValidatorOperations != null)
			return emailValidatorOperations;
		emailValidatorOperations = getOperations(EMAILVALIDATOR, EmailValidatorOperations.class);
		return emailValidatorOperations;
	}

	protected GenericOperations<Contact> getContactOperations() {
		if (recordOperationsCache != null)
			return recordOperationsCache;

		recordOperationsCache = getOperations(CONTACTOPER, GenericOperations.class);
		return recordOperationsCache;
	}

	protected GenericOperations<COI> getCollectionOperations() {
		if (recordOperationsCache != null)
			return recordOperationsCache;

		recordOperationsCache = getOperations(CONTACTOPER, GenericOperations.class);
		return recordOperationsCache;
	}

	protected GenericOperations<AbstractAttributeStorage> getRecordOperations() {
		if (recordOperationsCache != null)
			return recordOperationsCache;

		recordOperationsCache = getOperations(CONTACTOPER, GenericOperations.class);
		return recordOperationsCache;
	}

	protected String getRemoteHost() {
		return req.getRemoteHost();
	}
	
	public FolderOperations getFolderOperations() {
		HttpSession s = getSession();
		if (s == null)
			return getFolderOperations(null, null);
		Key k = null;
		KeyStorage ks = (KeyStorage) s.getAttribute(KeyStorage.SESS_ATR);
		if (ks != null)
			k = ks.getKey();
		return getFolderOperations((String) s.getAttribute(HV_USER_ID), k);
	}

	public FolderOperations getFolderOperations(String areaId, Key key) {
		if (folderOperationscache == null)
			folderOperationscache = getOperations(FOLDEROPER, FolderOperations.class);
		if (areaId != null)
			return new PersonalFolderOperations(areaId, folderOperationscache, key);
		else
			return folderOperationscache;
	}

	public AttachmentOperations getAttachmentOperations() {
		// TODO why do not reuse?
		AttachmentOperations result = new AttachmentOperations();
		result.init(this);
		return result;
	}

	public CipherOperations getCipherOperations() {
		if (cipherOperationsCache != null)
			return cipherOperationsCache;

		cipherOperationsCache = getOperations(CIPHEROPER, CipherOperations.class);
		return cipherOperationsCache;
	}

	public LogOperations getLogOperations() {
		if (logOperationsCache != null)
			return logOperationsCache;

		logOperationsCache = getOperations(LOGOPER, LogOperations.class);
		return logOperationsCache;
	}

	public CertificateOperations getCertificateOperations() {
		if (this.certOperCache != null)
			return certOperCache;

		certOperCache = getOperations(CERTOPER, CertificateOperations.class);
		return certOperCache;

	}

	protected <T extends AbstractOperations> T getOperations(String opersName, Class<T> cls) {
		T resultCache = null;

		synchronized (frontController) {
			resultCache = (T) frontController.getServletContext().getAttribute(opersName);
			if (resultCache != null)
				return resultCache;

			try {
				resultCache = (T) cls.newInstance();
				resultCache.init(this);
				frontController.getServletContext().setAttribute(opersName, resultCache);
				// System.err.printf("Set attr %s in context %s\n", opersName,
				// dispatcher.getServletContext());
			} catch (InstantiationException e) {
				log("",e);
			} catch (IllegalAccessException e) {
				log("",e);
			}
		}
		return resultCache;
	}
	
	@Override
	protected void start() {
		String httpAgent = req.getHeader(Static.HTTP.USER_AGENT);
		mobile = httpAgent!= null && (httpAgent.indexOf("Mobile") > 0 ||
				httpAgent.indexOf("Tablet") > 0 || httpAgent.indexOf("Silk") > 0); 
		if (mobile == false) {
			// check it in session and override the flag if needed (session is never null)
			mobile = Boolean.TRUE.equals(getSession().getAttribute(ATTR_WEBMOBILE));
		}
	}
	
	@Override
	protected void addEnv(Object modelData, boolean ajax) {
		super.addEnv(modelData, ajax);
		if (modelData instanceof Map)
			// TODO add a standard object keeping last 5 navigation history
			try {
				Map _pageData = (Map) modelData;
				if (!ajax) {
					String referer = req.getHeader(Static.HTTP.REFERER);
					_pageData.put("back", referer != null ? referer : "#");
				}
				_pageData.put("mobile", mobile);
				_pageData.put(
						"commonlabel",
						getResourceManager(ResourceManager.RESOURCE_RES).getResource("commonlabels",
								new ResourceManager.LocalizedRequester() {

									public Locale getLocale() {
										return AddressBookProcessor.this.getLocale();
									}

									public String getEncoding() {
										return getCharSet();
									}

									public TimeZone getTimeZone() {
										return AddressBookProcessor.this.getTimeZone();
									}
								}));
				// req.setAttribute("commonlabel",
				// _pageData.get("commonlabel"));
				_pageData.put(Variable.PAGE_TITLE, getTittle());
			} catch (ResourceException e) {
				log("No common res found:" + e, null);
			}
		req.setAttribute(Constant.Request.LABEL, textResource);
	}

	protected String getViewName(String viewName) {
		if (mobile) {
			return "mobile/"+viewName;
		} 
		return viewName;
	}
	
	protected String getTittle() {
		return VersionConstant.PROGRAMNAME+" "+VersionConstant.VERSION;
	}
	
	@Override
	protected String getUnauthorizedPage() {
		return "Login";
	}

	@Override
	protected String getViewName() {
		return getViewName(getResourceName() + ".htm"); // use const template extension
	}

	@Override
	public boolean isThreadFriendly() {
		return false;
	}

	@Override
	public boolean isThreadSafe() {
		return true;
	}

	public String getPreferredServiceName() {
		return getResourceName();
	}

	public Object getServiceProvider() {
		return this;
	}

	protected void fillWithForm(AttributeStorage as, String... strings) {
		for (String ffieldName : strings)
			as.setAttribute(ffieldName, getStringParameterValue(ffieldName, null, 0));
	}

	protected void fillWithFormFilled(AttributeStorage as, String... strings) {
		for (String ffieldName : strings) {
			String value = getStringParameterValue(ffieldName, "", 0);
			if (value.length() > 0)
				as.setAttribute(ffieldName, value);
		}
	}

	protected Map fillWithForm(Map data, String... strings) {
		for (String ffieldName : strings)
			data.put(ffieldName, getStringParameterValue(ffieldName, null, 0));
		return data;
	}

	protected String fillUrlWithForm(String... strings) {
		StringBuffer result = new StringBuffer();
		for (String ffieldName : strings)
			try {
				result.append(ffieldName).append('=')
						.append(URLEncoder.encode(getStringParameterValue(ffieldName, "", 0), "UTF-8")).append('&');
			} catch (UnsupportedEncodingException e) {
			}
		return result.toString();
	}

	protected Map createErrorMap(String errorId) {
		return createErrorMap(errorId, null);
	}

	protected Map fillMessage(Map map, String field, String msgId, Object[] details) {
		if (map == null)
			map = new HashMap(4);
		if (details == null)
			map.put(field, getResourceString(msgId, msgId));
		else
			map.put(field, MessageFormat.format(getResourceString(msgId, msgId), details));

		return map;
	}

	protected Map createErrorMap(String errorId, Object[] details) {
		return fillMessage(null, Static.Variable.ERROR, errorId, details);
	}

	protected String treeStateEncode(String url, String stateKeeperParamname, String treeId) {
		String[] states = getStringParameterValues(TreeViewHelper.HV_STATE);
		if (states != null) {
			String pn = "&state" + treeId + '=';
			for (String state : states)
				try {
					url += pn + URLEncoder.encode(state, getCharSet());
				} catch (UnsupportedEncodingException e) {
				}
		}
		return url;
	}

	protected String getCharSet() {
		return "UTF-8";
	}

	protected void log(String message, Throwable t) {
		if (t == null)
			frontController.log("[" + getClass().getName() + "] " + message);
		else
			frontController.log("[" + getClass().getName() + "] " + message, t);
	}

	protected TreeViewHelper.TreeState getTreeState() {
		return new TreeViewHelper.TreeState(getStringParameterValue(TreeViewHelper.HV_TREEID, "", 0),
				getStringParameterValues(TreeViewHelper.HV_STATE
						+ getStringParameterValue(TreeViewHelper.HV_TREEID, "", 0)), getStringParameterValue(
						TreeViewHelper.HV_NODEID + getStringParameterValue(TreeViewHelper.HV_TREEID, "", 0),
						(String) null, 0));
	}

	public String getDataRoot() {
		return getDataRoot(frontController.getProperty(DATAROOT));
	}

	public static String getDataRoot(String dataRoot) {
		if (dataRoot == null || dataRoot.length() == 0) {
			if (Env.isAndroid()) {
				String webdir = System.getProperty("tjws.webappdir");
				if (webdir != null)
					return new File(webdir).getParent();
				// System.out.println("No datat root ");
			}
			dataRoot = System.getProperty("user.home");
		}

		return dataRoot;
	}

	public String getProperty(String name) {
		return frontController.getProperty(name);
	}

	public String getProperty(String name, String def) {
		return frontController.getProperty(name, def);
	}

	public TemplateProcessor getTemplateProcessor() {
		return getTemplateProcessor(".ht");
	}
	
	public Object getRuntimeContext() {
		return frontController.getAttribute("##RuntimeEnv");
	}

	/*
	 * protected class OperationsResolver<T> { protected T getOperations(String
	 * attrName) { synchronized(dispatcher) { T resultCache =
	 * (T)dispatcher.getServletContext().getAttribute(attrName); if (resultCache
	 * != null) return resultCache; resultCache = new T();
	 * resultCache.init(this);
	 * dispatcher.getServletContext().setAttribute(attrName, resultCache); } }
	 */
}
