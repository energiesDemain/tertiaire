package com.ed.cgdd.derby.excelresult;

import java.io.IOException;
import java.util.HashMap;

import com.ed.cgdd.derby.model.financeObjects.CoutEnergie;

public interface ExcelXCoutsService {

	/**
	 * excelService appelle les méthodes : getCoutsXls(),
	 * setImportSheetHidden(isHidden), et updateXls().
	 * 
	 * @param pasTemps
	 * @param isHidden
	 */
	public void excelXService(int pasTemps, boolean isHidden);

	/**
	 * Integre la contribution climat energie dans table_cout.xls
	 * 
	 * @param coutEnergieMap
	 * @return
	 * @throws IOException
	 */
	public void getContributionClimat(HashMap<Integer, CoutEnergie> coutEnergieMap) throws IOException;

}