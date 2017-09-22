package com.ed.cgdd.derby.loadparam;

import java.io.IOException;

import com.ed.cgdd.derby.model.progression.Progression;

public interface LoadParamService {

	void initParam(Progression progression) throws IOException;

}
