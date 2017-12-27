package com.pdf.builder.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.gson.Gson;

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
import com.pdf.builder.util.Evaluator;
import com.pdf.builder.util.FooterEvent;
import com.pdf.builder.util.PDFEvent;
import com.pdf.builder.util.Global.Property;
import com.pdf.builder.util.Global.Type;
import com.pdf.builder.util.HeaderEvent;
import com.pdf.builder.util.Global.Label;
import com.pdf.builder.util.Global.Response;
import com.pdf.builder.util.Parser;
import com.pdf.builder.util.Utils;

import com.itextpdf.text.pdf.qrcode.EncodeHintType;
import com.itextpdf.text.pdf.qrcode.ErrorCorrectionLevel;

public class XMLToPDF {

	private Document document;
	private PdfWriter writer;
	
	private org.w3c.dom.Document xml;
	
	private Evaluator parser;
	private JSONObject data;
	
	private ByteArrayOutputStream byteArrayOutputStream  = new ByteArrayOutputStream();
	
	private float marginLeft; 
	private float marginRight; 
	private float marginTop; 
	private float marginBottom;
	
	private static final Logger logger = Logger.getLogger(XMLToPDF.class.getName());
	private static final DecimalFormat format = new DecimalFormat("0.00");
	
	public XMLToPDF(String inputPath, String outputPath, Object data, float marginLeft, float marginRight, float marginTop, float marginBottom, boolean vertical) {
		this.data = new JSONObject(new Gson().toJson(data));
        this.parser = new Evaluator(this.data);
        
        this.marginLeft = marginLeft;
        this.marginRight = marginRight;
        this.marginBottom = marginBottom;
        this.marginTop = marginTop;
        
		File file = new File(inputPath);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
			xml = builder.parse(file);
			
			create(outputPath, vertical);
		}
		catch (ParserConfigurationException e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
		catch (SAXException e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
		catch (IOException e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
	}
	
	public XMLToPDF(byte [] xmlFile, Object data, float marginLeft, float marginRight, float marginTop, float marginBottom, boolean vertical) {
		this.data = new JSONObject(new Gson().toJson(data));
        this.parser = new Evaluator(this.data);
        
        this.marginLeft = marginLeft;
        this.marginRight = marginRight;
        this.marginBottom = marginBottom;
        this.marginTop = marginTop;
        
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
			xml = builder.parse(new ByteArrayInputStream(xmlFile));
			
			create(null, vertical);
		} 
		catch (ParserConfigurationException e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		} 
		catch (SAXException e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		} 
		catch (IOException e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
	}
	
	public void build() {
		int columns = Integer.parseInt(xml.getDocumentElement().getAttribute(Property.NUMBER_COLUMNS.value()));
		
		PdfPTable header = new PdfPTable(columns);
		header.setHorizontalAlignment(Element.ALIGN_JUSTIFIED);
		header.setWidthPercentage(100);
		
		PdfPTable footer = new PdfPTable(columns);
		footer.setHorizontalAlignment(Element.ALIGN_JUSTIFIED);
		footer.setWidthPercentage(100);
		
		PdfPTable body = new PdfPTable(columns);
		body.setHorizontalAlignment(Element.ALIGN_JUSTIFIED);
		body.setWidthPercentage(100);
		body.flushContent();
		
		NodeList groups = xml.getElementsByTagName(Label.GROUP.value());
		NodeList waterMark = xml.getElementsByTagName(Label.WATERMARK.value());
		
		if(waterMark.getLength() > 0) buildWaterMark(waterMark.item(0));
		
		List<Cell> contentHeader = new ArrayList<Cell>();
		List<Cell> contentFooter = new ArrayList<Cell>();
		List<Cell> contentBody = new ArrayList<Cell>();
		
		for(int index = 0; index < groups.getLength(); index++) {
			if(groups.item(index).getParentNode().getNodeName().equals(Label.HEADER.value()))
				contentHeader.add(buildElements(groups.item(index)));
			else if(groups.item(index).getParentNode().getNodeName().equals(Label.FOOTER.value()))
				contentFooter.add(buildElements(groups.item(index)));
			else
				contentBody.add(buildElements(groups.item(index)));
		}
		
		addContent(columns, header, contentHeader);
		addContent(columns, footer, contentFooter);
		addContent(columns, body, contentBody);
		
		System.out.println(header.getTotalHeight());
		System.out.println(footer.getNumberOfColumns());
		System.out.println(body.getNumberOfColumns());
		
		if(!contentHeader.isEmpty()) {
			writer.setPageEvent(new HeaderEvent(header));
			marginTop += header.getTotalHeight();
		}
		
		if(!contentFooter.isEmpty())
			writer.setPageEvent(new FooterEvent(footer));
				
		document.setMargins(marginLeft, marginRight, marginTop, marginBottom);
		document.setMarginMirroring(false);
		document.open();
		
		try {
//			Paragraph p = new Paragraph("Test");
//			PdfPTable table = new PdfPTable(2);
//			for (int i = 1; i < 6; i++) {
//			    table.addCell("key " + i);
//			    table.addCell("value " + i);
//			}
//			for (int i = 0; i < 40; i++) {
//			    document.add(p);
//			}
//			
//			PdfPTable nesting = new PdfPTable(1);
//			PdfPCell cell = new PdfPCell(table);
//			cell.setBorder(PdfPCell.NO_BORDER);
//			nesting.addCell(cell);
//			document.add(nesting);
			body.setKeepTogether(true);
			document.add(body);
		} 
		catch (DocumentException e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
		
		document.close();
	}
	
	public byte[] getDocumentAsByteArray() {
		
		return this.byteArrayOutputStream.toByteArray();
	}
	
	private void create(String output, boolean vertical) {
		if(vertical)
			document = new Document(PageSize.A4);
		else
			document = new Document(PageSize.A4.rotate());
		
        try {
        	
        	if(output == null)
        		writer = PdfWriter.getInstance(document, byteArrayOutputStream);
        	else
        		writer = PdfWriter.getInstance(document, new FileOutputStream(output));
		} 
        catch (FileNotFoundException e) {
        	logger.log(Level.SEVERE, e.getMessage(), e);
		} 
        catch (DocumentException e) {
        	logger.log(Level.SEVERE, e.getMessage(), e);
		}
	}
	
	private void addContent(int maxColumns, PdfPTable container, List<Cell> content) {
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
		
		
		for (Cell cell : result) container.addCell(cell.container);
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
					else if(response == Response.DATA.value()) {
						String value = parser.getValue(temp);
						if (Utils.isNumeric(value) && value.contains(".")) {
							double number = Double.parseDouble(value);						        
					        text = format.format(number).replace(',', '.').concat("\n");
						}	
						else
							text = text.concat(value).concat("\n");
					}
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
					else if(response == Response.DATA.value()) {
						String value = parser.getValue(temp);
						if (Utils.isNumeric(value) && value.contains(".")) {
							double number = Double.parseDouble(value);
					        text = format.format(number).replace(',', '.').concat("\n");
						}
						else
							text = text.concat(value).concat("\n");
					}
					// else if(response == Response.DATA.value()) text = text.concat(parser.getValue(temp));
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
					else if(response == Response.DATA.value()) {
						String value = parser.getValue(temp);
						if (Utils.isNumeric(value) && value.contains(".")) {
							double number = Double.parseDouble(value);
					        text = format.format(number).replace(',', '.').concat("\n");
						}	
						else
							text = value.concat("\n");
					}
					// else if(response == Response.DATA.value()) text = parser.getValue(temp);
					
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
							
							// Verifica si es número decimal
							if (Utils.isNumeric(text) && text.contains(".")) {
								double number = Double.parseDouble(text);						        
						        text = format.format(number).replace(',', '.');
							}
							
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
		logger.info("-------------------");
		logger.info("|      Fonts      |");
		logger.info("-------------------");
		logger.info("1- " + BaseFont.COURIER);
		logger.info("2- " + BaseFont.HELVETICA);
		logger.info("3- " + BaseFont.SYMBOL);
		logger.info("4- " + BaseFont.TIMES_ROMAN);
		
		logger.info("-------------------");
		logger.info("|      Styles     |");
		logger.info("-------------------");
		logger.info("1- " + FontStyle.BOLD + ":" + FontStyle.BOLD.ordinal());
		logger.info("2- " + FontStyle.ITALIC + ":" + FontStyle.ITALIC.ordinal());
		logger.info("3- " + FontStyle.LINETHROUGH + ":" + FontStyle.LINETHROUGH.ordinal());
		logger.info("4- " + FontStyle.NORMAL + ":" + FontStyle.NORMAL.ordinal());
		logger.info("5- " + FontStyle.OBLIQUE + ":" + FontStyle.OBLIQUE.ordinal());
		logger.info("6- " + FontStyle.UNDERLINE + ":" + FontStyle.UNDERLINE.ordinal());
		
		logger.info("-------------------");
		logger.info("|      Border     |");
		logger.info("-------------------");
		logger.info("1- TOP:" + Rectangle.TOP);
		logger.info("2- LEFT:" + Rectangle.LEFT);
		logger.info("3- BOTTOM:" + Rectangle.BOTTOM);
		logger.info("4- RIGHT:" + Rectangle.RIGHT);
		logger.info("5- NO BORDER:" + Rectangle.NO_BORDER);
		logger.info("6- BOX:" + Rectangle.BOX);
		
		logger.info("-------------------");
		logger.info("|      Align      |");
		logger.info("-------------------");
		logger.info("1- CENTER:" + Alignment.RIGHT.ordinal());
		logger.info("2- LEFT:" + Alignment.LEFT.ordinal());
		logger.info("3- RIGHT:" + Alignment.CENTER.ordinal());
		logger.info("4- ANCHOR:" + Alignment.ANCHOR.ordinal());
	}
}