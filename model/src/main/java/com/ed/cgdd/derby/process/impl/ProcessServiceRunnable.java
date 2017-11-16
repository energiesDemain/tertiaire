package com.ed.cgdd.derby.process.impl;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.ed.cgdd.derby.model.financeObjects.*;
import com.ed.cgdd.derby.model.parc.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

import com.ed.cgdd.derby.common.CommonService;
import com.ed.cgdd.derby.finance.CreateNeufService;
import com.ed.cgdd.derby.finance.FinanceService;
import com.ed.cgdd.derby.finance.GesteService;
import com.ed.cgdd.derby.finance.InsertResultFinancementDAS;
import com.ed.cgdd.derby.finance.RecupParamFinDAS;
import com.ed.cgdd.derby.model.calcconso.Conso;
import com.ed.cgdd.derby.model.calcconso.EffetRebond;
import com.ed.cgdd.derby.model.calcconso.ParamBesoinsNeufs;
import com.ed.cgdd.derby.model.calcconso.ParamCoutEclVentil;
import com.ed.cgdd.derby.model.calcconso.ParamCoutEcs;
import com.ed.cgdd.derby.model.calcconso.ParamGainsUsages;
import com.ed.cgdd.derby.model.calcconso.ParamPMConso;
import com.ed.cgdd.derby.model.calcconso.ParamPMConsoChgtSys;
import com.ed.cgdd.derby.model.calcconso.ParamPartSolaireEcs;
import com.ed.cgdd.derby.model.calcconso.ParamPartSysPerfEcs;
import com.ed.cgdd.derby.model.calcconso.ParamRatioAux;
import com.ed.cgdd.derby.model.calcconso.ParamRdtCout;
import com.ed.cgdd.derby.model.calcconso.ParamRdtEcs;
import com.ed.cgdd.derby.model.calcconso.ParamRdtPerfEcs;
import com.ed.cgdd.derby.model.calcconso.ParamTauxCouvEcs;
import com.ed.cgdd.derby.model.calcconso.ParamTxClimExistant;
import com.ed.cgdd.derby.model.calcconso.ParamTxClimNeuf;
import com.ed.cgdd.derby.model.calcconso.ResultConso;
import com.ed.cgdd.derby.model.calcconso.ResultConsoRdt;
import com.ed.cgdd.derby.model.calcconso.ResultConsoRt;
import com.ed.cgdd.derby.model.calcconso.ResultConsoUClim;
import com.ed.cgdd.derby.model.calcconso.ResultConsoURt;
import com.ed.cgdd.derby.model.progression.Progression;
import com.ed.cgdd.derby.parc.InsertParcDAS;
import com.ed.cgdd.derby.parc.LoadParcDataDAS;
import com.ed.cgdd.derby.parc.ParamCalageEner;
import com.ed.cgdd.derby.parc.ParcService;
import com.ed.cgdd.derby.process.InitializeConsoService;
import com.ed.cgdd.derby.model.politiques;
import com.ed.cgdd.derby.model.CalibParameters;
import com.ed.cgdd.derby.usagesnonrt.BureauProcessService;
import com.ed.cgdd.derby.usagesnonrt.CuissonAutreService;
import com.ed.cgdd.derby.usagesnonrt.FroidAlimService;
import com.ed.cgdd.derby.usagesnonrt.InsertUsagesNonRTDAS;
import com.ed.cgdd.derby.usagesnonrt.LoadTableUsagesNonRTDAS;
import com.ed.cgdd.derby.usagesrt.ChauffageService;
import com.ed.cgdd.derby.usagesrt.ClimatisationService;
import com.ed.cgdd.derby.usagesrt.EclairageService;
import com.ed.cgdd.derby.usagesrt.EcsService;
import com.ed.cgdd.derby.usagesrt.InsertUsagesRTDAS;
import com.ed.cgdd.derby.usagesrt.LoadTableChauffClimDAS;
import com.ed.cgdd.derby.usagesrt.LoadTableRtDAS;
import org.apache.xmlbeans.impl.tool.Extension;

public class ProcessServiceRunnable implements Runnable {
	private final static Logger LOG = LogManager.getLogger(ProcessServiceImpl.class);

	private static final String ID_PARC = "idParc";
	private static final String YEAR = "annee";

	private final static int START_OCCUPANT = 6;
	private final static int LENGTH_OCCUPANT = 2;
	private final static int START_BRANCHE = 0;
	private final static int LENGTH_BRANCHE = 2;
	// BV ajouts 
	private static final int START_ID_COURANT = 0;
	private static final int ID_COURANT_LENGTH = 6;
	private static final int START_BATTYPE = 4;
	private static final int BATTYPE_LENGTH = 2;
	private static final int ID_ENERGIE_START = 8;
	private static final int ENERGIE_LENGTH = 2;
	private static final int ID_SYSCHAUF_START = 6;
	private static final int SYSCHAUF_LENGTH = 2;
	private static final int START_ID_BRANCHE = 0;
	private static final int LENGTH_ID_BRANCHE = 2;
	private static final int START_ID_OCCUPANT = 6;
	private static final int LENGTH_ID_OCCUPANT = 2;
	private static final int START_ID_SYS = 8;
	private static final int LENGTH_ID_SYS = 2;
	// BV fin ajouts
	
	private ParcService parcService;
	private LoadParcDataDAS loadParcDatadas;
	private InsertParcDAS insertParcdas;
	private BureauProcessService bureauProcessService;
	private CuissonAutreService cuissonAutreService;
	private FroidAlimService froidAlimService;
	private InsertUsagesNonRTDAS insertUsagesNonRTdas;
	private LoadTableUsagesNonRTDAS loadTableUsagesNonRTdas;
	private EcsService ecsService;
	private ClimatisationService climatisationService;
	private ChauffageService chauffageService;
	private EclairageService eclairageService;
	private InsertUsagesRTDAS insertUsagesRTdas;
	private LoadTableRtDAS loadTableRtdas;
	private LoadTableChauffClimDAS loadTableClimdas;
	private InitializeConsoService initializeConsoService;
	private CommonService commonService;
	private FinanceService financeService;
	private CreateNeufService createNeufService;

	private RecupParamFinDAS recupParamFinDAS;
	private GesteService gesteService;
	private InsertResultFinancementDAS insertResultFinancementDAS;

	int pasdeTempsInit;
	ParamCintObjects paramCintObject;
	float txRenovBati;
	HashMap<String, ParamParcArray> entreesMap;
	HashMap<String, ParamParcArray> sortiesMap;
	HashMap<String, ParamBesoinsNeufs> bNeufsMapInit;
	HashMap<String, ParamBesoinsNeufs> copyBesoinsNeufs;
	HashMap<String, EffetRebond> effetRebond;
	HashMap<String, ParamGainsUsages> gainsNonRTMap;
	HashMap<String, BigDecimal> dvUsagesMap;
	HashMap<String, ParamPMConso> pmCuissonMap;
	HashMap<String, ParamPMConso> pmAutresMap;
	HashMap<String, ParamPMConsoChgtSys> pmCuissonChgtMap;
	HashMap<String, ParamPMConsoChgtSys> pmAutresChgtMap;
	HashMap<String, BigDecimal> rythmeFrdRgltMap;
	HashMap<String, BigDecimal> gainFrdRgltMap;
	HashMap<String, ParamPMConso> pmEcsNeufMap;
	HashMap<String, ParamPMConsoChgtSys> pmEcsChgtMap;
	HashMap<String, ParamRdtEcs> bibliRdtEcsMap;
	HashMap<String, ParamRdtPerfEcs> rdtPerfEcsMap;
	HashMap<String, ParamPartSolaireEcs> partSolaireMap;
	HashMap<String, ParamTauxCouvEcs> txCouvSolaireMap;
	HashMap<String, ParamPartSysPerfEcs> partSysPerfEcsMap;
	HashMap<String, BigDecimal> dvEcsMap;
	HashMap<String, ParamCoutEcs> coutEcsMap;
	HashMap<String, ParamRdtCout> rdtCoutClimMap;
	HashMap<String, ParamTxClimExistant> txClimExistantMap;
	HashMap<String, ParamTxClimNeuf> txClimNeufMap;
	HashMap<String, ParamRdtCout> rdtCoutChauffMap;
	HashMap<String, BigDecimal> dvChauffMap;
	HashMap<TypeRenovBati, BigDecimal> dvGesteMap;
	HashMap<String, ParamRatioAux> auxChaud;
	HashMap<String, ParamRatioAux> auxFroid;
	HashMap<String, ParamGainsUsages> gainsEclairageMap;
	HashMap<String, ParamGainsUsages> gainsVentilationMap;
	HashMap<String, ParamCoutEclVentil> coutsEclVentilMap;
	HashMap<String,CalibCoutGlobal> coutIntangible;
	HashMap<String,CalibCoutGlobal> coutIntangibleBati;
	HashMap<String, BigDecimal> evolCoutBati;
	HashMap<String, BigDecimal> evolCoutTechno;
	Map<String, List<String>> periodeMap;
	HashMap<Integer, CoutEnergie> coutEnergieMap;
	HashMap<String, Emissions> emissionsMap;
	Reglementations reglementations;
	String idAgregParc;
	Progression progression;
	HashMap<String, TauxInteret> tauxInteretMap;
	HashMap<String, SurfMoy> surfMoyMap;
	HashMap<String, EvolValeurVerte> evolVVMap;
	HashMap<String, RepartStatutOccup> repartStatutOccupMap;
	HashMap<String, Maintenance> maintenanceMap;
	ElasticiteMap elasticiteMap;
	HashMap<String,CalibCoutGlobal> coutIntangibleNeuf;
	EvolBesoinMap evolBesoinMap;
	HashMap<String, ParamCalageEner> calageEner;  
	HashMap<String, BigDecimal> calageBranche;
	
	public ProcessServiceRunnable(int pasdeTempsInit, ParamCintObjects paramCintObject, float txRenovBati,
								  HashMap<String, ParamParcArray> entreesMap, HashMap<String, ParamParcArray> sortiesMap,
								  HashMap<String, ParamBesoinsNeufs> bNeufsMapInit,HashMap<String, ParamBesoinsNeufs> copyBesoinsNeufs, HashMap<String, EffetRebond> effetRebond,
								  HashMap<String, ParamGainsUsages> gainsNonRTMap, HashMap<String, BigDecimal> dvUsagesMap,
								  HashMap<String, ParamPMConso> pmCuissonMap, HashMap<String, ParamPMConso> pmAutresMap,
								  HashMap<String, ParamPMConsoChgtSys> pmCuissonChgtMap,
								  HashMap<String, ParamPMConsoChgtSys> pmAutresChgtMap, HashMap<String, BigDecimal> rythmeFrdRgltMap,
								  HashMap<String, BigDecimal> gainFrdRgltMap, HashMap<String, ParamPMConso> pmEcsNeufMap,
								  HashMap<String, ParamPMConsoChgtSys> pmEcsChgtMap, HashMap<String, ParamRdtEcs> bibliRdtEcsMap,
								  HashMap<String, ParamRdtPerfEcs> rdtPerfEcsMap, HashMap<String, ParamPartSolaireEcs> partSolaireMap,
								  HashMap<String, ParamTauxCouvEcs> txCouvSolaireMap, HashMap<String, ParamPartSysPerfEcs> partSysPerfEcsMap,
								  HashMap<String, BigDecimal> dvEcsMap, HashMap<String, ParamCoutEcs> coutEcsMap,
								  HashMap<String, ParamRdtCout> rdtCoutClimMap, HashMap<String, ParamTxClimExistant> txClimExistantMap,
								  HashMap<String, ParamTxClimNeuf> txClimNeufMap, HashMap<String, ParamRdtCout> rdtCoutChauffMap,
								  HashMap<String, BigDecimal> dvChauffMap, HashMap<TypeRenovBati, BigDecimal> dvGesteMap,
								  HashMap<String, ParamRatioAux> auxChaud, HashMap<String, ParamRatioAux> auxFroid,
								  HashMap<String, ParamGainsUsages> gainsEclairageMap, HashMap<String, ParamGainsUsages> gainsVentilationMap,
								  HashMap<String, ParamCoutEclVentil> coutsEclVentilMap, HashMap<String,CalibCoutGlobal> coutIntangible,
								  HashMap<String,CalibCoutGlobal> coutIntangibleBati, HashMap<String, BigDecimal> evolCoutBati,
								  HashMap<String, BigDecimal> evolCoutTechno, Map<String, List<String>> periodeMap,
								  HashMap<Integer, CoutEnergie> coutEnergieMap, HashMap<String, Emissions> emissionsMap,
								  Reglementations reglementations, String idAgregParc, Progression progression,
								  HashMap<String, TauxInteret> tauxInteretMap, HashMap<String, SurfMoy> surfMoyMap,
								  HashMap<String, EvolValeurVerte> evolVVMap, HashMap<String, RepartStatutOccup> repartStatutOccupMap,
								  HashMap<String, Maintenance> maintenanceMap, ElasticiteMap elasticiteMap, HashMap<String,CalibCoutGlobal> coutIntangibleNeuf, 
								  EvolBesoinMap evolBesoinMap,HashMap<String, ParamCalageEner> calageEner,  
								  HashMap<String, BigDecimal> calageBranche) {
		this.pasdeTempsInit = pasdeTempsInit;
		this.paramCintObject = paramCintObject;
		this.txRenovBati = txRenovBati;
		this.entreesMap = entreesMap;
		this.sortiesMap = sortiesMap;
		this.bNeufsMapInit = bNeufsMapInit;
		this.copyBesoinsNeufs=copyBesoinsNeufs;
		this.effetRebond = effetRebond;
		this.gainsNonRTMap = gainsNonRTMap;
		this.dvUsagesMap = dvUsagesMap;
		this.pmCuissonMap = pmCuissonMap;
		this.pmAutresMap = pmAutresMap;
		this.pmCuissonChgtMap = pmCuissonChgtMap;
		this.pmAutresChgtMap = pmAutresChgtMap;
		this.rythmeFrdRgltMap = rythmeFrdRgltMap;
		this.gainFrdRgltMap = gainFrdRgltMap;
		this.pmEcsNeufMap = pmEcsNeufMap;
		this.pmEcsChgtMap = pmEcsChgtMap;
		this.bibliRdtEcsMap = bibliRdtEcsMap;
		this.rdtPerfEcsMap = rdtPerfEcsMap;
		this.partSolaireMap = partSolaireMap;
		this.txCouvSolaireMap = txCouvSolaireMap;
		this.partSysPerfEcsMap = partSysPerfEcsMap;
		this.dvEcsMap = dvEcsMap;
		this.coutEcsMap = coutEcsMap;
		this.rdtCoutClimMap = rdtCoutClimMap;
		this.txClimExistantMap = txClimExistantMap;
		this.txClimNeufMap = txClimNeufMap;
		this.rdtCoutChauffMap = rdtCoutChauffMap;
		this.dvChauffMap = dvChauffMap;
		this.dvGesteMap = dvGesteMap;
		this.auxChaud = auxChaud;
		this.auxFroid = auxFroid;
		this.gainsEclairageMap = gainsEclairageMap;
		this.gainsVentilationMap = gainsVentilationMap;
		this.coutsEclVentilMap = coutsEclVentilMap;
		this.coutIntangible = coutIntangible;
		this.coutIntangibleBati = coutIntangibleBati;
		this.evolCoutBati = evolCoutBati;
		this.evolCoutTechno = evolCoutTechno;
		this.periodeMap = periodeMap;
		this.coutEnergieMap = coutEnergieMap;
		this.emissionsMap = emissionsMap;
		this.reglementations = reglementations;
		this.idAgregParc = idAgregParc;
		this.progression = progression;
		this.tauxInteretMap = tauxInteretMap;
		this.surfMoyMap = surfMoyMap;
		this.evolVVMap = evolVVMap;
		this.repartStatutOccupMap = repartStatutOccupMap;
		this.maintenanceMap = maintenanceMap;
		this.elasticiteMap = elasticiteMap;
		this.coutIntangibleNeuf = coutIntangibleNeuf;
		this.evolBesoinMap = evolBesoinMap;
		this.calageEner = calageEner;
		this.calageBranche = calageBranche;
	}

	public void initServices(ParcService parcService, LoadParcDataDAS loadParcDatadas, InsertParcDAS insertParcdas,
			BureauProcessService bureauProcessService, CuissonAutreService cuissonAutreService,
			FroidAlimService froidAlimService, InsertUsagesNonRTDAS insertUsagesNonRTdas,
			LoadTableUsagesNonRTDAS loadTableUsagesNonRTdas, EcsService ecsService,
			ClimatisationService climatisationService, ChauffageService chauffageService,
			EclairageService eclairageService, InsertUsagesRTDAS insertUsagesRTdas, LoadTableRtDAS loadTableRtdas,
			LoadTableChauffClimDAS loadTableClimdas, InitializeConsoService initializeConsoService,
			CommonService commonService, FinanceService financeService, CreateNeufService createNeufService,
			RecupParamFinDAS recupParamFinDAS, GesteService gesteService,
			InsertResultFinancementDAS insertResultFinancementDAS) {
		this.parcService = parcService;
		this.loadParcDatadas = loadParcDatadas;
		this.insertParcdas = insertParcdas;
		this.bureauProcessService = bureauProcessService;
		this.cuissonAutreService = cuissonAutreService;
		this.froidAlimService = froidAlimService;
		this.insertUsagesNonRTdas = insertUsagesNonRTdas;
		this.loadTableUsagesNonRTdas = loadTableUsagesNonRTdas;
		this.ecsService = ecsService;
		this.climatisationService = climatisationService;
		this.chauffageService = chauffageService;
		this.eclairageService = eclairageService;
		this.insertUsagesRTdas = insertUsagesRTdas;
		this.loadTableRtdas = loadTableRtdas;
		this.loadTableClimdas = loadTableClimdas;
		this.initializeConsoService = initializeConsoService;
		this.commonService = commonService;
		this.financeService = financeService;
		this.createNeufService = createNeufService;
		this.recupParamFinDAS = recupParamFinDAS;
		this.gesteService = gesteService;
		this.insertResultFinancementDAS = insertResultFinancementDAS;

	}

	@Override
	public void run() {
		processParc();
	}

	protected void processParc() {
		ResultParc resultatsParc;
		long timingSegmentStart = new Date().getTime();
		ThreadContext.put(ID_PARC, idAgregParc);
		//if (idAgregParc.equals("01018403")) {
		//if (idAgregParc.substring(0,2).equals("01") && idAgregParc.substring(4,6).equals("42") && idAgregParc.substring(6,8).equals("03")){
		//if (idAgregParc.substring(6,8).equals("03")) {
		//if (idAgregParc.equals("05141304")){
		//if (idAgregParc.substring(2,4).equals(B.TRANSPORT.getCode().toString()))
		// // if (!idAgregParc.substring(2,
		// // 4).equals(Branche.TRANSPORT.getCode().toString())) {
		// // if (idAgregParc.equals("01014203")) {
		// || idAgregParc.equals("02183805")
		// // }
		// || idAgregParc.equals("03164105") || idAgregParc.equals("06030103")
		// // || idAgregParc.equals("04100202")
		// || idAgregParc.equals("07134302") || idAgregParc.equals("05225203")
		// // if (idAgregParc.substring(0, 2).equals("08")) {
		// // {equals("08356005")
		// || idAgregParc.equals("01024401")) {
		// if (idAgregParc.equals("02186705")) {
		if (true) {
			int pasdeTemps = pasdeTempsInit;
			LOG.info("idAgregParc = {}", idAgregParc);
			try {
//				long start = System.currentTimeMillis();
				// Initialisation des donnees a traiter
				// Initialisation des consommations unitaires RT
				HashMap<String, ResultConsoURt> resultConsoURtMap = new HashMap<String, ResultConsoURt>();
				HashMap<String, ResultConsoUClim> resultConsoUClimMap = new HashMap<String, ResultConsoUClim>();
				// Chargement du parc initial
				List<Parc> parc = loadParcDatadas.getParamParcMapper(idAgregParc, pasdeTemps);
				// selectionne param Recalage parc init
				
				BigDecimal calageParc = BigDecimal.ZERO;
				
				calageParc = calageBranche.get(idAgregParc.substring(0,2));
				
				// Recalage du parc par branche
				for (Parc parcTemp : parc) {
				parcTemp.setAnnee(0,parcTemp.getAnnee(0).multiply(calageParc,MathContext.DECIMAL32)
						.multiply(calageEner.get(parcTemp.getIdenergchauff()).getFacteurCalageParc(),MathContext.DECIMAL32));
				}
				
				// rempli egalement resultConsoURtMap
				HashMap<String, Parc> parcTotMap = parcService.sortDataParc(parc, resultConsoURtMap, pasdeTemps,
						resultConsoUClimMap);
				// Chargement des tables de besoins initiaux et recalage
				ResultConso resultatsConso = loadInitConso(idAgregParc, pasdeTemps, calageParc);

				// Initialisation de l'ECS, rempli egalement
				// resultConsoURtMap
				ResultConsoRdt resultatsConsoEcs = InitializeEcs(idAgregParc, pasdeTemps, bibliRdtEcsMap,
						resultConsoURtMap,calageParc);
				// Chargement des tables de besoins initiaux d'eclairage,
				// ventilation , et rempli egalement resultConsoURtMap
				ResultConsoRt resultatsConsoRt = loadInitConsoRt(idAgregParc, pasdeTemps, resultConsoURtMap, calageParc);
				// Initialisation du chauffage
				resultatsConsoRt = initializeChauffClim(resultConsoUClimMap, resultatsConsoRt, idAgregParc, pasdeTemps,
						rdtCoutChauffMap, Usage.CHAUFFAGE.getLabel(), calageParc,  calageEner);
				// Initialisation des consommations d'auxiliaires
				resultatsConsoRt = initializeConsoService.initializeAuxChaud(resultatsConsoRt, auxChaud, pasdeTemps);
				// Initialisation de la climatisation et rempli
				// resultConsoUClimMap
				resultatsConsoRt = initializeChauffClim(resultConsoUClimMap, resultatsConsoRt, idAgregParc, pasdeTemps,
						rdtCoutClimMap, Usage.CLIMATISATION.getLabel(), calageParc, calageEner);
				// chargement des financements
				List<Financement> ensembleFinancements = recupParamFinDAS.recupFinancement(
						idAgregParc.substring(START_OCCUPANT, START_OCCUPANT + LENGTH_OCCUPANT),
						idAgregParc.substring(START_BRANCHE, START_BRANCHE + LENGTH_BRANCHE));
				List<CEE> subventions = financeService.cleanListeFinancement(ensembleFinancements);
				// Creation d'une HashMap de resultats pour les masses
				// financieres
				HashMap<ResFin, ValeurFinancement> resultFinance = new HashMap<>();

				// Chargement de la bibliotheque de geste
				BibliGeste bibliGeste = gesteService.createBibliGeste(dvChauffMap, dvGesteMap, idAgregParc,
						rdtCoutChauffMap, periodeMap);
				LOG.info("Chargement done !");
				// Initialisation du compteur
				BigDecimal compteur = BigDecimal.ZERO;
				// Initialisation de la map enregistrant les surfaces
				// touchees
				// par le decret
				HashMap<String, BigDecimal> decretMemory = new HashMap<String, BigDecimal>();

				// Chargement des facteurs d'elasticite des besoins
				// batiments existants
				HashMap<String, BigDecimal[]> elasticiteExistantMap = commonService.getFacteurElasticiteExistant(
						idAgregParc, coutEnergieMap, emissionsMap, elasticiteMap);
				// batiments neufs
				HashMap<String, BigDecimal[]> elasticiteNeufMap = commonService.getFacteurElasticiteNeuf(idAgregParc,
						coutEnergieMap, emissionsMap, elasticiteMap);

//				long end = System.currentTimeMillis();
//				LOG.info("creation time : {}ms", end - start);
				// Boucle de calcul des evolutions du parc et des
				// consommations
				// energetiques
				// HashMap<String, BigDecimal> debugMap = new
				// HashMap<String,
				// BigDecimal>();
				//for (int annee = 2010; annee <= 2050; annee++) {
			    for (int annee = 2010; annee <= 2013; annee++) {
//					long start2 = System.currentTimeMillis();
			    	//BV prise en compte travaux embarques
			    	if(politiques.checkTravEmb && annee == 2017){
			    	//	LOG.debug("taux avant trav emb{}",txRenovBati);
			        // travaux embarques on augmente le taux de renov tendancielle de 1.3%
			    	txRenovBati = txRenovBati + politiques.txRenovTravEmb;
			    	//LOG.debug("taux apres ={}", txRenovBati);
			    	}
//					long end2 = System.currentTimeMillis();
//					LOG.info("Travaux embarques : {}ms", end2 - start2);
			    	//LOG.debug("taux renov ={}", txRenovBati);
			    	
					long timingStart = new Date().getTime();
					ThreadContext.put(YEAR, String.valueOf(annee));

					//LOG.info("Annee {}", annee);
					List<Financement> listFin = getFinListPeriode(ensembleFinancements, annee);
					
					CEE subCEE = new CEE();
					
					// Ajout des CEE annuels
					if(politiques.checkCEEannuels){
						subCEE = getCEEannuel(subventions, annee);
					} else {
						subCEE = getCEE(subventions, annee);
					}
					
					int anneeNTab = calcBoucle(annee, pasdeTemps);

					compteur = compteur.add(BigDecimal.ONE);

					resultatsParc = new ResultParc();

					// On charge la bonne map des besoins neufs a utiliser en fonction de l'occupant et de l'annee
					HashMap<String, ParamBesoinsNeufs> bNeufsMap = getBNeufMapToUse(annee);
		
					// Calcul des parts de marche dans les batiments neufs
					//long startMarcheNeuf = System.currentTimeMillis();
					HashMap<String, BigDecimal> partsMarchesNeuf = createNeufService.pmChauffNeuf(bNeufsMap,
							dvChauffMap, rdtCoutChauffMap, idAgregParc, annee, "Proprietaire", paramCintObject.getSysNeuf().getNu(),
							coutIntangibleNeuf,
							coutEnergieMap, emissionsMap, evolCoutBati, evolCoutTechno, tauxInteretMap, maintenanceMap);
					//long endMarcheNeuf = System.currentTimeMillis();
					//LOG.info("Parts de marche neufs : {}ms", endMarcheNeuf - startMarcheNeuf);


//					long startParc = System.currentTimeMillis();
					resultatsParc = parcService.parc(txClimExistantMap, partsMarchesNeuf, 
							parcTotMap, entreesMap,
							sortiesMap, txClimNeufMap, pasdeTemps, anneeNTab, annee);
//					long endParc = System.currentTimeMillis();
//					LOG.info("Parc : {}ms", endParc - startParc);

					//LOG.info("Parc Done !");
					
					// debugMap = new HashMap<String, BigDecimal>();

// 					// calcul des parts de marche dans les batiments existants

//					LOG.debug("annee {} evol {}", annee, evolBesoinMap.getEvolBesoin()
//					.get(idAgregParc.substring(START_ID_BRANCHE,LENGTH_ID_BRANCHE)+Usage.CHAUFFAGE+annee)
//					.getEvolution());
					
					//long startPM = System.currentTimeMillis();
					HashMap<String, PartMarcheRenov> partMarcheMap = financeService.renovationSegmentGlobal(
							decretMemory, resultConsoUClimMap, resultConsoURtMap, listFin, subCEE, dvChauffMap,
							dvGesteMap, rdtCoutChauffMap, parcTotMap, resultatsConsoRt, annee, anneeNTab,
							coutIntangible, coutIntangibleBati, paramCintObject, txRenovBati, idAgregParc, bibliGeste,
							coutEnergieMap, emissionsMap, reglementations, compteur, coutsEclVentilMap, coutEcsMap,
							pmEcsNeufMap, bNeufsMap, gainsVentilationMap, bibliRdtEcsMap, evolCoutBati, evolCoutTechno,
							tauxInteretMap, surfMoyMap, evolVVMap, repartStatutOccupMap, maintenanceMap, 
							evolBesoinMap);
					//long endPM = System.currentTimeMillis();
					//LOG.info("PM existant : {}ms", endPM - startPM);

					//long startConso = System.currentTimeMillis();
					resultatsConsoRt = chauffageService.evolChauffageConso(resultFinance, idAgregParc, auxChaud,
							parcTotMap, partMarcheMap, bNeufsMap, rdtCoutChauffMap, anneeNTab, pasdeTemps, annee,
							resultatsConsoRt, gainsVentilationMap, effetRebond, elasticiteNeufMap,
							elasticiteExistantMap, evolBesoinMap);
					
		
					// LOG.info("Chauffage Done !");
					// LOG.info("Ventilation Done !");

					resultatsConsoRt = climatisationService.evolClimatisationConso(resultConsoUClimMap, auxFroid,
							parcTotMap, resultatsConsoRt, bNeufsMap, dvUsagesMap, rdtCoutClimMap, anneeNTab,
							pasdeTemps, annee, compteur, Usage.CLIMATISATION.getLabel(), elasticiteNeufMap,
							elasticiteExistantMap, evolBesoinMap);
					// LOG.info("Climatisation Done !");

					resultatsConso.put(Usage.BUREAUTIQUE.getLabel(), bureauProcessService.evolBureauProcess(
							resultatsParc.getMap(MapResultsKeys.PARC_TOT.getLabel()),
							resultatsConso.getMap(Usage.BUREAUTIQUE.getLabel()), dvUsagesMap, gainsNonRTMap, bNeufsMap,
							pasdeTemps, anneeNTab, annee, Usage.BUREAUTIQUE.getLabel(), elasticiteNeufMap,
							elasticiteExistantMap));
					// LOG.info("Bureautique Done !");

					resultatsConso.put(Usage.PROCESS.getLabel(), bureauProcessService.evolBureauProcess(
							resultatsParc.getMap(MapResultsKeys.PARC_TOT.getLabel()),
							resultatsConso.getMap(Usage.PROCESS.getLabel()), dvUsagesMap, gainsNonRTMap, bNeufsMap,
							pasdeTemps, anneeNTab, annee, Usage.PROCESS.getLabel(), elasticiteNeufMap,
							elasticiteExistantMap));
					// LOG.info("Process Done !");

					resultatsConso.put(Usage.CUISSON.getLabel(), cuissonAutreService.evolCuissonAutre(
							resultatsParc.getMap(MapResultsKeys.PARC_TOT.getLabel()),
							resultatsConso.getMap(Usage.CUISSON.getLabel()), dvUsagesMap, gainsNonRTMap, bNeufsMap,
							pmCuissonMap, pmAutresMap, pmCuissonChgtMap, pmAutresChgtMap, pasdeTemps, anneeNTab, annee,
							Usage.CUISSON.getLabel(), elasticiteNeufMap, elasticiteExistantMap));
					// LOG.info("Cuisson Done !");
					resultatsConso.put(Usage.AUTRES.getLabel(), cuissonAutreService.evolCuissonAutre(
							resultatsParc.getMap(MapResultsKeys.PARC_TOT.getLabel()),
							resultatsConso.getMap(Usage.AUTRES.getLabel()), dvUsagesMap, gainsNonRTMap, bNeufsMap,
							pmCuissonMap, pmAutresMap, pmCuissonChgtMap, pmAutresChgtMap, pasdeTemps, anneeNTab, annee,
							Usage.AUTRES.getLabel(), elasticiteNeufMap, elasticiteExistantMap));
					// LOG.info("Autres Done !");
					resultatsConso.put(Usage.FROID_ALIMENTAIRE.getLabel(), froidAlimService.evolFroidAlim(
							resultatsParc.getMap(MapResultsKeys.PARC_TOT.getLabel()),
							resultatsConso.getMap(Usage.FROID_ALIMENTAIRE.getLabel()), dvUsagesMap, gainsNonRTMap,
							rythmeFrdRgltMap, gainFrdRgltMap, bNeufsMap, pasdeTemps, anneeNTab, annee,
							Usage.FROID_ALIMENTAIRE.getLabel(), elasticiteNeufMap, elasticiteExistantMap));
					// LOG.info("Froid alimentaire Done !");
					resultatsConsoEcs = ecsService.evolEcsConso(coutEcsMap, parcTotMap, resultatsConsoEcs,
							pmEcsNeufMap, pmEcsChgtMap, bNeufsMap, partSolaireMap, txCouvSolaireMap, dvEcsMap,
							bibliRdtEcsMap, rdtPerfEcsMap, partSysPerfEcsMap, anneeNTab, pasdeTemps, annee, compteur,
							Usage.ECS.getLabel(), resultConsoURtMap, elasticiteNeufMap, elasticiteExistantMap);
					// LOG.info("ECS Done !");
					resultatsConsoRt = eclairageService.evolEclairageConso(coutsEclVentilMap, parcTotMap,
							resultatsConsoRt, gainsEclairageMap, bNeufsMap, dvUsagesMap, anneeNTab, pasdeTemps, annee,
							compteur, Usage.ECLAIRAGE.getLabel(), resultConsoURtMap, elasticiteNeufMap,
							elasticiteExistantMap);
					// LOG.info("Eclairage Done !");
					//long endConso = System.currentTimeMillis();
					//LOG.info("Conso : {}ms", endConso - startConso);

					//long startInsert = System.currentTimeMillis();
					// Insertion/Update des resultats
					if (anneeNTab == pasdeTemps || annee == 2010) {
						int pasdeTempsTemp = 0;
						if (annee == 2010) {
							pasdeTempsTemp = pasdeTemps;
							pasdeTemps = 1;
						}

						// Agregation du parc
						ResultParc resultatsAgregParc = commonService.agregateResultParc(resultatsParc, pasdeTemps);
						// Agregation de l'ECS
						ResultConsoRdt resultatsConsoEcsAgreg = commonService.agregateResultECS(resultatsConsoEcs,
								pasdeTemps, Usage.ECS.getLabel());
						// Agregation de la Climatisation
						ResultConsoRt resultatsConsoClimAgreg = commonService.agregateResultRtClim(resultatsConsoRt,
								pasdeTemps, Usage.CLIMATISATION.getLabel());
						// Agregation des consommations de chauffage,
						// auxiliaires,
						// ventilation et eclairage
						ResultConsoRt resultatsConsoRtAgreg = commonService.agregateResultRt(resultatsConsoRt,
								pasdeTemps);

						LOG.info("Insertions");
						insertParcdas.insert("Parc_resultats",
								resultatsAgregParc.getMap(MapResultsKeys.PARC_TOT.getLabel()), pasdeTemps, annee);
						insertParcdas.insert("Parc_entrant",
								resultatsAgregParc.getMap(MapResultsKeys.PARC_ENTRANT.getLabel()), pasdeTemps, annee);
						insertParcdas.insert("Parc_sortant",
								resultatsAgregParc.getMap(MapResultsKeys.PARC_SORTANT.getLabel()), pasdeTemps, annee);

						// LOG.info("Inserts usages non RT");
						insertUsagesNonRTdas.insert(Usage.BUREAUTIQUE.getLabel(), "Conso_non_RT_resultats",
								resultatsConso.getMap(Usage.BUREAUTIQUE.getLabel()), pasdeTemps, annee);
						insertUsagesNonRTdas.insert(Usage.PROCESS.getLabel(), "Conso_non_RT_resultats",
								resultatsConso.getMap(Usage.PROCESS.getLabel()), pasdeTemps, annee);
						insertUsagesNonRTdas.insert(Usage.CUISSON.getLabel(), "Conso_non_RT_resultats",
								resultatsConso.getMap(Usage.CUISSON.getLabel()), pasdeTemps, annee);
						insertUsagesNonRTdas.insert(Usage.AUTRES.getLabel(), "Conso_non_RT_resultats",
								resultatsConso.getMap(Usage.AUTRES.getLabel()), pasdeTemps, annee);
						insertUsagesNonRTdas.insert(Usage.FROID_ALIMENTAIRE.getLabel(), "Conso_non_RT_resultats",
								resultatsConso.getMap(Usage.FROID_ALIMENTAIRE.getLabel()), pasdeTemps, annee);

						// LOG.info("Inserts besoins d'ECS");
						insertUsagesRTdas.insert(Usage.ECS.getLabel(), "Besoin_RT_resultats",
								resultatsConsoEcsAgreg.getMap(MapResultsKeys.BESOIN_ECS.getLabel()), pasdeTemps, annee);
						// LOG.info("Inserts besoins Climatisation");
						insertUsagesRTdas.insert(Usage.CLIMATISATION.getLabel(), "Besoin_RT_resultats",
								resultatsConsoClimAgreg.getMap(MapResultsKeys.BESOIN_CLIM.getLabel()), pasdeTemps,
								annee);
						// LOG.info("Inserts besoins d'eclairage");
						insertUsagesRTdas.insert(Usage.ECLAIRAGE.getLabel(), "Besoin_RT_resultats",
								resultatsConsoRtAgreg.getMap(Usage.ECLAIRAGE.getLabel()), pasdeTemps, annee);
						// LOG.info("Inserts besoins de ventilation");
						insertUsagesRTdas.insert(Usage.VENTILATION.getLabel(), "Besoin_RT_resultats",
								resultatsConsoRtAgreg.getMap(Usage.VENTILATION.getLabel()), pasdeTemps, annee);
						// LOG.info("Inserts besoins de chauffage");
						insertUsagesRTdas.insert(Usage.CHAUFFAGE.getLabel(), "Besoin_RT_resultats",
								resultatsConsoRtAgreg.getMap(MapResultsKeys.BESOIN_CHAUFF.getLabel()), pasdeTemps,
								annee);
						// LOG.info("Inserts besoins en auxiliaires");
						insertUsagesRTdas.insert(Usage.AUXILIAIRES.getLabel(), "Besoin_RT_resultats",
								resultatsConsoRtAgreg.getMap(MapResultsKeys.AUXILIAIRES.getLabel()), pasdeTemps, annee);
						// insertUsagesRTdas.insertTest("ECS",
						// "Besoin_RT_resultats_test",
						// resultatsConsoEcs.getMap("Couts"),
						// pasdeTemps, annee);
						// insertUsagesRTdas.insertTest("Climatisation",
						// "Rendement_RT_resultats_test",
						// resultatsConsoClim.getMap("Rendements"),
						// pasdeTemps,
						// annee);

						// LOG.info("Inserts rendements RT");
						// insertUsagesRTdas.insert("ECS",
						// "Rendement_RT_resultats",
						// resultatsConsoEcs.getMap("Rendements"),
						// pasdeTemps, annee);

						// LOG.info("Inserts consoEF RT");
						insertUsagesRTdas.insert(Usage.ECS.getLabel(), "Conso_RT_resultats",
								resultatsConsoEcsAgreg.getMap(MapResultsKeys.CONSO_ECS.getLabel()), pasdeTemps, annee);
						insertUsagesRTdas
								.insert(Usage.CLIMATISATION.getLabel(), "Conso_RT_resultats",
										resultatsConsoClimAgreg.getMap(MapResultsKeys.CONSO_CLIM.getLabel()),
										pasdeTemps, annee);
						insertUsagesRTdas.insert(Usage.ECLAIRAGE.getLabel(), "Conso_RT_resultats",
								resultatsConsoRtAgreg.getMap(Usage.ECLAIRAGE.getLabel()), pasdeTemps, annee);
						insertUsagesRTdas.insert(Usage.VENTILATION.getLabel(), "Conso_RT_resultats",
								resultatsConsoRtAgreg.getMap(MapResultsKeys.VENTILATION.getLabel()), pasdeTemps, annee);
						insertUsagesRTdas
								.insert(Usage.CHAUFFAGE.getLabel(), "Conso_RT_resultats",
										resultatsConsoRtAgreg.getMap(MapResultsKeys.CONSO_CHAUFF.getLabel()),
										pasdeTemps, annee);
						insertUsagesRTdas.insert(Usage.AUXILIAIRES.getLabel(), "Conso_RT_resultats",
								resultatsConsoRtAgreg.getMap(MapResultsKeys.AUXILIAIRES.getLabel()), pasdeTemps, annee);

						// LOG.info("Inserts couts");
						insertUsagesRTdas.insert(Usage.ECS.getLabel(), "Couts_resultats",
								resultatsConsoEcsAgreg.getMap(MapResultsKeys.COUT_ECS.getLabel()), pasdeTemps, annee);
						insertUsagesRTdas.insert(Usage.ECLAIRAGE.getLabel(), "Couts_resultats",
								resultatsConsoRtAgreg.getMap(MapResultsKeys.COUT_ECLAIRAGE.getLabel()), pasdeTemps,
								annee);
						insertUsagesRTdas.insert(Usage.CLIMATISATION.getLabel(), "Couts_resultats",
								resultatsConsoClimAgreg.getMap(MapResultsKeys.COUT_CLIM.getLabel()), pasdeTemps, annee);

						//long endInsert = System.currentTimeMillis();
						//LOG.info("Insert : {}ms", endInsert - startInsert);

						if (annee == 2010) {
							pasdeTemps = pasdeTempsTemp;
						}
						//long startTransfert = System.currentTimeMillis();

						//
						LOG.info("Debut transfert");
						transfertMapParc(resultatsParc.getMap(MapResultsKeys.PARC_ENTRANT.getLabel()), pasdeTemps,
								annee);
						transfertMapParc(resultatsParc.getMap(MapResultsKeys.PARC_SORTANT.getLabel()), pasdeTemps,
								annee);
						transfertMapParc(resultatsParc.getMap(MapResultsKeys.PARC_TOT.getLabel()), pasdeTemps, annee);
						// LOG.info("Debut transfert Bureautique et Process");
						transfertMapParc(resultatsConso.getMap(Usage.BUREAUTIQUE.getLabel()), pasdeTemps, annee);
						transfertMapParc(resultatsConso.getMap(Usage.PROCESS.getLabel()), pasdeTemps, annee);
						// LOG.info("Debut transfert Cuisson et Autres");
						transfertMapParc(resultatsConso.getMap(Usage.CUISSON.getLabel()), pasdeTemps, annee);
						transfertMapParc(resultatsConso.getMap(Usage.AUTRES.getLabel()), pasdeTemps, annee);
						// LOG.info("Debut transfert Froid Alimentaire");
						transfertMapParc(resultatsConso.getMap(Usage.FROID_ALIMENTAIRE.getLabel()), pasdeTemps, annee);
						// LOG.info("Debut transfert ECS");
						transfertMapConso(resultatsConsoEcs.getMap(MapResultsKeys.BESOIN_ECS.getLabel()), pasdeTemps,
								annee);
						transfertMapConso(resultatsConsoEcs.getMap(MapResultsKeys.RDT_ECS.getLabel()), pasdeTemps,
								annee);
						transfertMapConso(resultatsConsoEcs.getMap(MapResultsKeys.CONSO_ECS.getLabel()), pasdeTemps,
								annee);
						transfertMapConso(resultatsConsoEcs.getMap(MapResultsKeys.COUT_ECS.getLabel()), pasdeTemps,
								annee);
						// LOG.info("Debut transfert Climatisation");
						transfertMapConso(resultatsConsoRt.getMap(MapResultsKeys.BESOIN_CLIM.getLabel()), pasdeTemps,
								annee);
						transfertMapConso(resultatsConsoRt.getMap(MapResultsKeys.RDT_CLIM.getLabel()), pasdeTemps,
								annee);
						transfertMapConso(resultatsConsoRt.getMap(MapResultsKeys.CONSO_CLIM.getLabel()), pasdeTemps,
								annee);
						transfertMapConso(resultatsConsoRt.getMap(MapResultsKeys.COUT_CLIM.getLabel()), pasdeTemps,
								annee);
						// LOG.info("Debut transfert Chauffage");
						transfertMapConso(resultatsConsoRt.getMap(MapResultsKeys.BESOIN_CHAUFF.getLabel()), pasdeTemps,
								annee);
						transfertMapConso(resultatsConsoRt.getMap(MapResultsKeys.RDT_CHAUFF.getLabel()), pasdeTemps,
								annee);
						transfertMapConso(resultatsConsoRt.getMap(MapResultsKeys.CONSO_CHAUFF.getLabel()), pasdeTemps,
								annee);
						// LOG.info("Debut transfert Eclairage, Ventilation et Auxiliaires");
						transfertMapConso(resultatsConsoRt.getMap(Usage.ECLAIRAGE.getLabel()), pasdeTemps, annee);
						transfertMapConso(resultatsConsoRt.getMap(Usage.VENTILATION.getLabel()), pasdeTemps, annee);
						transfertMapConso(resultatsConsoRt.getMap(Usage.AUXILIAIRES.getLabel()), pasdeTemps, annee);
						transfertMapConso(resultatsConsoRt.getMap(MapResultsKeys.COUT_ECLAIRAGE.getLabel()),
								pasdeTemps, annee);

						// LOG.info("Debut transfert des consoU");
						transfertMapConsoU(resultConsoURtMap, pasdeTemps, annee);
						transfertMapConsoUClim(resultConsoUClimMap, pasdeTemps, annee);
						LOG.info("Annee de la boucle {}, pas de temps = {}", annee, pasdeTemps);
						//long endTransfert = System.currentTimeMillis();
						//LOG.info("Transfert : {}ms", endTransfert - startTransfert);

					}

					if (anneeNTab == pasdeTemps) {

						pasdeTemps = calcPasdeTemps(annee, pasdeTemps);
					}
					long timingEnd = new Date().getTime();
					LOG.debug("Timing: {}ms", timingEnd - timingStart);
				}

				LOG.info("Inserts Resultats Financements");
				insertResultFinancementDAS.insert(resultFinance);

			} catch (Exception ex) {
				LOG.error("{}", ex);
				throw ex;
			} finally {
				long timingSegmentEnd = new Date().getTime();
				LOG.debug("TimingSegment: {}ms", timingSegmentEnd - timingSegmentStart);
				ThreadContext.clear();
			}
			progression.addFinishedParc();

		}

	}

	protected HashMap<String, ParamBesoinsNeufs> getBNeufMapToUse(int annee) {
		HashMap<String,ParamBesoinsNeufs> bNeufsMap = bNeufsMapInit;
		if(politiques.checkBatex){
        int periode = commonService.correspPeriode(annee);
        String idOccupant = idAgregParc.substring(START_OCCUPANT, START_OCCUPANT + LENGTH_OCCUPANT);


        // On modifie les besoins des usages Rt uniquement, pour l'Etat et les collectivites
        // et seulement pour les annees 2015 et les annees de debut de periode


        if (!(idOccupant.equals("05")|| idOccupant.equals("04")) && annee > 2014 &&
                (annee == commonService.anneeDebutPeriode(periode) || annee ==2015)){

            bNeufsMap=copyBesoinsNeufs;

        }
        }
		return bNeufsMap;
	}

	private ResultConso loadInitConso(String idAgregParc, int pasdeTemps, BigDecimal calageParc) {
		ResultConso resultatsConso = new ResultConso();
		resultatsConso.put(Usage.BUREAUTIQUE.getLabel(),
				loadTableUsagesNonRTdas.loadMapResultBesoin("Bureautique_init", idAgregParc, pasdeTemps,calageParc));
		resultatsConso.put(Usage.PROCESS.getLabel(),
				loadTableUsagesNonRTdas.loadMapResultBesoin("Process_init", idAgregParc, pasdeTemps,calageParc));
		resultatsConso.put(Usage.AUTRES.getLabel(),
				loadTableUsagesNonRTdas.loadMapResultBesoin("Autre_init", idAgregParc, pasdeTemps,calageParc));
		resultatsConso.put(Usage.CUISSON.getLabel(),
				loadTableUsagesNonRTdas.loadMapResultBesoin("Cuisson_init", idAgregParc, pasdeTemps,calageParc));
		resultatsConso.put(Usage.FROID_ALIMENTAIRE.getLabel(),
				loadTableUsagesNonRTdas.loadMapResultBesoin("Froid_alimentaire_init", idAgregParc, pasdeTemps,calageParc));

		return resultatsConso;
	}

	private ResultConsoRt loadInitConsoRt(String idAgregParc, int pasdeTemps,
			HashMap<String, ResultConsoURt> resultConsoURtMap, BigDecimal calageParc) {
		ResultConsoRt resultatsConso = new ResultConsoRt();
		resultatsConso.put(MapResultsKeys.ECLAIRAGE.getLabel(), loadTableRtdas.loadMapResultBesoinEclairage(
				"Eclairage_init", idAgregParc, pasdeTemps, resultConsoURtMap, calageParc));
		HashMap<String, Conso> coutMap = new HashMap<String, Conso>();
		resultatsConso.put(MapResultsKeys.COUT_ECLAIRAGE.getLabel(), coutMap);
		resultatsConso.put(MapResultsKeys.VENTILATION.getLabel(),
				loadTableRtdas.loadMapResultBesoinVentil("Ventilation_init", idAgregParc, pasdeTemps, calageParc));
		return resultatsConso;
	}

	protected void transfertMapParc(HashMap<String, Parc> totMap, int pasdeTemps, int annee) {

		Parc parcTemp = new Parc(pasdeTemps);
		BigDecimal value = BigDecimal.ZERO;
		if (annee == 2010) {
			Iterator<String> iterator = totMap.keySet().iterator();
			while (iterator.hasNext()) {
				String totMapKey = iterator.next();
				parcTemp = new Parc(totMap.get(totMapKey));
				value = parcTemp.getAnnee(1);
				if (value == null || value.signum() == 0) {
					iterator.remove();
				} else {
					parcTemp.setAnnee(0, value);
					for (int i = 1; i <= pasdeTemps; i++) {
						parcTemp.setAnnee(i, BigDecimal.ZERO);
					}
					totMap.put(totMapKey, parcTemp);
				}
			}
		} else {
			Iterator<String> iterator = totMap.keySet().iterator();
			while (iterator.hasNext()) {
				String totMapKey = iterator.next();
				parcTemp = new Parc(totMap.get(totMapKey));
				value = parcTemp.getAnnee(pasdeTemps);
				if (value == null || value.signum() == 0) {
					iterator.remove();
				} else {
					parcTemp.setAnnee(0, value);
					for (int i = 1; i <= pasdeTemps; i++) {
						parcTemp.setAnnee(i, BigDecimal.ZERO);
					}
					totMap.put(totMapKey, parcTemp);
				}
			}
		}
	}

	protected void transfertMapConso(HashMap<String, Conso> totMap, int pasdeTemps, int annee) {

		Conso parcTemp = new Conso(pasdeTemps);
		BigDecimal value = BigDecimal.ZERO;
		if (annee == 2010) {
			Iterator<String> iterator = totMap.keySet().iterator();
			while (iterator.hasNext()) {
				String totMapKey = iterator.next();
				parcTemp = new Conso(totMap.get(totMapKey));
				value = parcTemp.getAnnee(1);
				if (value == null || value.signum() == 0) {
					iterator.remove();
				} else {
					parcTemp.setAnnee(0, value);
					for (int i = 1; i <= pasdeTemps; i++) {
						parcTemp.setAnnee(i, BigDecimal.ZERO);
					}
					totMap.put(totMapKey, parcTemp);
				}

			}

		} else {
			Iterator<String> iterator = totMap.keySet().iterator();
			while (iterator.hasNext()) {
				String totMapKey = iterator.next();
				parcTemp = new Conso(totMap.get(totMapKey));
				value = parcTemp.getAnnee(pasdeTemps);
				if (value == null || value.signum() == 0) {
					iterator.remove();
				} else {
					parcTemp.setAnnee(0, value);
					for (int i = 1; i <= pasdeTemps; i++) {
						parcTemp.setAnnee(i, BigDecimal.ZERO);
					}
					totMap.put(totMapKey, parcTemp);
				}

			}
		}
	}

	protected void transfertMapConsoU(HashMap<String, ResultConsoURt> resultConsoUMap, int pasdeTemps, int annee) {

		ResultConsoURt resultTemp = new ResultConsoURt(pasdeTemps);

		if (annee == 2010) {
			Iterator<String> iterator = resultConsoUMap.keySet().iterator();
			while (iterator.hasNext()) {
				String resultKey = iterator.next();
				resultTemp = new ResultConsoURt(resultConsoUMap.get(resultKey));
				BigDecimal valueEclairageEF = BigDecimal.ZERO;
				BigDecimal valueEcsEF = BigDecimal.ZERO;
				BigDecimal valueEclairageEP = BigDecimal.ZERO;
				BigDecimal valueEcsEP = BigDecimal.ZERO;
				if (resultTemp.getConsoUEclairageEF(1) != null) {
					valueEclairageEF = resultTemp.getConsoUEclairageEF(1);
				}
				if (resultTemp.getConsoUEcsEF(1) != null) {
					valueEcsEF = resultTemp.getConsoUEcsEF(1);
				}
				resultTemp.setConsoUEcsEF(0, valueEcsEF);
				resultTemp.setConsoUEclairageEF(0, valueEclairageEF);

				if (resultTemp.getConsoUEclairageEP(1) != null) {
					valueEclairageEP = resultTemp.getConsoUEclairageEP(1);
				}
				if (resultTemp.getConsoUEcsEP(1) != null) {
					valueEcsEP = resultTemp.getConsoUEcsEP(1);
				}
				resultTemp.setConsoUEcsEP(0, valueEcsEP);
				resultTemp.setConsoUEclairageEP(0, valueEclairageEP);
				resultTemp.setSurfTot(0, resultTemp.getSurfTot(1));
				for (int i = 1; i <= pasdeTemps; i++) {
					resultTemp.setConsoUEcsEF(i, BigDecimal.ZERO);
					resultTemp.setConsoUEclairageEF(i, BigDecimal.ZERO);
					resultTemp.setConsoUEcsEP(i, BigDecimal.ZERO);
					resultTemp.setConsoUEclairageEP(i, BigDecimal.ZERO);
					resultTemp.setSurfTot(i, BigDecimal.ZERO);
				}
				resultConsoUMap.put(resultKey, resultTemp);

			}

		} else {
			Iterator<String> iterator = resultConsoUMap.keySet().iterator();
			while (iterator.hasNext()) {
				String totMapKey = iterator.next();
				resultTemp = new ResultConsoURt(resultConsoUMap.get(totMapKey));
				BigDecimal valueEclairageEF = BigDecimal.ZERO;
				BigDecimal valueEcsEF = BigDecimal.ZERO;
				BigDecimal valueEclairageEP = BigDecimal.ZERO;
				BigDecimal valueEcsEP = BigDecimal.ZERO;
				if (resultTemp.getConsoUEclairageEF(pasdeTemps) != null) {
					valueEclairageEF = resultTemp.getConsoUEclairageEF(pasdeTemps);
				}
				if (resultTemp.getConsoUEcsEF(pasdeTemps) != null) {
					valueEcsEF = resultTemp.getConsoUEcsEF(pasdeTemps);
				}
				resultTemp.setConsoUEcsEF(0, valueEcsEF);
				resultTemp.setConsoUEclairageEF(0, valueEclairageEF);

				if (resultTemp.getConsoUEclairageEP(pasdeTemps) != null) {
					valueEclairageEP = resultTemp.getConsoUEclairageEP(pasdeTemps);
				}
				if (resultTemp.getConsoUEcsEP(pasdeTemps) != null) {
					valueEcsEP = resultTemp.getConsoUEcsEP(pasdeTemps);
				}
				resultTemp.setConsoUEcsEP(0, valueEcsEP);
				resultTemp.setConsoUEclairageEP(0, valueEclairageEP);
				resultTemp.setSurfTot(0, resultTemp.getSurfTot(pasdeTemps));
				for (int i = 1; i <= pasdeTemps; i++) {
					resultTemp.setConsoUEcsEF(i, BigDecimal.ZERO);
					resultTemp.setConsoUEclairageEF(i, BigDecimal.ZERO);
					resultTemp.setConsoUEcsEP(i, BigDecimal.ZERO);
					resultTemp.setConsoUEclairageEP(i, BigDecimal.ZERO);
					resultTemp.setSurfTot(i, BigDecimal.ZERO);
				}

				resultConsoUMap.put(totMapKey, resultTemp);
			}

		}

	}

	protected void transfertMapConsoUClim(HashMap<String, ResultConsoUClim> resultConsoUClimMap, int pasdeTemps,
			int annee) {

		ResultConsoUClim resultTemp = new ResultConsoUClim(pasdeTemps);

		if (annee == 2010) {
			Iterator<String> iterator = resultConsoUClimMap.keySet().iterator();
			while (iterator.hasNext()) {
				String resultKey = iterator.next();
				resultTemp = new ResultConsoUClim(resultConsoUClimMap.get(resultKey));
				BigDecimal valueClimEF = BigDecimal.ZERO;
				BigDecimal valueClimEP = BigDecimal.ZERO;

				if (resultTemp.getConsoUClimEF(1) != null) {
					valueClimEF = resultTemp.getConsoUClimEF(1);
				}
				if (resultTemp.getConsoUClimEP(1) != null) {
					valueClimEP = resultTemp.getConsoUClimEP(1);
				}

				resultTemp.setConsoUClimEF(0, valueClimEF);
				resultTemp.setConsoUClimEP(0, valueClimEP);
				resultTemp.setSurfTot(0, resultTemp.getSurfTot(1));
				for (int i = 1; i <= pasdeTemps; i++) {

					resultTemp.setConsoUClimEF(i, BigDecimal.ZERO);
					resultTemp.setConsoUClimEP(i, BigDecimal.ZERO);
					resultTemp.setSurfTot(i, BigDecimal.ZERO);
				}
				resultConsoUClimMap.put(resultKey, resultTemp);

			}

		} else {
			Iterator<String> iterator = resultConsoUClimMap.keySet().iterator();
			while (iterator.hasNext()) {
				String totMapKey = iterator.next();
				resultTemp = new ResultConsoUClim(resultConsoUClimMap.get(totMapKey));
				BigDecimal valueClimEF = BigDecimal.ZERO;
				BigDecimal valueClimEP = BigDecimal.ZERO;

				if (resultTemp.getConsoUClimEF(pasdeTemps) != null) {
					valueClimEF = resultTemp.getConsoUClimEF(pasdeTemps);
				}
				if (resultTemp.getConsoUClimEP(pasdeTemps) != null) {
					valueClimEP = resultTemp.getConsoUClimEP(pasdeTemps);
				}

				resultTemp.setConsoUClimEF(0, valueClimEF);
				resultTemp.setConsoUClimEP(0, valueClimEP);
				resultTemp.setSurfTot(0, resultTemp.getSurfTot(pasdeTemps));
				for (int i = 1; i <= pasdeTemps; i++) {

					resultTemp.setConsoUClimEF(i, BigDecimal.ZERO);
					resultTemp.setConsoUClimEP(i, BigDecimal.ZERO);
					resultTemp.setSurfTot(i, BigDecimal.ZERO);
				}

				resultConsoUClimMap.put(totMapKey, resultTemp);
			}

		}

	}

	protected ResultConsoRt initializeChauffClim(HashMap<String, ResultConsoUClim> resultConsoUClimMap,
			ResultConsoRt resultatsConsoRt, String idAgregParc, int pasdeTemps,
			HashMap<String, ParamRdtCout> rdtCoutMap, String usage, BigDecimal calageParc, 
			HashMap<String, ParamCalageEner> calageEner) {

		if (usage.equals(Usage.CLIMATISATION.getLabel())) {
			// Initialisation de la climatisation
			HashMap<String, ParamCalageEner> calageEnerClim = new HashMap<String, ParamCalageEner>();
			resultatsConsoRt.put(MapResultsKeys.BESOIN_CLIM.getLabel(),
					loadTableClimdas.loadMapResultBesoin("Climatisation_init", idAgregParc, pasdeTemps, calageParc, calageEnerClim));
			resultatsConsoRt.put(MapResultsKeys.RDT_CLIM.getLabel(), initializeConsoService.initializeRdtClim(
					rdtCoutMap, resultatsConsoRt.getMap("Besoins_clim"), pasdeTemps));
			resultatsConsoRt.put(
					MapResultsKeys.CONSO_CLIM.getLabel(),
					initializeConsoService.initializeConsoClim(resultConsoUClimMap,
							resultatsConsoRt.getMap(MapResultsKeys.RDT_CLIM.getLabel()),
							resultatsConsoRt.getMap(MapResultsKeys.BESOIN_CLIM.getLabel()), pasdeTemps));
			HashMap<String, Conso> coutClim = new HashMap<String, Conso>();
			resultatsConsoRt.put(MapResultsKeys.COUT_CLIM.getLabel(), coutClim);
		} else {
			// Chargement des tables de chauffage
			resultatsConsoRt.put(MapResultsKeys.BESOIN_CHAUFF.getLabel(),
					loadTableClimdas.loadMapResultBesoin("Chauffage_init", idAgregParc, pasdeTemps, calageParc, calageEner));
			resultatsConsoRt.put(
					MapResultsKeys.RDT_CHAUFF.getLabel(),
					initializeConsoService.initializeRdtChauff(rdtCoutMap,
							resultatsConsoRt.getMap(MapResultsKeys.BESOIN_CHAUFF.getLabel()), pasdeTemps));
			resultatsConsoRt.put(
					MapResultsKeys.CONSO_CHAUFF.getLabel(),
					initializeConsoService.initializeConsoChauffClim(
							resultatsConsoRt.getMap(MapResultsKeys.RDT_CHAUFF.getLabel()),
							resultatsConsoRt.getMap(MapResultsKeys.BESOIN_CHAUFF.getLabel()), pasdeTemps));
		}
		return resultatsConsoRt;
	}

	private ResultConsoRdt InitializeEcs(String idAgregParc, int pasdeTemps,
			HashMap<String, ParamRdtEcs> bibliRdtEcsMap, HashMap<String, ResultConsoURt> resultConsoURtMap, 
			BigDecimal calageParc) {
		ResultConsoRdt resultatsConsoEcs = new ResultConsoRdt();
		resultatsConsoEcs.put(MapResultsKeys.BESOIN_ECS.getLabel(),
				loadTableRtdas.loadMapResultBesoin("ECS_init", idAgregParc, pasdeTemps, calageParc));
		resultatsConsoEcs.put(
				MapResultsKeys.RDT_ECS.getLabel(),
				initializeConsoService.initializeRdtEcs(bibliRdtEcsMap,
						resultatsConsoEcs.getMap(MapResultsKeys.BESOIN_ECS.getLabel()), pasdeTemps));
		resultatsConsoEcs.put(MapResultsKeys.CONSO_ECS.getLabel(), initializeConsoService.initializeConsoEcs(
				resultatsConsoEcs.getMap(MapResultsKeys.RDT_ECS.getLabel()),
				resultatsConsoEcs.getMap(MapResultsKeys.BESOIN_ECS.getLabel()), pasdeTemps, resultConsoURtMap));
		HashMap<String, Conso> coutMap = new HashMap<String, Conso>();
		resultatsConsoEcs.put(MapResultsKeys.COUT_ECS.getLabel(), coutMap);
		return resultatsConsoEcs;
	}

	protected int calcBoucle(int annee, int pasdeTemps) {
		int anneeNTab;
		if (annee == 2010) {
			anneeNTab = 1;
		} else {
			double temp = (annee - 2011) / pasdeTemps;
			int nbBoucle = (int) temp;

			anneeNTab = ((annee - 2010) - pasdeTemps * nbBoucle);
		}
		return anneeNTab;
	}

	protected int calcPasdeTemps(int annee, int pasdeTemps) {

		if (annee + 1 + pasdeTemps > 2050) {
			return 2050 - annee;
		}

		return pasdeTemps;

	}

	protected CEE getCEE(List<CEE> subventions, int annee) {
		String periodeFin = commonService.correspPeriodeFin(annee);
		CEE subCEE = new CEE();
		for (CEE sub : subventions) {

			if (sub.getPeriode().equals(periodeFin)) {
				subCEE = sub;
			}

		}
		return subCEE;
	}
	
	protected CEE getCEEannuel(List<CEE> subventions, int annee) {
		CEE subCEE = new CEE();
		subCEE.setPeriode(commonService.correspPeriodeString(annee));
		// verifier si on doit set la branche
		if(annee == 2015){
			subCEE.setPrixKWhCumac(politiques.pCEE2015);
		} else
		if(annee == 2016){
			subCEE.setPrixKWhCumac(politiques.pCEE2016);
		}else
		if(annee == 2017){
			subCEE.setPrixKWhCumac(politiques.pCEE2017);
		}else
		if(annee == 2018){
			subCEE.setPrixKWhCumac(politiques.pCEE2018);
		}else
		if(annee == 2019){
			subCEE.setPrixKWhCumac(politiques.pCEE2019);
		}else
		if(annee == 2020){
			subCEE.setPrixKWhCumac(politiques.pCEE2020);
		}else {
			subCEE.setPrixKWhCumac(BigDecimal.ZERO);
		}
			
			
		return subCEE;
	}
	protected List<Financement> getFinListPeriode(List<Financement> ensembleFinancements, int annee) {
		// on recupere les financements valables pour la periode
		String periodeFin = commonService.correspPeriodeFin(annee);
		List<Financement> listeFin = new ArrayList<Financement>();
		for (Financement fin : ensembleFinancements) {

			if (((PBC) fin).getPeriode().equals(periodeFin)) {
				listeFin.add(fin);
			}

		}
		return listeFin;
	}

	protected HashMap<String, BigDecimal> createCalageObject(HashMap<String, PartMarcheRenov> partMarcheMap) {
		HashMap<String, BigDecimal> calageObject = new HashMap<String, BigDecimal>();

		for (String calagePart : partMarcheMap.keySet()) {

			PartMarcheRenov geste = partMarcheMap.get(calagePart);
			String oldKey = geste.getId();
			if (calageObject.containsKey(oldKey)) {
				BigDecimal part = calageObject.get(oldKey).add(geste.getPart());
				calageObject.put(oldKey, part);
			} else {

				calageObject.put(oldKey, geste.getPart());
			}

			// PartMarcheRenov calage = partMarcheMap.get(calagePart);
			// if (calage.getTypeRenovSys().equals(TypeRenovSysteme.ETAT_INIT)
			// && calage.getTypeRenovBat().equals(TypeRenovBati.ETAT_INIT)) {
			//
			// calageObject.put(calage.getId(), calage.getPart());
			//
			// }

		}
		return calageObject;
	}
	

	private String getIdOccupant(String idAgreg) {

		return idAgreg.substring(START_ID_OCCUPANT, START_ID_OCCUPANT + LENGTH_ID_OCCUPANT);
	}
	private String getIdBranche(String idAgreg) {

		return idAgreg.substring(START_ID_BRANCHE, START_ID_BRANCHE + LENGTH_ID_BRANCHE);
	}
	
	
	private String getIdBesoinChauf(String idParamRdt) {
		return idParamRdt.substring(START_ID_COURANT, START_ID_COURANT + LENGTH_ID_BRANCHE)
				.concat(idParamRdt.substring(START_BATTYPE, START_BATTYPE + BATTYPE_LENGTH)).concat("Chauffage");
	}
	
}

