package com.ed.cgdd.derby.model.financeObjects;

import java.math.BigDecimal;

public class PartsMarche {
	private BigDecimal partOblig;
	private BigDecimal partDecret;
	private BigDecimal partRenouvSys;
	private BigDecimal partRegl;
	private BigDecimal partHorsRegl;

	public BigDecimal getPartRenouvSys() {
		return partRenouvSys;
	}

	public void setPartRenouvSys(BigDecimal partRenouvSys) {
		this.partRenouvSys = partRenouvSys;
	}

	public BigDecimal getPartHorsRegl() {
		return partHorsRegl;
	}

	public void setPartHorsRegl(BigDecimal partHorsRegl) {
		this.partHorsRegl = partHorsRegl;
	}

	public BigDecimal getPartOblig() {
		return partOblig;
	}

	public void setPartOblig(BigDecimal partOblig) {
		this.partOblig = partOblig;
	}

	public BigDecimal getPartRegl() {
		return partRegl;
	}

	public void setPartRegl(BigDecimal partRegl) {
		this.partRegl = partRegl;
	}

	public BigDecimal getPartDecret() {
		return partDecret;
	}

	public void setPartDecret(BigDecimal partDecret) {
		this.partDecret = partDecret;
	}

}
