package com.ed.cgdd.derby.loadparam.impl;

import java.io.IOException;
import java.util.HashMap;

import com.ed.cgdd.derby.loadparam.LoadInfoParamDAS;
import com.ed.cgdd.derby.model.excelobjects.ExcelParameters;

public class LoadInfoParamDASImpl implements LoadInfoParamDAS {

	// Remplissage des map avec les proprietes des parametres
	// a recuperer dans l'Excel (nom du fichier, de la feuille, premiere ligne
	// et premiere colonne du tableau de parametres)

	@Override
	public HashMap<String, ExcelParameters> parameters() throws IOException {
		HashMap<String, ExcelParameters> paramMap = new HashMap<String, ExcelParameters>();

		ExcelParameters param = new ExcelParameters();

		// Parametres des couts intangibles
		param.setFilename("./Tables_param/Parametres_utilisateurs.xls");
		param.setSheetname("Parametrage_couts_intangibles");
		param.setFline(4);
		param.setFcolumn(2);
		paramMap.put("Cint", param);

		// Parametres d'entrees de parc
		param = new ExcelParameters();
		param.setFilename("./Tables_param/Parametres_utilisateurs.xls");
		param.setSheetname("Evolution_parc");
		param.setFline(3);
		param.setFcolumn(1);
		paramMap.put("Entrees_parc", param);

		// Parametres de sorties de parc
		param = new ExcelParameters();
		param.setFilename("./Tables_param/Parametres_utilisateurs.xls");
		param.setSheetname("Evolution_parc");
		param.setFline(16);
		param.setFcolumn(1);
		paramMap.put("Sorties_parc", param);

		// Parametres financiers
		param = new ExcelParameters();
		param.setFilename("./Tables_param/Parametres_utilisateurs.xls");
		param.setSheetname("Outils_incitatifs");
		param.setFline(4);
		param.setFcolumn(2);
		paramMap.put("Parametres_financiers", param);

		// Parametres cout energies
		param = new ExcelParameters();
		param.setFilename("./Tables_param/Parametres_utilisateurs.xls");
		param.setSheetname("Energies");
		param.setFline(3);
		param.setFcolumn(2);
		paramMap.put("Cout_Energies", param);

		// Parametres taux actualisation
		param = new ExcelParameters();
		param.setFilename("./Tables_param/Parametres_utilisateurs.xls");
		param.setSheetname("Taux_actualisation");
		param.setFline(3);
		param.setFcolumn(1);
		paramMap.put("Taux_Actu", param);

		// Parametres repartition locataires / proprietaires
		param = new ExcelParameters();
		param.setFilename("./Tables_param/Parametres_utilisateurs.xls");
		param.setSheetname("Taux_actualisation");
		param.setFline(3);
		param.setFcolumn(8);
		paramMap.put("Proprio_Locataire", param);

		/*
		 * // liste de geste param = new ExcelParameters();
		 * param.setFilename("Bibli_geste_bati.xlsx");
		 * param.setSheetname("Geste_bati"); param.setFline(1);
		 * param.setFcolumn(1); paramMap.put("Exemple_Geste", param);
		 */

		// Parametres de gains des usages non reglementes
		param = new ExcelParameters();
		param.setFilename("./Tables_param/Parametres_utilisateurs.xls");
		param.setSheetname("Usages_autres");
		param.setFline(4);
		param.setFcolumn(1);
		paramMap.put("Gains_nonRT", param);

		// Parts de marche des energies dans les consommations de cuisson
		param = new ExcelParameters();
		param.setFilename("./Tables_param/Parametres_utilisateurs.xls");
		param.setSheetname("PM_batiments_entrants");
		param.setFline(4);
		param.setFcolumn(2);
		paramMap.put("PM_cuisson", param);

		// Parts de marche des energies dans les consommations de l'usage
		// "autre"
		param = new ExcelParameters();
		param.setFilename("./Tables_param/Parametres_utilisateurs.xls");
		param.setSheetname("PM_batiments_entrants");
		param.setFline(4);
		param.setFcolumn(11);
		paramMap.put("PM_autres", param);

		// Cycles de vie des systemes pour les usages hors chauffage,
		// climatisation et ECS
		param = new ExcelParameters();
		param.setFilename("./Tables_param/Parametres_utilisateurs.xls");
		param.setSheetname("Cycles_de_vie");
		param.setFline(3);
		param.setFcolumn(6);
		paramMap.put("DV_autres", param);

		// Consommations unitaires par usage pour les batiments
		// neufs
		param = new ExcelParameters();
		param.setFilename("./Tables_param/Parametres_utilisateurs.xls");
		param.setSheetname("Batiments_entrants");
		param.setFline(3);
		param.setFcolumn(2);
		paramMap.put("BesoinU_neuf", param);

		// Parts de marche des energies dans les consommations de cuisson lors
		// du renouvellement des systemes
		param = new ExcelParameters();
		param.setFilename("./Tables_param/Parametres_utilisateurs.xls");
		param.setSheetname("Usages_autres");
		param.setFline(4);
		param.setFcolumn(11);
		paramMap.put("PM_cuisson_chgt", param);

		// Parts de marche des energies dans les consommations de l'usage
		// "autre" lors du renouvellement des systemes
		param = new ExcelParameters();
		param.setFilename("./Tables_param/Parametres_utilisateurs.xls");
		param.setSheetname("Usages_autres");
		param.setFline(15);
		param.setFcolumn(11);
		paramMap.put("PM_autres_chgt", param);

		// Rythme de fermeture des meubles frigorifiques selon la reglementation
		// testee par l'utilisateur
		param = new ExcelParameters();
		param.setFilename("./Tables_param/Parametres_utilisateurs.xls");
		param.setSheetname("Usages_autres");
		param.setFline(26);
		param.setFcolumn(10);
		paramMap.put("Rythme_Froid_Alim_Rglt", param);

		// Gains obtenus apres fermeture des meubles frigorifiques selon la
		// reglementation
		// testee par l'utilisateur
		param = new ExcelParameters();
		param.setFilename("./Tables_param/Parametres_utilisateurs.xls");
		param.setSheetname("Usages_autres");
		param.setFline(26);
		param.setFcolumn(13);
		paramMap.put("Gains_Froid_Alim_Rglt", param);

		// Rendements moyens des systemes dans les batiments existants
		param = new ExcelParameters();
		param.setFilename("./Tables_param/Parametres_utilisateurs.xls");
		param.setSheetname("Usage_ECS");
		param.setFline(3);
		param.setFcolumn(1);
		paramMap.put("Rendement_ECS", param);

		// Rendements des systemes d'ECS performants
		param = new ExcelParameters();
		param.setFilename("./Tables_param/Parametres_utilisateurs.xls");
		param.setSheetname("Usage_ECS");
		param.setFline(15);
		param.setFcolumn(12);
		paramMap.put("Rendement_performant_ECS", param);

		// Part de marche des systemes performants d'ECS lors du renouvellement
		// dans les batiments anciens
		param = new ExcelParameters();
		param.setFilename("./Tables_param/Parametres_utilisateurs.xls");
		param.setSheetname("Usage_ECS");
		param.setFline(15);
		param.setFcolumn(18);
		paramMap.put("Part_sys_performants_ECS", param);

		// Part de marche des energies d'ECS dans les batiments neufs
		param = new ExcelParameters();
		param.setFilename("./Tables_param/Parametres_utilisateurs.xls");
		param.setSheetname("PM_batiments_entrants");
		param.setFline(17);
		param.setFcolumn(2);
		paramMap.put("PM_ECS_Neuf", param);

		// Parts de marche des energies dans les besoins d'ECS lors
		// du renouvellement des systemes
		param = new ExcelParameters();
		param.setFilename("./Tables_param/Parametres_utilisateurs.xls");
		param.setSheetname("Usage_ECS");
		param.setFline(3);
		param.setFcolumn(12);
		paramMap.put("PM_ECS_Chgt", param);

		// Taux de penetration des panneaux solaires thermiques dans les
		// surfaces neuves et existantes
		param = new ExcelParameters();
		param.setFilename("./Tables_param/Parametres_utilisateurs.xls");
		param.setSheetname("Usage_ECS");
		param.setFline(27);
		param.setFcolumn(12);
		paramMap.put("Solaire_ECS", param);

		// Taux de couverture du besoin en ECS annuel par les panneaux solaires
		// thermiques pour les batiments existants et neufs
		param = new ExcelParameters();
		param.setFilename("./Tables_param/Parametres_utilisateurs.xls");
		param.setSheetname("Usage_ECS");
		param.setFline(46);
		param.setFcolumn(13);
		paramMap.put("Tx_Couv_Solaire", param);

		// Cycles de vie des systemes pour l'ECS
		param = new ExcelParameters();
		param.setFilename("./Tables_param/Parametres_utilisateurs.xls");
		param.setSheetname("Cycles_de_vie");
		param.setFline(29);
		param.setFcolumn(6);
		paramMap.put("DV_ECS", param);

		// // Cycles de vie des systemes pour les systemes de climatisation
		// param = new ExcelParameters();
		// param.setFilename("./Tables_param/Parametres_utilisateurs.xls");
		// param.setSheetname("Cycles_de_vie");
		// // XXX probleme ici DV_CLIM renvoie vers une case vide
		// param.setFline(28);
		// param.setFcolumn(1);
		// paramMap.put("DV_Clim", param);

		// Taux d'evolution des taux de clim dans l'existant
		param = new ExcelParameters();
		param.setFilename("./Tables_param/Parametres_utilisateurs.xls");
		param.setSheetname("Usage_climatisation");
		param.setFline(3);
		param.setFcolumn(1);
		paramMap.put("Tx_Clim_Existant", param);

		// Taux d'evolution des taux de clim dans l'existant
		param = new ExcelParameters();
		param.setFilename("./Tables_param/Parametres_utilisateurs.xls");
		param.setSheetname("Usage_Climatisation");
		param.setFline(17);
		param.setFcolumn(1);
		paramMap.put("Tx_Clim_Neuf", param);

		// Parametres de gains de l'usage d'eclairage
		param = new ExcelParameters();
		param.setFilename("./Tables_param/Parametres_utilisateurs.xls");
		param.setSheetname("Usage_eclairage");
		param.setFline(3);
		param.setFcolumn(1);
		paramMap.put("Gains_eclairage", param);

		// Parametres de gains de l'usage de ventilation
		param = new ExcelParameters();
		param.setFilename("./Tables_param/Parametres_utilisateurs.xls");
		param.setSheetname("Usage_ventilation");
		param.setFline(3);
		param.setFcolumn(1);
		paramMap.put("Gains_ventilation", param);

		// Cycles de vie des systemes pour les systemes de chauffage
		param = new ExcelParameters();
		param.setFilename("./Tables_param/Parametres_utilisateurs.xls");
		param.setSheetname("Cycles_de_vie");
		param.setFline(3);
		param.setFcolumn(1);
		paramMap.put("DV_chauffage", param);

		// Cycles de vie des systemes pour les parois
		param = new ExcelParameters();
		param.setFilename("./Tables_param/Parametres_utilisateurs.xls");
		param.setSheetname("Cycles_de_vie");
		param.setFline(17);
		param.setFcolumn(7);
		paramMap.put("DV_parois", param);

		// Evolution des emissions de GES
		param = new ExcelParameters();
		param.setFilename("./Tables_param/Parametres_utilisateurs.xls");
		param.setSheetname("Taux_d'emission");
		param.setFline(3);
		param.setFcolumn(1);
		paramMap.put("Emissions", param);

		// RT existant
		param = new ExcelParameters();
		param.setFilename("./Tables_param/Parametres_utilisateurs.xls");
		param.setSheetname("Reglementations");
		param.setFline(3);
		param.setFcolumn(1);
		paramMap.put("Rt_existant", param);
		// variation des cout intangibles du fait de la "Valeur Verte"
		param = new ExcelParameters();
		param.setFilename("./Tables_param/Parametres_utilisateurs.xls");
		param.setSheetname("Taux_actualisation");
		param.setFline(27);
		param.setFcolumn(8);
		paramMap.put("Valeur_Verte", param);

		// Obligation de travaux (taux surfaciques)
		param = new ExcelParameters();
		param.setFilename("./Tables_param/Parametres_utilisateurs.xls");
		param.setSheetname("Reglementations");
		param.setFline(24);
		param.setFcolumn(1);
		paramMap.put("Obligation_travaux_Surf", param);

		// Obligation de travaux (exigence minimale)
		param = new ExcelParameters();
		param.setFilename("./Tables_param/Parametres_utilisateurs.xls");
		param.setSheetname("Reglementations");
		param.setFline(14);
		param.setFcolumn(1);
		paramMap.put("Obligation_travaux_Ex", param);

		// Decret de travaux
		param = new ExcelParameters();
		param.setFilename("./Tables_param/Parametres_utilisateurs.xls");
		param.setSheetname("Reglementations");
		param.setFline(35);
		param.setFcolumn(1);
		paramMap.put("Decret_travaux", param);

		// Effet Rebond
		param = new ExcelParameters();
		param.setFilename("./Tables_param/Parametres_utilisateurs.xls");
		param.setSheetname("Effet_Rebond");
		param.setFline(3);
		param.setFcolumn(1);
		paramMap.put("Effet_Rebond", param);

		// Couts de maintenance
		param = new ExcelParameters();
		param.setFilename("./Tables_param/Parametres_utilisateurs.xls");
		param.setSheetname("Maintenance_systeme");
		param.setFline(3);
		param.setFcolumn(1);
		paramMap.put("Maintenance", param);

		// Elasticite des besoins
		param = new ExcelParameters();
		param.setFilename("./Tables_param/Parametres_utilisateurs.xls");
		param.setSheetname("Elasticite_besoin");
		param.setFline(3);
		param.setFcolumn(1);
		paramMap.put("Elasticite_prix", param);

		// Evolution cout des technologies et du bati
		// traite dans loadParamService

		return paramMap;
	}

}
