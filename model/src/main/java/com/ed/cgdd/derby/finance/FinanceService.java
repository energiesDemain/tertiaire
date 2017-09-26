package com.ed.cgdd.derby.finance;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

import com.ed.cgdd.derby.model.calcconso.ParamBesoinsNeufs;
import com.ed.cgdd.derby.model.calcconso.ParamCoutEclVentil;
import com.ed.cgdd.derby.model.calcconso.ParamCoutEcs;
import com.ed.cgdd.derby.model.calcconso.ParamGainsUsages;
import com.ed.cgdd.derby.model.calcconso.ParamPMConso;
import com.ed.cgdd.derby.model.calcconso.ParamRdtCout;
import com.ed.cgdd.derby.model.calcconso.ParamRdtEcs;
import com.ed.cgdd.derby.model.calcconso.ResultConsoRt;
import com.ed.cgdd.derby.model.calcconso.ResultConsoUClim;
import com.ed.cgdd.derby.model.calcconso.ResultConsoURt;
import com.ed.cgdd.derby.model.financeObjects.*;
import com.ed.cgdd.derby.model.parc.ParamCintObjects;
import com.ed.cgdd.derby.model.parc.Parc;
import com.ed.cgdd.derby.model.parc.TypeRenovBati;

public interface FinanceService {

	List<CEE> cleanListeFinancement(List<Financement> listeFin);

	void extractionResult(HashMap<ResFin, ValeurFinancement> resultFinance, String idAgregParc, PartMarcheRenov temp,
			HashMap<String, Parc> parcTotMap, int anneeNTab, int annee);

	void agregateResFinancement(HashMap<ResFin, ValeurFinancement> resultatsIni, ResultatsFinancements result,
			String idAgregParc);

	HashMap<String, PartMarcheRenov> renovationSegmentGlobal(HashMap<String, BigDecimal> decretMemory,
		 HashMap<String, ResultConsoUClim> resultConsoUClimMap, HashMap<String, ResultConsoURt> resultConsoURtMap,
		 List<Financement> listFin, CEE subCEE, HashMap<String, BigDecimal> dvChauffMap,
		 HashMap<TypeRenovBati, BigDecimal> dvGesteMap, HashMap<String, ParamRdtCout> paramRdtCout,
		 HashMap<String, Parc> parcTotMap, ResultConsoRt resultConsoRt, int annee, int anneeNTab, List<CalibCoutGlobal> coutIntangible, List<CalibCoutGlobal> coutIntangibleBati, ParamCintObjects paramCintObject,
		 float txRenovBati, String idAggreg, BibliGeste bibliGeste, HashMap<Integer, CoutEnergie> coutEnergieMap,
		 HashMap<String, Emissions> emissionsMap, Reglementations reglementations, BigDecimal compteur,
		 HashMap<String, ParamCoutEclVentil> coutsEclVentilMap, HashMap<String, ParamCoutEcs> coutEcsMap,
		 HashMap<String, ParamPMConso> pmEcsNeufMap, HashMap<String, ParamBesoinsNeufs> bNeufsMap,
		 HashMap<String, ParamGainsUsages> gainsVentilationMap, HashMap<String, ParamRdtEcs> bibliRdtEcsMap,
		 HashMap<String, BigDecimal> evolCoutBati, HashMap<String, BigDecimal> evolCoutTechno,
		 HashMap<String, TauxInteret> tauxInteretMap, HashMap<String, SurfMoy> surfMoyMap,
		 HashMap<String, EvolValeurVerte> evolVVMap, HashMap<String, RepartStatutOccup> repartStatutOccupMap,
		 HashMap<String, Maintenance> maintenanceMap);

	// public HashMap<String, PartMarcheRenov>
	// traitementReglementation(HashMap<String, PartMarcheRenov> partMarcheIni);
}
