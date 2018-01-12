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

public class ExcelEtiquetteServiceImpl implements ExcelEtiquetteService {

	private final static Logger LOG = LogManager.getLogger(ExcelEtiquetteServiceImpl.class);

	// load data table PARC_INIT
	private JdbcTemplate jdbcTemplate;

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	/**
	 * excelService renvoie etiquette dans le fichier table_resultats.xls.
	 * 
	 * Il met à jour les résultats.
	 * 
	 * Il définit si les feuilles d'import sont cachées ou visibles.
	 */
	@Override
	public void excelService(int pasTemps, boolean isHidden) {
		try {

			LOG.info("Classeur table_resultat.xls");
			LOG.info("	Import Xls : etiquettResultats");
			getEtiquetteXls();
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

	// 1.b Evolution du parc : Insertion dans le .xls

	/**
	 * 
	 * Récupère les données sur le parc pour les inserer dans table_results.xls
	 *
	 * @throws SQLException
	 * @throws IOException
	 */
	public void getEtiquetteXls() throws SQLException, IOException {

		InputStream excelFileName = new FileInputStream("./Result_Excel/table_resultat.xls");
		HSSFWorkbook wb = new HSSFWorkbook(excelFileName);
		// get sheet
		HSSFSheet sheet = wb.getSheet("etiquette");

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
		List<ConsommationResultatsAnnee> list = getEtiquette();

		// create sheet headers
		HSSFRow row1 = sheet.createRow(0);
		HSSFCell cellHeader = row1.createCell(0);
		cellHeader.setCellValue("annee");
		HSSFCell cellHeader1 = row1.createCell(1);
		cellHeader1.setCellValue("branche");
		HSSFCell cellHeader2 = row1.createCell(2);
		cellHeader2.setCellValue("ss_branche");
		HSSFCell cellHeader3 = row1.createCell(3);
		cellHeader3.setCellValue("bat_type");
		HSSFCell cellHeader4 = row1.createCell(4);
		cellHeader4.setCellValue("occupant");
		HSSFCell cellHeader5 = row1.createCell(5);
		cellHeader5.setCellValue("periode_detail");
		HSSFCell cellHeader6 = row1.createCell(6);
		cellHeader6.setCellValue("periode_simple");
		HSSFCell cellHeader7 = row1.createCell(7);
		cellHeader7.setCellValue("systeme_chauff");
		HSSFCell cellHeader8 = row1.createCell(8);
		cellHeader8.setCellValue("systeme_froid");
		HSSFCell cellHeader9 = row1.createCell(9);
		cellHeader9.setCellValue("surface");
		HSSFCell cellHeader10 = row1.createCell(10);
		cellHeader10.setCellValue("conso_u");
		HSSFCell cellHeader11 = row1.createCell(11);
		cellHeader11.setCellValue("etiquette");

		// create row years
		int i = 1;
		for (ConsommationResultatsAnnee e : list) {

			HSSFRow row = sheet.createRow(i);

			HSSFCell cell = row.createCell(0);
			cell.setCellValue(e.getAnnee());
			HSSFCell cell1 = row.createCell(1);
			cell1.setCellValue(e.getBranche());
			HSSFCell cell2 = row.createCell(2);
			cell2.setCellValue(e.getSousBranche());
			HSSFCell cell3 = row.createCell(3);
			cell3.setCellValue(e.getBatimentType());
			HSSFCell cell4 = row.createCell(4);
			cell4.setCellValue(e.getOccupation());
			HSSFCell cell5 = row.createCell(5);
			cell5.setCellValue(e.getPeriodeDetail());
			HSSFCell cell6 = row.createCell(6);
			cell6.setCellValue(e.getPeriodeSimple());
			HSSFCell cell7 = row.createCell(7);
			cell7.setCellValue(e.getSystemeChaud());
			HSSFCell cell8 = row.createCell(8);
			cell8.setCellValue(e.getSystemeFroid());
			HSSFCell cell9 = row.createCell(9);
			cell9.setCellValue(e.getSurface());
			HSSFCell cell10 = row.createCell(10);
			cell10.setCellValue(e.getConsommationUnitaire());
			HSSFCell cell11 = row.createCell(11);
			cell11.setCellValue(e.getEtiquette());

			i++;

		}
		// iterating r number of rows

		FileOutputStream outFile = new FileOutputStream("./Result_Excel/table_resultat.xls");

		// write this workbook to an Outputstream.
		wb.write(outFile);
		outFile.flush();
	}

	public List<ConsommationResultatsAnnee> getEtiquette() {

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
				+ " 		and cp.annee in ('2010','2020','2030','2050')   "

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
				+ " 	 where cp.annee in ('2010','2020','2030','2050') "
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

				+ " 		 and cp.annee in ('2010','2020','2030','2050') 	 	 "
				+ " 		 group by substr(cp.id,1,2), substr(cp.id,3,2), substr(cp.id,5,2), substr(cp.id,7,2), substr(cp.id,9,2), substr(cp.id,11,2), substr(cp.id,13,2), cp.usage, substr(cp.id,length(id)-1,2),  cp.annee) "
				+ " 		 conso_det  "

				+ " 	 join   "
				+ " 		 (select "
				+ " 			substr(cp.id,1,2) as branche, substr(cp.id,3,2) as ss_branche, substr(cp.id,5,2) as bat_type, substr(cp.id,7,2) as occupant, substr(cp.id,9,2) as periode_detail, substr(cp.id,11,2) as periode_simple, substr(cp.id,15,2) as systeme_froid, cp.annee, sum(cp.surfaces) as surface    "
				+ " 		 from parc_resultats cp "

				+ " 		 where  cp.annee in ('2010','2020','2030','2050')  "

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
				+ " 			 where cp.usage in ('Eclairage','ECS') and cp.annee in ('2010','2020','2030','2050') 	  "
				+ " 			 group by substr(cp.id,1,2), substr(cp.id,3,2), substr(cp.id,5,2), substr(cp.id,7,2), substr(cp.id,9,2), substr(cp.id,11,2), cp.annee "
				+ " 			) conso_det   "
				+ " 	 "
				+ " 	left  join ( "
				+ " 		select "
				+ " 		substr(cp.id,1,2) as branche, substr(cp.id,3,2) as ss_branche, substr(cp.id,5,2) as bat_type, substr(cp.id,7,2) as occupant, substr(cp.id,9,2) as periode_detail, substr(cp.id,11,2) as periode_simple, cp.annee, sum(cp.surfaces) as surface    "

				+ " 		 from parc_resultats cp     "

				+ " 		 where cp.annee in ('2010','2020','2030','2050')  "

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

		sheet = wb.getSheet("etiquette");
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
