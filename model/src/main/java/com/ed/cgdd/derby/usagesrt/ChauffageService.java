package com.ed.cgdd.derby.usagesrt;

import java.math.BigDecimal;
import java.util.HashMap;

import com.ed.cgdd.derby.model.calcconso.EffetRebond;
import com.ed.cgdd.derby.model.calcconso.ParamBesoinsNeufs;
import com.ed.cgdd.derby.model.calcconso.ParamGainsUsages;
import com.ed.cgdd.derby.model.calcconso.ParamRatioAux;
import com.ed.cgdd.derby.model.calcconso.ParamRdtCout;
import com.ed.cgdd.derby.model.calcconso.ResultConsoRt;
import com.ed.cgdd.derby.model.financeObjects.PartMarcheRenov;
import com.ed.cgdd.derby.model.financeObjects.ResFin;
import com.ed.cgdd.derby.model.financeObjects.ValeurFinancement;
import com.ed.cgdd.derby.model.parc.EvolBesoinMap;
import com.ed.cgdd.derby.model.parc.Parc;

public interface ChauffageService {

	ResultConsoRt evolChauffageConso(HashMap<ResFin, ValeurFinancement> resultFinance, String idAgregParc,
			HashMap<String, ParamRatioAux> auxChaud, HashMap<String, Parc> parcTotMap,
			HashMap<String, PartMarcheRenov> partMarcheMap, HashMap<String, ParamBesoinsNeufs> bNeufsMap,
			HashMap<String, ParamRdtCout> rdtCoutChauffMap, int anneeNTab, int pasdeTemps, int annee,
			ResultConsoRt resultatsConsoRt, HashMap<String, ParamGainsUsages> gainsVentilationMap,
			HashMap<String, EffetRebond> effetRebond, HashMap<String, BigDecimal[]> elasticiteNeufMap,
			HashMap<String, BigDecimal[]> elasticiteExistantMap, EvolBesoinMap evolBesoinMap);
}
