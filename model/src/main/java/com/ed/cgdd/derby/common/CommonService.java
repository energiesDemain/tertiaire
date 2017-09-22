package com.ed.cgdd.derby.common;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

import com.ed.cgdd.derby.model.calcconso.Conso;
import com.ed.cgdd.derby.model.calcconso.ResultConsoRdt;
import com.ed.cgdd.derby.model.calcconso.ResultConsoRt;
import com.ed.cgdd.derby.model.calcconso.ResultConsoUClim;
import com.ed.cgdd.derby.model.calcconso.ResultConsoURt;
import com.ed.cgdd.derby.model.financeObjects.CoutEnergie;
import com.ed.cgdd.derby.model.financeObjects.ElasticiteMap;
import com.ed.cgdd.derby.model.financeObjects.Emissions;
import com.ed.cgdd.derby.model.parc.Parc;
import com.ed.cgdd.derby.model.parc.ResultParc;

public interface CommonService {

	String codeCreateEnerg(String energPmKey);

	int correspPeriode(int annee);

	String concatID(Parc parcAgreg, String usage);

	int correspPeriodeCstr(Parc parcExistant, int annee);

	HashMap<String, Parc> aggregateParc(HashMap<String, Parc> parcTotMap, int anneeNTab);

	String correspPeriodeString(int annee);

	String correspPeriodeFin(int annee);

	HashMap<String, Conso> aggregateConsoEcs(HashMap<String, Conso> parcTotMap, int anneeNTab);

	HashMap<String, List<String>> idAgregList(HashMap<String, Conso> besoinAgreg, HashMap<String, Parc> parcAgreg);

	HashMap<String, List<String>> idAgregBoucleList(List<String> listeId);

	ResultConsoRdt agregateResultECS(final ResultConsoRdt resultatsConso, int pasdeTemps, String usage);

	String generateIdMapResultRt(Conso besoin);

	BigDecimal serieGeometrique(BigDecimal multiplicatif, BigDecimal facteur, int duree);

	ResultConsoRt agregateResultRt(final ResultConsoRt resultatsConso, int pasdeTemps);

	HashMap<String, Parc> aggregateParcRehab(HashMap<String, Parc> parcTotMap, int anneeNTab);

	ResultParc agregateResultParc(final ResultParc resultatsParc, int pasdeTemps);

	ResultConsoRt agregateResultRtClim(final ResultConsoRt resultatsConsoRt, int pasdeTemps, String usage);

	HashMap<String, Parc> aggregateParcConsoU(HashMap<String, ResultConsoUClim> resultConsoUClimMap,
			HashMap<String, Parc> parcTotMap, int anneeNTab, int pasdeTemps);

	HashMap<String, Parc> aggregateParcEcs(HashMap<String, Parc> parcTotMap, int anneeNTab,
			HashMap<String, ResultConsoURt> resultConsoURtMap, int pasdeTemps);

	HashMap<String, Parc> aggregateParcEclairage(HashMap<String, Parc> parcTotMap, int anneeNTab);

	HashMap<String, Conso> aggregateBesoinClim(HashMap<String, Conso> besoinMap, int anneeNTab, int pasdeTemps);

	HashMap<String, Conso> aggregateBesoinEclairage(HashMap<String, Conso> besoinTotMap, int anneeNTab);

	HashMap<String, BigDecimal[]> getFacteurElasticiteExistant(String idAgreg,
			HashMap<Integer, CoutEnergie> coutEnergieMap, HashMap<String, Emissions> emissionsMap,
			ElasticiteMap elasticiteMap);

	HashMap<String, BigDecimal[]> getFacteurElasticiteNeuf(String idAgreg,
			HashMap<Integer, CoutEnergie> coutEnergieMap, HashMap<String, Emissions> emissionsMap,
			ElasticiteMap elasticiteMap);

}
