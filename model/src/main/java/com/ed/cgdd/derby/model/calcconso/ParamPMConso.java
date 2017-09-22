package com.ed.cgdd.derby.model.calcconso;

import java.math.BigDecimal;
import java.util.HashMap;

public class ParamPMConso {

	private String idBranche;
	private HashMap<String, BigDecimal> energie = new HashMap<String, BigDecimal>();

	public String getIDBranche() {
		return idBranche;
	}

	public void setIDBranche(String idBranche) {
		this.idBranche = idBranche;
	}

	public HashMap<String, BigDecimal> getEnergie() {
		return energie;
	}

	public void setEnergie(String key, BigDecimal valeurEnergie) {
		this.energie.put(key, valeurEnergie);
	}

	public BigDecimal getEnergie(String key) {
		return energie.get(key);
	}

}
