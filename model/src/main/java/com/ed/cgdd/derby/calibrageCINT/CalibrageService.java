package com.ed.cgdd.derby.calibrageCINT;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

import com.ed.cgdd.derby.model.calcconso.ParamBesoinsNeufs;
import com.ed.cgdd.derby.model.financeObjects.CalibCI;
import com.ed.cgdd.derby.model.financeObjects.CalibCIBati;
import com.ed.cgdd.derby.model.financeObjects.CalibCoutGlobal;
import com.ed.cgdd.derby.model.financeObjects.CoutEnergie;
import com.ed.cgdd.derby.model.financeObjects.Geste;
import com.ed.cgdd.derby.model.financeObjects.Maintenance;
import com.ed.cgdd.derby.model.financeObjects.TauxInteret;
import com.ed.cgdd.derby.model.parc.ParamCInt;
import com.ed.cgdd.derby.model.parc.ParamCalib;

public interface CalibrageService {

	/**
	 * Calculate CInt and return all cost parameters
	 * @param dataCalib
	 * @param paramCint
	 * @return
	 */
	HashMap<String, CalibCoutGlobal> calibreCI(HashMap<String, CalibCI> dataCalib, 
			ParamCInt paramCint, HashMap<String, Maintenance> maintenanceMap);
	HashMap<String, CalibCoutGlobal> calibreCIsystZERO(HashMap<String, CalibCI> dataCalib, 
			ParamCInt paramCint, HashMap<String, Maintenance> maintenanceMap);
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
	
	HashMap<String, CalibCoutGlobal> calibreCIBatidesag(HashMap<String, CalibCIBati> dataCalib, 
			ParamCInt paramCInt, HashMap<String, ParamCalib> paramCalibMap,  
			HashMap<String, List<Geste>>  bibliGesteBatiMap, HashMap<String, TauxInteret> tauxInteretMap);
	
	HashMap<String, CalibCoutGlobal> 	calibreCIBatiNRFonly(HashMap<String, CalibCIBati> dataCalib, ParamCInt paramCint);


	HashMap<String, BigDecimal> setLambdaNRF();
	
    void addingRowsInHashMap(HashMap<String, CalibCI> cintMapNeuf, HashMap<Integer, CoutEnergie> coutEnergieMap, HashMap<String, ParamBesoinsNeufs> bNeufsMap);
}
