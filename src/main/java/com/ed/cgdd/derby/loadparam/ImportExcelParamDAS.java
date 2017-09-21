package com.ed.cgdd.derby.loadparam;

import java.io.IOException;

import com.ed.cgdd.derby.model.excelobjects.ExcelParameters;
import com.ed.cgdd.derby.model.excelobjects.GenericExcelData;

public interface ImportExcelParamDAS {

	/**
	 * import d'Excel
	 * 
	 * @throws IOException
	 */

	GenericExcelData importExcel(ExcelParameters param, String name) throws IOException;

}
