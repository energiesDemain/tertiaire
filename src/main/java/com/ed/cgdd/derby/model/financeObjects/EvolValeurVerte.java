package com.ed.cgdd.derby.model.financeObjects;

import java.math.BigDecimal;

public class EvolValeurVerte {
	private String IdBranche;
	private String StatutOccup;
	private BigDecimal Evol;

	public String getIdBranche() {
		return IdBranche;
	}

	public void setIdBranche(String idBranche) {
		IdBranche = idBranche;
	}

	public String getStatutOccup() {
		return StatutOccup;
	}

	public void setStatutOccup(String statutOccup) {
		StatutOccup = statutOccup;
	}

	public BigDecimal getEvol() {
		return Evol;
	}

	public void setEvol(BigDecimal evol) {
		Evol = evol;
	}

}
