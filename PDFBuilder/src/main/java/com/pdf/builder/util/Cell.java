package com.pdf.builder.util;

import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;

public class Cell {

	public PdfPCell container;
	public int row;
	public int col;
	public int colspan;
	
	public Cell(PdfPCell container, int row, int col, int colspan) {
		this.container = container;
		this.row = row;
		this.col = col;
		this.colspan = colspan;
		
		this.container.setColspan(colspan);
	}
	
	public Cell(int row, int col, int colspan) {
		this.container = new PdfPCell(new Phrase(" "));
		this.container.setBorder(Rectangle.NO_BORDER);
		
		this.row = row;
		this.col = col;
		this.colspan = colspan;
		
		this.container.setColspan(colspan);
	}
	
	public Cell() { 
		this.container = new PdfPCell(new Phrase(" "));
		this.container.setBorder(Rectangle.NO_BORDER);
		
		this.row = 0;
		this.col = 0;
		this.colspan = 0;
	}

	@Override
	public String toString() {
		return "Cell [row=" + row + ", col=" + col + ", colspan=" + colspan + "]";
	}
}
