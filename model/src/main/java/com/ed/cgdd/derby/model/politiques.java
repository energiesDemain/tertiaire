package com.ed.cgdd.derby.model;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;

public class  politiques {
	
	// Activation politiques publiques
	// individualisation des frais de chauffage
	public final static boolean checkIFC=true;
	//public final static BigDecimal GainBU_2017 = new BigDecimal("0.993625");
	//public final static BigDecimal GainBU_2018 = new BigDecimal("0.98725");
	//public final static BigDecimal GainBU_2019 = new BigDecimal("0.980875");
	public final static BigDecimal GainBU_IFC_annuel = new BigDecimal("-0.006375");

	// RT existant 2018
	public final static boolean checkRTex=true;

	public final static BigDecimal GainSupRTex = new BigDecimal("0.06");
	public final static BigDecimal CoutSupRTex = new BigDecimal("0.09");
	
	public final static BigDecimal GainRdtSupRTex = new BigDecimal("0.10");
	public final static BigDecimal CoutRdtSupRTex = new BigDecimal("0.15");
	
	public final static BigDecimal GainRdtSupRTexElecJoule = new BigDecimal("0.05");
	public final static BigDecimal CoutRdtSupRTexElecJoule = new BigDecimal("0.075");
	
	// batiment exemplaire
	public final static boolean  checkBatex = true;
	public final static BigDecimal modifBUBatEx = new BigDecimal("0.8325");

	//travaux embarques
	public final static boolean  checkTravEmb =  true;
	// taux de renovation tendanciel supplementaire
	public static float  txRenovTravEmb = 0.011f;

	//surcout RT 2012 electrique direct
	public final static boolean  checkSurcoutRT2012 = true;
	// surcout en euros par m2 pour l'electrique joule
	public final static BigDecimal surcoutRT = new BigDecimal("20") ;
		
	// CEE prix annuels
	
	public final static boolean  checkCEEannuels = true;
//	public final static BigDecimal pCEE2015 = new BigDecimal("0.030");
//	public final static BigDecimal pCEE2016 = new BigDecimal("0.030");
//	public final static BigDecimal pCEE2017 = new BigDecimal("0.030");
//	public final static BigDecimal pCEE2018 = new BigDecimal("0.100");
//	public final static BigDecimal pCEE2019 = new BigDecimal("0.100");
//	public final static BigDecimal pCEE2020 = new BigDecimal("0.100");
	
//	valeur où on atteint 23 Twh en 2021 avec CINT subventionnés
	public final static BigDecimal pCEE2015 = new BigDecimal("0.070");
	public final static BigDecimal pCEE2016 = new BigDecimal("0.070");
	public final static BigDecimal pCEE2017 = new BigDecimal("0.100");
	public final static BigDecimal pCEE2018 = new BigDecimal("0.200");
	public final static BigDecimal pCEE2019 = new BigDecimal("0.300");
	public final static BigDecimal pCEE2020 = new BigDecimal("0.400");
//	
//	public final static BigDecimal pCEE2015 = new BigDecimal("0.053");
//	public final static BigDecimal pCEE2016 = new BigDecimal("0.090");
//	public final static BigDecimal pCEE2017 = new BigDecimal("0.100");
//	public final static BigDecimal pCEE2018 = new BigDecimal("0.110");
//	public final static BigDecimal pCEE2019 = new BigDecimal("0.140");
//	public final static BigDecimal pCEE2020 = new BigDecimal("0.180");
	
	// couts intangibles subventionnes 
	public final static boolean  checkCEECINT = true;
	
	// TODO faire une hasmap annee, pcee;
	public HashMap<Integer, BigDecimal> pCEE = new HashMap<Integer, BigDecimal>();

	// Adaptation CC TODO faire une Hasmap des tcam des besoins
	public final static boolean  checkAdaptationCC = true;
	// taux de croissance annuels moyens des besoin de chauffage par periode
	public final static BigDecimal tcamBesoinChauff20152020 = new BigDecimal("-0.001068949531");
	public final static BigDecimal tcamBesoinChauff20202025  = new BigDecimal("-0.001074693510");
	public final static BigDecimal tcamBesoinChauff20252030  = new BigDecimal("-0.0010804995531");
	public final static BigDecimal tcamBesoinChauff20302050  = new BigDecimal("-0.001914505186");

	// taux de croissance annuels moyens des besoin de climatisation par periode
	public final static BigDecimal tcamBesoinClim20152020 = new BigDecimal("0.007874988518");
	public final static BigDecimal tcamBesoinClim20202025  = new BigDecimal("0.007576624052");
	public final static BigDecimal tcamBesoinClim20252030  = new BigDecimal("0.007300045195");
	public final static BigDecimal tcamBesoinClim20302050  = new BigDecimal("0.006304199098");
	
	
}
