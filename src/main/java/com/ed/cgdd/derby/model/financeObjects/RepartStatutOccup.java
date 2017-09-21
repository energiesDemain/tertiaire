package com.ed.cgdd.derby.model.financeObjects;

import java.math.BigDecimal;

public class RepartStatutOccup {
	private String IdBranche;
	private String StatutOccup;
	private BigDecimal Repart;

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

	public BigDecimal getRepart() {
		return Repart;
	}

	public void setRepart(BigDecimal repart) {
		Repart = repart;
	}

}
