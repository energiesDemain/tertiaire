package com.ed.cgdd.derby.excelresult;

public interface ExcelXResultService {

	/**
	 * Appelle les méthodes : getParcAnneeXls(pasTemps),
	 * getConsoAnneeXls(pasTemps), getGESAnneeXls(pasTemps),
	 * setImportSheetHidden(isHidden)
	 * 
	 * @param pasTemps
	 * @param isHidden
	 */
	public void excelXService(int pasTemps, boolean isHidden);

}