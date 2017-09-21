package com.ed.cgdd.derby.usagesrt.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BddUsagesRTDAS {
	private final static Logger LOG = LogManager.getLogger(BddUsagesRTDAS.class);
	private static Properties properties = null;

	public Properties getProperties() {
		return properties;
	}

	public void setProperties(Properties properties) {
		BddUsagesRTDAS.properties = properties;
	}

	protected String getProperty(String key) {
		initProperties();
		return BddUsagesRTDAS.properties.getProperty(key);
	}

	private void initProperties() {
		if (BddUsagesRTDAS.properties == null) {
			BddUsagesRTDAS.properties = new Properties();
			InputStream inputStream = this.getClass().getClassLoader()
					.getResourceAsStream("requestUsagesRT.properties");
			if (inputStream == null) {
				LOG.error("property file 'tables.properties' not found in the classpath");
			} else {
				try {
					BddUsagesRTDAS.properties.load(inputStream);
				} catch (IOException e) {
					LOG.error(e);
				}
			}
		}
	}

}
