package com.ed.cgdd.derby.model.calcconso;

import java.math.BigDecimal;

public class PmClimEntrant {

	private String idBranche;
	private String idSysfroid;
	private BigDecimal part;

	public String getIdBranche() {
		return idBranche;
	}

	public void setIdBranche(String idBranche) {
		this.idBranche = idBranche;
	}

	public String getIdSysfroid() {
		return idSysfroid;
	}

	public void setIdSysfroid(String idSysfroid) {
		this.idSysfroid = idSysfroid;
	}

	public BigDecimal getPart() {
		return part;
	}

	public void setPart(BigDecimal part) {
		this.part = part;
	}

}
