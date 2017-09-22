package com.ed.cgdd.derby.model.financeObjects;

import java.math.BigDecimal;

public class DecretTravaux {

	private String branche;
	private String secteur;
	private BigDecimal partSurf;
	private BigDecimal tri;
	private BigDecimal coutMax;
	private BigDecimal gainMin;
	private BigDecimal debut;
	private BigDecimal fin;

	public String getBranche() {
		return branche;
	}

	public void setBranche(String branche) {
		this.branche = branche;
	}

	public String getSecteur() {
		return secteur;
	}

	public void setSecteur(String secteur) {
		this.secteur = secteur;
	}

	public BigDecimal getPartSurf() {
		return partSurf;
	}

	public void setPartSurf(BigDecimal partSurf) {
		this.partSurf = partSurf;
	}

	public BigDecimal getTri() {
		return tri;
	}

	public void setTri(BigDecimal tri) {
		this.tri = tri;
	}

	public BigDecimal getCoutMax() {
		return coutMax;
	}

	public void setCoutMax(BigDecimal coutMax) {
		this.coutMax = coutMax;
	}

	public BigDecimal getGainMin() {
		return gainMin;
	}

	public void setGainMin(BigDecimal gainMin) {
		this.gainMin = gainMin;
	}

	public BigDecimal getDebut() {
		return debut;
	}

	public void setDebut(BigDecimal debut) {
		this.debut = debut;
	}

	public BigDecimal getFin() {
		return fin;
	}

	public void setFin(BigDecimal fin) {
		this.fin = fin;
	}

}
