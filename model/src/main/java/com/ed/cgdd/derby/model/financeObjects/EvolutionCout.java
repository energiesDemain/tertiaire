package com.ed.cgdd.derby.model.financeObjects;

import java.math.BigDecimal;

public class EvolutionCout {
	private String annee;
	private String type;
	private BigDecimal evolution;

	public String getAnnee() {
		return annee;
	}

	public void setAnnee(String annee) {
		this.annee = annee;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public BigDecimal getEvolution() {
		return evolution;
	}

	public void setEvolution(BigDecimal evolution) {
		this.evolution = evolution;
	}

}
