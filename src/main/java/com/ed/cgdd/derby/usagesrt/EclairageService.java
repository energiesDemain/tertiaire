package com.ed.cgdd.derby.usagesrt;

import java.math.BigDecimal;
import java.util.HashMap;

import com.ed.cgdd.derby.model.calcconso.ParamBesoinsNeufs;
import com.ed.cgdd.derby.model.calcconso.ParamCoutEclVentil;
import com.ed.cgdd.derby.model.calcconso.ParamGainsUsages;
import com.ed.cgdd.derby.model.calcconso.ResultConsoRt;
import com.ed.cgdd.derby.model.calcconso.ResultConsoURt;
import com.ed.cgdd.derby.model.parc.Parc;

public interface EclairageService {

	ResultConsoRt evolEclairageConso(HashMap<String, ParamCoutEclVentil> coutsEclVentilMap,
			HashMap<String, Parc> parcTotMap, ResultConsoRt resultatsConso,
			HashMap<String, ParamGainsUsages> gainsEclairageMap, HashMap<String, ParamBesoinsNeufs> bNeufsMap,
			HashMap<String, BigDecimal> dvUsagesMap, int anneeNTab, int pasdeTemps, int annee, BigDecimal compteur,
			String usage, HashMap<String, ResultConsoURt> resultConsoURtMap,
			HashMap<String, BigDecimal[]> elasticiteNeufMap, HashMap<String, BigDecimal[]> elasticiteExistantMap);

}
