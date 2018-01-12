package com.ed.cgdd.derby.calibrageCINT.impl;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.ed.cgdd.derby.model.financeObjects.CalibCoutGlobal;
import com.ed.cgdd.derby.model.parc.CIntType;
import com.ed.cgdd.derby.model.parc.PeriodDetail;
import com.ed.cgdd.derby.model.parc.PeriodDetail2;

//import com.sun.org.apache.bcel.internal.generic.SWITCH;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.ed.cgdd.derby.calibrageCINT.CalibrageDAS;
import com.ed.cgdd.derby.common.CommonService;
import com.ed.cgdd.derby.model.financeObjects.CalibCI;
import com.ed.cgdd.derby.model.financeObjects.CalibCIBati;

public class CalibrageDASImpl extends BddDAS implements CalibrageDAS {

	private final String INSERT_CINT="INSERT_CINT_";
	private final String TRUNCATE_CINT="TRUNCATE_CINT_";

	private JdbcTemplate jdbcTemplate;
	private CommonService commonService;


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

	@Override
	public HashMap<String, CalibCIBati> recupCIBati() {
		HashMap<String, CalibCIBati> results = new HashMap<String, CalibCIBati>();

		String requete = getProperty("CINT_BATI_INIT_LOAD");
		List<CalibCIBati> res = jdbcTemplate.query(requete, new RowMapper<CalibCIBati>() {
			@Override
			public CalibCIBati mapRow(ResultSet rs, int rowNum) throws SQLException {

				CalibCIBati sortie = new CalibCIBati();

				sortie.setBranche(rs.getString("ID_BRANCHE"));
				sortie.setGeste(rs.getString("GESTE"));
				sortie.setChargeInit(rs.getBigDecimal("CHARGE_INIT"));
				sortie.setCoutMoy(rs.getBigDecimal("COUT_MOY"));
				sortie.setGainMoy(rs.getBigDecimal("GAIN_MOY"));
				sortie.setPartMarche(rs.getBigDecimal("PART_MARCHE"));
				sortie.setDureeVie(rs.getInt("DUREE_VIE"));

				return sortie;

			}

		});
		for (CalibCIBati calibCIBati : res) {
			results.put(createIDforCIBati(calibCIBati), calibCIBati);
		}

		return results;
	}


    protected String createIDforCIBati(CalibCIBati calibCIBati) {
		String key = calibCIBati.getBranche() + calibCIBati.getGeste();
		return key;
	}

	@Override
	public HashMap<String, CalibCI> recupCI() {
		HashMap<String, CalibCI> results = new HashMap<String, CalibCI>();

		String requete = getProperty("CINT_INIT_LOAD");

		List<CalibCI> res = jdbcTemplate.query(requete, new RowMapper<CalibCI>() {

			@Override
			public CalibCI mapRow(ResultSet rs, int rowNum) throws SQLException {

				CalibCI sortie = new CalibCI();

				sortie.setBranche(rs.getString("ID_BRANCHE"));
				sortie.setBatType(rs.getString("ID_BAT_TYPE"));

				String sysChaud = rs.getString("ID_PRODUCTION_CHAUD");

				sysPerf(sortie, sysChaud);
				sortie.setEnergies(rs.getString("ID_ENERGIE"));
				sortie.setCoutM2(rs.getBigDecimal("COUT"));
				sortie.setDureeVie(rs.getInt("DUREE_VIE"));
				sortie.setBesoinUnitaire(rs.getBigDecimal("BESOIN_U"));
				sortie.setRdt(rs.getBigDecimal("RDT"));
				sortie.setPartMarche2009(rs.getBigDecimal("PM_2009"));
				sortie.setCoutEner(rs.getBigDecimal("COUT_ENERGIE"));

				return sortie;

			}

		});
		for (CalibCI calibCI : res) {
			results.put(createIDforCI(calibCI), calibCI);
		}

		return results;
	}


	// Ajout 21092017
	@Override
	public HashMap<String, CalibCI> recupCINeuf() {
		HashMap<String, CalibCI> results = new HashMap<String, CalibCI>();

		String requete = getProperty("CINT_NEUF_INIT_LOAD");

		List<CalibCI> res = jdbcTemplate.query(requete, new RowMapper<CalibCI>() {

			@Override
			public CalibCI mapRow(ResultSet rs, int rowNum) throws SQLException {

				CalibCI sortie = new CalibCI();

				sortie.setBranche(rs.getString("ID_BRANCHE"));
				sortie.setBatType(rs.getString("ID_BAT_TYPE"));

				String sysChaud = rs.getString("ID_PRODUCTION_CHAUD");

				sysPerf(sortie, sysChaud);
				sortie.setEnergies(rs.getString("ID_ENERGIE"));
				sortie.setCoutM2(rs.getBigDecimal("COUT"));
				sortie.setDureeVie(rs.getInt("DUREE_VIE"));
				sortie.setRdt(rs.getBigDecimal("RDT"));
				sortie.setPartMarche2009(rs.getBigDecimal("PM_ENTRANT"));

				return sortie;

			}

		});
		for (CalibCI calibCI : res) {
			results.put(createIDforCI(calibCI), calibCI);
		}

		return results;
	}




	protected void sysPerf(CalibCI sortie, String sysChaud) {
		BigDecimal sysChaudTemp = new BigDecimal(sysChaud);
		if (sysChaudTemp.compareTo(new BigDecimal("20")) > 0) {
			sortie.setPerformant(true);
			String code = sysChaudTemp.subtract(new BigDecimal("20")).toString();
			if (code.length() == 1) {
				sortie.setSysteme("0" + code);
			} else {
				sortie.setSysteme(code);
			}
		} else {
			sortie.setPerformant(false);
			String code = sysChaudTemp.toString();
			if (code.length() == 1) {
				sortie.setSysteme("0" + code);
			} else {
				sortie.setSysteme(code);
			}
		}
	}

	protected String createIDforCI(CalibCI calibCI) {
		String key = calibCI.getBranche().concat(calibCI.getBatType()).concat(calibCI.getSysteme())
				.concat(calibCI.getEnergies()).concat(perfor(calibCI));
		return key;
	}

	protected String perfor(CalibCI calibCI) {
		if (calibCI.getPerformant()) {
			return "1";
		} else {
			return "0";
		}

	}


	@Override
	public void insertCInt(HashMap<String,CalibCoutGlobal> coutIntangibleMap, CIntType cIntType) {
		List<Object[]> objectInsert = new ArrayList<Object[]>();
		if (cIntType.equals(CIntType.BATI)){
			for (String key : coutIntangibleMap.keySet()) {
				Object[] object = new Object[4];
				object[0] = key;
				object[1] = key;
				object[2] = coutIntangibleMap.get(key).getCInt();
				object[3] = coutIntangibleMap.get(key).getCoutVariable();
				objectInsert.add(object);
			}
		} else {
			for (String key : coutIntangibleMap.keySet()) {
				Object[] object = new Object[6];
				object[0] = key;
				object[1] = key;
				object[2] = key;
				object[3] = key;
				object[4] = coutIntangibleMap.get(key).getCInt();
				object[5] = coutIntangibleMap.get(key).getCoutVariable();
				objectInsert.add(object);
			}

		}


		if (!objectInsert.isEmpty()) {
			// Truncate de la table avant insertion
			jdbcTemplate.update(getProperty(TRUNCATE_CINT + cIntType.toString()));
			// Insertion des valeurs
			jdbcTemplate.batchUpdate(getProperty(INSERT_CINT + cIntType.toString()), objectInsert);
		}

	}
	
	public void insertCIntDesag(HashMap<String,CalibCoutGlobal> coutIntangibleMap, CIntType cIntType) {
		List<Object[]> objectInsert = new ArrayList<Object[]>();
		if (cIntType.equals(CIntType.BATI)){
			for (String key : coutIntangibleMap.keySet()) {
				Object[] object = new Object[9];
				object[0] = key;			
				object[1] = key;
				object[2] = key;
				object[3] = key;
				object[4] = key;
				object[5] = PeriodDetail2.getEnumName(key.substring(8,10));
				object[6] = key;
				object[7] = coutIntangibleMap.get(key).getCInt();
				object[8] = coutIntangibleMap.get(key).getCoutVariable();
				objectInsert.add(object);
			}
		} else {
			for (String key : coutIntangibleMap.keySet()) {
				Object[] object = new Object[6];
				object[0] = key;
				object[1] = key;
				object[2] = key;
				object[3] = key;
				object[4] = coutIntangibleMap.get(key).getCInt();
				object[5] = coutIntangibleMap.get(key).getCoutVariable();
				objectInsert.add(object);
			}

		}


		if (!objectInsert.isEmpty()) {
			// Truncate de la table avant insertion
			jdbcTemplate.update(getProperty(TRUNCATE_CINT + cIntType.toString()));
			// Insertion des valeurs
			jdbcTemplate.batchUpdate(getProperty(INSERT_CINT + cIntType.toString() + "_DESAG"), objectInsert);
		}

	}
}
