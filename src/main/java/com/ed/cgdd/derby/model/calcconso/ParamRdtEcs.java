package com.ed.cgdd.derby.model.calcconso;

import java.math.BigDecimal;

public class ParamRdtEcs {

	private String idBranche;
	private String idEnergie;
	private BigDecimal[] rdt;

	public String getIdBranche() {
		return idBranche;
	}

	public void setIdBranche(String idBranche) {
		this.idBranche = idBranche;
	}

	public String getIdEnergie() {
		return idEnergie;
	}

	public void setIdEnergie(String idEnergie) {
		this.idEnergie = idEnergie;
	}

	public BigDecimal getRdt(int index) {
		return rdt[index];
	}

	public void setRdt(int index, BigDecimal valeurRdt) {
		this.rdt[index] = valeurRdt;
	}

	public ParamRdtEcs() {
		rdt = new BigDecimal[6];
	}

}
