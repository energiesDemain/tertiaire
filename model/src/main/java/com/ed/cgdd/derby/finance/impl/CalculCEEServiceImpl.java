package com.ed.cgdd.derby.finance.impl;

import java.math.BigDecimal;

import com.ed.cgdd.derby.finance.CalculCEEService;
import com.ed.cgdd.derby.model.financeObjects.CEE;
import com.ed.cgdd.derby.model.financeObjects.Geste;

public class CalculCEEServiceImpl implements CalculCEEService {
	public BigDecimal calculCEE(BigDecimal surface, Geste geste, CEE valeur) {

		BigDecimal aides = valeur.getPrixKWhCumac().multiply(surface).multiply(geste.getValeurCEE());
		return aides;

	}

}
