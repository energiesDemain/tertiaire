package com.ed.cgdd.derby.finance;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ed.cgdd.derby.model.calcconso.Conso;
import com.ed.cgdd.derby.model.calcconso.ParamBesoinsNeufs;
import com.ed.cgdd.derby.model.calcconso.ParamCoutEclVentil;
import com.ed.cgdd.derby.model.calcconso.ParamCoutEcs;
import com.ed.cgdd.derby.model.calcconso.ParamGainsUsages;
import com.ed.cgdd.derby.model.calcconso.ParamPMConso;
import com.ed.cgdd.derby.model.calcconso.ParamRdtCout;
import com.ed.cgdd.derby.model.calcconso.ParamRdtEcs;
import com.ed.cgdd.derby.model.calcconso.ResultConsoUClim;
import com.ed.cgdd.derby.model.calcconso.ResultConsoURt;
import com.ed.cgdd.derby.model.financeObjects.BibliGeste;
import com.ed.cgdd.derby.model.financeObjects.CoutEnergie;
import com.ed.cgdd.derby.model.financeObjects.Geste;
import com.ed.cgdd.derby.model.financeObjects.Maintenance;
import com.ed.cgdd.derby.model.financeObjects.PBC;
import com.ed.cgdd.derby.model.financeObjects.Reglementations;
import com.ed.cgdd.derby.model.parc.Parc;
import com.ed.cgdd.derby.model.parc.TypeRenovBati;

public interface GesteService {
	BibliGeste createBibliGeste(HashMap<String, BigDecimal> dvChauffMap,
			HashMap<TypeRenovBati, BigDecimal> dvGesteBati, String idAgregParc,
			HashMap<String, ParamRdtCout> gestesSys, Map<String, List<String>> periodeMap);

	String generateIdGesteBati(Parc parc);

	HashMap<TypeRenovBati, Geste> generateBatiGesteMap(List<Geste> gestesBati);

	Map<String, List<String>> getPeriodMap();

	BigDecimal tempsRetourInvestissement(BigDecimal coutInv, BigDecimal ecoEnergie, PBC tauxActu);

	List<Geste> cleanningGeste(HashMap<Integer, CoutEnergie> coutEnergieMap,
			HashMap<String, ResultConsoUClim> resultConsoUClimMap, HashMap<String, ResultConsoURt> resultConsoURtMap,
			Parc parc, BibliGeste bibliGeste, HashMap<String, BigDecimal> dvChauffMap,
			HashMap<TypeRenovBati, BigDecimal> dvGesteMap, int annee, int periode, Conso rdt, int anneeNTab,
			Reglementations reglementations, HashMap<String, ParamCoutEclVentil> coutsEclVentilMap,
			HashMap<String, ParamCoutEcs> coutEcsMap, HashMap<String, ParamPMConso> pmEcsNeufMap, Conso consoChauff,
			Conso ventil, Conso aux, HashMap<String, ParamBesoinsNeufs> bNeufsMap, Conso besoinInit,
			HashMap<String, ParamGainsUsages> gainsVentilationMap, HashMap<String, ParamRdtEcs> bibliRdtEcsMap,
			PBC tauxActu, HashMap<String, BigDecimal> evolCoutBati, HashMap<String, BigDecimal> evolCoutTechno,
			HashMap<String, Maintenance> maintenanceMap, HashMap<String, ParamRdtCout> paramRdtCout);
}
