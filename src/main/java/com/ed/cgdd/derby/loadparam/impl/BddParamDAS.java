package com.ed.cgdd.derby.loadparam.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BddParamDAS {
	private final static Logger LOG = LogManager.getLogger(BddParamDAS.class);
	private static Properties properties = null;

	public Properties getProperties() {
		return properties;
	}

	public void setProperties(Properties properties) {
		BddParamDAS.properties = properties;
	}

	protected String getProperty(String key) {
		initProperties();
		return BddParamDAS.properties.getProperty(key);
	}

	private void initProperties() {
		if (BddParamDAS.properties == null) {
			BddParamDAS.properties = new Properties();
			InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("requestparam.properties");
			if (inputStream == null) {
				LOG.error("property file 'tables.properties' not found in the classpath");
			} else {
				try {
					BddParamDAS.properties.load(inputStream);
				} catch (IOException e) {
					LOG.error(e);
				}
			}
		}
	}

}
