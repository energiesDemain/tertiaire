package com.ed.cgdd.derby.model.financeObjects;

public enum GesteObligation {
	ENSBBC("6"), FEN_MURBBC("5"), FENBBC("4"), ENSMOD("3"), FEN_MURMOD("2"), FENMOD("1"), GTB("7"), TOUS("8");
	;

	private String code;

	private GesteObligation(String code) {
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
		for (GesteObligation geste : GesteObligation.values()) {

			if (geste.getCode().toString().equals(code)) {

				name = geste.toString();
			}

		}

		return name;
	}

}
