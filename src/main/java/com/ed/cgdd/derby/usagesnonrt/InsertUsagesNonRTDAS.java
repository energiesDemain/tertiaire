package com.ed.cgdd.derby.usagesnonrt;

import java.util.HashMap;

import com.ed.cgdd.derby.model.parc.Parc;

public interface InsertUsagesNonRTDAS {

	/**
	 * insertParc
	 */

	void insert(String usage, String name, HashMap<String, Parc> usageMap, int pasdeTemps, int annee);

}
