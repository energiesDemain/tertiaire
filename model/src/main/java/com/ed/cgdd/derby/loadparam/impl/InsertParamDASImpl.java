package com.ed.cgdd.derby.loadparam.impl;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.ed.cgdd.derby.loadparam.InsertParamDAS;
import com.ed.cgdd.derby.model.excelobjects.ExcelParameters;
import com.ed.cgdd.derby.model.excelobjects.GenericExcelData;

public class InsertParamDASImpl extends BddParamDAS implements InsertParamDAS {

	private final static Logger LOG = LogManager.getLogger(InsertParamDASImpl.class);

	private final static String INSERT_KEY = "_INSERT";

	private JdbcTemplate jdbcTemplate;

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public void insert(GenericExcelData excelData, String name, ExcelParameters param) {

		String key = name + INSERT_KEY;
		String request = getProperty(key);

		executeUpdate(excelData, request);
		LOG.info("Insertion table {}", name);
	}

	private void executeUpdate(GenericExcelData excelData, String request) {
		ArrayList<ArrayList<Object>> liste = excelData.getListe();
		for (ArrayList<Object> ligne : liste) {
			NamedParameterJdbcTemplate named = new NamedParameterJdbcTemplate(jdbcTemplate);

			HashMap<String, Object> map = new HashMap<>();
			int index = 0;
			for (String name : excelData.getNames()) {
				map.put(name, ligne.get(index));
				index++;
			}
			named.update(request, map);

		}
	}

}