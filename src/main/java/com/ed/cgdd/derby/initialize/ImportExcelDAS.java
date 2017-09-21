package com.ed.cgdd.derby.initialize;

import java.io.IOException;

import com.ed.cgdd.derby.model.excelobjects.ExcelParameters;
import com.ed.cgdd.derby.model.excelobjects.GenericExcelData;

public interface ImportExcelDAS {

	/**
	 * import d'Excel
	 * 
	 * @throws IOException
	 */

	GenericExcelData importExcel(ExcelParameters param) throws IOException;

}
