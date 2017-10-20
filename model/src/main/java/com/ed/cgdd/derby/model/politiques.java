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
	public final static BigDecimal GainBU_IFC_annuel = new BigDecimal("-0,006375");

	
	// RT existant 2018
	public final static boolean checkRTex=false;

	public final static BigDecimal GainSupRTex = new BigDecimal("0.1");
	public final static BigDecimal GainRdtSupRTex = new BigDecimal("0.05");

	// batiment exemplaire
	public final static boolean  checkBatex = false;
	public final static BigDecimal modifBUBatEx = new BigDecimal("0.8325");
	
	//travaux embarques
	public final static boolean  checkTravEmb = false;
	// taux de renovation tendanciel supplementaire
	public static float  txRenovTravEmb = 0.013f;

	//surcout RT 2012 electrique direct
	public final static boolean  checkSurcoutRT2012 = true;
	
	// CEE prix annuels
	public final static boolean  checkCEEannuels = false;
	public final static BigDecimal pCEE2015 = new BigDecimal("0.008");
	public final static BigDecimal pCEE2016 = new BigDecimal("0.008");
	public final static BigDecimal pCEE2017 = new BigDecimal("0.008");
	public final static BigDecimal pCEE2018 = new BigDecimal("0.015");
	public final static BigDecimal pCEE2019 = new BigDecimal("0.015");
	public final static BigDecimal pCEE2020 = new BigDecimal("0.015");
	// TODO faire une hasmap annee, pcee;
	public HashMap<Integer, BigDecimal> pCEE = new HashMap<Integer, BigDecimal>();

	// surcout en euros par m2 pour l'electrique joule
	public final static BigDecimal surcoutRT = new BigDecimal("15") ;
	
	// Adaptation CC
	public final static boolean  checkAdaptationCC = true;
	public final static BigDecimal tcamBesoinChauff = new BigDecimal("-0.01");
	public final static BigDecimal tcamBesoinClim = new BigDecimal("0.01");
	
}
