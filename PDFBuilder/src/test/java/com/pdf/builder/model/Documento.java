package com.pdf.builder.model;

import java.util.ArrayList;
import java.util.List;

public class Documento {

	public long id;
	public String ruc;
	public String serie;
	public String correlativo;
	public String direccion_fiscal;
	public String denominacion;
	public String fecha_emision = "2017-11-09";
	public String moneda;
	public String observacion;
	public byte[] logo;
	public int tipo_doc_adquiriente = 1;
	public String num_doc_adquiriente = "47546977";
	public String tipo_comprobante = "03";
	public double monto_igv = 18.81;
	
	public List<Detalle> detalle;
	
	public Documento(long id, String ruc, String serie, String correlativo, String direccion_fiscal, String denominacion, String moneda, String observaciones, byte[] logo) {
		super();
		this.id = id;
		this.ruc = ruc;
		this.serie = serie;
		this.correlativo = correlativo;
		this.direccion_fiscal = direccion_fiscal;
		this.denominacion = denominacion;
		this.moneda = moneda;
		this.observacion = observaciones;
		this.logo = logo;
		
		this.detalle = new ArrayList<Detalle>();
		this.detalle.add(new Detalle(6, "KG", "P001", "Producto N° 1", 13));
		this.detalle.add(new Detalle(2, "LT", "P002", "Producto N° 2", 5));
		this.detalle.add(new Detalle(4, "LT", "P003", "Producto N° 3", 2));
		this.detalle.add(new Detalle(1, "KG", "P004", "Producto N° 4", 28));
	}
}
