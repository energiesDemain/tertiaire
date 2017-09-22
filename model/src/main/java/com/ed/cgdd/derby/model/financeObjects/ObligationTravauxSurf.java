package com.ed.cgdd.derby.model.financeObjects;

import java.math.BigDecimal;

public class ObligationTravauxSurf {

	private String secteur;

	private BigDecimal[] partSurf = new BigDecimal[6];

	public String getSecteur() {
		return secteur;
	}

	public void setSecteur(String secteur) {
		this.secteur = secteur;
	}

	public BigDecimal[] getPartSurf() {
		return partSurf;
	}

	public void setPartSurf(BigDecimal[] partSurf) {
		this.partSurf = partSurf;
	}

	public BigDecimal getPartSurf(int index) {
		return partSurf[index];
	}

	public void setPartSurf(int index, BigDecimal valeurPartSurf) {
		this.partSurf[index] = valeurPartSurf;
	}

}
