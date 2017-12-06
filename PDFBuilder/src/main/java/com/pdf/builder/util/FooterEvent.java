package com.pdf.builder.util;

import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;

public class FooterEvent extends PdfPageEventHelper {

	private PdfPTable content;
	 
	public FooterEvent(PdfPTable content) {
		this.content = content;
	}
	
	@Override
    public void onEndPage(PdfWriter writer, Document document) {
		content.setTotalWidth(document.right() - document.left());
		content.writeSelectedRows(0, -1, document.leftMargin(), document.bottom() + content.getTotalHeight(), writer.getDirectContent());
    }
}
