package com.ed.cgdd.derby.common.impl;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ed.cgdd.derby.common.CommonService;
import com.ed.cgdd.derby.finance.CoutEnergieService;
import com.ed.cgdd.derby.model.calcconso.Conso;
import com.ed.cgdd.derby.model.calcconso.ResultConsoRdt;
import com.ed.cgdd.derby.model.calcconso.ResultConsoRt;
import com.ed.cgdd.derby.model.calcconso.ResultConsoUClim;
import com.ed.cgdd.derby.model.calcconso.ResultConsoURt;
import com.ed.cgdd.derby.model.financeObjects.CoutEnergie;
import com.ed.cgdd.derby.model.financeObjects.ElasticiteMap;
import com.ed.cgdd.derby.model.financeObjects.Emissions;
import com.ed.cgdd.derby.model.parc.Energies;
import com.ed.cgdd.derby.model.parc.MapResultsKeys;
import com.ed.cgdd.derby.model.parc.Parc;
import com.ed.cgdd.derby.model.parc.Period;
import com.ed.cgdd.derby.model.parc.ResultParc;
import com.ed.cgdd.derby.model.parc.TypeRenovBati;
import com.ed.cgdd.derby.model.parc.Usage;

public class CommonServiceImpl implements CommonService {
	private final static Logger LOG = LogManager.getLogger(CommonServiceImpl.class);
	private static final int LENGTH_AGREG = 8;
	private static final int START_AGREG = 0;
	private static final int LENGTH_ID_ECS = 14;
	private static final int LENGTH_ID_RT = 12;
	private static final int START_ID = 0;
	private static final int LENGTH_ID_CLIM = 16;
	private static final int START_ID_PARC = 0;
	private static final int LENGTH_ID_PARC = 18;
	private static final int LENGTH_ID_TOT = 18;
	private static final int START_SYS_FROID = 12;
	private static final int LENGTH_SYS_FROID = 2;
	private static final int START_ID_BRANCHE = 0;
	private static final int LENGTH_ID_BRANCHE = 2;
	private static final String INIT_STATE = "Etat initial";
	private static final BigDecimal FACTEUR_TVA = new BigDecimal("1.20");
	private CoutEnergieService coutEnergieService;

	public CoutEnergieService getCoutEnergieService() {
		return coutEnergieService;
	}

	public void setCoutEnergieService(CoutEnergieService coutEnergieService) {
		this.coutEnergieService = coutEnergieService;
	}

	public HashMap<String, BigDecimal[]> getFacteurElasticiteExistant(String idAgreg,
			HashMap<Integer, CoutEnergie> coutEnergieMap, HashMap<String, Emissions> emissionsMap,
			ElasticiteMap elasticiteMap) {
		HashMap<String, BigDecimal[]> elasticiteSegment = new HashMap<String, BigDecimal[]>();
		BigDecimal evol = BigDecimal.ZERO;
		BigDecimal coutAnneeN = BigDecimal.ZERO;
		BigDecimal coutAnneeN1 = BigDecimal.ZERO;
		BigDecimal facteurEvol = BigDecimal.ZERO;

		for (Usage usage : Usage.values()) {
			if (usage.equals(Usage.AUTRES) || usage.equals(Usage.CHAUFFAGE) || usage.equals(Usage.CUISSON)
					|| usage.equals(Usage.ECS)) {
				for (Energies energie : Energies.values()) {
					if (!energie.equals(Energies.NON_CHAUFFE)) {
						BigDecimal[] tab = new BigDecimal[42];
						for (int annee = 2010; annee <= 2050; annee++) {
							// Cas des usages concurrentiels
							coutAnneeN1 = coutEnergieService.coutEnergie(coutEnergieMap, emissionsMap, annee,
									energie.getCode(), usage.getLabel(), FACTEUR_TVA);
							coutAnneeN = coutEnergieService.coutEnergie(coutEnergieMap, emissionsMap, annee - 1,
									energie.getCode(), usage.getLabel(), FACTEUR_TVA);
							evol = calcEvol(coutAnneeN1, coutAnneeN);
							facteurEvol = calcFacteurEvol(
									evol,
									elasticiteMap
											.getElasticite()
											.get(idAgreg.substring(START_ID_BRANCHE, START_ID_BRANCHE
													+ LENGTH_ID_BRANCHE)
													+ usage.getLabel() + energie.getCode())
											.getPeriode(correspPeriode(annee)));
							tab[annee - 2009] = facteurEvol;
							elasticiteSegment.put(usage.getLabel() + energie.getCode(), tab);

						}

					}
				}

			} else {

				BigDecimal[] tab = new BigDecimal[42];
				for (int annee = 2010; annee <= 2050; annee++) {
					// Cas des usages concurrentiels
					coutAnneeN1 = coutEnergieService.coutEnergie(coutEnergieMap, emissionsMap, annee,
							Energies.ELECTRICITE.getCode(), usage.getLabel(), FACTEUR_TVA);
					coutAnneeN = coutEnergieService.coutEnergie(coutEnergieMap, emissionsMap, annee - 1,
							Energies.ELECTRICITE.getCode(), usage.getLabel(), FACTEUR_TVA);
					evol = calcEvol(coutAnneeN1, coutAnneeN);
					facteurEvol = calcFacteurEvol(
							evol,
							elasticiteMap
									.getElasticite()
									.get(idAgreg.substring(START_ID_BRANCHE, START_ID_BRANCHE + LENGTH_ID_BRANCHE)
											+ usage.getLabel() + Energies.ELECTRICITE.getCode())
									.getPeriode(correspPeriode(annee)));
					tab[annee - 2009] = facteurEvol;
					elasticiteSegment.put(usage.getLabel() + Energies.ELECTRICITE.getCode(), tab);

				}
			}
		}

		return elasticiteSegment;
	}

	public HashMap<String, BigDecimal[]> getFacteurElasticiteNeuf(String idAgreg,
			HashMap<Integer, CoutEnergie> coutEnergieMap, HashMap<String, Emissions> emissionsMap,
			ElasticiteMap elasticiteMap) {
		HashMap<String, BigDecimal[]> elasticiteSegment = new HashMap<String, BigDecimal[]>();
		BigDecimal evol = BigDecimal.ZERO;
		BigDecimal coutAnneeN = BigDecimal.ZERO;
		BigDecimal coutAnneeN1 = BigDecimal.ZERO;
		BigDecimal facteurEvol = BigDecimal.ZERO;

		for (Usage usage : Usage.values()) {
			if (usage.equals(Usage.AUTRES) || usage.equals(Usage.CHAUFFAGE) || usage.equals(Usage.CUISSON)
					|| usage.equals(Usage.ECS)) {
				for (Energies energie : Energies.values()) {
					if (!energie.equals(Energies.NON_CHAUFFE)) {
						BigDecimal[] tab = new BigDecimal[42];
						for (int annee = 2010; annee <= 2050; annee++) {
							// Cas des usages concurrentiels
							coutAnneeN1 = coutEnergieService.coutEnergie(coutEnergieMap, emissionsMap, annee,
									energie.getCode(), usage.getLabel(), FACTEUR_TVA);
							coutAnneeN = coutEnergieService.coutEnergie(coutEnergieMap, emissionsMap, 2009,
									energie.getCode(), usage.getLabel(), FACTEUR_TVA);
							evol = calcEvol(coutAnneeN1, coutAnneeN);
							facteurEvol = calcFacteurEvol(
									evol,
									elasticiteMap
											.getElasticite()
											.get(idAgreg.substring(START_ID_BRANCHE, START_ID_BRANCHE
													+ LENGTH_ID_BRANCHE)
													+ usage.getLabel() + energie.getCode())
											.getPeriode(correspPeriode(annee)));
							tab[annee - 2009] = facteurEvol;
							elasticiteSegment.put(usage.getLabel() + energie.getCode(), tab);

						}

					}
				}

			} else {

				BigDecimal[] tab = new BigDecimal[42];
				for (int annee = 2010; annee <= 2050; annee++) {
					// Cas des usages concurrentiels
					coutAnneeN1 = coutEnergieService.coutEnergie(coutEnergieMap, emissionsMap, annee,
							Energies.ELECTRICITE.getCode(), usage.getLabel(), FACTEUR_TVA);
					coutAnneeN = coutEnergieService.coutEnergie(coutEnergieMap, emissionsMap, 2009,
							Energies.ELECTRICITE.getCode(), usage.getLabel(), FACTEUR_TVA);
					evol = calcEvol(coutAnneeN1, coutAnneeN);
					facteurEvol = calcFacteurEvol(
							evol,
							elasticiteMap
									.getElasticite()
									.get(idAgreg.substring(START_ID_BRANCHE, START_ID_BRANCHE + LENGTH_ID_BRANCHE)
											+ usage.getLabel() + Energies.ELECTRICITE.getCode())
									.getPeriode(correspPeriode(annee)));
					tab[annee - 2009] = facteurEvol;
					elasticiteSegment.put(usage.getLabel() + Energies.ELECTRICITE.getCode(), tab);

				}
			}
		}

		return elasticiteSegment;
	}

	protected BigDecimal calcFacteurEvol(BigDecimal evol, BigDecimal elasticite) {
		// facteur = exp(-0.2*ln(1+evol))
		// evol ne doit pas etre superieur ou egal a -1
		BigDecimal facteur = BigDecimal.ONE;
		if (evol.compareTo(new BigDecimal("-1")) > 0) {
			Double temp = (BigDecimal.ONE.add(evol)).doubleValue();
			Double elas = elasticite.doubleValue();
			facteur = BigDecimal.valueOf(Math.exp(elas * Math.log(temp))).setScale(3, BigDecimal.ROUND_HALF_UP);
		}

		return facteur;
	}

	protected BigDecimal calcEvol(BigDecimal coutAnneeN1, BigDecimal coutAnneeN) {

		BigDecimal result = BigDecimal.ZERO;
		if (coutAnneeN1 != null && coutAnneeN != null && coutAnneeN.signum() != 0) {
			// result = (coutAnneeN1/coutAnneeN)-1
			result = (coutAnneeN1.divide(coutAnneeN, MathContext.DECIMAL32)).subtract(BigDecimal.ONE);

		}

		return result;

	}

	public String codeCreateEnerg(String energPmKey) {
		Energies energies = Enum.valueOf(Energies.class, energPmKey);
		return energies.getCode();
	}

	public int correspPeriode(int annee) {

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

	public String correspPeriodeString(int annee) {

		String periodeStrg;
		if (annee > 2009 && annee < 2016) {
			periodeStrg = "PERIODE1";
		} else if (annee > 2015 && annee < 2021) {
			periodeStrg = "PERIODE2";
		} else if (annee > 2020 && annee < 2031) {
			periodeStrg = "PERIODE3";
		} else if (annee > 2030 && annee < 2041) {
			periodeStrg = "PERIODE4";
		} else {
			periodeStrg = "PERIODE5";
		}

		return periodeStrg;

	}

	public String correspPeriodeFin(int annee) {
		String periodeStrg;
		if (annee > 2009 && annee < 2016) {
			periodeStrg = "2010-2015";
		} else if (annee > 2015 && annee < 2021) {
			periodeStrg = "2015-2020";
		} else if (annee > 2020 && annee < 2031) {
			periodeStrg = "2020-2030";
		} else if (annee > 2030 && annee < 2041) {
			periodeStrg = "2030-2040";
		} else {
			periodeStrg = "2040-2050";
		}

		return periodeStrg;
	}

	public String concatID(Parc parcAgreg, String usage) {

		StringBuffer concatID = new StringBuffer();
		concatID.append(parcAgreg.getIdbranche());
		concatID.append(parcAgreg.getIdbattype());
		concatID.append(usage);

		return concatID.toString();

	}

	public int correspPeriodeCstr(Parc parcExistant, int annee) {
		int periodCstr = 0;
		if (parcExistant.getIdperiodesimple().equals(Period.PERIODE_BEFORE_1980.getCode())
				|| parcExistant.getIdperiodesimple().equals(Period.PERIODE_1981_1998.getCode())
				|| parcExistant.getIdperiodesimple().equals(Period.PERIODE_1999_2008.getCode())) {
			periodCstr = 0;
		} else if (parcExistant.getIdperiodesimple().equals(Period.PERIODE_2010_2015.getCode())) {
			periodCstr = 1;
		} else if (parcExistant.getIdperiodesimple().equals(Period.PERIODE_2016_2020.getCode())) {
			periodCstr = 2;
		} else if (parcExistant.getIdperiodesimple().equals(Period.PERIODE_2021_2030.getCode())) {
			periodCstr = 3;
		} else if (parcExistant.getIdperiodesimple().equals(Period.PERIODE_2031_2040.getCode())) {
			periodCstr = 4;
		} else if (parcExistant.getIdperiodesimple().equals(Period.PERIODE_2041_2050.getCode())) {
			periodCstr = 5;
		}
		return periodCstr;
	}

	public HashMap<String, Parc> aggregateParc(HashMap<String, Parc> parcTotMap, int anneeNTab) {

		HashMap<String, Parc> parcAgreg = new HashMap<String, Parc>();

		for (String mapKey : parcTotMap.keySet()) {

			Parc parcn = parcTotMap.get(mapKey);

			String cle = parcn.getIdagreg();

			if (parcAgreg.containsKey(cle)) {
				Parc temp = new Parc(2);
				temp = parcAgreg.get(cle);
				BigDecimal anneeN = BigDecimal.ZERO;
				BigDecimal anneeN1 = BigDecimal.ZERO;
				if (parcn.getAnnee(anneeNTab - 1) != null) {
					anneeN = parcn.getAnnee(anneeNTab - 1);
				}
				if (parcn.getAnnee(anneeNTab) != null) {
					anneeN1 = parcn.getAnnee(anneeNTab);
				}
				temp.setAnnee(0, temp.getAnnee(0).add(anneeN));
				temp.setAnnee(1, temp.getAnnee(1).add(anneeN1));
				parcAgreg.put(cle, new Parc(temp));

			} else {
				Parc temp = new Parc(2);
				BigDecimal anneeN = BigDecimal.ZERO;
				BigDecimal anneeN1 = BigDecimal.ZERO;
				if (parcn.getAnnee(anneeNTab - 1) != null) {
					anneeN = parcn.getAnnee(anneeNTab - 1);
				}
				if (parcn.getAnnee(anneeNTab) != null) {
					anneeN1 = parcn.getAnnee(anneeNTab);
				}
				temp.setId(cle);
				temp.setAnneeRenov(parcn.getAnneeRenov());
				temp.setTypeRenovBat(parcn.getTypeRenovBat());
				temp.setAnnee(0, anneeN);
				temp.setAnnee(1, anneeN1);
				parcAgreg.put(cle, new Parc(temp));
			}
		}
		return parcAgreg;
	}

	public HashMap<String, Parc> aggregateParcEcs(HashMap<String, Parc> parcTotMap, int anneeNTab,
			HashMap<String, ResultConsoURt> resultConsoURtMap, int pasdeTemps) {

		HashMap<String, Parc> parcAgreg = new HashMap<String, Parc>();
		// Etat initial par defaut, sauf si une renovation ENSBBC a ete menee
		for (String mapKey : parcTotMap.keySet()) {
			TypeRenovBati typeRenovBat = TypeRenovBati.ETAT_INIT;
			String anneeRenovBat = INIT_STATE;
			Parc parcn = parcTotMap.get(mapKey);
			if (parcn.getTypeRenovBat().equals(TypeRenovBati.ENSBBC)) {
				typeRenovBat = TypeRenovBati.ENSBBC;
				anneeRenovBat = parcn.getAnneeRenov();
			}

			String cle = parcn.getIdagreg() + anneeRenovBat + typeRenovBat;

			if (parcAgreg.containsKey(cle)) {
				Parc temp = new Parc(2);
				temp = parcAgreg.get(cle);
				BigDecimal anneeN = BigDecimal.ZERO;
				BigDecimal anneeN1 = BigDecimal.ZERO;
				if (parcn.getAnnee(anneeNTab - 1) != null) {
					anneeN = parcn.getAnnee(anneeNTab - 1);
				}
				if (parcn.getAnnee(anneeNTab) != null) {
					anneeN1 = parcn.getAnnee(anneeNTab);
				}
				temp.setAnnee(0, temp.getAnnee(0).add(anneeN));
				temp.setAnnee(1, temp.getAnnee(1).add(anneeN1));
				parcAgreg.put(cle, new Parc(temp));

			} else {
				Parc temp = new Parc(2);
				BigDecimal anneeN = BigDecimal.ZERO;
				BigDecimal anneeN1 = BigDecimal.ZERO;
				if (parcn.getAnnee(anneeNTab - 1) != null) {
					anneeN = parcn.getAnnee(anneeNTab - 1);
				}
				if (parcn.getAnnee(anneeNTab) != null) {
					anneeN1 = parcn.getAnnee(anneeNTab);
				}
				temp.setId(cle);
				temp.setAnneeRenov(anneeRenovBat);
				temp.setTypeRenovBat(typeRenovBat);
				temp.setAnnee(0, anneeN);
				temp.setAnnee(1, anneeN1);
				parcAgreg.put(cle, new Parc(temp));
			}
			// Rempli l'objet de retour des consoU d'ecs et d'eclairage
			if (resultConsoURtMap.containsKey(cle)) {
				ResultConsoURt resultInsert = resultConsoURtMap.get(cle);
				BigDecimal surfN = BigDecimal.ZERO;
				if (resultInsert.getSurfTot(anneeNTab) != null) {
					surfN = resultInsert.getSurfTot(anneeNTab);
				}
				resultInsert.setSurfTot(anneeNTab, surfN.add(parcn.getAnnee(anneeNTab)));
				resultConsoURtMap.put(resultInsert.getId(), resultInsert);
			} else {
				ResultConsoURt resultInsert = new ResultConsoURt(pasdeTemps);
				resultInsert.setId(cle);
				resultInsert.setTypeRenovBat(typeRenovBat);
				resultInsert.setAnneeRenov(anneeRenovBat);
				resultInsert.setSurfTot(anneeNTab, parcn.getAnnee(anneeNTab));
				resultConsoURtMap.put(resultInsert.getId(), resultInsert);
			}

		}
		return parcAgreg;
	}

	public HashMap<String, Parc> aggregateParcEclairage(HashMap<String, Parc> parcTotMap, int anneeNTab) {

		HashMap<String, Parc> parcAgreg = new HashMap<String, Parc>();
		// Etat initial par defaut, sauf si une renovation ENSBBC a ete menee
		for (String mapKey : parcTotMap.keySet()) {
			TypeRenovBati typeRenovBat = TypeRenovBati.ETAT_INIT;
			String anneeRenovBat = INIT_STATE;
			Parc parcn = parcTotMap.get(mapKey);
			if (parcn.getTypeRenovBat().equals(TypeRenovBati.ENSBBC)) {
				typeRenovBat = TypeRenovBati.ENSBBC;
				anneeRenovBat = parcn.getAnneeRenov();
			}

			String cle = parcn.getIdagreg() + anneeRenovBat + typeRenovBat;

			if (parcAgreg.containsKey(cle)) {
				Parc temp = new Parc(2);
				temp = parcAgreg.get(cle);
				BigDecimal anneeN = BigDecimal.ZERO;
				BigDecimal anneeN1 = BigDecimal.ZERO;
				if (parcn.getAnnee(anneeNTab - 1) != null) {
					anneeN = parcn.getAnnee(anneeNTab - 1);
				}
				if (parcn.getAnnee(anneeNTab) != null) {
					anneeN1 = parcn.getAnnee(anneeNTab);
				}
				temp.setAnnee(0, temp.getAnnee(0).add(anneeN));
				temp.setAnnee(1, temp.getAnnee(1).add(anneeN1));
				parcAgreg.put(cle, new Parc(temp));

			} else {
				Parc temp = new Parc(2);
				BigDecimal anneeN = BigDecimal.ZERO;
				BigDecimal anneeN1 = BigDecimal.ZERO;
				if (parcn.getAnnee(anneeNTab - 1) != null) {
					anneeN = parcn.getAnnee(anneeNTab - 1);
				}
				if (parcn.getAnnee(anneeNTab) != null) {
					anneeN1 = parcn.getAnnee(anneeNTab);
				}
				temp.setId(cle);
				temp.setAnneeRenov(anneeRenovBat);
				temp.setTypeRenovBat(typeRenovBat);
				temp.setAnnee(0, anneeN);
				temp.setAnnee(1, anneeN1);
				parcAgreg.put(cle, new Parc(temp));
			}
		}
		return parcAgreg;
	}

	public HashMap<String, Conso> aggregateBesoinEclairage(HashMap<String, Conso> besoinTotMap, int anneeNTab) {

		HashMap<String, Conso> besoinAgreg = new HashMap<String, Conso>();
		// Etat initial par defaut, sauf si une renovation ENSBBC a ete menee
		for (String mapKey : besoinTotMap.keySet()) {
			TypeRenovBati typeRenovBat = TypeRenovBati.ETAT_INIT;
			String anneeRenovBat = INIT_STATE;
			Conso besoinN = besoinTotMap.get(mapKey);
			if (besoinN.getTypeRenovBat().equals(TypeRenovBati.ENSBBC)) {
				typeRenovBat = TypeRenovBati.ENSBBC;
				anneeRenovBat = besoinN.getAnneeRenov();
			}

			String cle = besoinN.getIdagreg() + anneeRenovBat + typeRenovBat;

			if (besoinAgreg.containsKey(cle)) {
				Conso temp = new Conso(2);
				temp = besoinAgreg.get(cle);
				BigDecimal anneeN = BigDecimal.ZERO;
				BigDecimal anneeN1 = BigDecimal.ZERO;
				if (besoinN.getAnnee(anneeNTab - 1) != null) {
					anneeN = besoinN.getAnnee(anneeNTab - 1);
				}
				if (besoinN.getAnnee(anneeNTab) != null) {
					anneeN1 = besoinN.getAnnee(anneeNTab);
				}
				temp.setAnnee(0, temp.getAnnee(0).add(anneeN));
				temp.setAnnee(1, temp.getAnnee(1).add(anneeN1));
				besoinAgreg.put(cle, new Conso(temp));

			} else {
				Conso temp = new Conso(2);
				BigDecimal anneeN = BigDecimal.ZERO;
				BigDecimal anneeN1 = BigDecimal.ZERO;
				if (besoinN.getAnnee(anneeNTab - 1) != null) {
					anneeN = besoinN.getAnnee(anneeNTab - 1);
				}
				if (besoinN.getAnnee(anneeNTab) != null) {
					anneeN1 = besoinN.getAnnee(anneeNTab);
				}
				temp.setId(cle);
				temp.setAnneeRenov(anneeRenovBat);
				temp.setTypeRenovBat(typeRenovBat);
				temp.setAnnee(0, anneeN);
				temp.setAnnee(1, anneeN1);
				besoinAgreg.put(cle, new Conso(temp));
			}
		}
		return besoinAgreg;
	}

	public HashMap<String, Parc> aggregateParcRehab(HashMap<String, Parc> parcTotMap, int anneeNTab) {

		HashMap<String, Parc> parcAgreg = new HashMap<String, Parc>();

		for (String mapKey : parcTotMap.keySet()) {

			Parc parcn = parcTotMap.get(mapKey);

			String cle = parcn.getIdagreg() + Energies.ELECTRICITE.getCode() + parcn.getAnneeRenov()
					+ parcn.getTypeRenovBat();

			if (parcAgreg.containsKey(cle)) {
				Parc temp = new Parc(2);
				temp = parcAgreg.get(cle);
				BigDecimal anneeN = BigDecimal.ZERO;
				BigDecimal anneeN1 = BigDecimal.ZERO;
				if (parcn.getAnnee(anneeNTab - 1) != null) {
					anneeN = parcn.getAnnee(anneeNTab - 1);
				}
				if (parcn.getAnnee(anneeNTab) != null) {
					anneeN1 = parcn.getAnnee(anneeNTab);
				}
				temp.setAnnee(0, temp.getAnnee(0).add(anneeN));
				temp.setAnnee(1, temp.getAnnee(1).add(anneeN1));
				parcAgreg.put(cle, new Parc(temp));

			} else {
				Parc temp = new Parc(2);
				BigDecimal anneeN = BigDecimal.ZERO;
				BigDecimal anneeN1 = BigDecimal.ZERO;
				if (parcn.getAnnee(anneeNTab - 1) != null) {
					anneeN = parcn.getAnnee(anneeNTab - 1);
				}
				if (parcn.getAnnee(anneeNTab) != null) {
					anneeN1 = parcn.getAnnee(anneeNTab);
				}
				temp.setId(cle);
				temp.setAnneeRenov(parcn.getAnneeRenov());
				temp.setTypeRenovBat(parcn.getTypeRenovBat());
				temp.setAnnee(0, anneeN);
				temp.setAnnee(1, anneeN1);
				parcAgreg.put(cle, new Parc(temp));
			}
		}
		return parcAgreg;
	}

	public HashMap<String, Conso> aggregateConsoEcs(HashMap<String, Conso> besoinMap, int anneeNTab) {

		HashMap<String, Conso> parcAgreg = new HashMap<String, Conso>();

		for (String mapKey : besoinMap.keySet()) {

			TypeRenovBati typeRenovBat = TypeRenovBati.ETAT_INIT;
			String anneeRenovBat = INIT_STATE;
			Conso parcn = besoinMap.get(mapKey);
			if (parcn.getTypeRenovBat().equals(TypeRenovBati.ENSBBC)) {
				typeRenovBat = TypeRenovBati.ENSBBC;
				anneeRenovBat = parcn.getAnneeRenov();
			}

			String cle = parcn.getIdagreg() + anneeRenovBat + typeRenovBat;

			if (parcAgreg.containsKey(cle)) {
				Conso temp = new Conso(2);
				temp = parcAgreg.get(cle);
				BigDecimal anneeN = BigDecimal.ZERO;
				BigDecimal anneeN1 = BigDecimal.ZERO;
				if (parcn.getAnnee(anneeNTab - 1) != null) {
					anneeN = parcn.getAnnee(anneeNTab - 1);
				}
				if (parcn.getAnnee(anneeNTab) != null) {
					anneeN1 = parcn.getAnnee(anneeNTab);
				}
				temp.setAnnee(0, temp.getAnnee(0).add(anneeN));
				temp.setAnnee(1, temp.getAnnee(1).add(anneeN1));
				parcAgreg.put(cle, new Conso(temp));

			} else {
				Conso temp = new Conso(2);
				BigDecimal anneeN = BigDecimal.ZERO;
				BigDecimal anneeN1 = BigDecimal.ZERO;
				if (parcn.getAnnee(anneeNTab - 1) != null) {
					anneeN = parcn.getAnnee(anneeNTab - 1);
				}
				if (parcn.getAnnee(anneeNTab) != null) {
					anneeN1 = parcn.getAnnee(anneeNTab);
				}
				temp.setId(cle);
				temp.setAnneeRenovSys(parcn.getAnneeRenovSys());
				temp.setTypeRenovSys(parcn.getTypeRenovSys());
				temp.setAnnee(0, anneeN);
				temp.setAnnee(1, anneeN1);
				parcAgreg.put(cle, new Conso(temp));
			}
		}
		return parcAgreg;
	}

	public HashMap<String, List<String>> idAgregList(HashMap<String, Conso> besoinMap,
			HashMap<String, Parc> parcTotMapAgreg) {

		HashMap<String, List<String>> keyMap = new HashMap<String, List<String>>();
		List<String> listKey = new ArrayList<String>();
		for (String parcKey : parcTotMapAgreg.keySet()) {
			listKey = new ArrayList<String>();
			for (String besoinKey : besoinMap.keySet()) {
				Conso besoin = besoinMap.get(besoinKey);
				String cleCompare = besoin.getIdagreg() + besoin.getAnneeRenov() + besoin.getTypeRenovBat();
				if (cleCompare.equals(parcKey)) {

					listKey.add(besoinKey);
				}

			}
			keyMap.put(parcKey, listKey);

		}
		return keyMap;

	}

	public HashMap<String, List<String>> idAgregBoucleList(List<String> listeId) {

		HashMap<String, List<String>> keyMap = new HashMap<String, List<String>>();

		for (String idParc : listeId) {
			String idAgreg = idParc.substring(START_AGREG, LENGTH_AGREG + START_AGREG);
			if (keyMap.containsKey(idAgreg)) {

				List<String> listTemp = keyMap.get(idAgreg);
				listTemp.add(idParc);
				keyMap.put(idAgreg, listTemp);

			} else {

				List<String> listTemp = new ArrayList<String>();
				listTemp.add(idParc);
				keyMap.put(idAgreg, listTemp);
			}

		}
		return keyMap;

	}

	// methode pour avoir la somme d'une serie geometrique de raison "facteur"
	public BigDecimal serieGeometrique(BigDecimal multiplicatif, BigDecimal facteur, int duree) {
		return multiplicatif.multiply(
				BigDecimal.ONE.subtract(facteur.pow(duree + 1, MathContext.DECIMAL32), MathContext.DECIMAL32),
				MathContext.DECIMAL32).divide(BigDecimal.ONE.subtract(facteur, MathContext.DECIMAL32),
				MathContext.DECIMAL32);

	}

	public ResultConsoRdt agregateResultECS(final ResultConsoRdt resultatsConso, int pasdeTemps, String usage) {
		int lenght_id = 0;
		lenght_id = LENGTH_ID_ECS;

		ResultConsoRdt resultConsoTemp = new ResultConsoRdt();
		HashMap<String, Conso> resultatsAgreg = new HashMap<String, Conso>();
		for (String typeResult : resultatsConso.keySet()) {
			resultatsAgreg = new HashMap<String, Conso>();
			if (!typeResult.equals(MapResultsKeys.RDT_ECS.getLabel())) {
				HashMap<String, Conso> resultMap = resultatsConso.getMap(typeResult);
				for (String idEcs : resultMap.keySet()) {
					String id = idEcs.substring(START_ID, lenght_id + START_ID);
					if (resultatsAgreg.containsKey(id)) {
						Conso parcAgreg = resultatsAgreg.get(id);
						Conso parcResult = resultMap.get(idEcs);
						BigDecimal[] arrayResult = parcAgreg.getArray();

						for (int i = 0; i <= pasdeTemps; i++) {
							BigDecimal temp1 = BigDecimal.ZERO;
							BigDecimal temp2 = BigDecimal.ZERO;
							if (arrayResult[i] != null) {
								temp1 = arrayResult[i];
							}
							if (parcResult.getAnnee(i) != null) {
								temp2 = parcResult.getAnnee(i);
							}

							parcAgreg.setAnnee(i, temp1.add(temp2));

						}
						resultatsAgreg.put(id, parcAgreg);
					} else {
						Conso parcResult = resultMap.get(idEcs);
						resultatsAgreg.put(id, new Conso(parcResult));
					}

				}
			}
			resultConsoTemp.put(typeResult, resultatsAgreg);
		}
		return resultConsoTemp;
	}

	public ResultConsoRt agregateResultRtClim(final ResultConsoRt resultatsConsoRt, int pasdeTemps, String usage) {
		int lenght_id = 0;
		lenght_id = LENGTH_ID_CLIM;

		ResultConsoRt resultConsoTemp = new ResultConsoRt();
		HashMap<String, Conso> resultatsAgreg = new HashMap<String, Conso>();
		for (String typeResult : resultatsConsoRt.keySet()) {
			if (typeResult.equals(MapResultsKeys.BESOIN_CLIM.getLabel())
					|| typeResult.equals(MapResultsKeys.CONSO_CLIM.getLabel())
					|| typeResult.equals(MapResultsKeys.COUT_CLIM.getLabel())) {
				resultatsAgreg = new HashMap<String, Conso>();

				HashMap<String, Conso> resultMap = resultatsConsoRt.getMap(typeResult);
				for (String idEcs : resultMap.keySet()) {
					String id = idEcs.substring(START_ID, lenght_id + START_ID);
					if (resultatsAgreg.containsKey(id)) {
						Conso parcAgreg = resultatsAgreg.get(id);
						Conso parcResult = resultMap.get(idEcs);
						BigDecimal[] arrayResult = parcAgreg.getArray();

						for (int i = 0; i <= pasdeTemps; i++) {
							BigDecimal temp1 = BigDecimal.ZERO;
							BigDecimal temp2 = BigDecimal.ZERO;
							if (arrayResult[i] != null) {
								temp1 = arrayResult[i];
							}
							if (parcResult.getAnnee(i) != null) {
								temp2 = parcResult.getAnnee(i);
							}

							parcAgreg.setAnnee(i, temp1.add(temp2));

						}
						resultatsAgreg.put(id, parcAgreg);
					} else {
						Conso parcResult = resultMap.get(idEcs);
						resultatsAgreg.put(id, new Conso(parcResult));
					}

				}
				resultConsoTemp.put(typeResult, resultatsAgreg);
			}
		}
		return resultConsoTemp;
	}

	// Agrege les resultats du parc
	public ResultParc agregateResultParc(final ResultParc resultatsParc, int pasdeTemps) {

		ResultParc resultParcTemp = new ResultParc();
		HashMap<String, Parc> resultatsAgreg = new HashMap<String, Parc>();
		for (String typeResult : resultatsParc.keySet()) {
			resultatsAgreg = new HashMap<String, Parc>();

			HashMap<String, Parc> resultMap = resultatsParc.getMap(typeResult);
			for (String idParc : resultMap.keySet()) {
				String id = idParc.substring(START_ID_PARC, LENGTH_ID_PARC + START_ID_PARC);
				if (resultatsAgreg.containsKey(id)) {
					Parc parcAgreg = resultatsAgreg.get(id);
					Parc parcResult = resultMap.get(idParc);
					BigDecimal[] arrayResult = parcAgreg.getArray();

					for (int i = 0; i <= pasdeTemps; i++) {
						BigDecimal temp1 = BigDecimal.ZERO;
						BigDecimal temp2 = BigDecimal.ZERO;
						if (arrayResult[i] != null) {
							temp1 = arrayResult[i];
						}
						if (parcResult.getAnnee(i) != null) {
							temp2 = parcResult.getAnnee(i);
						}

						parcAgreg.setAnnee(i, temp1.add(temp2));

					}
					resultatsAgreg.put(id, parcAgreg);
				} else {
					Parc parcResult = resultMap.get(idParc);
					resultatsAgreg.put(id, new Parc(parcResult));
				}

			}

			resultParcTemp.put(typeResult, resultatsAgreg);
		}
		return resultParcTemp;
	}

	public ResultConsoRt agregateResultRt(final ResultConsoRt resultatsConso, int pasdeTemps) {

		ResultConsoRt resultConsoTemp = new ResultConsoRt();
		HashMap<String, Conso> resultatsAgreg = new HashMap<String, Conso>();
		// Boucle sur les usages contenus dans la map
		for (String usage : resultatsConso.keySet()) {
			int lenght_id;
			if (usage.equals(MapResultsKeys.VENTILATION.getLabel())
					|| usage.equals(MapResultsKeys.BESOIN_CHAUFF.getLabel())
					|| usage.equals(MapResultsKeys.CONSO_CHAUFF.getLabel())
					|| usage.equals(MapResultsKeys.RDT_CHAUFF.getLabel())
					|| usage.equals(MapResultsKeys.AUXILIAIRES.getLabel())) {
				lenght_id = LENGTH_ID_TOT;
			} else {
				lenght_id = LENGTH_ID_RT;
			}
			if (!usage.equals(MapResultsKeys.BESOIN_CLIM.getLabel())
					|| !usage.equals(MapResultsKeys.CONSO_CLIM.getLabel())
					|| !usage.equals(MapResultsKeys.RDT_CLIM.getLabel())) {
				resultatsAgreg = new HashMap<String, Conso>();

				HashMap<String, Conso> resultMap = resultatsConso.getMap(usage);
				for (String idUsage : resultMap.keySet()) {
					String id = idUsage.substring(START_ID, lenght_id + START_ID);
					if (resultatsAgreg.containsKey(id)) {
						Conso parcAgreg = resultatsAgreg.get(id);
						Conso parcResult = resultMap.get(idUsage);
						BigDecimal[] arrayResult = parcAgreg.getArray();

						for (int i = 0; i <= pasdeTemps; i++) {
							BigDecimal temp1 = BigDecimal.ZERO;
							BigDecimal temp2 = BigDecimal.ZERO;
							if (arrayResult[i] != null) {
								temp1 = arrayResult[i];
							}
							if (parcResult.getAnnee(i) != null) {
								temp2 = parcResult.getAnnee(i);
							}

							parcAgreg.setAnnee(i, temp1.add(temp2));

						}
						resultatsAgreg.put(id, parcAgreg);
					} else {
						Conso parcResult = resultMap.get(idUsage);
						resultatsAgreg.put(id, new Conso(parcResult));
					}

				}
				resultConsoTemp.put(usage, resultatsAgreg);
			}
		}
		return resultConsoTemp;
	}

	// Genere les id pour les hashMap de resultats (ecs/climatisation/eclairage)
	public String generateIdMapResultRt(Conso besoin) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(besoin.getId());
		buffer.append(besoin.getAnneeRenovSys());
		buffer.append(besoin.getTypeRenovSys());
		buffer.append(besoin.getAnneeRenov());
		buffer.append(besoin.getTypeRenovBat());

		return buffer.toString();
	}

	public HashMap<String, Parc> aggregateParcConsoU(HashMap<String, ResultConsoUClim> resultConsoUClimMap,
			HashMap<String, Parc> parcTotMap, int anneeNTab, int pasdeTemps) {

		HashMap<String, Parc> parcAgreg = new HashMap<String, Parc>();

		for (String mapKey : parcTotMap.keySet()) {
			TypeRenovBati typeRenovBat = TypeRenovBati.ETAT_INIT;
			String anneeRenovBat = INIT_STATE;
			Parc parcn = parcTotMap.get(mapKey);
			// if (parcn.getTypeRenovBat().equals(TypeRenovBati.ENSBBC)) {
			// typeRenovBat = TypeRenovBati.ENSBBC;
			// anneeRenovBat = parcn.getAnneeRenov();
			// }

			// prise en compte de l'id sys froid par rapport a l'id agreg
			String cle = parcn.getIdagreg() + parcn.getIdsysfroid() + anneeRenovBat + typeRenovBat;

			if (parcAgreg.containsKey(cle)) {
				Parc temp = new Parc(2);
				temp = parcAgreg.get(cle);
				BigDecimal anneeN = BigDecimal.ZERO;
				BigDecimal anneeN1 = BigDecimal.ZERO;
				if (parcn.getAnnee(anneeNTab - 1) != null) {
					anneeN = parcn.getAnnee(anneeNTab - 1);
				}
				if (parcn.getAnnee(anneeNTab) != null) {
					anneeN1 = parcn.getAnnee(anneeNTab);
				}
				temp.setAnnee(0, temp.getAnnee(0).add(anneeN));
				temp.setAnnee(1, temp.getAnnee(1).add(anneeN1));
				parcAgreg.put(cle, new Parc(temp));

			} else {
				Parc temp = new Parc(2);
				BigDecimal anneeN = BigDecimal.ZERO;
				BigDecimal anneeN1 = BigDecimal.ZERO;
				if (parcn.getAnnee(anneeNTab - 1) != null) {
					anneeN = parcn.getAnnee(anneeNTab - 1);
				}
				if (parcn.getAnnee(anneeNTab) != null) {
					anneeN1 = parcn.getAnnee(anneeNTab);
				}
				temp.setId(cle);
				temp.setAnneeRenov(parcn.getAnneeRenov());
				temp.setTypeRenovBat(parcn.getTypeRenovBat());
				temp.setAnnee(0, anneeN);
				temp.setAnnee(1, anneeN1);
				parcAgreg.put(cle, new Parc(temp));
			}

			// Rempli l'objet de retour des consoU d'ecs et d'eclairage
			if (resultConsoUClimMap.containsKey(cle)) {
				ResultConsoUClim resultInsert = resultConsoUClimMap.get(cle);
				BigDecimal surfN = BigDecimal.ZERO;
				if (resultInsert.getSurfTot(anneeNTab) != null) {
					surfN = resultInsert.getSurfTot(anneeNTab);
				}
				resultInsert.setSurfTot(anneeNTab, surfN.add(parcn.getAnnee(anneeNTab)));
				resultConsoUClimMap.put(resultInsert.getId(), resultInsert);
			} else {
				ResultConsoUClim resultInsert = new ResultConsoUClim(pasdeTemps);
				resultInsert.setId(cle);
				resultInsert.setSurfTot(anneeNTab, parcn.getAnnee(anneeNTab));
				resultConsoUClimMap.put(resultInsert.getId(), resultInsert);
			}
		}
		return parcAgreg;
	}

	public HashMap<String, Conso> aggregateBesoinClim(HashMap<String, Conso> besoinMap, int anneeNTab, int pasdeTemps) {

		HashMap<String, Conso> besoinAgreg = new HashMap<String, Conso>();

		for (String mapKey : besoinMap.keySet()) {
			TypeRenovBati typeRenovBat = TypeRenovBati.ETAT_INIT;
			String anneeRenovBat = INIT_STATE;
			Conso besoinN = besoinMap.get(mapKey);

			// prise en compte de l'id sys froid par rapport a l'id agreg
			String cle = besoinN.getIdagreg()
					+ besoinN.getId().substring(START_SYS_FROID, START_SYS_FROID + LENGTH_SYS_FROID) + anneeRenovBat
					+ typeRenovBat;

			if (besoinAgreg.containsKey(cle)) {
				Conso temp = new Conso(2);
				temp = besoinAgreg.get(cle);
				BigDecimal anneeN = BigDecimal.ZERO;
				BigDecimal anneeN1 = BigDecimal.ZERO;
				if (besoinN.getAnnee(anneeNTab - 1) != null) {
					anneeN = besoinN.getAnnee(anneeNTab - 1);
				}
				if (besoinN.getAnnee(anneeNTab) != null) {
					anneeN1 = besoinN.getAnnee(anneeNTab);
				}
				temp.setAnnee(0, temp.getAnnee(0).add(anneeN));
				temp.setAnnee(1, temp.getAnnee(1).add(anneeN1));
				besoinAgreg.put(cle, new Conso(temp));

			} else {
				Conso temp = new Conso(2);
				BigDecimal anneeN = BigDecimal.ZERO;
				BigDecimal anneeN1 = BigDecimal.ZERO;
				if (besoinN.getAnnee(anneeNTab - 1) != null) {
					anneeN = besoinN.getAnnee(anneeNTab - 1);
				}
				if (besoinN.getAnnee(anneeNTab) != null) {
					anneeN1 = besoinN.getAnnee(anneeNTab);
				}
				temp.setId(cle);
				temp.setAnneeRenov(besoinN.getAnneeRenov());
				temp.setTypeRenovBat(besoinN.getTypeRenovBat());
				temp.setAnnee(0, anneeN);
				temp.setAnnee(1, anneeN1);
				besoinAgreg.put(cle, new Conso(temp));
			}
		}
		return besoinAgreg;
	}
}
