package com.ed.cgdd.derby.model.parc;

public enum ParcType {
	PARC_TOT("ParcTot"), PARC_ENTRANT("ParcEntrant"), PARC_SORTANT("ParcSortant");

	private String label;

	private ParcType(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

}
