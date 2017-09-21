package com.ed.cgdd.derby.model.financeObjects;

import java.math.BigDecimal;

public class PretBonif extends PBC {
	Exigence ecoCond;
	BigDecimal pretMax;

	public Exigence getEcoCond() {
		return ecoCond;
	}

	public void setEcoCond(Exigence ecoCond) {
		this.ecoCond = ecoCond;
	}

	public BigDecimal getPretMax() {
		return pretMax;
	}

	public void setPretMax(BigDecimal pretMax) {
		this.pretMax = pretMax;
	}

	@Override
	public FinancementType getType() {
		return FinancementType.PretBonif;
	}

}
