package com.ed.cgdd.derby.model.financeObjects;

public enum Exigence {
	BBC_RENOVATION("01"), RT_PAR_ELEMENT("02"), AUCUNE("03"), GTB("04");
	private String code;

	private Exigence(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

}
