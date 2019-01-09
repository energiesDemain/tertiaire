package com.ed.cgdd.derby.model;

import java.math.BigDecimal;

public class CalibParameters {
	
	// annee de calibration (prix des energies)
	public static final Integer YEAR_CALIB=2009;
	
	public static final BigDecimal TAUX_ACTU_CALIB = new BigDecimal("0.04");
	
	// param surcout fuel switch en % du cout d'investissement
		public final static boolean  checkSurcoutFuelSwitch = true;
		public final static BigDecimal FacteurFuelCentrElec = new BigDecimal("0");
		public final static BigDecimal FacteurFuelElecCentr = new BigDecimal("0");
	
    // param surcout systemes electriques (facteur multiplicatif du cout d'investissement)
		public final static boolean  checkSurcoutSyst = false; 
//		public final static BigDecimal FacteurCINVelecJoule = new BigDecimal("0.4");
//		public final static BigDecimal FacteurCINVelecPAC = new BigDecimal("0.6");
//		public final static BigDecimal FacteurCINVGaz = new BigDecimal("1.4");
//		public final static BigDecimal FacteurCINVGazPerf = new BigDecimal("1.4");
//		
		public final static BigDecimal FacteurCINVelecJoule = new BigDecimal("1");
		public final static BigDecimal FacteurCINVelecPAC = new BigDecimal("1");
		public final static BigDecimal FacteurCINVGaz = new BigDecimal("1");
		public final static BigDecimal FacteurCINVGazPerf = new BigDecimal("1");
		
	// param surcout ENSBBC (facteur multiplicatif des couts)
		public final static boolean  checkSurcoutENSBBC = false;
		public final static BigDecimal FacteurENSBBC = new BigDecimal("1");
		public final static boolean  checkSurcoutENSMOD =false;
		public final static BigDecimal FacteurENSMOD = new BigDecimal("1");
		public final static boolean  checkSurcoutall = false;
		public final static BigDecimal Facteurall = new BigDecimal("1");
		
	// lambda cout intangible ne rien faire (pourcentage de reduction des charges initiales)
		public final static BigDecimal LambdaNRF = new BigDecimal("1");
	
//	// lambda par branche 2050
//		public final static BigDecimal LambdaNRF01 = new BigDecimal("1");
//		public final static BigDecimal LambdaNRF02 = new BigDecimal("1");
//		public final static BigDecimal LambdaNRF03 = new BigDecimal("1");
//		public final static BigDecimal LambdaNRF04 = new BigDecimal("1");
//		public final static BigDecimal LambdaNRF05 = new BigDecimal("1");
//		public final static BigDecimal LambdaNRF06 = new BigDecimal("1");
//		public final static BigDecimal LambdaNRF07 = new BigDecimal("1");
//		public final static BigDecimal LambdaNRF08 = new BigDecimal("1");
		
//		
//////		// lambda par branche 2050 AMS-AME 2017
//		public final static BigDecimal LambdaNRF01 = new BigDecimal("0.4");
//		public final static BigDecimal LambdaNRF02 = new BigDecimal("0.7");
//		public final static BigDecimal LambdaNRF03 = new BigDecimal("0.7");
//		public final static BigDecimal LambdaNRF04 = new BigDecimal("0.7");
//		public final static BigDecimal LambdaNRF05 = new BigDecimal("0.8");
//		public final static BigDecimal LambdaNRF06 = new BigDecimal("0.5");
//		public final static BigDecimal LambdaNRF07 = new BigDecimal("0.65");
//		public final static BigDecimal LambdaNRF08 = new BigDecimal("0.65");
////				
////		// lambda par branche debut de periode
//		public final static BigDecimal LambdaNRF01Debut  =  new BigDecimal("0.2");
//		public final static BigDecimal LambdaNRF02Debut  = new BigDecimal("0.7");
//		public final static BigDecimal LambdaNRF03Debut  = new BigDecimal("0.7");
//		public final static BigDecimal LambdaNRF04Debut  = new BigDecimal("0.7");
//		public final static BigDecimal LambdaNRF05Debut  = new BigDecimal("0.8");
//		public final static BigDecimal LambdaNRF06Debut  = new BigDecimal("0.3");
//		public final static BigDecimal LambdaNRF07Debut  = new BigDecimal("0.6");
//		public final static BigDecimal LambdaNRF08Debut  = new BigDecimal("0.6");
//		
//		
	//// lambda analyse de sensib	INV - 20 %	
	// lambda par branche 2050 AMS-AME 2017
	public final static BigDecimal LambdaNRF01 = new BigDecimal("0.32");
	public final static BigDecimal LambdaNRF02 = new BigDecimal("0.56");
	public final static BigDecimal LambdaNRF03 = new BigDecimal("0.56");
	public final static BigDecimal LambdaNRF04 = new BigDecimal("0.56");
	public final static BigDecimal LambdaNRF05 = new BigDecimal("0.64");
	public final static BigDecimal LambdaNRF06 = new BigDecimal("0.4");
	public final static BigDecimal LambdaNRF07 = new BigDecimal("0.52");
	public final static BigDecimal LambdaNRF08 = new BigDecimal("0.52");
//			
//	// lambda par branche debut de periode
	public final static BigDecimal LambdaNRF01Debut  =  new BigDecimal("0.2");
	public final static BigDecimal LambdaNRF02Debut  = new BigDecimal("0.56");
	public final static BigDecimal LambdaNRF03Debut  = new BigDecimal("0.56");
	public final static BigDecimal LambdaNRF04Debut  = new BigDecimal("0.56");
	public final static BigDecimal LambdaNRF05Debut  = new BigDecimal("0.64");
	public final static BigDecimal LambdaNRF06Debut  = new BigDecimal("0.3");
	public final static BigDecimal LambdaNRF07Debut  = new BigDecimal("0.52");
	public final static BigDecimal LambdaNRF08Debut  = new BigDecimal("0.52");

		
		
//// lambda analyse de sensib
//////	// lambda par branche 2050
//	public final static BigDecimal LambdaNRF01 = new BigDecimal("0.2");
//	public final static BigDecimal LambdaNRF02 = new BigDecimal("0.35");
//	public final static BigDecimal LambdaNRF03 = new BigDecimal("0.35");
//	public final static BigDecimal LambdaNRF04 = new BigDecimal("0.35");
//	public final static BigDecimal LambdaNRF05 = new BigDecimal("0.4");
//	public final static BigDecimal LambdaNRF06 = new BigDecimal("0.3");
//	public final static BigDecimal LambdaNRF07 = new BigDecimal("0.3");
//	public final static BigDecimal LambdaNRF08 = new BigDecimal("0.3");
////			
////	// lambda par branche debut de periode
//	public final static BigDecimal LambdaNRF01Debut  =  new BigDecimal("0.1");
//	public final static BigDecimal LambdaNRF02Debut  = new BigDecimal("0.35");
//	public final static BigDecimal LambdaNRF03Debut  = new BigDecimal("0.35");
//	public final static BigDecimal LambdaNRF04Debut  = new BigDecimal("0.35");
//	public final static BigDecimal LambdaNRF05Debut  = new BigDecimal("0.4");
//	public final static BigDecimal LambdaNRF06Debut  = new BigDecimal("0.15");
//	public final static BigDecimal LambdaNRF07Debut  = new BigDecimal("0.3");
//	public final static BigDecimal LambdaNRF08Debut  = new BigDecimal("0.3");
		
//	//// lambda analyse de sensib2
////////	// lambda par branche 2050
//		public final static BigDecimal LambdaNRF01 = new BigDecimal("0.32");
//		public final static BigDecimal LambdaNRF02 = new BigDecimal("0.56");
//		public final static BigDecimal LambdaNRF03 = new BigDecimal("0.56");
//		public final static BigDecimal LambdaNRF04 = new BigDecimal("0.56");
//		public final static BigDecimal LambdaNRF05 = new BigDecimal("0.64");
//		public final static BigDecimal LambdaNRF06 = new BigDecimal("0.4");
//		public final static BigDecimal LambdaNRF07 = new BigDecimal("0.48");
//		public final static BigDecimal LambdaNRF08 = new BigDecimal("0.48");
////				
////		// lambda par branche debut de periode
//		public final static BigDecimal LambdaNRF01Debut  =  new BigDecimal("0.16");
//		public final static BigDecimal LambdaNRF02Debut  = new BigDecimal("0.56");
//		public final static BigDecimal LambdaNRF03Debut  = new BigDecimal("0.56");
//		public final static BigDecimal LambdaNRF04Debut  = new BigDecimal("0.56");
//		public final static BigDecimal LambdaNRF05Debut  = new BigDecimal("0.64");
//		public final static BigDecimal LambdaNRF06Debut  = new BigDecimal("0.24");
//		public final static BigDecimal LambdaNRF07Debut  = new BigDecimal("0.48");
//		public final static BigDecimal LambdaNRF08Debut  = new BigDecimal("0.48");
//		
		
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
//		public final static BigDecimal CalageParcChauffElec 	= new BigDecimal("1.0420273");
//		public final static BigDecimal CalageParcChauffGaz 		= new BigDecimal("0.9924076");
//		public final static BigDecimal CalageParcChauffFioul 	= new BigDecimal("0.9203859");
//		public final static BigDecimal CalageParcChauffUrbain 	= new BigDecimal("1.1050800");
//		public final static BigDecimal CalageParcChauffAutres 	= new BigDecimal("1.1050800");
//		 
		public final static BigDecimal CalageParcChauffElec 	= new BigDecimal("1.0419581");
		public final static BigDecimal CalageParcChauffGaz 		= new BigDecimal("0.9996030");
		public final static BigDecimal CalageParcChauffFioul 	= new BigDecimal("0.9226443");
		public final static BigDecimal CalageParcChauffUrbain 	= new BigDecimal("1.0602076");
		public final static BigDecimal CalageParcChauffAutres 	= new BigDecimal("1.0602076");
		
//		public final static BigDecimal CalageParcChauffElec 	= new BigDecimal("1");
//		public final static BigDecimal CalageParcChauffGaz 		= new BigDecimal("1");
//		public final static BigDecimal CalageParcChauffFioul 	= new BigDecimal("1");
//		public final static BigDecimal CalageParcChauffUrbain 	= new BigDecimal("1");
//		public final static BigDecimal CalageParcChauffAutres 	= new BigDecimal("1");
//				
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

	// param recalage conso Elec specifique hors clim AME
	//	public final static BigDecimal CalageConsoElecspe 	= new BigDecimal("1.28");
		public final static BigDecimal CalageConsoElecspe 	= new BigDecimal("1");
		
	// param recalage conso Elec hors chauffage et elec spe
	//	public final static BigDecimal CalageConsoElecAutres	= new BigDecimal("1.0245238");
		public final static BigDecimal CalageConsoElecAutres	= new BigDecimal("1");
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
