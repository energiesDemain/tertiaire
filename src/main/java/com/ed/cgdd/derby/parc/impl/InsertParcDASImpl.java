package com.ed.cgdd.derby.parc.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;

import com.ed.cgdd.derby.model.parc.Parc;
import com.ed.cgdd.derby.parc.InsertParcDAS;

public class InsertParcDASImpl extends BddParcDAS implements InsertParcDAS {

	private final static Logger LOG = LogManager.getLogger(InsertParcDASImpl.class);

	// private final static String ID_KEY = "_DISTINCT_ID";
	// private final static String UPDATE = "_UPDATE";
	private final static String INSERT = "_INSERT";

	private JdbcTemplate jdbcTemplate;

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public void insert(String name, HashMap<String, Parc> parcMap, int pasdeTemps, int annee) {
		Parc parc;

		List<Object[]> objectInsert = new ArrayList<Object[]>();
		Object[] object = null;
		if (!name.equals("Parc_resultats")) {
			for (String parcKey : parcMap.keySet()) {
				parc = parcMap.get(parcKey);
				object = new Object[3];
				object[0] = parc.getId();
				object[1] = annee;
				object[2] = parc.getAnnee(pasdeTemps);
				objectInsert.add(object);
			}
		} else {
			for (String parcKey : parcMap.keySet()) {
				parc = parcMap.get(parcKey);
				if (annee - pasdeTemps == 2009) {
					object = new Object[3];
					object[0] = parc.getId();
					object[1] = "2009";
					object[2] = parc.getAnnee(0);
					if (parc.getAnnee(0) != null && parc.getAnnee(0).signum() != 0) {
						objectInsert.add(object);
					}
				}
				object = new Object[3];
				object[0] = parc.getId();
				object[1] = annee;
				object[2] = parc.getAnnee(pasdeTemps);
				if (parc.getAnnee(pasdeTemps) != null && parc.getAnnee(pasdeTemps).signum() != 0) {
					objectInsert.add(object);
				}

			}

			if (!objectInsert.isEmpty()) {
				String key = name + INSERT;
				String requestInsert = getProperty(key);
				jdbcTemplate.batchUpdate(requestInsert, objectInsert);
			}

			// LOG.info("Inserts Fin {}: nb insert {}", name,
			// objectInsert.size());
		}
	}

}