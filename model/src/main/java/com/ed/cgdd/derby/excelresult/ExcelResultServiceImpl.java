package com.ed.cgdd.derby.excelresult;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
//import org.apache.poi.ss.usermodel.Cell;
//import org.apache.poi.xssf.usermodel.XSSFCell;
//import org.apache.poi.xssf.usermodel.XSSFRow;
//import org.apache.poi.xssf.usermodel.XSSFSheet;
//import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.ed.cgdd.derby.model.parc.ConsommationResultatsAnnee;
import com.ed.cgdd.derby.model.parc.TableResult;

// Sorties Excel :
// 1. Evolution du parc
//	1.a. Extraction des résultats
//	1.b. Insertion dans le .xls onglet parcAnnée
//
// 2. Evolution des consommations
//	2.a. Extraction des résultats
//	2.b. Insertion dans le .xls onglet consoAnnée
//
// 3. Evolution des émissions de GES 
//	3.a. Extraction des résultats
//	3.b. Insertion dans le .xls onglet GESAnnée

public class ExcelResultServiceImpl implements ExcelResultService {

	private final static Logger LOG = LogManager.getLogger(ExcelResultServiceImpl.class);

	// load data table PARC_INIT
	private JdbcTemplate jdbcTemplate;

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	/**
	 * excelService renvoie parcResultats, consoResultats, GESResultats dans le
	 * fichier table_resultats.xls.
	 * 
	 * Il met à jour les résultats.
	 * 
	 * Il définit si les feuilles d'import sont cachées ou visibles.
	 */
	@Override
	public void excelService(int pasTemps, boolean isHidden) {
		try {

			LOG.info("Classeur table_resultat.xls");
			LOG.info("	Import Xls : parcResultats");
			getParcAnneeXls(pasTemps);
			LOG.info("	Import Xls : consoResultats");
			getConsoAnneeXls(pasTemps);
			LOG.info("	Import Xls : GESResultats");
			getGESAnneeXls(pasTemps);
			LOG.info("	Hide import sheet : " + isHidden);
			setImportSheetHidden(isHidden);
			// LOG.info("	Update XLS");
			// updateXls();
		} catch (SQLException ex) {
			LOG.error(ex);
		} catch (IOException e) {
			LOG.error(e);
		}

	}

	// 1. Evolution du parc
	// 1.a. Extraction des résultats
	// 1.b. Insertion dans le .xls

	// 1.a. Evolution du parc : Extraction des résultats

	public List<ConsommationResultatsAnnee> getParcAnnee(final int PasTemps) {

		String requestSelect = "select  substr(id, 1,2) as branche,  substr(id, 7,2) as occupation, "
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
					sorties.setAnnee2009(rs.getDouble("annee2009"));
					int n = 0;
					for (int i = 2010; i < 2050; i = i + PasTemps) {
						sorties.setAnnees(n, rs.getDouble("annee" + i));
						n = n + PasTemps;
					}
					sorties.setAnnee2050(rs.getDouble("annee2050"));
				}

				return sorties;

			}

		});

	}

	// 1.b Evolution du parc : Insertion dans le .xls

	/**
	 * 
	 * Récupère les données sur le parc pour les inserer dans table_results.xls
	 * 
	 * @param pasTemps
	 * @throws SQLException
	 * @throws IOException
	 */
	public void getParcAnneeXls(int pasTemps) throws SQLException, IOException {

		InputStream excelFileName = new FileInputStream("./Result_Excel/table_resultat.xls");
		HSSFWorkbook wb = new HSSFWorkbook(excelFileName);
		// get sheet
		HSSFSheet sheet = wb.getSheet("parcAnnee");

		// Empty Sheet
		int lastRow = sheet.getLastRowNum();
		HSSFRow oldRow = sheet.getRow(0);

		for (int n = 0; n <= lastRow; n++) {
			oldRow = sheet.getRow(n);
			if (oldRow != null) {
				sheet.removeRow(oldRow);
			}
		}

		// get list result (ParcResultat)
		List<ConsommationResultatsAnnee> list = getParcAnnee(pasTemps);

		// create sheet headers
		HSSFRow row1 = sheet.createRow(0);
		HSSFCell cellHeader = row1.createCell(0);
		cellHeader.setCellValue("branche");
		HSSFCell cellHeader1 = row1.createCell(1);
		cellHeader1.setCellValue("occupation");
		HSSFCell cellHeader2 = row1.createCell(2);
		cellHeader2.setCellValue("periodeSimple");
		HSSFCell cellHeader3 = row1.createCell(3);
		cellHeader3.setCellValue("energieChauffage");
		HSSFCell cellHeader4 = row1.createCell(4);
		cellHeader4.setCellValue(2009);

		int CursorHead = 5;
		for (int i = 2010; i < 2050; i = i + pasTemps) {
			HSSFCell cellHeaderAnnee = row1.createCell(CursorHead);
			cellHeaderAnnee.setCellValue(i);
			CursorHead++;
		}

		HSSFCell cellHeader12 = row1.createCell(CursorHead);
		cellHeader12.setCellValue(2050);

		// create row years
		int i = 1;
		for (ConsommationResultatsAnnee e : list) {

			HSSFRow row = sheet.createRow(i);

			HSSFCell cell = row.createCell(0);
			cell.setCellValue(e.getBranche());
			HSSFCell cell1 = row.createCell(1);
			cell1.setCellValue(e.getOccupation());
			HSSFCell cell2 = row.createCell(2);
			cell2.setCellValue(e.getPeriodeSimple());
			HSSFCell cell3 = row.createCell(3);
			cell3.setCellValue(e.getEnergieUsage());
			HSSFCell cell4 = row.createCell(4);
			cell4.setCellValue(e.getAnnee2009());

			int CursorCell = 5;
			int indexAnnee = 0;
			for (int n = 2010; n < 2050; n = n + pasTemps) {
				HSSFCell cellAnnee = row.createCell(CursorCell);
				cellAnnee.setCellValue(e.getAnnees(indexAnnee));
				CursorCell++;
				indexAnnee = indexAnnee + pasTemps;
			}

			// 2050
			HSSFCell cell12;
			cell12 = row.createCell(CursorCell);
			cell12.setCellValue(e.getAnnee2050());

			i++;

		}
		// iterating r number of rows

		FileOutputStream outFile = new FileOutputStream("./Result_Excel/table_resultat.xls");

		// write this workbook to an Outputstream.
		wb.write(outFile);
		outFile.flush();
	}

	// 2. Evolution des consommations
	// 2.a. Extraction des résultats
	// 2.a.i Consos Non RT
	// 2.a.ii Consos RT
	// 2.b. Insertion dans le .xls
	// 2.b.i Consos Non RT
	// 2.b.ii Consos RT

	// 2.a. Evolution des consommations : Extraction des résultats
	// 2.a.i consoNonRT
	public List<ConsommationResultatsAnnee> getConsoAnnee(final int pasTemps) {

		String requestSelect = "select substr(id, 1,2) as branche, substr(id, 3,2) as sousBranche,"
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

				+ "	 substr(id, 1,2), substr(id, 3,2), substr(id,7,2),	 substr(id, 11,2), substr(id,13,2), usage"

				+ " order by 1, 2 , 3, 4, 5, 6 ";

		return jdbcTemplate.query(requestSelect, new RowMapper<ConsommationResultatsAnnee>() {
			@Override
			public ConsommationResultatsAnnee mapRow(ResultSet rs, int rowNum) throws SQLException {
				ConsommationResultatsAnnee sorties = new ConsommationResultatsAnnee();
				int columncount = rs.getMetaData().getColumnCount();
				for (int k = 1; k <= columncount; k++) {
					sorties.setBranche(rs.getString("branche"));
					sorties.setSousBranche(rs.getString("sousBranche"));
					sorties.setOccupation(rs.getString("occupation"));
					sorties.setPeriodeSimple(rs.getString("periodeSimple"));
					sorties.setEnergieUsage(rs.getString("energieUsage"));
					sorties.setUsage(rs.getString("usage"));
					sorties.setUsageSimple(rs.getString("usageSimple"));
					sorties.setFacteurEnergiePrimaire(rs.getDouble("FEP"));
					sorties.setAnnee2009(rs.getDouble("annee2009"));
					int n = 0;
					for (int i = 2010; i < 2050; i = i + pasTemps) {
						sorties.setAnnees(n, rs.getDouble("annee" + i));
						n = n + pasTemps;
					}
					sorties.setAnnee2050(rs.getDouble("annee2050"));
				}

				return sorties;

			}

		});

	}

	// 2.a.ii consoRT
	public List<ConsommationResultatsAnnee> getConsoRTAnnee(final int PasTemps) {

		String requestSelect = "select substr(id, 1,2) as branche, substr(id, 7,2) as occupation, "
				+ "substr(id, 11,2) as periodeSimple, (case when usage in ('Ventilation','Eclairage','Auxiliaires')  then '02' "
				+ "else substr(id,length(id)-1,2) end) as energieUsage, usage, case when usage in "
				+ "('Eclairage','Ventilation', 'Auxiliaires') then 'Electricité Spécifique' "
				+ "else usage end as usageSimple, case when (case when usage in ('Ventilation','Eclairage','Auxiliaires')"
				+ " then '02' " + "else substr(id,length(id)-1,2) end)='02' then 2.58 else 1 end as FEP, "
				+ " sum(case when annee='2009' then consommation_ef else 0 end) as annee2009, ";

		for (int cursorAnnee = 2010; cursorAnnee < 2050; cursorAnnee = cursorAnnee + PasTemps) {
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
					sorties.setOccupation(rs.getString("occupation"));
					sorties.setPeriodeSimple(rs.getString("periodeSimple"));
					sorties.setEnergieUsage(rs.getString("energieUsage"));
					sorties.setUsage(rs.getString("usage"));
					sorties.setUsageSimple(rs.getString("usageSimple"));
					sorties.setFacteurEnergiePrimaire(rs.getDouble("FEP"));
					sorties.setAnnee2009(rs.getDouble("annee2009"));
					int n = 0;
					for (int i = 2010; i < 2050; i = i + PasTemps) {
						sorties.setAnnees(n, rs.getDouble("annee" + i));
						n = n + PasTemps;
					}
					sorties.setAnnee2050(rs.getDouble("annee2050"));
				}

				return sorties;

			}

		});

	}

	// 2.b. Evolution des consommations : Insertion dans le .xls

	/**
	 * Récupère les données sur les consommations pour les inserer dans
	 * table_results.xls
	 * 
	 * @param pasTemps
	 * @throws SQLException
	 * @throws IOException
	 */
	public void getConsoAnneeXls(int pasTemps) throws SQLException, IOException {

		InputStream excelFileName = new FileInputStream("./Result_Excel/table_resultat.xls");
		HSSFWorkbook wb = new HSSFWorkbook(excelFileName);

		// get sheet
		HSSFSheet sheet = wb.getSheet("consoAnnee");

		// Empty Sheet
		int lastRow = sheet.getLastRowNum();
		HSSFRow oldRow = sheet.getRow(0);

		for (int n = 0; n <= lastRow; n++) {
			oldRow = sheet.getRow(n);
			if (oldRow != null) {
				sheet.removeRow(oldRow);
			}
		}

		// get list result (ConsommationResultatsAnnee) RT et Non RT
		// 2.b.i : Consommation Non RT
		List<ConsommationResultatsAnnee> list = getConsoAnnee(pasTemps);

		// create sheet headers
		HSSFRow row1 = sheet.createRow(0);
		HSSFCell cellHeader = row1.createCell(0);
		cellHeader.setCellValue("branche");
		HSSFCell cellHeader1 = row1.createCell(1);
		cellHeader1.setCellValue("occupation");
		HSSFCell cellHeader2 = row1.createCell(2);
		cellHeader2.setCellValue("periodeSimple");
		HSSFCell cellHeader3 = row1.createCell(3);
		cellHeader3.setCellValue("energieUsage");
		HSSFCell cellHeader4 = row1.createCell(4);
		cellHeader4.setCellValue("usage");
		HSSFCell cellHeader5 = row1.createCell(5);
		cellHeader5.setCellValue("usageSimple");
		HSSFCell cellHeader6 = row1.createCell(6);
		cellHeader6.setCellValue("facteurEnergiePrimaire");
		HSSFCell cellHeader7 = row1.createCell(7);
		cellHeader7.setCellValue(2009);

		int CursorHead = 8;
		for (int i = 2010; i < 2050; i = i + pasTemps) {
			HSSFCell cellHeaderAnnee = row1.createCell(CursorHead);
			cellHeaderAnnee.setCellValue(i);
			CursorHead++;
		}

		HSSFCell cellHeader12 = row1.createCell(CursorHead);
		cellHeader12.setCellValue(2050);

		// create row years
		int i = 1;
		for (ConsommationResultatsAnnee e : list) {

			HSSFRow row = sheet.createRow(i);

			HSSFCell cell = row.createCell(0);
			cell.setCellValue(e.getBranche());
			HSSFCell cell1 = row.createCell(1);
			cell1.setCellValue(e.getOccupation());
			HSSFCell cell2 = row.createCell(2);
			cell2.setCellValue(e.getPeriodeSimple());
			HSSFCell cell3 = row.createCell(3);
			cell3.setCellValue(e.getEnergieUsage());
			HSSFCell cell4 = row.createCell(4);
			cell4.setCellValue(e.getUsage());
			HSSFCell cell5 = row.createCell(5);
			cell5.setCellValue(e.getUsageSimple());
			HSSFCell cell6 = row.createCell(6);
			cell6.setCellValue(e.getFacteurEnergiePrimaire());
			HSSFCell cell7 = row.createCell(7);
			cell7.setCellValue(e.getAnnee2009());

			int CursorCell = 8;
			int indexAnnee = 0;
			for (int n = 2010; n < 2050; n = n + pasTemps) {
				HSSFCell cellAnnee = row.createCell(CursorCell);
				cellAnnee.setCellValue(e.getAnnees(indexAnnee));
				CursorCell++;
				indexAnnee = indexAnnee + pasTemps;
			}

			// 2050
			HSSFCell cell12;
			cell12 = row.createCell(CursorCell);
			cell12.setCellValue(e.getAnnee2050());

			i++;

		}

		// get list result (ConsommationRTAnnee)
		// 2.b.ii : Consommation RT
		List<ConsommationResultatsAnnee> list1 = getConsoRTAnnee(pasTemps);

		for (ConsommationResultatsAnnee e : list1) {

			HSSFRow row = sheet.createRow(i);

			HSSFCell cell = row.createCell(0);
			cell.setCellValue(e.getBranche());
			HSSFCell cell1 = row.createCell(1);
			cell1.setCellValue(e.getOccupation());
			HSSFCell cell2 = row.createCell(2);
			cell2.setCellValue(e.getPeriodeSimple());
			HSSFCell cell3 = row.createCell(3);
			cell3.setCellValue(e.getEnergieUsage());
			HSSFCell cell4 = row.createCell(4);
			cell4.setCellValue(e.getUsage());
			HSSFCell cell5 = row.createCell(5);
			cell5.setCellValue(e.getUsageSimple());
			HSSFCell cell6 = row.createCell(6);
			cell6.setCellValue(e.getFacteurEnergiePrimaire());
			HSSFCell cell7 = row.createCell(7);
			cell7.setCellValue(e.getAnnee2009());

			int CursorCell = 8;
			int indexAnnee = 0;
			for (int n = 2010; n < 2050; n = n + pasTemps) {
				HSSFCell cellAnnee = row.createCell(CursorCell);
				cellAnnee.setCellValue(e.getAnnees(indexAnnee));
				CursorCell++;
				indexAnnee = indexAnnee + pasTemps;
			}

			// 2050
			HSSFCell cell12;
			cell12 = row.createCell(CursorCell);
			cell12.setCellValue(e.getAnnee2050());

			i++;

		}
		// iterating r number of rows

		// iterating r number of rows

		FileOutputStream outFile = new FileOutputStream("./Result_Excel/table_resultat.xls");

		// write this workbook to an Outputstream.
		wb.write(outFile);
		outFile.flush();
	}

	// 3. Evolution des émissions de GES
	// 3.a. Extraction des résultats
	// 3.a.i Emissions Non RT
	// 3.a.ii Emissions RT
	// 3.b. Insertion dans le .xls

	// 3.a. Extraction des résultats
	// 3.a.i Emissions Non RT

	public List<ConsommationResultatsAnnee> getGESAnnee(final int PasTemps) {

		String requestSelect = "select substr(c.id, 1,2) as branche, substr(c.id, 3,2) as sousBranche,"
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

				+ "	group by 	substr(c.id, 1,2), substr(c.id, 3,2), substr(c.id, 7,2),"
				+ "	substr(c.id, 11,2), substr(c.id,13,2), c.usage "

				+ " order by 1, 2 , 3, 4, 5, 6 ";

		return jdbcTemplate.query(requestSelect, new RowMapper<ConsommationResultatsAnnee>() {
			@Override
			public ConsommationResultatsAnnee mapRow(ResultSet rs, int rowNum) throws SQLException {
				ConsommationResultatsAnnee sorties = new ConsommationResultatsAnnee();
				int columncount = rs.getMetaData().getColumnCount();
				for (int k = 1; k <= columncount; k++) {
					sorties.setBranche(rs.getString("branche"));
					sorties.setSousBranche(rs.getString("sousBranche"));
					sorties.setOccupation(rs.getString("occupation"));
					sorties.setPeriodeSimple(rs.getString("periodeSimple"));
					sorties.setEnergieUsage(rs.getString("energieUsage"));
					sorties.setUsage(rs.getString("usage"));
					sorties.setUsageSimple(rs.getString("usageSimple"));
					sorties.setAnnee2009(rs.getDouble("annee2009"));
					int n = 0;
					for (int i = 2010; i < 2050; i = i + PasTemps) {
						sorties.setAnnees(n, rs.getDouble("annee" + i));
						n = n + PasTemps;
					}
					sorties.setAnnee2050(rs.getDouble("annee2050"));
				}

				return sorties;

			}

		});

	}

	// 3.a.ii Emissions RT
	public List<ConsommationResultatsAnnee> getGESRTAnnee(final int PasTemps) {

		String requestSelect = "select 	substr(c.id, 1,2) as branche,  substr(c.id, 7,2) as occupation, "
				+ "	substr(c.id, 11,2) as periodeSimple, 	(case when c.usage in ('Ventilation','Eclairage','Auxiliaires') then '02' "
				+ "	else substr(c.id,length(c.id)-1,2) end) as energieUsage, 	c.usage, "
				+ "	case when c.usage in 	('Eclairage','Ventilation', "
				+ "	'Bureautique','Froid_alimentaire', 'Process', 'Auxiliaires') then 'Electricité Spécifique' "
				+ "	else c.usage end as usageSimple, "

				+ "	sum(case when c.annee='2009' then c.consommation_ef*fe.periode1 else 0 end) as annee2009, ";

		// Boucle sur les années 2010 à 2050 selon le pas de temps
		for (int cursorAnnee = 2010; cursorAnnee < 2050; cursorAnnee = cursorAnnee + PasTemps) {
			requestSelect = requestSelect + "sum(case when c.annee='" + cursorAnnee + "' then c.consommation_ef*(case "
					+ "	when '" + cursorAnnee + "'*1 <2015 then fe.periode1 	when '" + cursorAnnee
					+ "'*1 <2020 then fe.periode2 	when '" + cursorAnnee + "'*1 <2030 then fe.periode3 when '"
					+ cursorAnnee + "'*1 <2040 then fe.periode4 " + "	else fe.periode5 end) else 0 end) as annee"
					+ cursorAnnee + ", ";
		}

		requestSelect = requestSelect
				+ "	sum(case when c.annee='2050' then consommation_ef*fe.periode5 else 0 end) as annee2050 "

				+ " from conso_rt_resultats c "

				// Correspondance Energie_ID vers Energie

				+ " join (select distinct substr(ii.id,17,2) as iid, energie_chauff as energie_usage from id ii) i "
				+ " 	on i.iid=case when c.usage in ('Ventilation','Eclairage','Auxiliaires') then '02' else substr(c.id,length(id)-1,2) end "

				// Facteurs d'emission issus du fichier parametres
				+ "  join param_emissions fe on fe.energie=i.energie_usage  "
				+ "			and fe.usage=case when c.usage in ('Ventilation','Auxiliaires') then 'Autres'"
				+ "						when c.usage in ('Eclairage') then c.usage	"
				+ "						when i.iid!='02' then 'Tous'"
				// +
				// "						when c.usage not in ('Chauffage', 'Climatisation', 'Cuisson', 'Eclairage', 'ECS') then 'Tous' "
				+ "						else c.usage end "

				+ " group by  	  substr(c.id, 1,2), substr(c.id, 7,2),  substr(c.id, 11,2), "
				+ " substr(c.id,length(c.id)-1,2), c.usage "

				+ " order by 1, 2 , 3, 4, 5, 6 ";

		return jdbcTemplate.query(requestSelect, new RowMapper<ConsommationResultatsAnnee>() {
			@Override
			public ConsommationResultatsAnnee mapRow(ResultSet rs, int rowNum) throws SQLException {
				ConsommationResultatsAnnee sorties = new ConsommationResultatsAnnee();
				int columncount = rs.getMetaData().getColumnCount();
				for (int k = 1; k <= columncount; k++) {
					sorties.setBranche(rs.getString("branche"));
					sorties.setOccupation(rs.getString("occupation"));
					sorties.setPeriodeSimple(rs.getString("periodeSimple"));
					sorties.setEnergieUsage(rs.getString("energieUsage"));
					sorties.setUsage(rs.getString("usage"));
					sorties.setUsageSimple(rs.getString("usageSimple"));
					sorties.setAnnee2009(rs.getDouble("annee2009"));
					int n = 0;
					for (int i = 2010; i < 2050; i = i + PasTemps) {
						sorties.setAnnees(n, rs.getDouble("annee" + i));
						n = n + PasTemps;
					}
					sorties.setAnnee2050(rs.getDouble("annee2050"));
				}

				return sorties;

			}

		});

	}

	// 3.b. Insertion dans le .xls

	/**
	 * Récupère les données sur les emissions de GES pour les inserer dans
	 * table_results.xls
	 * 
	 * @param pasTemps
	 * @throws SQLException
	 * @throws IOException
	 */
	public void getGESAnneeXls(int pasTemps) throws SQLException, IOException {

		InputStream excelFileName = new FileInputStream("./Result_Excel/table_resultat.xls");
		HSSFWorkbook wb = new HSSFWorkbook(excelFileName);

		// getSheet
		HSSFSheet sheet = wb.getSheet("GESAnnee");

		// Empty Sheet
		int lastRow = sheet.getLastRowNum();
		HSSFRow oldRow = sheet.getRow(0);

		for (int n = 0; n <= lastRow; n++) {
			oldRow = sheet.getRow(n);
			if (oldRow != null) {
				sheet.removeRow(oldRow);
			}
		}

		// get list Emission (ConsommationResultatsAnnee) RT et Non RT
		// 3.b.i : Emissions Non RT
		List<ConsommationResultatsAnnee> list = getGESAnnee(pasTemps);

		// create sheet headers
		HSSFRow row1 = sheet.createRow(0);
		HSSFCell cellHeader = row1.createCell(0);
		cellHeader.setCellValue("branche");
		HSSFCell cellHeader1 = row1.createCell(1);
		cellHeader1.setCellValue("occupation");
		HSSFCell cellHeader2 = row1.createCell(2);
		cellHeader2.setCellValue("periodeSimple");
		HSSFCell cellHeader3 = row1.createCell(3);
		cellHeader3.setCellValue("energieUsage");
		HSSFCell cellHeader4 = row1.createCell(4);
		cellHeader4.setCellValue("usage");
		HSSFCell cellHeader5 = row1.createCell(5);
		cellHeader5.setCellValue("usageSimple");
		HSSFCell cellHeader6 = row1.createCell(6);
		cellHeader6.setCellValue(2009);

		int CursorHead = 7;
		for (int i = 2010; i < 2050; i = i + pasTemps) {
			HSSFCell cellHeaderAnnee = row1.createCell(CursorHead);
			cellHeaderAnnee.setCellValue(i);
			CursorHead++;
		}

		HSSFCell cellHeader12 = row1.createCell(CursorHead);
		cellHeader12.setCellValue(2050);

		// create row years
		int i = 1;
		for (ConsommationResultatsAnnee e : list) {

			HSSFRow row = sheet.createRow(i);

			HSSFCell cell = row.createCell(0);
			cell.setCellValue(e.getBranche());
			HSSFCell cell1 = row.createCell(1);
			cell1.setCellValue(e.getOccupation());
			HSSFCell cell2 = row.createCell(2);
			cell2.setCellValue(e.getPeriodeSimple());
			HSSFCell cell3 = row.createCell(3);
			cell3.setCellValue(e.getEnergieUsage());
			HSSFCell cell4 = row.createCell(4);
			cell4.setCellValue(e.getUsage());
			HSSFCell cell5 = row.createCell(5);
			cell5.setCellValue(e.getUsageSimple());
			HSSFCell cell6 = row.createCell(6);
			cell6.setCellValue(e.getAnnee2009());

			int CursorCell = 7;
			int indexAnnee = 0;
			for (int n = 2010; n < 2050; n = n + pasTemps) {
				HSSFCell cellAnnee = row.createCell(CursorCell);
				cellAnnee.setCellValue(e.getAnnees(indexAnnee));
				CursorCell++;
				indexAnnee = indexAnnee + pasTemps;
			}

			// 2050
			HSSFCell cell12;
			cell12 = row.createCell(CursorCell);
			cell12.setCellValue(e.getAnnee2050());

			i++;

		}
		// LOG.info(" fin conso non rt i :" +i );

		// get list result (GESRTAnnee)
		// 3.b.ii : Emissions RT
		List<ConsommationResultatsAnnee> list1 = getGESRTAnnee(pasTemps);

		for (ConsommationResultatsAnnee e : list1) {

			HSSFRow row = sheet.createRow(i);

			HSSFCell cell = row.createCell(0);
			cell.setCellValue(e.getBranche());
			HSSFCell cell1 = row.createCell(1);
			cell1.setCellValue(e.getOccupation());
			HSSFCell cell2 = row.createCell(2);
			cell2.setCellValue(e.getPeriodeSimple());
			HSSFCell cell3 = row.createCell(3);
			cell3.setCellValue(e.getEnergieUsage());
			HSSFCell cell4 = row.createCell(4);
			cell4.setCellValue(e.getUsage());
			HSSFCell cell5 = row.createCell(5);
			cell5.setCellValue(e.getUsageSimple());
			HSSFCell cell6 = row.createCell(6);
			cell6.setCellValue(e.getAnnee2009());

			int CursorCell = 7;
			int indexAnnee = 0;
			for (int n = 2010; n < 2050; n = n + pasTemps) {
				HSSFCell cellAnnee = row.createCell(CursorCell);
				cellAnnee.setCellValue(e.getAnnees(indexAnnee));
				CursorCell++;
				indexAnnee = indexAnnee + pasTemps;
			}

			// 2050
			HSSFCell cell12;
			cell12 = row.createCell(CursorCell);
			cell12.setCellValue(e.getAnnee2050());

			i++;

		}
		// LOG.info(" fin de fichier i :" +i );

		// iterating r number of rows

		FileOutputStream outFile = new FileOutputStream("./Result_Excel/table_resultat.xls");

		// write this workbook to an Outputstream.
		wb.write(outFile);
		outFile.flush();
	}

	/* XSLX : pas utilise *//*
							 * public static void traitementXlsx() throws
							 * SQLException, IOException { int index = 0;
							 * InputStream excelFileName = new FileInputStream(
							 * "/home/belkaid/workspace/CGDD/Result_Excel/table_resultat.xlsx"
							 * );
							 * 
							 * XSSFWorkbook wb = new
							 * XSSFWorkbook(excelFileName);
							 * 
							 * // remove sheet index =
							 * wb.getSheetIndex("resultat_brut_conso_RT");
							 * wb.removeSheetAt(index); // create sheet
							 * XSSFSheet sheet =
							 * wb.createSheet("resultat_brut_conso_RT");
							 * List<TableResult> list = null; // list =
							 * getParcResultat();
							 * 
							 * // sheet headers XSSFRow row1 =
							 * sheet.createRow(0); XSSFCell cellHeader =
							 * row1.createCell(0);
							 * cellHeader.setCellValue("ID"); XSSFCell
							 * cellHeader1 = row1.createCell(1);
							 * cellHeader1.setCellValue("SURFACES"); // data int
							 * i = 1; for (TableResult e : list) {
							 * 
							 * XSSFRow row = sheet.createRow(i); XSSFCell cell =
							 * row.createCell(0); cell.setCellValue(e.getId());
							 * XSSFCell cell1 = row.createCell(1);
							 * cell1.setCellValue(e.getSurfaces());
							 * LOG.info(e.getId()); LOG.info(e.getSurfaces());
							 * i++;
							 * 
							 * } // iterating r number of rows
							 * 
							 * FileOutputStream outFile = new FileOutputStream(
							 * "/home/belkaid/workspace/CGDD/Result_Excel/table_resultat.xlsx"
							 * );
							 * 
							 * // write this workbook to an Outputstream.
							 * wb.write(outFile); outFile.flush(); }
							 */

	public void insertMasse(TableResult tableresult) {

		String requestInsert = "INSERT INTO PARC_INIT (ID,SURFACES) VALUES (?,?)";
		jdbcTemplate.update(requestInsert, new Object[] { tableresult.getId(), tableresult.getSurfaces() });

	}

	// TRANCAATTEEE
	public void trancate() {

		String requestTruncate = "TRUNCATE TABLE PARC_INIT";
		jdbcTemplate.update(requestTruncate);

	}

	/**
	 * Cache ou fait apparaitre les onglets du fichier xls qui contiennent les
	 * données brutes importées (parcAnnees, consoAnnes, GESAnnees,
	 * Nommenclature)
	 * 
	 * @param bool
	 * @throws IOException
	 */
	public void setImportSheetHidden(Boolean bool) throws IOException {

		InputStream excelFileName = new FileInputStream("./Result_Excel/table_resultat.xls");

		HSSFWorkbook wb = new HSSFWorkbook(excelFileName);
		HSSFSheet sheet;

		sheet = wb.getSheet("parcAnnee");
		wb.setSheetHidden(wb.getSheetIndex(sheet), bool);
		sheet = wb.getSheet("consoAnnee");
		wb.setSheetHidden(wb.getSheetIndex(sheet), bool);
		sheet = wb.getSheet("GESAnnee");
		wb.setSheetHidden(wb.getSheetIndex(sheet), bool);
		sheet = wb.getSheet("Nommenclature");
		wb.setSheetHidden(wb.getSheetIndex(sheet), bool);

		// write this workbook to an Outputstream.
		FileOutputStream outFile = new FileOutputStream("./Result_Excel/table_resultat.xls");
		wb.write(outFile);
		outFile.flush();
	}

	/**
	 * Met a jour les formules du fichier fichier Xls table_resultats.xls
	 * 
	 * Les formules renvoient #VALEUR! après l'opération, il faut quand meme les
	 * re-mettre à jour avec F9.
	 * 
	 * @throws IOException
	 */
	public void updateXls() throws IOException {

		InputStream excelFileName = new FileInputStream("./Result_Excel/table_resultat.xls");

		HSSFWorkbook wb = new HSSFWorkbook(excelFileName);

		// Evalue les formules de toutes les cellules de toutes les feuilles
		// Les formules renvoient #valeur! dans excel..
		// TODO Really update workbook

		FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
		evaluator.clearAllCachedResultValues();
		evaluator.evaluateAll();
		wb.setForceFormulaRecalculation(true);

		// write this workbook to an Outputstream.
		FileOutputStream outFile = new FileOutputStream("./Result_Excel/table_resultat.xls");
		wb.write(outFile);
		outFile.flush();
	}
}
