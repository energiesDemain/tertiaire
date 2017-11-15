package com.ed.cgdd.derby.usagesrt.impl;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;

import com.ed.cgdd.derby.model.CalibParameters;
import com.ed.cgdd.derby.model.calcconso.Conso;
import com.ed.cgdd.derby.model.calcconso.ParamDvChauff;
import com.ed.cgdd.derby.model.calcconso.ParamDvClim;
import com.ed.cgdd.derby.model.calcconso.ParamDvParois;
import com.ed.cgdd.derby.model.calcconso.ParamRdtCout;
import com.ed.cgdd.derby.model.calcconso.ParamTxClimExistant;
import com.ed.cgdd.derby.model.calcconso.ParamTxClimNeuf;
import com.ed.cgdd.derby.model.parc.TypeRenovBati;
import com.ed.cgdd.derby.model.parc.TypeRenovSysteme;
import com.ed.cgdd.derby.parc.ParamCalageEner;
import com.ed.cgdd.derby.usagesrt.LoadTableChauffClimDAS;

public class LoadTableChauffClimDASImpl extends BddUsagesRTDAS implements LoadTableChauffClimDAS {
	private final static Logger LOG = LogManager.getLogger(LoadTableChauffClimDASImpl.class);

	private final static String LOAD_DATA = "_LOAD";
	private final static String INIT_STATE = "Etat initial";

	private JdbcTemplate jdbcTemplate;

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	// Renvoie les champs de la table des durees de vie des systemes de
	// climatisation
	public HashMap<String, BigDecimal> loadTableDvClim(String tableName) {

		HashMap<String, BigDecimal> dvClimMap = new HashMap<String, BigDecimal>();
		List<ParamDvClim> loadParam = getParamDvClim(tableName);
		for (ParamDvClim paramDV : loadParam) {
			dvClimMap.put(paramDV.getIdSys(), paramDV.getDureeVie());
		}

		return dvClimMap;
	}

	// Renvoie les champs de la table des durees de vie des systemes de
	// chauffage
	public HashMap<String, BigDecimal> loadTableDvChauff(String tableName) {

		HashMap<String, BigDecimal> dvChauffMap = new HashMap<String, BigDecimal>();
		List<ParamDvChauff> loadParam = getParamDvChauff(tableName);
		for (ParamDvChauff paramDV : loadParam) {
			dvChauffMap.put(paramDV.getIdSys(), paramDV.getDureeVie());
		}

		return dvChauffMap;
	}

	// Renvoie les champs de la table des durees de vie des gestes
	public HashMap<TypeRenovBati, BigDecimal> loadTableDvGeste(String tableName) {

		HashMap<TypeRenovBati, BigDecimal> dvGesteMap = new HashMap<TypeRenovBati, BigDecimal>();
		List<ParamDvParois> loadParam = getParamDvGeste(tableName);
		for (ParamDvParois paramDV : loadParam) {
			dvGesteMap.put(paramDV.getGeste(), paramDV.getDureeVie());
		}

		return dvGesteMap;
	}

	// Charge les durees de vie des systemes de climatisation
	protected List<ParamDvClim> getParamDvClim(String tableName) {

		String key = tableName + LOAD_DATA;

		String request = getProperty(key);

		return jdbcTemplate.query(request, new RowMapper<ParamDvClim>() {
			@Override
			public ParamDvClim mapRow(ResultSet rs, int rowNum) throws SQLException {

				ParamDvClim dureeVie = new ParamDvClim();
				int columncount = rs.getMetaData().getColumnCount();

				for (int i = 1; i <= columncount; i++) {

					dureeVie.setIdSys(rs.getString("ID_SYS"));
					dureeVie.setDureeVie(new BigDecimal(rs.getString("DV")));

				}
				return dureeVie;

			}

		});
	}

	// Charge les durees de vie des systemes de chauffage
	protected List<ParamDvChauff> getParamDvChauff(String tableName) {

		String key = tableName + LOAD_DATA;

		String request = getProperty(key);

		return jdbcTemplate.query(request, new RowMapper<ParamDvChauff>() {
			@Override
			public ParamDvChauff mapRow(ResultSet rs, int rowNum) throws SQLException {

				ParamDvChauff dureeVie = new ParamDvChauff();
				int columncount = rs.getMetaData().getColumnCount();

				for (int i = 1; i <= columncount; i++) {

					dureeVie.setIdSys(rs.getString("ID_SYS"));
					dureeVie.setDureeVie(new BigDecimal(rs.getString("DV")));

				}
				return dureeVie;

			}

		});
	}

	// Charge les durees de vie des gestes
	protected List<ParamDvParois> getParamDvGeste(String tableName) {

		String key = tableName + LOAD_DATA;

		String request = getProperty(key);

		return jdbcTemplate.query(request, new RowMapper<ParamDvParois>() {
			@Override
			public ParamDvParois mapRow(ResultSet rs, int rowNum) throws SQLException {

				ParamDvParois dureeVie = new ParamDvParois();
				int columncount = rs.getMetaData().getColumnCount();

				for (int i = 1; i <= columncount; i++) {

					dureeVie.setGeste(TypeRenovBati.getEnumByLabel(rs.getString("GESTE")));
					dureeVie.setDureeVie(new BigDecimal(rs.getString("DV")));

				}
				return dureeVie;

			}

		});
	}

	// Renvoie les champs des tables Derby de besoins initiaux de climatisation
	// sous forme de HashMap
	public HashMap<String, Conso> loadMapResultBesoin(String tableName, final String idAgregParc, 
			final int pasdeTemps,  BigDecimal calageParc, HashMap<String, ParamCalageEner> calageEner) {

		HashMap<String, Conso> usageMap = new HashMap<String, Conso>();
		List<Conso> loadParc = getTableMap(tableName, idAgregParc, pasdeTemps, calageParc, calageEner);
		for (Conso parc : loadParc) {
			usageMap.put(
					parc.getId() + parc.getAnneeRenovSys() + parc.getTypeRenovSys() + parc.getAnneeRenov()
							+ parc.getTypeRenovBat(), parc);
		}

		return usageMap;
	}

	// Renvoie les champs des tables Derby de besoins initiaux de chauffage
	// sous forme de HashMap
	public HashMap<String, Conso> loadMapResultBesoinChauff(String tableName, final String idAgregParc,
			final int pasdeTemps,  BigDecimal calageParc, HashMap<String, ParamCalageEner> calageEner) {

		HashMap<String, Conso> usageMap = new HashMap<String, Conso>();
		List<Conso> loadParc = getTableMapChauff(tableName, idAgregParc, pasdeTemps,  calageParc, calageEner);
		for (Conso parc : loadParc) {
			usageMap.put(
					parc.getId() + parc.getAnneeRenov() + parc.getTypeRenovBat() + parc.getAnneeRenovSys()
							+ parc.getTypeRenovSys(), parc);
		}

		return usageMap;
	}

	// Renvoie les champs de la table de rendements de climatisation
	public HashMap<String, ParamRdtCout> loadTableRdtCout(String tableName) {

		HashMap<String, ParamRdtCout> rdtCoutClimMap = new HashMap<String, ParamRdtCout>();
		List<ParamRdtCout> loadParam = getParamRdtClim(tableName);
		for (ParamRdtCout paramRdt : loadParam) {
			rdtCoutClimMap.put(paramRdt.getId() + paramRdt.getPeriode(), paramRdt);

		}

		return rdtCoutClimMap;
	}

	// Charge les rendements par periode des systemes de climatisation
	protected List<ParamRdtCout> getParamRdtClim(String tableName) {

		String key = tableName + LOAD_DATA;

		String request = getProperty(key);

		return jdbcTemplate.query(request, new RowMapper<ParamRdtCout>() {
			@Override
			public ParamRdtCout mapRow(ResultSet rs, int rowNum) throws SQLException {

				ParamRdtCout rdtCoutClim = new ParamRdtCout();
				int columncount = rs.getMetaData().getColumnCount();

				for (int i = 1; i <= columncount; i++) {
					rdtCoutClim.setId(rs.getString("ID_AGREG"));
					rdtCoutClim.setPeriode(rs.getString("PERIODE"));
					rdtCoutClim.setRdt(rs.getBigDecimal("RDT"));
					rdtCoutClim.setCout(rs.getBigDecimal("COUT"));

				}
				return rdtCoutClim;

			}

		});
	}

	// Renvoie les champs de la table de rendements de chauffage avec les CEE
	public HashMap<String, ParamRdtCout> loadTableRdtCoutChauf(String tableName) {

		HashMap<String, ParamRdtCout> rdtCoutChaufMap = new HashMap<String, ParamRdtCout>();
		List<ParamRdtCout> loadParam = getParamRdtChauf(tableName);
		for (ParamRdtCout paramRdt : loadParam) {
			rdtCoutChaufMap.put(paramRdt.getId() + paramRdt.getPeriode(), paramRdt);

		}

		return rdtCoutChaufMap;
	}

	// Charge les rendements par periode des systemes de chauffage avec CEE
	protected List<ParamRdtCout> getParamRdtChauf(String tableName) {

		String key = tableName + LOAD_DATA;

		String request = getProperty(key);

		return jdbcTemplate.query(request, new RowMapper<ParamRdtCout>() {
			@Override
			public ParamRdtCout mapRow(ResultSet rs, int rowNum) throws SQLException {

				ParamRdtCout rdtCoutChauf = new ParamRdtCout();
				int columncount = rs.getMetaData().getColumnCount();

				for (int i = 1; i <= columncount; i++) {
					rdtCoutChauf.setId(rs.getString("ID_AGREG"));
					rdtCoutChauf.setPeriode(rs.getString("PERIODE"));
					rdtCoutChauf.setRdt(rs.getBigDecimal("RDT"));
					rdtCoutChauf.setCout(rs.getBigDecimal("COUT"));
					rdtCoutChauf.setCEE(rs.getBigDecimal("CEE"));

				}
				return rdtCoutChauf;

			}

		});
	}

	// Renvoie les champs de la table des taux de clim dans l'existant
	public HashMap<String, ParamTxClimExistant> loadTableTxClimExistant(String tableName) {

		HashMap<String, ParamTxClimExistant> txClimExistantMap = new HashMap<String, ParamTxClimExistant>();
		List<ParamTxClimExistant> loadParam = getParamTxClimExistant(tableName);
		for (ParamTxClimExistant paramRdt : loadParam) {
			txClimExistantMap.put(paramRdt.getIdbranche(), paramRdt);

		}

		return txClimExistantMap;
	}

	// Charge les taux de climatisation annuels pour les batiments existants
	protected List<ParamTxClimExistant> getParamTxClimExistant(String tableName) {

		String key = tableName + LOAD_DATA;

		String request = getProperty(key);

		return jdbcTemplate.query(request, new RowMapper<ParamTxClimExistant>() {
			@Override
			public ParamTxClimExistant mapRow(ResultSet rs, int rowNum) throws SQLException {

				ParamTxClimExistant txClimExistant = new ParamTxClimExistant();
				int columncount = rs.getMetaData().getColumnCount();

				for (int i = 1; i <= columncount; i++) {
					txClimExistant.setIdbranche(rs.getString("ID_BRANCHE"));
					txClimExistant.setTx(0, rs.getBigDecimal("PERIODE0"));
					txClimExistant.setTx(1, rs.getBigDecimal("PERIODE1"));
					txClimExistant.setTx(2, rs.getBigDecimal("PERIODE2"));
					txClimExistant.setTx(3, rs.getBigDecimal("PERIODE3"));
					txClimExistant.setTx(4, rs.getBigDecimal("PERIODE4"));
					txClimExistant.setTx(5, rs.getBigDecimal("PERIODE5"));
				}
				return txClimExistant;

			}

		});
	}

	// Renvoie les champs de la table des taux de clim dans le neuf
	public HashMap<String, ParamTxClimNeuf> loadTableTxClimNeuf(String tableName) {

		HashMap<String, ParamTxClimNeuf> txClimNeufMap = new HashMap<String, ParamTxClimNeuf>();
		List<ParamTxClimNeuf> loadParam = getParamTxClimNeuf(tableName);
		for (ParamTxClimNeuf paramRdt : loadParam) {
			txClimNeufMap.put(paramRdt.getIdbranche(), paramRdt);

		}

		return txClimNeufMap;
	}

	// Charge les taux de climatisation annuels pour les batiments neufs
	protected List<ParamTxClimNeuf> getParamTxClimNeuf(String tableName) {

		String key = tableName + LOAD_DATA;

		String request = getProperty(key);

		return jdbcTemplate.query(request, new RowMapper<ParamTxClimNeuf>() {
			@Override
			public ParamTxClimNeuf mapRow(ResultSet rs, int rowNum) throws SQLException {

				ParamTxClimNeuf txClimNeuf = new ParamTxClimNeuf();
				int columncount = rs.getMetaData().getColumnCount();

				for (int i = 1; i <= columncount; i++) {
					txClimNeuf.setIdbranche(rs.getString("ID_BRANCHE"));
					txClimNeuf.setTx(1, rs.getBigDecimal("PERIODE1"));
					txClimNeuf.setTx(2, rs.getBigDecimal("PERIODE2"));
					txClimNeuf.setTx(3, rs.getBigDecimal("PERIODE3"));
					txClimNeuf.setTx(4, rs.getBigDecimal("PERIODE4"));
					txClimNeuf.setTx(5, rs.getBigDecimal("PERIODE5"));
				}
				return txClimNeuf;

			}

		});
	}

	protected List<Conso> getTableMap(String tableName, final String idAgregParc, final int pasdeTemps, BigDecimal calageParc, 
			HashMap<String, ParamCalageEner> calageEner) {

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

				Conso parc = new Conso(pasdeTemps);
				int columncount = rs.getMetaData().getColumnCount();

				for (int i = 1; i <= columncount; i++) {

					parc.setId(rs.getString("ID"));
					
					parc.setAnneeRenov(INIT_STATE);
					parc.setTypeRenovBat(TypeRenovBati.ETAT_INIT);
					parc.setAnneeRenovSys(INIT_STATE);
					parc.setTypeRenovSys(TypeRenovSysteme.ETAT_INIT);
					
					// Ajout calage energies
					BigDecimal calageEnertmp = BigDecimal.ONE;
					// On recale seulement les besoins de chauffage (ID = longueur 18) pas la clim
					if (parc.getId().length() == 18){
					calageEnertmp = calageEner.get(parc.getId().substring(16,18)).getFacteurCalageParc()
							.multiply(calageEner.get(parc.getId().substring(16,18)).getFacteurCalageConso(),
									MathContext.DECIMAL32);
					} else if (parc.getId().substring(parc.getId().length()-2,parc.getId().length()).equals("02")){
						calageEnertmp = CalibParameters.CalageConsoHorsChauffElec;	
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

	protected List<Conso> getTableMapChauff(String tableName, final String idAgregParc, final int pasdeTemps,  
			BigDecimal calageParc, HashMap<String, ParamCalageEner> calageEner) {

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

				Conso parc = new Conso(pasdeTemps);
				int columncount = rs.getMetaData().getColumnCount();

				for (int i = 1; i <= columncount; i++) {

					parc.setId(rs.getString("ID"));
					parc.setAnneeRenov(INIT_STATE);
					parc.setTypeRenovBat(TypeRenovBati.ETAT_INIT);
					parc.setAnneeRenovSys(INIT_STATE);
					parc.setTypeRenovSys(TypeRenovSysteme.ETAT_INIT);
					
					// Ajout calage energies
					BigDecimal calageEnertmp = BigDecimal.ONE;
					// On recale seulement les besoins de chauffage (ID = longueur 18) pas la clim
					if (parc.getId().length() == 18){
					calageEnertmp = calageEner.get(parc.getId().substring(16,18)).getFacteurCalageParc()
							.multiply(calageEner.get(parc.getId().substring(16,18)).getFacteurCalageConso(),MathContext.DECIMAL32);
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