package com.ed.cgdd.derby.process;

import java.io.IOException;
import java.sql.SQLException;

import com.ed.cgdd.derby.model.parc.ParamCintObjects;
import com.ed.cgdd.derby.model.progression.Progression;

public interface ProcessService {

	void process(Progression progression, ParamCintObjects paramCint) throws IOException, SQLException;

	// int loadPasTemps() throws IOException;

}
