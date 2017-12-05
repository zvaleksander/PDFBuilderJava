package com.pdf.builder.util;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;

public class HeaderEvent extends PdfPageEventHelper {

	Font ffont = new Font(Font.FontFamily.UNDEFINED, 5, Font.ITALIC);
	
    public void onEndPage(PdfWriter writer, Document document) {
    	 PdfPTable table = new PdfPTable(1);
    	 System.out.println("left : " + document.left());
    	 System.out.println("right: " + document.right());
    	 System.out.println("top  : " + document.top());
    	 System.out.println("bot  : " + document.bottom());
         table.setTotalWidth(document.right() - document.left());
         PdfPCell cell = new PdfPCell(new Phrase("This is a test document"));
         cell.setBackgroundColor(BaseColor.ORANGE);
         table.addCell(cell);
         cell = new PdfPCell(new Phrase("This is a copyright notice"));
         cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
         
         table.addCell(cell);
         System.out.println("height  : " + table.getTotalHeight());
         table.writeSelectedRows(0, -1, document.leftMargin(),  document.top() + ((document.topMargin() + table.getTotalHeight()) / 2), writer.getDirectContent());
    }
}
