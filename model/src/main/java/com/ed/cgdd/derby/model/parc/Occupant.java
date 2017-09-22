package com.ed.cgdd.derby.model.parc;

public enum Occupant {
	BLOC_COMMUNAL("01"), DEPARTEMENTS("02"), ETAT("03"), PARA_PUBLIC("04"), PRIVE("05"), REGIONS("06");

	private String code;

	private Occupant(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

}
