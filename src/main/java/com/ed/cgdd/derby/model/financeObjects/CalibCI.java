package com.ed.cgdd.derby.model.financeObjects;

import java.math.BigDecimal;

public class CalibCI {
	String branche;
	String batType;
	String systeme;
	String energie;
	BigDecimal coutM2;
	int dureeVie;
	BigDecimal besoinUnitaire;
	BigDecimal rdt;
	BigDecimal partMarche2009;
	BigDecimal coutEner;
	Boolean performant;

	public String getBranche() {
		return branche;
	}

	public void setBranche(String branche) {
		this.branche = branche;
	}

	public String getBatType() {
		return batType;
	}

	public void setBatType(String batType) {
		this.batType = batType;
	}

	public String getSysteme() {
		return systeme;
	}

	public void setSysteme(String systeme) {
		this.systeme = systeme;
	}

	public void setEnergies(String energie) {
		this.energie = energie;
	}

	public String getEnergies() {
		return energie;
	}

	public BigDecimal getCoutM2() {
		return coutM2;
	}

	public void setCoutM2(BigDecimal coutM2) {
		this.coutM2 = coutM2;
	}

	public int getDureeVie() {
		return dureeVie;
	}

	public void setDureeVie(int dureeVie) {
		this.dureeVie = dureeVie;
	}

	public BigDecimal getBesoinUnitaire() {
		return besoinUnitaire;
	}

	public void setBesoinUnitaire(BigDecimal besoinUnitaire) {
		this.besoinUnitaire = besoinUnitaire;
	}

	public BigDecimal getRdt() {
		return rdt;
	}

	public void setRdt(BigDecimal rdt) {
		this.rdt = rdt;
	}

	public BigDecimal getPartMarche2009() {
		return partMarche2009;
	}

	public void setPartMarche2009(BigDecimal partMarche2009) {
		this.partMarche2009 = partMarche2009;
	}

	public BigDecimal getCoutEner() {
		return coutEner;
	}

	public void setCoutEner(BigDecimal coutEner) {
		this.coutEner = coutEner;
	}

	public Boolean getPerformant() {
		return performant;
	}

	public void setPerformant(Boolean performant) {
		this.performant = performant;
	}

}
