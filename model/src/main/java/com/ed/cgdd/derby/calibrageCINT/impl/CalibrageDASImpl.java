package com.ed.cgdd.derby.calibrageCINT.impl;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.ed.cgdd.derby.calibrageCINT.CalibrageDAS;
import com.ed.cgdd.derby.common.CommonService;
import com.ed.cgdd.derby.model.financeObjects.CalibCI;
import com.ed.cgdd.derby.model.financeObjects.CalibCIBati;

public class CalibrageDASImpl extends BddDAS implements CalibrageDAS {

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

	public HashMap<String, CalibCIBati> recupCIBati() {
		HashMap<String, CalibCIBati> results = new HashMap<String, CalibCIBati>();

		String requete = getProperty("CINT_BATI_INIT_LOAD");
		List<CalibCIBati> res = jdbcTemplate.query(requete, new RowMapper<CalibCIBati>() {
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

	public HashMap<String, CalibCI> recupCI() {
		HashMap<String, CalibCI> results = new HashMap<String, CalibCI>();

		String requete = getProperty("CINT_INIT_LOAD");

		List<CalibCI> res = jdbcTemplate.query(requete, new RowMapper<CalibCI>() {

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

}
