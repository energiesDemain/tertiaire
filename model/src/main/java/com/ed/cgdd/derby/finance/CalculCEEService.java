package com.ed.cgdd.derby.finance;

import java.math.BigDecimal;

import com.ed.cgdd.derby.model.financeObjects.CEE;
import com.ed.cgdd.derby.model.financeObjects.Geste;

public interface CalculCEEService {
	public BigDecimal calculCEE(BigDecimal surface, Geste geste, CEE valeur);

}
