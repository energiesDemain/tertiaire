package com.ed.cgdd.derby.calibrageCINT.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BddDAS {
	private final static Logger LOG = LogManager.getLogger(BddDAS.class);
	private static Properties properties = null;

	public Properties getProperties() {
		return properties;
	}

	public void setProperties(Properties properties) {
		BddDAS.properties = properties;
	}

	protected String getProperty(String key) {
		initProperties();
		return BddDAS.properties.getProperty(key);
	}

	private void initProperties() {
		if (BddDAS.properties == null) {
			BddDAS.properties = new Properties();
			InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("requestCINT.properties");
			if (inputStream == null) {
				LOG.error("property file 'tables.properties' not found in the classpath");
			} else {
				try {
					BddDAS.properties.load(inputStream);
				} catch (IOException e) {
					LOG.error(e);
				}
			}
		}
	}

}
