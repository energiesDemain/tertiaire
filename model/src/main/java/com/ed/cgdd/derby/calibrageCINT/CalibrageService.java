package com.ed.cgdd.derby.calibrageCINT;

import java.math.BigDecimal;
import java.util.HashMap;

import com.ed.cgdd.derby.model.financeObjects.CalibCI;
import com.ed.cgdd.derby.model.financeObjects.CalibCIBati;

public interface CalibrageService {
	public HashMap<String, BigDecimal> calibreCI(HashMap<String, CalibCI> dataCalib, int nu);

	public HashMap<String, BigDecimal> calibreCIBati(HashMap<String, CalibCIBati> dataCalib, int nu);

}
