package com.ed.cgdd.derby.model.parc;

import java.math.BigDecimal;

public class ParamCInt {
	private int nu;
	private BigDecimal cintRef;


	public int getNu() {
		return nu;
	}

	public void setNu(int nu) {
		this.nu = nu;
	}

	public BigDecimal getCintRef() {
		return cintRef;
	}

	public void setCintRef(BigDecimal cintRef) {
		this.cintRef = cintRef;
	}

	public void ParamCint(){};

	public void ParamCint(int nu, BigDecimal cintRef){
		this.nu = nu;
		this.cintRef=cintRef;
	}
}
