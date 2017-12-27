package com.pdf.builder.util;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.gson.Gson;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import net.objecthunter.exp4j.ValidationResult;

public class Evaluator {
	
	private static final Map<String, String> validate;
	private List<String> numberBuffer;
	private List<String> letterBuffer;
	private List<Token> result;
	private JSONObject json;
	private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00");
	
    static {
        Map<String, String> map = new HashMap<String, String>();
        map.put("IS_UNDERSCORE", "_");
        map.put("IS_DOT", ".");
        map.put("IS_SEPARATOR", "|:");
        map.put("IS_MUSTACHE", "{}");
        map.put("IS_OPERATOR", "+-*/%");
        map.put("IS_LEFTPAR", "(");
        map.put("IS_RIGHTPAR", ")");
        map.put("IS_LETTER", "[a-zA-Z]+");
        
        validate = Collections.unmodifiableMap(map);
    }

	public Evaluator(JSONObject object) {
		json = object;
		numberBuffer = new ArrayList<String>();
		letterBuffer = new ArrayList<String>();
	}
	
	public byte[] getImage(String property) {
		property = property.replaceAll("[{}]+", "");
		
		if(json.has(property)) {
			JSONArray entries = new JSONArray(json.get(property).toString());
		    byte[] array = new byte[entries.length()];
		    
		    for (int i = 0; i < entries.length(); i++) array[i] = (byte) entries.getInt(i);
			
			return array;
		}
		
		return null;
	}
	
	public JSONArray getArray(String list) {
		if(json.has(list)) return json.getJSONArray(list);
		
		return null;
	}
	
	public String getValue(String text) {
		result = new ArrayList<Token>();
		
		text = text.replaceAll("\\s+","");
		
		String expression = "";
		for (int index = 0; index < text.length(); index++) {
			char current = text.charAt(index);
			
			if (Character.isDigit(current)) {
				numberBuffer.add(String.valueOf(current));
			}
			else if (validate.get("IS_DOT").indexOf(current) >= 0) {
				numberBuffer.add(String.valueOf(current));
			}
			else if (Character.isLetter(current) || validate.get("IS_UNDERSCORE").indexOf(current) >= 0 || validate.get("IS_MUSTACHE").indexOf(current) >= 0) {
				if (!numberBuffer.isEmpty()) 
					emptyNumberBuffer();
				letterBuffer.add(String.valueOf(current));
			}
			else if (validate.get("IS_OPERATOR").indexOf(current) >= 0) {
				emptyNumberBuffer();
				emptyLetterBuffer();
				
				result.add(new Token("OPERADOR", String.valueOf(current)));
    		}
    		else if (validate.get("IS_SEPARATOR").indexOf(current) >= 0) {
    			emptyNumberBuffer();
				emptyLetterBuffer();
				
				result.add(new Token("SEPARADOR", String.valueOf(current)));
    		}
    		else if (validate.get("IS_LEFTPAR").indexOf(current) >= 0) {
    			if (!numberBuffer.isEmpty())
    				emptyNumberBuffer();
    			
    			result.add(new Token("LPARENTESIS", String.valueOf(current)));
    		}
    		else if (validate.get("IS_RIGHTPAR").indexOf(current) >= 0) {
    			emptyNumberBuffer();
				emptyLetterBuffer();
				
				result.add(new Token("RPARENTESIS", String.valueOf(current)));
    		}
		}
		
		if (!numberBuffer.isEmpty()) emptyNumberBuffer();
		if (!letterBuffer.isEmpty()) emptyLetterBuffer();
		
		boolean isAritmethicExpression = true;
		boolean isText = false;
		for (int index = 0; index < result.size(); index++) {
			String type = result.get(index).getType();
			
			String value = (result.get(index).getValue());
			if(type.equals("VARIABLE") && value.contains("{{") && value.contains("}}")) {
				value = value.replaceAll("[{}]+", "");
				if(json != null && json.has(value)) {
					if(!isNumeric(json.get(value).toString())) {
						isAritmethicExpression = false;
					}
					result.get(index).setValue(json.get(value).toString());
				}
			}
			else if (type.equals("SEPARADOR")) isText = true;
			
			expression += result.get(index).getValue();
		}
		
		if(result.size() > 1 && isAritmethicExpression && !isText)
			return evaluate(expression);
  
  		return expression;
	}
	
	private void emptyNumberBuffer() {
		if(!numberBuffer.isEmpty()) {
			result.add(new Token("LITERAL", String.join("", numberBuffer)));
			numberBuffer.clear();
		}
	}
	
	private void emptyLetterBuffer() {
		if(!letterBuffer.isEmpty()) {
			result.add(new Token("VARIABLE", String.join("", letterBuffer)));
			letterBuffer.clear();
		}
	}
	
	private boolean isNumeric(String str) {
		return str.matches("-?\\d+(\\.\\d+)?");
	}
	
	private String evaluate(String input) {
		Expression expression = new ExpressionBuilder(input).build();
		ValidationResult result = expression.validate();
		if (result.isValid())
			return String.valueOf(Math.round(expression.evaluate() * 100.00)/100.00);

		return input;
	}
	
	public class Token {

		private String type;
		private String value;
		
		public Token(String type, String value) {
			super();
			this.type = type;
			this.value = value;
		}

		public String getType() {
			return type;
		}
		
		public void setType(String type) {
			this.type = type;
		}
		
		public String getValue() {
			return value;
		}
		
		public void setValue(String value) {
			this.value = value;
		}
	}
}
