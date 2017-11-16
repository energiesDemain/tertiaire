package com.ed.cgdd.derby.model.excelobjects;

import java.math.BigDecimal;

public class BesoinConsoForCSV {

	/* Extractions des consos */
	private String branche;
	private Integer annee;
	private String codePeriodeSimple;
	private String usage;
	private String energie;
	private BigDecimal consoTot;
	private BigDecimal besoinTot;
	private String systemFroid;
	private String surfaceTot;

	public String getSurfaceTot() {
		return surfaceTot;
	}

	public void setSurfaceTot(String surfaceTot) {
		this.surfaceTot = surfaceTot;
	}

	public String getSystemFroid() {
		return systemFroid;
	}

	public void setSystemFroid(String systemFroid) {
		this.systemFroid = systemFroid;
	}

	public String getBranche() {
		return branche;
	}

	public void setBranche(String branche) {
		this.branche = branche;
	}

	public Integer getAnnee() {
		return annee;
	}

	public void setAnnee(Integer annee) {
		this.annee = annee;
	}

	public String getCodePeriodeSimple() {
		return codePeriodeSimple;
	}

	public void setCodePeriodeSimple(String codePeriodeSimple) {
		this.codePeriodeSimple = codePeriodeSimple;
	}

	public String getUsage() {
		return usage;
	}

	public void setUsage(String usage) {
		this.usage = usage;
	}

	public String getEnergie() {
		return energie;
	}

	public void setEnergie(String energie) {
		this.energie = energie;
	}

	public BigDecimal getConsoTot() {
		return consoTot;
	}

	public void setConsoTot(BigDecimal consoTot) {
		this.consoTot = consoTot;
	}

	public BigDecimal getBesoinTot() {
		return besoinTot;
	}

	public void setBesoinTot(BigDecimal besoinTot) {
		this.besoinTot = besoinTot;
	}
}
