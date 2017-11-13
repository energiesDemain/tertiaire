package com.ed.cgdd.derby.usagesrt;

import java.math.BigDecimal;
import java.util.HashMap;

import com.ed.cgdd.derby.model.calcconso.Conso;
import com.ed.cgdd.derby.model.calcconso.ParamRdtCout;
import com.ed.cgdd.derby.model.calcconso.ParamTxClimExistant;
import com.ed.cgdd.derby.model.calcconso.ParamTxClimNeuf;
import com.ed.cgdd.derby.model.parc.TypeRenovBati;
import com.ed.cgdd.derby.parc.ParamCalageEner;

public interface LoadTableChauffClimDAS {

	/**
	 * loadData
	 */

	HashMap<String, Conso> loadMapResultBesoin(String tableName, final String idAgregParc, final int pasdeTemps,  
			BigDecimal calageParc, HashMap<String, ParamCalageEner> calageEner);

	HashMap<String, Conso> loadMapResultBesoinChauff(String tableName, final String idAgregParc, 
			final int pasdeTemps,  BigDecimal calageParc,HashMap<String, ParamCalageEner> calageEner);

	HashMap<String, ParamRdtCout> loadTableRdtCout(String tableName);

	HashMap<String, BigDecimal> loadTableDvClim(String tableName);

	HashMap<String, ParamTxClimExistant> loadTableTxClimExistant(String tableName);

	HashMap<String, ParamTxClimNeuf> loadTableTxClimNeuf(String tableName);

	HashMap<String, BigDecimal> loadTableDvChauff(String tableName);

	HashMap<TypeRenovBati, BigDecimal> loadTableDvGeste(String tableName);

	HashMap<String, ParamRdtCout> loadTableRdtCoutChauf(String tableName);

}
