package com.ed.cgdd.derby.model;

import java.math.BigDecimal;

public class CalibParameters {
	
	// annee de calibration (prix des energies), choix du prix des energies pour la calibration (NE PAS TOUCHER)
	public static final Integer YEAR_CALIB=2009;
	// choix du taux d'actualisation pour la calibration (NE PAS TOUCHER)
	public static final BigDecimal TAUX_ACTU_CALIB = new BigDecimal("0.04");
	
	// Ci dessous un ensemble de parametres sur les couts d'investissements qui permettent de modifier la calibration 
	// des parts de marche des systemes et le niveau d'investissement
	// Utile pour faire des tests de sensibilite surtout sinon ne pas modifier
	
	// parametre surcout du fait du changement d'energie % du cout d'investissement
		public final static boolean  checkSurcoutFuelSwitch = true;
		// surcout si on passe d'un systeme centralise (gaz, fioul...) a decentralise (elec...)
		public final static BigDecimal FacteurFuelCentrElec = new BigDecimal("0");
		// surcout si on passe d'un decentralise (elec...) à un systeme centralise (gaz, fioul...)
		public final static BigDecimal FacteurFuelElecCentr = new BigDecimal("0");
	
    // parametre surcout systemes electriques (facteur multiplicatif du cout d'investissement)
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
		
	// parametre surcout ENSBBC (facteur multiplicatif des couts des gestes)
		public final static boolean  checkSurcoutENSBBC = false;
		public final static BigDecimal FacteurENSBBC = new BigDecimal("1");
	// parametre surcout ENSMOD (facteur multiplicatif des couts des gestes)
		public final static boolean  checkSurcoutENSMOD =false;
		public final static BigDecimal FacteurENSMOD = new BigDecimal("1");
	// parametre surcout pour tous les gestes sur le bati (facteur multiplicatif des couts des gestes)
		public final static boolean  checkSurcoutall = false;
		public final static BigDecimal Facteurall = new BigDecimal("1");

	// CALIBRATION DU NIVEAU D'INVESTISSEMENT DANS LA RENOVATION DU BATI
	// REMPLACE LES COUTS INTANGIBLES
	// les parametres lambda correspondent a des facteurs multiplicatifs du cout global du geste NE RIEN FAIRE
	// Plus on diminue le Lambda, plus il est interessant de ne rien faire cad ne pas investir
	// on peut ainsi controler le niveau d'investissement sur les annees initiales
	
		
	// lambda cout intangible ne rien faire (pourcentage de reduction des charges initiales) 
	// pour l'ensemble du parc
	// NE PAS TOUCHER laisser a 1
		public final static BigDecimal LambdaNRF = new BigDecimal("1");
	
	// ICI on parametre le lambda pour chaque branche du parc
	// on donne une valeur initiale pour 2010 et une valeur finale pour 2050
	// la valeur finale doit etre superieur a la valeur initiale
	// le lambda croit ensuite lineairement entre la valeur initiale et la valeur finale
	// mettre la meme valeur initiale et finale si on veut un lambda constant
		
	// VALEURS POUR LES SCENARIOS DGEC AMS-AME 2017	 (A GARDER AU CAS OU)
		
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
////		// lambda par branche debut de periode AMS-AME 2017
//		public final static BigDecimal LambdaNRF01Debut  =  new BigDecimal("0.2");
//		public final static BigDecimal LambdaNRF02Debut  = new BigDecimal("0.7");
//		public final static BigDecimal LambdaNRF03Debut  = new BigDecimal("0.7");
//		public final static BigDecimal LambdaNRF04Debut  = new BigDecimal("0.7");
//		public final static BigDecimal LambdaNRF05Debut  = new BigDecimal("0.8");
//		public final static BigDecimal LambdaNRF06Debut  = new BigDecimal("0.3");
//		public final static BigDecimal LambdaNRF07Debut  = new BigDecimal("0.6");
//		public final static BigDecimal LambdaNRF08Debut  = new BigDecimal("0.6");
//		
		
		// VALEURS POUR LA VERSION CALIBREE SUR LES ETUDES CODA STRATEGIES
			
////	// lambda par branche 2050 calibration sur CODA 
	public final static BigDecimal LambdaNRF01 = new BigDecimal("0.4");
	public final static BigDecimal LambdaNRF02 = new BigDecimal("0.85");
	public final static BigDecimal LambdaNRF03 = new BigDecimal("0.7");
	public final static BigDecimal LambdaNRF04 = new BigDecimal("0.7");
	public final static BigDecimal LambdaNRF05 = new BigDecimal("0.8");
	public final static BigDecimal LambdaNRF06 = new BigDecimal("0.5");
	public final static BigDecimal LambdaNRF07 = new BigDecimal("0.85");
	public final static BigDecimal LambdaNRF08 = new BigDecimal("0.65");
//			
//	// lambda par branche debut de periode calibration sur CODA 
	public final static BigDecimal LambdaNRF01Debut  =  new BigDecimal("0.33");
	public final static BigDecimal LambdaNRF02Debut  = new BigDecimal("0.85");
	public final static BigDecimal LambdaNRF03Debut  = new BigDecimal("0.68");
	public final static BigDecimal LambdaNRF04Debut  = new BigDecimal("0.68");
	public final static BigDecimal LambdaNRF05Debut  = new BigDecimal("0.58");
	public final static BigDecimal LambdaNRF06Debut  = new BigDecimal("0.37");
	public final static BigDecimal LambdaNRF07Debut  = new BigDecimal("0.85");
	public final static BigDecimal LambdaNRF08Debut  = new BigDecimal("0.6");
			
		
	// VALEURS POUR  analyse de sensibilité sur le niveau d'investissement

//	// lambda par branche 2050 AMS-AME 2017
//	public final static BigDecimal LambdaNRF01 = new BigDecimal("0.25");
//	public final static BigDecimal LambdaNRF02 = new BigDecimal("0.60");
//	public final static BigDecimal LambdaNRF03 = new BigDecimal("0.55");
//	public final static BigDecimal LambdaNRF04 = new BigDecimal("0.55");
//	public final static BigDecimal LambdaNRF05 = new BigDecimal("0.60");
//	public final static BigDecimal LambdaNRF06 = new BigDecimal("0.35");
//	public final static BigDecimal LambdaNRF07 = new BigDecimal("0.60");
//	public final static BigDecimal LambdaNRF08 = new BigDecimal("0.40");
////			
////	// lambda par branche debut de periode
//	public final static BigDecimal LambdaNRF01Debut  =  new BigDecimal("0.25");
//	public final static BigDecimal LambdaNRF02Debut  = new BigDecimal("0.60");
//	public final static BigDecimal LambdaNRF03Debut  = new BigDecimal("0.55");
//	public final static BigDecimal LambdaNRF04Debut  = new BigDecimal("0.55");
//	public final static BigDecimal LambdaNRF05Debut  = new BigDecimal("0.60");
//	public final static BigDecimal LambdaNRF06Debut  = new BigDecimal("0.35");
//	public final static BigDecimal LambdaNRF07Debut  = new BigDecimal("0.60");
//	public final static BigDecimal LambdaNRF08Debut  = new BigDecimal("0.40");
//
//	
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
		
	// parametres de recalage des surfaces du parc par branche en fonction du CEREN 2010 
	// il s'agit ici de facteurs multiplicatifs des surfaces par branche en 2010 pour retrouver les surfaces du CEREN
	// METTRE 1 pour enlever le recalage
		public final static BigDecimal CalageBranche01 = new BigDecimal("1.0239862");
		public final static BigDecimal CalageBranche02 = new BigDecimal("1.0420552");
		public final static BigDecimal CalageBranche03 = new BigDecimal("1.0163286");
		public final static BigDecimal CalageBranche04 = new BigDecimal("1.0069953");
		public final static BigDecimal CalageBranche05 = new BigDecimal("1.1043235");
		public final static BigDecimal CalageBranche06 = new BigDecimal("1.0193524");
		public final static BigDecimal CalageBranche07 = new BigDecimal("0.9779685");
		public final static BigDecimal CalageBranche08 = new BigDecimal("0.9979434");
		
	// parametres de recalage du parc par énergie de chauffage en fonction du CEREN 2010 
	// il s'agit ici de facteurs multiplicatifs des surfaces par branche en 2010 pour retrouver les surfaces du CEREN
	// METTRE 1 pour enlever le recalage
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

	// parametres de recalage des consommations d'electricite specifique hors clim
	// il s'agit ici de facteurs multiplicatifs des consommations d'electricite specifique hors clim
	// METTRE 1 pour enlever le recalage
	// NE PAS MODIFIER, utile pour une ancienne version du modele, laisser a 1
		
	// param recalage conso Elec specifique hors clim AME
	//	public final static BigDecimal CalageConsoElecspe 	= new BigDecimal("1.28");
		public final static BigDecimal CalageConsoElecspe 	= new BigDecimal("1");
		
	// parametres de recalage des consommations d'electricite hors chauffage et elec spe
	// il s'agit ici de facteurs multiplicatifs des consommations d'electricite hors chauffage et elec spe
	// METTRE 1 pour enlever le recalage
	// NE PAS MODIFIER, utile pour une ancienne version du modele, laisser a 1
				
	//	public final static BigDecimal CalageConsoElecAutres	= new BigDecimal("1.0245238");
		public final static BigDecimal CalageConsoElecAutres	= new BigDecimal("1");

	// parametres de recalage des consommations de chauffage par energie
	// il s'agit ici de facteurs multiplicatifs des consommations de chauffage par energie
	// METTRE 1 pour enlever le recalage
	// NE PAS MODIFIER, utile pour une ancienne version du modele, laisser a 1
			
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
