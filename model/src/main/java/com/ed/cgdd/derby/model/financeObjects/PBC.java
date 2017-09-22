package com.ed.cgdd.derby.model.financeObjects;

import java.math.BigDecimal;

public class PBC implements Financement {
	String periode;
	int duree;
	BigDecimal tauxInteret;
	String branche;

	public String getPeriode() {
		return periode;
	}

	public void setPeriode(String periode) {
		this.periode = periode;
	}

	public int getDuree() {
		return duree;
	}

	public void setDuree(int duree) {
		this.duree = duree;
	}

	public BigDecimal getTauxInteret() {
		return tauxInteret;
	}

	public void setTauxInteret(BigDecimal tauxInteret) {
		this.tauxInteret = tauxInteret;
	}

	public String getBranche() {
		return branche;
	}

	public void setBranche(String branche) {
		this.branche = branche;
	}

	@Override
	public FinancementType getType() {
		return FinancementType.PBC;
	}

}
