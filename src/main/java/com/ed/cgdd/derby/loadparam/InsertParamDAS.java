package com.ed.cgdd.derby.loadparam;

import com.ed.cgdd.derby.model.excelobjects.ExcelParameters;
import com.ed.cgdd.derby.model.excelobjects.GenericExcelData;

public interface InsertParamDAS {

	/**
	 * insert table
	 * 
	 * @return
	 */

	void insert(GenericExcelData excelData, String name, ExcelParameters param);

}
