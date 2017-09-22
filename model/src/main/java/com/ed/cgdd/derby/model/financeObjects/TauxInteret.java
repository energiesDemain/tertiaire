package com.ed.cgdd.derby.model.financeObjects;

public class TauxInteret {
	private String IdBranche;
	private String IdOccupant;
	private PBC PBC;
	private String StatutOccup;

	public String getIdBranche() {
		return IdBranche;
	}

	public void setIdBranche(String idBranche) {
		IdBranche = idBranche;
	}

	public String getIdOccupant() {
		return IdOccupant;
	}

	public void setIdOccupant(String idOccupant) {
		IdOccupant = idOccupant;
	}

	public PBC getPBC() {
		return PBC;
	}

	public void setPBC(PBC pBC) {
		PBC = pBC;
	}

	public String getStatutOccup() {
		return StatutOccup;
	}

	public void setStatutOccup(String statutOccup) {
		StatutOccup = statutOccup;
	}

}
