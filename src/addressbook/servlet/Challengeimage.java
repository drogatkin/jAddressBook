/* jaddressbook - Challengeimage.java
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
 *  $Id: Challengeimage.java,v 1.5 2011/09/02 04:10:18 dmitriy Exp $
 * Created on Nov 4, 2005
 */
package addressbook.servlet;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.MemoryCacheImageOutputStream;

import android.graphics.Canvas;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Bitmap.Config;
import android.graphics.Bitmap.CompressFormat;

import org.aldan3.app.Env;

import addressbook.servlet.model.UserOperations;

import static addressbook.Contact.*;

/**
 * @author Dmitriy
 * 
 */
public class Challengeimage extends AddressBookProcessor {

	/*
	 * (non-Javadoc)
	 * 
	 * @see rogatkin.servlet.BaseFormProcessor#producePageData()
	 */
	@Override
	protected Map getModel() {
		try {
			if (Env.isAndroid()) {
				drawImageAndroid();
			} else
				drawImage();
		} catch (IOException ioe) {
			log("Error at preparing or writing image:", ioe);
		}
		return null;
	}

	void drawImage() throws IOException {
		log("draw normal image", null);
		resp.setContentType("image/png");
		Graphics g = null;
		BufferedImage im = null;
		MemoryCacheImageOutputStream mcs = null;
		try {
			int x = getIntParameterValue("x", 0, 0);
			int y = getIntParameterValue("y", 20, 0);
			// TODO: define parameters for bgcolor fgcolor, font, and bgimage
			im = createImage();
			g = im.getGraphics();
			g.setFont(new Font("Default", Font.ITALIC, 20));
			g.setColor(Color.orange);
			g.drawString(getChallengeToken(getStringParameterValue(NAME, "???", 0)), x, y);
			g.setColor(Color.white);
			g.drawLine(0, im.getHeight() / 3, im.getWidth(), im.getHeight() / 3);
			g.drawLine(0, im.getHeight() / 3 * 2, im.getWidth(), im.getHeight() / 3 * 2);
			// ImageReaderWriterSpi.getFormatNames()
			Iterator<ImageWriter> ii = ImageIO.getImageWritersByFormatName("png");
			if (ii != null && ii.hasNext()) {
				ImageWriter iw = ii.next();
				OutputStream os;
				iw.setOutput(mcs = new MemoryCacheImageOutputStream(os = resp.getOutputStream()));
				iw.write(im);
				iw.dispose();
				os.close();
			}
		} finally {
			if (mcs != null)
				try {
					mcs.close();
				} catch (Exception e) {
				}
			if (g != null)
				g.dispose();
		}
	}

	void drawImageAndroid() throws IOException {
		log("draw Android image", null);
		resp.setContentType("image/png");
		Bitmap bitmap = Bitmap.createBitmap(getIntParameterValue("w", 120, 0), getIntParameterValue("h", 36, 0),
				Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		int x = getIntParameterValue("x", 18, 0);
		int y = getIntParameterValue("y", 30, 0);
		Paint paint = new Paint();
		paint.setStyle(Paint.Style.FILL);
		paint.setColor(android.graphics.Color.BLACK);
		canvas.drawPaint(paint);
		paint.setColor(android.graphics.Color.YELLOW);
		paint.setTextSize(30);
		paint.setStyle(Paint.Style.STROKE);
		canvas.drawText(getChallengeToken(getStringParameterValue(NAME, "???", 0)), x, y, paint);
		paint.setColor(android.graphics.Color.WHITE);
		paint.setStyle(Paint.Style.FILL);
		//log("width:"+ bitmap.getWidth()+", height:"+ bitmap.getHeight(), null);
		canvas.drawLine(0, bitmap.getHeight() / 3, bitmap.getWidth(), bitmap.getHeight() / 3, paint);
		canvas.drawLine(0, bitmap.getHeight() / 3 * 2, bitmap.getWidth(), bitmap.getHeight() / 3 * 2, paint);
		bitmap.compress(CompressFormat.PNG, 100, resp.getOutputStream());
	}

	protected String getChallengeToken(String name) {
		if (name == null)
			throw new NullPointerException("Can't build token for null");
		String result = UserOperations.getUserToken(name);
		if (result == null)
			return "";
		if (result.length() > 5)
			return result.substring(0, 5);
		return result;
	}

	/**
	 * A factory method to create an image, it uses image size as servlet width
	 * and hight parameters or 120x20 as default
	 */
	protected BufferedImage createImage() {
		int width = getIntParameterValue("w", 120, 0);
		int height = getIntParameterValue("h", 20, 0);
		return new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see rogatkin.servlet.BaseFormProcessor#validateFormData()
	 */
	@Override
	protected Map doControl() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see rogatkin.servlet.BaseFormProcessor#getSubmitPage()
	 */
	@Override
	protected String getSubmitPage() {
		return null;
	}

	@Override
	public boolean isPublic() {
		return true;
	}

	protected boolean useLabels() {
		return false;
	}
}
