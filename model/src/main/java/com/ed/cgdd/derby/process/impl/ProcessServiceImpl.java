package com.ed.cgdd.derby.process.impl;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import com.ed.cgdd.derby.model.financeObjects.*;
import com.ed.cgdd.derby.model.parc.*;
import com.ed.cgdd.derby.model.CalibParameters;
import com.ed.cgdd.derby.model.politiques;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import com.ed.cgdd.derby.calibrageCINT.CalibrageDAS;
import com.ed.cgdd.derby.calibrageCINT.CalibrageService;
import com.ed.cgdd.derby.common.CommonService;
import com.ed.cgdd.derby.excelresult.ExcelCoutsService;
import com.ed.cgdd.derby.excelresult.ExcelEtiquetteService;
import com.ed.cgdd.derby.excelresult.ExcelResultService;
import com.ed.cgdd.derby.excelresult.ExcelXCoutsService;
import com.ed.cgdd.derby.excelresult.ExcelXEtiquetteService;
import com.ed.cgdd.derby.excelresult.ExcelXResultService;
import com.ed.cgdd.derby.finance.CreateNeufService;
import com.ed.cgdd.derby.finance.FinanceService;
import com.ed.cgdd.derby.finance.GesteService;
import com.ed.cgdd.derby.finance.InsertResultFinancementDAS;
import com.ed.cgdd.derby.finance.RecupParamFinDAS;
import com.ed.cgdd.derby.finance.TruncateTableResFinanceDAS;
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
import com.ed.cgdd.derby.model.progression.Progression;
import com.ed.cgdd.derby.model.progression.ProgressionStep;
import com.ed.cgdd.derby.parc.InsertParcDAS;
import com.ed.cgdd.derby.parc.LoadParcDataDAS;
import com.ed.cgdd.derby.parc.ParamCalageEner;
import com.ed.cgdd.derby.parc.ParcService;
import com.ed.cgdd.derby.parc.TruncateParcTableDAS;
import com.ed.cgdd.derby.process.InitializeConsoService;
import com.ed.cgdd.derby.process.ProcessService;
import com.ed.cgdd.derby.usagesnonrt.BureauProcessService;
import com.ed.cgdd.derby.usagesnonrt.CuissonAutreService;
import com.ed.cgdd.derby.usagesnonrt.FroidAlimService;
import com.ed.cgdd.derby.usagesnonrt.InsertUsagesNonRTDAS;
import com.ed.cgdd.derby.usagesnonrt.LoadTableUsagesNonRTDAS;
import com.ed.cgdd.derby.usagesnonrt.TruncateTableUsagesNonRTDAS;
import com.ed.cgdd.derby.usagesrt.ChauffageService;
import com.ed.cgdd.derby.usagesrt.ClimatisationService;
import com.ed.cgdd.derby.usagesrt.EclairageService;
import com.ed.cgdd.derby.usagesrt.EcsService;
import com.ed.cgdd.derby.usagesrt.InsertUsagesRTDAS;
import com.ed.cgdd.derby.usagesrt.LoadTableChauffClimDAS;
import com.ed.cgdd.derby.usagesrt.LoadTableEffetRebondDAS;
import com.ed.cgdd.derby.usagesrt.LoadTableRtDAS;
import com.ed.cgdd.derby.usagesrt.TruncateTableUsagesRTDAS;

public class ProcessServiceImpl implements ProcessService {
	private final static Logger LOG = LogManager.getLogger(ProcessServiceImpl.class);

	private final static int NB_THREAD =40;
	public static final boolean checkXlsX = true;
	private ParcService parcService;
	private LoadParcDataDAS loadParcDatadas;
	private InsertParcDAS insertParcdas;
	private TruncateParcTableDAS truncateParcTabledas;
	private BureauProcessService bureauProcessService;
	private CuissonAutreService cuissonAutreService;
	private FroidAlimService froidAlimService;
	private InsertUsagesNonRTDAS insertUsagesNonRTdas;
	private LoadTableUsagesNonRTDAS loadTableUsagesNonRTdas;
	private TruncateTableUsagesNonRTDAS truncateTableUsagesNonRTdas;
	private EcsService ecsService;
	private ClimatisationService climatisationService;
	private ChauffageService chauffageService;
	private EclairageService eclairageService;
	private InsertUsagesRTDAS insertUsagesRTdas;
	private LoadTableRtDAS loadTableRtdas;
	private LoadTableChauffClimDAS loadTableClimdas;
	private TruncateTableUsagesRTDAS truncateTableUsagesRTdas;
	private InitializeConsoService initializeConsoService;
	private CommonService commonService;
	private FinanceService financeService;
	private CreateNeufService createNeufService;

	private ExcelResultService excelResultService;
	private ExcelCoutsService excelCoutsService;
	private ExcelEtiquetteService excelEtiquetteService;
	private ExcelXResultService excelXResultService;
	private ExcelXCoutsService excelXCoutsService;
	private ExcelXEtiquetteService excelXEtiquetteService;
	private CalibrageDAS calibrageDAS;
	private CalibrageService calibrageService;
	private RecupParamFinDAS recupParamFinDAS;
	private GesteService gesteService;
	private InsertResultFinancementDAS insertResultFinancementDAS;
	private TruncateTableResFinanceDAS truncateTableResFinanceDAS;
	private LoadTableEffetRebondDAS loadTableEffetRebondDAS;

	public ExcelCoutsService getExcelCoutsService() {
		return excelCoutsService;
	}

	public void setExcelCoutsService(ExcelCoutsService excelCoutsService) {
		this.excelCoutsService = excelCoutsService;
	}
	public ExcelXCoutsService getExcelXCoutsService() {
		return excelXCoutsService;
	}

	public void setExcelXCoutsService(ExcelXCoutsService excelXCoutsService) {
		this.excelXCoutsService = excelXCoutsService;
	}
	public CalibrageDAS getCalibrageDAS() {
		return calibrageDAS;
	}

	public void setCalibrageDAS(CalibrageDAS calibrageDAS) {
		this.calibrageDAS = calibrageDAS;
	}

	public CalibrageService getCalibrageService() {
		return calibrageService;
	}

	public void setCalibrageService(CalibrageService calibrageService) {
		this.calibrageService = calibrageService;
	}

	public ChauffageService getChauffageService() {
		return chauffageService;
	}

	public void setChauffageService(ChauffageService chauffageService) {
		this.chauffageService = chauffageService;
	}

	public CreateNeufService getCreateNeufService() {
		return createNeufService;
	}

	public void setCreateNeufService(CreateNeufService createNeufService) {
		this.createNeufService = createNeufService;
	}

	public ExcelResultService getExcelResultService() {
		return excelResultService;
	}

	public void setExcelResultService(ExcelResultService excelResultService) {
		this.excelResultService = excelResultService;
	}

	public void setExcelResult(ExcelResultService excelResult) {
		this.excelResultService = excelResult;
	}
	public ExcelXResultService getExcelXResultService() {
		return excelXResultService;
	}

	public void setExcelXResultService(ExcelXResultService excelXResultService) {
		this.excelXResultService = excelXResultService;
	}

	public void setExcelXResult(ExcelXResultService excelXResult) {
		this.excelXResultService = excelXResult;
	}
	
	public FinanceService getFinanceService() {
		return financeService;
	}

	public void setFinanceService(FinanceService financeService) {
		this.financeService = financeService;
	}

	public LoadTableRtDAS getLoadTableRtdas() {
		return loadTableRtdas;
	}

	public void setLoadTableRtdas(LoadTableRtDAS loadTableRtdas) {
		this.loadTableRtdas = loadTableRtdas;
	}

	public EclairageService getEclairageService() {
		return eclairageService;
	}

	public void setEclairageService(EclairageService eclairageService) {
		this.eclairageService = eclairageService;
	}

	public ClimatisationService getClimatisationService() {
		return climatisationService;
	}

	public void setClimatisationService(ClimatisationService climatisationService) {
		this.climatisationService = climatisationService;
	}

	public LoadTableChauffClimDAS getLoadTableClimdas() {
		return loadTableClimdas;
	}

	public void setLoadTableClimdas(LoadTableChauffClimDAS loadTableClimdas) {
		this.loadTableClimdas = loadTableClimdas;
	}

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

	public EcsService getEcsService() {
		return ecsService;
	}

	public void setEcsService(EcsService ecsService) {
		this.ecsService = ecsService;
	}

	public InsertUsagesRTDAS getInsertUsagesRTdas() {
		return insertUsagesRTdas;
	}

	public void setInsertUsagesRTdas(InsertUsagesRTDAS insertUsagesRTdas) {
		this.insertUsagesRTdas = insertUsagesRTdas;
	}

	public TruncateTableUsagesRTDAS getTruncateTableUsagesRTdas() {
		return truncateTableUsagesRTdas;
	}

	public void setTruncateTableUsagesRTdas(TruncateTableUsagesRTDAS truncateTableUsagesRTdas) {
		this.truncateTableUsagesRTdas = truncateTableUsagesRTdas;
	}

	public FroidAlimService getFroidAlimService() {
		return froidAlimService;
	}

	public void setFroidAlimService(FroidAlimService froidAlimService) {
		this.froidAlimService = froidAlimService;
	}

	public BureauProcessService getBureauProcessService() {
		return bureauProcessService;
	}

	public void setBureauProcessService(BureauProcessService bureauProcessService) {
		this.bureauProcessService = bureauProcessService;
	}

	public CuissonAutreService getCuissonAutreService() {
		return cuissonAutreService;
	}

	public void setCuissonAutreService(CuissonAutreService cuissonAutreService) {
		this.cuissonAutreService = cuissonAutreService;
	}

	public InsertUsagesNonRTDAS getInsertUsagesNonRTdas() {
		return insertUsagesNonRTdas;
	}

	public void setInsertUsagesNonRTdas(InsertUsagesNonRTDAS insertUsagesNonRTdas) {
		this.insertUsagesNonRTdas = insertUsagesNonRTdas;
	}

	public LoadTableUsagesNonRTDAS getLoadTableUsagesNonRTdas() {
		return loadTableUsagesNonRTdas;
	}

	public void setLoadTableUsagesNonRTdas(LoadTableUsagesNonRTDAS loadTableUsagesNonRTdas) {
		this.loadTableUsagesNonRTdas = loadTableUsagesNonRTdas;
	}

	public TruncateTableUsagesNonRTDAS getTruncateTableUsagesNonRTdas() {
		return truncateTableUsagesNonRTdas;
	}

	public void setTruncateTableUsagesNonRTdas(TruncateTableUsagesNonRTDAS truncateTableUsagesNonRTdas) {
		this.truncateTableUsagesNonRTdas = truncateTableUsagesNonRTdas;
	}

	public LoadTableEffetRebondDAS getLoadTableEffetRebondDAS() {
		return loadTableEffetRebondDAS;
	}

	public void setLoadTableEffetRebondDAS(LoadTableEffetRebondDAS loadTableEffetRebondDAS) {
		this.loadTableEffetRebondDAS = loadTableEffetRebondDAS;
	}

	public ParcService getParcService() {
		return parcService;
	}

	public void setParcService(ParcService parcService) {
		this.parcService = parcService;
	}

	public LoadParcDataDAS getLoadParcDatadas() {
		return loadParcDatadas;
	}

	public void setLoadParcDatadas(LoadParcDataDAS loadParcDatadas) {
		this.loadParcDatadas = loadParcDatadas;
	}

	public InsertParcDAS getInsertParcdas() {
		return insertParcdas;
	}

	public void setInsertParcdas(InsertParcDAS insertParcdas) {
		this.insertParcdas = insertParcdas;
	}

	public TruncateParcTableDAS getTruncateParcTabledas() {
		return truncateParcTabledas;
	}

	public void setTruncateParcTabledas(TruncateParcTableDAS truncateParcTabledas) {
		this.truncateParcTabledas = truncateParcTabledas;
	}

	public RecupParamFinDAS getRecupParamFinDAS() {
		return recupParamFinDAS;
	}

	public void setRecupParamFinDAS(RecupParamFinDAS recupParamFinDAS) {
		this.recupParamFinDAS = recupParamFinDAS;
	}

	public InsertResultFinancementDAS getInsertResultFinancementDAS() {
		return insertResultFinancementDAS;
	}

	public void setInsertResultFinancementDAS(InsertResultFinancementDAS insertResultFinancementDAS) {
		this.insertResultFinancementDAS = insertResultFinancementDAS;
	}

	public TruncateTableResFinanceDAS getTruncateTableResFinanceDAS() {
		return truncateTableResFinanceDAS;
	}

	public void setTruncateTableResFinanceDAS(TruncateTableResFinanceDAS truncateTableResFinanceDAS) {
		this.truncateTableResFinanceDAS = truncateTableResFinanceDAS;
	}

	public GesteService getGesteService() {
		return gesteService;
	}

	public void setGesteService(GesteService gesteService) {
		this.gesteService = gesteService;
	}

	@Override
	public void process(Progression progression, ParamCintObjects paramCintObjects) throws IOException {

		long start = System.currentTimeMillis();

		// Chargement et initialisation des donnees

		int pasdeTempsInit = 1;
		float txRenovBati = loadTxRenovBati();

		// Chargement des parametres influant sur l'evolution du parc
		List<ParamParcArray> entrees = loadParcDatadas.getParamEntreesMapper();
		List<ParamParcArray> sorties = loadParcDatadas.getParamSortiesMapper();
		HashMap<String, ParamParcArray> entreesMap = parcService.sortData(entrees);
		HashMap<String, ParamParcArray> sortiesMap = parcService.sortData(sorties);

		// Chargement des besoins par usage pour les batiments neufs
		HashMap<String, ParamBesoinsNeufs> bNeufsMap = loadTableUsagesNonRTdas.loadTableBesoinsNeufs("BesoinU_neuf");
		HashMap<String, ParamBesoinsNeufs> copyBNeufs = loadTableUsagesNonRTdas.loadTableBesoinsNeufs("BesoinU_neuf");

		// Chargement de l'impact de l'effet rebond
		HashMap<String, EffetRebond> effetRebond = loadTableEffetRebondDAS.recupEffetRebond("Effet_Rebond");

		// Chargement des parametres influant sur l'evolution des besoins non
		// reglementes
		HashMap<String, ParamGainsUsages> gainsNonRTMap = loadTableUsagesNonRTdas.loadTableGainsNonRT("Gains_nonRT");
		HashMap<String, BigDecimal> dvUsagesMap = loadTableUsagesNonRTdas.loadTableDVNonRT("DV_autres");
		HashMap<String, ParamPMConso> pmCuissonMap = loadTableUsagesNonRTdas.loadTablePMConso("PM_cuisson");
		HashMap<String, ParamPMConso> pmAutresMap = loadTableUsagesNonRTdas.loadTablePMConso("PM_autres");
		HashMap<String, ParamPMConsoChgtSys> pmCuissonChgtMap = loadTableUsagesNonRTdas
				.loadTablePMConsoChgtSys("PM_cuisson_chgt");
		HashMap<String, ParamPMConsoChgtSys> pmAutresChgtMap = loadTableUsagesNonRTdas
				.loadTablePMConsoChgtSys("PM_autres_chgt");
		HashMap<String, BigDecimal> rythmeFrdRgltMap = loadTableUsagesNonRTdas
				.loadTableRythmeFroidRglt("Rythme_Froid_Alim_Rglt");
		HashMap<String, BigDecimal> gainFrdRgltMap = loadTableUsagesNonRTdas
				.loadTableGainFroidRglt("Gains_Froid_Alim_Rglt");

		// Chargement des paramatres influant sur l'evolution des besoins
		// en ECS
		HashMap<String, ParamPMConso> pmEcsNeufMap = loadTableRtdas.loadTablePMEcs("PM_ECS_Neuf");

		HashMap<String, ParamPMConsoChgtSys> pmEcsChgtMap = loadTableRtdas.loadTablePMECSChgtSys("PM_ECS_Chgt");

		HashMap<String, ParamRdtEcs> bibliRdtEcsMap = loadTableRtdas.loadTableRdtEcs("Rendement_ECS");

		HashMap<String, ParamRdtPerfEcs> rdtPerfEcsMap = loadTableRtdas.loadTableRdtPerfEcs("Rendement_performant_ECS");

		HashMap<String, ParamPartSolaireEcs> partSolaireMap = loadTableRtdas.loadTablePartSolaireEcs("Solaire_ECS");

		HashMap<String, ParamTauxCouvEcs> txCouvSolaireMap = loadTableRtdas
				.loadTableTxCouvSolaireEcs("Tx_Couv_Solaire");

		HashMap<String, ParamPartSysPerfEcs> partSysPerfEcsMap = loadTableRtdas
				.loadTablePartSysPerfEcs("Part_sys_performants_ECS");
		HashMap<String, BigDecimal> dvEcsMap = loadTableRtdas.loadTableDvEcs("DV_ECS");

		HashMap<String, ParamCoutEcs> coutEcsMap = loadTableRtdas.loadTableCoutEcs("ECS_couts");

		// Chargement des paramatres influant sur l'evolution des besoins
		// en Climatisation

		HashMap<String, ParamRdtCout> rdtCoutClimMap = loadTableClimdas.loadTableRdtCout("Rdt_climatisation");

		HashMap<String, ParamTxClimExistant> txClimExistantMap = loadTableClimdas
				.loadTableTxClimExistant("Tx_Clim_Existant");

		HashMap<String, ParamTxClimNeuf> txClimNeufMap = loadTableClimdas.loadTableTxClimNeuf("Tx_Clim_Neuf");

		// Chargement des parametres influant sur l'evolution des besoin en
		// Chauffage
		HashMap<String, ParamRdtCout> rdtCoutChauffMap = loadTableClimdas.loadTableRdtCoutChauf("Rdt_chauffage");
		HashMap<String, BigDecimal> dvChauffMap = loadTableClimdas.loadTableDvChauff("DV_chauffage");
		HashMap<TypeRenovBati, BigDecimal> dvGesteMap = loadTableClimdas.loadTableDvGeste("DV_parois");

		// Chargement des parametres influant sur l'evolution des consommations
		// d'auxiliaires
		HashMap<String, ParamRatioAux> auxChaud = loadTableRtdas.loadTableRatioAuxChaud("Ratio_aux_chaud");
		HashMap<String, ParamRatioAux> auxFroid = loadTableRtdas.loadTableRatioAuxClim("Ratio_aux_froid");

		// Chargement des paramatres influant sur l'evolution des besoins
		// en Eclairage et ventilation

		HashMap<String, ParamGainsUsages> gainsEclairageMap = loadTableRtdas.loadTableGainsEclairage("Gains_eclairage");
		HashMap<String, ParamGainsUsages> gainsVentilationMap = loadTableRtdas
				.loadTableGainsVentilation("Gains_ventilation");
		HashMap<String, ParamCoutEclVentil> coutsEclVentilMap = loadTableRtdas
				.loadTableCoutEclVentil("Ecl_ventil_couts");
		
		// Recuperation des couts de maintenance
				HashMap<String, Maintenance> maintenanceMap = new HashMap<String, Maintenance>();
				maintenanceMap = recupParamFinDAS.recupMaintenance();

		// Chargement du prix des energies et de la contribution climat energie
		HashMap<Integer, CoutEnergie> coutEnergieMap = recupParamFinDAS.recupCoutEnergie("Cout_energie");

		// Initialisation des couts intangibles
		HashMap<String, CalibCI> cintMap = calibrageDAS.recupCI();
		HashMap<String, CalibCI> cintMapNeuf = calibrageDAS.recupCINeuf();
		HashMap<String, CalibCIBati> cintBatiMap = calibrageDAS.recupCIBati();

		// Ajout 21092017
		calibrageService.addingRowsInHashMap(cintMapNeuf,coutEnergieMap,bNeufsMap);

		// Couts intangibles dans l'existant
		HashMap<String,CalibCoutGlobal> coutIntangible = calibrageService.calibreCI(cintMap, paramCintObjects.getSysExist(), maintenanceMap);
		HashMap<String,CalibCoutGlobal> coutIntangibleBati = calibrageService.calibreCIBati(cintBatiMap, paramCintObjects.getGesteBat());

		// Couts intangibles dans le neuf
		HashMap<String,CalibCoutGlobal> coutIntangibleNeuf = calibrageService.calibreCI(cintMapNeuf, paramCintObjects.getSysNeuf(), maintenanceMap);

		// Enregistrement des couts intangibles
		LOG.debug("Insert couts intangibles");
		//calibrageDAS.insertCInt(coutIntangible, CIntType.SYS_EXISTANT);
		//calibrageDAS.insertCInt(coutIntangibleBati, CIntType.BATI);
		//calibrageDAS.insertCInt(coutIntangibleNeuf, CIntType.SYS_NEUF);
		LOG.debug("Insert couts intangibles - done");
		// Chargement de l'evolution du cout des techno et du bati
		HashMap<String, BigDecimal> evolCoutBati = recupParamFinDAS.getEvolutionCoutBati();
		HashMap<String, BigDecimal> evolCoutTechno = recupParamFinDAS.getEvolutionCoutTechno();

		// Chargement des periodes de construction
		Map<String, List<String>> periodeMap = gesteService.getPeriodMap();

		// Chargement des emissions de ges par energie, usage et periode (la cle
		// de la map est code energie + usage)
		HashMap<String, Emissions> emissionsMap = recupParamFinDAS.recupEmissions("Emissions");

		// Chargement des reglementations
		Reglementations reglementations = new Reglementations();
		// cle : idOccupant
		recupParamFinDAS.recupObligExig("Obligation_travaux_Ex", reglementations);
		// cle : idOccupant
		recupParamFinDAS.recupObligSurf("Obligation_travaux_Surf", reglementations);
		// cle : periode (string, ex : 2010-2015)
		recupParamFinDAS.recupRtExistant("Rt_existant", reglementations);
		// cle : idBranche + idOccupant
		recupParamFinDAS.recupDecret("Decret_travaux", reglementations);

		// Chargement des taux d'actu
		HashMap<String, TauxInteret> tauxInteretMap = new HashMap<String, TauxInteret>();
		tauxInteretMap = recupParamFinDAS.recupTauxInteret();

		// Recuperation de la surface moyenne
		HashMap<String, SurfMoy> surfMoyMap = new HashMap<String, SurfMoy>();
		surfMoyMap = recupParamFinDAS.recupSurfMoy();
		// Recuperation de l'evolution de la valeur verte
		HashMap<String, EvolValeurVerte> evolVVMap = new HashMap<String, EvolValeurVerte>();
		evolVVMap = recupParamFinDAS.recupEvolValeurVerte();
		// Recuperation de la repartion du statut d'occupation
		HashMap<String, RepartStatutOccup> repartStatutOccupMap = new HashMap<String, RepartStatutOccup>();
		repartStatutOccupMap = recupParamFinDAS.recupRepartStatutOccup();
		
		// Recuperation des facteurs d'elasticite-prix
		ElasticiteMap elasticiteMap = new ElasticiteMap();
		elasticiteMap = recupParamFinDAS.elasticite("Elasticite_prix", elasticiteMap);

		
		// Vidage des tables de resultats
		truncateResultTables();

		LOG.info("Truncate done");

		// Creation des deux maps de besoins pour les batiments entrant

		HashMap<String, ParamBesoinsNeufs> copyMapNeuf = modifBesoinUBatEx(copyBNeufs);

		// Creation de la HashMap contenant
		List<String> listeId = loadParcDatadas.getParamParcListeMapper();
		HashMap<String, List<String>> idAgregListMap = commonService.idAgregBoucleList(listeId);

		//Remplissage de la map pour l'evolution des besoins de chauffage et de climatisation 
		// (adaptation au CC et individualisation des frais de chauffage)
		EvolBesoinMap  evolBesoinMap = new EvolBesoinMap();
	    setEvolBesoin(evolBesoinMap,idAgregListMap); 

	  

	    // Remplissage de la Map de calage du parc par energie
	    HashMap<String, ParamCalageEner> calageEner = putCalageEner();
	 // Remplissage de la Map de calage du parc par branche
	    HashMap<String, BigDecimal> calageBranche = putCalageBranche();
	   
	    
		progression.setStep(ProgressionStep.CALCUL);
		progression.setParcSize(idAgregListMap.size());

		ExecutorService executor = Executors.newFixedThreadPool(NB_THREAD);
		List<Callable<Object>> process = new ArrayList<Callable<Object>>();
		for (String idAgregParc : idAgregListMap.keySet()) {
			ProcessServiceRunnable runnable = new ProcessServiceRunnable(pasdeTempsInit, paramCintObjects, txRenovBati, entreesMap,
					sortiesMap, bNeufsMap,copyMapNeuf, effetRebond, gainsNonRTMap, dvUsagesMap, pmCuissonMap, pmAutresMap,
					pmCuissonChgtMap, pmAutresChgtMap, rythmeFrdRgltMap, gainFrdRgltMap, pmEcsNeufMap, pmEcsChgtMap,
					bibliRdtEcsMap, rdtPerfEcsMap, partSolaireMap, txCouvSolaireMap, partSysPerfEcsMap, dvEcsMap,
					coutEcsMap, rdtCoutClimMap, txClimExistantMap, txClimNeufMap, rdtCoutChauffMap, dvChauffMap,
					dvGesteMap, auxChaud, auxFroid, gainsEclairageMap, gainsVentilationMap, coutsEclVentilMap,
					coutIntangible, coutIntangibleBati, evolCoutBati, evolCoutTechno, periodeMap, coutEnergieMap,
					emissionsMap, reglementations, idAgregParc, progression, tauxInteretMap, surfMoyMap, evolVVMap,
					repartStatutOccupMap, maintenanceMap, elasticiteMap, coutIntangibleNeuf, evolBesoinMap, calageEner, calageBranche);
			runnable.initServices(parcService, loadParcDatadas, insertParcdas, bureauProcessService,
					cuissonAutreService, froidAlimService, insertUsagesNonRTdas, loadTableUsagesNonRTdas, ecsService,
					climatisationService, chauffageService, eclairageService, insertUsagesRTdas, loadTableRtdas,
					loadTableClimdas, initializeConsoService, commonService, financeService, createNeufService,
					recupParamFinDAS, gesteService, insertResultFinancementDAS);
			if (NB_THREAD == 1) {
				runnable.processParc();
			} else {
				process.add(Executors.callable(runnable));
			}
		}

		try {
			if (NB_THREAD != 1) {
				List<Future<Object>> callables = executor.invokeAll(process);
			}
			// Recuperation des resultats
			// TODO export Xls
			//boolean isHidden = true;
			boolean isHidden = false;
			progression.setStep(ProgressionStep.EXTRACT);
			
			if(checkXlsX){
				excelXResultService.excelXService(pasdeTempsInit, isHidden);
				excelXCoutsService.excelXService(pasdeTempsInit, isHidden);
				excelXCoutsService.getContributionClimat(coutEnergieMap);
				excelXEtiquetteService.excelXService(pasdeTempsInit, isHidden);
					
			}else {
				excelResultService.excelService(pasdeTempsInit, isHidden);
				excelCoutsService.excelService(pasdeTempsInit, isHidden);
				excelCoutsService.getContributionClimat(coutEnergieMap);
				excelEtiquetteService.excelService(pasdeTempsInit, isHidden);
			}
			
			long end = System.currentTimeMillis();
			LOG.info("creation time : {}ms", end - start);
		} catch (InterruptedException e) {
			LOG.error(e);
		}

	}

private HashMap<String, ParamCalageEner> putCalageEner() {
	 
	HashMap<String, ParamCalageEner> CalageEner = new HashMap<String, ParamCalageEner>();
	ParamCalageEner paramCalageElec = new ParamCalageEner();
	ParamCalageEner paramCalageGaz = new ParamCalageEner();
	ParamCalageEner paramCalageFioul = new ParamCalageEner();
	ParamCalageEner paramCalageUrbain = new ParamCalageEner();
	ParamCalageEner paramCalageAutres = new ParamCalageEner();

	paramCalageElec.setFacteurCalageConso(CalibParameters.CalageConsoChauffElec);
	paramCalageGaz.setFacteurCalageConso(CalibParameters.CalageConsoChauffGaz);
	paramCalageFioul.setFacteurCalageConso(CalibParameters.CalageConsoChauffFioul);
	paramCalageUrbain.setFacteurCalageConso(CalibParameters.CalageConsoChauffUrbain);
	paramCalageAutres.setFacteurCalageConso(CalibParameters.CalageConsoChauffAutres);
	
	paramCalageElec.setFacteurCalageParc(CalibParameters.CalageParcChauffElec);
	paramCalageGaz.setFacteurCalageParc(CalibParameters.CalageParcChauffGaz);
	paramCalageFioul.setFacteurCalageParc(CalibParameters.CalageParcChauffFioul);
	paramCalageUrbain.setFacteurCalageParc(CalibParameters.CalageParcChauffUrbain);
	paramCalageAutres.setFacteurCalageParc(CalibParameters.CalageParcChauffAutres);
	
	
	CalageEner.put(Energies.ELECTRICITE.getCode(),paramCalageElec);
	CalageEner.put(Energies.GAZ.getCode(),paramCalageGaz);
	CalageEner.put(Energies.FIOUL.getCode(),paramCalageFioul);
	CalageEner.put(Energies.URBAIN.getCode(),paramCalageUrbain);
	CalageEner.put(Energies.AUTRES.getCode(),paramCalageAutres);
	return CalageEner;		
	}

private HashMap<String, BigDecimal> putCalageBranche() {
	HashMap<String, BigDecimal> CalageBranche = new HashMap<String, BigDecimal>();    
	CalageBranche.put(Branche.BUREAUX_ADMINISTRATION.getCode(),CalibParameters.CalageBranche01);
	CalageBranche.put(Branche.CAFE_HOSTEL_RESTAURANT.getCode(),CalibParameters.CalageBranche02);
	CalageBranche.put(Branche.COMMERCE.getCode(),CalibParameters.CalageBranche03);
	CalageBranche.put(Branche.ENSEIGNEMENT_RECHERCHE.getCode(),CalibParameters.CalageBranche04);
	CalageBranche.put(Branche.HABITAT_COMMUNAUTAIRE.getCode(),CalibParameters.CalageBranche05);
	CalageBranche.put(Branche.SANTE_ACTION_SOCIALE.getCode(),CalibParameters.CalageBranche06);
	CalageBranche.put(Branche.SPORT_LOISIR_CULTURE.getCode(),CalibParameters.CalageBranche07);
	CalageBranche.put(Branche.TRANSPORT.getCode(),CalibParameters.CalageBranche08);
	return CalageBranche;		
	}

private EvolBesoinMap setEvolBesoin(EvolBesoinMap evolBesoinMap, HashMap<String, List<String>> idAgregListMap) {
		
		for (String idAgregParc : idAgregListMap.keySet()) {
			for (int annee = 2010; annee <= 2050; annee++) { 
					for(Usage usage : Usage.values()){
						EvolutionBesoin evolbesoin = new EvolutionBesoin();
						evolbesoin.setIdBranche(idAgregParc.substring(0,2));
						evolbesoin.setAnnee(annee);
						evolbesoin.setUsage(usage);
						BigDecimal evolution = BigDecimal.ZERO; 
			
						if(politiques.checkAdaptationCC ){
							BigDecimal tcamBesoinChauff = BigDecimal.ZERO;
							BigDecimal tcamBesoinClim = BigDecimal.ZERO;
							
							if(annee > 2015 && annee < 2021){
							  tcamBesoinChauff = politiques.tcamBesoinChauff20152020;
							  tcamBesoinClim = politiques.tcamBesoinClim20152020;
							}
							if(annee > 2020 && annee < 2026){
								  tcamBesoinChauff = politiques.tcamBesoinChauff20202025;
								  tcamBesoinClim = politiques.tcamBesoinClim20202025;
							}
							if(annee > 2025 && annee < 2031){
								  tcamBesoinChauff = politiques.tcamBesoinChauff20252030;
								  tcamBesoinClim = politiques.tcamBesoinClim20252030;
							}
							if(annee > 2030 && annee <= 2050){
								  tcamBesoinChauff = politiques.tcamBesoinChauff20302050;
								  tcamBesoinClim = politiques.tcamBesoinClim20302050;
							}
							
							
							if(usage.equals(Usage.CHAUFFAGE)){	
								evolution = evolution.add(tcamBesoinChauff, MathContext.DECIMAL32);
							}
							
							if(usage.equals(Usage.CLIMATISATION)){				
								evolution =evolution.add(tcamBesoinClim, MathContext.DECIMAL32);
							}
							
						}
						if(annee > 2016 && annee < 2020 && politiques.checkIFC && usage.equals(Usage.CHAUFFAGE)){
								evolution =  evolution.add(politiques.GainBU_IFC_annuel, MathContext.DECIMAL32);
						}
			
			evolbesoin.setEvolution(evolution);
			evolBesoinMap.putEvolutionBesoin(evolbesoin);
			
				}
			}
		}

	return null;
}

	protected HashMap<String, ParamBesoinsNeufs> modifBesoinUBatEx(HashMap<String, ParamBesoinsNeufs> copyMapNeuf) {
		for(String keyMap : copyMapNeuf.keySet()){
		ParamBesoinsNeufs paramBesoinsNeufs = copyMapNeuf.get(keyMap);
		if(Arrays.asList(UsageRT.values()).stream().filter(p->p.toString().equals(Usage.getEnumName(paramBesoinsNeufs.getUsage())))
				.findAny().isPresent()){
			BigDecimal besoinUnitaire = null;
			for(int i=1 ; i<6 ; i++){
				besoinUnitaire = paramBesoinsNeufs.getPeriode(i);
				paramBesoinsNeufs.setPeriode(i,besoinUnitaire.multiply(politiques.modifBUBatEx, MathContext.DECIMAL32));
			}
		copyMapNeuf.put(keyMap,paramBesoinsNeufs);
		}}
		return copyMapNeuf;
	}




	protected void truncateResultTables() {
		truncateParcTabledas.truncateTable("Parc_entrant");
		truncateParcTabledas.truncateTable("Parc_sortant");
		truncateParcTabledas.truncateTable("Parc_resultats");
		truncateTableUsagesNonRTdas.truncateTable("Conso_non_RT_resultats");
		truncateTableUsagesRTdas.truncateTable("Conso_RT_resultats");
		truncateTableUsagesRTdas.truncateTable("Besoin_RT_resultats");
		truncateTableUsagesRTdas.truncateTable("Rendement_RT_resultats");
		truncateTableUsagesRTdas.truncateTable("Besoin_RT_resultats_test");
		truncateTableUsagesRTdas.truncateTable("Rendement_RT_resultats_test");
		truncateTableUsagesRTdas.truncateTable("Couts_resultats");
		truncateTableResFinanceDAS.truncateTable("Resultats_Financements");

	}

	public int loadPasTemps() throws IOException {
		double temp;
		int pasdeTemps;

		// Chargement du pas de temps attendu dans les resultats

		InputStream ExcelFileToUpdate = new FileInputStream("./Tables_param/Parametres_utilisateurs.xls");
		HSSFWorkbook wb = new HSSFWorkbook(ExcelFileToUpdate);
		HSSFSheet sheet = wb.getSheet("Accueil");

		int i = 1;
		int c = 1;
		HSSFCell cell = sheet.getRow(i).getCell(c);
		temp = cell.getNumericCellValue();
		pasdeTemps = (int) temp;
		if (pasdeTemps > 40) {
			pasdeTemps = 40;
		}
		return pasdeTemps;
	}


	public float loadTxRenovBati() throws IOException {
		double temp;

		// Chargement du parametre TxRenovBati

		InputStream ExcelFileToUpdate = new FileInputStream("./Tables_param/Parametres_utilisateurs.xls");
		HSSFWorkbook wb = new HSSFWorkbook(ExcelFileToUpdate);
		HSSFSheet sheet = wb.getSheet("Cycles_de_vie");

		int i = 30;
		int c = 1;
		HSSFCell cell = sheet.getRow(i).getCell(c);
		temp = cell.getNumericCellValue();

		return (float) temp;
	}

	public ExcelEtiquetteService getExcelEtiquetteService() {
		return excelEtiquetteService;
	}

	public void setExcelEtiquetteService(ExcelEtiquetteService excelEtiquetteService) {
		this.excelEtiquetteService = excelEtiquetteService;
	}
	public ExcelXEtiquetteService getExcelXEtiquetteService() {
		return excelXEtiquetteService;
	}

	public void setExcelXEtiquetteService(ExcelXEtiquetteService excelXEtiquetteService) {
		this.excelXEtiquetteService = excelXEtiquetteService;
	}
}
