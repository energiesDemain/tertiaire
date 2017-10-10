package com.ed.cgdd.derby.finance;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

import com.ed.cgdd.derby.model.calcconso.Conso;
import com.ed.cgdd.derby.model.financeObjects.*;
import com.ed.cgdd.derby.model.parc.Parc;

public interface TypeFinanceService {

	CoutRenovation recupParamSegment(Parc parcIni, Conso consoEner, Geste geste, int anneeNtab, int annee,
			BigDecimal surface, BigDecimal coutEnergie);

	GesteFinancement createRienFaire(Parc parcIni, Conso consoEner, Geste courant, int anneeNtab, PBC pretDeBase,
			BigDecimal surface, BigDecimal coutenergie);

	GesteFinancement createFinancement(Parc parcIni, Conso consoEner, Geste geste, Financement financement,
			int anneeNtab, int annee, PBC pretDeBase, CEE valeurCEE, BigDecimal surface,HashMap<String,CalibCoutGlobal> coutIntangible, HashMap<String,CalibCoutGlobal> coutIntangibleBati,
			BigDecimal coutEnergie, HashMap<String, BigDecimal> evolCoutBati, HashMap<String, BigDecimal> evolCoutTechno);

	CoutRenovation recupParamSegment(Parc parcIni, Conso consoEner, Geste geste, int anneeNtab, int annee,
			BigDecimal surface, HashMap<String,CalibCoutGlobal> coutIntangible,HashMap<String,CalibCoutGlobal> coutIntangibleBati, BigDecimal coutEnergie,
			HashMap<String, BigDecimal> evolCoutBati, HashMap<String, BigDecimal> evolCoutTechno);

}
