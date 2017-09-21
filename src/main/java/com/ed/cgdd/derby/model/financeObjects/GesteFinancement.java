package com.ed.cgdd.derby.model.financeObjects;

import java.util.List;

public class GesteFinancement {
	Geste geste;
	List<ListeFinanceValeur> listeFinancement;
	CoutRenovation coutRenov;
	String nomGesteFinance;

	public List<ListeFinanceValeur> getListeFinancement() {
		return listeFinancement;
	}

	public void setListeFinancement(List<ListeFinanceValeur> listeFinancement) {
		this.listeFinancement = listeFinancement;
	}

	public Geste getGeste() {
		return geste;
	}

	public void setGeste(Geste geste) {
		this.geste = geste;
	}

	public CoutRenovation getCoutRenov() {
		return coutRenov;
	}

	public void setCoutRenov(CoutRenovation coutRenov) {
		this.coutRenov = coutRenov;
	}

	public String getNomGesteFinance() {
		return nomGesteFinance;
	}

	public void setNomGesteFinance(String nomGesteFinance) {
		this.nomGesteFinance = nomGesteFinance;
	}

}
