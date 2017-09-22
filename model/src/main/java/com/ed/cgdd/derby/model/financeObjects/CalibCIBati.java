package com.ed.cgdd.derby.model.financeObjects;

import java.math.BigDecimal;

public class CalibCIBati {
	String branche;
	String geste;
	BigDecimal chargeInit;
	BigDecimal coutMoy;
	BigDecimal gainMoy;
	BigDecimal partMarche;
	int dureeVie;

	public String getBranche() {
		return branche;
	}

	public void setBranche(String branche) {
		this.branche = branche;
	}

	public String getGeste() {
		return geste;
	}

	public void setGeste(String geste) {
		this.geste = geste;
	}

	public BigDecimal getChargeInit() {
		return chargeInit;
	}

	public void setChargeInit(BigDecimal chargeInit) {
		this.chargeInit = chargeInit;
	}

	public BigDecimal getCoutMoy() {
		return coutMoy;
	}

	public void setCoutMoy(BigDecimal coutMoy) {
		this.coutMoy = coutMoy;
	}

	public BigDecimal getGainMoy() {
		return gainMoy;
	}

	public void setGainMoy(BigDecimal gainMoy) {
		this.gainMoy = gainMoy;
	}

	public BigDecimal getPartMarche() {
		return partMarche;
	}

	public void setPartMarche(BigDecimal partMarche) {
		this.partMarche = partMarche;
	}

	public int getDureeVie() {
		return dureeVie;
	}

	public void setDureeVie(int dureeVie) {
		this.dureeVie = dureeVie;
	}

}
