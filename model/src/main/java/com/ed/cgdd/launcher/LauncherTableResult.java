package com.ed.cgdd.launcher;

import java.io.IOException;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.ed.cgdd.derby.excelresult.ExcelCoutsService;
import com.ed.cgdd.derby.excelresult.ExcelEtiquetteService;
import com.ed.cgdd.derby.excelresult.ExcelResultService;

public class LauncherTableResult {

	private final static Logger LOG = LogManager.getLogger(LauncherTableResult.class);

	public static void main(String[] args) throws SQLException, IOException {
		LOG.info("Start engine");

		ApplicationContext context = new ClassPathXmlApplicationContext("applicationContextProcess.xml");

		ExcelCoutsService excelCoutService = (ExcelCoutsService) context.getBean("excelCoutsService");
		ExcelResultService excelResultService = (ExcelResultService) context.getBean("excelResultService");
		ExcelEtiquetteService excelEtiquetteService = (ExcelEtiquetteService) context.getBean("excelEtiquetteService");

		// TODO Open and close file only once

		excelResultService.excelService(1, false);
		excelCoutService.excelService(1, false);
		excelEtiquetteService.excelService(1, false);

		// excelCoutService.getContributionClimat(coutEnergieMap);

		LOG.info("End engine");

	}
}
