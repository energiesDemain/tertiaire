package com.ed.cgdd.derby.usagesrt.impl;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ed.cgdd.derby.common.CommonService;
import com.ed.cgdd.derby.model.calcconso.Conso;
import com.ed.cgdd.derby.model.calcconso.ParamBesoinsNeufs;
import com.ed.cgdd.derby.model.calcconso.ParamCoutEclVentil;
import com.ed.cgdd.derby.model.calcconso.ParamGainsUsages;
import com.ed.cgdd.derby.model.calcconso.ResultConsoRt;
import com.ed.cgdd.derby.model.calcconso.ResultConsoURt;
import com.ed.cgdd.derby.model.parc.Energies;
import com.ed.cgdd.derby.model.parc.MapResultsKeys;
import com.ed.cgdd.derby.model.parc.Parc;
import com.ed.cgdd.derby.model.parc.TypeRenovBati;
import com.ed.cgdd.derby.model.parc.TypeRenovSysteme;
import com.ed.cgdd.derby.model.parc.Usage;
import com.ed.cgdd.derby.process.InitializeConsoService;
import com.ed.cgdd.derby.usagesrt.EclairageService;

public class EclairageServiceImpl implements EclairageService {
	private final static Logger LOG = LogManager.getLogger(EclairageServiceImpl.class);
	private CommonService commonService;
	private InitializeConsoService initializeConsoService;
	private static final int START_ID_SEG = 0;
	private static final int LENGHT_ID_SEG_TOT = 14;
	private static final int LENGHT_ID_SEG_NEUF = 12;
	private static final int START_ID_BRANCHE = 0;
	private static final int LENGHT_ID_BRANCHE = 2;
	private static final BigDecimal FACTEUR_EP = new BigDecimal("2.58");
	private final static String INIT_STATE = "Etat initial";

	public CommonService getCommonService() {
		return commonService;
	}

	public void setCommonService(CommonService commonService) {
		this.commonService = commonService;
	}

	public InitializeConsoService getInitializeConsoService() {
		return initializeConsoService;
	}

	public void setInitializeConsoService(InitializeConsoService initializeConsoService) {
		this.initializeConsoService = initializeConsoService;
	}

	// Calcul les evolutions des besoins et des consommations de l'usage de
	// climatisation
	@Override
	public ResultConsoRt evolEclairageConso(HashMap<String, ParamCoutEclVentil> coutsEclVentilMap,
			HashMap<String, Parc> parcTotMap, ResultConsoRt resultatsConso,
			HashMap<String, ParamGainsUsages> gainsEclairageMap, HashMap<String, ParamBesoinsNeufs> bNeufsMap,
			HashMap<String, BigDecimal> dvUsagesMap, int anneeNTab, int pasdeTemps, int annee, BigDecimal compteur,
			String usage, HashMap<String, ResultConsoURt> resultConsoURtMap,
			HashMap<String, BigDecimal[]> elasticiteNeufMap, HashMap<String, BigDecimal[]> elasticiteExistantMap) {

		BigDecimal facteurElasNeuf = elasticiteNeufMap.get(usage + Energies.ELECTRICITE.getCode())[annee - 2009];
		BigDecimal facteurElasExistant = elasticiteExistantMap.get(usage + Energies.ELECTRICITE.getCode())[annee - 2009];

		// Initialisation des objets
		// cle de la map = idAgreg + code elec + anneeRenovBat + typeRenovBat
		HashMap<String, Conso> besoinMap = resultatsConso.getMap(MapResultsKeys.ECLAIRAGE.getLabel());
		HashMap<String, Conso> coutMap = resultatsConso.getMap(MapResultsKeys.COUT_ECLAIRAGE.getLabel());

		// Agregation du parc selon idAgreg + anneeRenovBat + typeRenovBat
		HashMap<String, Parc> parcTotAgregMap = commonService.aggregateParcEclairage(parcTotMap, anneeNTab);
		HashMap<String, Conso> besoinTotAgregMap = commonService.aggregateBesoinEclairage(besoinMap, anneeNTab);
		// construction de la liste contenant pour chaque idAgregParc toutes les
		// combinaisons contenues dans besoinMap
		HashMap<String, List<String>> keyMap = idAgregListExist(besoinMap, parcTotAgregMap);
		HashMap<String, List<String>> keyMapNeuf = idAgregListNeuf(parcTotAgregMap, annee);
		// Parcours chaque segment de parc agrege
		for (String idAgregParc : parcTotAgregMap.keySet()) {

			Parc parcAgreg = parcTotAgregMap.get(idAgregParc);
			Conso besoinAgreg = besoinTotAgregMap.get(idAgregParc);
			List<String> idList = keyMap.get(idAgregParc);
			List<String> idNeufList = keyMapNeuf.get(idAgregParc);
			List<String> listBoucle = new ArrayList<String>();
			if (idList.isEmpty()) {
				listBoucle = idNeufList;
			} else {
				listBoucle = idList;
			}
			int periodeCstr = commonService.correspPeriodeCstr(parcAgreg, annee);
			int periode = commonService.correspPeriode(annee);
			// KeyEcl = idAgreg + elec.getCode + anneeRenovSys + typeRenovSys +
			// anneeRenovBat + typeRenovBat
			if (listBoucle != null) {
				for (String keyEcl : listBoucle) {
					if (parcAgreg.getAnnee(0) == null || parcAgreg.getAnnee(0).signum() == 0) {
						// Si le parc a l'annee n-1 est null ou bien egal a 0
						// alors le segment correspond a des batiments neufs
						// ou bien il s'agit d'un batiment ayant ete renove en
						// ENSBBC

						batimentsNeufsEclairage(coutsEclVentilMap, coutMap, bNeufsMap, anneeNTab, pasdeTemps, annee,
								usage, besoinMap, parcAgreg, keyEcl, periodeCstr, periode, resultConsoURtMap,
								facteurElasNeuf);

					} else {

						// // cas d'un segment existant climatise
						batimentsExistantsEclairage(besoinAgreg, coutsEclVentilMap, coutMap, bNeufsMap, dvUsagesMap,
								gainsEclairageMap, anneeNTab, pasdeTemps, annee, compteur, besoinMap, keyEcl,
								idAgregParc, parcAgreg, periodeCstr, periode, usage, resultConsoURtMap,
								facteurElasExistant);

					}
				}
			}

		}
		// Encapsule les map de resultats
		resultatsConso.put(Usage.ECLAIRAGE.getLabel(), besoinMap);
		resultatsConso.put(MapResultsKeys.COUT_ECLAIRAGE.getLabel(), coutMap);

		return resultatsConso;
	}

	protected void batimentsExistantsEclairage(Conso besoinAgreg,
			HashMap<String, ParamCoutEclVentil> coutsEclVentilMap, HashMap<String, Conso> coutMap,
			HashMap<String, ParamBesoinsNeufs> bNeufsMap, HashMap<String, BigDecimal> dvUsagesMap,
			HashMap<String, ParamGainsUsages> gainsEclairageMap, int anneeNTab, int pasdeTemps, int annee,
			BigDecimal compteur, HashMap<String, Conso> besoinMap, String keyEcl, String idAgregParc, Parc parcAgreg,
			int periodeCstr, int periode, String usage, HashMap<String, ResultConsoURt> resultConsoURtMap,
			BigDecimal facteurElasExistant) {

		Conso besoinSegment = besoinMap.get(keyEcl);

		// Calcul de la duree de vie
		BigDecimal dureeVie = BigDecimal.ZERO;
		if (dvUsagesMap.get(usage) != null) {
			dureeVie = dvUsagesMap.get(usage);
		}

		// Taux d'evolution du besoin uniquement du fait de
		// l'evolution des surfaces
		BigDecimal tauxEvolBesoin = calcBesoinEvol(parcAgreg);

		// Generation des id a tester pour savoir si une renovation
		// doit etre menee ou non
		BigDecimal anneeBd = new BigDecimal(String.valueOf(annee));
		BigDecimal anneeRenovSys = anneeBd.subtract(dureeVie).setScale(0, BigDecimal.ROUND_HALF_UP);
		;

		// si besoin Evol est superieur a 1, alors un nouveau
		// segment est creee
		if (tauxEvolBesoin.compareTo(BigDecimal.ONE) == 1) {
			// Creation des besoins pour un nouveau segment
			if (besoinSegment != null) {
				Conso besoinSegmentNew = besoinNewSegment(parcAgreg, besoinSegment, tauxEvolBesoin, annee, pasdeTemps,
						anneeNTab, resultConsoURtMap, facteurElasExistant);
				newMapSegment(anneeNTab, besoinMap, besoinSegmentNew);
			}
		}
		// Test pour savoir sur une renovation va avoir lieu sur le
		// segment considere
		if (!besoinSegment.getAnneeRenovSys().equals(anneeRenovSys.toString())
				&& !besoinSegment.getAnneeRenovSys().equals(INIT_STATE)) {
			// Cas d'un segment n'ayant pas a faire de renovation
			// les besoins evoluent donc uniquement en fonction des
			// surfaces (entrees et sorties)
			// si des transferts d'energies doivent avoir lieu vers
			// ces segments alors ils le sont dans la seconde partie
			// de la condition
			if (tauxEvolBesoin.compareTo(BigDecimal.ONE) == 1) {
				tauxEvolBesoin = BigDecimal.ONE;
			}
			if (besoinSegment != null) {
				BigDecimal besoinNTemp = BigDecimal.ONE;
				if (besoinSegment.getAnnee(anneeNTab - 1) != null) {
					// Prise en compte du facteur d'elasticite du besoin
					besoinNTemp = besoinSegment.getAnnee(anneeNTab - 1).multiply(facteurElasExistant,
							MathContext.DECIMAL32);
				}
				besoinSegment.setAnnee(anneeNTab, tauxEvolBesoin.multiply(besoinNTemp));
				besoinMap.put(keyEcl, besoinSegment);
				// Remplissage de la map de consoU
				String idConsoU = besoinSegment.getIdagreg() + besoinSegment.getAnneeRenov()
						+ besoinSegment.getTypeRenovBat();
				if (resultConsoURtMap.containsKey(idConsoU)) {
					BigDecimal besoinTotEP = besoinSegment.getAnnee(anneeNTab).multiply(FACTEUR_EP);
					initializeConsoService.insertResultConsoUExistEcl(resultConsoURtMap, anneeNTab, idConsoU,
							besoinSegment.getAnnee(anneeNTab), besoinTotEP, annee);

				} else {
					LOG.info("Probleme l207..");
				}
			}

		} else {

			// Cas ou les segments doivent faire l'objet de travaux
			// de renovation
			// Calcul des besoins qui vont sortir du segment
			BigDecimal besoinSortant = calcBesoinTransfert(compteur, besoinSegment, dureeVie, keyEcl, anneeNTab, annee);

			// Calcul du nouveau besoin pour le segment pre-existant
			// ou ayant deja subit une renovation
			besoinSegment = besoinExistantModif(besoinSegment, tauxEvolBesoin, besoinSortant, anneeNTab, annee,
					resultConsoURtMap, facteurElasExistant);
			besoinMap.put(keyEcl, besoinSegment);
			// Creation des segments vers lesquels les besoins sont transferes
			// Les systemes sortants sont donc remplaces par des systemes
			// identiques mais plus performants
			besoinMap = besoinModifTrans(besoinSegment, keyEcl, besoinMap, gainsEclairageMap, besoinSortant, annee,
					anneeNTab, pasdeTemps, usage, facteurElasExistant);

			// Calcul des couts
			coutMap = coutExistant(gainsEclairageMap, coutMap, parcAgreg, besoinAgreg, coutsEclVentilMap,
					besoinSegment, keyEcl, besoinSortant, annee, anneeNTab, pasdeTemps);

		}

	}

	protected HashMap<String, Conso> besoinModifTrans(Conso besoinInit, String keyEcl,
			HashMap<String, Conso> besoinMap, HashMap<String, ParamGainsUsages> gainsEclairageMap,
			BigDecimal besoinEntrant, int annee, int anneeNTab, int pasdeTemps, String usage,
			BigDecimal facteurElasExistant) {

		ParamGainsUsages gainsUsages = gainsEclairageMap.get(keyEcl.substring(START_ID_BRANCHE, START_ID_BRANCHE
				+ LENGHT_ID_BRANCHE)
				+ usage);
		BigDecimal gainN1 = gainsUsages.getPeriode(commonService.correspPeriode(annee));
		// Nouveau besoin = besoinEntrant * (1-Gain)*facteurElasticite
		BigDecimal besoinEntrantNew = (besoinEntrant.multiply((BigDecimal.ONE.subtract(gainN1)))).multiply(
				facteurElasExistant, MathContext.DECIMAL32);

		if (!besoinMap.containsKey(generateIDNew(besoinInit, annee))) {

			// si le segment n'existe pas :
			Conso besoinSegment = new Conso(pasdeTemps);
			besoinSegment.setId(getIdSeg(keyEcl));
			besoinSegment.setAnneeRenovSys(String.valueOf(annee));
			besoinSegment.setTypeRenovSys(TypeRenovSysteme.CHGT_SYS);
			besoinSegment.setAnneeRenov(besoinInit.getAnneeRenov());
			besoinSegment.setTypeRenovBat(besoinInit.getTypeRenovBat());
			besoinSegment.setAnnee(anneeNTab, besoinEntrantNew);
			besoinMap.put(generateIDNew(besoinInit, annee), besoinSegment);

		} else {

			Conso besoinSegment = besoinMap.get(generateIDNew(besoinInit, annee));

			if (besoinSegment.getAnnee(anneeNTab) != null) {
				besoinSegment.setAnnee(anneeNTab, besoinEntrantNew.add(besoinSegment.getAnnee(anneeNTab)));
			} else {

				besoinSegment.setAnnee(anneeNTab, besoinEntrantNew);
			}
			besoinMap.put(generateIDNew(besoinSegment, annee), besoinSegment);
		}

		return besoinMap;
	}

	protected HashMap<String, Conso> coutExistant(HashMap<String, ParamGainsUsages> gainsEclairageMap,
			HashMap<String, Conso> coutMap, Parc parcAgreg, Conso besoinAgreg,
			HashMap<String, ParamCoutEclVentil> coutsEclVentilMap, Conso besoinInit, String keyEcl,
			BigDecimal besoinEntrant, int annee, int anneeNTab, int pasdeTemps) {

		ParamGainsUsages gainsUsages = gainsEclairageMap.get(keyEcl.substring(START_ID_BRANCHE, START_ID_BRANCHE
				+ LENGHT_ID_BRANCHE)
				+ Usage.ECLAIRAGE.getLabel());
		BigDecimal gainN1 = gainsUsages.getPeriode(commonService.correspPeriode(annee));

		Conso cout = new Conso(pasdeTemps);
		BigDecimal partBesoin = BigDecimal.ZERO;
		BigDecimal coutUTemp = coutsEclVentilMap.get(Usage.ECLAIRAGE.getLabel() + besoinInit.getIdbranche()).getCout();
		BigDecimal gainMax = new BigDecimal("0.6");
		// Le cout est calcule au proratat du gain attendu par rapport au gain
		// max atteignable
		if (gainN1.signum() <= 0) {
			// Valeur minimale pour le gain pour avoir un cout a chaque
			// changement de systeme
			gainN1 = new BigDecimal("0.1");
		}
		BigDecimal coutU = coutUTemp.multiply(gainN1.divide(gainMax, MathContext.DECIMAL32));
		if (besoinAgreg != null && besoinAgreg.getAnnee(0) != null && besoinAgreg.getAnnee(0).signum() != 0) {
			partBesoin = besoinEntrant.divide(besoinAgreg.getAnnee(0), MathContext.DECIMAL32);
		}

		BigDecimal surfTrans = BigDecimal.ZERO;
		if (parcAgreg != null && parcAgreg.getAnnee(0) != null) {
			surfTrans = partBesoin.multiply(parcAgreg.getAnnee(0));
		}
		BigDecimal coutInsert = coutU.multiply(surfTrans);

		if (!coutMap.containsKey(generateIDNew(besoinInit, annee))) {
			cout.setId(getIdSeg(keyEcl));
			cout.setAnneeRenovSys(String.valueOf(annee));
			cout.setTypeRenovSys(TypeRenovSysteme.CHGT_SYS);
			cout.setAnneeRenov(besoinInit.getAnneeRenov());
			cout.setTypeRenovBat(besoinInit.getTypeRenovBat());
			cout.setAnnee(anneeNTab, coutInsert);
			coutMap.put(generateIDNew(cout, annee), cout);
		} else {

			Conso coutExist = coutMap.get(generateIDNew(besoinInit, annee));

			if (coutExist.getAnnee(anneeNTab) != null) {
				coutExist.setAnnee(anneeNTab, coutInsert.add(coutExist.getAnnee(anneeNTab)));
			} else {

				cout.setAnnee(anneeNTab, coutInsert);
			}
			coutMap.put(generateIDNew(coutExist, annee), coutExist);
		}

		return coutMap;
	}

	protected String generateIDNew(Conso besoinInit, int annee) {

		StringBuffer buffer = new StringBuffer();
		buffer.append(getIdSeg(besoinInit.getId()));
		buffer.append(String.valueOf(annee));
		buffer.append(TypeRenovSysteme.CHGT_SYS);
		buffer.append(besoinInit.getAnneeRenov());
		buffer.append(besoinInit.getTypeRenovBat());

		return buffer.toString();

	}

	protected Conso besoinExistantModif(Conso besoinSegment, BigDecimal tauxEvolBesoin, BigDecimal besoinSortant,
			int anneeNTab, int annee, HashMap<String, ResultConsoURt> resultConsoURtMap, BigDecimal facteurElasExistant) {

		// Formule : besoinSegment = besoinEvol - besoinSortant
		// Test si le resultat est negatif on affecte 0
		BigDecimal besoinEvol = besoinSegment.getAnnee(anneeNTab - 1).multiply(tauxEvolBesoin);

		if ((besoinEvol.subtract(besoinSortant)).signum() == -1) {
			besoinSegment.setAnnee(anneeNTab, BigDecimal.ZERO);
		} else {
			// Prise en compte du facteur d'elasticite du besoin
			besoinSegment.setAnnee(anneeNTab,
					(besoinEvol.subtract(besoinSortant)).multiply(facteurElasExistant, MathContext.DECIMAL32));

		}

		// Remplissage de la map de consoU
		String idConsoU = besoinSegment.getIdagreg() + besoinSegment.getAnneeRenov() + besoinSegment.getTypeRenovBat();
		if (resultConsoURtMap.containsKey(idConsoU)) {
			BigDecimal besoinTotEP = besoinSegment.getAnnee(anneeNTab).multiply(FACTEUR_EP);
			initializeConsoService.insertResultConsoUExistEcl(resultConsoURtMap, anneeNTab, idConsoU,
					besoinSegment.getAnnee(anneeNTab), besoinTotEP, annee);

		} else {
			LOG.info("Probleme l207..");
		}

		return besoinSegment;
	}

	protected BigDecimal calcBesoinTransfert(BigDecimal compteur, Conso besoinSegment, BigDecimal dureeVie,
			String keyEcl, int anneeNTab, int annee) {

		// Calcul de la part du besoin renouvele
		BigDecimal besoinTransfert = BigDecimal.ONE;
		BigDecimal partRenouv = calcDureeVieClim(dureeVie, compteur);
		BigDecimal besoinSegmentN = besoinSegment.getAnnee(anneeNTab - 1);
		BigDecimal anneeBd = new BigDecimal(String.valueOf(annee));
		BigDecimal anneeRenovSys = (anneeBd.subtract(dureeVie)).setScale(0, BigDecimal.ROUND_HALF_UP);
		;

		if (besoinSegment.getTypeRenovSys().equals(TypeRenovSysteme.ETAT_INIT)) {
			// Test 1 : le segment est un segment initial
			// si le besoin a transferer est plus grand que le besoin de l'annee
			// n-1 alors besoinTransfert=besoinN
			if ((partRenouv.multiply(besoinSegmentN)).compareTo(besoinSegmentN) == -1) {
				besoinTransfert = partRenouv.multiply(besoinSegmentN);
			} else {
				besoinTransfert = besoinSegmentN;
			}
		}
		// si le segment a une duree de vie arrivant a echeance, alors c'est
		// tout le besoin qui est concerne
		if (besoinSegment.getAnneeRenovSys().equals(anneeRenovSys.toString())) {

			besoinTransfert = besoinSegmentN;

		}

		return besoinTransfert;
	}

	protected BigDecimal calcDureeVieClim(BigDecimal dureeVie, BigDecimal compteur) {

		BigDecimal partRenouv = BigDecimal.ZERO;
		int entier = 0;
		if (dureeVie.signum() != 0) {
			BigDecimal div = compteur.divide(dureeVie, MathContext.DECIMAL32);
			entier = div.intValue();
		}
		BigDecimal entierBd = new BigDecimal(String.valueOf(entier)).add(BigDecimal.ONE);

		BigDecimal temp = (dureeVie.multiply(entierBd)).subtract(compteur);
		BigDecimal denominateur = temp.add(BigDecimal.ONE);
		if (denominateur.compareTo(dureeVie) == 1) {
			denominateur = BigDecimal.ONE;
		}

		if (denominateur.signum() != 0) {
			partRenouv = (BigDecimal.ONE).divide(denominateur, MathContext.DECIMAL32);
		}

		return partRenouv;
	}

	private void newMapSegment(int anneeNTab, HashMap<String, Conso> besoinMap, Conso besoinSegmentNew) {
		if (besoinMap.containsKey(commonService.generateIdMapResultRt(besoinSegmentNew))) {
			Conso besoinSegmentTemp = new Conso(besoinMap.get(commonService.generateIdMapResultRt(besoinSegmentNew)));
			besoinSegmentTemp.setAnnee(anneeNTab,
					besoinSegmentTemp.getAnnee(anneeNTab).add(besoinSegmentNew.getAnnee(anneeNTab)));
			besoinMap.put(commonService.generateIdMapResultRt(besoinSegmentNew), besoinSegmentTemp);
		} else {
			besoinMap.put(commonService.generateIdMapResultRt(besoinSegmentNew), besoinSegmentNew);
		}
	}

	protected Conso besoinNewSegment(Parc parcAgreg, Conso besoinSegment, BigDecimal tauxEvolBesoin, int annee,
			int pasdeTemps, int anneeNTab, HashMap<String, ResultConsoURt> resultConsoURtMap,
			BigDecimal facteurElasExistant) {

		// besoinNew = besoinN * (1-txEvol)
		BigDecimal besoinN = BigDecimal.ZERO;
		if (besoinSegment != null && besoinSegment.getAnnee(anneeNTab - 1) != null) {
			besoinN = besoinSegment.getAnnee(anneeNTab - 1);
		}
		// Prise en compte du facteur d'elasticite du besoin
		BigDecimal besoinNew = (besoinN.multiply((tauxEvolBesoin).subtract(BigDecimal.ONE))).multiply(
				facteurElasExistant, MathContext.DECIMAL32);

		Conso newSegment = new Conso(pasdeTemps);
		// remplissage du nouvel objet Conso
		newSegment.setId(besoinSegment.getId());
		newSegment.setAnneeRenovSys(String.valueOf(annee));
		newSegment.setTypeRenovSys(TypeRenovSysteme.CHGT_SYS);
		newSegment.setAnneeRenov(parcAgreg.getAnneeRenov());
		newSegment.setTypeRenovBat(parcAgreg.getTypeRenovBat());
		newSegment.setAnnee(anneeNTab, besoinNew);

		String idConsoU = parcAgreg.getId();
		if (resultConsoURtMap.containsKey(idConsoU)) {
			BigDecimal besoinTotEP = besoinNew.multiply(FACTEUR_EP);
			initializeConsoService.insertResultConsoUExistEcl(resultConsoURtMap, anneeNTab, idConsoU, besoinNew,
					besoinTotEP, annee);

		} else {
			LOG.info("Probleme l681..");
		}

		return newSegment;
	}

	protected String generateIdTestRenov(String keyEcl, BigDecimal dureeVie, int annee) {
		BigDecimal anneeBd = new BigDecimal(String.valueOf(annee));
		BigDecimal anneeRenovSys = anneeBd.subtract(dureeVie);
		StringBuffer buffer = new StringBuffer();
		buffer.append(getIdSeg(keyEcl));
		buffer.append(anneeRenovSys.intValue());
		buffer.append(TypeRenovSysteme.CHGT_SYS);

		return buffer.toString();
	}

	protected String generateIdTestInitial(String keyEcl) {

		StringBuffer buffer = new StringBuffer();
		buffer.append(getIdSeg(keyEcl));
		buffer.append(INIT_STATE);
		buffer.append(TypeRenovSysteme.ETAT_INIT);

		return buffer.toString();
	}

	protected BigDecimal calcBesoinEvol(Parc parcAgreg) {

		BigDecimal surfN = parcAgreg.getAnnee(0);
		BigDecimal surfN1 = parcAgreg.getAnnee(1);
		BigDecimal tauxEvol = BigDecimal.ZERO;
		if (surfN.signum() != 0) {
			// BesoinEvol = surfN1/surfN
			tauxEvol = (surfN1.divide(surfN, MathContext.DECIMAL32));
		}

		return tauxEvol;
	}

	protected void batimentsNeufsEclairage(HashMap<String, ParamCoutEclVentil> coutsEclVentilMap,
			HashMap<String, Conso> coutMap, HashMap<String, ParamBesoinsNeufs> bNeufsMap, int anneeNTab,
			int pasdeTemps, int annee, String usage, HashMap<String, Conso> besoinMap, Parc parcAgreg, String keyEcl,
			int periodeCstr, int periode, HashMap<String, ResultConsoURt> resultConsoURtMap, BigDecimal facteurElasNeuf) {

		// Calcul des besoins en eclairage pour les batiments entrant dans
		// le parc et ayant ete renove en ENSBBC
		Conso besoinNeuf = new Conso(pasdeTemps);
		String concat = commonService.concatID(parcAgreg, usage);
		ParamBesoinsNeufs besoinsNeufs = bNeufsMap.get(concat);

		if (parcAgreg.getTypeRenovBat().equals(TypeRenovBati.ENSBBC)) {
			periodeCstr = commonService.correspPeriode(annee);
		}
		// Le besoin neuf est exprime en kWh/m²
		BigDecimal besoinsU = besoinsNeufs.getPeriode(periodeCstr).multiply(facteurElasNeuf, MathContext.DECIMAL32);

		besoinNeuf.setId(getIdSegNeuf(keyEcl));
		besoinNeuf.setAnneeRenovSys(String.valueOf(annee));
		besoinNeuf.setTypeRenovSys(TypeRenovSysteme.CHGT_SYS);
		besoinNeuf.setAnneeRenov(parcAgreg.getAnneeRenov());
		besoinNeuf.setTypeRenovBat(parcAgreg.getTypeRenovBat());
		// besoinNeuf =
		// parcAgreg.getAnnee(1)*besoinU
		BigDecimal besoinTot = parcAgreg.getAnnee(1).multiply(besoinsU);
		besoinNeuf.setAnnee(anneeNTab, besoinTot);

		besoinMap.put(commonService.generateIdMapResultRt(besoinNeuf), besoinNeuf);

		if (parcAgreg.getTypeRenovBat().equals(TypeRenovBati.ENSBBC)) {
			// calcul du cout
			Conso coutNeuf = new Conso(pasdeTemps);
			coutNeuf.setId(getIdSegNeuf(keyEcl));
			coutNeuf.setAnneeRenovSys(String.valueOf(annee));
			coutNeuf.setTypeRenovSys(TypeRenovSysteme.CHGT_SYS);
			coutNeuf.setAnneeRenov(parcAgreg.getAnneeRenov());
			coutNeuf.setTypeRenovBat(parcAgreg.getTypeRenovBat());
			BigDecimal coutUnit = coutsEclVentilMap.get(Usage.ECLAIRAGE.getLabel() + besoinNeuf.getIdbranche())
					.getCout();
			BigDecimal coutTot = parcAgreg.getAnnee(1).multiply(coutUnit);
			coutNeuf.setAnnee(anneeNTab, coutTot);
			coutMap.put(commonService.generateIdMapResultRt(coutNeuf), coutNeuf);
		}

		String idConsoU = parcAgreg.getId();
		if (resultConsoURtMap.containsKey(idConsoU)) {
			BigDecimal besoinTotEP = besoinTot.multiply(FACTEUR_EP);
			initializeConsoService.insertResultConsoUExistEcl(resultConsoURtMap, anneeNTab, idConsoU, besoinTot,
					besoinTotEP, annee);

		} else {
			LOG.info("Probleme Eclairage l393..");
		}

	}

	protected String getIdSegNeuf(String keyEcl) {

		String id = keyEcl.substring(START_ID_SEG, START_ID_SEG + LENGHT_ID_SEG_NEUF) + Energies.ELECTRICITE.getCode();
		return id;

	}

	protected HashMap<String, List<String>> idAgregListExist(HashMap<String, Conso> besoinMap,
			HashMap<String, Parc> parcAgreg) {

		HashMap<String, List<String>> keyMap = new HashMap<String, List<String>>();
		List<String> listKey = new ArrayList<String>();
		for (String parcKey : parcAgreg.keySet()) {
			listKey = new ArrayList<String>();
			for (String besoinKey : besoinMap.keySet()) {
				Conso besoin = besoinMap.get(besoinKey);
				String keyTest = besoin.getIdagreg() + besoin.getAnneeRenov() + besoin.getTypeRenovBat();
				if (keyTest.equals(parcKey)) {

					listKey.add(besoinKey);
				}

			}
			keyMap.put(parcKey, listKey);

		}
		return keyMap;

	}

	// Genere les id des segments a partir des id de la Map
	protected String getIdSeg(String keyClim) {

		String id = keyClim.substring(START_ID_SEG, START_ID_SEG + LENGHT_ID_SEG_TOT);

		return id;
	}

	protected HashMap<String, List<String>> idAgregListNeuf(HashMap<String, Parc> parcTotMap, int annee) {

		HashMap<String, List<String>> keyMap = new HashMap<String, List<String>>();
		List<String> listKey = new ArrayList<String>();
		for (String parcKey : parcTotMap.keySet()) {
			listKey = new ArrayList<String>();
			Parc parc = parcTotMap.get(parcKey);
			String key = parc.getIdagreg() + parc.getAnneeRenov() + parc.getTypeRenovBat();

			// Test : les segments retenus sont ceux construits apres 2009

			if (parc.getAnnee(0) == null || parc.getAnnee(0).signum() == 0) {
				if (!keyMap.containsKey(key)) {
					listKey.add(parcKey + String.valueOf(annee) + TypeRenovBati.ETAT_INIT);

				}

				keyMap.put(parcKey, listKey);

			}
		}
		return keyMap;

	}

}
