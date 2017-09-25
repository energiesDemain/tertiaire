package com.ed.cgdd.derby.calibrageCINT;

import java.math.BigDecimal;
import java.util.HashMap;

import com.ed.cgdd.derby.model.calcconso.ParamBesoinsNeufs;
import com.ed.cgdd.derby.model.financeObjects.CalibCI;
import com.ed.cgdd.derby.model.financeObjects.CalibCIBati;
import com.ed.cgdd.derby.model.financeObjects.CoutEnergie;

public interface CalibrageService {
	public HashMap<String, BigDecimal> calibreCI(HashMap<String, CalibCI> dataCalib, int nu, BigDecimal cintRef);

	public HashMap<String, BigDecimal> calibreCIBati(HashMap<String, CalibCIBati> dataCalib, int nu);

	/**
	 * adding energy cost and heating need
	 * @param cintMapNeuf
	 * @param coutEnergieMap
	 * @param bNeufsMap
	 */
    void addingRowsInHashMap(HashMap<String, CalibCI> cintMapNeuf, HashMap<Integer, CoutEnergie> coutEnergieMap, HashMap<String, ParamBesoinsNeufs> bNeufsMap);
}
