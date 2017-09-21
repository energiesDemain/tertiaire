package com.ed.cgdd.derby.model.financeObjects;

import java.math.BigDecimal;

public class CoutRenovation {
	String nomGeste; // nom du geste
	int duree; // duree de vie de la renovation
	BigDecimal CTA; // cout des travaux additionnels
	BigDecimal CT; // cout travaux = CINV
	BigDecimal CINT; // Cout intangibles
	BigDecimal CEini; // charges energetiques initiales
	BigDecimal maintenance;

	public String getNomGeste() {
		return nomGeste;
	}

	public void setNomGeste(String nomGeste) {
		this.nomGeste = nomGeste;
	}

	public int getDuree() {
		return duree;
	}

	public void setDuree(int duree) {
		this.duree = duree;
	}

	public BigDecimal getCTA() {
		return CTA;
	}

	public void setCTA(BigDecimal cTA) {
		CTA = cTA;
	}

	public BigDecimal getCT() {
		return CT;
	}

	public void setCT(BigDecimal cT) {
		CT = cT;
	}

	public BigDecimal getCINT() {
		return CINT;
	}

	public void setCINT(BigDecimal cINT) {
		CINT = cINT;
	}

	public BigDecimal getCEini() {
		return CEini;
	}

	public void setCEini(BigDecimal cEini) {
		CEini = cEini;
	}

	public BigDecimal getMaintenance() {
		return maintenance;
	}

	public void setMaintenance(BigDecimal maintenance) {
		this.maintenance = maintenance;
	}

}
