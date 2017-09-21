package com.ed.cgdd.derby.model.parc;

public enum Branche {
	BUREAUX_ADMINISTRATION("01"), CAFE_HOSTEL_RESTAURANT("02"), COMMERCE("03"), ENSEIGNEMENT_RECHERCHE("04"), HABITAT_COMMUNAUTAIRE(
			"05"), SANTE_ACTION_SOCIALE("06"), SPORT_LOISIR_CULTURE("07"), TRANSPORT("08");

	private String code;

	private Branche(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

}
