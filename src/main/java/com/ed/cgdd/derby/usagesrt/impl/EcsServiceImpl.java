package com.ed.cgdd.derby.usagesrt.impl;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ed.cgdd.derby.common.CommonService;
import com.ed.cgdd.derby.model.calcconso.Conso;
import com.ed.cgdd.derby.model.calcconso.ParamBesoinsNeufs;
import com.ed.cgdd.derby.model.calcconso.ParamCoutEcs;
import com.ed.cgdd.derby.model.calcconso.ParamPMConso;
import com.ed.cgdd.derby.model.calcconso.ParamPMConsoChgtSys;
import com.ed.cgdd.derby.model.calcconso.ParamPartSolaireEcs;
import com.ed.cgdd.derby.model.calcconso.ParamPartSysPerfEcs;
import com.ed.cgdd.derby.model.calcconso.ParamRdtEcs;
import com.ed.cgdd.derby.model.calcconso.ParamRdtPerfEcs;
import com.ed.cgdd.derby.model.calcconso.ParamTauxCouvEcs;
import com.ed.cgdd.derby.model.calcconso.ResultConsoRdt;
import com.ed.cgdd.derby.model.calcconso.ResultConsoURt;
import com.ed.cgdd.derby.model.parc.Energies;
import com.ed.cgdd.derby.model.parc.MapResultsKeys;
import com.ed.cgdd.derby.model.parc.Parc;
import com.ed.cgdd.derby.model.parc.TypeRenovBati;
import com.ed.cgdd.derby.model.parc.TypeRenovSysteme;
import com.ed.cgdd.derby.model.parc.Usage;
import com.ed.cgdd.derby.process.InitializeConsoService;
import com.ed.cgdd.derby.usagesrt.EcsService;

public class EcsServiceImpl implements EcsService {
	private final static Logger LOG = LogManager.getLogger(EcsServiceImpl.class);
	private CommonService commonService;
	private InitializeConsoService initializeConsoService;
	private static final int START_ENERG_AGREG = 12;
	private static final int LENGHT_ENERG_AGREG = 2;
	private static final BigDecimal FACTEUR_EP = new BigDecimal("2.58");
	private static final String INIT_STATE = "Etat initial";

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

	// Calcul les evolutions des besoins et des consommations de l'usage d'ECS
	@Override
	public ResultConsoRdt evolEcsConso(HashMap<String, ParamCoutEcs> coutEcsMap, HashMap<String, Parc> parcTotMap,
			ResultConsoRdt resultatsConsoEcs, HashMap<String, ParamPMConso> pmEcsNeufMap,
			HashMap<String, ParamPMConsoChgtSys> pmEcsChgtMap, HashMap<String, ParamBesoinsNeufs> bNeufsMap,
			HashMap<String, ParamPartSolaireEcs> partSolaireMap, HashMap<String, ParamTauxCouvEcs> txCouvSolaireMap,
			HashMap<String, BigDecimal> dvEcsMap, HashMap<String, ParamRdtEcs> bibliRdtEcsMap,
			HashMap<String, ParamRdtPerfEcs> rdtPerfEcsMap, HashMap<String, ParamPartSysPerfEcs> partSysPerfEcsMap,
			int anneeNTab, int pasdeTemps, int annee, BigDecimal compteur, String usage,
			HashMap<String, ResultConsoURt> resultConsoURtMap, HashMap<String, BigDecimal[]> elasticiteNeufMap,
			HashMap<String, BigDecimal[]> elasticiteExistantMap) {

		// Initialisation des objets

		HashMap<String, Conso> besoinMap = resultatsConsoEcs.getMap(MapResultsKeys.BESOIN_ECS.getLabel());
		HashMap<String, Conso> rdtMapEcs = resultatsConsoEcs.getMap(MapResultsKeys.RDT_ECS.getLabel());
		HashMap<String, Conso> consoMap = resultatsConsoEcs.getMap(MapResultsKeys.CONSO_ECS.getLabel());
		HashMap<String, Conso> coutMap = resultatsConsoEcs.getMap(MapResultsKeys.COUT_ECS.getLabel());

		// Agregation du parc et des besoins en ECS sans le code "energie" (deux
		// derniers chiffres de l'ID). Les besoins sont aussi agreges pour
		// pouvoir calculer les couts de changement des systemes
		// HashMap<String, Parc> parcTotAgregMap =
		// commonService.aggregateParc(parcTotMap, anneeNTab);
		HashMap<String, Parc> parcTotAgregMap = commonService.aggregateParcEcs(parcTotMap, anneeNTab,
				resultConsoURtMap, pasdeTemps);
		HashMap<String, Conso> besoinTotAgregMap = commonService.aggregateConsoEcs(besoinMap, anneeNTab);
		HashMap<String, List<String>> keyMap = commonService.idAgregList(besoinMap, parcTotAgregMap);
		// Parcours chaque segment de parc agrege
		for (String idAgregParc : parcTotAgregMap.keySet()) {
			Parc parcAgreg = parcTotAgregMap.get(idAgregParc);
			// Null dans le cas d'un segment non compris dans le parc ?
			Conso besoinAgreg = besoinTotAgregMap.get(idAgregParc);

			ParamPMConso pmEcsNeuf = pmEcsNeufMap.get(parcAgreg.getIdbranche());
			int periodeCstr = commonService.correspPeriodeCstr(parcAgreg, annee);
			int periode = commonService.correspPeriode(annee);
			if (parcAgreg.getAnnee(0) == null || parcAgreg.getAnnee(0).signum() == 0) {

				// Si le parc a l'annee n-1 est null ou bien egal a 0 alors
				// le
				// segment correspond a des batiments neufs
				batimentsEntrants(coutMap, coutEcsMap, bNeufsMap, bibliRdtEcsMap, anneeNTab, pasdeTemps, annee, usage,
						besoinMap, rdtMapEcs, consoMap, idAgregParc, parcAgreg, pmEcsNeuf, partSolaireMap,
						txCouvSolaireMap, periodeCstr, periode, resultConsoURtMap, elasticiteNeufMap);

			} else {
				// cas des segments existants
				batimentsExistants(coutMap, besoinAgreg, coutEcsMap, pmEcsChgtMap, partSysPerfEcsMap, dvEcsMap,
						bibliRdtEcsMap, rdtPerfEcsMap, anneeNTab, pasdeTemps, annee, compteur, besoinMap, rdtMapEcs,
						consoMap, keyMap, idAgregParc, parcAgreg, periodeCstr, periode, resultConsoURtMap,
						elasticiteExistantMap);

			}

			// Encapsule les map de resultats
			resultatsConsoEcs.put(MapResultsKeys.BESOIN_ECS.getLabel(), besoinMap);
			resultatsConsoEcs.put(MapResultsKeys.RDT_ECS.getLabel(), rdtMapEcs);
			resultatsConsoEcs.put(MapResultsKeys.CONSO_ECS.getLabel(), consoMap);
			resultatsConsoEcs.put(MapResultsKeys.COUT_ECS.getLabel(), coutMap);
		}
		return resultatsConsoEcs;
	}

	protected void batimentsExistants(HashMap<String, Conso> coutMap, Conso besoinAgreg,
			HashMap<String, ParamCoutEcs> coutEcsMap, HashMap<String, ParamPMConsoChgtSys> pmEcsChgtMap,
			HashMap<String, ParamPartSysPerfEcs> partSysPerfEcsMap, HashMap<String, BigDecimal> dvEcsMap,
			HashMap<String, ParamRdtEcs> bibliRdtEcsMap, HashMap<String, ParamRdtPerfEcs> rdtPerfEcsMap, int anneeNTab,
			int pasdeTemps, int annee, BigDecimal compteur, HashMap<String, Conso> besoinMap,
			HashMap<String, Conso> rdtMapEcs, HashMap<String, Conso> consoMap, HashMap<String, List<String>> keyMap,
			String idAgregParc, Parc parcAgreg, int periodeCstr, int periode,
			HashMap<String, ResultConsoURt> resultConsoURtMap, HashMap<String, BigDecimal[]> elasticiteExistantMap) {
		// cas des segments existants
		List<String> keyList = keyMap.get(idAgregParc);
		// boucle sur les idAgregEcs,
		// idAgreg+EnergEcs+AnneeRenovSys+TypeRenovSys+AnneeRenovBat+TypeRenovBat
		for (String idEcsMap : keyList) {

			Conso besoinSegment = besoinMap.get(idEcsMap);
			Conso consoSegment = consoMap.get(idEcsMap);
			Conso rdtSegment = rdtMapEcs.get(idEcsMap);
			// Recuperation de l'energie ECS du segment
			String energCodeSegment = idEcsMap.substring(START_ENERG_AGREG, START_ENERG_AGREG + LENGHT_ENERG_AGREG);

			// Calcul de la duree de vie
			BigDecimal dureeVie = BigDecimal.ZERO;
			if (dvEcsMap.get(energCodeSegment) != null) {
				dureeVie = dvEcsMap.get(energCodeSegment);
			}

			// Taux d'evolution du besoin uniquement du fait de
			// l'evolution des surfaces
			BigDecimal tauxEvolBesoin = calcBesoinEvol(parcAgreg, anneeNTab).setScale(2, BigDecimal.ROUND_HALF_UP);

			// si besoin Evol est superieur a 1, alors un nouveau
			// segment est creee
			if (tauxEvolBesoin.compareTo(BigDecimal.ONE) == 1) {
				// Creation des besoins pour un nouveau segment
				Conso besoinSegmentNew = besoinNewSegment(besoinSegment, tauxEvolBesoin, annee, pasdeTemps, anneeNTab,
						elasticiteExistantMap);
				mapCalc(anneeNTab, besoinMap, besoinSegmentNew);
				// Creation des rendements a associer au nouveau segment
				Conso rdtSegmentNew = rdtNewSegment(besoinSegmentNew, bibliRdtEcsMap, rdtPerfEcsMap, partSysPerfEcsMap,
						periode, periodeCstr, energCodeSegment, pasdeTemps, anneeNTab, annee);
				String newIdTest = commonService.generateIdMapResultRt(besoinSegmentNew);
				rdtMapEcs.put(newIdTest, rdtSegmentNew);
				// Creation des consommations a associer au nouveau
				// segment
				Conso consoSegmentNew = consoNeufEcs(parcAgreg, besoinSegmentNew, rdtSegmentNew, pasdeTemps, anneeNTab,
						annee, resultConsoURtMap);
				mapCalc(anneeNTab, consoMap, consoSegmentNew);
			}
			// Creation du test pour savoir si le systeme est en fin de vie
			BigDecimal anneeBd = new BigDecimal(String.valueOf(annee));
			BigDecimal anneeRenovSys = (anneeBd.subtract(dureeVie)).setScale(0, BigDecimal.ROUND_HALF_UP);
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
				BigDecimal besoinNTemp = BigDecimal.ONE;
				if (besoinSegment.getAnnee(anneeNTab - 1) != null) {
					besoinNTemp = besoinSegment.getAnnee(anneeNTab - 1);
				}
				BigDecimal facteurElasticite = elasticiteExistantMap.get(Usage.ECS.getLabel()
						+ besoinSegment.getId().substring(START_ENERG_AGREG, START_ENERG_AGREG + LENGHT_ENERG_AGREG))[annee - 2009];
				besoinSegment.setAnnee(anneeNTab,
						(tauxEvolBesoin.multiply(besoinNTemp)).multiply(facteurElasticite, MathContext.DECIMAL32));
				besoinMap.put(idEcsMap, besoinSegment);
				// Les rendements n'evoluent pas
				rdtSegment = calcRdtConstant(besoinSegment, rdtSegment, anneeNTab);
				rdtMapEcs.put(idEcsMap, rdtSegment);
				// Les consommations evoluent dans les memes proportions
				// que les besoins dans la mesure ou les rendements sont
				// constants
				besoinNTemp = BigDecimal.ONE;
				if (consoSegment.getAnnee(anneeNTab - 1) != null) {
					besoinNTemp = consoSegment.getAnnee(anneeNTab - 1);
				}
				consoSegment.setAnnee(anneeNTab, tauxEvolBesoin.multiply(besoinNTemp));
				consoMap.put(idEcsMap, consoSegment);
				// Remplissage de la map de consoU
				String idConsoU = consoSegment.getIdagreg() + consoSegment.getAnneeRenov()
						+ consoSegment.getTypeRenovBat();
				if (resultConsoURtMap.containsKey(idConsoU)) {
					BigDecimal consoEP = consoSegment.getAnnee(anneeNTab);
					if (consoSegment.getId().substring(START_ENERG_AGREG, START_ENERG_AGREG + LENGHT_ENERG_AGREG)
							.equals(Energies.ELECTRICITE.getCode())) {
						consoEP = consoSegment.getAnnee(anneeNTab).multiply(FACTEUR_EP);
					}
					initializeConsoService.insertResultConsoUExistECS(resultConsoURtMap, anneeNTab, idConsoU,
							consoSegment.getAnnee(anneeNTab), consoEP, annee);

				} else {
					LOG.info("Probleme l207..");
				}
			} else {

				if (tauxEvolBesoin.compareTo(BigDecimal.ONE) == 1) {
					tauxEvolBesoin = BigDecimal.ONE;
				}
				// Cas ou les segments doivent faire l'objet de travaux
				// de renovation
				// Calcul des besoins qui vont sortir du segment
				BigDecimal besoinSortant = calcBesoinTransfert(anneeRenovSys, compteur, besoinSegment, dureeVie,
						energCodeSegment, idAgregParc, anneeNTab, annee);

				// Calcul de la matrice de transfert pour l'energie
				// consideree
				HashMap<String, BigDecimal> besoinTransMap = new HashMap<String, BigDecimal>();
				besoinTransMap = calcBesoinTransMap(pmEcsChgtMap, besoinSegment, anneeNTab, besoinTransMap,
						besoinSortant, Energies.getEnumName(energCodeSegment).toString(), annee);

				HashMap<String, BigDecimal> coutTansMap = calcCoutTransMap(coutEcsMap, anneeNTab, besoinTransMap,
						parcAgreg, besoinAgreg, partSysPerfEcsMap, periode);
				coutMap = coutModifTrans(parcAgreg, besoinSegment.getIdagreg(), coutMap, coutTansMap, energCodeSegment,
						annee, anneeNTab, pasdeTemps);

				// Calcul du nouveau besoin pour le segment pre-existant
				// ou ayant deja subit une renovation
				besoinSegment = besoinExistantModif(besoinSegment, tauxEvolBesoin, besoinSortant, anneeNTab,
						elasticiteExistantMap, annee);
				besoinMap.put(idEcsMap, besoinSegment);
				// Les rendements n'evoluent pas
				rdtSegment = calcRdtConstant(besoinSegment, rdtSegment, anneeNTab);
				rdtMapEcs.put(idEcsMap, rdtSegment);
				// Les consommations sont recalculees de la meme facon
				// que les besoins, les rendements etant constants
				consoSegment = consoExistantModif(consoSegment, besoinSegment, rdtSegment, anneeNTab,
						resultConsoURtMap, annee);
				consoMap.put(idEcsMap, consoSegment);
				// Creation ou modification des segments vers lesquels
				// sont transferes les besoins sortant si il y a
				// substitution energetique
				besoinMap = besoinModifTrans(parcAgreg, besoinSegment.getIdagreg(), besoinMap, besoinTransMap,
						energCodeSegment, annee, anneeNTab, pasdeTemps, elasticiteExistantMap);
				rdtMapEcs = rdtModifTrans(parcAgreg, besoinSegment.getIdagreg(), rdtMapEcs, besoinMap, besoinTransMap,
						energCodeSegment, bibliRdtEcsMap, rdtPerfEcsMap, partSysPerfEcsMap, periode, periodeCstr,
						pasdeTemps, anneeNTab, annee);
				consoMap = consoModifTrans(parcAgreg, besoinSegment.getIdagreg(), consoMap, rdtMapEcs, besoinMap,
						besoinTransMap, energCodeSegment, periode, periodeCstr, pasdeTemps, anneeNTab, annee,
						resultConsoURtMap);

			}

		}

	}

	// Methode faisant evoluer les couts lies aux changements de systemes
	protected HashMap<String, Conso> coutModifTrans(Parc parcAgreg, String idAgregSegment,
			HashMap<String, Conso> coutMap, HashMap<String, BigDecimal> coutTransMap, String energCodeSegment,
			int annee, int anneeNTab, int pasdeTemps) {
		// boucle sur les energies a transferer
		for (String energ : coutTransMap.keySet()) {

			// si le segment existe deja :
			if (coutMap.containsKey(generateIDNew(parcAgreg, commonService.codeCreateEnerg(energ), idAgregSegment,
					annee))) {

				Conso coutSegment = coutMap.get(generateIDNew(parcAgreg, commonService.codeCreateEnerg(energ),
						idAgregSegment, annee));

				if (coutSegment.getAnnee(anneeNTab) != null) {
					coutSegment.setAnnee(anneeNTab, coutTransMap.get(energ).add(coutSegment.getAnnee(anneeNTab)));
				} else {

					coutSegment.setAnnee(anneeNTab, coutTransMap.get(energ));
				}
				coutMap.put(generateIDNew(parcAgreg, commonService.codeCreateEnerg(energ), idAgregSegment, annee),
						coutSegment);
			} else {
				// si le segment n'existe pas :
				if (coutTransMap.get(energ).signum() != 0) {
					// le BigDecimal.ONE remplace le facteur d'elasticite
					Conso besoinSegment = newSegmentbesoin(parcAgreg, coutTransMap.get(energ), anneeNTab, annee,
							idAgregSegment, commonService.codeCreateEnerg(energ), pasdeTemps, BigDecimal.ONE);
					coutMap.put(generateIDNew(parcAgreg, commonService.codeCreateEnerg(energ), idAgregSegment, annee),
							besoinSegment);

				}
			}

		}
		return coutMap;
	}

	// Methode permettant d'affecter des surfaces chauffees face aux besoins
	// subissant un renouvellement de systeme
	protected HashMap<String, BigDecimal> calcCoutTransMap(HashMap<String, ParamCoutEcs> coutEcsMap, int anneeNTab,
			HashMap<String, BigDecimal> besoinTransMap, Parc parcAgreg, Conso besoinAgreg,
			HashMap<String, ParamPartSysPerfEcs> partSysPerfEcsMap, int periode) {

		HashMap<String, BigDecimal> coutTransMap = new HashMap<String, BigDecimal>();

		for (String energ : besoinTransMap.keySet()) {

			BigDecimal besoinEnergTrans = besoinTransMap.get(energ);
			BigDecimal partBesoin = BigDecimal.ZERO;
			if (besoinAgreg.getAnnee(0) != null && besoinAgreg.getAnnee(0).signum() != 0) {
				// Calcul de la part du besoin changeant de systeme par rapport
				// a l'ensemble du besoin a l'annee N-1
				partBesoin = besoinEnergTrans.divide(besoinAgreg.getAnnee(0), MathContext.DECIMAL32);
			}

			// Calcul de la surface touchee par le changement de systeme
			BigDecimal surfTrans = BigDecimal.ZERO;
			if (parcAgreg != null && parcAgreg.getAnnee(0) != null) {
				surfTrans = partBesoin.multiply(parcAgreg.getAnnee(0));
			}

			// Calcul de la part de marche des systemes performants pour cette
			// energie
			ParamPartSysPerfEcs partSysPerfEcs = partSysPerfEcsMap.get(commonService.codeCreateEnerg(energ));
			BigDecimal partSysPerf = partSysPerfEcs.getPart(periode);
			BigDecimal partSysClassique = BigDecimal.ONE.subtract(partSysPerf);

			// Calcul du cout associe au changement de systeme
			ParamCoutEcs coutClassique = coutEcsMap.get(commonService.codeCreateEnerg(energ) + "Classique");
			ParamCoutEcs coutPerf = coutEcsMap.get(commonService.codeCreateEnerg(energ) + "Performant");

			BigDecimal coutTrans1 = coutClassique.getCout().multiply(surfTrans).multiply(partSysClassique);
			BigDecimal coutTrans2 = coutPerf.getCout().multiply(surfTrans).multiply(partSysPerf);
			BigDecimal coutTrans = coutTrans1.add(coutTrans2);
			coutTransMap.put(energ, coutTrans);
		}

		return coutTransMap;
	}

	private void mapCalc(int anneeNTab, HashMap<String, Conso> besoinMap, Conso besoinSegmentNew) {
		if (besoinMap.containsKey(commonService.generateIdMapResultRt(besoinSegmentNew))) {
			Conso besoinTemp = new Conso(besoinMap.get(commonService.generateIdMapResultRt(besoinSegmentNew)));
			besoinTemp.setAnnee(anneeNTab, besoinTemp.getAnnee(anneeNTab).add(besoinSegmentNew.getAnnee(anneeNTab)));
			besoinMap.put(commonService.generateIdMapResultRt(besoinSegmentNew), besoinTemp);
		} else {
			besoinMap.put(commonService.generateIdMapResultRt(besoinSegmentNew), besoinSegmentNew);
		}
	}

	protected HashMap<String, Conso> consoModifTrans(Parc parcAgreg, String idAgregSegment,
			HashMap<String, Conso> consoMap, HashMap<String, Conso> rdtMapEcs, HashMap<String, Conso> besoinMap,
			HashMap<String, BigDecimal> besoinTransMap, String energCodeSegment, int periode, int periodeCstr,
			int pasdeTemps, int anneeNTab, int annee, HashMap<String, ResultConsoURt> resultConsoURtMap) {
		// boucle sur les energies a transferer
		for (String energ : besoinTransMap.keySet()) {

			// si le segment existe deja :
			if (besoinMap.containsKey(generateIDNew(parcAgreg, commonService.codeCreateEnerg(energ), idAgregSegment,
					annee))) {
				if (besoinTransMap.get(energ).signum() != 0) {
					Conso besoinSegment = besoinMap.get(generateIDNew(parcAgreg, commonService.codeCreateEnerg(energ),
							idAgregSegment, annee));
					Conso rdtSegment = rdtMapEcs.get(generateIDNew(parcAgreg, commonService.codeCreateEnerg(energ),
							idAgregSegment, annee));
					BigDecimal newConso = BigDecimal.ZERO;
					if (rdtSegment.getAnnee(anneeNTab).signum() != 0 && rdtSegment.getAnnee(anneeNTab) != null) {

						newConso = besoinSegment.getAnnee(anneeNTab).divide(rdtSegment.getAnnee(anneeNTab),
								MathContext.DECIMAL32);
					}

					Conso consoSegment = new Conso(pasdeTemps);
					consoSegment.setId(besoinSegment.getId());
					consoSegment.setAnneeRenovSys(besoinSegment.getAnneeRenovSys());
					consoSegment.setTypeRenovSys(besoinSegment.getTypeRenovSys());
					consoSegment.setAnneeRenov(parcAgreg.getAnneeRenov());
					consoSegment.setTypeRenovBat(parcAgreg.getTypeRenovBat());
					consoSegment.setAnnee(anneeNTab, newConso);
					consoMap.put(generateIDNew(parcAgreg, commonService.codeCreateEnerg(energ), idAgregSegment, annee),
							consoSegment);

					// Remplissage de la map de consoU
					String idConsoU = consoSegment.getIdagreg() + consoSegment.getAnneeRenov()
							+ consoSegment.getTypeRenovBat();
					if (resultConsoURtMap.containsKey(idConsoU)) {
						BigDecimal consoEP = consoSegment.getAnnee(anneeNTab);
						if (consoSegment.getId().substring(START_ENERG_AGREG, START_ENERG_AGREG + LENGHT_ENERG_AGREG)
								.equals(Energies.ELECTRICITE.getCode())) {
							consoEP = consoSegment.getAnnee(anneeNTab).multiply(FACTEUR_EP);
						}
						initializeConsoService.insertResultConsoUExistECS(resultConsoURtMap, anneeNTab, idConsoU,
								consoSegment.getAnnee(anneeNTab), consoEP, annee);

					} else {
						LOG.info("Probleme l383..");
					}

				}
			}

		}
		return consoMap;
	}

	protected HashMap<String, Conso> rdtModifTrans(Parc parcAgreg, String idAgregSegment,
			HashMap<String, Conso> rdtMapEcs, HashMap<String, Conso> besoinMap,
			HashMap<String, BigDecimal> besoinTransMap, String energCodeSegment,
			HashMap<String, ParamRdtEcs> bibliRdtEcsMap, HashMap<String, ParamRdtPerfEcs> rdtPerfEcsMap,
			HashMap<String, ParamPartSysPerfEcs> partSysPerfEcsMap, int periode, int periodeCstr, int pasdeTemps,
			int anneeNTab, int annee) {
		// boucle sur les energies a transferer
		for (String energ : besoinTransMap.keySet()) {

			// si le segment existe deja :
			if (!rdtMapEcs.containsKey(generateIDNew(parcAgreg, commonService.codeCreateEnerg(energ), idAgregSegment,
					annee))) {

				if (besoinTransMap.get(energ).signum() != 0) {
					Conso besoinSegment = besoinMap.get(generateIDNew(parcAgreg, commonService.codeCreateEnerg(energ),
							idAgregSegment, annee));
					Conso rdtSegment = rdtNewSegment(besoinSegment, bibliRdtEcsMap, rdtPerfEcsMap, partSysPerfEcsMap,
							periode, periodeCstr, commonService.codeCreateEnerg(energ), pasdeTemps, anneeNTab, annee);
					rdtMapEcs.put(
							generateIDNew(parcAgreg, commonService.codeCreateEnerg(energ), idAgregSegment, annee),
							rdtSegment);

				}
			}

		}
		return rdtMapEcs;
	}

	protected Conso rdtNewSegment(Conso besoinSegment, HashMap<String, ParamRdtEcs> bibliRdtEcsMap,
			HashMap<String, ParamRdtPerfEcs> rdtPerfEcsMap, HashMap<String, ParamPartSysPerfEcs> partSysPerfEcsMap,
			int periode, int periodeCstr, String energCodeSegment, int pasdeTemps, int anneeNTab, int annee) {
		// Recuperation des parts des systemes performants
		Conso rdtEcsSegment = new Conso(pasdeTemps);
		ParamPartSysPerfEcs partSysPerfEcs = partSysPerfEcsMap.get(energCodeSegment);
		BigDecimal partSysPerf = partSysPerfEcs.getPart(periode);
		// Recuperation du rendement classique
		ParamRdtEcs rdtEcsBranche = bibliRdtEcsMap.get(besoinSegment.getIdbranche() + energCodeSegment);
		BigDecimal rdtNormal = rdtEcsBranche.getRdt(periodeCstr);
		// Recuperation du rendement performant
		ParamRdtPerfEcs rdtPerfExtract = rdtPerfEcsMap.get(energCodeSegment);
		BigDecimal rdtPerf = rdtPerfExtract.getRdt();

		// Formule nouveau rendement :
		// RdtNew = (RdtPerf*RdtNorm)/(partPerf*RdtNorm+partNorm*RdtPerf)
		BigDecimal rdtN1 = BigDecimal.ZERO;
		BigDecimal denomPart1 = partSysPerf.multiply(rdtNormal);
		BigDecimal denomPart2 = (BigDecimal.ONE.subtract(partSysPerf)).multiply(rdtPerf);
		BigDecimal numerateur = rdtNormal.multiply(rdtPerf);
		if ((denomPart1.add(denomPart2)).signum() != 0) {

			rdtN1 = numerateur.divide((denomPart1.add(denomPart2)), MathContext.DECIMAL32);
		}

		rdtEcsSegment.setId(besoinSegment.getId());
		rdtEcsSegment.setAnneeRenovSys(String.valueOf(annee));
		rdtEcsSegment.setTypeRenovSys(TypeRenovSysteme.CHGT_SYS);
		rdtEcsSegment.setAnneeRenov(besoinSegment.getAnneeRenov());
		rdtEcsSegment.setTypeRenovBat(besoinSegment.getTypeRenovBat());
		if (besoinSegment.getAnnee(anneeNTab).signum() == 0) {
			rdtEcsSegment.setAnnee(anneeNTab, BigDecimal.ZERO);

		} else {
			rdtEcsSegment.setAnnee(anneeNTab, rdtN1);
		}
		return rdtEcsSegment;
	}

	protected void batimentsEntrants(HashMap<String, Conso> coutMap, HashMap<String, ParamCoutEcs> coutECSMap,
			HashMap<String, ParamBesoinsNeufs> bNeufsMap, HashMap<String, ParamRdtEcs> bibliRdtEcsMap, int anneeNTab,
			int pasdeTemps, int annee, String usage, HashMap<String, Conso> besoinMap,
			HashMap<String, Conso> rdtMapEcs, HashMap<String, Conso> consoMap, String idAgregParc, Parc parcAgreg,
			ParamPMConso pmEcsNeuf, HashMap<String, ParamPartSolaireEcs> partSolaireMap,
			HashMap<String, ParamTauxCouvEcs> txCouvSolaireMap, int periodeCstr, int periode,
			HashMap<String, ResultConsoURt> resultConsoURtMap, HashMap<String, BigDecimal[]> elasticiteNeufMap) {
		ParamPartSolaireEcs partSolaireParam = new ParamPartSolaireEcs();
		ParamTauxCouvEcs txCouvSolaireParam = new ParamTauxCouvEcs();

		if (parcAgreg.getTypeRenovBat().equals(TypeRenovBati.ENSBBC)) {
			partSolaireParam = partSolaireMap.get("Existant" + parcAgreg.getIdbranche());
			txCouvSolaireParam = txCouvSolaireMap.get("Existant");
			periodeCstr = commonService.correspPeriode(annee);
		} else {
			partSolaireParam = partSolaireMap.get("Neuf" + parcAgreg.getIdbranche());
			txCouvSolaireParam = txCouvSolaireMap.get("Neuf");
		}

		HashMap<String, BigDecimal> pmNeuf = pmEcsNeuf.getEnergie();
		// Boucle sur les energies des parts de marche dans le neuf

		for (String energPmNeuf : pmNeuf.keySet()) {
			// Calcul des besoins en ECS pour le nouveau segment
			Conso parcBesoinNeuf = besoinNeufEcs(parcAgreg, bNeufsMap, pmNeuf.get(energPmNeuf),
					partSolaireParam.getPart(periode), txCouvSolaireParam.getTxcouv(), usage, periodeCstr, pasdeTemps,
					anneeNTab, energPmNeuf, annee, elasticiteNeufMap);
			Conso rdt = rdtNeufEcs(parcBesoinNeuf, parcAgreg, bibliRdtEcsMap, energPmNeuf, periodeCstr, pasdeTemps,
					anneeNTab, annee);
			Conso consoNeuf = consoNeufEcs(parcAgreg, parcBesoinNeuf, rdt, pasdeTemps, anneeNTab, annee,
					resultConsoURtMap);

			if (parcAgreg.getTypeRenovBat().equals(TypeRenovBati.ENSBBC)) {
				Conso coutNeuf = coutNeufEcs(coutECSMap, parcAgreg, pmNeuf.get(energPmNeuf),
						partSolaireParam.getPart(periode), txCouvSolaireParam.getTxcouv(), usage, periodeCstr,
						pasdeTemps, anneeNTab, energPmNeuf, annee);
				coutMap.put(commonService.generateIdMapResultRt(coutNeuf), coutNeuf);
			}
			// IDagreg ??
			rdtMapEcs.put(commonService.generateIdMapResultRt(rdt), rdt);
			besoinMap.put(commonService.generateIdMapResultRt(parcBesoinNeuf), parcBesoinNeuf);
			consoMap.put(commonService.generateIdMapResultRt(consoNeuf), consoNeuf);

		}
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

	protected Conso consoExistantModif(Conso consoSegment, Conso besoinSegment, Conso rdtSegment, int anneeNTab,
			HashMap<String, ResultConsoURt> resultConsoURtMap, int annee) {

		BigDecimal conso = BigDecimal.ZERO;
		if (rdtSegment.getAnnee(anneeNTab).signum() != 0 && rdtSegment != null) {
			conso = besoinSegment.getAnnee(anneeNTab).divide(rdtSegment.getAnnee(anneeNTab), MathContext.DECIMAL32);

		}

		consoSegment.setAnnee(anneeNTab, conso);

		// Remplissage de la map de consoU
		String idConsoU = consoSegment.getIdagreg() + consoSegment.getAnneeRenov() + consoSegment.getTypeRenovBat();
		if (resultConsoURtMap.containsKey(idConsoU)) {
			BigDecimal consoEP = consoSegment.getAnnee(anneeNTab);
			if (consoSegment.getId().substring(START_ENERG_AGREG, START_ENERG_AGREG + LENGHT_ENERG_AGREG)
					.equals(Energies.ELECTRICITE.getCode())) {
				consoEP = consoSegment.getAnnee(anneeNTab).multiply(FACTEUR_EP);
			}
			initializeConsoService.insertResultConsoUExistECS(resultConsoURtMap, anneeNTab, idConsoU,
					consoSegment.getAnnee(anneeNTab), consoEP, annee);

		} else {
			LOG.info("Probleme l522..");
		}

		return consoSegment;

	}

	protected Conso rdtSegmentNeufEcs(Conso besoinSegment, HashMap<String, ParamRdtEcs> bibliRdtEcsMap,
			int periodeCstr, String energCodeSegment, int pasdeTemps, int anneeNTab, int annee) {

		Conso rdtEcsSegment = new Conso(pasdeTemps);
		ParamRdtEcs rdtEcsBranche = bibliRdtEcsMap.get(besoinSegment.getIdbranche() + energCodeSegment);
		rdtEcsSegment.setId(besoinSegment.getId());
		rdtEcsSegment.setAnneeRenovSys(String.valueOf(annee));
		rdtEcsSegment.setTypeRenovSys(TypeRenovSysteme.CHGT_SYS);
		rdtEcsSegment.setAnnee(anneeNTab, rdtEcsBranche.getRdt(periodeCstr));
		return rdtEcsSegment;
	}

	protected Conso besoinNewSegment(Conso besoinSegment, BigDecimal tauxEvolBesoin, int annee, int pasdeTemps,
			int anneeNTab, HashMap<String, BigDecimal[]> elasticiteExistantMap) {

		// Facteur d'elasticite
		BigDecimal facteurElasticite = elasticiteExistantMap.get(Usage.ECS.getLabel()
				+ besoinSegment.getId().substring(START_ENERG_AGREG, START_ENERG_AGREG + LENGHT_ENERG_AGREG))[annee - 2009];
		// besoinNew = besoinN * (1-txEvol)
		BigDecimal besoinNew = besoinSegment.getAnnee(anneeNTab - 1)
				.multiply((tauxEvolBesoin).subtract(BigDecimal.ONE));

		Conso newSegment = new Conso(pasdeTemps);
		// remplissage du nouvel objet Conso
		newSegment.setId(besoinSegment.getId());
		newSegment.setAnneeRenovSys(String.valueOf(annee));
		newSegment.setTypeRenovSys(TypeRenovSysteme.CHGT_SYS);
		newSegment.setAnneeRenov(besoinSegment.getAnneeRenov());
		newSegment.setTypeRenovBat(besoinSegment.getTypeRenovBat());
		// Prise en compte du facteur d'elasticite
		newSegment.setAnnee(anneeNTab, besoinNew.multiply(facteurElasticite, MathContext.DECIMAL32));

		return newSegment;
	}

	protected HashMap<String, Conso> besoinModifTrans(Parc parcAgreg, String idAgregSegment,
			HashMap<String, Conso> besoinMap, HashMap<String, BigDecimal> besoinTransMap, String energCodeSegment,
			int annee, int anneeNTab, int pasdeTemps, HashMap<String, BigDecimal[]> elasticiteExistantMap) {
		// boucle sur les energies a transferer
		for (String energ : besoinTransMap.keySet()) {
			String energCode = commonService.codeCreateEnerg(energ);
			// Prise en compte du facteur d'elasticite des besoins
			BigDecimal facteurElasticite = elasticiteExistantMap.get(Usage.ECS.getLabel() + energCode)[annee - 2009];
			// si le segment existe deja :
			if (besoinMap.containsKey(generateIDNew(parcAgreg, energCode, idAgregSegment, annee))) {

				Conso besoinSegment = besoinMap.get(generateIDNew(parcAgreg, energCode, idAgregSegment, annee));

				if (besoinSegment.getAnnee(anneeNTab) != null) {
					besoinSegment.setAnnee(anneeNTab,
							(besoinTransMap.get(energ).add(besoinSegment.getAnnee(anneeNTab))).multiply(
									facteurElasticite, MathContext.DECIMAL32));
				} else {

					besoinSegment.setAnnee(anneeNTab,
							besoinTransMap.get(energ).multiply(facteurElasticite, MathContext.DECIMAL32));
				}
				besoinMap.put(generateIDNew(parcAgreg, energCode, idAgregSegment, annee), besoinSegment);
			} else {
				// si le segment n'existe pas :
				if (besoinTransMap.get(energ).signum() != 0) {
					Conso besoinSegment = newSegmentbesoin(parcAgreg, besoinTransMap.get(energ), anneeNTab, annee,
							idAgregSegment, energCode, pasdeTemps, facteurElasticite);
					besoinMap.put(generateIDNew(parcAgreg, energCode, idAgregSegment, annee), besoinSegment);

				}
			}

		}
		return besoinMap;
	}

	protected Conso newSegmentbesoin(Parc parcAgreg, BigDecimal pmEnergTrans, int anneeNTab, int annee, String idAgreg,
			String energCode, int pasdeTemps, BigDecimal facteurElasticite) {
		Conso besoinExistant = new Conso(pasdeTemps);

		besoinExistant.setId(parcAgreg.getIdagreg() + energCode);
		besoinExistant.setAnneeRenovSys(String.valueOf(annee));
		besoinExistant.setTypeRenovSys(TypeRenovSysteme.CHGT_SYS);
		besoinExistant.setAnneeRenov(parcAgreg.getAnneeRenov());
		besoinExistant.setTypeRenovBat(parcAgreg.getTypeRenovBat());
		besoinExistant.setAnnee(anneeNTab, pmEnergTrans.multiply(facteurElasticite, MathContext.DECIMAL32));
		return besoinExistant;
	}

	protected String generateIDNew(Parc parcAgreg, String energCodeSegment, String idAgregSegment, int annee) {

		StringBuffer buffer = new StringBuffer();
		buffer.append(idAgregSegment);
		buffer.append(energCodeSegment);
		buffer.append(String.valueOf(annee));
		buffer.append(TypeRenovSysteme.CHGT_SYS);
		buffer.append(parcAgreg.getAnneeRenov());
		buffer.append(parcAgreg.getTypeRenovBat());

		return buffer.toString();

	}

	protected BigDecimal calcBesoinTransfert(BigDecimal anneeRenovSys, BigDecimal compteur, Conso besoinSegment,
			BigDecimal dureeVie, String energCodeSegment, String idAgregParc, int anneeNTab, int annee) {

		// Calcul de la part du besoin renouvele
		BigDecimal besoinTransfert = BigDecimal.ONE;
		BigDecimal partRenouv = calcDureeVieEcs(dureeVie, compteur);
		BigDecimal besoinSegmentN = besoinSegment.getAnnee(anneeNTab - 1);

		if (besoinSegment.getAnneeRenovSys().equals(INIT_STATE)) {
			// Test 1 : le segment est un segment initial
			// si le besoin a transferer est plus grand que le besoin de l'annee
			// n-1 alors besoinTransfert=besoinN
			if ((partRenouv.multiply(besoinSegmentN)).compareTo(besoinSegmentN) == -1) {
				besoinTransfert = partRenouv.multiply(besoinSegmentN);
			} else {
				besoinTransfert = besoinSegmentN;
			}
		}
		// si le segment a une duree de vie arrivant a echeance ou bien si le
		// segment est renove en ENSBBC alors c'est
		// tout le besoin qui est concerne
		if (besoinSegment.getAnneeRenovSys().equals(anneeRenovSys.toString())
				|| (besoinSegment.getTypeRenovBat().equals(TypeRenovBati.ENSBBC) && besoinSegment.getAnneeRenov()
						.equals(String.valueOf(annee)))) {

			besoinTransfert = besoinSegmentN;

		}

		return besoinTransfert;
	}

	protected Conso besoinExistantModif(Conso besoinSegment, BigDecimal tauxEvolBesoin, BigDecimal besoinTransfert,
			int anneeNTab, HashMap<String, BigDecimal[]> elasticiteExistantMap, int annee) {

		BigDecimal facteurElasticite = elasticiteExistantMap.get(Usage.ECS.getLabel()
				+ besoinSegment.getId().substring(START_ENERG_AGREG, START_ENERG_AGREG + LENGHT_ENERG_AGREG))[annee - 2009];
		// Formule : besoinSegment = besoinEvol - besoinTransfert
		// Test si le resultat est negatif on affecte 0
		BigDecimal besoinEvol = besoinSegment.getAnnee(anneeNTab - 1).multiply(tauxEvolBesoin);
		if ((besoinEvol.subtract(besoinTransfert)).signum() == -1) {
			besoinSegment.setAnnee(anneeNTab, BigDecimal.ZERO);
		} else {
			// Prise en compte du facteur d'elasticite du besoin
			besoinSegment.setAnnee(anneeNTab,
					(besoinEvol.subtract(besoinTransfert).multiply(facteurElasticite, MathContext.DECIMAL32)));

		}

		return besoinSegment;
	}

	protected Conso consoNeufEcs(Parc parcAgregEntrant, Conso besoinSegment, Conso rdt, int pasdeTemps, int anneeNTab,
			int annee, HashMap<String, ResultConsoURt> resultConsoURtMap) {

		Conso consoNeuf = new Conso(pasdeTemps);
		BigDecimal conso = BigDecimal.ZERO;
		if (rdt.getAnnee(anneeNTab).signum() != 0 && rdt != null) {
			conso = besoinSegment.getAnnee(anneeNTab).divide(rdt.getAnnee(anneeNTab), MathContext.DECIMAL32);

		}
		consoNeuf.setId(besoinSegment.getId());
		consoNeuf.setAnneeRenovSys(String.valueOf(annee));
		consoNeuf.setTypeRenovSys(TypeRenovSysteme.CHGT_SYS);
		consoNeuf.setAnneeRenov(parcAgregEntrant.getAnneeRenov());
		consoNeuf.setTypeRenovBat(parcAgregEntrant.getTypeRenovBat());
		consoNeuf.setAnnee(anneeNTab, conso);

		String idConsoU = parcAgregEntrant.getId();
		if (resultConsoURtMap.containsKey(idConsoU)) {
			BigDecimal consoEP = consoNeuf.getAnnee(anneeNTab);
			if (consoNeuf.getId().substring(START_ENERG_AGREG, START_ENERG_AGREG + LENGHT_ENERG_AGREG)
					.equals(Energies.ELECTRICITE.getCode())) {
				consoEP = consoNeuf.getAnnee(anneeNTab).multiply(FACTEUR_EP);
			}
			initializeConsoService.insertResultConsoUExistECS(resultConsoURtMap, anneeNTab, idConsoU,
					consoNeuf.getAnnee(anneeNTab), consoEP, annee);

		} else {
			LOG.info("Probleme l681..");
		}

		return consoNeuf;

	}

	protected Conso rdtNeufEcs(Conso besoinSegment, Parc parcAgregExistant,
			HashMap<String, ParamRdtEcs> bibliRdtEcsMap, String energPmNeuf, int periodeCstr, int pasdeTemps,
			int anneeNTab, int annee) {

		Conso rdtEcsSegment = new Conso(pasdeTemps);
		String energ = StringUtils.stripAccents(energPmNeuf.toUpperCase());
		ParamRdtEcs rdtEcsBranche = bibliRdtEcsMap.get(parcAgregExistant.getIdbranche()
				+ commonService.codeCreateEnerg(energ));
		rdtEcsSegment.setId(parcAgregExistant.getIdagreg() + commonService.codeCreateEnerg(energ));
		rdtEcsSegment.setAnneeRenovSys(String.valueOf(annee));
		rdtEcsSegment.setTypeRenovSys(TypeRenovSysteme.CHGT_SYS);
		rdtEcsSegment.setAnneeRenov(parcAgregExistant.getAnneeRenov());
		rdtEcsSegment.setTypeRenovBat(parcAgregExistant.getTypeRenovBat());
		if (besoinSegment.getAnnee(anneeNTab).signum() == 0) {
			rdtEcsSegment.setAnnee(anneeNTab, BigDecimal.ZERO);
		} else {
			rdtEcsSegment.setAnnee(anneeNTab, rdtEcsBranche.getRdt(periodeCstr));
		}
		return rdtEcsSegment;
	}

	protected BigDecimal calcBesoinEvol(Parc parcAgreg, int anneeNTab) {

		BigDecimal surfN = parcAgreg.getAnnee(0);
		BigDecimal surfN1 = parcAgreg.getAnnee(1);
		BigDecimal tauxEvol = BigDecimal.ZERO;
		if (surfN.signum() != 0) {
			// BesoinEvol = surfN1/surfN
			tauxEvol = (surfN1.divide(surfN, MathContext.DECIMAL32));
		}

		return tauxEvol;
	}

	protected HashMap<String, BigDecimal> calcBesoinTransMap(HashMap<String, ParamPMConsoChgtSys> pmEcsChgtMap,
			Conso besoinSegment, int anneeNTab, HashMap<String, BigDecimal> besoinTransMap, BigDecimal besoinTransfert,
			String energInit, int annee) {

		ParamPMConsoChgtSys pmChgt = pmEcsChgtMap.get(energInit);

		for (String energTrans : pmEcsChgtMap.keySet()) {
			BigDecimal temp = BigDecimal.ZERO;
			if (besoinTransMap.get(energTrans) != null) {
				temp = besoinTransMap.get(energTrans);
			}
			besoinTransMap.put(energTrans,
					calcFormuleBesoinTransfertEcs(besoinSegment, besoinTransfert, pmChgt, temp, energTrans, annee));

		}
		return besoinTransMap;
	}

	protected BigDecimal calcFormuleBesoinTransfertEcs(Conso besoinSegment, BigDecimal besoinTransfert,
			ParamPMConsoChgtSys pmChgt, BigDecimal temp, String energTrans, int annee) {

		BigDecimal result = besoinTransfert.multiply(pmChgt.getPmChgt(energTrans)).add(temp);
		return result;
	}

	protected BigDecimal calcDureeVieEcs(BigDecimal dureeVie, BigDecimal compteur) {

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

	protected Conso besoinNeufEcs(Parc parcAgregEntrant, HashMap<String, ParamBesoinsNeufs> bNeufsMap,
			BigDecimal pmEnergNeuf, BigDecimal partSolaire, BigDecimal txCouvSolaire, String usage, int periodeCstr,
			int pasdeTemps, int anneeNTab, String energPmNeuf, int annee,
			HashMap<String, BigDecimal[]> elasticiteNeufMap) {

		// Calcul des besoins en ECS pour les batiments entrant dans le parc
		String energ = StringUtils.stripAccents(energPmNeuf.toUpperCase());
		String energCode = commonService.codeCreateEnerg(energ);
		BigDecimal facteurElasticite = elasticiteNeufMap.get(Usage.ECS.getLabel() + energCode)[annee - 2009];
		Conso besoinNeuf = new Conso(pasdeTemps);
		String concat = commonService.concatID(parcAgregEntrant, usage);
		ParamBesoinsNeufs besoinsNeufs = bNeufsMap.get(concat);
		// Prise en compte du facteur d'elasticite
		BigDecimal besoinsU = besoinsNeufs.getPeriode(periodeCstr).multiply(facteurElasticite, MathContext.DECIMAL32);
		BigDecimal couvElecSolaire = calcPartElecSolaire(energPmNeuf, partSolaire, txCouvSolaire);
		besoinNeuf.setId(parcAgregEntrant.getIdagreg() + energCode);

		besoinNeuf.setAnneeRenovSys(String.valueOf(annee));
		besoinNeuf.setTypeRenovSys(TypeRenovSysteme.CHGT_SYS);
		besoinNeuf.setAnneeRenov(parcAgregEntrant.getAnneeRenov());
		besoinNeuf.setTypeRenovBat(parcAgregEntrant.getTypeRenovBat());
		// besoinNeuf =
		// parcAgreg.getAnnee(1)*besoinU*pmEnergNeuf*(1-partSolaire)+couvElecSolaire*parcAgregEntrant.getAnnee(1)*besoinU
		besoinNeuf.setAnnee(
				anneeNTab,
				parcAgregEntrant.getAnnee(1).multiply(besoinsU).multiply(pmEnergNeuf)
						.multiply(BigDecimal.ONE.subtract(partSolaire))
						.add((couvElecSolaire).multiply(parcAgregEntrant.getAnnee(1)).multiply(besoinsU)));
		return besoinNeuf;
	}

	protected Conso coutNeufEcs(HashMap<String, ParamCoutEcs> coutECSMap, Parc parcAgregEntrant,
			BigDecimal pmEnergNeuf, BigDecimal partSolaire, BigDecimal txCouvSolaire, String usage, int periodeCstr,
			int pasdeTemps, int anneeNTab, String energPmNeuf, int annee) {
		String energ = StringUtils.stripAccents(energPmNeuf.toUpperCase());
		String energCode = commonService.codeCreateEnerg(energ);
		// Calcul des besoins en ECS pour les batiments entrant dans le parc
		BigDecimal cout = coutECSMap.get(energCode + "Performant").getCout();
		Conso coutNeuf = new Conso(pasdeTemps);
		coutNeuf.setId(parcAgregEntrant.getIdagreg() + energCode);
		coutNeuf.setAnneeRenovSys(String.valueOf(annee));
		coutNeuf.setTypeRenovSys(TypeRenovSysteme.CHGT_SYS);
		coutNeuf.setAnneeRenov(parcAgregEntrant.getAnneeRenov());
		coutNeuf.setTypeRenovBat(parcAgregEntrant.getTypeRenovBat());
		// coutNeuf =
		// parcAgreg.getAnnee(1)*pmEnergNeuf*coutUnit
		coutNeuf.setAnnee(anneeNTab, parcAgregEntrant.getAnnee(1).multiply(cout).multiply(pmEnergNeuf));
		return coutNeuf;
	}

	protected BigDecimal calcPartElecSolaire(String energPmNeuf, BigDecimal partSolaire, BigDecimal txCouvSolaire) {
		// Calcul la part du besoin pris en charge par des systemes electriques
		// pour servir d'appoint aux systemes solaires
		BigDecimal partElecSolaire = BigDecimal.ZERO;
		if (energPmNeuf.equals("ELECTRICITE")) {
			partElecSolaire = partSolaire.multiply(BigDecimal.ONE.subtract(txCouvSolaire));

		}
		return partElecSolaire;

	}

}
