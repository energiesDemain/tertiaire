package com.ed.cgdd.derby.model.financeObjects;

import java.util.HashMap;

public class Reglementations {

	private HashMap<String, ObligationTravauxExig> obligExig = new HashMap<String, ObligationTravauxExig>();

	private HashMap<String, ObligationTravauxSurf> obligSurf = new HashMap<String, ObligationTravauxSurf>();

	private HashMap<String, RtExistant> rtExistant = new HashMap<String, RtExistant>();

	private HashMap<String, DecretTravaux> decret = new HashMap<String, DecretTravaux>();

	public HashMap<String, DecretTravaux> getDecret() {
		return decret;
	}

	public void putDecret(DecretTravaux value) {
		this.decret.put(value.getBranche() + value.getSecteur(), value);
	}

	public HashMap<String, RtExistant> getRt() {
		return rtExistant;
	}

	public void putRt(RtExistant value) {
		this.rtExistant.put(value.getPeriode(), value);
	}

	public HashMap<String, ObligationTravauxSurf> getOblSurf() {
		return obligSurf;
	}

	public void putOblSurf(ObligationTravauxSurf value) {
		this.obligSurf.put(value.getSecteur(), value);
	}

	public HashMap<String, ObligationTravauxExig> getOblExig() {
		return obligExig;
	}

	public void putOblExig(ObligationTravauxExig value) {
		this.obligExig.put(value.getSecteur(), value);
	}
}
