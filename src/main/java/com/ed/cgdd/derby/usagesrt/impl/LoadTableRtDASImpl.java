package com.ed.cgdd.derby.usagesrt.impl;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;

import com.ed.cgdd.derby.model.calcconso.Conso;
import com.ed.cgdd.derby.model.calcconso.ParamCoutEclVentil;
import com.ed.cgdd.derby.model.calcconso.ParamCoutEcs;
import com.ed.cgdd.derby.model.calcconso.ParamDvEcs;
import com.ed.cgdd.derby.model.calcconso.ParamGainsUsages;
import com.ed.cgdd.derby.model.calcconso.ParamPMConso;
import com.ed.cgdd.derby.model.calcconso.ParamPMConsoChgtSys;
import com.ed.cgdd.derby.model.calcconso.ParamPartSolaireEcs;
import com.ed.cgdd.derby.model.calcconso.ParamPartSysPerfEcs;
import com.ed.cgdd.derby.model.calcconso.ParamRatioAux;
import com.ed.cgdd.derby.model.calcconso.ParamRdtEcs;
import com.ed.cgdd.derby.model.calcconso.ParamRdtPerfEcs;
import com.ed.cgdd.derby.model.calcconso.ParamTauxCouvEcs;
import com.ed.cgdd.derby.model.calcconso.ResultConsoURt;
import com.ed.cgdd.derby.model.parc.TypeRenovBati;
import com.ed.cgdd.derby.model.parc.TypeRenovSysteme;
import com.ed.cgdd.derby.process.InitializeConsoService;
import com.ed.cgdd.derby.usagesrt.LoadTableRtDAS;

public class LoadTableRtDASImpl extends BddUsagesRTDAS implements LoadTableRtDAS {
	private final static Logger LOG = LogManager.getLogger(LoadTableRtDASImpl.class);

	private final static String LOAD_DATA = "_LOAD";
	private final static BigDecimal FACTEUR_EP = new BigDecimal("2.58");
	private final static String INIT_STATE = "Etat initial";

	private JdbcTemplate jdbcTemplate;

	public InitializeConsoService getInitializeConsoService() {
		return initializeConsoService;
	}

	public void setInitializeConsoService(InitializeConsoService initializeConsoService) {
		this.initializeConsoService = initializeConsoService;
	}

	private InitializeConsoService initializeConsoService;

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	// Renvoie les champs des tables de PM des energies dans les consommations
	// d'ECS
	public HashMap<String, ParamPMConso> loadTablePMEcs(String tableName) {

		HashMap<String, ParamPMConso> pmNeufsMap = new HashMap<String, ParamPMConso>();
		List<ParamPMConso> loadParam = getParamPMNeuf(tableName);
		for (ParamPMConso paramPM : loadParam) {
			pmNeufsMap.put(paramPM.getIDBranche(), paramPM);
		}

		return pmNeufsMap;
	}

	// Renvoie les champs de la table des durees de vie des equipements d'ECS
	public HashMap<String, BigDecimal> loadTableDvEcs(String tableName) {

		HashMap<String, BigDecimal> dvEcsMap = new HashMap<String, BigDecimal>();
		List<ParamDvEcs> loadParam = getParamDvEcs(tableName);
		for (ParamDvEcs paramDV : loadParam) {
			dvEcsMap.put(paramDV.getIdEnergie(), paramDV.getDureeVie());
		}

		return dvEcsMap;
	}

	// Charge les durees de vie des systemes pour l'ECS
	protected List<ParamDvEcs> getParamDvEcs(String tableName) {

		String key = tableName + LOAD_DATA;

		String request = getProperty(key);

		return jdbcTemplate.query(request, new RowMapper<ParamDvEcs>() {
			@Override
			public ParamDvEcs mapRow(ResultSet rs, int rowNum) throws SQLException {

				ParamDvEcs dureeVie = new ParamDvEcs();
				int columncount = rs.getMetaData().getColumnCount();

				for (int i = 1; i <= columncount; i++) {

					dureeVie.setIdEnergie(rs.getString("ID_ENERGIE"));
					dureeVie.setDureeVie(new BigDecimal(rs.getString("DV")));

				}
				return dureeVie;

			}

		});
	}

	// Renvoie les champs des tables de PM des energies d'ECS lors du
	// renouvellement des sytemes
	public HashMap<String, ParamPMConsoChgtSys> loadTablePMECSChgtSys(String tableName) {

		HashMap<String, ParamPMConsoChgtSys> pmConsoChgtSysMap = new HashMap<String, ParamPMConsoChgtSys>();
		List<ParamPMConsoChgtSys> loadParam = getParamPMChgtSys(tableName);
		for (ParamPMConsoChgtSys paramPM : loadParam) {
			pmConsoChgtSysMap.put(paramPM.getEnergInit(), paramPM);
		}

		return pmConsoChgtSysMap;
	}

	// Renvoie les champs des tables Derby de besoins initiaux sous forme de
	// HashMap
	public HashMap<String, Conso> loadMapResultBesoinVentil(String tableName, final String idAgregParc,
			final int pasdeTemps) {

		HashMap<String, Conso> usageMap = new HashMap<String, Conso>();
		List<Conso> loadParc = getTableMap(tableName, idAgregParc, pasdeTemps);
		for (Conso parc : loadParc) {
			usageMap.put(
					parc.getId() + parc.getAnneeRenovSys() + parc.getTypeRenovSys() + parc.getAnneeRenov()
							+ parc.getTypeRenovBat(), parc);
		}

		return usageMap;
	}

	// Renvoie les champs des tables Derby de besoins initiaux sous forme de
	// HashMap et rempli les consoU
	public HashMap<String, Conso> loadMapResultBesoinEclairage(String tableName, final String idAgregParc,
			final int pasdeTemps, HashMap<String, ResultConsoURt> resultConsoURtMap) {
		String idResultRt;
		int anneeNTab = 0;
		HashMap<String, Conso> usageMap = new HashMap<String, Conso>();
		List<Conso> loadConso = getTableMap(tableName, idAgregParc, pasdeTemps);
		for (Conso conso : loadConso) {
			usageMap.put(conso.getId() + conso.getAnneeRenovSys() + conso.getTypeRenovSys() + conso.getAnneeRenov()
					+ conso.getTypeRenovBat(), conso);

			// Remplissage de la Map resultConsoURtMap
			idResultRt = conso.getIdagreg() + conso.getAnneeRenov() + conso.getTypeRenovBat();

			if (resultConsoURtMap.containsKey(idResultRt)) {
				BigDecimal consoEP = conso.getAnnee(anneeNTab).multiply(FACTEUR_EP);
				initializeConsoService.insertResultConsoUExistEcl(resultConsoURtMap, anneeNTab, idResultRt,
						conso.getAnnee(anneeNTab), consoEP, 2009);

			} else {
				LOG.info("Probleme chargement eclairage..");
			}
		}

		return usageMap;
	}

	// Renvoie les champs des tables Derby de besoins initiaux sous forme de
	// HashMap
	public HashMap<String, Conso> loadMapResultBesoin(String tableName, final String idAgregParc, final int pasdeTemps) {

		HashMap<String, Conso> usageMap = new HashMap<String, Conso>();
		List<Conso> loadConso = getTableMap(tableName, idAgregParc, pasdeTemps);
		for (Conso conso : loadConso) {
			usageMap.put(conso.getId() + conso.getAnneeRenovSys() + conso.getTypeRenovSys() + conso.getAnneeRenov()
					+ conso.getTypeRenovBat(), conso);
		}

		return usageMap;
	}

	// Renvoie les champs de la table de rendements de l'ECS
	public HashMap<String, ParamRdtEcs> loadTableRdtEcs(String tableName) {

		HashMap<String, ParamRdtEcs> rdtEcsMap = new HashMap<String, ParamRdtEcs>();
		List<ParamRdtEcs> loadParam = getParamRdtEcs(tableName);
		for (ParamRdtEcs paramRdt : loadParam) {
			rdtEcsMap.put(paramRdt.getIdBranche() + paramRdt.getIdEnergie(), paramRdt);

		}

		return rdtEcsMap;
	}

	// Renvoie les champs de la table de rendements performants de l'ECS
	public HashMap<String, ParamRdtPerfEcs> loadTableRdtPerfEcs(String tableName) {

		HashMap<String, ParamRdtPerfEcs> rdtEcsPerfMap = new HashMap<String, ParamRdtPerfEcs>();
		List<ParamRdtPerfEcs> loadParam = getParamRdtPerfEcs(tableName);
		for (ParamRdtPerfEcs paramRdt : loadParam) {
			rdtEcsPerfMap.put(paramRdt.getIdenergie(), paramRdt);

		}

		return rdtEcsPerfMap;
	}

	// Renvoie les champs de la table de couts des systemes ECS
	public HashMap<String, ParamCoutEcs> loadTableCoutEcs(String tableName) {

		HashMap<String, ParamCoutEcs> coutEcsMap = new HashMap<String, ParamCoutEcs>();
		List<ParamCoutEcs> loadParam = getParamCoutEcs(tableName);
		for (ParamCoutEcs paramCout : loadParam) {
			coutEcsMap.put(paramCout.getIdenergie() + paramCout.getPerformance(), paramCout);

		}

		return coutEcsMap;
	}

	// Charge les couts des systemes ECS
	protected List<ParamCoutEcs> getParamCoutEcs(String tableName) {

		String key = tableName + LOAD_DATA;

		String request = getProperty(key);

		return jdbcTemplate.query(request, new RowMapper<ParamCoutEcs>() {
			@Override
			public ParamCoutEcs mapRow(ResultSet rs, int rowNum) throws SQLException {

				ParamCoutEcs coutEcs = new ParamCoutEcs();
				int columncount = rs.getMetaData().getColumnCount();

				for (int i = 1; i <= columncount; i++) {
					coutEcs.setPerformance(rs.getString("PERFORMANCE"));
					coutEcs.setIdenergie(rs.getString("ID_ENERGIE"));
					coutEcs.setCout(rs.getBigDecimal("COUT"));

				}
				return coutEcs;

			}

		});
	}

	// Renvoie les champs de la table de couts de l'eclairage et la ventilation
	public HashMap<String, ParamCoutEclVentil> loadTableCoutEclVentil(String tableName) {

		HashMap<String, ParamCoutEclVentil> coutEcsMap = new HashMap<String, ParamCoutEclVentil>();
		List<ParamCoutEclVentil> loadParam = getParamCoutEclVentil(tableName);
		for (ParamCoutEclVentil paramCout : loadParam) {
			coutEcsMap.put(paramCout.getUsage() + paramCout.getIdbranche(), paramCout);

		}

		return coutEcsMap;
	}

	// Charge les couts de l'eclairage et la ventilation
	protected List<ParamCoutEclVentil> getParamCoutEclVentil(String tableName) {

		String key = tableName + LOAD_DATA;

		String request = getProperty(key);

		return jdbcTemplate.query(request, new RowMapper<ParamCoutEclVentil>() {
			@Override
			public ParamCoutEclVentil mapRow(ResultSet rs, int rowNum) throws SQLException {

				ParamCoutEclVentil coutEclVentil = new ParamCoutEclVentil();
				int columncount = rs.getMetaData().getColumnCount();

				for (int i = 1; i <= columncount; i++) {
					coutEclVentil.setUsage(rs.getString("USAGE"));
					coutEclVentil.setIdbranche(rs.getString("ID_BRANCHE"));
					coutEclVentil.setCout(rs.getBigDecimal("COUT"));

				}
				return coutEclVentil;

			}

		});
	}

	// Renvoie les champs de la table des parts de marche des energies solaires
	// dans le neuf et l'existant pour l'ECS
	public HashMap<String, ParamPartSolaireEcs> loadTablePartSolaireEcs(String tableName) {

		HashMap<String, ParamPartSolaireEcs> partSolaireMap = new HashMap<String, ParamPartSolaireEcs>();
		List<ParamPartSolaireEcs> loadParam = getParamPartSolaireEcs(tableName);
		for (ParamPartSolaireEcs partSolaire : loadParam) {
			partSolaireMap.put(partSolaire.getEtatbat() + partSolaire.getIdbranche(), partSolaire);

		}

		return partSolaireMap;
	}

	// Renvoie les champs de la table des taux de couverture du besoin d'ECS par
	// les systemes solaires
	public HashMap<String, ParamTauxCouvEcs> loadTableTxCouvSolaireEcs(String tableName) {

		HashMap<String, ParamTauxCouvEcs> txCouvMap = new HashMap<String, ParamTauxCouvEcs>();
		List<ParamTauxCouvEcs> loadParam = getParamTxCouvSolaireEcs(tableName);
		for (ParamTauxCouvEcs txCouvSolaire : loadParam) {
			txCouvMap.put(txCouvSolaire.getEtatbat(), txCouvSolaire);

		}

		return txCouvMap;
	}

	// Renvoie les champs de la table des parts des nouveaux systemes d'ECS
	public HashMap<String, ParamPartSysPerfEcs> loadTablePartSysPerfEcs(String tableName) {

		HashMap<String, ParamPartSysPerfEcs> partSysPerfEcsMap = new HashMap<String, ParamPartSysPerfEcs>();
		List<ParamPartSysPerfEcs> loadParam = getParamPartSysPerfEcs(tableName);
		for (ParamPartSysPerfEcs partSysPerfEcs : loadParam) {
			partSysPerfEcsMap.put(partSysPerfEcs.getIdenergie(), partSysPerfEcs);

		}

		return partSysPerfEcsMap;
	}

	// Charge les parts des systemes performants d'ECS
	protected List<ParamPartSysPerfEcs> getParamPartSysPerfEcs(String tableName) {

		String key = tableName + LOAD_DATA;

		String request = getProperty(key);

		return jdbcTemplate.query(request, new RowMapper<ParamPartSysPerfEcs>() {
			@Override
			public ParamPartSysPerfEcs mapRow(ResultSet rs, int rowNum) throws SQLException {

				ParamPartSysPerfEcs sysPerfEcs = new ParamPartSysPerfEcs();
				int columncount = rs.getMetaData().getColumnCount();

				for (int i = 1; i <= columncount; i++) {
					sysPerfEcs.setIdenergie(rs.getString("ID_ENERGIE"));
					sysPerfEcs.setPart(1, rs.getBigDecimal("PERIODE1"));
					sysPerfEcs.setPart(2, rs.getBigDecimal("PERIODE2"));
					sysPerfEcs.setPart(3, rs.getBigDecimal("PERIODE3"));
					sysPerfEcs.setPart(4, rs.getBigDecimal("PERIODE4"));
					sysPerfEcs.setPart(5, rs.getBigDecimal("PERIODE5"));

				}
				return sysPerfEcs;

			}

		});
	}

	// Charge les taux de couverture du besoin des systeme d'ECS solaire
	protected List<ParamTauxCouvEcs> getParamTxCouvSolaireEcs(String tableName) {

		String key = tableName + LOAD_DATA;

		String request = getProperty(key);

		return jdbcTemplate.query(request, new RowMapper<ParamTauxCouvEcs>() {
			@Override
			public ParamTauxCouvEcs mapRow(ResultSet rs, int rowNum) throws SQLException {

				ParamTauxCouvEcs TxCouvSolaire = new ParamTauxCouvEcs();
				int columncount = rs.getMetaData().getColumnCount();

				for (int i = 1; i <= columncount; i++) {
					TxCouvSolaire.setEtatbat(rs.getString("ETAT_BAT"));
					TxCouvSolaire.setTxcouv(rs.getBigDecimal("TX_COUV"));

				}
				return TxCouvSolaire;

			}

		});
	}

	// Charge les parts de marche d'energies solaires dans l'ECS
	protected List<ParamPartSolaireEcs> getParamPartSolaireEcs(String tableName) {

		String key = tableName + LOAD_DATA;

		String request = getProperty(key);

		return jdbcTemplate.query(request, new RowMapper<ParamPartSolaireEcs>() {
			@Override
			public ParamPartSolaireEcs mapRow(ResultSet rs, int rowNum) throws SQLException {

				ParamPartSolaireEcs pmSolaire = new ParamPartSolaireEcs();
				int columncount = rs.getMetaData().getColumnCount();

				for (int i = 1; i <= columncount; i++) {
					pmSolaire.setEtatbat(rs.getString("ETAT_BAT"));
					pmSolaire.setIdbranche(rs.getString("ID_BRANCHE"));
					pmSolaire.setPart(1, rs.getBigDecimal("PERIODE1"));
					pmSolaire.setPart(2, rs.getBigDecimal("PERIODE2"));
					pmSolaire.setPart(3, rs.getBigDecimal("PERIODE3"));
					pmSolaire.setPart(4, rs.getBigDecimal("PERIODE4"));
					pmSolaire.setPart(5, rs.getBigDecimal("PERIODE5"));

				}
				return pmSolaire;

			}

		});
	}

	// Charge les rendements initiaux des systemes d'ECS
	protected List<ParamRdtEcs> getParamRdtEcs(String tableName) {

		String key = tableName + LOAD_DATA;

		String request = getProperty(key);

		return jdbcTemplate.query(request, new RowMapper<ParamRdtEcs>() {
			@Override
			public ParamRdtEcs mapRow(ResultSet rs, int rowNum) throws SQLException {

				ParamRdtEcs rdtEcs = new ParamRdtEcs();
				int columncount = rs.getMetaData().getColumnCount();

				for (int i = 1; i <= columncount; i++) {
					rdtEcs.setIdBranche(rs.getString("ID_BRANCHE"));
					rdtEcs.setIdEnergie(rs.getString("ID_ENERGIE"));
					rdtEcs.setRdt(0, rs.getBigDecimal("PERIODE0"));
					rdtEcs.setRdt(1, rs.getBigDecimal("PERIODE1"));
					rdtEcs.setRdt(2, rs.getBigDecimal("PERIODE2"));
					rdtEcs.setRdt(3, rs.getBigDecimal("PERIODE3"));
					rdtEcs.setRdt(4, rs.getBigDecimal("PERIODE4"));
					rdtEcs.setRdt(5, rs.getBigDecimal("PERIODE5"));

				}
				return rdtEcs;

			}

		});
	}

	// Charge les rendements des systemes performants d'ECS
	protected List<ParamRdtPerfEcs> getParamRdtPerfEcs(String tableName) {

		String key = tableName + LOAD_DATA;

		String request = getProperty(key);

		return jdbcTemplate.query(request, new RowMapper<ParamRdtPerfEcs>() {
			@Override
			public ParamRdtPerfEcs mapRow(ResultSet rs, int rowNum) throws SQLException {

				ParamRdtPerfEcs rdtPerfEcs = new ParamRdtPerfEcs();
				int columncount = rs.getMetaData().getColumnCount();

				for (int i = 1; i <= columncount; i++) {
					rdtPerfEcs.setIdenergie(rs.getString("ID_ENERGIE"));
					rdtPerfEcs.setRdt(rs.getBigDecimal("RDT"));

				}
				return rdtPerfEcs;

			}

		});
	}

	protected List<Conso> getTableMap(String tableName, final String idAgregParc, final int pasdeTemps) {

		// Charge les tables de consommations initiales pour les usages ne
		// faisant pas l'objet de financements specifiques

		String key = tableName + LOAD_DATA;

		String request = getProperty(key);

		return jdbcTemplate.query(request, new PreparedStatementSetter() {
			public void setValues(PreparedStatement preparedStatement) throws SQLException {
				preparedStatement.setString(1, idAgregParc);
			}
		}, new RowMapper<Conso>() {
			@Override
			public Conso mapRow(ResultSet rs, int rowNum) throws SQLException {

				Conso conso = new Conso(pasdeTemps);
				int columncount = rs.getMetaData().getColumnCount();

				for (int i = 1; i <= columncount; i++) {

					conso.setId(rs.getString("ID"));
					conso.setAnneeRenovSys(INIT_STATE);
					conso.setTypeRenovSys(TypeRenovSysteme.ETAT_INIT);
					conso.setAnneeRenov(INIT_STATE);
					conso.setTypeRenovBat(TypeRenovBati.ETAT_INIT);
					conso.setAnnee(0, rs.getBigDecimal("BESOIN"));

					for (int j = 1; j <= pasdeTemps; j++) {

						conso.setAnnee(j, BigDecimal.ZERO);

					}
				}
				return conso;

			}

		});
	}

	/**
	 * Charge les parts de marche des energies dans les consommations (de
	 * cuisson ou de autres)
	 */
	protected List<ParamPMConso> getParamPMNeuf(String tableName) {

		String key = tableName + LOAD_DATA;

		String request = getProperty(key);

		return jdbcTemplate.query(request, new RowMapper<ParamPMConso>() {
			@Override
			public ParamPMConso mapRow(ResultSet rs, int rowNum) throws SQLException {

				ParamPMConso pmNonRT = new ParamPMConso();
				int columncount = rs.getMetaData().getColumnCount();

				for (int i = 1; i <= columncount; i++) {

					pmNonRT.setIDBranche(rs.getString("ID_BRANCHE"));
					pmNonRT.setEnergie("ELECTRICITE", rs.getBigDecimal("ELECTRICITE"));
					pmNonRT.setEnergie("GAZ", rs.getBigDecimal("GAZ"));
					pmNonRT.setEnergie("FIOUL", rs.getBigDecimal("FIOUL"));
					pmNonRT.setEnergie("URBAIN", rs.getBigDecimal("URBAIN"));
					pmNonRT.setEnergie("AUTRES", rs.getBigDecimal("AUTRES"));

				}
				return pmNonRT;

			}

		});
	}

	/**
	 * Charge les parts de marche des energies dans les consommations d'ECS
	 * apres renouvellement des systemes
	 * 
	 * @param tableName
	 * @return
	 */
	protected List<ParamPMConsoChgtSys> getParamPMChgtSys(String tableName) {

		String key = tableName + LOAD_DATA;

		String request = getProperty(key);

		return jdbcTemplate.query(request, new RowMapper<ParamPMConsoChgtSys>() {
			@Override
			public ParamPMConsoChgtSys mapRow(ResultSet rs, int rowNum) throws SQLException {

				ParamPMConsoChgtSys pmChgtSys = new ParamPMConsoChgtSys();
				int columncount = rs.getMetaData().getColumnCount();

				for (int i = 1; i <= columncount; i++) {
					pmChgtSys.setEnergInit(StringUtils.stripAccents(rs.getString("ENERGIE_INIT").toUpperCase()));
					pmChgtSys.setPmChgt("ELECTRICITE", rs.getBigDecimal("ELECTRICITE"));
					pmChgtSys.setPmChgt("GAZ", rs.getBigDecimal("GAZ"));
					pmChgtSys.setPmChgt("FIOUL", rs.getBigDecimal("FIOUL"));
					pmChgtSys.setPmChgt("URBAIN", rs.getBigDecimal("URBAIN"));
					pmChgtSys.setPmChgt("AUTRES", rs.getBigDecimal("AUTRES"));

				}
				return pmChgtSys;

			}

		});
	}

	// Renvoie les champs de la table des gains atteignables pour l'usage
	// de ventilation
	public HashMap<String, ParamGainsUsages> loadTableGainsVentilation(String tableName) {

		HashMap<String, ParamGainsUsages> gainsEclairageMap = new HashMap<String, ParamGainsUsages>();
		List<ParamGainsUsages> loadParam = getParamGainsVentilation(tableName);
		for (ParamGainsUsages gainsEclairage : loadParam) {
			gainsEclairageMap.put(gainsEclairage.getIdPartiel(), gainsEclairage);
		}

		return gainsEclairageMap;
	}

	// Charge les gains atteignables sur l'usage d'eclairage
	protected List<ParamGainsUsages> getParamGainsVentilation(String tableName) {

		String key = tableName + LOAD_DATA;

		String request = getProperty(key);

		return jdbcTemplate.query(request, new RowMapper<ParamGainsUsages>() {
			@Override
			public ParamGainsUsages mapRow(ResultSet rs, int rowNum) throws SQLException {

				ParamGainsUsages gainsEclairage = new ParamGainsUsages();
				int columncount = rs.getMetaData().getColumnCount();

				for (int i = 1; i <= columncount; i++) {

					gainsEclairage.setIdBranche(rs.getString("ID_BRANCHE"));
					gainsEclairage.setUsage(rs.getString("USAGE"));
					gainsEclairage.setPeriode(1, rs.getBigDecimal("PERIODE1"));
					gainsEclairage.setPeriode(2, rs.getBigDecimal("PERIODE2"));
					gainsEclairage.setPeriode(3, rs.getBigDecimal("PERIODE3"));
					gainsEclairage.setPeriode(4, rs.getBigDecimal("PERIODE4"));
					gainsEclairage.setPeriode(5, rs.getBigDecimal("PERIODE5"));

				}
				return gainsEclairage;

			}

		});
	}

	// Renvoie les champs de la table des gains atteignables pour l'usage
	// d'eclairage
	public HashMap<String, ParamGainsUsages> loadTableGainsEclairage(String tableName) {

		HashMap<String, ParamGainsUsages> gainsEclairageMap = new HashMap<String, ParamGainsUsages>();
		List<ParamGainsUsages> loadParam = getParamGainsEclairage(tableName);
		for (ParamGainsUsages gainsEclairage : loadParam) {
			gainsEclairageMap.put(gainsEclairage.getIdPartiel(), gainsEclairage);
		}

		return gainsEclairageMap;
	}

	// Charge les gains atteignables sur l'usage d'eclairage
	protected List<ParamGainsUsages> getParamGainsEclairage(String tableName) {

		String key = tableName + LOAD_DATA;

		String request = getProperty(key);

		return jdbcTemplate.query(request, new RowMapper<ParamGainsUsages>() {
			@Override
			public ParamGainsUsages mapRow(ResultSet rs, int rowNum) throws SQLException {

				ParamGainsUsages gainsEclairage = new ParamGainsUsages();
				int columncount = rs.getMetaData().getColumnCount();

				for (int i = 1; i <= columncount; i++) {

					gainsEclairage.setIdBranche(rs.getString("ID_BRANCHE"));
					gainsEclairage.setUsage(rs.getString("USAGE"));
					gainsEclairage.setPeriode(1, rs.getBigDecimal("PERIODE1"));
					gainsEclairage.setPeriode(2, rs.getBigDecimal("PERIODE2"));
					gainsEclairage.setPeriode(3, rs.getBigDecimal("PERIODE3"));
					gainsEclairage.setPeriode(4, rs.getBigDecimal("PERIODE4"));
					gainsEclairage.setPeriode(5, rs.getBigDecimal("PERIODE5"));

				}
				return gainsEclairage;

			}

		});
	}

	// Renvoie les champs de la table des ratios d'auxiliaires de chauffage
	public HashMap<String, ParamRatioAux> loadTableRatioAuxChaud(String tableName) {

		HashMap<String, ParamRatioAux> auxChaudRatioMap = new HashMap<String, ParamRatioAux>();
		List<ParamRatioAux> loadParam = getParamRatioAuxChaud(tableName);
		for (ParamRatioAux ratioAux : loadParam) {
			auxChaudRatioMap.put(ratioAux.getSysteme(), ratioAux);
		}

		return auxChaudRatioMap;
	}

	// Renvoie les champs de la table des ratios d'auxiliaires de climatisation
	public HashMap<String, ParamRatioAux> loadTableRatioAuxClim(String tableName) {

		HashMap<String, ParamRatioAux> auxFroidRatioMap = new HashMap<String, ParamRatioAux>();
		List<ParamRatioAux> loadParam = getParamRatioAuxFroid(tableName);
		for (ParamRatioAux ratioAux : loadParam) {
			auxFroidRatioMap.put(ratioAux.getSysteme(), ratioAux);
		}

		return auxFroidRatioMap;
	}

	// Charge les ratios necessaires au calcul des consommations d'auxiliaires
	// de chauffage
	protected List<ParamRatioAux> getParamRatioAuxChaud(String tableName) {

		String key = tableName + LOAD_DATA;

		String request = getProperty(key);

		return jdbcTemplate.query(request, new RowMapper<ParamRatioAux>() {
			@Override
			public ParamRatioAux mapRow(ResultSet rs, int rowNum) throws SQLException {

				ParamRatioAux ratioChaud = new ParamRatioAux();
				int columncount = rs.getMetaData().getColumnCount();

				for (int i = 1; i <= columncount; i++) {

					ratioChaud.setSysteme(rs.getString("SYS_CHAUD"));
					ratioChaud.setRatio(rs.getBigDecimal("RATIO"));

				}
				return ratioChaud;

			}

		});
	}

	// Charge les ratios necessaires au calcul des consommations d'auxiliaires
	// de climatisation
	protected List<ParamRatioAux> getParamRatioAuxFroid(String tableName) {

		String key = tableName + LOAD_DATA;

		String request = getProperty(key);

		return jdbcTemplate.query(request, new RowMapper<ParamRatioAux>() {
			@Override
			public ParamRatioAux mapRow(ResultSet rs, int rowNum) throws SQLException {

				ParamRatioAux ratioChaud = new ParamRatioAux();
				int columncount = rs.getMetaData().getColumnCount();

				for (int i = 1; i <= columncount; i++) {

					ratioChaud.setSysteme(rs.getString("SYS_FROID"));
					ratioChaud.setRatio(rs.getBigDecimal("RATIO"));

				}
				return ratioChaud;

			}

		});
	}

}