package com.ed.cgdd.derby.usagesrt;

import java.util.HashMap;

import com.ed.cgdd.derby.model.calcconso.Conso;

public interface InsertUsagesRTDAS {

	/**
	 * insertParc
	 */

	void insert(String usage, String name, HashMap<String, Conso> usageMap, int pasdeTemps, int annee);

	void insertTest(String usage, String name, HashMap<String, Conso> usageMap, int pasdeTemps, int annee);

}
