package com.ed.cgdd.derby.calibrageCINT;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

import com.ed.cgdd.derby.model.calcconso.ParamBesoinsNeufs;
import com.ed.cgdd.derby.model.financeObjects.CalibCI;
import com.ed.cgdd.derby.model.financeObjects.CalibCIBati;
import com.ed.cgdd.derby.model.financeObjects.CalibCoutGlobal;
import com.ed.cgdd.derby.model.financeObjects.CoutEnergie;
import com.ed.cgdd.derby.model.parc.ParamCInt;

public interface CalibrageService {

	/**
	 * Calculate CInt and return all cost parameters
	 * @param dataCalib
	 * @param paramCint
	 * @return
	 */
	List<CalibCoutGlobal> calibreCI(HashMap<String, CalibCI> dataCalib, ParamCInt paramCint);

	/**
	 * Calculate CInt and return all cost parameters
	 * @param dataCalib
	 * @param paramCint
	 * @return
	 */
	List<CalibCoutGlobal> calibreCIBati(HashMap<String, CalibCIBati> dataCalib, ParamCInt paramCint);

	/**
	 * adding energy cost and heating need
	 * @param cintMapNeuf
	 * @param coutEnergieMap
	 * @param bNeufsMap
	 */
    void addingRowsInHashMap(HashMap<String, CalibCI> cintMapNeuf, HashMap<Integer, CoutEnergie> coutEnergieMap, HashMap<String, ParamBesoinsNeufs> bNeufsMap);
}
