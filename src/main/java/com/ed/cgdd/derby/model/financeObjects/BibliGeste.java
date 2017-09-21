package com.ed.cgdd.derby.model.financeObjects;

import java.util.HashMap;
import java.util.List;

public class BibliGeste {

	private HashMap<String, List<Geste>> bibliGesteMap;
	private HashMap<String, List<Geste>> gestesBati;

	public void setBibliGesteMap(HashMap<String, List<Geste>> bibliGesteMap) {
		this.bibliGesteMap = bibliGesteMap;
	}

	public HashMap<String, List<Geste>> getGestesBati() {
		return gestesBati;
	}

	public void setGestesBati(HashMap<String, List<Geste>> gestesBati) {
		this.gestesBati = gestesBati;
	}

	public List<Geste> getGesteBatiMap(String periodeDetail, int period) {
		return bibliGesteMap.get(periodeDetail + period);
	}

}
