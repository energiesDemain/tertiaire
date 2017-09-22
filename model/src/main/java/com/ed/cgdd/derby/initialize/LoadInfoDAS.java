package com.ed.cgdd.derby.initialize;

import java.util.ArrayList;
import java.util.HashMap;

import com.ed.cgdd.derby.model.excelobjects.ExcelParameters;

public interface LoadInfoDAS {

	/**
	 * loadInfo
	 * 
	 * @return
	 */

	HashMap<String, ExcelParameters> excelTables();

	ArrayList<String> newTables();

}
