package com.pdf.builder.model;

public class Detalle {

	public int cantidad;
	public String unidad_medida;
	public String codigo;
	public String descripcion;
	public double precio_unitario;
	
	public Detalle(int cantidad, String unidad_medida, String codigo, 
			String descripcion, double precio_unitario) {
		super();
		this.cantidad = cantidad;
		this.unidad_medida = unidad_medida;
		this.codigo = codigo;
		this.descripcion = descripcion;
		this.precio_unitario = precio_unitario;
	}
}

