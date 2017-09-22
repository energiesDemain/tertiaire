package com.ed.cgdd.derby.model.parc;

import java.math.BigDecimal;

public class ParamParc {
	private String branche;

	public String getBranche() {
		return branche;
	}

	public void setBranche(String branche) {
		this.branche = branche;
	}

	public BigDecimal getPeriode1() {
		return periode1;
	}

	public void setPeriode1(BigDecimal periode1) {
		this.periode1 = periode1;
	}

	public BigDecimal getPeriode2() {
		return periode2;
	}

	public void setPeriode2(BigDecimal periode2) {
		this.periode2 = periode2;
	}

	public BigDecimal getPeriode3() {
		return periode3;
	}

	public void setPeriode3(BigDecimal periode3) {
		this.periode3 = periode3;
	}

	public BigDecimal getPeriode4() {
		return periode4;
	}

	public void setPeriode4(BigDecimal periode4) {
		this.periode4 = periode4;
	}

	public BigDecimal getPeriode5() {
		return periode5;
	}

	public void setPeriode5(BigDecimal periode5) {
		this.periode5 = periode5;
	}

	private BigDecimal periode1;
	private BigDecimal periode2;
	private BigDecimal periode3;
	private BigDecimal periode4;
	private BigDecimal periode5;

}
