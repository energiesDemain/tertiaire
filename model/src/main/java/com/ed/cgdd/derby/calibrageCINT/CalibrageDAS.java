package com.ed.cgdd.derby.calibrageCINT;

import java.util.HashMap;
import com.ed.cgdd.derby.model.financeObjects.CalibCI;
import com.ed.cgdd.derby.model.financeObjects.CalibCIBati;
import com.ed.cgdd.derby.model.financeObjects.CalibCoutGlobal;
import com.ed.cgdd.derby.model.parc.CIntType;

public interface CalibrageDAS {
	public HashMap<String, CalibCI> recupCI();

	public HashMap<String, CalibCI> recupCINeuf();

	public HashMap<String, CalibCIBati> recupCIBati();

	/**
	 * insert CInt into database
	 * @param coutIntangibleMap
	 * @param cIntType
	 */
	void insertCInt(HashMap<String,CalibCoutGlobal> coutIntangibleMap, CIntType cIntType);
	void insertCIntDesag(HashMap<String,CalibCoutGlobal> coutIntangibleMap, CIntType cIntType);
}
