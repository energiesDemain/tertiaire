package com.ed.cgdd.derby.model.calcconso;

import java.math.BigDecimal;

public class ParamBesoinsNeufs {

	private String idBranche;
	private String idBatType;
	private String usage;
	private BigDecimal[] periode = new BigDecimal[6];

	public String getIdpartiel() {
		return idBranche + idBatType + usage;
	}

	public String getIDBranche() {
		return idBranche;
	}

	public void setIDBranche(String idBranche) {
		this.idBranche = idBranche;
	}

	public String getIDBat_type() {
		return idBatType;
	}

	public void setIDBat_type(String idBatType) {
		this.idBatType = idBatType;
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

}
