package com.ed.cgdd.derby.model.calcconso;

import java.math.BigDecimal;

public class ParamRdtCout {

	private String periode;
	private BigDecimal rdt;
	private BigDecimal cout;
	private String id;
	private BigDecimal CEE; // uniquement pour le chauffage

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPeriode() {
		return periode;
	}

	public void setPeriode(String periode) {
		this.periode = periode;
	}

	public BigDecimal getRdt() {
		return rdt;
	}

	public void setRdt(BigDecimal rdt) {
		this.rdt = rdt;
	}

	public BigDecimal getCout() {
		return cout;
	}

	public void setCout(BigDecimal cout) {
		this.cout = cout;
	}

	public BigDecimal getCEE() {
		return CEE;
	}

	public void setCEE(BigDecimal cEE) {
		CEE = cEE;
	}

}
