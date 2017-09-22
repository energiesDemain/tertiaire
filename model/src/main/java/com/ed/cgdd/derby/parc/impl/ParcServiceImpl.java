package com.ed.cgdd.derby.parc.impl;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;

import com.ed.cgdd.derby.model.calcconso.ParamTxClimExistant;
import com.ed.cgdd.derby.model.calcconso.ParamTxClimNeuf;
import com.ed.cgdd.derby.model.calcconso.ResultConsoUClim;
import com.ed.cgdd.derby.model.calcconso.ResultConsoURt;
import com.ed.cgdd.derby.model.parc.MapResultsKeys;
import com.ed.cgdd.derby.model.parc.ParamParcArray;
import com.ed.cgdd.derby.model.parc.Parc;
import com.ed.cgdd.derby.model.parc.Period;
import com.ed.cgdd.derby.model.parc.PmNeuf;
import com.ed.cgdd.derby.model.parc.ResultParc;
import com.ed.cgdd.derby.model.parc.TypeRenovBati;
import com.ed.cgdd.derby.model.parc.TypeRenovSysteme;
import com.ed.cgdd.derby.parc.InsertParcDAS;
import com.ed.cgdd.derby.parc.LoadParcDataDAS;
import com.ed.cgdd.derby.parc.ParcService;
import com.ed.cgdd.derby.parc.TruncateParcTableDAS;

public class ParcServiceImpl implements ParcService {
	private final static Logger LOG = LogManager.getLogger(ParcServiceImpl.class);
	private final static int START_ID_PARC = 0;
	private final static int LENGTH_ID_PARC = 18;
	private final static int START_SYS_CHAUD_PM = 6;
	private final static int LENGHT_SYS_CHAUD_PM = 2;
	private final static int START_ENERG_PM = 8;
	private final static int LENGHT_ENERG_PM = 2;
	private final static String CODE_CLIM = "01";
	private final static String CODE_NON_CLIM = "02";
	private static final int START_ID_SEG = 0;
	private static final int LENGTH_ID_SEG_AGREG = 12;
	// TODO : réparer
	private static final int SURF_CLIM_LIMITE = 1000000000;
	private JdbcTemplate jdbcTemplate;
	private LoadParcDataDAS loadParcDatadas;
	private InsertParcDAS insertParcdas;
	private TruncateParcTableDAS truncateParcTabledas;

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
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

	@Override
	public ResultParc parc(HashMap<String, ParamTxClimExistant> txClimExistantMap,
			HashMap<String, BigDecimal> partsMarchesNeuf, HashMap<String, Parc> parcTotMap,
			HashMap<String, ParamParcArray> entreesMap, HashMap<String, ParamParcArray> sortiesMap,
			HashMap<String, ParamTxClimNeuf> txClimNeufMap, int pasdeTemps, int anneeNTab, int annee) {

		HashMap<String, Parc> parcEntrantMap = new HashMap<String, Parc>();
		HashMap<String, Parc> parcSortantMap = new HashMap<String, Parc>();
		ResultParc resultatsParc = new ResultParc();

		// Agregation des surfaces selon un ID reduit (sans la periode de
		// cstruction, l'energie de chauffage et les systemes de production
		// chaud et froid)

		HashMap<String, Parc> parcAgregMapN = aggregateParc(parcTotMap, anneeNTab);

		// Calcul des entrees dans le parc a l'annee N1
		// LOG.info("Parc entrant");
		resultatsParc.put(
				MapResultsKeys.PARC_ENTRANT.getLabel(),
				parcEntrant(parcEntrantMap, entreesMap, partsMarchesNeuf, txClimNeufMap, parcAgregMapN, anneeNTab,
						pasdeTemps, annee));

		// Calcul des sorties du parc a l'annee N1
		// LOG.info("Parc sortant");
		resultatsParc.put(MapResultsKeys.PARC_SORTANT.getLabel(),
				parcSortant(parcSortantMap, parcTotMap, sortiesMap, anneeNTab, pasdeTemps, annee));

		// Evolution de l'ensemble du parc a l'annee N1
		// LOG.info("Parc total");
		resultatsParc.put(
				MapResultsKeys.PARC_TOT.getLabel(),
				evolutionParc(txClimExistantMap, parcTotMap, parcEntrantMap, parcSortantMap, anneeNTab, pasdeTemps,
						annee));

		return resultatsParc;

	}

	public HashMap<String, ParamParcArray> sortData(List<ParamParcArray> liste) {

		HashMap<String, ParamParcArray> listeMap = new HashMap<String, ParamParcArray>();
		// entrees dans le parc
		for (ParamParcArray paramParcEntrees : liste) {
			listeMap.put(paramParcEntrees.getBranche(), paramParcEntrees);
		}
		return listeMap;
	}

	public HashMap<String, Parc> sortDataParc(List<Parc> parc, HashMap<String, ResultConsoURt> resultConsoURtMap,
			int pasdeTemps, HashMap<String, ResultConsoUClim> resultConsoUClimMap) {

		HashMap<String, Parc> parcMap = new HashMap<String, Parc>();

		String idTempAgregRt;
		String idTempAgregClim;

		int anneeNTab = 0;
		for (Parc parcTemp : parc) {
			idTempAgregRt = parcTemp.getIdagreg() + parcTemp.getAnneeRenov() + parcTemp.getTypeRenovBat();
			idTempAgregClim = parcTemp.getIdagreg() + parcTemp.getIdsysfroid() + parcTemp.getAnneeRenov()
					+ parcTemp.getTypeRenovBat();
			parcMap.put(
					parcTemp.getId() + parcTemp.getAnneeRenov() + parcTemp.getTypeRenovBat()
							+ parcTemp.getAnneeRenovSys() + parcTemp.getTypeRenovSys(), parcTemp);
			insertConsoURt(resultConsoURtMap, pasdeTemps, idTempAgregRt, anneeNTab, parcTemp, 2009);
			insertConsoUClim(resultConsoUClimMap, pasdeTemps, idTempAgregClim, anneeNTab, parcTemp, 2009);
		}

		return parcMap;
	}

	protected void insertConsoURt(HashMap<String, ResultConsoURt> resultConsoURtMap, int pasdeTemps,
			String idTempAgreg, int anneeNTab, Parc parcTemp, int annee) {
		int index = anneeNTab;
		if (annee == 2009) {
			index = 0;
		}
		if (resultConsoURtMap.containsKey(idTempAgreg)) {
			ResultConsoURt resultInsert = resultConsoURtMap.get(idTempAgreg);
			resultInsert.setSurfTot(index, resultInsert.getSurfTot(index).add(parcTemp.getAnnee(index)));
			resultConsoURtMap.put(resultInsert.getId(), resultInsert);
		} else {
			ResultConsoURt resultInsert = new ResultConsoURt(pasdeTemps);
			resultInsert.setId(idTempAgreg);
			resultInsert.setTypeRenovBat(TypeRenovBati.ETAT_INIT);
			resultInsert.setAnneeRenov(parcTemp.getAnneeRenov());
			resultInsert.setSurfTot(index, parcTemp.getAnnee(index));
			resultConsoURtMap.put(resultInsert.getId(), resultInsert);
		}
	}

	protected void insertConsoUClim(HashMap<String, ResultConsoUClim> resultConsoUClimMap, int pasdeTemps,
			String idTempAgregClim, int anneeNTab, Parc parcTemp, int annee) {

		int index = anneeNTab;
		if (annee == 2009) {
			index = 0;
		}

		if (resultConsoUClimMap.containsKey(idTempAgregClim)) {
			ResultConsoUClim resultInsert = resultConsoUClimMap.get(idTempAgregClim);
			resultInsert.setSurfTot(index, resultInsert.getSurfTot(index).add(parcTemp.getAnnee(index)));
			resultConsoUClimMap.put(resultInsert.getId(), resultInsert);
		} else {
			ResultConsoUClim resultInsert = new ResultConsoUClim(pasdeTemps);
			resultInsert.setId(idTempAgregClim);
			resultInsert.setTypeRenovBat(TypeRenovBati.ETAT_INIT);
			resultInsert.setAnneeRenov(parcTemp.getAnneeRenov());
			resultInsert.setSurfTot(index, parcTemp.getAnnee(index));
			resultConsoUClimMap.put(resultInsert.getId(), resultInsert);
		}
	}

	public HashMap<String, PmNeuf> sortDataPm(List<PmNeuf> pm) {

		HashMap<String, PmNeuf> pmMap = new HashMap<String, PmNeuf>();

		for (PmNeuf pmNeuf : pm) {
			if (pmNeuf.getPart().compareTo(new BigDecimal("0")) != 0) {
				StringBuffer buffer = new StringBuffer();
				buffer.append(pmNeuf.getIdSyschaud());
				buffer.append(pmNeuf.getIdEnerg());
				pmMap.put(buffer.toString(), pmNeuf);
			}

		}

		return pmMap;
	}

	protected HashMap<String, Parc> aggregateParc(HashMap<String, Parc> parcTotMap, int anneeNTab) {

		HashMap<String, Parc> parcAgreg = new HashMap<String, Parc>();

		for (String mapKey : parcTotMap.keySet()) {

			Parc parcn = parcTotMap.get(mapKey);

			String cle = generateIDAgreg(parcn);

			if (parcAgreg.containsKey(cle)) {
				Parc temp = new Parc(0);
				temp = parcAgreg.get(cle);
				temp.setAnnee(0, temp.getAnnee(0).add(parcn.getAnnee(anneeNTab - 1)));
				parcAgreg.put(cle, new Parc(temp));

			} else {
				Parc temp = new Parc(0);
				temp.setId(cle);
				temp.setAnneeRenov(parcn.getAnneeRenov());
				temp.setTypeRenovBat(parcn.getTypeRenovBat());
				temp.setAnnee(0, parcn.getAnnee(anneeNTab - 1));
				parcAgreg.put(cle, new Parc(temp));
			}
		}

		return parcAgreg;
	}

	protected int correspPeriode(int annee) {

		int periode;
		if (annee > 2009 && annee < 2016) {
			periode = 1;
		} else if (annee > 2015 && annee < 2021) {
			periode = 2;
		} else if (annee > 2020 && annee < 2031) {
			periode = 3;
		} else if (annee > 2030 && annee < 2041) {
			periode = 4;
		} else {
			periode = 5;
		}

		return periode;

	}

	protected BigDecimal calcEntree(BigDecimal parcAgregN, BigDecimal pmN, BigDecimal tx, BigDecimal entreesN) {
		BigDecimal entreesN1 = parcAgregN.multiply(pmN).multiply(entreesN).multiply(tx);
		return entreesN1;

	}

	protected BigDecimal calcSortie(BigDecimal parcN, BigDecimal sortiesN) {

		BigDecimal sortiesN1 = parcN.multiply(sortiesN);
		return sortiesN1;

	}

	protected Collection<String> clesTot(HashMap<String, Parc> parcEntrantMap, HashMap<String, Parc> parcSortantMap) {

		Set<String> cles = new HashSet<String>();
		cles.addAll(parcEntrantMap.keySet());
		cles.addAll(parcSortantMap.keySet());

		return cles;
	}

	protected String bufferCle(String mapKeyParcAgregN, int newPeriode, int newPeriodeDetail, String mapKeyPm,
			String eqClim) {

		StringBuffer id = new StringBuffer();
		id.append(mapKeyParcAgregN);
		id.append(newPeriodeDetail);
		id.append("0");
		id.append(newPeriode);
		// SysChaud
		id.append(mapKeyPm.substring(START_SYS_CHAUD_PM, START_SYS_CHAUD_PM + LENGHT_SYS_CHAUD_PM));
		id.append(eqClim);
		// Energie
		id.append(mapKeyPm.substring(START_ENERG_PM, START_ENERG_PM + LENGHT_ENERG_PM));
		String cle = id.toString();

		return cle;

	}

	protected HashMap<String, Parc> parcEntrant(HashMap<String, Parc> parcEntrantMap,
			HashMap<String, ParamParcArray> entreesMap, HashMap<String, BigDecimal> partsMarchesNeuf,
			HashMap<String, ParamTxClimNeuf> txClimNeufMap, HashMap<String, Parc> parcAgregMapN, int anneeNTab,
			int pasdeTemps, int annee) {
		// TODO : tester la methode
		int periode = correspPeriode(annee);
		int newPeriode = periode + 3;
		int newPeriodeDetail = periode + 18;
		// Boucle sur les idAgreg
		for (String mapKeyParcAgregN : parcAgregMapN.keySet()) {
			// Boucle sur les cles de pmMap (idEnergie+idSysChaud)
			Parc parcAgregN = parcAgregMapN.get(mapKeyParcAgregN);

			BigDecimal txClim = txClimNeufMap.get(parcAgregN.getIdbranche()).getTx(periode);
			BigDecimal txNonClim = BigDecimal.ONE.subtract(txClim);
			// determination des systemes chauds et des energies de
			// chauffage
			for (String mapKeyPm : partsMarchesNeuf.keySet()) {
				// mapKeyPm est un agregat des codes :
				// branche, ss_branche, bat_type, production_chaud,
				// energie, periode
				HashMap<String, BigDecimal> cleMap = new HashMap<String, BigDecimal>();
				ParamParcArray neufN = entreesMap.get(parcAgregN.getIdbranche());
				String cleClim = bufferCle(mapKeyParcAgregN, newPeriode, newPeriodeDetail, mapKeyPm, CODE_CLIM);
				String cleNonClim = bufferCle(mapKeyParcAgregN, newPeriode, newPeriodeDetail, mapKeyPm, CODE_NON_CLIM);
				cleMap.put(cleClim, txClim);
				cleMap.put(cleNonClim, txNonClim);
				// Boucle pour prendre en compte les surfaces climatisees et non
				// climatisees
				for (String cle : cleMap.keySet()) {
					BigDecimal tx = cleMap.get(cle);
					Parc parcNeuf = null;
					if (parcEntrantMap.containsKey(cle)) {
						parcNeuf = parcEntrantMap.get(cle);
					}

					Parc resultEntrant = calcEntreesParc(parcNeuf, parcAgregN, partsMarchesNeuf.get(mapKeyPm), tx,
							neufN, anneeNTab, pasdeTemps, annee, cle);
					parcEntrantMap.put(cleMap(cle, resultEntrant), resultEntrant);

				}
			}

		}
		return parcEntrantMap;

	}

	protected String cleMap(String cle, Parc resultEntrant) {

		StringBuffer buffer = new StringBuffer();
		buffer.append(cle);
		buffer.append(resultEntrant.getAnneeRenov());
		buffer.append(resultEntrant.getTypeRenovBat());
		buffer.append(resultEntrant.getAnneeRenovSys());
		buffer.append(resultEntrant.getTypeRenovSys());

		return buffer.toString();

	}

	protected HashMap<String, Parc> parcSortant(HashMap<String, Parc> parcSortantMap, HashMap<String, Parc> parcTotMap,
			HashMap<String, ParamParcArray> sortiesMap, int anneeNTab, int pasdeTemps, int annee) {

		int periode = correspPeriode(annee);

		for (String mapKeyParcN : parcTotMap.keySet()) {
			Parc sortie = null;
			if (parcSortantMap.containsKey(mapKeyParcN)) {
				sortie = parcSortantMap.get(mapKeyParcN);
			} else {
				sortie = new Parc(pasdeTemps);
			}
			Parc parcN = parcTotMap.get(mapKeyParcN);
			ParamParcArray sortiesN = sortiesMap.get(parcN.getIdbranche());

			String period = parcN.getIdperiodesimple();

			if ((period.equals(Period.PERIODE_BEFORE_1980.getCode())
					|| period.equals(Period.PERIODE_1981_1998.getCode()) || period.equals(Period.PERIODE_1999_2008
					.getCode())) && parcN.getAnnee(anneeNTab - 1).signum() != 0) {

				BigDecimal parcNvalue = BigDecimal.ZERO;
				if (parcN != null) { // && parcN.getAnneeN() != null) {
					parcNvalue = parcN.getAnnee(anneeNTab - 1);
				}
				BigDecimal sortiesNvalue = BigDecimal.ZERO;
				if (sortiesN != null && sortiesN.getPeriode(periode) != null) {
					sortiesNvalue = sortiesN.getPeriode(periode);
				}

				sortie.setId(mapKeyParcN);
				sortie.setAnnee(anneeNTab, calcSortie(parcNvalue, sortiesNvalue));
				parcSortantMap.put(mapKeyParcN, sortie);

			}

		}
		return parcSortantMap;
	}

	protected HashMap<String, Parc> evolutionParc(HashMap<String, ParamTxClimExistant> txClimExistantMap,
			HashMap<String, Parc> parcTotMap, HashMap<String, Parc> parcEntrantMap,
			HashMap<String, Parc> parcSortantMap, int anneeNTab, int pasdeTemps, int annee) {

		Collection<String> clesTot = clesTot(parcEntrantMap, parcSortantMap);
		int periode = correspPeriode(annee);
		Iterator<String> iterator = parcTotMap.keySet().iterator();
		while (iterator.hasNext()) {
			String parcTotKey = iterator.next();
			Parc entree = new Parc(pasdeTemps);
			entree = parcEntrantMap.get(parcTotKey);
			Parc sortie = new Parc(pasdeTemps);
			sortie = parcSortantMap.get(parcTotKey);
			Parc parcN = parcTotMap.get(parcTotKey);
			// Recuperation du taux de climatisation supplementaire dans le bati
			// existant
			BigDecimal txClimExistant = txClimExistantMap.get(parcN.getIdbranche()).getTx(periode);
			parcN = calcEvol(parcN, entree, sortie, anneeNTab);
			// Si le segment est non climatise alors on prend en compte une
			// possible evolution du taux de clim
			if (parcN.getIdsysfroid().equals(CODE_NON_CLIM)) {
				BigDecimal surfClim = BigDecimal.ZERO;
				BigDecimal surfNonClim = BigDecimal.ZERO;
				// Nouvelle surface climatisee
				if (txClimExistant != null && txClimExistant.signum() != 0 && parcN.getAnnee(anneeNTab) != null) {
					surfClim = txClimExistant.multiply(parcN.getAnnee(anneeNTab));
					surfNonClim = (BigDecimal.ONE.subtract(txClimExistant)).multiply(parcN.getAnnee(anneeNTab));
				}
				// si la surface climatisee supplementaire est differente de
				// zero
				if (surfClim.signum() != 0) {
					String cleClim = newParcIdMap(parcN, CODE_CLIM);
					if (parcTotMap.containsKey(cleClim)) {
						// Si le segment existe deja alors on ajoute les
						// surfaces
						// climatisees supplementaires
						Parc parcClimExist = parcTotMap.get(cleClim);
						parcClimExist.setAnnee(anneeNTab, parcClimExist.getAnnee(anneeNTab).add(surfClim));

					} else {
						// Un nouveau segment est cree uniquement si les
						// nouvelles
						// surfaces climatisees sont superieures a 100m²
						if (surfClim.compareTo(new BigDecimal(SURF_CLIM_LIMITE)) >= 0) {
							Parc parcNew = new Parc(pasdeTemps);
							parcNew.setId(cleClim.substring(START_ID_PARC, START_ID_PARC + LENGTH_ID_PARC));
							parcNew.setAnneeRenov(parcN.getAnneeRenov());
							parcNew.setAnneeRenovSys(parcN.getAnneeRenovSys());
							parcNew.setTypeRenovBat(parcN.getTypeRenovBat());
							parcNew.setTypeRenovSys(parcN.getTypeRenovSys());
							// NewSurfClim = surfClimSegment+pmSysteme
							parcNew.setAnnee(anneeNTab, surfClim);
							parcTotMap.put(cleClim, parcNew);
						} else {
							surfNonClim = surfNonClim.add(surfClim);
						}
					}
					// Modification du segmenet initial
					parcN.setAnnee(anneeNTab, surfNonClim);
				}

			}

		}

		for (String cle : clesTot) { // parcourt les nouvelles cles

			Parc entree = parcEntrantMap.get(cle);
			Parc sortie = parcSortantMap.get(cle);

			if (!parcTotMap.containsKey(cle)) { // si la condition est verifiee
				Parc parcN = new Parc(pasdeTemps); // un nouveau parc est
													// affecte
				parcN.setId(cle.substring(START_ID_PARC, START_ID_PARC + LENGTH_ID_PARC));
				parcN.setAnneeRenov(String.valueOf(annee));
				parcN.setTypeRenovBat(TypeRenovBati.ETAT_INIT);
				parcN.setAnneeRenovSys(String.valueOf(annee));
				parcN.setTypeRenovSys(TypeRenovSysteme.ETAT_INIT);
				parcN = calcEvol(parcN, entree, sortie, anneeNTab);
				parcTotMap.put(cleMap(cle.substring(START_ID_PARC, START_ID_PARC + LENGTH_ID_PARC), parcN), new Parc(
						parcN));

			}

		}

		return parcTotMap;

	}

	protected String newParcIdMap(Parc parcAgreg, String sysCode) {

		StringBuffer buffer = new StringBuffer();
		buffer.append(parcAgreg.getId().substring(START_ID_SEG, START_ID_SEG + LENGTH_ID_SEG_AGREG));
		buffer.append(parcAgreg.getIdsyschaud());
		buffer.append(sysCode);
		buffer.append(parcAgreg.getIdenergchauff());
		buffer.append(parcAgreg.getAnneeRenov());
		buffer.append(parcAgreg.getTypeRenovBat());
		buffer.append(parcAgreg.getAnneeRenovSys());
		buffer.append(parcAgreg.getTypeRenovSys());

		return buffer.toString();

	}

	protected Parc calcEvol(Parc parcN, Parc entree, Parc sortie, int anneeNTab) {

		BigDecimal parcNValue = BigDecimal.ZERO;
		BigDecimal surfSup = BigDecimal.ZERO;
		if (parcN.getAnnee(anneeNTab) != null && parcN.getAnnee(anneeNTab).signum() != 0) {
			surfSup = parcN.getAnnee(anneeNTab);
		}
		if (parcN.getAnnee(anneeNTab - 1) != null) {// && parcN.getAnneeN() !=
													// null) {
			parcNValue = parcN.getAnnee(anneeNTab - 1);
		}
		BigDecimal entreeN1Value = BigDecimal.ZERO;
		if (entree != null) {// && entree.getAnneeN1() != null) {
			entreeN1Value = entree.getAnnee(anneeNTab);
		}

		BigDecimal sortieN1Value = BigDecimal.ZERO;
		if (sortie != null) {// && sortie.getAnneeN1() != null) {
			sortieN1Value = sortie.getAnnee(anneeNTab);
		}

		parcN.setAnnee(anneeNTab, parcNValue.subtract(sortieN1Value).add(entreeN1Value).add(surfSup));

		return parcN;

	}

	protected Parc calcEntreesParc(Parc parcNeuf, Parc parcAgregN, BigDecimal pmN, BigDecimal tx, ParamParcArray neufN,
			int anneeNTab, int pasdeTemps, int annee, String cle) {
		int periode = correspPeriode(annee);
		if (parcNeuf == null) {
			parcNeuf = new Parc(pasdeTemps);
		}

		BigDecimal parcAgregNvalue = BigDecimal.ZERO;
		if (parcAgregN != null) {// && parcAgregN.getAnneeN() != null) {
			parcAgregNvalue = parcAgregN.getAnnee(0);
		}
		BigDecimal pmNvalue = BigDecimal.ZERO;
		if (pmN != null) {
			pmNvalue = pmN;
		}
		BigDecimal neufNvalue = BigDecimal.ZERO;
		if (neufN != null && neufN.getPeriode(periode) != null) {
			neufNvalue = neufN.getPeriode(periode);
		}

		parcNeuf.setId(cle);
		parcNeuf.setAnnee(anneeNTab, calcEntree(parcAgregNvalue, pmNvalue, tx, neufNvalue));
		parcNeuf.setAnneeRenov(String.valueOf(annee));
		parcNeuf.setTypeRenovBat(TypeRenovBati.ETAT_INIT);
		parcNeuf.setAnneeRenovSys(String.valueOf(annee));
		parcNeuf.setTypeRenovSys(TypeRenovSysteme.ETAT_INIT);
		return parcNeuf;
	}

	// protected Parc initializeParc(String cle) {
	// Parc parcNeuf = new Parc();
	// parcNeuf.setId(cle);
	//
	// return parcNeuf;
	// }

	protected String generateIDAgreg(Parc parcn) {
		StringBuffer idAgreg = new StringBuffer();
		idAgreg.append(parcn.getIdbranche());
		idAgreg.append(parcn.getIdssbranche());
		idAgreg.append(parcn.getIdbattype());
		idAgreg.append(parcn.getIdoccupant());
		return idAgreg.toString();
	}

	protected void transfertParc(HashMap<String, Parc> parcMap, int pasdeTemps) {

		Parc parcTemp = new Parc(pasdeTemps);
		for (String parcMapKey : parcMap.keySet()) {
			parcTemp = parcMap.get(parcMapKey);
			parcTemp.setAnnee(0, parcTemp.getAnnee(pasdeTemps));
			for (int i = 1; i <= pasdeTemps; i++) {
				parcTemp.setAnnee(i, BigDecimal.ZERO);
			}
			parcMap.put(parcMapKey, parcTemp);
			parcTemp = new Parc(pasdeTemps);
		}

	}

}
