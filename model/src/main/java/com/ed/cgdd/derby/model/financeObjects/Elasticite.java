package com.ed.cgdd.derby.model.financeObjects;

import java.math.BigDecimal;

public class Elasticite {

	private String idBranche;
	private String idEnergie;
	private String usage;
	private BigDecimal[] periode = new BigDecimal[6];

	public String getIdEnergie() {
		return idEnergie;
	}

	public void setIdEnergie(String idEnergie) {
		this.idEnergie = idEnergie;
	}

	public String getUsage() {
		return usage;
	}

	public void setUsage(String usage) {
		this.usage = usage;
	}

	public BigDecimal[] getPeriode() {
		return periode;
	}

	public void setPeriode(BigDecimal[] periode) {
		this.periode = periode;
	}

	public BigDecimal getPeriode(int index) {
		return periode[index];
	}

	public void setPeriode(int index, BigDecimal valeurPeriode) {
		this.periode[index] = valeurPeriode;
	}

	public String getIdBranche() {
		return idBranche;
	}

	public void setIdBranche(String idBranche) {
		this.idBranche = idBranche;
	}

}
