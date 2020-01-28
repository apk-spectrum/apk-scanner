package com.apkspectrum.swing;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Dictionary;
import java.util.Hashtable;

import javax.imageio.ImageIO;
import javax.swing.text.Element;
import javax.swing.text.html.HTML;
import javax.swing.text.html.ImageView;

import com.apkspectrum.util.Base64;

// refer to https://stackoverflow.com/questions/51103717/jeditorpane-content-type-for-html-embedded-base64-images
class BASE64ImageView extends ImageView {
	private String src;

	public BASE64ImageView(Element elmnt) {
		super(elmnt);
		src = (String) elmnt.getAttributes().getAttribute(HTML.Attribute.SRC);
		populateImage();
	}

	private void populateImage() {
		URL src = getImageURL();
		Image img = loadImage();
		if(src != null && img != null) {
			@SuppressWarnings("unchecked")
			Dictionary<URL, Image> cache = (Dictionary<URL, Image>) getDocument()
					.getProperty("imageCache");
			if (cache == null) {
				cache = new Hashtable<>();
				getDocument().putProperty("imageCache", cache);
			}
			cache.put(src, img);
		}
	}

	private Image loadImage() {
		BufferedImage image = null;

		if (isBase64Encoded()) {
			byte[] b64 = Base64.getDecorder().decode(getBASE64Image());
			try (ByteArrayInputStream bais = new ByteArrayInputStream(b64)){
				image = ImageIO.read(bais);
			} catch (Throwable ex) {
				ex.printStackTrace();
			}
		} else {
			URL url = getImageURL();
			if(url != null) {
				try {
					image = ImageIO.read(url);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return image;
	}

	@Override
	public URL getImageURL() {
		if (isBase64Encoded()) {
			try {
				return new URL("file:"+src);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
		return super.getImageURL();
	}

	private boolean isBase64Encoded() {
		return src != null && src.contains("base64,");
	}

	private String getBASE64Image() {
		if (!isBase64Encoded()) {
			return null;
		}
		return src.substring(src.indexOf("base64,") + 7).trim();
	}
}
