package com.ed.cgdd.derby.usagesrt;

import java.math.BigDecimal;
import java.util.HashMap;

import com.ed.cgdd.derby.model.calcconso.Conso;
import com.ed.cgdd.derby.model.calcconso.ParamCoutEclVentil;
import com.ed.cgdd.derby.model.calcconso.ParamCoutEcs;
import com.ed.cgdd.derby.model.calcconso.ParamGainsUsages;
import com.ed.cgdd.derby.model.calcconso.ParamPMConso;
import com.ed.cgdd.derby.model.calcconso.ParamPMConsoChgtSys;
import com.ed.cgdd.derby.model.calcconso.ParamPartSolaireEcs;
import com.ed.cgdd.derby.model.calcconso.ParamPartSysPerfEcs;
import com.ed.cgdd.derby.model.calcconso.ParamRatioAux;
import com.ed.cgdd.derby.model.calcconso.ParamRdtEcs;
import com.ed.cgdd.derby.model.calcconso.ParamRdtPerfEcs;
import com.ed.cgdd.derby.model.calcconso.ParamTauxCouvEcs;
import com.ed.cgdd.derby.model.calcconso.ResultConsoURt;

public interface LoadTableRtDAS {

	/**
	 * loadData
	 */

	HashMap<String, ParamPMConso> loadTablePMEcs(String tableName);

	HashMap<String, ParamPMConsoChgtSys> loadTablePMECSChgtSys(String tableName);

	HashMap<String, Conso> loadMapResultBesoin(String tableName, final String idAgregParc, final int pasdeTemps, 
			BigDecimal calageParc);

	HashMap<String, ParamRdtEcs> loadTableRdtEcs(String tableName);

	HashMap<String, ParamRdtPerfEcs> loadTableRdtPerfEcs(String tableName);

	HashMap<String, ParamPartSolaireEcs> loadTablePartSolaireEcs(String tableName);

	HashMap<String, ParamTauxCouvEcs> loadTableTxCouvSolaireEcs(String tableName);

	HashMap<String, ParamPartSysPerfEcs> loadTablePartSysPerfEcs(String tableName);

	HashMap<String, BigDecimal> loadTableDvEcs(String tableName);

	HashMap<String, ParamGainsUsages> loadTableGainsEclairage(String tableName);

	HashMap<String, ParamGainsUsages> loadTableGainsVentilation(String tableName);

	HashMap<String, ParamCoutEcs> loadTableCoutEcs(String tableName);

	HashMap<String, ParamCoutEclVentil> loadTableCoutEclVentil(String tableName);

	HashMap<String, ParamRatioAux> loadTableRatioAuxClim(String tableName);

	HashMap<String, ParamRatioAux> loadTableRatioAuxChaud(String tableName);

	HashMap<String, Conso> loadMapResultBesoinVentil(String tableName, final String idAgregParc, final int pasdeTemps, BigDecimal calageParc);

	HashMap<String, Conso> loadMapResultBesoinEclairage(String tableName, final String idAgregParc,
			final int pasdeTemps, HashMap<String, ResultConsoURt> resultConsoURtMap, BigDecimal calageParc);

}
