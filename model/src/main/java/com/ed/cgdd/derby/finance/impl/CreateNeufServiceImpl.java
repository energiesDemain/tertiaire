package com.ed.cgdd.derby.finance.impl;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.HashMap;
import java.util.List;

import com.ed.cgdd.derby.model.financeObjects.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ed.cgdd.derby.common.CommonService;
import com.ed.cgdd.derby.finance.CoutEnergieService;
import com.ed.cgdd.derby.finance.CreateNeufService;
import com.ed.cgdd.derby.finance.RecupParamFinDAS;
import com.ed.cgdd.derby.model.calcconso.ParamBesoinsNeufs;
import com.ed.cgdd.derby.model.calcconso.ParamRdtCout;
import com.ed.cgdd.derby.model.parc.SysChaud;
import com.ed.cgdd.derby.model.parc.Usage;
import com.ed.cgdd.derby.model.politiques;

public class CreateNeufServiceImpl implements CreateNeufService {
	private final static Logger LOG = LogManager.getLogger(CreateNeufServiceImpl.class);
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
	private static final BigDecimal PM_LIMITE = BigDecimal.valueOf(0.01);
	private CommonService commonService;
	private CoutEnergieService coutEnergieService;
	private RecupParamFinDAS recupParamFinDAS;

	public CommonService getCommonService() {
		return commonService;
	}

	public void setCommonService(CommonService commonService) {
		this.commonService = commonService;
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

	@Override
	public HashMap<String, BigDecimal> pmChauffNeuf(HashMap<String, ParamBesoinsNeufs> bNeufsMap,
			HashMap<String, BigDecimal> dvChauffMap, HashMap<String, ParamRdtCout> rdtCoutChauffMap, String idAgreg,
			int annee, String statut_occup, int nu, HashMap<String,CalibCoutGlobal> coutIntangible,
			HashMap<Integer, CoutEnergie> coutEnergieMap, HashMap<String, Emissions> emissionsMap,
			HashMap<String, BigDecimal> evolCoutBati, HashMap<String, BigDecimal> evolCoutTechno,
			HashMap<String, TauxInteret> tauxInteretMap, HashMap<String, Maintenance> maintenanceMap) {

		int periode = commonService.correspPeriode(annee);
		String idCourant = getIdCourant(idAgreg);

		String idBranche = getIdBranche(idAgreg);
		String idOccupant = getIdOccupant(idAgreg);
		PBC pretDeBase = tauxInteretMap.get(idBranche + idOccupant + statut_occup).getPBC();

		String idBesoin = getIdBesoinChauf(idAgreg);
		// BigDecimal besoinUnitaire = bNeufsMap.get(idBesoin).getPeriode(periode);
		
		
		
		HashMap<String, ParamRdtCout> mapRdtTravail = new HashMap<String, ParamRdtCout>();
		HashMap<String, BigDecimal> coutGlobaux = new HashMap<String, BigDecimal>();

		for (String idParamRdt : rdtCoutChauffMap.keySet()) {
			String paramTestPeriode = rdtCoutChauffMap.get(idParamRdt).getPeriode();
			String temp = idParamRdt.substring(START_ID_COURANT, START_ID_COURANT + ID_COURANT_LENGTH);
			if (temp.equals(idCourant) && paramTestPeriode.equals(String.valueOf(periode))) {
				mapRdtTravail.put(idParamRdt, rdtCoutChauffMap.get(idParamRdt));
				// idParamRdt contient l'idAgreg ainsi que la periode
				//LOG.debug("id={}",  idParamRdt, rdtCoutChauffMap.get(idParamRdt));
			}
			
		}

		for (String str : mapRdtTravail.keySet()) {
			
			String energie = getEnergie(str);
			String idCoutIntangible = getIdCoutInt(str);
			String idSysChaud = getIdSysChauf(str);
			BigDecimal besoinUnitaire = bNeufsMap.get(idBesoin).getPeriode(periode);
				
			BigDecimal rdt = mapRdtTravail.get(str).getRdt();
			BigDecimal cout = mapRdtTravail.get(str).getCout()
					.multiply(getVariation(idSysChaud, annee, evolCoutTechno), MathContext.DECIMAL32);
			
			int dureeVie = dvChauffMap.get(getIdSysChauf(str)).intValueExact();
			
			
			// BV prise en compte d'un surcout pour l'electrique joule du fait de la RT 2012
	
			if(politiques.checkSurcoutRT2012 && annee > 2012 &&
	    			(
	    					
	    			idSysChaud.equals(SysChaud.CASSETTE_RAYONNANTE.getCode()) ||
	    			 idSysChaud.equals(SysChaud.CASSETTE_RAYONNANTE_PERFORMANT.getCode()) ||
	    			 idSysChaud.equals(SysChaud.ELECTRIQUE_DIRECT.getCode()) ||
	    			 idSysChaud.equals(SysChaud.ELECTRIQUE_DIRECT_PERFORMANT.getCode())
	    			)){
				
	    		cout = cout.add(politiques.surcoutRT);	 

			}
			

			BigDecimal coutInt = coutIntangible.get(idCoutIntangible).getCInt();


			// Ajout des couts de maintenance
		    // BV modif Ajout des couts de maintenance en %
			BigDecimal coutMaintenance = cout.multiply(maintenanceMap.get(str.substring(START_ID_SYS, START_ID_SYS + LENGTH_ID_SYS))
								.getPart(), MathContext.DECIMAL32);
			
			// Ce sont des cout annuels fixes. on les ajoute aux cout intangibles
			coutInt = coutInt.add(coutMaintenance);
						
			
			BigDecimal coutEnergie = coutEnergieService.coutEnergie(coutEnergieMap, emissionsMap, annee, energie,
					Usage.CHAUFFAGE.getLabel(), BigDecimal.ONE);
			BigDecimal coutGlobal = coutNeuf(besoinUnitaire, rdt, cout, coutInt, dureeVie, pretDeBase, coutEnergie);
			coutGlobaux.put(str, coutGlobal);
			//LOG.debug("id={} idC={} CINT={} CENER={} COUT={} BESOIN={} RDT={} DV={} CM ={}", str, idCoutIntangible, coutInt, 
			//		coutEnergie, cout,besoinUnitaire, rdt,dureeVie,coutMaintenance);
			//LOG.debug("id={} idC={} CG={}", str,idCoutIntangible, coutGlobal);
		}

		return pmNeuf(coutGlobaux, nu);

	}

	protected BigDecimal getVariation(String type, int annee, HashMap<String, BigDecimal> evolCout) {
		String cle = String.valueOf(annee) + "_" + type;
		return evolCout.get(cle);
	}

	protected String getIdCoutInt(String str) {
		String branche = getIdBranche(str);
		String batType = str.substring(START_BATTYPE, START_BATTYPE + BATTYPE_LENGTH);
		String idSysChauf = getIdSysChauf(str);
		String energie = getEnergie(str);
		String perf;
		if (Integer.valueOf(idSysChauf) > 20) {
			perf = "1";
		} else {
			perf = "0";
		}
		return branche + batType + idSysChauf + energie + perf;

	}

	private String getIdOccupant(String idAgreg) {

		return idAgreg.substring(START_ID_OCCUPANT, START_ID_OCCUPANT + LENGTH_ID_OCCUPANT);
	}

	private String getIdBranche(String idAgreg) {

		return idAgreg.substring(START_ID_BRANCHE, START_ID_BRANCHE + LENGTH_ID_BRANCHE);
	}

	protected String getIdSysChauf(String str) {

		return str.substring(ID_SYSCHAUF_START, ID_SYSCHAUF_START + SYSCHAUF_LENGTH);
	}

	protected String getEnergie(String idParamRdt) {
		return idParamRdt.substring(ID_ENERGIE_START, ID_ENERGIE_START + ENERGIE_LENGTH);
	}

	protected BigDecimal coutNeuf(BigDecimal besoinUnitaire, BigDecimal rdt, BigDecimal cout, BigDecimal coutInt,
			int dureeVie, PBC pretDeBase, BigDecimal coutEnergie) {
		BigDecimal result = cout;
		BigDecimal besoin = besoinUnitaire.divide(rdt, MathContext.DECIMAL32).multiply(coutEnergie,
				MathContext.DECIMAL32);
		BigDecimal tauxInt = BigDecimal.ONE.add(pretDeBase.getTauxInteret(), MathContext.DECIMAL32);
		BigDecimal inverse = BigDecimal.ONE.divide(tauxInt, MathContext.DECIMAL32);
		
		
//		result = result.add(
//				commonService.serieGeometrique(besoin.multiply(inverse, MathContext.DECIMAL32), inverse, dureeVie - 1),
//				MathContext.DECIMAL32);
//		return (result.divide(BigDecimal.valueOf(dureeVie), MathContext.DECIMAL32)).add(coutInt, MathContext.DECIMAL32);
//		
		// version avec investissement actualise
		BigDecimal coefactu = commonService.serieGeometrique(inverse, inverse, dureeVie - 1);
		
		
		
		//LOG.debug("Coef ={}, tint = {}", coefactu, tauxInt);
		
		
		result = result.divide(coefactu,MathContext.DECIMAL32).add(
				besoin,MathContext.DECIMAL32).add(coutInt, MathContext.DECIMAL32);
		return 	result;
		
	}

	protected HashMap<String, BigDecimal> pmNeuf(HashMap<String, BigDecimal> coutNeuf, int nu) {
		HashMap<String, BigDecimal> results = new HashMap<String, BigDecimal>();
		HashMap<String, BigDecimal> resultsFinaux = new HashMap<String, BigDecimal>();
		BigDecimal intermediaire = BigDecimal.ZERO;
		BigDecimal sommeTot = BigDecimal.ZERO;
		for (String str : coutNeuf.keySet()) {
			intermediaire = intermediaire.add(coutNeuf.get(str).pow(-nu, MathContext.DECIMAL32), MathContext.DECIMAL32);
		}
		for (String strBis : coutNeuf.keySet()) {
			BigDecimal val = (coutNeuf.get(strBis).pow(-nu, MathContext.DECIMAL32)).divide(intermediaire,
					MathContext.DECIMAL32);

			// On enleve les parts de marche trop faibles avant de retourner
			// l'objet de resultat
			if (val.compareTo(PM_LIMITE) > 0) {
				sommeTot = sommeTot.add(val, MathContext.DECIMAL32);
				results.put(strBis, new BigDecimal(val.toString()));
			}

		}

		// on ajoute le reste des PM uniformement
		for (String cle2 : results.keySet()) {
			BigDecimal partMarche = results.get(cle2).divide(sommeTot, MathContext.DECIMAL32);
			resultsFinaux.put(cle2, partMarche);
		}

		return resultsFinaux;
	}

	private String getIdBesoinChauf(String idParamRdt) {
		return idParamRdt.substring(START_ID_COURANT, START_ID_COURANT + LENGTH_ID_BRANCHE)
				.concat(idParamRdt.substring(START_BATTYPE, START_BATTYPE + BATTYPE_LENGTH)).concat("Chauffage");
	}

	protected String getIdCourant(String idAgreg) {
		return idAgreg.substring(START_ID_COURANT, START_ID_COURANT + ID_COURANT_LENGTH);
	}

}
