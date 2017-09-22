package com.ed.cgdd.derby.model.financeObjects;

import java.math.BigDecimal;

public class ListeFinanceValeur {
	Financement finance;
	BigDecimal valeur;

	public Financement getFinance() {
		return finance;
	}

	public void setFinance(Financement finance) {
		this.finance = finance;
	}

	public BigDecimal getValeur() {
		return valeur;
	}

	public void setValeur(BigDecimal valeur) {
		this.valeur = valeur;
	}

	public ListeFinanceValeur(Financement finance, BigDecimal valeur) {
		super();
		this.finance = finance;
		this.valeur = valeur;
	}

}
