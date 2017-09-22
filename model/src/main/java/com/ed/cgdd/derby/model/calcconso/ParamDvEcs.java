package com.ed.cgdd.derby.model.calcconso;

import java.math.BigDecimal;

public class ParamDvEcs {

	private String idEnergie;

	public String getIdEnergie() {
		return idEnergie;
	}

	public void setIdEnergie(String idEnergie) {
		this.idEnergie = idEnergie;
	}

	public BigDecimal getDureeVie() {
		return dureeVie;
	}

	public void setDureeVie(BigDecimal dureeVie) {
		this.dureeVie = dureeVie;
	}

	private BigDecimal dureeVie;

}
