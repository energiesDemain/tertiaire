package com.ed.cgdd.derby.initialize;

import com.ed.cgdd.derby.model.excelobjects.ExcelParameters;
import com.ed.cgdd.derby.model.excelobjects.GenericExcelData;

public interface InsertDAS {

	/**
	 * insert table
	 * 
	 * @return
	 */

	void insert(GenericExcelData excelData, String name, ExcelParameters param);

}
