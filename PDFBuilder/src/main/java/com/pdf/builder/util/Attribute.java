package com.pdf.builder.util;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.pdf.builder.util.Global.Property;

public class Attribute {
	
	private Element element;
	
	public String type = "";
	public int numColumns = 0;
	public int row = 0;
	public int col = 0;
	public int colspan = 0;
	public int border = 15;
    public String fontFamily = "Times-Roman";
    public String fontColor = "#000000";
    public int fontStyle = 0;
    public int fontSize = 11;
    public String background = "#FFFFFF";
    public int alignment = 0;
    public String header = "";
    public String name = "";
    public String data = "";
    public int scale = 50;
    public int aux_1 = 0;
    public double aux_2 = 0;
    public String aux_3 = "";
    
    public Attribute() { }
    
    public Attribute(Node node) {
    	element = (Element) node;
    	
    	NamedNodeMap map = element.getAttributes();
    	
    	for (int index = 0; index < map.getLength(); index++) {
    		
    		String attribute = map.item(index).getNodeName();
    		String value = map.item(index).getNodeValue().trim();
    		
			if(attribute.equals(Property.TYPE.value())) this.type = value;
			else if(attribute.equals(Property.NUMBER_COLUMNS.value())) this.numColumns = Integer.parseInt(value);
			else if(attribute.equals(Property.ROW.value())) this.row = Integer.parseInt(value);
			else if(attribute.equals(Property.COLUMN.value())) this.col = Integer.parseInt(value);
			else if(attribute.equals(Property.COLUMN_SPAN.value())) this.colspan = Integer.parseInt(value);
			else if(attribute.equals(Property.BORDER.value())) this.border = Integer.parseInt(value);
			else if(attribute.equals(Property.FONT_FAMILY.value())) this.fontFamily = value;
			else if(attribute.equals(Property.FONT_COLOR.value())) this.fontColor = value;
			else if(attribute.equals(Property.FONT_STYLE.value())) this.fontStyle = Integer.parseInt(value);
			else if(attribute.equals(Property.FONT_SIZE.value())) this.fontSize = Integer.parseInt(value);
			else if(attribute.equals(Property.BACKGROUND.value())) this.background = value;
			else if(attribute.equals(Property.ALIGNMENT.value())) this.alignment = Integer.parseInt(value);
			else if(attribute.equals(Property.HEADER.value())) this.header = value;
			else if(attribute.equals(Property.NAME.value())) this.name = value;
			else if(attribute.equals(Property.DATA.value())) this.data = value;
			else if(attribute.equals(Property.SCALE.value())) this.scale = Integer.parseInt(value);
		}
    }
}
