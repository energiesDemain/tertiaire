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
import com.ed.cgdd.derby.model.parc.SsBranche;
import com.ed.cgdd.derby.model.parc.TypeRenovBati;
import com.ed.cgdd.derby.usagesnonrt.FroidAlimService;

public class FroidAlimServiceImpl implements FroidAlimService {
	private final static Logger LOG = LogManager.getLogger(FroidAlimServiceImpl.class);
	private CommonService commonService;
	private final static String INIT_STATE = "Etat initial";

	public CommonService getCommonService() {
		return commonService;
	}

	public void setCommonService(CommonService commonService) {
		this.commonService = commonService;
	}

	// Calcul les evolutions des besoins (=Conso) des usages Bureautique et
	// Process
	@Override
	public HashMap<String, Parc> evolFroidAlim(HashMap<String, Parc> parcTotMap, HashMap<String, Parc> usageMap,
			HashMap<String, BigDecimal> dvNonRTMap, HashMap<String, ParamGainsUsages> gainsMap,
			HashMap<String, BigDecimal> rythmeFrdRgltMap, HashMap<String, BigDecimal> gainFrdRgltMap,
			HashMap<String, ParamBesoinsNeufs> bNeufsMap, int pasdeTemps, int anneeNTab, int annee, String usage,
			HashMap<String, BigDecimal[]> elasticiteNeufMap, HashMap<String, BigDecimal[]> elasticiteExistantMap) {

		BigDecimal facteurElasNeuf = elasticiteNeufMap.get(usage + Energies.ELECTRICITE.getCode())[annee - 2009];
		BigDecimal facteurElasExistant = elasticiteExistantMap.get(usage + Energies.ELECTRICITE.getCode())[annee - 2009];
		// Agregation du parc sans le code "energie"
		HashMap<String, Parc> parcTotAgreg = commonService.aggregateParc(parcTotMap, anneeNTab);
		// Actualise les besoins/consommations du parc de batiments existant
		// l'annee N

		for (String idAgreg : parcTotAgreg.keySet()) {

			Parc parcAgreg = parcTotAgreg.get(idAgreg);

			int periode = commonService.correspPeriode(annee);
			if (parcAgreg.getAnnee(0) == null || parcAgreg.getAnnee(0).signum() == 0) {
				// si une des deux conditions est realisee alors il s'agit
				// d'un
				// segment entrant dans le parc
				usageMap.put(
						idAgreg + Energies.ELECTRICITE.getCode(),
						besoinNeufFroidAlim(parcAgreg, usageMap, bNeufsMap, usage, periode, pasdeTemps, anneeNTab,
								idAgreg, facteurElasNeuf));
			} else {
				// sinon il s'agit d'un segment existant a l'annee n
				if (usageMap.containsKey(idAgreg + Energies.ELECTRICITE.getCode())) {
					usageMap.put(
							idAgreg + Energies.ELECTRICITE.getCode(),
							besoinExistantFroidAlim(usageMap, bNeufsMap, parcAgreg, dvNonRTMap, gainsMap,
									rythmeFrdRgltMap, gainFrdRgltMap, pasdeTemps, anneeNTab, annee, usage, idAgreg,
									facteurElasExistant));
				}
			}

		}

		return usageMap;

	}

	protected Parc besoinExistantFroidAlim(HashMap<String, Parc> usageMap,
			HashMap<String, ParamBesoinsNeufs> bNeufsMap, Parc parcAgregExistant,
			HashMap<String, BigDecimal> dvNonRTMap, HashMap<String, ParamGainsUsages> gainsMap,
			HashMap<String, BigDecimal> rythmeFrdRgltMap, HashMap<String, BigDecimal> gainFrdRgltMap, int pasdeTemps,
			int anneeNTab, int annee, String usage, String idAgreg, BigDecimal facteurElasExistant) {

		BigDecimal dureeVie = dvNonRTMap.get(usage);
		BigDecimal rythmeRglt = rythmeFrdRgltMap.get(commonService.correspPeriodeString(annee));
		Parc besoinExistant = new Parc(pasdeTemps);
		ParamGainsUsages paramGainsN = gainsMap.get(parcAgregExistant.getIdbranche() + usage);
		BigDecimal gainsN1 = paramGainsN.getPeriode(commonService.correspPeriode(annee));
		BigDecimal gainsRgt = gainFrdRgltMap.get(commonService.correspPeriodeString(annee));
		BigDecimal surfN1 = parcAgregExistant.getAnnee(1);
		BigDecimal surfN = parcAgregExistant.getAnnee(0);

		besoinExistant = usageMap.get(idAgreg + Energies.ELECTRICITE.getCode());
		BigDecimal besoinN = BigDecimal.ZERO;
		if (besoinExistant.getAnnee(anneeNTab - 1) != null) {
			besoinN = besoinExistant.getAnnee(anneeNTab - 1);
		}

		if (parcAgregExistant.getIdssbranche().equals(SsBranche.GRAND_COMMERCE_ALIMENTAIRE.getCode())) {
			besoinExistant.setAnnee(
					anneeNTab,
					calcBesoinN1Rglt(surfN1, dureeVie, rythmeRglt, surfN, besoinN, gainsN1, gainsRgt,
							facteurElasExistant));
		} else {
			besoinExistant.setAnnee(anneeNTab,
					calcBesoinN1(surfN1, dureeVie, surfN, besoinN, gainsN1, facteurElasExistant));
		}
		return besoinExistant;

	}

	protected BigDecimal formuleNewSegment(BigDecimal besoinsU, BigDecimal surfN1) {

		BigDecimal newBesoin = besoinsU.multiply(surfN1);
		return newBesoin;
	}

	protected BigDecimal calcBesoinN1(BigDecimal surfN1, BigDecimal dureeVie, BigDecimal surfN, BigDecimal besoinN,
			BigDecimal gainsN1, BigDecimal facteurElasExistant) {

		BigDecimal besoinUN = calcBesoinUN(besoinN, surfN);
		BigDecimal besoinUN1 = calcBesoinUN1(besoinN, surfN, gainsN1);
		BigDecimal besoinN1 = BigDecimal.ZERO;
		if (dureeVie.signum() == 0) {
			besoinN1 = surfN1.multiply(besoinUN);
		} else {
			besoinN1 = ((surfN1.subtract((BigDecimal.ONE.divide(dureeVie, MathContext.DECIMAL32)).multiply(surfN)))
					.multiply(besoinUN))
					.add((((BigDecimal.ONE.divide(dureeVie, MathContext.DECIMAL32)).multiply(surfN))
							.multiply(besoinUN1)));
		}
		return besoinN1.multiply(facteurElasExistant);

	}

	protected BigDecimal calcBesoinN1Rglt(BigDecimal surfN1, BigDecimal dureeVie, BigDecimal rythmeRglt,
			BigDecimal surfN, BigDecimal besoinN, BigDecimal gainsN1, BigDecimal gainsRgt,
			BigDecimal facteurElasExistant) {

		BigDecimal rythmeDV = BigDecimal.ZERO;
		if (dureeVie.signum() != 0) {
			rythmeDV = new BigDecimal("1").divide(dureeVie, MathContext.DECIMAL32);
		}
		BigDecimal besoinUN = calcBesoinUN(besoinN, surfN);
		BigDecimal besoinUN1 = calcBesoinUN1(besoinN, surfN, gainsN1);
		BigDecimal besoinUN1Rglt = calcBesoinUN1Rglt(besoinN, surfN, gainsRgt);
		BigDecimal besoinN1Rglt = BigDecimal.ZERO;
		if (rythmeRglt.compareTo(rythmeDV) == -1) {
			BigDecimal surfRenouv = rythmeDV.multiply(surfN);
			BigDecimal besoinRenouvTendanciel = (rythmeDV.subtract(rythmeRglt)).multiply(surfN).multiply(besoinUN1);
			BigDecimal besoinRenouvRglt = rythmeRglt.multiply(surfN).multiply(besoinUN1Rglt);
			besoinN1Rglt = ((surfN1.subtract(surfRenouv)).multiply(besoinUN)).add(besoinRenouvTendanciel).add(
					besoinRenouvRglt);
		} else {
			besoinN1Rglt = ((surfN1.subtract(rythmeRglt.multiply(surfN))).multiply(besoinUN)).add(rythmeRglt.multiply(
					surfN).multiply(besoinUN1Rglt));
		}
		return besoinN1Rglt.multiply(facteurElasExistant);

	}

	protected BigDecimal calcBesoinUN(BigDecimal besoinN, BigDecimal surfN) {

		BigDecimal surfTempN = BigDecimal.ONE;
		if (surfN.signum() != 0) {
			surfTempN = surfN;
		}
		BigDecimal besoinUN = besoinN.divide(surfTempN, MathContext.DECIMAL32);

		return besoinUN;

	}

	protected BigDecimal calcBesoinUN1(BigDecimal besoinN, BigDecimal surfN, BigDecimal gainsN1) {

		BigDecimal surfTempN = BigDecimal.ONE;
		if (surfN.signum() != 0) {
			surfTempN = surfN;
		}
		BigDecimal besoinUN1 = (besoinN.divide(surfTempN, MathContext.DECIMAL32)).multiply((BigDecimal.ONE
				.subtract(gainsN1)));

		return besoinUN1;

	}

	protected BigDecimal calcBesoinUN1Rglt(BigDecimal besoinN, BigDecimal surfN, BigDecimal gainsRgt) {

		BigDecimal surfTempN = BigDecimal.ONE;
		if (surfN.signum() != 0) {
			surfTempN = surfN;
		}
		BigDecimal besoinUN1Rglt = (besoinN.divide(surfTempN, MathContext.DECIMAL32)).multiply((BigDecimal.ONE
				.subtract(gainsRgt)));

		return besoinUN1Rglt;

	}

	protected Parc besoinNeufFroidAlim(Parc parcAgregEntrant, HashMap<String, Parc> usageMap,
			HashMap<String, ParamBesoinsNeufs> bNeufsMap, String usage, int periode, int pasdeTemps, int anneeNTab,
			String idAgreg, BigDecimal facteurElasNeuf) {

		// Calcul les besoins/consommations en froid alimentaire des
		// batiments entrants
		Parc newBesoin = new Parc(pasdeTemps);

		String concat = commonService.concatID(parcAgregEntrant, usage);
		ParamBesoinsNeufs besoinsNeufs = bNeufsMap.get(concat);
		// Multiplication par
		BigDecimal besoinsU = besoinsNeufs.getPeriode(periode).multiply(facteurElasNeuf);
		newBesoin = new Parc(pasdeTemps);
		newBesoin.setId(idAgreg + Energies.ELECTRICITE.getCode());
		newBesoin.setAnneeRenov(INIT_STATE);
		newBesoin.setTypeRenovBat(TypeRenovBati.ETAT_INIT);
		// newBesoin.setAnnee(anneeNTab - 1, 0f);
		newBesoin.setAnnee(anneeNTab, parcAgregEntrant.getAnnee(1).multiply(besoinsU));

		return newBesoin;
	}

}
