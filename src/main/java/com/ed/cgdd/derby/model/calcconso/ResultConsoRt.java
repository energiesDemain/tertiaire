package com.ed.cgdd.derby.model.calcconso;

import java.util.HashMap;
import java.util.Set;

public class ResultConsoRt {

	private HashMap<String, HashMap<String, Conso>> map = new HashMap<String, HashMap<String, Conso>>();

	public Set<String> keySet() {
		return map.keySet();
	}

	public HashMap<String, Conso> getMap(String key) {
		return map.get(key);
	}

	public void put(String key, HashMap<String, Conso> value) {
		this.map.put(key, value);
	}

}
