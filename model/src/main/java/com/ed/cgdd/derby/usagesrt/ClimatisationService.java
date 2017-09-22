package com.ed.cgdd.derby.usagesrt;

import java.math.BigDecimal;
import java.util.HashMap;

import com.ed.cgdd.derby.model.calcconso.ParamBesoinsNeufs;
import com.ed.cgdd.derby.model.calcconso.ParamRatioAux;
import com.ed.cgdd.derby.model.calcconso.ParamRdtCout;
import com.ed.cgdd.derby.model.calcconso.ResultConsoRt;
import com.ed.cgdd.derby.model.calcconso.ResultConsoUClim;
import com.ed.cgdd.derby.model.parc.Parc;

public interface ClimatisationService {

	ResultConsoRt evolClimatisationConso(HashMap<String, ResultConsoUClim> resultConsoUClimMap,
			HashMap<String, ParamRatioAux> auxFroid, HashMap<String, Parc> parcTotMap, ResultConsoRt resultatsConsoRt,
			HashMap<String, ParamBesoinsNeufs> bNeufsMap, HashMap<String, BigDecimal> dvUsagesMap,
			HashMap<String, ParamRdtCout> rdtCoutClimMap, int anneeNTab, int pasdeTemps, int annee,
			BigDecimal compteur, String usage, HashMap<String, BigDecimal[]> elasticiteNeufMap,
			HashMap<String, BigDecimal[]> elasticiteExistantMap);

}
