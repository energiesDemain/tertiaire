package com.ed.cgdd.derby.model.financeObjects;

import java.math.BigDecimal;
import java.util.HashMap;

public class CalculPM {
	private BigDecimal sommeCGProp;
	private HashMap<String, CoutFinal> coutFinalPropMap;
	private BigDecimal sommeCGLoc;
	private HashMap<String, CoutFinal> coutFinalLocMap;

	public BigDecimal getSommeCGProp() {
		return sommeCGProp;
	}

	public void setSommeCGProp(BigDecimal sommeCGProp) {
		this.sommeCGProp = sommeCGProp;
	}

	public HashMap<String, CoutFinal> getCoutFinalPropMap() {
		return coutFinalPropMap;
	}

	public void setCoutFinalPropMap(HashMap<String, CoutFinal> coutFinalPropMap) {
		this.coutFinalPropMap = coutFinalPropMap;
	}

	public BigDecimal getSommeCGLoc() {
		return sommeCGLoc;
	}

	public void setSommeCGLoc(BigDecimal sommeCGLoc) {
		this.sommeCGLoc = sommeCGLoc;
	}

	public HashMap<String, CoutFinal> getCoutFinalLocMap() {
		return coutFinalLocMap;
	}

	public void setCoutFinalLocMap(HashMap<String, CoutFinal> coutFinalLocMap) {
		this.coutFinalLocMap = coutFinalLocMap;
	}

}
