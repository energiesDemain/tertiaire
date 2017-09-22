package com.ed.cgdd.derby.finance.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;

import com.ed.cgdd.derby.finance.InsertResultFinancementDAS;
import com.ed.cgdd.derby.model.financeObjects.ResFin;
import com.ed.cgdd.derby.model.financeObjects.ValeurFinancement;

public class InsertResultFinancementDASImpl extends BddDAS implements InsertResultFinancementDAS {

	private final static Logger LOG = LogManager.getLogger(InsertResultFinancementDASImpl.class);

	// private final static String ID_KEY = "_DISTINCT_ID";
	// private final static String UPDATE = "_UPDATE";
	private final static String INSERT = "_INSERT";

	private final static int START_OCCUPANT = 6;
	private final static int LENGTH_OCCUPANT = 2;

	private JdbcTemplate jdbcTemplate;

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public void insert(HashMap<ResFin, ValeurFinancement> datas) {

		List<Object[]> objectInsert = new ArrayList<Object[]>();
		Object[] object = null;

		for (ResFin resFin : datas.keySet()) {
			object = new Object[13];

			// caracteristiques
			object[0] = resFin.getIdAgregParc();
			object[1] = resFin.getBranche();
			// occupant
			object[2] = resFin.getIdAgregParc().substring(START_OCCUPANT, START_OCCUPANT + LENGTH_OCCUPANT);
			object[3] = resFin.getReglementation();
			object[4] = resFin.getTypeRenovBati().getLabel();
			object[5] = resFin.getAnneeRenovBati();
			object[6] = resFin.getTypeRenovSysteme().getLabel() + "_" + resFin.getSysChaud();
			object[7] = resFin.getAnneeRenovSysteme();

			// donnees
			object[8] = datas.get(resFin).getSurface();
			object[9] = datas.get(resFin).getAides();
			object[10] = datas.get(resFin).getCoutInvestissement();
			object[11] = datas.get(resFin).getValeurPret();
			object[12] = datas.get(resFin).getValeurPretBonif();

			if (object != null) {
				objectInsert.add(object);
			}

		}

		if (!objectInsert.isEmpty()) {
			String key = "Resultats_Financements" + INSERT;
			String requestInsert = getProperty(key);
			jdbcTemplate.batchUpdate(requestInsert, objectInsert);
		}

		LOG.info("Inserts Fin {}: nb insert {}", "Resultats_Financements", objectInsert.size());

	}

}
