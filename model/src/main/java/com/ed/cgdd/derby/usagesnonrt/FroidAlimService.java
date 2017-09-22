package com.ed.cgdd.derby.usagesnonrt;

import java.math.BigDecimal;
import java.util.HashMap;

import com.ed.cgdd.derby.model.calcconso.ParamBesoinsNeufs;
import com.ed.cgdd.derby.model.calcconso.ParamGainsUsages;
import com.ed.cgdd.derby.model.parc.Parc;

public interface FroidAlimService {

	HashMap<String, Parc> evolFroidAlim(HashMap<String, Parc> parcTotMap, HashMap<String, Parc> usageMap,
			HashMap<String, BigDecimal> dvNonRTMap, HashMap<String, ParamGainsUsages> gainsMap,
			HashMap<String, BigDecimal> rythmeFrdRgltMap, HashMap<String, BigDecimal> gainFrdRgltMap,
			HashMap<String, ParamBesoinsNeufs> bNeufsMap, int pasdeTemps, int anneeNTab, int annee, String usage,
			HashMap<String, BigDecimal[]> elasticiteNeufMap, HashMap<String, BigDecimal[]> elasticiteExistantMap);

}
