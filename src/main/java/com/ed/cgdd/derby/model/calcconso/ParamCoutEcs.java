package com.ed.cgdd.derby.model.calcconso;

import java.math.BigDecimal;

public class ParamCoutEcs {

	private String energie;
	private String performance;
	private BigDecimal cout;

	public String getIdenergie() {
		return energie;
	}

	public void setIdenergie(String energie) {
		this.energie = energie;
	}

	public String getPerformance() {
		return performance;
	}

	public void setPerformance(String performance) {
		this.performance = performance;
	}

	public BigDecimal getCout() {
		return cout;
	}

	public void setCout(BigDecimal cout) {
		this.cout = cout;
	}

}
