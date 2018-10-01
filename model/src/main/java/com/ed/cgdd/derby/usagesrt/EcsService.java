package com.ed.cgdd.derby.usagesrt;

import java.math.BigDecimal;
import java.util.HashMap;

import com.ed.cgdd.derby.model.calcconso.ParamBesoinsNeufs;
import com.ed.cgdd.derby.model.calcconso.ParamCoutEcs;
import com.ed.cgdd.derby.model.calcconso.ParamPMConso;
import com.ed.cgdd.derby.model.calcconso.ParamPMConsoChgtSys;
import com.ed.cgdd.derby.model.calcconso.ParamPartSolaireEcs;
import com.ed.cgdd.derby.model.calcconso.ParamPartSysPerfEcs;
import com.ed.cgdd.derby.model.calcconso.ParamRdtEcs;
import com.ed.cgdd.derby.model.calcconso.ParamRdtPerfEcs;
import com.ed.cgdd.derby.model.calcconso.ParamTauxCouvEcs;
import com.ed.cgdd.derby.model.calcconso.ResultConsoRdt;
import com.ed.cgdd.derby.model.calcconso.ResultConsoURt;
import com.ed.cgdd.derby.model.parc.EvolBesoinMap;
import com.ed.cgdd.derby.model.parc.Parc;

public interface EcsService {

	ResultConsoRdt evolEcsConso(HashMap<String, ParamCoutEcs> coutEcsMap, HashMap<String, Parc> parcTotMap,
			ResultConsoRdt resultatsConsoEcs, HashMap<String, ParamPMConso> pmEcsNeufMap,
			HashMap<String, ParamPMConsoChgtSys> pmEcsChgtMap, HashMap<String, ParamBesoinsNeufs> bNeufsMap,
			HashMap<String, ParamPartSolaireEcs> partSolaireMap, HashMap<String, ParamTauxCouvEcs> txCouvSolaireMap,
			HashMap<String, BigDecimal> dvEcsMap, HashMap<String, ParamRdtEcs> bibliRdtEcsMap,
			HashMap<String, ParamRdtPerfEcs> rdtPerfEcsMap, HashMap<String, ParamPartSysPerfEcs> partSysPerfEcsMap,
			int anneeNTab, int pasdeTemps, int annee, BigDecimal compteur, String usage,
			HashMap<String, ResultConsoURt> resultConsoURtMap, HashMap<String, BigDecimal[]> elasticiteNeufMap,
			HashMap<String, BigDecimal[]> elasticiteExistantMap, EvolBesoinMap  evolBesoinMap);

}
