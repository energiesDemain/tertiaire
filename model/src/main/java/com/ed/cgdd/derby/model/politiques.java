package com.ed.cgdd.derby.model;
import java.math.BigDecimal;
import java.util.HashMap;

public class  politiques {
	
	// Script d'activation de certaines politiques publiques 
	// mettre true pour activer puis modifier les parametres pour la mesure
	
	//1) individualisation des frais de chauffage
	public final static boolean checkIFC=true;
	//public final static BigDecimal GainBU_2017 = new BigDecimal("0.993625");
	//public final static BigDecimal GainBU_2018 = new BigDecimal("0.98725");
	//public final static BigDecimal GainBU_2019 = new BigDecimal("0.980875");
	
	// Indiquer ici le gain annuel sur le besoin de chauffage en % sur 2017 2018 et 2019 (periode d'equipement du parc)  
	public final static BigDecimal GainBU_IFC_annuel = new BigDecimal("-0.006375");

	//2) RT existant 2018 (hausse de la performance minimale des gestes et des systemes de chauffage)
	public final static boolean checkRTex=true;

	// Hausse du gain (en point de pourcentage) des gestes sur le bati
	public final static BigDecimal GainSupRTex = new BigDecimal("0.06");
	// Hausse du cout (en pourcentage) des gestes sur le bati
	public final static BigDecimal CoutSupRTex = new BigDecimal("0.09");
	
	// Hausse du rendement (en pourcentage) des systemes de chauffage autres que electrique joule
	public final static BigDecimal GainRdtSupRTex = new BigDecimal("0.05");
	// Hausse du cout (en pourcentage) des systemes de chauffage autres que electrique joule
	public final static BigDecimal CoutRdtSupRTex = new BigDecimal("0.075");
	
	// Hausse du rendement (en pourcentage) des systemes de chauffage  electrique joule
	public final static BigDecimal GainRdtSupRTexElecJoule = new BigDecimal("0.05");
	// Hausse du cout (en pourcentage) des systemes de chauffage electrique joule
	public final static BigDecimal CoutRdtSupRTexElecJoule = new BigDecimal("0.075");
	
	// 3) batiment exemplaire de l'Etat (amelioration de la performance des batiments neufs de l'Etat à partir de 2017)
	public final static boolean  checkBatex = true;
	// facteur multiplicatif des besoins unitaires des usages RT des batiments entrants de l'Etat 
	public final static BigDecimal modifBUBatEx = new BigDecimal("0.8325");

	// 4) travaux embarques (obligation de renover de faire des travaux de renovation energetique 
	// si on fait d'importants travaux sur un batiment type ravalement)
	// Ne pas toucher, j'active cette mesure dans le fichier de parametre maintenant en la modelisant comme une obligation de renovation
	// (vois parametres AME et AMS)
	public final static boolean  checkTravEmb =false;
	// taux de renovation tendanciel supplementaire
	//public static float  txRenovTravEmb = 0.011f;
	public static float  txRenovTravEmb = 0.0000001f;
	
	
	// 5) surcout RT 2012 electrique joule (surcout pour prendre en compte les couts d'isolation additionnels pour les batiments
	// chauffes à l'electricite joule du fait de la RT 2012)
	public final static boolean  checkSurcoutRT2012 = true;
	// surcout en euros par m2 pour l'electrique joule
	public final static BigDecimal surcoutRT = new BigDecimal("20");
		
	// 6) CEE modelisation avec des prix annee par annee pour la periode 2015 2016
	// il y a differente valeurs commentes selon le scenario
	public final static boolean  checkCEEannuels =true;
	
	// prix 2015-2020 en euros par kwhcumac (valeur de la subvention accordee par kwhcumac)
	public final static BigDecimal pCEE2015 = new BigDecimal("0.000");
	public final static BigDecimal pCEE2016 = new BigDecimal("0.005");
	public final static BigDecimal pCEE2017 = new BigDecimal("0.005");
	public final static BigDecimal pCEE2018 = new BigDecimal("0.006");
	public final static BigDecimal pCEE2019 = new BigDecimal("0.006");
	public final static BigDecimal pCEE2020 = new BigDecimal("0.006");
	
	// prix initial et taux de croissance des prix apres 2020, mettre a zero pour arreter les CEE. ICI = valeur AMS
	public final static BigDecimal pCEEsup2020 = new BigDecimal("0.006");
	public final static BigDecimal tcamCEEsup2020 = new BigDecimal("0.012");
	public final static BigDecimal tcamCEEsup2030 = new BigDecimal("0.07");


//// prix initial et taux de croissance des prix apres 2020, mettre a zero pour arreter les CEE. ICI = valeur AME
//		public final static BigDecimal pCEEsup2020 = new BigDecimal("0");
//		public final static BigDecimal tcamCEEsup2020 = new BigDecimal("0");
//		public final static BigDecimal tcamCEEsup2030 = new BigDecimal("0");

	// couts intangibles subventionnes (ancienne version du modele)
	public final static boolean  checkCEECINT =true;
	
	// Subvention aux economies d energie actualisees (ancienne version du modele)
	public final static boolean  checkSubEcoEner = false;
		
	// TODO faire une hasmap annee, pcee;
	public HashMap<Integer, BigDecimal> pCEE = new HashMap<Integer, BigDecimal>();

	
	
	// 7) Adaptation au CC (baisse du besoin unitaire de chauffage et hausse du besoin unitaire de climatisation du fait de la hausse 
	// de la temperature exterieure
	// (TODO faire une Hasmap des tcam des besoins)
	public final static boolean  checkAdaptationCC = true;
	// taux de croissance annuels moyens des besoins de chauffage par periode
	//	public final static BigDecimal tcamBesoinChauff20152020 = new BigDecimal("-0.001068949531");
	public final static BigDecimal tcamBesoinChauff20152020 = new BigDecimal("0");
	public final static BigDecimal tcamBesoinChauff20202025  = new BigDecimal("-0.001074693510");
	public final static BigDecimal tcamBesoinChauff20252030  = new BigDecimal("-0.0010804995531");
	public final static BigDecimal tcamBesoinChauff20302050  = new BigDecimal("-0.001914505186");

	// taux de croissance annuels moyens des besoin de climatisation par periode
	//public final static BigDecimal tcamBesoinClim20152020 = new BigDecimal("0.007874988518");
	public final static BigDecimal tcamBesoinClim20152020 = new BigDecimal("0");
	public final static BigDecimal tcamBesoinClim20202025  = new BigDecimal("0.007576624052");
	public final static BigDecimal tcamBesoinClim20252030  = new BigDecimal("0.007300045195");
	public final static BigDecimal tcamBesoinClim20302050  = new BigDecimal("0.006304199098");

	
	// 8) MESURE AMS : baisse du besoin unitaire en ECS (amelioration des equipements, mitigeur)
	public final static boolean checkBaisseBesoinECS =true;
	// taux de croissance annuel moyen des besoin en ECS
	public final static BigDecimal tcamBesoinECS20152050 = new BigDecimal("-0.0063552495"); 

	
	
	
}
