package com.ed.cgdd.derby.usagesnonrt.impl;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ed.cgdd.derby.common.CommonService;
import com.ed.cgdd.derby.model.calcconso.ParamBesoinsNeufs;
import com.ed.cgdd.derby.model.calcconso.ParamGainsUsages;
import com.ed.cgdd.derby.model.parc.Energies;
import com.ed.cgdd.derby.model.parc.Parc;
import com.ed.cgdd.derby.model.parc.Period;
import com.ed.cgdd.derby.model.parc.TypeRenovBati;
import com.ed.cgdd.derby.usagesnonrt.BureauProcessService;

public class BureauProcessServiceImpl implements BureauProcessService {
	private final static Logger LOG = LogManager.getLogger(BureauProcessServiceImpl.class);
	private final static String INIT_STATE = "Etat initial";

	public CommonService getCommonService() {
		return commonService;
	}

	public void setCommonService(CommonService commonService) {
		this.commonService = commonService;
	}

	private CommonService commonService;

	// TODO Rempli une table de resultats
	// TODO froid alimentaire, autres, cuisson
	// Calcul les evolutions des besoins (=Conso) des usages Bureautique et
	// Process
	@Override
	public HashMap<String, Parc> evolBureauProcess(HashMap<String, Parc> parcTotMap, HashMap<String, Parc> usageMap,
			HashMap<String, BigDecimal> dvNonRTMap, HashMap<String, ParamGainsUsages> gainsMap,
			HashMap<String, ParamBesoinsNeufs> bNeufsMap, int pasdeTemps, int anneeNTab, int annee, String usage,
			HashMap<String, BigDecimal[]> elasticiteNeufMap, HashMap<String, BigDecimal[]> elasticiteExistantMap) {

		// Agregation du parc sans le code "energie"
		HashMap<String, Parc> parcTotAgreg = commonService.aggregateParc(parcTotMap, anneeNTab);
		// Actualise les besoins/consommations du parc de batiments existant
		// l'annee N

		for (String idAgreg : parcTotAgreg.keySet()) {

			Parc parcAgreg = parcTotAgreg.get(idAgreg);
			int periode = commonService.correspPeriode(annee);
			if (parcAgreg.getAnnee(0) == null || parcAgreg.getAnnee(0).compareTo(BigDecimal.ZERO) == 0) {
				// si une des deux conditions est realisee alors il s'agit d'un
				// segment entrant dans le parc
				usageMap.put(
						idAgreg + Energies.ELECTRICITE.getCode(),
						besoinNeufBurProcess(parcAgreg, usageMap, bNeufsMap, usage, periode, pasdeTemps, anneeNTab,
								idAgreg, elasticiteNeufMap, annee));
			} else {
				// sinon il s'agit d'un segment existant a l'annee n
				if (usageMap.containsKey(idAgreg + Energies.ELECTRICITE.getCode())) {
					usageMap.put(
							idAgreg + Energies.ELECTRICITE.getCode(),
							besoinExistantBurProcess(usageMap, bNeufsMap, parcAgreg, dvNonRTMap, gainsMap, pasdeTemps,
									anneeNTab, annee, usage, idAgreg, elasticiteExistantMap, elasticiteNeufMap));
				}
			}

		}

		return usageMap;

	}

	protected Parc besoinExistantBurProcess(HashMap<String, Parc> usageMap,
			HashMap<String, ParamBesoinsNeufs> bNeufsMap, Parc parcAgregExistant,
			HashMap<String, BigDecimal> dvNonRTMap, HashMap<String, ParamGainsUsages> gainsMap, int pasdeTemps,
			int anneeNTab, int annee, String usage, String idAgreg,
			HashMap<String, BigDecimal[]> elasticiteExistantMap, HashMap<String, BigDecimal[]> elasticiteNeufMap) {

		BigDecimal facteurElasticite = elasticiteExistantMap.get(usage + Energies.ELECTRICITE.getCode())[annee - 2009];
		BigDecimal dureeVie = dvNonRTMap.get(usage);
		Parc besoinExistant = new Parc(pasdeTemps);
		ParamGainsUsages paramGainsN = gainsMap.get(parcAgregExistant.getIdbranche() + usage);
		BigDecimal gainsN1 = paramGainsN.getPeriode(commonService.correspPeriode(annee));
		BigDecimal surfN1 = parcAgregExistant.getAnnee(1);
		BigDecimal surfN = parcAgregExistant.getAnnee(0);

		if (usageMap.containsKey(idAgreg + Energies.ELECTRICITE.getCode())) {

			besoinExistant = usageMap.get(idAgreg + Energies.ELECTRICITE.getCode());
			BigDecimal besoinN = BigDecimal.ZERO;
			if (besoinExistant.getAnnee(anneeNTab - 1) != null) {
				besoinN = besoinExistant.getAnnee(anneeNTab - 1);
			}
			besoinExistant.setAnnee(anneeNTab,
					calcBesoinN1(surfN1, dureeVie, surfN, besoinN, gainsN1, facteurElasticite, annee));
		} else {

			besoinExistant = calcBesoinNewSegment(parcAgregExistant,
					bNeufsMap.get(commonService.concatID(parcAgregExistant, usage)), idAgreg, pasdeTemps, anneeNTab,
					annee, commonService.concatID(parcAgregExistant, usage), elasticiteNeufMap, usage);

		}

		return besoinExistant;

	}

	protected Parc calcBesoinNewSegment(Parc parcAgregExistant, ParamBesoinsNeufs besoinsNeufs, String idAgreg,
			int pasdeTemps, int anneeNTab, int annee, String concat, HashMap<String, BigDecimal[]> elasticiteNeufMap,
			String usage) {

		BigDecimal facteurElasticite = elasticiteNeufMap.get(usage + Energies.ELECTRICITE.getCode())[annee - 2009];
		Parc besoinExistant = new Parc(pasdeTemps);
		BigDecimal surfN1 = parcAgregExistant.getAnnee(1);
		int periodeCstr = correspPeriodeCstr(parcAgregExistant, annee);
		BigDecimal besoinsU = besoinsNeufs.getPeriode(periodeCstr);

		besoinExistant.setId(idAgreg + Energies.ELECTRICITE.getCode());
		besoinExistant.setAnneeRenov(INIT_STATE);
		besoinExistant.setTypeRenovBat(TypeRenovBati.ETAT_INIT);
		besoinExistant.setAnnee(anneeNTab, formuleNewSegment(besoinsU, surfN1, facteurElasticite));

		return besoinExistant;
	}

	protected BigDecimal formuleNewSegment(BigDecimal besoinsU, BigDecimal surfN1, BigDecimal facteurElasticite) {

		BigDecimal newBesoin = (besoinsU.multiply(surfN1)).multiply(facteurElasticite, MathContext.DECIMAL32);
		return newBesoin;
	}

	protected int correspPeriodeCstr(Parc parcExistant, int annee) {
		int periodCstr = 0;
		if (parcExistant.getIdperiodesimple().equals(Period.PERIODE_BEFORE_1980.getCode())
				|| parcExistant.getIdperiodesimple().equals(Period.PERIODE_1981_1998.getCode())
				|| parcExistant.getIdperiodesimple().equals(Period.PERIODE_1999_2008.getCode())) {
			periodCstr = 0;
		} else if (parcExistant.getIdperiodesimple().equals(Period.PERIODE_2010_2015.getCode())) {
			periodCstr = 1;
		} else if (parcExistant.getIdperiodesimple().equals(Period.PERIODE_2016_2020.getCode())) {
			periodCstr = 2;
		} else if (parcExistant.getIdperiodesimple().equals(Period.PERIODE_2021_2030.getCode())) {
			periodCstr = 3;
		} else if (parcExistant.getIdperiodesimple().equals(Period.PERIODE_2031_2040.getCode())) {
			periodCstr = 4;
		} else if (parcExistant.getIdperiodesimple().equals(Period.PERIODE_2041_2050.getCode())) {
			periodCstr = 5;
		}
		return periodCstr;
	}

	protected BigDecimal calcBesoinN1(BigDecimal surfN1, BigDecimal dureeVie, BigDecimal surfN, BigDecimal besoinN,
			BigDecimal gainsN1, BigDecimal facteurElasticite, int annee) {

		BigDecimal besoinUN = calcBesoinUN(besoinN, surfN);
		BigDecimal besoinUN1 = calcBesoinUN1(besoinN, surfN, gainsN1);
		BigDecimal besoinN1 = new BigDecimal(0);
		if (dureeVie.compareTo(BigDecimal.ZERO) == 0) {
			besoinN1 = (surfN1.multiply(besoinUN)).multiply(facteurElasticite, MathContext.DECIMAL32);
		} else {
			BigDecimal besoinPart1 = surfN1.subtract(
					(BigDecimal.ONE.divide(dureeVie, MathContext.DECIMAL32)).multiply(surfN)).multiply(besoinUN);
			BigDecimal besoinPart2 = (BigDecimal.ONE.divide(dureeVie, MathContext.DECIMAL32)).multiply(surfN).multiply(
					besoinUN1);
			// multiplication par le facteur d'elasticite
			besoinN1 = (besoinPart1.add(besoinPart2)).multiply(facteurElasticite, MathContext.DECIMAL32);
		}
		return besoinN1;

		/*
		 * besoinN1 = ((surfN1 - (1 / dureeVie) * surfN) * besoinUN) + (((1 /
		 * dureeVie) * surfN) * besoinUN1);
		 */
	}

	protected BigDecimal calcBesoinUN(BigDecimal besoinN, BigDecimal surfN) {

		BigDecimal surfTempN = BigDecimal.ONE;
		if (surfN.compareTo(BigDecimal.ZERO) != 0) {
			surfTempN = surfN;
		}
		BigDecimal besoinUN = besoinN.divide(surfTempN, MathContext.DECIMAL32);

		return besoinUN;

	}

	protected BigDecimal calcBesoinUN1(BigDecimal besoinN, BigDecimal surfN, BigDecimal gainsN1) {

		BigDecimal surfTempN = BigDecimal.ONE;
		if (surfN.compareTo(BigDecimal.ZERO) != 0) {
			surfTempN = surfN;
		}
		BigDecimal besoinUN1 = (besoinN.divide(surfTempN, MathContext.DECIMAL32)).multiply((BigDecimal.ONE
				.subtract(gainsN1)));

		return besoinUN1;

	}

	protected Parc besoinNeufBurProcess(Parc parcAgregEntrant, HashMap<String, Parc> usageMap,
			HashMap<String, ParamBesoinsNeufs> bNeufsMap, String usage, int periode, int pasdeTemps, int anneeNTab,
			String idAgreg, HashMap<String, BigDecimal[]> elasticiteNeufMap, int annee) {

		// Calcul les besoins/consommations en bureautique ou process des
		// batiments entrants
		Parc newBesoin = new Parc(pasdeTemps);

		String concat = commonService.concatID(parcAgregEntrant, usage);
		ParamBesoinsNeufs besoinsNeufs = bNeufsMap.get(concat);
		// Le besoin unitaire est multiplie par le facteur d'elasticite
		BigDecimal besoinsU = besoinsNeufs.getPeriode(periode).multiply(
				elasticiteNeufMap.get(usage + Energies.ELECTRICITE.getCode())[annee - 2009], MathContext.DECIMAL32);
		newBesoin = new Parc(pasdeTemps);
		newBesoin.setId(idAgreg + Energies.ELECTRICITE.getCode());
		newBesoin.setAnneeRenov(INIT_STATE);
		newBesoin.setTypeRenovBat(TypeRenovBati.ETAT_INIT);
		// newBesoin.setAnnee(anneeNTab - 1, 0f);
		newBesoin.setAnnee(anneeNTab, parcAgregEntrant.getAnnee(1).multiply(besoinsU));

		return newBesoin;
	}
}
