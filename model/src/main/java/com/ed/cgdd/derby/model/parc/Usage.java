package com.ed.cgdd.derby.model.parc;

public enum Usage {
	CHAUFFAGE("Chauffage"), CLIMATISATION("Climatisation"), BUREAUTIQUE("Bureautique"), ECS("ECS"), VENTILATION(
			"Ventilation"), AUXILIAIRES("Auxiliaires"), ECLAIRAGE("Eclairage"), PROCESS("Process"), CUISSON("Cuisson"), AUTRES(
			"Autre"), FROID_ALIMENTAIRE("Froid_alimentaire");

	private String label;

	private Usage(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

}


