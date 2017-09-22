package com.ed.cgdd.derby.usagesnonrt.impl;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ed.cgdd.derby.common.CommonService;
import com.ed.cgdd.derby.model.calcconso.ParamBesoinsNeufs;
import com.ed.cgdd.derby.model.calcconso.ParamGainsUsages;
import com.ed.cgdd.derby.model.calcconso.ParamPMConso;
import com.ed.cgdd.derby.model.calcconso.ParamPMConsoChgtSys;
import com.ed.cgdd.derby.model.parc.Parc;
import com.ed.cgdd.derby.model.parc.TypeRenovBati;
import com.ed.cgdd.derby.model.parc.Usage;
import com.ed.cgdd.derby.usagesnonrt.CuissonAutreService;

public class CuissonAutreServiceImpl implements CuissonAutreService {
	private final static Logger LOG = LogManager.getLogger(CuissonAutreServiceImpl.class);
	private final static String INIT_STATE = "Etat initial";
	private CommonService commonService;

	// TODO Rempli une table de resultats

	public CommonService getCommonService() {
		return commonService;
	}

	public void setCommonService(CommonService commonService) {
		this.commonService = commonService;
	}

	// Calcul les evolutions des besoins (=Conso) des usages Cuisson et
	// Autres (usages regroupes car susceptibles de changer d'energie lors du
	// renouvellement des systemes
	@Override
	public HashMap<String, Parc> evolCuissonAutre(HashMap<String, Parc> parcTotMap, HashMap<String, Parc> usageMap,
			HashMap<String, BigDecimal> dvNonRTMap, HashMap<String, ParamGainsUsages> gainsMap,
			HashMap<String, ParamBesoinsNeufs> bNeufsMap, HashMap<String, ParamPMConso> pmCuissonMap,
			HashMap<String, ParamPMConso> pmAutresMap, HashMap<String, ParamPMConsoChgtSys> pmCuissonChgtMap,
			HashMap<String, ParamPMConsoChgtSys> pmAutresChgtMap, int pasdeTemps, int anneeNTab, int annee,
			String usage, HashMap<String, BigDecimal[]> elasticiteNeufMap,
			HashMap<String, BigDecimal[]> elasticiteExistantMap) {

		// Agregation du parc sans le code "energie" (deux derniers chiffres de
		// l'ID)
		HashMap<String, Parc> parcTotAgreg = commonService.aggregateParc(parcTotMap, anneeNTab);
		HashMap<String, Parc> besoinTotAgreg = commonService.aggregateParc(usageMap, anneeNTab);

		// Parcours chaque segment de parc agrege

		for (String idAgreg : parcTotAgreg.keySet()) {

			Parc parcAgreg = parcTotAgreg.get(idAgreg);

			ParamPMConso pmNeuf = new ParamPMConso();
			HashMap<String, ParamPMConsoChgtSys> pmChgtMap = new HashMap<String, ParamPMConsoChgtSys>();
			// Teste s'il s'agit de l'usage de cuisson ou autres
			if (usage.equals(Usage.CUISSON.getLabel())) {
				pmNeuf = pmCuissonMap.get(parcAgreg.getIdbranche());
				pmChgtMap = pmCuissonChgtMap;
			} else {
				pmNeuf = pmAutresMap.get(parcAgreg.getIdbranche());
				pmChgtMap = pmAutresChgtMap;
			}

			int periodeCstr = commonService.correspPeriodeCstr(parcAgreg, annee);

			if (parcAgreg.getAnnee(0) == null || parcAgreg.getAnnee(0).signum() == 0) {
				HashMap<String, BigDecimal> energPm = pmNeuf.getEnergie();
				// boucle sur les energies des parts de marche dans le neuf

				for (String energPmNeufKey : energPm.keySet()) {
					// si le parc agrege est null a l'annee n-1 alors il s'agit
					// de batiments neufs

					Parc parcInsert = besoinNeufCuissAutre(parcAgreg, bNeufsMap, energPm.get(energPmNeufKey), usage,
							periodeCstr, pasdeTemps, anneeNTab, idAgreg, energPmNeufKey, elasticiteNeufMap, annee);
					usageMap.put(parcInsert.getId(), parcInsert);

				}
			} else {
				// Remplit l'objet energTransfert contenant les besoins
				// actualiseq a transferer par energie de l'annee n
				// a l'annee n1

				Parc besoinAgreg = besoinTotAgreg.get(idAgreg);
				HashMap<String, BigDecimal> besoinTransMap = new HashMap<String, BigDecimal>();
				besoinTransMap = calcTransfertBesoin(pmChgtMap, usageMap, parcAgreg, besoinAgreg, gainsMap, dvNonRTMap,
						idAgreg, usage, annee, anneeNTab);

				for (String energTransKey : besoinTransMap.keySet()) {

					if (besoinTransMap.get(energTransKey).signum() != 0 && besoinTransMap.get(energTransKey) != null) {
						Parc parcInsert = besoinExistantCuissAutre(usageMap, parcAgreg,
								besoinTransMap.get(energTransKey), dvNonRTMap.get(usage), pasdeTemps, anneeNTab, annee,
								usage, idAgreg, energTransKey, elasticiteExistantMap);

						usageMap.put(parcInsert.getId(), parcInsert);

					}
				}

			}

		}

		return usageMap;
	}

	protected HashMap<String, BigDecimal> calcTransfertBesoin(HashMap<String, ParamPMConsoChgtSys> pmChgtMap,
			HashMap<String, Parc> usageMap, Parc parcAgreg, Parc besoinAgreg,
			HashMap<String, ParamGainsUsages> gainsMap, HashMap<String, BigDecimal> dvNonRTMap, String idAgreg,
			String usage, int annee, int anneeNTab) {

		HashMap<String, BigDecimal> besoinTransMap = new HashMap<String, BigDecimal>();
		for (String pmEnergInitKey : pmChgtMap.keySet()) {
			ParamPMConsoChgtSys pmChgt = new ParamPMConsoChgtSys();
			pmChgt = pmChgtMap.get(pmEnergInitKey);
			String energ = StringUtils.stripAccents(pmEnergInitKey.toUpperCase());

			if (usageMap.containsKey(idAgreg + commonService.codeCreateEnerg(energ))) {
				Parc besoinSegment = usageMap.get(idAgreg + commonService.codeCreateEnerg(energ));

				ParamGainsUsages paramGainsN = gainsMap.get(parcAgreg.getIdbranche() + usage);

				BigDecimal surfN = parcAgreg.getAnnee(0);
				BigDecimal dureeVie = dvNonRTMap.get(usage);
				BigDecimal gainsN1 = paramGainsN.getPeriode(commonService.correspPeriode(annee));
				BigDecimal surfTransfert = BigDecimal.ZERO;
				if (dureeVie.signum() != 0) {
					surfTransfert = (BigDecimal.ONE.divide(dureeVie, MathContext.DECIMAL32)).multiply(surfN);
				}
				BigDecimal besoinSegmentN = BigDecimal.ZERO;
				if (besoinSegment.getAnnee(anneeNTab - 1) != null) {
					besoinSegmentN = besoinSegment.getAnnee(anneeNTab - 1);
				}
				BigDecimal pmEnergN = BigDecimal.ZERO;
				if (besoinAgreg.getAnnee(0) != null && besoinAgreg.getAnnee(0).signum() != 0) {
					pmEnergN = besoinSegmentN.divide(besoinAgreg.getAnnee(0), MathContext.DECIMAL32);
				}

				BigDecimal besoinTotU = besoinAgreg.getAnnee(0).divide(parcAgreg.getAnnee(0), MathContext.DECIMAL32);

				for (String energTrans : pmChgtMap.keySet()) {
					BigDecimal temp = BigDecimal.ZERO;
					if (besoinTransMap.get(energTrans) != null) {
						temp = besoinTransMap.get(energTrans);
					}
					besoinTransMap.put(
							energTrans,
							calcFormuleBesoinTransfert(surfTransfert, pmEnergN, besoinTotU, gainsN1, pmChgt, temp,
									energTrans));

				}

			}
		}

		return besoinTransMap;

	}

	protected BigDecimal calcFormuleBesoinTransfert(BigDecimal surfTransfert, BigDecimal pmEnergN,
			BigDecimal besoinTotU, BigDecimal gainsN1, ParamPMConsoChgtSys pmChgt, BigDecimal temp, String energTrans) {

		BigDecimal result = surfTransfert.multiply(pmEnergN).multiply(besoinTotU)
				.multiply((BigDecimal.ONE.subtract(gainsN1)).multiply(pmChgt.getPmChgt(energTrans))).add(temp);
		return result;
	}

	protected Parc besoinNeufCuissAutre(Parc parcAgregEntrant, HashMap<String, ParamBesoinsNeufs> bNeufsMap,
			BigDecimal pmEnergNeuf, String usage, int periodeCstr, int pasdeTemps, int anneeNTab, String idAgreg,
			String energPmKey, HashMap<String, BigDecimal[]> elasticiteNeufMap, int annee) {

		// Calcul les besoins/consommations en cuisson et usage "Autre" des
		// batiments entrants ou des nouveaux segments
		Parc newBesoin = new Parc(pasdeTemps);

		String concat = commonService.concatID(parcAgregEntrant, usage);
		ParamBesoinsNeufs besoinsNeufs = bNeufsMap.get(concat);
		String energ = StringUtils.stripAccents(energPmKey.toUpperCase());
		String energCode = commonService.codeCreateEnerg(energ);
		// le besoin unitaire est multiplie par le facteur d'elasticite
		BigDecimal besoinsU = besoinsNeufs.getPeriode(periodeCstr).multiply(
				elasticiteNeufMap.get(usage + energCode)[annee - 2009], MathContext.DECIMAL32);
		newBesoin.setId(idAgreg + energCode);
		newBesoin.setAnneeRenov(INIT_STATE);
		newBesoin.setTypeRenovBat(TypeRenovBati.ETAT_INIT);
		newBesoin.setAnnee(anneeNTab, parcAgregEntrant.getAnnee(1).multiply(besoinsU).multiply(pmEnergNeuf));

		return newBesoin;
	}

	protected Parc besoinExistantCuissAutre(HashMap<String, Parc> usageMap, Parc parcAgregExistant,
			BigDecimal pmEnergTrans, BigDecimal dureeVie, int pasdeTemps, int anneeNTab, int annee, String usage,
			String idAgreg, String energTransKey, HashMap<String, BigDecimal[]> elasticiteExistantMap) {

		Parc besoinExistant = new Parc(pasdeTemps);

		BigDecimal surfN1 = parcAgregExistant.getAnnee(1);
		BigDecimal surfN = parcAgregExistant.getAnnee(0);
		String energ = StringUtils.stripAccents(energTransKey.toUpperCase());
		String energCode = commonService.codeCreateEnerg(energ);
		BigDecimal facteurElasticite = elasticiteExistantMap.get(usage + energCode)[annee - 2009];
		if (usageMap.containsKey(idAgreg + energCode)) {

			besoinExistant = usageMap.get(idAgreg + energCode);
			BigDecimal besoinN = BigDecimal.ZERO;
			if (besoinExistant.getAnnee(anneeNTab - 1) != null) {
				besoinN = besoinExistant.getAnnee(anneeNTab - 1);
			}
			besoinExistant.setAnnee(anneeNTab,
					calcBesoinN1(pmEnergTrans, surfN1, dureeVie, surfN, besoinN, facteurElasticite));
		} else {

			besoinExistant = calcBesoinNewSegment(parcAgregExistant, pmEnergTrans, idAgreg, pasdeTemps, anneeNTab,
					annee, energTransKey, facteurElasticite);

		}

		return besoinExistant;

	}

	protected BigDecimal calcBesoinN1(BigDecimal pmEnergTrans, BigDecimal surfN1, BigDecimal dureeVie,
			BigDecimal surfN, BigDecimal besoinN, BigDecimal facteurElasticite) {
		// Facteur d'elasticite
		BigDecimal besoinUN = calcBesoinUN(besoinN, surfN);

		BigDecimal besoinN1 = BigDecimal.ZERO;
		if (dureeVie.signum() == 0) {
			besoinN1 = (surfN1.multiply(besoinUN)).multiply(facteurElasticite, MathContext.DECIMAL32);
		} else {
			besoinN1 = ((surfN1.subtract((BigDecimal.ONE.divide(dureeVie, MathContext.DECIMAL32)).multiply(surfN))
					.multiply(besoinUN)).add(pmEnergTrans)).multiply(facteurElasticite, MathContext.DECIMAL32);
		}
		return besoinN1;

	}

	protected BigDecimal calcBesoinUN(BigDecimal besoinN, BigDecimal surfN) {

		BigDecimal surfTempN = BigDecimal.ONE;
		if (surfN.signum() != 0) {
			surfTempN = surfN;
		}
		BigDecimal besoinUN = besoinN.divide(surfTempN, MathContext.DECIMAL32);

		return besoinUN;

	}

	protected Parc calcBesoinNewSegment(Parc parcAgregExistant, BigDecimal pmEnergTrans, String idAgreg,
			int pasdeTemps, int anneeNTab, int annee, String energTransKey, BigDecimal facteurElasticite) {

		Parc besoinExistant = new Parc(pasdeTemps);
		String energ = StringUtils.stripAccents(energTransKey.toUpperCase());
		besoinExistant.setId(idAgreg + commonService.codeCreateEnerg(energ));
		besoinExistant.setAnneeRenov(INIT_STATE);
		besoinExistant.setTypeRenovBat(TypeRenovBati.ETAT_INIT);
		// Multiplication par le facteur d'elasticite
		besoinExistant.setAnnee(anneeNTab, pmEnergTrans.multiply(facteurElasticite, MathContext.DECIMAL32));

		return besoinExistant;
	}

}
