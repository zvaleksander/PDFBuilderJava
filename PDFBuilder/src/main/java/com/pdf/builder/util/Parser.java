package com.pdf.builder.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.gson.Gson;

public class Parser {
	private static String operadores = "+-*/%|";
	private Stack <String> pEntrada = new Stack <String> (); //Pila entrada
	private Stack <String> pTemporal = new Stack <String> (); //Pila temporal para operadores
	private Stack <String> pSalida = new Stack <String> (); //Pila salida
	
	private String resultado;
	private JSONObject json;
	
	public Parser(Object object) {
		this.json = new JSONObject(new Gson().toJson(object));
	}
	
	public Parser(JSONObject json) {
		this.json = json;
	}
	
	public String getResultado() {
		return resultado;
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
	
	/**Calcula el valor de la expresion matematica
	 * @param entrada es la expresion matematica
	 * @return el valor de la expresion matematica
	 */
	
	public JSONArray getArray(String list) {
		if(json.has(list)) return json.getJSONArray(list);
		
		return null;
	}
	
	public String getValue(String entrada) {
		
		entrada = entrada.replaceAll("[{}]+", "");
		
		List<String> collection = new ArrayList<String>();
		entrada = depurar(entrada);
		
		for (String string : entrada.split(" ")) {
			collection.add(string);
		}

		for (int i = collection.size() - 1; i >= 0; i--) {
			pEntrada.push(collection.get(i));      
		}

		try {

			while (!pEntrada.isEmpty()) {	    	  

				switch (pref(pEntrada.peek())){

				case 1: pTemporal.push(pEntrada.pop()); break;
				case 3:
				case 4:
				case 5:
				case 6:
					while(pref(pTemporal.peek()) >= pref(pEntrada.peek()))	pSalida.push(pTemporal.pop());
					pTemporal.push(pEntrada.pop());
					break;
				case 2:
					while(!pTemporal.peek().equals("(")) pSalida.push(pTemporal.pop());
					pTemporal.pop();
					pEntrada.pop();
					break; 
				default:pSalida.push(pEntrada.pop()); 
				} 
			}
		}
		catch(Exception ex){ 
			System.out.println("Error en la expresión algebraica");
			System.err.println(ex);
		}
		
		int size = pSalida.size();
		for (int i = 1; i<=size ; i++) {
			pEntrada.push(pSalida.pop());
		}

		while (!pEntrada.isEmpty()) {
			if (operadores.contains("" + pEntrada.peek())) {
				pTemporal.push(evaluar(pEntrada.pop(), pTemporal.pop(), pTemporal.pop()) + "");

			}else {
				pTemporal.push(pEntrada.pop());

			} 
		}
		
		return recuperar(pTemporal.peek());
	} 


	/** Formate la expresion 
	 * @param string es la expresion antes de ser formateada
	 * @return string es la expresion formateada
	 */
	private String depurar(String string) {
		string = string.replaceAll("\\s+", ""); //Elimina espacios en blanco
		string = "(" + string + ")";
		String res = "";
		String temp = operadores + "()";

		for (int i = 0; i < string.length(); i++) {
			if (temp.contains("" + string.charAt(i))) {
				if(string.charAt(i)=='-'&&(operadores.contains(""+string.charAt(i-1))||string.charAt(i-1)=='('))
					res += string.charAt(i);
				else
					res += " " + string.charAt(i) + " ";
			}
			else
				res += string.charAt(i);
		}
		
		return res.replaceAll("\\s+", " ").trim();
	} 

	/** Revisa la preferencia de un operador
	 * @param op es el operador
	 * @return el valor de la preferencia
	 */
	private static int pref(String op) {
		int prf = 99;
		if (op.equals("*") || op.equals("/")) prf = 5;
		if (op.equals("+") || op.equals("-")) prf = 4;
		if (op.equals("%")) prf = 3;
		if (op.equals(")")) prf = 2;
		if (op.equals("(")) prf = 1;
		if (op.equals("|")) prf = 6;

		return prf;
	}

	/** Revisa si el contenido es un numero
	 * @param str es un string
	 * @return true si el contenido es un numero false si el contenido es una variable
	 */
	private boolean isNumeric(String str) {  
		try {
			Double.parseDouble(str);
		}  
		catch(NumberFormatException nfe){
			return false;
		}
		
		return true;  
	}

	/** Recupera el valor de una variable de un objeto JSON
	 * @param str es una variable 
	 * @return retorna el valor de la variable
	 */
	private String recuperar(String str) {
		if(json.has(str)) return json.get(str).toString();
		
		return str;
	}

	/** Revisa el contenido del token
	 * @param str es un string que contiene un numero o variable
	 * @return el numero del string o el valor de la variable 
	 */
	private String revisar(String str) {
		if(!isNumeric(str)) 
			return recuperar(str);
		
		return str;			
	}

	/** Evalua la operacion 
	 * @param op es un operador 
	 * @param n1 es el primer operando
	 * @param n2 es el segundo operando
	 * @return el valor de la operacion
	 */
	private String evaluar(String op, String n2, String n1) {
		n1 = revisar(n1);
		n2 = revisar(n2);
		if(isNumeric(n1) && isNumeric(n2)) {
			double num1 = Double.parseDouble(n1);
			double num2 = Double.parseDouble(n2);
			double result = 0;
			
			if (op.equals("+")) result = (num1 + num2);
			else if (op.equals("-")) result = (num1 - num2);
			else if (op.equals("*")) result = (num1 * num2);
			else if (op.equals("/")) result = (num1 / num2);
			else if (op.equals("%")) result = (num1 % num2);
			else return n1 + " " + op + " "  + n2;
			
			return String.valueOf(Math.round(result * 100.0)/100.0);
		}
		else 
			return n1 + " " + op + " "  + n2;
	}
	
	public void print() {
		System.out.println("Entrada : " + this.pEntrada);
		System.out.println("Salida  : " + this.pSalida);
		System.out.println("Temporal: " + this.pTemporal);
	}
}
