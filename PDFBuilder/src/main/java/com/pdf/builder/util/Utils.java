package com.pdf.builder.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.pdf.builder.util.Global.Response;

public class Utils {
	
	public static int validate(String text) {
		Pattern simbols = Pattern.compile("\\{{2}[\\w\\\b]+\\}{2}");
		Matcher matcher = simbols.matcher(text);
		
		if (matcher.find()) return Response.DATA.value();
		
		return Response.PLAIN_TEXT.value();
	}
	
	public static byte[] extractBytes (String source) {
		Path path = Paths.get(source);
		
	    byte[] image = null;
	    
		try {
			image = Files.readAllBytes(path);
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		
	    return image;
	}
	
	public static int[] hexToRbg(String color) {
		
		return new int[] 
				{
					Integer.valueOf(color.substring(1, 3), 16),
					Integer.valueOf(color.substring(3, 5), 16),
					Integer.valueOf(color.substring(5, 7), 16),
				};
	}
	
	public static boolean isNumeric(String s) {  
	    return s != null && s.matches("[-+]?\\d*\\.?\\d+");  
	}
}
