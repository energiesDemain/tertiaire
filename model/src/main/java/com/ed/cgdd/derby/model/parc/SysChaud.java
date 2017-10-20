package com.ed.cgdd.derby.model.parc;

public enum SysChaud {
	AUTRE_SYSTEME_CENTRALISE("01"), CASSETTE_RAYONNANTE("02"), CHAUDIERE_FIOUL("03"), CHAUDIERE_GAZ("04"), DRV("05"), ELECTRIQUE_DIRECT(
			"06"), NON_CHAUFFE("07"), NR("08"), PAC("09"), ROOFTOP("10"), TUBE_RADIANT("11"), AUTRE_SYSTEME_CENTRALISE_PERFORMANT(
			"21"), CASSETTE_RAYONNANTE_PERFORMANT("22"), CHAUDIERE_CONDENSATION_FIOUL("23"), CHAUDIERE_CONDENSATION_GAZ(
			"24"), DRV_PERFORMANT("25"), ELECTRIQUE_DIRECT_PERFORMANT("26"), PAC_PERFORMANT("29"), ROOFTOP_PERFORMANT(
			"30"), TUBE_RADIANT_PERFORMANT("31");

	private String code;

	private SysChaud(String code) {
		this.code = code;
	}

	public String Sys() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}	
	
	public String getCode() {
		return code;
	}
	
	public static String getEnumName(String code) {

		String name = new String();
		for (SysChaud Sys : SysChaud.values()) {

			if (Sys.getCode().toString().equals(code)) {

				name = Sys.toString();
			}

		}

		return name;
	}
}
