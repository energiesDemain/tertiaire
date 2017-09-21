package com.ed.cgdd.derby.model.financeObjects;

import java.math.BigDecimal;

public class StatutOccup {
	private BigDecimal partLoc;
	private BigDecimal partProp;
	private PBC tauxActuLoc;
	private PBC tauxActuProp;

	public BigDecimal getPartLoc() {
		return partLoc;
	}

	public void setPartLoc(BigDecimal partLoc) {
		this.partLoc = partLoc;
	}

	public BigDecimal getPartProp() {
		return partProp;
	}

	public void setPartProp(BigDecimal partProp) {
		this.partProp = partProp;
	}

	public PBC getTauxActuLoc() {
		return tauxActuLoc;
	}

	public void setTauxActuLoc(PBC tauxActuLoc) {
		this.tauxActuLoc = tauxActuLoc;
	}

	public PBC getTauxActuProp() {
		return tauxActuProp;
	}

	public void setTauxActuProp(PBC tauxActuProp) {
		this.tauxActuProp = tauxActuProp;
	}

}
