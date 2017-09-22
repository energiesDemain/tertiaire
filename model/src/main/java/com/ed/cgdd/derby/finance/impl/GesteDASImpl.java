package com.ed.cgdd.derby.finance.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.ed.cgdd.derby.finance.GesteDAS;
import com.ed.cgdd.derby.model.financeObjects.Exigence;
import com.ed.cgdd.derby.model.financeObjects.Geste;
import com.ed.cgdd.derby.model.parc.TypeRenovBati;

public class GesteDASImpl extends BddDAS implements GesteDAS {
	private final static Logger LOG = LogManager.getLogger(GesteDASImpl.class);
	private final static String LOAD_DATA = "_LOAD";

	private JdbcTemplate jdbcTemplate;

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public List<Geste> getGesteBatiData(String idAggreg) {
		String requete = "SELECT id_agreg,geste, case when exigence = 'BBC renovation' then 'BBC_RENOVATION' when exigence = 'GTB' then 'GTB' else 'RT_PAR_ELEMENT' end as exigence, gain, cout, cee "
				+ "FROM GESTE_BATI WHERE SUBSTR(ID_AGREG,1,6) = '"
				+ idAggreg.substring(0, 6)
				+ "' AND COUT!=0 AND GAIN!=0";

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

	@Override
	public Map<String, List<String>> getPeriodMap() {
		HashMap<String, List<String>> periodMap = new HashMap<String, List<String>>();
		List<String[]> loadParam = getPeriods();
		for (String[] periods : loadParam) {
			String key = periods[0] + periods[1];
			if (periodMap.containsKey(key)) {
				List<String> listPeriod = periodMap.get(key);
				listPeriod.add(periods[2]);
			} else {
				List<String> listPeriod = new ArrayList<String>();
				listPeriod.add(periods[2]);
				periodMap.put(key, listPeriod);
			}
		}
		return periodMap;
	}

	protected List<String[]> getPeriods() {

		String key = "Periodes" + LOAD_DATA;

		String request = getProperty(key);

		return jdbcTemplate.query(request, new RowMapper<String[]>() {
			@Override
			public String[] mapRow(ResultSet rs, int rowNum) throws SQLException {
				String[] periods = new String[3];
				periods[0] = rs.getString("ID_BRANCHE");
				periods[1] = rs.getString("ID_BAT_TYPE");
				periods[2] = rs.getString("ID_PERIODE_DETAIL");
				return periods;

			}

		});

	}

}
