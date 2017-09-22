package com.ed.cgdd.test.model;

public class Determinant {
	public String detlib ;
	public String type ;
	
	public Determinant(String detlib, String type) {
		this.detlib = detlib ;
		this.type = type ;
	}
	
	public String getDetlib() {
		return detlib;
	}
	public void setDetlib(String detlib) {
		this.detlib = detlib;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}

}
