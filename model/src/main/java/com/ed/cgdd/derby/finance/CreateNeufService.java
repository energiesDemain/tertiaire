package com.ed.cgdd.derby.finance;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

import com.ed.cgdd.derby.model.calcconso.ParamBesoinsNeufs;
import com.ed.cgdd.derby.model.calcconso.ParamRdtCout;
import com.ed.cgdd.derby.model.financeObjects.*;

public interface CreateNeufService {

	HashMap<String, BigDecimal> pmChauffNeuf(HashMap<String, ParamBesoinsNeufs> bNeufsMap,
			HashMap<String, BigDecimal> dvChauffMap, HashMap<String, ParamRdtCout> rdtCoutChauffMap, String idAgreg,
			int annee, String statut_occup, int nu, HashMap<String,CalibCoutGlobal> coutIntangible,
			HashMap<Integer, CoutEnergie> coutEnergieMap, HashMap<String, Emissions> emissionsMap,
			HashMap<String, BigDecimal> evolCoutBati, HashMap<String, BigDecimal> evolCoutTechno,
			HashMap<String, TauxInteret> tauxInteretMap, HashMap<String, Maintenance> maintenanceMap);

}
