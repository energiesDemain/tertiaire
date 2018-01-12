package com.ed.cgdd.derby.excelresult;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
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

import com.ed.cgdd.derby.model.financeObjects.CoutEnergie;
import com.ed.cgdd.derby.model.parc.ConsommationResultatsAnnee;

public class ExcelCoutsServiceImpl implements ExcelCoutsService {
	private final static Logger LOG = LogManager.getLogger(ExcelCoutsServiceImpl.class);
	private JdbcTemplate jdbcTemplate;

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	/**
	 * excelService appelle les méthodes : getCoutsXls(),
	 * setImportSheetHidden(isHidden), et updateXls().
	 * 
	 */
	@Override
	public void excelService(int pasTemps, boolean isHidden) {
		try {
			LOG.info("Classeur table_resultat.xls");
			LOG.info("	Import Xls : coutResultats");
			getCoutXls();
			LOG.info("	Import Xls : coutSystemesAutres");
			getCoutAutresXls();
			LOG.info("	Hide import sheet : " + isHidden);
			setImportSheetHidden(isHidden);
			// LOG.info("	Update Xls");
			// updateXls();

		} catch (SQLException ex) {
			LOG.error(ex);
		} catch (IOException e) {
			LOG.error(e);

		}
	}

	// Sorties Excel :
	// 1.a. Extraction des résultats
	// 1.b. Insertion dans le .xls onglet couts

	// 1.a. Extraction des résultats

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
				// Le geste sur le bâti est codé dans Cible : xx =
				// [Etat initial = 00; Renovation Modeste = 1x; GTB = 3x;
				// Renovation BBC = 2x;
				// Fenetres = x1; Fenetres+Murs = x2;
				// Ensemble = x3; GTB = x4]

				+ " case when r.annee_renov_sys ='NP' then r.annee_renov_bat else r.annee_renov_sys end as annee, "
				+ " r.reglementation as reglementation,  "
				+ " sum(r.surface) as surface, sum(r.aides) as aides, sum(r.cout_inv) as investissement, sum(r.valeur_pret) as prets, sum(r.valeur_pret_bonif) as prets_bonif "

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

	// 1.b. Insertion dans le .xls onglet couts
	// 1.b Evolution du parc : Insertion dans le .xls

	/**
	 * Récupère les données sur les couts d'investissement depuis
	 * resultats_financements et les insère dans table_resultats.xls
	 * 
	 * @throws SQLException
	 * @throws IOException
	 */
	public void getCoutXls() throws SQLException, IOException {

		int pasTemps = 1;

		InputStream excelFileName = new FileInputStream("./Result_Excel/table_resultat.xls");

		HSSFWorkbook wb = new HSSFWorkbook(excelFileName);

		// get sheet
		HSSFSheet sheet = wb.getSheet("cout");

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
		List<ConsommationResultatsAnnee> list = getCout(pasTemps);

		// create sheet headers
		HSSFRow row1 = sheet.createRow(0);
		HSSFCell cellHeader = row1.createCell(0);
		cellHeader.setCellValue("branche");
		HSSFCell cellHeader1 = row1.createCell(1);
		cellHeader1.setCellValue("occupant");
		HSSFCell cellHeader2 = row1.createCell(2);
		cellHeader2.setCellValue("typeRenovationBatiment");
		HSSFCell cellHeader3 = row1.createCell(3);
		cellHeader3.setCellValue("typeRenovationSysteme");
		HSSFCell cellHeader4 = row1.createCell(4);
		cellHeader4.setCellValue("cible");
		HSSFCell cellHeader5 = row1.createCell(5);
		cellHeader5.setCellValue("annee");
		HSSFCell cellHeader6 = row1.createCell(6);
		cellHeader6.setCellValue("surface");
		HSSFCell cellHeader7 = row1.createCell(7);
		cellHeader7.setCellValue("aides");
		HSSFCell cellHeader8 = row1.createCell(8);
		cellHeader8.setCellValue("investissement");
		HSSFCell cellHeader9 = row1.createCell(9);
		cellHeader9.setCellValue("prets");
		HSSFCell cellHeader10 = row1.createCell(10);
		cellHeader10.setCellValue("pretsBonifies");
		HSSFCell cellHeader11 = row1.createCell(11);
		cellHeader11.setCellValue("reglementation");

		// create row years
		int i = 1;
		for (ConsommationResultatsAnnee e : list) {

			HSSFRow row = sheet.createRow(i);

			HSSFCell cell = row.createCell(0);
			cell.setCellValue(e.getBranche());
			HSSFCell cell1 = row.createCell(1);
			cell1.setCellValue(e.getOccupation());
			HSSFCell cell2 = row.createCell(2);
			cell2.setCellValue(e.getTypeRenovationBati());
			HSSFCell cell3 = row.createCell(3);
			cell3.setCellValue(e.getTypeRenovationSysteme());
			HSSFCell cell4 = row.createCell(4);
			cell4.setCellValue(e.getCible());
			HSSFCell cell5 = row.createCell(5);
			cell5.setCellValue(e.getAnnee());
			HSSFCell cell6 = row.createCell(6);
			cell6.setCellValue(e.getSurface());
			HSSFCell cell7 = row.createCell(7);
			cell7.setCellValue(e.getAides());
			HSSFCell cell8 = row.createCell(8);
			cell8.setCellValue(e.getInvestissement());
			HSSFCell cell9 = row.createCell(9);
			cell9.setCellValue(e.getPrets());
			HSSFCell cell10 = row.createCell(10);
			cell10.setCellValue(e.getPretsBonifies());
			HSSFCell cell11 = row.createCell(11);
			cell11.setCellValue(e.getReglementation());
			i++;

		}

		FileOutputStream outFile = new FileOutputStream("./Result_Excel/table_resultat.xls");

		// write this workbook to an Outputstream.
		wb.write(outFile);
		outFile.flush();
	}

	/**
	 * Récupère les données sur les couts d'investissement pour ECS Eclairage et
	 * clim et les insère dans table_resultats.xls, onglet coutSystemesAutres
	 * 
	 * @throws SQLException
	 * @throws IOException
	 */
	public void getCoutAutresXls() throws SQLException, IOException {

		int pasTemps = 1;

		InputStream excelFileName = new FileInputStream("./Result_Excel/table_resultat.xls");

		HSSFWorkbook wb = new HSSFWorkbook(excelFileName);

		// get sheet
		HSSFSheet sheet = wb.getSheet("coutSystemesAutres");

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
		List<ConsommationResultatsAnnee> list = getCoutSystemesAutres(pasTemps);

		// create sheet headers
		HSSFRow row1 = sheet.createRow(0);
		HSSFCell cellHeader = row1.createCell(0);
		cellHeader.setCellValue("branche");
		HSSFCell cellHeader1 = row1.createCell(1);
		cellHeader1.setCellValue("occupant");
		HSSFCell cellHeader2 = row1.createCell(2);
		cellHeader2.setCellValue("typeRenovationBatiment");
		HSSFCell cellHeader3 = row1.createCell(3);
		cellHeader3.setCellValue("typeRenovationSysteme");
		HSSFCell cellHeader4 = row1.createCell(4);
		cellHeader4.setCellValue("cible");
		HSSFCell cellHeader5 = row1.createCell(5);
		cellHeader5.setCellValue("annee");
		HSSFCell cellHeader6 = row1.createCell(6);
		cellHeader6.setCellValue("surface");
		HSSFCell cellHeader7 = row1.createCell(7);
		cellHeader7.setCellValue("aides");
		HSSFCell cellHeader8 = row1.createCell(8);
		cellHeader8.setCellValue("investissement");
		HSSFCell cellHeader9 = row1.createCell(9);
		cellHeader9.setCellValue("prets");
		HSSFCell cellHeader10 = row1.createCell(10);
		cellHeader10.setCellValue("pretsBonifies");
		HSSFCell cellHeader11 = row1.createCell(11);
		cellHeader11.setCellValue("reglementation");

		// create row years
		int i = 1;
		for (ConsommationResultatsAnnee e : list) {

			HSSFRow row = sheet.createRow(i);

			HSSFCell cell = row.createCell(0);
			cell.setCellValue(e.getBranche());
			HSSFCell cell1 = row.createCell(1);
			cell1.setCellValue(e.getOccupation());
			HSSFCell cell2 = row.createCell(2);
			cell2.setCellValue(e.getTypeRenovationBati());
			HSSFCell cell3 = row.createCell(3);
			cell3.setCellValue(e.getTypeRenovationSysteme());
			HSSFCell cell4 = row.createCell(4);
			cell4.setCellValue(e.getCible());
			HSSFCell cell5 = row.createCell(5);
			cell5.setCellValue(e.getAnnee());
			HSSFCell cell6 = row.createCell(6);
			cell6.setCellValue(e.getSurface());
			HSSFCell cell7 = row.createCell(7);
			cell7.setCellValue(e.getAides());
			HSSFCell cell8 = row.createCell(8);
			cell8.setCellValue(e.getInvestissement());
			HSSFCell cell9 = row.createCell(9);
			cell9.setCellValue(e.getPrets());
			HSSFCell cell10 = row.createCell(10);
			cell10.setCellValue(e.getPretsBonifies());
			HSSFCell cell11 = row.createCell(11);
			cell11.setCellValue(e.getReglementation());
			i++;

		}

		FileOutputStream outFile = new FileOutputStream("./Result_Excel/table_resultat.xls");

		// write this workbook to an Outputstream.
		wb.write(outFile);
		outFile.flush();
	}

	/**
	 * Insère la contribution climat dans le fichier table_resultat.xls
	 * 
	 * @throws IOException
	 */

	@Override
	public void getContributionClimat(HashMap<Integer, CoutEnergie> coutEnergieMap) throws IOException {

		// LOG.info("test");

		InputStream excelFileName = new FileInputStream("./Result_Excel/table_resultat.xls");
		HSSFWorkbook wb = new HSSFWorkbook(excelFileName);

		// get sheet
		HSSFSheet sheet = wb.getSheet("contributionClimat");

		// Empty Sheet
		int lastRow = sheet.getLastRowNum();
		HSSFRow oldRow = sheet.getRow(0);

		for (int n = 0; n <= lastRow; n++) {
			oldRow = sheet.getRow(n);
			if (oldRow != null) {
				sheet.removeRow(oldRow);
			}
		}

		// create sheet headers
		HSSFRow row1 = sheet.createRow(0);
		HSSFCell cellHeader = row1.createCell(0);
		cellHeader.setCellValue("année");
		HSSFCell cellHeader1 = row1.createCell(1);
		cellHeader1.setCellValue("contributionClimat");

		// int annee = 2010;
		// create row years
		int i = 0;
		CoutEnergie coutAnnee;
		for (int cle : coutEnergieMap.keySet()) {

			coutAnnee = coutEnergieMap.get(cle);
			HSSFRow row = sheet.createRow(i);

			HSSFCell cell = row.createCell(0);
			cell.setCellValue(coutAnnee.getAnnee());
			HSSFCell cell1 = row.createCell(1);
			cell1.setCellValue(coutAnnee.getCCE().doubleValue());

			i++;

		}

		FileOutputStream outFile = new FileOutputStream("./Result_Excel/table_resultat.xls");

		// write this workbook to an Outputstream.
		wb.write(outFile);
		outFile.flush();
	}

	/**
	 * Cache ou fait apparaitre les onglets du fichier xls qui contiennent les
	 * données brutes importées (cout, coutSystemesAutres, Nommenclature)
	 * 
	 * isHidden = [true : cache les feuilles; false : les fait apparaitre];
	 * 
	 * @param bool
	 * @throws IOException
	 */
	public void setImportSheetHidden(Boolean bool) throws IOException {

		InputStream excelFileName = new FileInputStream("./Result_Excel/table_resultat.xls");

		HSSFWorkbook wb = new HSSFWorkbook(excelFileName);
		HSSFSheet sheet;

		sheet = wb.getSheet("cout");
		wb.setSheetHidden(wb.getSheetIndex(sheet), bool);
		sheet = wb.getSheet("Nommenclature");
		wb.setSheetHidden(wb.getSheetIndex(sheet), bool);
		sheet = wb.getSheet("contributionClimat");
		wb.setSheetHidden(wb.getSheetIndex(sheet), bool);
		sheet = wb.getSheet("coutSystemesAutres");
		wb.setSheetHidden(wb.getSheetIndex(sheet), bool);

		// Write output to file

		FileOutputStream outFile = new FileOutputStream("./Result_Excel/table_resultat.xls");

		wb.write(outFile);
		outFile.flush();
	}

	/**
	 * Met a jour les formules du fichier fichier Xls table_resultats.xls Les
	 * formules renvoient #VALEUR! après l'opération : il faut quand meme le
	 * mettre à jour avec F9.
	 * 
	 * Evite à l'utilisateur de travailler avec des données dépassées
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
