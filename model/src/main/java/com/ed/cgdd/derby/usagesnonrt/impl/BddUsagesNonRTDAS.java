package com.ed.cgdd.derby.usagesnonrt.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BddUsagesNonRTDAS {
	private final static Logger LOG = LogManager.getLogger(BddUsagesNonRTDAS.class);
	private static Properties properties = null;

	public Properties getProperties() {
		return properties;
	}

	public void setProperties(Properties properties) {
		BddUsagesNonRTDAS.properties = properties;
	}

	protected String getProperty(String key) {
		initProperties();
		return BddUsagesNonRTDAS.properties.getProperty(key);
	}

	private void initProperties() {
		if (BddUsagesNonRTDAS.properties == null) {
			BddUsagesNonRTDAS.properties = new Properties();
			InputStream inputStream = this.getClass().getClassLoader()
					.getResourceAsStream("requestUsagesNonRT.properties");
			if (inputStream == null) {
				LOG.error("property file 'tables.properties' not found in the classpath");
			} else {
				try {
					BddUsagesNonRTDAS.properties.load(inputStream);
				} catch (IOException e) {
					LOG.error(e);
				}
			}
		}
	}

}
