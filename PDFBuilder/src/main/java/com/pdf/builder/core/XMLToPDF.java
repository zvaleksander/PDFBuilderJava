package com.pdf.builder.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.gson.Gson;
//import com.google.zxing.BarcodeFormat;
//import com.google.zxing.WriterException;
//import com.google.zxing.client.j2se.MatrixToImageWriter;
//import com.google.zxing.common.BitMatrix;
//import com.google.zxing.qrcode.QRCodeWriter;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Font.FontStyle;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.TabStop.Alignment;
import com.itextpdf.text.pdf.BarcodeQRCode;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.pdf.builder.util.Attribute;
import com.pdf.builder.util.Cell;
import com.pdf.builder.util.PDFEvent;
import com.pdf.builder.util.Global.Property;
import com.pdf.builder.util.Global.Type;
import com.pdf.builder.util.Global.Label;
import com.pdf.builder.util.Global.Response;
import com.pdf.builder.util.Parser;
import com.pdf.builder.util.Utils;

import com.itextpdf.text.pdf.qrcode.EncodeHintType;
import com.itextpdf.text.pdf.qrcode.ErrorCorrectionLevel;

public class XMLToPDF {

	private Document document;
	private PdfWriter writer; 
	private PdfPTable body;
	private List<Cell> content;
	
	private org.w3c.dom.Document xml;
	
	private Parser parser;
	private JSONObject data;
	
	private ByteArrayOutputStream byteArrayOutputStream  = new ByteArrayOutputStream();
	
	public XMLToPDF(String inputPath, String outputPath, Object data, float marginLeft, float marginRight, float marginTop, float marginBottom, boolean vertical) {
		this.data = new JSONObject(new Gson().toJson(data));
        this.parser = new Parser(this.data);
        
		File file = new File(inputPath);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
			xml = builder.parse(file);
			
			create(outputPath, marginLeft, marginRight, marginTop, marginBottom, vertical);
		}
		catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		catch (SAXException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public XMLToPDF(byte [] xmlFile, Object data, float marginLeft, float marginRight, float marginTop, float marginBottom, boolean vertical) {
		this.data = new JSONObject(new Gson().toJson(data));
        this.parser = new Parser(this.data);
        
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
			xml = builder.parse(new ByteArrayInputStream(xmlFile));
			
			create(null, marginLeft, marginRight, marginTop, marginBottom, vertical);
		} 
		catch (ParserConfigurationException e) {
			e.printStackTrace();
		} 
		catch (SAXException e) {
			e.printStackTrace();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void build() {
		document.open();
		
		int columns = Integer.parseInt(xml.getDocumentElement().getAttribute(Property.NUMBER_COLUMNS.value()));
		
 		body = new PdfPTable(columns);
		body.setHorizontalAlignment(Element.ALIGN_JUSTIFIED);
		body.setWidthPercentage(100);
				
		content = new ArrayList<Cell>();
		
		NodeList groups = xml.getElementsByTagName(Label.GROUP.value());
		NodeList waterMark = xml.getElementsByTagName(Label.WATERMARK.value());
		
		if(waterMark.getLength() > 0) buildWaterMark(waterMark.item(0));
		
		for(int index = 0; index < groups.getLength(); index++) 
			content.add(buildElements(groups.item(index)));
		
		addContent(columns);
		try {
			document.add(body);
		} 
		catch (DocumentException e) {
			e.printStackTrace();
		}
		
		document.close();
	}
	
	public byte[] getDocumentAsByteArray() {
		
		return this.byteArrayOutputStream.toByteArray();
	}
	
	private void create(String output, float marginLeft, float marginRight, float marginTop, float marginBottom, boolean vertical) {
		if(vertical)
			document = new Document(PageSize.A4, marginLeft, marginRight, marginTop, marginBottom);
		else
			document = new Document(PageSize.A4.rotate(), marginLeft, marginRight, marginTop, marginBottom);
		
        try {
        	
        	if(output == null)
        		writer = PdfWriter.getInstance(document, byteArrayOutputStream);
        	else
        		writer = PdfWriter.getInstance(document, new FileOutputStream(output));
		} 
        catch (FileNotFoundException e) {
			e.printStackTrace();
		} 
        catch (DocumentException e) {
			e.printStackTrace();
		}
	}
	
	private void addContent(int maxColumns) {
		Cell anterior = new Cell();
		int rowDifference = 0;
		int colDifference = 0;
		int total = 0;
		
		List<Cell> result = new ArrayList<Cell>();
		
		for (int index = 0; index < content.size(); index++) {
			Cell actual = content.get(index);
			
			rowDifference = actual.row - anterior.row;
			colDifference = actual.col - anterior.col;
			
			if(rowDifference == 0) {
				if(colDifference == 0) {
					result.add(actual);
					total += actual.colspan;
				}
				else {
					if(anterior.colspan + anterior.col < actual.col) {
						result.add(new Cell(anterior.row, anterior.col + anterior.colspan, actual.col - anterior.col - anterior.colspan));
						total += actual.col - anterior.col - anterior.colspan;
					}
					result.add(actual);
					total += actual.colspan;
				}
				anterior.row = actual.row;
				anterior.col = actual.col;
				anterior.colspan = actual.colspan;
			}
			else {
				if(total < maxColumns) result.add(new Cell(anterior.row, anterior.col + anterior.colspan, maxColumns - total));
				
				total = 0;
				anterior.row += 1;
				anterior.col = 0;
				anterior.colspan = 0;
				index--;
			}
		}
		
		if(maxColumns > total) result.add(new Cell(anterior.row, total, maxColumns - total));
		
		
		for (Cell cell : result) body.addCell(cell.container);
	}
	
	private Cell buildElements(Node parent) {		
		Cell main = new Cell();
		NodeList children;
		
		String text = "";
		String temp = "";
		int response = -1;
		
		Attribute parentAttributes = new Attribute(parent);
		
		if(parentAttributes.type.equals(Type.TEXT.value())) {
			children = getChildrenByTagName(parent, Label.TEXT.value());
			
			if(children != null) {
				for(int index = 0; index < children.getLength(); index++) {
					temp = children.item(index).getTextContent().trim();
					response = Utils.validate(temp);
					
					if(response == Response.PLAIN_TEXT.value()) text = text.concat(temp).concat("\n");
					else if(response == Response.DATA.value()) text = text.concat(parser.getValue(temp)).concat("\n");
				}
				PdfPCell container = getCell(getPhrase(text, parentAttributes), parentAttributes);
								
				main = new Cell(container, parentAttributes.row, parentAttributes.col, parentAttributes.colspan);
			}
		}
		else if(parentAttributes.type.equals(Type.IMAGE.value())) {
			children = getChildrenByTagName(parent, Label.SOURCE.value());
			PdfPTable table = new PdfPTable(1);
			
			if(children != null) {
				for(int index = 0; index < children.getLength(); index++) {
					Node node = children.item(index);
					
					byte[] bytesImage = parser.getImage(node.getTextContent());
					
					PdfPCell cell = new PdfPCell(new Phrase("Image not found"));
					
					if(bytesImage != null) {
						try {
							Image image = Image.getInstance(bytesImage);
							image.scalePercent(parentAttributes.scale);
							cell = new PdfPCell(image);
							cell.setBorder(Rectangle.NO_BORDER);
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
					}
					table.addCell(cell);
				}
				
				PdfPCell container = new PdfPCell(table);
				container.setBorder(Rectangle.NO_BORDER);
				
				main = new Cell(container, parentAttributes.row, parentAttributes.col, parentAttributes.colspan);
			}
		}
		else if(parentAttributes.type.equals(Type.QR_CODE.value())) {
			children = getChildrenByTagName(parent, Label.TEXT.value());
			PdfPTable table = new PdfPTable(1);
			
			if(children != null) {
				for(int index = 0; index < children.getLength(); index++) {
					temp = children.item(index).getTextContent().trim();
					response = Utils.validate(temp);
					
					if(response == Response.PLAIN_TEXT.value()) text = text.concat(temp);
					else if(response == Response.DATA.value()) text = text.concat(parser.getValue(temp));
				}

				// Generación código QR
				int sizeQR = 150;
				Map<EncodeHintType, Object> hint = new HashMap<EncodeHintType, Object>();
				hint.put(EncodeHintType.CHARACTER_SET, "UTF-8");
				hint.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.Q);
				
				BarcodeQRCode qrcode = new BarcodeQRCode(text, sizeQR, sizeQR, hint);
				Image qrcodeImage;
				
				PdfPCell cell = new PdfPCell(new Phrase("QR not found"));
				
				try {
					qrcodeImage = qrcode.getImage();
					qrcodeImage.scalePercent(227);
					qrcodeImage.scaleToFit(sizeQR, sizeQR);
					
					cell = new PdfPCell(qrcodeImage);
					cell.setBorder(Rectangle.NO_BORDER);
				} 
				catch (BadElementException e) {
					e.printStackTrace();
				}
				table.addCell(cell);

				/*
				byte[] bytesImage = getQRCodeImage(text);
				
				PdfPCell cell = new PdfPCell(new Phrase("QR not found"));
				
				if(bytesImage != null) {
					try {
						Image image = Image.getInstance(bytesImage);
						image.scalePercent(parentAttributes.scale);
						image.setBorder(Rectangle.BOX);
						cell = new PdfPCell(image);
						cell.setBorder(Rectangle.NO_BORDER);
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
				}
				table.addCell(cell);
				*/
				PdfPCell container = new PdfPCell(table);
				container.setBorder(Rectangle.NO_BORDER);
				
				main = new Cell(container, parentAttributes.row, parentAttributes.col, parentAttributes.colspan);
			}
		}
		else if(parentAttributes.type.equals(Type.TABLE.value())) {
			children = getChildrenByTagName(parent, Label.CELL.value());
			PdfPTable table = new PdfPTable(parentAttributes.numColumns);
			
			if(children != null) {
				for(int index = 0; index < children.getLength(); index++) {
					Node node = children.item(index);
					Attribute attribute = new Attribute(node);
					
					temp = children.item(index).getTextContent().trim();
					response = Utils.validate(temp);
					
					if(response == Response.PLAIN_TEXT.value()) text = temp;
					else if(response == Response.DATA.value()) text = parser.getValue(temp);
					
					PdfPCell cell = getCell(getPhrase(text, attribute), attribute);
					table.addCell(cell);
				}
				PdfPCell container = new PdfPCell(table);
				container.setColspan(parentAttributes.colspan);
				container.setBorder(parentAttributes.border);
				
				main = new Cell(container, parentAttributes.row, parentAttributes.col, parentAttributes.colspan);
			}
		}
		else if(parentAttributes.type.equals(Type.DATATABLE.value())) {
			children = getChildrenByTagName(parent, Label.COLUMN.value());
			PdfPTable table = new PdfPTable(parentAttributes.numColumns);
			
			List<Attribute> childrenAttributes = new ArrayList<Attribute>();
			
			if(children != null) {
				for(int index = 0; index < children.getLength(); index++) {
					Node node = children.item(index);
					Attribute attribute = new Attribute(node);
					
					// Creación de cabeceras utilizando el atributo HEADER
					PdfPCell cell = getCell(getPhrase(attribute.header, parentAttributes), parentAttributes);
					cell.setColspan(attribute.colspan);
					
					table.addCell(cell);
					
					attribute.aux_3 = node.getTextContent();
					childrenAttributes.add(attribute);
				}
				
				JSONArray array = parser.getArray(parentAttributes.data);
				
				if(array != null) {
					for(int index = 0; index < array.length(); index++) {
						Parser parserTemp = new Parser(array.getJSONObject(index));
						
						for (Attribute attribute : childrenAttributes) {
							parentAttributes.fontColor = "#000000";
							
							text = parserTemp.getValue(attribute.aux_3);
							
							// Agrega el cuerpo de la tabla
							PdfPCell cell = getCell(getPhrase(text, parentAttributes), parentAttributes);
							cell.setBackgroundColor(BaseColor.WHITE);
							cell.setColspan(attribute.colspan);
							table.addCell(cell);
							
							if(!attribute.name.isEmpty()) attribute.aux_2 += Double.parseDouble(text);
						}
					}
					
					for (Attribute attribute : childrenAttributes)
						if(! attribute.name.isEmpty())
							this.data.put(attribute.name, attribute.aux_2);
				}
				
				PdfPCell container = new PdfPCell(table);
				container.setColspan(parentAttributes.colspan);
				main = new Cell(container, parentAttributes.row, parentAttributes.col, parentAttributes.colspan);
			}
		}
		
		return main;
	}
	
	private void buildWaterMark(Node node) {
		Attribute attribute = new Attribute(node);
		
		NodeList children = null;
		
		boolean isImage = false;
		
		if(attribute.type.equals(Type.TEXT.value()))
			children = getChildrenByTagName(node, Label.TEXT.value());
		
		else if(attribute.type.equals(Type.IMAGE.value())) {
			children = getChildrenByTagName(node, Label.SOURCE.value());
			isImage = true;
		}	
		
		if(children != null) {
			for(int index = 0; index < children.getLength(); index++) {
				String content = children.item(index).getTextContent().trim();
				if(isImage) {
					byte[] source = parser.getImage(content);
					
					writer.setPageEvent(new PDFEvent(source));
				}
				else {
					String text = parser.getValue(content);
					
					writer.setPageEvent(new PDFEvent(text, attribute));
				}
			}
		}
	}
	
	private PdfPCell getCell(Phrase phrase, Attribute attribute) {
		int[] color = Utils.hexToRbg(attribute.background);
		
		PdfPCell cell = new PdfPCell(phrase);
		cell.setBorder(attribute.border);
		cell.setColspan(attribute.colspan);
		cell.setHorizontalAlignment(attribute.alignment);
		cell.setBackgroundColor(new BaseColor(color[0], color[1], color[2]));
		cell.setPadding(5);
		
		return cell;
	}
	
	private Phrase getPhrase(String text, Attribute attribute) {
		
		int[] color = Utils.hexToRbg(attribute.fontColor);
		
		Font font = new Font(FontFactory.getFont(attribute.fontFamily));
		font.setSize(attribute.fontSize);
		font.setStyle(attribute.fontStyle);
		font.setColor(color[0], color[1], color[2]);
		
		return new Phrase(text.trim(), font);
	}
	
	private NodeList getChildrenByTagName(Node node, String tag) {
		if(node.getNodeType() == Node.ELEMENT_NODE) {
			org.w3c.dom.Element element = (org.w3c.dom.Element)node;
			
			return element.getElementsByTagName(tag);
		}
		
		return null;
	}
	
//	@SuppressWarnings("unused")
//	private byte[] getQRCodeImage(String text) {
//		Map<com.google.zxing.EncodeHintType, Object> hintMap = new HashMap<com.google.zxing.EncodeHintType, Object>();
//		hintMap.put(com.google.zxing.EncodeHintType.CHARACTER_SET, "UTF-8");
//		hintMap.put(com.google.zxing.EncodeHintType.MARGIN, 0);
//		hintMap.put(com.google.zxing.EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.Q);
//		
//		QRCodeWriter qrCodeWriter = new QRCodeWriter();
//	    BitMatrix bitMatrix;
//	    ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
//		try {
//			bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, 227, 227);
//			MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
//		} 
//		catch (WriterException e) {
//			e.printStackTrace();
//		} 
//		catch (IOException e) {
//			e.printStackTrace();
//		}
//		
//	    byte[] pngData = pngOutputStream.toByteArray();
//	    
//	    return pngData;
//	}
	
	public void help() {
		System.out.println("-------------------");
		System.out.println("|      Fonts      |");
		System.out.println("-------------------");
		System.out.println("1- " + BaseFont.COURIER);
		System.out.println("2- " + BaseFont.HELVETICA);
		System.out.println("3- " + BaseFont.SYMBOL);
		System.out.println("4- " + BaseFont.TIMES_ROMAN);
		
		System.out.println("-------------------");
		System.out.println("|      Styles     |");
		System.out.println("-------------------");
		System.out.println("1- " + FontStyle.BOLD + ":" + FontStyle.BOLD.ordinal());
		System.out.println("2- " + FontStyle.ITALIC + ":" + FontStyle.ITALIC.ordinal());
		System.out.println("3- " + FontStyle.LINETHROUGH + ":" + FontStyle.LINETHROUGH.ordinal());
		System.out.println("4- " + FontStyle.NORMAL + ":" + FontStyle.NORMAL.ordinal());
		System.out.println("5- " + FontStyle.OBLIQUE + ":" + FontStyle.OBLIQUE.ordinal());
		System.out.println("6- " + FontStyle.UNDERLINE + ":" + FontStyle.UNDERLINE.ordinal());
		
		System.out.println("-------------------");
		System.out.println("|      Border     |");
		System.out.println("-------------------");
		System.out.println("1- TOP:" + Rectangle.TOP);
		System.out.println("2- LEFT:" + Rectangle.LEFT);
		System.out.println("3- BOTTOM:" + Rectangle.BOTTOM);
		System.out.println("4- RIGHT:" + Rectangle.RIGHT);
		System.out.println("5- NO BORDER:" + Rectangle.NO_BORDER);
		System.out.println("6- BOX:" + Rectangle.BOX);
		
		System.out.println("-------------------");
		System.out.println("|      Align      |");
		System.out.println("-------------------");
		System.out.println("1- CENTER:" + Alignment.RIGHT.ordinal());
		System.out.println("2- LEFT:" + Alignment.LEFT.ordinal());
		System.out.println("3- RIGHT:" + Alignment.CENTER.ordinal());
		System.out.println("4- ANCHOR:" + Alignment.ANCHOR.ordinal());
	}
}