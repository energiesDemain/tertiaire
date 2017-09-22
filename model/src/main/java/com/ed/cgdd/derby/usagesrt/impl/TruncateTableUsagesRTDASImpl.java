package com.ed.cgdd.derby.usagesrt.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;

import com.ed.cgdd.derby.usagesrt.TruncateTableUsagesRTDAS;

public class TruncateTableUsagesRTDASImpl extends BddUsagesRTDAS implements TruncateTableUsagesRTDAS {
	private final static Logger LOG = LogManager.getLogger(TruncateTableUsagesRTDASImpl.class);

	private final static String TRUNCATE_KEY = "_TRUNCATE";

	private JdbcTemplate jdbcTemplate;

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	// Create table

	@Override
	public void truncateTable(String name) {
		String key = name + TRUNCATE_KEY;
		String request = getProperty(key);
		try {
			jdbcTemplate.execute(request);

		} catch (Exception e) {
		}

	}
}