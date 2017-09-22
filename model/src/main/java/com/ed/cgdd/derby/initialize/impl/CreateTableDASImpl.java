package com.ed.cgdd.derby.initialize.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;

import com.ed.cgdd.derby.initialize.CreateTableDAS;

public class CreateTableDASImpl extends BddDAS implements CreateTableDAS {
	private final static Logger LOG = LogManager.getLogger(CreateTableDASImpl.class);

	private final static String CREATE_KEY = "_CREATE";

	private JdbcTemplate jdbcTemplate;

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	// Create table

	@Override
	public void createTable(String name) {
		String key = name + CREATE_KEY;
		String request = getProperty(key);
		jdbcTemplate.execute(request);

	}

}
