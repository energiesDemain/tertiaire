package com.ed.cgdd.derby.calibrageCINT;

import java.util.HashMap;

import com.ed.cgdd.derby.model.calcconso.ParamBesoinsNeufs;
import com.ed.cgdd.derby.model.financeObjects.CalibCI;
import com.ed.cgdd.derby.model.financeObjects.CalibCIBati;
import com.ed.cgdd.derby.model.financeObjects.CalibCoutGlobal;
import com.ed.cgdd.derby.model.financeObjects.CoutEnergie;
import com.ed.cgdd.derby.model.financeObjects.Maintenance;
import com.ed.cgdd.derby.model.parc.ParamCInt;

public interface CalibrageService {

	/**
	 * Calculate CInt and return all cost parameters
	 * @param dataCalib
	 * @param paramCint
	 * @return
	 */
	HashMap<String, CalibCoutGlobal> calibreCI(HashMap<String, CalibCI> dataCalib, ParamCInt paramCint, HashMap<String, Maintenance> maintenanceMap);

	/**
	 * Calculate CInt and return all cost parameters
	 * @param dataCalib
	 * @param paramCint
	 * @return
	 */
	HashMap<String, CalibCoutGlobal> calibreCIBati(HashMap<String, CalibCIBati> dataCalib, ParamCInt paramCint);

	/**
	 * adding energy cost and heating need
	 * @param cintMapNeuf
	 * @param coutEnergieMap
	 * @param bNeufsMap
	 */
    void addingRowsInHashMap(HashMap<String, CalibCI> cintMapNeuf, HashMap<Integer, CoutEnergie> coutEnergieMap, HashMap<String, ParamBesoinsNeufs> bNeufsMap);
}
