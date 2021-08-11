package addressbook.servlet.model;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;

import addressbook.AddressBookResources;
import addressbook.Contact;
import addressbook.EMail;
import addressbook.Folder;
import addressbook.Name;
import addressbook.Telephone;

import android.content.ContentResolver;
import android.database.Cursor;
//import android.os.Bundle;
import android.provider.ContactsContract;
import android.content.Context;

public class AndroidContacts extends Folder<Contact> {
	Context context;

	public AndroidContacts(Object androidContext) {
		super(AddressBookResources.LABEL_LOCAL);
		setType(PERSON);
		context = (Context) androidContext;
		fill();
	}

	public void saveXML(OutputStream _out, String _enc, int _order) throws IOException, UnsupportedEncodingException {
		// no save for the folder
	}

	protected void fill() {
		// System.err.printf("Entered with context:%s%n", context);
		if (context == null)
			return;
		ContentResolver cr = context.getContentResolver();
		Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
		if (cur.getCount() > 0) {
			while (cur.moveToNext()) {
				try {
					String id = (cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID)));
					Contact current = new Contact(new Name(cur.getString(cur
							.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))));
					//System.err.printf("Adding local contact %s%n", current);
					add(current);
					if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
						Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
								ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[] { id }, null);
						while (pCur.moveToNext()) {
							current.add(new Telephone(pCur.getString(pCur
									.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE)), pCur.getString(pCur
									.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))));
						}
						pCur.close();
					}

					Cursor emailCur = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
							ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?", new String[] { id }, null);
					while (emailCur.moveToNext()) {
						// This would allow you get several email addresses
						// if the email addresses were stored in an array
						String email = emailCur.getString(emailCur
								.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
						String emailType = emailCur.getString(emailCur
								.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE));
						current.add(new EMail(email, emailType));
					}
					emailCur.close();

				} catch (ParseException e) {
					System.err.printf("Skipped contact because %s%n", e);
				}
			}
		}
		cur.close(); // finally
	}
}
