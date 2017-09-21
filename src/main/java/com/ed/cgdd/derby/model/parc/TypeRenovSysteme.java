package com.ed.cgdd.derby.model.parc;

public enum TypeRenovSysteme {
	CHGT_SYS("Chgt systeme"), ETAT_INIT("Etat initial");

	private String label;

	private TypeRenovSysteme(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

}
