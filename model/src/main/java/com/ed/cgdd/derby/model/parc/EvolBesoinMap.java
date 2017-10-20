package com.ed.cgdd.derby.model.parc;

import java.util.HashMap;

public class EvolBesoinMap {

	private HashMap<String, EvolutionBesoin> evolBesoin = new HashMap<String, EvolutionBesoin>();

	public HashMap<String, EvolutionBesoin> getEvolBesoin() {
		return evolBesoin;
	}

	public void putEvolutionBesoin(EvolutionBesoin value) {
		this.evolBesoin.put(value.getIdBranche() + value.getUsage() +  value.getAnnee(), value);
	}

}
