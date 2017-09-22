package com.ed.cgdd.derby.model.parc;

public enum Energies {
	AUTRES("01"), ELECTRICITE("02"), FIOUL("03"), GAZ("04"), NON_CHAUFFE("05"), URBAIN("06");

	private String code;

	private Energies(String code) {
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
		for (Energies energies : Energies.values()) {

			if (energies.getCode().toString().equals(code)) {

				name = energies.toString();
			}

		}

		return name;
	}

}
