package com.ed.cgdd.derby.model.parc;

import java.math.BigDecimal;

public class ParamParcArray {
	private String branche;
	private BigDecimal[] periode = new BigDecimal[6];

	public BigDecimal getPeriode(int index) {
		return periode[index];
	}

	public void setPeriode(int index, BigDecimal valeurPeriode) {
		this.periode[index] = valeurPeriode;
	}

	public String getBranche() {
		return branche;
	}

	public void setBranche(String branche) {
		this.branche = branche;
	}

}
