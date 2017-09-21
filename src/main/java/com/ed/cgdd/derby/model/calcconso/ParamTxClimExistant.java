package com.ed.cgdd.derby.model.calcconso;

import java.math.BigDecimal;

public class ParamTxClimExistant {

	private String id_branche;
	private BigDecimal[] txClim = new BigDecimal[6];

	public String getIdbranche() {
		return id_branche;
	}

	public void setIdbranche(String id_branche) {
		this.id_branche = id_branche;
	}

	public BigDecimal[] getTx() {
		return txClim;
	}

	public BigDecimal getTx(int index) {
		return txClim[index];
	}

	public void setTx(int index, BigDecimal valeurTx) {
		this.txClim[index] = valeurTx;
	}

}
