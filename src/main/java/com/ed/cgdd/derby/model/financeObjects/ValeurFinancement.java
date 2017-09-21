package com.ed.cgdd.derby.model.financeObjects;

import java.math.BigDecimal;

public class ValeurFinancement {
	BigDecimal surface;
	BigDecimal aides;
	BigDecimal coutInvestissement;
	BigDecimal valeurPret;
	BigDecimal valeurPretBonif;

	public ValeurFinancement(ResultatsFinancements result) {
		super();
		this.setAides(result.getValeurAides());
		this.setCoutInvestissement(result.getCoutInvestissement());
		this.setSurface(result.getSurface());
		this.setValeurPret(result.getValeurPBC());
		this.setValeurPretBonif(result.getValeurPretBonif());
	}

	public BigDecimal getSurface() {
		return surface;
	}

	public void setSurface(BigDecimal surface) {
		this.surface = surface;
	}

	public BigDecimal getAides() {
		return aides;
	}

	public void setAides(BigDecimal aides) {
		this.aides = aides;
	}

	public BigDecimal getCoutInvestissement() {
		return coutInvestissement;
	}

	public void setCoutInvestissement(BigDecimal coutInvestissement) {
		this.coutInvestissement = coutInvestissement;
	}

	public BigDecimal getValeurPret() {
		return valeurPret;
	}

	public void setValeurPret(BigDecimal valeurPret) {
		this.valeurPret = valeurPret;
	}

	public BigDecimal getValeurPretBonif() {
		return valeurPretBonif;
	}

	public void setValeurPretBonif(BigDecimal valeurPretBonif) {
		this.valeurPretBonif = valeurPretBonif;
	}
}
