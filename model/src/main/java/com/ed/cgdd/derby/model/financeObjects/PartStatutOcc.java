package com.ed.cgdd.derby.model.financeObjects;

import java.math.BigDecimal;

public class PartStatutOcc {
	BigDecimal part;
	String statutOccupation;

	public BigDecimal getPart() {
		return part;
	}

	public void setPart(BigDecimal part) {
		this.part = part;
	}

	public String getStatutOccupation() {
		return statutOccupation;
	}

	public void setStatutOccupation(String statutOccupation) {
		this.statutOccupation = statutOccupation;
	}

}
