package com.ed.cgdd.derby.model.parc;

public enum SysFroid {
	AUCUN("01"), DRV("02"), GROUPE_ANCIEN("03"), NON_CHAUFFE("04"), NR("05"), PAC("06"), ROOFTOP("07");

	private String code;

	private SysFroid(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

}
