package com.ed.cgdd.derby.usagesnonrt;

import java.math.BigDecimal;
import java.util.HashMap;

import com.ed.cgdd.derby.model.calcconso.ParamBesoinsNeufs;
import com.ed.cgdd.derby.model.calcconso.ParamGainsUsages;
import com.ed.cgdd.derby.model.calcconso.ParamPMConso;
import com.ed.cgdd.derby.model.calcconso.ParamPMConsoChgtSys;
import com.ed.cgdd.derby.model.parc.Parc;

public interface CuissonAutreService {

	HashMap<String, Parc> evolCuissonAutre(HashMap<String, Parc> parcTotMap, HashMap<String, Parc> usageMap,
			HashMap<String, BigDecimal> dvNonRTMap, HashMap<String, ParamGainsUsages> gainsMap,
			HashMap<String, ParamBesoinsNeufs> bNeufsMap, HashMap<String, ParamPMConso> pmCuissonMap,
			HashMap<String, ParamPMConso> pmAutresMap, HashMap<String, ParamPMConsoChgtSys> pmCuissonChgtMap,
			HashMap<String, ParamPMConsoChgtSys> pmAutresChgtMap, int pasdeTemps, int anneeNTab, int annee,
			String usage, HashMap<String, BigDecimal[]> elasticiteNeufMap,
			HashMap<String, BigDecimal[]> elasticiteExistantMap);

}
