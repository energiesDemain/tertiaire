package com.ed.cgdd.derby.model.parc;

import java.math.BigDecimal;

public class PmNeuf {
	private String idEnerg;
	private String idSyschaud;
	private BigDecimal part;

	public String getIdEnerg() {
		return idEnerg;
	}

	public void setIdEnerg(String idEnerg) {
		this.idEnerg = idEnerg;
	}

	public String getIdSyschaud() {
		return idSyschaud;
	}

	public void setIdSyschaud(String idSyschaud) {
		this.idSyschaud = idSyschaud;
	}

	public BigDecimal getPart() {
		return part;
	}

	public void setPart(BigDecimal part) {
		this.part = part;
	}

}
