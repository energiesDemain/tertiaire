package com.ed.cgdd.derby.model.parc;

public enum MapResultsKeys {
	BESOIN_CHAUFF("Besoins_chauff"), CONSO_CHAUFF("ConsoEF_chauff"), RDT_CHAUFF("Rendements_chauff"), BESOIN_CLIM(
			"Besoins_clim"), CONSO_CLIM("ConsoEF_clim"), RDT_CLIM("Rendements_Clim"), BESOIN_ECS("Besoins"), CONSO_ECS(
			"ConsoEF"), RDT_ECS("Rendements"), PARC_ENTRANT("ParcEntrant"), PARC_SORTANT("ParcSortant"), PARC_TOT(
			"ParcTot"), VENTILATION("Ventilation"), AUXILIAIRES("Auxiliaires"), ECLAIRAGE("Eclairage"), COUT_ECS(
			"Couts"), COUT_CLIM("Cout_Clim"), COUT_ECLAIRAGE("Cout_Eclairage");

	private String label;

	private MapResultsKeys(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

}
