package com.ed.cgdd.derby.process.impl;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ed.cgdd.derby.common.CommonService;
import com.ed.cgdd.derby.model.Constants;
import com.ed.cgdd.derby.model.calcconso.Conso;
import com.ed.cgdd.derby.model.calcconso.ParamRatioAux;
import com.ed.cgdd.derby.model.calcconso.ParamRdtCout;
import com.ed.cgdd.derby.model.calcconso.ParamRdtEcs;
import com.ed.cgdd.derby.model.calcconso.ResultConsoRt;
import com.ed.cgdd.derby.model.calcconso.ResultConsoUClim;
import com.ed.cgdd.derby.model.calcconso.ResultConsoURt;
import com.ed.cgdd.derby.model.parc.MapResultsKeys;
import com.ed.cgdd.derby.model.parc.TypeRenovBati;
import com.ed.cgdd.derby.model.parc.TypeRenovSysteme;
import com.ed.cgdd.derby.process.InitializeConsoService;

public class InitializeConsoServiceImpl implements InitializeConsoService {
	private final static Logger LOG = LogManager.getLogger(InitializeConsoServiceImpl.class);
	private static final int START_ENERG_AGREG = 12;
	private static final int LENGHT_ENERG_AGREG = 2;
	private static final int START_SYS_FROID_AGREG = 12;
	private static final int LENGHT_SYS_FROID_AGREG = 2;
	private static final int START_ENERG = 16;
	private static final int LENGHT_ENERG = 2;
	private static final BigDecimal FACTEUR_EP = new BigDecimal("2.58");
	private CommonService commonService;

	public CommonService getCommonService() {
		return commonService;
	}

	public void setCommonService(CommonService commonService) {
		this.commonService = commonService;
	}

	// Methode initialisant la HashMap de rendements pour l'usage ECS
	public HashMap<String, Conso> initializeRdtEcs(HashMap<String, ParamRdtEcs> bibliRdtEcsMap,
			HashMap<String, Conso> besoinsInit, int pasdeTemps) {

		HashMap<String, Conso> rdtEcs = new HashMap<String, Conso>();

		for (String id : besoinsInit.keySet()) {
			Conso rdt = new Conso(pasdeTemps);
			Conso besoinSegment = besoinsInit.get(id);
			ParamRdtEcs rdtSegment = bibliRdtEcsMap.get(besoinSegment.getIdbranche()
					+ besoinSegment.getId().substring(START_ENERG_AGREG, START_ENERG_AGREG + LENGHT_ENERG_AGREG));
			rdt.setId(besoinSegment.getId());
			rdt.setAnneeRenovSys(Constants.INIT_STATE);
			rdt.setTypeRenovSys(TypeRenovSysteme.ETAT_INIT);
			rdt.setAnneeRenov(Constants.INIT_STATE);
			rdt.setTypeRenovBat(TypeRenovBati.ETAT_INIT);
			rdt.setAnnee(0, rdtSegment.getRdt(0));
			rdtEcs.put(id, rdt);
		}

		return rdtEcs;

	}

	@Override
	public ResultConsoRt initializeAuxChaud(ResultConsoRt resultatsConsoRt, HashMap<String, ParamRatioAux> auxChaud,
			int pasdeTemps) {
		HashMap<String, Conso> auxChauffMap = new HashMap<String, Conso>();
		HashMap<String, Conso> chauffMap = resultatsConsoRt.getMap(MapResultsKeys.BESOIN_CHAUFF.getLabel());
		for (String keyChauff : chauffMap.keySet()) {
			Conso besoinChauff = chauffMap.get(keyChauff);
			BigDecimal ratioAuxChaud = auxChaud.get(besoinChauff.getIdsyschaud()).getRatio();
			Conso consoAux = new Conso(pasdeTemps);
			consoAux.setId(besoinChauff.getId());
			consoAux.setAnneeRenov(besoinChauff.getAnneeRenov());
			consoAux.setAnneeRenovSys(besoinChauff.getAnneeRenovSys());
			consoAux.setTypeRenovBat(besoinChauff.getTypeRenovBat());
			consoAux.setTypeRenovSys(besoinChauff.getTypeRenovSys());
			consoAux.setAnnee(0, besoinChauff.getAnnee(0).multiply(ratioAuxChaud));
			auxChauffMap.put(keyChauff, consoAux);
		}
		resultatsConsoRt.put(MapResultsKeys.AUXILIAIRES.getLabel(), auxChauffMap);
		return resultatsConsoRt;
	}

	// Methode initialisant la HashMap de rendements pour l'usage de
	// climatisation
	public HashMap<String, Conso> initializeRdtClim(HashMap<String, ParamRdtCout> rdtCoutClimMap,
			HashMap<String, Conso> besoinsInit, int pasdeTemps) {

		HashMap<String, Conso> rdtClim = new HashMap<String, Conso>();

		for (String id : besoinsInit.keySet()) {
			Conso rdt = new Conso(pasdeTemps);
			Conso besoinSegment = besoinsInit.get(id);
			ParamRdtCout rdtSegment = rdtCoutClimMap.get(getIdConstructorClim(besoinSegment));
			rdt.setId(besoinSegment.getId());
			rdt.setAnneeRenovSys(Constants.INIT_STATE);
			rdt.setTypeRenovSys(TypeRenovSysteme.ETAT_INIT);
			rdt.setAnneeRenov(Constants.INIT_STATE);
			rdt.setTypeRenovBat(TypeRenovBati.ETAT_INIT);

			rdt.setAnnee(0, rdtSegment.getRdt());
			rdtClim.put(id, rdt);
		}

		return rdtClim;

	}

	// Methode initialisant la HashMap de rendements pour l'usage de
	// chauffage
	public HashMap<String, Conso> initializeRdtChauff(HashMap<String, ParamRdtCout> rdtCoutChauffMap,
			HashMap<String, Conso> besoinsInit, int pasdeTemps) {

		HashMap<String, Conso> rdtChauff = new HashMap<String, Conso>();

		for (String id : besoinsInit.keySet()) {
			Conso rdt = new Conso(pasdeTemps);
			Conso besoinSegment = besoinsInit.get(id);
			String idTest = getIdConstructorChauff(besoinSegment);
			ParamRdtCout rdtSegment = rdtCoutChauffMap.get(idTest);
			rdt.setId(besoinSegment.getId());
			rdt.setAnneeRenov(Constants.INIT_STATE);
			rdt.setTypeRenovBat(TypeRenovBati.ETAT_INIT);
			rdt.setAnneeRenovSys(Constants.INIT_STATE);
			rdt.setTypeRenovSys(TypeRenovSysteme.ETAT_INIT);
			rdt.setAnnee(0, rdtSegment.getRdt());
			rdtChauff.put(id, rdt);
		}

		return rdtChauff;

	}

	protected String getIdConstructorClim(Conso besoinSegment) {

		StringBuffer buffer = new StringBuffer();
		buffer.append(besoinSegment.getIdbranche());
		buffer.append(besoinSegment.getIdssbranche());
		buffer.append(besoinSegment.getIdbattype());
		buffer.append(besoinSegment.getId().substring(START_SYS_FROID_AGREG,
				START_SYS_FROID_AGREG + LENGHT_SYS_FROID_AGREG));
		buffer.append("0");

		return buffer.toString();

	}

	protected String getIdConstructorChauff(Conso besoinSegment) {

		StringBuffer buffer = new StringBuffer();
		buffer.append(besoinSegment.getIdbranche());
		buffer.append(besoinSegment.getIdssbranche());
		buffer.append(besoinSegment.getIdbattype());
		buffer.append(besoinSegment.getIdsyschaud());
		buffer.append(besoinSegment.getId().substring(START_ENERG, START_ENERG + LENGHT_ENERG));
		buffer.append("0");

		return buffer.toString();

	}

	// Methode initialisant la HashMap de resultats de consommations pour
	// l'usage d'ECS
	public HashMap<String, Conso> initializeConsoEcs(HashMap<String, Conso> rdtEcsMap,
			HashMap<String, Conso> besoinsInit, int pasdeTemps, HashMap<String, ResultConsoURt> resultConsoURtMap) {
		int anneeNTab = 0;
		HashMap<String, Conso> consoEcsMap = new HashMap<String, Conso>();
		String idResultRt;
		// id = idAgreg (14char) + idEnergEcs (2char) + anneeRenovSys +
		// typeRenovSys
		for (String id : besoinsInit.keySet()) {
			Conso consoEcs = new Conso(pasdeTemps);
			Conso besoinSegment = besoinsInit.get(id);
			Conso rdtSegment = rdtEcsMap.get(id);
			BigDecimal conso = new BigDecimal("0");
			if (rdtSegment != null && rdtSegment.getAnnee(0).signum() != 0) {
				conso = besoinSegment.getAnnee(0).divide(rdtSegment.getAnnee(0), MathContext.DECIMAL32);

			}

			consoEcs.setId(besoinSegment.getId());
			consoEcs.setAnneeRenovSys(Constants.INIT_STATE);
			consoEcs.setTypeRenovSys(TypeRenovSysteme.ETAT_INIT);
			consoEcs.setAnneeRenov(Constants.INIT_STATE);
			consoEcs.setTypeRenovBat(TypeRenovBati.ETAT_INIT);
			consoEcs.setAnnee(0, conso);
			consoEcsMap.put(id, consoEcs);

			// Remplissage de la Map resultConsoURtMap
			idResultRt = besoinSegment.getIdagreg() + besoinSegment.getAnneeRenov() + besoinSegment.getTypeRenovBat();

			if (resultConsoURtMap.containsKey(idResultRt)) {
				BigDecimal consoEP = conso.multiply(FACTEUR_EP).multiply(new BigDecimal("0.35"));
				consoEP = consoEP.add(conso.multiply(new BigDecimal("0.65")));
				insertResultConsoUExistECS(resultConsoURtMap, anneeNTab, idResultRt, conso, consoEP, 2009);

			} else {
				LOG.info("Probleme initialize ECS");
			}

		}

		return consoEcsMap;

	}

	public void insertResultConsoUExistEcl(HashMap<String, ResultConsoURt> resultConsoURtMap, int anneeNTab,
			String idResultRtClim, BigDecimal consoEF, BigDecimal consoEP, int annee) {
		int index = anneeNTab;
		if (annee == 2009) {
			index = 0;
		}
		ResultConsoURt resultInsert = resultConsoURtMap.get(idResultRtClim);
		BigDecimal surfExist = BigDecimal.ZERO;
		if (resultInsert.getSurfTot(index) != null) {
			surfExist = resultInsert.getSurfTot(index);
		}
		BigDecimal consoUExistEF = BigDecimal.ZERO;
		if (resultInsert.getConsoUEclairageEF(index) != null) {
			consoUExistEF = resultInsert.getConsoUEclairageEF(index);
		}
		BigDecimal consoUExistEP = BigDecimal.ZERO;
		if (resultInsert.getConsoUEclairageEP(index) != null) {
			consoUExistEP = resultInsert.getConsoUEclairageEP(index);
		}
		BigDecimal consoUEF = BigDecimal.ZERO;
		BigDecimal consoUEP = BigDecimal.ZERO;
		if (surfExist.signum() != 0) {
			consoUEF = consoEF.divide(surfExist, MathContext.DECIMAL32);
			consoUEP = consoEP.divide(surfExist, MathContext.DECIMAL32);
		}
		resultInsert.setConsoUEclairageEF(index, consoUExistEF.add(consoUEF));
		resultInsert.setConsoUEclairageEP(index, consoUExistEP.add(consoUEP));
		resultConsoURtMap.put(resultInsert.getId(), resultInsert);
	}

	public void insertResultConsoUExistECS(HashMap<String, ResultConsoURt> resultConsoURtMap, int anneeNTab,
			String idResultRtClim, BigDecimal consoEF, BigDecimal consoEP, int annee) {
		int index = anneeNTab;
		if (annee == 2009) {
			index = 0;
		}
		ResultConsoURt resultInsert = resultConsoURtMap.get(idResultRtClim);
		BigDecimal surfExist = BigDecimal.ZERO;
		if (resultInsert.getSurfTot(index) != null) {
			surfExist = resultInsert.getSurfTot(index);
		}
		BigDecimal consoUExistEF = BigDecimal.ZERO;
		if (resultInsert.getConsoUEcsEF(index) != null) {
			consoUExistEF = resultInsert.getConsoUEcsEF(index);
		}
		BigDecimal consoUExistEP = BigDecimal.ZERO;
		if (resultInsert.getConsoUEcsEP(index) != null) {
			consoUExistEP = resultInsert.getConsoUEcsEP(index);
		}
		BigDecimal consoUEF = BigDecimal.ZERO;
		BigDecimal consoUEP = BigDecimal.ZERO;
		if (surfExist.signum() != 0) {
			consoUEF = consoEF.divide(surfExist, MathContext.DECIMAL32);
			consoUEP = consoEP.divide(surfExist, MathContext.DECIMAL32);
		}
		resultInsert.setConsoUEcsEF(index, consoUExistEF.add(consoUEF));
		resultInsert.setConsoUEcsEP(index, consoUExistEP.add(consoUEP));
		resultConsoURtMap.put(resultInsert.getId(), resultInsert);
	}

	public void insertResultConsoUExistClim(HashMap<String, ResultConsoUClim> resultConsoUClimMap, int anneeNTab,
			String idResultRtClim, BigDecimal consoEF, BigDecimal consoEP, int annee) {
		int index = anneeNTab;
		if (annee == 2009) {
			index = 0;
		}
		ResultConsoUClim resultInsert = resultConsoUClimMap.get(idResultRtClim);
		BigDecimal surfExist = BigDecimal.ZERO;
		if (resultInsert.getSurfTot(index) != null) {
			surfExist = resultInsert.getSurfTot(index);
		}
		BigDecimal consoUExistEF = BigDecimal.ZERO;
		if (resultInsert.getConsoUClimEF(index) != null) {
			consoUExistEF = resultInsert.getConsoUClimEF(index);
		}
		BigDecimal consoUExistEP = BigDecimal.ZERO;
		if (resultInsert.getConsoUClimEP(index) != null) {
			consoUExistEP = resultInsert.getConsoUClimEP(index);
		}
		BigDecimal consoUEF = BigDecimal.ZERO;
		BigDecimal consoUEP = BigDecimal.ZERO;
		if (surfExist.signum() != 0) {
			consoUEF = consoEF.divide(surfExist, MathContext.DECIMAL32);
			consoUEP = consoEP.divide(surfExist, MathContext.DECIMAL32);
		}
		resultInsert.setConsoUClimEF(index, consoUExistEF.add(consoUEF));
		resultInsert.setConsoUClimEP(index, consoUExistEP.add(consoUEP));
		resultConsoUClimMap.put(resultInsert.getId(), resultInsert);
	}

	// Methode initialisant la HashMap de resultats de consommations pour
	// l'usage de climatisation
	public HashMap<String, Conso> initializeConsoChauffClim(HashMap<String, Conso> rdtMap,
			HashMap<String, Conso> besoinsInit, int pasdeTemps) {

		HashMap<String, Conso> consoMap = new HashMap<String, Conso>();

		for (String id : besoinsInit.keySet()) {
			Conso consoChauffClim = new Conso(pasdeTemps);
			Conso besoinSegment = besoinsInit.get(id);
			Conso rdtSegment = rdtMap.get(id);
			BigDecimal conso = BigDecimal.ZERO;
			if (rdtSegment != null && rdtSegment.getAnnee(0).signum() != 0) {
				conso = besoinSegment.getAnnee(0).divide(rdtSegment.getAnnee(0), MathContext.DECIMAL32);

			}

			consoChauffClim.setId(besoinSegment.getId());
			consoChauffClim.setAnneeRenovSys(Constants.INIT_STATE);
			consoChauffClim.setTypeRenovSys(TypeRenovSysteme.ETAT_INIT);
			consoChauffClim.setAnnee(0, conso);
			consoMap.put(id, consoChauffClim);
		}

		return consoMap;

	}

	// Methode initialisant la HashMap de resultats de consommations pour
	// l'usage de climatisation
	public HashMap<String, Conso> initializeConsoClim(HashMap<String, ResultConsoUClim> resultConsoUClimMap,
			HashMap<String, Conso> rdtMap, HashMap<String, Conso> besoinsInit, int pasdeTemps) {

		HashMap<String, Conso> consoMap = new HashMap<String, Conso>();
		String idResultClim;
		int anneeNTab = 0;
		for (String id : besoinsInit.keySet()) {
			Conso consoChauffClim = new Conso(pasdeTemps);
			Conso besoinSegment = besoinsInit.get(id);
			Conso rdtSegment = rdtMap.get(id);
			BigDecimal conso = BigDecimal.ZERO;
			if (rdtSegment != null && rdtSegment.getAnnee(0).signum() != 0) {
				conso = besoinSegment.getAnnee(0).divide(rdtSegment.getAnnee(0), MathContext.DECIMAL32);

			}

			consoChauffClim.setId(besoinSegment.getId());
			consoChauffClim.setAnneeRenovSys(Constants.INIT_STATE);
			consoChauffClim.setTypeRenovSys(TypeRenovSysteme.ETAT_INIT);
			consoChauffClim.setTypeRenovBat(TypeRenovBati.ETAT_INIT);
			consoChauffClim.setAnneeRenov(Constants.INIT_STATE);
			consoChauffClim.setAnnee(0, conso);
			consoMap.put(id, consoChauffClim);

			// Remplissage de la Map resultConsoURtMap
			idResultClim = consoChauffClim.getIdagreg()
					+ consoChauffClim.getId().substring(START_SYS_FROID_AGREG,
							START_SYS_FROID_AGREG + LENGHT_SYS_FROID_AGREG) + consoChauffClim.getAnneeRenov()
					+ consoChauffClim.getTypeRenovBat();

			if (resultConsoUClimMap.containsKey(idResultClim)) {
				BigDecimal consoEP = conso.multiply(FACTEUR_EP);
				insertResultConsoUExistClim(resultConsoUClimMap, anneeNTab, idResultClim, conso, consoEP, 2009);

			} else {
				LOG.info("Probleme Initialize Clim");
			}
		}
		return consoMap;

	}

}
