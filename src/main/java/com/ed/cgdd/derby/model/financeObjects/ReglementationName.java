package com.ed.cgdd.derby.model.financeObjects;

public enum ReglementationName {
	OBLIG_TRAVAUX("ObligationTravaux"), DECRET("Décret"), AUCUNE("non"), OBLIG_TRAVAUX_DECRET(
			"ObligationTravaux et décret");

	private String label;

	private ReglementationName(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

}
