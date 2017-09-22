package com.ed.cgdd.test.das.impl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.ed.cgdd.test.das.CreateTableDAS;
import com.ed.cgdd.test.model.Contrainte;
import com.ed.cgdd.test.model.Determinant;
import com.ed.cgdd.test.model.LigneBat;
import com.ed.cgdd.test.model.TestMapper;

public class CreateTableDASImpl implements CreateTableDAS {
	private final static Logger LOG = LogManager.getLogger(CreateTableDASImpl.class);
	private JdbcTemplate jdbcTemplate;

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	// create table
	public void createTable() {
		jdbcTemplate.execute("CREATE TABLE TEST (ID VARCHAR(20) not null, AGE INT, TYPE VARCHAR(10), VALEUR FLOAT)");
		LOG.info("table creee");
	}

	// createtableparam utilisation d'une liste chainee pour déclarer les
	// colonnes
	public void createTableParam(String nomTable, LinkedList<Determinant> det) {
		String requete = "CREATE TABLE " + nomTable + " (";
		if (det.isEmpty()) {
			LOG.info("pas de determinants");
			return;
		}
		// initialisation
		int i = 0;
		Determinant courant = det.get(i);
		requete += " " + courant.detlib + " " + courant.type;
		i++;

		// boucle pour determinants
		while (i < det.size()) {
			courant = det.get(i);
			requete += ", " + courant.detlib + " " + courant.type;
			i++;
		}
		requete += ")";
		jdbcTemplate.execute(requete);
	}

	// delete table
	public void deleteTable(String nomTable) {
		String request = "DROP TABLE " + nomTable;
		jdbcTemplate.execute(request);
		LOG.info("table supprimee");
	}

	// table insert
	public void insertTable(LigneBat ligne) {
		jdbcTemplate.update("INSERT INTO TEST (ID, AGE, TYPE, VALEUR) VALUES(?, ?, ?, ?)", new Object[] { ligne.id,
				ligne.age, ligne.type, ligne.valeur });
		LOG.info("Ligne inseree");
	}

	// table insert parameters
	public void insertTableParam(LigneBat ligne, String nomTable) {
		Map<String, Object> parameters = new HashMap<String, Object>();

		parameters.put("ID", ligne.getId());
		parameters.put("AGE", ligne.getAge());
		parameters.put("TYPE", ligne.getType());
		parameters.put("VALEUR", ligne.getValeur());
		// LOG.info("debug " + parameters);

		String sqlInsert = "INSERT INTO " + nomTable + " (ID, AGE, TYPE, VALEUR) VALUES(:ID,:AGE,:TYPE,:VALEUR)";
		NamedParameterJdbcTemplate named = new NamedParameterJdbcTemplate(jdbcTemplate);
		named.update(sqlInsert, parameters);

		LOG.info("Ligne inseree");
	}

	public void insertTableGeneric(HashMap<String, LigneBat> data, LinkedList<Determinant> det, String nomTable) {

		// pour ecrire la requete
		int i = 0;
		Determinant courant = det.get(i);
		String sqlPart1 = "INSERT INTO " + nomTable + " (" + courant.detlib;
		String sqlPart2 = ") VALUES(:" + courant.detlib;
		i += 1;
		while (i < det.size()) {
			courant = det.get(i);
			sqlPart1 += ", " + courant.detlib;
			sqlPart2 += ", :" + courant.detlib;
			i++;
		}
		;

		String sqlInsert = sqlPart1 + sqlPart2 + ") ";
		System.out.print(sqlInsert);

		NamedParameterJdbcTemplate named = new NamedParameterJdbcTemplate(jdbcTemplate);
		Map<String, Object> parameters = new HashMap<String, Object>();
		for (String a : data.keySet()) {
			parameters.put("ID", data.get(a).getId());
			parameters.put("AGE", data.get(a).getAge());
			parameters.put("TYPE", data.get(a).getType());
			parameters.put("VALEUR", data.get(a).getValeur());
			// LOG.info(data.get(a));
			named.update(sqlInsert, parameters);
		}
		;
		LOG.debug("fin insert");
	}

	// autre tentative
	public void insertTableGenericV2(HashMap<String, LigneBat> data, LinkedList<Determinant> det, String nomTable) {

		// pour ecrire la requete
		int i = 0;
		Determinant courant = det.get(i);
		String sqlPart1 = "INSERT INTO " + nomTable + " (" + courant.detlib;
		String sqlPart2 = ") VALUES(:" + courant.detlib;
		i += 1;
		while (i < det.size()) {
			courant = det.get(i);
			sqlPart1 += ", " + courant.detlib;
			sqlPart2 += ", :" + courant.detlib;
			i++;
		}
		;

		String sqlInsert = sqlPart1 + sqlPart2 + ") ";
		System.out.print(sqlInsert);

		NamedParameterJdbcTemplate named = new NamedParameterJdbcTemplate(jdbcTemplate);
		Map<String, Object> parameters = new HashMap<String, Object>();
		for (String a : data.keySet()) {
			for (int k = 0; k < det.size(); k++) {
				parameters.put(det.get(k).detlib, data.get(a).getGeneric(det.get(k).detlib));
			}
			;
			// LOG.info(data.get(a));
			named.update(sqlInsert, parameters);
		}
		;
		LOG.debug("fin insert");
	}

	// selection de table

	public List<LigneBat> selectTable(LinkedList<Determinant> det, String nomTable) {
		// Construction du string de requete
		String requete = "SELECT";
		if (det.isEmpty()) {
			LOG.info("pas de determinants");
			return null;
		}
		// initialisation
		int i = 0;
		Determinant courant = det.get(i);
		requete += " " + courant.detlib;

		// boucle pour determinants
		i++;
		while (i < det.size()) {
			courant = det.get(i);
			requete += ", " + courant.detlib;
			i++;
		}
		requete += " FROM " + nomTable;
		LOG.info(requete);

		NamedParameterJdbcTemplate named = new NamedParameterJdbcTemplate(jdbcTemplate);

		return named.query(requete, new TestMapper());

	}

	// update

	public void updateTable(LinkedList<Determinant> det, String nomTable) {

		String requete = "UPDATE " + nomTable + " SET ";
		if (det.isEmpty()) {
			LOG.info("pas de determinants");
			return;
		}
		// initialisation
		int i = 0;
		Determinant courant = det.get(i);
		requete += " " + courant.detlib + " = " + courant.type;

		// boucle pour determinants
		i++;
		while (i < det.size()) {
			courant = det.get(i);
			requete += ", " + courant.detlib + " = " + courant.type;
		}
		LOG.info(requete);
		jdbcTemplate.execute(requete);
	}

	// update avec contrainte WHERE
	public void updateTableConstr(LinkedList<Determinant> det, LinkedList<Contrainte> constr, String nomTable) {

		String requete = "UPDATE " + nomTable + " SET ";
		if (det.isEmpty()) {
			LOG.info("pas de determinants");
			return;
		}
		// initialisation
		int i = 0;
		Determinant courant = det.get(i);
		requete += " " + courant.detlib + " = " + courant.type;

		// boucle pour determinants
		i++;
		while (i < det.size()) {
			courant = det.get(i);
			requete += ", " + courant.detlib + " = " + courant.type;
			i++;
		}

		// on ajoute les contraintes
		if (constr.isEmpty()) {
			LOG.info("pas de contrainte");
			jdbcTemplate.execute(requete);
			return;
		}
		int j = 0;
		Contrainte courantConstr = constr.get(j);
		requete += " WHERE " + courantConstr.libelle + " " + courantConstr.contrainte;

		// boucle pour determinants
		j++;
		while (j < constr.size()) {
			courantConstr = constr.get(j);
			requete += " AND " + courantConstr.libelle + " " + courantConstr.contrainte;
			j++;
		}

		LOG.info(requete);
		jdbcTemplate.execute(requete);
	}

	// pour faire un update sur un select
	public void updateSelect(String nomTable, LinkedList<Determinant> contraintes) {

	}

}
