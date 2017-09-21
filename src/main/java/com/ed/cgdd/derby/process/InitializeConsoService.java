package com.ed.cgdd.derby.process;

import java.math.BigDecimal;
import java.util.HashMap;

import com.ed.cgdd.derby.model.calcconso.Conso;
import com.ed.cgdd.derby.model.calcconso.ParamRatioAux;
import com.ed.cgdd.derby.model.calcconso.ParamRdtCout;
import com.ed.cgdd.derby.model.calcconso.ParamRdtEcs;
import com.ed.cgdd.derby.model.calcconso.ResultConsoRt;
import com.ed.cgdd.derby.model.calcconso.ResultConsoUClim;
import com.ed.cgdd.derby.model.calcconso.ResultConsoURt;

public interface InitializeConsoService {

	HashMap<String, Conso> initializeRdtEcs(HashMap<String, ParamRdtEcs> bibliRdtEcsMap,
			HashMap<String, Conso> besoinsInit, int pasdeTemps);

	HashMap<String, Conso> initializeRdtClim(HashMap<String, ParamRdtCout> rdtCoutClimMap,
			HashMap<String, Conso> besoinsInit, int pasdeTemps);

	HashMap<String, Conso> initializeConsoClim(HashMap<String, ResultConsoUClim> resultConsoUClimMap,
			HashMap<String, Conso> rdtMap, HashMap<String, Conso> besoinsInit, int pasdeTemps);

	HashMap<String, Conso> initializeRdtChauff(HashMap<String, ParamRdtCout> rdtCoutClimMap,
			HashMap<String, Conso> besoinsInit, int pasdeTemps);

	ResultConsoRt initializeAuxChaud(ResultConsoRt resultatsConsoRt, HashMap<String, ParamRatioAux> auxChaud,
			int pasdeTemps);

	void insertResultConsoUExistEcl(HashMap<String, ResultConsoURt> resultConsoURtMap, int anneeNTab,
			String idResultRtClim, BigDecimal consoEF, BigDecimal consoEP, int annee);

	HashMap<String, Conso> initializeConsoEcs(HashMap<String, Conso> rdtEcsMap, HashMap<String, Conso> besoinsInit,
			int pasdeTemps, HashMap<String, ResultConsoURt> resultConsoURtMap);

	HashMap<String, Conso> initializeConsoChauffClim(HashMap<String, Conso> rdtMap, HashMap<String, Conso> besoinsInit,
			int pasdeTemps);

	void insertResultConsoUExistECS(HashMap<String, ResultConsoURt> resultConsoURtMap, int anneeNTab,
			String idResultRtClim, BigDecimal consoEF, BigDecimal consoEP, int annee);

	void insertResultConsoUExistClim(HashMap<String, ResultConsoUClim> resultConsoUClimMap, int anneeNTab,
			String idResultRtClim, BigDecimal consoEF, BigDecimal consoEP, int annee);

}
