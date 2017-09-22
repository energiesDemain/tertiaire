package com.ed.cgdd.derby.model.calcconso;

import java.math.BigDecimal;

public class ParamCoutEclVentil {

	private String usage;
	private String branche;
	private BigDecimal cout;

	public String getUsage() {
		return usage;
	}

	public void setUsage(String usage) {
		this.usage = usage;
	}

	public String getIdbranche() {
		return branche;
	}

	public void setIdbranche(String branche) {
		this.branche = branche;
	}

	public BigDecimal getCout() {
		return cout;
	}

	public void setCout(BigDecimal cout) {
		this.cout = cout;
	}

}
