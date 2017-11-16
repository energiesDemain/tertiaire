package com.ed.cgdd.derby.excelresult;

import com.ed.cgdd.derby.model.financeObjects.CoutEnergie;

import java.io.IOException;
import java.util.HashMap;

public interface CSVService {

	/**
	 * excelService appelle les méthodes : getCoutsXls()
	 * 
	 * @param pasTemps
	 */
	public void csvService(int pasTemps);


}