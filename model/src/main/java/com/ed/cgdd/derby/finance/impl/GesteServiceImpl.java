package com.ed.cgdd.derby.finance.impl;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ed.cgdd.derby.common.CommonService;
import com.ed.cgdd.derby.finance.CoutEnergieService;
import com.ed.cgdd.derby.finance.GesteDAS;
import com.ed.cgdd.derby.finance.GesteService;
import com.ed.cgdd.derby.finance.RecupParamFinDAS;
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
import com.ed.cgdd.derby.model.financeObjects.Exigence;
import com.ed.cgdd.derby.model.financeObjects.Geste;
import com.ed.cgdd.derby.model.financeObjects.GesteObligation;
import com.ed.cgdd.derby.model.financeObjects.Maintenance;
import com.ed.cgdd.derby.model.financeObjects.PBC;
import com.ed.cgdd.derby.model.financeObjects.ReglementationName;
import com.ed.cgdd.derby.model.financeObjects.Reglementations;
import com.ed.cgdd.derby.model.financeObjects.ResultConsoDecret;
import com.ed.cgdd.derby.model.parc.Energies;
import com.ed.cgdd.derby.model.parc.Occupant;
import com.ed.cgdd.derby.model.parc.Parc;
import com.ed.cgdd.derby.model.parc.Period;
import com.ed.cgdd.derby.model.parc.SysChaud;
import com.ed.cgdd.derby.model.parc.TypeRenovBati;
import com.ed.cgdd.derby.model.parc.TypeRenovSysteme;
import com.ed.cgdd.derby.model.parc.Usage;
import com.ed.cgdd.derby.process.politiques;


public class GesteServiceImpl implements GesteService {
	private final static Logger LOG = LogManager.getLogger(GesteServiceImpl.class);
	private final static int START_ID_SYS = 0;

	private final static int START_ID_DV = 6;
	private final static int LENGTH_ID_SYS = 6;
	private final static int LENGTH_ID_DV = 2;

	private final static int START_ID_PERIODE = 10;
	private final static int LENGTH_ID_PERIODE = 1;
	private final static int LENGTH_ID_ENERG = 4;
	private final static int LENGTH_ID_ENERG_CHAUFF = 2;

	private final static int START_ID_ENERG_CHAUFF = 8;
	private final static int LENGTH_ID_SYS_CHAUFF = 2;

	private final static int START_ID_SYS_CHAUFF = 6;
	private final static int START_ID_AGREG = 0;

	private final static int LENGTH_ID_GESTE = 6;
	private static final int START_PERIOD_DETAIL = 6;
	private static final int LENGTH_PERIOD_DETAIL = 2;

	private static final int START_ID_BRANCHE = 0;
	private static final int LENGTH_ID_BRANCHE = 2;

	private static final int START_ID_BAT = 4;
	private static final int LENGTH_ID_BAT = 2;
	private static final BigDecimal FACTEUR_EP = new BigDecimal("2.58");
	private static final String INIT_STATE = "Etat initial";
	private static final String CHGT_SYS = "Chgt systeme";
	private static final String NO_OBLIG = "Aucune obligation";
	private GesteDAS gesteDAS;
	private CommonService commonService;
	private CoutEnergieService coutEnergieService;
	private RecupParamFinDAS recupParamFinDAS;

	public CommonService getCommonService() {
		return commonService;
	}

	public void setCommonService(CommonService commonService) {
		this.commonService = commonService;
	}

	public GesteDAS getGesteDAS() {
		return gesteDAS;
	}

	public void setGesteDAS(GesteDAS gesteDAS) {
		this.gesteDAS = gesteDAS;
	}

	public CoutEnergieService getCoutEnergieService() {
		return coutEnergieService;
	}

	public void setCoutEnergieService(CoutEnergieService coutEnergieService) {
		this.coutEnergieService = coutEnergieService;
	}

	public RecupParamFinDAS getRecupParamFinDAS() {
		return recupParamFinDAS;
	}

	public void setRecupParamFinDAS(RecupParamFinDAS recupParamFinDAS) {
		this.recupParamFinDAS = recupParamFinDAS;
	}

	public Map<String, List<String>> getPeriodMap() {
		return gesteDAS.getPeriodMap();
	}

	public HashMap<TypeRenovBati, Geste> generateBatiGesteMap(List<Geste> gestesBati) {

		HashMap<TypeRenovBati, Geste> gesteMap = new HashMap<TypeRenovBati, Geste>();
		for (Geste geste : gestesBati) {

			gesteMap.put(geste.getTypeRenovBati(), geste);

		}

		return gesteMap;

	}

	protected HashMap<String, List<Geste>> getGesteBati(String idAggreg, Map<String, List<String>> periodeMap) {
		HashMap<String, List<Geste>> gestesBatiMap = new HashMap<String, List<Geste>>();
		List<Geste> listGest = gesteDAS.getGesteBatiData(idAggreg);

		// Extract de la bonne liste
		List<String> periodList = periodeMap.get(idAggreg.substring(START_ID_BRANCHE, START_ID_BRANCHE
				+ LENGTH_ID_BRANCHE)
				+ idAggreg.substring(START_ID_BAT, START_ID_BAT + LENGTH_ID_BAT));
		periodList.add("19");
		periodList.add("20");
		periodList.add("21");
		periodList.add("22");
		periodList.add("23");
		for (String periode : periodList) {

			Geste rienFaire = new Geste(INIT_STATE + "AUCUNE");
			rienFaire.setTypeRenovBati(TypeRenovBati.ETAT_INIT);
			rienFaire.setCoutGesteBati(BigDecimal.ZERO);
			rienFaire.setCoutGesteSys(BigDecimal.ZERO);
			rienFaire.setValeurCEE(BigDecimal.ZERO);
			rienFaire.setGainEner(BigDecimal.ZERO);
			rienFaire.setDureeBati(1);
			rienFaire.setExigence(Exigence.AUCUNE);
			List<Geste> gestes = new ArrayList<Geste>();
			gestes.add(rienFaire);
			gestesBatiMap.put(periode, gestes);
		}

		for (Geste geste : listGest) {
			gestesBatiMap
					.get(geste.getIdGesteAggreg().substring(START_PERIOD_DETAIL,
							START_PERIOD_DETAIL + LENGTH_PERIOD_DETAIL)).add(geste);
		}
		return gestesBatiMap;
	}

	@Override
	public List<Geste> cleanningGeste(HashMap<Integer, CoutEnergie> coutEnergieMap,
			HashMap<String, ResultConsoUClim> resultConsoUClimMap, HashMap<String, ResultConsoURt> resultConsoURtMap,
			Parc parc, BibliGeste bibliGeste, HashMap<String, BigDecimal> dvChauffMap,
			HashMap<TypeRenovBati, BigDecimal> dvGesteMap, int annee, int periode, Conso rdt, int anneeNTab,
			Reglementations reglementations, HashMap<String, ParamCoutEclVentil> coutsEclVentilMap,
			HashMap<String, ParamCoutEcs> coutEcsMap, HashMap<String, ParamPMConso> pmEcsNeufMap, Conso consoChauff,
			Conso ventil, Conso aux, HashMap<String, ParamBesoinsNeufs> bNeufsMap, Conso besoinInit,
			HashMap<String, ParamGainsUsages> gainsVentilationMap, HashMap<String, ParamRdtEcs> bibliRdtEcsMap,
			PBC tauxActu, HashMap<String, BigDecimal> evolCoutBati, HashMap<String, BigDecimal> evolCoutTechno,
			HashMap<String, Maintenance> maintenanceMap) {

		// Recuperation du niveau minimal attendu par la RT existant
		String periodeString = commonService.correspPeriodeFin(annee);
		String rtExistant = reglementations.getRt().get(periodeString).getExigence();
		
		
		
		// Recuperation du niveau minimal attendu par les obligations de travaux
		// (si il y en a, sinon null)
		String obligExig = NO_OBLIG;
		BigDecimal surfOblig = reglementations.getOblSurf().get(parc.getIdoccupant()).getPartSurf(periode);
		if (surfOblig.signum() != 0) {
			obligExig = reglementations.getOblExig().get(parc.getIdoccupant()).getExigence(periode);
		}
		// Application de la reglementation des 2014 dans le cas des batiments
		// de l'Etat
		
		
		if ((annee == 2014 || annee == 2015) && surfOblig.signum() == 0
				&& parc.getIdoccupant().equals(Occupant.ETAT.getCode())
				&& reglementations.getOblSurf().get(parc.getIdoccupant()).getPartSurf(2).signum() != 0) {
			surfOblig = reglementations.getOblSurf().get(parc.getIdoccupant()).getPartSurf(2);
			obligExig = reglementations.getOblExig().get(parc.getIdoccupant()).getExigence(2);
			
			
		}
		//LOG.debug(" {} ",surfOblig, surfOblig.signum());
		// Generation d'une HashMap ne contenant que les gestes sur le bati
		HashMap<TypeRenovBati, Geste> gesteBatiMap = generateBatiGesteMap(bibliGeste.getGestesBati().get(
				parc.getIdperiodedetail()));

		// Recuperation des gestes pour la bonne periode (systeme + bati)
		List<Geste> ensembleGestes = bibliGeste.getGesteBatiMap(parc.getIdperiodedetail(), periode);

		// Creation de la liste de resultats
		List<Geste> gestesFinaux = new ArrayList<Geste>();

		// Initialisation des variables
		BigDecimal anneeRenovSys = BigDecimal.ZERO;
		if (!parc.getAnneeRenovSys().equals(INIT_STATE)) {
			anneeRenovSys = new BigDecimal(parc.getAnneeRenovSys());
		}

		BigDecimal anneeRenovBat = BigDecimal.ZERO;
		if (!parc.getAnneeRenov().equals(INIT_STATE)) {
			anneeRenovBat = new BigDecimal(parc.getAnneeRenov());
		}

		BigDecimal anneeEnCours = new BigDecimal(annee);
		// 1er test : si le segment n'est pas chauffe alors on ne conserve que

		if (parc.getIdenergchauff().equals(Energies.NON_CHAUFFE.getCode().toString())) {
			// les gestes "Etat initial"

			nonChauffeGeste(ensembleGestes, gestesFinaux);
		} else {
			// Parcours de la bibliotheque de gestes
			BigDecimal rdtN = BigDecimal.ONE;
			if (rdt != null) {
				rdtN = rdt.getAnnee(anneeNTab - 1);
			}
			
			for (Geste geste : ensembleGestes) {
				Geste copyGeste = new Geste(geste);
				BigDecimal evolCoutBatUnit = getVariation(copyGeste.getTypeRenovBati().getLabel(), annee, evolCoutBati);

				// evolution des couts bati et systeme
				// on verifie qu'on est dans un cas ou les couts evoluent
				if (copyGeste.getTypeRenovBati() != TypeRenovBati.ETAT_INIT
						&& copyGeste.getTypeRenovBati() != TypeRenovBati.GTB) {
					copyGeste.setCoutGesteBati(copyGeste.getCoutGesteBati().multiply(evolCoutBatUnit,
							MathContext.DECIMAL32));
				}
				
				if (copyGeste.getTypeRenovSys() != TypeRenovSysteme.ETAT_INIT) {
					copyGeste.setCoutGesteSys(copyGeste.getCoutGesteSys().multiply(
							getVariation(copyGeste.getSysChaud(), annee, evolCoutTechno), MathContext.DECIMAL32));
					BigDecimal coutAdd = BigDecimal.ZERO;
					if (travauxAdd(copyGeste, parc)) {
						// si le booleen est vrai alors des travaux additionnels
						// sont a prevoir
						// Ceux-ci sont egaux aux couts initiaux du changement
						// de systeme
						coutAdd = copyGeste.getCoutGesteSys().multiply(
								getVariation(copyGeste.getSysChaud(), annee, evolCoutTechno), MathContext.DECIMAL32);
					}
					BigDecimal coutMaintenance = BigDecimal.ZERO;
					if (maintenanceMap.get(copyGeste.getSysChaud()) != null
							&& maintenanceMap.get(copyGeste.getSysChaud()).getPart() != null
							&& copyGeste.getCoutGesteSys() != null) {
						// Ajout des frais de maintenance (en % du prix initial
						// du systeme)
						// BV les couts de maintenance ne sont pas (1 + part maintenance) * cout mais partmaintenance*cout!
						BigDecimal partMaintenance = maintenanceMap.get(copyGeste.getSysChaud()).getPart();
						// coutMaintenance = (BigDecimal.ONE.add(partMaintenance)).multiply(copyGeste.getCoutGesteSys());
						coutMaintenance = partMaintenance.multiply(copyGeste.getCoutGesteSys());
					}
					coutAdd = coutAdd.add(coutMaintenance);
					
					// BV ces additionnels couts ne sont jamais utilises il me semble. Il n'y a pas de setter. test de modifier le code
					if(!coutAdd.equals(BigDecimal.ZERO) && coutAdd.compareTo(new BigDecimal("0.0001")) == 1){
					copyGeste.setCoutTravauxAddGeste(coutAdd); 
					// copyGeste.getCoutTravauxAddGeste(); 
					}
					
				}

				if (copyGeste.getTypeRenovSys().equals(TypeRenovSysteme.ETAT_INIT)) {
					copyGeste.setSysChaud(parc.getIdsyschaud());
					copyGeste.setEnergie(parc.getIdenergchauff());
					copyGeste.setRdt(rdtN);
				}

				// BV changement de gains pour les gestes et les systemes respectant la RT existant en 2018, 
				
				BigDecimal Gain = copyGeste.getGainEner();
				BigDecimal Rdt = copyGeste.getRdt();
				
				if(politiques.checkRTex==1){
					
				if (copyGeste.getExigence().equals(Exigence.RT_PAR_ELEMENT) && annee > 2017) {
					Gain = Gain.add(politiques.GainSupRTex);
					copyGeste.setGainEner(Gain);
					copyGeste.setCoutGesteBati(copyGeste.getCoutGesteBati().multiply((BigDecimal.ONE.add(politiques.GainSupRTex))));
				}
				
				
				if (copyGeste.getTypeRenovSys().equals(TypeRenovSysteme.CHGT_SYS) &&
						copyGeste.getSysChaud().substring(0,1).equals("0") && annee > 2017 && 
						!(copyGeste.getEnergie().contentEquals("03"))) {
					Rdt = Rdt.add(politiques.GainRdtSupRTex);
					copyGeste.setRdt(Rdt);
					copyGeste.setCoutGesteSys(copyGeste.getCoutGesteSys().multiply((BigDecimal.ONE.add(politiques.GainRdtSupRTex ))));
				} 
				}
				
				// 1eme test concernant les systemes : si le systeme n'est pas
				// en fin de vie alors on ne le change pas
				// 2eme test : si le parc n'a subit aucune renovation alors
				// on retient tous les gestes sauf dans le cas d'un batiment
				// neuf
				// Si une renovation a deja eu lieu sur le bati alors les
				// gestes
				// concernant le bati ne sont retenus qu'apres un certain
				// laps de temps

				if (createWorkList(parc, copyGeste, dvChauffMap, anneeRenovSys, anneeEnCours)
						&& etatInitialEtNeufs(parc, copyGeste, anneeRenovBat, anneeEnCours)) {
					boolean obligBool;
					if (parc.getTypeRenovBat().equals(TypeRenovBati.ETAT_INIT)) {
						obligBool = obligationExigence(parc, copyGeste, obligExig, surfOblig);
						decretTravaux(coutEnergieMap, parc, copyGeste, obligBool, resultConsoUClimMap,
								resultConsoURtMap, reglementations, coutsEclVentilMap, coutEcsMap, pmEcsNeufMap,
								consoChauff, anneeNTab, ventil, aux, bNeufsMap, gainsVentilationMap, bibliRdtEcsMap,
								annee, besoinInit, tauxActu);
						if (rtExistant(copyGeste, rtExistant, true)) {
							gestesFinaux.add(copyGeste);
						}
					} else {
						// 3eme test : si le parc a subit une renovation de type
						// "ENSBBC"
						boolean addToMap = ensBbcMethode(parc, dvGesteMap, copyGeste, anneeRenovBat, anneeEnCours);
						// 4eme test : si le parc a subit une renovation de type
						// "ENSMOD"
						addToMap = addToMap
								| ensModMethode(gesteBatiMap, parc, dvGesteMap, copyGeste, anneeRenovBat, anneeEnCours);
						// 5eme test : si le parc a subit une renovation de type
						// "FENBBC"
						addToMap = addToMap
								| fenBbcMethode(gesteBatiMap, parc, dvGesteMap, copyGeste, anneeRenovBat, anneeEnCours,
										evolCoutBatUnit);
						// 6eme test : si le parc a subit une renovation de type
						// "FEN_MURBBC"
						addToMap = addToMap
								| fenMurBbcMethode(gesteBatiMap, parc, dvGesteMap, copyGeste, anneeRenovBat,
										anneeEnCours, evolCoutBatUnit);
						// 7eme test : si le parc a subit une renovation de type
						// "FENMOD"
						addToMap = addToMap
								| fenModMethode(gesteBatiMap, parc, dvGesteMap, copyGeste, anneeRenovBat, anneeEnCours,
										evolCoutBatUnit);
						// 8eme test : si le parc a subit une renovation de type
						// "FEN_MURMOD"
						addToMap = addToMap
								| fenMurModMethode(gesteBatiMap, parc, dvGesteMap, copyGeste, anneeRenovBat,
										anneeEnCours, evolCoutBatUnit);

						// 9eme test : si le parc a subit une renovation de type
						// "GTB"
						addToMap = addToMap | GTBMethode(parc, copyGeste, anneeRenovBat, anneeEnCours);

						// 10eme test : le geste doit coller a minima aux
						// exigences
						// de la rt existant
						addToMap = addToMap && rtExistant(copyGeste, rtExistant, addToMap);

						if (addToMap) {
							// 11eme test : si le parc doit subir une obligation
							// de
							// travaux, alor les gestes retenus doivent
							// correspondre
							// a l'exigence de la reglementation
							obligBool = obligationExigence(parc, copyGeste, obligExig, surfOblig);
							// 12eme test : decret
							decretTravaux(coutEnergieMap, parc, copyGeste, obligBool, resultConsoUClimMap,
									resultConsoURtMap, reglementations, coutsEclVentilMap, coutEcsMap, pmEcsNeufMap,
									consoChauff, anneeNTab, ventil, aux, bNeufsMap, gainsVentilationMap,
									bibliRdtEcsMap, annee, besoinInit, tauxActu);
							gestesFinaux.add(copyGeste);

						}

					}
				}
			}
		}
		// LOG.debug("nombre de gestes retenus : {}", gestesFinaux.size());
		return gestesFinaux;

	}

	protected boolean travauxAdd(Geste copyGeste, Parc parcInit) {

		boolean travauxAdd = false;
		// Test : si l'ancien systeme de chauffage est un systeme decentralise
		if (parcInit.getIdsyschaud().equals(SysChaud.CASSETTE_RAYONNANTE.getCode())
				|| parcInit.getIdsyschaud().equals(SysChaud.CASSETTE_RAYONNANTE_PERFORMANT.getCode())
				|| parcInit.getIdsyschaud().equals(SysChaud.DRV.getCode())
				|| parcInit.getIdsyschaud().equals(SysChaud.DRV_PERFORMANT.getCode())
				|| parcInit.getIdsyschaud().equals(SysChaud.ELECTRIQUE_DIRECT.getCode())
				|| parcInit.getIdsyschaud().equals(SysChaud.ELECTRIQUE_DIRECT_PERFORMANT.getCode())
				|| parcInit.getIdsyschaud().equals(SysChaud.PAC.getCode())
				|| parcInit.getIdsyschaud().equals(SysChaud.PAC_PERFORMANT.getCode())
				|| parcInit.getIdsyschaud().equals(SysChaud.ROOFTOP.getCode())
				|| parcInit.getIdsyschaud().equals(SysChaud.ROOFTOP_PERFORMANT.getCode())) {
			// alors si le nouveau systeme est egalement un systeme
			// decentralise, il n'y aura pas de travaux additionnels
			if (copyGeste.getSysChaud().equals(SysChaud.CASSETTE_RAYONNANTE.getCode())
					|| copyGeste.getSysChaud().equals(SysChaud.CASSETTE_RAYONNANTE_PERFORMANT.getCode())
					|| copyGeste.getSysChaud().equals(SysChaud.DRV.getCode())
					|| copyGeste.getSysChaud().equals(SysChaud.DRV_PERFORMANT.getCode())
					|| copyGeste.getSysChaud().equals(SysChaud.ELECTRIQUE_DIRECT.getCode())
					|| copyGeste.getSysChaud().equals(SysChaud.ELECTRIQUE_DIRECT_PERFORMANT.getCode())
					|| copyGeste.getSysChaud().equals(SysChaud.PAC.getCode())
					|| copyGeste.getSysChaud().equals(SysChaud.PAC_PERFORMANT.getCode())
					|| copyGeste.getSysChaud().equals(SysChaud.ROOFTOP.getCode())
					|| copyGeste.getSysChaud().equals(SysChaud.ROOFTOP_PERFORMANT.getCode())) {
				travauxAdd = false;
			} else {
				// Sinon, des travaux additionnels sont a prevoir
				travauxAdd = true;
			}

			//BV sinon on part d'un systeme non centralise pour aller vers un centralise, on aussi des surcouts
			
		} else {
			if (copyGeste.getSysChaud().equals(SysChaud.CASSETTE_RAYONNANTE.getCode())
					|| copyGeste.getSysChaud().equals(SysChaud.CASSETTE_RAYONNANTE_PERFORMANT.getCode())
					|| copyGeste.getSysChaud().equals(SysChaud.DRV.getCode())
					|| copyGeste.getSysChaud().equals(SysChaud.DRV_PERFORMANT.getCode())
					|| copyGeste.getSysChaud().equals(SysChaud.ELECTRIQUE_DIRECT.getCode())
					|| copyGeste.getSysChaud().equals(SysChaud.ELECTRIQUE_DIRECT_PERFORMANT.getCode())
					|| copyGeste.getSysChaud().equals(SysChaud.PAC.getCode())
					|| copyGeste.getSysChaud().equals(SysChaud.PAC_PERFORMANT.getCode())
					|| copyGeste.getSysChaud().equals(SysChaud.ROOFTOP.getCode())
					|| copyGeste.getSysChaud().equals(SysChaud.ROOFTOP_PERFORMANT.getCode())) {
				travauxAdd = true;
			} else {
				// Sinon, des travaux additionnels sont a prevoir
				travauxAdd = false;
			}
			
			
		}

		return travauxAdd;
	}

	protected void decretTravaux(HashMap<Integer, CoutEnergie> coutEnergieMap, Parc parc, Geste copyGeste,
			boolean obligBool, HashMap<String, ResultConsoUClim> resultConsoUClimMap,
			HashMap<String, ResultConsoURt> resultConsoURtMap, Reglementations reglementations,
			HashMap<String, ParamCoutEclVentil> coutsEclVentilMap, HashMap<String, ParamCoutEcs> coutEcsMap,
			HashMap<String, ParamPMConso> pmEcsNeufMap, Conso consoChauff, int anneeNTab, Conso ventil, Conso aux,
			HashMap<String, ParamBesoinsNeufs> bNeufsMap, HashMap<String, ParamGainsUsages> gainsVentilationMap,
			HashMap<String, ParamRdtEcs> bibliRdtEcsMap, int annee, Conso besoinInit, PBC tauxActu) {
		// Recuperation des parametres d'application du decret
		BigDecimal partDecret = reglementations.getDecret().get(parc.getIdbranche() + parc.getIdoccupant())
				.getPartSurf();
		BigDecimal triDecret = reglementations.getDecret().get(parc.getIdbranche() + parc.getIdoccupant()).getTri();
		BigDecimal coutMaxDecret = reglementations.getDecret().get(parc.getIdbranche() + parc.getIdoccupant())
				.getCoutMax();
		BigDecimal gainMinDecret = reglementations.getDecret().get(parc.getIdbranche() + parc.getIdoccupant())
				.getGainMin();
		BigDecimal anneeDebut = reglementations.getDecret().get(parc.getIdbranche() + parc.getIdoccupant()).getDebut();
		BigDecimal anneeFin = reglementations.getDecret().get(parc.getIdbranche() + parc.getIdoccupant()).getFin();
		// Si le decret s'applique a la branche alors
		if (partDecret.signum() != 0 && anneeDebut.compareTo(new BigDecimal(annee)) <= 0
				&& anneeFin.compareTo(new BigDecimal(annee)) >= 0) {
			// initialisation d'un booleen
			boolean decretBool = true;
			// 1er test : periode de construction
			decretBool = decretBool && testPeriodeCstrDecret(parc);
			if (decretBool) {
				// Calcul total du cout du geste (en euros/m²)
				BigDecimal coutTot = calcCoutGesteDecret(parc, copyGeste, coutsEclVentilMap, coutEcsMap, pmEcsNeufMap);
				// 2eme test : cout max admissible
				decretBool = decretBool && testSurfMaxDecret(coutMaxDecret, coutTot);
				if (decretBool) {
					// Calcul de la conso totale d'energie primaire
					ResultConsoDecret consoInitEP = calcConsoEPDecret(parc, resultConsoUClimMap, resultConsoURtMap,
							consoChauff, anneeNTab, ventil, aux);
					ResultConsoDecret consoNewEP = calcNewConsoRt(consoInitEP, consoChauff, parc, consoInitEP,
							copyGeste, bNeufsMap, gainsVentilationMap, bibliRdtEcsMap, annee, anneeNTab, pmEcsNeufMap,
							besoinInit);
					// 3eme test : gain minimal
					decretBool = decretBool
							&& testGainMinDecret(parc, consoInitEP, copyGeste, gainMinDecret, consoNewEP, annee);
					if (decretBool) {
						// Calcul des economies d'energie (en kWh/m².an)
						BigDecimal ecoEnergie = calcEcoEnergie(coutEnergieMap, parc, copyGeste, consoInitEP,
								consoNewEP, pmEcsNeufMap, annee, anneeNTab);
						// 4eme test : temps de retour sur investissement
						decretBool = decretBool && testTRIDecret(triDecret, parc, ecoEnergie, coutTot, tauxActu);
					}
				}
			}
			// Ajout de la reglementation dans copyGeste
			if (decretBool) {
				if (obligBool) {
					copyGeste.setReglementation(ReglementationName.OBLIG_TRAVAUX_DECRET.getLabel());
				} else {
					copyGeste.setReglementation(ReglementationName.DECRET.getLabel());
				}

			}
		}

	}

	protected boolean testPeriodeCstrDecret(Parc parc) {

		return parc.getIdperiodesimple().equals(Period.PERIODE_BEFORE_1980.getCode())
				|| parc.getIdperiodesimple().equals(Period.PERIODE_1981_1998.getCode())

				|| parc.getIdperiodesimple().equals(Period.PERIODE_1999_2008.getCode());
	}

	protected boolean testTRIDecret(BigDecimal triDecret, Parc parc, BigDecimal ecoEnergie, BigDecimal coutTot,
			PBC tauxActu) {

		if (ecoEnergie != null && ecoEnergie.signum() != 0) {
			BigDecimal tri = tempsRetourInvestissement(coutTot, ecoEnergie, tauxActu);
			if (tri.compareTo(triDecret) <= 0) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}

	}

	protected BigDecimal calcEcoEnergie(HashMap<Integer, CoutEnergie> coutEnergieMap, Parc parc, Geste copyGeste,
			ResultConsoDecret consoTot, ResultConsoDecret consoNew, HashMap<String, ParamPMConso> pmEcsNeufMap,
			int annee, int anneeNTab) {

		BigDecimal coutRtOld = (consoTot.getConsoAuxEF().add(consoTot.getConsoClimEF())
				.add(consoTot.getConsoVentilEF()).add(consoTot.getConsoEclairageEF())).multiply(coutEnergieMap.get(
				annee).getEnergie(Energies.getEnumName(Energies.ELECTRICITE.getCode())));
		BigDecimal coutRtNew = (consoNew.getConsoAuxEF().add(consoNew.getConsoClimEF())
				.add(consoNew.getConsoVentilEF()).add(consoNew.getConsoEclairageEF())).multiply(coutEnergieMap.get(
				annee).getEnergie(Energies.getEnumName(Energies.ELECTRICITE.getCode())));
		BigDecimal coutChauffOld = consoTot.getConsoChauffEF().multiply(
				coutEnergieMap.get(annee).getEnergie(Energies.getEnumName(parc.getIdenergchauff())));
		BigDecimal coutChauffNew = consoNew.getConsoChauffEF().multiply(
				coutEnergieMap.get(annee).getEnergie(Energies.getEnumName(copyGeste.getEnergie())));
		BigDecimal coutECSOld = BigDecimal.ZERO;
		BigDecimal coutECSNew = BigDecimal.ZERO;

		for (String energie : pmEcsNeufMap.get(parc.getIdbranche()).getEnergie().keySet()) {
			// boucle sur les parts de marche des energies d'ecs
			String energ = StringUtils.stripAccents(energie.toUpperCase());
			String codeEnerg = commonService.codeCreateEnerg(energ);
			BigDecimal part = pmEcsNeufMap.get(parc.getIdbranche()).getEnergie().get(energ);
			BigDecimal coutEnerg = coutEnergieMap.get(annee).getEnergie(Energies.getEnumName(codeEnerg));
			coutECSOld = coutECSOld.add(consoTot.getConsoECSEF().multiply(part).multiply(coutEnerg));
			coutECSNew = coutECSNew.add(consoNew.getConsoECSEF().multiply(part).multiply(coutEnerg));

		}
		BigDecimal coutTotOld = coutRtOld.add(coutChauffOld).add(coutECSOld);
		BigDecimal coutTotNew = coutRtNew.add(coutChauffNew).add(coutECSNew);
		BigDecimal economie = BigDecimal.ZERO;
		if ((coutTotOld.subtract(coutTotNew)).signum() >= 0) {
			economie = coutTotOld.subtract(coutTotNew);
		}
		BigDecimal economieSurfacique = economie.divide(parc.getAnnee(anneeNTab - 1), MathContext.DECIMAL32);

		return economieSurfacique;
	}

	protected ResultConsoDecret calcConsoEPDecret(Parc parc, HashMap<String, ResultConsoUClim> resultConsoUClimMap,
			HashMap<String, ResultConsoURt> resultConsoURtMap, Conso consoChauff, int anneeNTab, Conso ventil, Conso aux) {
		// Calcul du gain total du geste
		ResultConsoDecret resultConso = new ResultConsoDecret();
		// Chauffage
		BigDecimal facteurEP = facteurEPChauff(consoChauff.getIdenergchauff());
		if (consoChauff.getAnnee(anneeNTab - 1) != null) {
			resultConso.setConsoChauffEF(consoChauff.getAnnee(anneeNTab - 1));
			resultConso.setConsoChauffEP(consoChauff.getAnnee(anneeNTab - 1).multiply(facteurEP));
		}
		ResultConsoUClim resultClim = resultConsoUClimMap.get(parc.getIdagreg() + parc.getIdsysfroid() + INIT_STATE
				+ TypeRenovBati.ETAT_INIT);
		// Climatisation
		if (resultClim.getConsoUClimEF(anneeNTab - 1) != null) {
			resultConso
					.setConsoClimEP(resultClim.getConsoUClimEP(anneeNTab - 1).multiply(parc.getAnnee(anneeNTab - 1)));
			resultConso.setConsoClimEF((resultClim.getConsoUClimEF(anneeNTab - 1)).multiply(parc
					.getAnnee(anneeNTab - 1)));
		}
		// TODO : cas d'un batiment ayant deja eu un geste de travaux durant la
		// periode d'application du decret
		String anneeRenov = INIT_STATE;
		TypeRenovBati typeRenovBat = TypeRenovBati.ETAT_INIT;
		if (parc.getTypeRenovBat().equals(TypeRenovBati.ENSBBC)) {
			anneeRenov = parc.getAnneeRenov();
			typeRenovBat = parc.getTypeRenovBat();
		}
		ResultConsoURt resultECSEcl = resultConsoURtMap.get(parc.getIdagreg() + anneeRenov + typeRenovBat);
		// Eclairage
		if (resultECSEcl.getConsoUEclairageEF(anneeNTab - 1) != null) {
			resultConso.setConsoEclairageEP(resultECSEcl.getConsoUEclairageEP(anneeNTab - 1).multiply(
					parc.getAnnee(anneeNTab - 1)));
			resultConso.setConsoEclairageEF((resultECSEcl.getConsoUEclairageEF(anneeNTab - 1)).multiply(parc
					.getAnnee(anneeNTab - 1)));
		}
		// ECS
		if (resultECSEcl.getConsoUEcsEF(anneeNTab - 1) != null) {
			resultConso
					.setConsoECSEP(resultECSEcl.getConsoUEcsEP(anneeNTab - 1).multiply(parc.getAnnee(anneeNTab - 1)));
			resultConso
					.setConsoECSEF(resultECSEcl.getConsoUEcsEF(anneeNTab - 1).multiply(parc.getAnnee(anneeNTab - 1)));
		}
		// Ventilation
		if (ventil != null && ventil.getAnnee(anneeNTab - 1) != null) {
			resultConso.setConsoVentilEF(ventil.getAnnee(anneeNTab - 1));
			resultConso.setConsoVentilEP(ventil.getAnnee(anneeNTab - 1).multiply(FACTEUR_EP));
		}
		// Auxiliaires
		if (aux != null && aux.getAnnee(anneeNTab - 1) != null) {
			resultConso.setConsoAuxEF(aux.getAnnee(anneeNTab - 1));
			resultConso.setConsoAuxEP(aux.getAnnee(anneeNTab - 1).multiply(FACTEUR_EP));
		}
		return resultConso;
	}

	protected boolean testSurfMaxDecret(BigDecimal coutMaxDecret, BigDecimal coutTot) {

		if (coutTot.compareTo(coutMaxDecret) <= 0) {
			return true;
		} else {
			return false;
		}
	}

	protected boolean testGainMinDecret(Parc parc, ResultConsoDecret consoTotEP, Geste copyGeste, BigDecimal gainMin,
			ResultConsoDecret consoNewTot, int annee) {

		BigDecimal oldConsoEP = consoTotEP.getConsoAuxEP().add(consoTotEP.getConsoClimEP())
				.add(consoTotEP.getConsoVentilEP()).add(consoTotEP.getConsoChauffEP())
				.add(consoTotEP.getConsoEclairageEP()).add(consoTotEP.getConsoECSEP());
		BigDecimal newConsoEP = consoNewTot.getConsoAuxEP().add(consoNewTot.getConsoClimEP())
				.add(consoNewTot.getConsoVentilEP()).add(consoNewTot.getConsoChauffEP())
				.add(consoNewTot.getConsoEclairageEP()).add(consoNewTot.getConsoECSEP());
		BigDecimal gainCalc = (BigDecimal.ONE.subtract((newConsoEP.divide(oldConsoEP, MathContext.DECIMAL32))));
		if (gainCalc.signum() < 0) {
			gainCalc = BigDecimal.ZERO;
		}
		if (gainCalc.compareTo(gainMin) >= 0) {
			return true;
		} else {

			return false;
		}

	}

	protected ResultConsoDecret calcNewConsoRt(ResultConsoDecret consoInit, Conso consoChauff, Parc parc,
			ResultConsoDecret consoTotInit, Geste copyGeste, HashMap<String, ParamBesoinsNeufs> bNeufsMap,
			HashMap<String, ParamGainsUsages> gainsVentilationMap, HashMap<String, ParamRdtEcs> bibliRdtEcsMap,
			int annee, int anneeNTab, HashMap<String, ParamPMConso> pmEcsNeufMap, Conso besoinChauff) {

		ResultConsoDecret result = new ResultConsoDecret();
		if (copyGeste.getTypeRenovBati().equals(TypeRenovBati.ETAT_INIT)) {
			result = new ResultConsoDecret(consoInit);
			// Recalcul de la conso de chauffage
			BigDecimal facteurEP = facteurEPChauff(copyGeste.getEnergie());
			result.setConsoChauffEP((besoinChauff.getAnnee(anneeNTab - 1).multiply(facteurEP)).divide(
					copyGeste.getRdt(), MathContext.DECIMAL32));
			result.setConsoChauffEF((besoinChauff.getAnnee(anneeNTab - 1)).divide(copyGeste.getRdt(),
					MathContext.DECIMAL32));

		} else if (copyGeste.getTypeRenovBati().equals(TypeRenovBati.ENSBBC)) {

			// Calcul ECS (prise en compte du solaire??)
			BigDecimal newBesoinEcsEP = BigDecimal.ZERO;
			BigDecimal newBesoinEcsEF = BigDecimal.ZERO;
			BigDecimal besoinTemp = bNeufsMap.get(commonService.concatID(parc, Usage.ECS.getLabel())).getPeriode(
					commonService.correspPeriode(annee));
			BigDecimal consoECSEP = BigDecimal.ZERO;
			BigDecimal consoECSEF = BigDecimal.ZERO;
			for (String energie : pmEcsNeufMap.get(parc.getIdbranche()).getEnergie().keySet()) {
				// boucle sur les parts de marche des energies d'ecs
				String energ = StringUtils.stripAccents(energie.toUpperCase());
				String codeEnerg = commonService.codeCreateEnerg(energ);
				BigDecimal part = pmEcsNeufMap.get(parc.getIdbranche()).getEnergie().get(energ);
				BigDecimal newRdtEcs = bibliRdtEcsMap.get(parc.getIdbranche() + codeEnerg).getRdt(
						commonService.correspPeriode(annee));
				BigDecimal facteurEP = facteurEPChauff(codeEnerg);
				newBesoinEcsEP = (besoinTemp.multiply(part).multiply(facteurEP));
				consoECSEP = consoECSEP.add((newBesoinEcsEP.divide(newRdtEcs, MathContext.DECIMAL32)).multiply(parc
						.getAnnee(anneeNTab - 1)));
				newBesoinEcsEF = (besoinTemp.multiply(part));
				consoECSEF = consoECSEF.add((newBesoinEcsEF.divide(newRdtEcs, MathContext.DECIMAL32)).multiply(parc
						.getAnnee(anneeNTab - 1)));
			}
			result.setConsoECSEF(consoECSEF);
			result.setConsoECSEP(consoECSEP);
			// Calcul ventil
			BigDecimal gainVentil = gainsVentilationMap.get(parc.getIdbranche() + Usage.VENTILATION.getLabel())
					.getPeriode(commonService.correspPeriode(annee));
			result.setConsoVentilEP(consoTotInit.getConsoVentilEP().multiply(BigDecimal.ONE.subtract(gainVentil)));
			result.setConsoVentilEF(consoTotInit.getConsoVentilEF().multiply(BigDecimal.ONE.subtract(gainVentil)));
			// Calcul eclairage
			result.setConsoEclairageEP((bNeufsMap.get(commonService.concatID(parc, Usage.ECLAIRAGE.getLabel()))
					.getPeriode(commonService.correspPeriode(annee)).multiply(FACTEUR_EP)).multiply(parc
					.getAnnee(anneeNTab - 1)));
			result.setConsoEclairageEF((bNeufsMap.get(commonService.concatID(parc, Usage.ECLAIRAGE.getLabel()))
					.getPeriode(commonService.correspPeriode(annee))).multiply(parc.getAnnee(anneeNTab - 1)));
			// Calcul chauffage
			BigDecimal facteurEP = facteurEPChauff(copyGeste.getEnergie());
			BigDecimal besoinNewEF = (besoinChauff.getAnnee(anneeNTab - 1).multiply(BigDecimal.ONE.subtract(copyGeste
					.getGainEner())));
			BigDecimal besoinNewEP = (besoinChauff.getAnnee(anneeNTab - 1).multiply(BigDecimal.ONE.subtract(copyGeste
					.getGainEner()))).multiply(facteurEP);

			result.setConsoChauffEF(besoinNewEF.divide(copyGeste.getRdt(), MathContext.DECIMAL32));
			result.setConsoChauffEP(besoinNewEP.divide(copyGeste.getRdt(), MathContext.DECIMAL32));
			// Calcul clim
			result.setConsoClimEF(consoInit.getConsoClimEF());
			result.setConsoClimEP(consoInit.getConsoClimEP());
			// Calcul auxiliaires
			result.setConsoAuxEF(consoInit.getConsoAuxEF().multiply(BigDecimal.ONE.subtract(copyGeste.getGainEner())));
			result.setConsoAuxEP(consoInit.getConsoAuxEP().multiply(BigDecimal.ONE.subtract(copyGeste.getGainEner())));

		} else {
			result = new ResultConsoDecret(consoInit);
			// ReCalcul du chauffage
			BigDecimal facteurEP = facteurEPChauff(copyGeste.getEnergie());
			BigDecimal besoinNewEP = (besoinChauff.getAnnee(anneeNTab - 1).multiply(BigDecimal.ONE.subtract(copyGeste
					.getGainEner()))).multiply(facteurEP);
			BigDecimal besoinNewEF = (besoinChauff.getAnnee(anneeNTab - 1).multiply(BigDecimal.ONE.subtract(copyGeste
					.getGainEner())));

			result.setConsoChauffEP(besoinNewEP.divide(copyGeste.getRdt(), MathContext.DECIMAL32));
			result.setConsoChauffEF(besoinNewEF.divide(copyGeste.getRdt(), MathContext.DECIMAL32));
			// Calcul auxiliaires
			result.setConsoAuxEF(consoInit.getConsoAuxEF().multiply(BigDecimal.ONE.subtract(copyGeste.getGainEner())));
			result.setConsoAuxEP(consoInit.getConsoAuxEP().multiply(BigDecimal.ONE.subtract(copyGeste.getGainEner())));
		}
		return result;
	}

	protected BigDecimal facteurEPChauff(String codeEnerg) {
		BigDecimal facteurEP = BigDecimal.ONE;
		if (codeEnerg.equals(Energies.ELECTRICITE.getCode())) {
			facteurEP = FACTEUR_EP;
		}
		return facteurEP;
	}

	protected BigDecimal calcCoutGesteDecret(Parc parc, Geste copyGeste,
			HashMap<String, ParamCoutEclVentil> coutsEclVentilMap, HashMap<String, ParamCoutEcs> coutEcsMap,
			HashMap<String, ParamPMConso> pmEcsNeufMap) {
		// Retourne un cout par m²
		BigDecimal coutTot = BigDecimal.ZERO;
		BigDecimal coutAdd = BigDecimal.ZERO;
		BigDecimal coutInv = BigDecimal.ZERO;
		if (copyGeste.getTypeRenovBati().equals(TypeRenovBati.ENSBBC)) {
			BigDecimal coutECS = BigDecimal.ZERO;
			for (String energie : pmEcsNeufMap.get(parc.getIdbranche()).getEnergie().keySet()) {
				// boucle sur les parts de marche des energies d'ecs
				String energ = StringUtils.stripAccents(energie.toUpperCase());
				String codeEnerg = commonService.codeCreateEnerg(energ);
				BigDecimal part = pmEcsNeufMap.get(parc.getIdbranche()).getEnergie().get(energ);
				BigDecimal coutSys = coutEcsMap.get(codeEnerg + "Performant").getCout();
				coutECS = coutECS.add(part.multiply(coutSys));
			}
			BigDecimal coutEcl = coutsEclVentilMap.get(Usage.ECLAIRAGE.getLabel() + parc.getIdbranche()).getCout();

			if (copyGeste.getCoutTravauxAddGeste() != null) {
				coutAdd = copyGeste.getCoutTravauxAddGeste();
			}
			if (copyGeste.getCoutGesteBati() != null && copyGeste.getCoutGesteSys() != null) {
				coutInv = copyGeste.getCoutGesteBati().add(copyGeste.getCoutGesteSys());
			}
			coutTot = coutEcl.add(coutECS).add(coutInv).add(coutAdd);
		} else {

			if (copyGeste.getCoutTravauxAddGeste() != null) {
				coutAdd = copyGeste.getCoutTravauxAddGeste();
			}
			if (copyGeste.getCoutGesteBati() != null && copyGeste.getCoutGesteSys() != null) {
				coutInv = copyGeste.getCoutGesteBati().add(copyGeste.getCoutGesteSys());
			}
			coutTot = coutInv.add(coutAdd);

		}
		return coutTot;
	}

	protected boolean rtExistant(Geste geste, String rtExistant, boolean addToMap) {
		// si la RT existant est a minima un geste modeste alors on garde tous
		// les gestes
		if (rtExistant.equals("MOD") && addToMap) {
			return true;
		}
		// si la RT existant est a minima BBC alors on ne garde que les gestes
		// BBC
		else if ((geste.getTypeRenovBati().equals(TypeRenovBati.ENSBBC)
				|| geste.getTypeRenovBati().equals(TypeRenovBati.FEN_MURBBC)
				|| geste.getTypeRenovBati().equals(TypeRenovBati.FENBBC)
				|| geste.getTypeRenovBati().equals(TypeRenovBati.GTB) || geste.getTypeRenovBati().equals(
				TypeRenovBati.ETAT_INIT))
				&& addToMap) {
			return true;
		}
		return false;
	}

	protected boolean obligationExigence(Parc parc, Geste geste, String obligExig, BigDecimal surfOblig) {
		// si l'exigence minimale de l'obligation est TOUS ou bien si elle est
		// egale au geste
		// si la surface touchee est differente de 0
		// si le geste n'est pas un geste "ne rien faire"
		// si le batiment n'a pas ete construit apres 2009
		boolean obligBool = false;
		if ((obligExig.equals(GesteObligation.TOUS.toString()) || obligExig.equals(geste.getTypeRenovBati().getLabel()))
				&& surfOblig.signum() != 0
				&& !geste.getTypeRenovBati().equals(TypeRenovBati.ETAT_INIT)
				&& (parc.getIdperiodesimple().equals(Period.PERIODE_BEFORE_1980.getCode())
						|| parc.getIdperiodesimple().equals(Period.PERIODE_1999_2008.getCode()) || parc
						.getIdperiodesimple().equals(Period.PERIODE_1981_1998.getCode()))) {
			geste.setReglementation(ReglementationName.OBLIG_TRAVAUX.getLabel());
			obligBool = true;

		} else {
			geste.setReglementation(ReglementationName.AUCUNE.getLabel());
			obligBool = false;
		}

		return obligBool;
	}

	protected boolean GTBMethode(Parc parc, Geste geste, BigDecimal anneeRenovBat, BigDecimal anneeEnCours) {

		if (parc.getTypeRenovBat().equals(TypeRenovBati.GTB) && !geste.getTypeRenovBati().equals(TypeRenovBati.GTB)) {
			// Si un geste GTB a deja ete mene alors il ne peut plus l'etre

			return true;

		} else {

			return false;
		}
	}

	protected boolean fenMurModMethode(HashMap<TypeRenovBati, Geste> gesteBatiMap, Parc parc,
			HashMap<TypeRenovBati, BigDecimal> dvGesteMap, Geste geste, BigDecimal anneeRenovBat,
			BigDecimal anneeEnCours, BigDecimal evolCoutBatUnit) {
		if (parc.getTypeRenovBat().equals(TypeRenovBati.FEN_MURMOD)
				&& (anneeRenovBat.add(dvGesteMap.get(TypeRenovBati.FEN_MURMOD))).compareTo(anneeEnCours) > 0) {
			// Seuls les gestes sur les systemes sont conserves ainsi que ENSMOD
			if (geste.getTypeRenovBati().equals(TypeRenovBati.ETAT_INIT)
					|| geste.getTypeRenovBati().equals(TypeRenovBati.ENSMOD)
					|| geste.getTypeRenovBati().equals(TypeRenovBati.GTB)) {
				// Calcul du cout initial des fenetres et de l'isolation des
				// murs
				BigDecimal coutInit = gesteBatiMap.get(TypeRenovBati.FEN_MURMOD).getCoutGesteBati();
				// Calcul du gain initial realise en changeant les fenetres
				// et en isolant les murs
				BigDecimal gainInit = gesteBatiMap.get(TypeRenovBati.FEN_MURMOD).getGainEner();
				// Calcul du nouveau gain et du nouveau cout dans le cas ou
				// est realisee une renovation ENSMOD
				if (geste.getTypeRenovBati().equals(TypeRenovBati.ENSMOD)) {

					geste.setCoutGesteBati(geste.getCoutGesteBati().subtract(
							coutInit.multiply(evolCoutBatUnit, MathContext.DECIMAL32)));
					geste.setGainEner(geste.getGainEner().subtract(gainInit));
				}
				return true;
			}

		} else if (parc.getTypeRenovBat().equals(TypeRenovBati.FEN_MURMOD)
				&& (anneeRenovBat.add(dvGesteMap.get(TypeRenovBati.FEN_MURMOD))).compareTo(anneeEnCours) <= 0) {
			BigDecimal gainInit = gesteBatiMap.get(TypeRenovBati.FEN_MURMOD).getGainEner();
			// Sinon tous les gestes sont conserves
			// Les gestes FEN_MURMOD et FENMOD ont des gains nulls
			// Le gain du geste ENSMOD est diminue

			if (geste.getExigence().equals(Exigence.RT_PAR_ELEMENT)) {
				// Calcul du gain initial realise en changeant les fenetres
				// et en isolant les murs
				BigDecimal newGain = BigDecimal.ZERO;
				if (geste.getTypeRenovBati().equals(TypeRenovBati.ENSMOD)) {
					newGain = geste.getGainEner().subtract(gainInit);
				}
				geste.setGainEner(newGain);

			}
			return true;

		}

		return false;
	}

	protected boolean fenModMethode(HashMap<TypeRenovBati, Geste> gesteBatiMap, Parc parc,
			HashMap<TypeRenovBati, BigDecimal> dvGesteMap, Geste geste, BigDecimal anneeRenovBat,
			BigDecimal anneeEnCours, BigDecimal evolCoutBatUnit) {
		// si le parc a deja subit une renovation du meme type et que sa duree
		// de vie n'est pas terminee
		if (parc.getTypeRenovBat().equals(TypeRenovBati.FENMOD)
				&& (anneeRenovBat.add(dvGesteMap.get(TypeRenovBati.FENMOD))).compareTo(anneeEnCours) > 0) {
			// Seuls les gestes sur les systemes sont conserves ainsi que tous
			// sauf FENMOD, FENBBC et FEN_MURBBC
			if (!geste.getTypeRenovBati().equals(TypeRenovBati.FENMOD)
					&& !geste.getTypeRenovBati().equals(TypeRenovBati.FENBBC)
					&& !geste.getTypeRenovBati().equals(TypeRenovBati.FEN_MURBBC)) {
				// Calcul du cout initial des fenetres
				BigDecimal coutInit = gesteBatiMap.get(TypeRenovBati.FENMOD).getCoutGesteBati();
				// Calcul du gain initial realise en changeant les fenetres
				BigDecimal gainInit = gesteBatiMap.get(TypeRenovBati.FENMOD).getGainEner();
				// Calcul du nouveau gain et du nouveau cout dans le cas ou
				// est realisee une renovation ENSBBC
				if (!geste.getTypeRenovBati().equals(TypeRenovBati.ETAT_INIT)
						&& !geste.getTypeRenovBati().equals(TypeRenovBati.GTB)) {
					geste.setCoutGesteBati(geste.getCoutGesteBati().subtract(
							coutInit.multiply(evolCoutBatUnit, MathContext.DECIMAL32)));
					geste.setGainEner(geste.getGainEner().subtract(gainInit));
				}
				return true;

			}

		} // si la duree de vie arrive a echeance
		else if (parc.getTypeRenovBat().equals(TypeRenovBati.FENMOD)
				&& (anneeRenovBat.add(dvGesteMap.get(TypeRenovBati.FENMOD))).compareTo(anneeEnCours) <= 0) {
			BigDecimal gainInit = gesteBatiMap.get(TypeRenovBati.FENMOD).getGainEner();
			// Tous les gestes sont conserves

			if (!geste.getTypeRenovBati().equals(TypeRenovBati.GTB)) {
				BigDecimal newGain = BigDecimal.ZERO;
				if ((geste.getGainEner().subtract(gainInit)).signum() > 0) {
					newGain = geste.getGainEner().subtract(gainInit);
				}
				geste.setGainEner(newGain);
			}
			return true;

		}
		return false;
	}

	protected boolean fenMurBbcMethode(HashMap<TypeRenovBati, Geste> gesteBatiMap, Parc parc,
			HashMap<TypeRenovBati, BigDecimal> dvGesteMap, Geste geste, BigDecimal anneeRenovBat,
			BigDecimal anneeEnCours, BigDecimal evolCoutBatUnit) {

		if (parc.getTypeRenovBat().equals(TypeRenovBati.FEN_MURBBC)
				&& (anneeRenovBat.add(dvGesteMap.get(TypeRenovBati.FEN_MURBBC))).compareTo(anneeEnCours) > 0) {
			// Seuls les gestes sur les systemes sont conserves ainsi que ENSBBC
			if (geste.getTypeRenovBati().equals(TypeRenovBati.ETAT_INIT)
					|| geste.getTypeRenovBati().equals(TypeRenovBati.ENSBBC)
					|| geste.getTypeRenovBati().equals(TypeRenovBati.GTB)) {

				// Calcul du nouveau gain et du nouveau cout dans le cas ou
				// est realisee une renovation ENSBBC
				if (geste.getTypeRenovBati().equals(TypeRenovBati.ENSBBC)) {
					// Calcul du gain initial realise en changeant les fenetres
					BigDecimal gainInit = gesteBatiMap.get(TypeRenovBati.FEN_MURBBC).getGainEner();
					// Calcul du cout initial des fenetres
					BigDecimal coutInit = gesteBatiMap.get(TypeRenovBati.FEN_MURBBC).getCoutGesteBati();
					geste.setCoutGesteBati(geste.getCoutGesteBati().subtract(
							coutInit.multiply(evolCoutBatUnit, MathContext.DECIMAL32)));
					geste.setGainEner(geste.getGainEner().subtract(gainInit));
				}
				return true;

			}
		} else if (parc.getTypeRenovBat().equals(TypeRenovBati.FEN_MURBBC)
				&& (anneeRenovBat.add(dvGesteMap.get(TypeRenovBati.FEN_MURBBC))).compareTo(anneeEnCours) <= 0) {

			// Sinon seuls les gestes d'exigence "BBC" et ne rien faire sont
			// conserves

			if (geste.getExigence().equals(Exigence.BBC_RENOVATION) || geste.getExigence().equals(Exigence.AUCUNE)
					|| geste.getExigence().equals(Exigence.GTB)) {
				// Calcul du gain initial realise en changeant les fenetres

				BigDecimal newGain = BigDecimal.ZERO;
				if (geste.getTypeRenovBati().equals(TypeRenovBati.ENSBBC)) {
					// Calcul du gain initial realise en changeant les fenetres
					BigDecimal gainInit = gesteBatiMap.get(TypeRenovBati.FEN_MURBBC).getGainEner();
					geste.setGainEner(geste.getGainEner().subtract(gainInit));
				}
				geste.setGainEner(newGain);
				return true;
			}
		}
		return false;
	}

	protected boolean fenBbcMethode(HashMap<TypeRenovBati, Geste> gesteBatiMap, Parc parc,
			HashMap<TypeRenovBati, BigDecimal> dvGesteMap, Geste geste, BigDecimal anneeRenovBat,
			BigDecimal anneeEnCours, BigDecimal evolCoutBatUnit) {
		if (parc.getTypeRenovBat().equals(TypeRenovBati.FENBBC)
				&& (anneeRenovBat.add(dvGesteMap.get(TypeRenovBati.FENBBC))).compareTo(anneeEnCours) > 0) {
			// Seuls les gestes sur les systemes sont conserves ainsi que ENSBBC
			if (geste.getTypeRenovBati().equals(TypeRenovBati.ETAT_INIT)
					|| geste.getTypeRenovBati().equals(TypeRenovBati.ENSBBC)
					|| geste.getTypeRenovBati().equals(TypeRenovBati.GTB)) {

				// Calcul du cout initial des fenetres
				BigDecimal coutInit = gesteBatiMap.get(TypeRenovBati.FENBBC).getCoutGesteBati();
				// Calcul du gain initial realise en changeant les fenetres
				BigDecimal gainInit = gesteBatiMap.get(TypeRenovBati.FENBBC).getGainEner();
				// Calcul du nouveau gain et du nouveau cout dans le cas ou
				// est realisee une renovation ENSBBC
				if (geste.getTypeRenovBati().equals(TypeRenovBati.ENSBBC)) {
					geste.setCoutGesteBati(geste.getCoutGesteBati().subtract(
							coutInit.multiply(evolCoutBatUnit, MathContext.DECIMAL32)));
					geste.setGainEner(geste.getGainEner().subtract(gainInit));
				}
				return true;

			}

		} else if (parc.getTypeRenovBat().equals(TypeRenovBati.FENBBC)
				&& (anneeRenovBat.add(dvGesteMap.get(TypeRenovBati.FENBBC))).compareTo(anneeEnCours) <= 0) {
			BigDecimal gainInit = gesteBatiMap.get(TypeRenovBati.FENBBC).getGainEner();
			// Sinon seuls les gestes d'exigence "BBC" et ne rien faire sont
			// conserves

			if (geste.getExigence().equals(Exigence.BBC_RENOVATION) || geste.getExigence().equals(Exigence.AUCUNE)
					|| geste.getExigence().equals(Exigence.GTB)) {
				// Calcul du gain initial realise en changeant les fenetres

				BigDecimal newGain = BigDecimal.ZERO;
				if ((geste.getGainEner().subtract(gainInit)).signum() > 0
						&& !geste.getTypeRenovBati().equals(TypeRenovBati.GTB)) {
					newGain = geste.getGainEner().subtract(gainInit);
					geste.setGainEner(newGain);
				}

				return true;
			}

		}
		return false;
	}

	protected boolean ensModMethode(HashMap<TypeRenovBati, Geste> gesteBatiMap, Parc parc,
			HashMap<TypeRenovBati, BigDecimal> dvGesteMap, Geste geste, BigDecimal anneeRenovBat,
			BigDecimal anneeEnCours) {
		if (parc.getTypeRenovBat().equals(TypeRenovBati.ENSMOD)
				&& (anneeRenovBat.add(dvGesteMap.get(TypeRenovBati.ENSMOD))).compareTo(anneeEnCours) > 0) {
			// Seuls les gestes sur les systemes sont conserves
			if (geste.getTypeRenovBati().equals(TypeRenovBati.ETAT_INIT)) {
				return true;
			}
		} else if (parc.getTypeRenovBat().equals(TypeRenovBati.ENSMOD)
				&& (anneeRenovBat.add(dvGesteMap.get(TypeRenovBati.ENSMOD))).compareTo(anneeEnCours) <= 0) {
			// Sinon on retient tous les gestes, les gains des gestes modestes
			// etant nulls
			// Dans le cas d'une renovation bbc, les gains initiaux sont
			// soustraits
			// Calcul du gain initial realise
			BigDecimal gainInit = gesteBatiMap.get(TypeRenovBati.ENSMOD).getGainEner();

			if (geste.getExigence().equals(Exigence.RT_PAR_ELEMENT)) {

				geste.setGainEner(BigDecimal.ZERO);
			} else if (!geste.getExigence().equals(Exigence.GTB)) {
				geste.setGainEner(geste.getGainEner().subtract(gainInit));

			}
			return true;
		}
		return false;
	}

	protected boolean ensBbcMethode(Parc parc, HashMap<TypeRenovBati, BigDecimal> dvGesteMap, Geste geste,
			BigDecimal anneeRenovBat, BigDecimal anneeEnCours) {
		if (parc.getTypeRenovBat().equals(TypeRenovBati.ENSBBC)
				&& (anneeRenovBat.add(dvGesteMap.get(TypeRenovBati.ENSBBC))).compareTo(anneeEnCours) > 0) {
			// Seuls les gestes sur les systemes sont conserves
			if (geste.getTypeRenovBati().equals(TypeRenovBati.ETAT_INIT)) {
				return true;
			}

		} else if (parc.getTypeRenovBat().equals(TypeRenovBati.ENSBBC)
				&& (anneeRenovBat.add(dvGesteMap.get(TypeRenovBati.ENSBBC))).compareTo(anneeEnCours) <= 0) {
			// Seuls les gestes "ne rien faire" et "ENSBBC" avec un gain
			// null sont retenus
			if (geste.getExigence().equals(Exigence.AUCUNE) || geste.getExigence().equals(Exigence.BBC_RENOVATION)) {
				if (geste.getExigence().equals(Exigence.BBC_RENOVATION)) {
					geste.setGainEner(BigDecimal.ZERO);
				}
				return true;
			}
		}
		return false;
	}

	protected boolean etatInitialEtNeufs(Parc parc, Geste geste, BigDecimal anneeRenovBat, BigDecimal anneeEnCours) {

		if ((parc.getTypeRenovBat().equals(TypeRenovBati.ETAT_INIT) && (anneeRenovBat.add(new BigDecimal("20")))
				.compareTo(anneeEnCours) > 0)
				|| (!parc.getTypeRenovBat().equals(TypeRenovBati.ETAT_INIT) && (anneeRenovBat.add(BigDecimal.TEN))
						.compareTo(anneeEnCours) > 0)) {
			// si le segment est un segment neuf, alors il des renovations sont
			// menees a partir de 20 ans.

			// si le segment a deja ete renove, alors une autre renovation peut
			// etre envisagee dans 10 ans

			if (geste.getTypeRenovBati().equals(TypeRenovBati.ETAT_INIT)) {
				return true;
			}
		} else if (parc.getTypeRenovBat().equals(TypeRenovBati.ETAT_INIT)
				&& (anneeRenovBat.add(new BigDecimal("20"))).compareTo(anneeEnCours) <= 0) {
			// cas des batiments neufs construits il y a vingt ans
			// tous les gestes sont retenus
			return true;
		} else if ((!parc.getTypeRenovBat().equals(TypeRenovBati.ETAT_INIT) && (anneeRenovBat.add(BigDecimal.TEN))
				.compareTo(anneeEnCours) <= 0)) {
			// cas des batiments ayant subi une renovation il y a plus de 10
			// ans. ils peuvent en subir une nouvelle

			return true;
		}
		return false;
	}

	protected boolean createWorkList(Parc parc, Geste geste, HashMap<String, BigDecimal> dvChauffMap,
			BigDecimal anneeRenovSys, BigDecimal anneeEnCours) {
		BigDecimal dvSys = dvChauffMap.get(parc.getIdsyschaud());
		// Si les systemes ont ete changes et ne doivent pas l'etre de nouveau
		if (!parc.getAnneeRenovSys().equals(INIT_STATE) && (anneeRenovSys.add(dvSys)).compareTo(anneeEnCours) > 0) {
			// Si le geste prevoit un changement de systeme
			if (geste.getTypeRenovSys().equals(TypeRenovSysteme.ETAT_INIT)) {
				return true;
				// gestesTemp.add(geste);
			}
		} else {
			return true;
			// gestesTemp.addAll(ensembleGestes);
		}
		return false;
	}

	protected void nonChauffeGeste(List<Geste> ensembleGestes, List<Geste> gestesFinaux) {
		for (Geste geste : ensembleGestes) {
			if (geste.getTypeRenovBati().equals(TypeRenovBati.ETAT_INIT)
					&& geste.getTypeRenovSys().equals(TypeRenovSysteme.ETAT_INIT)) {
				gestesFinaux.add(geste);
			}
		}
	}

	protected int compareDv(String anneeRenov, int annee, BigDecimal dvParoi) {
		int result = 0;

		if (!anneeRenov.equals(INIT_STATE)) {
			BigDecimal anneeBg = new BigDecimal(annee);
			BigDecimal anneeRenovBg = new BigDecimal(anneeRenov);

			result = (anneeRenovBg.add(dvParoi)).compareTo(anneeBg);
		}
		return result;
	}

	@Override
	public BibliGeste createBibliGeste(HashMap<String, BigDecimal> dvChauffMap,
			HashMap<TypeRenovBati, BigDecimal> dvGesteBati, String idAgregParc,
			HashMap<String, ParamRdtCout> rdtCoutChauffMap, Map<String, List<String>> periodeMap) {
		HashMap<String, List<Geste>> resultsMap = new HashMap<String, List<Geste>>();

		// Construction de la liste des gestes bati
		HashMap<String, List<Geste>> gestesBati = getGesteBati(idAgregParc, periodeMap);

		// Construction geste systeme
		String idGesteSys = generateIdGesteSys(idAgregParc);
		HashMap<String, ParamRdtCout> gestesSys = listeGesteSys(rdtCoutChauffMap, idGesteSys);
		// Parcours des gestes pouvant etre menes sur le bati
		for (String periodeDetail : gestesBati.keySet()) {
			List<Geste> listeBati = gestesBati.get(periodeDetail);

			for (Geste geste : listeBati) {

				// Cette premiere etape permet de creer un geste "ne rien faire"
				// pour les systemes, tout en renovant le bati
				// Ajout du rendement du systeme initial, de la duree de vie du
				// systeme ainsi que de l'Id du systeme et de l'energie
				// Initialise egalement la duree de vie des gestes sur le bati
				int dvBati = 1;
				if (!geste.getTypeRenovBati().equals(TypeRenovBati.ETAT_INIT)) {
					dvBati = dvGesteBati.get(geste.getTypeRenovBati()).intValueExact();
				}
				geste.setDureeBati(dvBati);
				geste.setTypeRenovSys(TypeRenovSysteme.ETAT_INIT);
				geste.setDureeSys(1);

				// Parcours les gestes pouvant etre menes sur les systemes
				String keyMap = null;
				List<String> geree = new ArrayList<String>();
				for (String str : gestesSys.keySet()) {
					keyMap = periodeDetail + str.substring(START_ID_PERIODE, START_ID_PERIODE + LENGTH_ID_PERIODE);
					// Chargement de la duree de vie des systemes

					BigDecimal dvSys = dvChauffMap.get(str.substring(START_ID_DV, START_ID_DV + LENGTH_ID_DV));
					// Copie du geste sur le bati
					Geste copyGeste = new Geste(geste);
					// Recuperation du geste a mener sur le systeme
					ParamRdtCout sysGeste = gestesSys.get(str);
					copyGeste.setDureeBati(dvBati);
					copyGeste.setRdt(sysGeste.getRdt());
					copyGeste.setCoutGesteSys(sysGeste.getCout());

					copyGeste.setGesteNom(copyGeste.getGesteNom() + CHGT_SYS);
					copyGeste.setTypeRenovSys(TypeRenovSysteme.CHGT_SYS);
					copyGeste.setDureeSys(dvSys.intValueExact());
					copyGeste.setEnergie(str.substring(START_ID_ENERG_CHAUFF, START_ID_ENERG_CHAUFF
							+ LENGTH_ID_ENERG_CHAUFF));
					copyGeste.setSysChaud(str
							.substring(START_ID_SYS_CHAUFF, START_ID_SYS_CHAUFF + LENGTH_ID_SYS_CHAUFF));
					copyGeste.setValeurCEE(sysGeste.getCEE());

					List<Geste> gestes;

					if (geree.contains(keyMap)) {
						resultsMap.get(keyMap).add(copyGeste);
					} else {
						if (resultsMap.containsKey(keyMap)) {
							gestes = resultsMap.get(keyMap);
						} else {
							gestes = new ArrayList<Geste>();
							resultsMap.put(keyMap, gestes);
						}
						gestes.add(copyGeste);
						gestes.add(geste);
						geree.add(keyMap);
					}

				}
			}
		}
		BibliGeste bibliGeste = new BibliGeste();
		bibliGeste.setBibliGesteMap(resultsMap);

		bibliGeste.setGestesBati(gestesBati);
		return bibliGeste;
	}

	public String generateIdGesteBati(Parc parc) {
		StringBuffer corresp = new StringBuffer();

		corresp.append(parc.getIdbranche());
		corresp.append(parc.getIdssbranche());
		corresp.append(parc.getIdbattype());
		corresp.append(parc.getIdperiodedetail());
		corresp.append(parc.getIdperiodesimple());

		return corresp.toString();
	}

	protected String generateIdGesteSys(String idAgreg) {

		return idAgreg.substring(START_ID_AGREG, START_ID_AGREG + LENGTH_ID_GESTE);

	}

	protected HashMap<String, ParamRdtCout> listeGesteSys(HashMap<String, ParamRdtCout> rdtCoutChauffMap,
			String idGesteSys) {
		// Methode permettant de recuperer pour chaque systeme le rendement
		// ainsi que le cout pour le segment considere a la periode en cours
		HashMap<String, ParamRdtCout> gesteSysList = new HashMap<String, ParamRdtCout>();

		for (String keyMap : rdtCoutChauffMap.keySet()) {
			String keyAgreg = cutId(keyMap);
			if (idGesteSys.equals(keyAgreg)) {
				gesteSysList.put(keyMap, rdtCoutChauffMap.get(keyMap));

			}

		}

		return gesteSysList;
	}

	protected String cutId(String keyMap) {

		return keyMap.substring(START_ID_SYS, START_ID_SYS + LENGTH_ID_SYS);

	}

	protected String getPeriodeIdSys(String keyMap) {

		return keyMap.substring(START_ID_PERIODE, START_ID_PERIODE + LENGTH_ID_PERIODE);

	}

	protected String generateProdEnergId(String keyMap) {

		return keyMap.substring(START_ID_SYS + LENGTH_ID_SYS, START_ID_SYS + LENGTH_ID_SYS + LENGTH_ID_ENERG);

	}

	protected BigDecimal getVariation(String type, int annee, HashMap<String, BigDecimal> evolCout) {
		String cle = String.valueOf(annee) + "_" + type;
		return evolCout.get(cle);
	}

	public BigDecimal tempsRetourInvestissement(BigDecimal coutInv, BigDecimal ecoEnergie, PBC tauxActu) {
		// formule T = ln(1-taux*Cinv/annuitee) / -ln(1+taux)
		BigDecimal inter = BigDecimal.ONE.subtract(
				coutInv.multiply(tauxActu.getTauxInteret()).divide(ecoEnergie, MathContext.DECIMAL32),
				MathContext.DECIMAL32);
		if (inter.signum() <= 0) {
			return new BigDecimal("200");
		} else {
			// on passe en double pour utiliser les log
			Double inter2 = -Math.log(inter.doubleValue()) / Math.log(1 + tauxActu.getTauxInteret().doubleValue());
			return BigDecimal.valueOf(inter2);
		}
	}
}