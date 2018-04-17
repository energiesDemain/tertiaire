package com.ed.cgdd.derby.excelresult;

import com.ed.cgdd.derby.excelresult.CSVService;
import com.ed.cgdd.derby.model.excelobjects.BesoinConsoForCSV;
import com.ed.cgdd.derby.model.parc.ConsommationResultatsAnnee;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;



public class CSVServiceImpl implements CSVService {

	private final static Logger LOG = LogManager.getLogger(CSVServiceImpl.class);


	private JdbcTemplate jdbcTemplate;

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}


	@Override
	public void csvService(int pasTemps) {
		try {

			LOG.info("csv Parc");
			getParcAnneeCSV(pasTemps);
			LOG.info("csv Conso non RT");
			getConsoAnneeCSV(pasTemps);
			LOG.info("csv Conso RT");
			getConsoRTAnneeCSV(pasTemps);
			LOG.info("csv GES");
			getGESAnneeCSV(pasTemps);
			LOG.info("csv Besoin/Conso RT");
			getBesoinAnneeCSV(pasTemps);
			LOG.info("csv Besoin/Conso RT avec systemes");
			getBesoinAnneeSystCSV(pasTemps);
			LOG.info("csv Conso non RT");
			getConsoNonRTAnneeCSV(pasTemps);
			LOG.info("Surfaces climatisees");
			getClimAnnee(pasTemps);
			LOG.info("csv couts");
			getCoutAnnee(pasTemps);
			LOG.info("csv parts de marche");
			getPM(pasTemps);
//			LOG.info("csv etiquettes");
//			getEtiquetteCSV(pasTemps);

		} catch (SQLException ex) {
			LOG.error(ex);
		} catch (IOException e) {
			LOG.error(e);
		}

	}
	public void getPM(int pasTemps) throws SQLException, IOException {

		OutputStream os = new FileOutputStream("./Result_csv/part_marche.csv");
		PrintWriter out = new PrintWriter(new OutputStreamWriter(os, "UTF-8"));
		CSVPrinter printer = CSVFormat.DEFAULT.withDelimiter(';').withHeader("annee","branche",
				"periodeSimple", "energieChauffage","systeme_chauff", "surface").print(out);
		List<ConsommationResultatsAnnee> list = getPM();
		for (ConsommationResultatsAnnee rs : list) {
			List record = new ArrayList<>();
			record.add(rs.getAnnee());
			record.add(rs.getBranche());
			record.add(rs.getPeriodeSimple());
			record.add(rs.getEnergieUsage());
			record.add(rs.getSystemeChaud());
			record.add(rs.getSurface());
			printer.printRecord(record);
		}
		out.flush();
		out.close();
		printer.close();
		os.close();
	}

	private List<ConsommationResultatsAnnee> getPM() {

		String requestSelect = "select  annee,substr(id, 1,2) as branche, " +
				"substr(id, 11,2) as periodeSimple, substr(id,length(id)-1,2) as energieChauffage, " +
				"substr(id,13,2) as system_chauff,sum(surfaces) as surface " +
				"from parc_resultats  " +
				"group by  annee,substr(id, 1,2) , substr(id, 11,2) ,  substr(id,length(id)-1,2),substr(id, 13,2)";

		return jdbcTemplate.query(requestSelect, new RowMapper<ConsommationResultatsAnnee>() {
			@Override
			public ConsommationResultatsAnnee mapRow(ResultSet rs, int rowNum) throws SQLException {
				ConsommationResultatsAnnee sorties = new ConsommationResultatsAnnee();
				int columncount = rs.getMetaData().getColumnCount();
	//			for (int k = 1; k <= columncount; k++) {
					sorties.setAnnee(rs.getInt("annee"));
					sorties.setBranche(rs.getString("branche"));
					sorties.setPeriodeSimple(rs.getString("periodeSimple"));
					sorties.setEnergieUsage(rs.getString("energieChauffage"));
					sorties.setSystemeChaud(rs.getString("system_chauff"));
					sorties.setSurface(rs.getDouble("surface"));
	//				}
				return sorties;
			};
		});
	}



			public void getEtiquetteCSV(int pasTemps) throws SQLException, IOException {

		OutputStream os = new FileOutputStream("./Result_csv/etiquette.csv");
		PrintWriter out = new PrintWriter(new OutputStreamWriter(os, "UTF-8"));
		CSVPrinter printer = CSVFormat.DEFAULT.withDelimiter(';').withHeader("annee","branche",
				"ss_branche","bat_type","occupant","periode_detail","periode_simple",
				"systeme_chauff", "systeme_froid","surface", "conso_u",
				"etiquette").print(out);
		List<ConsommationResultatsAnnee> list = getEtiquette();
		for (ConsommationResultatsAnnee rs : list) {
			List record = new ArrayList<>();
			record.add(rs.getAnnee());
			record.add(rs.getBranche());
			record.add(rs.getSousBranche());
			record.add(rs.getBatimentType());
			record.add(rs.getOccupation());
			record.add(rs.getPeriodeDetail());
			record.add(rs.getPeriodeSimple());
			record.add(rs.getSystemeChaud());
			record.add(rs.getSystemeFroid());
			record.add(rs.getSurface());
			record.add(rs.getConsommationUnitaire());
			record.add(rs.getEtiquette());
			printer.printRecord(record);
		}
		out.flush();
		out.close();
		printer.close();
		os.close();
	}



	public void getCoutAnnee(int pasTemps) throws SQLException, IOException {

		OutputStream os = new FileOutputStream("./Result_csv/couts.csv");
		PrintWriter out = new PrintWriter(new OutputStreamWriter(os, "UTF-8"));
		CSVPrinter printer = CSVFormat.DEFAULT.withDelimiter(';').withHeader("branche",
				"occupant",
				"typeRenovationBatiment", "typeRenovationSysteme","cible", "annee",
				"surface", "aides",
				"investissement", "prets", "pretsBonifies", "reglementation").print(out);
		List<ConsommationResultatsAnnee> list = getCout(pasTemps);
		for (ConsommationResultatsAnnee rs : list) {
			List record = new ArrayList<>();
			record.add(rs.getBranche());
			record.add(rs.getOccupation());
			record.add(rs.getTypeRenovationBati());
			record.add(rs.getTypeRenovationSysteme());
			record.add(rs.getCible());
			record.add(rs.getAnnee());
			record.add(rs.getSurface());
			record.add(rs.getAides());
			record.add(rs.getInvestissement());
			record.add(rs.getPrets());
			record.add(rs.getPretsBonifies());
			record.add(rs.getReglementation());
			printer.printRecord(record);
		}
		out.flush();
		out.close();
		printer.close();
		os.close();


		// COUTS AUTRES

		os = new FileOutputStream("./Result_csv/coutsSystemesAutres.csv");
		out = new PrintWriter(new OutputStreamWriter(os, "UTF-8"));
		printer = CSVFormat.DEFAULT.withDelimiter(';').withHeader("branche",
				"occupant",
				"typeRenovationBatiment", "typeRenovationSysteme","cible", "annee",
				"surface", "aides",
				"investissement", "prets", "pretsBonifies", "reglementation").print(out);
		list = getCoutSystemesAutres(pasTemps);
		for (ConsommationResultatsAnnee rs : list) {
			List record = new ArrayList<>();
			record.add(rs.getBranche());
			record.add(rs.getOccupation());
			record.add(rs.getTypeRenovationBati());
			record.add(rs.getTypeRenovationSysteme());
			record.add(rs.getCible());
			record.add(rs.getAnnee());
			record.add(rs.getSurface());
			record.add(rs.getAides());
			record.add(rs.getInvestissement());
			record.add(rs.getPrets());
			record.add(rs.getPretsBonifies());
			record.add(rs.getReglementation());
			printer.printRecord(record);
		}
		out.flush();
		out.close();
		printer.close();
		os.close();

	}



	protected void getParcAnneeCSV(int pasTemps) throws SQLException, IOException {
		OutputStream os = new FileOutputStream("./Result_csv/parcAnnee.csv");
		PrintWriter out = new PrintWriter(new OutputStreamWriter(os, "UTF-8"));
		CSVPrinter printer = CSVFormat.DEFAULT.withDelimiter(';').withHeader("branche",
				"occupation",
				"periodeSimple", "energieChauffage","annee2009", "annee2010", "annee2011", "annee2012",
				"annee2013", "annee2014", "annee2015", "annee2016", "annee2017",
				"annee2018", "annee2019", "annee2020", "annee2021", "annee2022",
				"annee2023", "annee2024", "annee2025", "annee2026", "annee2027",
				"annee2028", "annee2029", "annee2030", "annee2031", "annee2032",
				"annee2033", "annee2034", "annee2035", "annee2036", "annee2037",
				"annee2038", "annee2039", "annee2040", "annee2041", "annee2042",
				"annee2043", "annee2044", "annee2045", "annee2046", "annee2047",
				"annee2048", "annee2049", "annee2050").print(out);
		List<ConsommationResultatsAnnee> list = getParcAnnee(pasTemps);
		for (ConsommationResultatsAnnee rs : list) {
			List record = new ArrayList<>();
			record.add(rs.getBranche());
			record.add(rs.getOccupation());
			record.add(rs.getPeriodeSimple());
			record.add(rs.getEnergieUsage());
			for (int i = 0; i < rs.getAnnees().length; i++) {
				record.add(rs.getAnnees(i));
			}
			printer.printRecord(record);
		}
		out.flush();
		out.close();
		printer.close();
		os.close();


	}



	protected void getGESAnneeCSV(int pasTemps) throws SQLException, IOException {
		OutputStream os = new FileOutputStream("./Result_csv/GESAnnee.csv");
		PrintWriter out = new PrintWriter(new OutputStreamWriter(os, "UTF-8"));
		CSVPrinter printer = CSVFormat.DEFAULT.withDelimiter(';').withHeader("branche",
//				"sousBranche",
				"occupation",
				"periodeSimple", "energieUsage","usage","usageSimple","annee2009", "annee2010", "annee2011", "annee2012",
				"annee2013", "annee2014", "annee2015", "annee2016", "annee2017",
				"annee2018", "annee2019", "annee2020", "annee2021", "annee2022",
				"annee2023", "annee2024", "annee2025", "annee2026", "annee2027",
				"annee2028", "annee2029", "annee2030", "annee2031", "annee2032",
				"annee2033", "annee2034", "annee2035", "annee2036", "annee2037",
				"annee2038", "annee2039", "annee2040", "annee2041", "annee2042",
				"annee2043", "annee2044", "annee2045", "annee2046", "annee2047",
				"annee2048", "annee2049", "annee2050").print(out);
		List<ConsommationResultatsAnnee> list = getGESAnnee(pasTemps);
		for (ConsommationResultatsAnnee rs : list) {
			List record = new ArrayList<>();
			record.add(rs.getBranche());
//			record.add(rs.getSousBranche());
			record.add(rs.getOccupation());
			record.add(rs.getPeriodeSimple());
			record.add(rs.getEnergieUsage());
			record.add(rs.getUsage());
			record.add(rs.getUsageSimple());
			for (int i = 0; i < rs.getAnnees().length; i++) {
				record.add(rs.getAnnees(i));
			}
			printer.printRecord(record);
		}
		out.flush();
		out.close();
		printer.close();
		os.close();
	}



	protected void getConsoAnneeCSV(int pasTemps) throws SQLException, IOException {
		OutputStream os = new FileOutputStream("./Result_csv/consoAnnee.csv");
		PrintWriter out = new PrintWriter(new OutputStreamWriter(os, "UTF-8"));
		CSVPrinter printer = CSVFormat.DEFAULT.withDelimiter(';').withHeader("branche",
	//			"sousBranche",
				"occupation",
				"periodeSimple", "energieUsage", "usage", "usageSimple",
				"facteurEnergiePrimaire", "annee2009", "annee2010", "annee2011", "annee2012",
				"annee2013", "annee2014", "annee2015", "annee2016", "annee2017",
				"annee2018", "annee2019", "annee2020", "annee2021", "annee2022",
				"annee2023", "annee2024", "annee2025", "annee2026", "annee2027",
				"annee2028", "annee2029", "annee2030", "annee2031", "annee2032",
				"annee2033", "annee2034", "annee2035", "annee2036", "annee2037",
				"annee2038", "annee2039", "annee2040", "annee2041", "annee2042",
				"annee2043", "annee2044", "annee2045", "annee2046", "annee2047",
				"annee2048", "annee2049", "annee2050").print(out);
		List<ConsommationResultatsAnnee> list = getConsoAnnee(pasTemps);
		for (ConsommationResultatsAnnee rs : list) {
			List record = new ArrayList<>();
			record.add(rs.getBranche());
//			record.add(rs.getSousBranche());
			record.add(rs.getOccupation());
			record.add(rs.getPeriodeSimple());
			record.add(rs.getEnergieUsage());
			record.add(rs.getUsage());
			record.add(rs.getUsageSimple());
			record.add(rs.getFacteurEnergiePrimaire());
			for (int i = 0; i < rs.getAnnees().length; i++) {
				record.add(rs.getAnnees(i));
			}
			printer.printRecord(record);
		}
		out.flush();
		out.close();
		printer.close();
		os.close();
	}
	
	protected void getConsoRTAnneeCSV(int pasTemps) throws SQLException, IOException {
		OutputStream os = new FileOutputStream("./Result_csv/consoRTAnnee.csv");
		PrintWriter out = new PrintWriter(new OutputStreamWriter(os, "UTF-8"));
		CSVPrinter printer = CSVFormat.DEFAULT.withDelimiter(';').withHeader("branche",
	//			"sousBranche",
				"occupation",
				"periodeSimple", "energieUsage", "usage", "usageSimple",
				"facteurEnergiePrimaire", "annee2009", "annee2010", "annee2011", "annee2012",
				"annee2013", "annee2014", "annee2015", "annee2016", "annee2017",
				"annee2018", "annee2019", "annee2020", "annee2021", "annee2022",
				"annee2023", "annee2024", "annee2025", "annee2026", "annee2027",
				"annee2028", "annee2029", "annee2030", "annee2031", "annee2032",
				"annee2033", "annee2034", "annee2035", "annee2036", "annee2037",
				"annee2038", "annee2039", "annee2040", "annee2041", "annee2042",
				"annee2043", "annee2044", "annee2045", "annee2046", "annee2047",
				"annee2048", "annee2049", "annee2050").print(out);
		List<ConsommationResultatsAnnee> list = getConsoRTAnnee(pasTemps);
		for (ConsommationResultatsAnnee rs : list) {
			List record = new ArrayList<>();
			record.add(rs.getBranche());
//			record.add(rs.getSousBranche());
			record.add(rs.getOccupation());
			record.add(rs.getPeriodeSimple());
			record.add(rs.getEnergieUsage());
			record.add(rs.getUsage());
			record.add(rs.getUsageSimple());
			record.add(rs.getFacteurEnergiePrimaire());
			for (int i = 0; i < rs.getAnnees().length; i++) {
				record.add(rs.getAnnees(i));
			}
			printer.printRecord(record);
		}
		out.flush();
		out.close();
		printer.close();
		os.close();
	}
	protected void getBesoinAnneeCSV(int pasTemps) throws SQLException, IOException {
		OutputStream os = new FileOutputStream("./Result_csv/export_BESOIN_RT_PERIODE.csv");
		PrintWriter out = new PrintWriter(new OutputStreamWriter(os, "UTF-8"));
		CSVPrinter printer = CSVFormat.DEFAULT.withDelimiter(';').withHeader("COD_BRANCHE",
				"ANNEE", "COD_PERIODE_SIMPLE", "USAGE","COD_ENERGIE","CONSO_TOT","BESOIN_TOT"
		).print(out);
		List<BesoinConsoForCSV> list = getBesoinAnnee(pasTemps);
		for (BesoinConsoForCSV rs : list) {
			List record = new ArrayList<>();
			record.add(rs.getBranche());
			record.add(rs.getAnnee());
			record.add(rs.getCodePeriodeSimple());
			record.add(rs.getUsage());
			record.add(rs.getEnergie());
			record.add(rs.getConsoTot());
			record.add(rs.getBesoinTot());
			printer.printRecord(record);
		}
		out.flush();
		out.close();
		printer.close();
		os.close();

	}

	protected void getBesoinAnneeSystCSV(int pasTemps) throws SQLException, IOException {
		OutputStream os = new FileOutputStream("./Result_csv/export_BESOIN_RT_PERIODE_SYST.csv");
		PrintWriter out = new PrintWriter(new OutputStreamWriter(os, "UTF-8"));
		CSVPrinter printer = CSVFormat.DEFAULT.withDelimiter(';').withHeader("COD_BRANCHE",
				"ANNEE", "COD_PERIODE_SIMPLE", "USAGE","COD_SYSTEME_CHAUD","COD_ENERGIE","CONSO_TOT","BESOIN_TOT"
		).print(out);
		List<BesoinConsoForCSV> list = getBesoinAnneeSyst(pasTemps);
		
		for (BesoinConsoForCSV rs : list) {
			List record = new ArrayList<>();
			record.add(rs.getBranche());
			record.add(rs.getAnnee());
			record.add(rs.getCodePeriodeSimple());
			record.add(rs.getUsage());
			record.add(rs.getSystemChaud());
			record.add(rs.getEnergie());
			record.add(rs.getConsoTot());
			record.add(rs.getBesoinTot());
			printer.printRecord(record);
		}
		out.flush();
		out.close();
		printer.close();
		os.close();

	}
	protected void getConsoNonRTAnneeCSV(int pasTemps) throws SQLException, IOException {
		OutputStream os = new FileOutputStream("./Result_csv/export_CONSO_NON_RT_PERIODE.csv");
		PrintWriter out = new PrintWriter(new OutputStreamWriter(os, "UTF-8"));
		CSVPrinter printer = CSVFormat.DEFAULT.withDelimiter(';').withHeader("COD_BRANCHE",
				"ANNEE", "COD_PERIODE_SIMPLE", "USAGE","COD_ENERGIE","CONSO_TOT"
		).print(out);
		List<BesoinConsoForCSV> list = getConsononRTAnnee(pasTemps);
		for (BesoinConsoForCSV rs : list) {
			List record = new ArrayList<>();
			record.add(rs.getBranche());
			record.add(rs.getAnnee());
			record.add(rs.getCodePeriodeSimple());
			record.add(rs.getUsage());
			record.add(rs.getEnergie());
			record.add(rs.getConsoTot());
			printer.printRecord(record);
		}
		out.flush();
		out.close();
		printer.close();
		os.close();

	}

	protected void getClimAnnee(int pasTemps) throws SQLException, IOException {
		OutputStream os = new FileOutputStream("./Result_csv/export_SURFACES_CLIM.csv");
		PrintWriter out = new PrintWriter(new OutputStreamWriter(os, "UTF-8"));
		CSVPrinter printer = CSVFormat.DEFAULT.withDelimiter(';').withHeader("COD_BRANCHE",
				"ANNEE", "COD_PERIODE_SIMPLE", "COD_SYSTEME_FROID","SURFACES_TOT"
		).print(out);
		List<BesoinConsoForCSV> list = getSurfaceClimAnnee(pasTemps);
		for (BesoinConsoForCSV rs : list) {
			List record = new ArrayList<>();
			record.add(rs.getBranche());
			record.add(rs.getAnnee());
			record.add(rs.getCodePeriodeSimple());
			record.add(rs.getSystemFroid());
			record.add(rs.getSurfaceTot());
			printer.printRecord(record);
		}
		out.flush();
		out.close();
		printer.close();
		os.close();
	}

	protected List<ConsommationResultatsAnnee> getConsoAnnee(final int pasTemps) {

		String requestSelect = "select substr(id, 1,2) as branche,"
				+ "substr(id, 7,2) as occupation, substr(id, 11,2) as periodeSimple,"
				+ " substr(id,13,2) as energieUsage, usage, case when usage in	 ('Eclairage','Ventilation',"
				+ " 'Bureautique','Froid_alimentaire', 'Process','Auxiliaires') then 'Electricité Spécifique' "
				+ "	 else usage end as usageSimple,  case when usage in	('Bureautique','Froid_alimentaire', 'Process') then 2.58 "

				+ "when substr(id,13,2)='02' then 2.58 else 1 end as FEP,"

				+ " sum(case when annee='2009' then consommation_ef else 0 end) as annee2009, ";

		for (int cursorAnnee = 2010; cursorAnnee < 2050; cursorAnnee = cursorAnnee + pasTemps) {
			requestSelect = requestSelect + "sum(case when annee='" + cursorAnnee
					+ "' then consommation_ef else 0 end) as annee" + cursorAnnee + ", ";
		}

		requestSelect = requestSelect + "	sum(case when annee='2050' then consommation_ef else 0 end) as annee2050 "
				+ " from conso_non_rt_resultats 	 group by "

				+ "	 substr(id, 1,2), substr(id,7,2),	 substr(id, 11,2), substr(id,13,2), usage"

				+ " order by 1, 2 , 3, 4, 5, 6 ";
		LOG.debug(requestSelect);
		return jdbcTemplate.query(requestSelect, new RowMapper<ConsommationResultatsAnnee>() {
			@Override
			public ConsommationResultatsAnnee mapRow(ResultSet rs, int rowNum) throws SQLException {
				ConsommationResultatsAnnee sorties = new ConsommationResultatsAnnee();
				int columncount = rs.getMetaData().getColumnCount();
				for (int k = 1; k <= columncount; k++) {
					sorties.setBranche(rs.getString("branche"));
					//sorties.setSousBranche(rs.getString("sousBranche"));
					sorties.setOccupation(rs.getString("occupation"));
					sorties.setPeriodeSimple(rs.getString("periodeSimple"));
					sorties.setEnergieUsage(rs.getString("energieUsage"));
					sorties.setUsage(rs.getString("usage"));
					sorties.setUsageSimple(rs.getString("usageSimple"));
					sorties.setFacteurEnergiePrimaire(rs.getDouble("FEP"));
					int n = 0;
					for (int i = 2009; i < 2051; i = i + pasTemps) {
						sorties.setAnnees(n, rs.getDouble("annee" + i));
						n = n + pasTemps;
					}

				}
				return sorties;

			}

		});

	}

	protected List<ConsommationResultatsAnnee> getConsoRTAnnee(final int pasTemps) {

		String requestSelect = "select substr(id, 1,2) as branche, substr(id, 7,2) as occupation, "
				+ "substr(id, 11,2) as periodeSimple, (case when usage in ('Ventilation','Eclairage','Auxiliaires')  then '02' "
				+ "else substr(id,length(id)-1,2) end) as energieUsage, usage, case when usage in "
				+ "('Eclairage','Ventilation', 'Auxiliaires') then 'Electricité Spécifique' "
				+ "else usage end as usageSimple, case when (case when usage in ('Ventilation','Eclairage','Auxiliaires')"
				+ " then '02' " + "else substr(id,length(id)-1,2) end)='02' then 2.58 else 1 end as FEP, "
				+ " sum(case when annee='2009' then consommation_ef else 0 end) as annee2009, ";

		for (int cursorAnnee = 2010; cursorAnnee < 2050; cursorAnnee = cursorAnnee + pasTemps) {
			requestSelect = requestSelect + "sum(case when annee='" + cursorAnnee
					+ "' then consommation_ef else 0 end) as annee" + cursorAnnee + ", ";
		}

		requestSelect = requestSelect + "	sum(case when annee='2050' then consommation_ef else 0 end) as annee2050 "

		+ " from conso_rt_resultats 	 group by 	 substr(id, 1,2), substr(id, 7,2), "
				+ "	 substr(id, 11,2),usage,substr(id,length(id)-1,2)"

				+ " order by 1, 2 , 3, 4, 5, 6 ";

		return jdbcTemplate.query(requestSelect, new RowMapper<ConsommationResultatsAnnee>() {
			@Override
			public ConsommationResultatsAnnee mapRow(ResultSet rs, int rowNum) throws SQLException {
				ConsommationResultatsAnnee sorties = new ConsommationResultatsAnnee();
				int columncount = rs.getMetaData().getColumnCount();
				for (int k = 1; k <= columncount; k++) {
					sorties.setBranche(rs.getString("branche"));
					//sorties.setSousBranche(rs.getString("sousBranche"));
					sorties.setOccupation(rs.getString("occupation"));
					sorties.setPeriodeSimple(rs.getString("periodeSimple"));
					sorties.setEnergieUsage(rs.getString("energieUsage"));
					sorties.setUsage(rs.getString("usage"));
					sorties.setUsageSimple(rs.getString("usageSimple"));
					sorties.setFacteurEnergiePrimaire(rs.getDouble("FEP"));
					int n = 0;
					for (int i = 2009; i < 2051; i = i + pasTemps) {
						sorties.setAnnees(n, rs.getDouble("annee" + i));
						n = n + pasTemps;
					}

				}
				return sorties;
			}

		});

	}


	protected List<ConsommationResultatsAnnee> getEtiquette() {

		String requestSelect = "select "
				+ " etiquette.annee, etiquette.branche, etiquette.ss_branche, etiquette.bat_type, etiquette.occupant, etiquette.periode_detail, etiquette.periode_simple, etiquette.systeme_chauff, etiquette.systeme_froid, etiquette.surface, etiquette.conso_u_tot, etiquette.etiquette  "

				+ " from ( "
				+ " select "
				+ " 	chauff.annee, chauff.branche, chauff.ss_branche, chauff.occupant, chauff.bat_type, chauff.periode_detail, chauff.periode_simple, chauff.systeme_chauff, chauff.systeme_froid, sum(chauff.conso_ep) as conso, sum(parc.surface) as surface, sum(chauff.conso_ep)/sum(parc.surface) as conso_u_chauf, sum(case when clim.conso_u is null then 0 else clim.conso_u end) as conso_u_clim , (case when ecs.conso_u is null then 0 else ecs.conso_u end) as conso_u_ecs, sum(chauff.conso_ep)/sum(parc.surface)+sum(case when clim.conso_u is null then 0 else clim.conso_u end)+ecs.conso_u as conso_u_tot, borne.etiquette, borne.conso_min, borne.conso_max  "

				/* Consos chauffage */
				+ " from  "
				+ " (select "
				+ " 		substr(cp.id,1,2) as branche, substr(cp.id,3,2) as ss_branche, substr(cp.id,5,2) as bat_type, substr(cp.id,7,2) as occupant, substr(cp.id,9,2) as periode_detail, substr(cp.id,11,2) as periode_simple, substr(cp.id,13,2) as systeme_chauff, substr(cp.id,15,2) as systeme_froid, cp.usage, case when cp.usage in ('Ventilation','Auxiliaires') then '02' else substr(cp.id,length(id)-1,2) end as energie_usage, substr(cp.id,length(id)-1,2) as energie_chauff, cp.annee, sum(consommation_ef*(case when cp.usage in ('Ventilation','Auxiliaires') then 2.58 when substr(cp.id,length(id)-1,2) = '02' then 2.58 else 1 end) ) as conso_ep      "

				+ " 	 from conso_rt_resultats cp  "

				+ " 	 where cp.usage in ('Ventilation','Chauffage','Auxiliaires') "
				+ " 		and cp.annee in ('2010','2015','2020','2025','2030','2035','2040','2045','2050')   "

				+ " 	 group by "
				+ " 		substr(cp.id,1,2), substr(cp.id,3,2), substr(cp.id,5,2), substr(cp.id,7,2), substr(cp.id,9,2), substr(cp.id,11,2), substr(cp.id,13,2), substr(cp.id,15,2), cp.usage, substr(cp.id,length(id)-1,2), cp.annee "
				+ " ) "
				+ " chauff    "

				/* Parc */
				+ " left  join "
				+ " ( "
				+ " 	select "
				+ " 		substr(cp.id,1,2) as branche, substr(cp.id,3,2) as ss_branche, substr(cp.id,5,2) as bat_type, substr(cp.id,7,2) as occupant, substr(cp.id,9,2) as periode_detail,  substr(cp.id,11,2) as periode_simple, substr(cp.id,13,2) as systeme_chauff, substr(cp.id,15,2) as systeme_froid, substr(cp.id,length(id)-1,2) as energie_chauff, cp.annee,  sum(cp.surfaces) as surface    "

				+ " 	 from parc_resultats cp   "
				+ "  "
				+ " 	 where cp.annee in ('2010','2015','2020','2025','2030','2035','2040','2045','2050') "
				+ " 	  "
				+ " 	 group by  substr(cp.id,1,2), substr(cp.id,3,2), substr(cp.id,5,2), substr(cp.id,7,2), substr(cp.id,9,2),  substr(cp.id,11,2), substr(cp.id,13,2), substr(cp.id,15,2), substr(cp.id,length(id)-1,2), cp.annee "
				+ " ) "
				+ " parc	 "
				+ " 	on parc.branche=chauff.branche and parc.ss_branche=chauff.ss_branche and parc.bat_type=chauff.bat_type and parc.occupant=chauff.occupant and parc.periode_detail=chauff.periode_detail and parc.periode_simple=chauff.periode_simple and parc.energie_chauff=chauff.energie_chauff and parc.annee=chauff.annee and parc.systeme_chauff=chauff.systeme_chauff and parc.systeme_froid=chauff.systeme_froid and chauff.usage='Chauffage'  "

				/* Climatisation */
				+ "  left join "
				+ " 	(select "
				+ " 		conso_det.annee, conso_det.branche, conso_det.ss_branche, conso_det.bat_type, conso_det.occupant, conso_det.periode_detail, conso_det.periode_simple, conso_det.systeme_froid, sum(conso_det.conso_ep) as conso_ep, sum(surface_det.surface) as surface, sum(conso_det.conso_ep)/sum(surface_det.surface) as conso_u  "
				+ "  "
				+ " 	from  "
				+ " 		(select substr(cp.id,1,2) as branche, substr(cp.id,3,2) as ss_branche, substr(cp.id,5,2) as bat_type, substr(cp.id,7,2) as occupant, substr(cp.id,9,2) as periode_detail, substr(cp.id,11,2) as periode_simple, substr(cp.id,13,2) as systeme_froid, cp.usage, '02'  as energie_usage, cp.annee, sum(consommation_ef*2.58) as conso_ep    	 "
				+ " 		 from conso_rt_resultats cp 	 	 "
				+ " 		 where cp.usage in ('Climatisation') "

				+ " 		 and cp.annee in ('2010','2015','2020','2025','2030','2035','2040','2045','2050') 	 	 "
				+ " 		 group by substr(cp.id,1,2), substr(cp.id,3,2), substr(cp.id,5,2), substr(cp.id,7,2), substr(cp.id,9,2), substr(cp.id,11,2), substr(cp.id,13,2), cp.usage, substr(cp.id,length(id)-1,2),  cp.annee) "
				+ " 		 conso_det  "

				+ " 	 join   "
				+ " 		 (select "
				+ " 			substr(cp.id,1,2) as branche, substr(cp.id,3,2) as ss_branche, substr(cp.id,5,2) as bat_type, substr(cp.id,7,2) as occupant, substr(cp.id,9,2) as periode_detail, substr(cp.id,11,2) as periode_simple, substr(cp.id,15,2) as systeme_froid, cp.annee, sum(cp.surfaces) as surface    "
				+ " 		 from parc_resultats cp "

				+ " 		 where  cp.annee in ('2010','2015','2020','2025','2030','2035','2040','2045','2050')  "

				+ " 		 group by  "
				+ " 			substr(cp.id,1,2), substr(cp.id,3,2), substr(cp.id,5,2), substr(cp.id,7,2), substr(cp.id,9,2), substr(cp.id,11,2), substr(cp.id,15,2), cp.annee "
				+ " 	) surface_det "
				+ " 	 "
				+ " 		on surface_det.branche=conso_det.branche and surface_det.ss_branche=conso_det.ss_branche and surface_det.bat_type=conso_det.bat_type and surface_det.occupant=conso_det.occupant and surface_det.periode_detail=conso_det.periode_detail and surface_det.periode_simple=conso_det.periode_simple and surface_det.annee=conso_det.annee and surface_det.systeme_froid=conso_det.systeme_froid "

				+ " 	group by conso_det.annee, conso_det.branche, conso_det.ss_branche, conso_det.bat_type, conso_det.occupant, conso_det.periode_detail, conso_det.periode_simple, conso_det.systeme_froid "
				+ " ) "
				+ " clim "
				+ " 	on clim.annee=parc.annee and clim.branche=chauff.branche and clim.ss_branche=chauff.ss_branche and clim.bat_type=chauff.bat_type and clim.occupant=chauff.occupant and clim.periode_detail=chauff.periode_detail and clim.periode_simple=chauff.periode_simple and clim.systeme_froid=chauff.systeme_froid   "

				/* ECS */
				+ " join (  "
				+ " 		select conso_det.annee, conso_det.branche, conso_det.ss_branche, conso_det.bat_type, conso_det.occupant, conso_det.periode_detail, conso_det.periode_simple, sum(conso_det.conso_ep) as conso, sum(surface_det.surface) as surface, sum(conso_det.conso_ep)/sum(surface_det.surface) as conso_u   	 "
				+ " 		from "
				+ " 			(select substr(cp.id,1,2) as branche, substr(cp.id,3,2) as ss_branche, substr(cp.id,5,2) as bat_type, substr(cp.id,7,2) as occupant, substr(cp.id,9,2) as periode_detail, substr(cp.id,11,2) as periode_simple, cp.annee, sum(consommation_ef*(case when cp.usage in ('Eclairage') then 2.58 when substr(cp.id,13,2) = '02' then 2.58 else 1 end) ) as conso_ep  "

				+ " 			 from conso_rt_resultats cp  "
				+ " 			 where cp.usage in ('Eclairage','ECS') and cp.annee in ('2010','2015','2020','2025','2030','2035','2040','2045','2050') 	  "
				+ " 			 group by substr(cp.id,1,2), substr(cp.id,3,2), substr(cp.id,5,2), substr(cp.id,7,2), substr(cp.id,9,2), substr(cp.id,11,2), cp.annee "
				+ " 			) conso_det   "
				+ " 	 "
				+ " 	left  join ( "
				+ " 		select "
				+ " 		substr(cp.id,1,2) as branche, substr(cp.id,3,2) as ss_branche, substr(cp.id,5,2) as bat_type, substr(cp.id,7,2) as occupant, substr(cp.id,9,2) as periode_detail, substr(cp.id,11,2) as periode_simple, cp.annee, sum(cp.surfaces) as surface    "

				+ " 		 from parc_resultats cp     "

				+ " 		 where cp.annee in ('2010','2015','2020','2025','2030','2035','2040','2045','2050')  "

				+ " 		 group by substr(cp.id,1,2), substr(cp.id,3,2), substr(cp.id,5,2), substr(cp.id,7,2), substr(cp.id,9,2), substr(cp.id,11,2), cp.annee "
				+ " 		 ) surface_det "

				+ " 			on surface_det.branche=conso_det.branche	 "
				+ " 				 and surface_det.ss_branche=conso_det.ss_branche	 "
				+ " 				 and surface_det.bat_type=conso_det.bat_type		 "
				+ " 				 and surface_det.occupant=conso_det.occupant		 "
				+ " 				 and surface_det.periode_detail=conso_det.periode_detail	 "
				+ " 				 and surface_det.periode_simple=conso_det.periode_simple "
				+ " 				 and surface_det.annee=conso_det.annee "

				+ " 	 group by conso_det.annee, conso_det.branche, conso_det.ss_branche, conso_det.bat_type, conso_det.occupant, conso_det.periode_detail, conso_det.periode_simple "
				+ " ) "
				+ " ecs   "
				+ " 	on ecs.annee=chauff.annee and	ecs.branche=chauff.branche and	ecs.ss_branche=chauff.ss_branche and	ecs.bat_type=chauff.bat_type and ecs.occupant=chauff.occupant and ecs.periode_detail=chauff.periode_detail and	ecs.periode_simple=chauff.periode_simple  "

				+ " left join etiquettes_categories cat "
				+ " 	on cat.branche*1=chauff.branche*1 "
				+ " 		and cat.bat_type*1=chauff.bat_type*1 "

				+ " left join etiquettes_bornes borne "
				+ " 	on borne.categorie_etiquette=cat.categorie_etiquette "

				+ " group by chauff.annee, chauff.branche, chauff.ss_branche, chauff.bat_type, chauff.occupant, chauff.periode_detail, chauff.periode_simple, chauff.systeme_chauff, chauff.systeme_froid, ecs.conso_u, borne.etiquette, borne.conso_min, borne.conso_max "
				+ " ) etiquette "

				+ " where  etiquette.conso_u_tot<etiquette.conso_max and etiquette.conso_u_tot>etiquette.conso_min";

		return jdbcTemplate.query(requestSelect, new RowMapper<ConsommationResultatsAnnee>() {
			@Override
			public ConsommationResultatsAnnee mapRow(ResultSet rs, int rowNum) throws SQLException {
				ConsommationResultatsAnnee sorties = new ConsommationResultatsAnnee();
				int columncount = rs.getMetaData().getColumnCount();
				for (int k = 1; k <= columncount; k++) {

					sorties.setAnnee(rs.getInt("annee"));
					sorties.setBranche(rs.getString("branche"));
					sorties.setSousBranche(rs.getString("ss_branche"));
					sorties.setBatimentType(rs.getString("bat_type"));
					sorties.setOccupation(rs.getString("occupant"));
					sorties.setPeriodeDetail(rs.getString("periode_detail"));
					sorties.setPeriodeSimple(rs.getString("periode_simple"));
					sorties.setSystemeChaud(rs.getString("systeme_chauff"));
					sorties.setSystemeFroid(rs.getString("systeme_froid"));
					sorties.setSurface(rs.getDouble("surface"));
					sorties.setConsommationUnitaire(rs.getDouble("conso_u_tot"));
					sorties.setEtiquette(rs.getString("etiquette"));

				}

				return sorties;

			}

		});

	}

	protected List<BesoinConsoForCSV> getBesoinAnnee(final int pasTemps) {
			String requestSelect = "SELECT r.COD_BRANCHE, r.ANNEE,r.COD_PERIODE_SIMPLE,r.USAGE,r.COD_ENERGIE, r.CONSO_TOT,s.BESOIN_TOT FROM " +
					"(SELECT a.COD_BRANCHE, a.ANNEE,a.COD_PERIODE_SIMPLE, a.USAGE, a.COD_ENERGIE,  sum(a.CONSOMMATION_EF) as CONSO_TOT FROM " +
					"(SELECT  substr(ci.ID,1,2) as COD_BRANCHE, ci.ANNEE, ci.USAGE, ci.CONSOMMATION_EF, " +
					"substr(ci.ID,11,2) as COD_PERIODE_SIMPLE, " +
					"(case when ci.usage in ('Ventilation','Eclairage','Auxiliaires') then '02' else substr(ci.ID,length(ci.ID)-1,2) end) as COD_ENERGIE " +
					"FROM CONSO_RT_RESULTATS ci " +
					") a  " +
					"GROUP BY a.COD_BRANCHE, a.ANNEE,a.COD_PERIODE_SIMPLE, a.USAGE,  a.COD_ENERGIE) r " +
					"JOIN " +
					"(SELECT  b.COD_BRANCHE, b.ANNEE,b.COD_PERIODE_SIMPLE, b.USAGE, b.COD_ENERGIE,sum(b.BESOIN) as BESOIN_TOT FROM " +
					"(SELECT substr(cs.ID,1,2) as COD_BRANCHE,cs.ANNEE, cs.USAGE, cs.BESOIN,substr(cs.ID,11,2) as COD_PERIODE_SIMPLE, " +
					"(case when cs.usage in ('Ventilation','Eclairage','Auxiliaires') then '02' else substr(cs.ID,length(cs.ID)-1,2) end) as COD_ENERGIE " +
					"FROM BESOIN_RT_RESULTATS cs " +
					") b " +
					"GROUP BY b.COD_BRANCHE,b.ANNEE,b.COD_PERIODE_SIMPLE, b.USAGE,  b.COD_ENERGIE) s " +
					"on r.COD_BRANCHE= s.COD_BRANCHE AND r.ANNEE = s.ANNEE AND r.USAGE = s.USAGE AND r.COD_ENERGIE = s.COD_ENERGIE AND r.COD_PERIODE_SIMPLE = s.COD_PERIODE_SIMPLE ";
			LOG.debug(requestSelect);
		return jdbcTemplate.query(requestSelect, new RowMapper<BesoinConsoForCSV>() {
			@Override
			
			public BesoinConsoForCSV mapRow(ResultSet rs, int rowNum) throws SQLException {
				BesoinConsoForCSV sorties = new BesoinConsoForCSV();
				int columncount = rs.getMetaData().getColumnCount();
	//			for (int k = 1; k <= columncount; k++) {
					sorties.setBranche(rs.getString("COD_BRANCHE"));
					sorties.setCodePeriodeSimple(rs.getString("COD_PERIODE_SIMPLE"));
					sorties.setAnnee(rs.getInt("ANNEE"));
					sorties.setUsage(rs.getString("USAGE"));
					sorties.setEnergie(rs.getString("COD_ENERGIE"));
					sorties.setBesoinTot(rs.getBigDecimal("BESOIN_TOT"));
					sorties.setConsoTot(rs.getBigDecimal("CONSO_TOT"));
	//			}
				return sorties;

			}

		});

	}

	protected List<BesoinConsoForCSV> getBesoinAnneeSyst(final int pasTemps) {
		String requestSelect = "SELECT r.COD_BRANCHE, r.ANNEE,r.COD_PERIODE_SIMPLE,r.USAGE,r.COD_SYSTEME_CHAUD,r.COD_ENERGIE, r.CONSO_TOT,s.BESOIN_TOT FROM  " +
					"(SELECT a.COD_BRANCHE, a.ANNEE,a.COD_PERIODE_SIMPLE, a.USAGE, a.COD_SYSTEME_CHAUD, a.COD_ENERGIE,  sum(a.CONSOMMATION_EF) as CONSO_TOT FROM " +
					"(SELECT  substr(ci.ID,1,2) as COD_BRANCHE, ci.ANNEE, ci.USAGE, ci.CONSOMMATION_EF, " +
					"substr(ci.ID,11,2) as COD_PERIODE_SIMPLE, " +
					"(case when ci.usage in ('Ventilation','Eclairage','Auxiliaires') then '02' else substr(ci.ID,length(ci.ID)-1,2) end) as COD_ENERGIE, " +
					"(case when ci.usage in ('Chauffage') THEN substr(ci.ID,13,2) ELSE '' end) AS COD_SYSTEME_CHAUD " +
					"FROM CONSO_RT_RESULTATS ci " +
					") a  " +
					"GROUP BY a.COD_BRANCHE, a.ANNEE,a.COD_PERIODE_SIMPLE, a.USAGE,a.COD_SYSTEME_CHAUD,  a.COD_ENERGIE) r " +
					"JOIN " +
					"(SELECT  b.COD_BRANCHE, b.ANNEE,b.COD_PERIODE_SIMPLE, b.USAGE, b.COD_SYSTEME_CHAUD, b.COD_ENERGIE,sum(b.BESOIN) as BESOIN_TOT FROM " +
					"(SELECT substr(cs.ID,1,2) as COD_BRANCHE,cs.ANNEE, cs.USAGE, cs.BESOIN,substr(cs.ID,11,2) as COD_PERIODE_SIMPLE, " +
					"(case when cs.usage in ('Ventilation','Eclairage','Auxiliaires') then '02' else substr(cs.ID,length(cs.ID)-1,2) end) as COD_ENERGIE, " +
					"substr(cs.ID,13,2) AS COD_SYSTEME_CHAUD " +
					"FROM BESOIN_RT_RESULTATS cs " +
					") b " +
					"GROUP BY b.COD_BRANCHE,b.ANNEE,b.COD_PERIODE_SIMPLE, b.USAGE,b.COD_SYSTEME_CHAUD,  b.COD_ENERGIE) s " +
					"on r.COD_BRANCHE= s.COD_BRANCHE AND r.ANNEE = s.ANNEE AND r.USAGE = s.USAGE AND r.COD_ENERGIE = s.COD_ENERGIE " +
					"AND r.COD_PERIODE_SIMPLE = s.COD_PERIODE_SIMPLE AND r.COD_SYSTEME_CHAUD= s.COD_SYSTEME_CHAUD ";
		
		LOG.debug(requestSelect);
	return jdbcTemplate.query(requestSelect, new RowMapper<BesoinConsoForCSV>() {
		@Override
		public BesoinConsoForCSV mapRow(ResultSet rs, int rowNum) throws SQLException {
			BesoinConsoForCSV sorties = new BesoinConsoForCSV();
			int columncount = rs.getMetaData().getColumnCount();
//			for (int k = 1; k <= columncount; k++) {
				sorties.setBranche(rs.getString("COD_BRANCHE"));
				sorties.setCodePeriodeSimple(rs.getString("COD_PERIODE_SIMPLE"));
				sorties.setAnnee(rs.getInt("ANNEE"));
				sorties.setUsage(rs.getString("USAGE"));
				sorties.setSystemChaud(rs.getString("COD_SYSTEME_CHAUD"));
				sorties.setEnergie(rs.getString("COD_ENERGIE"));
				sorties.setBesoinTot(rs.getBigDecimal("BESOIN_TOT"));
				sorties.setConsoTot(rs.getBigDecimal("CONSO_TOT"));
	//		}
			return sorties;

		}

	});

}
	protected List<BesoinConsoForCSV> getConsononRTAnnee(final int pasTemps) {
		String requestSelect = "SELECT r.COD_BRANCHE, r.ANNEE, r.COD_PERIODE_SIMPLE, r.USAGE,r.COD_ENERGIE, r.CONSO_TOT FROM  " +
				"(SELECT a.COD_BRANCHE, a.ANNEE, a.COD_PERIODE_SIMPLE, a.USAGE, a.COD_ENERGIE,  sum(a.CONSOMMATION_EF) as CONSO_TOT FROM " +
				"(SELECT  substr(ci.ID,1,2) as COD_BRANCHE, ci.ANNEE, ci.USAGE, ci.CONSOMMATION_EF, " +
				"substr(ci.ID,11,2) as COD_PERIODE_SIMPLE, " +
				"(case when ci.usage in ('Ventilation','Eclairage','Auxiliaires') then '02' else substr(ci.ID,length(ci.ID)-1,2) end) as COD_ENERGIE " +
				"FROM CONSO_NON_RT_RESULTATS ci " +
				") a   " +
				"GROUP BY a.COD_BRANCHE, a.ANNEE,a.COD_PERIODE_SIMPLE, a.USAGE,  a.COD_ENERGIE) r ";
		LOG.debug(requestSelect);
		return jdbcTemplate.query(requestSelect, new RowMapper<BesoinConsoForCSV>() {
			@Override
			public BesoinConsoForCSV mapRow(ResultSet rs, int rowNum) throws SQLException {
				BesoinConsoForCSV sorties = new BesoinConsoForCSV();
				int columncount = rs.getMetaData().getColumnCount();
//				for (int k = 1; k <= columncount; k++) {
					sorties.setBranche(rs.getString("COD_BRANCHE"));
					sorties.setCodePeriodeSimple(rs.getString("COD_PERIODE_SIMPLE"));
					sorties.setAnnee(rs.getInt("ANNEE"));
					sorties.setUsage(rs.getString("USAGE"));
					sorties.setEnergie(rs.getString("COD_ENERGIE"));
					sorties.setConsoTot(rs.getBigDecimal("CONSO_TOT"));
//				}
				return sorties;

			}

		});

	}

	


	protected List<BesoinConsoForCSV> getSurfaceClimAnnee(final int pasTemps) {
		String requestSelect = "SELECT r.COD_BRANCHE, r.ANNEE, r.COD_PERIODE_SIMPLE,r.COD_SYSTEME_FROID, r.SURFACES_TOT FROM  " +
				"(SELECT a.COD_BRANCHE, a.ANNEE, a.COD_PERIODE_SIMPLE,a.COD_SYSTEME_FROID, sum(a.SURFACES)  as SURFACES_TOT FROM " +
				"(SELECT  substr(ci.ID,1,2) as COD_BRANCHE, ci.ANNEE,substr(ci.ID,15,2) as COD_SYSTEME_FROID, ci.SURFACES,  " +
				"substr(ci.ID,11,2) as COD_PERIODE_SIMPLE " +
				"FROM PARC_RESULTATS ci " +
				") a  " +
				"GROUP BY a.COD_BRANCHE, a.ANNEE,a.COD_PERIODE_SIMPLE, a.COD_SYSTEME_FROID) r ";
		LOG.debug(requestSelect);
		return jdbcTemplate.query(requestSelect, new RowMapper<BesoinConsoForCSV>() {
			@Override
			public BesoinConsoForCSV mapRow(ResultSet rs, int rowNum) throws SQLException {
				BesoinConsoForCSV sorties = new BesoinConsoForCSV();
				int columncount = rs.getMetaData().getColumnCount();
//				for (int k = 1; k <= columncount; k++) {
					sorties.setBranche(rs.getString("COD_BRANCHE"));
					sorties.setCodePeriodeSimple(rs.getString("COD_PERIODE_SIMPLE"));
					sorties.setAnnee(rs.getInt("ANNEE"));
					sorties.setSystemFroid(rs.getString("COD_SYSTEME_FROID"));
					sorties.setSurfaceTot(rs.getBigDecimal("SURFACES_TOT"));
//				}
				return sorties;

			}

		});

	}




	protected List<ConsommationResultatsAnnee> getGESAnnee(final int PasTemps) {

		String requestSelect = "select substr(c.id, 1,2) as branche,"
				+ " substr(c.id, 7,2) as occupation, substr(c.id, 11,2) as periodeSimple,"
				+ " case when c.usage in ('Eclairage','Ventilation',"

				+ " 'Bureautique','Froid_alimentaire', 'Process') then '02' else substr(c.id,13,2) end as energieUsage, c.usage as usage,"
				+ " case when c.usage in ('Eclairage','Ventilation',"
				+ " 'Bureautique','Froid_alimentaire', 'Process') then 'Electricité Spécifique' "
				+ "	 else c.usage end as usageSimple, "

				+ " sum(case when c.annee='2009' then consommation_ef*fe.periode1 else 0 end) as annee2009, ";

		for (int cursorAnnee = 2010; cursorAnnee < 2050; cursorAnnee = cursorAnnee + PasTemps) {
			requestSelect = requestSelect + "sum(case when c.annee='" + cursorAnnee + "' then c.consommation_ef*(case "
					+ "	when '" + cursorAnnee + "'*1 <2015 then fe.periode1 	when '" + cursorAnnee
					+ "'*1 <2020 then fe.periode2 	when '" + cursorAnnee + "'*1 <2030 then fe.periode3 " + "	when '"
					+ cursorAnnee + "'*1 <2040 then fe.periode4 " + "	else fe.periode5 end) else 0 end) as annee"
					+ cursorAnnee + ", ";
		}

		requestSelect = requestSelect
				+ "	sum(case when c.annee='2050' then c.consommation_ef*fe.periode5 else 0 end) as annee2050 "
				+ " from conso_non_rt_resultats c "

				+ " join (select distinct substr(id,17,2) as id, energie_chauff from id) i on i.id=substr(c.id,13,2) "
				+ " join param_emissions fe 		on fe.energie=i.energie_chauff and fe.usage=case "
				+ "			when c.usage in ('Ventilation','Auxiliaires') then 'Autres'"
				+ "			when substr(c.id,length(c.id)-1,2)!='02' then 'Tous'"
				+ "			when c.usage not in ('Chauffage', 'Climatisation', 'Cuisson', 'Eclairage', 'ECS') then 'Autres' "
				+ "			else c.usage end "

				+ "	group by 	substr(c.id, 1,2), substr(c.id, 7,2),"
				+ "	substr(c.id, 11,2), substr(c.id,13,2), c.usage "

				+ " order by 1, 2 , 3, 4, 5, 6 ";

		return jdbcTemplate.query(requestSelect, new RowMapper<ConsommationResultatsAnnee>() {
			@Override
			public ConsommationResultatsAnnee mapRow(ResultSet rs, int rowNum) throws SQLException {
				ConsommationResultatsAnnee sorties = new ConsommationResultatsAnnee();
				int columncount = rs.getMetaData().getColumnCount();
				for (int k = 1; k <= columncount; k++) {
					sorties.setBranche(rs.getString("branche"));
//					sorties.setSousBranche(rs.getString("sousBranche"));
					sorties.setOccupation(rs.getString("occupation"));
					sorties.setPeriodeSimple(rs.getString("periodeSimple"));
					sorties.setEnergieUsage(rs.getString("energieUsage"));
					sorties.setUsage(rs.getString("usage"));
					sorties.setUsageSimple(rs.getString("usageSimple"));
					int n = 0;
					for (int i = 2009; i < 2051; i = i + PasTemps) {
						sorties.setAnnees(n, rs.getDouble("annee" + i));
						n = n + PasTemps;
					}
				}
				return sorties;
			}
		});
	}


	protected List<ConsommationResultatsAnnee> getParcAnnee(final int PasTemps) {

		String requestSelect = "select  substr(id, 1,2) as branche, substr(id, 7,2) as occupation, "
				+ "substr(id, 11,2) as periodeSimple, substr(id,length(id)-1,2) as energieChauffage, "

				+ " sum(case when annee='2009' then surfaces else 0 end) as annee2009, ";

		for (int cursorAnnee = 2010; cursorAnnee < 2050; cursorAnnee = cursorAnnee + PasTemps) {
			requestSelect = requestSelect + "sum(case when annee='" + cursorAnnee
					+ "' then surfaces else 0 end) as annee" + cursorAnnee + ", ";
		}

		requestSelect = requestSelect + "	sum(case when annee='2050' then surfaces else 0 end) as annee2050 "

				+ "from parc_resultats "

				+ "group by  substr(id, 1,2) , substr(id, 7,2) ,  substr(id, 11,2) , substr(id,length(id)-1,2) "

				+ "order by 1,2,3,4";

		return jdbcTemplate.query(requestSelect, new RowMapper<ConsommationResultatsAnnee>() {
			@Override
			public ConsommationResultatsAnnee mapRow(ResultSet rs, int rowNum) throws SQLException {
				ConsommationResultatsAnnee sorties = new ConsommationResultatsAnnee();
				int columncount = rs.getMetaData().getColumnCount();
				for (int k = 1; k <= columncount; k++) {

					sorties.setBranche(rs.getString("branche"));
					sorties.setOccupation(rs.getString("occupation"));
					sorties.setPeriodeSimple(rs.getString("periodeSimple"));
					sorties.setEnergieUsage(rs.getString("energieChauffage"));
					int n = 0;
					for (int i = 2009; i < 2051; i = i + PasTemps) {
						sorties.setAnnees(n, rs.getDouble("annee" + i));
						n = n + PasTemps;
					}
				}

				return sorties;

			}

		});

	}


	private List<ConsommationResultatsAnnee> getCout(final int PasTemps) {

		String requestSelect = " select "
				+ " r.branche, r.occupant, r.type_renov_bat, r.type_renov_sys, "
				+ " (case when substr(r.type_renov_bat, length(r.type_renov_bat)-2,3)='MOD' then '1'"
				+ "				when substr(r.type_renov_bat, length(r.type_renov_bat)-2,3)='BBC' then '2'"
				+ " 			when r.type_renov_bat='GTB' then '3'"
				+ "				else '0' end )"
				+ " || "
				+ "			(case when substr(r.type_renov_bat, 1,4)='FEN_' then '2'"
				+ "				when substr(r.type_renov_bat, 1,3)='FEN' then '1'"
				+ "				when substr(r.type_renov_bat, 1,3)='ENS' then '3'"
				+ "				when r.type_renov_bat='GTB' then '4'"
				+ " 			else '0' end) as cible, "
				// Le geste sur le bÃ¢ti est codÃ© dans Cible : xx =
				// [Etat initial = 00; Renovation Modeste = 1x; GTB = 3x;
				// Renovation BBC = 2x;
				// Fenetres = x1; Fenetres+Murs = x2;
				// Ensemble = x3; GTB = x4]

				+ " case when r.annee_renov_sys ='NP' then r.annee_renov_bat else r.annee_renov_sys end as annee, "
				+ " r.reglementation as reglementation,  "
				+ " sum(r.surface) as surface, sum(r.aides) as aides, sum(r.cout_inv) as investissement, "
				+ "sum(r.valeur_pret) as prets, sum(r.valeur_pret_bonif) as prets_bonif "

				+ " from resultats_financements r "
				+ " group by  r.branche, r.occupant, r.type_renov_bat, r.type_renov_sys, "
				+ " 	 r.type_renov_bat, case when r.annee_renov_sys ='NP' then r.annee_renov_bat else r.annee_renov_sys end, r.reglementation "
				+ " order by  5 ";

		return jdbcTemplate.query(requestSelect, new RowMapper<ConsommationResultatsAnnee>() {
			@Override
			public ConsommationResultatsAnnee mapRow(ResultSet rs, int rowNum) throws SQLException {
				ConsommationResultatsAnnee sorties = new ConsommationResultatsAnnee();
				int columncount = rs.getMetaData().getColumnCount();

				for (int k = 1; k <= columncount; k++) {

					sorties.setBranche(rs.getString("branche"));
					sorties.setOccupation(rs.getString("occupant"));
					sorties.setTypeRenovationBati(rs.getString("type_renov_bat"));
					sorties.setTypeRenovationSysteme(rs.getString("type_renov_sys"));
					sorties.setCible(rs.getInt("cible"));
					sorties.setAnnee(rs.getInt("annee"));
					sorties.setSurface(rs.getDouble("surface"));
					sorties.setAides(rs.getDouble("aides"));
					sorties.setInvestissement(rs.getDouble("investissement"));
					sorties.setPrets(rs.getDouble("prets"));
					sorties.setPretsBonifies(rs.getDouble("prets_bonif"));
					sorties.setReglementation(rs.getString("reglementation"));

				}

				return sorties;

			}

		});

	}




	private List<ConsommationResultatsAnnee> getCoutSystemesAutres(final int PasTemps) {

		String requestSelect = " select "
				+ " substr(cr.id, 1, 2) as branche, substr(cr.id, 7,2) as occupant,  'Etat Initial' as type_renov_bat, cr.usage as type_renov_sys, '99' as cible,  cr.annee as annee, sum(surf.surface) as surface, 0 as aides, sum(cr.couts) as investissement, sum(cr.couts) as prets, 0 as prets_bonif, 'non' as reglementation "

				+ " from couts_resultats cr "

				+ " left join "
				+ " (select branche, occupant, case when annee_renov_sys ='NP' then annee_renov_bat else annee_renov_sys end as annee, sum(surface) as surface from resultats_financements group by branche, occupant,  case when annee_renov_sys ='NP' then annee_renov_bat else annee_renov_sys end order by 1,2,3) surf "
				+ " on surf.branche=substr(cr.id, 1, 2) and surf.annee=cr.annee and surf.occupant = substr(cr.id, 7,2) "

				+ " group by substr(cr.id, 1, 2), substr(cr.id, 7,2), cr.usage, cr.annee "

				+ " order by 1,2,4,5,6 ";

		return jdbcTemplate.query(requestSelect, new RowMapper<ConsommationResultatsAnnee>() {
			@Override
			public ConsommationResultatsAnnee mapRow(ResultSet rs, int rowNum) throws SQLException {
				ConsommationResultatsAnnee sorties = new ConsommationResultatsAnnee();
				int columncount = rs.getMetaData().getColumnCount();

				for (int k = 1; k <= columncount; k++) {

					sorties.setBranche(rs.getString("branche"));
					sorties.setOccupation(rs.getString("occupant"));
					sorties.setTypeRenovationBati(rs.getString("type_renov_bat"));
					sorties.setTypeRenovationSysteme(rs.getString("type_renov_sys"));
					sorties.setCible(rs.getInt("cible"));
					sorties.setAnnee(rs.getInt("annee"));
					sorties.setSurface(rs.getDouble("surface"));
					sorties.setAides(rs.getDouble("aides"));
					sorties.setInvestissement(rs.getDouble("investissement"));
					sorties.setPrets(rs.getDouble("prets"));
					sorties.setPretsBonifies(rs.getDouble("prets_bonif"));
					sorties.setReglementation(rs.getString("reglementation"));

				}

				return sorties;

			}

		});

	}

}