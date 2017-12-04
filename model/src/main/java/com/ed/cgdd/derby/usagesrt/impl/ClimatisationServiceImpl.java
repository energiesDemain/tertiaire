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
import com.ed.cgdd.derby.model.calcconso.ParamRatioAux;
import com.ed.cgdd.derby.model.calcconso.ParamRdtCout;
import com.ed.cgdd.derby.model.calcconso.ResultConsoRt;
import com.ed.cgdd.derby.model.calcconso.ResultConsoUClim;
import com.ed.cgdd.derby.model.parc.Energies;
import com.ed.cgdd.derby.model.parc.MapResultsKeys;
import com.ed.cgdd.derby.model.parc.Parc;
import com.ed.cgdd.derby.model.parc.Period;
import com.ed.cgdd.derby.model.parc.TypeRenovBati;
import com.ed.cgdd.derby.model.parc.TypeRenovSysteme;
import com.ed.cgdd.derby.model.parc.Usage;
import com.ed.cgdd.derby.model.parc.EvolBesoinMap;
import com.ed.cgdd.derby.process.InitializeConsoService;
import com.ed.cgdd.derby.usagesrt.ClimatisationService;

public class ClimatisationServiceImpl implements ClimatisationService {
	private final static Logger LOG = LogManager.getLogger(ClimatisationServiceImpl.class);
	private CommonService commonService;
	private InitializeConsoService initializeConsoService;
	private static final int START_ID_BRANCHE = 0;
	private static final int LENGTH_ID_BRANCHE = 2;
	private static final int START_ID_RDT = 0;
	private static final int LENGTH_ID_RDT = 6;
	private static final int START_SYS_FROID = 12;
	private static final int LENGTH_SYS_FROID = 2;
	private static final int START_ID_SEG = 0;
	private static final int LENGTH_ID_SEG_AGREG2 = 14;
	private static final int LENGTH_ID_SEG_TOT = 16;
	private final static String CODE_CLIM = "01";
	private final static String CODE_NON_CLIM = "02";
	private final static BigDecimal FACTEUR_EP = new BigDecimal("2.58");

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
	public ResultConsoRt evolClimatisationConso(HashMap<String, ResultConsoUClim> resultConsoUClimMap,
			HashMap<String, ParamRatioAux> auxFroid, HashMap<String, Parc> parcTotMap, ResultConsoRt resultatsConsoRt,
			HashMap<String, ParamBesoinsNeufs> bNeufsMap, HashMap<String, BigDecimal> dvUsagesMap,
			HashMap<String, ParamRdtCout> rdtCoutClimMap, int anneeNTab, int pasdeTemps, int annee,
			BigDecimal compteur, String usage, HashMap<String, BigDecimal[]> elasticiteNeufMap,
			HashMap<String, BigDecimal[]> elasticiteExistantMap, EvolBesoinMap evolBesoinMap) {

		// Initialisation des objets
		HashMap<String, Conso> besoinMap = resultatsConsoRt.getMap(MapResultsKeys.BESOIN_CLIM.getLabel());
		HashMap<String, Conso> rdtMapClim = resultatsConsoRt.getMap(MapResultsKeys.RDT_CLIM.getLabel());
		HashMap<String, Conso> consoMap = resultatsConsoRt.getMap(MapResultsKeys.CONSO_CLIM.getLabel());
		HashMap<String, Conso> coutMap = resultatsConsoRt.getMap(MapResultsKeys.COUT_CLIM.getLabel());

		// Agregation du parc selon les 8 premiers ID (zone, branche,
		// ss_branche, bat_type, occupant,periode_simple, periode_detail,
		// sysFroid) ainsi anneeRenov + typeRenovBat
		HashMap<String, Parc> parcTotAgregMap = commonService.aggregateParcConsoU(resultConsoUClimMap, parcTotMap,
				anneeNTab, pasdeTemps);
		HashMap<String, Conso> besoinTotAgregMap = commonService.aggregateBesoinClim(besoinMap, anneeNTab, pasdeTemps);

		// construction de la liste contenant pour chaque idAgregParc toutes les
		// combinaisons contenues dans besoinMap
		HashMap<String, List<String>> keyMap = idAgregListClim(besoinMap, parcTotAgregMap);
		HashMap<String, List<String>> keyMapNeuf = idAgregListNeuf(parcTotAgregMap, annee, anneeNTab);
		// Parcours chaque segment de parc agrege
		for (String idAgregParc : parcTotAgregMap.keySet()) {

			Parc parcAgreg = parcTotAgregMap.get(idAgregParc);
			Conso besoinAgregCout = besoinTotAgregMap.get(idAgregParc);
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
			for (String keyClim : listBoucle) {

				String eqFroid = keyClim.substring(START_SYS_FROID, START_SYS_FROID + LENGTH_SYS_FROID);
				if (parcAgreg.getAnnee(0) == null || parcAgreg.getAnnee(0).signum() == 0) {
					// Si le parc a l'annee n-1 est null ou bien egal a 0
					// alors
					// le segment correspond a des batiments neufs
					// le taux de climatisation des batiments neufs est deja
					// pris en compte lors de l'evolution du parc
					// Parcours toutes les cles de la liste, afin de prendre
					// en
					// compte tous les segments de parc (idAgregParc +
					// systeme
					// froid + energie Clim)

					if (eqFroid.equals(CODE_CLIM)) {
						// Cas ou un batiment est climatise
						batimentsNeufsClim(resultConsoUClimMap, auxFroid, bNeufsMap, rdtCoutClimMap, anneeNTab,
								pasdeTemps, annee, usage, besoinMap, rdtMapClim, consoMap, parcAgreg, keyClim,
								periodeCstr, periode, elasticiteNeufMap);
						// Les batiments non climatises ne sont donc pas
						// pris en
						// compte

					}
				} else {

					if (eqFroid.equals(CODE_CLIM)) {
						// cas d'un segment existant climatise
						batimentsExistantsClim(besoinAgregCout, resultConsoUClimMap, auxFroid, dvUsagesMap,
								rdtCoutClimMap, anneeNTab, pasdeTemps, annee, compteur, besoinMap, rdtMapClim,
								consoMap, keyClim, eqFroid, parcAgreg, periodeCstr, periode, usage, coutMap,
								elasticiteExistantMap,evolBesoinMap);
					}
				}
			}

		}
		// Encapsule les map de resultats
		resultatsConsoRt.put(MapResultsKeys.BESOIN_CLIM.getLabel(), besoinMap);
		resultatsConsoRt.put(MapResultsKeys.RDT_CLIM.getLabel(), rdtMapClim);
		resultatsConsoRt.put(MapResultsKeys.CONSO_CLIM.getLabel(), consoMap);
		resultatsConsoRt.put(MapResultsKeys.COUT_CLIM.getLabel(), coutMap);

		return resultatsConsoRt;
	}

	protected void batimentsExistantsClim(Conso besoinAgregCout, HashMap<String, ResultConsoUClim> resultConsoUClimMap,
			HashMap<String, ParamRatioAux> auxFroid, HashMap<String, BigDecimal> dvClimMap,
			HashMap<String, ParamRdtCout> rdtCoutClimMap, int anneeNTab, int pasdeTemps, int annee,
			BigDecimal compteur, HashMap<String, Conso> besoinMap, HashMap<String, Conso> rdtMapClim,
			HashMap<String, Conso> consoMap, String keyClim, String sysFroid, Parc parcAgreg, int periodeCstr,
			int periode, String usage, HashMap<String, Conso> coutMap,
			HashMap<String, BigDecimal[]> elasticiteExistantMap, EvolBesoinMap evolBesoinMap) {

		Conso besoinSegment = besoinMap.get(keyClim);
		Conso consoSegment = consoMap.get(keyClim);
		Conso rdtSegment = rdtMapClim.get(keyClim);
		if (besoinSegment != null) {
			// Calcul de la duree de vie
			BigDecimal dureeVie = BigDecimal.ZERO;
			if (dvClimMap.get(usage) != null) {
				dureeVie = dvClimMap.get(usage);
			}

			// Taux d'evolution du besoin uniquement du fait de
			// l'evolution des surfaces
			BigDecimal tauxEvolBesoin = calcBesoinEvol(parcAgreg).setScale(2, BigDecimal.ROUND_HALF_UP);

			// Generation des id a tester pour savoir si une renovation
			// doit etre menee ou non
			String idTestRenov = generateIdTestRenov(keyClim, dureeVie, annee);
			String idTestInit = generateIdTestInitial(keyClim);

			// si besoin Evol est superieur a 1, alors un nouveau
			// segment est creee
			if (tauxEvolBesoin.compareTo(BigDecimal.ONE) == 1) {
				// Creation des besoins pour un nouveau segment
				Conso besoinSegmentNew = besoinNewSegment(besoinSegment, tauxEvolBesoin, annee, pasdeTemps, anneeNTab,
						elasticiteExistantMap,  evolBesoinMap);
				newMapSegment(anneeNTab, besoinMap, besoinSegmentNew);
				// Creation des rendements a associer au nouveau segment
				Conso rdtSegmentNew = rdtNeufClim(besoinSegmentNew, rdtCoutClimMap, pasdeTemps, anneeNTab, keyClim,
						annee, periode);
				rdtMapClim.put(commonService.generateIdMapResultRt(besoinSegmentNew), rdtSegmentNew);
				// Creation des consommations a associer au nouveau
				// segment
				Conso consoSegmentNew = consoNeufClim(resultConsoUClimMap, besoinSegmentNew, rdtSegmentNew, pasdeTemps,
						anneeNTab, annee);
				newMapSegment(anneeNTab, consoMap, consoSegmentNew);

				String idConsoU = consoSegmentNew.getId().substring(START_ID_SEG, START_ID_SEG + LENGTH_ID_SEG_AGREG2)
						+ consoSegment.getAnneeRenov() + consoSegment.getTypeRenovBat();
				if (resultConsoUClimMap.containsKey(idConsoU)) {
					BigDecimal consoTotEP = consoSegmentNew.getAnnee(anneeNTab).multiply(FACTEUR_EP);
					initializeConsoService.insertResultConsoUExistClim(resultConsoUClimMap, anneeNTab, idConsoU,
							consoSegmentNew.getAnnee(anneeNTab), consoTotEP, annee);

				} else {
					LOG.info("Probleme Climatisation l187..");
				}

			}
			// Test pour savoir sur une renovation va avoir lieu sur le
			// segment considere
			if (!keyClim.equals(idTestRenov) && !keyClim.equals(idTestInit)) {
				// Cas d'un segment n'ayant pas a faire de renovation
				// les besoins evoluent donc uniquement en fonction des
				// surfaces (entrees et sorties)
				// si des transferts d'energies doivent avoir lieu vers
				// ces segments alors ils le sont dans la seconde partie
				// de la condition
				if (tauxEvolBesoin.compareTo(BigDecimal.ONE) == 1) {
					tauxEvolBesoin = BigDecimal.ONE;
				}
				BigDecimal besoinNTemp = BigDecimal.ONE;
				if (besoinSegment.getAnnee(anneeNTab - 1) != null) {
					// Le besoin est multiplie par un facteur d'elasticite
					besoinNTemp = besoinSegment.getAnnee(anneeNTab - 1)
							.multiply(
									elasticiteExistantMap.get(Usage.CLIMATISATION.getLabel()
											+ Energies.ELECTRICITE.getCode())[annee - 2009], MathContext.DECIMAL32);

					// Modification du besoin Evol pour adaptation CC
					BigDecimal evolBesoinClim = 
							evolBesoinMap.getEvolBesoin()
							.get(besoinSegment.getId().substring(START_ID_BRANCHE,LENGTH_ID_BRANCHE)+Usage.CLIMATISATION+annee)
							.getEvolution();
					besoinNTemp = besoinNTemp.multiply(BigDecimal.ONE.add(evolBesoinClim),MathContext.DECIMAL32);
					
				
				}
				besoinSegment.setAnnee(anneeNTab, tauxEvolBesoin.multiply(besoinNTemp));
				besoinMap.put(keyClim, besoinSegment);
				// Les rendements n'evoluent pas
				rdtSegment = calcRdtConstant(besoinSegment, rdtSegment, anneeNTab);
				rdtMapClim.put(keyClim, rdtSegment);
				// Les consommations evoluent dans les memes proportions
				// que les besoins dans la mesure ou les rendements sont
				// constants
				besoinNTemp = BigDecimal.ONE;
				if (consoSegment.getAnnee(anneeNTab - 1) != null) {
					besoinNTemp = consoSegment.getAnnee(anneeNTab - 1);
				}
				consoSegment.setAnnee(anneeNTab, tauxEvolBesoin.multiply(besoinNTemp));
				consoMap.put(keyClim, consoSegment);

				String idConsoU = consoSegment.getId().substring(START_ID_SEG, START_ID_SEG + LENGTH_ID_SEG_AGREG2)
						+ consoSegment.getAnneeRenov() + consoSegment.getTypeRenovBat();
				if (resultConsoUClimMap.containsKey(idConsoU)) {
					BigDecimal consoTotEP = consoSegment.getAnnee(anneeNTab).multiply(FACTEUR_EP);
					initializeConsoService.insertResultConsoUExistClim(resultConsoUClimMap, anneeNTab, idConsoU,
							consoSegment.getAnnee(anneeNTab), consoTotEP, annee);

				} else {
					LOG.info("Probleme Climatisation l229..");
				}
			} else {

				if (tauxEvolBesoin.compareTo(BigDecimal.ONE) == 1) {
					tauxEvolBesoin = BigDecimal.ONE;
				}
				// Cas ou les segments doivent faire l'objet de travaux
				// de renovation
				// Calcul des besoins qui vont sortir du segment
				BigDecimal besoinSortant = calcBesoinTransfert(compteur, besoinSegment, dureeVie, keyClim, anneeNTab,
						annee);

				// Calcul du nouveau besoin pour le segment pre-existant
				// ou ayant deja subit une renovation
				besoinSegment = besoinExistantModif(besoinSegment, tauxEvolBesoin, besoinSortant, anneeNTab,
						elasticiteExistantMap, annee, evolBesoinMap);
				besoinMap.put(keyClim, besoinSegment);
				// Les rendements n'evoluent pas
				rdtSegment = calcRdtConstant(besoinSegment, rdtSegment, anneeNTab);
				rdtMapClim.put(keyClim, rdtSegment);
				// Les consommations sont recalculees de la meme facon
				// que les besoins, les rendements etant constants
				consoSegment = consoExistantModif(consoSegment, besoinSegment, rdtSegment, anneeNTab);
				consoMap.put(keyClim, consoSegment);

				String idConsoU = consoSegment.getId().substring(START_ID_SEG, START_ID_SEG + LENGTH_ID_SEG_AGREG2)
						+ consoSegment.getAnneeRenov() + consoSegment.getTypeRenovBat();
				if (resultConsoUClimMap.containsKey(idConsoU)) {
					BigDecimal consoTotEP = consoSegment.getAnnee(anneeNTab).multiply(FACTEUR_EP);
					initializeConsoService.insertResultConsoUExistClim(resultConsoUClimMap, anneeNTab, idConsoU,
							consoSegment.getAnnee(anneeNTab), consoTotEP, annee);

				} else {
					LOG.info("Probleme Climatisation l261..");
				}
				// Creation des segments vers lesquels les besoins sont
				// transferes
				// Les parts de marche des systemes sont consideres constants
				// Les systemes sortants sont donc remplaces par des systemes
				// identiques mais plus performants
				besoinMap = besoinModifTrans(keyClim, besoinMap, besoinSortant, annee, anneeNTab, pasdeTemps,
						elasticiteExistantMap, evolBesoinMap);
				Conso rdt = rdtNeufClim(besoinMap.get(generateIDNew(keyClim, annee)), rdtCoutClimMap, pasdeTemps,
						anneeNTab, keyClim, annee, periode);
				rdtMapClim.put(generateIDNew(keyClim, annee), rdt);
				Conso conso = consoNeufClim(resultConsoUClimMap, besoinMap.get(generateIDNew(keyClim, annee)), rdt,
						pasdeTemps, anneeNTab, annee);
				consoMap.put(generateIDNew(keyClim, annee), conso);
				Conso cout = coutNeufClim(parcAgreg, besoinSegment, besoinSortant, besoinAgregCout, rdtCoutClimMap,
						pasdeTemps, anneeNTab, keyClim, annee, periode);
				coutMap.put(generateIDNew(keyClim, annee), cout);
			}
		}

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

	protected HashMap<String, Conso> besoinModifTrans(String keyClim, HashMap<String, Conso> besoinMap,
			BigDecimal besoinEntrant, int annee, int anneeNTab, int pasdeTemps,
			HashMap<String, BigDecimal[]> elasticiteExistantMap, EvolBesoinMap evolBesoinMap) {
		// Le besoin est modifie pour inclure le facteur d'elasticite
		BigDecimal besoinModif = besoinEntrant
				.multiply(
						elasticiteExistantMap.get(Usage.CLIMATISATION.getLabel() + Energies.ELECTRICITE.getCode())[annee - 2009],
						MathContext.DECIMAL32);

		// Modification du besoin Evol pour adaptation CC
				BigDecimal evolBesoinClim = 
						evolBesoinMap.getEvolBesoin()
						.get(keyClim.substring(START_ID_BRANCHE,LENGTH_ID_BRANCHE)+Usage.CLIMATISATION+annee)
						.getEvolution();
				besoinModif = besoinModif.multiply(BigDecimal.ONE.add(evolBesoinClim),MathContext.DECIMAL32);
				
				// affiche les elements de calcul du besoin modif
//				LOG.debug("keyClim {} elas {} evol {} besoinUi {}  besoinUmod {}  ",
//						keyClim,
//						elasticiteExistantMap.get(Usage.CLIMATISATION.getLabel() + Energies.ELECTRICITE.getCode())[annee - 2009],
//						evolBesoinClim, besoinEntrant, besoinModif);
//				
		
		if (!besoinMap.containsKey(generateIDNew(keyClim, annee))) {

			// si le segment n'existe pas :
			Conso besoinSegment = new Conso(pasdeTemps);
			besoinSegment.setId(getIdSeg(keyClim));
			besoinSegment.setAnneeRenovSys(String.valueOf(annee));
			besoinSegment.setTypeRenovSys(TypeRenovSysteme.CHGT_SYS);
			besoinSegment.setAnneeRenov(INIT_STATE);
			besoinSegment.setTypeRenovBat(TypeRenovBati.ETAT_INIT);
			besoinSegment.setAnnee(anneeNTab, besoinModif);
			besoinMap.put(generateIDNew(keyClim, annee), besoinSegment);

		} else {

			Conso besoinSegment = besoinMap.get(generateIDNew(keyClim, annee));

			if (besoinSegment.getAnnee(anneeNTab) != null) {
				besoinSegment.setAnnee(anneeNTab, besoinModif.add(besoinSegment.getAnnee(anneeNTab)));
			} else {

				besoinSegment.setAnnee(anneeNTab, besoinModif);
			}
			besoinMap.put(generateIDNew(keyClim, annee), besoinSegment);
		}

		return besoinMap;
	}

	protected String generateIDNew(String keyClim, int annee) {

		StringBuffer buffer = new StringBuffer();
		buffer.append(getIdSeg(keyClim));
		buffer.append(String.valueOf(annee));
		buffer.append(TypeRenovSysteme.CHGT_SYS);
		buffer.append(INIT_STATE);
		buffer.append(TypeRenovBati.ETAT_INIT);

		return buffer.toString();

	}

	protected Conso consoExistantModif(Conso consoSegment, Conso besoinSegment, Conso rdtSegment, int anneeNTab) {

		BigDecimal conso = BigDecimal.ZERO;
		if (rdtSegment != null && rdtSegment.getAnnee(anneeNTab).signum() != 0) {
			conso = besoinSegment.getAnnee(anneeNTab).divide(rdtSegment.getAnnee(anneeNTab), MathContext.DECIMAL32);

		}

		consoSegment.setAnnee(anneeNTab, conso);

		return consoSegment;

	}

	protected Conso besoinExistantModif(Conso besoinSegment, BigDecimal tauxEvolBesoin, BigDecimal besoinSortant,
			int anneeNTab, HashMap<String, BigDecimal[]> elasticiteExistantMap, int annee, EvolBesoinMap evolBesoinMap) {

		// Formule : besoinSegment = besoinEvol - besoinSortant
		// Test si le resultat est negatif on affecte 0
		BigDecimal besoinEvol = besoinSegment.getAnnee(anneeNTab - 1).multiply(tauxEvolBesoin);
		if ((besoinEvol.subtract(besoinSortant)).signum() == -1) {
			besoinSegment.setAnnee(anneeNTab, BigDecimal.ZERO);
		} else {
			// Le besoin n'etant pas modifie est multiplie par le facteur
			// d'elasticite
			BigDecimal besoinTemp = (besoinEvol.subtract(besoinSortant))
					.multiply(
							elasticiteExistantMap.get(Usage.CLIMATISATION.getLabel() + Energies.ELECTRICITE.getCode())[annee - 2009],
							MathContext.DECIMAL32);
			
			// Modification du besoin Evol pour adaptation CC
			BigDecimal evolBesoinClim = 
					evolBesoinMap.getEvolBesoin()
					.get(besoinSegment.getId().substring(START_ID_BRANCHE,LENGTH_ID_BRANCHE)+Usage.CLIMATISATION+annee)
					.getEvolution();
			 besoinTemp =  besoinTemp.multiply(BigDecimal.ONE.add(evolBesoinClim),MathContext.DECIMAL32);
			 besoinSegment.setAnnee(anneeNTab, besoinTemp);
		}

		return besoinSegment;
	}

	protected BigDecimal calcBesoinTransfert(BigDecimal compteur, Conso besoinSegment, BigDecimal dureeVie,
			String keyClim, int anneeNTab, int annee) {

		// Calcul de la part du besoin renouvele
		BigDecimal besoinTransfert = BigDecimal.ONE;
		BigDecimal partRenouv = calcDureeVieClim(dureeVie, compteur);
		BigDecimal besoinSegmentN = besoinSegment.getAnnee(anneeNTab - 1);

		if (keyClim.equals(generateIdTestInitial(keyClim))) {
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
		if (keyClim.equals(generateIdTestRenov(keyClim, dureeVie, annee))) {

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

	protected Conso calcRdtConstant(Conso besoinSegment, Conso rdtSegment, int anneeNTab) {
		BigDecimal rdtN = BigDecimal.ZERO;
		if (rdtSegment != null && rdtSegment.getAnnee(anneeNTab - 1) != null) {
			rdtN = rdtSegment.getAnnee(anneeNTab - 1);
		}
		if (besoinSegment.getAnnee(anneeNTab).signum() == 0) {
			rdtSegment.setAnnee(anneeNTab, BigDecimal.ZERO);
		} else {
			rdtSegment.setAnnee(anneeNTab, rdtN);
		}
		return rdtSegment;
	}

	protected Conso besoinNewSegment(Conso besoinSegment, BigDecimal tauxEvolBesoin, int annee, int pasdeTemps,
			int anneeNTab, HashMap<String, BigDecimal[]> elasticiteExistantMap, EvolBesoinMap evolBesoinMap) {

		// besoinNew = besoinN * (1-txEvol)
		BigDecimal besoinN = BigDecimal.ZERO;
		if (besoinSegment.getAnnee(anneeNTab - 1) != null) {
			besoinN = besoinSegment.getAnnee(anneeNTab - 1);
		}
		// Le besoin est multiplie par le facteur d'elasticite
		BigDecimal besoinNew = besoinN
				.multiply((tauxEvolBesoin).subtract(BigDecimal.ONE))
				.multiply(
						elasticiteExistantMap.get(Usage.CLIMATISATION.getLabel() + Energies.ELECTRICITE.getCode())[annee - 2009],
						MathContext.DECIMAL32);
		
		// Modification du besoin Evol pour adaptation CC
		BigDecimal evolBesoinClim = 
				evolBesoinMap.getEvolBesoin()
				.get(besoinSegment.getId().substring(START_ID_BRANCHE,LENGTH_ID_BRANCHE)+Usage.CLIMATISATION+annee)
				.getEvolution();
		besoinNew = besoinNew.multiply(BigDecimal.ONE.add(evolBesoinClim),MathContext.DECIMAL32);
		
		Conso newSegment = new Conso(pasdeTemps);
		// remplissage du nouvel objet Conso
		newSegment.setId(besoinSegment.getId());
		newSegment.setAnneeRenovSys(String.valueOf(annee));
		newSegment.setTypeRenovSys(TypeRenovSysteme.CHGT_SYS);
		newSegment.setAnneeRenov(besoinSegment.getAnneeRenov());
		newSegment.setTypeRenovBat(besoinSegment.getTypeRenovBat());
		newSegment.setAnnee(anneeNTab, besoinNew);

		return newSegment;
	}

	protected String generateIdTestRenov(String keyClim, BigDecimal dureeVie, int annee) {
		BigDecimal anneeBd = new BigDecimal(String.valueOf(annee));
		BigDecimal anneeRenovSys = anneeBd.subtract(dureeVie);
		StringBuffer buffer = new StringBuffer();
		buffer.append(getIdSeg(keyClim));
		buffer.append(anneeRenovSys.intValue());
		buffer.append(TypeRenovSysteme.CHGT_SYS);
		buffer.append(INIT_STATE);
		buffer.append(TypeRenovBati.ETAT_INIT);

		return buffer.toString();
	}

	protected String generateIdTestInitial(String keyClim) {

		StringBuffer buffer = new StringBuffer();
		buffer.append(getIdSeg(keyClim));
		buffer.append(INIT_STATE);
		buffer.append(TypeRenovSysteme.ETAT_INIT);
		buffer.append(INIT_STATE);
		buffer.append(TypeRenovBati.ETAT_INIT);

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

	protected void batimentsNeufsClim(HashMap<String, ResultConsoUClim> resultConsoUClimMap,
			HashMap<String, ParamRatioAux> auxFroid, HashMap<String, ParamBesoinsNeufs> bNeufsMap,
			HashMap<String, ParamRdtCout> rdtCoutClimMap, int anneeNTab, int pasdeTemps, int annee, String usage,
			HashMap<String, Conso> besoinMap, HashMap<String, Conso> rdtMapClim, HashMap<String, Conso> consoMap,
			Parc parcAgreg, String keyClim, int periodeCstr, int periode,
			HashMap<String, BigDecimal[]> elasticiteNeufMap) {

		// Calcul des besoins en climatisation pour le nouveau segment
		Conso besoinNeuf = besoinNeufClim(auxFroid, parcAgreg, bNeufsMap, usage, periodeCstr, pasdeTemps, anneeNTab,
				keyClim, annee, elasticiteNeufMap);
		// Calcul des rendements des systemes de climatisation pour le
		// nouveau segment
		Conso rdt = rdtNeufClim(besoinNeuf, rdtCoutClimMap, pasdeTemps, anneeNTab, keyClim, annee, periode);
		// Calcul des consommations de climatisation pour le nouveau segment
		Conso consoNeuf = consoNeufClim(resultConsoUClimMap, besoinNeuf, rdt, pasdeTemps, anneeNTab, annee);

		if (rdt.getAnnee(anneeNTab) != null && rdt.getAnnee(anneeNTab).signum() != 0) {
			rdtMapClim.put(commonService.generateIdMapResultRt(rdt), rdt);
		}
		if (besoinNeuf.getAnnee(anneeNTab) != null && besoinNeuf.getAnnee(anneeNTab).signum() != 0) {
			besoinMap.put(commonService.generateIdMapResultRt(besoinNeuf), besoinNeuf);
		}
		if (consoNeuf.getAnnee(anneeNTab) != null && consoNeuf.getAnnee(anneeNTab).signum() != 0) {
			consoMap.put(commonService.generateIdMapResultRt(consoNeuf), consoNeuf);
		}

	}

	protected Conso consoNeufClim(HashMap<String, ResultConsoUClim> resultConsoUClimMap, Conso besoinSegment,
			Conso rdt, int pasdeTemps, int anneeNTab, int annee) {

		Conso consoNeuf = new Conso(pasdeTemps);
		BigDecimal conso = BigDecimal.ZERO;
		if (rdt != null && rdt.getAnnee(anneeNTab).signum() != 0) {
			conso = besoinSegment.getAnnee(anneeNTab).divide(rdt.getAnnee(anneeNTab), MathContext.DECIMAL32);

		}
		consoNeuf.setId(besoinSegment.getId());
		consoNeuf.setAnneeRenovSys(String.valueOf(annee));
		consoNeuf.setTypeRenovSys(TypeRenovSysteme.CHGT_SYS);
		consoNeuf.setAnneeRenov(besoinSegment.getAnneeRenov());
		consoNeuf.setTypeRenovBat(besoinSegment.getTypeRenovBat());
		consoNeuf.setAnnee(anneeNTab, conso);

		String idConsoU = consoNeuf.getId().substring(START_ID_SEG, START_ID_SEG + LENGTH_ID_SEG_AGREG2)
				+ consoNeuf.getAnneeRenov() + consoNeuf.getTypeRenovBat();
		if (resultConsoUClimMap.containsKey(idConsoU)) {
			BigDecimal consoTotEP = consoNeuf.getAnnee(anneeNTab).multiply(FACTEUR_EP);
			initializeConsoService.insertResultConsoUExistClim(resultConsoUClimMap, anneeNTab, idConsoU,
					consoNeuf.getAnnee(anneeNTab), consoTotEP, annee);

		} else {
			LOG.info("Probleme Climatisation l495..");
		}

		return consoNeuf;

	}

	protected Conso coutNeufClim(Parc parcAgreg, Conso besoinSegment, BigDecimal besoinSortant, Conso besoinAgreg,
			HashMap<String, ParamRdtCout> rdtCoutClimMap, int pasdeTemps, int anneeNTab, String keyClim, int annee,
			int periode) {

		Conso coutSegment = new Conso(pasdeTemps);
		BigDecimal coutClim = rdtCoutClimMap.get(generateRdtCoutClimId(keyClim, periode)).getCout();
		BigDecimal partBesoin = BigDecimal.ZERO;
		// Determination de la part du besoin total qui evolue
		if (besoinAgreg != null && besoinAgreg.getAnnee(0) != null && besoinAgreg.getAnnee(0).signum() != 0) {
			partBesoin = besoinSortant.divide(besoinAgreg.getAnnee(0), MathContext.DECIMAL32);
		}
		// Calcul de la surface touchee par le changement de systeme
		BigDecimal surfTrans = BigDecimal.ZERO;
		if (parcAgreg != null && parcAgreg.getAnnee(0) != null) {
			surfTrans = partBesoin.multiply(parcAgreg.getAnnee(0));
		}
		BigDecimal coutInsert = coutClim.multiply(surfTrans);
		coutSegment.setId(getIdSeg(keyClim));
		coutSegment.setAnneeRenovSys(String.valueOf(annee));
		coutSegment.setTypeRenovSys(TypeRenovSysteme.CHGT_SYS);
		coutSegment.setAnneeRenov(besoinSegment.getAnneeRenov());
		coutSegment.setTypeRenovBat(besoinSegment.getTypeRenovBat());
		coutSegment.setAnnee(anneeNTab, coutInsert);
		return coutSegment;
	}

	protected Conso rdtNeufClim(Conso besoinSegment, HashMap<String, ParamRdtCout> rdtCoutClimMap, int pasdeTemps,
			int anneeNTab, String keyClim, int annee, int periode) {

		Conso rdtClimSegment = new Conso(pasdeTemps);
		ParamRdtCout idRdtCoutClim = rdtCoutClimMap.get(generateRdtCoutClimId(keyClim, periode));

		rdtClimSegment.setId(getIdSeg(keyClim));
		rdtClimSegment.setAnneeRenovSys(String.valueOf(annee));
		rdtClimSegment.setTypeRenovSys(TypeRenovSysteme.CHGT_SYS);
		rdtClimSegment.setAnneeRenov(besoinSegment.getAnneeRenov());
		rdtClimSegment.setTypeRenovBat(besoinSegment.getTypeRenovBat());
		if (besoinSegment.getAnnee(anneeNTab).signum() == 0) {
			rdtClimSegment.setAnnee(anneeNTab, BigDecimal.ZERO);
		} else {
			rdtClimSegment.setAnnee(anneeNTab, idRdtCoutClim.getRdt());
		}
		return rdtClimSegment;
	}

	protected Conso besoinNeufClim(HashMap<String, ParamRatioAux> auxFroid, Parc parcAgregEntrant,
			HashMap<String, ParamBesoinsNeufs> bNeufsMap, String usage, int periodeCstr, int pasdeTemps, int anneeNTab,
			String keyClim, int annee, HashMap<String, BigDecimal[]> elasticiteNeufMap) {

		// Calcul des besoins en climatisation pour les batiments entrant dans
		// le parc et etant climatises
		// Les taux de climatisation des batiments entrants a deja ete pris en
		// compte lors de l'evolution du parc pour l'annee N
		Conso besoinNeuf = new Conso(pasdeTemps);
		String concat = commonService.concatID(parcAgregEntrant, usage);
		ParamBesoinsNeufs besoinsNeufs = bNeufsMap.get(concat);
		// Le besoin neuf est exprime en kWh/m² climatise
		// il est multiplie par un facteur d'elasticite
		BigDecimal besoinsU = besoinsNeufs.getPeriode(periodeCstr).multiply(
				elasticiteNeufMap.get(Usage.CLIMATISATION.getLabel() + Energies.ELECTRICITE.getCode())[annee - 2009]);
		BigDecimal besoinAux = besoinsU.multiply(auxFroid.get(CODE_CLIM).getRatio());
		// Ajout des auxiliaires
		besoinsU = besoinsU.add(besoinAux);
		besoinNeuf.setId(getIdSeg(keyClim));
		besoinNeuf.setAnneeRenovSys(String.valueOf(annee));
		besoinNeuf.setTypeRenovSys(TypeRenovSysteme.CHGT_SYS);
		besoinNeuf.setAnneeRenov(INIT_STATE);
		besoinNeuf.setTypeRenovBat(TypeRenovBati.ETAT_INIT);

		// besoinNeuf =
		// parcAgreg.getAnnee(1)*besoinU
		besoinNeuf.setAnnee(anneeNTab, parcAgregEntrant.getAnnee(1).multiply(besoinsU));
		return besoinNeuf;
	}

	// Genere les Id d'extraction des rdts de la hashMap
	protected String generateRdtCoutClimId(String keyClim, int periode) {

		StringBuffer buffer = new StringBuffer();
		buffer.append(keyClim.substring(START_ID_RDT, START_ID_RDT + LENGTH_ID_RDT));
		buffer.append(keyClim.substring(START_SYS_FROID, START_SYS_FROID + LENGTH_SYS_FROID));
		buffer.append(periode);

		return buffer.toString();

	}

	// Genere les id des segments a partir des id de la Map
	protected String getIdSeg(String keyClim) {

		String id = keyClim.substring(START_ID_SEG, START_ID_SEG + LENGTH_ID_SEG_TOT);

		return id;
	}

	protected HashMap<String, List<String>> idAgregListClim(HashMap<String, Conso> besoinMap,
			HashMap<String, Parc> parcAgreg) {

		HashMap<String, List<String>> keyMap = new HashMap<String, List<String>>();
		List<String> listKey = new ArrayList<String>();
		TypeRenovBati typeRenovBat = TypeRenovBati.ETAT_INIT;
		String anneeRenovBat = INIT_STATE;
		for (String parcKey : parcAgreg.keySet()) {
			Parc parcTemp = parcAgreg.get(parcKey);
			String idTest = parcTemp.getId().substring(START_ID_SEG, START_ID_SEG + LENGTH_ID_SEG_AGREG2)
					+ Energies.ELECTRICITE.getCode() + anneeRenovBat + typeRenovBat;
			listKey = new ArrayList<String>();
			for (String besoinKey : besoinMap.keySet()) {
				Conso besoin = besoinMap.get(besoinKey);
				String id = besoin.getIdagreg()
						+ besoin.getId().substring(START_SYS_FROID, START_SYS_FROID + LENGTH_SYS_FROID)
						+ Energies.ELECTRICITE.getCode() + besoin.getAnneeRenov() + besoin.getTypeRenovBat();
				if (id.equals(idTest)) {

					listKey.add(besoin.getIdagreg()
							+ besoin.getId().substring(START_SYS_FROID, START_SYS_FROID + LENGTH_SYS_FROID)
							+ Energies.ELECTRICITE.getCode() + besoin.getAnneeRenovSys() + besoin.getTypeRenovSys()
							+ besoin.getAnneeRenov() + besoin.getTypeRenovBat());
				}

			}
			keyMap.put(parcKey, listKey);

		}
		return keyMap;

	}

	protected HashMap<String, List<String>> idAgregListNeuf(HashMap<String, Parc> parcTotMap, int annee, int anneeNTab) {

		HashMap<String, List<String>> keyMap = new HashMap<String, List<String>>();
		List<String> listKey = new ArrayList<String>();
		TypeRenovBati typeRenovBat = TypeRenovBati.ETAT_INIT;
		String anneeRenovBat = INIT_STATE;
		for (String parcKey : parcTotMap.keySet()) {
			listKey = new ArrayList<String>();
			Parc parc = parcTotMap.get(parcKey);
			String key = parcKey.substring(START_ID_SEG, START_ID_SEG + LENGTH_ID_SEG_AGREG2);
			String sysFroid = parcKey.substring(START_SYS_FROID, START_SYS_FROID + LENGTH_SYS_FROID);
			// Test : les segments retenus sont ceux construits apres 2009 et
			// ceux n'etant pas climatises
			if ((parc.getIdperiodesimple().compareTo(Period.PERIODE_BEFORE_1980.getCode()) != 0
					&& parc.getIdperiodesimple().compareTo(Period.PERIODE_1981_1998.getCode()) != 0 && parc
					.getIdperiodesimple().compareTo(Period.PERIODE_1999_2008.getCode()) != 0)
					|| sysFroid.compareTo(CODE_NON_CLIM) == 0) {
				if (!keyMap.containsKey(key)) {
					if (parc.getIdperiodesimple().compareTo(Period.PERIODE_BEFORE_1980.getCode()) != 0
							&& parc.getIdperiodesimple().compareTo(Period.PERIODE_1981_1998.getCode()) != 0
							&& parc.getIdperiodesimple().compareTo(Period.PERIODE_1999_2008.getCode()) != 0) {
						if (parc.getAnnee(anneeNTab - 1).signum() == 0 || parc.getAnnee(anneeNTab - 1) == null) {
							listKey.add(key + Energies.ELECTRICITE.getCode().toString() + INIT_STATE
									+ TypeRenovSysteme.ETAT_INIT + anneeRenovBat + typeRenovBat);
						}
					} else {

						listKey.add(parc.getId().substring(START_ID_SEG, START_ID_SEG + LENGTH_ID_SEG_AGREG2)
								+ Energies.ELECTRICITE.getCode().toString() + INIT_STATE + TypeRenovSysteme.ETAT_INIT
								+ INIT_STATE + TypeRenovBati.ETAT_INIT);
					}
				}

				keyMap.put(parcKey, listKey);

			}
		}
		return keyMap;

	}

}
