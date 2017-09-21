package com.ed.cgdd.derby.loadparam.impl;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import com.ed.cgdd.derby.loadparam.ImportExcelParamDAS;
import com.ed.cgdd.derby.loadparam.InsertParamDAS;
import com.ed.cgdd.derby.loadparam.LoadInfoParamDAS;
import com.ed.cgdd.derby.loadparam.LoadParamService;
import com.ed.cgdd.derby.loadparam.TruncateParamDAS;
import com.ed.cgdd.derby.model.excelobjects.ExcelParameters;
import com.ed.cgdd.derby.model.excelobjects.GenericExcelData;
import com.ed.cgdd.derby.model.progression.Progression;
import com.ed.cgdd.derby.model.progression.ProgressionStep;

public class LoadParamServiceImpl implements LoadParamService {
	private final static Logger LOG = LogManager.getLogger(LoadParamServiceImpl.class);

	private InsertParamDAS insertParamdas;

	private ImportExcelParamDAS importExcelParamdas;
	private LoadInfoParamDAS loadInfoParamdas;
	private TruncateParamDAS truncateParamdas;

	public InsertParamDAS getInsertParamdas() {
		return insertParamdas;
	}

	public void setInsertParamdas(InsertParamDAS insertParamdas) {
		this.insertParamdas = insertParamdas;
	}

	public ImportExcelParamDAS getImportExcelParamdas() {
		return importExcelParamdas;
	}

	public void setImportExcelParamdas(ImportExcelParamDAS importExcelParamdas) {
		this.importExcelParamdas = importExcelParamdas;
	}

	public LoadInfoParamDAS getLoadInfoParamdas() {
		return loadInfoParamdas;
	}

	public void setLoadInfoParamdas(LoadInfoParamDAS loadInfoParamdas) {
		this.loadInfoParamdas = loadInfoParamdas;
	}

	public TruncateParamDAS getTruncateParamdas() {
		return truncateParamdas;
	}

	public void setTruncateParamdas(TruncateParamDAS truncateParamdas) {
		this.truncateParamdas = truncateParamdas;
	}

	// TODO ouvrir l'Excel qu'une seule fois - voir avec EDR
	@Override
	public void initParam(Progression progression) throws IOException {
		HashMap<String, ExcelParameters> paramMap = loadInfoParamdas.parameters();
		long start = System.currentTimeMillis();

		// Charge les tables de parametres - a lancer a chaque fois
		progression.setStep(ProgressionStep.CHARGEMENT);

		for (String mapKey : paramMap.keySet()) {
			LOG.info("Table {}", mapKey);
			GenericExcelData excelData = importExcelParamdas.importExcel(paramMap.get(mapKey), mapKey);
			truncateParamdas.truncateParam(mapKey);
			insertParamdas.insert(excelData, mapKey, paramMap.get(mapKey));

		}

		// on traite de facon specifique l'evolution des couts
		// pour les systemes de chauffage
		GenericExcelData evolCout = evolutionCouts();
		truncateParamdas.truncateParam("Evolution_couts");
		insertParamdas.insert(evolCout, "Evolution_couts", null);
		// pour le bati
		GenericExcelData evolCoutBati = evolutionCoutsBati();
		truncateParamdas.truncateParam("Evolution_couts_bati");
		insertParamdas.insert(evolCoutBati, "Evolution_couts_bati", null);

		long end = System.currentTimeMillis();
		LOG.info("creation time : {}ms", end - start);
	}

	protected GenericExcelData evolutionCouts() throws IOException {
		ExcelParameters param = choixEvolutionCouts();
		GenericExcelData excelData = importExcelParamdas.importExcel(param, "Evolution_couts");

		GenericExcelData newData = new GenericExcelData();

		ArrayList<String> nameNewData = new ArrayList<>();
		nameNewData.add(0, "SYS_CHAUFF");
		nameNewData.add(1, "ANNEE");
		nameNewData.add(2, "EVOLUTION");

		ArrayList<ArrayList<Object>> newDataLine = new ArrayList<>();

		if (param.getFline() == 9) {
			// utilisation de la formule

			for (ArrayList<Object> nameLine : excelData.getListe()) {
				String nomSysChauf = (String) nameLine.get(0);
				BigDecimal courbure = (BigDecimal) nameLine.get(1);
				BigDecimal reduction = (BigDecimal) nameLine.get(2);

				for (int annee = 2010; annee < 2051; annee++) {
					ArrayList<Object> ligneRes = new ArrayList<>();
					ligneRes.add(0, nomSysChauf);
					ligneRes.add(1, annee);
					ligneRes.add(2, evolCoutFormule(annee, courbure, reduction));

					newDataLine.add(ligneRes);
				}

			}

		} else {
			// utilisation du tableau
			for (ArrayList<Object> nameLine : excelData.getListe()) {
				String nomSysChauf = (String) nameLine.get(0);
				for (int annee = 2010; annee < 2051; annee++) {
					ArrayList<Object> ligneRes = new ArrayList<>();
					ligneRes.add(0, nomSysChauf);
					ligneRes.add(1, annee);
					ligneRes.add(2, nameLine.get(annee - 2010 + 1));

					newDataLine.add(ligneRes);
				}

			}
		}
		newData.setNames(nameNewData);
		newData.setListe(newDataLine);
		return newData;

	}

	protected ExcelParameters choixEvolutionCouts() throws IOException {
		InputStream ExcelFileToUpdate = new FileInputStream("./Tables_param/Parametres_utilisateurs.xls");
		HSSFWorkbook wb = new HSSFWorkbook(ExcelFileToUpdate);
		HSSFSheet sheet = wb.getSheet("Evolution_couts_systemes");

		int i = 3;
		int c = 2;
		HSSFCell cell = sheet.getRow(i).getCell(c);

		double choix = cell.getNumericCellValue();

		ExcelParameters param = new ExcelParameters();

		param.setFilename("./Tables_param/Parametres_utilisateurs.xls");
		param.setSheetname("Evolution_couts_systemes");

		if (choix == 1) {
			// utilisation de la formule
			param.setFline(9);
			param.setFcolumn(1);

		} else {
			// utilisation du tableau
			param.setFline(32);
			param.setFcolumn(2);

		}

		return param;
	}

	protected BigDecimal evolCoutFormule(int annee, BigDecimal courbure, BigDecimal reduction) {

		BigDecimal result = BigDecimal.ONE;
		BigDecimal inter = (BigDecimal.valueOf(annee).subtract(BigDecimal.valueOf(2010)))
				.divide(BigDecimal.valueOf(40));
		// oblige de passer par des double
		BigDecimal inter2 = BigDecimal.valueOf(Math.log(1 + Math.pow(inter.doubleValue(), courbure.doubleValue())
				* (Math.E - 1)));
		result = result.add(inter2.multiply(reduction, MathContext.DECIMAL32), MathContext.DECIMAL32);

		return result;
	}

	protected GenericExcelData evolutionCoutsBati() throws IOException {
		ExcelParameters param = choixEvolutionCoutsBati();
		GenericExcelData excelData = importExcelParamdas.importExcel(param, "Evolution_couts_bati");

		GenericExcelData newData = new GenericExcelData();

		ArrayList<String> nameNewData = new ArrayList<>();
		nameNewData.add(0, "CODE");
		nameNewData.add(1, "ANNEE");
		nameNewData.add(2, "EVOLUTION");

		ArrayList<ArrayList<Object>> newDataLine = new ArrayList<>();

		if (param.getFline() == 9) {
			// utilisation de la formule

			for (ArrayList<Object> nameLine : excelData.getListe()) {
				String code = (String) nameLine.get(0);
				BigDecimal courbure = (BigDecimal) nameLine.get(1);
				BigDecimal reduction = (BigDecimal) nameLine.get(2);

				for (int annee = 2010; annee < 2051; annee++) {
					ArrayList<Object> ligneRes = new ArrayList<>();
					ligneRes.add(0, code);
					ligneRes.add(1, annee);
					ligneRes.add(2, evolCoutFormule(annee, courbure, reduction));

					newDataLine.add(ligneRes);
				}

			}

		} else {
			// utilisation du tableau
			for (ArrayList<Object> nameLine : excelData.getListe()) {
				String code = (String) nameLine.get(0);
				for (int annee = 2010; annee < 2051; annee++) {
					ArrayList<Object> ligneRes = new ArrayList<>();
					ligneRes.add(0, code);
					ligneRes.add(1, annee);
					ligneRes.add(2, nameLine.get(annee - 2010 + 1));

					newDataLine.add(ligneRes);
				}

			}
		}
		newData.setNames(nameNewData);
		newData.setListe(newDataLine);
		return newData;

	}

	protected ExcelParameters choixEvolutionCoutsBati() throws IOException {
		InputStream ExcelFileToUpdate = new FileInputStream("./Tables_param/Parametres_utilisateurs.xls");
		HSSFWorkbook wb = new HSSFWorkbook(ExcelFileToUpdate);
		HSSFSheet sheet = wb.getSheet("Evolution_couts_bati");

		int i = 3;
		int c = 2;
		HSSFCell cell = sheet.getRow(i).getCell(c);

		double choix = cell.getNumericCellValue();

		ExcelParameters param = new ExcelParameters();

		param.setFilename("./Tables_param/Parametres_utilisateurs.xls");
		param.setSheetname("Evolution_couts_bati");

		if (choix == 1) {
			// utilisation de la formule
			param.setFline(9);
			param.setFcolumn(3);

		} else {
			// utilisation du tableau
			param.setFline(20);
			param.setFcolumn(3);

		}

		return param;
	}
}
