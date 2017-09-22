package com.ed.cgdd.derby.model.financeObjects;

import java.math.BigDecimal;
import java.util.HashMap;

public class CoutEnergie {
	Integer annee;
	BigDecimal CCE;
	private HashMap<String, BigDecimal> energie = new HashMap<String, BigDecimal>();

	public HashMap<String, BigDecimal> getEnergie() {
		return energie;
	}

	public void setEnergie(String key, BigDecimal valeurEnergie) {
		this.energie.put(key, valeurEnergie);
	}

	public BigDecimal getEnergie(String key) {
		return energie.get(key);
	}

	public Integer getAnnee() {
		return annee;
	}

	public void setAnnee(Integer annee) {
		this.annee = annee;
	}

	public BigDecimal getCCE() {
		return CCE;
	}

	public void setCCE(BigDecimal cCE) {
		CCE = cCE;
	}

}
