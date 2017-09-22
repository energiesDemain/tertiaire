package com.ed.cgdd.derby.model.parc;

public enum UsageRT {
	CHAUFFAGE("Chauffage"), CLIMATISATION("Climatisation"), ECS("ECS"), VENTILATION(
			"Ventilation"), AUXILIAIRES("Auxiliaires"), ECLAIRAGE("Eclairage");

	private String label;

	private UsageRT(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
}
