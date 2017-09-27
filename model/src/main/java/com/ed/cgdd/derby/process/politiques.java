package com.ed.cgdd.derby.process;
import java.math.BigDecimal;

public class  politiques {
	
	// Activation politiques publiques
	// individualisation des frais de chauffage
	public final static int checkIFC = 1;
	public static int getCheckifc() {
		return checkIFC;
	}

	// RT existant 2018
	public final static int  checkRTex = 0;
	public static int getCheckRTex() {
		return checkRTex;
	}

	// batiment exemplaire
	public final static int  checkBatex = 1;
	public static int getCheckBatex() {
		return checkBatex;
	}
	
	//travaux embarques
	public final static int  checkTravEmb = 1;
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
	public final static BigDecimal surcoutRT = new BigDecimal("20") ;

	
	
}
