package com.ed.cgdd.launcher;

import java.io.IOException;
import java.sql.SQLException;


import com.ed.cgdd.derby.initialize.InitializeService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class LauncherInit {

	private final static Logger LOG = LogManager.getLogger(LauncherInit.class);

	public static void main(String[] args) throws SQLException, IOException {
		LOG.info("Start engine");

		ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");

		InitializeService service = (InitializeService) context.getBean("initializeService");

		// recuperation des parametres d'entrees dans le parc

		service.init();

		LOG.info("End engine");

	}
}
