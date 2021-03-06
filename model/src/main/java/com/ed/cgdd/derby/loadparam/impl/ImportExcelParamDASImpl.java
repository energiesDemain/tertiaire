package com.ed.cgdd.derby.loadparam.impl;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.jdbc.core.JdbcTemplate;

import com.ed.cgdd.derby.loadparam.ImportExcelParamDAS;
import com.ed.cgdd.derby.model.excelobjects.ExcelParameters;
import com.ed.cgdd.derby.model.excelobjects.GenericExcelData;

public class ImportExcelParamDASImpl implements ImportExcelParamDAS {
	private JdbcTemplate jdbcTemplate;

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	// Importer depuis un excel

	@Override
	public GenericExcelData importExcel(ExcelParameters param, String name) throws IOException {
		GenericExcelData data = new GenericExcelData();

		ArrayList<ArrayList<Object>> recup = new ArrayList<ArrayList<Object>>();
		InputStream ExcelFileToUpdate = new FileInputStream(param.getFilename());
		HSSFWorkbook wb = new HSSFWorkbook(ExcelFileToUpdate);
		// Get first sheet from the workbook
		HSSFSheet sheet = wb.getSheet(param.getSheetname());

		// recup liste donnees
		data.setNames(getNames(sheet, param));

		int i = param.getFline() + 1;
		int c = param.getFcolumn() - 1;
		HSSFCell firstCell = sheet.getRow(i).getCell(c);
		while (firstCell != null && !firstCell.toString().isEmpty()) {
			ArrayList<Object> line = new ArrayList<Object>();
			if (name.equals("Gains_Froid_Alim_Rglt") || name.equals("Rythme_Froid_Alim_Rglt")) {
				Object value = convertPeriod(getObject(firstCell));
				line.add(value);
			} else {
				line.add(getObject(firstCell));
			}
			for (int j = c + 1; j - c < data.getNames().size(); j++) {
				HSSFCell cell = sheet.getRow(i).getCell(j);
				line.add(getObject(cell));
			}
			recup.add(line);
			i++;
			HSSFRow row = sheet.getRow(i);
			if (row != null) {
				firstCell = row.getCell(c);
			} else {
				firstCell = null;
			}
		}
		data.setListe(recup);

		ExcelFileToUpdate.close();
		return data;
	}

	protected Object convertPeriod(Object cellValue) {
		Object result = new Object();
		if (cellValue.toString().equals("2010-2015")) {
			result = "PERIODE1";
		} else if (cellValue.toString().equals("2015-2020")) {
			result = "PERIODE2";
		} else if (cellValue.toString().equals("2020-2030")) {
			result = "PERIODE3";
		} else if (cellValue.toString().equals("2030-2040")) {
			result = "PERIODE4";
		} else if (cellValue.toString().equals("2040-2050")) {
			result = "PERIODE5";
		} else {
			result = cellValue;
		}
		return result;
	}

	private Object getObject(HSSFCell cell) {
		int type = cell.getCellType();
		if (type == HSSFCell.CELL_TYPE_STRING) {
			return cell.getStringCellValue();
		} else if (type == HSSFCell.CELL_TYPE_NUMERIC) {
			double number = cell.getNumericCellValue();
			return new BigDecimal(String.valueOf(number));
		} else {
			return null;
		}
	}

	protected ArrayList<String> getNames(HSSFSheet sheet, ExcelParameters params) {
		HSSFCell cell;
		ArrayList<String> names = new ArrayList<String>();
		cell = sheet.getRow(params.getFline() - 1).getCell(params.getFcolumn() - 1);
		int index = 1;
		while (cell != null && !cell.toString().isEmpty()) {
			names.add(cell.toString());
			cell = sheet.getRow(params.getFline() - 1).getCell(params.getFcolumn() - 1 + index);
			index++;
		}
		return names;
	}
}
