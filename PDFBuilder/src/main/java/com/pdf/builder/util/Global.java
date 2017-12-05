package com.pdf.builder.util;

public class Global {

	public enum Label {
		PDF 	  ("XMLToPDF"), 
		BODY 	  ("body"),
		HEADER 	  ("header"),
		FOOTER 	  ("footer"),
		GROUP 	  ("group"), 
		TEXT 	  ("text"), 
		SOURCE    ("source"), 
		CELL 	  ("cell"),
		COLUMN 	  ("column"),
		WATERMARK ("watermark");
		
		private String code;

		private Label(final String code) {
			this.code = code;
		}
		
		public String value() {
			return code;
		}
	}
	
	public enum Property {
		TYPE 			("type"), 
		NUMBER_COLUMNS 	("number-columns"), 
		ROW 			("row"), 
		COLUMN 			("column"), 
		COLUMN_SPAN 	("column-span"),
		BORDER 			("border"), 
		ALIGNMENT 		("alignment"), 
		FONT_FAMILY 	("font-family"), 
		FONT_COLOR 		("font-color"),
		FONT_STYLE 		("font-style"), 
		FONT_SIZE 		("font-size"), 
		BACKGROUND 		("background-color"),
		HEADER 			("header"),
		NAME 			("name"),
		DATA 			("data"),
		SCALE 			("scale");
		
		private String code;
		
		private Property(final String code) {
			this.code = code;
		}
		
		public String value() {
			return this.code;
		}
	}
	
	public enum Type {

		TEXT 		("text"), 
		IMAGE 		("image"),
		QR_CODE		("qrcode"), 
		TABLE 		("table"), 
		DATATABLE 	("datatable");
			
		private String code;
		
		private Type(final String code) {
			this.code = code;
		}
		
		public String value() {
			return this.code;
		}
	}
	
	public enum Response {

		PLAIN_TEXT (0),
		DATA (1),
		OPERATION (2);
		
		private int code;
		
		private Response(final int code) {
			this.code = code;
		}
		
		public int value() {
			return this.code;
		}
	}
}
