package com.pdf.builder.test;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.itextpdf.text.DocumentException;
import com.pdf.builder.core.XMLToPDF;
import com.pdf.builder.model.Documento;
import com.pdf.builder.util.Utils;

public class Builder {

	private static final String INPUT = "C:\\Users\\xixaos\\Desktop\\input.xml"; 
	private static final String OUTPUT = "C:\\Users\\xixaos\\Desktop\\output.pdf";
	private static final String OUTPUT_ARRAY = "C:\\Users\\xixaos\\Desktop\\output_array.pdf";
	
	public static void main(String[] args) throws DocumentException, ParserConfigurationException, SAXException, IOException {
		
		String logo = "C:\\Users\\xixaos\\Desktop\\batman.png";
		
		Documento documento = new Documento(2, "73037079108", "F001", "1001", "CALLE LOS GIRASOLES MZ. C LT. 15", "SFU LA SALLE", "USD", "Ninguna observación", Utils.extractBytes(logo));
		
		create(documento);
//		createAsByteArray(documento);
		
		System.out.println("Done");
	}
	
	public static void create(Documento documento) {
		XMLToPDF instance = new XMLToPDF(INPUT, OUTPUT, documento, 25, 25, 25, 25, true);
//		instance.help();
		instance.build();
	}
	
	public static void createAsByteArray(Documento documento) {
		XMLToPDF instance = new XMLToPDF(Utils.extractBytes(INPUT), documento, 25, 25, 25, 25, true);
		instance.build();
		
		byte[] bytes = instance.getDocumentAsByteArray();
		
		OutputStream out;
		try {
			out = new FileOutputStream(OUTPUT_ARRAY);
			out.write(bytes);
			out.close();
		} 
		catch (FileNotFoundException e) {
			e.printStackTrace();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}
