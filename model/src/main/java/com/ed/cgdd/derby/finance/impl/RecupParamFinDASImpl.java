package com.ed.cgdd.derby.finance.impl;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.ed.cgdd.derby.common.CommonService;
import com.ed.cgdd.derby.finance.RecupParamFinDAS;
import com.ed.cgdd.derby.model.financeObjects.CEE;
import com.ed.cgdd.derby.model.financeObjects.CoutEnergie;
import com.ed.cgdd.derby.model.financeObjects.DecretTravaux;
import com.ed.cgdd.derby.model.financeObjects.Elasticite;
import com.ed.cgdd.derby.model.financeObjects.ElasticiteMap;
import com.ed.cgdd.derby.model.financeObjects.Emissions;
import com.ed.cgdd.derby.model.financeObjects.EvolValeurVerte;
import com.ed.cgdd.derby.model.financeObjects.EvolutionCout;
import com.ed.cgdd.derby.model.financeObjects.Exigence;
import com.ed.cgdd.derby.model.financeObjects.Financement;
import com.ed.cgdd.derby.model.financeObjects.Geste;
import com.ed.cgdd.derby.model.financeObjects.GesteObligation;
import com.ed.cgdd.derby.model.financeObjects.Maintenance;
import com.ed.cgdd.derby.model.financeObjects.ObligationTravauxExig;
import com.ed.cgdd.derby.model.financeObjects.ObligationTravauxSurf;
import com.ed.cgdd.derby.model.financeObjects.PBC;
import com.ed.cgdd.derby.model.financeObjects.PretBonif;
import com.ed.cgdd.derby.model.financeObjects.Reglementations;
import com.ed.cgdd.derby.model.financeObjects.RepartStatutOccup;
import com.ed.cgdd.derby.model.financeObjects.RtExistant;
import com.ed.cgdd.derby.model.financeObjects.SurfMoy;
import com.ed.cgdd.derby.model.financeObjects.TauxInteret;
import com.ed.cgdd.derby.model.parc.TypeRenovBati;

public class RecupParamFinDASImpl extends BddDAS implements RecupParamFinDAS {
	private final static Logger LOG = LogManager.getLogger(RecupParamFinDASImpl.class);

	private JdbcTemplate jdbcTemplate;
	private CommonService commonService;
	private static final String LOAD_DATA = "_LOAD";
	private static final int START_EXIG_NUM = 0;
	private static final int LENGTH_EXIG_NUM = 1;

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public CommonService getCommonService() {
		return commonService;
	}

	public void setCommonService(CommonService commonService) {
		this.commonService = commonService;
	}

	// Renvoie les parametres du decret de la loi Grenelle
	@Override
	public Reglementations recupDecret(String tableName, Reglementations reglementation) {

		List<DecretTravaux> loadParam = getDecret(tableName);
		for (DecretTravaux decret : loadParam) {
			reglementation.putDecret(decret);
		}

		return reglementation;
	}

	// Charge les parametres du decret de la loi Grenelle
	protected List<DecretTravaux> getDecret(String tableName) {

		String key = tableName + LOAD_DATA;

		String request = getProperty(key);

		return jdbcTemplate.query(request, new RowMapper<DecretTravaux>() {
			@Override
			public DecretTravaux mapRow(ResultSet rs, int rowNum) throws SQLException {

				DecretTravaux decret = new DecretTravaux();
				int columncount = rs.getMetaData().getColumnCount();

				for (int i = 1; i <= columncount; i++) {

					decret.setBranche(rs.getString("ID_BRANCHE"));
					decret.setSecteur(rs.getString("ID_OCCUPANT"));
					decret.setTri(rs.getBigDecimal("TRI"));
					decret.setPartSurf(rs.getBigDecimal("PART_SURF"));
					decret.setGainMin(rs.getBigDecimal("GAIN_MIN"));
					decret.setCoutMax(rs.getBigDecimal("COUT_MAX"));
					decret.setDebut(rs.getBigDecimal("DEBUT"));
					decret.setFin(rs.getBigDecimal("FIN"));
				}
				return decret;

			}

		});
	}

	// Renvoie les parametres d'elasticite prix
	@Override
	public ElasticiteMap elasticite(String tableName, ElasticiteMap elasticiteMap) {

		List<Elasticite> loadParam = getElasticite(tableName);
		for (Elasticite param : loadParam) {
			elasticiteMap.putElasticite(param);
		}

		return elasticiteMap;
	}

	// Charge les parametres d'elasticite prix
	protected List<Elasticite> getElasticite(String tableName) {

		String key = tableName + LOAD_DATA;

		String request = getProperty(key);

		return jdbcTemplate.query(request, new RowMapper<Elasticite>() {
			@Override
			public Elasticite mapRow(ResultSet rs, int rowNum) throws SQLException {

				Elasticite elasticite = new Elasticite();
				int columncount = rs.getMetaData().getColumnCount();

				for (int i = 1; i <= columncount; i++) {

					elasticite.setIdBranche(rs.getString("ID_BRANCHE"));
					elasticite.setUsage(rs.getString("USAGE"));
					elasticite.setIdEnergie(rs.getString("ID_ENERGIE"));
					elasticite.setPeriode(1, rs.getBigDecimal("PERIODE1"));
					elasticite.setPeriode(2, rs.getBigDecimal("PERIODE2"));
					elasticite.setPeriode(3, rs.getBigDecimal("PERIODE3"));
					elasticite.setPeriode(4, rs.getBigDecimal("PERIODE4"));
					elasticite.setPeriode(5, rs.getBigDecimal("PERIODE5"));
				}
				return elasticite;

			}

		});
	}

	// Renvoie les champs de la table de la RT existant

	public Reglementations recupRtExistant(String tableName, Reglementations reglementation) {

		List<RtExistant> loadParam = getRtExist(tableName);
		for (RtExistant rt : loadParam) {
			reglementation.putRt(rt);
		}

		return reglementation;
	}

	// Charge les niveaux d'exigence de la RT existant
	protected List<RtExistant> getRtExist(String tableName) {

		String key = tableName + LOAD_DATA;

		String request = getProperty(key);

		return jdbcTemplate.query(request, new RowMapper<RtExistant>() {
			@Override
			public RtExistant mapRow(ResultSet rs, int rowNum) throws SQLException {

				RtExistant rtExistant = new RtExistant();
				int columncount = rs.getMetaData().getColumnCount();

				for (int i = 1; i <= columncount; i++) {

					rtExistant.setPeriode(rs.getString("PERIODE"));
					rtExistant.setExigence(rs.getString("PERFORMANCE"));

				}
				return rtExistant;

			}

		});
	}

	// Renvoie les champs de la table des de l'obligation de travaux (surfaces)

	public Reglementations recupObligSurf(String tableName, Reglementations reglementation) {

		List<ObligationTravauxSurf> loadParam = getObligSurf(tableName);
		for (ObligationTravauxSurf oblig : loadParam) {
			reglementation.putOblSurf(oblig);
		}

		return reglementation;
	}

	// Charge les surfaces touchees par les obligations de travaux
	protected List<ObligationTravauxSurf> getObligSurf(String tableName) {

		String key = tableName + LOAD_DATA;

		String request = getProperty(key);

		return jdbcTemplate.query(request, new RowMapper<ObligationTravauxSurf>() {
			@Override
			public ObligationTravauxSurf mapRow(ResultSet rs, int rowNum) throws SQLException {

				ObligationTravauxSurf obligSurf = new ObligationTravauxSurf();
				int columncount = rs.getMetaData().getColumnCount();

				for (int i = 1; i <= columncount; i++) {

					obligSurf.setSecteur(rs.getString("ID_OCCUPANT"));
					obligSurf.setPartSurf(1, rs.getBigDecimal("PERIODE1"));
					obligSurf.setPartSurf(2, rs.getBigDecimal("PERIODE2"));
					obligSurf.setPartSurf(3, rs.getBigDecimal("PERIODE3"));
					obligSurf.setPartSurf(4, rs.getBigDecimal("PERIODE4"));
					obligSurf.setPartSurf(5, rs.getBigDecimal("PERIODE5"));

				}
				return obligSurf;

			}

		});
	}

	// Renvoie les champs de la table des de l'obligation de travaux (exigence)

	public Reglementations recupObligExig(String tableName, Reglementations reglementation) {

		List<ObligationTravauxExig> loadParam = getObligExig(tableName);
		for (ObligationTravauxExig oblig : loadParam) {
			reglementation.putOblExig(oblig);
		}

		return reglementation;
	}

	// Charge les surfaces touchees par les obligations de travaux
	protected List<ObligationTravauxExig> getObligExig(String tableName) {

		String key = tableName + LOAD_DATA;

		String request = getProperty(key);

		return jdbcTemplate.query(request, new RowMapper<ObligationTravauxExig>() {
			@Override
			public ObligationTravauxExig mapRow(ResultSet rs, int rowNum) throws SQLException {

				ObligationTravauxExig obligExig = new ObligationTravauxExig();
				int columncount = rs.getMetaData().getColumnCount();

				for (int i = 1; i <= columncount; i++) {

					obligExig.setSecteur(rs.getString("ID_OCCUPANT"));
					obligExig.setExigence(
							1,
							GesteObligation.getEnumName(rs.getString("PERIODE1").substring(START_EXIG_NUM,
									START_EXIG_NUM + LENGTH_EXIG_NUM)));
					obligExig.setExigence(
							2,
							GesteObligation.getEnumName(rs.getString("PERIODE2").substring(START_EXIG_NUM,
									START_EXIG_NUM + LENGTH_EXIG_NUM)));
					obligExig.setExigence(
							3,
							GesteObligation.getEnumName(rs.getString("PERIODE3").substring(START_EXIG_NUM,
									START_EXIG_NUM + LENGTH_EXIG_NUM)));
					obligExig.setExigence(
							4,
							GesteObligation.getEnumName(rs.getString("PERIODE4").substring(START_EXIG_NUM,
									START_EXIG_NUM + LENGTH_EXIG_NUM)));
					obligExig.setExigence(
							5,
							GesteObligation.getEnumName(rs.getString("PERIODE5").substring(START_EXIG_NUM,
									START_EXIG_NUM + LENGTH_EXIG_NUM)));

				}
				return obligExig;

			}

		});
	}

	// Renvoie les champs de la table des gains atteignables pour les usages non
	// reglementes
	public HashMap<String, Emissions> recupEmissions(String tableName) {

		HashMap<String, Emissions> emissionsMap = new HashMap<String, Emissions>();
		List<Emissions> loadParam = getEmissions(tableName);
		for (Emissions emissions : loadParam) {
			emissionsMap.put(emissions.getIdEnergie() + emissions.getUsage(), emissions);
		}

		return emissionsMap;
	}

	// Charge les gains atteignables sur les usages non reglementes
	protected List<Emissions> getEmissions(String tableName) {

		String key = tableName + LOAD_DATA;

		String request = getProperty(key);

		return jdbcTemplate.query(request, new RowMapper<Emissions>() {
			@Override
			public Emissions mapRow(ResultSet rs, int rowNum) throws SQLException {

				Emissions emissions = new Emissions();
				int columncount = rs.getMetaData().getColumnCount();

				for (int i = 1; i <= columncount; i++) {

					emissions.setIdEnergie(rs.getString("ID_ENERGIE"));
					emissions.setUsage(rs.getString("USAGE"));
					emissions.setPeriode(1, rs.getBigDecimal("PERIODE1"));
					emissions.setPeriode(2, rs.getBigDecimal("PERIODE2"));
					emissions.setPeriode(3, rs.getBigDecimal("PERIODE3"));
					emissions.setPeriode(4, rs.getBigDecimal("PERIODE4"));
					emissions.setPeriode(5, rs.getBigDecimal("PERIODE5"));

				}
				return emissions;

			}

		});
	}

	// requete pour les prix de l'energie : on stocke toutes les donnees dans un
	// objet tout au long du deroulement du programme
	public HashMap<Integer, CoutEnergie> recupCoutEnergie(String tableName) {
		HashMap<Integer, CoutEnergie> coutEnergMap = new HashMap<Integer, CoutEnergie>();
		List<CoutEnergie> loadParam = getCoutEnergie(tableName);
		for (CoutEnergie cout : loadParam) {
			coutEnergMap.put(cout.getAnnee(), cout);
		}
		return coutEnergMap;
	}

	// Charge les couts des energies et la contribution climat energie pour
	// chaque annee
	protected List<CoutEnergie> getCoutEnergie(String tableName) {

		String key = tableName + LOAD_DATA;

		String request = getProperty(key);

		return jdbcTemplate.query(request, new RowMapper<CoutEnergie>() {
			@Override
			public CoutEnergie mapRow(ResultSet rs, int rowNum) throws SQLException {

				CoutEnergie coutEnergie = new CoutEnergie();
				int columncount = rs.getMetaData().getColumnCount();

				for (int i = 1; i <= columncount; i++) {

					coutEnergie.setAnnee(rs.getInt("ANNEE"));
					coutEnergie.setEnergie("ELECTRICITE", rs.getBigDecimal("ELECTRICITE"));
					coutEnergie.setEnergie("GAZ", rs.getBigDecimal("GAZ"));
					coutEnergie.setEnergie("FIOUL", rs.getBigDecimal("FIOUL"));
					coutEnergie.setEnergie("URBAIN", rs.getBigDecimal("URBAIN"));
					coutEnergie.setEnergie("AUTRES", rs.getBigDecimal("AUTRES"));
					coutEnergie.setCCE(rs.getBigDecimal("CCE"));

				}
				return coutEnergie;

			}

		});
	}

	// requete pour recuperer les infos de la base Parametres Financiers -->
	// renvoie une liste de financement possibles
	public List<Financement> recupFinancement(String occupant, String branche) {

		String requete = getProperty("Parametres_financiers_LOAD");
		Object[] whereParam = new Object[2];
		whereParam[0] = occupant;
		whereParam[1] = branche;

		List<Financement> res = jdbcTemplate.query(requete, whereParam, new RowMapper<Financement>() {

			public Financement mapRow(ResultSet rs, int rowNum) throws SQLException {

				if (rs.getString("CODE").substring(0, 3).equals("CEE")) {
					CEE sortie = new CEE();
					sortie.setPeriode(rs.getString("PERIODE"));
					sortie.setPrixKWhCumac(rs.getBigDecimal("PRIX_KWHCUMAC"));
					return sortie;
				}

				if (rs.getString("CODE").substring(0, 3).equals("PBC")) {
					PBC sortie = new PBC();
					sortie.setPeriode(rs.getString("PERIODE"));
					sortie.setDuree(rs.getInt("DUREE"));
					sortie.setTauxInteret(rs.getBigDecimal("TAUX_INTERET"));
					return sortie;
				}
				if (rs.getString("CODE").substring(0, 3).equals("BPI")) {
					PretBonif sortie = new PretBonif();
					sortie.setPeriode(rs.getString("PERIODE"));
					sortie.setDuree(rs.getInt("DUREE"));
					sortie.setTauxInteret(rs.getBigDecimal("TAUX_INTERET"));
					sortie.setEcoCond(Exigence.valueOf(rs.getString("ECO_COND")));
					sortie.setPretMax(rs.getBigDecimal("PRET_MAX"));
					return sortie;
				}

				return null;
			}
		});

		return res;
	}

	public List<Geste> getGesteBatiData(String idAggreg) {
		String requete = "SELECT id_agreg,geste, case when exigence = 'BBC renovation' then 'BBC_RENOVATION' when exigence = 'GTB' then 'GTB' else 'RT_PAR_ELEMENT' end as exigence, gain, cout, cee "
				+ "FROM GESTE_BATI WHERE SUBSTR(ID_AGREG,1,6) = '" + idAggreg.substring(0, 6) + "' AND COUT!=0";

		List<Geste> res = jdbcTemplate.query(requete, new RowMapper<Geste>() {

			public Geste mapRow(ResultSet rs, int rowNum) throws SQLException {

				Geste sortie = new Geste();

				sortie.setIdGesteAggreg(rs.getString("ID_AGREG"));
				String geste = rs.getString("GESTE");
				sortie.setGesteNom(geste + rs.getString("EXIGENCE"));
				sortie.setTypeRenovBati(TypeRenovBati.getEnumByLabel(geste));
				sortie.setExigence(Exigence.valueOf(rs.getString("EXIGENCE")));
				sortie.setCoutGesteBati(rs.getBigDecimal("COUT"));
				sortie.setGainEner(rs.getBigDecimal("GAIN"));
				sortie.setValeurCEE(rs.getBigDecimal("CEE"));

				return sortie;

			}
		});
		return res;
	}

	public HashMap<String, SurfMoy> recupSurfMoy() {
		HashMap<String, SurfMoy> surfMoyMap = new HashMap<String, SurfMoy>();
		List<SurfMoy> loadParam = getSurface();
		for (SurfMoy surf : loadParam) {
			surfMoyMap.put(surf.getId(), surf);
		}
		return surfMoyMap;
	}

	protected List<SurfMoy> getSurface() {

		String requete = getProperty("Surface_moy_LOAD");
		List<SurfMoy> result = jdbcTemplate.query(requete, new RowMapper<SurfMoy>() {
			@Override
			public SurfMoy mapRow(ResultSet rs, int rowNum) throws SQLException {
				SurfMoy surf = new SurfMoy();

				surf.setId(rs.getString("ID"));
				surf.setSurfMoy(rs.getBigDecimal("SURFACE"));

				return surf;
			}

		});
		return result;

	}

	public HashMap<String, TauxInteret> recupTauxInteret() {
		HashMap<String, TauxInteret> tauxActuMap = new HashMap<String, TauxInteret>();
		List<TauxInteret> loadParam = getTauxActu();
		for (TauxInteret taux : loadParam) {
			tauxActuMap.put(taux.getIdBranche() + taux.getIdOccupant() + taux.getStatutOccup(), taux);
		}
		return tauxActuMap;
	}

	protected List<TauxInteret> getTauxActu() {
		String requete = getProperty("Taux_Actu_LOAD");
		List<TauxInteret> result = jdbcTemplate.query(requete, new RowMapper<TauxInteret>() {
			@Override
			public TauxInteret mapRow(ResultSet rs, int rowNum) throws SQLException {
				TauxInteret taux = new TauxInteret();
				PBC pbc = new PBC();
				taux.setIdBranche(rs.getString("ID_BRANCHE"));
				taux.setIdOccupant(rs.getString("ID_OCCUPANT"));
				taux.setStatutOccup(rs.getString("STATUT_OCCUP"));
				pbc.setTauxInteret(rs.getBigDecimal("TAUX"));
				taux.setPBC(pbc);

				return taux;
			}

		});
		return result;

	}

	public HashMap<String, Maintenance> recupMaintenance() {
		HashMap<String, Maintenance> maintenanceMap = new HashMap<String, Maintenance>();
		List<Maintenance> loadParam = getMaintenance();
		for (Maintenance maintenance : loadParam) {
			maintenanceMap.put(maintenance.getIdSysteme(), maintenance);
		}
		return maintenanceMap;
	}

	protected List<Maintenance> getMaintenance() {
		String requete = getProperty("Maintenance_LOAD");
		List<Maintenance> result = jdbcTemplate.query(requete, new RowMapper<Maintenance>() {
			@Override
			public Maintenance mapRow(ResultSet rs, int rowNum) throws SQLException {

				Maintenance maintenance = new Maintenance();
				maintenance.setIdSysteme(rs.getString("ID_SYSTEME"));
				maintenance.setPart(rs.getBigDecimal("PART"));

				return maintenance;

			}

		});
		return result;
	}

	public HashMap<String, RepartStatutOccup> recupRepartStatutOccup() {
		HashMap<String, RepartStatutOccup> repartMap = new HashMap<String, RepartStatutOccup>();
		List<RepartStatutOccup> loadParam = getProprioLocataire();
		for (RepartStatutOccup repart : loadParam) {
			repartMap.put(repart.getIdBranche() + repart.getStatutOccup(), repart);
		}
		return repartMap;
	}

	protected List<RepartStatutOccup> getProprioLocataire() {
		String requete = getProperty("Proprio_Locataire_LOAD");
		List<RepartStatutOccup> result = jdbcTemplate.query(requete, new RowMapper<RepartStatutOccup>() {
			@Override
			public RepartStatutOccup mapRow(ResultSet rs, int rowNum) throws SQLException {

				RepartStatutOccup repart = new RepartStatutOccup();
				repart.setIdBranche(rs.getString("ID_BRANCHE"));
				repart.setStatutOccup(rs.getString("STATUT_OCCUP"));
				repart.setRepart(rs.getBigDecimal("REPART"));

				return repart;

			}

		});
		return result;
	}

	public HashMap<String, EvolValeurVerte> recupEvolValeurVerte() {
		HashMap<String, EvolValeurVerte> valeurVerteMap = new HashMap<String, EvolValeurVerte>();
		List<EvolValeurVerte> loadParam = getValeurVerte();
		for (EvolValeurVerte valeurVerte : loadParam) {
			valeurVerteMap.put(valeurVerte.getIdBranche() + valeurVerte.getStatutOccup(), valeurVerte);
		}
		return valeurVerteMap;
	}

	protected List<EvolValeurVerte> getValeurVerte() {
		String requete = getProperty("Valeur_Verte_LOAD");
		List<EvolValeurVerte> result = jdbcTemplate.query(requete, new RowMapper<EvolValeurVerte>() {
			@Override
			public EvolValeurVerte mapRow(ResultSet rs, int rowNum) throws SQLException {

				EvolValeurVerte evolVV = new EvolValeurVerte();
				evolVV.setIdBranche(rs.getString("ID_BRANCHE"));
				evolVV.setStatutOccup(rs.getString("STATUT_OCCUP"));
				evolVV.setEvol(rs.getBigDecimal("VARIATION_CINT"));

				return evolVV;

			}

		});
		return result;
	}

	protected List<EvolutionCout> getListEvolCoutTechno() {
		String requete = getProperty("Evolution_couts_LOAD");
		List<EvolutionCout> result = jdbcTemplate.query(requete, new RowMapper<EvolutionCout>() {
			@Override
			public EvolutionCout mapRow(ResultSet rs, int rowNum) throws SQLException {

				EvolutionCout evolutionCout = new EvolutionCout();
				evolutionCout.setAnnee(rs.getString("ANNEE"));
				evolutionCout.setType(rs.getString("SYS_CHAUFF"));
				evolutionCout.setEvolution(rs.getBigDecimal("EVOLUTION"));

				return evolutionCout;

			}

		});
		return result;
	}


	protected List<EvolutionCout> getListEvolCoutIntTechno() {
		String requete = getProperty("Evolution_couts_int_LOAD");
		List<EvolutionCout> result = jdbcTemplate.query(requete, new RowMapper<EvolutionCout>() {
			@Override
			public EvolutionCout mapRow(ResultSet rs, int rowNum) throws SQLException {

				EvolutionCout evolutionCout = new EvolutionCout();
				evolutionCout.setAnnee(rs.getString("ANNEE"));
				evolutionCout.setType(rs.getString("SYS_CHAUFF"));
				evolutionCout.setEvolution(rs.getBigDecimal("EVOLUTION"));

				return evolutionCout;

			}

		});
		return result;
	}


	public HashMap<String, BigDecimal> getEvolutionCoutTechno() {
		HashMap<String, BigDecimal> result = new HashMap<>();
		List<EvolutionCout> listeRes = getListEvolCoutTechno();
		for (EvolutionCout evolutionCout : listeRes) {
			String cle = evolutionCout.getAnnee() + "_" + evolutionCout.getType();
			result.put(cle, evolutionCout.getEvolution());
		}
		return result;
	}

	public HashMap<String, BigDecimal> getEvolutionCoutIntTechno() {
		HashMap<String, BigDecimal> result = new HashMap<>();
		List<EvolutionCout> listeRes = getListEvolCoutIntTechno();
		for (EvolutionCout evolutionCout : listeRes) {
			String cle = evolutionCout.getAnnee() + "_" + evolutionCout.getType();
			result.put(cle, evolutionCout.getEvolution());
		}
		return result;
	}

	protected List<EvolutionCout> getListEvolCoutBati() {
		String requete = getProperty("Evolution_couts_bati_LOAD");
		List<EvolutionCout> result = jdbcTemplate.query(requete, new RowMapper<EvolutionCout>() {
			@Override
			public EvolutionCout mapRow(ResultSet rs, int rowNum) throws SQLException {

				EvolutionCout evolutionCout = new EvolutionCout();
				evolutionCout.setAnnee(rs.getString("ANNEE"));
				evolutionCout.setType(rs.getString("CODE"));
				evolutionCout.setEvolution(rs.getBigDecimal("EVOLUTION"));

				return evolutionCout;

			}

		});
		return result;
	}

	public HashMap<String, BigDecimal> getEvolutionCoutBati() {
		HashMap<String, BigDecimal> result = new HashMap<>();
		List<EvolutionCout> listeRes = getListEvolCoutBati();
		for (EvolutionCout evolutionCout : listeRes) {
			String cle = evolutionCout.getAnnee() + "_" + evolutionCout.getType();
			result.put(cle, evolutionCout.getEvolution());
		}
		return result;
	}

}
