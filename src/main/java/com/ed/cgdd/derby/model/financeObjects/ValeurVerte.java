package com.ed.cgdd.derby.model.financeObjects;

import java.math.BigDecimal;

public class ValeurVerte {
	private BigDecimal valeurProp;
	private BigDecimal valeurLoc;

	public BigDecimal getValeurProp() {
		return valeurProp;
	}

	public void setValeurProp(BigDecimal valeurProp) {
		this.valeurProp = valeurProp;
	}

	public BigDecimal getValeurLoc() {
		return valeurLoc;
	}

	public void setValeurLoc(BigDecimal valeurLoc) {
		this.valeurLoc = valeurLoc;
	}

}
