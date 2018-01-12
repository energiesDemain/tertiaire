package com.ed.cgdd.derby.model.parc;

public enum PeriodDetail { PERIODE_1916_1948("01"), PERIODE_1949_1978("02"), PERIODE_1961_1980("03"), 
	PERIODE_1975_1982("04"), PERIODE_1979_1999("05"), PERIODE_1980_1999("06"), PERIODE_1981_1998("07"), 
	PERIODE_1981_1999("08"), PERIODE_1983_1988("09"), PERIODE_1989_1999("10"), PERIODE_1999_2008("11"), 
	PERIODE_2000_2007("12"), PERIODE_BEFORE_1915("13"), PERIODE_BEFORE_1948("14"), PERIODE_BEFORE_1960("15"), 
	PERIODE_BEFORE_1974("16"), PERIODE_BEFORE_1980("17"),PERIODE_BEFORE_1960_BIS("18");

	private String code;

	private PeriodDetail(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public static String getEnumName(String code) {

		String name = new String();
		for (PeriodDetail period : PeriodDetail.values()) {

			if (period.getCode().toString().equals(code)) {

				name = period.toString();
			}

		}

		return name;
	}
	
}


