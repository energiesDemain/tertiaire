package com.ed.cgdd.derby.loadparam.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;

import com.ed.cgdd.derby.loadparam.TruncateParamDAS;

public class TruncateParamDASImpl extends BddParamDAS implements TruncateParamDAS {
	private final static Logger LOG = LogManager.getLogger(TruncateParamDASImpl.class);

	private final static String TRUNCATE_KEY = "_TRUNCATE";

	private JdbcTemplate jdbcTemplate;

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	// Truncate table

	@Override
	public void truncateParam(String name) {
		String key = name + TRUNCATE_KEY;
		String request = getProperty(key);
		try {
			jdbcTemplate.execute(request);

		} catch (Exception e) {
		}

	}
}