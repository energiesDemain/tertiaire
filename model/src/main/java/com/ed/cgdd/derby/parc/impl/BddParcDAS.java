package com.ed.cgdd.derby.parc.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BddParcDAS {
	private final static Logger LOG = LogManager.getLogger(BddParcDAS.class);
	private static Properties properties = null;

	public Properties getProperties() {
		return properties;
	}

	public void setProperties(Properties properties) {
		BddParcDAS.properties = properties;
	}

	protected String getProperty(String key) {
		initProperties();
		return BddParcDAS.properties.getProperty(key);
	}

	private void initProperties() {
		if (BddParcDAS.properties == null) {
			BddParcDAS.properties = new Properties();
			InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("requestparc.properties");
			if (inputStream == null) {
				LOG.error("property file 'tables.properties' not found in the classpath");
			} else {
				try {
					BddParcDAS.properties.load(inputStream);
				} catch (IOException e) {
					LOG.error(e);
				}
			}
		}
	}

}
