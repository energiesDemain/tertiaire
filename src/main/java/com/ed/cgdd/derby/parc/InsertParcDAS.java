package com.ed.cgdd.derby.parc;

import java.util.HashMap;

import com.ed.cgdd.derby.model.parc.Parc;

public interface InsertParcDAS {

	/**
	 * insertParc
	 */

	void insert(String name, HashMap<String, Parc> parcMap, int pasdeTemps, int annee);

}
