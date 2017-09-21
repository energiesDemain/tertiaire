package com.ed.cgdd.derby.model.calcconso;

import java.math.BigDecimal;

public class ParamPartSolaireEcs {

	private String etat_bat;
	private String id_branche;
	private BigDecimal[] part = new BigDecimal[6];

	public String getEtatbat() {
		return etat_bat;
	}

	public void setEtatbat(String etat_bat) {
		this.etat_bat = etat_bat;
	}

	public String getIdbranche() {
		return id_branche;
	}

	public void setIdbranche(String id_branche) {
		this.id_branche = id_branche;
	}

	public BigDecimal[] getPart() {
		return part;
	}

	public BigDecimal getPart(int index) {
		return part[index];
	}

	public void setPart(int index, BigDecimal valeurPart) {
		this.part[index] = valeurPart;
	}

}
