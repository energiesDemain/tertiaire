package com.ed.cgdd.derby.model.calcconso;

import java.util.HashMap;
import java.util.Set;

import com.ed.cgdd.derby.model.parc.Parc;

public class ResultConso {

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
