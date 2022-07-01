package addressbook;

import java.io.IOException;
import java.io.OutputStream;

public class Commentary extends GenericAttribute {

	public Commentary(String comment, String description) {
		super(comment, description);
		type = DataBookIO.COMMENT_TAG;
		//new Exception("Init val:"+comment).printStackTrace();
	}
	
	@Override
	public void update(Object [] params) {
		if (params == null || (params.length != 2))
			throw new IllegalArgumentException();
		value = (String)params[0];
		description = (String)params[1];
		//type = DataBookIO.COMMENT_TAG;
	}
	
	public void saveXML(OutputStream _out, String _enc, int _order) throws IOException {
		saveAsTag(_out, _enc, _order, DataBookIO.COMMENT_TAG);
	}
	
	public void saveVCard(OutputStream _out, String _enc, int _order) throws IOException {
		_out.write(("NOTE;CHARSET="+_enc+":").getBytes(_enc));
		_out.write(value.getBytes(_enc));
		_out.write(GenericAttribute.CRLF.getBytes(_enc));
	}

}
