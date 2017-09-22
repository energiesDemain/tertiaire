package com.ed.cgdd.derby.model.calcconso;

import java.math.BigDecimal;

public class dvObject {

	private String keyAgreg;

	public String getKeyAgreg() {
		return keyAgreg;
	}

	public void setKeyAgreg(String keyAgreg) {
		this.keyAgreg = keyAgreg;
	}

	private String usage;
	private String idSys;
	private String idEnergie;
	private BigDecimal[] dureeVie;

	public String getUsage() {
		return usage;
	}

	public void setUsage(String usage) {
		this.usage = usage;
	}

	public String getIdSys() {
		return idSys;
	}

	public void setIdSys(String idSys) {
		this.idSys = idSys;
	}

	public String getIdEnergie() {
		return idEnergie;
	}

	public void setIdEnergie(String idEnergie) {
		this.idEnergie = idEnergie;
	}

	public BigDecimal getDureeVie(int index) {
		return dureeVie[index];
	}

	public void setDureeVie(int index, BigDecimal valeur) {
		this.dureeVie[index] = valeur;
	}

	public dvObject() {
		dureeVie = new BigDecimal[40];
	}

}
