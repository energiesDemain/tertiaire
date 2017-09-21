package com.ed.cgdd.derby.initialize.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;

import com.ed.cgdd.derby.initialize.PkeyDAS;

public class PkeyDASImpl extends BddDAS implements PkeyDAS {
	private final static Logger LOG = LogManager.getLogger(PkeyDASImpl.class);

	private final static String ALTER_KEY = "_PKEY";

	private JdbcTemplate jdbcTemplate;

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	// Create table

	@Override
	public void pkey(String name) {
		String key = name + ALTER_KEY;
		String request = getProperty(key);
		jdbcTemplate.execute(request);

	}

}
