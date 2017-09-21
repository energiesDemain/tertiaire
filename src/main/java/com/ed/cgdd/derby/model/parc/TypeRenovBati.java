package com.ed.cgdd.derby.model.parc;

public enum TypeRenovBati {
	ENSBBC("ENSBBC"), FEN_MURBBC("FEN_MURBBC"), FENBBC("FENBBC"), ENSMOD("ENSMOD"), FEN_MURMOD("FEN_MURMOD"), FENMOD(
			"FENMOD"), ETAT_INIT("Etat initial"), GTB("GTB");

	private String label;

	private TypeRenovBati(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public static TypeRenovBati getEnumByLabel(String label) {
		for (TypeRenovBati renov : TypeRenovBati.values()) {
			if (renov.getLabel().equals(label)) {
				return renov;
			}
		}
		return null;
	}
}
