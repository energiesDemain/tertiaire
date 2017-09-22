package com.ed.cgdd.derby.calibrageCINT;

import java.util.HashMap;

import com.ed.cgdd.derby.model.financeObjects.CalibCI;
import com.ed.cgdd.derby.model.financeObjects.CalibCIBati;

public interface CalibrageDAS {
	public HashMap<String, CalibCI> recupCI();

	public HashMap<String, CalibCIBati> recupCIBati();

}
