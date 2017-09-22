package com.ed.cgdd.derby.excelresult;

public interface ExcelResultService {

	/**
	 * Appelle les méthodes : getParcAnneeXls(pasTemps),
	 * getConsoAnneeXls(pasTemps), getGESAnneeXls(pasTemps),
	 * setImportSheetHidden(isHidden)
	 * 
	 * @param pasTemps
	 * @param isHidden
	 */
	public void excelService(int pasTemps, boolean isHidden);

}
