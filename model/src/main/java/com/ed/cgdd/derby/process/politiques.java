package com.ed.cgdd.derby.process;
import java.math.BigDecimal;
import java.math.MathContext;

public class  politiques {
	
	// Activation politiques publiques
	// individualisation des frais de chauffage
	public final static int checkIFC = 0;
	public static int getCheckifc() {
		return checkIFC;
	}
	public final static BigDecimal GainBU_2017 = new BigDecimal("0.993625");
	public final static BigDecimal GainBU_2018 = new BigDecimal("0.98725");
	public final static BigDecimal GainBU_2019 = new BigDecimal("0.980875");
	
	
	// RT existant 2018
	public final static int  checkRTex =0;
	public static int getCheckRTex() {
		return checkRTex;
	}
	
	public final static BigDecimal GainSupRTex = new BigDecimal("0.1");
	public final static BigDecimal GainRdtSupRTex = new BigDecimal("0.05");

	// batiment exemplaire
	public final static int  checkBatex = 0;
	public static int getCheckBatex() {
		return checkBatex;
	}
	public final static BigDecimal modifBUBatEx = new BigDecimal("0.8325");
	
	//travaux embarques
	public final static int  checkTravEmb = 0;
	public static int getCheckTravEmb() {
		return checkTravEmb;
	}
	// taux de renovation tendanciel supplementaire
	public static float  txRenovTravEmb = 0.013f;

	//surcout RT 2012 electrique direct
	public final static int  checkSurcoutRT2012 = 1;
	public static int getCheckSurcoutRT2012() {
		return  checkSurcoutRT2012;
	}
	
	// surcout en euros par m2 pour l'electrique joule
	public final static BigDecimal surcoutRT = new BigDecimal("15") ;
	
	
}
