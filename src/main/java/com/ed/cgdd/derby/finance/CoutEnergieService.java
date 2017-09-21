package com.ed.cgdd.derby.finance;

import java.math.BigDecimal;
import java.util.HashMap;

import com.ed.cgdd.derby.model.financeObjects.CoutEnergie;
import com.ed.cgdd.derby.model.financeObjects.Emissions;
import com.ed.cgdd.derby.model.financeObjects.Geste;

public interface CoutEnergieService {

	BigDecimal chargesEnerAnnuelles(BigDecimal surface, BigDecimal besoinEnerUnitaireIni, Geste geste,
			BigDecimal coutEnergie);

	BigDecimal coutEnergie(HashMap<Integer, CoutEnergie> coutEnergieMap, HashMap<String, Emissions> emissionsMap,
			int annee, String energie, String usage, BigDecimal tva);

}
