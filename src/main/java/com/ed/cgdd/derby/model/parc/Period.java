package com.ed.cgdd.derby.model.parc;

public enum Period {
	PERIODE_2041_2050("08"), PERIODE_2031_2040("07"), PERIODE_2021_2030("06"), PERIODE_2016_2020("05"), PERIODE_2010_2015(
			"04"), PERIODE_BEFORE_1980("03"), PERIODE_1981_1998("02"), PERIODE_1999_2008("01");

	private String code;

	private Period(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

}
