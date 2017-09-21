package com.ed.cgdd.derby.finance;

import java.math.BigDecimal;
import java.util.HashMap;

import com.ed.cgdd.derby.model.calcconso.Conso;
import com.ed.cgdd.derby.model.financeObjects.CEE;
import com.ed.cgdd.derby.model.financeObjects.CoutRenovation;
import com.ed.cgdd.derby.model.financeObjects.Financement;
import com.ed.cgdd.derby.model.financeObjects.Geste;
import com.ed.cgdd.derby.model.financeObjects.GesteFinancement;
import com.ed.cgdd.derby.model.financeObjects.PBC;
import com.ed.cgdd.derby.model.parc.Parc;

public interface TypeFinanceService {

	CoutRenovation recupParamSegment(Parc parcIni, Conso consoEner, Geste geste, int anneeNtab, int annee,
			BigDecimal surface, BigDecimal coutEnergie);

	GesteFinancement createRienFaire(Parc parcIni, Conso consoEner, Geste courant, int anneeNtab, PBC pretDeBase,
			BigDecimal surface, BigDecimal coutenergie);

	GesteFinancement createFinancement(Parc parcIni, Conso consoEner, Geste geste, Financement financement,
			int anneeNtab, int annee, PBC pretDeBase, CEE valeurCEE, BigDecimal surface,
			HashMap<String, BigDecimal> coutIntangible, HashMap<String, BigDecimal> coutIntangibleBati,
			BigDecimal coutEnergie, HashMap<String, BigDecimal> evolCoutBati, HashMap<String, BigDecimal> evolCoutTechno);

	CoutRenovation recupParamSegment(Parc parcIni, Conso consoEner, Geste geste, int anneeNtab, int annee,
			BigDecimal surface, HashMap<String, BigDecimal> coutIntangible,
			HashMap<String, BigDecimal> coutIntangibleBati, BigDecimal coutEnergie,
			HashMap<String, BigDecimal> evolCoutBati, HashMap<String, BigDecimal> evolCoutTechno);

}
