package com.ed.cgdd.derby.model;

import java.math.BigDecimal;

public class CalibParameters {
	
	// annee de calibration (prix des energies)
	public static final Integer YEAR_CALIB=2009;
	
	public static final BigDecimal TAUX_ACTU_CALIB = new BigDecimal("0.04");
	
	// param surcout fuel switch en % du cout d'investissement
		public final static boolean  checkSurcoutFuelSwitch = true;
		public final static BigDecimal FacteurFuelCentrElec = new BigDecimal("1");
		public final static BigDecimal FacteurFuelElecCentr = new BigDecimal("1");
		
	// param recalage du parc par branche en fonction du CEREN 2010 
		public final static BigDecimal CalageBranche01 = new BigDecimal("1.0239862");
		public final static BigDecimal CalageBranche02 = new BigDecimal("1.0420552");
		public final static BigDecimal CalageBranche03 = new BigDecimal("1.0163286");
		public final static BigDecimal CalageBranche04 = new BigDecimal("1.0069953");
		public final static BigDecimal CalageBranche05 = new BigDecimal("1.1043235");
		public final static BigDecimal CalageBranche06 = new BigDecimal("1.0193524");
		public final static BigDecimal CalageBranche07 = new BigDecimal("0.9779685");
		public final static BigDecimal CalageBranche08 = new BigDecimal("0.9979434");
		
	// param recalage du parc par énergie de chauffage en fonction du CEREN 2010 
		public final static BigDecimal CalageParcChauffElec 	= new BigDecimal("1.0420273");
		public final static BigDecimal CalageParcChauffGaz 		= new BigDecimal("0.9924076");
		public final static BigDecimal CalageParcChauffFioul 	= new BigDecimal("0.9203859");
		public final static BigDecimal CalageParcChauffUrbain 	= new BigDecimal("1.1050800");
		public final static BigDecimal CalageParcChauffAutres 	= new BigDecimal("1.1050800");
		
		
//	// param recalage du parc par énergie de chauffage en fonction du CEREN 2010 
//		public final static BigDecimal CalageParcChauffElec 	= new BigDecimal("1.0649036");
//		public final static BigDecimal CalageParcChauffGaz 		= new BigDecimal("0.9970268");
//		public final static BigDecimal CalageParcChauffFioul 	= new BigDecimal("0.9065154");
//		public final static BigDecimal CalageParcChauffUrbain 	= new BigDecimal("1.0547371");
//		public final static BigDecimal CalageParcChauffAutres 	= new BigDecimal("1.0547371");
//			
////	// param recalage des besoins de chauffage  par énergie en fonction du CEREN 2010 
//		public final static BigDecimal CalageConsoChauffElec 	= new BigDecimal("1.0508951");
//		public final static BigDecimal CalageConsoChauffGaz 	= new BigDecimal("1.0016967");
//		public final static BigDecimal CalageConsoChauffFioul 	= new BigDecimal("0.9932186");
//		public final static BigDecimal CalageConsoChauffUrbain 	= new BigDecimal("0.9633484");
//		public final static BigDecimal CalageConsoChauffAutres 	= new BigDecimal("0.9633484");
//		

//		
	// param recalage conso Elec specifique hors clim AME
		public final static BigDecimal CalageConsoElecspe 	= new BigDecimal("1.28");
		
	// param recalage conso Elec hors chauffage et elec spe
		public final static BigDecimal CalageConsoElecAutres	= new BigDecimal("1.0245238");
//		 
	// Décommenter pour enlever le recalage 
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
		public final static  BigDecimal  CalageConsoChauffElec 	 = BigDecimal.ONE;
		public final static  BigDecimal  CalageConsoChauffGaz 	 = BigDecimal.ONE;
		public final static  BigDecimal  CalageConsoChauffFioul 	 = BigDecimal.ONE;
		public final static  BigDecimal  CalageConsoChauffUrbain  = BigDecimal.ONE;
		public final static  BigDecimal  CalageConsoChauffAutres  = BigDecimal.ONE;	
//		public final static  BigDecimal  CalageConsoHorsChauffElec 	 = BigDecimal.ONE;	
		
}
