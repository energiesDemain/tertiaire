package com.ed.cgdd.derby.usagesrt.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.ed.cgdd.derby.model.calcconso.EffetRebond;
import com.ed.cgdd.derby.usagesrt.LoadTableEffetRebondDAS;

public class LoadTableEffetRebondDASImpl extends BddUsagesRTDAS implements LoadTableEffetRebondDAS {
	private final static Logger LOG = LogManager.getLogger(LoadTableEffetRebondDASImpl.class);

	private final static String LOAD_DATA = "_LOAD";

	private JdbcTemplate jdbcTemplate;

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public HashMap<String, EffetRebond> recupEffetRebond(String tableName) {

		HashMap<String, EffetRebond> effetRebond = new HashMap<String, EffetRebond>();
		List<EffetRebond> loadParam = getEffetRebond(tableName);
		for (EffetRebond valeur : loadParam) {
			effetRebond.put(valeur.getIdBranche(), valeur);
		}

		return effetRebond;
	}

	// Charge les gains atteignables sur les usages non reglementes
	protected List<EffetRebond> getEffetRebond(String tableName) {

		String key = tableName + LOAD_DATA;

		String request = getProperty(key);

		return jdbcTemplate.query(request, new RowMapper<EffetRebond>() {
			@Override
			public EffetRebond mapRow(ResultSet rs, int rowNum) throws SQLException {

				EffetRebond effetRebond = new EffetRebond();
				int columncount = rs.getMetaData().getColumnCount();

				for (int i = 1; i <= columncount; i++) {

					effetRebond.setIdBranche(rs.getString("ID_BRANCHE"));
					effetRebond.setValeur(rs.getBigDecimal("AUGMENTATION"));

				}
				return effetRebond;

			}

		});
	}

}
