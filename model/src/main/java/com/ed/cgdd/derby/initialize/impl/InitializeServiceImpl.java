package com.ed.cgdd.derby.initialize.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ed.cgdd.derby.initialize.CreateTableDAS;
import com.ed.cgdd.derby.initialize.DropTableDAS;
import com.ed.cgdd.derby.initialize.ImportExcelDAS;
import com.ed.cgdd.derby.initialize.InitializeService;
import com.ed.cgdd.derby.initialize.InsertDAS;
import com.ed.cgdd.derby.initialize.LoadInfoDAS;
import com.ed.cgdd.derby.initialize.PkeyDAS;
import com.ed.cgdd.derby.model.excelobjects.ExcelParameters;
import com.ed.cgdd.derby.model.excelobjects.GenericExcelData;

public class InitializeServiceImpl implements InitializeService {
	private final static Logger LOG = LogManager.getLogger(InitializeServiceImpl.class);
 
	public CreateTableDAS getCreatedas() {
		return createdas;
	}

	public void setCreatedas(CreateTableDAS createdas) {
		this.createdas = createdas;
	}

	public DropTableDAS getDropdas() {
		return dropdas;
	}

	public void setDropdas(DropTableDAS dropdas) {
		this.dropdas = dropdas;
	}

	public InsertDAS getInsertdas() {
		return insertdas;
	}

	public void setInsertdas(InsertDAS insertdas) {
		this.insertdas = insertdas;
	}

	public PkeyDAS getPkeydas() {
		return pkeydas;
	}

	public void setPkeydas(PkeyDAS pkeydas) {
		this.pkeydas = pkeydas;
	}

	public ImportExcelDAS getImportExceldas() {
		return importExceldas;
	}

	public void setImportExceldas(ImportExcelDAS importExceldas) {
		this.importExceldas = importExceldas;
	}

	private CreateTableDAS createdas;
	private DropTableDAS dropdas;
	private InsertDAS insertdas;
	private PkeyDAS pkeydas;
	private ImportExcelDAS importExceldas;
	private LoadInfoDAS loadInfodas;

	public LoadInfoDAS getLoadInfodas() {
		return loadInfodas;
	}

	public void setLoadInfodas(LoadInfoDAS loadInfodas) {
		this.loadInfodas = loadInfodas;
	}

	@Override
	public void init() throws IOException {
		HashMap<String, ExcelParameters> excelMap = loadInfodas.excelTables();

		long start = System.currentTimeMillis();

		// Charge les tables "inamovibles" - a ne lancer qu'une fois
		for (String mapkey : excelMap.keySet()) {
			
			LOG.info("{} - Begin", mapkey);
			dropdas.dropTable(mapkey);
			createdas.createTable(mapkey);
			GenericExcelData excelData = importExceldas.importExcel(excelMap.get(mapkey));
			insertdas.insert(excelData, mapkey, excelMap.get(mapkey));
			pkeydas.pkey(mapkey);
			LOG.info("{} - Done", mapkey);
		}

		ArrayList<String> newTablesListe = loadInfodas.newTables();
		for (int i = 0; i < newTablesListe.size(); i++) {
			String name = newTablesListe.get(i);
			dropdas.dropTable(name);
			createdas.createTable(name);
			pkeydas.pkey(name);
			LOG.info("Create {} - Done", name);
		}

		long end = System.currentTimeMillis();
		LOG.info("creation time : {}ms", end - start);
	}
}
