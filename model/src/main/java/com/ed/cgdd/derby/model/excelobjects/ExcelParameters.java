package com.ed.cgdd.derby.model.excelobjects;

public class ExcelParameters {

	private String filename;

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getSheetname() {
		return sheetname;
	}

	public void setSheetname(String sheetname) {
		this.sheetname = sheetname;
	}

	public int getFline() {
		return fline;
	}

	public void setFline(int fline) {
		this.fline = fline;
	}

	public int getFcolumn() {
		return fcolumn;
	}

	public void setFcolumn(int fcolumn) {
		this.fcolumn = fcolumn;
	}

	private String sheetname;
	private int fline;
	private int fcolumn;

}
