package com.ed.cgdd.launcher;

import java.io.IOException;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import com.ed.cgdd.derby.excelresult.*;
import com.ed.cgdd.derby.model.progression.ProgressionStep;


public class LauncherTableResult {
	public static final boolean checkXlsX = true;
	public static final boolean csvCheck = true;

	private final static Logger LOG = LogManager.getLogger(LauncherTableResult.class);

	public static void main(String[] args) throws SQLException, IOException {
		LOG.info("Start engine");

		ApplicationContext context = new ClassPathXmlApplicationContext("applicationContextProcess.xml");
		int pasdeTempsInit = 1;
		if(csvCheck){
			CSVService csvService = (CSVService)  context.getBean("csvService");
			csvService.csvService(pasdeTempsInit);
		} else {
		if(checkXlsX){
			ExcelXCoutsService excelXCoutService = (ExcelXCoutsService) context.getBean("excelXCoutsService");
			ExcelXResultService excelXResultService = (ExcelXResultService) context.getBean("excelXResultService");
			ExcelXEtiquetteService excelXEtiquetteService = (ExcelXEtiquetteService) context.getBean("excelXEtiquetteService");

			// TODO Open and close file only once
			
			excelXResultService.excelXService(1, false);
			excelXCoutService.excelXService(1, false);
			excelXEtiquetteService.excelXService(1, false);
			
		} else {
		
			ExcelCoutsService excelCoutService = (ExcelCoutsService) context.getBean("excelCoutsService");
			ExcelResultService excelResultService = (ExcelResultService) context.getBean("excelResultService");
			ExcelEtiquetteService excelEtiquetteService = (ExcelEtiquetteService) context.getBean("excelEtiquetteService");
	
			// TODO Open and close file only once
			
			excelResultService.excelService(1, false);
			excelCoutService.excelService(1, false);
			excelEtiquetteService.excelService(1, false);
		}
		// excelCoutService.getContributionClimat(coutEnergieMap);
		}
		LOG.info("End engine");

	}
}
