package com.ed.cgdd.derby.loadparam;

import java.io.IOException;
import java.util.HashMap;

import com.ed.cgdd.derby.model.excelobjects.ExcelParameters;

public interface LoadInfoParamDAS {

	/**
	 * parameters
	 * 
	 * @return
	 */
	HashMap<String, ExcelParameters> parameters() throws IOException;

}
