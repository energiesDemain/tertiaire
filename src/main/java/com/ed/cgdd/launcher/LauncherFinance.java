package com.ed.cgdd.launcher;

import java.io.IOException;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.ed.cgdd.derby.finance.FinanceService;

public class LauncherFinance {

	private final static Logger LOG = LogManager.getLogger(LauncherProcess.class);

	public static void main(String[] args) throws SQLException, IOException {
		LOG.info("Start engine");

		ApplicationContext context = new ClassPathXmlApplicationContext("applicationContextProcess.xml");

		FinanceService serviceParam = (FinanceService) context.getBean("financeService");

		LOG.info("End engine");

	}
}
