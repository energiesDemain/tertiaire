package com.ed.cgdd.derby.usagesnonrt.impl;

import java.math.BigDecimal;
import java.math.MathContext;
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

import com.ed.cgdd.derby.model.CalibParameters;
import com.ed.cgdd.derby.model.calcconso.ParamBesoinsNeufs;
import com.ed.cgdd.derby.model.calcconso.ParamDVNonRT;
import com.ed.cgdd.derby.model.calcconso.ParamGainFroidRglt;
import com.ed.cgdd.derby.model.calcconso.ParamGainsUsages;
import com.ed.cgdd.derby.model.calcconso.ParamPMConso;
import com.ed.cgdd.derby.model.calcconso.ParamPMConsoChgtSys;
import com.ed.cgdd.derby.model.calcconso.ParamRythmeFroidRglt;
import com.ed.cgdd.derby.model.parc.Parc;
import com.ed.cgdd.derby.model.parc.TypeRenovBati;
import com.ed.cgdd.derby.usagesnonrt.LoadTableUsagesNonRTDAS;


public class LoadTableUsagesNonRTDASImpl extends BddUsagesNonRTDAS implements LoadTableUsagesNonRTDAS {
	private final static Logger LOG = LogManager.getLogger(LoadTableUsagesNonRTDASImpl.class);

	private final static String LOAD_DATA = "_LOAD";
	private final static String INIT_STATE = "Etat initial";
	private JdbcTemplate jdbcTemplate;

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	// Renvoie les champs des tables Derby de besoins initiaux sous forme de
	// HashMap
	@Override
	public HashMap<String, Parc> loadMapResultBesoin(String tableName, final String idAgregParc, 
			final int pasdeTemps, BigDecimal calageParc) {

		HashMap<String, Parc> usageMap = new HashMap<String, Parc>();
		List<Parc> loadParc = getTableMap(tableName, idAgregParc, pasdeTemps, calageParc);
		for (Parc parc : loadParc) {
			usageMap.put(parc.getId(), parc);
		}

		return usageMap;
	}

	// Renvoie les champs de la table de besoins Neufs sous forme de HashMap
	@Override
	public HashMap<String, ParamBesoinsNeufs> loadTableBesoinsNeufs(String tableName) {

		HashMap<String, ParamBesoinsNeufs> besoinsNeufsMap = new HashMap<String, ParamBesoinsNeufs>();
		List<ParamBesoinsNeufs> loadParam = getParamBesoinsNeufs(tableName);
		for (ParamBesoinsNeufs paramBesoins : loadParam) {
			besoinsNeufsMap.put(paramBesoins.getIdpartiel(), paramBesoins);

		}

		return besoinsNeufsMap;
	}

	// Renvoie les champs des tables de PM des energies dans les consommations
	// non RT sous forme de HashMap
	@Override
	public HashMap<String, ParamPMConso> loadTablePMConso(String tableName) {

		HashMap<String, ParamPMConso> besoinsNeufsMap = new HashMap<String, ParamPMConso>();
		List<ParamPMConso> loadParam = getParamPMNonRT(tableName);
		for (ParamPMConso paramPM : loadParam) {
			besoinsNeufsMap.put(paramPM.getIDBranche(), paramPM);
		}

		return besoinsNeufsMap;
	}

	// Renvoie les champs des tables de PM des energies dans les consommations
	// non RT sous forme de HashMap
	@Override
	public HashMap<String, ParamPMConsoChgtSys> loadTablePMConsoChgtSys(String tableName) {

		HashMap<String, ParamPMConsoChgtSys> pmConsoChgtSysMap = new HashMap<String, ParamPMConsoChgtSys>();
		List<ParamPMConsoChgtSys> loadParam = getParamPMChgtSys(tableName);
		for (ParamPMConsoChgtSys paramPM : loadParam) {
			pmConsoChgtSysMap.put(paramPM.getEnergInit(), paramPM);
		}

		return pmConsoChgtSysMap;
	}

	// Renvoie les champs de la table des durees de vie des equipements pour les
	// usages non RT
	@Override
	public HashMap<String, BigDecimal> loadTableDVNonRT(String tableName) {

		HashMap<String, BigDecimal> dvNonRTMap = new HashMap<String, BigDecimal>();
		List<ParamDVNonRT> loadParam = getParamDVNonRT(tableName);
		for (ParamDVNonRT paramDV : loadParam) {
			dvNonRTMap.put(paramDV.getUsage(), paramDV.getDureeVie());
		}

		return dvNonRTMap;
	}

	// Renvoie les champs de la table des rythmes de fermeture des meubles
	// frigorifiques defini par la reglementation
	@Override
	public HashMap<String, BigDecimal> loadTableRythmeFroidRglt(String tableName) {

		HashMap<String, BigDecimal> rythmeFroidMap = new HashMap<String, BigDecimal>();
		List<ParamRythmeFroidRglt> loadParam = getParamFroidAlimRythme(tableName);
		for (ParamRythmeFroidRglt paramRythme : loadParam) {
			rythmeFroidMap.put(paramRythme.getPeriode(), paramRythme.getRythme());
		}

		return rythmeFroidMap;
	}

	// Renvoie les champs de la table des gains obtenus lors de la fermeture des
	// meubles
	// frigorifiques defini par la reglementation
	@Override
	public HashMap<String, BigDecimal> loadTableGainFroidRglt(String tableName) {

		HashMap<String, BigDecimal> gainFroidMap = new HashMap<String, BigDecimal>();
		List<ParamGainFroidRglt> loadParam = getParamFroidAlimGain(tableName);
		for (ParamGainFroidRglt paramGain : loadParam) {
			gainFroidMap.put(paramGain.getPeriode(), paramGain.getGain());
		}

		return gainFroidMap;
	}

	// Renvoie les champs de la table des gains atteignables pour les usages non
	// reglementes
	@Override
	public HashMap<String, ParamGainsUsages> loadTableGainsNonRT(String tableName) {

		HashMap<String, ParamGainsUsages> gainsNonRTMap = new HashMap<String, ParamGainsUsages>();
		List<ParamGainsUsages> loadParam = getParamGainsNonRT(tableName);
		for (ParamGainsUsages gainsNonRT : loadParam) {
			gainsNonRTMap.put(gainsNonRT.getIdPartiel(), gainsNonRT);
		}

		return gainsNonRTMap;
	}

	// Charge les besoins par usage des batiments neufs
	protected List<ParamBesoinsNeufs> getParamBesoinsNeufs(String tableName) {

		String key = tableName + LOAD_DATA;

		String request = getProperty(key);

		return jdbcTemplate.query(request, new RowMapper<ParamBesoinsNeufs>() {
			@Override
			public ParamBesoinsNeufs mapRow(ResultSet rs, int rowNum) throws SQLException {

				ParamBesoinsNeufs besoinsNeufs = new ParamBesoinsNeufs();
				int columncount = rs.getMetaData().getColumnCount();

				for (int i = 1; i <= columncount; i++) {

					besoinsNeufs.setIDBranche(rs.getString("ID_BRANCHE"));
					besoinsNeufs.setIDBat_type(rs.getString("ID_BAT_TYPE"));
					besoinsNeufs.setUsage(rs.getString("USAGE"));
					besoinsNeufs.setPeriode(0, rs.getBigDecimal("PERIODE0"));
					besoinsNeufs.setPeriode(1, rs.getBigDecimal("PERIODE1"));
					besoinsNeufs.setPeriode(2, rs.getBigDecimal("PERIODE2"));
					besoinsNeufs.setPeriode(3, rs.getBigDecimal("PERIODE3"));
					besoinsNeufs.setPeriode(4, rs.getBigDecimal("PERIODE4"));
					besoinsNeufs.setPeriode(5, rs.getBigDecimal("PERIODE5"));

				}
				return besoinsNeufs;

			}

		});
	}

	// Charge les durees de vie des systemes pour les usages non RT
	protected List<ParamDVNonRT> getParamDVNonRT(String tableName) {

		String key = tableName + LOAD_DATA;

		String request = getProperty(key);

		return jdbcTemplate.query(request, new RowMapper<ParamDVNonRT>() {
			@Override
			public ParamDVNonRT mapRow(ResultSet rs, int rowNum) throws SQLException {

				ParamDVNonRT dureeVie = new ParamDVNonRT();
				int columncount = rs.getMetaData().getColumnCount();

				for (int i = 1; i <= columncount; i++) {

					dureeVie.setUsage(rs.getString("USAGE"));
					dureeVie.setDureeVie(rs.getBigDecimal("DV"));

				}
				return dureeVie;

			}

		});
	}

	// Charge le rythme de fermeture des meubles frigorifiques
	protected List<ParamRythmeFroidRglt> getParamFroidAlimRythme(String tableName) {

		String key = tableName + LOAD_DATA;

		String request = getProperty(key);

		return jdbcTemplate.query(request, new RowMapper<ParamRythmeFroidRglt>() {
			@Override
			public ParamRythmeFroidRglt mapRow(ResultSet rs, int rowNum) throws SQLException {

				ParamRythmeFroidRglt rythmeFroid = new ParamRythmeFroidRglt();
				int columncount = rs.getMetaData().getColumnCount();

				for (int i = 1; i <= columncount; i++) {

					rythmeFroid.setPeriode(rs.getString("PERIODE"));
					rythmeFroid.setRythme(rs.getBigDecimal("RYTHME"));

				}
				return rythmeFroid;

			}

		});
	}

	// Charge les gains obtenus lors de la fermeture des meubles frigorifiques
	protected List<ParamGainFroidRglt> getParamFroidAlimGain(String tableName) {

		String key = tableName + LOAD_DATA;

		String request = getProperty(key);

		return jdbcTemplate.query(request, new RowMapper<ParamGainFroidRglt>() {
			@Override
			public ParamGainFroidRglt mapRow(ResultSet rs, int rowNum) throws SQLException {

				ParamGainFroidRglt gainFroid = new ParamGainFroidRglt();
				int columncount = rs.getMetaData().getColumnCount();

				for (int i = 1; i <= columncount; i++) {

					gainFroid.setPeriode(rs.getString("PERIODE"));
					gainFroid.setGain(rs.getBigDecimal("GAIN"));

				}
				return gainFroid;

			}

		});
	}

	// Charge les gains atteignables sur les usages non reglementes
	protected List<ParamGainsUsages> getParamGainsNonRT(String tableName) {

		String key = tableName + LOAD_DATA;

		String request = getProperty(key);

		return jdbcTemplate.query(request, new RowMapper<ParamGainsUsages>() {
			@Override
			public ParamGainsUsages mapRow(ResultSet rs, int rowNum) throws SQLException {

				ParamGainsUsages gainsNonRT = new ParamGainsUsages();
				int columncount = rs.getMetaData().getColumnCount();

				for (int i = 1; i <= columncount; i++) {

					gainsNonRT.setIdBranche(rs.getString("ID_BRANCHE"));
					gainsNonRT.setUsage(rs.getString("USAGE"));
					gainsNonRT.setPeriode(1, rs.getBigDecimal("PERIODE1"));
					gainsNonRT.setPeriode(2, rs.getBigDecimal("PERIODE2"));
					gainsNonRT.setPeriode(3, rs.getBigDecimal("PERIODE3"));
					gainsNonRT.setPeriode(4, rs.getBigDecimal("PERIODE4"));
					gainsNonRT.setPeriode(5, rs.getBigDecimal("PERIODE5"));

				}
				return gainsNonRT;

			}

		});
	}

	// Charge les parts de marche des energies dans les consommations (de
	// cuisson ou de autres)
	protected List<ParamPMConso> getParamPMNonRT(String tableName) {

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

	// Charge les parts de marche des energies dans les consommations (de
	// cuisson ou de autres) apres renouvellement des systemes
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

	protected List<Parc> getTableMap(String tableName, final String idAgregParc, final int pasdeTemps, BigDecimal calageParc) {

		// Charge les tables de consommations initiales pour les usages ne
		// faisant pas l'objet de financements specifiques
		String key = tableName + LOAD_DATA;

		String request = getProperty(key);

		return jdbcTemplate.query(request, new PreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement preparedStatement) throws SQLException {
				preparedStatement.setString(1, idAgregParc);
			}
		}, new RowMapper<Parc>() {
			@Override
			public Parc mapRow(ResultSet rs, int rowNum) throws SQLException {

				Parc parc = new Parc(pasdeTemps);
				int columncount = rs.getMetaData().getColumnCount();

				for (int i = 1; i <= columncount; i++) {

					parc.setId(rs.getString("ID"));
					parc.setAnneeRenov(INIT_STATE);
					parc.setTypeRenovBat(TypeRenovBati.ETAT_INIT);
					
					// Ajout calage energies
					BigDecimal calageEnertmp = BigDecimal.ONE;
					
					if (parc.getId().substring(parc.getId().length()-2,parc.getId().length()).equals("02")){
						if(tableName.equals("Bureautique_init") || tableName.equals("Froid_alimentaire_init") 
								|| tableName.contentEquals("Eclairage_init") || tableName.contentEquals("Ventilation_init") ||
								tableName.contentEquals("Process_init")){
						calageEnertmp = CalibParameters.CalageConsoElecspe;	
						} else {
						calageEnertmp = CalibParameters.CalageConsoElecAutres;	
						}
					} else {
				    calageEnertmp = BigDecimal.ONE;	
					}
					
					parc.setAnnee(0, rs.getBigDecimal("BESOIN").multiply(calageParc,MathContext.DECIMAL32)
							.multiply(calageEnertmp,MathContext.DECIMAL32));
			
					for (int j = 1; j <= pasdeTemps; j++) {

						parc.setAnnee(j, BigDecimal.ZERO);

					}
				}
				return parc;

			}

		});
	}
}