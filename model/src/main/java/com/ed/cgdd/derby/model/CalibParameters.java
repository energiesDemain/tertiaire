package com.ed.cgdd.derby.model;

import java.math.BigDecimal;

public class CalibParameters {

	// param surcout fuel switch en % du cout d'investissement
		public final static boolean  checkSurcoutFuelSwitch = true;
		public final static BigDecimal FacteurFuelCentrElec = new BigDecimal("2");
		public final static BigDecimal FacteurFuelElecCentr = new BigDecimal("2");
		
	// param recalage CEREN 2010 
		public final static BigDecimal CalageBranche01 = new BigDecimal("1.030164531");
		public final static BigDecimal CalageBranche02 = new BigDecimal("1.0427078006");
		public final static BigDecimal CalageBranche03 = new BigDecimal("1.0170808301");
		public final static BigDecimal CalageBranche04 = new BigDecimal("1.0076259587");
		public final static BigDecimal CalageBranche05 = new BigDecimal("1.1050151776");
		public final static BigDecimal CalageBranche06 = new BigDecimal("1.0184018233");
		public final static BigDecimal CalageBranche07 = new BigDecimal("0.9785809937");
		public final static BigDecimal CalageBranche08 = new BigDecimal("0.9985684362");
		
}
