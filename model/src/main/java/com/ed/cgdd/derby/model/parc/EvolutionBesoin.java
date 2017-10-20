package com.ed.cgdd.derby.model.parc;

import java.math.BigDecimal;

public class EvolutionBesoin {
	private String idBranche;
	private int annee;
	private Usage usage;
	private BigDecimal evolution;

	public int getAnnee() {
		return annee;
	}

	public void setAnnee(int annee) {
		this.annee = annee;
	}

	public Usage getUsage() {
		return usage;
	}

	public void setUsage(Usage usage) {
		this.usage = usage;
	}

	public BigDecimal getEvolution() {
		return evolution;
	}

	public void setEvolution(BigDecimal evolution) {
		this.evolution = evolution;
	}

	public String getIdBranche() {
		return idBranche;
	}

	public void setIdBranche(String idBranche) {
		this.idBranche = idBranche;
	}

}
