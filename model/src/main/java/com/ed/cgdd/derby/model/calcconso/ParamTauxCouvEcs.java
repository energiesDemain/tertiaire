package com.ed.cgdd.derby.model.calcconso;

import java.math.BigDecimal;

public class ParamTauxCouvEcs {

	private String etat_bat;

	public String getEtatbat() {
		return etat_bat;
	}

	public void setEtatbat(String etat_bat) {
		this.etat_bat = etat_bat;
	}

	public BigDecimal getTxcouv() {
		return tx_couv;
	}

	public void setTxcouv(BigDecimal tx_couv) {
		this.tx_couv = tx_couv;
	}

	private BigDecimal tx_couv;

}
