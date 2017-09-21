package com.ed.cgdd.derby.initialize.impl;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.jdbc.core.JdbcTemplate;

import com.ed.cgdd.derby.initialize.ImportExcelDAS;
import com.ed.cgdd.derby.model.excelobjects.ExcelParameters;
import com.ed.cgdd.derby.model.excelobjects.GenericExcelData;

public class ImportExcelDASImpl implements ImportExcelDAS {
	private JdbcTemplate jdbcTemplate;

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	// Importer depuis un excel

	@Override
	public GenericExcelData importExcel(ExcelParameters param) throws IOException {
		GenericExcelData data = new GenericExcelData();

		ArrayList<ArrayList<Object>> recup = new ArrayList<ArrayList<Object>>();
		InputStream excelFileToUpdate = new FileInputStream(getPath(param.getFilename()));
		XSSFWorkbook wb = new XSSFWorkbook(excelFileToUpdate);
		// Get first sheet from the workbook
		XSSFSheet sheet = wb.getSheet(param.getSheetname());

		// recup liste donnees
		data.setNames(getNames(sheet, param));

		int i = param.getFline();
		int c = param.getFcolumn() - 1;
		XSSFCell firstCell = sheet.getRow(i).getCell(c);
		while (firstCell != null && !firstCell.toString().isEmpty()) {
			ArrayList<Object> line = new ArrayList<Object>();
			line.add(getObject(firstCell));
			for (int j = c + 1; j - c < data.getNames().size(); j++) {
				XSSFCell cell = sheet.getRow(i).getCell(j);
				line.add(getObject(cell));
			}
			recup.add(line);
			i++;
			XSSFRow row = sheet.getRow(i);
			if (row != null) {
				firstCell = row.getCell(c);
			} else {
				firstCell = null;
			}
		}
		data.setListe(recup);

		excelFileToUpdate.close();
		return data;
	}

	private Object getObject(XSSFCell cell) {
		int type = cell.getCellType();
		if (type == XSSFCell.CELL_TYPE_STRING) {
			return cell.getStringCellValue();
		} else if (type == XSSFCell.CELL_TYPE_NUMERIC) {
			double number = cell.getNumericCellValue();
			return new BigDecimal(String.valueOf(number));
		} else {
			return null;
		}
	}

	protected ArrayList<String> getNames(XSSFSheet sheet, ExcelParameters params) {
		XSSFCell cell;
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

	protected String getPath(String filename) {
		String path = new String();
		StringBuffer buffer = new StringBuffer();

		buffer.append("./Tables_init/");
		buffer.append(filename);
		path = buffer.toString();
		return path;
	}
}
