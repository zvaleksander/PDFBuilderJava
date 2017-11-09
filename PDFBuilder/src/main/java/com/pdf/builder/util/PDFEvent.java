package com.pdf.builder.util;

import java.io.IOException;
import java.net.MalformedURLException;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfGState;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;

public class PDFEvent extends PdfPageEventHelper{
		
	private boolean isImage = false;
	private Phrase phrase;
	private Image image;
	
	private float width;
	private float height;
	
	private static final float OPACITY = 0.25f;
	
	public PDFEvent(String text, Attribute attribute) {
		phrase = getPhrase(text, attribute);
	}
	
	public PDFEvent(byte[] source) {
		try {
			image = Image.getInstance(source);
			width = (image.getPlainWidth() > 500) ? 250 : image.getPlainWidth();
			height = (image.getPlainHeight() > 500) ? 200 : image.getPlainHeight();
		} 
		catch (BadElementException e) {
			e.printStackTrace();
		} 
		catch (MalformedURLException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		isImage = true;
	}

	@Override
	public void onEndPage(PdfWriter writer, Document document) {
		float x = (document.getPageSize().getLeft() + document.getPageSize().getRight()) / 2;
        float y = (document.getPageSize().getTop() + document.getPageSize().getBottom()) / 2;
		
		PdfGState state = new PdfGState();
        state.setFillOpacity(OPACITY);
		
		PdfContentByte canvas = writer.getDirectContent();
		canvas.setGState(state);
		
		if(isImage) {
			try {
				canvas.addImage(image, width, 0, 0, height, x - (width / 2), y - (height / 2));
			} 
			catch (DocumentException e) {
				e.printStackTrace();
			}
		}
		else {
			ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER, phrase, x, y, 45);
		}
	}
	
	private Phrase getPhrase(String text, Attribute attribute) {
		
		int[] color = Utils.hexToRbg(attribute.fontColor);
		
		Font font = new Font(FontFactory.getFont(attribute.fontFamily));
		font.setSize(attribute.fontSize);
		font.setStyle(attribute.fontStyle);
		font.setColor(color[0], color[1], color[2]);
		
		return new Phrase(text.trim(), font);
	}
}
