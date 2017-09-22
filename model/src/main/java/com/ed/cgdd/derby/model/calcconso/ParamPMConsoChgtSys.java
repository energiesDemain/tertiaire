package com.ed.cgdd.derby.model.calcconso;

import java.math.BigDecimal;
import java.util.HashMap;

public class ParamPMConsoChgtSys {

	private String energInit = new String();
	private HashMap<String, BigDecimal> pmChgt = new HashMap<String, BigDecimal>();

	public String getEnergInit() {
		return energInit;
	}

	public void setEnergInit(String energInit) {
		this.energInit = energInit;
	}

	public HashMap<String, BigDecimal> getPmChgt() {
		return pmChgt;
	}

	public void setPmChgt(String key, BigDecimal valeurPmChgt) {
		this.pmChgt.put(key, valeurPmChgt);
	}

	public BigDecimal getPmChgt(String key) {
		return pmChgt.get(key);
	}

}
