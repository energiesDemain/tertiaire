package com.ed.cgdd.derby.finance;

import java.util.HashMap;

import com.ed.cgdd.derby.model.financeObjects.ResFin;
import com.ed.cgdd.derby.model.financeObjects.ValeurFinancement;

public interface InsertResultFinancementDAS {
	public void insert(HashMap<ResFin, ValeurFinancement> datas);

}
