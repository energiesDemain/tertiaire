package com.ed.cgdd.derby.loadparam;

import java.io.IOException;

import com.ed.cgdd.derby.model.parc.ParamCintObjects;
import com.ed.cgdd.derby.model.progression.Progression;

public interface LoadParamService {

	/**
	 * Get all Excel parameters and save them into database excepts Cint parameters
	 * @param progression
	 * @return
	 * @throws IOException
	 */
	ParamCintObjects initParam(Progression progression) throws IOException;

}
