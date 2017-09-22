package com.ed.cgdd.derby.initialize.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;

import com.ed.cgdd.derby.initialize.DropTableDAS;

public class DropTableDASImpl extends BddDAS implements DropTableDAS {
	private final static Logger LOG = LogManager.getLogger(DropTableDASImpl.class);

	private final static String DROP_KEY = "_DROP";

	private JdbcTemplate jdbcTemplate;

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	// Create table

	@Override
	public void dropTable(String name) {
		String key = name + DROP_KEY;
		String request = getProperty(key);
		try {
			jdbcTemplate.execute(request);
		} catch (Exception e) {
			LOG.warn("table {} does not exist", name);
		}

	}
}