package com.ed.cgdd.derby.model;

import java.math.BigDecimal;

public class CalibParameters {

	// param surcout fuel switch en % du cout d'investissement
		public final static boolean  checkSurcoutFuelSwitch = true;
		public final static BigDecimal FacteurFuelCentrElec = new BigDecimal("1");
		public final static BigDecimal FacteurFuelElecCentr = new BigDecimal("1");
		
	// param recalage du parc par branche en fonction du CEREN 2010 
		public final static BigDecimal CalageBranche01 = new BigDecimal("1.0240165");
		public final static BigDecimal CalageBranche02 = new BigDecimal("1.0420448");
		public final static BigDecimal CalageBranche03 = new BigDecimal("1.0163465");
		public final static BigDecimal CalageBranche04 = new BigDecimal("1.0069852");
		public final static BigDecimal CalageBranche05 = new BigDecimal("1.1043125");
		public final static BigDecimal CalageBranche06 = new BigDecimal("1.0193301");
		public final static BigDecimal CalageBranche07 = new BigDecimal("0.9779587");
		public final static BigDecimal CalageBranche08 = new BigDecimal("0.9979335");
		
//	// param recalage du parc par énergie de chauffage en fonction du CEREN 2010 
		public final static BigDecimal CalageParcChauffElec 	= new BigDecimal("1.0649036");
		public final static BigDecimal CalageParcChauffGaz 		= new BigDecimal("0.9970268");
		public final static BigDecimal CalageParcChauffFioul 	= new BigDecimal("0.9065154");
		public final static BigDecimal CalageParcChauffUrbain 	= new BigDecimal("1.0547371");
		public final static BigDecimal CalageParcChauffAutres 	= new BigDecimal("1.0547371");
		
//	// param recalage des besoins de chauffage  par énergie en fonction du CEREN 2010 
		public final static BigDecimal CalageConsoChauffElec 	= new BigDecimal("1.0508951");
		public final static BigDecimal CalageConsoChauffGaz 	= new BigDecimal("1.0016967");
		public final static BigDecimal CalageConsoChauffFioul 	= new BigDecimal("0.9932186");
		public final static BigDecimal CalageConsoChauffUrbain 	= new BigDecimal("0.9633484");
		public final static BigDecimal CalageConsoChauffAutres 	= new BigDecimal("0.9633484");
		
	
	// enlever le recalage 
    //public final static boolean noCalage = false;
//		public final static  BigDecimal  CalageBranche01 = BigDecimal.ONE;
//		public final static  BigDecimal  CalageBranche02 = BigDecimal.ONE;
//		public final static  BigDecimal  CalageBranche03 = BigDecimal.ONE;
//		public final static  BigDecimal  CalageBranche04 = BigDecimal.ONE;
//		public final static  BigDecimal  CalageBranche05 = BigDecimal.ONE;
//		public final static  BigDecimal  CalageBranche06 = BigDecimal.ONE;
//		public final static  BigDecimal  CalageBranche07 = BigDecimal.ONE;
//		public final static  BigDecimal  CalageBranche08 = BigDecimal.ONE;
//		public final static  BigDecimal  CalageParcChauffElec 	 = BigDecimal.ONE;
//		public final static  BigDecimal  CalageParcChauffGaz 	 = BigDecimal.ONE;
//		public final static  BigDecimal  CalageParcChauffFioul 	 = BigDecimal.ONE;
//		public final static  BigDecimal  CalageParcChauffUrbain 	 = BigDecimal.ONE;
//		public final static  BigDecimal  CalageParcChauffAutres 	 = BigDecimal.ONE;
//		public final static  BigDecimal  CalageConsoChauffElec 	 = BigDecimal.ONE;
//		public final static  BigDecimal  CalageConsoChauffGaz 	 = BigDecimal.ONE;
//		public final static  BigDecimal  CalageConsoChauffFioul 	 = BigDecimal.ONE;
//		public final static  BigDecimal  CalageConsoChauffUrbain  = BigDecimal.ONE;
//		public final static  BigDecimal  CalageConsoChauffAutres  = BigDecimal.ONE;	
		
}
