package com.ed.cgdd.derby.finance.impl;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.ed.cgdd.derby.model.financeObjects.*;
import com.ed.cgdd.derby.model.parc.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ed.cgdd.derby.common.CommonService;
import com.ed.cgdd.derby.finance.CalculCoutService;
import com.ed.cgdd.derby.finance.CoutEnergieService;
import com.ed.cgdd.derby.finance.FinanceService;
import com.ed.cgdd.derby.finance.GesteService;
import com.ed.cgdd.derby.finance.RecupParamFinDAS;
import com.ed.cgdd.derby.finance.TypeFinanceService;
import com.ed.cgdd.derby.model.calcconso.Conso;
import com.ed.cgdd.derby.model.calcconso.ParamBesoinsNeufs;
import com.ed.cgdd.derby.model.calcconso.ParamCoutEclVentil;
import com.ed.cgdd.derby.model.calcconso.ParamCoutEcs;
import com.ed.cgdd.derby.model.calcconso.ParamGainsUsages;
import com.ed.cgdd.derby.model.calcconso.ParamPMConso;
import com.ed.cgdd.derby.model.calcconso.ParamRdtCout;
import com.ed.cgdd.derby.model.calcconso.ParamRdtEcs;
import com.ed.cgdd.derby.model.calcconso.ResultConsoRt;
import com.ed.cgdd.derby.model.calcconso.ResultConsoUClim;
import com.ed.cgdd.derby.model.calcconso.ResultConsoURt;

public class FinanceServiceImpl implements FinanceService {
	private final static Logger LOG = LogManager.getLogger(FinanceServiceImpl.class);

	private static final int START_ID_BRANCHE = 0;
	private static final int LENGTH_ID_BRANCHE = 2;
	private static final int START_ID_SURF = 0;
	private static final int LENGTH_ID_SURF = 6;
	private static final int START_ID_OCCUPANT = 6;
	private static final int LENGTH_ID_OCCUPANT = 2;
	private static final int STRING_FINANCEMENT_LENGTH = 3;
	private static final BigDecimal PM_LIMITE = BigDecimal.valueOf(0.01);
	private static final BigDecimal SURF_LIMITE = BigDecimal.valueOf(10);
	private static final String PROPRIO = "Proprietaire";
	private static final String LOCATAIRE = "Locataire";
	private RecupParamFinDAS recupParamFinDAS;
	private GesteService gesteService;
	private CommonService commonService;
	private TypeFinanceService pretBonifService;
	private TypeFinanceService pbcService;
	private CalculCoutService calculCoutService;
	private CoutEnergieService coutEnergieService;

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

	public GesteService getGesteService() {
		return gesteService;
	}

	public void setGesteService(GesteService gesteService) {
		this.gesteService = gesteService;
	}

	public CommonService getCommonService() {
		return commonService;
	}

	public void setCommonService(CommonService commonService) {
		this.commonService = commonService;
	}

	public TypeFinanceService getPretBonifService() {
		return pretBonifService;
	}

	public void setPretBonifService(TypeFinanceService pretBonifService) {
		this.pretBonifService = pretBonifService;
	}

	public TypeFinanceService getPbcService() {
		return pbcService;
	}

	public void setPbcService(TypeFinanceService pbcService) {
		this.pbcService = pbcService;
	}

	public CalculCoutService getCalculCoutService() {
		return calculCoutService;
	}

	public void setCalculCoutService(CalculCoutService calculCoutService) {
		this.calculCoutService = calculCoutService;
	}

	@Override
	public HashMap<String, PartMarcheRenov> renovationSegmentGlobal(HashMap<String, BigDecimal> decretMemory,
			HashMap<String, ResultConsoUClim> resultConsoUClimMap, HashMap<String, ResultConsoURt> resultConsoURtMap,
			List<Financement> listFin, CEE subCEE, HashMap<String, BigDecimal> dvChauffMap,
			HashMap<TypeRenovBati, BigDecimal> dvGesteMap, HashMap<String, ParamRdtCout> paramRdtCout,
			HashMap<String, Parc> parcTotMap, ResultConsoRt resultConsoRt, int annee, int anneeNTab,
			List<CalibCoutGlobal> coutIntangible, List<CalibCoutGlobal> coutIntangibleBati, ParamCintObjects paramCintObjects,
			float txRenovBati, String idAggreg, BibliGeste bibliGeste, HashMap<Integer, CoutEnergie> coutEnergieMap,
			HashMap<String, Emissions> emissionsMap, Reglementations reglementations, BigDecimal compteur,
			HashMap<String, ParamCoutEclVentil> coutsEclVentilMap, HashMap<String, ParamCoutEcs> coutEcsMap,
			HashMap<String, ParamPMConso> pmEcsNeufMap, HashMap<String, ParamBesoinsNeufs> bNeufsMap,
			HashMap<String, ParamGainsUsages> gainsVentilationMap, HashMap<String, ParamRdtEcs> bibliRdtEcsMap,
			HashMap<String, BigDecimal> evolCoutBati, HashMap<String, BigDecimal> evolCoutTechno,
			HashMap<String, TauxInteret> tauxInteretMap, HashMap<String, SurfMoy> surfMoyMap,
			HashMap<String, EvolValeurVerte> evolVVMap, HashMap<String, RepartStatutOccup> repartStatutOccupMap,
			HashMap<String, Maintenance> maintenanceMap) {

		int periode = commonService.correspPeriode(annee);
		// recuperation des id
		String idBranche = idAggreg.substring(START_ID_BRANCHE, START_ID_BRANCHE + LENGTH_ID_BRANCHE);
		String idOccupant = idAggreg.substring(START_ID_OCCUPANT, START_ID_OCCUPANT + LENGTH_ID_OCCUPANT);
		String idSurf = idAggreg.substring(START_ID_SURF, START_ID_SURF + LENGTH_ID_SURF);
		// Recuperation de la repartition proprietaire/locataire (depend de la
		// branche)
		StatutOccup statutOccup = new StatutOccup();
		statutOccup.setPartProp(repartStatutOccupMap.get(idBranche + PROPRIO).getRepart());
		statutOccup.setPartLoc(repartStatutOccupMap.get(idBranche + LOCATAIRE).getRepart());
		statutOccup.setTauxActuProp(tauxInteretMap.get(idBranche + idOccupant + PROPRIO).getPBC());
		statutOccup.setTauxActuLoc(tauxInteretMap.get(idBranche + idOccupant + LOCATAIRE).getPBC());

		// Evolution de la valeur verte
		ValeurVerte valeurVerte = new ValeurVerte();
		valeurVerte.setValeurProp(evolVVMap.get(idBranche + PROPRIO).getEvol());
		valeurVerte.setValeurLoc(evolVVMap.get(idBranche + LOCATAIRE).getEvol());

		// Recuperation de la surface moyenne
		BigDecimal avgSurf = surfMoyMap.get(idSurf).getSurfMoy();

		// LOG.debug("Start renov segment");
		// Calcul des parts de marche pour les segments proprietaires
		HashMap<String, PartMarcheRenov> pmResult = renovationSegment(decretMemory, resultConsoUClimMap,
				resultConsoURtMap, listFin, subCEE, dvChauffMap, dvGesteMap, paramRdtCout, parcTotMap, annee,
				resultConsoRt, anneeNTab, coutIntangible, coutIntangibleBati, paramCintObjects, txRenovBati, avgSurf, statutOccup,
				bibliGeste, periode, coutEnergieMap, emissionsMap, valeurVerte, reglementations, compteur,
				coutsEclVentilMap, coutEcsMap, pmEcsNeufMap, bNeufsMap, gainsVentilationMap, bibliRdtEcsMap,
				evolCoutBati, evolCoutTechno, maintenanceMap);
		// LOG.debug("end renov");

		return pmResult;

	}

	protected HashMap<String, PartMarcheRenov> renovationSegment(HashMap<String, BigDecimal> decretMemory,
			HashMap<String, ResultConsoUClim> resultConsoUClimMap, HashMap<String, ResultConsoURt> resultConsoURtMap,
			List<Financement> listFin, CEE subCEE, HashMap<String, BigDecimal> dvChauffMap,
			HashMap<TypeRenovBati, BigDecimal> dvGesteMap, HashMap<String, ParamRdtCout> paramRdtCout,
			HashMap<String, Parc> parcTotMap, int annee, ResultConsoRt resultConsoRt, int anneeNTab,List<CalibCoutGlobal> coutIntangible, List<CalibCoutGlobal> coutIntangibleBati, ParamCintObjects paramCintObjects,
			float txRenovBati, BigDecimal avgSurf, StatutOccup statutOccup, BibliGeste bibliGeste, int periode,
			HashMap<Integer, CoutEnergie> coutEnergieMap, HashMap<String, Emissions> emissionsMap,
			ValeurVerte valeurVerte, Reglementations reglementations, BigDecimal compteur,
			HashMap<String, ParamCoutEclVentil> coutsEclVentilMap, HashMap<String, ParamCoutEcs> coutEcsMap,
			HashMap<String, ParamPMConso> pmEcsNeufMap, HashMap<String, ParamBesoinsNeufs> bNeufsMap,
			HashMap<String, ParamGainsUsages> gainsVentilationMap, HashMap<String, ParamRdtEcs> bibliRdtEcsMap,
			HashMap<String, BigDecimal> evolCoutBati, HashMap<String, BigDecimal> evolCoutTechno,
			HashMap<String, Maintenance> maintenanceMap) {

		HashMap<String, PartMarcheRenov> partGesteFin = new HashMap<String, PartMarcheRenov>();

		HashMap<String, Conso> consoEner = resultConsoRt.getMap(MapResultsKeys.CONSO_CHAUFF.getLabel());
		HashMap<String, Conso> rdtIniMap = resultConsoRt.getMap(MapResultsKeys.RDT_CHAUFF.getLabel());
		HashMap<String, Conso> besoinIniMap = resultConsoRt.getMap(MapResultsKeys.BESOIN_CHAUFF.getLabel());
		HashMap<String, Conso> ventilationMap = resultConsoRt.getMap(MapResultsKeys.VENTILATION.getLabel());
		HashMap<String, Conso> auxiliairesMap = resultConsoRt.getMap(MapResultsKeys.AUXILIAIRES.getLabel());

		// LOG.debug("# parc : {}", parcTotMap.keySet().size());
		// TODO implementer calcul taux renov en dehors de la boucle des
		// segments use txRenovBati

		BigDecimal[] txRenovBatiVentile = ventileTxRenovBat(dvGesteMap, annee, txRenovBati, reglementations);
		BigDecimal surfaceSegmentTot = BigDecimal.ZERO;

		for (String idParc : parcTotMap.keySet()) {

			// Recuperation de l'objet parc ainsi que des consommations de
			// chauffage et des rendements de chauffage
			// LOG.info("Segment {}", idParc);
			Parc parc = parcTotMap.get(idParc);
			BigDecimal surfN = parc.getAnnee(anneeNTab - 1);
			if (surfN != null && surfN.signum() != 0) {
				surfaceSegmentTot = surfaceSegmentTot.add(surfN, MathContext.DECIMAL32);
			}
			Conso consoIni = consoEner.get(idParc);
			Conso ventil = ventilationMap.get(idParc);
			Conso aux = auxiliairesMap.get(idParc);
			// Si non chauffe, alors on ne fait rien
			// si il est nouveau segment alors on ne le traite pas
			// si les surfaces/conso a l'annee n-1 sont nulles ou egales a 0
			// alors on ne traite pas le segment
			if (consoEner.get(idParc) != null && surfN != null && surfN.signum() != 0 && consoIni != null
					&& consoIni.getAnnee(anneeNTab - 1) != null && consoIni.getAnnee(anneeNTab - 1).signum() != 0) {

				Conso rdtIni = rdtIniMap.get(idParc);
				Conso besoinIni = besoinIniMap.get(idParc);

				// on calcul le besoin unitaire par m2
				BigDecimal besoinIniUnitaire = besoinIni.getAnnee(anneeNTab - 1).divide(parc.getAnnee(anneeNTab - 1),
						MathContext.DECIMAL32);

				CalculPM coutGestes = renovSegment(resultConsoUClimMap, resultConsoURtMap, idParc, avgSurf, listFin,
						subCEE, dvChauffMap, dvGesteMap, paramRdtCout, parc, besoinIniUnitaire, consoIni, rdtIni,
						annee, anneeNTab, coutIntangible, coutIntangibleBati, bibliGeste, statutOccup, paramCintObjects, periode,
						coutEnergieMap, emissionsMap, valeurVerte, reglementations, coutsEclVentilMap, coutEcsMap,
						pmEcsNeufMap, ventil, aux, bNeufsMap, gainsVentilationMap, bibliRdtEcsMap, besoinIni,
						evolCoutBati, evolCoutTechno, maintenanceMap);

				// insertion resultats dans BDD
				calculPM(decretMemory, dvChauffMap, coutGestes, paramCintObjects, partGesteFin, statutOccup, parc, anneeNTab,
						reglementations, annee, compteur, txRenovBatiVentile, surfN);
			}
		}
		// ajout de la reno tendancielle
		partGesteFin = renoTendancielle(partGesteFin, txRenovBatiVentile, surfaceSegmentTot, parcTotMap, anneeNTab);

		return partGesteFin;
	}

	protected BigDecimal[] ventileTxRenovBat(HashMap<TypeRenovBati, BigDecimal> dvGesteMap, int annee,
			float txRenovBati, Reglementations reglementations) {
		BigDecimal[] txRenovBatiVentile = new BigDecimal[5];

		// 0 : taux initial renseigne par l'utilisateur
		txRenovBatiVentile[0] = BigDecimal.valueOf(txRenovBati);
		String exigence = reglementations.getRt().get(commonService.correspPeriodeFin(annee)).getExigence();
		BigDecimal temp1 = BigDecimal.ZERO;
		BigDecimal temp2 = BigDecimal.ZERO;
		BigDecimal temp3 = BigDecimal.ZERO;
		BigDecimal temp4 = BigDecimal.ZERO;
		BigDecimal sumTemp = BigDecimal.ZERO;
		if (exigence.equals("BBC")) {
			temp1 = BigDecimal.ONE.divide(dvGesteMap.get(TypeRenovBati.FENBBC), MathContext.DECIMAL32);
			temp2 = BigDecimal.ONE.divide(dvGesteMap.get(TypeRenovBati.FEN_MURBBC), MathContext.DECIMAL32);
			temp3 = BigDecimal.ONE.divide(dvGesteMap.get(TypeRenovBati.ENSBBC), MathContext.DECIMAL32);
			temp4 = BigDecimal.ONE.divide(dvGesteMap.get(TypeRenovBati.GTB), MathContext.DECIMAL32);
		} else {
			temp1 = BigDecimal.ONE.divide(dvGesteMap.get(TypeRenovBati.FENMOD), MathContext.DECIMAL32);
			temp2 = BigDecimal.ONE.divide(dvGesteMap.get(TypeRenovBati.FEN_MURMOD), MathContext.DECIMAL32);
			temp3 = BigDecimal.ONE.divide(dvGesteMap.get(TypeRenovBati.ENSMOD), MathContext.DECIMAL32);
			temp4 = BigDecimal.ONE.divide(dvGesteMap.get(TypeRenovBati.GTB), MathContext.DECIMAL32);
		}
		sumTemp = temp1.add(temp2).add(temp3).add(temp4);
		// 1 : taux ventile pour FEN
		txRenovBatiVentile[1] = txRenovBatiVentile[0].multiply(temp1.divide(sumTemp, MathContext.DECIMAL32));
		// 2 : taux ventile pour FEN_MUR
		txRenovBatiVentile[2] = txRenovBatiVentile[0].multiply(temp2.divide(sumTemp, MathContext.DECIMAL32));
		// 3 : taux ventile pour ENS
		txRenovBatiVentile[3] = txRenovBatiVentile[0].multiply(temp3.divide(sumTemp, MathContext.DECIMAL32));
		// 4 : taux ventile pour GTB
		txRenovBatiVentile[4] = txRenovBatiVentile[0].multiply(temp4.divide(sumTemp, MathContext.DECIMAL32));

		return txRenovBatiVentile;
	}

	// methode de calcul des PM a partir d'une hashmap de cout et du parametre
	// nu
	public void calculPM(HashMap<String, BigDecimal> decretMemory, HashMap<String, BigDecimal> dvChauffMap,
			CalculPM calculPM, ParamCintObjects paramCintObjects, HashMap<String, PartMarcheRenov> partGesteFin, StatutOccup statutOccup,
			Parc parcInit, int anneeNTab, Reglementations reglementations, int annee, BigDecimal compteur,
			BigDecimal[] tauxRenovBatiVentile, BigDecimal surfParc) {
		// Initialisation des objets de resultats
		CompilResultGeste compilHorsRegl = new CompilResultGeste();
		CompilResultGeste compilOblig = new CompilResultGeste();
		CompilResultGeste compilDecret = new CompilResultGeste();
		// Initialisation
		BigDecimal sommeProprio = calculPM.getSommeCGProp();
		BigDecimal sommeLoc = calculPM.getSommeCGLoc();
		// Initialisation de la periode avec une valeur qui permettra d'eviter
		// une erreur dans la condition
		String periode = Period.PERIODE_BEFORE_1980.getCode();
		int periodeInt = commonService.correspPeriode(annee);
		HashMap<String, CoutFinal> coutTotProprio = calculPM.getCoutFinalPropMap();
		HashMap<String, CoutFinal> coutTotLoc = calculPM.getCoutFinalLocMap();

		// Initialisation des parts surfaciques touchees par une obligation de
		// travaux
		BigDecimal partOblig = reglementations.getOblSurf().get(parcInit.getIdoccupant()).getPartSurf(periodeInt);
		if ((annee == 2014 || annee == 2015) && partOblig.signum() == 0
				&& parcInit.getIdoccupant().equals(Occupant.ETAT.getCode())
				&& reglementations.getOblSurf().get(parcInit.getIdoccupant()).getPartSurf(2).signum() != 0) {
			partOblig = reglementations.getOblSurf().get(parcInit.getIdoccupant()).getPartSurf(2);

		}
		// Initialisation des parametres des reglementations
		PartsMarche partsMarcheModif = new PartsMarche();
		partsMarcheModif.setPartOblig(partOblig);
		partsMarcheModif.setPartDecret(calclPartDecret(decretMemory, parcInit, reglementations, annee, anneeNTab));
		partsMarcheModif.setPartRenouvSys(calcPartRenouvSys(dvChauffMap, parcInit, compteur, annee));

		partsMarcheModif = compareDecretOblig(partsMarcheModif);

		// Calcul de la surface du segment a l'annee n+1
		BigDecimal surfInit = getSurfInit(parcInit, anneeNTab);
		periode = parcInit.getIdperiodesimple();

		for (String prop : coutTotProprio.keySet()) {
			CoutFinal etatFinalProp = coutTotProprio.get(prop);
			// Calcul des parts de marche
			// PM = CG^-nu / somme(CG^-nu)
			BigDecimal pmProp = formulePM(paramCintObjects.getGesteBat().getNu(), sommeProprio, etatFinalProp);
			// Si la map des locataires contient le geste mene sur les proprio
			BigDecimal pmLoc = BigDecimal.ZERO;
			CoutFinal etatFinalLoc = null;
			if (coutTotLoc.containsKey(prop)) {
				etatFinalLoc = coutTotLoc.get(prop);
				pmLoc = formulePM(paramCintObjects.getGesteBat().getNu(), sommeLoc, etatFinalLoc);
				coutTotLoc.remove(prop);
			}
			// Calcul de la part de marche agregee
			BigDecimal pmAgreg = calcPMAgreg(statutOccup, pmProp, pmLoc);
			// on ajoute les financements du locataire dans la map de proprio
			Collection<ListeFinanceValeur> financementsPropLoc = concatFinancement(statutOccup, pmLoc, pmProp,
					etatFinalProp, etatFinalLoc, pmAgreg);
			// cas ou une obligation de travaux est couplee avec un decret
			partsMarcheModif = testObliDecret(partsMarcheModif, etatFinalProp);

			// Si le geste est impose par le decret

			if (etatFinalProp.getReglementation().equals(ReglementationName.DECRET.getLabel())) {
				compilDecret = loadResultGesteRegl(partsMarcheModif.getPartDecret(), surfInit, prop, etatFinalProp,
						pmAgreg, financementsPropLoc, compilDecret, ReglementationName.DECRET.getLabel());
			}
			// Si le geste est impose par la reglementation
			if (etatFinalProp.getReglementation().equals(ReglementationName.OBLIG_TRAVAUX.getLabel())) {
				compilOblig = loadResultGesteRegl(partsMarcheModif.getPartOblig(), surfInit, prop, etatFinalProp,
						pmAgreg, financementsPropLoc, compilOblig, ReglementationName.OBLIG_TRAVAUX.getLabel());
			}
			// Si le geste n'est impose par aucune reglementation
			if (etatFinalProp.getReglementation().equals(ReglementationName.AUCUNE.getLabel())) {
				compilHorsRegl = loadResultGesteHorsRegl(compilHorsRegl, periode, surfInit, prop, etatFinalProp,
						pmAgreg, financementsPropLoc, ReglementationName.AUCUNE.getLabel());
			}
		}

		// Si la map des locataires n'est pas vide alors tous les gestes ne sont
		// pas traites...
		if (!coutTotLoc.isEmpty()) {
			LOG.debug("Probleme au niveau des gestes sur les locataires");
		}
		// Actualisation des parts de marches imposees ou non par les
		// reglementations
		partsMarcheModif.setPartRegl(partsMarcheModif.getPartOblig().add(partsMarcheModif.getPartDecret()));
		partsMarcheModif.setPartHorsRegl(BigDecimal.ONE.subtract(partsMarcheModif.getPartRegl()));

		// Ajout des parts de marche des gestes imposes par l'obligation de
		// travaux
		partGesteFin = loadResultFinOblig(partGesteFin, compilOblig, partsMarcheModif, surfInit);
		// Ajout des parts de marche des gestes imposes par le decret
		partGesteFin = loadResultFinDecret(partGesteFin, compilDecret, partsMarcheModif, surfInit);

		BigDecimal sommeSysRegl = compilDecret.getSommeSys().add(compilOblig.getSommeSys());
		// la part de renouvellement des systemes est modifiee en fonction des
		// systemes renouveles par l'obligation de travaux
		partsMarcheModif.setPartRenouvSys(modifPartRenouvSys(sommeSysRegl, partsMarcheModif.getPartRenouvSys()));
		// Ajout des gestes non imposes par des reglementations
		partGesteFin = calcPMHorsRegl(partsMarcheModif, partGesteFin, compilHorsRegl, surfInit);

	}

	private HashMap<String, PartMarcheRenov> renoTendancielle(HashMap<String, PartMarcheRenov> partGesteFin,
			BigDecimal[] tauxRenovBatiVentile, BigDecimal surfaceSegmentTot, HashMap<String, Parc> parcTotMap,
			int anneeNtab) {

		// on veut decomposer la map en element du meme segment
		// creation map id segment id --> list (key de la map PartMarcheRenov
		// pour id)
		// la liste comprend seulement les elements necessaire a la modif pour
		// les parts de marché tendanciel
		Map<String, InfoTendanciel> mapIdSegKey = new HashMap<String, InfoTendanciel>();
		for (String key : partGesteFin.keySet()) {
			if (partGesteFin.get(key).getTypeRenovSys().equals(TypeRenovSysteme.ETAT_INIT)) {
				String temp = partGesteFin.get(key).getId();
				if (mapIdSegKey.containsKey(temp)) {
					mapIdSegKey.get(temp).getIds().add(key);
				} else {
					List<String> listTemp = new ArrayList<String>();
					listTemp.add(key);
					BigDecimal surfTemp = parcTotMap.get(partGesteFin.get(key).getId()).getAnnee(anneeNtab);

					InfoTendanciel infoTemp = new InfoTendanciel(listTemp, surfTemp);
					mapIdSegKey.put(temp, infoTemp);
				}
				switch (partGesteFin.get(key).getTypeRenovBat()) {
				case GTB:
					mapIdSegKey.get(temp).setCompteurGTB(mapIdSegKey.get(temp).getCompteurGTB() + 1);
					mapIdSegKey.get(temp).setGtb(true);
					break;
				case FENMOD:
					mapIdSegKey.get(temp).setCompteurFENMOD(mapIdSegKey.get(temp).getCompteurFENMOD() + 1);
					mapIdSegKey.get(temp).setFenMod(true);
					break;
				case FENBBC:
					mapIdSegKey.get(temp).setCompteurFENBBC(mapIdSegKey.get(temp).getCompteurFENBBC() + 1);
					mapIdSegKey.get(temp).setFenBbc(true);
					break;
				case FEN_MURMOD:
					mapIdSegKey.get(temp).setCompteurFEN_MURMOD(mapIdSegKey.get(temp).getCompteurFEN_MURMOD() + 1);
					mapIdSegKey.get(temp).setFen_murMod(true);
					break;
				case FEN_MURBBC:
					mapIdSegKey.get(temp).setCompteurFEN_MURBBC(mapIdSegKey.get(temp).getCompteurFEN_MURBBC() + 1);
					mapIdSegKey.get(temp).setFen_murBbc(true);
					break;
				case ENSBBC:
					mapIdSegKey.get(temp).setCompteurENSBBC(mapIdSegKey.get(temp).getCompteurENSBBC() + 1);
					mapIdSegKey.get(temp).setEnsBbc(true);
					break;

				case ENSMOD:
					mapIdSegKey.get(temp).setCompteurENSMOD(mapIdSegKey.get(temp).getCompteurENSMOD() + 1);
					mapIdSegKey.get(temp).setEnsMod(true);
					break;
				case ETAT_INIT:
					if (mapIdSegKey.get(temp).isRienFaire())
						LOG.info("trop de rien faire !!!");

					// if
					// (partGesteFin.get(key).getPart().compareTo(tauxRenovBatiVentile[0])
					// >= 0) {
					// la PM de rien Faire est assez grande
					mapIdSegKey.get(temp).setPartRienFaire(
							mapIdSegKey.get(temp).getPartRienFaire()
									.add(partGesteFin.get(key).getPart(), MathContext.DECIMAL32));
					mapIdSegKey.get(temp).setRienFaire(true);
					// }

					break;
				default:
					break;
				}

			}

		}
		BigDecimal surfDisp = BigDecimal.ZERO;
		for (String idCountSurf : mapIdSegKey.keySet()) {
			// calcul de la surface disponible pour la rénovation
			if (renovation(mapIdSegKey.get(idCountSurf))) {
				surfDisp = surfDisp.add(mapIdSegKey.get(idCountSurf).getSurface(), MathContext.DECIMAL32);
			}
		}
		// modif de tauxRenovBati
		BigDecimal multTemp = BigDecimal.ONE;
		if (surfDisp != null && surfDisp.signum() != 0) {
			multTemp = multTemp.add(surfaceSegmentTot.divide(surfDisp, MathContext.DECIMAL32), MathContext.DECIMAL32);
		}
		BigDecimal[] tauxRenovBatiTemp = tauxRenovBatiVentile.clone();
		for (int i = 0; i < tauxRenovBatiTemp.length; i++) {
			tauxRenovBatiTemp[i] = tauxRenovBatiTemp[i].multiply(multTemp, MathContext.DECIMAL32);
		}

		for (String id : mapIdSegKey.keySet()) {
			if (renovation(mapIdSegKey.get(id))) {
				InfoTendanciel temp = mapIdSegKey.get(id);
				if (temp.getPartRienFaire().compareTo(tauxRenovBatiTemp[0]) < 0) {
					BigDecimal temp1 = temp.getPartRienFaire().divide(tauxRenovBatiTemp[0], MathContext.DECIMAL32);
					for (int i = 1; i < tauxRenovBatiTemp.length; i++) {
						tauxRenovBatiTemp[i] = tauxRenovBatiTemp[i].multiply(temp1, MathContext.DECIMAL32);
					}
					tauxRenovBatiTemp[0] = temp.getPartRienFaire();

				}

				// mise en place des regles en fonction du type de gestes
				// existants dans la liste
				// si pas de geste ens, on reporte la réno sur fen_mur
				if (!(temp.isEnsMod() || temp.isEnsBbc())) {
					tauxRenovBatiTemp[2] = tauxRenovBatiTemp[2].add(tauxRenovBatiTemp[3], MathContext.DECIMAL32);
					tauxRenovBatiTemp[3] = BigDecimal.ZERO;
				}
				// si pas de gest fen_mur, on reporte la réno sur fen
				if (!(temp.isFen_murMod() || temp.isFen_murBbc())) {
					tauxRenovBatiTemp[1] = tauxRenovBatiTemp[1].add(tauxRenovBatiTemp[2], MathContext.DECIMAL32);
					tauxRenovBatiTemp[2] = BigDecimal.ZERO;
				}
				// si pas de geste fen, on reporte la réno sur gtb
				if (!(temp.isFenMod() || temp.isFenBbc())) {
					tauxRenovBatiTemp[4] = tauxRenovBatiTemp[4].add(tauxRenovBatiTemp[1], MathContext.DECIMAL32);
					tauxRenovBatiTemp[1] = BigDecimal.ZERO;
				}
				// si pas de gest gtb, on reporte si possible la réno sur un
				// autre geste
				if (!temp.isGtb()) {
					if ((temp.isFenMod() || temp.isFenBbc())) {
						tauxRenovBatiTemp[1] = tauxRenovBatiTemp[1].add(tauxRenovBatiTemp[4], MathContext.DECIMAL32);
						tauxRenovBatiTemp[4] = BigDecimal.ZERO;
					} else if (temp.isFen_murMod() || temp.isFen_murBbc()) {
						tauxRenovBatiTemp[2] = tauxRenovBatiTemp[2].add(tauxRenovBatiTemp[4], MathContext.DECIMAL32);
						tauxRenovBatiTemp[4] = BigDecimal.ZERO;
					} else if (temp.isEnsMod() || temp.isEnsBbc()) {
						tauxRenovBatiTemp[3] = tauxRenovBatiTemp[3].add(tauxRenovBatiTemp[4], MathContext.DECIMAL32);
						tauxRenovBatiTemp[4] = BigDecimal.ZERO;
					}
				}

				// calcul des nouveau pmr
				for (String pmr2 : mapIdSegKey.get(id).getIds()) {
					BigDecimal newPartTemp = BigDecimal.ZERO;

					switch (partGesteFin.get(pmr2).getTypeRenovBat()) {
					case GTB:
						if (temp.isGtb()) {
							newPartTemp = tauxRenovBatiTemp[4].divide(BigDecimal.valueOf(temp.getCompteurGTB()),
									MathContext.DECIMAL32);
						}
						break;
					case FENMOD:
						if (temp.isFenMod()) {
							newPartTemp = tauxRenovBatiTemp[1].divide(BigDecimal.valueOf(temp.getCompteurFENMOD()),
									MathContext.DECIMAL32);
						}
						break;

					case FENBBC:
						if (temp.isFenBbc() && !temp.isFenMod()) {
							newPartTemp = tauxRenovBatiTemp[1].divide(BigDecimal.valueOf(temp.getCompteurFENBBC()),
									MathContext.DECIMAL32);
						}

						break;
					case FEN_MURMOD:
						if (temp.isFen_murMod()) {
							newPartTemp = tauxRenovBatiTemp[2].divide(BigDecimal.valueOf(temp.getCompteurFEN_MURMOD()),
									MathContext.DECIMAL32);
						}

						break;
					case FEN_MURBBC:
						if (temp.isFen_murBbc() && !temp.isFen_murMod()) {
							newPartTemp = tauxRenovBatiTemp[2].divide(BigDecimal.valueOf(temp.getCompteurFEN_MURBBC()),
									MathContext.DECIMAL32);
						}
						break;
					case ENSBBC:
						if (temp.isEnsBbc() && !temp.isEnsMod()) {
							newPartTemp = tauxRenovBatiTemp[3].divide(BigDecimal.valueOf(temp.getCompteurENSBBC()),
									MathContext.DECIMAL32);
						}
						break;

					case ENSMOD:
						if (temp.isEnsMod()) {
							newPartTemp = tauxRenovBatiTemp[3].divide(BigDecimal.valueOf(temp.getCompteurENSMOD()),
									MathContext.DECIMAL32);
						}
						break;
					case ETAT_INIT:
						// on est dans un cas rienFaire = true

						newPartTemp = tauxRenovBatiTemp[0].negate();

						break;
					default:

						break;

					}
					// changement de la valeur dans PartGesteFin
					partGesteFin.get(pmr2).setPart(
							partGesteFin.get(pmr2).getPart().add(newPartTemp, MathContext.DECIMAL32));
				}
			}
			//
		}

		return partGesteFin;
	}

	private boolean renovation(InfoTendanciel infoTendanciel) {
		if (infoTendanciel.isRienFaire()) {
			int temp = infoTendanciel.getCompteurENSBBC() + infoTendanciel.getCompteurENSMOD()
					+ infoTendanciel.getCompteurFEN_MURBBC() + infoTendanciel.getCompteurFEN_MURMOD()
					+ infoTendanciel.getCompteurFENBBC() + infoTendanciel.getCompteurFENMOD()
					+ infoTendanciel.getCompteurGTB();
			if (temp != 0) {
				return true;
			}

		}
		return false;

	}

	protected List<PartMarcheRenov> hashToList(HashMap<String, PartMarcheRenov> hashMap) {
		List<PartMarcheRenov> res = new ArrayList<PartMarcheRenov>();
		for (String key : hashMap.keySet()) {
			res.add(hashMap.get(key));
		}
		return res;
	}

	protected HashMap<String, PartMarcheRenov> calcPMHorsRegl(PartsMarche partsMarcheModif,
			HashMap<String, PartMarcheRenov> partGesteFin, CompilResultGeste compilHorsRegl, BigDecimal surfInit) {
		boolean testSys = false;
		BigDecimal partHorsReglBat = partsMarcheModif.getPartHorsRegl().subtract(partsMarcheModif.getPartRenouvSys());
		BigDecimal sommeBat = BigDecimal.ZERO;
		BigDecimal sommeSys = BigDecimal.ZERO;
		BigDecimal sommeNonChgtSys = BigDecimal.ZERO;
		Boolean testNeRienFaire = false;

		// si la map de resultats est non null, un premier tour de boucle est
		// realise pour enlever les parts de marche trop faibles
		// on garde les gestes de renovation tendancielle
		if (!compilHorsRegl.getResults().isEmpty()) {
			boolean ensBbcTen = false;
			boolean ensModTen = false;
			boolean fen_murBbcTen = false;
			boolean fen_murModTen = false;
			boolean fenBbcTen = false;
			boolean fenModTen = false;
			boolean gtbTen = false;

			Iterator<String> iterator = compilHorsRegl.getResults().keySet().iterator();

			while (iterator.hasNext()) {
				PartMarcheRenov geste = compilHorsRegl.getResults().get(iterator.next());

				if (geste.getTypeRenovSys().equals(TypeRenovSysteme.ETAT_INIT)) {

					// il s'agit donc d'un geste impactant uniquement le bati
					// part Inter = (geste.getPart() /
					// (sommeBat))*partHorsReglBat
					BigDecimal partInter = (geste.getPart().divide(compilHorsRegl.getSommeBat(), MathContext.DECIMAL32))
							.multiply(partHorsReglBat);

					if ((partInter.compareTo(PM_LIMITE) < 0 || (partInter.multiply(surfInit)).compareTo(SURF_LIMITE) < 0)) {
						switch (geste.getTypeRenovBat()) {
						case ENSBBC:
							if (ensBbcTen || ensModTen) {
								iterator.remove();
							} else {
								ensBbcTen = true;
								sommeBat = sommeBat.add(geste.getPart());
							}
							break;
						case ENSMOD:
							if (ensModTen) {
								iterator.remove();
							} else {
								ensModTen = true;
								sommeBat = sommeBat.add(geste.getPart());
							}
							break;
						case FEN_MURBBC:
							if (fen_murBbcTen || fen_murModTen) {
								iterator.remove();
							} else {
								fen_murBbcTen = true;
								sommeBat = sommeBat.add(geste.getPart());
							}
							break;
						case FEN_MURMOD:
							if (fen_murModTen) {
								iterator.remove();
							} else {
								fen_murModTen = true;
								sommeBat = sommeBat.add(geste.getPart());
							}
							break;
						case FENBBC:
							if (fenBbcTen || fenModTen) {
								iterator.remove();
							} else {
								fenBbcTen = true;
								sommeBat = sommeBat.add(geste.getPart());
							}
							break;
						case FENMOD:
							if (fenModTen) {
								iterator.remove();
							} else {
								fenModTen = true;
								sommeBat = sommeBat.add(geste.getPart());
							}
							break;
						case GTB:
							if (gtbTen || fenBbcTen || fenModTen) {
								iterator.remove();
							} else {
								gtbTen = true;
								sommeBat = sommeBat.add(geste.getPart());
							}
							break;
						default:
							iterator.remove();

						}

						// // si le geste n'est pas un geste de renovation
						// // tendanciel et
						// // si la part ne respecte pas les bornes minimales
						// alors
						// // elle est enlevee des resultats
						// iterator.remove();
					} else {
						sommeBat = sommeBat.add(geste.getPart());
						switch (geste.getTypeRenovBat()) {
						case ENSBBC:
						case ENSMOD:
							ensModTen = true;
							break;
						case FEN_MURBBC:
						case FEN_MURMOD:
							fen_murModTen = true;
							break;
						case FENBBC:
						case FENMOD:
							fenModTen = true;
							break;
						case GTB:
							gtbTen = true;
							break;
						case ETAT_INIT:
							testNeRienFaire = true;
						default:
						}
					}
				} else {
					// si il s'agit d'un geste portant sur les systemes
					BigDecimal partInter = (geste.getPart().divide(compilHorsRegl.getSommeSys(), MathContext.DECIMAL32))
							.multiply(partsMarcheModif.getPartRenouvSys());
					if (partInter.compareTo(PM_LIMITE) < 0 || (partInter.multiply(surfInit)).compareTo(SURF_LIMITE) < 0) {
						// si la part ne respecte pas les bornes minimales alors
						// elle est enlevee des resultats
						iterator.remove();
					} else {
						sommeSys = sommeSys.add(geste.getPart());
					}

				}

			}

		}

		sommeNonChgtSys = compilHorsRegl.getSommeSys().subtract(sommeSys);
		if (testNeRienFaire) {
			sommeBat = sommeBat.add(sommeNonChgtSys);
		}
		// si la map est vide
		if (compilHorsRegl.getResults().isEmpty()) {
			if (compilHorsRegl.getCleBat() != null) {
				partGesteFin.put(compilHorsRegl.getCleBat(),
						getMaxPM(compilHorsRegl.getCoutFinalBat(), partHorsReglBat));
			}
			if (compilHorsRegl.getCleSys() != null) {
				partGesteFin.put(compilHorsRegl.getCleSys(),
						getMaxPM(compilHorsRegl.getCoutFinalSys(), partsMarcheModif.getPartRenouvSys()));
			}
		} else {

			// on ajoute le reste des PM uniformement
			PartMarcheRenov partMarcheRenov = new PartMarcheRenov();
			for (String cle2 : compilHorsRegl.getResults().keySet()) {
				partMarcheRenov = compilHorsRegl.getResults().get(cle2);
				if (partMarcheRenov.getTypeRenovSys().equals(TypeRenovSysteme.ETAT_INIT)) {
					if (partMarcheRenov.getTypeRenovBat().equals(TypeRenovBati.ETAT_INIT)) {
						BigDecimal partTemp = partMarcheRenov.getPart().add(sommeNonChgtSys);

						partMarcheRenov.setPart((partTemp.divide(sommeBat, MathContext.DECIMAL32))
								.multiply(partHorsReglBat));
					} else {
						// pm = (pmOrigine/sommeBat)*partHorsReglBat
						partMarcheRenov.setPart((compilHorsRegl.getResults().get(cle2).getPart().divide(sommeBat,
								MathContext.DECIMAL32)).multiply(partHorsReglBat));
					}
					if (partMarcheRenov.getPart().signum() > 0) {
						partGesteFin.put(cle2, partMarcheRenov);
					}
				} else {// pm = (pmOrigine/sommeSys)*partRenouvSys
					testSys = true;
					partMarcheRenov.setPart((compilHorsRegl.getResults().get(cle2).getPart().divide(sommeSys,
							MathContext.DECIMAL32)).multiply(partsMarcheModif.getPartRenouvSys()));
					if (partMarcheRenov.getPart().signum() > 0) {
						partGesteFin.put(cle2, partMarcheRenov);
					}
				}

			}
			// Ajout d'un geste sur le systeme si il n'y en a pas
			if (!testSys && partsMarcheModif.getPartRenouvSys().signum() != 0) {

				partGesteFin.put(compilHorsRegl.getCleSys(),
						getMaxPM(compilHorsRegl.getCoutFinalSys(), partsMarcheModif.getPartRenouvSys()));
			}

		}
		return partGesteFin;
	}

	protected HashMap<String, PartMarcheRenov> loadResultFinDecret(HashMap<String, PartMarcheRenov> partGesteFin,
			CompilResultGeste compilDecret, PartsMarche partsMarcheModif, BigDecimal surfInit) {

		BigDecimal sommeTot = BigDecimal.ZERO;
		if (!compilDecret.getResults().isEmpty()) {
			Iterator<String> iterator = compilDecret.getResults().keySet().iterator();
			while (iterator.hasNext()) {

				PartMarcheRenov geste = compilDecret.getResults().get(iterator.next());
				// il s'agit donc d'un geste impactant uniquement le bati
				BigDecimal partInter = (geste.getPart().divide(compilDecret.getSommeTot(), MathContext.DECIMAL32))
						.multiply(partsMarcheModif.getPartDecret());
				if (partInter.compareTo(PM_LIMITE) < 0 || (partInter.multiply(surfInit)).compareTo(SURF_LIMITE) < 0) {
					// si la part ne respecte pas les bornes minimales alors
					// elle est enlevee des resultats
					iterator.remove();
				} else {
					sommeTot = sommeTot.add(geste.getPart());
				}

			}
		}

		// Ajout des parts de marche des gestes imposes par le decret
		if (compilDecret.getResults().isEmpty() && partsMarcheModif.getPartDecret().signum() != 0) {
			if (compilDecret.isBool()) {
				PartMarcheRenov partMarcheInteg = getMaxPM(compilDecret.getCoutFinalRegl(),
						partsMarcheModif.getPartDecret());
				if (partMarcheInteg.getTypeRenovSys().equals(TypeRenovSysteme.CHGT_SYS)) {
					compilDecret.setSommeSys(partMarcheInteg.getPart());
				}

				partGesteFin.put(compilDecret.getCleRegl(), partMarcheInteg);
			} else {
				// Si un geste a deja ete mene sur le bati alors il faut annuler
				// la part
				// reglementaire
				BigDecimal insert = BigDecimal.ONE;
				if ((partsMarcheModif.getPartHorsRegl().add(partsMarcheModif.getPartDecret()))
						.compareTo(BigDecimal.ONE) < 0) {
					insert = partsMarcheModif.getPartHorsRegl().add(partsMarcheModif.getPartDecret());
				}
				partsMarcheModif.setPartHorsRegl(insert);
			}

		} else if (!compilDecret.getResults().isEmpty()) {
			PartMarcheRenov partMarcheRenovDecret = new PartMarcheRenov();
			// Initialisation de sommeSysDecret
			compilDecret.setSommeSys(BigDecimal.ZERO);
			for (String cleDecret : compilDecret.getResults().keySet()) {

				partMarcheRenovDecret = compilDecret.getResults().get(cleDecret);
				// pm = (pmOrigine/sommeRegl)*PartDecret
				partMarcheRenovDecret.setPart((compilDecret.getResults().get(cleDecret).getPart().divide(sommeTot,
						MathContext.DECIMAL32)).multiply(partsMarcheModif.getPartDecret()));
				compilDecret.setSommeSys(compilDecret.getSommeSys().add(partMarcheRenovDecret.getPart()));
				partGesteFin.put(cleDecret, partMarcheRenovDecret);

			}

		}
		return partGesteFin;
	}

	protected HashMap<String, PartMarcheRenov> loadResultFinOblig(HashMap<String, PartMarcheRenov> partGesteFin,
			CompilResultGeste compilOblig, PartsMarche partsMarcheModif, BigDecimal surfInit) {
		BigDecimal sommeTot = BigDecimal.ZERO;
		if (!compilOblig.getResults().isEmpty()) {
			Iterator<String> iterator = compilOblig.getResults().keySet().iterator();
			while (iterator.hasNext()) {

				PartMarcheRenov geste = compilOblig.getResults().get(iterator.next());

				BigDecimal partInter = (geste.getPart().divide(compilOblig.getSommeTot(), MathContext.DECIMAL32))
						.multiply(partsMarcheModif.getPartOblig());
				if (partInter.compareTo(PM_LIMITE) < 0 || (partInter.multiply(surfInit)).compareTo(SURF_LIMITE) < 0) {
					// si la part ne respecte pas les bornes minimales alors
					// elle est enlevee des resultats
					iterator.remove();
				} else {
					sommeTot = sommeTot.add(geste.getPart());
				}

			}
		}

		if (compilOblig.getResults().isEmpty() && partsMarcheModif.getPartOblig().signum() != 0) {
			if (compilOblig.isBool()) {
				PartMarcheRenov partMarcheInteg = getMaxPM(compilOblig.getCoutFinalRegl(),
						partsMarcheModif.getPartOblig());
				if (partMarcheInteg.getTypeRenovSys().equals(TypeRenovSysteme.CHGT_SYS)) {
					compilOblig.setSommeSys(partMarcheInteg.getPart());
				}

				partGesteFin.put(compilOblig.getCleRegl(), partMarcheInteg);
			} else {

				BigDecimal insert = BigDecimal.ONE;
				if ((partsMarcheModif.getPartHorsRegl().add(partsMarcheModif.getPartRegl())).compareTo(BigDecimal.ONE) < 0) {
					insert = partsMarcheModif.getPartHorsRegl().add(partsMarcheModif.getPartRegl());
				}
				partsMarcheModif.setPartHorsRegl(insert);

			}

		} else if (!compilOblig.getResults().isEmpty()) {
			PartMarcheRenov partMarcheRenovOblig = new PartMarcheRenov();
			// si sommeObli est superieur a la part de l'obligation de travaux
			// alors on garde les parts initiales
			if (sommeTot.compareTo(partsMarcheModif.getPartOblig()) >= 0) {
				partGesteFin.putAll(compilOblig.getResults());
				// partHorsRegl = partHorsRegl-(sommeObli-partObligation)
				if ((partsMarcheModif.getPartHorsRegl().subtract(sommeTot.subtract(partsMarcheModif.getPartOblig())))
						.signum() >= 0) {
					partsMarcheModif.setPartHorsRegl(partsMarcheModif.getPartHorsRegl().subtract(
							sommeTot.subtract(partsMarcheModif.getPartOblig())));
				} else {
					partsMarcheModif.setPartHorsRegl(BigDecimal.ZERO);
				}
			} else {
				// Initialisation de sommeSysObli
				compilOblig.setSommeSys(BigDecimal.ZERO);
				for (String cleOblig : compilOblig.getResults().keySet()) {

					partMarcheRenovOblig = compilOblig.getResults().get(cleOblig);
					// pm = (pmOrigine/sommeRegl)*PartObligation
					partMarcheRenovOblig.setPart((compilOblig.getResults().get(cleOblig).getPart().divide(sommeTot,
							MathContext.DECIMAL32)).multiply(partsMarcheModif.getPartOblig()));
					compilOblig.setSommeSys(compilOblig.getSommeSys().add(partMarcheRenovOblig.getPart()));
					partGesteFin.put(cleOblig, partMarcheRenovOblig);

				}
			}

		}
		return partGesteFin;
	}

	protected CompilResultGeste loadResultGesteHorsRegl(CompilResultGeste compilHorsRegl, String periode,
			BigDecimal surfInit, String prop, CoutFinal etatFinalProp, BigDecimal pmAgreg,
			Collection<ListeFinanceValeur> financementsPropLoc, String reglName) {

		if (etatFinalProp.getReglementation().equals(reglName)) {
			// on retient le geste ne touchant pas les systemes ayant la
			// part de marche la plus importante
			if (etatFinalProp.getTypeRenovSys().equals(TypeRenovSysteme.ETAT_INIT)
					&& pmAgreg.compareTo(compilHorsRegl.getPartMaxBat()) > 0) {
				compilHorsRegl.setPartMaxBat(pmAgreg);
				compilHorsRegl.setCoutFinalBat(etatFinalProp);
				compilHorsRegl.setCleBat(prop);
			} else if (etatFinalProp.getTypeRenovSys().equals(TypeRenovSysteme.CHGT_SYS)
					&& pmAgreg.compareTo(compilHorsRegl.getPartMaxSys()) > 0) {
				compilHorsRegl.setPartMaxSys(pmAgreg);
				compilHorsRegl.setCoutFinalSys(etatFinalProp);
				compilHorsRegl.setCleSys(prop);
			}
			if (resultCreate(pmAgreg, etatFinalProp, surfInit, financementsPropLoc, periode) != null) {
				PartMarcheRenov partMarche = resultCreate(pmAgreg, etatFinalProp, surfInit, financementsPropLoc,
						periode);
				if (etatFinalProp.getTypeRenovSys().equals(TypeRenovSysteme.ETAT_INIT)) {
					compilHorsRegl.setSommeBat(compilHorsRegl.getSommeBat().add(partMarche.getPart(),
							MathContext.DECIMAL32));
				} else {
					compilHorsRegl.setSommeSys(compilHorsRegl.getSommeSys().add(partMarche.getPart(),
							MathContext.DECIMAL32));
				}
				HashMap<String, PartMarcheRenov> tempMap = compilHorsRegl.getResults();
				tempMap.put(prop, partMarche);
				compilHorsRegl.setResults(tempMap);
			}
		}
		return compilHorsRegl;
	}

	protected CompilResultGeste loadResultGesteRegl(BigDecimal partAttendue, BigDecimal surfInit, String prop,
			CoutFinal etatFinalProp, BigDecimal pmAgreg, Collection<ListeFinanceValeur> financementsPropLoc,
			CompilResultGeste compilGeste, String reglName) {
		if (etatFinalProp.getReglementation().equals(reglName)) {
			compilGeste.setBool(true);
			// on retient le geste ayant la part de marche la plus
			// importante
			if (pmAgreg.compareTo(compilGeste.getPartMaxRegl()) > 0) {
				compilGeste.setPartMaxRegl(pmAgreg);
				compilGeste.setCoutFinalRegl(etatFinalProp);
				compilGeste.setCleRegl(prop);
			}
			if (resultCreateRegl(pmAgreg, etatFinalProp, surfInit, financementsPropLoc) != null) {
				PartMarcheRenov partMarche = resultCreateRegl(pmAgreg, etatFinalProp, surfInit, financementsPropLoc);
				compilGeste.setSommeTot(compilGeste.getSommeTot().add(partMarche.getPart(), MathContext.DECIMAL32));
				if (!partMarche.getTypeRenovSys().equals(TypeRenovSysteme.ETAT_INIT)) {
					compilGeste.setSommeSys(compilGeste.getSommeSys().add(partMarche.getPart(), MathContext.DECIMAL32));
				}
				HashMap<String, PartMarcheRenov> tempMap = compilGeste.getResults();
				tempMap.put(prop, partMarche);
				compilGeste.setResults(tempMap);
			}

		}
		return compilGeste;
	}

	protected PartsMarche testObliDecret(PartsMarche partsMarcheModif, CoutFinal etatFinalProp) {
		if (etatFinalProp.getReglementation().equals(ReglementationName.OBLIG_TRAVAUX_DECRET.getLabel())) {
			// Si le geste correspond aux exigences des deux reglementations
			// alors celle touchant la plus grande part du parc est
			// conservee
			if (partsMarcheModif.getPartDecret().compareTo(partsMarcheModif.getPartOblig()) >= 0) {
				etatFinalProp.setReglementation(ReglementationName.DECRET.getLabel());
				partsMarcheModif.setPartOblig(BigDecimal.ZERO);
			} else {
				etatFinalProp.setReglementation(ReglementationName.OBLIG_TRAVAUX.getLabel());
				partsMarcheModif.setPartDecret(BigDecimal.ZERO);
			}
		}
		return partsMarcheModif;
	}

	protected BigDecimal calcPMAgreg(StatutOccup statutOccup, BigDecimal pmProp, BigDecimal pmLoc) {
		// pmAgreg = partLoc*pmLoc+partProprio*pmProp
		return (statutOccup.getPartLoc().multiply(pmLoc)).add(statutOccup.getPartProp().multiply(pmProp));
	}

	protected BigDecimal formulePM(int nu, BigDecimal sommeProprio, CoutFinal etatFinalProp) {
		return etatFinalProp.getCoutGlobal().pow(-nu, MathContext.DECIMAL32)
				.divide(sommeProprio, MathContext.DECIMAL32);
	}

	protected BigDecimal getSurfInit(Parc parcInit, int anneeNTab) {

		BigDecimal surfInit = BigDecimal.ZERO;
		if (parcInit != null && parcInit.getAnnee(anneeNTab) != null) {
			surfInit = parcInit.getAnnee(anneeNTab);

		}
		return surfInit;
	}

	protected PartsMarche compareDecretOblig(PartsMarche partsMarcheModif) {
		if ((partsMarcheModif.getPartOblig().add(partsMarcheModif.getPartDecret())).compareTo(BigDecimal.ONE) > 0) {
			BigDecimal somme = partsMarcheModif.getPartOblig().add(partsMarcheModif.getPartDecret());
			partsMarcheModif.setPartOblig(partsMarcheModif.getPartOblig().divide(somme, MathContext.DECIMAL32));
			partsMarcheModif.setPartDecret(partsMarcheModif.getPartDecret().divide(somme, MathContext.DECIMAL32));
		}
		return partsMarcheModif;
	}

	protected BigDecimal calclPartDecret(HashMap<String, BigDecimal> decretMemory, Parc parcInit,
			Reglementations reglementations, int annee, int anneeNTab) {
		BigDecimal partDecret = BigDecimal.ZERO;
		BigDecimal anneeDebut = reglementations.getDecret().get(parcInit.getIdbranche() + parcInit.getIdoccupant())
				.getDebut();
		// Prise en compte de l'annee de fin dans le calcul du decret
		BigDecimal anneeFin = reglementations.getDecret().get(parcInit.getIdbranche() + parcInit.getIdoccupant())
				.getFin().add(BigDecimal.ONE);
		// Si le decret a deja eu un impact sur ce segment
		String cleMemory = parcInit.getId() + parcInit.getAnneeRenov() + parcInit.getTypeRenovBat()
				+ parcInit.getAnneeRenovSys() + parcInit.getTypeRenovSys();
		if (anneeDebut.compareTo(new BigDecimal(annee)) <= 0 && anneeFin.compareTo(new BigDecimal(annee)) > 0) {
			if (decretMemory.containsKey(cleMemory)) {
				BigDecimal part = BigDecimal.ZERO;
				if (parcInit.getAnnee(anneeNTab).signum() != 0) {
					if (decretMemory.get(cleMemory).compareTo(parcInit.getAnnee(anneeNTab)) > 0) {
						part = BigDecimal.ONE;
					} else {
						part = decretMemory.get(cleMemory).divide(parcInit.getAnnee(anneeNTab), MathContext.DECIMAL32);
					}

				}
				partDecret = part;
			} else {
				// Si aucun geste n'a ete impose par le decret sur ce segment

				BigDecimal part = reglementations.getDecret().get(parcInit.getIdbranche() + parcInit.getIdoccupant())
						.getPartSurf();
				partDecret = part.divide(anneeFin.subtract(anneeDebut), MathContext.DECIMAL32);
				decretMemory.put(cleMemory, partDecret.multiply(parcInit.getAnnee(anneeNTab)));
			}
		}

		return partDecret;
	}

	protected BigDecimal modifPartRenouvSys(BigDecimal sommeSysObli, BigDecimal partRenouvSys) {
		// Recalcule de la part de marche des systemes
		if (sommeSysObli.compareTo(partRenouvSys) >= 0) {
			partRenouvSys = BigDecimal.ZERO;
		} else {
			partRenouvSys = partRenouvSys.subtract(sommeSysObli);
		}
		return partRenouvSys;
	}

	protected BigDecimal calcPartRenouvSys(HashMap<String, BigDecimal> dvChauffMap, Parc parcInit, BigDecimal compteur,
			int annee) {
		BigDecimal compteurSpec = compteur.subtract(BigDecimal.ONE, MathContext.DECIMAL32);
		BigDecimal partRenouv = BigDecimal.ZERO;
		BigDecimal dvSys = dvChauffMap.get(parcInit.getIdsyschaud());
		// Si le segment est construit avant 2009
		if (parcInit.getAnneeRenovSys().equals("Etat initial")) {
			// Recuperation de la duree de vie du systeme initial

			if ((dvSys.subtract(compteurSpec)).signum() <= 0) {
				partRenouv = BigDecimal.ONE;
			} else {
				// partRenouv = 1/(dvSys-compteur)
				partRenouv = BigDecimal.ONE.divide((dvSys.subtract(compteurSpec)), MathContext.DECIMAL32);
			}
		} else {
			BigDecimal anneeFinVie = new BigDecimal(parcInit.getAnneeRenovSys()).add(dvSys);
			if (anneeFinVie.compareTo(new BigDecimal(annee)) == 0) {
				partRenouv = BigDecimal.ONE;
			}

		}

		return partRenouv;
	}

	protected PartMarcheRenov getMaxPM(CoutFinal coutFinalMax, BigDecimal partAttendue) {
		PartMarcheRenov partMarcheMax = new PartMarcheRenov();
		partMarcheMax.setId(coutFinalMax.getId());
		partMarcheMax.setAnneeRenovBat(coutFinalMax.getAnneeRenovBat());
		partMarcheMax.setTypeRenovBat(coutFinalMax.getTypeRenovBat());
		partMarcheMax.setAnneeRenovSys(coutFinalMax.getAnneeRenovSys());
		partMarcheMax.setTypeRenovSys(coutFinalMax.getTypeRenovSys());
		partMarcheMax.setFinancements(coutFinalMax.getDetailFinancement());
		partMarcheMax.setPart(partAttendue);
		partMarcheMax.setReglementation(coutFinalMax.getReglementation());

		partMarcheMax.setEnergie(coutFinalMax.getEnergieFin());
		partMarcheMax.setGainEnerg(coutFinalMax.getGainEner());
		partMarcheMax.setSysChaud(coutFinalMax.getSysChaud());
		partMarcheMax.setRdt(coutFinalMax.getRdtFin());
		partMarcheMax.setSurfaceUnitaire(coutFinalMax.getSurfaceUnitaire());
		return partMarcheMax;
	}

	protected PartMarcheRenov resultCreate(BigDecimal pmAgreg, CoutFinal etatFinalProp, BigDecimal surfInit,
			Collection<ListeFinanceValeur> financementsPropLoc, String periode) {
		// on garde le geste Ne rien faire par defaut
		// on garde les gestes tendanciels (V2)
		if ((pmAgreg.compareTo(PM_LIMITE) > 0 && pmAgreg.multiply(surfInit).compareTo(SURF_LIMITE) >= 0)
				|| (etatFinalProp.getTypeRenovBat().equals(TypeRenovBati.ETAT_INIT) && etatFinalProp.getTypeRenovSys()
						.equals(TypeRenovSysteme.ETAT_INIT))
				|| ((etatFinalProp.getTypeRenovSys().equals(TypeRenovSysteme.ETAT_INIT) && (pmAgreg
						.compareTo(new BigDecimal("0.0001")) >= 0 && surfInit.multiply(new BigDecimal("0.01"))
						.compareTo(SURF_LIMITE) >= 0))))
		// (

		// || (periode.equals(Period.PERIODE_2010_2015.getCode())
		// || periode.equals(Period.PERIODE_2016_2020.getCode())
		// || periode.equals(Period.PERIODE_2021_2030.getCode()) || periode
		// .equals(Period.PERIODE_2031_2040.getCode())
		// && periode.equals(Period.PERIODE_2041_2050.getCode()))
		{

			PartMarcheRenov partMarche = new PartMarcheRenov();
			partMarche.setId(etatFinalProp.getId());
			partMarche.setAnneeRenovBat(etatFinalProp.getAnneeRenovBat());
			partMarche.setTypeRenovBat(etatFinalProp.getTypeRenovBat());
			partMarche.setAnneeRenovSys(etatFinalProp.getAnneeRenovSys());
			partMarche.setTypeRenovSys(etatFinalProp.getTypeRenovSys());
			partMarche.setFinancements(financementsPropLoc);
			partMarche.setPart(pmAgreg);
			partMarche.setReglementation(etatFinalProp.getReglementation());
			partMarche.setEnergie(etatFinalProp.getEnergieFin());
			partMarche.setGainEnerg(etatFinalProp.getGainEner());
			partMarche.setSysChaud(etatFinalProp.getSysChaud());
			partMarche.setRdt(etatFinalProp.getRdtFin());
			partMarche.setSurfaceUnitaire(etatFinalProp.getSurfaceUnitaire());

			return partMarche;
		}
		return null;
	}

	protected PartMarcheRenov resultCreateRegl(BigDecimal pmAgreg, CoutFinal etatFinalProp, BigDecimal surfInit,
			Collection<ListeFinanceValeur> financementsPropLoc) {

		if ((pmAgreg).compareTo(PM_LIMITE) > 0 && (pmAgreg).multiply(surfInit).compareTo(SURF_LIMITE) >= 0) {

			PartMarcheRenov partMarche = new PartMarcheRenov();
			partMarche.setId(etatFinalProp.getId());
			partMarche.setAnneeRenovBat(etatFinalProp.getAnneeRenovBat());
			partMarche.setTypeRenovBat(etatFinalProp.getTypeRenovBat());
			partMarche.setAnneeRenovSys(etatFinalProp.getAnneeRenovSys());
			partMarche.setTypeRenovSys(etatFinalProp.getTypeRenovSys());
			partMarche.setFinancements(financementsPropLoc);
			partMarche.setPart(pmAgreg);
			partMarche.setReglementation(etatFinalProp.getReglementation());
			partMarche.setEnergie(etatFinalProp.getEnergieFin());
			partMarche.setGainEnerg(etatFinalProp.getGainEner());
			partMarche.setSysChaud(etatFinalProp.getSysChaud());
			partMarche.setRdt(etatFinalProp.getRdtFin());
			partMarche.setSurfaceUnitaire(etatFinalProp.getSurfaceUnitaire());

			return partMarche;
		}
		return null;
	}

	protected Collection<ListeFinanceValeur> concatFinancement(StatutOccup statutOccup, BigDecimal pmLoc,
			BigDecimal pmProp, CoutFinal etatFinalProp, CoutFinal etatFinalLoc, BigDecimal pmAgreg) {

		HashMap<String, ListeFinanceValeur> mapElement = new HashMap<String, ListeFinanceValeur>();
		for (ListeFinanceValeur element : etatFinalProp.getDetailFinancement()) {
			mapElement.put(
					element.getFinance().getType().toString(),
					new ListeFinanceValeur(element.getFinance(), element.getValeur()
							.multiply(statutOccup.getPartProp(), MathContext.DECIMAL32)
							.multiply(pmProp, MathContext.DECIMAL32).divide(pmAgreg, MathContext.DECIMAL32)));
		}
		if (etatFinalLoc != null && pmLoc.compareTo(BigDecimal.ZERO) != 0) {
			for (ListeFinanceValeur elementLoc : etatFinalLoc.getDetailFinancement()) {

				if (mapElement.containsKey(elementLoc.getFinance().getType().toString())) {
					mapElement.get(elementLoc.getFinance().getType().toString()).setValeur(
							mapElement
									.get(elementLoc.getFinance().getType().toString())
									.getValeur()
									.add(elementLoc.getValeur().multiply(pmLoc, MathContext.DECIMAL32)
											.multiply(statutOccup.getPartLoc(), MathContext.DECIMAL32)
											.divide(pmAgreg, MathContext.DECIMAL32)));
				} else {
					mapElement.put(
							elementLoc.getFinance().getType().toString(),
							new ListeFinanceValeur(elementLoc.getFinance(), elementLoc.getValeur()
									.multiply(statutOccup.getPartLoc(), MathContext.DECIMAL32)
									.multiply(pmLoc, MathContext.DECIMAL32).divide(pmAgreg, MathContext.DECIMAL32)));
				}

			}
		}

		return mapElement.values();
	}

	protected String getIdInitial(String idParcGesteFin) {

		return idParcGesteFin.substring(0, idParcGesteFin.indexOf("|"));

	}

	String decomposeFinance(String str) {
		if (str.subSequence(str.length() - STRING_FINANCEMENT_LENGTH, str.length()).equals("PBC")) {
			return "PBC";
		} else {
			return "PretBonif";
		}

	}

	// cette methode renvoie pour le segment une liste de combinaisons geste
	// financement avec la quantite d'argent dans chaque financement
	protected CalculPM renovSegment(HashMap<String, ResultConsoUClim> resultConsoUClimMap,
			HashMap<String, ResultConsoURt> resultConsoURtMap, String idParc, BigDecimal surface,
			List<Financement> listFin, CEE subCEE, HashMap<String, BigDecimal> dvChauffMap,
			HashMap<TypeRenovBati, BigDecimal> dvGesteMap, HashMap<String, ParamRdtCout> paramRdtCout, Parc parcIni,
			BigDecimal besoinInitUnitaire, Conso consoEner, Conso rdtIni, int annee, int anneeNTab,
			List<CalibCoutGlobal> coutIntangible, List<CalibCoutGlobal> coutIntangibleBati,
			BibliGeste bibliGeste, StatutOccup statutOccup, ParamCintObjects paramCintObjects, int periode,
			HashMap<Integer, CoutEnergie> coutEnergieMap, HashMap<String, Emissions> emissionsMap,
			ValeurVerte valeurVerte, Reglementations reglementations,
			HashMap<String, ParamCoutEclVentil> coutsEclVentilMap, HashMap<String, ParamCoutEcs> coutEcsMap,
			HashMap<String, ParamPMConso> pmEcsNeufMap, Conso ventil, Conso aux,
			HashMap<String, ParamBesoinsNeufs> bNeufsMap, HashMap<String, ParamGainsUsages> gainsVentilationMap,
			HashMap<String, ParamRdtEcs> bibliRdtEcsMap, Conso besoinInit, HashMap<String, BigDecimal> evolCoutBati,
			HashMap<String, BigDecimal> evolCoutTechno, HashMap<String, Maintenance> maintenanceMap) {

		HashMap<String, CoutFinal> coutFinalMapProp = new HashMap<String, CoutFinal>();
		HashMap<String, CoutFinal> coutFinalMapLoc = new HashMap<String, CoutFinal>();

		// Generation d'une liste de geste apres trie des gestes pouvant etre
		// effectivement menes a l'annee N

		List<Geste> gestesPossibles = gesteService.cleanningGeste(coutEnergieMap, resultConsoUClimMap,
				resultConsoURtMap, parcIni, bibliGeste, dvChauffMap, dvGesteMap, annee, periode, rdtIni, anneeNTab,
				reglementations, coutsEclVentilMap, coutEcsMap, pmEcsNeufMap, consoEner, ventil, aux, bNeufsMap,
				besoinInit, gainsVentilationMap, bibliRdtEcsMap, statutOccup.getTauxActuProp(), evolCoutBati,
				evolCoutTechno, maintenanceMap);
		
		
		
		// on sort une copie du pret bancaire classique
		PBC pretDeBase = (PBC) getFinancementByType(listFin, FinancementType.PBC);

		// Initialisation de l'objet de somme du cout global
		BigDecimal sommeProp = BigDecimal.ZERO;
		BigDecimal sommeLoc = BigDecimal.ZERO;

		// on construit les GestesFinancement
		BigDecimal coutEnergie = coutEnergieService.coutEnergie(coutEnergieMap, emissionsMap, annee,
				parcIni.getIdenergchauff(), Usage.CHAUFFAGE.getLabel(), BigDecimal.ONE);

		GesteFinancement inter;
		CoutFinal coutFinalProp;
		CoutFinal coutFinalLoc;
		for (Geste courant : gestesPossibles) {
			// on evacue le geste Ne rien faire qui accepte tous les
			// financements
			if (courant.getGesteNom().equals("Etat initialAUCUNE")) {
				inter = getFinanceService(pretDeBase).createRienFaire(parcIni, consoEner, courant, anneeNTab,
						pretDeBase, surface, coutEnergie);
				coutFinalProp = calculCoutService.calculCoutFinal(surface, besoinInitUnitaire, parcIni, inter, annee,
						idParc, anneeNTab, statutOccup.getTauxActuProp(), coutEnergieMap, emissionsMap,
						valeurVerte.getValeurProp());
				coutFinalLoc = calculCoutService.calculCoutFinal(surface, besoinInitUnitaire, parcIni, inter, annee,
						idParc, anneeNTab, statutOccup.getTauxActuLoc(), coutEnergieMap, emissionsMap,
						valeurVerte.getValeurLoc());

				coutFinalMapProp.put(calculCoutService.outputName(idParc, inter, annee, coutFinalProp), coutFinalProp);
				coutFinalMapLoc.put(calculCoutService.outputName(idParc, inter, annee, coutFinalLoc), coutFinalLoc);

				sommeProp = sommeProp.add(coutFinalProp.getCoutGlobal().pow(-paramCintObjects.getGesteBat().getNu(), MathContext.DECIMAL32));
				sommeLoc = sommeLoc.add(coutFinalLoc.getCoutGlobal().pow(-paramCintObjects.getGesteBat().getNu(), MathContext.DECIMAL32));
			} else {
				for (Financement financement : listFin) {
					inter = getFinanceService(financement).createFinancement(parcIni, consoEner, courant, financement,
							anneeNTab, annee, pretDeBase, subCEE, surface, coutIntangible, coutIntangibleBati,
							coutEnergie, evolCoutBati, evolCoutTechno);

					if (inter != null) {
						// if
						// (courant.getTypeRenovBati().equals(TypeRenovBati.ENSBBC))
						// {
						// LOG.debug(
						// "Rapport CINT/CT {}",
						// inter.getCoutRenov()
						// .getCINT()
						// .divide(inter.getCoutRenov().getCT().add(inter.getCoutRenov().getCTA()),
						// MathContext.DECIMAL32));
						// }
						coutFinalProp = calculCoutService.calculCoutFinal(surface, besoinInitUnitaire, parcIni, inter,
								annee, idParc, anneeNTab, statutOccup.getTauxActuProp(), coutEnergieMap, emissionsMap,
								valeurVerte.getValeurProp());
						coutFinalLoc = calculCoutService.calculCoutFinal(surface, besoinInitUnitaire, parcIni, inter,
								annee, idParc, anneeNTab, statutOccup.getTauxActuLoc(), coutEnergieMap, emissionsMap,
								valeurVerte.getValeurLoc());
						coutFinalMapProp.put(calculCoutService.outputName(idParc, inter, annee, coutFinalProp),
								coutFinalProp);
						coutFinalMapLoc.put(calculCoutService.outputName(idParc, inter, annee, coutFinalLoc),
								coutFinalLoc);

						sommeProp = sommeProp.add(coutFinalProp.getCoutGlobal().pow(-paramCintObjects.getGesteBat().getNu(), MathContext.DECIMAL32));
						sommeLoc = sommeLoc.add(coutFinalLoc.getCoutGlobal().pow(-paramCintObjects.getGesteBat().getNu(), MathContext.DECIMAL32));
					}
				}
			}

		}

		CalculPM calculPM = new CalculPM();
		calculPM.setSommeCGProp(sommeProp);
		calculPM.setCoutFinalPropMap(coutFinalMapProp);
		calculPM.setSommeCGLoc(sommeLoc);
		calculPM.setCoutFinalLocMap(coutFinalMapLoc);

		return calculPM;
	}

	protected void energieGeste(Parc parcIni, List<Geste> gestesPossibles) {
		for (Geste geste : gestesPossibles) {
			geste.setEnergie(parcIni.getIdenergchauff());
		}
	}

	// Methode pour avoir tous les financements "valides" (on enleve les CEE)
	public List<CEE> cleanListeFinancement(List<Financement> listeFin) {
		List<CEE> result = new ArrayList<CEE>();

		Iterator<Financement> iterator = listeFin.iterator();
		Financement financement;
		while (iterator.hasNext()) {
			financement = iterator.next();
			if (financement.getType().equals(FinancementType.CEE)) {
				result.add((CEE) financement);
				iterator.remove();
			}
		}

		return result;
	}

	Financement getFinancementByType(List<Financement> list, FinancementType type) {
		for (Financement financement : list) {
			if (financement.getType().equals(type)) {
				return financement;
			}
		}
		LOG.error("Pas de financement de type: {}", type);
		// Bad...
		throw new NullPointerException("Pas de financement de type" + type);
	}

	public TypeFinanceService getFinanceService(Financement financement) {
		switch (financement.getType()) {

		case PretBonif:
			return pretBonifService;

		default:
			return pbcService;
		}
	}

	private String getIdBranche(String idAgreg) {

		return idAgreg.substring(START_ID_BRANCHE, START_ID_BRANCHE + LENGTH_ID_BRANCHE);
	}

	public void extractionResult(HashMap<ResFin, ValeurFinancement> resultFinance, String idAgregParc,
			PartMarcheRenov temp, HashMap<String, Parc> parcTotMap, int anneeNTab, int annee) {

		if (temp.getAnneeRenovBat().equals(String.valueOf(annee))
				|| temp.getAnneeRenovSys().equals(String.valueOf(annee))) {
			// pour avoir les surfaces du parc qui vont etre modifiees
			Parc parcCopy = new Parc(parcTotMap.get(temp.getId()));
			BigDecimal surfaceMod = calcSurfModif(anneeNTab, parcCopy, temp);

			ResultatsFinancements resTemp = new ResultatsFinancements();
			resTemp.setBranche(getIdBranche(temp.getId()));
			String reglementation = ReglementationName.AUCUNE.getLabel();
			if (temp.getReglementation() != null) {
				reglementation = temp.getReglementation();
			}
			resTemp.setReglementation(reglementation);
			if (temp.getAnneeRenovBat().equals(Integer.toString(annee))) {
				resTemp.setTypeRenovBati(temp.getTypeRenovBat());
				resTemp.setAnneeRenovBat(temp.getAnneeRenovBat());
			} else {
				// Il n'y a pas eu de changement : on met ETAT_INIT, meme si
				// ce n'est pas l'etat du segment
				resTemp.setTypeRenovBati(TypeRenovBati.ETAT_INIT);
				resTemp.setAnneeRenovBat("NP");
			}
			if (temp.getAnneeRenovSys().equals(Integer.toString(annee))) {
				resTemp.setTypeRenovSys(temp.getTypeRenovSys());
				resTemp.setAnneeRenovSys(temp.getAnneeRenovSys());
				resTemp.setSysChaud(temp.getSysChaud());
			} else {
				resTemp.setTypeRenovSys(TypeRenovSysteme.ETAT_INIT);
				resTemp.setAnneeRenovSys("NP");
				resTemp.setSysChaud("NP");
			}

			resTemp.setSurface(surfaceMod);

			// on parcours l'ensemble des financements
			BigDecimal valeurPBC = BigDecimal.ZERO;
			BigDecimal valeurPretBonif = BigDecimal.ZERO;
			BigDecimal valeurAide = BigDecimal.ZERO;

			for (ListeFinanceValeur listeFin : temp.getFinancements()) {
				BigDecimal valueToAdd = listeFin.getValeur().multiply(surfaceMod, MathContext.DECIMAL32)
						.divide(temp.getSurfaceUnitaire(), MathContext.DECIMAL32);
				switch (listeFin.getFinance().getType()) {
				case PBC:
					valeurPBC = valeurPBC.add(valueToAdd, MathContext.DECIMAL32);
					break;
				case PretBonif:
					valeurPretBonif = valeurPretBonif.add(valueToAdd, MathContext.DECIMAL32);
					break;
				case CEE:
					valeurAide = valeurAide.add(valueToAdd, MathContext.DECIMAL32);
					break;
				}
			}

			resTemp.setValeurAides(valeurAide);
			resTemp.setValeurPBC(valeurPBC);
			resTemp.setValeurPretBonif(valeurPretBonif);
			resTemp.setCoutInvestissement(valeurAide.add(valeurPretBonif, MathContext.DECIMAL32).add(valeurPBC,
					MathContext.DECIMAL32));

			// Modification de l'objet de retour
			agregateResFinancement(resultFinance, resTemp, idAgregParc);
		}

	}

	protected BigDecimal calcSurfModif(int anneeNTab, Parc parcInit, PartMarcheRenov geste) {
		BigDecimal surfModif = BigDecimal.ZERO;
		if (parcInit != null && parcInit.getAnnee(anneeNTab) != null && geste != null && geste.getPart() != null) {
			// surfModif = surfN1 * partMarche
			surfModif = parcInit.getAnnee(anneeNTab).multiply(geste.getPart());
		}
		return surfModif;
	}

	// methode d'agregation des valeurs
	public void agregateResFinancement(HashMap<ResFin, ValeurFinancement> resultatsIni, ResultatsFinancements result,
			String idAgregParc) {
		HashMap<ResFin, ValeurFinancement> resultats = resultatsIni;

		ResFin nouveau = new ResFin(result, idAgregParc);
		if (resultats.containsKey(nouveau)) {
			BigDecimal newAides = resultats.get(nouveau).getAides().add(result.getValeurAides(), MathContext.DECIMAL32);
			resultats.get(nouveau).setAides(newAides);

			BigDecimal newCoutInv = resultats.get(nouveau).getCoutInvestissement()
					.add(result.getCoutInvestissement(), MathContext.DECIMAL32);
			resultats.get(nouveau).setCoutInvestissement(newCoutInv);

			BigDecimal newPret = resultats.get(nouveau).getValeurPret()
					.add(result.getValeurPBC(), MathContext.DECIMAL32);
			resultats.get(nouveau).setValeurPret(newPret);

			BigDecimal newPretBonif = resultats.get(nouveau).getValeurPretBonif()
					.add(result.getValeurPretBonif(), MathContext.DECIMAL32);
			resultats.get(nouveau).setValeurPretBonif(newPretBonif);

			BigDecimal newSurface = resultats.get(nouveau).getSurface().add(result.getSurface(), MathContext.DECIMAL32);
			resultats.get(nouveau).setSurface(newSurface);

		} else {
			ValeurFinancement newValFin = new ValeurFinancement(result);
			resultats.put(nouveau, newValFin);
		}

	}

}