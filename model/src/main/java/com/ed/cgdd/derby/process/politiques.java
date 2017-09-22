package com.ed.cgdd.derby.process;

public class  politiques {
	
	// Activation politiques publiques
	// individualisation des frais de chauffage
	public final static int checkIFC = 1;
	public static int getCheckifc() {
		return checkIFC;
	}

	// RT existant 2018
	public final static int  checkRTex = 1;
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
	
}
