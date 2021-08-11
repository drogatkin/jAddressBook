/* AddressBook - AddressBookResources
 * Copyright (C) 1999 Dmitriy Rogatkin.  All rights reserved.
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

import java.util.ListResourceBundle;

public class AddressBookResources extends ListResourceBundle {
    public final static String MENU_FILE = "File";
    public final static String MENU_NEWCONTACT = "New contact...";
    public final static String MENU_NEWBOOKMARK = "New bookmark...";
    public final static String MENU_NEWCOOKIE = "New cookie...";
    public final static String MENU_NEWGROUP = "New group...";
    public final static String MENU_NEWFOLDER = "New folder...";
    public final static String MENU_PROPERTIES = "Properties";
    public final static String MENU_DELETE = "Delete";
    public final static String MENU_IMPORT = "Import";
    public final static String MENU_EXPORT = "Export";
    public final static String MENU_ADDRBOOK = "Address Book...";
    public final static String MENU_BUSCARD = "Business Card(vCard)...";
    public final static String MENU_OTHERADDRBOOK = "Other Address Book...";
    public final static String MENU_PRINT = "Print";
    public final static String MENU_EXIT = "Exit";
    public final static String MENU_CLOSE = "Close";
    public final static String MENU_EDIT = "Edit";
    public final static String MENU_COPY = "Copy";
    public final static String MENU_PASTE = "Paste";
    public final static String MENU_PROFILE = "Profile...";
    public final static String MENU_FINDPEOPLE = "Find People...";
    public final static String MENU_SELECTALL = "Select all";
    public final static String MENU_VIEW = "View";
    public final static String MENU_TOOLBAR = "Toolbar";
    public final static String MENU_SYNCHRONIZE = "Synchronize now";
    public final static String MENU_FOLDERGROUP = "Folders and Groups";
    public final static String MENU_REFRESH = "Refresh";
    public final static String MENU_ACCOUNTS = "Accounts";
    public final static String MENU_STATUSBAR = "Statusbar";
    public final static String MENU_SORTBY = "Sort By";
    public final static String MENU_NAME = "Name";
    public final static String MENU_EMAILADDR = "E-Mail Address";
    public final static String MENU_BUSPHONE = "Business Phone";
    public final static String MENU_HOMEPHONE = "Home Phone";
    public final static String MENU_PHONE = "Telephone";
    public final static String MENU_CHAT = "Chat Id";
    public final static String MENU_FIRSTNAME = "First Name";
    public final static String MENU_LASTNAME = "Last Name";
    public final static String MENU_ASC = "Ascending";
    public final static String MENU_DESC = "Descending";
    public final static String MENU_TOOLS = "Tools";
    public final static String MENU_OPTIONS = "Options";
    public final static String MENU_ACTION = "Action";
    public final static String MENU_SENDMAIL = "Send Mail";
    public final static String MENU_DIALTO = "Dial to";
    public final static String MENU_CHAT_TO = "Chat to";
    public final static String MENU_SHOWURL = "Show bookmark";
    public final static String MENU_APPCOOKIE = "Apply cookie";
    public final static String MENU_GETDIRECTIONS = "Get directions";
	public final static String MENU_IPODCONTACTS = "iPod Contacts";
    public final static String [] MENUS_ACTION = {MENU_SENDMAIL, MENU_SHOWURL,
        MENU_CHAT_TO, MENU_DIALTO, MENU_GETDIRECTIONS,
        MENU_APPCOOKIE}; 
    public final static String MENU_HELP = "Help";
    public final static String MENU_CONTENTS = "Contents";
    public final static String MENU_ABOUT = "About...";
    public final static String[] HEADER_ADDRBOOK =
    {MENU_NAME, MENU_EMAILADDR, MENU_PHONE, MENU_CHAT };
    public final static String TAB_STORAGE = "Storage";
	
    public final static String [] LABELS_IMP_EXP_FMT_NAME = {"Outlook .CSV format", "Other .CSV format", "XML"};
	public final static String LABEL_TNAMEORLIST = "Type name or select from list";
    public final static String LABEL_GLOBAL = "Global";
    public final static String LABEL_HOST = "Host";
    public final static String LABEL_LOCAL = "Local";
	public final static String LABEL_STATUS_BAR = "Status bar";
    //public final static String LABEL_DB_URL = "JDBC URL";
    public final static String LABEL_DATABOOK = "XML Databook";
    public final static String LABEL_SHAREADDR = "Share my address";
    public final static String LABEL_SYNCHRONIZE = "Synchronize";
    public final static String LABEL_LOGIN = "Login";
    public final static String LABEL_PASSWORD = "Password";
    public final static String LABEL_NAME = "Name";
    public final static String LABEL_ADDRESSES = "Addresses";
    public final static String LABEL_CHATS = "Chats";
    public final static String LABEL_WEBS = "Bookmarks";
    public final static String LABEL_PGPS = "Certificates";
    public final static String LABEL_PHOTO = "Photo";
    public final static String LABEL_COMMENT = "Comment";
    public final static String LABEL_CHAT_ID = "Chat Id";
    public final static String LABEL_CHAT_TYPE = "Type";
    public final static String LABEL_ROOM = "Room";

	public final static String LABEL_LOCALES = "Locales";
    public final static String [] COUNTRIES = {"US", "FR", "RU"};
    public final static String [] LANGUAGES = {"en", "fr", "ru"};
	
	// reserved folder names
    public final static String LABEL_PERSONS = "Persons";
    public final static String LABEL_TRASH = "Trash";
    public final static String LABEL_SHARED = "Shared";
    //public final static String LABEL_LOCAL = "Local";
    
	public final static String LABEL_NONAME = "Name field can't be empty.";
    public final static String LABEL_DUP_FLDR = "Duplicated folder name. A new folder hasn't been added.";
	
	public final static String LABEL_NEW_ITEM_PTRN = "<new>";
	public final static String LABEL_EMAIL_PTRN = "name@host.com";
	public final static String LABEL_PHONE_PTRN = "1(222)436-9231";
	public final static String LABEL_BIRTHDAY = "DOB as MM-DD-YYYY";
	
	public final static String LABEL_ABREV = "Abrev";
	
	public final static String LABEL_FOLDER_NAME = "Folder name";
	
	public final static String LABEL_SELECT_EMAIL = "Select e-mail";
	public final static String LABEL_SELECT_LINK  = "Select URL";
	public final static String LABEL_SELECT_PHONE  = "Select telephone #";

	public final static String LABEL_ENCRYPTIONS[] = {"None", "DES", "3DES", "SSL", "SSL128"};
	public final static String LABEL_AUTHONTEFICATS[] = {"None", "BASIC", "SESSION", "ADVANCE+", "IP"};
	public final static String LABEL_ENCRYPTION = "Encryption";
	public final static String LABEL_AUTHONTICATION = "Authentication";
	public final static String LABEL_CONF_DEL_CONT = "Are you sure to delete contact?";
	public final static String LABEL_CONF_DEL_FLDR = "Are you sure to delete folder?";
	public final static String LABEL_NOFOLDER = "Plese select a folder or create one first";
	public final static String LABEL_M = "M";
	public final static String LABEL_TYPE = "Type";
	public final static String LABEL_SELECT_IMPORT_FORMAT = "Select imported format:";
	public final static String LABEL_CONFIRM_OVERWRITE = "It was a problem of reading your addressbook, do you want to overwrite with new data?";
	
    public final static String TITLE_NEWCONTACT = "New Contact";
    public final static String TITLE_NEWLINK = "New Link";
    public final static String TITLE_EDITLINK = "Edit Link";
    public final static String TITLE_NEWCOOKIE = "New Cookie";
    public final static String TITLE_EDITCOOKIE = "Edit Cookie";
    public final static String TITLE_WARNING = "Warning";
    public final static String TITLE_CONFIRM = "AddressBook: Confirm";
	public final static String TITLE_ENTER = "Enter";
	public final static String TITLE_LOCAL_PROFILE = "Local profile";
	public final static String TITLE_IMPORT_TYPE = "Import type selection";
    
    public final static String TAB_GENERAL = "General";
    public final static String TAB_DETAILS = "Details";
    public final static String TAB_CHAT_N_WEB = "Chat&Web";
    public final static String TAB_CERTIFICATES = "Certificates";

    public final static String TTIP_GENERALCONTACTINFO = "Specify general contact information here";
    public final static String TTIP_DETAILCONTACTINFO = "Provide more details for a friend and/or partner";
    public final static String TTIP_CHAT_N_WEB = "Add chat and web information";
    public final static String TTIP_CERTIFICATES = "Add certificates for signing and encryption";
    public final static String TTIP_STORAGE = "Provide storage location of the address book source";
	public final static String TTIP_NEW_ELEMENT = "Creation of a new person, a link, a cookie, etc."; 
	public final static String TTIP_PROPERTY = "Edit properties of selected element.";
    
    public final static String CMD_OK = "OK";
    public final static String CMD_APPLY = "<html><i>Apply";
    public final static String CMD_CANCEL = "Cancel";
    public final static String CMD_ADD = "Add";
    public final static String CMD_MODIFY = "Modify";
    public final static String CMD_DELETE = "Delete";
	public static final String CMD_COPY = "Copy";
	public static final String CMD_SELECT = "Select";

    public final static String EXT_GIF = ".gif";

    public final static String IMG_NEW = "New";
    public final static String IMG_PROPERTIES = "Properties"; //MENU_PROPERTIES;
    public final static String IMG_DELETE = MENU_DELETE;
    public final static String IMG_FINDPEOPLE = "Find";
    public final static String IMG_PRINT = MENU_PRINT;
    public final static String IMG_ACTION = MENU_ACTION;
    public final static String IMG_PROGRAM = "AddressBook.jpg";

    public final static String URL_HELP = "../jaddressbook.html";
    
	public final static Integer YES_INT = new Integer(1);
	public final static Integer NO_INT = new Integer(0);
    public final static int CTRL_VERT_SIZE = 24;
    public final static int CTRL_VERT_GAP = 4;
    public final static int CTRL_HORIS_INSET = 8;
    public final static int CTRL_HORIZ_GAP = 8;
    public final static java.awt.Dimension MIN_PANEL_DIMENSION = new java.awt.Dimension(40, 20);

    public Object[][] getContents() {
	return contents;
    }
    
    static final Object[][] contents = {
	{MENU_FILE, "File"},
	{"CancelKey", "Cancel"},
    };
}