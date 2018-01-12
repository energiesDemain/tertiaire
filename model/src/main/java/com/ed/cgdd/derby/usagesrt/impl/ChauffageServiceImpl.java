package com.ed.cgdd.derby.usagesrt.impl;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ed.cgdd.derby.common.CommonService;
import com.ed.cgdd.derby.finance.FinanceService;
import com.ed.cgdd.derby.model.calcconso.Conso;
import com.ed.cgdd.derby.model.calcconso.EffetRebond;
import com.ed.cgdd.derby.model.calcconso.ParamBesoinsNeufs;
import com.ed.cgdd.derby.model.calcconso.ParamGainsUsages;
import com.ed.cgdd.derby.model.calcconso.ParamRatioAux;
import com.ed.cgdd.derby.model.calcconso.ParamRdtCout;
import com.ed.cgdd.derby.model.calcconso.ResultConsoRt;
import com.ed.cgdd.derby.model.financeObjects.PartMarcheRenov;
import com.ed.cgdd.derby.model.financeObjects.ResFin;
import com.ed.cgdd.derby.model.financeObjects.ValeurFinancement;
import com.ed.cgdd.derby.model.parc.Energies;
import com.ed.cgdd.derby.model.parc.MapResultsKeys;
import com.ed.cgdd.derby.model.parc.Parc;
import com.ed.cgdd.derby.model.parc.TypeRenovBati;
import com.ed.cgdd.derby.model.parc.TypeRenovSysteme;
import com.ed.cgdd.derby.model.parc.Usage;
import com.ed.cgdd.derby.model.parc.EvolBesoinMap;
import com.ed.cgdd.derby.usagesrt.ChauffageService;

public class ChauffageServiceImpl implements ChauffageService {
	private final static Logger LOG = LogManager.getLogger(ChauffageServiceImpl.class);
	private CommonService commonService;
	private FinanceService financeService;
	private final static int START_ID_PARC = 0;
	private final static int LENGTH_ID_PARC = 18;
	private final static int START_ID_RDT = 0;
	private final static int LENGTH_ID_RDT = 6;
	private final static int START_SYS_CHAUD = 12;
	private final static int LENGTH_SYS_CHAUD = 2;
	private final static int START_NRJ = 16;
	private final static int LENGTH_NRJ = 2;
	private static final int LENGTH_ID_BRANCHE = 2;
	private static final int START_ID_BRANCHE = 0;

	public CommonService getCommonService() {
		return commonService;
	}

	public void setCommonService(CommonService commonService) {
		this.commonService = commonService;
	}

	public FinanceService getFinanceService() {
		return financeService;
	}

	public void setFinanceService(FinanceService financeService) {
		this.financeService = financeService;
	}

	// Calcul les evolutions des besoins et des consommations de l'usage de
	// climatisation
	@Override
	public ResultConsoRt evolChauffageConso(HashMap<ResFin, ValeurFinancement> resultFinance, String idAgregParc,
			HashMap<String, ParamRatioAux> auxChaud, HashMap<String, Parc> parcTotMap,
			HashMap<String, PartMarcheRenov> partMarcheMap, HashMap<String, ParamBesoinsNeufs> bNeufsMap,
			HashMap<String, ParamRdtCout> rdtCoutChauffMap, int anneeNTab, int pasdeTemps, int annee,
			ResultConsoRt resultatsConsoRt, HashMap<String, ParamGainsUsages> gainsVentilationMap,
			HashMap<String, EffetRebond> effetRebond, HashMap<String, BigDecimal[]> elasticiteNeufMap,
			HashMap<String, BigDecimal[]> elasticiteExistantMap, EvolBesoinMap evolBesoinMap) {

		// Initialisation des objets pour le chauffage
		HashMap<String, Conso> besoinMapChauff = resultatsConsoRt.getMap(MapResultsKeys.BESOIN_CHAUFF.getLabel());
		HashMap<String, Conso> rdtMapChauff = resultatsConsoRt.getMap(MapResultsKeys.RDT_CHAUFF.getLabel());
		HashMap<String, Conso> consoMapChauff = resultatsConsoRt.getMap(MapResultsKeys.CONSO_CHAUFF.getLabel());
		
		// Initialisation des objets pour la ventilation et les auxiliaires de
		// chauffage
		HashMap<String, Conso> besoinMapVentil = resultatsConsoRt.getMap(MapResultsKeys.VENTILATION.getLabel());
		HashMap<String, Conso> consoAuxChauff = resultatsConsoRt.getMap(MapResultsKeys.AUXILIAIRES.getLabel());

		// Modification du parc, des besoins de chauffage et de ventilation
		// selon les gestes appliques ainsi que des rendements de chauffage.
		// Comptabilise egalement les rehab
		modifParcBesoinGeste(resultFinance, idAgregParc, auxChaud, consoAuxChauff, besoinMapVentil, besoinMapChauff,
				rdtMapChauff, parcTotMap, partMarcheMap, anneeNTab, pasdeTemps, gainsVentilationMap, annee,
				effetRebond, elasticiteNeufMap, elasticiteExistantMap, evolBesoinMap);
		
		// Ajout des besoins et des rendements de chauffage des batiments neufs
		// ainsi que les besoins de ventilation
		resultatsConsoRt = besoinsNeuf(auxChaud, resultatsConsoRt, rdtCoutChauffMap, parcTotMap, bNeufsMap, anneeNTab,
				pasdeTemps, annee, elasticiteNeufMap);

		// Modification des consommations de chauffage
		consoMapChauff = modifConsoChauff(rdtMapChauff, besoinMapChauff, consoMapChauff, anneeNTab, pasdeTemps, annee);
		
//      Commentaire BV pour verifier la prise en comptre de la modif du besoin		
//		HashMap<String, Conso> besoinMapChaufftest = resultatsConsoRt.getMap(MapResultsKeys.BESOIN_CHAUFF.getLabel());
//		for (String key : partMarcheMap.keySet()) {
//			PartMarcheRenov geste = partMarcheMap.get(key);
//
//			// Chargement de la copie de travail du besoin initial
//						Conso besoinInitChauff2 = null;
//						if (besoinMapChauff.containsKey(geste.getId())) {
//							besoinInitChauff2 = new Conso(besoinMapChaufftest.get(geste.getId()));
//
//						}
//
//						
//						LOG.debug("id {} Bi modif {}",besoinInitChauff2.getId(),besoinInitChauff2.getAnnee(anneeNTab - 1));
//		}
//		
		
		return resultatsConsoRt;

	}

	// Calcul des consommations de chauffage
	protected HashMap<String, Conso> modifConsoChauff(HashMap<String, Conso> rdtMapChauff,
			HashMap<String, Conso> besoinMap, HashMap<String, Conso> consoMap, int anneeNTab, int pasdeTemps, int annee) {

		for (String idBesoin : besoinMap.keySet()) {

			Conso besoin = besoinMap.get(idBesoin);
			Conso rdt = rdtMapChauff.get(idBesoin);
			// Si le segment existe deja dans la map des consommations
			if (consoMap.containsKey(idBesoin)) {
				Conso consoExistant = consoExistModif(consoMap, anneeNTab, idBesoin, besoin, rdt);
				consoMap.put(idBesoin, consoExistant);
			} else {

				// Si le segment n'existe pas
				Conso consoNew = consoNewCreate(anneeNTab, pasdeTemps, besoin, rdt);
				consoMap.put(idBesoin, consoNew);
			}

		}

		return consoMap;
	}

	protected Conso consoNewCreate(int anneeNTab, int pasdeTemps, Conso besoin, Conso rdt) {
		Conso consoNew = new Conso(pasdeTemps);
		consoNew.setId(besoin.getId());
		consoNew.setAnneeRenov(besoin.getAnneeRenov());
		consoNew.setAnneeRenovSys(besoin.getAnneeRenovSys());
		consoNew.setTypeRenovBat(besoin.getTypeRenovBat());
		consoNew.setTypeRenovSys(besoin.getTypeRenovSys());
		BigDecimal consoN = BigDecimal.ZERO;
		if (rdt.getAnnee(anneeNTab).compareTo(BigDecimal.ZERO) != 0 && besoin.getAnnee(anneeNTab) != null) {
			// Calcul de la consommation dans le cas ou le besoin est
			// non null et le rendement different de zero
			consoN = besoin.getAnnee(anneeNTab).divide(rdt.getAnnee(anneeNTab), MathContext.DECIMAL32);

		}
		consoNew.setAnnee(anneeNTab, consoN);
		return consoNew;
	}

	protected Conso consoExistModif(HashMap<String, Conso> consoMap, int anneeNTab, String idBesoin, Conso besoin,
			Conso rdt) {
		BigDecimal consoN = BigDecimal.ZERO;
		if (rdt.getAnnee(anneeNTab) != null && besoin.getAnnee(anneeNTab) != null
				&& rdt.getAnnee(anneeNTab).signum() != 0 && besoin.getAnnee(anneeNTab).signum() != 0) {
			// Calcul de la consommation dans le cas ou le besoin est
			// non null et le rendement different de zero
			consoN = besoin.getAnnee(anneeNTab).divide(rdt.getAnnee(anneeNTab), MathContext.DECIMAL32);

		}
		Conso consoExistant = consoMap.get(idBesoin);
		consoExistant.setAnnee(anneeNTab, consoN);
		return consoExistant;
	}

	protected Conso rdtNewCreate(int anneeNTab, String idParc, Parc parcNew, BigDecimal rdt, Conso rdtNewChauff) {
		rdtNewChauff.setId(idParc.substring(START_ID_PARC, START_ID_PARC + LENGTH_ID_PARC));
		rdtNewChauff.setTypeRenovBat(parcNew.getTypeRenovBat());
		rdtNewChauff.setAnneeRenov(parcNew.getAnneeRenov());
		rdtNewChauff.setTypeRenovSys(parcNew.getTypeRenovSys());
		rdtNewChauff.setAnneeRenovSys(parcNew.getAnneeRenovSys());
		rdtNewChauff.setAnnee(anneeNTab, rdt);
		return rdtNewChauff;
	}

	protected Conso rdtNewModif(Conso besoinInitChauff, int anneeNTab, String idBesoin, PartMarcheRenov geste,
			Conso rdtNewChauff) {
		rdtNewChauff.setId(idBesoin.substring(START_ID_PARC, START_ID_PARC + LENGTH_ID_PARC));
		rdtNewChauff.setTypeRenovBat(typeRenovBat(besoinInitChauff.getTypeRenovBat(), geste));
		rdtNewChauff.setAnneeRenov(anneeRenovBat(besoinInitChauff.getAnneeRenov(), geste));
		rdtNewChauff.setTypeRenovSys(typeRenovSys(besoinInitChauff.getTypeRenovSys(), geste));
		rdtNewChauff.setAnneeRenovSys(anneeRenovSys(besoinInitChauff.getAnneeRenovSys(), geste));
		// Nouveau rendement
		BigDecimal rdtNew = geste.getRdt();
		rdtNewChauff.setAnnee(anneeNTab, rdtNew);
		return rdtNewChauff;
	}

	protected Conso rdtExistModif(int anneeNTab, Conso rdtChauff) {
		BigDecimal rdtN1 = BigDecimal.ZERO;
		 
		if (rdtChauff.getAnnee(anneeNTab) != null && rdtChauff.getAnnee(anneeNTab).signum() != 0) {
			rdtN1 = rdtChauff.getAnnee(anneeNTab);
		} else if (rdtChauff.getAnnee(anneeNTab - 1) != null) {
			rdtN1 = rdtChauff.getAnnee(anneeNTab - 1);
		} else {

			LOG.info("rendement égal à 0 !!");
		}
		rdtChauff.setAnnee(anneeNTab, rdtN1);
		return rdtChauff;
	}

	// Genere les Id d'extraction des rdts de la hashMap
	protected String generateRdtCoutChauffId(String keyChauff, int periode) {

		StringBuffer buffer = new StringBuffer();
		buffer.append(keyChauff.substring(START_ID_RDT, START_ID_RDT + LENGTH_ID_RDT));
		buffer.append(keyChauff.substring(START_SYS_CHAUD, START_SYS_CHAUD + LENGTH_SYS_CHAUD));
		buffer.append(keyChauff.substring(START_NRJ, START_NRJ + LENGTH_NRJ));
		buffer.append(periode);

		return buffer.toString();

	}

	// Calcul du besoin de chauffage
	protected ResultConsoRt besoinsNeuf(HashMap<String, ParamRatioAux> auxChaud, ResultConsoRt resultatsConsoRt,
			HashMap<String, ParamRdtCout> rdtCoutChauffMap, HashMap<String, Parc> parcTotMap,
			HashMap<String, ParamBesoinsNeufs> bNeufsMap, int anneeNTab, int pasdeTemps, int annee,
			HashMap<String, BigDecimal[]> elasticiteNeufMap) {
		// Parcours des nouveaux segments de parc
		for (String idParc : parcTotMap.keySet()) {
			// Initialisation des objets
			Parc parcNew = parcTotMap.get(idParc);
			// si le besoin n'existe pas pour cet id et que le parc de l'annee
			// N-1 est null alors il s'agit d'un batiment entrant
			if (resultatsConsoRt.getMap(MapResultsKeys.BESOIN_CHAUFF.getLabel()).get(idParc) == null
					&& parcNew.getAnnee(anneeNTab - 1) == null) {

				batEntrant(resultatsConsoRt.getMap(MapResultsKeys.BESOIN_CHAUFF.getLabel()), bNeufsMap, anneeNTab,
						pasdeTemps, annee, idParc, parcNew, Usage.CHAUFFAGE.getLabel(), elasticiteNeufMap);

				rdtEntrant(resultatsConsoRt.getMap(MapResultsKeys.RDT_CHAUFF.getLabel()), rdtCoutChauffMap, anneeNTab,
						pasdeTemps, annee, idParc, parcNew);

				batEntrant(resultatsConsoRt.getMap(MapResultsKeys.VENTILATION.getLabel()), bNeufsMap, anneeNTab,
						pasdeTemps, annee, idParc, parcNew, Usage.VENTILATION.getLabel(), elasticiteNeufMap);
				calcAuxNeuf(idParc, resultatsConsoRt.getMap(MapResultsKeys.AUXILIAIRES.getLabel()), auxChaud,
						resultatsConsoRt.getMap(MapResultsKeys.BESOIN_CHAUFF.getLabel()), anneeNTab, pasdeTemps);
			}
		}

		return resultatsConsoRt;
	}

	protected void calcAuxNeuf(String idParc, HashMap<String, Conso> auxMap, HashMap<String, ParamRatioAux> auxChaud,
			HashMap<String, Conso> besoinChauffMap, int anneeNTab, int pasdeTemps) {
		if (besoinChauffMap.get(idParc) != null && besoinChauffMap.get(idParc).getAnnee(anneeNTab) != null
				&& besoinChauffMap.get(idParc).getAnnee(anneeNTab).signum() != 0) {
			Conso besoinChauff = besoinChauffMap.get(idParc);
			BigDecimal aux = BigDecimal.ZERO;
			if (auxChaud.get(besoinChauff.getIdsyschaud()).getRatio() != null
					&& auxChaud.get(besoinChauff.getIdsyschaud()).getRatio().signum() != 0) {

				aux = auxChaud.get(besoinChauff.getIdsyschaud()).getRatio();
				Conso besoinNewAux = new Conso(pasdeTemps);
				besoinNewAux.setId(besoinChauff.getId());
				besoinNewAux.setTypeRenovBat(besoinChauff.getTypeRenovBat());
				besoinNewAux.setAnneeRenov(besoinChauff.getAnneeRenov());
				besoinNewAux.setTypeRenovSys(besoinChauff.getTypeRenovSys());
				besoinNewAux.setAnneeRenovSys(besoinChauff.getAnneeRenovSys());
				besoinNewAux.setAnnee(anneeNTab, besoinChauff.getAnnee(anneeNTab).multiply(aux));
				auxMap.put(idParc, besoinNewAux);

			}

		}

	}

	protected void rdtEntrant(HashMap<String, Conso> rdtMapChauff, HashMap<String, ParamRdtCout> rdtCoutChauffMap,
			int anneeNTab, int pasdeTemps, int annee, String idParc, Parc parcNew) {

		BigDecimal rdt = BigDecimal.ZERO;
		int periode = commonService.correspPeriode(annee);
		String idRdt = generateRdtCoutChauffId(idParc, periode);
		if (rdtCoutChauffMap.get(idRdt) != null) {
			ParamRdtCout rdtCoutChauff = rdtCoutChauffMap.get(generateRdtCoutChauffId(idParc, periode));
			rdt = rdtCoutChauff.getRdt();
		}
		Conso rdtNewChauff = new Conso(pasdeTemps);
		rdtNewChauff = rdtNewCreate(anneeNTab, idParc, parcNew, rdt, rdtNewChauff);
		rdtMapChauff.put(idParc, rdtNewChauff);

	}

	protected void batEntrant(HashMap<String, Conso> resultatsMap, HashMap<String, ParamBesoinsNeufs> bNeufsMap,
			int anneeNTab, int pasdeTemps, int annee, String idParc, Parc parcNew, String usage,
			HashMap<String, BigDecimal[]> elasticiteNeufMap) {

		// TODO : verifier que les segments non chauffes ou bien n'ayant
		// pas de geste (hors bat neufs) ne sont pas ecrases.
		String concat = commonService.concatID(parcNew, usage);
		int periodeCstr = commonService.correspPeriodeCstr(parcNew, annee);
		String energie = Energies.ELECTRICITE.getCode();
		if (usage.equals(Usage.CHAUFFAGE.getLabel())) {
			energie = parcNew.getIdenergchauff();
		}
		BigDecimal besoinNeuf = BigDecimal.ZERO;
		if (bNeufsMap.get(concat) != null && bNeufsMap.get(concat).getPeriode(periodeCstr) != null) {
			// le besoinNeuf inclus l'elasticite du besoin par rapport aux prix
			// des energies de 2009
			besoinNeuf = bNeufsMap.get(concat).getPeriode(periodeCstr)
					.multiply(elasticiteNeufMap.get(usage + energie)[annee - 2009], MathContext.DECIMAL32);
		}
		
		
		Conso besoinNew = new Conso(pasdeTemps);
		besoinNew = createNeufConso(anneeNTab, idParc, parcNew, besoinNeuf, besoinNew);
		resultatsMap.put(idParc, besoinNew);

	}

	protected Conso createNeufConso(int anneeNTab, String idParcModif, Parc parcNew, BigDecimal besoinNeuf,
			Conso besoinNew) {
		besoinNew.setId(idParcModif.substring(START_ID_PARC, START_ID_PARC + LENGTH_ID_PARC));
		besoinNew.setTypeRenovBat(parcNew.getTypeRenovBat());
		besoinNew.setAnneeRenov(parcNew.getAnneeRenov());
		besoinNew.setTypeRenovSys(parcNew.getTypeRenovSys());
		besoinNew.setAnneeRenovSys(parcNew.getAnneeRenovSys());
		BigDecimal surfN1 = BigDecimal.ZERO;
		if (parcNew != null && parcNew.getAnnee(anneeNTab) != null) {
			surfN1 = parcNew.getAnnee(anneeNTab);
		}
		BigDecimal besoinN1 = BigDecimal.ZERO;
		if (besoinNeuf != null) {
			besoinN1 = besoinNeuf.multiply(surfN1);
		} else {
			LOG.info("besoinNeuf null");
		}
		besoinNew.setAnnee(anneeNTab, besoinN1);
		return besoinNew;
	}

	protected Conso createNewSegmentBesoin(Conso besoinInit, int anneeNTab, String idParcModif, PartMarcheRenov geste,
			BigDecimal besoinU, Conso besoinNewChauff, BigDecimal surfModif) {
		besoinNewChauff.setId(idParcModif.substring(START_ID_PARC, START_ID_PARC + LENGTH_ID_PARC));
		besoinNewChauff.setTypeRenovBat(typeRenovBat(besoinInit.getTypeRenovBat(), geste));
		besoinNewChauff.setAnneeRenov(anneeRenovBat(besoinInit.getAnneeRenov(), geste));
		besoinNewChauff.setTypeRenovSys(typeRenovSys(besoinInit.getTypeRenovSys(), geste));
		besoinNewChauff.setAnneeRenovSys(anneeRenovSys(besoinInit.getAnneeRenovSys(), geste));
		// Calcul du nouveau besoin de chauffage
		BigDecimal besoinN1 = calcBesoinN1(besoinU, surfModif, anneeNTab);
		besoinNewChauff.setAnnee(anneeNTab, besoinN1);
		return besoinNewChauff;
	}

	protected Conso createSegmentVentil(Conso besoinInitVentil, int anneeNTab, String idParcModif,
			PartMarcheRenov geste, BigDecimal besoinModif, Conso besoinNewVentil, BigDecimal surfModif) {
		if (besoinInitVentil == null) {
			besoinNewVentil.setId(idParcModif.substring(START_ID_PARC, START_ID_PARC + LENGTH_ID_PARC));
			besoinNewVentil.setTypeRenovBat(geste.getTypeRenovBat());
			besoinNewVentil.setAnneeRenov(geste.getAnneeRenovBat());
			besoinNewVentil.setTypeRenovSys(geste.getTypeRenovSys());
			besoinNewVentil.setAnneeRenovSys(geste.getAnneeRenovSys());
		} else {

			besoinNewVentil.setId(idParcModif.substring(START_ID_PARC, START_ID_PARC + LENGTH_ID_PARC));
			besoinNewVentil.setTypeRenovBat(typeRenovBat(besoinInitVentil.getTypeRenovBat(), geste));
			besoinNewVentil.setAnneeRenov(anneeRenovBat(besoinInitVentil.getAnneeRenov(), geste));
			besoinNewVentil.setTypeRenovSys(typeRenovSys(besoinInitVentil.getTypeRenovSys(), geste));
			besoinNewVentil.setAnneeRenovSys(anneeRenovSys(besoinInitVentil.getAnneeRenovSys(), geste));
		}
		// Calcul du nouveau besoin de ventilation
		BigDecimal besoinN1 = calcBesoinN1Ventil(besoinModif, anneeNTab, surfModif);
		besoinNewVentil.setAnnee(anneeNTab, besoinN1);
		return besoinNewVentil;
	}

	protected Conso consoChauffExist(int anneeNTab, Conso besoinChauff, BigDecimal besoinU, BigDecimal surfModif) {

		BigDecimal besoinN1 = BigDecimal.ZERO;
		BigDecimal besoinNew = BigDecimal.ZERO;

		if (surfModif != null) {
			// besoinNew = surfaceN1*besoinU
			besoinNew = surfModif.multiply(besoinU);
		}
		if (besoinChauff.getAnnee(anneeNTab) != null) {
			besoinN1 = besoinNew.add(besoinChauff.getAnnee(anneeNTab));
		} else {
			besoinN1 = besoinNew;
		}
		besoinChauff.setAnnee(anneeNTab, besoinN1);
		return besoinChauff;
	}

	protected Conso besoinVentilExist(PartMarcheRenov geste, BigDecimal besoinUVentil, int anneeNTab,
			Conso besoinVentil, BigDecimal surfModif) {

		BigDecimal besoinN1 = BigDecimal.ZERO;
		BigDecimal besoinCalc = BigDecimal.ZERO;
		if (surfModif != null) {

			besoinCalc = surfModif.multiply(besoinUVentil);
		}

		if (besoinVentil.getAnnee(anneeNTab) != null) {

			besoinN1 = besoinCalc.add(besoinVentil.getAnnee(anneeNTab));

		} else {
			besoinN1 = besoinCalc;
		}
		besoinVentil.setAnnee(anneeNTab, besoinN1);
		return besoinVentil;
	}

	protected BigDecimal calcBesoinN1(BigDecimal besoinU, BigDecimal surfModif, int anneeNTab) {
		BigDecimal besoinN1 = BigDecimal.ZERO;
		if (surfModif.signum() != 0) {
			// besoinN1 = besoinU*surfN1
			besoinN1 = (besoinU.multiply(surfModif));
		}
		return besoinN1;
	}

	protected BigDecimal calcBesoinN1Ventil(BigDecimal besoinU, int anneeNTab, BigDecimal surfModif) {
		BigDecimal besoinN1 = BigDecimal.ZERO;
		if (surfModif != null) {
			// besoinN1 = besoinTransfert*(1-gain)
			besoinN1 = besoinU.multiply(surfModif);
		}
		return besoinN1;
	}

	protected void modifParcBesoinGeste(HashMap<ResFin, ValeurFinancement> resultFinance, String idAgregParc,
			HashMap<String, ParamRatioAux> auxChaud, HashMap<String, Conso> consoAuxChauff,
			HashMap<String, Conso> besoinMapVentil, HashMap<String, Conso> besoinMapChauff,
			HashMap<String, Conso> rdtMapChauff, HashMap<String, Parc> parcTotMap,
			HashMap<String, PartMarcheRenov> partMarcheMap, int anneeNTab, int pasdeTemps,
			HashMap<String, ParamGainsUsages> gainsVentilationMap, int annee, HashMap<String, EffetRebond> effetRebond,
			HashMap<String, BigDecimal[]> elasticiteNeufMap, 
			HashMap<String, BigDecimal[]> elasticiteExistantMap, EvolBesoinMap evolBesoinMap) {

		HashMap<String, Parc> parcTotMapCopy = new HashMap<String, Parc>(parcTotMap);

		BigDecimal valeurRebond = BigDecimal.ZERO;

		// Boucle sur PartMarcheMap
		for (String key : partMarcheMap.keySet()) {

			// Recuperation du geste
			PartMarcheRenov geste = partMarcheMap.get(key);
			// Travail a partir d'une copie de Parc
			Parc parcInit = new Parc(parcTotMapCopy.get(geste.getId()));
			// Chargement du parc initial qui sera modifie
			Parc parcInitModif = new Parc(parcTotMap.get(geste.getId()));

			// Calcul des volumes de rehab
			financeService.extractionResult(resultFinance, idAgregParc, geste, parcTotMapCopy, anneeNTab, annee);

			// Test si renovation ENSBBC, alors effet rebond
			if (!geste.getTypeRenovBat().equals(TypeRenovBati.ETAT_INIT)) {
				valeurRebond = effetRebond.get(parcInit.getIdbranche()).getValeur();
			}

			// Chargement de la copie de travail du besoin initial
			Conso besoinInitChauff = null;
			if (besoinMapChauff.containsKey(geste.getId())) {
				besoinInitChauff = new Conso(besoinMapChauff.get(geste.getId()));

			}
			
			
			// Le besoinU integre le gain, l'effet rebond, l'elasticite prix et l'evolution liee au CC et IFC
			BigDecimal besoinU = calcBesoinUModif(geste, besoinInitChauff, anneeNTab, parcInit, valeurRebond,
					elasticiteExistantMap, evolBesoinMap, annee);
			
			
			// Chargement des ratios de consommation en auxiliaires
			BigDecimal ratioAux = auxChaud.get(geste.getSysChaud()).getRatio();
			// Generation du nouvel idParc
			String newId = generateNewId(parcInit, geste);
			// Insertion du nouvel Id dans l'objet geste
			geste.setNewId(newId);
			// Calcul de la surfModif, surface sortante du segment existant
			BigDecimal surfModif = calcSurfModif(anneeNTab, parcInit, geste);
			
			// Evolution des surfaces chauffees
			parcTotMap = EvolParc(parcTotMap, anneeNTab, pasdeTemps, geste, parcInit, parcInitModif, newId, surfModif);
			// Modification des besoins en chauffage et en auxiliaires
			besoinMapChauff = EvolChauffage(besoinInitChauff, consoAuxChauff, ratioAux, geste, besoinMapChauff,
					anneeNTab, pasdeTemps, newId, besoinU, surfModif);
			// Modification des rendements de chauffage
			rdtMapChauff = EvolRdtsChauff(besoinInitChauff, geste, rdtMapChauff, anneeNTab, pasdeTemps, newId);
			
			// Modification des besoins en ventilation 
			besoinMapVentil = EvolVentil(besoinMapVentil, anneeNTab, pasdeTemps, geste, newId, gainsVentilationMap,
					annee, parcTotMap.get(geste.getId()), parcTotMap.get(geste.getNewId()), surfModif,
					elasticiteExistantMap);

		}

	}

	protected HashMap<String, Conso> EvolRdtsChauff(Conso besoinInitChauff, PartMarcheRenov geste,
			HashMap<String, Conso> rdtMapChauff, int anneeNTab, int pasdeTemps, String newId) {
		if (rdtMapChauff.containsKey(newId)) {
			Conso rdtChauff = new Conso(rdtMapChauff.get(newId));
			rdtChauff = rdtExistModif(anneeNTab, rdtChauff);
			rdtMapChauff.put(newId, rdtChauff);
		} else {
			// Ajout des besoins de chauffage
			Conso rdtChauffNew = new Conso(pasdeTemps);
			rdtChauffNew = rdtNewModif(besoinInitChauff, anneeNTab, newId, geste, rdtChauffNew);
			rdtMapChauff.put(newId, rdtChauffNew);
		}
		return rdtMapChauff;
	}

	protected HashMap<String, Conso> EvolVentil(HashMap<String, Conso> besoinMapVentil, int anneeNTab, int pasdeTemps,
			PartMarcheRenov geste, String newId, HashMap<String, ParamGainsUsages> gainsVentilationMap, int annee,
			Parc parcInit, Parc parcNew, BigDecimal surfModif, HashMap<String, BigDecimal[]> elasticiteExistantMap) {
		// Chargement de la copie de travail du besoin initial de
		// ventilation
		Conso besoinInitVentil = null;
		if (besoinMapVentil.containsKey(geste.getId())) {
			besoinInitVentil = new Conso(besoinMapVentil.get(geste.getId()));
		}

		BigDecimal besoinUVentil = calcBesoinModif(newId, geste, gainsVentilationMap, besoinInitVentil, anneeNTab,
				parcInit, annee, elasticiteExistantMap);
		// Si la nouvelle cle est deja contenue dans la map alors l'objet conso
		// est modifie
		if (besoinMapVentil.containsKey(newId)) {
			Conso besoinExistant = new Conso(besoinMapVentil.get(newId));
			besoinExistant = besoinVentilExist(geste, besoinUVentil, anneeNTab, besoinExistant, surfModif);
			besoinMapVentil.put(newId, besoinExistant);
		} else {
			// Ajout des besoins de ventilation
			Conso besoinNewVentil = new Conso(pasdeTemps);
			besoinNewVentil = createSegmentVentil(besoinInitVentil, anneeNTab, newId, geste, besoinUVentil,
					besoinNewVentil, surfModif);
			besoinMapVentil.put(newId, besoinNewVentil);
		}
		return besoinMapVentil;
	}

	protected BigDecimal calcBesoinModif(String newId, PartMarcheRenov geste,
			HashMap<String, ParamGainsUsages> gainsVentilationMap, Conso besoinInit, int anneeNTab, Parc parcInit,
			int annee, HashMap<String, BigDecimal[]> elasticiteExistantMap) {

		BigDecimal besoinModif = BigDecimal.ZERO;
		BigDecimal gain = calcGainVentil(geste, newId, gainsVentilationMap, besoinInit, annee);

		if (geste.getPart() != null && besoinInit != null && besoinInit.getAnnee(anneeNTab - 1) != null
				&& parcInit != null && parcInit.getAnnee(anneeNTab - 1) != null
				&& parcInit.getAnnee(anneeNTab - 1).signum() != 0) {
			BigDecimal facteurElasticite = elasticiteExistantMap.get(Usage.VENTILATION.getLabel()
					+ Energies.ELECTRICITE.getCode())[annee - 2009];
			BigDecimal besoinU = besoinInit.getAnnee(anneeNTab - 1).divide(parcInit.getAnnee(anneeNTab - 1),
					MathContext.DECIMAL32);
			besoinModif = besoinU.multiply(BigDecimal.ONE.subtract(gain)).multiply(facteurElasticite,
					MathContext.DECIMAL32);

		}

		return besoinModif;
	}

	protected BigDecimal calcGainVentil(PartMarcheRenov geste, String newId,
			HashMap<String, ParamGainsUsages> gainsVentilationMap, Conso besoinSegmentOld, int annee) {
		BigDecimal gain = BigDecimal.ZERO;
		if (geste.getTypeRenovBat().equals(TypeRenovBati.ENSBBC) && besoinSegmentOld != null
				&& besoinSegmentOld.getTypeRenovBat() != null
				&& !besoinSegmentOld.getTypeRenovBat().equals(TypeRenovBati.ENSBBC)) {
			ParamGainsUsages gainsUsages = gainsVentilationMap.get(newId.substring(START_ID_BRANCHE, START_ID_BRANCHE
					+ LENGTH_ID_BRANCHE)
					+ Usage.VENTILATION.getLabel());
			gain = gainsUsages.getPeriode(commonService.correspPeriode(annee));
		}
		return gain;
	}

	protected HashMap<String, Conso> EvolChauffage(Conso besoinInitChauff, HashMap<String, Conso> consoAuxChauff,
			BigDecimal ratioAux, PartMarcheRenov geste, HashMap<String, Conso> besoinMapChauff, int anneeNTab,
			int pasdeTemps, String newId, BigDecimal besoinU, BigDecimal surfModif) {
		if (besoinMapChauff.containsKey(newId)) {
			Conso besoinExistant = new Conso(besoinMapChauff.get(newId));
			besoinExistant = consoChauffExist(anneeNTab, besoinExistant, besoinU, surfModif);
			besoinMapChauff.put(newId, besoinExistant);
		} else {
			// Ajout des besoins de chauffage
			Conso besoinNewChauff = new Conso(pasdeTemps);
			besoinNewChauff = createNewSegmentBesoin(besoinInitChauff, anneeNTab, newId, geste, besoinU,
					besoinNewChauff, surfModif);
			besoinMapChauff.put(newId, besoinNewChauff);
		}
		// Calcul des conso en auxiliaires
		if (ratioAux.signum() != 0) {
			Conso consoAux = new Conso(pasdeTemps);
			Conso besoinExist = besoinMapChauff.get(newId);
			if (consoAuxChauff.containsKey(newId)) {
				consoAux = consoAuxChauff.get(newId);
			} else {
				consoAux.setId(besoinExist.getId());
				consoAux.setAnneeRenov(besoinExist.getAnneeRenov());
				consoAux.setAnneeRenovSys(besoinExist.getAnneeRenovSys());
				consoAux.setTypeRenovBat(besoinExist.getTypeRenovBat());
				consoAux.setTypeRenovSys(besoinExist.getTypeRenovSys());
			}
			consoAux.setAnnee(anneeNTab, besoinExist.getAnnee(anneeNTab).multiply(ratioAux));
			consoAuxChauff.put(newId, consoAux);
		}
		return besoinMapChauff;
	}

	protected HashMap<String, Parc> EvolParc(HashMap<String, Parc> parcTotMap, int anneeNTab, int pasdeTemps,
			PartMarcheRenov geste, Parc parcInit, Parc parcInitModif, String newId, BigDecimal surfModif) {
		// Si au moins un geste de renovation est mene, alors le parc
		// est modifie
		if (!geste.getTypeRenovBat().equals(TypeRenovBati.ETAT_INIT)
				|| !geste.getTypeRenovSys().equals(TypeRenovSysteme.ETAT_INIT)) {
			// Creation d'un nouveau segment ou modification du segment
			// pre-existant
			if (parcTotMap.containsKey(newId)) {
				// si le segment est deja contenu dans la map
				// Modification du parc
				Parc parcExistant = new Parc(parcTotMap.get(newId));
				parcExistant = modifSegmentExistant(anneeNTab, surfModif, parcExistant);
				parcTotMap.put(newId, parcExistant);

			} else {
				// si le segment modifie n'existe pas
				// Ajout du parc
				Parc newParc = new Parc(pasdeTemps);
				newParc = createNewSegmentParc(anneeNTab, parcInit, geste, newId, surfModif, newParc);
				parcTotMap.put(newId, newParc);

			}
			// Modification du segment initial
			parcInitModif = modifSegmentInit(anneeNTab, parcInitModif, surfModif);
			parcTotMap.put(geste.getId(), parcInitModif);
		}
		return parcTotMap;
	}

	protected BigDecimal calcBesoinUModif(PartMarcheRenov geste, Conso besoinInit, int anneeNTab, Parc parcInit,
			BigDecimal valeurRebond, HashMap<String, BigDecimal[]> elasticiteExistantMap,EvolBesoinMap evolBesoinMap, int annee) {

		BigDecimal besoinModif = BigDecimal.ZERO;
		BigDecimal gain = BigDecimal.ONE;
		
		if (geste.getGainEnerg() != null) {
			// gain modifie par effet rebond en pourcentage du gain gagne
			// gain = 1 - geste.Gain*(1-effetRebond)
			gain = BigDecimal.ONE.subtract(geste.getGainEnerg().multiply(
					BigDecimal.ONE.subtract(valeurRebond, MathContext.DECIMAL32), MathContext.DECIMAL32));
		}
		
		BigDecimal facteurElasticite = BigDecimal.ONE;
		BigDecimal besoinU = BigDecimal.ZERO;
		if (besoinInit != null && besoinInit.getAnnee(anneeNTab - 1) != null && parcInit != null
				&& parcInit.getAnnee(anneeNTab - 1) != null && parcInit.getAnnee(anneeNTab - 1).signum() != 0) {
			facteurElasticite = elasticiteExistantMap.get(Usage.CHAUFFAGE.getLabel() + geste.getEnergie())[annee - 2009];
			// besoinU = (besoinInit/parcInit)
			besoinU = (besoinInit.getAnnee(anneeNTab - 1).divide(parcInit.getAnnee(anneeNTab - 1),
					MathContext.DECIMAL32));
			// besoinModif = besoinU*gain*facteurElasticite
			BigDecimal temp = (besoinU.multiply(gain, MathContext.DECIMAL32)).multiply(facteurElasticite,
					MathContext.DECIMAL32);
			besoinModif = (temp);

		}
	
		//On modifie le besoin de chauffage si le besoin evolue (adaptation CC ou IFC)
		BigDecimal evolBesoinChauff = 
				evolBesoinMap.getEvolBesoin()
				.get(parcInit.getIdagreg().substring(START_ID_BRANCHE,LENGTH_ID_BRANCHE)+Usage.CHAUFFAGE+annee)
				.getEvolution();
		besoinModif = besoinModif.multiply(BigDecimal.ONE.add(evolBesoinChauff), MathContext.DECIMAL32);
		
//		// affiche les elements de calcul du besoin modif
//		LOG.debug("id {} Bi {} gain {} elas {} evol {} besoinUi {}  besoinUmod {}  ",
//				besoinInit.getId(),besoinInit.getAnnee(anneeNTab - 1),
//				gain,
//				facteurElasticite, evolBesoinChauff, besoinU, besoinModif);
//		

		return besoinModif;
	}

	protected BigDecimal calcSurfModif(int anneeNTab, Parc parcInit, PartMarcheRenov geste) {
		BigDecimal surfModif = BigDecimal.ZERO;
		if (parcInit != null && parcInit.getAnnee(anneeNTab) != null && geste != null && geste.getPart() != null) {
			// surfModif = surfN1 * partMarche
			surfModif = parcInit.getAnnee(anneeNTab).multiply(geste.getPart());
		}
		return surfModif;
	}

	protected Parc modifSegmentExistant(int anneeNTab, BigDecimal surfModif, Parc parcExistant) {
		parcExistant.setAnnee(anneeNTab, parcExistant.getAnnee(anneeNTab).add(surfModif));
		return parcExistant;
	}

	protected Parc createNewSegmentParc(int anneeNTab, Parc parcInit, PartMarcheRenov geste, String newId,
			BigDecimal surfModif, Parc newParc) {

		newParc.setId(newId.substring(START_ID_PARC, START_ID_PARC + LENGTH_ID_PARC));
		newParc.setAnneeRenov(anneeRenovBat(parcInit.getAnneeRenov(), geste));
		newParc.setAnneeRenovSys(anneeRenovSys(parcInit.getAnneeRenovSys(), geste));
		newParc.setTypeRenovBat(typeRenovBat(parcInit.getTypeRenovBat(), geste));
		newParc.setTypeRenovSys(typeRenovSys(parcInit.getTypeRenovSys(), geste));
		newParc.setAnnee(anneeNTab, surfModif);
		return newParc;
	}

	protected Parc modifSegmentInit(int anneeNTab, Parc parcInitModif, BigDecimal surfModif) {
		if (parcInitModif.getAnnee(anneeNTab).subtract(surfModif).compareTo(BigDecimal.ONE) >= 0) {
			parcInitModif.setAnnee(anneeNTab, parcInitModif.getAnnee(anneeNTab).subtract(surfModif));
		} else {
			parcInitModif.setAnnee(anneeNTab, BigDecimal.ZERO);
		}
		return parcInitModif;
	}

	protected String anneeRenovBat(String anneeInit, PartMarcheRenov partMarcheRenov) {
		String anneeRenov;
		if (partMarcheRenov.getTypeRenovBat().equals(TypeRenovBati.ETAT_INIT)) {

			anneeRenov = anneeInit;

		} else {
			anneeRenov = partMarcheRenov.getAnneeRenovBat();
		}

		return anneeRenov;

	}

	protected TypeRenovSysteme typeRenovSys(TypeRenovSysteme typeRenovInit, PartMarcheRenov partMarcheRenov) {
		TypeRenovSysteme typeRenovSys;
		if (partMarcheRenov.getTypeRenovSys().equals(TypeRenovSysteme.ETAT_INIT)) {

			typeRenovSys = typeRenovInit;

		} else {
			typeRenovSys = partMarcheRenov.getTypeRenovSys();
		}

		return typeRenovSys;

	}

	protected TypeRenovBati typeRenovBat(TypeRenovBati typeRenovBatInit, PartMarcheRenov partMarcheRenov) {
		TypeRenovBati typeRenovBat;
		if (partMarcheRenov.getTypeRenovBat().equals(TypeRenovBati.ETAT_INIT)) {

			typeRenovBat = typeRenovBatInit;

		} else {
			typeRenovBat = partMarcheRenov.getTypeRenovBat();
		}

		return typeRenovBat;

	}

	protected String anneeRenovSys(String anneeRenovSys, PartMarcheRenov partMarcheRenov) {
		String anneeRenov;
		if (partMarcheRenov.getTypeRenovSys().equals(TypeRenovSysteme.ETAT_INIT)) {

			anneeRenov = anneeRenovSys;

		} else {
			anneeRenov = partMarcheRenov.getAnneeRenovSys();
		}

		return anneeRenov;

	}

	protected String generateNewId(Parc parcInit, PartMarcheRenov partMarcheRenov) {

		StringBuffer buffer = new StringBuffer();
		String idAgreg = parcInit.getIdagreg();
		String idSysFroid = parcInit.getIdsysfroid();
		String newSysChaud = parcInit.getIdsyschaud();
		TypeRenovSysteme typeRenovSys = parcInit.getTypeRenovSys();
		String anneeRenovSys = parcInit.getAnneeRenovSys();
		String newEnergie = parcInit.getIdenergchauff();
		TypeRenovBati typeRenovBat = parcInit.getTypeRenovBat();
		String anneeRenovBat = parcInit.getAnneeRenov();
		if (!partMarcheRenov.getTypeRenovSys().equals(TypeRenovSysteme.ETAT_INIT)) {
			newSysChaud = partMarcheRenov.getSysChaud();
			typeRenovSys = partMarcheRenov.getTypeRenovSys();
			anneeRenovSys = partMarcheRenov.getAnneeRenovSys();
			newEnergie = partMarcheRenov.getEnergie();
		}
		if (!partMarcheRenov.getTypeRenovBat().equals(TypeRenovBati.ETAT_INIT)) {
			typeRenovBat = partMarcheRenov.getTypeRenovBat();
			anneeRenovBat = partMarcheRenov.getAnneeRenovBat();
		}

		buffer.append(idAgreg);
		buffer.append(newSysChaud);
		buffer.append(idSysFroid);
		buffer.append(newEnergie);
		buffer.append(anneeRenovBat);
		buffer.append(typeRenovBat);
		buffer.append(anneeRenovSys);
		buffer.append(typeRenovSys);

		return buffer.toString();
	}

}
