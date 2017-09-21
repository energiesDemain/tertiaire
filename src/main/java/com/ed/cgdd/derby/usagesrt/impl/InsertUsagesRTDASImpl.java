package com.ed.cgdd.derby.usagesrt.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;

import com.ed.cgdd.derby.model.calcconso.Conso;
import com.ed.cgdd.derby.model.parc.TypeRenovSysteme;
import com.ed.cgdd.derby.usagesrt.InsertUsagesRTDAS;

public class InsertUsagesRTDASImpl extends BddUsagesRTDAS implements InsertUsagesRTDAS {

	private final static Logger LOG = LogManager.getLogger(InsertUsagesRTDASImpl.class);

	private final static String INSERT = "_INSERT";

	private JdbcTemplate jdbcTemplate;

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public void insert(String usage, String name, HashMap<String, Conso> usageMap, int pasdeTemps, int annee) {

		List<Object[]> objectInsert = new ArrayList<Object[]>();
		Object[] object = null;

		for (String parcKey : usageMap.keySet()) {
			Conso besoinUsage = usageMap.get(parcKey);
			if (annee - pasdeTemps == 2009) {
				object = new Object[4];
				object[0] = besoinUsage.getId();
				object[1] = usage;
				object[2] = "2009";
				object[3] = besoinUsage.getAnnee(0);
				if (besoinUsage.getAnnee(0) != null && (besoinUsage.getAnnee(0).signum()) != 0) {
					objectInsert.add(object);
				}
			}
			object = new Object[4];
			object[0] = besoinUsage.getId();
			object[1] = usage;
			object[2] = annee;
			object[3] = besoinUsage.getAnnee(pasdeTemps);
			if (besoinUsage.getAnnee(pasdeTemps) != null && (besoinUsage.getAnnee(pasdeTemps).signum()) != 0) {
				objectInsert.add(object);
			}

		}

		if (!objectInsert.isEmpty()) {
			String key = name + INSERT;
			String requestInsert = getProperty(key);
			jdbcTemplate.batchUpdate(requestInsert, objectInsert);
		}

		// LOG.info("Inserts Fin {}: nb insert {}", name, objectInsert.size());

	}

	@Override
	public void insertTest(String usage, String name, HashMap<String, Conso> usageMap, int pasdeTemps, int annee) {

		List<Object[]> objectInsert = new ArrayList<Object[]>();
		Object[] object = null;

		for (String parcKey : usageMap.keySet()) {
			Conso besoinUsage = usageMap.get(parcKey);
			if (annee - pasdeTemps == 2009) {
				object = new Object[6];
				object[0] = besoinUsage.getId();
				object[1] = usage;
				object[2] = "Etat initial"; // TODO EDR
				object[3] = TypeRenovSysteme.CHGT_SYS.getLabel();
				object[4] = "2009";
				object[5] = besoinUsage.getAnnee(0);
				if (besoinUsage.getAnnee(0) != null && (besoinUsage.getAnnee(0).signum()) != 0) {
					objectInsert.add(object);
				}
			}
			object = new Object[6];
			object[0] = besoinUsage.getId();
			object[1] = usage;
			object[2] = besoinUsage.getAnneeRenovSys();
			object[3] = besoinUsage.getTypeRenovSys().getLabel();
			object[4] = annee;
			object[5] = besoinUsage.getAnnee(pasdeTemps);
			if (besoinUsage.getAnnee(pasdeTemps) != null && (besoinUsage.getAnnee(pasdeTemps).signum()) != 0) {
				objectInsert.add(object);
			}

		}

		if (!objectInsert.isEmpty()) {
			String key = name + INSERT;
			String requestInsert = getProperty(key);
			jdbcTemplate.batchUpdate(requestInsert, objectInsert);
		}

		// LOG.info("Inserts Fin {}: nb insert {}", name, objectInsert.size());

	}
}