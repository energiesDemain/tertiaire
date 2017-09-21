package com.ed.cgdd.derby.finance.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;

import com.ed.cgdd.derby.finance.TruncateTableResFinanceDAS;

public class TruncateTableResFinanceDASImpl extends BddDAS implements TruncateTableResFinanceDAS {

	private final static Logger LOG = LogManager.getLogger(TruncateTableResFinanceDASImpl.class);

	private final static String TRUNCATE_KEY = "_TRUNCATE";

	private JdbcTemplate jdbcTemplate;

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

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
