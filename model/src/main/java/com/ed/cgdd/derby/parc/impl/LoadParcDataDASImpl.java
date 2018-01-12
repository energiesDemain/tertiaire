package com.ed.cgdd.derby.parc.impl;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.ed.cgdd.derby.model.parc.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;

import com.ed.cgdd.derby.parc.LoadParcDataDAS;

public class LoadParcDataDASImpl extends BddParcDAS implements LoadParcDataDAS {
	private final static Logger LOG = LogManager.getLogger(LoadParcDataDASImpl.class);

	private final static String LOAD_DATA = "_LOAD";

	private JdbcTemplate jdbcTemplate;

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	// Create table

	@Override
	public List<ParamParcArray> getParamEntreesMapper() {

		// Parametres d'entrees dans le parc

		String key = "Entrees" + LOAD_DATA;

		String request = getProperty(key);
		
		return jdbcTemplate.query(request, new RowMapper<ParamParcArray>() {
			@Override
			public ParamParcArray mapRow(ResultSet rs, int rowNum) throws SQLException {

				ParamParcArray entrees = new ParamParcArray();
				int columncount = rs.getMetaData().getColumnCount();
				for (int i = 1; i <= columncount; i++) {
					// String label = rs.getMetaData().getColumnLabel(i);
					entrees.setBranche(rs.getString("ID_BRANCHE"));
					entrees.setPeriode(1, rs.getBigDecimal("PERIODE1"));
					entrees.setPeriode(2, rs.getBigDecimal("PERIODE2"));
					entrees.setPeriode(3, rs.getBigDecimal("PERIODE3"));
					entrees.setPeriode(4, rs.getBigDecimal("PERIODE4"));
					entrees.setPeriode(5, rs.getBigDecimal("PERIODE5"));
					
				}
				return entrees;

			}

		});
	}

	@Override
	public List<ParamParcArray> getParamSortiesMapper() {

		// Parametres de sorties du parc

		String key = "Sorties" + LOAD_DATA;

		String request = getProperty(key);

		return jdbcTemplate.query(request, new RowMapper<ParamParcArray>() {
			@Override
			public ParamParcArray mapRow(ResultSet rs, int rowNum) throws SQLException {

				ParamParcArray sorties = new ParamParcArray();
				int columncount = rs.getMetaData().getColumnCount();
				for (int i = 1; i <= columncount; i++) {
					// String label = rs.getMetaData().getColumnLabel(i);
					sorties.setBranche(rs.getString("ID_BRANCHE"));
					sorties.setPeriode(1, rs.getBigDecimal("PERIODE1"));
					sorties.setPeriode(2, rs.getBigDecimal("PERIODE2"));
					sorties.setPeriode(3, rs.getBigDecimal("PERIODE3"));
					sorties.setPeriode(4, rs.getBigDecimal("PERIODE4"));
					sorties.setPeriode(5, rs.getBigDecimal("PERIODE5"));
					
				}
				return sorties;

			}

		});
	}

	@Override
	public List<Parc> getParamParcMapper(final String idAgregParc, final int pasdeTemps) {

		// Load parc

		String key = "Parc_init" + LOAD_DATA;

		String request = getProperty(key);

		return jdbcTemplate.query(request, new PreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement preparedStatement) throws SQLException {
				preparedStatement.setString(1, idAgregParc);
			}
		}, new RowMapper<Parc>() {
			@Override
			public Parc mapRow(ResultSet rs, int rowNum) throws SQLException {

				Parc parc = new Parc(pasdeTemps);
				int columncount = rs.getMetaData().getColumnCount();

				for (int i = 1; i <= columncount; i++) {

					parc.setId(rs.getString("ID"));
					parc.setAnneeRenov("Etat initial");
					parc.setTypeRenovBat(TypeRenovBati.ETAT_INIT);
					parc.setAnneeRenovSys("Etat initial");
					parc.setTypeRenovSys(TypeRenovSysteme.ETAT_INIT);
					parc.setAnnee(0, rs.getBigDecimal("SURFACES"));

					for (int j = 1; j <= pasdeTemps; j++) {

						parc.setAnnee(j, new BigDecimal("0"));

					}
				}
				return parc;

			}

		});
	}

	@Override
	public List<String> getParamParcListeMapper() {

		// Load parc

		String key = "Parc_init_liste_id";

		String request = getProperty(key);

		return jdbcTemplate.query(request, new RowMapper<String>() {
			@Override
			public String mapRow(ResultSet rs, int rowNum) throws SQLException {

				int columncount = rs.getMetaData().getColumnCount();
				String id = new String();
				for (int i = 1; i <= columncount; i++) {

					id = rs.getString("ID");

				}
				return id;

			}

		});
	}

}