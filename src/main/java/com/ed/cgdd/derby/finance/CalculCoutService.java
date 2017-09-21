package com.ed.cgdd.derby.finance;

import java.math.BigDecimal;
import java.util.HashMap;

import com.ed.cgdd.derby.model.financeObjects.CoutEnergie;
import com.ed.cgdd.derby.model.financeObjects.CoutFinal;
import com.ed.cgdd.derby.model.financeObjects.Emissions;
import com.ed.cgdd.derby.model.financeObjects.GesteFinancement;
import com.ed.cgdd.derby.model.financeObjects.PBC;
import com.ed.cgdd.derby.model.parc.Parc;

public interface CalculCoutService {

	BigDecimal calculCoutGlobal(CoutFinal coutFinal, PBC tauxActualisation);

	String outputName(String idParc, GesteFinancement courant, int annee, CoutFinal coutFinal);

	CoutFinal calculCoutFinal(BigDecimal surface, BigDecimal besoinInitUnitaire, Parc parcInit,
			GesteFinancement gesteFin, int annee, String idParc, int anneeNTab, PBC tauxActu,
			HashMap<Integer, CoutEnergie> coutEnergieMap, HashMap<String, Emissions> emissionsMap,
			BigDecimal valeurVerte);
}
