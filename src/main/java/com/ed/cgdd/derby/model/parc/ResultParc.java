package com.ed.cgdd.derby.model.parc;

import java.util.HashMap;
import java.util.Set;

public class ResultParc {
	private HashMap<String, HashMap<String, Parc>> map = new HashMap<String, HashMap<String, Parc>>();

	public Set<String> keySet() {
		return map.keySet();
	}

	public HashMap<String, Parc> getMap(String key) {
		return map.get(key);
	}

	public void put(String key, HashMap<String, Parc> value) {
		this.map.put(key, value);
	}
}
