package com.ed.cgdd.derby.usagesnonrt;

import java.math.BigDecimal;
import java.util.HashMap;

import com.ed.cgdd.derby.model.calcconso.ParamBesoinsNeufs;
import com.ed.cgdd.derby.model.calcconso.ParamGainsUsages;
import com.ed.cgdd.derby.model.calcconso.ParamPMConso;
import com.ed.cgdd.derby.model.calcconso.ParamPMConsoChgtSys;
import com.ed.cgdd.derby.model.parc.Parc;

public interface LoadTableUsagesNonRTDAS {

	/**
	 * loadData
	 */
	HashMap<String, Parc> loadMapResultBesoin(String tableName, final String idAgreg, final int pasdeTemps, BigDecimal calageParc);

	HashMap<String, ParamGainsUsages> loadTableGainsNonRT(String tableName);

	HashMap<String, BigDecimal> loadTableDVNonRT(String tableName);

	HashMap<String, ParamPMConso> loadTablePMConso(String tableName);

	HashMap<String, ParamBesoinsNeufs> loadTableBesoinsNeufs(String tableName);

	HashMap<String, ParamPMConsoChgtSys> loadTablePMConsoChgtSys(String tableName);

	HashMap<String, BigDecimal> loadTableRythmeFroidRglt(String tableName);

	HashMap<String, BigDecimal> loadTableGainFroidRglt(String tableName);

}
