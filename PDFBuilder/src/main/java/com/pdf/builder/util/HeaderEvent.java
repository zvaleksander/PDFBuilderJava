package com.pdf.builder.util;

import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;

public class HeaderEvent extends PdfPageEventHelper {

	private PdfPTable content;
	
	public HeaderEvent(PdfPTable content) {
		this.content = content;
	}
	
    public void onEndPage(PdfWriter writer, Document document) {
        content.setTotalWidth(document.right() - document.left());
        content.setLockedWidth(true);
        System.out.println(content.getTotalHeight());
        System.out.println(document.top());
        System.out.println(document.top() + ((document.topMargin() + content.getTotalHeight()) / 2));
        content.writeSelectedRows(0, -1, document.leftMargin(), document.top(), writer.getDirectContent());
    }
}
