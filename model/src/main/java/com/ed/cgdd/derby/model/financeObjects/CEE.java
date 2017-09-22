package com.ed.cgdd.derby.model.financeObjects;

import java.math.BigDecimal;

public class CEE implements Financement {
	String periode;
	BigDecimal prixKWhCumac;
	String branche;

	public CEE(String string, BigDecimal val) {
		this.prixKWhCumac = val;
	}

	public CEE() {
		super();

	}

	public String getPeriode() {
		return periode;
	}

	public void setPeriode(String periode) {
		this.periode = periode;
	}

	public BigDecimal getPrixKWhCumac() {
		return prixKWhCumac;
	}

	public void setPrixKWhCumac(BigDecimal prixKWhCumac) {
		this.prixKWhCumac = prixKWhCumac;
	}

	public String getBranche() {
		return branche;
	}

	public void setBranche(String branche) {
		this.branche = branche;
	}

	@Override
	public FinancementType getType() {
		return FinancementType.CEE;
	}

}
