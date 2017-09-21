package com.ed.cgdd.derby.model.financeObjects;


public class ObligationTravauxExig {

	private String secteur;

	private String[] exigence = new String[6];

	public String getSecteur() {
		return secteur;
	}

	public void setSecteur(String secteur) {
		this.secteur = secteur;
	}

	public String[] getExigence() {
		return exigence;
	}

	public void setExigence(String[] partSurf) {
		this.exigence = partSurf;
	}

	public String getExigence(int index) {
		return exigence[index];
	}

	public void setExigence(int index, String valeurPartSurf) {
		this.exigence[index] = valeurPartSurf;
	}

}
